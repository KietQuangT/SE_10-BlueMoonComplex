package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.application.ResidentInvoiceService;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;

/** Uses only rows created in the transaction, including fee rates; no seeded rates are required. */
@SpringBootTest
@Transactional
class ResidentInvoiceRegressionIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired ResidentInvoiceService service;
    long actor, home, otherHome, first, second, zero, cancelled, foreign, r1, r2, r3, a1, a2, a3;
    String month;
    AuthenticatedUser resident;

    @BeforeEach void fixture() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id,username,password_hash,full_name,status,created_at,updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'),?,'test','private staff','ACTIVE',NOW(6),NOW(6))", marker);
        actor = id();
        home = household(marker + "a");
        otherHome = household(marker + "b");
        resident = user(11, home);
        int year = jdbc.queryForObject("SELECT MIN(y) FROM (SELECT 9000 + a.n*100 + b.n*10 + c.n AS y FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b CROSS JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c) years WHERE NOT EXISTS (SELECT 1 FROM billing_periods p WHERE p.period LIKE CONCAT(years.y,'-%')) AND NOT EXISTS (SELECT 1 FROM fee_rates f WHERE YEAR(f.effective_from)=years.y)", Integer.class);
        month = year + "-01";
        long old = period(month, "CANCELLED");
        long current = period(month, "ISSUED");
        long next = period(year + "-02", "ISSUED");
        long last = period(year + "-03", "ISSUED");
        cancelled = invoice(old, home, "500", "CANCELLED");
        first = invoice(current, home, "9007199254740993", "ISSUED");
        foreign = invoice(current, otherHome, "999999", "ISSUED");
        second = invoice(next, home, "1000", "ISSUED");
        zero = invoice(last, home, "0", "ISSUED");
        for (String code : List.of("MANAGEMENT_FEE", "WASTE_FEE")) {
            jdbc.update("INSERT INTO fee_rates (fee_type_id,unit_price,effective_from,created_by,created_at) VALUES ((SELECT id FROM fee_types WHERE code=?),10,?,?,NOW(6))", code, year + "-01-01", actor);
            long rate = id();
            jdbc.update("INSERT INTO invoice_lines (invoice_id,fee_type_id,fee_type_code,fee_type_name,calculation_method,area_m2,quantity,unit_price,fee_rate_id,amount) SELECT ?,id,code,name,calculation_method,CASE WHEN calculation_method='AREA' THEN 50.00 ELSE NULL END,CASE WHEN calculation_method='AREA' THEN 50.00 ELSE 1.00 END,10.00,?,CASE WHEN calculation_method='AREA' THEN 500 ELSE 10 END FROM fee_types WHERE code=?", first, rate, code);
        }
        r1 = receipt(first, home, "300", "200", "100", "POSTED", "2026-01-02");
        r2 = receipt(first, home, "100", "100", "0", "POSTED", "2020-01-02");
        r3 = receipt(second, home, "90", "90", "0", "VOIDED", "2029-01-02");
        receipt(foreign, otherHome, "700", "400", "300", "POSTED", "2026-01-01");
        a1 = allocation(first, home, "20", "ACTIVE");
        a2 = allocation(first, home, "30", "ACTIVE");
        a3 = allocation(second, home, "10", "REVERSED");
        jdbc.update("INSERT INTO payment_credit_allocation_sources VALUES (?,?,20),(?,?,30),(?,?,10)", a1,r1,a2,r1,a3,r1);
    }
    long id() { return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class); }
    long household(String code) {
        jdbc.update("INSERT INTO households (household_code,apartment_code,area_m2,status,created_at,updated_at) VALUES (?,?,50,'ACTIVE',NOW(6),NOW(6))", code, code);
        return id();
    }
    long period(String value, String status) {
        jdbc.update("INSERT INTO billing_periods (period,due_date,status,version,revision,receipt_count,created_by,created_at) VALUES (?,'2020-01-01',?,0,1,0,?,NOW(6))", value,status,actor);
        return id();
    }
    long invoice(long period, long household, String total, String status) {
        jdbc.update("INSERT INTO invoices (period_id,household_id,household_code,apartment_code,due_date,total,status,issued_at,cancelled_at) VALUES (?,?, 'SNAP-H','SNAP-A','2020-01-01',?,?,NOW(6),CASE WHEN ?='CANCELLED' THEN NOW(6) ELSE NULL END)",period,household,total,status,status);
        return id();
    }
    long receipt(long invoice, long household, String amount, String applied, String credit, String status, String date) {
        jdbc.update("INSERT INTO payment_receipts (invoice_id,household_id,amount,applied_amount,credit_amount,debt_after,paid_on,status,recorded_by,recorded_at,late_reason,voided_by,voided_at,void_reason) VALUES (?,?,?,?,?,0,?,?,?,NOW(6),'private late reason',CASE WHEN ?='VOIDED' THEN ? ELSE NULL END,CASE WHEN ?='VOIDED' THEN NOW(6) ELSE NULL END,CASE WHEN ?='VOIDED' THEN 'private void reason' ELSE NULL END)",invoice,household,amount,applied,credit,date,status,actor,status,actor,status,status);
        return id();
    }
    long allocation(long invoice, long household, String amount, String status) {
        jdbc.update("INSERT INTO payment_credit_allocations (invoice_id,household_id,amount,debt_after,status,recorded_by,recorded_at,reversed_by,reversed_at,reverse_reason) VALUES (?,?,?,0,?,?,NOW(6),CASE WHEN ?='REVERSED' THEN ? ELSE NULL END,CASE WHEN ?='REVERSED' THEN NOW(6) ELSE NULL END,CASE WHEN ?='REVERSED' THEN 'private reverse reason' ELSE NULL END)",invoice,household,amount,status,actor,status,actor,status,status);
        return id();
    }
    AuthenticatedUser user(long id, long household) { return new AuthenticatedUser(id,"resident","private resident","RESIDENT",household,false); }

    @Test void exactMoneySnapshotAndIndependentAggregates() {
        jdbc.update("UPDATE households SET area_m2=99 WHERE id=?", home);
        jdbc.update("UPDATE fee_rates SET unit_price=99 WHERE created_by=?", actor);
        var detail = service.invoiceDetail(resident, first);
        assertEquals("9007199254740993", detail.total());
        assertEquals("350", detail.settled());
        assertEquals("9007199254740643", detail.remaining());
        assertEquals("SNAP-H", detail.householdCode());
        assertEquals("50.00", detail.lines().getFirst().areaM2());
        assertEquals("10.00", detail.lines().getFirst().unitPrice());
        assertEquals(2, detail.lines().size());
        assertNull(detail.lines().getLast().areaM2());
        var summary = service.billingSummary(resident);
        assertEquals("9007199254741643", summary.totalOutstanding());
        assertEquals(2, summary.outstandingInvoiceCount());
        assertEquals("50", summary.creditBalance());
        assertEquals("PAID", service.invoiceDetail(resident, zero).paymentStatus());
        assertEquals("0", service.invoiceDetail(resident, cancelled).remaining());
        assertEquals("500", service.invoiceDetail(resident, cancelled).total());
    }

    @Test void listsFilterSortCountAndPageInsideHousehold() {
        var all = service.invoices(resident, null, "ALL", 0, 2);
        assertEquals(4, all.totalElements()); assertEquals(2, all.totalPages());
        assertEquals(List.of(zero, second), all.content().stream().map(i -> i.invoiceId()).toList());
        assertEquals(List.of(first, cancelled), service.invoices(resident, month, "ALL", 0, 20).content().stream().map(i -> i.invoiceId()).toList());
        assertEquals(List.of(first), service.invoices(resident, null, "PARTIALLY_PAID", 0, 20).content().stream().map(i -> i.invoiceId()).toList());
        assertEquals(List.of(second), service.invoices(resident, null, "UNPAID", 0, 20).content().stream().map(i -> i.invoiceId()).toList());
        assertEquals(List.of(zero), service.invoices(resident, null, "PAID", 0, 20).content().stream().map(i -> i.invoiceId()).toList());
        assertEquals(List.of(cancelled), service.invoices(resident, null, "CANCELLED", 0, 20).content().stream().map(i -> i.invoiceId()).toList());
        var beyond = service.invoices(resident, null, "ALL", Integer.MAX_VALUE, 100);
        assertTrue(beyond.content().isEmpty()); assertEquals(4, beyond.totalElements());
        assertEquals(all, service.invoices(user(12, home), null, "ALL", 0, 2));
        assertEquals(List.of(foreign), service.invoices(user(11, otherHome), null, "ALL", 0, 20).content().stream().map(i -> i.invoiceId()).toList());
    }

    @Test void historyFiltersPagingAndForeignIds() {
        var receipts = service.receipts(resident, null, "ALL", 0, 2);
        assertEquals(3, receipts.totalElements()); assertEquals(2, receipts.totalPages());
        assertEquals(List.of(r3,r2), receipts.content().stream().map(r -> r.receiptId()).toList());
        assertTrue(receipts.content().getFirst().hasVoidReason());
        assertEquals("90", receipts.content().getFirst().amount());
        assertEquals(List.of(r2,r1), service.receipts(resident, first, "POSTED", 0, 20).content().stream().map(r -> r.receiptId()).toList());
        assertEquals("100", service.receipts(resident, first, "POSTED", 0, 20).content().getLast().creditAmount());
        assertEquals(0, service.receipts(resident, zero, "ALL", 0, 20).totalPages());
        assertTrue(service.receipts(resident, null, "ALL", Integer.MAX_VALUE, 100).content().isEmpty());
        var allocations = service.creditAllocations(resident, null, "ALL", 0, 2);
        assertEquals(3, allocations.totalElements()); assertEquals(2, allocations.totalPages());
        assertEquals(List.of(a3,a2), allocations.content().stream().map(a -> a.allocationId()).toList());
        assertTrue(allocations.content().getFirst().hasReverseReason());
        assertEquals(List.of(a2,a1), service.creditAllocations(resident, first, "ACTIVE", 0, 20).content().stream().map(a -> a.allocationId()).toList());
        assertTrue(service.creditAllocations(resident, null, "ALL", Integer.MAX_VALUE, 100).content().isEmpty());
        for (long id : List.of(foreign, Long.MAX_VALUE)) {
            assertThrows(NotFoundException.class, () -> service.invoiceDetail(resident, id));
            assertThrows(NotFoundException.class, () -> service.receipts(resident, id, "ALL", 0, 20));
            assertThrows(NotFoundException.class, () -> service.creditAllocations(resident, id, "ALL", 0, 20));
        }
    }
}
