package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;

class BillingReadStorePaginationTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final BillingReadStore store = new BillingReadStore(jdbc);

    @Test
    void selectsOnePageOfIdsAndBatchesOnlyThoseHouseholds() {
        var first = item(7);
        var second = item(9);
        when(jdbc.queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(42L), eq(2), eq(2L)))
                .thenReturn(List.of(7L, 9L));
        when(jdbc.query(contains("household_id IN ("), org.mockito.ArgumentMatchers.<RowMapper<BillingReadStore.Item>>any(),
                eq(42L), eq(7L), eq(9L))).thenReturn(List.of(first, second));
        assertEquals(List.of(first, second), store.items(42, 1, 2));
        var ordered = inOrder(jdbc);
        ordered.verify(jdbc).queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(42L), eq(2), eq(2L));
        ordered.verify(jdbc).query(contains("?,?) ORDER BY household_id, fee_type_id"),
                org.mockito.ArgumentMatchers.<RowMapper<BillingReadStore.Item>>any(), eq(42L), eq(7L), eq(9L));
        verifyNoMoreInteractions(jdbc);
    }

    @Test
    void emptyPageDoesNotQueryFeeLines() {
        when(jdbc.queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(42L), eq(2), eq(6L)))
                .thenReturn(List.of());
        assertEquals(List.of(), store.items(42, 3, 2));
        org.mockito.Mockito.verify(jdbc).queryForList(contains("LIMIT ? OFFSET ?"), eq(Long.class), eq(42L), eq(2), eq(6L));
        verifyNoMoreInteractions(jdbc);
    }

    private static BillingReadStore.Item item(long id) {
        return new BillingReadStore.Item(id, "H" + id, "A" + id, BigDecimal.ONE, "MANAGEMENT_FEE", "Test",
                CalculationMethod.AREA, BigDecimal.ONE, BigDecimal.ONE, 1, BigDecimal.ONE);
    }
}
