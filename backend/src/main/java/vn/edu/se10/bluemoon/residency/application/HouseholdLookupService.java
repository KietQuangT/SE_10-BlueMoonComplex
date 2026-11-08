package vn.edu.se10.bluemoon.residency.application;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.se10.bluemoon.residency.persistence.HouseholdRepository;

@Service
public class HouseholdLookupService implements HouseholdLookup {
    private final HouseholdRepository repository;
    public HouseholdLookupService(HouseholdRepository repository) {
        this.repository = repository;
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public List<BillingHousehold> lockActiveForBilling() {
        return repository.lockActiveForBilling().stream()
                .map(h -> new BillingHousehold(h.getId(), h.getHouseholdCode(), h.getApartmentCode(), h.getAreaM2()))
                .toList();
    }
    public Optional<String> findStatus(long id) {
        return repository.findById(id).map(h -> h.getStatus());
    }
    public Optional<BigDecimal> findAreaM2(long householdId) {
        return repository.findById(householdId).map(h -> h.getAreaM2());
    }
}
