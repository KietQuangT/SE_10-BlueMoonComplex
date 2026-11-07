package vn.edu.se10.bluemoon.residency.persistence;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ResidencyChangeRepository extends JpaRepository<ResidencyChange,Long> {
 Page<ResidencyChange> findByHouseholdId(Long householdId,Pageable pageable);
 boolean existsByResidentIdAndChangeTypeAndStatus(Long residentId,String type,String status);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from ResidencyChange c where c.id=:id") Optional<ResidencyChange> lockById(@Param("id") long id);
 @Query("select c from ResidencyChange c, Resident r, Household h where c.residentId=r.id and c.householdId=h.id and c.changeType='TEMPORARY_ABSENCE' and (:household is null or c.householdId=:household) and (:status is null or c.status=:status) and (:apartment is null or lower(h.apartmentCode) like :apartment escape '!' or lower(h.householdCode) like :apartment escape '!') and (:name is null or r.fullName like :name escape '!')")
 Page<ResidencyChange> search(@Param("household") Long household,@Param("status") String status,@Param("apartment") String apartment,@Param("name") String name,Pageable pageable);
}
