package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.application.BillingReceiptGuard;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@SpringBootTest
@Import(BillingReceiptRaceIT.FixedClock.class)
class BillingReceiptRaceIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired BillingPeriodService periods;
    @Autowired BillingReceiptGuard receipts;
    @Autowired PlatformTransactionManager manager;
    long actor;
    long period;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("9000-04-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @BeforeEach
    void setUp() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) VALUES ('9000-04', '9000-04-30', 'ISSUED', 0, 1, 0, ?, NOW(6))", actor);
        period = jdbc.queryForObject("SELECT id FROM billing_periods WHERE created_by=?", Long.class, actor);
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
        jdbc.update("DELETE FROM billing_periods WHERE id=?", period);
        jdbc.update("DELETE FROM users WHERE id=?", actor);
    }

    @Test
    void receiptCommittedFirstBlocksCancellation() throws Exception {
        var tx = new TransactionTemplate(manager);
        var locked = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var cancelStarted = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var receipt = pool.submit(() -> tx.executeWithoutResult(status -> {
                receipts.recordReceipt(period);
                locked.countDown();
                await(release);
            }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            var cancellation = pool.submit(() -> {
                cancelStarted.countDown();
                try {
                    periods.cancel(period, 1, "test", actor);
                    return "cancelled";
                } catch (ConflictException ex) {
                    return ex.code();
                }
            });
            assertTrue(cancelStarted.await(10, TimeUnit.SECONDS));
            Thread.sleep(300);
            assertFalse(cancellation.isDone());
            release.countDown();
            receipt.get(10, TimeUnit.SECONDS);
            assertEquals("PERIOD_HAS_RECEIPTS", cancellation.get(10, TimeUnit.SECONDS));
            assertEquals(1L, jdbc.queryForObject("SELECT receipt_count FROM billing_periods WHERE id=?", Long.class, period));
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) throw new AssertionError("timed out");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError(ex);
        }
    }
}
