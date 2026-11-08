package vn.edu.se10.bluemoon.billing.application;

import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import vn.edu.se10.bluemoon.billing.api.BillingHouseholdResponse;
import vn.edu.se10.bluemoon.billing.api.BillingLineResponse;
import vn.edu.se10.bluemoon.billing.api.BillingPeriodResponse;
import vn.edu.se10.bluemoon.billing.api.BillingSubmissionResponse;
import vn.edu.se10.bluemoon.billing.api.InvoiceResponse;
import vn.edu.se10.bluemoon.billing.api.PageResponse;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;
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
    private final BillingReadStore reads;

    public BillingPeriodService(BillingPeriodRepository periods, AuditService audit, Clock clock, HouseholdLookup households, BillingSnapshotStore snapshots, BillingReadStore reads) {
        this.periods = periods;
        this.audit = audit;
        this.clock = clock;
        this.rules = new BillingPeriodRules(clock);
        this.households = households;
        this.snapshots = snapshots;
        this.reads = reads;
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
        var lines = snapshotLines(source);
        BigDecimal total = lines.stream().map(BillingSnapshotStore.Line::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var submission = snapshots.save(id, nextRevision, actor, now, lines, source.households().size(), total);
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
        if (!snapshotInputs(snapshots.inputs(submissionId)).matches(source.inputs())) {
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
    public BillingPeriod withdraw(long id, long version, long actor) {
        BillingPeriod period = editablePeriod(id, version, "PENDING_APPROVAL");
        period.markDraft(null);
        periods.flush();
        audit.record(actor, "BILLING_PERIOD_WITHDRAWN", "BillingPeriod", Long.toString(id),
                Map.of("period", period.getPeriod(), "revision", Long.toString(period.getRevision()), "status", "DRAFT"));
        return period;
    }

    @Transactional
    public BillingPeriod returnToDraft(long id, long version, String reason, long actor) {
        String normalizedReason = requiredReason(reason);
        BillingPeriod period = editablePeriod(id, version, "PENDING_APPROVAL");
        period.markDraft(normalizedReason);
        periods.flush();
        audit.record(actor, "BILLING_PERIOD_RETURNED", "BillingPeriod", Long.toString(id),
                Map.of("period", period.getPeriod(), "revision", Long.toString(period.getRevision()), "status", "DRAFT"));
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
        if (reason == null || reason.isBlank() || reason.strip().codePointCount(0, reason.strip().length()) > 255) {
            throw new BadRequestException("REASON_REQUIRED");
        }
        return reason.strip();
    }

    private record Source(List<HouseholdLookup.BillingHousehold> households, List<BillingSnapshotStore.Rate> rates) {
        BillingInputs inputs() {
            return new BillingInputs(households.stream().map(h -> new BillingInputs.Household(h.id(), h.areaM2())).toList(),
                    rates.stream().map(r -> new BillingInputs.Rate(r.code(), r.rateId(), r.unitPrice(), r.method())).toList());
        }
    }

    private static BillingInputs snapshotInputs(BillingSnapshotStore.SnapshotInputs inputs) {
        return new BillingInputs(inputs.households().stream().map(h -> new BillingInputs.Household(h.id(), h.areaM2())).toList(),
                inputs.rates().stream().map(r -> new BillingInputs.Rate(r.code(), r.rateId(), r.unitPrice(), r.method())).toList());
    }

    private List<BillingSnapshotStore.Line> snapshotLines(Source source) {
        var lines = new ArrayList<BillingSnapshotStore.Line>();
        var calculator = new FeeCalculator();
        for (var household : source.households()) {
            for (var rate : source.rates()) {
                BigDecimal area = rate.method() == CalculationMethod.AREA ? household.areaM2() : null;
                BigDecimal quantity = area == null ? BigDecimal.ONE : area;
                lines.add(new BillingSnapshotStore.Line(household.id(), household.householdCode(), household.apartmentCode(),
                        area, rate.typeId(), rate.code(), rate.name(), rate.method(), quantity, rate.unitPrice(),
                        rate.rateId(), calculator.calculate(rate.method(), area, rate.unitPrice())));
            }
        }
        return lines;
    }

    private Source lockSource(BillingPeriod period) {
        var active = households.lockActiveForBilling();
        if (active.isEmpty()) throw new ConflictException("NO_ACTIVE_HOUSEHOLDS");
        var rates = snapshots.lockRates(YearMonth.parse(period.getPeriod()).atDay(1));
        return new Source(active, rates);
    }

    @Transactional(readOnly = true)
    public BillingPeriodResponse detail(long id, String role) {
        BillingPeriod period = getPeriod(id);
        var summary = reads.summaries(List.of(id)).get(id);
        return response(period, role, summary);
    }

    @Transactional(readOnly = true)
    public PageResponse<BillingPeriodResponse> list(String month, String status, int page, int size, String role) {
        validatePage(page, size);
        if (month != null) {
            try {
                if (!month.matches("[0-9]{4}-(0[1-9]|1[0-2])")) throw new IllegalArgumentException();
                YearMonth.parse(month);
            } catch (RuntimeException ex) {
                throw new BadRequestException("INVALID_PERIOD");
            }
        }
        if (status != null && !List.of("DRAFT", "PENDING_APPROVAL", "ISSUED", "CANCELLED").contains(status)) {
            throw new BadRequestException("INVALID_FILTER");
        }
        var result = periods.search(month, status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "period", "id")));
        var summaries = reads.summaries(result.getContent().stream().map(BillingPeriod::getId).toList());
        return PageResponse.from(result.map(p -> response(p, role, summaries.get(p.getId()))));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public BillingSubmissionResponse preview(long id, int page, int size) {
        validatePage(page, size);
        BillingPeriod period = getPeriod(id);
        if ("PENDING_APPROVAL".equals(period.getStatus())) return submissionFor(period, period.getRevision(), page, size);
        if (!"DRAFT".equals(period.getStatus())) throw new ConflictException("INVALID_STATE");
        var active = households.listActiveForBilling();
        if (active.isEmpty()) throw new ConflictException("NO_ACTIVE_HOUSEHOLDS");
        var source = new Source(active, snapshots.readRates(YearMonth.parse(period.getPeriod()).atDay(1)));
        var lines = snapshotLines(source);
        var items = lines.stream().map(line -> new BillingReadStore.Item(line.householdId(), line.householdCode(),
                line.apartmentCode(), line.areaM2(), line.code(), line.name(), line.method(), line.quantity(),
                line.unitPrice(), line.rateId(), line.amount())).toList();
        BigDecimal total = lines.stream().map(BillingSnapshotStore.Line::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var householdsPage = householdsPage(items, page, size);
        return new BillingSubmissionResponse(null, null, null, active.size(), total.toPlainString(), period.getVersion(), householdsPage);
    }

    @Transactional(readOnly = true)
    public BillingSubmissionResponse submission(long id, Long revision, int page, int size) {
        validatePage(page, size);
        BillingPeriod period = getPeriod(id);
        long selected = revision == null ? period.getRevision() : revision;
        if (selected < 1) throw new BadRequestException("INVALID_FILTER");
        return submissionFor(period, selected, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> invoices(long id, int page, int size) {
        validatePage(page, size);
        getPeriod(id);
        long count = reads.invoiceCount(id);
        var rows = reads.invoices(id, size, (long) page * size);
        var mapped = invoiceResponses(rows);
        return new PageResponse<>(mapped, page, size, count, pages(count, size));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse invoice(long id) {
        return invoiceResponses(List.of(reads.invoice(id))).getFirst();
    }

    private BillingSubmissionResponse submissionFor(BillingPeriod period, long revision, int page, int size) {
        var submission = reads.submission(period.getId(), revision);
        return new BillingSubmissionResponse(submission.revision(), submission.submittedAt(), submission.submittedBy(),
                submission.count(), submission.total().toPlainString(), period.getVersion(), householdsPage(reads.items(submission.id()), page, size));
    }

    private PageResponse<BillingHouseholdResponse> householdsPage(List<BillingReadStore.Item> items, int page, int size) {
        Map<Long, List<BillingReadStore.Item>> grouped = new LinkedHashMap<>();
        for (var item : items) grouped.computeIfAbsent(item.householdId(), ignored -> new ArrayList<>()).add(item);
        var all = new ArrayList<BillingHouseholdResponse>();
        for (var rows : grouped.values()) {
            var first = rows.getFirst();
            var lines = rows.stream().map(i -> BillingLineResponse.of(i.code(), i.name(), i.method(), i.quantity(), i.areaM2(),
                    i.unitPrice(), i.rateId(), i.amount())).toList();
            var area = rows.stream().map(BillingReadStore.Item::areaM2).filter(java.util.Objects::nonNull).findFirst().orElse(null);
            var total = rows.stream().map(BillingReadStore.Item::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            all.add(new BillingHouseholdResponse(first.householdId(), first.householdCode(), first.apartmentCode(),
                    area == null ? null : area.toPlainString(), lines, total.toPlainString()));
        }
        long start = (long) page * size;
        int from = (int) Math.min(start, all.size());
        int to = Math.min(from + size, all.size());
        return new PageResponse<>(all.subList(from, to), page, size, all.size(), pages(all.size(), size));
    }

    private List<InvoiceResponse> invoiceResponses(List<BillingReadStore.InvoiceRow> rows) {
        var lines = reads.invoiceLines(rows.stream().map(BillingReadStore.InvoiceRow::id).toList());
        Map<Long, List<BillingLineResponse>> grouped = new HashMap<>();
        for (var line : lines) grouped.computeIfAbsent(line.invoiceId(), ignored -> new ArrayList<>()).add(
                BillingLineResponse.of(line.code(), line.name(), line.method(), line.quantity(), line.areaM2(), line.unitPrice(), line.rateId(), line.amount()));
        return rows.stream().map(row -> new InvoiceResponse(row.id(), row.periodId(), row.householdId(), row.householdCode(), row.apartmentCode(),
                row.period(), row.dueDate(), row.status(), row.issuedAt(), row.cancelledAt(), grouped.getOrDefault(row.id(), List.of()), row.total().toPlainString())).toList();
    }

    private BillingPeriodResponse response(BillingPeriod p, String role, BillingReadStore.Summary summary) {
        boolean old = YearMonth.parse(p.getPeriod()).isBefore(YearMonth.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh"))));
        boolean expired = old && ("DRAFT".equals(p.getStatus()) || "PENDING_APPROVAL".equals(p.getStatus()));
        boolean current = p.getPeriod().equals(YearMonth.now(clock.withZone(ZoneId.of("Asia/Ho_Chi_Minh"))).toString());
        boolean editable = current && "DRAFT".equals(p.getStatus()) && "ACCOUNTANT".equals(role);
        List<String> actions = new ArrayList<>();
        if ("DRAFT".equals(p.getStatus()) || "PENDING_APPROVAL".equals(p.getStatus())) actions.add("PREVIEW");
        if (editable) actions.addAll(List.of("UPDATE", "SUBMIT"));
        if (current && "PENDING_APPROVAL".equals(p.getStatus())) {
            if ("ACCOUNTANT".equals(role)) actions.add("WITHDRAW");
            if ("ADMIN".equals(role)) actions.addAll(List.of("RETURN", "ISSUE"));
        }
        if (current && "ISSUED".equals(p.getStatus()) && p.getReceiptCount() == 0 && "ADMIN".equals(role)) actions.add("CANCEL");
        return new BillingPeriodResponse(p.getId(), p.getPeriod(), p.getDueDate(), p.getStatus(), p.getVersion(), p.getRevision(),
                expired, editable, List.copyOf(actions), summary == null ? null : summary.count(),
                summary == null ? null : summary.total().toPlainString(), p.getCreatedAt(), p.getSubmittedAt(), p.getIssuedAt(),
                p.getCancelledAt(), p.getReturnReason(), p.getCancelReason(), p.getReplacesPeriodId(), p.getReceiptCount());
    }

    private BillingPeriod getPeriod(long id) {
        return periods.findById(id).orElseThrow(() -> new NotFoundException("BILLING_PERIOD_NOT_FOUND"));
    }

    private static void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new BadRequestException("INVALID_FILTER");
    }

    private static int pages(long count, int size) { return (int) Math.min(Integer.MAX_VALUE, (count + size - 1) / size); }

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
