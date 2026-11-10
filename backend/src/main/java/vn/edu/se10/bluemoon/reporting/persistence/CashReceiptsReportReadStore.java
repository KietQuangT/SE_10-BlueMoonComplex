package vn.edu.se10.bluemoon.reporting.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.reporting.application.CashReceiptReportData;
import vn.edu.se10.bluemoon.reporting.application.CashReceiptReportRow;
import vn.edu.se10.bluemoon.reporting.application.CashReceiptReportTotals;
import vn.edu.se10.bluemoon.reporting.application.ReportMetadata;

@Repository
public class CashReceiptsReportReadStore {
    private final NamedParameterJdbcTemplate jdbc;

    public CashReceiptsReportReadStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public CashReceiptReportData load(LocalDate from, LocalDate to, Instant exportedAt) {
        var params = new MapSqlParameterSource()
            .addValue("from", from)
            .addValue("to", to);

        // 1. Count
        String countSql = """
            SELECT COUNT(*)
            FROM payment_receipts r
            WHERE r.status = 'POSTED' AND r.paid_on >= :from AND r.paid_on <= :to
            """;
        Long count = jdbc.queryForObject(countSql, params, Long.class);
        long receiptCount = count != null ? count : 0L;
        if (receiptCount > 50000) {
            throw new BadRequestException("REPORT_TOO_LARGE");
        }

        // 2. Query rows
        String rowsSql = """
            SELECT r.id AS receipt_id,
                   p.period AS invoice_period,
                   i.household_code,
                   i.apartment_code,
                   r.paid_on,
                   r.amount,
                   r.applied_amount,
                   r.credit_amount
            FROM payment_receipts r
            JOIN invoices i ON i.id = r.invoice_id
            JOIN billing_periods p ON p.id = i.period_id
            WHERE r.status = 'POSTED' AND r.paid_on >= :from AND r.paid_on <= :to
            ORDER BY r.paid_on ASC, r.id ASC
            """;

        List<CashReceiptReportRow> rows = jdbc.query(rowsSql, params, (rs, rowNum) -> {
            long receiptId = rs.getLong("receipt_id");
            String invoicePeriod = rs.getString("invoice_period");
            String householdCode = rs.getString("household_code");
            String apartmentCode = rs.getString("apartment_code");
            var paidOn = rs.getDate("paid_on").toLocalDate();
            BigDecimal amount = rs.getBigDecimal("amount");
            BigDecimal appliedAmount = rs.getBigDecimal("applied_amount");
            BigDecimal creditAmount = rs.getBigDecimal("credit_amount");
            if (amount == null) amount = BigDecimal.ZERO;
            if (appliedAmount == null) appliedAmount = BigDecimal.ZERO;
            if (creditAmount == null) creditAmount = BigDecimal.ZERO;

            // Invariant check: amount = applied_amount + credit_amount
            if (amount.compareTo(appliedAmount.add(creditAmount)) != 0) {
                throw new IllegalStateException("Bất biến số tiền receipt bị vi phạm tại receipt " + receiptId);
            }

            return new CashReceiptReportRow(
                receiptId,
                invoicePeriod,
                householdCode,
                apartmentCode,
                paidOn,
                amount,
                appliedAmount,
                creditAmount
            );
        });

        // 3. Totals
        BigDecimal cashReceived = BigDecimal.ZERO;
        for (CashReceiptReportRow row : rows) {
            cashReceived = cashReceived.add(row.amount());
        }

        var totals = new CashReceiptReportTotals(receiptCount, cashReceived);
        var metadata = new ReportMetadata("CASH_RECEIPTS_V1", "1.0", exportedAt, "Tiền mặt theo ngày thực thu");
        return new CashReceiptReportData(metadata, from, to, totals, rows);
    }
}
