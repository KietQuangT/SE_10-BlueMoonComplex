package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
import vn.edu.se10.bluemoon.common.audit.AuditService;

@SpringBootTest
@Import(BillingAuditRollbackIT.FixedClock.class)
class BillingAuditRollbackIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired BillingPeriodService service;
    @MockitoSpyBean AuditService audit;
    long actor;
    long household;
    long period;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("9000-05-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @BeforeEach
    void setUp() {
        String marker = "s08-" + UUID.randomUUID();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 10000, '9000-05-01', ?, NOW(6) FROM fee_types WHERE code='MANAGEMENT_FEE'", actor);
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 30000, '9000-05-01', ?, NOW(6) FROM fee_types WHERE code='WASTE_FEE'", actor);
        period = service.create("9000-05", LocalDate.of(9000, 5, 31), actor).getId();
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM invoice_lines WHERE invoice_id IN (SELECT id FROM invoices WHERE period_id=?)", period);
        jdbc.update("DELETE FROM invoices WHERE period_id=?", period);
        jdbc.update("DELETE FROM billing_submission_items WHERE submission_id IN (SELECT id FROM billing_submissions WHERE period_id=?)", period);
        jdbc.update("DELETE FROM billing_submissions WHERE period_id=?", period);
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
        jdbc.update("DELETE FROM billing_periods WHERE id=?", period);
        jdbc.update("DELETE FROM fee_rates WHERE created_by=?", actor);
        jdbc.update("DELETE FROM households WHERE id=?", household);
        jdbc.update("DELETE FROM users WHERE id=?", actor);
    }

    @Test
    void auditFailureRollsBackSubmissionAndRevision() {
        failAudit("BILLING_PERIOD_SUBMITTED");
        assertThrows(IllegalStateException.class, () -> service.submit(period, 0, actor));
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM billing_periods WHERE id=?", String.class, period));
        assertEquals(0L, jdbc.queryForObject("SELECT revision FROM billing_periods WHERE id=?", Long.class, period));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM billing_submissions WHERE period_id=?", Integer.class, period));
    }

    @Test
    void auditFailureRollsBackInvoicesAndIssueState() {
        var submitted = service.submit(period, 0, actor);
        failAudit("BILLING_PERIOD_ISSUED");
        assertThrows(IllegalStateException.class, () -> service.issue(period, submitted.getVersion(), submitted.getRevision(), actor));
        assertEquals("PENDING_APPROVAL", jdbc.queryForObject("SELECT status FROM billing_periods WHERE id=?", String.class, period));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE period_id=?", Integer.class, period));
    }

    private void failAudit(String action) {
        AuditService target = AopTestUtils.getUltimateTargetObject(audit);
        doThrow(new IllegalStateException("test audit failure")).when(target)
                .record(anyLong(), eq(action), anyString(), anyString(), anyMap());
    }
}
