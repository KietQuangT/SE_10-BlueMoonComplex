package vn.edu.se10.bluemoon.reporting.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportData;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportRow;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportTotals;
import vn.edu.se10.bluemoon.reporting.application.ReportMetadata;

@Repository
public class FeeCollectionReportReadStore {
    private final NamedParameterJdbcTemplate jdbc;

    public FeeCollectionReportReadStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public FeeCollectionReportData load(String period, Instant exportedAt) {
        var params = new MapSqlParameterSource("period", period);

        // 1. Count
        String countSql = """
            SELECT COUNT(*)
            FROM invoices i
            JOIN billing_periods p ON p.id = i.period_id
            WHERE i.status = 'ISSUED' AND p.status = 'ISSUED' AND p.period = :period
            """;
        Long count = jdbc.queryForObject(countSql, params, Long.class);
        long invoiceCount = count != null ? count : 0L;
        if (invoiceCount > 50000) {
            throw new BadRequestException("REPORT_TOO_LARGE");
        }

        // 2. Query rows
        String rowsSql = """
            SELECT i.id, i.household_code, i.apartment_code, i.due_date, i.total,
                   COALESCE(r.paid, 0) + COALESCE(a.paid, 0) AS settled
            FROM invoices i
            JOIN billing_periods p ON p.id = i.period_id
            LEFT JOIN (
                SELECT invoice_id, SUM(applied_amount) AS paid
                FROM payment_receipts
                WHERE status = 'POSTED'
                GROUP BY invoice_id
            ) r ON r.invoice_id = i.id
            LEFT JOIN (
                SELECT invoice_id, SUM(amount) AS paid
                FROM payment_credit_allocations
                WHERE status = 'ACTIVE'
                GROUP BY invoice_id
            ) a ON a.invoice_id = i.id
            WHERE i.status = 'ISSUED' AND p.status = 'ISSUED' AND p.period = :period
            ORDER BY i.id ASC
            """;

        List<FeeCollectionReportRow> rows = jdbc.query(rowsSql, params, (rs, rowNum) -> {
            long invoiceId = rs.getLong("id");
            String householdCode = rs.getString("household_code");
            String apartmentCode = rs.getString("apartment_code");
            var dueDate = rs.getDate("due_date").toLocalDate();
            BigDecimal total = rs.getBigDecimal("total");
            BigDecimal settled = rs.getBigDecimal("settled");
            if (settled == null) settled = BigDecimal.ZERO;
            if (total == null) total = BigDecimal.ZERO;

            BigDecimal remaining = total.subtract(settled);

            // Invariant check: settled cannot be negative or exceed total
            if (settled.signum() < 0 || remaining.signum() < 0 || settled.compareTo(total) > 0) {
                throw new IllegalStateException("Bất biến số liệu thanh toán bị vi phạm tại hóa đơn " + invoiceId);
            }

            String paymentStatus = remaining.signum() == 0 ? "PAID" : settled.signum() == 0 ? "UNPAID" : "PARTIALLY_PAID";

            return new FeeCollectionReportRow(
                invoiceId,
                householdCode,
                apartmentCode,
                dueDate,
                total,
                settled,
                remaining,
                paymentStatus
            );
        });

        // 3. Totals
        BigDecimal totalDue = BigDecimal.ZERO;
        BigDecimal totalSettled = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;

        for (FeeCollectionReportRow row : rows) {
            totalDue = totalDue.add(row.total());
            totalSettled = totalSettled.add(row.settled());
            totalRemaining = totalRemaining.add(row.remaining());
        }

        BigDecimal ratioPercent = null;
        if (totalDue.signum() > 0) {
            ratioPercent = totalSettled.multiply(BigDecimal.valueOf(100)).divide(totalDue, 2, RoundingMode.HALF_UP);
        }

        var totals = new FeeCollectionReportTotals(invoiceCount, totalDue, totalSettled, totalRemaining, ratioPercent);
        var metadata = new ReportMetadata("FEE_COLLECTION_V1", "1.0", exportedAt, "Thu theo kỳ hóa đơn");
        return new FeeCollectionReportData(metadata, period, totals, rows);
    }
}
