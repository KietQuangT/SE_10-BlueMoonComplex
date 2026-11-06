package vn.edu.se10.bluemoon.residency.api;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record ResidentRequest(@Size(max=150) String fullName,@Size(max=50) String identityNumber,LocalDate dateOfBirth,@Size(max=30) String phone,@Size(max=100) String relationshipToHead) {}
