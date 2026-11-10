package vn.edu.se10.bluemoon.reporting.application;

import java.math.BigDecimal;

public record CashReceiptReportTotals(
    long receiptCount,
    BigDecimal cashReceived
) {}
