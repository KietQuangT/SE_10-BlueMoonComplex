package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import vn.edu.se10.bluemoon.reporting.application.AgencyPackageRow;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyDataPackageStore;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyPackageRecord;

class AgencyDataPackageStoreTest {
    private NamedParameterJdbcTemplate jdbc;
    private AgencyDataPackageStore store;
    private final Instant now = Instant.parse("2026-11-10T10:00:00Z");

    @BeforeEach
    void setUp() {
        jdbc = mock(NamedParameterJdbcTemplate.class);
        store = new AgencyDataPackageStore(jdbc);
    }

    @Test
    void validateActiveHouseholdIds_empty_returnsEmptyWithoutQuery() {
        List<Long> result = store.validateActiveHouseholdIds(List.of());
        assertTrue(result.isEmpty());
        verify(jdbc, never()).queryForList(anyString(), anyMap(), eq(Long.class));
    }

    @Test
    void validateActiveHouseholdIds_withIds_queriesDb() {
        when(jdbc.queryForList(anyString(), anyMap(), eq(Long.class))).thenReturn(List.of(1L, 2L));
        List<Long> result = store.validateActiveHouseholdIds(List.of(1L, 2L));
        assertEquals(2, result.size());
        assertEquals(List.of(1L, 2L), result);
    }

    @Test
    void fetchSnapshotResidents_queriesDbWithOrdering() {
        when(jdbc.query(anyString(), anyMap(), any(RowMapper.class))).thenReturn(List.of(
            new AgencyPackageRow("H01", "A101", "User A", "RESIDING")
        ));
        List<AgencyPackageRow> result = store.fetchSnapshotResidents(List.of(1L));
        assertEquals(1, result.size());
        assertEquals("User A", result.get(0).fullName());
    }

    @Test
    void findPackageById_notFound_returnsEmpty() {
        when(jdbc.queryForObject(anyString(), anyMap(), any(RowMapper.class)))
            .thenThrow(new EmptyResultDataAccessException(1));
        Optional<AgencyPackageRecord> result = store.findPackageById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findPackageById_found_mapsRecordCorrectly() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(100L);
        when(rs.getString("status")).thenReturn("DRAFT");
        when(rs.getLong("version")).thenReturn(1L);
        when(rs.getString("template_id")).thenReturn("AGENCY_RESIDENCY_V1");
        when(rs.getString("template_version")).thenReturn("1.0");
        when(rs.getString("recipient_agency_code")).thenReturn("CAQ1");
        when(rs.getString("request_reference")).thenReturn("REQ-01");
        when(rs.getString("licence_reference")).thenReturn("LIC-02");
        when(rs.getLong("created_by")).thenReturn(1L);
        when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(now));
        when(rs.getTimestamp("content_delete_after")).thenReturn(Timestamp.from(now.plusSeconds(30 * 86400)));
        when(rs.getString("content_sha256")).thenReturn("sha256");
        when(rs.getLong("household_count")).thenReturn(1L);
        when(rs.getLong("resident_count")).thenReturn(1L);
        when(rs.getLong("residing_count")).thenReturn(1L);
        when(rs.getLong("temporarily_absent_count")).thenReturn(0L);

        when(jdbc.queryForObject(anyString(), anyMap(), any(RowMapper.class)))
            .thenAnswer(inv -> {
                RowMapper<AgencyPackageRecord> mapper = inv.getArgument(2);
                return mapper.mapRow(rs, 1);
            });

        Optional<AgencyPackageRecord> result = store.findPackageById(100L);
        assertTrue(result.isPresent());
        assertEquals(100L, result.get().id());
        assertEquals("DRAFT", result.get().status());
        assertEquals(1L, result.get().householdCount());
    }

    @Test
    void deleteExpiredAudits_callsJdbcUpdate() {
        when(jdbc.update(anyString(), anyMap())).thenReturn(5);
        int deleted = store.deleteExpiredAudits(now, List.of("AGENCY_PACKAGE_CREATED"), 1000);
        assertEquals(5, deleted);
    }
}
