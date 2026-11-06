package vn.edu.se10.bluemoon.residency.persistence;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="households")
public class Household {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="household_code", nullable=false, length=50, updatable=false) private String householdCode;
 @Column(name="apartment_code", nullable=false, length=50, updatable=false) private String apartmentCode;
 @Column(name="area_m2", nullable=false, precision=10, scale=2) private BigDecimal areaM2;
 @Column(length=255) private String address;
 @Column(nullable=false, length=20) private String status;
 @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
 @Column(name="updated_at", nullable=false) private Instant updatedAt;
 protected Household() {}
 public Household(String code, String apartment, BigDecimal area, String address, Instant now) { this.householdCode=code; this.apartmentCode=apartment; this.areaM2=area; this.address=address; this.status="ACTIVE"; this.createdAt=now; this.updatedAt=now; }
 public void update(BigDecimal area, String address, Instant now) { if(area!=null) this.areaM2=area; if(address!=null) this.address=address; this.updatedAt=now; }
 public Long getId(){return id;} public String getHouseholdCode(){return householdCode;} public String getApartmentCode(){return apartmentCode;} public BigDecimal getAreaM2(){return areaM2;} public String getAddress(){return address;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
