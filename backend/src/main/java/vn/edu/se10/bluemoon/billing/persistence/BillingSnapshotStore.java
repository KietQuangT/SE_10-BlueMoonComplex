package vn.edu.se10.bluemoon.billing.persistence;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@Repository
public class BillingSnapshotStore {
    private final JdbcTemplate jdbc;

    public BillingSnapshotStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Rate(long typeId, String code, String name, CalculationMethod method,
                       long rateId, BigDecimal unitPrice) { }
    public record Line(long householdId, String householdCode, String apartmentCode, BigDecimal areaM2,
                       long typeId, String code, String name, CalculationMethod method, BigDecimal quantity,
                       BigDecimal unitPrice, long rateId, BigDecimal amount) { }
    public record HouseholdInput(long id, BigDecimal areaM2) { }
    public record RateInput(String code, long rateId, BigDecimal unitPrice, CalculationMethod method) { }
    public record SnapshotInputs(List<HouseholdInput> households, List<RateInput> rates) { }
    public record Submission(long id, long count, BigDecimal total) { }

    /** Caller locks the ACTIVE household range before calling this method, in REPEATABLE_READ. */
    public List<Rate> lockRates(LocalDate month) { return rates(month, true); }
    public List<Rate> readRates(LocalDate month) { return rates(month, false); }

    private List<Rate> rates(LocalDate month, boolean lock) {
        String sql = """
                SELECT t.id, t.code, t.name, t.calculation_method, r.id AS rate_id, r.unit_price
                FROM fee_rates r FORCE INDEX (uq_fee_rates_type_month)
                JOIN fee_types t ON t.id = r.fee_type_id
                WHERE t.code IN ('MANAGEMENT_FEE', 'WASTE_FEE') AND r.effective_from <= ?
                ORDER BY r.fee_type_id, r.effective_from DESC
                """ + (lock ? " FOR UPDATE" : "");
        List<Rate> rows = jdbc.query(sql, (rs, row) -> new Rate(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                CalculationMethod.valueOf(rs.getString("calculation_method")), rs.getLong("rate_id"),
                rs.getBigDecimal("unit_price")), month);
        Map<String, Rate> latest = new LinkedHashMap<>();
        rows.forEach(rate -> latest.putIfAbsent(rate.code(), rate));
        if (latest.size() != 2) throw new ConflictException("FEE_RATE_UNAVAILABLE");
        return List.copyOf(latest.values());
    }

    public Submission save(long periodId, long revision, long actor, Instant now, List<Line> lines,
                           long householdCount, BigDecimal total) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO billing_submissions (period_id, revision, submitted_by, submitted_at, household_count, total)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, periodId);
            statement.setLong(2, revision);
            statement.setLong(3, actor);
            statement.setTimestamp(4, Timestamp.from(now));
            statement.setLong(5, householdCount);
            statement.setBigDecimal(6, total);
            return statement;
        }, keys);
        long submissionId = keys.getKey().longValue();
        List<Object[]> batch = lines.stream().map(line -> new Object[]{submissionId, line.householdId(),
                line.householdCode(), line.apartmentCode(), line.areaM2(), line.typeId(), line.code(), line.name(),
                line.method().name(), line.quantity(), line.unitPrice(), line.rateId(), line.amount()}).toList();
        jdbc.batchUpdate("""
                INSERT INTO billing_submission_items (submission_id, household_id, household_code, apartment_code,
                area_m2, fee_type_id, fee_type_code, fee_type_name, calculation_method, quantity, unit_price, fee_rate_id, amount)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, batch);
        return new Submission(submissionId, householdCount, total);
    }

    public long submissionId(long periodId, long revision) {
        List<Long> ids = jdbc.queryForList("SELECT id FROM billing_submissions WHERE period_id=? AND revision=?", Long.class, periodId, revision);
        if (ids.size() != 1) throw new ConflictException("INVALID_STATE");
        return ids.getFirst();
    }

    public SnapshotInputs inputs(long submissionId) {
        List<HouseholdInput> households = jdbc.query("""
                SELECT household_id, area_m2 FROM billing_submission_items
                WHERE submission_id=? AND fee_type_code='MANAGEMENT_FEE' ORDER BY household_id
                """, (rs, row) -> new HouseholdInput(rs.getLong("household_id"), rs.getBigDecimal("area_m2")), submissionId);
        List<RateInput> rates = jdbc.query("""
                SELECT DISTINCT fee_type_code, fee_rate_id, unit_price, calculation_method
                FROM billing_submission_items WHERE submission_id=? ORDER BY fee_type_code
                """, (rs, row) -> new RateInput(rs.getString("fee_type_code"), rs.getLong("fee_rate_id"),
                rs.getBigDecimal("unit_price"), CalculationMethod.valueOf(rs.getString("calculation_method"))), submissionId);
        return new SnapshotInputs(households, rates);
    }

    public void issue(long periodId, long submissionId, LocalDate dueDate, Instant now) {
        jdbc.update("""
                INSERT INTO invoices (period_id, household_id, household_code, apartment_code, due_date, total, status, issued_at)
                SELECT ?, household_id, household_code, apartment_code, ?, SUM(amount), 'ISSUED', ?
                FROM billing_submission_items WHERE submission_id=?
                GROUP BY household_id, household_code, apartment_code
                """, periodId, dueDate, Timestamp.from(now), submissionId);
        jdbc.update("""
                INSERT INTO invoice_lines (invoice_id, fee_type_id, fee_type_code, fee_type_name, calculation_method,
                                           area_m2, quantity, unit_price, fee_rate_id, amount)
                SELECT i.id, s.fee_type_id, s.fee_type_code, s.fee_type_name, s.calculation_method,
                       s.area_m2, s.quantity, s.unit_price, s.fee_rate_id, s.amount
                FROM billing_submission_items s JOIN invoices i ON i.period_id=? AND i.household_id=s.household_id
                WHERE s.submission_id=?
                """, periodId, submissionId);
    }
    public void cancelInvoices(long periodId, Instant now) {
        jdbc.update("UPDATE invoices SET status='CANCELLED', cancelled_at=? WHERE period_id=? AND status='ISSUED'",
                Timestamp.from(now), periodId);
    }

}
