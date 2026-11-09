package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.ObjectMapper;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

/**
 * Bằng chứng trên MySQL thật cho VOL-01.1, 01.2, 02.1, 02.2, 03.1, 04.1, 05.1, 05.2 và 06.1.
 * {@code VoluntaryContributionService.record} dùng Propagation.NEVER nên lớp này không thể là
 * {@code @Transactional}: dữ liệu được commit thật và dọn theo ID/actor của chính test.
 * "Hôm nay" theo giờ Việt Nam được cố định là 9000-08-15.
 */
@SpringBootTest
@Import(VoluntaryCaseIT.FixedClock.class)
class VoluntaryCaseIT {
    private static final LocalDate TODAY = LocalDate.of(9000, 8, 15);
    private static final LocalDate OPENS = LocalDate.of(9000, 8, 1);
    private static final LocalDate CLOSES = LocalDate.of(9000, 8, 31);

    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired VoluntaryCampaignService campaigns;
    @Autowired VoluntaryContributionService contributions;
    @Autowired ResidentDonorLookup donors;

    String marker;
    String token;
    long admin;
    long accountant;
    long activeHousehold;
    long inactiveHousehold;
    long residing;
    long absent;
    long movedOut;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary Clock voluntaryCaseClock() { return Clock.fixed(Instant.parse("9000-08-15T00:00:00Z"), ZoneOffset.UTC); }
    }

    @BeforeEach
    void setUp() {
        marker = "vol-" + UUID.randomUUID();
        token = "Vt" + marker.substring(4, 12);
        admin = user("ADMIN");
        accountant = user("ACCOUNTANT");
        activeHousehold = household("ACTIVE", "a");
        inactiveHousehold = household("INACTIVE", "i");
        residing = resident(activeHousehold, "Nguyen " + token + " A", "RESIDING");
        absent = resident(activeHousehold, "Nguyen " + token + " B", "TEMPORARILY_ABSENT");
        movedOut = resident(inactiveHousehold, "Nguyen " + token + " C", "MOVED_OUT");
    }

    @AfterEach
    void cleanUp() {
        List<Long> ids = jdbc.queryForList("SELECT id FROM voluntary_campaigns WHERE created_by IN (?, ?)", Long.class, admin, accountant);
        for (long id : ids) jdbc.update("DELETE FROM voluntary_contributions WHERE campaign_id=?", id);
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id IN (?, ?)", admin, accountant);
        jdbc.update("DELETE FROM voluntary_campaigns WHERE created_by IN (?, ?)", admin, accountant);
        jdbc.update("DELETE FROM residents WHERE household_id IN (?, ?)", activeHousehold, inactiveHousehold);
        jdbc.update("DELETE FROM households WHERE id IN (?, ?)", activeHousehold, inactiveHousehold);
        jdbc.update("DELETE FROM users WHERE id IN (?, ?)", admin, accountant);
    }

    // ---------------------------------------------------------------- VOL-01.1, 01.2, 02.1
    @Test
    void campaignLifecycleIsPersistedWithVersionsActorsAndAudit() {
        var created = campaigns.create("  Quy Khuyen hoc " + token + "  ", "  Ung ho hoc sinh  ", OPENS, CLOSES, admin);
        long id = created.getId();
        var staff = campaigns.staffDetail(id);
        assertEquals("Quy Khuyen hoc " + token, staff.name());
        assertEquals("Ung ho hoc sinh", staff.description());
        assertEquals("DRAFT", staff.status());
        assertEquals(0L, staff.version());
        assertNull(staff.publishedAt());
        assertNull(staff.closedAt());
        assertEquals(0L, staff.postedCount());
        assertEquals("0", staff.totalCollected());

        campaigns.update(id, 0, "Quy Khuyen hoc dot 1 " + token, "Cap nhat mo ta", OPENS, CLOSES, admin);
        assertEquals(1L, dbLong("SELECT version FROM voluntary_campaigns WHERE id=?", id));
        assertEquals("Quy Khuyen hoc dot 1 " + token, jdbc.queryForObject("SELECT name FROM voluntary_campaigns WHERE id=?", String.class, id));
        // Version cũ và đợt không tồn tại.
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class,
                () -> campaigns.update(id, 0, "Doi", null, OPENS, CLOSES, admin)).code());
        assertEquals("CAMPAIGN_NOT_FOUND", assertThrows(NotFoundException.class,
                () -> campaigns.update(999_999_999L, 0, "Doi", null, OPENS, CLOSES, admin)).code());
        assertEquals(1L, dbLong("SELECT version FROM voluntary_campaigns WHERE id=?", id));

        campaigns.publish(id, 1, admin);
        assertEquals(2L, dbLong("SELECT version FROM voluntary_campaigns WHERE id=?", id));
        assertEquals("PUBLISHED", jdbc.queryForObject("SELECT status FROM voluntary_campaigns WHERE id=?", String.class, id));
        assertEquals(admin, dbLong("SELECT published_by FROM voluntary_campaigns WHERE id=?", id));
        assertNotNull(jdbc.queryForObject("SELECT published_at FROM voluntary_campaigns WHERE id=?", Object.class, id));
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaigns.publish(id, 2, admin)).code());
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class,
                () -> campaigns.update(id, 2, "Doi", null, OPENS, CLOSES, admin)).code());

        campaigns.close(id, 2, admin);
        assertEquals(3L, dbLong("SELECT version FROM voluntary_campaigns WHERE id=?", id));
        assertEquals("CLOSED", jdbc.queryForObject("SELECT status FROM voluntary_campaigns WHERE id=?", String.class, id));
        assertEquals(admin, dbLong("SELECT closed_by FROM voluntary_campaigns WHERE id=?", id));
        assertNotNull(jdbc.queryForObject("SELECT closed_at FROM voluntary_campaigns WHERE id=?", Object.class, id));
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaigns.close(id, 3, admin)).code());
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaigns.publish(id, 3, admin)).code());

        for (String action : List.of("CREATED", "UPDATED", "PUBLISHED", "CLOSED")) {
            assertEquals(1L, auditCount("VOLUNTARY_CAMPAIGN_" + action, "VoluntaryCampaign", id), action);
            assertEquals(Set.of("status", "opensOn", "closesOn"), auditKeys("VOLUNTARY_CAMPAIGN_" + action, id), action);
        }
    }

    @Test
    void anExpiredDraftCannotBePublishedAndStaysADraft() {
        long id = campaigns.create("Het han " + token, null, OPENS, LocalDate.of(9000, 8, 10), admin).getId();
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaigns.publish(id, 0, admin)).code());
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM voluntary_campaigns WHERE id=?", String.class, id));
        assertEquals(0L, auditCount("VOLUNTARY_CAMPAIGN_PUBLISHED", "VoluntaryCampaign", id));
    }

    // ---------------------------------------------------------------- VOL-02.2
    @Test
    void residentsOnlySeePublishedCampaignsAndEndedOnesShowAsClosed() {
        long draft = campaigns.create("Nhap " + token, null, OPENS, CLOSES, admin).getId();
        long open = campaigns.create("Dang mo " + token, null, OPENS, CLOSES, admin).getId();
        campaigns.publish(open, 0, admin);
        jdbc.update("INSERT INTO voluntary_campaigns (name, opens_on, closes_on, status, version, created_by, created_at, published_by, published_at) VALUES (?, '9000-08-01', '9000-08-10', 'PUBLISHED', 0, ?, NOW(6), ?, '9000-08-01 03:00:00')",
                "Qua han " + token, admin, admin);
        long ended = dbLong("SELECT id FROM voluntary_campaigns WHERE name=?", "Qua han " + token);

        var all = ids(campaigns.listResident(null, 0, 100).getContent().stream().map(c -> c.id()).toList());
        assertTrue(all.contains(open) && all.contains(ended), all.toString());
        assertFalse(all.contains(draft), "resident list must not contain a draft");
        var statuses = campaigns.listResident(null, 0, 100).getContent().stream()
                .collect(Collectors.toMap(c -> c.id(), c -> c.status()));
        assertEquals("PUBLISHED", statuses.get(open));
        assertEquals("CLOSED", statuses.get(ended));

        assertEquals(0L, campaigns.listResident("DRAFT", 0, 100).getTotalElements());
        var closed = campaigns.listResident("CLOSED", 0, 100).getContent().stream().map(c -> c.id()).toList();
        assertTrue(closed.contains(ended) && !closed.contains(open));
        var published = campaigns.listResident("PUBLISHED", 0, 100).getContent().stream().map(c -> c.id()).toList();
        assertTrue(published.contains(open) && !published.contains(ended));

        assertEquals("CAMPAIGN_NOT_FOUND", assertThrows(NotFoundException.class, () -> campaigns.residentDetail(draft)).code());
        assertEquals("CLOSED", campaigns.residentDetail(ended).status());
        assertEquals("PUBLISHED", campaigns.residentDetail(open).status());

        var staff = campaigns.listStaff(null, 0, 100).getContent().stream().collect(Collectors.toMap(c -> c.id(), c -> c.status()));
        assertEquals("DRAFT", staff.get(draft));
        assertEquals("PUBLISHED", staff.get(open));
        assertEquals("CLOSED", staff.get(ended));
        var staffClosed = campaigns.listStaff("CLOSED", 0, 100).getContent().stream().map(c -> c.id()).toList();
        assertTrue(staffClosed.contains(ended) && !staffClosed.contains(open) && !staffClosed.contains(draft));
    }

    // ---------------------------------------------------------------- VOL-03.1
    @Test
    void donorSearchFindsEveryResidencyStatusAndTreatsWildcardsAsLiterals() {
        var found = donors.search(token, 0, 20);
        assertEquals(Set.of(residing, absent, movedOut), found.getContent().stream().map(d -> d.id()).collect(Collectors.toSet()));
        var moved = found.getContent().stream().filter(d -> d.id() == movedOut).findFirst().orElseThrow();
        assertEquals(inactiveHousehold, moved.householdId());
        assertEquals("Nguyen " + token + " C", moved.fullName());
        assertEquals(marker + "-i", moved.householdCode());

        // Ký tự % và _ trong q là ký tự thường, không phải wildcard.
        assertEquals(0L, donors.search(token + "%", 0, 20).getTotalElements());
        assertEquals(0L, donors.search(token.substring(0, token.length() - 1) + "_", 0, 20).getTotalElements());
        assertEquals(0L, donors.search("%%", 0, 20).getTotalElements());
        assertEquals(0L, donors.search("!!", 0, 20).getTotalElements());
        for (String q : new String[] {"N", "%", "   ", "", null}) {
            assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class, () -> donors.search(q, 0, 20)).code(), String.valueOf(q));
        }
    }

    // ---------------------------------------------------------------- VOL-04.1
    @Test
    void twoContributionsKeepSnapshotsAddUpAndNeverTouchBillingOrLeakIntoAudit() {
        long campaign = openCampaign("Quy " + token);
        long invoicesBefore = dbLong("SELECT COUNT(*) FROM invoices");
        long receiptsBefore = dbLong("SELECT COALESCE(SUM(receipt_count), 0) FROM billing_periods");

        var first = contributions.record(input(campaign, movedOut, "200000", null, UUID.randomUUID().toString()), accountant);
        var second = contributions.record(input(campaign, movedOut, "200000", null, UUID.randomUUID().toString()), accountant);
        for (var result : List.of(first, second)) {
            assertFalse(result.replay());
            var c = result.contribution();
            assertEquals("POSTED", c.getStatus());
            assertEquals(0, new java.math.BigDecimal("200000").compareTo(c.getAmount()));
            assertEquals(inactiveHousehold, c.getHouseholdId());
            assertEquals("Nguyen " + token + " C", c.getDonorName());
            assertEquals(marker + "-i", c.getHouseholdCode());
        }
        assertNotEquals(first.contribution().getId(), second.contribution().getId());

        // Đổi tên nhân khẩu sau khi ghi nhận: sổ vẫn giữ tên lúc ghi nhận.
        jdbc.update("UPDATE residents SET full_name='Da doi ten' WHERE id=?", movedOut);
        var ledger = contributions.list(campaign, null, 0, 20).getContent();
        assertEquals(2, ledger.size());
        assertTrue(ledger.stream().allMatch(c -> c.getDonorName().equals("Nguyen " + token + " C")));

        var staff = campaigns.staffDetail(campaign);
        assertEquals(2L, staff.postedCount());
        assertEquals("400000", staff.totalCollected());
        assertEquals(invoicesBefore, dbLong("SELECT COUNT(*) FROM invoices"));
        assertEquals(receiptsBefore, dbLong("SELECT COALESCE(SUM(receipt_count), 0) FROM billing_periods"));

        assertEquals(2L, dbLong("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='VOLUNTARY_CONTRIBUTION_RECORDED'", accountant));
        for (String changes : jdbc.queryForList("SELECT changes FROM audit_log WHERE actor_user_id=? AND action='VOLUNTARY_CONTRIBUTION_RECORDED'", String.class, accountant)) {
            assertEquals(Set.of("campaignId", "amount", "paidOn", "status"), keys(changes));
            assertFalse(changes.contains("Nguyen") || changes.contains(token), changes);
        }
    }

    // ---------------------------------------------------------------- VOL-05.1
    @Test
    void replayConflictCloseAndVoidSequenceNeverCreatesASecondRecordOrAudit() {
        long campaign = openCampaign("Quy replay " + token);
        String key = "11111111-2222-3333-4444-555555555555";
        var original = input(campaign, residing, "150000", null, key);

        var created = contributions.record(original, accountant);
        assertFalse(created.replay());
        long id = created.contribution().getId();
        var replay = contributions.record(input(campaign, residing, "150000", null, key.toUpperCase()), accountant);
        assertTrue(replay.replay());
        assertEquals(id, replay.contribution().getId());
        assertEquals(1L, campaigns.staffDetail(campaign).postedCount());
        assertEquals("150000", campaigns.staffDetail(campaign).totalCollected());

        for (var changed : List.of(input(campaign, residing, "250000", null, key), input(campaign, absent, "150000", null, key),
                input(campaign, residing, "150000", "co ly do", key))) {
            assertEquals("IDEMPOTENCY_CONFLICT", assertThrows(ConflictException.class, () -> contributions.record(changed, accountant)).code());
        }

        long version = campaigns.staffDetail(campaign).version();
        campaigns.close(campaign, version, admin);
        assertTrue(contributions.record(original, accountant).replay(), "replay is checked before the window rules");

        var voided = contributions.voidContribution(id, 0, "Nhap nham", accountant);
        assertEquals("VOIDED", voided.getStatus());
        var afterVoid = contributions.record(original, accountant);
        assertTrue(afterVoid.replay());
        assertEquals("VOIDED", afterVoid.contribution().getStatus());
        assertEquals(id, afterVoid.contribution().getId());

        assertEquals(1L, dbLong("SELECT COUNT(*) FROM voluntary_contributions WHERE campaign_id=?", campaign));
        assertEquals(1L, auditCount("VOLUNTARY_CONTRIBUTION_RECORDED", "VoluntaryContribution", id));
        assertEquals(1L, auditCount("VOLUNTARY_CONTRIBUTION_VOIDED", "VoluntaryContribution", id));

        // Đợt đã đóng tay: khoản mới cùng ngày chỉ được ghi muộn kèm lý do.
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> contributions.record(input(campaign, residing, "1000", null, UUID.randomUUID().toString()), accountant)).code());
        assertEquals("POSTED", contributions.record(input(campaign, residing, "1000", "Thu tien mat cuoi ngay", UUID.randomUUID().toString()),
                accountant).contribution().getStatus());
    }

    // ---------------------------------------------------------------- VOL-05.2
    @Test
    void concurrentSameKeyAddsTheAmountToTheCampaignTotalExactlyOnce() throws Exception {
        long campaign = openCampaign("Quy race " + token);
        var original = input(campaign, residing, "500000", null, "33333333-4444-5555-6666-777777777777");
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> { start.await(); return contributions.record(original, accountant); });
            var second = pool.submit(() -> { start.await(); return contributions.record(original, accountant); });
            start.countDown();
            var a = first.get(20, TimeUnit.SECONDS);
            var b = second.get(20, TimeUnit.SECONDS);
            assertEquals(a.contribution().getId(), b.contribution().getId());
            assertNotEquals(a.replay(), b.replay(), "exactly one request creates the record");
            var staff = campaigns.staffDetail(campaign);
            assertEquals(1L, staff.postedCount());
            assertEquals("500000", staff.totalCollected());
            assertEquals(1L, auditCount("VOLUNTARY_CONTRIBUTION_RECORDED", "VoluntaryContribution", a.contribution().getId()));
        } finally {
            pool.shutdownNow();
        }
    }

    // ---------------------------------------------------------------- VOL-06.1
    @Test
    void voidKeepsHistoryUpdatesTotalsAndIsIdempotent() {
        long campaign = openCampaign("Quy huy " + token);
        long a = contributions.record(input(campaign, residing, "300000", null, UUID.randomUUID().toString()), accountant).contribution().getId();
        long b = contributions.record(input(campaign, absent, "100000", null, UUID.randomUUID().toString()), accountant).contribution().getId();
        assertEquals(2L, campaigns.staffDetail(campaign).postedCount());
        assertEquals("400000", campaigns.staffDetail(campaign).totalCollected());

        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class, () -> contributions.voidContribution(a, 0, "   ", accountant)).code());
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class, () -> contributions.voidContribution(a, 9, "Nhap nham", accountant)).code());
        assertEquals("CONTRIBUTION_NOT_FOUND", assertThrows(NotFoundException.class,
                () -> contributions.voidContribution(999_999_999L, 0, "Nhap nham", accountant)).code());
        assertEquals("POSTED", jdbc.queryForObject("SELECT status FROM voluntary_contributions WHERE id=?", String.class, a));

        var voided = contributions.voidContribution(a, 0, "  Nhap nham so tien  ", accountant);
        assertEquals("VOIDED", voided.getStatus());
        assertEquals("Nhap nham so tien", voided.getVoidReason());
        assertEquals(accountant, voided.getVoidedBy());
        assertNotNull(voided.getVoidedAt());
        var again = contributions.voidContribution(a, 0, "Ly do khac", accountant);
        assertEquals("VOIDED", again.getStatus());
        assertEquals("Nhap nham so tien", again.getVoidReason());

        var staff = campaigns.staffDetail(campaign);
        assertEquals(1L, staff.postedCount());
        assertEquals("100000", staff.totalCollected());
        assertEquals(List.of(b), contributions.list(campaign, "POSTED", 0, 20).getContent().stream().map(c -> c.getId()).toList());
        assertEquals(List.of(a), contributions.list(campaign, "VOIDED", 0, 20).getContent().stream().map(c -> c.getId()).toList());
        assertEquals(2, contributions.list(campaign, null, 0, 20).getTotalElements());

        assertEquals(1L, auditCount("VOLUNTARY_CONTRIBUTION_VOIDED", "VoluntaryContribution", a));
        String changes = jdbc.queryForObject("SELECT changes FROM audit_log WHERE action='VOLUNTARY_CONTRIBUTION_VOIDED' AND entity_id=?", String.class, Long.toString(a));
        assertEquals(Set.of("campaignId", "amount", "status"), keys(changes));
        assertFalse(changes.contains("Nhap nham"), changes);
    }

    // ---------------------------------------------------------------- helpers
    private long openCampaign(String name) {
        long id = campaigns.create(name, null, OPENS, CLOSES, admin).getId();
        campaigns.publish(id, 0, admin);
        return id;
    }

    private ContributionInput input(long campaign, long resident, String amount, String reason, String key) {
        return ContributionInput.parse(campaign, resident, amount, TODAY, reason, key);
    }

    private long dbLong(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private long auditCount(String action, String entityType, long entityId) {
        return dbLong("SELECT COUNT(*) FROM audit_log WHERE action=? AND entity_type=? AND entity_id=?", action, entityType, Long.toString(entityId));
    }

    private Set<String> auditKeys(String action, long entityId) {
        return keys(jdbc.queryForObject("SELECT changes FROM audit_log WHERE action=? AND entity_id=?", String.class, action, Long.toString(entityId)));
    }

    @SuppressWarnings("unchecked")
    private Set<String> keys(String json) {
        return ((Map<String, Object>) mapper.readValue(json, Map.class)).keySet();
    }

    private static Set<Long> ids(List<Long> values) {
        return Set.copyOf(values);
    }

    private long user(String role) {
        String name = marker + "-" + role;
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code=?), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", role, name);
        return dbLong("SELECT id FROM users WHERE username=?", name);
    }

    private long household(String status, String suffix) {
        String code = marker + "-" + suffix;
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, ?, NOW(6), NOW(6))", code, code, status);
        return dbLong("SELECT id FROM households WHERE household_code=?", code);
    }

    private long resident(long household, String name, String status) {
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) VALUES (?, ?, ?, NOW(6), NOW(6))", household, name, status);
        return dbLong("SELECT id FROM residents WHERE household_id=? AND full_name=?", household, name);
    }
}
