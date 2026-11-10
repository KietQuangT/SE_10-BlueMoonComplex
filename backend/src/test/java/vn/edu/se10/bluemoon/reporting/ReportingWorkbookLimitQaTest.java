package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import vn.edu.se10.bluemoon.reporting.application.*;

class ReportingWorkbookLimitQaTest {
    private static final int SIZE = 50_000;
    private static final LocalDate DAY = LocalDate.of(2026, 10, 31);
    private static final Instant NOW = Instant.parse("2026-11-10T10:00:00Z");
    private final XlsxReportWriter writer = new XlsxReportWriter();

    @Test
    void feeWorkbookContainsEveryRowAndExactTotals() throws Exception {
        var rows = new ArrayList<FeeCollectionReportRow>(SIZE);
        for (int i = 1; i <= SIZE; i++) rows.add(new FeeCollectionReportRow((long) i, "H" + i, "A" + i, DAY, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(9), "PARTIALLY_PAID"));
        var totals = new FeeCollectionReportTotals(SIZE, new BigDecimal("500000"), new BigDecimal("50000"), new BigDecimal("450000"), new BigDecimal("10.00"));
        byte[] bytes = writer.writeFeeCollectionReport(new FeeCollectionReportData(meta("FEE_COLLECTION_V1"), "2026-10", totals, rows));
        Map<String, ParsedSheet> sheets = parse(bytes);
        var detail = sheets.get("HoaDon");
        assertEquals(SIZE + 1, detail.rowCount);
        assertEquals("50000", detail.selected.get("A50001"));
        var summary = sheets.get("TongHop");
        assertEquals("50000", summary.selected.get("C9"));
        assertEquals("500000", summary.selected.get("D9"));
        assertEquals("50000", summary.selected.get("E9"));
        assertEquals("450000", summary.selected.get("F9"));
        assertEquals("10.00%", summary.selected.get("G9"));
    }

    @Test
    void cashWorkbookContainsEveryRowAndExactTotals() throws Exception {
        var rows = new ArrayList<CashReceiptReportRow>(SIZE);
        for (int i = 1; i <= SIZE; i++) rows.add(new CashReceiptReportRow((long) i, "2026-10", "H" + i, "A" + i, DAY, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(9)));
        byte[] bytes = writer.writeCashReceiptsReport(new CashReceiptReportData(meta("CASH_RECEIPTS_V1"), DAY, DAY, new CashReceiptReportTotals(SIZE, new BigDecimal("500000")), rows));
        Map<String, ParsedSheet> sheets = parse(bytes);
        assertEquals(SIZE + 1, sheets.get("PhieuThu").rowCount);
        assertEquals("50000", sheets.get("PhieuThu").selected.get("A50001"));
        assertEquals("50000", sheets.get("TongHop").selected.get("D9"));
        assertEquals("500000", sheets.get("TongHop").selected.get("E9"));
    }

    @Test
    void residencyWorkbookContainsEveryRowAndExactCounts() throws Exception {
        var rows = new ArrayList<ResidencyReportRow>(SIZE);
        for (int i = 1; i <= SIZE; i++) rows.add(new ResidencyReportRow("H" + i, "A" + i, "Người " + i, i % 2 == 0 ? "RESIDING" : "TEMPORARILY_ABSENT"));
        byte[] bytes = writer.writeResidencyReport(new ResidencyReportData(meta("CURRENT_RESIDENCY_V1"), new ResidencyReportTotals(SIZE, SIZE / 2, SIZE / 2), rows));
        ParsedSheet sheet = parse(bytes).get("CuTru");
        assertEquals(SIZE + 10, sheet.rowCount);
        assertEquals("H50000", sheet.selected.get("A50010"));
        assertEquals("Người 50000", sheet.selected.get("C50010"));
        assertEquals("50000", sheet.selected.get("B6"));
        assertEquals("25000", sheet.selected.get("B7"));
        assertEquals("25000", sheet.selected.get("B8"));
    }

    private static ReportMetadata meta(String id) { return new ReportMetadata(id, "1.0", NOW, "QA"); }

    // SAX reads the actual sheet XML without materializing 400,000 XSSF cells in heap.
    private static Map<String, ParsedSheet> parse(byte[] bytes) throws Exception {
        Map<String, ParsedSheet> sheets = new HashMap<>();
        try (OPCPackage pkg = OPCPackage.open(new ByteArrayInputStream(bytes))) {
            var reader = new XSSFReader(pkg);
            var it = (XSSFReader.SheetIterator) reader.getSheetsData();
            while (it.hasNext()) {
                try (InputStream in = it.next()) {
                    var sheet = new ParsedSheet();
                    SAXParserFactory.newInstance().newSAXParser().parse(in, sheet);
                    sheets.put(it.getSheetName(), sheet);
                }
            }
        }
        return sheets;
    }

    private static class ParsedSheet extends DefaultHandler {
        int rowCount;
        String cell;
        boolean inText;
        StringBuilder value = new StringBuilder();
        Map<String, String> selected = new HashMap<>();
        @Override public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equals("row")) rowCount++;
            if (qName.equals("c")) { cell = attributes.getValue("r"); value.setLength(0); }
            if (qName.equals("t") || qName.equals("v")) inText = true;
        }
        @Override public void characters(char[] ch, int start, int length) { if (inText) value.append(ch, start, length); }
        @Override public void endElement(String uri, String localName, String qName) {
            if (qName.equals("t") || qName.equals("v")) inText = false;
            if (qName.equals("c") && cell != null && (cell.matches("[A-G](6|7|8|9)") || cell.equals("A50001") || cell.equals("A50010") || cell.equals("C50010"))) selected.put(cell, value.toString());
        }
    }
}
