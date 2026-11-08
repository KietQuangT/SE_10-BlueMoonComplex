package vn.edu.se10.bluemoon.billing.application;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@Service
public class BillingPeriodService {
    private final BillingPeriodRepository periods;
    private final AuditService audit;
    private final Clock clock;
    private final BillingPeriodRules rules;

    public BillingPeriodService(BillingPeriodRepository periods, AuditService audit, Clock clock) {
        this.periods = periods;
        this.audit = audit;
        this.clock = clock;
        this.rules = new BillingPeriodRules(clock);
    }

    @Transactional
    public BillingPeriod create(String period, LocalDate dueDate, long actor) {
        rules.validateCreation(period, dueDate);
        if (periods.existsByPeriodAndStatusNot(period, "CANCELLED")) {
            throw new ConflictException("BILLING_PERIOD_EXISTS");
        }
        Long previous = periods.findFirstByPeriodAndStatusOrderByIdDesc(period, "CANCELLED")
                .map(BillingPeriod::getId).orElse(null);
        BillingPeriod created;
        try {
            created = periods.saveAndFlush(new BillingPeriod(period, dueDate, previous, actor, clock.instant()));
        } catch (DataIntegrityViolationException failure) {
            if (duplicateActiveMonth(failure)) throw new ConflictException("BILLING_PERIOD_EXISTS");
            throw failure;
        }
        audit.record(actor, "BILLING_PERIOD_CREATED", "BillingPeriod", created.getId().toString(),
                Map.of("period", period, "status", "DRAFT", "dueDate", dueDate.toString()));
        return created;
    }

    private boolean duplicateActiveMonth(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && sql.getErrorCode() == 1062
                    && sql.getMessage() != null && sql.getMessage().contains("uq_billing_period_active_month")) {
                return true;
            }
        }
        return false;
    }
}
