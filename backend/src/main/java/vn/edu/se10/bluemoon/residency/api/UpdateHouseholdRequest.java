package vn.edu.se10.bluemoon.residency.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record UpdateHouseholdRequest(@DecimalMin(value="0", inclusive=false) @Digits(integer=8,fraction=2) BigDecimal areaM2, @Size(max=255) String address) { @AssertTrue public boolean isUpdatePresent(){return areaM2 != null || address != null;} }
