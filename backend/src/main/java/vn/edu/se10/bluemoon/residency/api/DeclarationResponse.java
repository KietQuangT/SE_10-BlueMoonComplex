package vn.edu.se10.bluemoon.residency.api;
import java.time.*;
public record DeclarationResponse(Long id,Long residentId,String residentName,Long householdId,String householdCode,String apartmentCode,String changeType,String status,LocalDate effectiveDate,LocalDate endDate,String reason,String decisionNote,Instant decidedAt,Instant createdAt,boolean cancellable) {}
