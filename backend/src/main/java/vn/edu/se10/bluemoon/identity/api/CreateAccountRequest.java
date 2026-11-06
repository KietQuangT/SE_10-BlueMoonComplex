package vn.edu.se10.bluemoon.identity.api;
import jakarta.validation.constraints.*;
public record CreateAccountRequest(
        @NotNull @Pattern(regexp="[A-Za-z0-9._-]{3,100}") String username,
        @NotBlank @Size(max=150) @Pattern(regexp="^[^\\p{Cc}]*$") String fullName,
        @NotBlank @ValidAccountEmail String email,
        @NotBlank @Pattern(regexp="ADMIN|ACCOUNTANT|RESIDENT") String role,
        @Positive Long householdId) {}
