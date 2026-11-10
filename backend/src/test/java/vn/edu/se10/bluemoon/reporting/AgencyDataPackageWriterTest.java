package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageWriter;
import vn.edu.se10.bluemoon.reporting.application.AgencyPackageRow;

class AgencyDataPackageWriterTest {
    private final AgencyDataPackageWriter writer = new AgencyDataPackageWriter();
    private final Instant now = Instant.parse("2026-11-10T10:00:00Z");

    @Test
    void writeAndParse_standardRows_matchesContractLayout() throws Exception {
        List<AgencyPackageRow> rows = List.of(
            new AgencyPackageRow("001", "A101", "Nguyễn Văn A", "RESIDING"),
            new AgencyPackageRow("002", "B202", "Trần Thị B", "TEMPORARILY_ABSENT")
        );

        byte[] bytes = writer.writePackage(now, 2, 1, 1, rows);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        String hash = AgencyDataPackageWriter.computeSha256(bytes);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("^[0-9a-f]{64}$"));

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertEquals(1, wb.getNumberOfSheets());
            Sheet sheet = wb.getSheet("CuTru");
            assertNotNull(sheet);

            // Metadata rows 0-7
            assertEquals("Mã mẫu", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("AGENCY_RESIDENCY_V1", sheet.getRow(0).getCell(1).getStringCellValue());

            assertEquals("Phiên bản", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("1.0", sheet.getRow(1).getCell(1).getStringCellValue());

            assertEquals("Phiên bản nội dung", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("1", sheet.getRow(2).getCell(1).getStringCellValue());

            assertEquals("Thời điểm snapshot (UTC)", sheet.getRow(3).getCell(0).getStringCellValue());
            assertEquals("2026-11-10T10:00:00Z", sheet.getRow(3).getCell(1).getStringCellValue());

            assertEquals("Thời điểm snapshot (Asia/Ho_Chi_Minh)", sheet.getRow(4).getCell(0).getStringCellValue());
            assertEquals("2026-11-10 17:00:00 +07:00", sheet.getRow(4).getCell(1).getStringCellValue());

            assertEquals("Tổng người", sheet.getRow(5).getCell(0).getStringCellValue());
            assertEquals("2", sheet.getRow(5).getCell(1).getStringCellValue());

            assertEquals("Số RESIDING", sheet.getRow(6).getCell(0).getStringCellValue());
            assertEquals("1", sheet.getRow(6).getCell(1).getStringCellValue());

            assertEquals("Số TEMPORARILY_ABSENT", sheet.getRow(7).getCell(0).getStringCellValue());
            assertEquals("1", sheet.getRow(7).getCell(1).getStringCellValue());

            // Header row 9
            Row header = sheet.getRow(9);
            assertEquals("Mã hộ", header.getCell(0).getStringCellValue());
            assertEquals("Căn hộ", header.getCell(1).getStringCellValue());
            assertEquals("Họ tên", header.getCell(2).getStringCellValue());
            assertEquals("Trạng thái", header.getCell(3).getStringCellValue());

            // Data rows 10, 11
            Row r1 = sheet.getRow(10);
            assertEquals("001", r1.getCell(0).getStringCellValue());
            assertEquals("A101", r1.getCell(1).getStringCellValue());
            assertEquals("Nguyễn Văn A", r1.getCell(2).getStringCellValue());
            assertEquals("Đang cư trú", r1.getCell(3).getStringCellValue());

            Row r2 = sheet.getRow(11);
            assertEquals("002", r2.getCell(0).getStringCellValue());
            assertEquals("B202", r2.getCell(1).getStringCellValue());
            assertEquals("Trần Thị B", r2.getCell(2).getStringCellValue());
            assertEquals("Tạm vắng", r2.getCell(3).getStringCellValue());
        }

        // Parse workbook rows
        List<AgencyPackageRow> parsed = writer.parseWorkbookRows(bytes);
        assertEquals(2, parsed.size());
        assertEquals("001", parsed.get(0).householdCode());
        assertEquals("A101", parsed.get(0).apartmentCode());
        assertEquals("Nguyễn Văn A", parsed.get(0).fullName());
        assertEquals("RESIDING", parsed.get(0).residencyStatus());

        assertEquals("002", parsed.get(1).householdCode());
        assertEquals("B202", parsed.get(1).apartmentCode());
        assertEquals("Trần Thị B", parsed.get(1).fullName());
        assertEquals("TEMPORARILY_ABSENT", parsed.get(1).residencyStatus());
    }

    @Test
    void writePackage_formulaInjection_renderedAsLiteralString() throws Exception {
        // Test formula injection characters: =, +, -, @
        List<AgencyPackageRow> rows = List.of(
            new AgencyPackageRow("001", "A01", "=1+1", "RESIDING"),
            new AgencyPackageRow("002", "A02", "@SUM(A1:A10)", "RESIDING"),
            new AgencyPackageRow("003", "A03", "-cmd", "RESIDING"),
            new AgencyPackageRow("004", "A04", "+calc", "RESIDING")
        );

        byte[] bytes = writer.writePackage(now, 4, 4, 0, rows);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheet("CuTru");
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.getRow(10 + i);
                assertEquals(CellType.STRING, row.getCell(2).getCellType());
                assertEquals(rows.get(i).fullName(), row.getCell(2).getStringCellValue());
            }
        }
    }

    @Test
    void writePackage_emptyRows_validWorkbook() {
        byte[] bytes = writer.writePackage(now, 0, 0, 0, List.of());
        assertNotNull(bytes);

        List<AgencyPackageRow> parsed = writer.parseWorkbookRows(bytes);
        assertTrue(parsed.isEmpty());
    }
}
