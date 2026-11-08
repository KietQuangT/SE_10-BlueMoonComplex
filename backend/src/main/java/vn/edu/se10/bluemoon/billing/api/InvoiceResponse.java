package vn.edu.se10.bluemoon.billing.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record InvoiceResponse(long id, long billingPeriodId, long householdId, String householdCode, String apartmentCode,
                              String period, LocalDate dueDate, String status, Instant issuedAt, Instant cancelledAt,
                              List<BillingLineResponse> lines, String total) { }
