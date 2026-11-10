package vn.edu.se10.bluemoon.billing.api;

public record ResidentReceipt(
        long receiptId,
        long invoiceId,
        String period,
        String paidOn,
        String recordedAt,
        String amount,
        String appliedAmount,
        String creditAmount,
        String status,
        String voidedAt,
        boolean hasLateReason,
        boolean hasVoidReason
) {}
