package vn.edu.se10.bluemoon.payment;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.payment.application.PaymentService;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.BadRequestException;

@SpringBootTest
@Transactional(isolation = Isolation.READ_COMMITTED)
class PaymentFlowIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired PaymentService payments;

    @Test void receiptReplayAndFifoAllocationReversal() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 30, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        long invoice1 = invoice(actor, household, "9988-01");
        long invoice2 = invoice(actor, household, "9988-02");
        String key = UUID.randomUUID().toString();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        var first = payments.receipt(invoice1, new BigDecimal("150"), today, null, key, actor);
        assertEquals("100", first.appliedAmount());
        assertEquals("50", first.creditAmount());
        assertEquals("PAID", payments.summary(invoice1).status());
        assertEquals("UNPAID", payments.summary(invoice2).status());
        assertEquals(first, payments.receipt(invoice1, new BigDecimal("150"), today, null, key, actor));
        assertEquals(1L, jdbc.queryForObject("SELECT COUNT(*) FROM payment_receipts", Long.class));
        var second = payments.receipt(invoice1, new BigDecimal("40"), today, null, UUID.randomUUID().toString(), actor);
        String allocationKey = UUID.randomUUID().toString();
        var allocation = payments.allocate(household, invoice2, new BigDecimal("70"), allocationKey, actor);
        assertEquals("IDEMPOTENCY_CONFLICT", assertThrows(ConflictException.class, () -> payments.allocate(household, invoice2, new BigDecimal("70"), allocationKey, actor + 1)).code());
        assertEquals("30", allocation.debtAfter());
        assertEquals(new BigDecimal("50"), jdbc.queryForObject("SELECT amount FROM payment_credit_allocation_sources WHERE allocation_id=? AND receipt_id=?", BigDecimal.class, allocation.allocationId(), first.receiptId()));
        assertEquals(new BigDecimal("20"), jdbc.queryForObject("SELECT amount FROM payment_credit_allocation_sources WHERE allocation_id=? AND receipt_id=?", BigDecimal.class, allocation.allocationId(), second.receiptId()));
        assertEquals("PARTIALLY_PAID", payments.summary(invoice2).status());
        assertEquals("70", payments.paymentInvoices(null, "9988-02", "ALL", 0, 20).content().getFirst().settled());
        var history = payments.allocations(household, invoice2, first.receiptId(), "ACTIVE", 0, 20);
        assertEquals(1, history.totalElements());
        assertEquals(2, history.content().getFirst().sources().size());
        assertEquals(0, payments.allocations(household, invoice1, first.receiptId(), "ACTIVE", 0, 20).totalElements());
        assertEquals(0, payments.allocations(household, invoice2, first.receiptId(), "REVERSED", 0, 20).totalElements());
        assertEquals(0, payments.allocations(household, invoice2, first.receiptId(), "ACTIVE", 1, 1).content().size());
        assertEquals(first.receiptId(), history.content().getFirst().sources().getFirst().receiptId());
        var receiptHistory = payments.receipts(invoice1, 0, 20);
        var firstHistory = receiptHistory.content().stream().filter(r -> r.receiptId() == first.receiptId()).findFirst().orElseThrow();
        assertEquals(1, firstHistory.activeAllocationCount());
        assertFalse(firstHistory.canVoid());
        assertEquals(today.toString(), firstHistory.paidOn());
        assertEquals("20", payments.creditBalance(household).balance());
        assertThrows(RuntimeException.class, () -> payments.voidReceipt(first.receiptId(), "reason", UUID.randomUUID().toString(), actor));
        String reverseKey = UUID.randomUUID().toString();
        var reversed = payments.reverse(allocation.allocationId(), "reason", reverseKey, actor);
        assertEquals("IDEMPOTENCY_CONFLICT", assertThrows(ConflictException.class, () -> payments.reverse(allocation.allocationId(), "reason", reverseKey, actor + 1)).code());
        assertEquals("100", reversed.debtAfter());
        assertTrue(reversed.hasReverseReason());
        assertFalse(jdbc.queryForObject("SELECT result_json FROM payment_commands WHERE idempotency_key=?", String.class, reverseKey).contains("reason"));
        assertEquals("90", payments.creditBalance(household).balance());
        assertEquals("0", payments.paymentInvoices(null, "9988-02", "ALL", 0, 20).content().getFirst().settled());
        var reversedHistory = payments.allocations(household, null, first.receiptId(), "REVERSED", 0, 20);
        assertTrue(reversedHistory.content().getFirst().hasReverseReason());
        assertEquals(actor, reversedHistory.content().getFirst().reversedBy());
        assertTrue(payments.receipts(invoice1, 0, 20).content().stream().filter(r -> r.receiptId() == first.receiptId()).findFirst().orElseThrow().canVoid());
        String voidKey = UUID.randomUUID().toString();
        var voided = payments.voidReceipt(first.receiptId(), "reason", voidKey, actor);
        assertEquals("IDEMPOTENCY_CONFLICT", assertThrows(ConflictException.class, () -> payments.voidReceipt(first.receiptId(), "reason", voidKey, actor + 1)).code());
        assertEquals("100", voided.debtAfter());
        assertTrue(voided.hasVoidReason());
        assertFalse(jdbc.queryForObject("SELECT result_json FROM payment_commands WHERE idempotency_key=?", String.class, voidKey).contains("\"reason\""));
        assertEquals("40", payments.creditBalance(household).balance());
        var voidedHistory = payments.receipts(invoice1, 0, 20).content().stream().filter(r -> r.receiptId() == first.receiptId()).findFirst().orElseThrow();
        assertTrue(voidedHistory.hasVoidReason());
        assertEquals(actor, voidedHistory.voidedBy());
        assertFalse(voidedHistory.canVoid());
    }

    @Test void paymentInvoiceListFiltersAndPaginatesOutstandingAndPaid() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 30, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        long first = invoice(actor, household, "9986-01");
        long second = invoice(actor, household, "9986-02");
        long cancelled = invoice(actor, household, "9986-03");
        jdbc.update("UPDATE invoices SET household_code=?, apartment_code=? WHERE household_id=?", marker, marker, household);
        jdbc.update("UPDATE invoices SET status='CANCELLED' WHERE id=?", cancelled);
        payments.receipt(first, new BigDecimal("100"), LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), null, UUID.randomUUID().toString(), actor);
        var outstanding = payments.paymentInvoices(marker, null, null, 0, 1);
        assertEquals(1, outstanding.totalElements());
        assertEquals(second, outstanding.content().getFirst().invoiceId());
        assertEquals("100", outstanding.content().getFirst().remaining());
        var paid = payments.paymentInvoices(marker, "9986-01", "PAID", 0, 20);
        assertEquals(1, paid.totalElements());
        assertEquals(first, paid.content().getFirst().invoiceId());
        assertEquals("100", paid.content().getFirst().settled());
        assertEquals(2, payments.paymentInvoices(marker, null, "ALL", 0, 20).totalElements());
        assertEquals(1, payments.paymentInvoices(marker, null, "ALL", 1, 1).content().size());
        assertEquals(0, payments.paymentInvoices("%", null, "ALL", 0, 20).totalElements());
        payments.receipt(second, BigDecimal.TEN, LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), null, UUID.randomUUID().toString(), actor);
        assertEquals("90", payments.paymentInvoices(marker, null, "PARTIALLY_PAID", 0, 20).content().getFirst().remaining());
        assertEquals(0, payments.paymentInvoices(marker, null, "UNPAID", 0, 20).totalElements());
        assertThrows(BadRequestException.class, () -> payments.paymentInvoices(null, "2026-13", null, 0, 20));
        assertThrows(BadRequestException.class, () -> payments.paymentInvoices(null, null, "BAD", 0, 20));
        assertThrows(BadRequestException.class, () -> payments.paymentInvoices(null, null, null, 0, 101));
    }
    @Test void cancelledInvoiceIsNotCollectible() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 30, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        long id = invoice(actor, household, "9988-03");
        jdbc.update("UPDATE invoices SET status='CANCELLED' WHERE id=?", id);
        var summary = payments.summary(id);
        assertEquals("CANCELLED", summary.status());
        assertEquals("0", summary.remaining());
        assertEquals("100", summary.total());
    }
    private long invoice(long actor, long household, String period) {
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) VALUES (?, '9999-12-31', 'ISSUED', 0, 1, 0, ?, NOW(6))", period, actor);
        long id = jdbc.queryForObject("SELECT id FROM billing_periods WHERE period=?", Long.class, period);
        jdbc.update("INSERT INTO invoices (period_id, household_id, household_code, apartment_code, due_date, total, status, issued_at) VALUES (?, ?, 'H', 'A', '9999-12-31', 100, 'ISSUED', NOW(6))", id, household);
        return jdbc.queryForObject("SELECT id FROM invoices WHERE period_id=?", Long.class, id);
    }
}
