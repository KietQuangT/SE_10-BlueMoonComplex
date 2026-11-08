package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
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
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.common.audit.AuditService;

@SpringBootTest
@Import(BillingPeriodCancelAndAuditRollbackIT.FixedClock.class)
class BillingPeriodCancelAndAuditRollbackIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired BillingPeriodService billingService;
    @Autowired VoluntaryCampaignService campaignService;
    @Autowired VoluntaryContributionService contributionService;
    @MockitoSpyBean AuditService audit;

    long actor;
    long household;
    long resident;
    long period;
    long campaign;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary Clock cancelAuditTestClock() {
            return Clock.fixed(Instant.parse("9000-07-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @BeforeEach
    void setUp() {
        String marker = "s08-" + UUID.randomUUID();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);

        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);

        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) VALUES (?, 'Test Resident', 'RESIDING', NOW(6), NOW(6))", household);
        resident = jdbc.queryForObject("SELECT id FROM residents WHERE household_id=?", Long.class, household);

        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 10000, '9000-07-01', ?, NOW(6) FROM fee_types WHERE code='MANAGEMENT_FEE'", actor);
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 30000, '9000-07-01', ?, NOW(6) FROM fee_types WHERE code='WASTE_FEE'", actor);

        period = billingService.create("9000-07", LocalDate.of(9000, 7, 31), actor).getId();

        var camp = campaignService.create("Chiến dịch 9000-07", "Mô tả", LocalDate.of(9000, 7, 1), LocalDate.of(9000, 7, 31), actor);
        campaign = camp.getId();
        campaignService.publish(campaign, camp.getVersion(), actor);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM voluntary_contributions WHERE campaign_id=?", campaign);
        jdbc.update("DELETE FROM voluntary_campaigns WHERE id=?", campaign);
        jdbc.update("DELETE FROM invoice_lines WHERE invoice_id IN (SELECT id FROM invoices WHERE period_id=?)", period);
        jdbc.update("DELETE FROM invoices WHERE period_id=?", period);
        jdbc.update("DELETE FROM billing_submission_items WHERE submission_id IN (SELECT id FROM billing_submissions WHERE period_id=?)", period);
        jdbc.update("DELETE FROM billing_submissions WHERE period_id=?", period);
        jdbc.update("DELETE FROM billing_periods WHERE id=?", period);
        jdbc.update("DELETE FROM fee_rates WHERE created_by=?", actor);
        jdbc.update("DELETE FROM residents WHERE id=?", resident);
        jdbc.update("DELETE FROM households WHERE id=?", household);
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
        jdbc.update("DELETE FROM users WHERE id=?", actor);
    }

    @Test
    void cancellationKeepsSnapshotsAndMarksAllInvoicesCancelledAndAllowsReplacementPeriod() {
        var submitted = billingService.submit(period, 0L, actor);
        var issued = billingService.issue(period, submitted.getVersion(), submitted.getRevision(), actor);

        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE period_id=? AND status='ISSUED'", Integer.class, period));
        assertNull(jdbc.queryForObject("SELECT cancelled_at FROM invoices WHERE period_id=?", LocalDateTime.class, period));

        var cancelled = billingService.cancel(period, issued.getVersion(), "Hủy kỳ để sửa diện tích", actor);
        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("Hủy kỳ để sửa diện tích", cancelled.getCancelReason());

        // Assert invoices are CANCELLED with timestamp
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE period_id=? AND status='ISSUED'", Integer.class, period));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE period_id=? AND status='CANCELLED'", Integer.class, period));
        assertEquals(LocalDateTime.of(9000, 7, 15, 0, 0), jdbc.queryForObject("SELECT cancelled_at FROM invoices WHERE period_id=?", LocalDateTime.class, period));

        // Assert submission and lines snapshots are kept intact
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM billing_submissions WHERE period_id=?", Integer.class, period));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM billing_submission_items WHERE submission_id IN (SELECT id FROM billing_submissions WHERE period_id=?)", Integer.class, period));

        // Assert replacement period for the same month can be created
        var replacement = billingService.create("9000-07", LocalDate.of(9000, 7, 31), actor);
        assertEquals("DRAFT", replacement.getStatus());
        // Clean up replacement
        jdbc.update("DELETE FROM billing_periods WHERE id=?", replacement.getId());
    }

    @Test
    void auditFailureRollsBackCancellation() {
        var submitted = billingService.submit(period, 0L, actor);
        var issued = billingService.issue(period, submitted.getVersion(), submitted.getRevision(), actor);

        AuditService target = AopTestUtils.getUltimateTargetObject(audit);
        doThrow(new IllegalStateException("simulated audit failure")).when(target)
                .record(anyLong(), eq("BILLING_PERIOD_CANCELLED"), anyString(), anyString(), anyMap());

        assertThrows(IllegalStateException.class,
                () -> billingService.cancel(period, issued.getVersion(), "Hủy kỳ", actor));

        // Assert rollback: period stays ISSUED, invoices stay ISSUED, cancel_reason remains null
        assertEquals("ISSUED", jdbc.queryForObject("SELECT status FROM billing_periods WHERE id=?", String.class, period));
        assertNull(jdbc.queryForObject("SELECT cancel_reason FROM billing_periods WHERE id=?", String.class, period));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE period_id=? AND status='ISSUED'", Integer.class, period));
    }

    @Test
    void auditFailureRollsBackVoidContribution() {
        var input = ContributionInput.parse(campaign, resident, "200000", LocalDate.of(9000, 7, 15),
                null, UUID.randomUUID().toString());
        var recorded = contributionService.record(input, actor).contribution();

        AuditService target = AopTestUtils.getUltimateTargetObject(audit);
        doThrow(new IllegalStateException("simulated audit failure")).when(target)
                .record(anyLong(), eq("VOLUNTARY_CONTRIBUTION_VOIDED"), anyString(), anyString(), anyMap());

        assertThrows(IllegalStateException.class,
                () -> contributionService.voidContribution(recorded.getId(), recorded.getVersion(), "Nhầm tiền", actor));

        // Assert rollback: contribution stays POSTED, void_reason is null
        assertEquals("POSTED", jdbc.queryForObject("SELECT status FROM voluntary_contributions WHERE id=?", String.class, recorded.getId()));
        assertNull(jdbc.queryForObject("SELECT void_reason FROM voluntary_contributions WHERE id=?", String.class, recorded.getId()));
    }
}
