package vn.edu.se10.bluemoon.billing.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.common.error.ConflictException;

class PaymentInvoiceAccessTest {
    @Test void locksPeriodAndReturnsOnlyIssuedInvoice() {
        var read = mock(BillingReadStore.class);
        var periods = mock(BillingPeriodRepository.class);
        var invoice = new BillingReadStore.InvoiceRow(1, 2, 3, "H", "A", "2026-11", LocalDate.of(2026, 11, 30), "ISSUED", Instant.EPOCH, null, new BigDecimal("530000"));
        var period = mock(BillingPeriod.class);
        when(read.invoice(1)).thenReturn(invoice);
        when(periods.lockById(2)).thenReturn(Optional.of(period));
        when(period.getStatus()).thenReturn("ISSUED");
        var result = new PaymentInvoiceAccess(read, periods, mock(BillingReceiptGuard.class)).lockIssued(1);
        assertEquals(3, result.householdId());
        assertEquals(new BigDecimal("530000"), result.total());
        verify(periods).lockById(2);
        verify(read, times(2)).invoice(1);
    }
    @Test void delegatesReceiptCountToBillingGuard() {
        var guard = mock(BillingReceiptGuard.class);
        new PaymentInvoiceAccess(mock(BillingReadStore.class), mock(BillingPeriodRepository.class), guard).incrementReceiptCount(42);
        verify(guard).recordReceipt(42);
    }
    @Test void rejectsCancelledPeriodAfterLock() {
        var read = mock(BillingReadStore.class);
        var periods = mock(BillingPeriodRepository.class);
        var invoice = new BillingReadStore.InvoiceRow(1, 2, 3, "H", "A", "2026-11", LocalDate.of(2026, 11, 30), "ISSUED", Instant.EPOCH, null, BigDecimal.ONE);
        var period = mock(BillingPeriod.class);
        when(read.invoice(1)).thenReturn(invoice);
        when(periods.lockById(2)).thenReturn(Optional.of(period));
        when(period.getStatus()).thenReturn("CANCELLED");
        assertThrows(ConflictException.class, () -> new PaymentInvoiceAccess(read, periods, mock(BillingReceiptGuard.class)).lockIssued(1));
    }
}
