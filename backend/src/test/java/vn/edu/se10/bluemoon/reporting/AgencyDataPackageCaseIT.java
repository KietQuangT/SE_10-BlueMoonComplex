package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageWriter;
import vn.edu.se10.bluemoon.reporting.application.CreatePackageRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageActionRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageDetail;
import vn.edu.se10.bluemoon.reporting.application.PackagePreviewResponse;

/**
 * Ca S12 trên MySQL thật: vòng đời đầy đủ gồm bàn giao, mốc hạn tải, snapshot, ràng buộc schema,
 * retention và tranh chấp đồng thời. Dùng {@code @SpringBootTest} thuần để dùng chung context đã cache
 * (không thêm connection pool); dữ liệu được commit nên dọn theo ID ở {@link #cleanUp()}.
 */
@SpringBootTest
class AgencyDataPackageCaseIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired AgencyDataPackageService service;

    private final List<Long> users = new ArrayList<>();
    private final List<Long> households = new ArrayList<>();
    private final List<Long> probeAuditIds = new ArrayList<>();
    private final String marker = UUID.randomUUID().toString().substring(0, 8);

    @AfterEach
    void cleanUp() {
        String u = ids(users);
        String h = ids(households);
        if (!probeAuditIds.isEmpty()) {
            jdbc.update("DELETE FROM audit_log WHERE id IN (" + ids(probeAuditIds) + ")");
        }
        if (!users.isEmpty()) {
            jdbc.update("DELETE FROM audit_log WHERE actor_user_id IN (" + u + ")");
            String pkgs = "SELECT id FROM (SELECT id FROM agency_data_packages WHERE created_by IN (" + u + ")) x";
            jdbc.update("DELETE FROM agency_data_package_contents WHERE package_id IN (" + pkgs + ")");
            jdbc.update("DELETE FROM agency_data_package_households WHERE package_id IN (" + pkgs + ")");
            jdbc.update("DELETE FROM agency_data_packages WHERE created_by IN (" + u + ")");
        }
        if (!households.isEmpty()) {
            jdbc.update("DELETE FROM residents WHERE household_id IN (" + h + ")");
            jdbc.update("DELETE FROM households WHERE id IN (" + h + ")");
        }
        if (!users.isEmpty()) {
            jdbc.update("DELETE FROM users WHERE id IN (" + u + ")");
        }
    }

    private static String ids(List<Long> list) {
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private CreatePackageRequest request(String tag, Long... householdIds) {
        return new CreatePackageRequest(List.of(householdIds), "CA_" + tag, "REQ-" + marker + "-" + tag, "LIC-" + marker + "-" + tag);
    }

    private static PackageActionRequest actionOf(PackageDetail d) {
        return new PackageActionRequest(d.version(), d.contentSha256());
    }

    private List<Map<String, Object>> auditOf(long packageId) {
        return jdbc.queryForList("SELECT action, actor_user_id, changes FROM audit_log "
            + "WHERE entity_type = 'AgencyDataPackage' AND entity_id = ? ORDER BY id", String.valueOf(packageId));
    }

    // ---- Vòng đời đầy đủ, bàn giao và audit ----

    @Test
    void fullLifecycleWithHandoffPersistsStateAndAuditsEveryStepWithoutPersonalData() {
        long creator = admin("c");
        long approver = admin("a");
        long hh = household("LC", "ACTIVE");
        resident(hh, "Họ Tên Bí Mật " + marker, "RESIDING");

        PackageDetail draft = service.createPackage(request("LC", hh), creator);
        PackageDetail approved = service.approvePackage(draft.id(), actionOf(draft), approver);
        byte[] file = service.downloadPackage(approved.id(), approver);
        assertNotNull(file);
        PackageDetail handed = service.handoffPackage(approved.id(), actionOf(approved), approver);

        assertEquals("APPROVED", handed.status());
        assertEquals(approver, handed.handedOffBy());
        assertNotNull(handed.handedOffAt());
        assertEquals(approved.version() + 1, handed.version());
        assertFalse(handed.canHandoff(), "Đã bàn giao thì không được bàn giao lần hai");

        Map<String, Object> row = jdbc.queryForMap(
            "SELECT handed_off_by, handed_off_at, version, status FROM agency_data_packages WHERE id = ?", handed.id());
        assertEquals(approver, ((Number) row.get("handed_off_by")).longValue());
        assertNotNull(row.get("handed_off_at"));
        assertEquals("APPROVED", row.get("status"));

        ConflictException dup = assertThrows(ConflictException.class,
            () -> service.handoffPackage(handed.id(), actionOf(handed), approver));
        assertEquals("PACKAGE_STATE_CONFLICT", dup.code());

        List<Map<String, Object>> audit = auditOf(draft.id());
        assertEquals(List.of("AGENCY_PACKAGE_CREATED", "AGENCY_PACKAGE_APPROVED",
            "AGENCY_PACKAGE_DOWNLOAD_AUTHORIZED", "AGENCY_PACKAGE_HANDED_OFF"),
            audit.stream().map(r -> (String) r.get("action")).toList());
        assertEquals(List.of(creator, approver, approver, approver),
            audit.stream().map(r -> ((Number) r.get("actor_user_id")).longValue()).toList());
        for (Map<String, Object> a : audit) {
            String changes = (String) a.get("changes");
            assertFalse(changes.contains("Bí Mật"), "Audit không được chứa họ tên cư dân");
            assertFalse(changes.toLowerCase().contains("fullname"));
            assertTrue(changes.contains("CA_LC"));
        }
    }

    @Test
    void revokeAfterHandoffIsAllowedAndBlocksFurtherDownload() {
        long creator = admin("c");
        long approver = admin("a");
        long hh = household("RV", "ACTIVE");
        resident(hh, "Cư dân RV", "RESIDING");

        PackageDetail draft = service.createPackage(request("RV", hh), creator);
        PackageDetail approved = service.approvePackage(draft.id(), actionOf(draft), approver);
        PackageDetail handed = service.handoffPackage(approved.id(), actionOf(approved), approver);
        PackageDetail revoked = service.revokePackage(handed.id(), actionOf(handed), approver);

        assertEquals("REVOKED", revoked.status());
        assertEquals(approver, revoked.revokedBy());
        assertFalse(revoked.canDownload());
        ConflictException ex = assertThrows(ConflictException.class, () -> service.downloadPackage(revoked.id(), approver));
        assertEquals("PACKAGE_STATE_CONFLICT", ex.code());
        assertEquals("AGENCY_PACKAGE_REVOKED", auditOf(draft.id()).get(auditOf(draft.id()).size() - 1).get("action"));
    }

    // ---- Mốc hạn tải trên DB thật ----

    @Test
    void downloadDeadlineIsSevenDaysAfterApprovalNormally() {
        long creator = admin("c");
        long approver = admin("a");
        long hh = household("D7", "ACTIVE");
        resident(hh, "Cư dân D7", "RESIDING");

        PackageDetail draft = service.createPackage(request("D7", hh), creator);
        PackageDetail approved = service.approvePackage(draft.id(), actionOf(draft), approver);

        assertEquals(Duration.ofDays(7), Duration.between(approved.approvedAt(), approved.downloadUntil()));
        assertTrue(approved.downloadUntil().isBefore(approved.contentDeleteAfter()));
        assertEquals(Duration.ofDays(30), Duration.between(approved.createdAt(), approved.contentDeleteAfter()));
    }

    @Test
    void downloadDeadlineIsCappedWhenContentDeletesSoonerThanSevenDays() {
        long creator = admin("c");
        long approver = admin("a");
        long hh = household("DC", "ACTIVE");
        resident(hh, "Cư dân DC", "RESIDING");

        PackageDetail draft = service.createPackage(request("DC", hh), creator);
        // Đẩy mốc xóa nội dung về còn 2 ngày nữa (gói đã tạo cách đây ~28 ngày).
        jdbc.update("UPDATE agency_data_packages SET created_at = ?, content_delete_after = ? WHERE id = ?",
            Timestamp.from(Instant.now().minus(28, ChronoUnit.DAYS)),
            Timestamp.from(Instant.now().plus(2, ChronoUnit.DAYS)), draft.id());

        PackageDetail approved = service.approvePackage(draft.id(), actionOf(draft), approver);

        assertEquals(approved.contentDeleteAfter(), approved.downloadUntil());
        assertTrue(Duration.between(approved.approvedAt(), approved.downloadUntil()).compareTo(Duration.ofDays(3)) < 0);
    }

    @Test
    void expiredDownloadWindowBlocksDownloadPreviewAndHandoffOnRealDb() {
        long creator = admin("c");
        long approver = admin("a");
        long hh = household("EX", "ACTIVE");
        resident(hh, "Cư dân EX", "RESIDING");

        PackageDetail draft = service.createPackage(request("EX", hh), creator);
        PackageDetail approved = service.approvePackage(draft.id(), actionOf(draft), approver);
        jdbc.update("UPDATE agency_data_packages SET download_until = ? WHERE id = ?",
            Timestamp.from(Instant.now().minusSeconds(1)), approved.id());

        assertEquals("PACKAGE_DOWNLOAD_EXPIRED",
            assertThrows(ConflictException.class, () -> service.downloadPackage(approved.id(), approver)).code());
        assertEquals("PACKAGE_DOWNLOAD_EXPIRED",
            assertThrows(ConflictException.class, () -> service.previewPackage(approved.id(), 0, 20, approver)).code());
        assertEquals("PACKAGE_DOWNLOAD_EXPIRED",
            assertThrows(ConflictException.class, () -> service.handoffPackage(approved.id(), actionOf(approved), approver)).code());
        assertFalse(service.getPackageDetail(approved.id(), approver).canDownload());
    }

    // ---- Snapshot: phạm vi, thứ tự, đếm, phân trang ----

    @Test
    void snapshotIncludesOnlyCurrentResidentsOrderedByApartmentAndCountsMatchWorkbook() throws Exception {
        long creator = admin("c");
        long hhB = household("SNB", "ACTIVE");
        long hhA = household("SNA", "ACTIVE");
        long outside = household("SNX", "ACTIVE");
        resident(hhB, "Cư dân B1", "RESIDING");
        resident(hhA, "Cư dân A1", "RESIDING");
        resident(hhA, "Cư dân A2", "TEMPORARILY_ABSENT");
        resident(hhA, "Cư dân đã chuyển đi", "MOVED_OUT");
        resident(outside, "Người ngoài phạm vi " + marker, "RESIDING");

        PackageDetail draft = service.createPackage(request("SN", hhB, hhA), creator);

        assertEquals(2, draft.householdCount());
        assertEquals(3, draft.residentCount());
        PackagePreviewResponse preview = service.previewPackage(draft.id(), 0, 50, creator);
        assertEquals(3, preview.totalElements());
        List<String> names = preview.content().stream().map(r -> r.fullName()).toList();
        assertFalse(names.contains("Cư dân đã chuyển đi"));
        assertFalse(names.stream().anyMatch(n -> n.contains("ngoài phạm vi")));
        // Mã căn hộ "SNA-..." đứng trước "SNB-..." dù hộ B được tạo trước.
        assertEquals("Cư dân A1", names.get(0));
        assertEquals("Cư dân A2", names.get(1));
        assertEquals("Cư dân B1", names.get(2));

        PackageDetail approved = service.approvePackage(draft.id(), actionOf(draft), admin("a"));
        byte[] file = service.downloadPackage(approved.id(), creator);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(file))) {
            Sheet sheet = wb.getSheet("CuTru");
            assertEquals("3", sheet.getRow(5).getCell(1).getStringCellValue());
            assertEquals("2", sheet.getRow(6).getCell(1).getStringCellValue());
            assertEquals("1", sheet.getRow(7).getCell(1).getStringCellValue());
            assertEquals(AgencyDataPackageWriter.computeSha256(file), approved.contentSha256());
        }
        long inDb = jdbc.queryForObject("SELECT residing_count + temporarily_absent_count FROM agency_data_packages WHERE id = ?",
            Long.class, draft.id());
        assertEquals(3L, inDb);
    }

    @Test
    void previewPagesThroughAllRowsWithoutOverlap() {
        long creator = admin("c");
        long hh = household("PG", "ACTIVE");
        for (int i = 1; i <= 25; i++) {
            resident(hh, String.format("Cư dân %02d", i), "RESIDING");
        }
        PackageDetail draft = service.createPackage(request("PG", hh), creator);

        PackagePreviewResponse p0 = service.previewPackage(draft.id(), 0, 10, creator);
        PackagePreviewResponse p2 = service.previewPackage(draft.id(), 2, 10, creator);
        PackagePreviewResponse p3 = service.previewPackage(draft.id(), 3, 10, creator);

        assertEquals(25, p0.totalElements());
        assertEquals(3, p0.totalPages());
        assertEquals(10, p0.content().size());
        assertEquals("Cư dân 01", p0.content().get(0).fullName());
        assertEquals(5, p2.content().size());
        assertEquals("Cư dân 21", p2.content().get(0).fullName());
        assertEquals("Cư dân 25", p2.content().get(4).fullName());
        assertTrue(p3.content().isEmpty());
    }

    @Test
    void householdDeactivatedAfterCreationDoesNotChangeTheFrozenSnapshot() {
        long creator = admin("c");
        long hh = household("FZ", "ACTIVE");
        resident(hh, "Cư dân FZ", "RESIDING");
        PackageDetail draft = service.createPackage(request("FZ", hh), creator);

        jdbc.update("UPDATE households SET status = 'INACTIVE' WHERE id = ?", hh);
        jdbc.update("DELETE FROM residents WHERE household_id = ?", hh);

        PackagePreviewResponse preview = service.previewPackage(draft.id(), 0, 20, creator);
        assertEquals(1, preview.totalElements());
        assertEquals("Cư dân FZ", preview.content().get(0).fullName());
        assertEquals(draft.contentSha256(), preview.contentSha256());
    }

    // ---- Ràng buộc schema (lớp bảo vệ cuối) ----

    @Test
    void schemaRejectsSelfApprovalInconsistentCountsBadStatusAndOversizeContent() {
        long creator = admin("c");
        long hh = household("SC", "ACTIVE");
        resident(hh, "Cư dân SC", "RESIDING");
        PackageDetail draft = service.createPackage(request("SC", hh), creator);

        assertViolates("ck_adp_approved_not_creator",
            "UPDATE agency_data_packages SET status = 'APPROVED', approved_by = created_by WHERE id = ?", draft.id());
        assertViolates("ck_adp_resident_count_sum",
            "UPDATE agency_data_packages SET resident_count = 99 WHERE id = ?", draft.id());
        assertViolates("ck_adp_status",
            "UPDATE agency_data_packages SET status = 'EXPIRED' WHERE id = ?", draft.id());
        assertViolates("ck_adpc_byte_length",
            "UPDATE agency_data_package_contents SET byte_length = 10485761 WHERE package_id = ?", draft.id());
        assertViolates("fk_adph_household",
            "INSERT INTO agency_data_package_households (package_id, household_id) VALUES (?, 987654321)", draft.id());
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM agency_data_packages WHERE id = ?", String.class, draft.id()));
    }

    private void assertViolates(String constraint, String sql, Object... args) {
        DataAccessException ex = assertThrows(DataAccessException.class, () -> jdbc.update(sql, args));
        assertTrue(String.valueOf(ex.getMostSpecificCause().getMessage()).contains(constraint),
            "Phải vi phạm " + constraint + " nhưng là: " + ex.getMostSpecificCause().getMessage());
    }

    // ---- Tranh chấp đồng thời ----

    @Test
    void concurrentApproveAndRejectOfTheSameDraftHaveExactlyOneWinner() throws Exception {
        long creator = admin("c");
        long approver = admin("a");
        long hh = household("CC", "ACTIVE");
        resident(hh, "Cư dân CC", "RESIDING");
        PackageDetail draft = service.createPackage(request("CC", hh), creator);

        List<Object> outcomes = race(
            () -> service.approvePackage(draft.id(), actionOf(draft), approver),
            () -> service.rejectPackage(draft.id(), actionOf(draft), creator));

        long successes = outcomes.stream().filter(o -> o instanceof PackageDetail).count();
        long conflicts = outcomes.stream().filter(o -> o instanceof ConflictException).count();
        assertEquals(1, successes, "Đúng một thao tác thắng: " + outcomes);
        assertEquals(1, conflicts);
        String finalStatus = jdbc.queryForObject("SELECT status FROM agency_data_packages WHERE id = ?", String.class, draft.id());
        assertTrue(List.of("APPROVED", "REJECTED").contains(finalStatus));
        List<String> decisions = auditOf(draft.id()).stream().map(r -> (String) r.get("action"))
            .filter(a -> a.equals("AGENCY_PACKAGE_APPROVED") || a.equals("AGENCY_PACKAGE_REJECTED")).toList();
        assertEquals(1, decisions.size(), "Chỉ một audit quyết định, không có audit của thao tác thua");
    }

    @Test
    void twoAdminsApprovingConcurrentlyYieldSingleApproval() throws Exception {
        long creator = admin("c");
        long approverOne = admin("a1");
        long approverTwo = admin("a2");
        long hh = household("C2", "ACTIVE");
        resident(hh, "Cư dân C2", "RESIDING");
        PackageDetail draft = service.createPackage(request("C2", hh), creator);

        List<Object> outcomes = race(
            () -> service.approvePackage(draft.id(), actionOf(draft), approverOne),
            () -> service.approvePackage(draft.id(), actionOf(draft), approverTwo));

        assertEquals(1, outcomes.stream().filter(o -> o instanceof PackageDetail).count(), outcomes.toString());
        assertEquals(1, outcomes.stream().filter(o -> o instanceof ConflictException).count());
        Map<String, Object> row = jdbc.queryForMap("SELECT version, approved_by FROM agency_data_packages WHERE id = ?", draft.id());
        assertEquals(2L, ((Number) row.get("version")).longValue());
        assertTrue(List.of(approverOne, approverTwo).contains(((Number) row.get("approved_by")).longValue()));
    }

    private List<Object> race(Callable<?> first, Callable<?> second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch go = new CountDownLatch(1);
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<?> task : List.of(first, second)) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    try {
                        return (Object) task.call();
                    } catch (Exception e) {
                        return e;
                    }
                }));
            }
            ready.await();
            go.countDown();
            List<Object> results = new ArrayList<>();
            for (Future<Object> f : futures) {
                results.add(f.get());
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }

    // ---- Retention ----

    @Test
    void retentionCleanupRemovesExpiredContentMetadataAndS12AuditsOnly() {
        long actor = admin("r");
        long hh = household("RT", "ACTIVE");
        long lastAuditBefore = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM audit_log", Long.class);
        Instant now = Instant.now();

        long contentExpired = rawPackage("P1", actor, now.minus(31, ChronoUnit.DAYS), now.minus(1, ChronoUnit.DAYS), null);
        long metadataExpired = rawPackage("P2", actor, now.minus(5 * 366L, ChronoUnit.DAYS), now.minus(5 * 366L - 30, ChronoUnit.DAYS), hh);
        long fresh = rawPackage("P3", actor, now, now.plus(30, ChronoUnit.DAYS), null);

        long oldS12Audit = probeAudit("AGENCY_PACKAGE_CREATED", "AgencyDataPackage", actor, now.minus(5 * 366L, ChronoUnit.DAYS));
        long recentS12Audit = probeAudit("AGENCY_PACKAGE_CREATED", "AgencyDataPackage", actor, now.minus(365, ChronoUnit.DAYS));
        long oldOtherAudit = probeAudit("S12_RETENTION_PROBE_OTHER", "RetentionProbe", actor, now.minus(5 * 366L, ChronoUnit.DAYS));

        service.executeRetentionCleanup();

        // Nội dung hết hạn: xóa file, giữ metadata, đánh dấu thời điểm xóa.
        assertEquals(0, count("SELECT COUNT(*) FROM agency_data_package_contents WHERE package_id = ?", contentExpired));
        assertNotNull(jdbc.queryForObject("SELECT content_deleted_at FROM agency_data_packages WHERE id = ?", Timestamp.class, contentExpired));
        // Metadata quá 5 năm: mất cả gói, liên kết hộ và nội dung.
        assertEquals(0, count("SELECT COUNT(*) FROM agency_data_packages WHERE id = ?", metadataExpired));
        assertEquals(0, count("SELECT COUNT(*) FROM agency_data_package_households WHERE package_id = ?", metadataExpired));
        assertEquals(0, count("SELECT COUNT(*) FROM agency_data_package_contents WHERE package_id = ?", metadataExpired));
        // Gói mới nguyên vẹn.
        assertEquals(1, count("SELECT COUNT(*) FROM agency_data_package_contents WHERE package_id = ?", fresh));
        assertNull(jdbc.queryForObject("SELECT content_deleted_at FROM agency_data_packages WHERE id = ?", Timestamp.class, fresh));
        // Audit S12 quá 5 năm bị xóa; audit S12 còn hạn và audit không thuộc S12 được giữ.
        assertEquals(0, count("SELECT COUNT(*) FROM audit_log WHERE id = ?", oldS12Audit));
        assertEquals(1, count("SELECT COUNT(*) FROM audit_log WHERE id = ?", recentS12Audit));
        assertEquals(1, count("SELECT COUNT(*) FROM audit_log WHERE id = ?", oldOtherAudit));

        // Có ít nhất một dòng audit dọn dẹp hệ thống (actor NULL) sau thời điểm bắt đầu; scheduler nền có thể đã chạy trước.
        List<Map<String, Object>> cleanupAudits = jdbc.queryForList(
            "SELECT actor_user_id, entity_type, changes FROM audit_log WHERE action = 'AGENCY_PACKAGE_RETENTION_CLEANUP' AND id > ?",
            lastAuditBefore);
        assertFalse(cleanupAudits.isEmpty());
        for (Map<String, Object> a : cleanupAudits) {
            assertNull(a.get("actor_user_id"));
            assertEquals("RetentionBatch", a.get("entity_type"));
            String changes = (String) a.get("changes");
            for (String key : List.of("batchId", "cleanupAt", "contentCount", "metadataCount", "auditCount")) {
                assertTrue(changes.contains("\"" + key + "\""), key);
            }
        }
    }

    @Test
    void packageOlderThanFiveYearsIsHiddenFromListAndDetail() {
        long actor = admin("h");
        long visible = rawPackage("V1", actor, Instant.now(), Instant.now().plus(30, ChronoUnit.DAYS), null);
        long expired = rawPackage("V2", actor, Instant.now().minus(5 * 366L, ChronoUnit.DAYS),
            Instant.now().minus(5 * 366L - 30, ChronoUnit.DAYS), null);

        List<Long> listed = service.listPackages(null, 0, 100, actor).content().stream().map(s -> s.id()).toList();

        assertTrue(listed.contains(visible));
        assertFalse(listed.contains(expired));
        assertEquals("PACKAGE_NOT_FOUND", assertThrows(NotFoundException.class, () -> service.getPackageDetail(expired, actor)).code());
        assertEquals(visible, service.getPackageDetail(visible, actor).id());
    }

    @Test
    void listFiltersByStatusAndPagesNewestFirst() {
        long creator = admin("c");
        long approver = admin("a");
        long hh = household("LS", "ACTIVE");
        resident(hh, "Cư dân LS", "RESIDING");
        PackageDetail first = service.createPackage(request("L1", hh), creator);
        PackageDetail second = service.createPackage(request("L2", hh), creator);
        PackageDetail approved = service.approvePackage(second.id(), actionOf(second), approver);

        List<Long> approvedIds = service.listPackages("APPROVED", 0, 100, creator).content().stream().map(s -> s.id()).toList();
        List<Long> draftIds = service.listPackages("DRAFT", 0, 100, creator).content().stream().map(s -> s.id()).toList();

        assertTrue(approvedIds.contains(approved.id()));
        assertFalse(approvedIds.contains(first.id()));
        assertTrue(draftIds.contains(first.id()));
        assertFalse(draftIds.contains(approved.id()));
        List<Long> all = service.listPackages(null, 0, 100, creator).content().stream().map(s -> s.id()).toList();
        assertTrue(all.indexOf(second.id()) < all.indexOf(first.id()), "Gói tạo sau phải đứng trước");
    }

    // ---- Dữ liệu mẫu ----

    private long admin(String tag) {
        String username = "s12_" + tag + "_" + marker;
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) "
            + "VALUES ((SELECT id FROM roles WHERE code='ADMIN'), ?, 'pwd_hash', 'Quản trị S12', 'ACTIVE', NOW(6), NOW(6))", username);
        long id = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
        users.add(id);
        return id;
    }

    private long household(String tag, String status) {
        String code = "H" + tag + "-" + marker;
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) "
            + "VALUES (?, ?, 60.5, ?, NOW(6), NOW(6))", code, code, status);
        long id = jdbc.queryForObject("SELECT id FROM households WHERE household_code = ?", Long.class, code);
        households.add(id);
        return id;
    }

    private void resident(long householdId, String name, String status) {
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) "
            + "VALUES (?, ?, ?, NOW(6), NOW(6))", householdId, name, status);
    }

    private long rawPackage(String tag, long createdBy, Instant createdAt, Instant contentDeleteAfter, Long householdId) {
        byte[] bytes = {1, 2, 3};
        String sha = AgencyDataPackageWriter.computeSha256(bytes);
        String reference = "RAW-" + marker + "-" + tag;
        jdbc.update("INSERT INTO agency_data_packages (status, version, template_id, template_version, recipient_agency_code, "
            + "request_reference, licence_reference, created_by, created_at, content_delete_after, content_sha256, "
            + "household_count, resident_count, residing_count, temporarily_absent_count) "
            + "VALUES ('DRAFT', 1, 'AGENCY_RESIDENCY_V1', '1.0', 'CA_RAW', ?, ?, ?, ?, ?, ?, 1, 0, 0, 0)",
            reference, reference, createdBy, Timestamp.from(Instant.now()), Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS)), sha);
        long id = jdbc.queryForObject("SELECT id FROM agency_data_packages WHERE request_reference = ?", Long.class, reference);
        jdbc.update("INSERT INTO agency_data_package_contents (package_id, file_bytes, byte_length, content_sha256) VALUES (?, ?, ?, ?)",
            id, bytes, bytes.length, sha);
        if (householdId != null) {
            jdbc.update("INSERT INTO agency_data_package_households (package_id, household_id) VALUES (?, ?)", id, householdId);
        }
        // Chỉ làm gói "già" sau khi đã đủ dữ liệu con, để scheduler nền không xóa giữa chừng.
        jdbc.update("UPDATE agency_data_packages SET created_at = ?, content_delete_after = ? WHERE id = ?",
            Timestamp.from(createdAt), Timestamp.from(contentDeleteAfter), id);
        return id;
    }

    private long probeAudit(String action, String entityType, long actor, Instant occurredAt) {
        String entityId = "probe-" + marker + "-" + probeAuditIds.size();
        jdbc.update("INSERT INTO audit_log (occurred_at, actor_user_id, action, entity_type, entity_id, changes) VALUES (?, ?, ?, ?, ?, NULL)",
            Timestamp.from(occurredAt), actor, action, entityType, entityId);
        long id = jdbc.queryForObject("SELECT MAX(id) FROM audit_log WHERE entity_id = ? AND action = ?", Long.class,
            entityId, action);
        probeAuditIds.add(id);
        return id;
    }

    private long count(String sql, long id) {
        return jdbc.queryForObject(sql, Long.class, id);
    }
}
