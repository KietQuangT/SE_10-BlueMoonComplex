package vn.edu.se10.bluemoon.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
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
class IdentitySessionIT {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;

    long userId;
    String username;
    String sessionId;

    @BeforeEach void createAccount() {
        username = "s01-it-" + UUID.randomUUID();
        long roleId = jdbc.queryForObject("SELECT id FROM roles WHERE code = 'ACCOUNTANT'", Long.class);
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at)"
                        + " VALUES (?, ?, ?, 'Ke Toan Test', 'ACTIVE', NOW(6), NOW(6))",
                roleId, username, passwordEncoder.encode("test-password-only"));
        userId = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    @AfterEach void deleteAccount() {
        if (sessionId != null) jdbc.update("DELETE FROM SPRING_SESSION WHERE SESSION_ID = ?", sessionId);
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
        int remainingSeconds = jdbc.queryForObject("SELECT TIMESTAMPDIFF(SECOND, UTC_TIMESTAMP(6), locked_until) FROM users WHERE id = ?", Integer.class, userId);
        assertThat(remainingSeconds).isBetween(59, 60);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ? AND action = 'LOGIN_FAILED'", Integer.class, userId)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ? AND action = 'ACCOUNT_LOCKED'", Integer.class, userId)).isEqualTo(1);
        int auditRows = jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ?", Integer.class, userId);

        postLogin(csrf, "test-password-only").andExpect(status().isUnauthorized());
        assertThat(jdbc.queryForMap("SELECT failed_login_count, lock_level, locked_until FROM users WHERE id = ?", userId))
                .containsEntry("failed_login_count", 0).containsEntry("lock_level", 1).containsEntry("locked_until", locked.get("locked_until"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ?", Integer.class, userId)).isEqualTo(auditRows);
    }

    @Test void successfulLoginAfterExpiryResetsAllLockColumns() throws Exception {
        jdbc.update("UPDATE users SET failed_login_count = 0, lock_level = 2, locked_until = DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 1 SECOND) WHERE id = ?", userId);
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
