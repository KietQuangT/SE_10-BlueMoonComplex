package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.application.BillingReceiptGuard;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.common.error.ConflictException;

class BillingReceiptGuardTest {
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final BillingReceiptGuard guard = new BillingReceiptGuard(periods);

    @Test
    void recordsReceiptOnlyForIssuedPeriod() {
        BillingPeriod period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, Instant.EPOCH);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> guard.recordReceipt(5)).code());
        ReflectionTestUtils.setField(period, "status", "ISSUED");
        guard.recordReceipt(5);
        guard.recordReceipt(5);
        assertEquals(2, period.getReceiptCount());
    }
}
