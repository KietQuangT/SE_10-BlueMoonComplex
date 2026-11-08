package vn.edu.se10.bluemoon.billing.application;

import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

@Service
public class BillingPeriodService {
    private final BillingPeriodRepository periods;
    private final AuditService audit;
    private final Clock clock;
    private final BillingPeriodRules rules;
    private final HouseholdLookup households;
    private final BillingSnapshotStore snapshots;

    public BillingPeriodService(BillingPeriodRepository periods, AuditService audit, Clock clock, HouseholdLookup households, BillingSnapshotStore snapshots) {
        this.periods = periods;
        this.audit = audit;
        this.clock = clock;
        this.rules = new BillingPeriodRules(clock);
        this.households = households;
        this.snapshots = snapshots;
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

    @Transactional
    public BillingPeriod updateDueDate(long id, LocalDate dueDate, long version, long actor) {
        BillingPeriod period = editablePeriod(id, version, "DRAFT");
        rules.requireDueDateInMonth(YearMonth.parse(period.getPeriod()), dueDate);
        period.setDueDate(dueDate);
        periods.flush();
        audit.record(actor, "BILLING_PERIOD_UPDATED", "BillingPeriod", Long.toString(id),
                Map.of("period", period.getPeriod(), "dueDate", dueDate.toString()));
        return period;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BillingPeriod submit(long id, long version, long actor) {
        BillingPeriod period = editablePeriod(id, version, "DRAFT");
        var source = lockSource(period);
        var now = clock.instant();
        long nextRevision = period.getRevision() + 1;
        var submission = snapshots.save(id, nextRevision, actor, now, source);
        period.markSubmitted(actor, now);
        periods.flush();
        audit.record(actor, "BILLING_PERIOD_SUBMITTED", "BillingPeriod", Long.toString(id),
                Map.of("period", period.getPeriod(), "revision", Long.toString(nextRevision),
                        "count", Long.toString(submission.count()), "total", submission.total().toPlainString()));
        return period;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BillingPeriod issue(long id, long version, long revision, long actor) {
        BillingPeriod period = editablePeriod(id, version, "PENDING_APPROVAL");
        if (period.getRevision() != revision) throw new ConflictException("STALE_VERSION");
        rules.requireDueDateNotPast(period.getDueDate());
        var source = lockSource(period);
        long submissionId = snapshots.submissionId(id, revision);
        if (!snapshots.inputs(submissionId).matches(source.inputs())) {
            throw new ConflictException("BILLING_INPUT_CHANGED");
        }
        var now = clock.instant();
        snapshots.issue(id, submissionId, period.getDueDate(), now);
        period.markIssued(actor, now);
        periods.flush();
        audit.record(actor, "BILLING_PERIOD_ISSUED", "BillingPeriod", Long.toString(id),
                Map.of("period", period.getPeriod(), "revision", Long.toString(revision), "status", "ISSUED"));
        return period;
    }

    @Transactional
    public BillingPeriod cancel(long id, long version, String reason, long actor) {
        String normalizedReason = requiredReason(reason);
        BillingPeriod period = editablePeriod(id, version, "ISSUED");
        if (period.getReceiptCount() != 0) throw new ConflictException("PERIOD_HAS_RECEIPTS");
        var now = clock.instant();
        snapshots.cancelInvoices(id, now);
        period.markCancelled(actor, now, normalizedReason);
        periods.flush();
        audit.record(actor, "BILLING_PERIOD_CANCELLED", "BillingPeriod", Long.toString(id),
                Map.of("period", period.getPeriod(), "status", "CANCELLED"));
        return period;
    }

    private String requiredReason(String reason) {
        if (reason == null || reason.isBlank() || reason.trim().length() > 255) {
            throw new BadRequestException("REASON_REQUIRED");
        }
        return reason.trim();
    }

    private BillingSnapshotStore.Source lockSource(BillingPeriod period) {
        var active = households.lockActiveForBilling();
        if (active.isEmpty()) throw new ConflictException("NO_ACTIVE_HOUSEHOLDS");
        var rates = snapshots.lockRates(YearMonth.parse(period.getPeriod()).atDay(1));
        return new BillingSnapshotStore.Source(active, rates);
    }

    private BillingPeriod editablePeriod(long id, long version, String expectedStatus) {
        BillingPeriod period = periods.lockById(id)
                .orElseThrow(() -> new NotFoundException("BILLING_PERIOD_NOT_FOUND"));
        if (period.getVersion() != version) throw new ConflictException("STALE_VERSION");
        rules.requireCurrent(period.getPeriod());
        if (!expectedStatus.equals(period.getStatus())) throw new ConflictException("INVALID_STATE");
        return period;
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
