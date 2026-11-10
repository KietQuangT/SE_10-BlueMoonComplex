package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.reporting.application.*;

@SpringBootTest
@Transactional
class ReportingIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired FeeCollectionReportService feeService;
    @Autowired CashReceiptsReportService cashService;
    @Autowired ResidencyReportService residencyService;

    @Test
    void ac1_financialWalkthroughAndPrecision() throws IOException {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) " +
            "VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'Test Accountant', 'ACTIVE', NOW(6), NOW(6))", "acc_" + marker);
        long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, "acc_" + marker);

        // Household A and B
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
            "VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", "H_A_" + marker, "A_" + marker);
        long householdA = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, "H_A_" + marker);

        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
            "VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", "H_B_" + marker, "B_" + marker);
        long householdB = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, "H_B_" + marker);

        String periodA = "4026-01";
        String periodB = "4026-02";

        long pA = createPeriod(periodA, actor);
        long pB = createPeriod(periodB, actor);

        long invoiceA = createInvoice(pA, householdA, "H_A_" + marker, "A_" + marker, new BigDecimal("530000"));
        long invoiceB = createInvoice(pB, householdB, "H_B_" + marker, "B_" + marker, new BigDecimal("100000"));

        LocalDate paidDate = LocalDate.of(4026, 1, 15);

        // Step 1: Nhận 200,000 rồi 400,000 cho A
        long r1 = createReceipt(invoiceA, householdA, new BigDecimal("200000"), new BigDecimal("200000"), BigDecimal.ZERO, paidDate, actor, "POSTED");
        long r2 = createReceipt(invoiceA, householdA, new BigDecimal("400000"), new BigDecimal("330000"), new BigDecimal("70000"), paidDate, actor, "POSTED");

        byte[] feeA1 = feeService.export(new FeeCollectionReportRequest(periodA));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(feeA1))) {
            XSSFSheet tongHop = wb.getSheet("TongHop");
            assertEquals("530000", tongHop.getRow(8).getCell(3).getStringCellValue());
            assertEquals("530000", tongHop.getRow(8).getCell(4).getStringCellValue());
            assertEquals("0", tongHop.getRow(8).getCell(5).getStringCellValue());
            assertEquals("100.00%", tongHop.getRow(8).getCell(6).getStringCellValue());
        }

        byte[] cash1 = cashService.export(new CashReceiptsReportRequest(LocalDate.of(4026, 1, 1), LocalDate.of(4026, 1, 31)));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(cash1))) {
            XSSFSheet tongHop = wb.getSheet("TongHop");
            assertEquals("2", tongHop.getRow(8).getCell(3).getStringCellValue());
            assertEquals("600000", tongHop.getRow(8).getCell(4).getStringCellValue());
        }

        // Step 2: Bù 50,000 credit vào B
        long allocId = createAllocation(householdB, invoiceB, new BigDecimal("50000"), actor, "ACTIVE");
        byte[] feeB2 = feeService.export(new FeeCollectionReportRequest(periodB));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(feeB2))) {
            XSSFSheet tongHop = wb.getSheet("TongHop");
            assertEquals("100000", tongHop.getRow(8).getCell(3).getStringCellValue());
            assertEquals("50000", tongHop.getRow(8).getCell(4).getStringCellValue());
            assertEquals("50000", tongHop.getRow(8).getCell(5).getStringCellValue());
            assertEquals("50.00%", tongHop.getRow(8).getCell(6).getStringCellValue());
        }

        // Step 3: Đảo allocation 50,000
        jdbc.update("UPDATE payment_credit_allocations SET status='REVERSED', reversed_by=?, reversed_at=NOW(6) WHERE id=?", actor, allocId);
        byte[] feeB3 = feeService.export(new FeeCollectionReportRequest(periodB));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(feeB3))) {
            XSSFSheet tongHop = wb.getSheet("TongHop");
            assertEquals("0", tongHop.getRow(8).getCell(4).getStringCellValue());
            assertEquals("100000", tongHop.getRow(8).getCell(5).getStringCellValue());
            assertEquals("0.00%", tongHop.getRow(8).getCell(6).getStringCellValue());
        }

        // Step 4: Hủy receipt 400,000 sau đảo
        jdbc.update("UPDATE payment_receipts SET status='VOIDED', voided_by=?, voided_at=NOW(6), void_reason='Mistake' WHERE id=?", actor, r2);
        byte[] feeA4 = feeService.export(new FeeCollectionReportRequest(periodA));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(feeA4))) {
            XSSFSheet tongHop = wb.getSheet("TongHop");
            assertEquals("530000", tongHop.getRow(8).getCell(3).getStringCellValue());
            assertEquals("200000", tongHop.getRow(8).getCell(4).getStringCellValue());
            assertEquals("330000", tongHop.getRow(8).getCell(5).getStringCellValue());
            assertEquals("37.74%", tongHop.getRow(8).getCell(6).getStringCellValue());

            XSSFSheet hoaDon = wb.getSheet("HoaDon");
            assertEquals("200000", hoaDon.getRow(1).getCell(5).getStringCellValue());
            assertEquals("330000", hoaDon.getRow(1).getCell(6).getStringCellValue());
            assertEquals("PARTIALLY_PAID", hoaDon.getRow(1).getCell(7).getStringCellValue());
        }

        byte[] cash4 = cashService.export(new CashReceiptsReportRequest(LocalDate.of(4026, 1, 1), LocalDate.of(4026, 1, 31)));
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(cash4))) {
            XSSFSheet tongHop = wb.getSheet("TongHop");
            assertEquals("1", tongHop.getRow(8).getCell(3).getStringCellValue());
            assertEquals("200000", tongHop.getRow(8).getCell(4).getStringCellValue());
        }
    }

    @Test
    void ac2_residencyMinimalDataAndStates() throws IOException {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        // Active household
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
            "VALUES (?, ?, 60, 'ACTIVE', NOW(6), NOW(6))", "H_ACT_" + marker, "ACT_" + marker);
        long activeHId = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, "H_ACT_" + marker);

        // Inactive household
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
            "VALUES (?, ?, 60, 'INACTIVE', NOW(6), NOW(6))", "H_INACT_" + marker, "INACT_" + marker);
        long inactHId = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, "H_INACT_" + marker);

        // Add residents:
        // R1: Active household, RESIDING
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, identity_number, phone, created_at, updated_at) " +
            "VALUES (?, ?, 'RESIDING', '123456789012', '0901234567', NOW(6), NOW(6))", activeHId, "Nguyen Van Residing " + marker);

        // R2: Active household, TEMPORARILY_ABSENT
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, identity_number, phone, created_at, updated_at) " +
            "VALUES (?, ?, 'TEMPORARILY_ABSENT', '234567890123', '0902345678', NOW(6), NOW(6))", activeHId, "Tran Thi Absent " + marker);

        // R3: Active household, MOVED_OUT (should be excluded)
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, identity_number, phone, created_at, updated_at) " +
            "VALUES (?, ?, 'MOVED_OUT', '345678901234', '0903456789', NOW(6), NOW(6))", activeHId, "Le Van MovedOut " + marker);

        // R4: Inactive household, RESIDING (should be excluded because household is INACTIVE)
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, identity_number, phone, created_at, updated_at) " +
            "VALUES (?, ?, 'RESIDING', '456789012345', '0904567890', NOW(6), NOW(6))", inactHId, "Pham Van InactiveH " + marker);

        byte[] report = residencyService.export();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(report))) {
            XSSFSheet cuTru = wb.getSheet("CuTru");
            assertNotNull(cuTru);

            // Verify header
            assertEquals("Mã hộ", cuTru.getRow(9).getCell(0).getStringCellValue());
            assertEquals("Căn hộ", cuTru.getRow(9).getCell(1).getStringCellValue());
            assertEquals("Họ tên", cuTru.getRow(9).getCell(2).getStringCellValue());
            assertEquals("Trạng thái", cuTru.getRow(9).getCell(3).getStringCellValue());

            // Scan through rows to verify our created residents
            boolean foundR1 = false;
            boolean foundR2 = false;
            boolean foundR3 = false;
            boolean foundR4 = false;

            for (int r = 10; r <= cuTru.getLastRowNum(); r++) {
                var row = cuTru.getRow(r);
                if (row == null) continue;
                String name = row.getCell(2).getStringCellValue();
                String status = row.getCell(3).getStringCellValue();

                if (name.contains("Nguyen Van Residing " + marker)) {
                    foundR1 = true;
                    assertEquals("Đang cư trú", status);
                }
                if (name.contains("Tran Thi Absent " + marker)) {
                    foundR2 = true;
                    assertEquals("Tạm vắng", status);
                }
                if (name.contains("Le Van MovedOut " + marker)) {
                    foundR3 = true;
                }
                if (name.contains("Pham Van InactiveH " + marker)) {
                    foundR4 = true;
                }
            }

            assertTrue(foundR1, "RESIDING resident of active household must be present");
            assertTrue(foundR2, "TEMPORARILY_ABSENT resident of active household must be present");
            assertFalse(foundR3, "MOVED_OUT resident must be excluded");
            assertFalse(foundR4, "Resident of INACTIVE household must be excluded");
        }
    }

    private long createPeriod(String period, long actor) {
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, created_by, created_at, issued_by, issued_at) " +
            "VALUES (?, '4026-01-31', 'ISSUED', ?, NOW(6), ?, NOW(6))", period, actor, actor);
        return jdbc.queryForObject("SELECT id FROM billing_periods WHERE period=? AND status='ISSUED'", Long.class, period);
    }

    private long createInvoice(long periodId, long householdId, String hCode, String aCode, BigDecimal total) {
        jdbc.update("INSERT INTO invoices (period_id, household_id, household_code, apartment_code, due_date, total, status, issued_at) " +
            "VALUES (?, ?, ?, ?, '4026-01-31', ?, 'ISSUED', NOW(6))", periodId, householdId, hCode, aCode, total);
        return jdbc.queryForObject("SELECT id FROM invoices WHERE period_id=? AND household_id=?", Long.class, periodId, householdId);
    }

    private long createReceipt(long invoiceId, long householdId, BigDecimal amount, BigDecimal applied, BigDecimal credit, LocalDate paidOn, long actor, String status) {
        jdbc.update("INSERT INTO payment_receipts (invoice_id, household_id, amount, applied_amount, credit_amount, debt_after, paid_on, status, recorded_by, recorded_at) " +
            "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, NOW(6))", invoiceId, householdId, amount, applied, credit, paidOn, status, actor);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private long createAllocation(long householdId, long invoiceId, BigDecimal amount, long actor, String status) {
        jdbc.update("INSERT INTO payment_credit_allocations (household_id, invoice_id, amount, debt_after, status, recorded_by, recorded_at) " +
            "VALUES (?, ?, ?, 0, ?, ?, NOW(6))", householdId, invoiceId, amount, status, actor);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }
}
