package vn.edu.se10.bluemoon.residency.application;
import java.util.Optional;
import org.springframework.stereotype.Service;
import vn.edu.se10.bluemoon.residency.persistence.HouseholdRepository;
@Service
public class HouseholdLookupService implements HouseholdLookup {
 private final HouseholdRepository repository;
 public HouseholdLookupService(HouseholdRepository repository){this.repository=repository;}
 public Optional<String> findStatus(long id){return repository.findById(id).map(h -> h.getStatus());}
}
