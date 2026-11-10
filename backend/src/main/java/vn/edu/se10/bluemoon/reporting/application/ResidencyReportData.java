package vn.edu.se10.bluemoon.reporting.application;

import java.util.List;

public record ResidencyReportData(
    ReportMetadata metadata,
    ResidencyReportTotals totals,
    List<ResidencyReportRow> rows
) {}
