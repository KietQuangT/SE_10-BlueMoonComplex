package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@SpringBootTest
@Import(BillingPeriodConcurrencyIT.FixedClock.class)
class BillingPeriodConcurrencyIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired BillingPeriodService service;
    long actor;
    long household;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary Clock testClock() { return Clock.fixed(Instant.parse("9000-06-15T00:00:00Z"), ZoneOffset.UTC); }
    }

    @BeforeEach
    void setUp() {
        String marker = "s08-" + UUID.randomUUID();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 10000, '9000-06-01', ?, NOW(6) FROM fee_types WHERE code='MANAGEMENT_FEE'", actor);
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, 30000, '9000-06-01', ?, NOW(6) FROM fee_types WHERE code='WASTE_FEE'", actor);
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM invoice_lines WHERE invoice_id IN (SELECT i.id FROM invoices i JOIN billing_periods p ON p.id=i.period_id WHERE p.created_by=?)", actor);
        jdbc.update("DELETE FROM invoices WHERE period_id IN (SELECT id FROM billing_periods WHERE created_by=?)", actor);
        jdbc.update("DELETE FROM billing_submission_items WHERE submission_id IN (SELECT s.id FROM billing_submissions s JOIN billing_periods p ON p.id=s.period_id WHERE p.created_by=?)", actor);
        jdbc.update("DELETE FROM billing_submissions WHERE period_id IN (SELECT id FROM billing_periods WHERE created_by=?)", actor);
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
        jdbc.update("DELETE FROM billing_periods WHERE created_by=?", actor);
        jdbc.update("DELETE FROM fee_rates WHERE created_by=?", actor);
        jdbc.update("DELETE FROM households WHERE id=?", household);
        jdbc.update("DELETE FROM users WHERE id=?", actor);
    }

    @Test
    void concurrentCreationHasOneWinner() throws Exception {
        var results = race(() -> {
            try {
                service.create("9000-06", LocalDate.of(9000, 6, 30), actor);
                return "created";
            } catch (ConflictException ex) {
                return ex.code();
            }
        });
        assertTrue(results.containsAll(List.of("created", "BILLING_PERIOD_EXISTS")));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM billing_periods WHERE period='9000-06' AND status <> 'CANCELLED'", Integer.class));
    }

    @Test
    void concurrentIssueCreatesOneInvoiceSet() throws Exception {
        var period = service.create("9000-06", LocalDate.of(9000, 6, 30), actor);
        var submitted = service.submit(period.getId(), period.getVersion(), actor);
        long id = submitted.getId();
        long version = submitted.getVersion();
        long revision = submitted.getRevision();
        var results = race(() -> {
            try {
                service.issue(id, version, revision, actor);
                return "issued";
            } catch (ConflictException ex) {
                return ex.code();
            }
        });
        assertTrue(results.containsAll(List.of("issued", "STALE_VERSION")));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE period_id=? AND household_id=?", Integer.class, id, household));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM invoice_lines WHERE invoice_id=(SELECT id FROM invoices WHERE period_id=? AND household_id=?)", Integer.class, id, household));
    }

    private static List<String> race(Callable<String> action) throws Exception {
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<String> task = () -> { start.await(); return action.call(); };
            Future<String> first = pool.submit(task);
            Future<String> second = pool.submit(task);
            start.countDown();
            return List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
    }
}
