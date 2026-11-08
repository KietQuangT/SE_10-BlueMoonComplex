package vn.edu.se10.bluemoon.billing.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;

/** The financial inputs that must still match an approved submission. */
public record BillingInputs(List<Household> households, List<Rate> rates) {
    public BillingInputs {
        households = List.copyOf(households);
        rates = List.copyOf(rates);
    }

    public record Household(long id, BigDecimal areaM2) { }
    public record Rate(String feeTypeCode, long feeRateId, BigDecimal unitPrice, CalculationMethod method) { }

    public boolean matches(BillingInputs other) {
        Map<Long, Household> householdMap = households.stream()
                .collect(Collectors.toMap(Household::id, Function.identity()));
        Map<Long, Household> otherHouseholds = other.households.stream()
                .collect(Collectors.toMap(Household::id, Function.identity()));
        if (!householdMap.keySet().equals(otherHouseholds.keySet())) return false;
        for (var entry : householdMap.entrySet()) {
            if (entry.getValue().areaM2().compareTo(otherHouseholds.get(entry.getKey()).areaM2()) != 0) return false;
        }
        Map<String, Rate> rateMap = rates.stream().collect(Collectors.toMap(Rate::feeTypeCode, Function.identity()));
        Map<String, Rate> otherRates = other.rates.stream().collect(Collectors.toMap(Rate::feeTypeCode, Function.identity()));
        if (!rateMap.keySet().equals(otherRates.keySet())) return false;
        for (var entry : rateMap.entrySet()) {
            Rate a = entry.getValue();
            Rate b = otherRates.get(entry.getKey());
            if (a.feeRateId() != b.feeRateId() || a.unitPrice().compareTo(b.unitPrice()) != 0
                    || !Objects.equals(a.method(), b.method())) return false;
        }
        return true;
    }
}
