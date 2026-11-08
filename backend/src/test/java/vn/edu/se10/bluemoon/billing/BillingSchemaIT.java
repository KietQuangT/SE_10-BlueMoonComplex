package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BillingSchemaIT {
    @Autowired JdbcTemplate jdbc;

    @Test
    void onlyOneNonCancelledPeriodPerMonthButCancelledHistoryCanRemain() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        Long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        String month = "9999-12";
        insertPeriod(actor, month, "CANCELLED");
        insertPeriod(actor, month, "CANCELLED");
        insertPeriod(actor, month, "DRAFT");
        assertThrows(DataIntegrityViolationException.class, () -> insertPeriod(actor, month, "PENDING_APPROVAL"));
    }

    @Test
    void mysqlEnforcesChecksAndForeignKeys() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        Long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) VALUES ('9998-01', '9998-01-31', 'DRAFT', 0, 0, -1, ?, NOW(6))", actor))
                .isInstanceOf(DataAccessException.class).hasStackTraceContaining("ck_billing_period_counters");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) VALUES ('9998-02', '9998-02-28', 'DRAFT', 0, 0, 0, 9223372036854775807, NOW(6))"))
                .isInstanceOf(DataAccessException.class).hasStackTraceContaining("fk_billing_period_creator");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO voluntary_campaigns (name, opens_on, closes_on, status, version, created_by, created_at) VALUES ('test', '9998-02-02', '9998-02-01', 'DRAFT', 0, ?, NOW(6))", actor))
                .isInstanceOf(DataAccessException.class).hasStackTraceContaining("ck_campaign_dates");
    }

    private void insertPeriod(Long actor, String month, String status) {
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) VALUES (?, '9999-12-31', ?, 0, 0, 0, ?, NOW(6))", month, status, actor);
    }
}
