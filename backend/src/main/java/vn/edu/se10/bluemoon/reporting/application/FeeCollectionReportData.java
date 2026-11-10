package vn.edu.se10.bluemoon.reporting.application;

import java.util.List;

public record FeeCollectionReportData(
    ReportMetadata metadata,
    String period,
    FeeCollectionReportTotals totals,
    List<FeeCollectionReportRow> rows
) {}
