package vn.edu.se10.bluemoon.residency.application;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.residency.persistence.ResidentRepository;

@Service
public class ResidentDonorLookupService implements ResidentDonorLookup {
    private final ResidentRepository residents;

    public ResidentDonorLookupService(ResidentRepository residents) {
        this.residents = residents;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Donor> lockById(long id) {
        return residents.lockDonor(id).map(ResidentDonorLookupService::toDonor);
    }

    @Transactional(readOnly = true)
    public Page<Donor> search(String query, int page, int size) {
        if (query == null || query.strip().codePointCount(0, query.strip().length()) < 2 || page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("INVALID_FILTER");
        }
        String escaped = query.strip().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return residents.searchDonors("%" + escaped + "%", PageRequest.of(page, size)).map(ResidentDonorLookupService::toDonor);
    }

    private static Donor toDonor(ResidentRepository.DonorProjection projection) {
        return new Donor(projection.getId(), projection.getHouseholdId(), projection.getFullName(),
                projection.getHouseholdCode(), projection.getApartmentCode());
    }
}
