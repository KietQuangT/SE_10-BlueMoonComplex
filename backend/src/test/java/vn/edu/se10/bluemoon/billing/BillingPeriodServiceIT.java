package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@SpringBootTest
@Import(BillingPeriodServiceIT.FixedClock.class)
@Transactional
class BillingPeriodServiceIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired BillingPeriodService service;
    @Autowired BillingReadStore reads;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary
        Clock billingTestClock() {
            return Clock.fixed(Instant.parse("9000-03-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Test
    void createsDraftAndAuditTogetherAndRejectsDuplicate() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        var created = service.create("9000-03", LocalDate.of(9000, 3, 31), actor);
        assertEquals("DRAFT", created.getStatus());
        assertEquals(0, created.getReceiptCount());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='BILLING_PERIOD_CREATED'", Integer.class, actor));
        assertEquals("BILLING_PERIOD_EXISTS", assertThrows(ConflictException.class,
                () -> service.create("9000-03", LocalDate.of(9000, 3, 31), actor)).code());
    }
    @Test
    void issueUsesApprovedSnapshotEvenWhenSourcesChangeAfterIssue() {
        long actor = actor();
        long household = household();
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) VALUES (?, 'Absent test', 'TEMPORARILY_ABSENT', NOW(6), NOW(6))", household);
        seedRates(actor);
        var period = service.create("9000-03", LocalDate.of(9000, 3, 31), actor);
        service.submit(period.getId(), period.getVersion(), actor);
        service.issue(period.getId(), period.getVersion(), period.getRevision(), actor);
        jdbc.update("UPDATE households SET area_m2=60 WHERE id=?", household);
        assertEquals(new BigDecimal("530000"), jdbc.queryForObject(
                "SELECT total FROM invoices WHERE period_id=? AND household_id=?", BigDecimal.class, period.getId(), household));
        assertEquals(2, jdbc.queryForObject(
                "SELECT COUNT(*) FROM invoice_lines WHERE invoice_id=(SELECT id FROM invoices WHERE period_id=? AND household_id=?)", Integer.class, period.getId(), household));
    }

    @Test
    void sourceAreaChangeAfterSubmissionBlocksIssue() {
        long actor = actor();
        long household = household();
        seedRates(actor);
        var period = service.create("9000-03", LocalDate.of(9000, 3, 31), actor);
        service.submit(period.getId(), period.getVersion(), actor);
        jdbc.update("UPDATE households SET area_m2=60 WHERE id=?", household);
        assertEquals("BILLING_INPUT_CHANGED", assertThrows(ConflictException.class,
                () -> service.issue(period.getId(), period.getVersion(), period.getRevision(), actor)).code());
    }

    @Test
    void snapshotStoreLoadsOnlyRequestedHouseholdsAndTheirTwoLines() {
        long actor = actor();
        household();
        household();
        household();
        seedRates(actor);
        var period = service.create("9000-03", LocalDate.of(9000, 3, 31), actor);
        service.submit(period.getId(), period.getVersion(), actor);
        long submissionId = jdbc.queryForObject("SELECT id FROM billing_submissions WHERE period_id=? AND revision=?",
                Long.class, period.getId(), period.getRevision());
        long count = jdbc.queryForObject("SELECT COUNT(DISTINCT household_id) FROM billing_submission_items WHERE submission_id=?",
                Long.class, submissionId);
        var firstId = jdbc.queryForObject("SELECT MIN(household_id) FROM billing_submission_items WHERE submission_id=?",
                Long.class, submissionId);
        var lastId = jdbc.queryForObject("SELECT MAX(household_id) FROM billing_submission_items WHERE submission_id=?",
                Long.class, submissionId);
        var first = reads.items(submissionId, 0, 1);
        var last = reads.items(submissionId, Math.toIntExact(count - 1), 1);
        var empty = reads.items(submissionId, Math.toIntExact(count), 1);
        assertEquals(2, first.size());
        assertEquals(java.util.Set.of(firstId), first.stream().map(BillingReadStore.Item::householdId).collect(java.util.stream.Collectors.toSet()));
        assertEquals(2, last.size());
        assertEquals(java.util.Set.of(lastId), last.stream().map(BillingReadStore.Item::householdId).collect(java.util.stream.Collectors.toSet()));
        assertEquals(java.util.List.of(), empty);
        assertEquals(count, service.preview(period.getId(), 0, 1).households().totalElements());
        assertEquals(count, service.submission(period.getId(), period.getRevision(), Math.toIntExact(count), 1).households().totalElements());
    }

    private long actor() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        return jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
    }

    private long household() {
        String marker = "s08-" + UUID.randomUUID();
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        return jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
    }

    private void seedRates(long actor) {
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 10000, '9000-03-01', ?, NOW(6) FROM fee_types WHERE code='MANAGEMENT_FEE'", actor);
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 30000, '9000-03-01', ?, NOW(6) FROM fee_types WHERE code='WASTE_FEE'", actor);
    }

}
