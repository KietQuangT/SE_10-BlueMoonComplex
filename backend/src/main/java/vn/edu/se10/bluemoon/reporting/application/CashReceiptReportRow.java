package vn.edu.se10.bluemoon.reporting.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashReceiptReportRow(
    Long receiptId,
    String invoicePeriod,
    String householdCode,
    String apartmentCode,
    LocalDate paidOn,
    BigDecimal amount,
    BigDecimal appliedAmount,
    BigDecimal initialCreditAmount
) {}
