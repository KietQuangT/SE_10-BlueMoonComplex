package vn.edu.se10.bluemoon.billing.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateFeeRateRequest(@NotNull @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 10, fraction = 2) BigDecimal unitPrice) { }
