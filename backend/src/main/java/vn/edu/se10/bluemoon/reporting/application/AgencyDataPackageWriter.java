package vn.edu.se10.bluemoon.reporting.application;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class AgencyDataPackageWriter {
    public static final String TEMPLATE_ID = "AGENCY_RESIDENCY_V1";
    public static final String TEMPLATE_VERSION = "1.0";
    public static final String CONTENT_VERSION = "1";

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX").withZone(VIETNAM_ZONE);
    private static final DateTimeFormatter UTC_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public static final int[] COLUMN_WIDTHS = {36, 32, 32, 18};

    public byte[] writePackage(Instant snapshotAt, long residentCount, long residingCount, long temporarilyAbsentCount, List<AgencyPackageRow> rows) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            try {
                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle boldStyle = createBoldStyle(workbook);
                CellStyle nameWrapStyle = createWrapStyle(workbook);

                SXSSFSheet sheet = workbook.createSheet("CuTru");
                setColumnWidths(sheet, COLUMN_WIDTHS);

                writeMetadataRow(sheet, 0, "Mã mẫu", TEMPLATE_ID, boldStyle);
                writeMetadataRow(sheet, 1, "Phiên bản", TEMPLATE_VERSION, boldStyle);
                writeMetadataRow(sheet, 2, "Phiên bản nội dung", CONTENT_VERSION, boldStyle);
                writeMetadataRow(sheet, 3, "Thời điểm snapshot (UTC)", UTC_TIME_FORMATTER.format(snapshotAt), boldStyle);
                writeMetadataRow(sheet, 4, "Thời điểm snapshot (Asia/Ho_Chi_Minh)", LOCAL_TIME_FORMATTER.format(snapshotAt), boldStyle);
                writeMetadataRow(sheet, 5, "Tổng người", String.valueOf(residentCount), boldStyle);
                writeMetadataRow(sheet, 6, "Số RESIDING", String.valueOf(residingCount), boldStyle);
                writeMetadataRow(sheet, 7, "Số TEMPORARILY_ABSENT", String.valueOf(temporarilyAbsentCount), boldStyle);

                // Row 8: blank
                sheet.createRow(8);

                // Row 9: Header
                Row headerRow = sheet.createRow(9);
                String[] cols = {"Mã hộ", "Căn hộ", "Họ tên", "Trạng thái"};
                for (int i = 0; i < cols.length; i++) {
                    createStyledCell(headerRow, i, cols[i], headerStyle);
                }

                // Row 10+: Data
                int rowIdx = 10;
                for (AgencyPackageRow r : rows) {
                    Row row = sheet.createRow(rowIdx++);
                    setCell(row, 0, r.householdCode());
                    setCell(row, 1, r.apartmentCode());
                    createStyledCell(row, 2, r.fullName(), nameWrapStyle);
                    if (r.fullName() != null && r.fullName().length() > 28) {
                        int lines = (int) Math.ceil((double) r.fullName().length() / 28.0);
                        row.setHeightInPoints(Math.max(15.0f, lines * 15.0f));
                    }
                    String statusLabel = "RESIDING".equals(r.residencyStatus()) ? "Đang cư trú" : "Tạm vắng";
                    setCell(row, 3, statusLabel);
                }

                sheet.createFreezePane(0, 10);
                sheet.setAutoFilter(new CellRangeAddress(9, Math.max(9, 9 + rows.size()), 0, cols.length - 1));

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                return out.toByteArray();
            } finally {
                workbook.dispose();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lỗi ghi dữ liệu gói cơ quan XLSX", e);
        }
    }

    public List<AgencyPackageRow> parseWorkbookRows(byte[] fileBytes) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getSheet("CuTru");
            if (sheet == null) {
                throw new IllegalStateException("Sheet CuTru không tồn tại");
            }
            List<AgencyPackageRow> rows = new ArrayList<>();
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 10; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Cell c0 = row.getCell(0);
                Cell c1 = row.getCell(1);
                Cell c2 = row.getCell(2);
                Cell c3 = row.getCell(3);

                String hhCode = getCellString(c0);
                String aptCode = getCellString(c1);
                String fullName = getCellString(c2);
                String statusLabel = getCellString(c3);

                String residencyStatus = "Đang cư trú".equals(statusLabel) ? "RESIDING" : "TEMPORARILY_ABSENT";
                rows.add(new AgencyPackageRow(hhCode, aptCode, fullName, residencyStatus));
            }
            return rows;
        } catch (IOException e) {
            throw new IllegalStateException("Lỗi đọc dữ liệu gói cơ quan XLSX", e);
        }
    }

    public static String computeSha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder(64);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    private static String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return cell.getStringCellValue();
    }

    private static void writeMetadataRow(Sheet sheet, int rowIdx, String label, String value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowIdx);
        Cell c0 = row.createCell(0, CellType.STRING);
        c0.setCellValue(label);
        c0.setCellStyle(labelStyle);

        Cell c1 = row.createCell(1, CellType.STRING);
        c1.setCellValue(value != null ? value : "");
    }

    private static void createStyledCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private static void setCell(Row row, int colIndex, String value) {
        Cell cell = row.createCell(colIndex, CellType.STRING);
        cell.setCellValue(value != null ? value : "");
    }

    private static void setColumnWidths(Sheet sheet, int[] widthsInChars) {
        for (int i = 0; i < widthsInChars.length; i++) {
            sheet.setColumnWidth(i, widthsInChars[i] * 256);
        }
    }

    private static CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }

    private static CellStyle createWrapStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }

    private static CellStyle createBoldStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        return style;
    }
}
