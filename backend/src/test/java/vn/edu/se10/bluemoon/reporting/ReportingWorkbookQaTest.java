package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.reporting.application.*;

class ReportingWorkbookQaTest {
    @Test
    void allUserTextStaysLiteralAndWorkbookContainsOnlyApprovedColumns() throws Exception {
        var metadata = new ReportMetadata("CURRENT_RESIDENCY_V1", "1.0", Instant.parse("2026-11-10T10:00:00Z"), "Danh sách cư trú hiện tại");
        String name = "Nguyễn & <An>_%\nBình";
        byte[] bytes = new XlsxReportWriter().writeResidencyReport(new ResidencyReportData(metadata,
            new ResidencyReportTotals(1, 1, 0), List.of(new ResidencyReportRow("=123", "+A01", name, "RESIDING"))));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(1, wb.getNumberOfSheets());
            assertFalse(wb.isSheetHidden(0));
            var sheet = wb.getSheetAt(0);
            assertEquals(4, sheet.getRow(9).getLastCellNum());
            var row = sheet.getRow(10);
            assertEquals(4, row.getLastCellNum());
            assertEquals("=123", row.getCell(0).getStringCellValue());
            assertEquals("+A01", row.getCell(1).getStringCellValue());
            assertEquals(name, row.getCell(2).getStringCellValue());
            for (var r : sheet) for (var cell : r) {
                assertNotEquals(CellType.FORMULA, cell.getCellType());
                assertNull(cell.getCellComment());
                assertFalse(sheet.isColumnHidden(cell.getColumnIndex()));
            }
            assertTrue(wb.getExternalLinksTable().isEmpty());
            for (var part : wb.getPackage().getParts()) {
                String path = part.getPartName().getName();
                assertFalse(path.contains("vbaProject"));
                assertFalse(path.startsWith("/xl/externalLinks/"));
                assertFalse(path.startsWith("/xl/comments"));
            }
        }
    }

    @Test
    void cashLargeIdAndAmountsRemainExactTextAndCodesAreNotFormulas() throws Exception {
        var metadata = new ReportMetadata("CASH_RECEIPTS_V1", "1.0", Instant.parse("2026-11-10T10:00:00Z"), "Tiền mặt theo ngày thực thu");
        var amount = new BigDecimal("999999999999999999999999");
        var row = new CashReceiptReportRow(9007199254740993L, "2026-10", "@house", "-A01", LocalDate.parse("2026-10-31"), amount, amount, BigDecimal.ZERO);
        byte[] bytes = new XlsxReportWriter().writeCashReceiptsReport(new CashReceiptReportData(metadata, LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"), new CashReceiptReportTotals(1, amount), List.of(row)));
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var cells = wb.getSheet("PhieuThu").getRow(1);
            assertEquals("9007199254740993", cells.getCell(0).getStringCellValue());
            assertEquals("@house", cells.getCell(2).getStringCellValue());
            assertEquals("-A01", cells.getCell(3).getStringCellValue());
            assertEquals("2026-10-31", cells.getCell(4).getStringCellValue());
            assertEquals("999999999999999999999999", cells.getCell(5).getStringCellValue());
            for (var cell : cells) assertEquals(CellType.STRING, cell.getCellType());
            assertEquals("999999999999999999999999", wb.getSheet("TongHop").getRow(8).getCell(4).getStringCellValue());
        }
    }
}
