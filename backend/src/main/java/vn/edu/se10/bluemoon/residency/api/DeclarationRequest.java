package vn.edu.se10.bluemoon.residency.api;
import java.time.LocalDate;
public record DeclarationRequest(Long residentId,LocalDate effectiveDate,LocalDate endDate,String reason) {}
