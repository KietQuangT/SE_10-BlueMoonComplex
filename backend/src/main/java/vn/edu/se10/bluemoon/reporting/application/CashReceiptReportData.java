package vn.edu.se10.bluemoon.reporting.application;

import java.time.LocalDate;
import java.util.List;

public record CashReceiptReportData(
    ReportMetadata metadata,
    LocalDate from,
    LocalDate to,
    CashReceiptReportTotals totals,
    List<CashReceiptReportRow> rows
) {}
