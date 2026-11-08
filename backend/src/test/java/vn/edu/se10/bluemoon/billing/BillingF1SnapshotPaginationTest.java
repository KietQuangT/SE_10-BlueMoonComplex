package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

class BillingF1SnapshotPaginationTest {
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final BillingReadStore reads = mock(BillingReadStore.class);
    private final BillingPeriodService service = new BillingPeriodService(periods, mock(AuditService.class),
            Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC), mock(HouseholdLookup.class),
            mock(BillingSnapshotStore.class), reads);

    @BeforeEach
    void setUp() {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, Instant.EPOCH);
        ReflectionTestUtils.setField(period, "id", 7L);
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        ReflectionTestUtils.setField(period, "revision", 3L);
        ReflectionTestUtils.setField(period, "version", 2L);
        when(periods.findById(7L)).thenReturn(Optional.of(period));
        when(reads.submission(7, 3)).thenReturn(new BillingReadStore.SubmissionInfo(15, 3, 1,
                Instant.parse("2026-11-08T02:00:00Z"), 5, new BigDecimal("2650000")));
    }

    @Test
    void pendingPreviewTraversesFirstMiddleLastAndEmptyPagesWithInvariantMetadata() {
        // 5 households total, page size = 2 => pages: 0 (h1, h2), 1 (h3, h4), 2 (h5), 3 (empty)
        when(reads.items(15, 0, 2)).thenReturn(List.of(
                item(1, CalculationMethod.AREA), item(1, CalculationMethod.PER_HOUSEHOLD),
                item(2, CalculationMethod.AREA), item(2, CalculationMethod.PER_HOUSEHOLD)));
        when(reads.items(15, 1, 2)).thenReturn(List.of(
                item(3, CalculationMethod.AREA), item(3, CalculationMethod.PER_HOUSEHOLD),
                item(4, CalculationMethod.AREA), item(4, CalculationMethod.PER_HOUSEHOLD)));
        when(reads.items(15, 2, 2)).thenReturn(List.of(
                item(5, CalculationMethod.AREA), item(5, CalculationMethod.PER_HOUSEHOLD)));
        when(reads.items(15, 3, 2)).thenReturn(List.of());

        // Page 0 (First page)
        var page0 = service.preview(7, 0, 2);
        assertEquals(5, page0.householdCount());
        assertEquals("2650000", page0.total());
        assertEquals(2L, page0.version());
        assertEquals(0, page0.households().page());
        assertEquals(2, page0.households().size());
        assertEquals(5, page0.households().totalElements());
        assertEquals(3, page0.households().totalPages());
        assertEquals(List.of(1L, 2L), page0.households().content().stream().map(h -> h.householdId()).toList());
        assertEquals(2, page0.households().content().get(0).lines().size());
        assertEquals(2, page0.households().content().get(1).lines().size());

        // Page 1 (Middle page)
        var page1 = service.preview(7, 1, 2);
        assertEquals(5, page1.householdCount());
        assertEquals("2650000", page1.total());
        assertEquals(2L, page1.version());
        assertEquals(1, page1.households().page());
        assertEquals(2, page1.households().size());
        assertEquals(5, page1.households().totalElements());
        assertEquals(3, page1.households().totalPages());
        assertEquals(List.of(3L, 4L), page1.households().content().stream().map(h -> h.householdId()).toList());

        // Page 2 (Final page)
        var page2 = service.preview(7, 2, 2);
        assertEquals(5, page2.householdCount());
        assertEquals("2650000", page2.total());
        assertEquals(2L, page2.version());
        assertEquals(2, page2.households().page());
        assertEquals(2, page2.households().size());
        assertEquals(5, page2.households().totalElements());
        assertEquals(3, page2.households().totalPages());
        assertEquals(List.of(5L), page2.households().content().stream().map(h -> h.householdId()).toList());

        // Page 3 (Empty page)
        var page3 = service.preview(7, 3, 2);
        assertEquals(5, page3.householdCount());
        assertEquals("2650000", page3.total());
        assertEquals(2L, page3.version());
        assertEquals(3, page3.households().page());
        assertEquals(2, page3.households().size());
        assertEquals(5, page3.households().totalElements());
        assertEquals(3, page3.households().totalPages());
        assertTrue(page3.households().content().isEmpty());

        verify(reads).items(15, 0, 2);
        verify(reads).items(15, 1, 2);
        verify(reads).items(15, 2, 2);
        verify(reads).items(15, 3, 2);
    }

    @Test
    void submissionHistoryMaintainsRevisionAndTotalsAcrossAllPages() {
        when(reads.items(15, 0, 2)).thenReturn(List.of(
                item(1, CalculationMethod.AREA), item(1, CalculationMethod.PER_HOUSEHOLD),
                item(2, CalculationMethod.AREA), item(2, CalculationMethod.PER_HOUSEHOLD)));
        when(reads.items(15, 2, 2)).thenReturn(List.of(
                item(5, CalculationMethod.AREA), item(5, CalculationMethod.PER_HOUSEHOLD)));
        when(reads.items(15, 4, 2)).thenReturn(List.of());

        var first = service.submission(7, 3L, 0, 2);
        assertEquals(3L, first.revision());
        assertEquals(Instant.parse("2026-11-08T02:00:00Z"), first.submittedAt());
        assertEquals(1L, first.submittedBy());
        assertEquals(5, first.householdCount());
        assertEquals("2650000", first.total());
        assertEquals(2L, first.version());
        assertEquals(2, first.households().content().size());

        var last = service.submission(7, 3L, 2, 2);
        assertEquals(3L, last.revision());
        assertEquals(5, last.householdCount());
        assertEquals("2650000", last.total());
        assertEquals(1, last.households().content().size());

        var empty = service.submission(7, 3L, 4, 2);
        assertEquals(3L, empty.revision());
        assertEquals(5, empty.householdCount());
        assertEquals("2650000", empty.total());
        assertTrue(empty.households().content().isEmpty());
    }

    @Test
    void readStorePaginationCalculatesLimitsOffsetsAndSkipsEmptyLinesQuery() {
        var jdbc = mock(JdbcTemplate.class);
        var store = new BillingReadStore(jdbc);

        // Page 0, size 2 -> LIMIT 2 OFFSET 0
        when(jdbc.queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(99L), eq(2), eq(0L)))
                .thenReturn(List.of(10L, 20L));
        when(jdbc.query(contains("household_id IN ("), org.mockito.ArgumentMatchers.<RowMapper<BillingReadStore.Item>>any(),
                eq(99L), eq(10L), eq(20L))).thenReturn(List.of(item(10, CalculationMethod.AREA), item(20, CalculationMethod.AREA)));
        var res0 = store.items(99L, 0, 2);
        assertEquals(2, res0.size());

        // Middle Page 1, size 2 -> LIMIT 2 OFFSET 2
        when(jdbc.queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(99L), eq(2), eq(2L)))
                .thenReturn(List.of(30L, 40L));
        when(jdbc.query(contains("household_id IN ("), org.mockito.ArgumentMatchers.<RowMapper<BillingReadStore.Item>>any(),
                eq(99L), eq(30L), eq(40L))).thenReturn(List.of(item(30, CalculationMethod.AREA), item(40, CalculationMethod.AREA)));
        var res1 = store.items(99L, 1, 2);
        assertEquals(2, res1.size());

        // Empty Page 5, size 2 -> LIMIT 2 OFFSET 10 -> empty list, no line query
        when(jdbc.queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(99L), eq(2), eq(10L)))
                .thenReturn(List.of());
        var resEmpty = store.items(99L, 5, 2);
        assertTrue(resEmpty.isEmpty());

        var ordered = inOrder(jdbc);
        ordered.verify(jdbc).queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(99L), eq(2), eq(0L));
        ordered.verify(jdbc).query(contains("?,?) ORDER BY household_id, fee_type_id"),
                org.mockito.ArgumentMatchers.<RowMapper<BillingReadStore.Item>>any(), eq(99L), eq(10L), eq(20L));
        ordered.verify(jdbc).queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(99L), eq(2), eq(2L));
        ordered.verify(jdbc).query(contains("?,?) ORDER BY household_id, fee_type_id"),
                org.mockito.ArgumentMatchers.<RowMapper<BillingReadStore.Item>>any(), eq(99L), eq(30L), eq(40L));
        ordered.verify(jdbc).queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(99L), eq(2), eq(10L));
        verifyNoMoreInteractions(jdbc);
    }

    private static BillingReadStore.Item item(long id, CalculationMethod method) {
        boolean area = method == CalculationMethod.AREA;
        return new BillingReadStore.Item(id, "H" + id, "A" + id, area ? new BigDecimal("50") : null,
                area ? "MANAGEMENT_FEE" : "WASTE_FEE", "Test", method, area ? new BigDecimal("50") : BigDecimal.ONE,
                area ? new BigDecimal("10000") : new BigDecimal("30000"), area ? 1 : 2,
                area ? new BigDecimal("500000") : new BigDecimal("30000"));
    }
}
