package vn.edu.se10.bluemoon.residency.api;
import java.math.BigDecimal;
import java.time.Instant;
import vn.edu.se10.bluemoon.residency.persistence.Household;
public record HouseholdResponse(Long id, String householdCode, String apartmentCode, BigDecimal areaM2, String address, String status, Instant createdAt, Instant updatedAt) {
 public static HouseholdResponse from(Household h){return new HouseholdResponse(h.getId(),h.getHouseholdCode(),h.getApartmentCode(),h.getAreaM2(),h.getAddress(),h.getStatus(),h.getCreatedAt(),h.getUpdatedAt());}
}
