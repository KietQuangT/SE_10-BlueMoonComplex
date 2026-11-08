package vn.edu.se10.bluemoon.residency.persistence;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface HouseholdRepository extends JpaRepository<Household,Long> {
 @Query("select h from Household h where (:apartment is null or h.apartmentCode = :apartment) and (:code is null or h.householdCode = :code) and (:status is null or h.status = :status)")
 Page<Household> search(@Param("apartment") String apartment, @Param("code") String code, @Param("status") String status, Pageable pageable);
 interface BillingProjection {
  Long getId();
  String getHouseholdCode();
  String getApartmentCode();
  java.math.BigDecimal getAreaM2();
 }
 @Query(value="select id, household_code as householdCode, apartment_code as apartmentCode, area_m2 as areaM2 from households force index (idx_households_status_id) where status = 'ACTIVE' order by status, id for update", nativeQuery=true)
 java.util.List<BillingProjection> lockActiveForBilling();
 @Query(value="select id, household_code as householdCode, apartment_code as apartmentCode, area_m2 as areaM2 from households where status = 'ACTIVE' order by id", nativeQuery=true)
 java.util.List<BillingProjection> listActiveForBilling();
 @Query(value="select id from households order by id limit 1 for update",nativeQuery=true) Long lockIdentityGuard();
}
