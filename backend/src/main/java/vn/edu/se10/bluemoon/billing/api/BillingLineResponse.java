package vn.edu.se10.bluemoon.billing.api;

import java.math.BigDecimal;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;

public record BillingLineResponse(String feeTypeCode, String feeTypeName, CalculationMethod calculationMethod,
                                  String quantity, String unit, String areaM2, String unitPrice, long feeRateId, String amount) {
    public static BillingLineResponse of(String code, String name, CalculationMethod method, BigDecimal quantity,
                                         BigDecimal area, BigDecimal price, long rateId, BigDecimal amount) {
        return new BillingLineResponse(code, name, method, quantity.toPlainString(), method == CalculationMethod.AREA ? "M2" : "HOUSEHOLD",
                area == null ? null : area.toPlainString(), price.toPlainString(), rateId, amount.toPlainString());
    }
}
