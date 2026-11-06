package vn.edu.se10.bluemoon.residency.api;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record MoveOutRequest(@NotNull LocalDate effectiveDate,@Size(max=255) String reason) {}
