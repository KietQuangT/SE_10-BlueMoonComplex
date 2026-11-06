package vn.edu.se10.bluemoon.residency.persistence;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ResidencyChangeRepository extends JpaRepository<ResidencyChange,Long> { Page<ResidencyChange> findByHouseholdId(Long householdId,Pageable pageable); }
