package vn.edu.se10.bluemoon.billing.api;

import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;
import vn.edu.se10.bluemoon.billing.persistence.FeeType;

public record FeeTypeResponse(Long id, String code, String name, CalculationMethod calculationMethod, String unit) {
    public static FeeTypeResponse from(FeeType type) {
        return new FeeTypeResponse(type.getId(), type.getCode(), type.getName(), type.getCalculationMethod(),
                type.getCalculationMethod() == CalculationMethod.AREA ? "m²/tháng" : "hộ/tháng");
    }
}
