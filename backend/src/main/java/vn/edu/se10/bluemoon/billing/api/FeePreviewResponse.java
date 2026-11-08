package vn.edu.se10.bluemoon.billing.api;

import java.math.BigDecimal;
import java.util.List;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;

public record FeePreviewResponse(long householdId, String period, List<Line> lines, BigDecimal total) {
    public record Line(String feeTypeCode, String feeTypeName, CalculationMethod calculationMethod,
            BigDecimal areaM2, BigDecimal unitPrice, Long feeRateId, BigDecimal amount) { }
}
