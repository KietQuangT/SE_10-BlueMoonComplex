package vn.edu.se10.bluemoon.billing.persistence;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeeRateRepository extends JpaRepository<FeeRate, Long> {
    boolean existsByFeeTypeIdAndEffectiveFrom(Long feeTypeId, LocalDate effectiveFrom);
    Page<FeeRate> findByFeeTypeId(Long feeTypeId, Pageable pageable);
    Optional<FeeRate> findFirstByFeeTypeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(Long feeTypeId, LocalDate date);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from FeeRate r where r.id = :id")
    Optional<FeeRate> lockById(@Param("id") long id);
}
