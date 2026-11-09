package vn.edu.se10.bluemoon.billing.application;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

/** Billing boundary for payment; the caller owns a READ_COMMITTED transaction. */
@Service
public class PaymentInvoiceAccess {
    private final BillingReadStore invoices;
    private final BillingPeriodRepository periods;
    private final BillingReceiptGuard receiptGuard;

    public PaymentInvoiceAccess(BillingReadStore invoices, BillingPeriodRepository periods, BillingReceiptGuard receiptGuard) {
        this.invoices = invoices;
        this.periods = periods;
        this.receiptGuard = receiptGuard;
    }

    public record Invoice(long id, long periodId, long householdId, BigDecimal total, Instant issuedAt, String status) { }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public Invoice read(long invoiceId) {
        var invoice = invoices.invoice(invoiceId);
        return new Invoice(invoice.id(), invoice.periodId(), invoice.householdId(), invoice.total(), invoice.issuedAt(), invoice.status());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Invoice lockIssued(long invoiceId) {
        var initial = invoices.invoice(invoiceId);
        var period = periods.lockById(initial.periodId())
                .orElseThrow(() -> new NotFoundException("BILLING_PERIOD_NOT_FOUND"));
        if (!"ISSUED".equals(period.getStatus())) throw new ConflictException("INVALID_STATE");
        var invoice = invoices.invoice(invoiceId);
        if (!"ISSUED".equals(invoice.status())) throw new ConflictException("INVALID_STATE");
        return new Invoice(invoice.id(), invoice.periodId(), invoice.householdId(), invoice.total(), invoice.issuedAt(), invoice.status());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void incrementReceiptCount(long periodId) {
        receiptGuard.recordReceipt(periodId);
    }
}
