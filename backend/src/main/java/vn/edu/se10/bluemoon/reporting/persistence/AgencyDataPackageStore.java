package vn.edu.se10.bluemoon.reporting.persistence;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import vn.edu.se10.bluemoon.reporting.application.AgencyPackageRow;

@Repository
public class AgencyDataPackageStore {
    private final NamedParameterJdbcTemplate jdbc;

    public AgencyDataPackageStore(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AgencyPackageRecord> ROW_MAPPER = (rs, rowNum) -> {
        Timestamp cat = rs.getTimestamp("created_at");
        Timestamp aat = rs.getTimestamp("approved_at");
        Timestamp rat = rs.getTimestamp("rejected_at");
        Timestamp vat = rs.getTimestamp("revoked_at");
        Timestamp hat = rs.getTimestamp("handed_off_at");
        Timestamp dut = rs.getTimestamp("download_until");
        Timestamp cda = rs.getTimestamp("content_delete_after");
        Timestamp cdt = rs.getTimestamp("content_deleted_at");

        return new AgencyPackageRecord(
            rs.getLong("id"),
            rs.getString("status"),
            rs.getLong("version"),
            rs.getString("template_id"),
            rs.getString("template_version"),
            rs.getString("recipient_agency_code"),
            rs.getString("request_reference"),
            rs.getString("licence_reference"),
            rs.getLong("created_by"),
            cat != null ? cat.toInstant() : null,
            rs.getObject("approved_by") != null ? rs.getLong("approved_by") : null,
            aat != null ? aat.toInstant() : null,
            rs.getObject("rejected_by") != null ? rs.getLong("rejected_by") : null,
            rat != null ? rat.toInstant() : null,
            rs.getObject("revoked_by") != null ? rs.getLong("revoked_by") : null,
            vat != null ? vat.toInstant() : null,
            rs.getObject("handed_off_by") != null ? rs.getLong("handed_off_by") : null,
            hat != null ? hat.toInstant() : null,
            dut != null ? dut.toInstant() : null,
            cda != null ? cda.toInstant() : null,
            cdt != null ? cdt.toInstant() : null,
            rs.getString("content_sha256"),
            rs.getLong("household_count"),
            rs.getLong("resident_count"),
            rs.getLong("residing_count"),
            rs.getLong("temporarily_absent_count")
        );
    };

    public List<Long> validateActiveHouseholdIds(List<Long> householdIds) {
        if (householdIds == null || householdIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = "SELECT id FROM households WHERE id IN (:householdIds) AND status = 'ACTIVE'";
        return jdbc.queryForList(sql, Map.of("householdIds", householdIds), Long.class);
    }

    public List<AgencyPackageRow> fetchSnapshotResidents(List<Long> householdIds) {
        if (householdIds == null || householdIds.isEmpty()) {
            return Collections.emptyList();
        }
        String sql = """
            SELECT h.household_code,
                   h.apartment_code,
                   r.full_name,
                   r.residency_status
            FROM residents r
            JOIN households h ON h.id = r.household_id
            WHERE h.id IN (:householdIds)
              AND h.status = 'ACTIVE'
              AND r.residency_status IN ('RESIDING', 'TEMPORARILY_ABSENT')
            ORDER BY h.apartment_code ASC, h.household_code ASC, r.id ASC
            """;
        return jdbc.query(sql, Map.of("householdIds", householdIds), (rs, rowNum) -> new AgencyPackageRow(
            rs.getString("household_code"),
            rs.getString("apartment_code"),
            rs.getString("full_name"),
            rs.getString("residency_status")
        ));
    }

    public long insertPackage(AgencyPackageRecord pkg) {
        String sql = """
            INSERT INTO agency_data_packages (
                status, version, template_id, template_version, recipient_agency_code,
                request_reference, licence_reference, created_by, created_at,
                content_delete_after, content_sha256,
                household_count, resident_count, residing_count, temporarily_absent_count
            ) VALUES (
                :status, :version, :templateId, :templateVersion, :recipientAgencyCode,
                :requestReference, :licenceReference, :createdBy, :createdAt,
                :contentDeleteAfter, :contentSha256,
                :householdCount, :residentCount, :residingCount, :temporarilyAbsentCount
            )
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("status", pkg.status())
            .addValue("version", pkg.version())
            .addValue("templateId", pkg.templateId())
            .addValue("templateVersion", pkg.templateVersion())
            .addValue("recipientAgencyCode", pkg.recipientAgencyCode())
            .addValue("requestReference", pkg.requestReference())
            .addValue("licenceReference", pkg.licenceReference())
            .addValue("createdBy", pkg.createdBy())
            .addValue("createdAt", Timestamp.from(pkg.createdAt()))
            .addValue("contentDeleteAfter", Timestamp.from(pkg.contentDeleteAfter()))
            .addValue("contentSha256", pkg.contentSha256())
            .addValue("householdCount", pkg.householdCount())
            .addValue("residentCount", pkg.residentCount())
            .addValue("residingCount", pkg.residingCount())
            .addValue("temporarilyAbsentCount", pkg.temporarilyAbsentCount());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void insertPackageHouseholds(Long packageId, List<Long> householdIds) {
        if (householdIds == null || householdIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO agency_data_package_households (package_id, household_id) VALUES (:packageId, :householdId)";
        MapSqlParameterSource[] batch = new MapSqlParameterSource[householdIds.size()];
        for (int i = 0; i < householdIds.size(); i++) {
            batch[i] = new MapSqlParameterSource()
                .addValue("packageId", packageId)
                .addValue("householdId", householdIds.get(i));
        }
        jdbc.batchUpdate(sql, batch);
    }

    public void insertPackageContent(Long packageId, byte[] fileBytes, long byteLength, String contentSha256) {
        String sql = """
            INSERT INTO agency_data_package_contents (package_id, file_bytes, byte_length, content_sha256)
            VALUES (:packageId, :fileBytes, :byteLength, :contentSha256)
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("packageId", packageId)
            .addValue("fileBytes", fileBytes)
            .addValue("byteLength", byteLength)
            .addValue("contentSha256", contentSha256);
        jdbc.update(sql, params);
    }

    public Optional<AgencyPackageRecord> findPackageById(Long packageId) {
        String sql = "SELECT * FROM agency_data_packages WHERE id = :id";
        try {
            AgencyPackageRecord record = jdbc.queryForObject(sql, Map.of("id", packageId), ROW_MAPPER);
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<AgencyPackageRecord> findPackageForUpdate(Long packageId) {
        String sql = "SELECT * FROM agency_data_packages WHERE id = :id FOR UPDATE";
        try {
            AgencyPackageRecord record = jdbc.queryForObject(sql, Map.of("id", packageId), ROW_MAPPER);
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Long> findHouseholdIdsByPackageId(Long packageId) {
        String sql = "SELECT household_id FROM agency_data_package_households WHERE package_id = :packageId ORDER BY household_id ASC";
        return jdbc.queryForList(sql, Map.of("packageId", packageId), Long.class);
    }

    public Optional<byte[]> findPackageContentBytes(Long packageId) {
        String sql = "SELECT file_bytes FROM agency_data_package_contents WHERE package_id = :packageId";
        try {
            byte[] bytes = jdbc.queryForObject(sql, Map.of("packageId", packageId), byte[].class);
            return Optional.ofNullable(bytes);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<AgencyPackageRecord> findPackages(String status, Instant notExpiredCutoff, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM agency_data_packages WHERE created_at > :notExpiredCutoff");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("notExpiredCutoff", Timestamp.from(notExpiredCutoff))
            .addValue("limit", limit)
            .addValue("offset", offset);

        if (status != null && !status.isBlank()) {
            sql.append(" AND status = :status");
            params.addValue("status", status);
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset");
        return jdbc.query(sql.toString(), params, ROW_MAPPER);
    }

    public long countPackages(String status, Instant notExpiredCutoff) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM agency_data_packages WHERE created_at > :notExpiredCutoff");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("notExpiredCutoff", Timestamp.from(notExpiredCutoff));

        if (status != null && !status.isBlank()) {
            sql.append(" AND status = :status");
            params.addValue("status", status);
        }
        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count : 0L;
    }

    public void updatePackageApprove(Long id, Long approvedBy, Instant approvedAt, Instant downloadUntil, Long newVersion) {
        String sql = """
            UPDATE agency_data_packages
            SET status = 'APPROVED',
                approved_by = :approvedBy,
                approved_at = :approvedAt,
                download_until = :downloadUntil,
                version = :newVersion
            WHERE id = :id
            """;
        jdbc.update(sql, Map.of(
            "id", id,
            "approvedBy", approvedBy,
            "approvedAt", Timestamp.from(approvedAt),
            "downloadUntil", Timestamp.from(downloadUntil),
            "newVersion", newVersion
        ));
    }

    public void updatePackageReject(Long id, Long rejectedBy, Instant rejectedAt, Long newVersion) {
        String sql = """
            UPDATE agency_data_packages
            SET status = 'REJECTED',
                rejected_by = :rejectedBy,
                rejected_at = :rejectedAt,
                version = :newVersion
            WHERE id = :id
            """;
        jdbc.update(sql, Map.of(
            "id", id,
            "rejectedBy", rejectedBy,
            "rejectedAt", Timestamp.from(rejectedAt),
            "newVersion", newVersion
        ));
    }

    public void updatePackageRevoke(Long id, Long revokedBy, Instant revokedAt, Long newVersion) {
        String sql = """
            UPDATE agency_data_packages
            SET status = 'REVOKED',
                revoked_by = :revokedBy,
                revoked_at = :revokedAt,
                version = :newVersion
            WHERE id = :id
            """;
        jdbc.update(sql, Map.of(
            "id", id,
            "revokedBy", revokedBy,
            "revokedAt", Timestamp.from(revokedAt),
            "newVersion", newVersion
        ));
    }

    public void updatePackageHandoff(Long id, Long handedOffBy, Instant handedOffAt, Long newVersion) {
        String sql = """
            UPDATE agency_data_packages
            SET handed_off_by = :handedOffBy,
                handed_off_at = :handedOffAt,
                version = :newVersion
            WHERE id = :id
            """;
        jdbc.update(sql, Map.of(
            "id", id,
            "handedOffBy", handedOffBy,
            "handedOffAt", Timestamp.from(handedOffAt),
            "newVersion", newVersion
        ));
    }

    public List<Long> findPackageIdsForContentDeletion(Instant now, int limit) {
        String sql = """
            SELECT id FROM agency_data_packages
            WHERE content_deleted_at IS NULL AND content_delete_after <= :now
            ORDER BY id ASC
            LIMIT :limit
            """;
        return jdbc.queryForList(sql, Map.of("now", Timestamp.from(now), "limit", limit), Long.class);
    }

    public int deletePackageContents(List<Long> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return 0;
        }
        String sql = "DELETE FROM agency_data_package_contents WHERE package_id IN (:packageIds)";
        return jdbc.update(sql, Map.of("packageIds", packageIds));
    }

    public void markContentsDeleted(List<Long> packageIds, Instant deletedAt) {
        if (packageIds == null || packageIds.isEmpty()) {
            return;
        }
        String sql = "UPDATE agency_data_packages SET content_deleted_at = :deletedAt WHERE id IN (:packageIds)";
        jdbc.update(sql, Map.of("packageIds", packageIds, "deletedAt", Timestamp.from(deletedAt)));
    }

    public List<Long> findPackageIdsForMetadataDeletion(Instant cutoff, int limit) {
        String sql = """
            SELECT id FROM agency_data_packages
            WHERE created_at <= :cutoff
            ORDER BY id ASC
            LIMIT :limit
            """;
        return jdbc.queryForList(sql, Map.of("cutoff", Timestamp.from(cutoff), "limit", limit), Long.class);
    }

    public int deletePackagesMetadata(List<Long> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = Map.of("packageIds", packageIds);
        jdbc.update("DELETE FROM agency_data_package_contents WHERE package_id IN (:packageIds)", params);
        jdbc.update("DELETE FROM agency_data_package_households WHERE package_id IN (:packageIds)", params);
        return jdbc.update("DELETE FROM agency_data_packages WHERE id IN (:packageIds)", params);
    }

    public int deleteExpiredAudits(Instant cutoff, List<String> actions, int limit) {
        if (actions == null || actions.isEmpty()) {
            return 0;
        }
        String sql = """
            DELETE FROM audit_log
            WHERE action IN (:actions) AND occurred_at <= :cutoff
            LIMIT :limit
            """;
        return jdbc.update(sql, Map.of(
            "actions", actions,
            "cutoff", Timestamp.from(cutoff),
            "limit", limit
        ));
    }
}
