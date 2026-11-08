package vn.edu.se10.bluemoon.billing.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingPeriodRepository extends JpaRepository<BillingPeriod, Long> {
    boolean existsByPeriodAndStatusNot(String period, String status);
    Optional<BillingPeriod> findFirstByPeriodAndStatusOrderByIdDesc(String period, String status);
}
