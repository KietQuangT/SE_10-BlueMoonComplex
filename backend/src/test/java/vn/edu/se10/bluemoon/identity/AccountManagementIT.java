package vn.edu.se10.bluemoon.identity;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import tools.jackson.databind.json.JsonMapper;

class AccountManagementIT extends AccountIntegrationBase {
    @MockitoSpyBean AuditService audit;
    @AfterEach void cleanHouses() {
        jdbc.update("UPDATE users SET household_id=NULL WHERE username LIKE ?", prefix+"%");
        jdbc.update("DELETE FROM residents WHERE household_id IN (SELECT id FROM households WHERE household_code LIKE ?)", prefix+"%");
        jdbc.update("DELETE FROM households WHERE household_code LIKE ?",prefix+"%");
    }
    long createUser(String suffix, String role) {
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code=?), ?, ?, 'Test', 'ACTIVE', NOW(6), NOW(6))", role, prefix+suffix, encoder.encode("test-only-pass"));
        return jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, prefix+suffix);
    }
    Cookie login(String suffix) throws Exception {
        var first=mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        var token=JsonMapper.builder().build().readTree(first.getResponse().getContentAsString()).get("token").asString();
        var result=mvc.perform(post("/api/v1/auth/login").cookie(remember(first.getResponse().getCookie("SESSION"))).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+prefix+suffix+"\",\"password\":\"test-only-pass\"}"))
            .andExpect(status().isOk()).andReturn();
        return remember(result.getResponse().getCookie("SESSION"));
    }
    void manage(long id, String action, int expected) throws Exception {
        mvc.perform(post("/api/v1/accounts/"+id+"/"+action).cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().is(expected));
    }
    @Test void disableRevokesAllSessionsAndEnableRestoresLogin() throws Exception {
        long target=createUser("-target", "ACCOUNTANT"); Cookie one=login("-target"), two=login("-target");
        manage(target,"disable",204);
        for (Cookie session : new Cookie[]{one,two}) mvc.perform(get("/api/v1/auth/me").cookie(session)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").cookie(cookie)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME=?",Long.class,prefix+"-target")).isZero();
        assertThat(jdbc.queryForList("SELECT action FROM audit_log WHERE entity_type='User' AND entity_id=? AND action IN ('ACCOUNT_DISABLED','SESSIONS_REVOKED')",String.class,Long.toString(target))).containsExactlyInAnyOrder("ACCOUNT_DISABLED","SESSIONS_REVOKED");
        manage(target,"disable",409); manage(target,"enable",204); manage(target,"enable",409); login("-target");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='ACCOUNT_ENABLED' AND entity_id=?",Long.class,Long.toString(target))).isEqualTo(1);
    }
    @Test void freshStatusAndRoleAreEnforcedOnNextRequest() throws Exception {
        long target=createUser("-target", "ACCOUNTANT"); Cookie targetCookie=login("-target");
        jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code='ADMIN') WHERE id=?",target);
        mvc.perform(get("/api/v1/households").cookie(targetCookie)).andExpect(status().isOk());
        jdbc.update("UPDATE users SET status='INACTIVE' WHERE id=?",target);
        mvc.perform(get("/api/v1/auth/me").cookie(targetCookie)).andExpect(status().isUnauthorized());
    }
    @Test void unlockClearsLockAndAllowsImmediateLogin() throws Exception {
        long target=createUser("-target", "ACCOUNTANT");
        jdbc.update("UPDATE users SET failed_login_count=3, lock_level=4, locked_until=DATE_ADD(UTC_TIMESTAMP(6), INTERVAL 10 MINUTE) WHERE id=?",target);
        manage(target,"unlock",204); manage(target,"unlock",409);
        assertThat(jdbc.queryForObject("SELECT failed_login_count + lock_level FROM users WHERE id=?",Integer.class,target)).isZero();
        assertThat(jdbc.queryForObject("SELECT locked_until FROM users WHERE id=?",java.sql.Timestamp.class,target)).isNull();
        login("-target");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='ACCOUNT_UNLOCKED' AND entity_id=?",Long.class,Long.toString(target))).isEqualTo(1);
    }
    @Test void conflictsAndMissingAccount() throws Exception { manage(adminId,"disable",409); manage(adminId,"unlock",409); manage(Long.MAX_VALUE,"enable",404); }
    @Test void updatedHouseholdIsUsedImmediately() throws Exception {
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 10, 'ACTIVE', NOW(6), NOW(6))",prefix+"-house",prefix+"-apt");
        long house=jdbc.queryForObject("SELECT id FROM households WHERE household_code=?",Long.class,prefix+"-house");
        long target=createUser("-target", "ACCOUNTANT"); Cookie targetCookie=login("-target");
        jdbc.update("UPDATE users SET household_id=? WHERE id=?",house,target);
        mvc.perform(get("/api/v1/auth/me").cookie(targetCookie)).andExpect(status().isOk()).andExpect(jsonPath("$.householdId").value(house));
        jdbc.update("UPDATE users SET household_id=NULL WHERE id=?",target);
        mvc.perform(get("/api/v1/auth/me").cookie(targetCookie)).andExpect(status().isOk()).andExpect(jsonPath("$.householdId").doesNotExist());
    }
    @Test void demotedAdminLosesAccessOnNextRequest() throws Exception {
        jdbc.update("UPDATE users SET role_id=(SELECT id FROM roles WHERE code='ACCOUNTANT') WHERE id=?", adminId);
        manage(adminId,"enable",403);
    }
    @Test void expiredLockOrFailedCountCanBeReset() throws Exception {
        long target=createUser("-target", "ACCOUNTANT");
        jdbc.update("UPDATE users SET failed_login_count=0, lock_level=2, locked_until=DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 1 MINUTE) WHERE id=?",target);
        manage(target,"unlock",204);
        jdbc.update("UPDATE users SET failed_login_count=1, lock_level=0, locked_until=NULL WHERE id=?",target);
        manage(target,"unlock",204); manage(target,"unlock",409); manage(adminId,"unlock",409);
        assertThat(jdbc.queryForObject("SELECT failed_login_count + lock_level FROM users WHERE id=?",Integer.class,target)).isZero();
        assertThat(jdbc.queryForObject("SELECT locked_until FROM users WHERE id=?",java.sql.Timestamp.class,target)).isNull();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='ACCOUNT_UNLOCKED' AND entity_id=?",Long.class,Long.toString(target))).isEqualTo(2);
    }
    @Test void residentSeesNewHouseholdOnNextRequest() throws Exception {
        for (String suffix : new String[]{"-h1","-h2"})
            jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 10, 'ACTIVE', NOW(6), NOW(6))",prefix+suffix,prefix+suffix);
        long first=jdbc.queryForObject("SELECT id FROM households WHERE household_code=?",Long.class,prefix+"-h1");
        long second=jdbc.queryForObject("SELECT id FROM households WHERE household_code=?",Long.class,prefix+"-h2");
        jdbc.update("INSERT INTO residents (household_id,full_name,residency_status,created_at,updated_at) VALUES (?, 'First', 'RESIDING', NOW(6), NOW(6)), (?, 'Second', 'RESIDING', NOW(6), NOW(6))",first,second);
        long target=createUser("-resident", "RESIDENT"); jdbc.update("UPDATE users SET household_id=? WHERE id=?",first,target);
        Cookie resident=login("-resident");
        mvc.perform(get("/api/v1/households/me/residents").cookie(resident)).andExpect(status().isOk()).andExpect(jsonPath("$[0].fullName").value("First"));
        jdbc.update("UPDATE users SET household_id=? WHERE id=?",second,target);
        mvc.perform(get("/api/v1/households/me/residents").cookie(resident)).andExpect(status().isOk()).andExpect(jsonPath("$[0].fullName").value("Second"));
    }
    @Test void invalidatedSessionCanFetchNewTokenAndLogin() throws Exception {
        long target=createUser("-target", "ACCOUNTANT"); Cookie old=login("-target");
        jdbc.update("UPDATE users SET status='INACTIVE' WHERE id=?",target);
        mvc.perform(get("/api/v1/auth/me").cookie(old)).andExpect(status().isUnauthorized());
        var tokenResponse=mvc.perform(get("/api/v1/auth/csrf").cookie(old)).andExpect(status().isOk()).andReturn();
        jdbc.update("UPDATE users SET status='ACTIVE' WHERE id=?",target);
        String token=JsonMapper.builder().build().readTree(tokenResponse.getResponse().getContentAsString()).get("token").asString();
        Cookie fresh=remember(tokenResponse.getResponse().getCookie("SESSION"));
        var result=mvc.perform(post("/api/v1/auth/login").cookie(fresh).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+prefix+"-target\",\"password\":\"test-only-pass\"}"))
            .andExpect(status().isOk()).andReturn();
        remember(result.getResponse().getCookie("SESSION"));
    }
    @Test void managementAuditChangesContainNoEmailOrPassword() throws Exception {
        long target=createUser("-target", "ACCOUNTANT");
        jdbc.update("UPDATE users SET email=? WHERE id=?", "audit-test@example.test", target);
        manage(target,"disable",204);
        assertAuditChangesEmpty(target,"ACCOUNT_DISABLED");
        assertAuditChangesEmpty(target,"SESSIONS_REVOKED");
        manage(target,"enable",204);
        assertAuditChangesEmpty(target,"ACCOUNT_ENABLED");
        jdbc.update("UPDATE users SET failed_login_count=1 WHERE id=?", target);
        manage(target,"unlock",204);
        assertAuditChangesEmpty(target,"ACCOUNT_UNLOCKED");
    }
    void assertAuditChangesEmpty(long target, String action) {
        var rows=jdbc.queryForList("SELECT changes FROM audit_log WHERE entity_type='User' AND entity_id=? AND action=? ORDER BY id DESC LIMIT 1", Long.toString(target), action);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("changes")).isNull();
    }
    @Test void auditFailureRollsBackStatusAndSessions() throws Exception {
        long target=createUser("-target", "ACCOUNTANT"); Cookie targetCookie=login("-target");
        AuditService actual=AopTestUtils.getUltimateTargetObject(audit);
        doThrow(new IllegalStateException()).when(actual).record(eq(adminId),eq("SESSIONS_REVOKED"),anyString(),anyString(),isNull());
        manage(target,"disable",500);
        assertThat(jdbc.queryForObject("SELECT status FROM users WHERE id=?",String.class,target)).isEqualTo("ACTIVE");
        mvc.perform(get("/api/v1/auth/me").cookie(targetCookie)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='ACCOUNT_DISABLED' AND entity_id=?",Long.class,Long.toString(target))).isZero();
    }
    @Test void concurrentDisableHasOneWinner() throws Exception {
        long target=createUser("-target", "ACCOUNTANT");
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        var start=new java.util.concurrent.CountDownLatch(1);
        try {
            java.util.concurrent.Callable<Integer> call=() -> { start.await(); return mvc.perform(post("/api/v1/accounts/"+target+"/disable").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andReturn().getResponse().getStatus(); };
            var first=pool.submit(call); var second=pool.submit(call); start.countDown();
            assertThat(java.util.List.of(first.get(30,java.util.concurrent.TimeUnit.SECONDS),second.get(30,java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(204,409);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='ACCOUNT_DISABLED' AND entity_id=?",Long.class,Long.toString(target))).isEqualTo(1);
        } finally { start.countDown(); pool.shutdownNow(); }
    }
}
