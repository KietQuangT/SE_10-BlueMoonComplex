package vn.edu.se10.bluemoon.residency.api;
import java.time.LocalDate;
import vn.edu.se10.bluemoon.residency.persistence.*;
public record MyResidentResponse(Long id,String fullName,LocalDate dateOfBirth,String relationshipToHead,ResidencyStatus residencyStatus) {
 public static MyResidentResponse from(Resident r){return new MyResidentResponse(r.getId(),r.getFullName(),r.getDateOfBirth(),r.getRelationshipToHead(),r.getResidencyStatus());}
}
