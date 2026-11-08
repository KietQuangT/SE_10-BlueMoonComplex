package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import vn.edu.se10.bluemoon.billing.application.FeeCalculationService;
import vn.edu.se10.bluemoon.billing.persistence.*;
import vn.edu.se10.bluemoon.common.error.*;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

class FeeCalculationServiceTest {
    private final FeeTypeRepository types = mock(FeeTypeRepository.class);
    private final FeeRateRepository rates = mock(FeeRateRepository.class);
    private final HouseholdLookup households = mock(HouseholdLookup.class);
    private final FeeCalculationService service = new FeeCalculationService(types, rates, households);

    @Test
    void calculatesBothLinesUsingRatesEffectiveAtMonthStart() {
        FeeType area = type(1L, "MANAGEMENT_FEE", CalculationMethod.AREA);
        FeeType waste = type(2L, "WASTE_FEE", CalculationMethod.PER_HOUSEHOLD);
        when(types.findAll(any(Sort.class))).thenReturn(List.of(area, waste));
        when(households.findAreaM2(7)).thenReturn(Optional.of(new BigDecimal("50")));
        FeeRate november = rate(1L, "7000");
        FeeRate december = rate(2L, "8000");
        FeeRate flat = rate(3L, "30000");
        when(rates.findFirstByFeeTypeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(1L, LocalDate.parse("2026-11-01"))).thenReturn(Optional.of(november));
        when(rates.findFirstByFeeTypeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(1L, LocalDate.parse("2026-12-01"))).thenReturn(Optional.of(december));
        when(rates.findFirstByFeeTypeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(eq(2L), any())).thenReturn(Optional.of(flat));
        var nov = service.preview(7, "2026-11");
        assertEquals(new BigDecimal("350000"), nov.lines().get(0).amount());
        assertNull(nov.lines().get(1).areaM2());
        assertEquals(new BigDecimal("380000"), nov.total());
        assertEquals(new BigDecimal("430000"), service.preview(7, "2026-12").total());
        assertEquals("FEE_RATE_UNAVAILABLE", assertThrows(ConflictException.class, () -> service.preview(7, "2026-10")).code());
        when(rates.findFirstByFeeTypeIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(eq(2L), any())).thenReturn(Optional.empty());
        assertEquals("FEE_RATE_UNAVAILABLE", assertThrows(ConflictException.class, () -> service.preview(7, "2026-11")).code());
    }

    @Test
    void validatesPeriodAndHousehold() {
        for (String period : List.of("2026-1", "2026-13", "abc")) {
            assertEquals("INVALID_PERIOD", assertThrows(BadRequestException.class, () -> service.preview(1, period)).code());
        }
        assertEquals("HOUSEHOLD_NOT_FOUND", assertThrows(NotFoundException.class, () -> service.preview(1, "2026-11")).code());
    }

    private FeeType type(long id, String code, CalculationMethod method) {
        FeeType type = mock(FeeType.class);
        when(type.getId()).thenReturn(id);
        when(type.getCode()).thenReturn(code);
        when(type.getCalculationMethod()).thenReturn(method);
        return type;
    }

    private FeeRate rate(long id, String price) {
        FeeRate rate = mock(FeeRate.class);
        when(rate.getId()).thenReturn(id);
        when(rate.getUnitPrice()).thenReturn(new BigDecimal(price));
        return rate;
    }
}
