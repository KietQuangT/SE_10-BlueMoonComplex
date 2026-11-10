package vn.edu.se10.bluemoon.billing.api;

public record ResidentInvoiceLine(
        String feeTypeCode,
        String feeTypeName,
        String calculationMethod,
        String quantity,
        String unit,
        String areaM2,
        String unitPrice,
        String amount
) {}
