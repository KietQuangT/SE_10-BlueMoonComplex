package vn.edu.se10.bluemoon.billing.persistence;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoluntaryContributionRepository extends JpaRepository<VoluntaryContribution, Long> {
    @Query("select c from VoluntaryContribution c where c.campaignId=:campaignId and (:status is null or c.status=:status)")
    Page<VoluntaryContribution> search(@Param("campaignId") long campaignId, @Param("status") String status, Pageable pageable);
    @Query("select c.campaignId, count(c), sum(c.amount) from VoluntaryContribution c where c.campaignId in :ids and c.status='POSTED' group by c.campaignId")
    List<Object[]> totals(@Param("ids") List<Long> ids);
    Optional<VoluntaryContribution> findByRecordedByAndIdempotencyKey(long actor, String key);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from VoluntaryContribution c where c.id=:id")
    Optional<VoluntaryContribution> lockById(@Param("id") long id);
    @Query("select c.campaignId from VoluntaryContribution c where c.id=:id")
    Optional<Long> findCampaignId(@Param("id") long id);
}
