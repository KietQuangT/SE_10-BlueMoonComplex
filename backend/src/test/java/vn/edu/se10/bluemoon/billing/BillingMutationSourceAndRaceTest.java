package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

class BillingMutationSourceAndRaceTest {
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final BillingSnapshotStore snapshots = mock(BillingSnapshotStore.class);
    private final HouseholdLookup households = mock(HouseholdLookup.class);
    private final AuditService audit = mock(AuditService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC);
    private final BillingPeriodService service = new BillingPeriodService(periods, audit, clock,
            households, snapshots, mock(BillingReadStore.class));

    private BillingPeriod period;

    @BeforeEach
    void setUp() {
        period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 10L);
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        ReflectionTestUtils.setField(period, "revision", 1L);
        ReflectionTestUtils.setField(period, "version", 1L);
        when(periods.lockById(10L)).thenReturn(Optional.of(period));
        when(snapshots.submissionId(10L, 1L)).thenReturn(100L);

        // Snapshot inputs at submission: 1 household (id 1, area 50), 2 rates (MANAGEMENT 10000 AREA, WASTE 30000 PER_HOUSEHOLD)
        when(snapshots.inputs(100L)).thenReturn(new BillingSnapshotStore.SnapshotInputs(
                List.of(new BillingSnapshotStore.HouseholdInput(1L, new BigDecimal("50"))),
                List.of(new BillingSnapshotStore.RateInput("MANAGEMENT_FEE", 11L, new BigDecimal("10000"), CalculationMethod.AREA),
                        new BillingSnapshotStore.RateInput("WASTE_FEE", 12L, new BigDecimal("30000"), CalculationMethod.PER_HOUSEHOLD))));
    }

    @Test
    void issueRejectsWhenAreaChangedBetweenSubmitAndIssue() {
        when(households.lockActiveForBilling()).thenReturn(List.of(
                new HouseholdLookup.BillingHousehold(1L, "H01", "A101", new BigDecimal("60"))));
        when(snapshots.lockRates(any())).thenReturn(List.of(
                new BillingSnapshotStore.Rate(1L, "MANAGEMENT_FEE", "Management", CalculationMethod.AREA, 11L, new BigDecimal("10000")),
                new BillingSnapshotStore.Rate(2L, "WASTE_FEE", "Waste", CalculationMethod.PER_HOUSEHOLD, 12L, new BigDecimal("30000"))));

        assertEquals("BILLING_INPUT_CHANGED", assertThrows(ConflictException.class,
                () -> service.issue(10L, 1L, 1L, 99L)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

    @Test
    void issueRejectsWhenActiveHouseholdsCountChanged() {
        // Additional active household added after submission
        when(households.lockActiveForBilling()).thenReturn(List.of(
                new HouseholdLookup.BillingHousehold(1L, "H01", "A101", new BigDecimal("50")),
                new HouseholdLookup.BillingHousehold(2L, "H02", "A102", new BigDecimal("50"))));
        when(snapshots.lockRates(any())).thenReturn(List.of(
                new BillingSnapshotStore.Rate(1L, "MANAGEMENT_FEE", "Management", CalculationMethod.AREA, 11L, new BigDecimal("10000")),
                new BillingSnapshotStore.Rate(2L, "WASTE_FEE", "Waste", CalculationMethod.PER_HOUSEHOLD, 12L, new BigDecimal("30000"))));

        assertEquals("BILLING_INPUT_CHANGED", assertThrows(ConflictException.class,
                () -> service.issue(10L, 1L, 1L, 99L)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

    @Test
    void issueRejectsWhenFeeRatePriceChanged() {
        when(households.lockActiveForBilling()).thenReturn(List.of(
                new HouseholdLookup.BillingHousehold(1L, "H01", "A101", new BigDecimal("50"))));
        when(snapshots.lockRates(any())).thenReturn(List.of(
                new BillingSnapshotStore.Rate(1L, "MANAGEMENT_FEE", "Management", CalculationMethod.AREA, 11L, new BigDecimal("12000")), // Changed from 10000 to 12000
                new BillingSnapshotStore.Rate(2L, "WASTE_FEE", "Waste", CalculationMethod.PER_HOUSEHOLD, 12L, new BigDecimal("30000"))));

        assertEquals("BILLING_INPUT_CHANGED", assertThrows(ConflictException.class,
                () -> service.issue(10L, 1L, 1L, 99L)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

    @Test
    void issueRejectsWhenFeeRateCalculationMethodChanged() {
        when(households.lockActiveForBilling()).thenReturn(List.of(
                new HouseholdLookup.BillingHousehold(1L, "H01", "A101", new BigDecimal("50"))));
        when(snapshots.lockRates(any())).thenReturn(List.of(
                new BillingSnapshotStore.Rate(1L, "MANAGEMENT_FEE", "Management", CalculationMethod.PER_HOUSEHOLD, 11L, new BigDecimal("10000")), // Method changed
                new BillingSnapshotStore.Rate(2L, "WASTE_FEE", "Waste", CalculationMethod.PER_HOUSEHOLD, 12L, new BigDecimal("30000"))));

        assertEquals("BILLING_INPUT_CHANGED", assertThrows(ConflictException.class,
                () -> service.issue(10L, 1L, 1L, 99L)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

    @Test
    void issueSucceedsWhenApartmentCodeOrAddressChangedWithoutChangingAreaOrId() {
        // Household renamed from H01/A101 to H01-renamed/A101-renamed, but id and area remain identical
        when(households.lockActiveForBilling()).thenReturn(List.of(
                new HouseholdLookup.BillingHousehold(1L, "H01-renamed", "A101-renamed", new BigDecimal("50"))));
        when(snapshots.lockRates(any())).thenReturn(List.of(
                new BillingSnapshotStore.Rate(1L, "MANAGEMENT_FEE", "Management", CalculationMethod.AREA, 11L, new BigDecimal("10000")),
                new BillingSnapshotStore.Rate(2L, "WASTE_FEE", "Waste", CalculationMethod.PER_HOUSEHOLD, 12L, new BigDecimal("30000"))));

        var issued = service.issue(10L, 1L, 1L, 99L);
        assertEquals("ISSUED", issued.getStatus());
        verify(snapshots).issue(10L, 100L, LocalDate.of(2026, 11, 30), clock.instant());
    }

    @Test
    void issueRejectsWhenDueDateHasPassedAtIssueTime() {
        // Set dueDate to yesterday (2026-11-07)
        period.setDueDate(LocalDate.of(2026, 11, 7));

        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> service.issue(10L, 1L, 1L, 99L)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

    @Test
    void issueRejectsStaleRevision() {
        // Requesting approval of revision 0 when current revision is 1
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class,
                () -> service.issue(10L, 1L, 0L, 99L)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }

    @Test
    void issueRejectsStalePeriodVersion() {
        // Requesting with version 0 when current version is 1
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class,
                () -> service.issue(10L, 0L, 1L, 99L)).code());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
    }
}
