package vn.edu.se10.bluemoon.billing.persistence;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

@Repository
public class BillingReadStore {
    private final JdbcTemplate jdbc;

    public BillingReadStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Summary(long count, BigDecimal total) { }
    public record SubmissionInfo(long id, long revision, long submittedBy, Instant submittedAt, long count, BigDecimal total) { }
    public record Item(long householdId, String householdCode, String apartmentCode, BigDecimal areaM2,
                       String code, String name, CalculationMethod method, BigDecimal quantity,
                       BigDecimal unitPrice, long rateId, BigDecimal amount) { }
    public record InvoiceRow(long id, long periodId, long householdId, String householdCode, String apartmentCode,
                             String period, LocalDate dueDate, String status, Instant issuedAt, Instant cancelledAt, BigDecimal total) { }
    public record InvoiceLine(long invoiceId, String code, String name, CalculationMethod method, BigDecimal quantity,
                              BigDecimal areaM2, BigDecimal unitPrice, long rateId, BigDecimal amount) { }

    public Map<Long, Summary> summaries(List<Long> ids) {
        Map<Long, Summary> result = new HashMap<>();
        if (ids.isEmpty()) return result;
        String params = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbc.query("SELECT p.id, s.household_count, s.total FROM billing_periods p JOIN billing_submissions s ON s.period_id=p.id AND s.revision=p.revision WHERE p.id IN (" + params + ")",
                rs -> { result.put(rs.getLong("id"), new Summary(rs.getLong("household_count"), rs.getBigDecimal("total"))); }, ids.toArray());
        return result;
    }

    public SubmissionInfo submission(long periodId, long revision) {
        var result = jdbc.query("SELECT id, revision, submitted_by, submitted_at, household_count, total FROM billing_submissions WHERE period_id=? AND revision=?",
                (rs, row) -> new SubmissionInfo(rs.getLong("id"), rs.getLong("revision"), rs.getLong("submitted_by"),
                        instant(rs.getTimestamp("submitted_at")), rs.getLong("household_count"), rs.getBigDecimal("total")), periodId, revision);
        if (result.isEmpty()) throw new NotFoundException("BILLING_SUBMISSION_NOT_FOUND");
        return result.getFirst();
    }

    public List<Item> items(long submissionId, int page, int size) {
        List<Long> householdIds = jdbc.queryForList("""
                SELECT DISTINCT household_id FROM billing_submission_items
                WHERE submission_id=? ORDER BY household_id LIMIT ? OFFSET ?
                """, Long.class, submissionId, size, (long) page * size);
        if (householdIds.isEmpty()) return List.of();
        String params = String.join(",", Collections.nCopies(householdIds.size(), "?"));
        List<Object> arguments = new ArrayList<>();
        arguments.add(submissionId);
        arguments.addAll(householdIds);
        return jdbc.query("""
                SELECT household_id, household_code, apartment_code, area_m2, fee_type_code, fee_type_name,
                       calculation_method, quantity, unit_price, fee_rate_id, amount
                FROM billing_submission_items WHERE submission_id=? AND household_id IN (
                """ + params + ") ORDER BY household_id, fee_type_id", (rs, row) -> new Item(
                rs.getLong("household_id"), rs.getString("household_code"), rs.getString("apartment_code"),
                rs.getBigDecimal("area_m2"), rs.getString("fee_type_code"), rs.getString("fee_type_name"),
                CalculationMethod.valueOf(rs.getString("calculation_method")), rs.getBigDecimal("quantity"),
                rs.getBigDecimal("unit_price"), rs.getLong("fee_rate_id"), rs.getBigDecimal("amount")), arguments.toArray());
    }

    public long invoiceCount(long periodId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM invoices WHERE period_id=?", Long.class, periodId);
    }

    public List<InvoiceRow> invoices(long periodId, int size, long offset) {
        return jdbc.query("""
                SELECT i.*, p.period FROM invoices i JOIN billing_periods p ON p.id=i.period_id
                WHERE i.period_id=? ORDER BY i.household_id LIMIT ? OFFSET ?
                """, (rs, row) -> invoice(rs), periodId, size, offset);
    }

    public InvoiceRow invoice(long id) {
        var rows = jdbc.query("SELECT i.*, p.period FROM invoices i JOIN billing_periods p ON p.id=i.period_id WHERE i.id=?",
                (rs, row) -> invoice(rs), id);
        if (rows.isEmpty()) throw new NotFoundException("INVOICE_NOT_FOUND");
        return rows.getFirst();
    }

    public List<InvoiceLine> invoiceLines(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String params = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT * FROM invoice_lines WHERE invoice_id IN (" + params + ") ORDER BY invoice_id, fee_type_id",
                (rs, row) -> new InvoiceLine(rs.getLong("invoice_id"), rs.getString("fee_type_code"), rs.getString("fee_type_name"),
                        CalculationMethod.valueOf(rs.getString("calculation_method")), rs.getBigDecimal("quantity"), rs.getBigDecimal("area_m2"),
                        rs.getBigDecimal("unit_price"), rs.getLong("fee_rate_id"), rs.getBigDecimal("amount")), ids.toArray());
    }

    private static InvoiceRow invoice(ResultSet rs) throws SQLException {
        return new InvoiceRow(rs.getLong("id"), rs.getLong("period_id"), rs.getLong("household_id"), rs.getString("household_code"),
                rs.getString("apartment_code"), rs.getString("period"), rs.getDate("due_date").toLocalDate(), rs.getString("status"),
                instant(rs.getTimestamp("issued_at")), instant(rs.getTimestamp("cancelled_at")), rs.getBigDecimal("total"));
    }

    private static Instant instant(Timestamp value) { return value == null ? null : value.toInstant(); }
}
