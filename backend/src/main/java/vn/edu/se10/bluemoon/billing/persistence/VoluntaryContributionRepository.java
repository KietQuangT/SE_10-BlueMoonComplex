package vn.edu.se10.bluemoon.billing.persistence;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoluntaryContributionRepository extends JpaRepository<VoluntaryContribution, Long> {
    Optional<VoluntaryContribution> findByRecordedByAndIdempotencyKey(long actor, String key);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from VoluntaryContribution c where c.id=:id")
    Optional<VoluntaryContribution> lockById(@Param("id") long id);
    @Query("select c.campaignId from VoluntaryContribution c where c.id=:id")
    Optional<Long> findCampaignId(@Param("id") long id);
}
