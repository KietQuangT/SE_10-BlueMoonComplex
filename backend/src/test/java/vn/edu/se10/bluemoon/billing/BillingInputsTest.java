package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.application.BillingInputs;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;

class BillingInputsTest {
    private BillingInputs.Household household(long id, String area) {
        return new BillingInputs.Household(id, new BigDecimal(area));
    }
    private BillingInputs.Rate rate(long id, String price) {
        return new BillingInputs.Rate("MANAGEMENT_FEE", id, new BigDecimal(price), CalculationMethod.AREA);
    }
    private BillingInputs inputs(List<BillingInputs.Household> households, BillingInputs.Rate rate) {
        return new BillingInputs(households, List.of(rate));
    }

    @Test
    void orderingAndDecimalScaleDoNotCauseFalseConflict() {
        var before = inputs(List.of(household(1, "50.00"), household(2, "30.00")), rate(3, "1.00"));
        var after = inputs(List.of(household(2, "30"), household(1, "50")), rate(3, "1"));
        assertTrue(before.matches(after));
    }

    @Test
    void sameTotalWithDifferentHouseholdOrRateStillConflicts() {
        var before = inputs(List.of(household(1, "50")), rate(3, "10"));
        assertFalse(before.matches(inputs(List.of(household(2, "50")), rate(3, "10"))));
        assertFalse(before.matches(inputs(List.of(household(1, "50")), rate(4, "10"))));
        assertFalse(before.matches(inputs(List.of(household(1, "40")), rate(3, "12.50"))));
    }
}
