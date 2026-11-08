package vn.edu.se10.bluemoon.residency.application;

import java.math.BigDecimal;
import java.util.Optional;

public interface HouseholdLookup {
    Optional<String> findStatus(long id);
    Optional<BigDecimal> findAreaM2(long householdId);
}
