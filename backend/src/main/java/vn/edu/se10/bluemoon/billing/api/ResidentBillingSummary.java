package vn.edu.se10.bluemoon.billing.api;

public record ResidentBillingSummary(
        String totalOutstanding,
        long outstandingInvoiceCount,
        String nearestDueDate,
        String nearestDuePeriod,
        String creditBalance
) {}
