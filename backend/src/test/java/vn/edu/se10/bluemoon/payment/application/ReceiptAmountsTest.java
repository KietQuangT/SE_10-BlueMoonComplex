package vn.edu.se10.bluemoon.payment.application;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReceiptAmountsTest {
    @Test void partialPaymentReducesDebtWithoutCredit() {
        var result = ReceiptAmounts.apply(new BigDecimal("530000"), new BigDecimal("200000"));
        assertEquals(new BigDecimal("200000"), result.applied());
        assertEquals(new BigDecimal("330000"), result.remaining());
        assertEquals(BigDecimal.ZERO, result.credit());
    }
    @Test void excessAndAlreadyPaidBecomeCredit() {
        var result = ReceiptAmounts.apply(new BigDecimal("330000"), new BigDecimal("400000"));
        assertEquals(new BigDecimal("330000"), result.applied());
        assertEquals(BigDecimal.ZERO, result.remaining());
        assertEquals(new BigDecimal("70000"), result.credit());
        assertEquals(new BigDecimal("50"), ReceiptAmounts.apply(BigDecimal.ZERO, new BigDecimal("50")).credit());
    }
    @Test void rejectsHugeExponentWithoutExpandingIt() {
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () -> {
            assertThrows(IllegalArgumentException.class, () -> ReceiptAmounts.apply(BigDecimal.ONE, new BigDecimal("1E+100000000")));
            assertThrows(IllegalArgumentException.class, () -> ReceiptAmounts.apply(BigDecimal.ONE, new BigDecimal("1E-100000000")));
        });
    }
    @Test void rejectsInvalidReceiptAmount() {
        assertThrows(IllegalArgumentException.class, () -> ReceiptAmounts.apply(BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> ReceiptAmounts.apply(BigDecimal.ONE, new BigDecimal("1.1")));
        assertThrows(IllegalArgumentException.class, () -> ReceiptAmounts.apply(BigDecimal.ONE, new BigDecimal("1000000000000000000000000")));
    }
}
