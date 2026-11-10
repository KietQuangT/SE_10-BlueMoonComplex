package vn.edu.se10.bluemoon.reporting.persistence;

import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.reporting.application.ReportMetadata;
import vn.edu.se10.bluemoon.reporting.application.ResidencyReportData;
import vn.edu.se10.bluemoon.reporting.application.ResidencyReportRow;
import vn.edu.se10.bluemoon.reporting.application.ResidencyReportTotals;

@Repository
public class ResidencyReportReadStore {
    private final NamedParameterJdbcTemplate jdbc;

    public ResidencyReportReadStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ResidencyReportData load(Instant exportedAt) {
        // 1. Count
        String countSql = """
            SELECT COUNT(*)
            FROM residents r
            JOIN households h ON h.id = r.household_id
            WHERE h.status = 'ACTIVE' AND r.residency_status IN ('RESIDING', 'TEMPORARILY_ABSENT')
            """;
        Long count = jdbc.queryForObject(countSql, EmptySqlParameterSource.INSTANCE, Long.class);
        long residentCount = count != null ? count : 0L;
        if (residentCount > 50000) {
            throw new BadRequestException("REPORT_TOO_LARGE");
        }

        // 2. Query rows
        String rowsSql = """
            SELECT h.household_code,
                   h.apartment_code,
                   r.full_name,
                   r.residency_status
            FROM residents r
            JOIN households h ON h.id = r.household_id
            WHERE h.status = 'ACTIVE' AND r.residency_status IN ('RESIDING', 'TEMPORARILY_ABSENT')
            ORDER BY h.apartment_code ASC, h.household_code ASC, r.id ASC
            """;

        List<ResidencyReportRow> rows = jdbc.query(rowsSql, EmptySqlParameterSource.INSTANCE, (rs, rowNum) -> {
            String householdCode = rs.getString("household_code");
            String apartmentCode = rs.getString("apartment_code");
            String fullName = rs.getString("full_name");
            String residencyStatus = rs.getString("residency_status");

            return new ResidencyReportRow(
                householdCode,
                apartmentCode,
                fullName,
                residencyStatus
            );
        });

        long residingCount = 0;
        long temporarilyAbsentCount = 0;
        for (ResidencyReportRow row : rows) {
            if ("RESIDING".equals(row.residencyStatus())) {
                residingCount++;
            } else if ("TEMPORARILY_ABSENT".equals(row.residencyStatus())) {
                temporarilyAbsentCount++;
            }
        }

        var totals = new ResidencyReportTotals(residentCount, residingCount, temporarilyAbsentCount);
        var metadata = new ReportMetadata("CURRENT_RESIDENCY_V1", "1.0", exportedAt, "Danh sách cư trú hiện tại");
        return new ResidencyReportData(metadata, totals, rows);
    }
}
