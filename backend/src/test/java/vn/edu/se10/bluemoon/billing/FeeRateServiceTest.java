package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.api.CreateFeeRateRequest;
import vn.edu.se10.bluemoon.billing.application.FeeRateService;
import vn.edu.se10.bluemoon.billing.persistence.*;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

class FeeRateServiceTest {
    private final FeeRateRepository rates = mock(FeeRateRepository.class);
    private final FeeTypeRepository types = mock(FeeTypeRepository.class);
    private final AuditService audit = mock(AuditService.class);
    private final FeeRateService service = new FeeRateService(rates, types, audit,
            Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void rejectsPastAndNonFirstDay() {
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class, () -> service.create(request("2026-10-01"), 1)).code());
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class, () -> service.create(request("2026-11-02"), 1)).code());
    }

    @Test
    void rejectsUnknownTypeAndDuplicateMonth() {
        assertEquals("FEE_TYPE_NOT_FOUND", assertThrows(NotFoundException.class, () -> service.create(request("2026-11-01"), 1)).code());
        FeeType type = mock(FeeType.class);
        when(types.findById(1L)).thenReturn(Optional.of(type));
        when(rates.existsByFeeTypeIdAndEffectiveFrom(1L, LocalDate.parse("2026-11-01"))).thenReturn(true);
        assertEquals("FEE_RATE_DUPLICATE_MONTH", assertThrows(ConflictException.class, () -> service.create(request("2026-11-01"), 1)).code());
    }

    @Test
    void freezesCurrentAndPastRatesForUpdateAndDelete() {
        FeeRate rate = mock(FeeRate.class);
        when(rates.lockById(1L)).thenReturn(Optional.of(rate));
        for (String date : new String[]{"2026-10-01", "2026-11-01"}) {
            when(rate.getEffectiveFrom()).thenReturn(LocalDate.parse(date));
            assertEquals("FEE_RATE_FROZEN", assertThrows(ConflictException.class, () -> service.update(1, new BigDecimal("2"), 1)).code());
            assertEquals("FEE_RATE_FROZEN", assertThrows(ConflictException.class, () -> service.delete(1, 1)).code());
        }
        verifyNoInteractions(audit);
    }

    @Test
    void updatesAndDeletesFutureRateWithAudit() {
        FeeType type = mock(FeeType.class);
        when(type.getId()).thenReturn(1L);
        when(type.getCode()).thenReturn("MANAGEMENT_FEE");
        when(type.getName()).thenReturn("Phí quản lý");
        FeeRate rate = mock(FeeRate.class);
        when(rate.getId()).thenReturn(2L);
        when(rate.getFeeType()).thenReturn(type);
        when(rate.getUnitPrice()).thenReturn(new BigDecimal("7000"));
        when(rate.getEffectiveFrom()).thenReturn(LocalDate.parse("2026-12-01"));
        when(rates.lockById(2L)).thenReturn(Optional.of(rate));
        service.update(2, new BigDecimal("8000"), 1);
        verify(rate).setUnitPrice(new BigDecimal("8000"));
        verify(audit).record(eq(1L), eq("FEE_RATE_UPDATED"), eq("FeeRate"), eq("2"), argThat(changes ->
                changes.keySet().equals(java.util.Set.of("feeTypeCode", "effectiveFrom", "unitPrice", "previousUnitPrice"))
                        && changes.get("previousUnitPrice").equals("7000")));
        service.delete(2, 1);
        verify(rates).delete(rate);
        verify(audit).record(eq(1L), eq("FEE_RATE_DELETED"), eq("FeeRate"), eq("2"), argThat(changes ->
                changes.keySet().equals(java.util.Set.of("feeTypeCode", "effectiveFrom", "unitPrice"))));
    }

    private CreateFeeRateRequest request(String date) {
        return new CreateFeeRateRequest(1L, new BigDecimal("7000"), LocalDate.parse(date));
    }
}
