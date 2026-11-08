package vn.edu.se10.bluemoon.billing.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record BillingPeriodResponse(long id, String period, LocalDate dueDate, String status, long version,
                                    long latestRevision, boolean expired, boolean editable, List<String> allowedActions,
                                    Long householdCount, String total, Instant createdAt, Instant submittedAt,
                                    Instant issuedAt, Instant cancelledAt, String returnReason, String cancelReason,
                                    Long replacesPeriodId, long receiptCount) { }
