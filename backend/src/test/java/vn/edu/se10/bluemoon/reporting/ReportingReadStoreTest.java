package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportData;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportRow;
import vn.edu.se10.bluemoon.reporting.persistence.CashReceiptsReportReadStore;
import vn.edu.se10.bluemoon.reporting.persistence.FeeCollectionReportReadStore;
import vn.edu.se10.bluemoon.reporting.persistence.ResidencyReportReadStore;

class ReportingReadStoreTest {
    private NamedParameterJdbcTemplate jdbc;
    private Instant now;

    @BeforeEach
    void setUp() {
        jdbc = mock(NamedParameterJdbcTemplate.class);
        now = Instant.parse("2026-11-10T10:00:00Z");
    }

    @Test
    void feeCollection_exceeds50000_throwsReportTooLarge() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(Long.class)))
            .thenReturn(50001L);

        var store = new FeeCollectionReportReadStore(jdbc);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> store.load("2026-10", now));
        assertEquals("REPORT_TOO_LARGE", ex.code());
    }

    @Test
    void feeCollection_computesSettledRemainingAndRatioCorrectly() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(Long.class)))
            .thenReturn(2L);

        when(jdbc.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
            .thenAnswer(inv -> {
                RowMapper<FeeCollectionReportRow> mapper = inv.getArgument(2);

                // Row 1: 530,000 total, 200,000 settled -> 330,000 remaining, PARTIALLY_PAID
                ResultSet rs1 = mock(ResultSet.class);
                when(rs1.getLong("id")).thenReturn(101L);
                when(rs1.getString("household_code")).thenReturn("H01");
                when(rs1.getString("apartment_code")).thenReturn("A01");
                when(rs1.getDate("due_date")).thenReturn(Date.valueOf(LocalDate.of(2026, 10, 31)));
                when(rs1.getBigDecimal("total")).thenReturn(new BigDecimal("530000"));
                when(rs1.getBigDecimal("settled")).thenReturn(new BigDecimal("200000"));

                // Row 2: 100,000 total, 100,000 settled -> 0 remaining, PAID
                ResultSet rs2 = mock(ResultSet.class);
                when(rs2.getLong("id")).thenReturn(102L);
                when(rs2.getString("household_code")).thenReturn("H02");
                when(rs2.getString("apartment_code")).thenReturn("A02");
                when(rs2.getDate("due_date")).thenReturn(Date.valueOf(LocalDate.of(2026, 10, 31)));
                when(rs2.getBigDecimal("total")).thenReturn(new BigDecimal("100000"));
                when(rs2.getBigDecimal("settled")).thenReturn(new BigDecimal("100000"));

                return List.of(mapper.mapRow(rs1, 0), mapper.mapRow(rs2, 1));
            });

        var store = new FeeCollectionReportReadStore(jdbc);
        FeeCollectionReportData data = store.load("2026-10", now);

        assertEquals(2, data.totals().invoiceCount());
        assertEquals(new BigDecimal("630000"), data.totals().totalDue());
        assertEquals(new BigDecimal("300000"), data.totals().totalSettled());
        assertEquals(new BigDecimal("330000"), data.totals().totalRemaining());

        // ratio = 300,000 * 100 / 630,000 = 47.6190... -> 47.62%
        assertNotNull(data.totals().ratioPercent());
        assertEquals(new BigDecimal("47.62"), data.totals().ratioPercent());

        assertEquals("PARTIALLY_PAID", data.rows().get(0).paymentStatus());
        assertEquals("PAID", data.rows().get(1).paymentStatus());
    }

    @Test
    void feeCollection_invariantViolation_settledExceedsTotal_throwsIllegalStateException() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(Long.class)))
            .thenReturn(1L);

        when(jdbc.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
            .thenAnswer(inv -> {
                RowMapper<FeeCollectionReportRow> mapper = inv.getArgument(2);
                ResultSet rs = mock(ResultSet.class);
                when(rs.getLong("id")).thenReturn(201L);
                when(rs.getString("household_code")).thenReturn("H01");
                when(rs.getString("apartment_code")).thenReturn("A01");
                when(rs.getDate("due_date")).thenReturn(Date.valueOf(LocalDate.of(2026, 10, 31)));
                when(rs.getBigDecimal("total")).thenReturn(new BigDecimal("500000"));
                when(rs.getBigDecimal("settled")).thenReturn(new BigDecimal("600000")); // Invariant broken!
                return List.of(mapper.mapRow(rs, 0));
            });

        var store = new FeeCollectionReportReadStore(jdbc);
        assertThrows(IllegalStateException.class, () -> store.load("2026-10", now));
    }

    @Test
    void cashReceipts_exceeds50000_throwsReportTooLarge() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(Long.class)))
            .thenReturn(50001L);

        var store = new CashReceiptsReportReadStore(jdbc);
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
            store.load(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), now));
        assertEquals("REPORT_TOO_LARGE", ex.code());
    }

    @Test
    void cashReceipts_invariantViolation_sumMismatch_throwsIllegalStateException() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(Long.class)))
            .thenReturn(1L);

        when(jdbc.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
            .thenAnswer(inv -> {
                RowMapper<?> mapper = inv.getArgument(2);
                ResultSet rs = mock(ResultSet.class);
                when(rs.getLong("receipt_id")).thenReturn(301L);
                when(rs.getString("invoice_period")).thenReturn("2026-10");
                when(rs.getString("household_code")).thenReturn("H01");
                when(rs.getString("apartment_code")).thenReturn("A01");
                when(rs.getDate("paid_on")).thenReturn(Date.valueOf(LocalDate.of(2026, 10, 15)));
                when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("500000"));
                when(rs.getBigDecimal("applied_amount")).thenReturn(new BigDecimal("300000"));
                when(rs.getBigDecimal("credit_amount")).thenReturn(new BigDecimal("100000")); // Sum is 400k != 500k!
                return List.of(mapper.mapRow(rs, 0));
            });

        var store = new CashReceiptsReportReadStore(jdbc);
        assertThrows(IllegalStateException.class, () ->
            store.load(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), now));
    }

    @Test
    void residency_exceeds50000_throwsReportTooLarge() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(Long.class)))
            .thenReturn(50001L);

        var store = new ResidencyReportReadStore(jdbc);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> store.load(now));
        assertEquals("REPORT_TOO_LARGE", ex.code());
    }
}
