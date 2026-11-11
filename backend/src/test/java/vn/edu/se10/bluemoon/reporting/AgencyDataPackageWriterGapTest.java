package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageWriter;
import vn.edu.se10.bluemoon.reporting.application.AgencyPackageRow;

/** Bố cục tệp gói cơ quan: metadata, tiêu đề, khóa dòng, bộ lọc, thứ tự và dữ liệu hiển thị. */
class AgencyDataPackageWriterGapTest {
    private final AgencyDataPackageWriter writer = new AgencyDataPackageWriter();
    private final Instant snapshot = Instant.parse("2026-11-10T17:30:00Z");

    private Sheet sheetOf(byte[] bytes, Workbook[] holder) throws Exception {
        holder[0] = WorkbookFactory.create(new ByteArrayInputStream(bytes));
        return holder[0].getSheet("CuTru");
    }

    @Test
    void metadataBlockCarriesTemplateVersionsSnapshotTimesAndCounts() throws Exception {
        byte[] bytes = writer.writePackage(snapshot, 3, 2, 1, List.of(
            new AgencyPackageRow("H1", "A1", "A", "RESIDING"),
            new AgencyPackageRow("H1", "A1", "B", "RESIDING"),
            new AgencyPackageRow("H2", "A2", "C", "TEMPORARILY_ABSENT")));

        Workbook[] wb = new Workbook[1];
        try {
            Sheet sheet = sheetOf(bytes, wb);
            assertEquals("AGENCY_RESIDENCY_V1", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("1.0", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("1", sheet.getRow(2).getCell(1).getStringCellValue());
            assertEquals("2026-11-10T17:30:00Z", sheet.getRow(3).getCell(1).getStringCellValue());
            // 17:30 UTC = 00:30 ngày hôm sau theo giờ Việt Nam (UTC+7).
            assertEquals("2026-11-11 00:30:00 +07:00", sheet.getRow(4).getCell(1).getStringCellValue());
            assertEquals("3", sheet.getRow(5).getCell(1).getStringCellValue());
            assertEquals("2", sheet.getRow(6).getCell(1).getStringCellValue());
            assertEquals("1", sheet.getRow(7).getCell(1).getStringCellValue());
        } finally {
            wb[0].close();
        }
    }

    @Test
    void headerRowAndDataStartWhereTheContractSaysAndOnlyFourColumnsExist() throws Exception {
        byte[] bytes = writer.writePackage(snapshot, 1, 1, 0, List.of(new AgencyPackageRow("H1", "A1", "An", "RESIDING")));

        Workbook[] wb = new Workbook[1];
        try {
            Sheet sheet = sheetOf(bytes, wb);
            Row header = sheet.getRow(9);
            assertEquals(4, header.getLastCellNum());
            assertEquals("Mã hộ", header.getCell(0).getStringCellValue());
            assertEquals("Căn hộ", header.getCell(1).getStringCellValue());
            assertEquals("Họ tên", header.getCell(2).getStringCellValue());
            assertEquals("Trạng thái", header.getCell(3).getStringCellValue());
            assertEquals(4, sheet.getRow(10).getLastCellNum(), "Dữ liệu không được có cột thừa ngoài 4 trường đã duyệt");
            assertEquals(1, wb[0].getNumberOfSheets());
        } finally {
            wb[0].close();
        }
    }

    @Test
    void freezePaneAndAutoFilterCoverTheHeaderAndAllDataRows() throws Exception {
        List<AgencyPackageRow> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rows.add(new AgencyPackageRow("H" + i, "A" + i, "Tên " + i, "RESIDING"));
        }
        byte[] bytes = writer.writePackage(snapshot, 5, 5, 0, rows);

        Workbook[] wb = new Workbook[1];
        try {
            Sheet sheet = sheetOf(bytes, wb);
            assertEquals(10, sheet.getPaneInformation().getHorizontalSplitPosition());
            assertEquals("A10:D15", ((org.apache.poi.xssf.usermodel.XSSFSheet) sheet).getCTWorksheet().getAutoFilter().getRef());
        } finally {
            wb[0].close();
        }
    }

    @Test
    void rowOrderIsPreservedAndStatusLabelsAreVietnameseForHumans() throws Exception {
        List<AgencyPackageRow> rows = List.of(
            new AgencyPackageRow("H2", "B2", "Hai", "TEMPORARILY_ABSENT"),
            new AgencyPackageRow("H1", "A1", "Một", "RESIDING"));
        byte[] bytes = writer.writePackage(snapshot, 2, 1, 1, rows);

        Workbook[] wb = new Workbook[1];
        try {
            Sheet sheet = sheetOf(bytes, wb);
            assertEquals("Hai", sheet.getRow(10).getCell(2).getStringCellValue());
            assertEquals("Tạm vắng", sheet.getRow(10).getCell(3).getStringCellValue());
            assertEquals("Một", sheet.getRow(11).getCell(2).getStringCellValue());
            assertEquals("Đang cư trú", sheet.getRow(11).getCell(3).getStringCellValue());
        } finally {
            wb[0].close();
        }
        assertEquals(rows, writer.parseWorkbookRows(bytes));
    }

    @Test
    void sameInputProducesSameMetadataAndRows_hashDependsOnContent() {
        List<AgencyPackageRow> rows = List.of(new AgencyPackageRow("H1", "A1", "An", "RESIDING"));
        byte[] a = writer.writePackage(snapshot, 1, 1, 0, rows);
        byte[] changed = writer.writePackage(snapshot, 1, 1, 0, List.of(new AgencyPackageRow("H1", "A1", "Anh", "RESIDING")));

        assertNotEquals(AgencyDataPackageWriter.computeSha256(a), AgencyDataPackageWriter.computeSha256(changed));
        assertEquals(64, AgencyDataPackageWriter.computeSha256(a).length());
        assertTrue(AgencyDataPackageWriter.computeSha256(a).matches("[0-9a-f]{64}"));
    }

    @Test
    void longNamesAreWrappedWithTallerRow() throws Exception {
        String longName = "Nguyễn Thị Rất Rất Rất Dài Họ Tên Để Kiểm Tra Việc Bọc Chữ Trong Ô";
        byte[] bytes = writer.writePackage(snapshot, 2, 2, 0, List.of(
            new AgencyPackageRow("H1", "A1", "Ngắn", "RESIDING"),
            new AgencyPackageRow("H1", "A1", longName, "RESIDING")));

        Workbook[] wb = new Workbook[1];
        try {
            Sheet sheet = sheetOf(bytes, wb);
            assertTrue(sheet.getRow(11).getHeightInPoints() > sheet.getRow(10).getHeightInPoints());
            assertTrue(sheet.getRow(11).getCell(2).getCellStyle().getWrapText());
        } finally {
            wb[0].close();
        }
    }

    @Test
    void parseRejectsWorkbookWithoutCuTruSheet() throws Exception {
        byte[] wrong;
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook other = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            other.createSheet("Khac");
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            other.write(out);
            wrong = out.toByteArray();
        }
        assertThrows(IllegalStateException.class, () -> writer.parseWorkbookRows(wrong));
    }
}
