package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.ForbiddenException;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageWriter;
import vn.edu.se10.bluemoon.reporting.application.AgencyPackageRow;
import vn.edu.se10.bluemoon.reporting.application.CreatePackageRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageActionRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageDetail;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyDataPackageStore;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyPackageRecord;

/** Ca S12 chưa có bằng chứng tự động: mốc thời hạn, giới hạn kích thước, nội dung audit, bảo vệ truy cập. */
class AgencyDataPackageCaseGapTest {
    private static final Instant T0 = Instant.parse("2026-11-10T10:00:00Z");
    private static final byte[] BYTES = {10, 20, 30};
    private static final String SHA = AgencyDataPackageWriter.computeSha256(BYTES);
    private static final long CREATOR = 1L;
    private static final long OTHER = 2L;

    private AgencyDataPackageStore store;
    private AgencyDataPackageWriter writer;
    private AuditService audit;

    @BeforeEach
    void setUp() {
        store = mock(AgencyDataPackageStore.class);
        writer = mock(AgencyDataPackageWriter.class);
        audit = mock(AuditService.class);
    }

    private AgencyDataPackageService serviceAt(Instant now) {
        return new AgencyDataPackageService(store, writer, audit, Clock.fixed(now, ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    private AgencyPackageRecord draft(Instant createdAt) {
        return new AgencyPackageRecord(100L, "DRAFT", 1L, "AGENCY_RESIDENCY_V1", "1.0", "CONG_AN_Q1", "REF-1", "LIC-1",
            CREATOR, createdAt, null, null, null, null, null, null, null, null,
            null, createdAt.plus(30, ChronoUnit.DAYS), null, SHA, 1L, 1L, 1L, 0L);
    }

    private AgencyPackageRecord approved(Instant createdAt, Instant approvedAt, Instant downloadUntil) {
        return new AgencyPackageRecord(100L, "APPROVED", 2L, "AGENCY_RESIDENCY_V1", "1.0", "CONG_AN_Q1", "REF-1", "LIC-1",
            CREATOR, createdAt, OTHER, approvedAt, null, null, null, null, null, null,
            downloadUntil, createdAt.plus(30, ChronoUnit.DAYS), null, SHA, 1L, 1L, 1L, 0L);
    }

    private static PackageActionRequest action(long version) {
        return new PackageActionRequest(version, SHA);
    }

    // ---- Thời hạn tải: min(duyệt + 7 ngày, tạo + 30 ngày) ----

    @Test
    void approve_downloadDeadlineIsSevenDaysAfterApprovalWhenEarlier() {
        Instant now = T0.plus(1, ChronoUnit.DAYS);
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(draft(T0)));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(BYTES));
        when(store.findPackageById(100L)).thenReturn(Optional.of(approved(T0, now, now.plus(7, ChronoUnit.DAYS))));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(1L));

        serviceAt(now).approvePackage(100L, action(1), OTHER);

        verify(store).updatePackageApprove(100L, OTHER, now, now.plus(7, ChronoUnit.DAYS), 2L);
    }

    @Test
    void approve_downloadDeadlineIsCappedAtContentDeleteAfter() {
        // Duyệt ở ngày 25: duyệt + 7 ngày = ngày 32 > tạo + 30 ngày, nên hạn tải là ngày 30.
        Instant now = T0.plus(25, ChronoUnit.DAYS);
        Instant cap = T0.plus(30, ChronoUnit.DAYS);
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(draft(T0)));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(BYTES));
        when(store.findPackageById(100L)).thenReturn(Optional.of(approved(T0, now, cap)));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(1L));

        serviceAt(now).approvePackage(100L, action(1), OTHER);

        verify(store).updatePackageApprove(100L, OTHER, now, cap, 2L);
    }

    @Test
    void approve_exactlySevenDaysBeforeCapKeepsSevenDays() {
        // Duyệt ở ngày 23: duyệt + 7 = đúng ngày 30; hai nhánh trùng nhau.
        Instant now = T0.plus(23, ChronoUnit.DAYS);
        Instant cap = T0.plus(30, ChronoUnit.DAYS);
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(draft(T0)));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(BYTES));
        when(store.findPackageById(100L)).thenReturn(Optional.of(approved(T0, now, cap)));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(1L));

        serviceAt(now).approvePackage(100L, action(1), OTHER);

        verify(store).updatePackageApprove(100L, OTHER, now, cap, 2L);
    }

    // ---- Mốc chính xác: đúng thời điểm hết hạn thì bị chặn, trước 1 giây thì được ----

    @Test
    void download_blockedExactlyAtDeadlineAllowedOneSecondBefore() {
        Instant until = T0.plus(7, ChronoUnit.DAYS);
        AgencyPackageRecord pkg = approved(T0, T0, until);
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(BYTES));

        ConflictException atDeadline = assertThrows(ConflictException.class,
            () -> serviceAt(until).downloadPackage(100L, OTHER));
        assertEquals("PACKAGE_DOWNLOAD_EXPIRED", atDeadline.code());

        assertArrayEquals(BYTES, serviceAt(until.minusSeconds(1)).downloadPackage(100L, OTHER));
    }

    @Test
    void handoff_blockedExactlyAtDeadlineAllowedOneSecondBefore() {
        Instant until = T0.plus(7, ChronoUnit.DAYS);
        AgencyPackageRecord pkg = approved(T0, T0, until);
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));
        when(store.findPackageById(100L)).thenReturn(Optional.of(pkg));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(1L));

        ConflictException atDeadline = assertThrows(ConflictException.class,
            () -> serviceAt(until).handoffPackage(100L, action(2), OTHER));
        assertEquals("PACKAGE_DOWNLOAD_EXPIRED", atDeadline.code());

        assertDoesNotThrow(() -> serviceAt(until.minusSeconds(1)).handoffPackage(100L, action(2), OTHER));
        verify(store).updatePackageHandoff(100L, OTHER, until.minusSeconds(1), 3L);
    }

    @Test
    void preview_approvedPackageBlockedExactlyAtDownloadDeadline() {
        Instant until = T0.plus(7, ChronoUnit.DAYS);
        when(store.findPackageById(100L)).thenReturn(Optional.of(approved(T0, T0, until)));

        ConflictException ex = assertThrows(ConflictException.class,
            () -> serviceAt(until).previewPackage(100L, 0, 20, OTHER));
        assertEquals("PACKAGE_DOWNLOAD_EXPIRED", ex.code());
    }

    @Test
    void approveAndReject_blockedExactlyAtContentDeleteAfter() {
        Instant cutoff = T0.plus(30, ChronoUnit.DAYS);
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(draft(T0)));

        ConflictException approve = assertThrows(ConflictException.class,
            () -> serviceAt(cutoff).approvePackage(100L, action(1), OTHER));
        assertEquals("PACKAGE_CONTENT_UNAVAILABLE", approve.code());

        ConflictException reject = assertThrows(ConflictException.class,
            () -> serviceAt(cutoff).rejectPackage(100L, action(1), CREATOR));
        assertEquals("PACKAGE_CONTENT_UNAVAILABLE", reject.code());

        verify(store, never()).updatePackageApprove(anyLong(), anyLong(), any(), any(), anyLong());
        verify(store, never()).updatePackageReject(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void preview_draftBlockedExactlyAtContentDeleteAfter() {
        Instant cutoff = T0.plus(30, ChronoUnit.DAYS);
        when(store.findPackageById(100L)).thenReturn(Optional.of(draft(T0)));

        ConflictException ex = assertThrows(ConflictException.class,
            () -> serviceAt(cutoff).previewPackage(100L, 0, 20, CREATOR));
        assertEquals("PACKAGE_CONTENT_UNAVAILABLE", ex.code());
    }

    // ---- Giới hạn kích thước ----

    private static List<Long> ids(int n) {
        return LongStream.rangeClosed(1, n).boxed().toList();
    }

    @Test
    void create_exactlyThousandHouseholdsAccepted() {
        List<Long> ids = ids(1000);
        when(store.validateActiveHouseholdIds(ids)).thenReturn(ids);
        when(store.fetchSnapshotResidents(ids)).thenReturn(List.of());
        when(writer.writePackage(any(), anyLong(), anyLong(), anyLong(), anyList())).thenReturn(BYTES);
        when(store.insertPackage(any())).thenReturn(100L);
        when(store.findPackageById(100L)).thenReturn(Optional.of(draft(T0)));

        PackageDetail detail = serviceAt(T0).createPackage(new CreatePackageRequest(ids, "CONG_AN_Q1", "REF-1", "LIC-1"), CREATOR);

        assertEquals("DRAFT", detail.status());
    }

    @Test
    void create_moreThanFiftyThousandResidentRowsRejectedBeforeWritingFile() {
        List<Long> ids = List.of(1L);
        List<AgencyPackageRow> rows = new ArrayList<>();
        for (int i = 0; i < 50_001; i++) {
            rows.add(new AgencyPackageRow("H1", "A1", "N" + i, "RESIDING"));
        }
        when(store.validateActiveHouseholdIds(ids)).thenReturn(ids);
        when(store.fetchSnapshotResidents(ids)).thenReturn(rows);

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> serviceAt(T0).createPackage(new CreatePackageRequest(ids, "CONG_AN_Q1", "REF-1", "LIC-1"), CREATOR));

        assertEquals("PACKAGE_TOO_LARGE", ex.code());
        verifyNoInteractions(writer);
        verify(store, never()).insertPackage(any());
        verifyNoInteractions(audit);
    }

    @Test
    void create_exactlyFiftyThousandResidentRowsAccepted() {
        List<Long> ids = List.of(1L);
        List<AgencyPackageRow> rows = new ArrayList<>();
        for (int i = 0; i < 50_000; i++) {
            rows.add(new AgencyPackageRow("H1", "A1", "N" + i, i % 2 == 0 ? "RESIDING" : "TEMPORARILY_ABSENT"));
        }
        when(store.validateActiveHouseholdIds(ids)).thenReturn(ids);
        when(store.fetchSnapshotResidents(ids)).thenReturn(rows);
        when(writer.writePackage(any(), eq(50_000L), eq(25_000L), eq(25_000L), eq(rows))).thenReturn(BYTES);
        when(store.insertPackage(any())).thenReturn(100L);
        when(store.findPackageById(100L)).thenReturn(Optional.of(draft(T0)));

        assertDoesNotThrow(() -> serviceAt(T0)
            .createPackage(new CreatePackageRequest(ids, "CONG_AN_Q1", "REF-1", "LIC-1"), CREATOR));
    }

    @Test
    void create_fileLargerThanTenMegabytesRejectedAndNothingStored() {
        List<Long> ids = List.of(1L);
        when(store.validateActiveHouseholdIds(ids)).thenReturn(ids);
        when(store.fetchSnapshotResidents(ids)).thenReturn(List.of());
        when(writer.writePackage(any(), anyLong(), anyLong(), anyLong(), anyList())).thenReturn(new byte[10 * 1024 * 1024 + 1]);

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> serviceAt(T0).createPackage(new CreatePackageRequest(ids, "CONG_AN_Q1", "REF-1", "LIC-1"), CREATOR));

        assertEquals("PACKAGE_TOO_LARGE", ex.code());
        verify(store, never()).insertPackage(any());
        verify(store, never()).insertPackageContent(anyLong(), any(), anyLong(), anyString());
    }

    @Test
    void create_snapshotCountsComeFromRowsNotFromRequest() {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<AgencyPackageRow> rows = List.of(
            new AgencyPackageRow("H1", "A1", "A", "RESIDING"),
            new AgencyPackageRow("H1", "A1", "B", "RESIDING"),
            new AgencyPackageRow("H2", "A2", "C", "TEMPORARILY_ABSENT"));
        when(store.validateActiveHouseholdIds(ids)).thenReturn(ids);
        when(store.fetchSnapshotResidents(ids)).thenReturn(rows);
        when(writer.writePackage(any(), eq(3L), eq(2L), eq(1L), eq(rows))).thenReturn(BYTES);
        when(store.insertPackage(any())).thenReturn(100L);
        when(store.findPackageById(100L)).thenReturn(Optional.of(draft(T0)));

        serviceAt(T0).createPackage(new CreatePackageRequest(ids, "CONG_AN_Q1", "REF-1", "LIC-1"), CREATOR);

        ArgumentCaptor<AgencyPackageRecord> saved = ArgumentCaptor.forClass(AgencyPackageRecord.class);
        verify(store).insertPackage(saved.capture());
        assertEquals(3L, saved.getValue().householdCount());
        assertEquals(3L, saved.getValue().residentCount());
        assertEquals(2L, saved.getValue().residingCount());
        assertEquals(1L, saved.getValue().temporarilyAbsentCount());
        assertEquals(T0.plus(30, ChronoUnit.DAYS), saved.getValue().contentDeleteAfter());
        assertEquals("DRAFT", saved.getValue().status());
        assertEquals(1L, saved.getValue().version());
        assertNull(saved.getValue().downloadUntil());
    }

    // ---- Nội dung audit: chỉ khóa đã duyệt, không có dữ liệu cá nhân ----

    @SuppressWarnings("unchecked")
    private Map<String, String> auditChanges(String action) {
        ArgumentCaptor<Map<String, String>> changes = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(anyLong(), eq(action), eq("AgencyDataPackage"), eq("100"), changes.capture());
        return changes.getValue();
    }

    @Test
    void auditChanges_createCarriesOnlyAllowedKeysAndNoResidentName() {
        List<Long> ids = List.of(1L);
        List<AgencyPackageRow> rows = List.of(new AgencyPackageRow("H1", "A1", "Nguyễn Bí Mật", "RESIDING"));
        when(store.validateActiveHouseholdIds(ids)).thenReturn(ids);
        when(store.fetchSnapshotResidents(ids)).thenReturn(rows);
        when(writer.writePackage(any(), anyLong(), anyLong(), anyLong(), anyList())).thenReturn(BYTES);
        when(store.insertPackage(any())).thenReturn(100L);
        when(store.findPackageById(100L)).thenReturn(Optional.of(draft(T0)));

        serviceAt(T0).createPackage(new CreatePackageRequest(ids, "CONG_AN_Q1", "REF-1", "LIC-1"), CREATOR);

        Map<String, String> changes = auditChanges("AGENCY_PACKAGE_CREATED");
        assertEquals(Set.of("version", "templateId", "householdCount", "residentCount", "recipientAgencyCode"), changes.keySet());
        assertFalse(changes.values().stream().anyMatch(v -> v.contains("Bí Mật")));
        assertEquals("1", changes.get("householdCount"));
        assertEquals("1", changes.get("residentCount"));
    }

    @Test
    void auditChanges_approveRejectRevokeHandoffRecordStatusVersionAndHash() {
        Instant until = T0.plus(7, ChronoUnit.DAYS);
        AgencyPackageRecord appr = approved(T0, T0, until);
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(draft(T0)));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(BYTES));
        when(store.findPackageById(100L)).thenReturn(Optional.of(appr));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(1L));

        serviceAt(T0).approvePackage(100L, action(1), OTHER);
        Map<String, String> approve = auditChanges("AGENCY_PACKAGE_APPROVED");
        assertEquals(Map.of("version", "2", "contentSha256", SHA, "status", "APPROVED", "recipientAgencyCode", "CONG_AN_Q1"), approve);

        reset(audit);
        serviceAt(T0).rejectPackage(100L, action(1), CREATOR);
        Map<String, String> reject = auditChanges("AGENCY_PACKAGE_REJECTED");
        assertEquals(Map.of("version", "2", "contentSha256", SHA, "status", "REJECTED", "recipientAgencyCode", "CONG_AN_Q1"), reject);

        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(appr));
        reset(audit);
        serviceAt(T0).revokePackage(100L, action(2), OTHER);
        Map<String, String> revoke = auditChanges("AGENCY_PACKAGE_REVOKED");
        assertEquals(Map.of("version", "3", "contentSha256", SHA, "status", "REVOKED", "recipientAgencyCode", "CONG_AN_Q1"), revoke);

        reset(audit);
        serviceAt(T0).handoffPackage(100L, action(2), OTHER);
        Map<String, String> handoff = auditChanges("AGENCY_PACKAGE_HANDED_OFF");
        assertEquals(Map.of("version", "3", "contentSha256", SHA, "status", "APPROVED", "recipientAgencyCode", "CONG_AN_Q1"), handoff);
    }

    @Test
    void auditChanges_downloadAuthorizationRecordsContentFingerprintOnly() {
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(approved(T0, T0, T0.plus(7, ChronoUnit.DAYS))));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(BYTES));

        serviceAt(T0).downloadPackage(100L, OTHER);

        Map<String, String> changes = auditChanges("AGENCY_PACKAGE_DOWNLOAD_AUTHORIZED");
        assertEquals(Set.of("contentVersion", "contentSha256", "templateId", "householdCount", "residentCount", "recipientAgencyCode"),
            changes.keySet());
        assertEquals(SHA, changes.get("contentSha256"));
    }

    @Test
    void failedTransitionsWriteNoAuditAndNoUpdate() {
        // Sai mã băm -> 409; self-approve -> 403; không bản ghi nào được ghi.
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(draft(T0)));
        PackageActionRequest wrongHash = new PackageActionRequest(1L, "0".repeat(64));

        assertThrows(ConflictException.class, () -> serviceAt(T0).approvePackage(100L, wrongHash, OTHER));
        assertThrows(ForbiddenException.class, () -> serviceAt(T0).approvePackage(100L, action(1), CREATOR));
        assertThrows(ConflictException.class, () -> serviceAt(T0).rejectPackage(100L, wrongHash, CREATOR));

        verifyNoInteractions(audit);
        verify(store, never()).updatePackageApprove(anyLong(), anyLong(), any(), any(), anyLong());
        verify(store, never()).updatePackageReject(anyLong(), anyLong(), any(), anyLong());
    }

    // ---- Bảo vệ truy cập và toàn vẹn nội dung ----

    @Test
    void unauthenticatedActorRejectedBeforeAnyStoreAccess() {
        AgencyDataPackageService service = serviceAt(T0);
        PackageActionRequest req = action(1);
        CreatePackageRequest create = new CreatePackageRequest(List.of(1L), "CONG_AN_Q1", "REF-1", "LIC-1");

        assertThrows(ForbiddenException.class, () -> service.createPackage(create, null));
        assertThrows(ForbiddenException.class, () -> service.approvePackage(100L, req, null));
        assertThrows(ForbiddenException.class, () -> service.rejectPackage(100L, req, null));
        assertThrows(ForbiddenException.class, () -> service.revokePackage(100L, req, null));
        assertThrows(ForbiddenException.class, () -> service.handoffPackage(100L, req, null));
        assertThrows(ForbiddenException.class, () -> service.downloadPackage(100L, null));

        verifyNoInteractions(store, writer, audit);
    }

    @Test
    void download_tamperedContentIsNeverReleasedNorAudited() {
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(approved(T0, T0, T0.plus(7, ChronoUnit.DAYS))));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(new byte[]{9, 9, 9}));

        assertThrows(IllegalStateException.class, () -> serviceAt(T0).downloadPackage(100L, OTHER));

        verifyNoInteractions(audit);
    }

    @Test
    void preview_tamperedContentIsNeverParsed() {
        when(store.findPackageById(100L)).thenReturn(Optional.of(draft(T0)));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(new byte[]{9, 9, 9}));

        assertThrows(IllegalStateException.class, () -> serviceAt(T0).previewPackage(100L, 0, 20, CREATOR));

        verifyNoInteractions(writer);
    }

    @Test
    void download_contentRowMissingGivesUnavailableNotServerError() {
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(approved(T0, T0, T0.plus(7, ChronoUnit.DAYS))));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.empty());

        ConflictException ex = assertThrows(ConflictException.class, () -> serviceAt(T0).downloadPackage(100L, OTHER));

        assertEquals("PACKAGE_CONTENT_UNAVAILABLE", ex.code());
        verifyNoInteractions(audit);
    }

    // ---- Cờ hành động theo vai trò người xem ----

    @Test
    void actionFlags_creatorCannotApproveButOtherAdminCan() {
        AgencyPackageRecord pkg = draft(T0);
        assertFalse(AgencyDataPackageService.canApprove(pkg, CREATOR, T0));
        assertTrue(AgencyDataPackageService.canApprove(pkg, OTHER, T0));
        assertFalse(AgencyDataPackageService.canApprove(pkg, null, T0));
        assertTrue(AgencyDataPackageService.canReject(pkg, T0));
        assertFalse(AgencyDataPackageService.canDownload(pkg, T0));
        assertFalse(AgencyDataPackageService.canRevoke(pkg));
        assertFalse(AgencyDataPackageService.canHandoff(pkg, T0));
    }

    @Test
    void actionFlags_approvedPackageOffersDownloadHandoffRevokeUntilDeadline() {
        Instant until = T0.plus(7, ChronoUnit.DAYS);
        AgencyPackageRecord pkg = approved(T0, T0, until);
        assertTrue(AgencyDataPackageService.canDownload(pkg, until.minusSeconds(1)));
        assertFalse(AgencyDataPackageService.canDownload(pkg, until));
        assertTrue(AgencyDataPackageService.canHandoff(pkg, until.minusSeconds(1)));
        assertFalse(AgencyDataPackageService.canHandoff(pkg, until));
        assertTrue(AgencyDataPackageService.canRevoke(pkg));
        assertFalse(AgencyDataPackageService.canApprove(pkg, OTHER, T0));
        assertFalse(AgencyDataPackageService.canReject(pkg, T0));
    }
}
