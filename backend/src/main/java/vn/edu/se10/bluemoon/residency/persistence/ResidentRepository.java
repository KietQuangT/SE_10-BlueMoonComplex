package vn.edu.se10.bluemoon.residency.persistence;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ResidentRepository extends JpaRepository<Resident,Long> {
 interface DonorProjection {
  Long getId();
  Long getHouseholdId();
  String getFullName();
  String getHouseholdCode();
  String getApartmentCode();
 }
 @Query(value="select r.id, r.household_id as householdId, r.full_name as fullName, h.household_code as householdCode, h.apartment_code as apartmentCode from residents r join households h on h.id=r.household_id where r.id=:id for update", nativeQuery=true)
 Optional<DonorProjection> lockDonor(@Param("id") long id);
 @Query(value="select r.id, r.household_id as householdId, r.full_name as fullName, h.household_code as householdCode, h.apartment_code as apartmentCode from residents r join households h on h.id=r.household_id where r.full_name like :name escape '!' order by r.full_name, r.id", countQuery="select count(*) from residents r where r.full_name like :name escape '!'", nativeQuery=true)
 Page<DonorProjection> searchDonors(@Param("name") String name, Pageable pageable);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Resident r where r.id=:id") Optional<Resident> lockById(@Param("id") long id);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Resident r where r.identityNumber=:identity and r.residencyStatus in :statuses and (:exclude is null or r.id<>:exclude)") List<Resident> findConflicts(@Param("identity") String identity,@Param("statuses") Collection<ResidencyStatus> statuses,@Param("exclude") Long exclude);
 Page<Resident> findByHouseholdIdAndResidencyStatusIn(Long householdId,Collection<ResidencyStatus> statuses,Pageable pageable);
 List<Resident> findByHouseholdIdAndResidencyStatusInOrderById(Long householdId,Collection<ResidencyStatus> statuses);
 @Query("select r from Resident r where r.residencyStatus in :statuses and r.fullName like :name escape '!' order by r.fullName, r.id") List<Resident> suggest(@Param("statuses") Collection<ResidencyStatus> statuses,@Param("name") String name,Pageable pageable);
}
