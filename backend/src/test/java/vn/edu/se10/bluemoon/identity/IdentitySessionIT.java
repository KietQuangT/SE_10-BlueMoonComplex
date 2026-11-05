package vn.edu.se10.bluemoon.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.identity.application.LoginAttemptService;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import org.flywaydb.core.Flyway;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

// Runs only with -Pintegration and a dedicated MySQL database.
@SpringBootTest
@AutoConfigureMockMvc
@Import(IdentitySessionIT.ClockConfig.class)
@org.junit.jupiter.api.extension.ExtendWith(org.springframework.boot.test.system.OutputCaptureExtension.class)
class IdentitySessionIT {
    static final class AdjustableClock extends Clock {
        Instant now = Instant.parse("2026-11-05T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return Clock.fixed(now, zone); }
        @Override public Instant instant() { return now; }
    }
    @TestConfiguration static class ClockConfig {
        @Bean @Primary AdjustableClock adjustableClock() { return new AdjustableClock(); }
    }
    @Autowired AdjustableClock clock;
    @Autowired LoginAttemptService attempts;
    @Autowired AuditService audit;
    @Autowired TransactionTemplate transactions;
    @Autowired Flyway flyway;
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;

    long userId;
    String username;
    String sessionId;

    @BeforeEach void createAccount() {
        clock.now = Instant.parse("2026-11-05T12:00:00Z");
        username = "s01-it-" + UUID.randomUUID();
        long roleId = jdbc.queryForObject("SELECT id FROM roles WHERE code = 'ACCOUNTANT'", Long.class);
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at)"
                        + " VALUES (?, ?, ?, 'Ke Toan Test', 'ACTIVE', NOW(6), NOW(6))",
                roleId, username, passwordEncoder.encode("test-password-only"));
        userId = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    @AfterEach void deleteAccount() {
        if (sessionId != null) jdbc.update("DELETE FROM SPRING_SESSION WHERE SESSION_ID = ?", sessionId);
        jdbc.update("DELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?", username);
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id = ?", userId);
        jdbc.update("DELETE FROM users WHERE id = ?", userId);
    }

    @Test void csrfLoginCreatesCanonicalPrincipalAndMeReturnsSafeFields() throws Exception {
        MvcResult csrf = mvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        String token = JsonMapper.builder().build().readTree(csrf.getResponse().getContentAsString()).get("token").asString();
        MockHttpSession session = (MockHttpSession) csrf.getRequest().getSession(false);
        sessionId = session.getId();
        String oldSessionId = session.getId();

        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", token)
                        .contentType("application/json").content("{\"username\":\"" + username + "\",\"password\":\"test-password-only\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.fullName").value("Ke Toan Test"))
                .andExpect(jsonPath("$.role").value("ACCOUNTANT"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());

        assertThat(session.getId()).isNotEqualTo(oldSessionId);
        sessionId = session.getId();
        mvc.perform(get("/api/v1/auth/me").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(username));
        assertThat(jdbc.queryForObject("SELECT PRINCIPAL_NAME FROM SPRING_SESSION WHERE SESSION_ID = ?", String.class, session.getId()))
                .isEqualTo(username);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ? AND action = 'LOGIN_SUCCESS'", Integer.class, userId))
                .isEqualTo(1);
    }

    @Test void failedLoginCountCommitsWith401() throws Exception {
        MvcResult csrf = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        String token = JsonMapper.builder().build().readTree(csrf.getResponse().getContentAsString()).get("token").asString();
        MockHttpSession session = (MockHttpSession) csrf.getRequest().getSession(false);
        sessionId = session.getId();
        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", token)
                        .contentType("application/json").content("{\"username\":\"" + username + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForObject("SELECT failed_login_count FROM users WHERE id = ?", Integer.class, userId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ? AND action = 'LOGIN_FAILED'", Integer.class, userId))
                .isEqualTo(1);
    }

    @Test void fifthFailureLocksForOneMinuteAndCorrectPasswordWhileLockedChangesNothing() throws Exception {
        CsrfSession csrf = newCsrfSession();
        for (int i = 1; i <= 5; i++) postLogin(csrf, "wrong").andExpect(status().isUnauthorized());
        var locked = jdbc.queryForMap("SELECT failed_login_count, lock_level, locked_until FROM users WHERE id = ?", userId);
        assertThat(locked.get("failed_login_count")).isEqualTo(0);
        assertThat(locked.get("lock_level")).isEqualTo(1);
        assertThat(locked.get("locked_until")).isNotNull();
        assertThat(((java.sql.Timestamp) locked.get("locked_until")).toLocalDateTime()).isEqualTo(LocalDateTime.ofInstant(clock.now.plusSeconds(60), ZoneOffset.UTC));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ? AND action = 'LOGIN_FAILED'", Integer.class, userId)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ? AND action = 'ACCOUNT_LOCKED'", Integer.class, userId)).isEqualTo(1);
        int auditRows = jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ?", Integer.class, userId);

        postLogin(csrf, "test-password-only").andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level, locked_until FROM users WHERE id = ?", userId))
                .containsEntry("failed_login_count", 0).containsEntry("lock_level", 1).containsEntry("locked_until", locked.get("locked_until"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ?", Integer.class, userId)).isEqualTo(auditRows);
    }

    @Test void successfulLoginAfterExpiryResetsAllLockColumns() throws Exception {
        jdbc.update("UPDATE users SET failed_login_count = 0, lock_level = 2, locked_until = ? WHERE id = ?", LocalDateTime.ofInstant(clock.now.minusSeconds(1), ZoneOffset.UTC), userId);
        CsrfSession csrf = newCsrfSession();
        postLogin(csrf, "test-password-only").andExpect(status().isOk());
        assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level, locked_until FROM users WHERE id = ?", userId))
                .containsEntry("failed_login_count", 0).containsEntry("lock_level", 0).containsEntry("locked_until", null);
    }

    @Test void adminFailuresAreAuditedButNeverLocked() throws Exception {
        jdbc.update("UPDATE users SET role_id = (SELECT id FROM roles WHERE code = 'ADMIN') WHERE id = ?", userId);
        CsrfSession csrf = newCsrfSession();
        for (int i = 0; i < 20; i++) postLogin(csrf, "wrong").andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level, locked_until FROM users WHERE id = ?", userId))
                .containsEntry("failed_login_count", 0).containsEntry("lock_level", 0).containsEntry("locked_until", null);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ? AND action = 'LOGIN_FAILED'", Integer.class, userId))
                .isEqualTo(20);
        postLogin(csrf, "test-password-only").andExpect(status().isOk());
    }


    @Test void loginFailureBodiesAreIdenticalForAllDeniedBranches() throws Exception {
        CsrfSession csrf = newCsrfSession();
        String wrong = postLogin(csrf, "wrong").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        int auditBefore = jdbc.queryForObject("SELECT COUNT(*) FROM audit_log", Integer.class);
        var absent = mvc.perform(post("/api/v1/auth/login").session(csrf.session()).header("X-CSRF-TOKEN", csrf.token())
                .contentType("application/json").content("{\"username\":\"missing-" + username + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log", Integer.class)).isEqualTo(auditBefore);
        jdbc.update("UPDATE users SET status='INACTIVE' WHERE id=?", userId);
        String inactive = postLogin(csrf, "wrong").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        jdbc.update("UPDATE users SET status='ACTIVE', locked_until=? WHERE id=?", LocalDateTime.ofInstant(clock.now.plusSeconds(60), ZoneOffset.UTC), userId);
        String locked = postLogin(csrf, "wrong").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        jdbc.update("UPDATE users SET locked_until=NULL, role_id=(SELECT id FROM roles WHERE code='RESIDENT'), household_id=NULL WHERE id=?", userId);
        String noHouse = postLogin(csrf, "wrong").andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(absent).isEqualTo(wrong); assertThat(inactive).isEqualTo(wrong);
        assertThat(locked).isEqualTo(wrong); assertThat(noHouse).isEqualTo(wrong);
    }

    @Test void lookupIsCaseInsensitiveAndReturnsCanonicalAccount() {
        var result = attempts.login(username.toUpperCase(java.util.Locale.ROOT), "test-password-only");
        assertThat(result.authenticated()).isTrue();
        assertThat(result.user().username()).isEqualTo(username);
        assertThat(result.user().role()).isEqualTo("ACCOUNTANT");
    }

    @ParameterizedTest @CsvSource({"1,1", "2,5", "3,10", "4,15", "5,30", "6,60"})
    void eachLockLevelUsesExpectedDuration(int level, int minutes) {
        jdbc.update("UPDATE users SET lock_level=?, failed_login_count=0, locked_until=? WHERE id=?",
                level - 1, LocalDateTime.ofInstant(clock.now.minusSeconds(1), ZoneOffset.UTC), userId);
        int attemptsNeeded = level == 1 ? 5 : 1;
        for (int i = 1; i <= attemptsNeeded; i++) {
            assertThat(attempts.login(username, "wrong").authenticated()).isFalse();
            if (i < attemptsNeeded) assertThat(jdbc.queryForObject("SELECT lock_level FROM users WHERE id=?", Integer.class, userId)).isEqualTo(0);
        }
        var row = jdbc.queryForMap("SELECT status, lock_level, failed_login_count, locked_until FROM users WHERE id=?", userId);
        assertThat(row).containsEntry("status", "ACTIVE").containsEntry("lock_level", level).containsEntry("failed_login_count", 0);
        assertThat(((java.sql.Timestamp) row.get("locked_until")).toLocalDateTime()).isEqualTo(LocalDateTime.ofInstant(clock.now.plusSeconds(minutes * 60L), ZoneOffset.UTC));
        String changes = jdbc.queryForObject("SELECT changes FROM audit_log WHERE actor_user_id=? AND action='ACCOUNT_LOCKED'", String.class, userId);
        assertThat(JsonMapper.builder().build().readTree(changes).get("level").asString()).isEqualTo(Integer.toString(level));
        assertThat(JsonMapper.builder().build().readTree(changes).get("minutes").asString()).isEqualTo(Integer.toString(minutes));
        assertThat(changes).doesNotContain(username, "password", "email");
    }

    @Test void successResetsFailuresBeforeAnotherFourFailures() {
        for (int i=0; i<3; i++) attempts.login(username, "wrong");
        assertThat(attempts.login(username, "test-password-only").authenticated()).isTrue();
        for (int i=0; i<4; i++) attempts.login(username, "wrong");
        assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level, locked_until FROM users WHERE id=?", userId))
                .containsEntry("failed_login_count", 4).containsEntry("lock_level", 0).containsEntry("locked_until", null);
    }

    @Test void lockAuditRollsBackWithTransaction() {
        jdbc.update("UPDATE users SET failed_login_count=4 WHERE id=?", userId);
        assertThatThrownBy(() -> transactions.executeWithoutResult(status -> {
            attempts.login(username, "wrong");
            throw new IllegalStateException("rollback test");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("SELECT failed_login_count FROM users WHERE id=?", Integer.class, userId)).isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=?", Integer.class, userId)).isZero();
    }

    @Test void bootstrapAuditRequiresTransaction() {
        assertThatThrownBy(() -> audit.recordBootstrap("test", Map.of())).isInstanceOf(org.springframework.transaction.IllegalTransactionStateException.class);
    }

    @Test void migrationIsValidatedAndRejectsNegativeValues() {
        flyway.validate();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE version='5' AND success=1", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level FROM users WHERE id=?", userId))
                .containsEntry("failed_login_count", 0).containsEntry("lock_level", 0);
        assertThatThrownBy(() -> jdbc.update("UPDATE users SET failed_login_count=-1 WHERE id=?", userId)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE users SET lock_level=-1 WHERE id=?", userId)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }


    @Test void concurrentFailuresDoNotLoseUpdates() throws Exception {
        var executor = java.util.concurrent.Executors.newFixedThreadPool(10);
        var start = new java.util.concurrent.CountDownLatch(1);
        try {
            var results = new java.util.ArrayList<java.util.concurrent.Future<LoginAttemptService.Result>>();
            for (int i = 0; i < 10; i++) results.add(executor.submit(() -> { start.await(); return attempts.login(username, "wrong"); }));
            start.countDown();
            for (var result : results) assertThat(result.get().authenticated()).isFalse();
            assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level FROM users WHERE id=?", userId))
                    .containsEntry("failed_login_count", 0).containsEntry("lock_level", 1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='LOGIN_FAILED'", Integer.class, userId)).isEqualTo(5);
        } finally { executor.shutdownNow(); }
    }

    @Test void lockingOneAccountDoesNotAffectAnother() {
        String other = username + "-b";
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) SELECT role_id, ?, password_hash, full_name, status, NOW(6), NOW(6) FROM users WHERE id=?", other, userId);
        long otherId = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, other);
        try {
            for (int i=0; i<5; i++) attempts.login(username, "wrong");
            assertThat(attempts.login(other, "test-password-only").authenticated()).isTrue();
            assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level, locked_until FROM users WHERE id=?", otherId))
                    .containsEntry("failed_login_count", 0).containsEntry("lock_level", 0).containsEntry("locked_until", null);
        } finally {
            jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", otherId);
            jdbc.update("DELETE FROM users WHERE id=?", otherId);
        }
    }

    @Test void rejectedCsrfDoesNotChangeCounterOrAudit() throws Exception {
        CsrfSession csrf = newCsrfSession();
        for (String token : new String[] {"", "invalid"}) {
            mvc.perform(post("/api/v1/auth/login").session(csrf.session()).header("X-CSRF-TOKEN", token)
                    .contentType("application/json").content("{\"username\":\"" + username + "\",\"password\":\"wrong\"}"))
                    .andExpect(status().isForbidden());
            mvc.perform(post("/api/v1/auth/logout").session(csrf.session()).header("X-CSRF-TOKEN", token))
                    .andExpect(status().isForbidden());
        }
        assertThat(jdbc.queryForObject("SELECT failed_login_count FROM users WHERE id=?", Integer.class, userId)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=?", Integer.class, userId)).isZero();
    }


    @Test void realAdminAndResidentLoginReturnOnlySafeFields() throws Exception {
        String householdCode = "s01-" + UUID.randomUUID();
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'ACTIVE', NOW(6), NOW(6))", householdCode, householdCode);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, householdCode);
        try {
            for (String role : new String[] {"ADMIN", "RESIDENT"}) {
                jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code=?), household_id=? WHERE id=?", role, household, userId);
                var result = postLogin(newCsrfSession(), "test-password-only").andExpect(status().isOk()).andReturn();
                var body = JsonMapper.builder().build().readTree(result.getResponse().getContentAsString());
                assertThat(body.size()).isEqualTo(4);
                assertThat(body.get("role").asString()).isEqualTo(role);
                for (String field : new String[] {"passwordHash", "email", "status", "failedLoginCount", "lockedUntil"}) assertThat(body.has(field)).isFalse();
            }
        } finally {
            jdbc.update("UPDATE users SET household_id=NULL WHERE id=?", userId);
            jdbc.update("DELETE FROM households WHERE id=?", household);
        }
    }

    @Test void loginBranchesDoNotLogSecrets(org.springframework.boot.test.system.CapturedOutput output) throws Exception {
        CsrfSession csrf = newCsrfSession();
        postLogin(csrf, "wrong").andExpect(status().isUnauthorized());
        attempts.login("missing-" + username, "wrong");
        jdbc.update("UPDATE users SET status='INACTIVE' WHERE id=?", userId);
        postLogin(csrf, "test-password-only").andExpect(status().isUnauthorized());
        jdbc.update("UPDATE users SET status='ACTIVE', locked_until=? WHERE id=?", LocalDateTime.ofInstant(clock.now.plusSeconds(60), ZoneOffset.UTC), userId);
        postLogin(csrf, "test-password-only").andExpect(status().isUnauthorized());
        jdbc.update("UPDATE users SET locked_until=NULL WHERE id=?", userId);
        postLogin(csrf, "test-password-only").andExpect(status().isOk());
        assertThat(output.getAll()).doesNotContain(username, "test-password-only", csrf.token(), csrf.session().getId())
                .doesNotContainIgnoringCase("generated security password");
    }

    private CsrfSession newCsrfSession() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        String token = JsonMapper.builder().build().readTree(result.getResponse().getContentAsString()).get("token").asString();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        sessionId = session.getId();
        return new CsrfSession(session, token);
    }

    private org.springframework.test.web.servlet.ResultActions postLogin(CsrfSession csrf, String password) throws Exception {
        var actions = mvc.perform(post("/api/v1/auth/login").session(csrf.session()).header("X-CSRF-TOKEN", csrf.token())
                .contentType("application/json").content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"));
        sessionId = csrf.session().getId();
        return actions;
    }

    private record CsrfSession(MockHttpSession session, String token) {}
}
