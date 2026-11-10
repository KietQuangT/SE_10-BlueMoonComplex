package vn.edu.se10.bluemoon.reporting.application;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeCollectionReportRow(
    Long invoiceId,
    String householdCode,
    String apartmentCode,
    LocalDate dueDate,
    BigDecimal total,
    BigDecimal settled,
    BigDecimal remaining,
    String paymentStatus
) {}
