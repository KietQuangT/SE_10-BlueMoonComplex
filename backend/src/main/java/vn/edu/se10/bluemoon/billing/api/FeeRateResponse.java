package vn.edu.se10.bluemoon.billing.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import vn.edu.se10.bluemoon.billing.persistence.FeeRate;

public record FeeRateResponse(Long id, Long feeTypeId, String feeTypeCode, String feeTypeName,
        BigDecimal unitPrice, LocalDate effectiveFrom, Instant createdAt, boolean editable) {
    public static FeeRateResponse from(FeeRate rate, LocalDate currentMonth) {
        return new FeeRateResponse(rate.getId(), rate.getFeeType().getId(), rate.getFeeType().getCode(),
                rate.getFeeType().getName(), rate.getUnitPrice(), rate.getEffectiveFrom(), rate.getCreatedAt(),
                rate.getEffectiveFrom().isAfter(currentMonth));
    }
}
