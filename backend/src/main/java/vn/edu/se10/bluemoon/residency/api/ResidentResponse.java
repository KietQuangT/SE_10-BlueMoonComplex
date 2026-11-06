package vn.edu.se10.bluemoon.residency.api;
import java.time.*;
import vn.edu.se10.bluemoon.residency.persistence.*;
public record ResidentResponse(Long id,Long householdId,String fullName,String identityNumber,LocalDate dateOfBirth,String phone,String relationshipToHead,ResidencyStatus residencyStatus,Instant createdAt,Instant updatedAt) {
 public static ResidentResponse from(Resident r){return new ResidentResponse(r.getId(),r.getHouseholdId(),r.getFullName(),r.getIdentityNumber(),r.getDateOfBirth(),r.getPhone(),r.getRelationshipToHead(),r.getResidencyStatus(),r.getCreatedAt(),r.getUpdatedAt());}
}
