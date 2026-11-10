package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import vn.edu.se10.bluemoon.billing.application.ResidentInvoiceService;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.payment.application.PaymentService;

/** MySQL only: force a weaker connection default so removing service isolation breaks this test. */
@SpringBootTest(properties = "spring.datasource.hikari.transaction-isolation=TRANSACTION_READ_COMMITTED")
class ResidentInvoiceSnapshotIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired ResidentInvoiceService service;
    @Autowired PaymentService payments;
    @MockitoSpyBean NamedParameterJdbcTemplate namedJdbc;

    @Test void paymentCommitsBetweenCountAndContentWithoutChangingTheReadSnapshot() throws Exception {
        String marker = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        long actor = 0, home = 0, period = 0;
        var countFinished = new CountDownLatch(1);
        var writerCommitted = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            jdbc.update("INSERT INTO users (role_id,username,password_hash,full_name,status,created_at,updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'),?,'test','test','ACTIVE',NOW(6),NOW(6))", marker);
            actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
            jdbc.update("INSERT INTO households (household_code,apartment_code,area_m2,status,created_at,updated_at) VALUES (?,?,50,'ACTIVE',NOW(6),NOW(6))", marker, marker);
            home = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
            String month = jdbc.queryForObject("SELECT MIN(CONCAT(y,'-12')) FROM (SELECT 9000+a.n*100+b.n*10+c.n y FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c) years WHERE NOT EXISTS (SELECT 1 FROM billing_periods p WHERE p.period=CONCAT(y,'-12'))", String.class);
            assertNotNull(month, "fixture requires an unused period");
            jdbc.update("INSERT INTO billing_periods (period,due_date,status,version,revision,receipt_count,created_by,created_at) VALUES (?,'9999-12-31','ISSUED',0,1,0,?,NOW(6))", month, actor);
            period = jdbc.queryForObject("SELECT id FROM billing_periods WHERE period=?", Long.class, month);
            jdbc.update("INSERT INTO invoices (period_id,household_id,household_code,apartment_code,due_date,total,status,issued_at) VALUES (?,?,'H','A','9999-12-31',100,'ISSUED',NOW(6))", period, home);
            long invoice = jdbc.queryForObject("SELECT id FROM invoices WHERE period_id=? AND household_id=?", Long.class, period, home);
            var resident = new AuthenticatedUser(999L, "resident", "resident", "RESIDENT", home, false);
            doAnswer(invocation -> {
                Object result = invocation.callRealMethod();
                String sql = invocation.getArgument(0);
                if (sql.startsWith("SELECT COUNT(*) ") && sql.contains("d.invoice_status = 'ISSUED' AND d.remaining > 0")) {
                    countFinished.countDown();
                    if (!writerCommitted.await(10, TimeUnit.SECONDS)) throw new AssertionError("writer did not commit before content query");
                }
                return result;
            }).when(namedJdbc).queryForObject(anyString(), anyMap(), eq(Long.class));
            var reading = pool.submit(() -> service.invoices(resident, null, "OUTSTANDING", 0, 20));
            assertTrue(countFinished.await(10, TimeUnit.SECONDS), "reader did not complete count");
            long paymentActor = actor;
            var writing = pool.submit(() -> payments.receipt(invoice, new BigDecimal("100"), LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), null, key, paymentActor));
            writing.get(8, TimeUnit.SECONDS);
            assertEquals(1L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_receipts WHERE invoice_id=?", Long.class, invoice));
            writerCommitted.countDown();
            var page = reading.get(10, TimeUnit.SECONDS);
            assertEquals(1, page.totalElements());
            assertEquals(1, page.content().size());
            assertEquals(invoice, page.content().getFirst().invoiceId());
            assertEquals("0", page.content().getFirst().settled());
            assertEquals("100", page.content().getFirst().remaining());
            assertEquals(0, service.invoices(resident, null, "OUTSTANDING", 0, 20).totalElements());
        } finally {
            writerCommitted.countDown();
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(15, TimeUnit.SECONDS), "reader did not stop before cleanup");
            if (home != 0) {
                jdbc.update("DELETE FROM payment_receipts WHERE household_id=?", home);
                jdbc.update("DELETE FROM payment_household_locks WHERE household_id=?", home);
                jdbc.update("DELETE FROM invoices WHERE household_id=?", home);
            }
            jdbc.update("DELETE FROM payment_commands WHERE idempotency_key=?", key);
            if (actor != 0) jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
            if (period != 0) jdbc.update("DELETE FROM billing_periods WHERE id=?", period);
            if (home != 0) jdbc.update("DELETE FROM households WHERE id=?", home);
            if (actor != 0) jdbc.update("DELETE FROM users WHERE id=?", actor);
        }
    }
}
