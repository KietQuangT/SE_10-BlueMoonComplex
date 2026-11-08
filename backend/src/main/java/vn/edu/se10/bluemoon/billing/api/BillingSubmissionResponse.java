package vn.edu.se10.bluemoon.billing.api;

import java.time.Instant;

public record BillingSubmissionResponse(Long revision, Instant submittedAt, Long submittedBy, long householdCount,
                                        String total, long version, PageResponse<BillingHouseholdResponse> households) { }
