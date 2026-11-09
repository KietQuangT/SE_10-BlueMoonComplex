package vn.edu.se10.bluemoon.billing.persistence;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.edu.se10.bluemoon.billing.api.PageResponse;
import vn.edu.se10.bluemoon.billing.api.ResidentAllocation;
import vn.edu.se10.bluemoon.billing.api.ResidentBillingSummary;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceDetail;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceLine;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceSummary;
import vn.edu.se10.bluemoon.billing.api.ResidentReceipt;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

@Repository
public class ResidentInvoiceReadStore {
    private final NamedParameterJdbcTemplate jdbc;

    public ResidentInvoiceReadStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public PageResponse<ResidentInvoiceSummary> invoices(long householdId, String period, String paymentStatus, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("householdId", householdId);
        params.put("period", period);
        params.put("size", size);
        params.put("offset", (long) page * size);

        String statusClause = switch (paymentStatus) {
            case "OUTSTANDING" -> "d.invoice_status = 'ISSUED' AND d.remaining > 0";
            case "UNPAID" -> "d.invoice_status = 'ISSUED' AND d.settled = 0 AND d.total > 0";
            case "PARTIALLY_PAID" -> "d.invoice_status = 'ISSUED' AND d.settled > 0 AND d.settled < d.total";
            case "PAID" -> "d.invoice_status = 'ISSUED' AND d.remaining = 0";
            case "CANCELLED" -> "d.invoice_status = 'CANCELLED'";
            default -> "1=1";
        };

        String base = """
                FROM (
                    SELECT i.id, i.period_id, i.household_id, i.household_code, i.apartment_code,
                           i.due_date, i.total, i.status AS invoice_status, i.issued_at, i.cancelled_at,
                           p.period,
                           CASE WHEN i.status = 'CANCELLED' THEN 0 ELSE COALESCE(r.paid, 0) + COALESCE(a.paid, 0) END AS settled,
                           CASE WHEN i.status = 'CANCELLED' THEN 0 ELSE (i.total - (COALESCE(r.paid, 0) + COALESCE(a.paid, 0))) END AS remaining
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
                    WHERE i.household_id = :householdId
                      AND i.status IN ('ISSUED', 'CANCELLED')
                      AND (:period IS NULL OR p.period = :period)
                ) d WHERE """ + statusClause;

        Long totalCount = jdbc.queryForObject("SELECT COUNT(*) " + base, params, Long.class);
        long total = totalCount != null ? totalCount : 0L;
        int totalPages = (int) Math.min(Integer.MAX_VALUE, (total + size - 1) / size);

        List<ResidentInvoiceSummary> rows = jdbc.query(
                "SELECT d.* " + base + " ORDER BY d.period DESC, d.id DESC LIMIT :size OFFSET :offset",
                params,
                (rs, rowNum) -> {
                    BigDecimal amount = rs.getBigDecimal("total");
                    BigDecimal settled = rs.getBigDecimal("settled");
                    BigDecimal remaining = rs.getBigDecimal("remaining");
                    String invoiceStatus = rs.getString("invoice_status");
                    String state;
                    if ("CANCELLED".equals(invoiceStatus)) {
                        state = "CANCELLED";
                    } else if (remaining.signum() == 0) {
                        state = "PAID";
                    } else if (settled.signum() == 0) {
                        state = "UNPAID";
                    } else {
                        state = "PARTIALLY_PAID";
                    }
                    return new ResidentInvoiceSummary(
                            rs.getLong("id"),
                            rs.getString("period"),
                            rs.getString("household_code"),
                            rs.getString("apartment_code"),
                            rs.getDate("due_date").toLocalDate().toString(),
                            instantString(rs.getTimestamp("issued_at")),
                            instantString(rs.getTimestamp("cancelled_at")),
                            invoiceStatus,
                            state,
                            amount.toPlainString(),
                            settled.toPlainString(),
                            remaining.toPlainString()
                    );
                }
        );
        return new PageResponse<>(rows, page, size, total, totalPages);
    }

    public ResidentInvoiceDetail invoiceDetail(long householdId, long invoiceId) {
        Map<String, Object> params = new HashMap<>();
        params.put("householdId", householdId);
        params.put("invoiceId", invoiceId);

        String sql = """
                SELECT i.id, i.period_id, i.household_id, i.household_code, i.apartment_code,
                       i.due_date, i.total, i.status AS invoice_status, i.issued_at, i.cancelled_at,
                       p.period,
                       CASE WHEN i.status = 'CANCELLED' THEN 0 ELSE COALESCE(r.paid, 0) + COALESCE(a.paid, 0) END AS settled,
                       CASE WHEN i.status = 'CANCELLED' THEN 0 ELSE (i.total - (COALESCE(r.paid, 0) + COALESCE(a.paid, 0))) END AS remaining
                FROM invoices i
                JOIN billing_periods p ON p.id = i.period_id
                LEFT JOIN (
                    SELECT invoice_id, SUM(applied_amount) AS paid
                    FROM payment_receipts
                    WHERE invoice_id = :invoiceId AND status = 'POSTED'
                    GROUP BY invoice_id
                ) r ON r.invoice_id = i.id
                LEFT JOIN (
                    SELECT invoice_id, SUM(amount) AS paid
                    FROM payment_credit_allocations
                    WHERE invoice_id = :invoiceId AND status = 'ACTIVE'
                    GROUP BY invoice_id
                ) a ON a.invoice_id = i.id
                WHERE i.id = :invoiceId
                  AND i.household_id = :householdId
                  AND i.status IN ('ISSUED', 'CANCELLED')
                """;

        List<ResidentInvoiceSummary> list = jdbc.query(sql, params, (rs, rowNum) -> {
            BigDecimal amount = rs.getBigDecimal("total");
            BigDecimal settled = rs.getBigDecimal("settled");
            BigDecimal remaining = rs.getBigDecimal("remaining");
            String invoiceStatus = rs.getString("invoice_status");
            String state;
            if ("CANCELLED".equals(invoiceStatus)) {
                state = "CANCELLED";
            } else if (remaining.signum() == 0) {
                state = "PAID";
            } else if (settled.signum() == 0) {
                state = "UNPAID";
            } else {
                state = "PARTIALLY_PAID";
            }
            return new ResidentInvoiceSummary(
                    rs.getLong("id"),
                    rs.getString("period"),
                    rs.getString("household_code"),
                    rs.getString("apartment_code"),
                    rs.getDate("due_date").toLocalDate().toString(),
                    instantString(rs.getTimestamp("issued_at")),
                    instantString(rs.getTimestamp("cancelled_at")),
                    invoiceStatus,
                    state,
                    amount.toPlainString(),
                    settled.toPlainString(),
                    remaining.toPlainString()
            );
        });

        if (list.isEmpty()) {
            throw new NotFoundException("INVOICE_NOT_FOUND");
        }
        ResidentInvoiceSummary summary = list.getFirst();

        String linesSql = """
                SELECT fee_type_code, fee_type_name, calculation_method,
                       quantity, area_m2, unit_price, amount
                FROM invoice_lines
                WHERE invoice_id = :invoiceId
                ORDER BY fee_type_id ASC
                """;

        List<ResidentInvoiceLine> lines = jdbc.query(linesSql, params, (rs, rowNum) -> {
            String method = rs.getString("calculation_method");
            String unit = "AREA".equals(method) ? "M2" : "HOUSEHOLD";
            BigDecimal area = rs.getBigDecimal("area_m2");
            String areaM2 = ("AREA".equals(method) && area != null) ? area.toPlainString() : null;
            return new ResidentInvoiceLine(
                    rs.getString("fee_type_code"),
                    rs.getString("fee_type_name"),
                    method,
                    rs.getBigDecimal("quantity").toPlainString(),
                    unit,
                    areaM2,
                    rs.getBigDecimal("unit_price").toPlainString(),
                    rs.getBigDecimal("amount").toPlainString()
            );
        });

        return new ResidentInvoiceDetail(
                summary.invoiceId(),
                summary.period(),
                summary.householdCode(),
                summary.apartmentCode(),
                summary.dueDate(),
                summary.issuedAt(),
                summary.cancelledAt(),
                summary.invoiceStatus(),
                summary.paymentStatus(),
                summary.total(),
                summary.settled(),
                summary.remaining(),
                lines
        );
    }

    public PageResponse<ResidentReceipt> receipts(long householdId, Long invoiceId, String status, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("householdId", householdId);
        params.put("invoiceId", invoiceId);
        params.put("status", "ALL".equals(status) ? null : status);
        params.put("size", size);
        params.put("offset", (long) page * size);

        if (invoiceId != null) {
            Long exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM invoices WHERE id = :invoiceId AND household_id = :householdId AND status IN ('ISSUED', 'CANCELLED')",
                    params, Long.class);
            if (exists == null || exists == 0) {
                throw new NotFoundException("INVOICE_NOT_FOUND");
            }
        }

        String base = """
                FROM payment_receipts r
                JOIN invoices i ON i.id = r.invoice_id
                JOIN billing_periods p ON p.id = i.period_id
                WHERE r.household_id = :householdId
                  AND (:invoiceId IS NULL OR r.invoice_id = :invoiceId)
                  AND (:status IS NULL OR r.status = :status)
                  AND i.status IN ('ISSUED', 'CANCELLED')
                """;

        Long totalCount = jdbc.queryForObject("SELECT COUNT(*) " + base, params, Long.class);
        long total = totalCount != null ? totalCount : 0L;
        int totalPages = (int) Math.min(Integer.MAX_VALUE, (total + size - 1) / size);

        List<ResidentReceipt> rows = jdbc.query(
                "SELECT r.id, r.invoice_id, p.period, r.paid_on, r.recorded_at, r.amount, " +
                "r.applied_amount, r.credit_amount, r.status, r.voided_at, " +
                "r.late_reason IS NOT NULL AS has_late_reason, " +
                "r.void_reason IS NOT NULL AS has_void_reason " +
                base + " ORDER BY r.id DESC LIMIT :size OFFSET :offset",
                params,
                (rs, rowNum) -> new ResidentReceipt(
                        rs.getLong("id"),
                        rs.getLong("invoice_id"),
                        rs.getString("period"),
                        rs.getDate("paid_on").toLocalDate().toString(),
                        instantString(rs.getTimestamp("recorded_at")),
                        rs.getBigDecimal("amount").toPlainString(),
                        rs.getBigDecimal("applied_amount").toPlainString(),
                        rs.getBigDecimal("credit_amount").toPlainString(),
                        rs.getString("status"),
                        instantString(rs.getTimestamp("voided_at")),
                        rs.getBoolean("has_late_reason"),
                        rs.getBoolean("has_void_reason")
                )
        );
        return new PageResponse<>(rows, page, size, total, totalPages);
    }

    public PageResponse<ResidentAllocation> creditAllocations(long householdId, Long invoiceId, String status, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("householdId", householdId);
        params.put("invoiceId", invoiceId);
        params.put("status", "ALL".equals(status) ? null : status);
        params.put("size", size);
        params.put("offset", (long) page * size);

        if (invoiceId != null) {
            Long exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM invoices WHERE id = :invoiceId AND household_id = :householdId AND status IN ('ISSUED', 'CANCELLED')",
                    params, Long.class);
            if (exists == null || exists == 0) {
                throw new NotFoundException("INVOICE_NOT_FOUND");
            }
        }

        String base = """
                FROM payment_credit_allocations a
                JOIN invoices i ON i.id = a.invoice_id
                JOIN billing_periods p ON p.id = i.period_id
                WHERE a.household_id = :householdId
                  AND (:invoiceId IS NULL OR a.invoice_id = :invoiceId)
                  AND (:status IS NULL OR a.status = :status)
                  AND i.status IN ('ISSUED', 'CANCELLED')
                """;

        Long totalCount = jdbc.queryForObject("SELECT COUNT(*) " + base, params, Long.class);
        long total = totalCount != null ? totalCount : 0L;
        int totalPages = (int) Math.min(Integer.MAX_VALUE, (total + size - 1) / size);

        List<ResidentAllocation> rows = jdbc.query(
                "SELECT a.id, a.invoice_id, p.period, a.amount, a.status, a.recorded_at, a.reversed_at, " +
                "a.reverse_reason IS NOT NULL AS has_reverse_reason " +
                base + " ORDER BY a.id DESC LIMIT :size OFFSET :offset",
                params,
                (rs, rowNum) -> new ResidentAllocation(
                        rs.getLong("id"),
                        rs.getLong("invoice_id"),
                        rs.getString("period"),
                        rs.getBigDecimal("amount").toPlainString(),
                        rs.getString("status"),
                        instantString(rs.getTimestamp("recorded_at")),
                        instantString(rs.getTimestamp("reversed_at")),
                        rs.getBoolean("has_reverse_reason")
                )
        );
        return new PageResponse<>(rows, page, size, total, totalPages);
    }

    public ResidentBillingSummary billingSummary(long householdId) {
        Map<String, Object> params = Map.of("householdId", householdId);

        String sql = """
                SELECT i.id, i.due_date, p.period,
                       i.total - (COALESCE(r.paid, 0) + COALESCE(a.paid, 0)) AS remaining
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
                WHERE i.household_id = :householdId
                  AND i.status = 'ISSUED'
                  AND (i.total - (COALESCE(r.paid, 0) + COALESCE(a.paid, 0))) > 0
                ORDER BY i.due_date ASC, p.period ASC, i.id DESC
                """;

        record DueRow(LocalDate dueDate, String period, BigDecimal remaining) {}
        List<DueRow> dueRows = jdbc.query(sql, params, (rs, rowNum) -> new DueRow(
                rs.getDate("due_date").toLocalDate(),
                rs.getString("period"),
                rs.getBigDecimal("remaining")
        ));

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        for (DueRow row : dueRows) {
            totalOutstanding = totalOutstanding.add(row.remaining());
        }
        long count = dueRows.size();
        String nearestDueDate = count > 0 ? dueRows.getFirst().dueDate().toString() : null;
        String nearestDuePeriod = count > 0 ? dueRows.getFirst().period() : null;

        String creditSql = """
                SELECT COALESCE(SUM(r.credit_amount - COALESCE((
                    SELECT SUM(s.amount)
                    FROM payment_credit_allocation_sources s
                    JOIN payment_credit_allocations a ON a.id = s.allocation_id
                    WHERE s.receipt_id = r.id AND a.status = 'ACTIVE'
                ), 0)), 0)
                FROM payment_receipts r
                WHERE r.household_id = :householdId AND r.status = 'POSTED'
                """;
        BigDecimal credit = jdbc.queryForObject(creditSql, params, BigDecimal.class);
        String creditBalance = (credit != null ? credit : BigDecimal.ZERO).toPlainString();

        return new ResidentBillingSummary(
                totalOutstanding.toPlainString(),
                count,
                nearestDueDate,
                nearestDuePeriod,
                creditBalance
        );
    }

    private static String instantString(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant().toString() : null;
    }
}
