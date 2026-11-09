package vn.edu.se10.bluemoon.billing.api;

public record ResidentAllocation(
        long allocationId,
        long invoiceId,
        String period,
        String amount,
        String status,
        String recordedAt,
        String reversedAt,
        boolean hasReverseReason
) {}
