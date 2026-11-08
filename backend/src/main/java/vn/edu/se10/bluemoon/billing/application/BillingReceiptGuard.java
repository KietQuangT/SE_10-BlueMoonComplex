package vn.edu.se10.bluemoon.billing.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

/** S09 must call this in the same transaction as creating each mandatory receipt. */
@Service
public class BillingReceiptGuard {
    private final BillingPeriodRepository periods;

    public BillingReceiptGuard(BillingPeriodRepository periods) {
        this.periods = periods;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordReceipt(long periodId) {
        var period = periods.lockById(periodId)
                .orElseThrow(() -> new NotFoundException("BILLING_PERIOD_NOT_FOUND"));
        if (!"ISSUED".equals(period.getStatus())) throw new ConflictException("INVALID_STATE");
        period.incrementReceiptCount();
        periods.flush();
    }
}
