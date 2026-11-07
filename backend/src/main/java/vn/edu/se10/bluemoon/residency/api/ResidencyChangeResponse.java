package vn.edu.se10.bluemoon.residency.api;
import java.time.*;
import vn.edu.se10.bluemoon.residency.persistence.ResidencyChange;
public record ResidencyChangeResponse(Long id,Long residentId,String changeType,LocalDate effectiveDate,String status,Long recordedBy,Instant createdAt,String reason,LocalDate endDate,String decisionNote,Instant decidedAt) {
 public static ResidencyChangeResponse from(ResidencyChange c){return new ResidencyChangeResponse(c.getId(),c.getResidentId(),c.getChangeType(),c.getEffectiveDate(),c.getStatus(),c.getRecordedBy(),c.getCreatedAt(),c.getReason(),c.getEndDate(),c.getDecisionNote(),c.getDecidedAt());}
}
