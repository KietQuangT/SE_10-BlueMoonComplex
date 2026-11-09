package vn.edu.se10.bluemoon.payment.application;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import vn.edu.se10.bluemoon.billing.application.PaymentInvoiceAccess;
import vn.edu.se10.bluemoon.billing.application.PaymentInvoiceQuery;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

@Service
public class PaymentService {
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");
    private final JdbcTemplate jdbc;
    private final PaymentInvoiceAccess billing;
    private final PaymentInvoiceQuery invoiceQuery;
    private final AuditService audit;
    private final ObjectMapper mapper;
    private final Clock clock;

    public PaymentService(JdbcTemplate jdbc, PaymentInvoiceAccess billing, PaymentInvoiceQuery invoiceQuery, AuditService audit, ObjectMapper mapper, Clock clock) {
        this.jdbc = jdbc; this.billing = billing; this.invoiceQuery = invoiceQuery; this.audit = audit; this.mapper = mapper; this.clock = clock;
    }
    public record Receipt(long receiptId, long invoiceId, String paidOn, String amount, String appliedAmount,
                          String creditAmount, String debtAfter, String status, String recordedAt,
                          boolean hasLateReason, boolean hasVoidReason) { }
    public record Allocation(long allocationId, long invoiceId, long householdId, String amount, String debtAfter, String status,
                             boolean hasReverseReason) { }
    public record Balance(long householdId, String balance) { }
    public record Summary(long invoiceId, String total, String settled, String remaining, String status) { }
    public record ReceiptHistory(long receiptId, long invoiceId, String paidOn, String amount, String appliedAmount,
                                 String creditAmount, String debtAfter, String status, String recordedAt, long recordedBy,
                                 boolean hasLateReason, Long voidedBy, String voidedAt, boolean hasVoidReason,
                                 long activeAllocationCount, boolean canVoid) { }
    public record Page(List<ReceiptHistory> content, int page, int size, long totalElements, int totalPages) { }
    public record AllocationSource(long receiptId, String amount) { }
    public record AllocationHistory(long allocationId, long invoiceId, long householdId, String amount, String debtAfter,
                                    String status, long recordedBy, String recordedAt, Long reversedBy, String reversedAt,
                                    boolean hasReverseReason, List<AllocationSource> sources) { }
    public record AllocationPage(List<AllocationHistory> content, int page, int size, long totalElements, int totalPages) { }
    private record Command(String fingerprint, String result) { }
    private record ReceiptRef(long invoiceId, long householdId, String status) { }
    private record AllocationRef(long invoiceId, long householdId, String status) { }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Receipt receipt(long invoiceId, BigDecimal amount, LocalDate paidOn, String lateReason, String key, long actor) {
        BigDecimal value = money(amount);
        if (paidOn == null) throw new BadRequestException("INVALID_DATE");
        String reason = optionalReason(lateReason);
        return command(key, List.of("RECEIPT", actor, invoiceId, value.toPlainString(), paidOn.toString(), reason == null ? "" : reason), Receipt.class, () -> {
            var invoice = billing.lockIssued(invoiceId);
            var today = LocalDate.now(clock.withZone(VIETNAM));
            if (paidOn.isBefore(invoice.issuedAt().atZone(VIETNAM).toLocalDate()) || paidOn.isAfter(today)) throw new BadRequestException("INVALID_PAID_ON");
            if (paidOn.isBefore(today) && reason == null) throw new BadRequestException("LATE_REASON_REQUIRED");
            lockHousehold(invoice.householdId());
            var split = ReceiptAmounts.apply(debt(invoice.id(), invoice.total()), value);
            var now = clock.instant();
            long id = insert("INSERT INTO payment_receipts (invoice_id, household_id, amount, applied_amount, credit_amount, debt_after, paid_on, late_reason, status, recorded_by, recorded_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'POSTED', ?, ?)",
                    invoice.id(), invoice.householdId(), value, split.applied(), split.credit(), split.remaining(), paidOn, reason, actor, Timestamp.from(now));
            billing.incrementReceiptCount(invoice.periodId());
            audit.record(actor, "RECEIPT_RECORDED", "PaymentReceipt", Long.toString(id), Map.of("invoiceId", Long.toString(invoiceId), "amount", value.toPlainString()));
            return new Receipt(id, invoiceId, paidOn.toString(), value.toPlainString(), split.applied().toPlainString(), split.credit().toPlainString(), split.remaining().toPlainString(), "POSTED", now.toString(), reason != null, false);
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Receipt voidReceipt(long receiptId, String reason, String key, long actor) {
        String text = requiredReason(reason);
        return command(key, List.of("VOID_RECEIPT", actor, receiptId, text), Receipt.class, () -> {
            var ref = receiptRef(receiptId);
            var invoice = billing.lockIssued(ref.invoiceId());
            lockHousehold(ref.householdId());
            ref = receiptRef(receiptId);
            if (!"POSTED".equals(ref.status())) throw new ConflictException("RECEIPT_ALREADY_VOIDED");
            Long used = jdbc.queryForObject("SELECT COUNT(*) FROM payment_credit_allocation_sources s JOIN payment_credit_allocations a ON a.id=s.allocation_id WHERE s.receipt_id=? AND a.status='ACTIVE'", Long.class, receiptId);
            if (used != null && used > 0) throw new ConflictException("RECEIPT_CREDIT_IN_USE");
            jdbc.update("UPDATE payment_receipts SET status='VOIDED', voided_by=?, voided_at=?, void_reason=? WHERE id=?", actor, Timestamp.from(clock.instant()), text, receiptId);
            audit.record(actor, "RECEIPT_VOIDED", "PaymentReceipt", Long.toString(receiptId), Map.of("invoiceId", Long.toString(invoice.id())));
            var row = receiptRow(receiptId);
            return new Receipt(row.receiptId(), row.invoiceId(), row.paidOn(), row.amount(), row.appliedAmount(),
                    row.creditAmount(), debt(invoice.id(), invoice.total()).toPlainString(), row.status(), row.recordedAt(), row.hasLateReason(), row.hasVoidReason());
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Allocation allocate(long householdId, long invoiceId, BigDecimal amount, String key, long actor) {
        BigDecimal value = money(amount);
        return command(key, List.of("ALLOCATE", actor, householdId, invoiceId, value.toPlainString()), Allocation.class, () -> {
            var invoice = billing.lockIssued(invoiceId);
            if (invoice.householdId() != householdId) throw new ConflictException("HOUSEHOLD_MISMATCH");
            lockHousehold(householdId);
            BigDecimal remaining = debt(invoiceId, invoice.total());
            if (value.compareTo(remaining) > 0) throw new ConflictException("ALLOCATION_EXCEEDS_DEBT");
            var sources = jdbc.query("SELECT r.id, r.credit_amount - COALESCE((SELECT SUM(s.amount) FROM payment_credit_allocation_sources s JOIN payment_credit_allocations a ON a.id=s.allocation_id WHERE s.receipt_id=r.id AND a.status='ACTIVE'), 0) available FROM payment_receipts r WHERE r.household_id=? AND r.status='POSTED' AND r.credit_amount>0 ORDER BY r.id", (rs, n) -> new CreditAllocationAmounts.Source(rs.getLong("id"), rs.getBigDecimal("available")), householdId);
            List<CreditAllocationAmounts.Share> shares;
            try { shares = CreditAllocationAmounts.allocate(sources, value); }
            catch (IllegalArgumentException ex) { throw new ConflictException("INSUFFICIENT_CREDIT"); }
            BigDecimal after = remaining.subtract(value);
            long id = insert("INSERT INTO payment_credit_allocations (household_id, invoice_id, amount, debt_after, status, recorded_by, recorded_at) VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?)", householdId, invoiceId, value, after, actor, Timestamp.from(clock.instant()));
            for (var share : shares) jdbc.update("INSERT INTO payment_credit_allocation_sources (allocation_id, receipt_id, amount) VALUES (?, ?, ?)", id, share.receiptId(), share.amount());
            billing.incrementReceiptCount(invoice.periodId());
            audit.record(actor, "CREDIT_ALLOCATED", "CreditAllocation", Long.toString(id), Map.of("invoiceId", Long.toString(invoiceId), "amount", value.toPlainString()));
            return new Allocation(id, invoiceId, householdId, value.toPlainString(), after.toPlainString(), "ACTIVE", false);
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Allocation reverse(long allocationId, String reason, String key, long actor) {
        String text = requiredReason(reason);
        return command(key, List.of("REVERSE_ALLOCATION", actor, allocationId, text), Allocation.class, () -> {
            var ref = allocationRef(allocationId);
            var invoice = billing.lockIssued(ref.invoiceId());
            lockHousehold(ref.householdId());
            ref = allocationRef(allocationId);
            if (!"ACTIVE".equals(ref.status())) throw new ConflictException("ALLOCATION_ALREADY_REVERSED");
            jdbc.update("UPDATE payment_credit_allocations SET status='REVERSED', reversed_by=?, reversed_at=?, reverse_reason=? WHERE id=?", actor, Timestamp.from(clock.instant()), text, allocationId);
            audit.record(actor, "CREDIT_ALLOCATION_REVERSED", "CreditAllocation", Long.toString(allocationId), Map.of("invoiceId", Long.toString(ref.invoiceId())));
            var row = allocationRow(allocationId);
            return new Allocation(row.allocationId(), row.invoiceId(), row.householdId(), row.amount(),
                    debt(invoice.id(), invoice.total()).toPlainString(), row.status(), row.hasReverseReason());
        });
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Summary summary(long invoiceId) {
        var invoice = billing.read(invoiceId);
        BigDecimal remaining = debt(invoiceId, invoice.total());
        BigDecimal settled = invoice.total().subtract(remaining);
        if ("CANCELLED".equals(invoice.status())) {
            return new Summary(invoiceId, invoice.total().toPlainString(), settled.toPlainString(), "0", "CANCELLED");
        }
        String status = remaining.signum() == 0 ? "PAID" : settled.signum() == 0 ? "UNPAID" : "PARTIALLY_PAID";
        return new Summary(invoiceId, invoice.total().toPlainString(), settled.toPlainString(), remaining.toPlainString(), status);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Balance creditBalance(long householdId) {
        Long found = jdbc.queryForObject("SELECT COUNT(*) FROM households WHERE id=?", Long.class, householdId);
        if (found == null || found == 0) throw new NotFoundException("HOUSEHOLD_NOT_FOUND");
        BigDecimal amount = jdbc.queryForObject("SELECT COALESCE(SUM(r.credit_amount - COALESCE((SELECT SUM(s.amount) FROM payment_credit_allocation_sources s JOIN payment_credit_allocations a ON a.id=s.allocation_id WHERE s.receipt_id=r.id AND a.status='ACTIVE'),0)),0) FROM payment_receipts r WHERE r.household_id=? AND r.status='POSTED'", BigDecimal.class, householdId);
        return new Balance(householdId, amount.toPlainString());
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PaymentInvoiceQuery.Page paymentInvoices(String q, String period, String paymentStatus, int page, int size) {
        return invoiceQuery.search(q, period, paymentStatus, page, size);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page receipts(long invoiceId, int page, int size) {
        checkPage(page, size);
        billing.read(invoiceId);
        long total = jdbc.queryForObject("SELECT COUNT(*) FROM payment_receipts WHERE invoice_id=?", Long.class, invoiceId);
        List<ReceiptHistory> rows = jdbc.query("""
                SELECT r.id, r.invoice_id, r.paid_on, r.amount, r.applied_amount, r.credit_amount, r.debt_after,
                       r.status, r.recorded_at, r.recorded_by, r.voided_by, r.voided_at,
                       r.late_reason IS NOT NULL AS has_late_reason, r.void_reason IS NOT NULL AS has_void_reason,
                       (SELECT COUNT(*) FROM payment_credit_allocation_sources s
                    JOIN payment_credit_allocations a ON a.id=s.allocation_id
                    WHERE s.receipt_id=r.id AND a.status='ACTIVE') active_count
                FROM payment_receipts r WHERE r.invoice_id=? ORDER BY r.id DESC LIMIT ? OFFSET ?
                """, (rs, n) -> {
                    long active = rs.getLong("active_count"); String status = rs.getString("status");
                    return new ReceiptHistory(rs.getLong("id"), rs.getLong("invoice_id"), rs.getDate("paid_on").toLocalDate().toString(),
                            rs.getBigDecimal("amount").toPlainString(), rs.getBigDecimal("applied_amount").toPlainString(),
                            rs.getBigDecimal("credit_amount").toPlainString(), rs.getBigDecimal("debt_after").toPlainString(),
                            status, timestamp(rs.getTimestamp("recorded_at")), rs.getLong("recorded_by"), rs.getBoolean("has_late_reason"),
                            rs.getObject("voided_by", Long.class), timestamp(rs.getTimestamp("voided_at")), rs.getBoolean("has_void_reason"),
                            active, "POSTED".equals(status) && active == 0);
                }, invoiceId, size, (long) page * size);
        return new Page(rows, page, size, total, pages(total, size));
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AllocationPage allocations(long householdId, Long invoiceId, Long receiptId, String status, int page, int size) {
        checkPage(page, size);
        if (status != null && !status.isBlank() && !List.of("ACTIVE", "REVERSED", "ALL").contains(status)) throw new BadRequestException("INVALID_FILTER");
        if (status == null || status.isBlank() || "ALL".equals(status)) status = null;
        Long found = jdbc.queryForObject("SELECT COUNT(*) FROM households WHERE id=?", Long.class, householdId);
        if (found == null || found == 0) throw new NotFoundException("HOUSEHOLD_NOT_FOUND");
        String where = " WHERE a.household_id=? AND (? IS NULL OR a.invoice_id=?) AND (? IS NULL OR a.status=?) AND (? IS NULL OR EXISTS (SELECT 1 FROM payment_credit_allocation_sources s WHERE s.allocation_id=a.id AND s.receipt_id=?))";
        long total = jdbc.queryForObject("SELECT COUNT(*) FROM payment_credit_allocations a" + where, Long.class, householdId, invoiceId, invoiceId, status, status, receiptId, receiptId);
        List<AllocationHistory> rows = jdbc.query("SELECT a.id, a.invoice_id, a.household_id, a.amount, a.debt_after, a.status, a.recorded_by, a.recorded_at, a.reversed_by, a.reversed_at, a.reverse_reason IS NOT NULL AS has_reverse_reason FROM payment_credit_allocations a" + where + " ORDER BY a.id DESC LIMIT ? OFFSET ?", (rs, n) -> {
            return new AllocationHistory(rs.getLong("id"), rs.getLong("invoice_id"), rs.getLong("household_id"), rs.getBigDecimal("amount").toPlainString(), rs.getBigDecimal("debt_after").toPlainString(), rs.getString("status"), rs.getLong("recorded_by"), timestamp(rs.getTimestamp("recorded_at")), rs.getObject("reversed_by", Long.class), timestamp(rs.getTimestamp("reversed_at")), rs.getBoolean("has_reverse_reason"), List.of());
        }, householdId, invoiceId, invoiceId, status, status, receiptId, receiptId, size, (long) page * size);
        if (rows.isEmpty()) return new AllocationPage(rows, page, size, total, pages(total, size));
        String placeholders = String.join(",", java.util.Collections.nCopies(rows.size(), "?"));
        Map<Long, List<AllocationSource>> sources = new java.util.HashMap<>();
        jdbc.query("SELECT allocation_id, receipt_id, amount FROM payment_credit_allocation_sources WHERE allocation_id IN (" + placeholders + ") ORDER BY allocation_id, receipt_id", rs -> {
            sources.computeIfAbsent(rs.getLong("allocation_id"), ignored -> new java.util.ArrayList<>())
                    .add(new AllocationSource(rs.getLong("receipt_id"), rs.getBigDecimal("amount").toPlainString()));
        }, rows.stream().map(AllocationHistory::allocationId).toArray());
        List<AllocationHistory> result = rows.stream().map(row -> new AllocationHistory(row.allocationId(), row.invoiceId(), row.householdId(), row.amount(), row.debtAfter(), row.status(), row.recordedBy(), row.recordedAt(), row.reversedBy(), row.reversedAt(), row.hasReverseReason(), List.copyOf(sources.getOrDefault(row.allocationId(), List.of())))).toList();
        return new AllocationPage(result, page, size, total, pages(total, size));
    }

    private static void checkPage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new BadRequestException("VALIDATION");
    }
    private static int pages(long total, int size) { return (int) Math.min(Integer.MAX_VALUE, (total + size - 1) / size); }
    private static String timestamp(Timestamp value) { return value == null ? null : value.toInstant().toString(); }

    private BigDecimal debt(long invoiceId, BigDecimal total) {
        BigDecimal paid = jdbc.queryForObject("SELECT COALESCE((SELECT SUM(applied_amount) FROM payment_receipts WHERE invoice_id=? AND status='POSTED'),0) + COALESCE((SELECT SUM(amount) FROM payment_credit_allocations WHERE invoice_id=? AND status='ACTIVE'),0)", BigDecimal.class, invoiceId, invoiceId);
        return total.subtract(paid);
    }
    private void lockHousehold(long householdId) {
        jdbc.update("INSERT INTO payment_household_locks (household_id) VALUES (?) ON DUPLICATE KEY UPDATE household_id=payment_household_locks.household_id", householdId);
        jdbc.queryForObject("SELECT household_id FROM payment_household_locks WHERE household_id=? FOR UPDATE", Long.class, householdId);
    }
    private ReceiptRef receiptRef(long id) {
        var rows = jdbc.query("SELECT invoice_id, household_id, status FROM payment_receipts WHERE id=?", (rs, n) -> new ReceiptRef(rs.getLong(1), rs.getLong(2), rs.getString(3)), id);
        if (rows.isEmpty()) throw new NotFoundException("RECEIPT_NOT_FOUND"); return rows.getFirst();
    }
    private AllocationRef allocationRef(long id) {
        var rows = jdbc.query("SELECT invoice_id, household_id, status FROM payment_credit_allocations WHERE id=?", (rs, n) -> new AllocationRef(rs.getLong(1), rs.getLong(2), rs.getString(3)), id);
        if (rows.isEmpty()) throw new NotFoundException("ALLOCATION_NOT_FOUND"); return rows.getFirst();
    }
    private Receipt receiptRow(long id) {
        return jdbc.queryForObject("SELECT r.*, r.late_reason IS NOT NULL AS has_late_reason, r.void_reason IS NOT NULL AS has_void_reason FROM payment_receipts r WHERE id=?", (rs, n) -> new Receipt(rs.getLong("id"), rs.getLong("invoice_id"), rs.getDate("paid_on").toLocalDate().toString(), rs.getBigDecimal("amount").toPlainString(), rs.getBigDecimal("applied_amount").toPlainString(), rs.getBigDecimal("credit_amount").toPlainString(), rs.getBigDecimal("debt_after").toPlainString(), rs.getString("status"), rs.getTimestamp("recorded_at").toInstant().toString(), rs.getBoolean("has_late_reason"), rs.getBoolean("has_void_reason")), id);
    }
    private Allocation allocationRow(long id) {
        return jdbc.queryForObject("SELECT a.*, a.reverse_reason IS NOT NULL AS has_reverse_reason FROM payment_credit_allocations a WHERE id=?", (rs, n) -> new Allocation(rs.getLong("id"), rs.getLong("invoice_id"), rs.getLong("household_id"), rs.getBigDecimal("amount").toPlainString(), rs.getBigDecimal("debt_after").toPlainString(), rs.getString("status"), rs.getBoolean("has_reverse_reason")), id);
    }
    private long insert(String sql, Object... params) {
        var holder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) statement.setObject(i + 1, params[i]);
            return statement;
        }, holder);
        return holder.getKey().longValue();
    }
    private <T> T command(String key, List<?> payload, Class<T> type, Supplier<T> action) {
        String canonicalKey;
        try { canonicalKey = UUID.fromString(key).toString(); if (!canonicalKey.equalsIgnoreCase(key)) throw new IllegalArgumentException(); }
        catch (RuntimeException ex) { throw new BadRequestException("INVALID_IDEMPOTENCY_KEY"); }
        String hash;
        try { hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(mapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
        jdbc.update("INSERT INTO payment_commands (idempotency_key, fingerprint) VALUES (?, ?) ON DUPLICATE KEY UPDATE idempotency_key=payment_commands.idempotency_key", canonicalKey, hash);
        var stored = jdbc.queryForObject("SELECT fingerprint, result_json FROM payment_commands WHERE idempotency_key=? FOR UPDATE", (rs, n) -> new Command(rs.getString(1), rs.getString(2)), canonicalKey);
        if (!stored.fingerprint().equals(hash)) throw new ConflictException("IDEMPOTENCY_CONFLICT");
        if (stored.result() != null) return mapper.readValue(stored.result(), type);
        T result = action.get();
        jdbc.update("UPDATE payment_commands SET result_json=? WHERE idempotency_key=?", mapper.writeValueAsString(result), canonicalKey);
        return result;
    }
    private static BigDecimal money(BigDecimal amount) {
        try {
            BigDecimal result = ReceiptAmounts.integer(amount);
            if (result.signum() <= 0) throw new IllegalArgumentException();
            return result;
        } catch (RuntimeException ex) { throw new BadRequestException("INVALID_AMOUNT"); }
    }
    private static String optionalReason(String reason) {
        if (reason == null || reason.isBlank()) return null;
        if (reason.length() > 255) throw new BadRequestException("VALIDATION");
        return reason.trim();
    }
    private static String requiredReason(String reason) {
        String result = optionalReason(reason);
        if (result == null) throw new BadRequestException("REASON_REQUIRED");
        return result;
    }
}
