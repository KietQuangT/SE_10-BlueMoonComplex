package vn.edu.se10.bluemoon.residency.persistence;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ResidentRepository extends JpaRepository<Resident,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Resident r where r.id=:id") Optional<Resident> lockById(@Param("id") long id);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Resident r where r.identityNumber=:identity and r.residencyStatus in :statuses and (:exclude is null or r.id<>:exclude)") List<Resident> findConflicts(@Param("identity") String identity,@Param("statuses") Collection<ResidencyStatus> statuses,@Param("exclude") Long exclude);
 Page<Resident> findByHouseholdIdAndResidencyStatusIn(Long householdId,Collection<ResidencyStatus> statuses,Pageable pageable);
 List<Resident> findByHouseholdIdAndResidencyStatusInOrderById(Long householdId,Collection<ResidencyStatus> statuses);
}
