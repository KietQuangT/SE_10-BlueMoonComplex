package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

class BillingSubmissionPaginationTest {
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final BillingReadStore reads = mock(BillingReadStore.class);
    private final BillingPeriodService service = new BillingPeriodService(periods, mock(AuditService.class),
            Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC), mock(HouseholdLookup.class),
            mock(BillingSnapshotStore.class), reads);

    @BeforeEach
    void setUp() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, Instant.EPOCH);
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        ReflectionTestUtils.setField(period, "revision", 2L);
        when(periods.findById(5L)).thenReturn(Optional.of(period));
        when(reads.submission(5, 2)).thenReturn(new BillingReadStore.SubmissionInfo(10, 2, 1,
                Instant.EPOCH, 5, new BigDecimal("2650000")));
    }

    @Test
    void pendingPreviewUsesRequestedPageWithoutSlicingItAgain() {
        when(reads.items(10, 1, 2)).thenReturn(List.of(item(3, CalculationMethod.AREA), item(3, CalculationMethod.PER_HOUSEHOLD),
                item(4, CalculationMethod.AREA), item(4, CalculationMethod.PER_HOUSEHOLD)));
        var response = service.preview(5, 1, 2);
        assertEquals(List.of(3L, 4L), response.households().content().stream().map(h -> h.householdId()).toList());
        assertEquals(2, response.households().content().getFirst().lines().size());
        assertEquals(5, response.householdCount());
        assertEquals("2650000", response.total());
        assertEquals(3, response.households().totalPages());
        verify(reads).items(10, 1, 2);
    }

    @Test
    void historicalSubmissionPreservesTotalsForFinalAndEmptyPages() {
        when(reads.items(10, 2, 2)).thenReturn(List.of(item(5, CalculationMethod.AREA), item(5, CalculationMethod.PER_HOUSEHOLD)));
        when(reads.items(10, 3, 2)).thenReturn(List.of());
        var last = service.submission(5, 2L, 2, 2);
        var empty = service.submission(5, 2L, 3, 2);
        assertEquals(List.of(5L), last.households().content().stream().map(h -> h.householdId()).toList());
        assertEquals(2, last.households().content().getFirst().lines().size());
        assertEquals(List.of(), empty.households().content());
        assertEquals(5, empty.households().totalElements());
        assertEquals("2650000", empty.total());
        verify(reads).items(10, 2, 2);
        verify(reads).items(10, 3, 2);
    }

    private static BillingReadStore.Item item(long id, CalculationMethod method) {
        boolean area = method == CalculationMethod.AREA;
        return new BillingReadStore.Item(id, "H" + id, "A" + id, area ? new BigDecimal("50") : null,
                area ? "MANAGEMENT_FEE" : "WASTE_FEE", "Test", method, area ? new BigDecimal("50") : BigDecimal.ONE,
                area ? new BigDecimal("10000") : new BigDecimal("30000"), area ? 1 : 2,
                area ? new BigDecimal("500000") : new BigDecimal("30000"));
    }
}
