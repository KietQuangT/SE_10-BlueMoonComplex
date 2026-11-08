package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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

    private void insertPeriod(Long actor, String month, String status) {
        jdbc.update("INSERT INTO billing_periods (period, due_date, status, version, revision, receipt_count, created_by, created_at) VALUES (?, '9999-12-31', ?, 0, 0, 0, ?, NOW(6))", month, status, actor);
    }
}
