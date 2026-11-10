package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.reporting.application.*;
import vn.edu.se10.bluemoon.reporting.persistence.*;

@SpringBootTest
class ReportingSnapshotQaIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;
    @Autowired FeeCollectionReportReadStore fee;
    @Autowired CashReceiptsReportReadStore cash;
    @Autowired ResidencyReportReadStore residency;
    private long actor, household, periodId, invoice, receipt, resident;
    private String period, name;
    private final LocalDate paidOn = LocalDate.of(3099, 4, 18);
    private final Instant now = Instant.parse("2026-11-10T10:00:00Z");

    @BeforeEach
    void seedCommittedFixture() {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        name = "QA snapshot " + tag;
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'QA', 'ACTIVE', NOW(6), NOW(6))", "qs_" + tag);
        actor = id("SELECT id FROM users WHERE username=?", "qs_" + tag);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", "S" + tag, "T" + tag);
        household = id("SELECT id FROM households WHERE household_code=?", "S" + tag);
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) VALUES (?, ?, 'RESIDING', NOW(6), NOW(6))", household, name);
        resident = id("SELECT id FROM residents WHERE household_id=?", household);
        // Pick an unused period, avoiding assumptions about other tests' data.
        for (int year = 8000; year < 9000; year++) {
            String candidate = year + "-04";
            if (jdbc.queryForObject("SELECT COUNT(*) FROM billing_periods WHERE period=?", Long.class, candidate) == 0) { period = candidate; break; }
        }
        assertNotNull(period);
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, created_by, created_at, issued_by, issued_at) VALUES (?, '3099-04-30', 'ISSUED', ?, NOW(6), ?, NOW(6))", period, actor, actor);
        periodId = id("SELECT id FROM billing_periods WHERE period=?", period);
        jdbc.update("INSERT INTO invoices (period_id, household_id, household_code, apartment_code, due_date, total, status, issued_at) VALUES (?, ?, ?, ?, '3099-04-30', 100, 'ISSUED', NOW(6))", periodId, household, "S" + tag, "T" + tag);
        invoice = id("SELECT id FROM invoices WHERE period_id=?", periodId);
        jdbc.update("INSERT INTO payment_receipts (invoice_id, household_id, amount, applied_amount, credit_amount, debt_after, paid_on, status, recorded_by, recorded_at) VALUES (?, ?, 40, 40, 0, 60, ?, 'POSTED', ?, NOW(6))", invoice, household, paidOn, actor);
        receipt = id("SELECT id FROM payment_receipts WHERE invoice_id=?", invoice);
    }

    @AfterEach
    void removeOnlyFixtureRows() {
        if (receipt != 0) jdbc.update("DELETE FROM payment_receipts WHERE id=?", receipt);
        if (invoice != 0) jdbc.update("DELETE FROM invoices WHERE id=?", invoice);
        if (periodId != 0) jdbc.update("DELETE FROM billing_periods WHERE id=?", periodId);
        if (resident != 0) jdbc.update("DELETE FROM residents WHERE id=?", resident);
        if (household != 0) jdbc.update("DELETE FROM households WHERE id=?", household);
        if (actor != 0) jdbc.update("DELETE FROM users WHERE id=?", actor);
    }

    @Test
    void feeVoidCommitBetweenCountAndRowsKeepsOldSnapshotThenNextReadSeesVoid() throws Exception {
        FeeCollectionReportData before = readWithInterveningCommit(fee, () -> fee.load(period, now), this::voidReceipt);
        assertEquals(1, before.totals().invoiceCount());
        assertEquals(1, before.rows().size());
        assertEquals(0, before.totals().totalSettled().compareTo(new java.math.BigDecimal("40")));
        FeeCollectionReportData after = fee.load(period, now);
        assertEquals(0, after.totals().totalSettled().signum());
        assertEquals("UNPAID", after.rows().get(0).paymentStatus());
    }

    @Test
    void cashVoidCommitBetweenCountAndRowsKeepsCountRowsAndTotalsConsistent() throws Exception {
        CashReceiptReportData before = readWithInterveningCommit(cash, () -> cash.load(paidOn, paidOn, now), this::voidReceipt);
        CashReceiptReportData after = cash.load(paidOn, paidOn, now);
        assertEquals(before.totals().receiptCount(), before.rows().size());
        assertTrue(before.rows().stream().anyMatch(r -> r.receiptId() == receipt));
        assertFalse(after.rows().stream().anyMatch(r -> r.receiptId() == receipt));
        assertEquals(before.totals().receiptCount() - 1, after.totals().receiptCount());
        assertEquals(0, before.totals().cashReceived().subtract(after.totals().cashReceived()).compareTo(new java.math.BigDecimal("40")));
    }

    @Test
    void residencyMoveOutBetweenCountAndRowsKeepsOldSnapshotThenNextReadSeesChange() throws Exception {
        ResidencyReportData before = readWithInterveningCommit(residency, () -> residency.load(now), () -> jdbc.update("UPDATE residents SET residency_status='MOVED_OUT' WHERE id=?", resident));
        ResidencyReportData after = residency.load(now);
        assertEquals(before.totals().residentCount(), before.rows().size());
        assertTrue(before.rows().stream().anyMatch(r -> r.fullName().equals(name)));
        assertFalse(after.rows().stream().anyMatch(r -> r.fullName().equals(name)));
        assertEquals(before.totals().residentCount() - 1, after.totals().residentCount());
        assertEquals(before.totals().residingCount() - 1, after.totals().residingCount());
    }

    private void voidReceipt() { jdbc.update("UPDATE payment_receipts SET status='VOIDED', voided_by=?, voided_at=NOW(6), void_reason='QA' WHERE id=?", actor, receipt); }
    private long id(String sql, Object arg) { return jdbc.queryForObject(sql, Long.class, arg); }

    private <T> T readWithInterveningCommit(Object proxy, Supplier<T> read, Runnable commit) throws Exception {
        Object target = AopTestUtils.getTargetObject(proxy);
        Object original = ReflectionTestUtils.getField(target, "jdbc");
        var counted = new CountDownLatch(1);
        var resume = new CountDownLatch(1);
        var first = new AtomicBoolean(true);
        var intercepted = new NamedParameterJdbcTemplate(dataSource) {
            @Override public <V> V queryForObject(String sql, SqlParameterSource params, Class<V> requiredType) {
                V result = super.queryForObject(sql, params, requiredType);
                if (first.compareAndSet(true, false)) {
                    counted.countDown();
                    try {
                        if (!resume.await(15, TimeUnit.SECONDS)) throw new AssertionError("commit did not finish");
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
                }
                return result;
            }
        };
        ReflectionTestUtils.setField(target, "jdbc", intercepted);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var future = executor.submit(read::get);
            try {
                assertTrue(counted.await(15, TimeUnit.SECONDS), "count query did not complete");
                commit.run(); // Separate connection, autocommitted before the blocked reader continues.
            } finally { resume.countDown(); }
            return future.get(15, TimeUnit.SECONDS);
        } finally { ReflectionTestUtils.setField(target, "jdbc", original); }
    }
}
