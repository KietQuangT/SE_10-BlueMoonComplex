package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

class BillingPeriodCancellationTest {
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final BillingSnapshotStore snapshots = mock(BillingSnapshotStore.class);
    private final AuditService audit = mock(AuditService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T10:00:00Z"), ZoneOffset.UTC);
    private final BillingPeriodService service = new BillingPeriodService(periods, audit, clock,
            mock(HouseholdLookup.class), snapshots, mock(BillingReadStore.class));

    private BillingPeriod period;

    @BeforeEach
    void setUp() {
        period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", "ISSUED");
        ReflectionTestUtils.setField(period, "version", 2L);
        ReflectionTestUtils.setField(period, "receiptCount", 0L);
        when(periods.lockById(5L)).thenReturn(Optional.of(period));
    }

    @Test
    void cancelSuccessfullyTransitionsIssuedPeriodToCancelledAndCancelsInvoices() {
        var cancelled = service.cancel(5L, 2L, "  Sai đơn giá quản lý  ", 99L);

        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals("Sai đơn giá quản lý", cancelled.getCancelReason());
        assertEquals(99L, ReflectionTestUtils.getField(cancelled, "cancelledBy"));
        assertEquals(clock.instant(), cancelled.getCancelledAt());

        verify(snapshots).cancelInvoices(5L, clock.instant());
        verify(periods).flush();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(99L), eq("BILLING_PERIOD_CANCELLED"), eq("BillingPeriod"), eq("5"), captor.capture());
        var changes = captor.getValue();
        assertEquals("2026-11", changes.get("period"));
        assertEquals("CANCELLED", changes.get("status"));
        // Confirm no reason or PII in audit
        assertEquals(2, changes.size());
    }

    @Test
    void cancelRejectsBlankOrNullReason() {
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.cancel(5L, 2L, null, 99L)).code());
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.cancel(5L, 2L, "   ", 99L)).code());
        verify(snapshots, never()).cancelInvoices(anyLong(), any());
    }

    @Test
    void cancelRejectsReasonExceeding255CodePoints() {
        String longReason = "A".repeat(256);
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.cancel(5L, 2L, longReason, 99L)).code());
        verify(snapshots, never()).cancelInvoices(anyLong(), any());
    }

    @Test
    void cancelRejectsNonIssuedStatus() {
        ReflectionTestUtils.setField(period, "status", "DRAFT");
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class,
                () -> service.cancel(5L, 2L, "Valid reason", 99L)).code());

        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class,
                () -> service.cancel(5L, 2L, "Valid reason", 99L)).code());

        ReflectionTestUtils.setField(period, "status", "CANCELLED");
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class,
                () -> service.cancel(5L, 2L, "Valid reason", 99L)).code());

        verify(snapshots, never()).cancelInvoices(anyLong(), any());
    }

    @Test
    void cancelRejectsStaleVersion() {
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class,
                () -> service.cancel(5L, 1L, "Valid reason", 99L)).code());
        verify(snapshots, never()).cancelInvoices(anyLong(), any());
    }

    @Test
    void cancelRejectsWhenReceiptCountIsPositive() {
        ReflectionTestUtils.setField(period, "receiptCount", 1L);
        assertEquals("PERIOD_HAS_RECEIPTS", assertThrows(ConflictException.class,
                () -> service.cancel(5L, 2L, "Valid reason", 99L)).code());
        verify(snapshots, never()).cancelInvoices(anyLong(), any());
    }

    @Test
    void cancelRejectsExpiredMonthPeriod() {
        ReflectionTestUtils.setField(period, "period", "2026-10");
        assertEquals("BILLING_PERIOD_EXPIRED", assertThrows(ConflictException.class,
                () -> service.cancel(5L, 2L, "Valid reason", 99L)).code());
        verify(snapshots, never()).cancelInvoices(anyLong(), any());
    }
}
