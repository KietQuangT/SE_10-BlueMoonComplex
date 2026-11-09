package vn.edu.se10.bluemoon.billing.application;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.error.BadRequestException;

/** Read model exposed to payment without exposing billing persistence. */
@Service
public class PaymentInvoiceQuery {
    private final NamedParameterJdbcTemplate jdbc;
    public PaymentInvoiceQuery(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Invoice(long invoiceId, long periodId, long householdId, String householdCode, String apartmentCode,
                          String period, String dueDate, String total, String settled, String remaining, String paymentStatus) { }
    public record Page(List<Invoice> content, int page, int size, long totalElements, int totalPages) { }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public Page search(String q, String period, String paymentStatus, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new BadRequestException("VALIDATION");
        String status = paymentStatus == null || paymentStatus.isBlank() ? "OUTSTANDING" : paymentStatus;
        if (!List.of("OUTSTANDING", "UNPAID", "PARTIALLY_PAID", "PAID", "ALL").contains(status)) throw new BadRequestException("INVALID_FILTER");
        if (period != null && !period.isBlank()) {
            try { if (!period.matches("[0-9]{4}-[0-9]{2}") || YearMonth.parse(period).getYear() < 1000) throw new DateTimeParseException("invalid", period, 0); }
            catch (DateTimeParseException ex) { throw new BadRequestException("INVALID_FILTER"); }
        } else period = null;
        String search = q == null || q.isBlank() ? null : q.trim();
        if (search != null && search.length() > 100) throw new BadRequestException("INVALID_FILTER");
        Map<String, Object> params = new HashMap<>();
        params.put("period", period);
        params.put("search", search == null ? null : "%" + search.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%");
        params.put("size", size); params.put("offset", (long) page * size);
        String base = """
                FROM (
                    SELECT i.id, i.period_id, i.household_id, i.household_code, i.apartment_code, i.due_date,
                           i.total, p.period,
                           COALESCE(r.paid, 0) + COALESCE(a.paid, 0) AS settled
                    FROM invoices i JOIN billing_periods p ON p.id=i.period_id
                    LEFT JOIN (SELECT invoice_id, SUM(applied_amount) paid FROM payment_receipts WHERE status='POSTED' GROUP BY invoice_id) r ON r.invoice_id=i.id
                    LEFT JOIN (SELECT invoice_id, SUM(amount) paid FROM payment_credit_allocations WHERE status='ACTIVE' GROUP BY invoice_id) a ON a.invoice_id=i.id
                    WHERE i.status='ISSUED' AND p.status='ISSUED'
                      AND (:period IS NULL OR p.period=:period)
                      AND (:search IS NULL OR i.household_code LIKE :search ESCAPE '!' OR i.apartment_code LIKE :search ESCAPE '!')
                ) d WHERE
                """ + switch (status) {
                    case "OUTSTANDING" -> "d.settled < d.total";
                    case "UNPAID" -> "d.settled = 0 AND d.total > 0";
                    case "PARTIALLY_PAID" -> "d.settled > 0 AND d.settled < d.total";
                    case "PAID" -> "d.settled = d.total";
                    default -> "1=1";
                };
        long total = jdbc.queryForObject("SELECT COUNT(*) " + base, params, Long.class);
        List<Invoice> rows = jdbc.query("SELECT d.* " + base + " ORDER BY d.period DESC, d.id DESC LIMIT :size OFFSET :offset", params, (rs, row) -> {
            BigDecimal amount = rs.getBigDecimal("total"), settled = rs.getBigDecimal("settled");
            BigDecimal remaining = amount.subtract(settled);
            String state = remaining.signum() == 0 ? "PAID" : settled.signum() == 0 ? "UNPAID" : "PARTIALLY_PAID";
            return new Invoice(rs.getLong("id"), rs.getLong("period_id"), rs.getLong("household_id"), rs.getString("household_code"), rs.getString("apartment_code"), rs.getString("period"), rs.getDate("due_date").toLocalDate().toString(), amount.toPlainString(), settled.toPlainString(), remaining.toPlainString(), state);
        });
        return new Page(rows, page, size, total, (int) Math.min(Integer.MAX_VALUE, (total + size - 1) / size));
    }
}
