package vn.edu.se10.bluemoon.billing.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;

public class FeeCalculator {
    public BigDecimal calculate(CalculationMethod method, BigDecimal areaM2, BigDecimal unitPrice) {
        BigDecimal amount = method == CalculationMethod.AREA ? areaM2.multiply(unitPrice) : unitPrice;
        return amount.setScale(0, RoundingMode.HALF_UP);
    }
}
