package vn.edu.se10.bluemoon.residency.application;

import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ResidentDonorLookup {
    record Donor(long id, long householdId, String fullName, String householdCode, String apartmentCode) { }
    Optional<Donor> lockById(long id);
    Page<Donor> search(String query, int page, int size);
}
