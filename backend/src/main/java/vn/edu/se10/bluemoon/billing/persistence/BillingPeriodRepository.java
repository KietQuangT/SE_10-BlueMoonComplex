package vn.edu.se10.bluemoon.billing.persistence;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingPeriodRepository extends JpaRepository<BillingPeriod, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from BillingPeriod p where p.id = :id")
    Optional<BillingPeriod> lockById(@Param("id") long id);
    boolean existsByPeriodAndStatusNot(String period, String status);
    Optional<BillingPeriod> findFirstByPeriodAndStatusOrderByIdDesc(String period, String status);
}
