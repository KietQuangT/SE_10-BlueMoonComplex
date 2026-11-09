package vn.edu.se10.bluemoon.billing.api;

public record ResidentInvoiceSummary(
        long invoiceId,
        String period,
        String householdCode,
        String apartmentCode,
        String dueDate,
        String issuedAt,
        String cancelledAt,
        String invoiceStatus,
        String paymentStatus,
        String total,
        String settled,
        String remaining
) {}
