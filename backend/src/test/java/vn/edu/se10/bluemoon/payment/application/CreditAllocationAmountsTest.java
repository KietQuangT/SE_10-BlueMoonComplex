package vn.edu.se10.bluemoon.payment.application;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreditAllocationAmountsTest {
    @Test void consumesOldestCreditFirstAndMaySpanSources() {
        var sources = List.of(new CreditAllocationAmounts.Source(10, new BigDecimal("30")),
                new CreditAllocationAmounts.Source(11, new BigDecimal("70")));
        assertEquals(List.of(new CreditAllocationAmounts.Share(10, new BigDecimal("30")),
                new CreditAllocationAmounts.Share(11, new BigDecimal("20"))),
                CreditAllocationAmounts.allocate(sources, new BigDecimal("50")));
    }
    @Test void rejectsAllocationExceedingAvailableCredit() {
        var sources = List.of(new CreditAllocationAmounts.Source(10, new BigDecimal("30")));
        assertThrows(IllegalArgumentException.class, () -> CreditAllocationAmounts.allocate(sources, new BigDecimal("31")));
    }
}
