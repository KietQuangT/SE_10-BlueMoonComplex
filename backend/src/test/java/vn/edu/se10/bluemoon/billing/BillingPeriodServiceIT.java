package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@SpringBootTest
@Import(BillingPeriodServiceIT.FixedClock.class)
@Transactional
class BillingPeriodServiceIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired BillingPeriodService service;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary
        Clock billingTestClock() {
            return Clock.fixed(Instant.parse("9000-03-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Test
    void createsDraftAndAuditTogetherAndRejectsDuplicate() {
        String marker = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        long actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        var created = service.create("9000-03", LocalDate.of(9000, 3, 31), actor);
        assertEquals("DRAFT", created.getStatus());
        assertEquals(0, created.getReceiptCount());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='BILLING_PERIOD_CREATED'", Integer.class, actor));
        assertEquals("BILLING_PERIOD_EXISTS", assertThrows(ConflictException.class,
                () -> service.create("9000-03", LocalDate.of(9000, 3, 31), actor)).code());
    }
}
