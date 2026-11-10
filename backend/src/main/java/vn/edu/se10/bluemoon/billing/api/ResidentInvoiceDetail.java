package vn.edu.se10.bluemoon.billing.api;

import java.util.List;

public record ResidentInvoiceDetail(
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
        String remaining,
        List<ResidentInvoiceLine> lines
) {}
