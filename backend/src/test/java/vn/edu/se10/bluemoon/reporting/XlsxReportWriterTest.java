package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.reporting.application.*;

class XlsxReportWriterTest {
    private XlsxReportWriter writer;
    private Instant testInstant;

    @BeforeEach
    void setUp() {
        writer = new XlsxReportWriter();
        testInstant = Instant.parse("2026-11-10T09:52:00Z");
    }

    @Test
    void writeFeeCollectionReport_createsValidWorkbookWithCorrectStructure() throws IOException {
        var metadata = new ReportMetadata("FEE_COLLECTION_V1", "1.0", testInstant, "Thu theo kỳ hóa đơn");
        var rows = List.of(
            new FeeCollectionReportRow(1001L, "00123", "P101", LocalDate.of(2026, 10, 31),
                new BigDecimal("530000"), new BigDecimal("200000"), new BigDecimal("330000"), "PARTIALLY_PAID"),
            new FeeCollectionReportRow(1002L, "00124", "P102", LocalDate.of(2026, 10, 31),
                new BigDecimal("100000"), new BigDecimal("100000"), BigDecimal.ZERO, "PAID"),
            new FeeCollectionReportRow(1003L, "00125", "P103", LocalDate.of(2026, 10, 31),
                new BigDecimal("999999999999999999999999"), BigDecimal.ZERO, new BigDecimal("999999999999999999999999"), "UNPAID")
        );
        var totals = new FeeCollectionReportTotals(3, new BigDecimal("1000000000000000000629999"),
            new BigDecimal("300000"), new BigDecimal("1000000000000000000329999"), new BigDecimal("37.74"));
        var data = new FeeCollectionReportData(metadata, "2026-10", totals, rows);

        byte[] bytes = writer.writeFeeCollectionReport(data);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, wb.getNumberOfSheets());
            assertEquals("TongHop", wb.getSheetName(0));
            assertEquals("HoaDon", wb.getSheetName(1));

            // Check TongHop metadata
            Sheet tongHop = wb.getSheetAt(0);
            assertEquals("Mã mẫu", getCell(tongHop, 0, 0));
            assertEquals("FEE_COLLECTION_V1", getCell(tongHop, 0, 1));
            assertEquals("Phiên bản", getCell(tongHop, 1, 0));
            assertEquals("1.0", getCell(tongHop, 1, 1));

            // Check TongHop table header
            assertEquals("Kỳ", getCell(tongHop, 7, 0));
            assertEquals("Tỷ lệ %", getCell(tongHop, 7, 6));

            // Check TongHop data
            assertEquals("2026-10", getCell(tongHop, 8, 0));
            assertEquals("3", getCell(tongHop, 8, 2));
            assertEquals("37.74%", getCell(tongHop, 8, 6));

            // Check HoaDon headers
            Sheet hoaDon = wb.getSheetAt(1);
            assertEquals("Mã hóa đơn", getCell(hoaDon, 0, 0));
            assertEquals("Mã hộ", getCell(hoaDon, 0, 1));
            assertEquals("Căn hộ", getCell(hoaDon, 0, 2));
            assertEquals("Hạn nộp", getCell(hoaDon, 0, 3));
            assertEquals("Phải thu", getCell(hoaDon, 0, 4));
            assertEquals("Đã thanh toán", getCell(hoaDon, 0, 5));
            assertEquals("Còn nợ", getCell(hoaDon, 0, 6));
            assertEquals("Trạng thái thanh toán", getCell(hoaDon, 0, 7));

            // Check HoaDon data rows
            assertEquals("1001", getCell(hoaDon, 1, 0));
            assertEquals("00123", getCell(hoaDon, 1, 1)); // Leading zero preserved!
            assertEquals("PARTIALLY_PAID", getCell(hoaDon, 1, 7));

            // Check 24-digit exact money
            assertEquals("999999999999999999999999", getCell(hoaDon, 3, 4));
            assertEquals("UNPAID", getCell(hoaDon, 3, 7));

            // Ensure all cells are STRING type
            for (int r = 0; r <= 3; r++) {
                Row row = hoaDon.getRow(r);
                for (int c = 0; c < 8; c++) {
                    assertEquals(CellType.STRING, row.getCell(c).getCellType());
                }
            }
        }
    }

    @Test
    void writeFeeCollectionReport_emptyDataProducesDashRatio() throws IOException {
        var metadata = new ReportMetadata("FEE_COLLECTION_V1", "1.0", testInstant, "Thu theo kỳ hóa đơn");
        var totals = new FeeCollectionReportTotals(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
        var data = new FeeCollectionReportData(metadata, "2026-10", totals, List.of());

        byte[] bytes = writer.writeFeeCollectionReport(data);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet tongHop = wb.getSheetAt(0);
            assertEquals("0", getCell(tongHop, 8, 2));
            assertEquals("0", getCell(tongHop, 8, 3));
            assertEquals("—", getCell(tongHop, 8, 6));

            Sheet hoaDon = wb.getSheetAt(1);
            assertEquals("Mã hóa đơn", getCell(hoaDon, 0, 0));
            assertNull(hoaDon.getRow(1)); // Only header row
        }
    }

    @Test
    void writeCashReceiptsReport_createsValidWorkbook() throws IOException {
        var metadata = new ReportMetadata("CASH_RECEIPTS_V1", "1.0", testInstant, "Tiền mặt theo ngày thực thu");
        var rows = List.of(
            new CashReceiptReportRow(501L, "2026-10", "00456", "B202", LocalDate.of(2026, 10, 15),
                new BigDecimal("600000"), new BigDecimal("530000"), new BigDecimal("70000"))
        );
        var totals = new CashReceiptReportTotals(1, new BigDecimal("600000"));
        var data = new CashReceiptReportData(metadata, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), totals, rows);

        byte[] bytes = writer.writeCashReceiptsReport(data);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, wb.getNumberOfSheets());
            assertEquals("TongHop", wb.getSheetName(0));
            assertEquals("PhieuThu", wb.getSheetName(1));

            Sheet tongHop = wb.getSheetAt(0);
            assertEquals("CASH_RECEIPTS_V1", getCell(tongHop, 0, 1));
            assertEquals("2026-10-01", getCell(tongHop, 8, 0));
            assertEquals("2026-10-31", getCell(tongHop, 8, 1));
            assertEquals("1", getCell(tongHop, 8, 3));
            assertEquals("600000", getCell(tongHop, 8, 4));

            Sheet phieuThu = wb.getSheetAt(1);
            assertEquals("Mã receipt", getCell(phieuThu, 0, 0));
            assertEquals("Kỳ hóa đơn nguồn", getCell(phieuThu, 0, 1));
            assertEquals("Mã hộ", getCell(phieuThu, 0, 2));
            assertEquals("Căn hộ", getCell(phieuThu, 0, 3));
            assertEquals("Ngày thực thu paidOn", getCell(phieuThu, 0, 4));
            assertEquals("Tiền nhận", getCell(phieuThu, 0, 5));
            assertEquals("Phần áp dụng trực tiếp", getCell(phieuThu, 0, 6));
            assertEquals("Credit phát sinh ban đầu", getCell(phieuThu, 0, 7));

            assertEquals("501", getCell(phieuThu, 1, 0));
            assertEquals("2026-10", getCell(phieuThu, 1, 1));
            assertEquals("00456", getCell(phieuThu, 1, 2));
            assertEquals("600000", getCell(phieuThu, 1, 5));
            assertEquals("530000", getCell(phieuThu, 1, 6));
            assertEquals("70000", getCell(phieuThu, 1, 7));
        }
    }

    @Test
    void writeResidencyReport_preservesFormulaLikeStringsAsLiterals() throws IOException {
        var metadata = new ReportMetadata("CURRENT_RESIDENCY_V1", "1.0", testInstant, "Danh sách cư trú hiện tại");
        var rows = List.of(
            new ResidencyReportRow("00101", "A101", "=SUM(1,2)", "RESIDING"),
            new ResidencyReportRow("00102", "A102", "@AdminUser", "TEMPORARILY_ABSENT"),
            new ResidencyReportRow("00103", "A103", "+84901234567", "RESIDING"),
            new ResidencyReportRow("00104", "A104", "-DangerCmd", "TEMPORARILY_ABSENT")
        );
        var totals = new ResidencyReportTotals(4, 2, 2);
        var data = new ResidencyReportData(metadata, totals, rows);

        byte[] bytes = writer.writeResidencyReport(data);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(1, wb.getNumberOfSheets());
            assertEquals("CuTru", wb.getSheetName(0));

            Sheet sheet = wb.getSheetAt(0);
            assertEquals("CURRENT_RESIDENCY_V1", getCell(sheet, 0, 1));
            assertEquals("4", getCell(sheet, 5, 1));
            assertEquals("2", getCell(sheet, 6, 1));
            assertEquals("2", getCell(sheet, 7, 1));

            // Table headers
            assertEquals("Mã hộ", getCell(sheet, 9, 0));
            assertEquals("Căn hộ", getCell(sheet, 9, 1));
            assertEquals("Họ tên", getCell(sheet, 9, 2));
            assertEquals("Trạng thái", getCell(sheet, 9, 3));

            // Check rows
            assertEquals("=SUM(1,2)", getCell(sheet, 10, 2));
            assertEquals(CellType.STRING, sheet.getRow(10).getCell(2).getCellType());
            assertEquals("Đang cư trú", getCell(sheet, 10, 3));

            assertEquals("@AdminUser", getCell(sheet, 11, 2));
            assertEquals(CellType.STRING, sheet.getRow(11).getCell(2).getCellType());
            assertEquals("Tạm vắng", getCell(sheet, 11, 3));

            assertEquals("+84901234567", getCell(sheet, 12, 2));
            assertEquals(CellType.STRING, sheet.getRow(12).getCell(2).getCellType());

            assertEquals("-DangerCmd", getCell(sheet, 13, 2));
            assertEquals(CellType.STRING, sheet.getRow(13).getCell(2).getCellType());
        }
    }

    @Test
    void largeDataset_streamWritesWithoutError() {
        var metadata = new ReportMetadata("FEE_COLLECTION_V1", "1.0", testInstant, "Thu theo kỳ hóa đơn");
        List<FeeCollectionReportRow> rows = new ArrayList<>(50000);
        for (int i = 1; i <= 50000; i++) {
            rows.add(new FeeCollectionReportRow((long) i, "H" + i, "A" + i, LocalDate.of(2026, 10, 31),
                new BigDecimal("100000"), new BigDecimal("100000"), BigDecimal.ZERO, "PAID"));
        }
        var totals = new FeeCollectionReportTotals(50000, new BigDecimal("5000000000"), new BigDecimal("5000000000"),
            BigDecimal.ZERO, new BigDecimal("100.00"));
        var data = new FeeCollectionReportData(metadata, "2026-10", totals, rows);

        byte[] bytes = writer.writeFeeCollectionReport(data);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void writeSyntheticFilesToTargetDirectory() throws IOException {
        java.nio.file.Path dir = java.nio.file.Path.of("target/synthetic_reports");
        java.nio.file.Files.createDirectories(dir);

        // 1. Fee collection
        var feeMeta = new ReportMetadata("FEE_COLLECTION_V1", "1.0", testInstant, "Thu theo kỳ hóa đơn");
        var feeRows = List.of(
            new FeeCollectionReportRow(1L, "00123", "P101", LocalDate.of(2026, 10, 31),
                new BigDecimal("530000"), new BigDecimal("200000"), new BigDecimal("330000"), "PARTIALLY_PAID"),
            new FeeCollectionReportRow(2L, "00124", "P102", LocalDate.of(2026, 10, 31),
                new BigDecimal("100000"), new BigDecimal("100000"), BigDecimal.ZERO, "PAID")
        );
        var feeTotals = new FeeCollectionReportTotals(2, new BigDecimal("630000"), new BigDecimal("300000"), new BigDecimal("330000"), new BigDecimal("47.62"));
        byte[] feeBytes = writer.writeFeeCollectionReport(new FeeCollectionReportData(feeMeta, "2026-10", feeTotals, feeRows));
        java.nio.file.Files.write(dir.resolve("thu-theo-ky-2026-10.xlsx"), feeBytes);

        // 2. Cash receipts
        var cashMeta = new ReportMetadata("CASH_RECEIPTS_V1", "1.0", testInstant, "Tiền mặt theo ngày thực thu");
        var cashRows = List.of(
            new CashReceiptReportRow(101L, "2026-10", "00123", "P101", LocalDate.of(2026, 10, 15),
                new BigDecimal("200000"), new BigDecimal("200000"), BigDecimal.ZERO)
        );
        var cashTotals = new CashReceiptReportTotals(1, new BigDecimal("200000"));
        byte[] cashBytes = writer.writeCashReceiptsReport(new CashReceiptReportData(cashMeta, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), cashTotals, cashRows));
        java.nio.file.Files.write(dir.resolve("tien-mat-2026-10-01-2026-10-31.xlsx"), cashBytes);

        // 3. Residency
        var resMeta = new ReportMetadata("CURRENT_RESIDENCY_V1", "1.0", testInstant, "Danh sách cư trú hiện tại");
        var resRows = List.of(
            new ResidencyReportRow("00123", "P101", "Nguyễn Văn An", "RESIDING"),
            new ResidencyReportRow("00123", "P101", "Trần Thị Bình", "TEMPORARILY_ABSENT")
        );
        var resTotals = new ResidencyReportTotals(2, 1, 1);
        byte[] resBytes = writer.writeResidencyReport(new ResidencyReportData(resMeta, resTotals, resRows));
        java.nio.file.Files.write(dir.resolve("cu-tru-hien-tai-20261110.xlsx"), resBytes);

        assertTrue(java.nio.file.Files.exists(dir.resolve("thu-theo-ky-2026-10.xlsx")));
        assertTrue(java.nio.file.Files.exists(dir.resolve("tien-mat-2026-10-01-2026-10-31.xlsx")));
        assertTrue(java.nio.file.Files.exists(dir.resolve("cu-tru-hien-tai-20261110.xlsx")));
    }

    private static String getCell(Sheet sheet, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        return cell.getStringCellValue();
    }
}
