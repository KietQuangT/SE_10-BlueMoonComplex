package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.application.ResidentInvoiceService;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.payment.application.PaymentService;

@SpringBootTest
@Transactional(isolation = Isolation.READ_COMMITTED)
class ResidentInvoiceIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired ResidentInvoiceService residentService;
    @Autowired PaymentService paymentService;

    private static AuthenticatedUser resident(long householdId) {
        return new AuthenticatedUser(999L, "res_user", "Resident Name", "RESIDENT", householdId, false);
    }

    @Test void residentBillingWalkthrough() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) " +
                "VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long accountant = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);

        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
                "VALUES (?, ?, 50.00, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        var user = resident(household);

        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) " +
                "SELECT id, 10000.00, '9970-01-01', ?, NOW(6) FROM fee_types WHERE code='MANAGEMENT_FEE'", accountant);
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) " +
                "SELECT id, 30000.00, '9970-01-01', ?, NOW(6) FROM fee_types WHERE code='WASTE_FEE'", accountant);

        // Period 1 and Invoice A: 530,000 VND
        long periodA = createPeriod("9970-01", accountant, "2026-11-30");
        long invoiceA = createInvoice(periodA, household, marker, marker, "2026-11-30", new BigDecimal("530000"), "ISSUED");
        createLine(invoiceA, "MANAGEMENT_FEE", "Phí quản lý", "AREA", new BigDecimal("50.00"), new BigDecimal("50.00"), new BigDecimal("10000.00"), new BigDecimal("500000"));
        createLine(invoiceA, "WASTE_FEE", "Phí rác sinh hoạt", "PER_HOUSEHOLD", new BigDecimal("1.00"), null, new BigDecimal("30000.00"), new BigDecimal("30000"));

        // Period 2 and Invoice B: 100,000 VND
        long periodB = createPeriod("9970-02", accountant, "2026-12-31");
        long invoiceB = createInvoice(periodB, household, marker, marker, "2026-12-31", new BigDecimal("100000"), "ISSUED");
        createLine(invoiceB, "MANAGEMENT_FEE", "Phí quản lý", "AREA", new BigDecimal("10.00"), new BigDecimal("10.00"), new BigDecimal("10000.00"), new BigDecimal("100000"));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Step 1: Initial state
        var invList = residentService.invoices(user, null, "ALL", 0, 20);
        assertEquals(2, invList.totalElements());
        var invA = invList.content().stream().filter(i -> i.invoiceId() == invoiceA).findFirst().orElseThrow();
        assertEquals("530000", invA.total());
        assertEquals("0", invA.settled());
        assertEquals("530000", invA.remaining());
        assertEquals("UNPAID", invA.paymentStatus());

        var summary0 = residentService.billingSummary(user);
        assertEquals("630000", summary0.totalOutstanding());
        assertEquals(2, summary0.outstandingInvoiceCount());
        assertEquals("2026-11-30", summary0.nearestDueDate());
        assertEquals("9970-01", summary0.nearestDuePeriod());
        assertEquals("0", summary0.creditBalance());

        // Step 2: Thu 200,000 vào Invoice A -> settled 200000, remaining 330000, PARTIALLY_PAID
        paymentService.receipt(invoiceA, new BigDecimal("200000"), today, null, UUID.randomUUID().toString(), accountant);
        var invADetail1 = residentService.invoiceDetail(user, invoiceA);
        assertEquals("200000", invADetail1.settled());
        assertEquals("330000", invADetail1.remaining());
        assertEquals("PARTIALLY_PAID", invADetail1.paymentStatus());

        // Step 3: Thu 400,000 vào Invoice A -> settled 530000, remaining 0, PAID, credit 70000
        var receipt2 = paymentService.receipt(invoiceA, new BigDecimal("400000"), today, null, UUID.randomUUID().toString(), accountant);
        var invADetail2 = residentService.invoiceDetail(user, invoiceA);
        assertEquals("530000", invADetail2.settled());
        assertEquals("0", invADetail2.remaining());
        assertEquals("PAID", invADetail2.paymentStatus());

        var summary1 = residentService.billingSummary(user);
        assertEquals("100000", summary1.totalOutstanding());
        assertEquals(1, summary1.outstandingInvoiceCount());
        assertEquals("2026-12-31", summary1.nearestDueDate());
        assertEquals("9970-02", summary1.nearestDuePeriod());
        assertEquals("70000", summary1.creditBalance());

        // Step 4: Dùng 50,000 credit cho Invoice B 100,000 -> B settled 50000, remaining 50000, credit 20000
        var alloc = paymentService.allocate(household, invoiceB, new BigDecimal("50000"), UUID.randomUUID().toString(), accountant);
        var invBDetail1 = residentService.invoiceDetail(user, invoiceB);
        assertEquals("50000", invBDetail1.settled());
        assertEquals("50000", invBDetail1.remaining());
        assertEquals("PARTIALLY_PAID", invBDetail1.paymentStatus());

        var summary2 = residentService.billingSummary(user);
        assertEquals("50000", summary2.totalOutstanding());
        assertEquals(1, summary2.outstandingInvoiceCount());
        assertEquals("20000", summary2.creditBalance());

        // Step 5: Đảo phân bổ -> B remaining 100000, credit 70000
        paymentService.reverse(alloc.allocationId(), "Lý do hoàn tác", UUID.randomUUID().toString(), accountant);
        var invBDetail2 = residentService.invoiceDetail(user, invoiceB);
        assertEquals("0", invBDetail2.settled());
        assertEquals("100000", invBDetail2.remaining());
        assertEquals("UNPAID", invBDetail2.paymentStatus());

        var summary3 = residentService.billingSummary(user);
        assertEquals("100000", summary3.totalOutstanding());
        assertEquals("70000", summary3.creditBalance());

        // Step 6: Hủy receipt 400,000 sau đảo -> A remaining 330000, B remaining 100000, credit 0
        paymentService.voidReceipt(receipt2.receiptId(), "Lý do hủy", UUID.randomUUID().toString(), accountant);
        var invADetail3 = residentService.invoiceDetail(user, invoiceA);
        assertEquals("200000", invADetail3.settled());
        assertEquals("330000", invADetail3.remaining());
        assertEquals("PARTIALLY_PAID", invADetail3.paymentStatus());

        var summary4 = residentService.billingSummary(user);
        assertEquals("430000", summary4.totalOutstanding());
        assertEquals(2, summary4.outstandingInvoiceCount());
        assertEquals("2026-11-30", summary4.nearestDueDate());
        assertEquals("9970-01", summary4.nearestDuePeriod());
        assertEquals("0", summary4.creditBalance());

        // Step 7: Check receipts and allocations history
        var receiptsPage = residentService.receipts(user, null, "ALL", 0, 20);
        assertEquals(2, receiptsPage.totalElements());
        var voidedReceipt = receiptsPage.content().stream().filter(r -> r.receiptId() == receipt2.receiptId()).findFirst().orElseThrow();
        assertEquals("VOIDED", voidedReceipt.status());
        assertTrue(voidedReceipt.hasVoidReason());
        assertNotNull(voidedReceipt.voidedAt());

        var allocationsPage = residentService.creditAllocations(user, null, "ALL", 0, 20);
        assertEquals(1, allocationsPage.totalElements());
        var reversedAlloc = allocationsPage.content().getFirst();
        assertEquals("REVERSED", reversedAlloc.status());
        assertTrue(reversedAlloc.hasReverseReason());
        assertNotNull(reversedAlloc.reversedAt());
    }

    @Test void cancelledInvoiceKeepsOriginalTotalAndSettledZero() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) " +
                "VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long accountant = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);

        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
                "VALUES (?, ?, 50.00, 'ACTIVE', NOW(6), NOW(6))", marker, marker);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        var user = resident(household);

        long periodOld = createPeriod("9971-01", accountant, "2026-11-30");
        long invoiceCancelled = createInvoice(periodOld, household, marker, marker, "2026-11-30", new BigDecimal("500000"), "CANCELLED");

        long periodNew = createPeriod("9971-02", accountant, "2026-11-30");
        long invoiceReplacement = createInvoice(periodNew, household, marker, marker, "2026-11-30", new BigDecimal("550000"), "ISSUED");

        var allInvoices = residentService.invoices(user, null, "ALL", 0, 20);
        assertEquals(2, allInvoices.totalElements());

        var cancelledDetail = residentService.invoiceDetail(user, invoiceCancelled);
        assertEquals("500000", cancelledDetail.total());
        assertEquals("0", cancelledDetail.settled());
        assertEquals("0", cancelledDetail.remaining());
        assertEquals("CANCELLED", cancelledDetail.invoiceStatus());
        assertEquals("CANCELLED", cancelledDetail.paymentStatus());

        var outstandingOnly = residentService.invoices(user, null, "OUTSTANDING", 0, 20);
        assertEquals(1, outstandingOnly.totalElements());
        assertEquals(invoiceReplacement, outstandingOnly.content().getFirst().invoiceId());

        var cancelledOnly = residentService.invoices(user, null, "CANCELLED", 0, 20);
        assertEquals(1, cancelledOnly.totalElements());
        assertEquals(invoiceCancelled, cancelledOnly.content().getFirst().invoiceId());

        var summary = residentService.billingSummary(user);
        assertEquals("550000", summary.totalOutstanding());
        assertEquals(1, summary.outstandingInvoiceCount());
    }

    @Test void twoHouseholdsAreIsolated() {
        String marker1 = UUID.randomUUID().toString();
        String marker2 = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) " +
                "VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker1);
        long accountant = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker1);

        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
                "VALUES (?, ?, 50.00, 'ACTIVE', NOW(6), NOW(6))", marker1, marker1);
        long household1 = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker1);

        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
                "VALUES (?, ?, 60.00, 'ACTIVE', NOW(6), NOW(6))", marker2, marker2);
        long household2 = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker2);

        long period = createPeriod("9972-01", accountant, "2026-11-30");
        long invoice1 = createInvoice(period, household1, marker1, marker1, "2026-11-30", new BigDecimal("100000"), "ISSUED");
        long invoice2 = createInvoice(period, household2, marker2, marker2, "2026-11-30", new BigDecimal("200000"), "ISSUED");

        var user1 = resident(household1);
        var user2 = resident(household2);

        var list1 = residentService.invoices(user1, null, "ALL", 0, 20);
        assertEquals(1, list1.totalElements());
        assertEquals(invoice1, list1.content().getFirst().invoiceId());

        var list2 = residentService.invoices(user2, null, "ALL", 0, 20);
        assertEquals(1, list2.totalElements());
        assertEquals(invoice2, list2.content().getFirst().invoiceId());

        // User 1 cannot access invoice 2 -> INVOICE_NOT_FOUND
        assertThrows(NotFoundException.class, () -> residentService.invoiceDetail(user1, invoice2));
        assertThrows(NotFoundException.class, () -> residentService.receipts(user1, invoice2, "ALL", 0, 20));
        assertThrows(NotFoundException.class, () -> residentService.creditAllocations(user1, invoice2, "ALL", 0, 20));
    }

    @Test void inactiveHouseholdCanStillRead() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) " +
                "VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long accountant = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);

        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
                "VALUES (?, ?, 50.00, 'INACTIVE', NOW(6), NOW(6))", marker, marker);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);

        long period = createPeriod("9973-01", accountant, "2026-11-30");
        long invoiceId = createInvoice(period, household, marker, marker, "2026-11-30", new BigDecimal("100000"), "ISSUED");

        var user = resident(household);
        var invoices = residentService.invoices(user, null, "ALL", 0, 20);
        assertEquals(1, invoices.totalElements());
        assertEquals(invoiceId, invoices.content().getFirst().invoiceId());
    }

    private long createPeriod(String period, long creator, String dueDate) {
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) " +
                "VALUES (?, ?, 'ISSUED', 0, 1, 0, ?, NOW(6))", period, dueDate, creator);
        return jdbc.queryForObject("SELECT id FROM billing_periods WHERE period=?", Long.class, period);
    }

    private long createInvoice(long periodId, long householdId, String householdCode, String apartmentCode, String dueDate, BigDecimal total, String status) {
        jdbc.update("INSERT INTO invoices (period_id, household_id, household_code, apartment_code, due_date, total, status, issued_at, cancelled_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(6), ?)", periodId, householdId, householdCode, apartmentCode, dueDate, total, status, "CANCELLED".equals(status) ? LocalDate.now() : null);
        return jdbc.queryForObject("SELECT id FROM invoices WHERE period_id=? AND household_id=?", Long.class, periodId, householdId);
    }

    private void createLine(long invoiceId, String feeTypeCode, String feeTypeName, String calcMethod, BigDecimal quantity, BigDecimal areaM2, BigDecimal unitPrice, BigDecimal amount) {
        long feeTypeId = jdbc.queryForObject("SELECT id FROM fee_types WHERE code=?", Long.class, feeTypeCode);
        List<Long> rateIds = jdbc.query("SELECT id FROM fee_rates WHERE fee_type_id=? ORDER BY id DESC LIMIT 1", (rs, rowNum) -> rs.getLong(1), feeTypeId);
        long feeRateId;
        if (!rateIds.isEmpty()) {
            feeRateId = rateIds.getFirst();
        } else {
            jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) " +
                    "VALUES (?, ?, '1970-01-01', (SELECT id FROM users LIMIT 1), NOW(6))", feeTypeId, unitPrice);
            feeRateId = jdbc.queryForObject("SELECT id FROM fee_rates WHERE fee_type_id=? ORDER BY id DESC LIMIT 1", Long.class, feeTypeId);
        }
        jdbc.update("INSERT INTO invoice_lines (invoice_id, fee_type_id, fee_type_code, fee_type_name, calculation_method, area_m2, quantity, unit_price, fee_rate_id, amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", invoiceId, feeTypeId, feeTypeCode, feeTypeName, calcMethod, areaM2, quantity, unitPrice, feeRateId, amount);
    }
}
