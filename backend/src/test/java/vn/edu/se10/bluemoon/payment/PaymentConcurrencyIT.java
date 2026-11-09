package vn.edu.se10.bluemoon.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.payment.application.PaymentService;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@SpringBootTest
class PaymentConcurrencyIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired PaymentService payments;
    @Autowired BillingPeriodService billingPeriods;
    @MockitoSpyBean AuditService audit;
    long actor, household, invoice1, invoice2, invoice3, period1, period2, period3, period4;
    final List<String> keys = new ArrayList<>();
    String key() { String key = UUID.randomUUID().toString(); keys.add(key); return key; }
    @BeforeEach void setup() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 30, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        period1 = period("9987-01"); period2 = period("9987-02"); period3 = period("9987-03");
        invoice1 = invoice(period1); invoice2 = invoice(period2); invoice3 = invoice(period3);
    }
    long period(String month) {
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) VALUES (?, '9999-12-31', 'ISSUED', 0, 1, 0, ?, NOW(6))", month, actor);
        return jdbc.queryForObject("SELECT id FROM billing_periods WHERE period=?", Long.class, month);
    }
    long invoice(long period) {
        jdbc.update("INSERT INTO invoices (period_id, household_id, household_code, apartment_code, due_date, total, status, issued_at) VALUES (?, ?, 'H', 'A', '9999-12-31', 100, 'ISSUED', NOW(6))", period, household);
        return jdbc.queryForObject("SELECT id FROM invoices WHERE period_id=?", Long.class, period);
    }
    @AfterEach void cleanup() {
        jdbc.update("DELETE s FROM payment_credit_allocation_sources s JOIN payment_credit_allocations a ON a.id=s.allocation_id WHERE a.household_id=?", household);
        jdbc.update("DELETE FROM payment_credit_allocations WHERE household_id=?", household);
        jdbc.update("DELETE FROM payment_receipts WHERE household_id=?", household);
        jdbc.update("DELETE FROM payment_household_locks WHERE household_id=?", household);
        for (String key : keys) jdbc.update("DELETE FROM payment_commands WHERE idempotency_key=?", key);
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
        jdbc.update("DELETE FROM invoices WHERE household_id=?", household);
        jdbc.update("DELETE FROM billing_periods WHERE id IN (?, ?, ?, ?)", period1, period2, period3, period4);
        jdbc.update("DELETE FROM households WHERE id=?", household);
        jdbc.update("DELETE FROM users WHERE id=?", actor);
    }
    @RepeatedTest(5) void simultaneousReceiptReplayAndDistinctKeysDoNotLoseMoney() throws Exception {
        String shared = key(); LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        var pool = Executors.newFixedThreadPool(2); var start = new CountDownLatch(1);
        try {
            var a = pool.submit(() -> { start.await(); return payments.receipt(invoice1, new BigDecimal("80"), today, null, shared, actor); });
            var b = pool.submit(() -> { start.await(); return payments.receipt(invoice1, new BigDecimal("80"), today, null, shared, actor); });
            start.countDown(); assertEquals(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
            assertEquals(1L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_receipts WHERE invoice_id=?", Long.class, invoice1));
            assertEquals(1L, jdbc.queryForObject("SELECT receipt_count FROM billing_periods WHERE id=?", Long.class, period1));
            assertThrows(ConflictException.class, () -> payments.receipt(invoice1, new BigDecimal("81"), today, null, shared, actor));
            assertThrows(ConflictException.class, () -> payments.receipt(invoice1, new BigDecimal("80"), today, null, shared, actor + 1));
            String k1 = key(), k2 = key(); var again = new CountDownLatch(1);
            var c = pool.submit(() -> { again.await(); return payments.receipt(invoice1, new BigDecimal("70"), today, null, k1, actor); });
            var d = pool.submit(() -> { again.await(); return payments.receipt(invoice1, new BigDecimal("70"), today, null, k2, actor); });
            again.countDown(); c.get(10, TimeUnit.SECONDS); d.get(10, TimeUnit.SECONDS);
            assertEquals("0", payments.summary(invoice1).remaining());
            assertEquals("120", payments.creditBalance(household).balance());
            assertEquals(3L, jdbc.queryForObject("SELECT receipt_count FROM billing_periods WHERE id=?", Long.class, period1));
        } finally { pool.shutdownNow(); }
    }
    @Test void auditFailureRollsBackReceiptCommandAndPeriodCount() {
        String idempotency = key();
        AuditService target = AopTestUtils.getUltimateTargetObject(audit);
        doThrow(new IllegalStateException("audit failure")).when(target).record(anyLong(), eq("RECEIPT_RECORDED"), anyString(), anyString(), anyMap());
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        assertThrows(IllegalStateException.class, () -> payments.receipt(invoice1, new BigDecimal("150"), today, null, idempotency, actor));
        assertEquals(0L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_receipts WHERE invoice_id=?", Long.class, invoice1));
        assertEquals(0L, jdbc.queryForObject("SELECT receipt_count FROM billing_periods WHERE id=?", Long.class, period1));
        assertEquals(0L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_commands WHERE idempotency_key=?", Long.class, idempotency));
    }
    @Test void rejectsFutureAndPreIssueDatesAndInvalidAmounts() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        assertThrows(BadRequestException.class, () -> payments.receipt(invoice1, BigDecimal.ONE, today.plusDays(1), null, key(), actor));
        assertThrows(BadRequestException.class, () -> payments.receipt(invoice1, BigDecimal.ONE, today.minusDays(1), "late", key(), actor));
        assertThrows(BadRequestException.class, () -> payments.receipt(invoice1, new BigDecimal("1.5"), today, null, key(), actor));
        assertThrows(BadRequestException.class, () -> payments.receipt(invoice1, BigDecimal.ZERO, today, null, key(), actor));
        assertEquals(0L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_receipts WHERE invoice_id=?", Long.class, invoice1));
    }
    @Test void rejectsExtremeExponentBeforeExpansion() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () -> {
            assertThrows(BadRequestException.class, () -> payments.receipt(invoice1, new BigDecimal("1E+100000000"), today, null, UUID.randomUUID().toString(), actor));
            assertThrows(BadRequestException.class, () -> payments.receipt(invoice1, new BigDecimal("1E-100000000"), today, null, UUID.randomUUID().toString(), actor));
        });
    }
    @Test void backdatedReceiptRequiresReasonAndStoresNoReasonInAudit() {
        jdbc.update("UPDATE invoices SET issued_at=DATE_SUB(NOW(6), INTERVAL 2 DAY) WHERE id=?", invoice1);
        LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).minusDays(1);
        assertThrows(BadRequestException.class, () -> payments.receipt(invoice1, BigDecimal.TEN, yesterday, null, key(), actor));
        String receiptKey = key();
        var receipt = payments.receipt(invoice1, BigDecimal.TEN, yesterday, "private late reason", receiptKey, actor);
        assertTrue(receipt.hasLateReason());
        assertFalse(jdbc.queryForObject("SELECT result_json FROM payment_commands WHERE idempotency_key=?", String.class, receiptKey).contains("private late reason"));
        String auditJson = jdbc.queryForObject("SELECT changes FROM audit_log WHERE actor_user_id=? AND action='RECEIPT_RECORDED'", String.class, actor);
        assertFalse(auditJson.contains("private late reason"));
        assertTrue(payments.receipts(invoice1, 0, 20).content().getFirst().hasLateReason());
        assertEquals("90", receipt.debtAfter());
    }
    @Test void allocationCannotTargetAnotherHousehold() {
        assertThrows(ConflictException.class, () -> payments.allocate(household + 1000000, invoice2, BigDecimal.ONE, key(), actor));
        assertEquals(0L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_credit_allocations WHERE invoice_id=?", Long.class, invoice2));
    }
    @Test void receiptAndPeriodCancellationSerialize() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        period4 = period(YearMonth.from(today).toString());
        long currentInvoice = invoice(period4);
        String receiptKey = key(); var start = new CountDownLatch(1); var pool = Executors.newFixedThreadPool(2);
        try {
            var receipt = pool.submit(() -> { start.await(); try { payments.receipt(currentInvoice, BigDecimal.TEN, today, null, receiptKey, actor); return true; } catch (ConflictException ex) { return false; } });
            var cancel = pool.submit(() -> { start.await(); try { billingPeriods.cancel(period4, 0, "reason", actor); return true; } catch (ConflictException ex) { return false; } });
            start.countDown(); boolean receiptWon = receipt.get(10, TimeUnit.SECONDS); boolean cancelWon = cancel.get(10, TimeUnit.SECONDS);
            assertNotEquals(receiptWon, cancelWon);
            assertEquals(receiptWon ? "ISSUED" : "CANCELLED", jdbc.queryForObject("SELECT status FROM billing_periods WHERE id=?", String.class, period4));
            assertEquals(receiptWon ? 1L : 0L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_receipts WHERE invoice_id=?", Long.class, currentInvoice));
            assertEquals(receiptWon ? 1L : 0L, jdbc.queryForObject("SELECT receipt_count FROM billing_periods WHERE id=?", Long.class, period4));
        } finally { pool.shutdownNow(); }
    }
    @Test void voidAndNewReceiptSerializeWithoutLosingMoney() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        var original = payments.receipt(invoice1, new BigDecimal("80"), today, null, key(), actor);
        String voidKey = key(), newKey = key(); var start = new CountDownLatch(1); var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> { start.await(); return payments.voidReceipt(original.receiptId(), "reason", voidKey, actor); });
            var b = pool.submit(() -> { start.await(); return payments.receipt(invoice1, new BigDecimal("80"), today, null, newKey, actor); });
            start.countDown(); a.get(10, TimeUnit.SECONDS); var posted = b.get(10, TimeUnit.SECONDS);
            assertEquals(new BigDecimal("100").subtract(new BigDecimal(posted.appliedAmount())).toPlainString(), payments.summary(invoice1).remaining());
            assertEquals(posted.creditAmount(), payments.creditBalance(household).balance());
            assertEquals(2L, jdbc.queryForObject("SELECT receipt_count FROM billing_periods WHERE id=?", Long.class, period1));
        } finally { pool.shutdownNow(); }
    }
    @RepeatedTest(10) void simultaneousAllocationsCannotOverdrawCredit() throws Exception {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        payments.receipt(invoice1, new BigDecimal("150"), today, null, key(), actor);
        String k1 = key(), k2 = key(); var start = new CountDownLatch(1); var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> { start.await(); try { payments.allocate(household, invoice2, new BigDecimal("40"), k1, actor); return true; } catch (ConflictException ex) { return false; } });
            var b = pool.submit(() -> { start.await(); try { payments.allocate(household, invoice3, new BigDecimal("40"), k2, actor); return true; } catch (ConflictException ex) { return false; } });
            start.countDown(); assertNotEquals(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
            assertEquals("10", payments.creditBalance(household).balance());
            BigDecimal totalRemaining = new BigDecimal(payments.summary(invoice2).remaining()).add(new BigDecimal(payments.summary(invoice3).remaining()));
            assertEquals(new BigDecimal("160"), totalRemaining);
            assertEquals(1L, jdbc.queryForObject("SELECT SUM(receipt_count) FROM billing_periods WHERE id IN (?, ?)", Long.class, period2, period3));
        } finally { pool.shutdownNow(); }
    }
}
