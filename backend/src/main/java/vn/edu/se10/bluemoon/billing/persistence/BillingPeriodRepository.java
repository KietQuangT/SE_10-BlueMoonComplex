package vn.edu.se10.bluemoon.billing.persistence;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BillingPeriodRepository extends JpaRepository<BillingPeriod, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from BillingPeriod p where p.id = :id")
    Optional<BillingPeriod> lockById(@Param("id") long id);
    @Query("select p from BillingPeriod p where (:period is null or p.period=:period) and (:status is null or p.status=:status)")
    Page<BillingPeriod> search(@Param("period") String period, @Param("status") String status, Pageable pageable);
    boolean existsByPeriodAndStatusNot(String period, String status);
    Optional<BillingPeriod> findFirstByPeriodAndStatusOrderByIdDesc(String period, String status);
}
