package vn.edu.se10.bluemoon.billing.persistence;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoluntaryCampaignRepository extends JpaRepository<VoluntaryCampaign, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from VoluntaryCampaign c where c.id = :id")
    Optional<VoluntaryCampaign> lockById(@Param("id") long id);
}
