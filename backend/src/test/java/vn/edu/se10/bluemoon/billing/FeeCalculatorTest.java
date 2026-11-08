package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.application.FeeCalculator;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;

class FeeCalculatorTest {
    @Test
    void roundsEachFeeToWholeDong() {
        FeeCalculator calculator = new FeeCalculator();
        assertEquals(new BigDecimal("500000"), calculator.calculate(CalculationMethod.AREA, new BigDecimal("50"), new BigDecimal("10000")));
        assertEquals(new BigDecimal("350000"), calculator.calculate(CalculationMethod.AREA, new BigDecimal("50"), new BigDecimal("7000")));
        assertEquals(new BigDecimal("233310"), calculator.calculate(CalculationMethod.AREA, new BigDecimal("33.33"), new BigDecimal("7000")));
        assertEquals(new BigDecimal("233467"), calculator.calculate(CalculationMethod.AREA, new BigDecimal("33.35"), new BigDecimal("7000.50")));
        assertEquals(new BigDecimal("87513"), calculator.calculate(CalculationMethod.AREA, new BigDecimal("12.5"), new BigDecimal("7001")));
        assertEquals(new BigDecimal("30000"), calculator.calculate(CalculationMethod.PER_HOUSEHOLD, null, new BigDecimal("30000")));
        assertEquals(new BigDecimal("30001"), calculator.calculate(CalculationMethod.PER_HOUSEHOLD, null, new BigDecimal("30000.50")));
    }
}
