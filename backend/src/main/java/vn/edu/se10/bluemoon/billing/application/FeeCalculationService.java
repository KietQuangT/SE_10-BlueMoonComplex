package vn.edu.se10.bluemoon.billing.application;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.api.FeePreviewResponse;
import vn.edu.se10.bluemoon.billing.persistence.*;
import vn.edu.se10.bluemoon.common.error.*;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

@Service
public class FeeCalculationService {
    private final FeeTypeRepository types;
    private final FeeRateRepository rates;
    private final HouseholdLookup households;
    private final FeeCalculator calculator = new FeeCalculator();

    public FeeCalculationService(FeeTypeRepository types, FeeRateRepository rates, HouseholdLookup households) {
        this.types = types;
        this.rates = rates;
        this.households = households;
    }

    @Transactional(readOnly = true)
    public FeePreviewResponse preview(long householdId, String period) {
        YearMonth month;
        if (period == null || !period.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new BadRequestException("INVALID_PERIOD");
        }
        try {
            month = YearMonth.parse(period);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException("INVALID_PERIOD");
        }
        BigDecimal area = households.findAreaM2(householdId).orElseThrow(() -> new NotFoundException("HOUSEHOLD_NOT_FOUND"));
        List<FeePreviewResponse.Line> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (FeeType type : types.findAll(Sort.by("id"))) {
            FeeRate rate = rates.findFirstByFeeTypeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(type.getId(), month.atDay(1))
                    .orElseThrow(() -> new ConflictException("FEE_RATE_UNAVAILABLE"));
            BigDecimal lineArea = type.getCalculationMethod() == CalculationMethod.AREA ? area : null;
            BigDecimal amount = calculator.calculate(type.getCalculationMethod(), lineArea, rate.getUnitPrice());
            lines.add(new FeePreviewResponse.Line(type.getCode(), type.getName(), type.getCalculationMethod(),
                    lineArea, rate.getUnitPrice(), rate.getId(), amount));
            total = total.add(amount);
        }
        return new FeePreviewResponse(householdId, period, lines, total);
    }
}
