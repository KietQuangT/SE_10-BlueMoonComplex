package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;

class BillingPeriodServiceTest {
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final AuditService audit = mock(AuditService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC);
    private final HouseholdLookup households = mock(HouseholdLookup.class);
    private final BillingSnapshotStore snapshots = mock(BillingSnapshotStore.class);
    private final BillingPeriodService service = new BillingPeriodService(periods, audit, clock, households, snapshots, mock(BillingReadStore.class));

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
    @Test
    void draftCanChangeDueDateAndRejectsStaleVersion() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 20), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class,
                () -> service.updateDueDate(5, LocalDate.of(2026, 11, 25), 1, 1)).code());
        service.updateDueDate(5, LocalDate.of(2026, 11, 25), 0, 1);
        assertEquals(LocalDate.of(2026, 11, 25), period.getDueDate());
    }

    @Test
    void expiredDraftCannotBeChanged() {
        var period = new BillingPeriod("2026-10", LocalDate.of(2026, 10, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        assertEquals("BILLING_PERIOD_EXPIRED", assertThrows(ConflictException.class,
                () -> service.updateDueDate(5, LocalDate.of(2026, 10, 31), 0, 1)).code());
    }

    @Test
    void issueRejectsChangedHouseholdSetWithoutCreatingInvoices() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        ReflectionTestUtils.setField(period, "revision", 1L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        when(households.lockActiveForBilling()).thenReturn(List.of(new HouseholdLookup.BillingHousehold(2, "H2", "A2", new BigDecimal("50"))));
        when(snapshots.lockRates(any())).thenReturn(List.of(new BillingSnapshotStore.Rate(1, "MANAGEMENT_FEE", "fee", CalculationMethod.AREA, 1, BigDecimal.TEN)));
        when(snapshots.submissionId(5, 1)).thenReturn(7L);
        when(snapshots.inputs(7)).thenReturn(new BillingSnapshotStore.SnapshotInputs(
                List.of(new BillingSnapshotStore.HouseholdInput(1, new BigDecimal("50"))),
                List.of(new BillingSnapshotStore.RateInput("MANAGEMENT_FEE", 1, BigDecimal.TEN, CalculationMethod.AREA))));
        assertEquals("BILLING_INPUT_CHANGED", assertThrows(ConflictException.class,
                () -> service.issue(5, 0, 1, 1)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

    @Test
    void cancellationRejectsAnyPreviouslyCommittedReceipt() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", "ISSUED");
        ReflectionTestUtils.setField(period, "receiptCount", 1L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        assertEquals("PERIOD_HAS_RECEIPTS", assertThrows(ConflictException.class,
                () -> service.cancel(5, 0, "reason", 1)).code());
    }

    @Test
    void withdrawnSubmissionKeepsRevisionAndBecomesDraft() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        ReflectionTestUtils.setField(period, "revision", 2L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        service.withdraw(5, 0, 1);
        assertEquals("DRAFT", period.getStatus());
        assertEquals(2, period.getRevision());
    }

    @Test
    void returningSubmissionRequiresReasonAndStoresIt() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.returnToDraft(5, 0, "  ", 1)).code());
        service.returnToDraft(5, 0, " update due date ", 1);
        assertEquals("DRAFT", period.getStatus());
        assertEquals("update due date", period.getReturnReason());
    }

    @Test
    void submissionCalculatesTwoRoundedLinesForEveryActiveHousehold() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        when(households.lockActiveForBilling()).thenReturn(List.of(new HouseholdLookup.BillingHousehold(2, "H2", "A2", new BigDecimal("50"))));
        when(snapshots.lockRates(any())).thenReturn(List.of(
                new BillingSnapshotStore.Rate(1, "MANAGEMENT_FEE", "Management", CalculationMethod.AREA, 11, new BigDecimal("10000")),
                new BillingSnapshotStore.Rate(2, "WASTE_FEE", "Waste", CalculationMethod.PER_HOUSEHOLD, 12, new BigDecimal("30000"))));
        when(snapshots.save(eq(5L), eq(1L), eq(1L), any(), anyList(), eq(1L), eq(new BigDecimal("530000"))))
                .thenReturn(new BillingSnapshotStore.Submission(9, 1, new BigDecimal("530000")));
        service.submit(5, 0, 1);
        assertEquals("PENDING_APPROVAL", period.getStatus());
        assertEquals(1, period.getRevision());
        @SuppressWarnings("unchecked") ArgumentCaptor<List<BillingSnapshotStore.Line>> lines = ArgumentCaptor.forClass(List.class);
        verify(snapshots).save(eq(5L), eq(1L), eq(1L), any(), lines.capture(), eq(1L), eq(new BigDecimal("530000")));
        assertEquals(2, lines.getValue().size());
        assertEquals(new BigDecimal("500000"), lines.getValue().get(0).amount());
        assertEquals(new BigDecimal("30000"), lines.getValue().get(1).amount());
    }

    @Test
    void noActiveHouseholdsPreventsSubmission() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        when(households.lockActiveForBilling()).thenReturn(List.of());
        assertEquals("NO_ACTIVE_HOUSEHOLDS", assertThrows(ConflictException.class, () -> service.submit(5, 0, 1)).code());
        verify(snapshots, never()).save(anyLong(), anyLong(), anyLong(), any(), anyList(), anyLong(), any());
    }

    @Test
    void staleRevisionCannotIssueEvenWhenVersionMatches() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        ReflectionTestUtils.setField(period, "revision", 2L);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class, () -> service.issue(5, 0, 1, 1)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

}
