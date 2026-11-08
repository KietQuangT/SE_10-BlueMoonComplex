package vn.edu.se10.bluemoon.residency.application;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

public interface HouseholdLookup {
    record BillingHousehold(long id, String householdCode, String apartmentCode, BigDecimal areaM2) { }

    /** Requires an existing REPEATABLE_READ transaction held until issue commits. */
    List<BillingHousehold> lockActiveForBilling();
    Optional<String> findStatus(long id);
    Optional<BigDecimal> findAreaM2(long householdId);
}
