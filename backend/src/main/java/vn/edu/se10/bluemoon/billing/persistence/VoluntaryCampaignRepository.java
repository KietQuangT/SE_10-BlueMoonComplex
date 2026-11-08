package vn.edu.se10.bluemoon.billing.persistence;

import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoluntaryCampaignRepository extends JpaRepository<VoluntaryCampaign, Long> {
    @Query("""
            select c from VoluntaryCampaign c
            where (:publishedOnly = false or c.publishedAt is not null)
            and (:status is null or (:status = 'DRAFT' and c.status = 'DRAFT')
                 or (:status = 'PUBLISHED' and c.status = 'PUBLISHED' and c.closesOn >= :today)
                 or (:status = 'CLOSED' and (c.status = 'CLOSED' or (c.status = 'PUBLISHED' and c.closesOn < :today))))
            """)
    Page<VoluntaryCampaign> search(@Param("publishedOnly") boolean publishedOnly, @Param("status") String status,
                                   @Param("today") LocalDate today, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from VoluntaryCampaign c where c.id = :id")
    Optional<VoluntaryCampaign> lockById(@Param("id") long id);
}
