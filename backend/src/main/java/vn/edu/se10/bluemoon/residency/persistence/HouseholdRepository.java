package vn.edu.se10.bluemoon.residency.persistence;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface HouseholdRepository extends JpaRepository<Household,Long> {
 @Query("select h from Household h where (:apartment is null or h.apartmentCode = :apartment) and (:code is null or h.householdCode = :code) and (:status is null or h.status = :status)")
 Page<Household> search(@Param("apartment") String apartment, @Param("code") String code, @Param("status") String status, Pageable pageable);
}
