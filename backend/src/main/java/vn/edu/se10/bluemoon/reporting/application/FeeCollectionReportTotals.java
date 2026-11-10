package vn.edu.se10.bluemoon.reporting.application;

import java.math.BigDecimal;

public record FeeCollectionReportTotals(
    long invoiceCount,
    BigDecimal totalDue,
    BigDecimal totalSettled,
    BigDecimal totalRemaining,
    BigDecimal ratioPercent
) {}
