package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;

class BillingPeriodServiceTest {
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final AuditService audit = mock(AuditService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC);
    private final BillingPeriodService service = new BillingPeriodService(periods, audit, clock);

    @Test
    void createRejectsAnExistingActivePeriod() {
        when(periods.existsByPeriodAndStatusNot("2026-11", "CANCELLED")).thenReturn(true);
        assertEquals("BILLING_PERIOD_EXISTS", assertThrows(ConflictException.class,
                () -> service.create("2026-11", LocalDate.of(2026, 11, 30), 1)).code());
    }

    @Test
    void createRejectsPastMonthBeforePersisting() {
        assertEquals("INVALID_PERIOD", assertThrows(BadRequestException.class,
                () -> service.create("2026-10", LocalDate.of(2026, 10, 31), 1)).code());
    }
}
