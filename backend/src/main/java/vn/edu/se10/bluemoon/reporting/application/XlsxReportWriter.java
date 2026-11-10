package vn.edu.se10.bluemoon.reporting.application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class XlsxReportWriter {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX").withZone(VIETNAM_ZONE);
    private static final DateTimeFormatter UTC_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @FunctionalInterface
    interface WorkbookConsumer {
        void accept(SXSSFWorkbook workbook) throws IOException;
    }

    protected SXSSFWorkbook createWorkbookInstance() {
        return new SXSSFWorkbook(100);
    }

    private byte[] createWorkbook(WorkbookConsumer consumer) {
        try (SXSSFWorkbook workbook = createWorkbookInstance()) {
            try {
                consumer.accept(workbook);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                workbook.write(out);
                return out.toByteArray();
            } finally {
                workbook.dispose();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lỗi ghi dữ liệu báo cáo XLSX", e);
        }
    }

    public byte[] writeFeeCollectionReport(FeeCollectionReportData data) {
        return createWorkbook(workbook -> {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            // 1. Sheet TongHop
            SXSSFSheet tongHop = workbook.createSheet("TongHop");
            writeMetadataRow(tongHop, 0, "Mã mẫu", data.metadata().templateId(), boldStyle);
            writeMetadataRow(tongHop, 1, "Phiên bản", data.metadata().version(), boldStyle);
            writeMetadataRow(tongHop, 2, "Phạm vi", data.metadata().scopeDescription(), boldStyle);
            writeMetadataRow(tongHop, 3, "Đơn vị tiền tệ", "VND", boldStyle);
            writeMetadataRow(tongHop, 4, "Thời điểm xuất (UTC)", UTC_TIME_FORMATTER.format(data.metadata().exportedAt()), boldStyle);
            writeMetadataRow(tongHop, 5, "Thời điểm xuất (Asia/Ho_Chi_Minh)", LOCAL_TIME_FORMATTER.format(data.metadata().exportedAt()), boldStyle);

            // Row 6: blank
            tongHop.createRow(6);

            // Row 7: Header
            Row thHeader = tongHop.createRow(7);
            String[] thCols = {"Kỳ", "Thời điểm xuất", "Số invoice ISSUED", "Tổng phải thu", "Đã thanh toán", "Còn nợ", "Tỷ lệ %"};
            for (int i = 0; i < thCols.length; i++) {
                createStyledCell(thHeader, i, thCols[i], headerStyle);
            }

            // Row 8: Data row
            Row thData = tongHop.createRow(8);
            setCell(thData, 0, data.period());
            setCell(thData, 1, LOCAL_TIME_FORMATTER.format(data.metadata().exportedAt()));
            setCell(thData, 2, String.valueOf(data.totals().invoiceCount()));
            setCell(thData, 3, data.totals().totalDue().toPlainString());
            setCell(thData, 4, data.totals().totalSettled().toPlainString());
            setCell(thData, 5, data.totals().totalRemaining().toPlainString());
            String ratioText = data.totals().ratioPercent() == null ? "—" : data.totals().ratioPercent().setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
            setCell(thData, 6, ratioText);

            tongHop.createFreezePane(0, 8);
            tongHop.setAutoFilter(new CellRangeAddress(7, 8, 0, thCols.length - 1));

            // 2. Sheet HoaDon
            SXSSFSheet hoaDon = workbook.createSheet("HoaDon");
            Row hdHeader = hoaDon.createRow(0);
            String[] hdCols = {"Mã hóa đơn", "Mã hộ", "Căn hộ", "Hạn nộp", "Phải thu", "Đã thanh toán", "Còn nợ", "Trạng thái thanh toán"};
            for (int i = 0; i < hdCols.length; i++) {
                createStyledCell(hdHeader, i, hdCols[i], headerStyle);
            }

            int rowIdx = 1;
            for (FeeCollectionReportRow r : data.rows()) {
                Row row = hoaDon.createRow(rowIdx++);
                setCell(row, 0, String.valueOf(r.invoiceId()));
                setCell(row, 1, r.householdCode());
                setCell(row, 2, r.apartmentCode());
                setCell(row, 3, r.dueDate().toString());
                setCell(row, 4, r.total().toPlainString());
                setCell(row, 5, r.settled().toPlainString());
                setCell(row, 6, r.remaining().toPlainString());
                setCell(row, 7, r.paymentStatus());
            }

            hoaDon.createFreezePane(0, 1);
            hoaDon.setAutoFilter(new CellRangeAddress(0, Math.max(0, data.rows().size()), 0, hdCols.length - 1));
        });
    }

    public byte[] writeCashReceiptsReport(CashReceiptReportData data) {
        return createWorkbook(workbook -> {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            // 1. Sheet TongHop
            SXSSFSheet tongHop = workbook.createSheet("TongHop");
            writeMetadataRow(tongHop, 0, "Mã mẫu", data.metadata().templateId(), boldStyle);
            writeMetadataRow(tongHop, 1, "Phiên bản", data.metadata().version(), boldStyle);
            writeMetadataRow(tongHop, 2, "Phạm vi", data.metadata().scopeDescription(), boldStyle);
            writeMetadataRow(tongHop, 3, "Đơn vị tiền tệ", "VND", boldStyle);
            writeMetadataRow(tongHop, 4, "Thời điểm xuất (UTC)", UTC_TIME_FORMATTER.format(data.metadata().exportedAt()), boldStyle);
            writeMetadataRow(tongHop, 5, "Thời điểm xuất (Asia/Ho_Chi_Minh)", LOCAL_TIME_FORMATTER.format(data.metadata().exportedAt()), boldStyle);

            // Row 6: blank
            tongHop.createRow(6);

            // Row 7: Header
            Row thHeader = tongHop.createRow(7);
            String[] thCols = {"Từ ngày", "Đến ngày", "Thời điểm xuất", "Số receipt POSTED", "Tổng tiền mặt nhận"};
            for (int i = 0; i < thCols.length; i++) {
                createStyledCell(thHeader, i, thCols[i], headerStyle);
            }

            // Row 8: Data row
            Row thData = tongHop.createRow(8);
            setCell(thData, 0, data.from().toString());
            setCell(thData, 1, data.to().toString());
            setCell(thData, 2, LOCAL_TIME_FORMATTER.format(data.metadata().exportedAt()));
            setCell(thData, 3, String.valueOf(data.totals().receiptCount()));
            setCell(thData, 4, data.totals().cashReceived().toPlainString());

            tongHop.createFreezePane(0, 8);
            tongHop.setAutoFilter(new CellRangeAddress(7, 8, 0, thCols.length - 1));

            // 2. Sheet PhieuThu
            SXSSFSheet phieuThu = workbook.createSheet("PhieuThu");
            Row ptHeader = phieuThu.createRow(0);
            String[] ptCols = {"Mã receipt", "Kỳ hóa đơn nguồn", "Mã hộ", "Căn hộ", "Ngày thực thu paidOn", "Tiền nhận", "Phần áp dụng trực tiếp", "Credit phát sinh ban đầu"};
            for (int i = 0; i < ptCols.length; i++) {
                createStyledCell(ptHeader, i, ptCols[i], headerStyle);
            }

            int rowIdx = 1;
            for (CashReceiptReportRow r : data.rows()) {
                Row row = phieuThu.createRow(rowIdx++);
                setCell(row, 0, String.valueOf(r.receiptId()));
                setCell(row, 1, r.invoicePeriod());
                setCell(row, 2, r.householdCode());
                setCell(row, 3, r.apartmentCode());
                setCell(row, 4, r.paidOn().toString());
                setCell(row, 5, r.amount().toPlainString());
                setCell(row, 6, r.appliedAmount().toPlainString());
                setCell(row, 7, r.initialCreditAmount().toPlainString());
            }

            phieuThu.createFreezePane(0, 1);
            phieuThu.setAutoFilter(new CellRangeAddress(0, Math.max(0, data.rows().size()), 0, ptCols.length - 1));
        });
    }

    public byte[] writeResidencyReport(ResidencyReportData data) {
        return createWorkbook(workbook -> {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            // Sheet CuTru
            SXSSFSheet cuTru = workbook.createSheet("CuTru");
            writeMetadataRow(cuTru, 0, "Mã mẫu", data.metadata().templateId(), boldStyle);
            writeMetadataRow(cuTru, 1, "Phiên bản", data.metadata().version(), boldStyle);
            writeMetadataRow(cuTru, 2, "Phạm vi", data.metadata().scopeDescription(), boldStyle);
            writeMetadataRow(cuTru, 3, "Thời điểm xuất (UTC)", UTC_TIME_FORMATTER.format(data.metadata().exportedAt()), boldStyle);
            writeMetadataRow(cuTru, 4, "Thời điểm xuất (Asia/Ho_Chi_Minh)", LOCAL_TIME_FORMATTER.format(data.metadata().exportedAt()), boldStyle);
            writeMetadataRow(cuTru, 5, "Tổng người", String.valueOf(data.totals().residentCount()), boldStyle);
            writeMetadataRow(cuTru, 6, "Số RESIDING", String.valueOf(data.totals().residingCount()), boldStyle);
            writeMetadataRow(cuTru, 7, "Số TEMPORARILY_ABSENT", String.valueOf(data.totals().temporarilyAbsentCount()), boldStyle);

            // Row 8: blank
            cuTru.createRow(8);

            // Row 9: Header
            Row ctHeader = cuTru.createRow(9);
            String[] ctCols = {"Mã hộ", "Căn hộ", "Họ tên", "Trạng thái"};
            for (int i = 0; i < ctCols.length; i++) {
                createStyledCell(ctHeader, i, ctCols[i], headerStyle);
            }

            int rowIdx = 10;
            for (ResidencyReportRow r : data.rows()) {
                Row row = cuTru.createRow(rowIdx++);
                setCell(row, 0, r.householdCode());
                setCell(row, 1, r.apartmentCode());
                setCell(row, 2, r.fullName());
                String statusLabel = "RESIDING".equals(r.residencyStatus()) ? "Đang cư trú" : "Tạm vắng";
                setCell(row, 3, statusLabel);
            }

            cuTru.createFreezePane(0, 10);
            cuTru.setAutoFilter(new CellRangeAddress(9, Math.max(9, 9 + data.rows().size()), 0, ctCols.length - 1));
        });
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

    private static CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createBoldStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
