package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@SpringBootTest
@Import(VoluntaryContributionIT.FixedClock.class)
class VoluntaryContributionIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired VoluntaryContributionService service;
    long actor;
    long campaign;
    long household;
    long resident;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary Clock testClock() {
            return Clock.fixed(Instant.parse("9000-03-22T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @BeforeEach
    void setUp() {
        String marker = "s08-" + UUID.randomUUID();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'INACTIVE', NOW(6), NOW(6))", marker, marker);
        household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) VALUES (?, 'Test', 'MOVED_OUT', NOW(6), NOW(6))", household);
        resident = jdbc.queryForObject("SELECT id FROM residents WHERE household_id=?", Long.class, household);
        jdbc.update("INSERT INTO voluntary_campaigns (name, opens_on, closes_on, status, version, created_by, created_at, published_by, published_at) VALUES ('Test', '9000-03-01', '9000-03-20', 'PUBLISHED', 0, ?, NOW(6), ?, '9000-03-05 00:00:00')", actor, actor);
        campaign = jdbc.queryForObject("SELECT id FROM voluntary_campaigns WHERE created_by=?", Long.class, actor);
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
        jdbc.update("DELETE FROM voluntary_contributions WHERE campaign_id=?", campaign);
        jdbc.update("DELETE FROM voluntary_campaigns WHERE id=?", campaign);
        jdbc.update("DELETE FROM residents WHERE id=?", resident);
        jdbc.update("DELETE FROM households WHERE id=?", household);
        jdbc.update("DELETE FROM users WHERE id=?", actor);
    }

    @Test
    void lateEntryVoidAndReplayPreserveHistory() {
        String key = UUID.randomUUID().toString();
        var input = input(key, "100", LocalDate.of(9000, 3, 19), "entered late");
        var created = service.record(input, actor).contribution();
        assertEquals("POSTED", created.getStatus());
        var voided = service.voidContribution(created.getId(), created.getVersion(), "duplicate", actor);
        assertEquals("VOIDED", voided.getStatus());
        assertEquals("VOIDED", service.record(input, actor).contribution().getStatus());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM voluntary_contributions WHERE campaign_id=? AND status='POSTED'", Integer.class, campaign));
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.record(input(UUID.randomUUID().toString(), "1", LocalDate.of(9000, 3, 19), null), actor)).code());
        assertEquals("INVALID_PAID_DATE", assertThrows(ConflictException.class,
                () -> service.record(input(UUID.randomUUID().toString(), "1", LocalDate.of(9000, 3, 21), "late"), actor)).code());
    }

    @Test
    void concurrentSameKeyCreatesOneRecordAndOneAudit() throws Exception {
        var input = input(UUID.randomUUID().toString(), "100", LocalDate.of(9000, 3, 19), "late");
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> { start.await(); return service.record(input, actor); });
            var second = pool.submit(() -> { start.await(); return service.record(input, actor); });
            start.countDown();
            var a = first.get(15, TimeUnit.SECONDS);
            var b = second.get(15, TimeUnit.SECONDS);
            assertEquals(a.contribution().getId(), b.contribution().getId());
            assertNotEquals(a.replay(), b.replay());
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM voluntary_contributions WHERE campaign_id=?", Integer.class, campaign));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='VOLUNTARY_CONTRIBUTION_RECORDED'", Integer.class, actor));
        } finally {
            pool.shutdownNow();
        }
    }

    private ContributionInput input(String key, String amount, LocalDate paidOn, String reason) {
        return ContributionInput.parse(campaign, resident, amount, paidOn, reason, key);
    }
}
