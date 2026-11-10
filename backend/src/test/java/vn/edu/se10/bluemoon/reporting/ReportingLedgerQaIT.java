package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.reporting.application.*;

@SpringBootTest
@Transactional
class ReportingLedgerQaIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired FeeCollectionReportService fee;
    @Autowired CashReceiptsReportService cash;

    @Test
    void multipleReceiptsAndCreditSourcesDoNotMultiplyAndSnapshotSurvivesHouseholdChanges() throws Exception {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'QA', 'ACTIVE', NOW(6), NOW(6))", "qa_" + tag);
        long actor = id("SELECT id FROM users WHERE username=?", "qa_" + tag);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", "H" + tag, "A" + tag);
        long household = id("SELECT id FROM households WHERE household_code=?", "H" + tag);
        long oldPeriod = period("4027-01", actor);
        long newPeriod = period("4027-02", actor);
        long source = invoice(oldPeriod, household, "H" + tag, "A" + tag, 100);
        long target = invoice(newPeriod, household, "H" + tag, "A" + tag, 300);
        long credit1 = receipt(source, household, 100, 50, 50, "4027-01-01", actor);
        long credit2 = receipt(source, household, 100, 50, 50, "4027-01-02", actor);
        receipt(target, household, 40, 40, 0, "4027-02-01", actor);
        receipt(target, household, 60, 60, 0, "4027-02-28", actor);
        allocation(target, household, credit1, 50, actor);
        allocation(target, household, credit2, 50, actor);
        jdbc.update("UPDATE households SET household_code=?, apartment_code=?, area_m2=99, status='INACTIVE' WHERE id=?", "NEW" + tag, "N" + tag, household);

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(fee.export(new FeeCollectionReportRequest("4027-02"))))) {
            var summary = wb.getSheet("TongHop").getRow(8);
            assertEquals("300", summary.getCell(3).getStringCellValue());
            assertEquals("200", summary.getCell(4).getStringCellValue());
            assertEquals("100", summary.getCell(5).getStringCellValue());
            assertEquals("66.67%", summary.getCell(6).getStringCellValue());
            var row = wb.getSheet("HoaDon").getRow(1);
            assertEquals("H" + tag, row.getCell(1).getStringCellValue());
            assertEquals("A" + tag, row.getCell(2).getStringCellValue());
            assertEquals("4027-02-28", row.getCell(3).getStringCellValue());
        }
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(cash.export(new CashReceiptsReportRequest(LocalDate.parse("4027-01-02"), LocalDate.parse("4027-02-01")))))) {
            var summary = wb.getSheet("TongHop").getRow(8);
            assertEquals("2", summary.getCell(3).getStringCellValue());
            assertEquals("140", summary.getCell(4).getStringCellValue());
            var first = wb.getSheet("PhieuThu").getRow(1);
            assertEquals("4027-01", first.getCell(1).getStringCellValue());
            assertEquals("50", first.getCell(7).getStringCellValue());
            assertEquals("H" + tag, first.getCell(2).getStringCellValue());
        }
    }

    @Test
    void ratioRoundsHalfUpAndZeroValueInvoiceIsPaid() throws Exception {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'QA', 'ACTIVE', NOW(6), NOW(6))", "qb_" + tag);
        long actor = id("SELECT id FROM users WHERE username=?", "qb_" + tag);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", "Q1" + tag, "B1" + tag);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", "Q2" + tag, "B2" + tag);
        long h1 = id("SELECT id FROM households WHERE household_code=?", "Q1" + tag);
        long h2 = id("SELECT id FROM households WHERE household_code=?", "Q2" + tag);
        long period = period("4028-01", actor);
        long first = invoice(period, h1, "Q1" + tag, "B1" + tag, 32);
        invoice(period, h2, "Q2" + tag, "B2" + tag, 0);
        receipt(first, h1, 1, 1, 0, "4028-01-01", actor);
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(fee.export(new FeeCollectionReportRequest("4028-01"))))) {
            var summary = wb.getSheet("TongHop").getRow(8);
            assertEquals("2", summary.getCell(2).getStringCellValue());
            assertEquals("32", summary.getCell(3).getStringCellValue());
            assertEquals("1", summary.getCell(4).getStringCellValue());
            assertEquals("31", summary.getCell(5).getStringCellValue());
            assertEquals("3.13%", summary.getCell(6).getStringCellValue());
            assertEquals("PAID", wb.getSheet("HoaDon").getRow(2).getCell(7).getStringCellValue());
        }
    }

    private long id(String sql, Object value) { return jdbc.queryForObject(sql, Long.class, value); }
    private long lastId() { return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class); }
    private long period(String value, long actor) {
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, created_by, created_at, issued_by, issued_at) VALUES (?, '4027-02-28', 'ISSUED', ?, NOW(6), ?, NOW(6))", value, actor, actor);
        return lastId();
    }
    private long invoice(long period, long household, String code, String apartment, int total) {
        jdbc.update("INSERT INTO invoices (period_id, household_id, household_code, apartment_code, due_date, total, status, issued_at) VALUES (?, ?, ?, ?, '4027-02-28', ?, 'ISSUED', NOW(6))", period, household, code, apartment, total);
        return lastId();
    }
    private long receipt(long invoice, long household, int amount, int applied, int credit, String date, long actor) {
        jdbc.update("INSERT INTO payment_receipts (invoice_id, household_id, amount, applied_amount, credit_amount, debt_after, paid_on, status, recorded_by, recorded_at) VALUES (?, ?, ?, ?, ?, 0, ?, 'POSTED', ?, NOW(6))", invoice, household, amount, applied, credit, date, actor);
        return lastId();
    }
    private void allocation(long invoice, long household, long source, int amount, long actor) {
        jdbc.update("INSERT INTO payment_credit_allocations (household_id, invoice_id, amount, debt_after, status, recorded_by, recorded_at) VALUES (?, ?, ?, 0, 'ACTIVE', ?, NOW(6))", household, invoice, amount, actor);
        long allocation = lastId();
        jdbc.update("INSERT INTO payment_credit_allocation_sources (allocation_id, receipt_id, amount) VALUES (?, ?, ?)", allocation, source, amount);
    }
}
