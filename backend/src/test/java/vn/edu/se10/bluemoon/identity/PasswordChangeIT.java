package vn.edu.se10.bluemoon.identity;

import jakarta.servlet.http.Cookie;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.AopTestUtils;
import tools.jackson.databind.json.JsonMapper;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PasswordChangeIT extends AccountIntegrationBase {
    @MockitoSpyBean AuditService audit;
    record Session(Cookie cookie, String csrf) {}
    Session login(String name, String password, int expected) throws Exception {
        var first=mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        var token=JsonMapper.builder().build().readTree(first.getResponse().getContentAsString()).get("token").asString();
        Cookie initial=remember(first.getResponse().getCookie("SESSION"));
        var response=mvc.perform(post("/api/v1/auth/login").cookie(initial).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+name+"\",\"password\":\""+password+"\"}")).andExpect(status().is(expected)).andReturn();
        if(expected!=200) return null;
        Cookie logged=remember(response.getResponse().getCookie("SESSION"));
        var after=mvc.perform(get("/api/v1/auth/csrf").cookie(logged)).andExpect(status().isOk()).andReturn();
        return new Session(logged,JsonMapper.builder().build().readTree(after.getResponse().getContentAsString()).get("token").asString());
    }
    org.springframework.mock.web.MockHttpServletResponse changeResponse(Session session,String old,String next) throws Exception {
        return mvc.perform(post("/api/v1/auth/change-password").cookie(session.cookie()).header("X-CSRF-TOKEN",session.csrf()).contentType("application/json").content("{\"currentPassword\":\""+old+"\",\"newPassword\":\""+next+"\"}")).andReturn().getResponse();
    }
    int change(Session session,String old,String next) throws Exception { return changeResponse(session,old,next).getStatus(); }
    long createTarget() {
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, must_change_password, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, ?, 'Test', 'ACTIVE', TRUE, NOW(6), NOW(6))",prefix+"-target",encoder.encode("Temporary1!"));
        return jdbc.queryForObject("SELECT id FROM users WHERE username=?",Long.class,prefix+"-target");
    }
    @Test void threeCookiesAreRevokedAndTemporaryMailIsRedacted(CapturedOutput output) throws Exception {
        long target=createTarget();
        jdbc.update("INSERT INTO mock_emails (recipient_user_id, recipient_email, subject, body, status, created_at) VALUES (?, 'fixture@example.test', 'subject', 'Temporary1!', 'SIMULATED', NOW(6)), (?, 'other@example.test', 'other', 'keep', 'SIMULATED', NOW(6))",target,adminId);
        var otherBefore=jdbc.queryForMap("SELECT recipient_email,subject,body,status FROM mock_emails WHERE recipient_user_id=?",adminId);
        Session a=login(prefix+"-target","Temporary1!",200), b=login(prefix+"-target","Temporary1!",200), c=login(prefix+"-target","Temporary1!",200);
        mvc.perform(get("/api/v1/households").cookie(a.cookie())).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        assertThat(change(a,"Temporary1!","Changed2!")).isEqualTo(204);
        for(Session s:List.of(a,b,c)) mvc.perform(get("/api/v1/auth/me").cookie(s.cookie())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").cookie(cookie)).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME=?",Long.class,prefix+"-target")).isZero();
        login(prefix+"-target","Temporary1!",401); Session fresh=login(prefix+"-target","Changed2!",200);
        mvc.perform(get("/api/v1/auth/me").cookie(fresh.cookie())).andExpect(status().isOk()).andExpect(jsonPath("$.mustChangePassword").value(false));
        mvc.perform(get("/api/v1/households/me/residents").cookie(fresh.cookie())).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").doesNotExist());
        var row=jdbc.queryForMap("SELECT recipient_email,subject,body,status FROM mock_emails WHERE recipient_user_id=?",target);
        assertThat(row.get("recipient_email")).isEqualTo("fixture@example.test"); assertThat(row.get("subject")).isEqualTo("subject"); assertThat(row.get("body")).isEqualTo("Nội dung đã được ẩn sau khi đổi mật khẩu."); assertThat(row.get("status")).isEqualTo("SIMULATED");
        assertThat(jdbc.queryForMap("SELECT recipient_email,subject,body,status FROM mock_emails WHERE recipient_user_id=?",adminId)).isEqualTo(otherBefore);
        assertThat(jdbc.queryForList("SELECT action FROM audit_log WHERE entity_id=? AND action IN ('PASSWORD_CHANGED','SESSIONS_REVOKED')",String.class,Long.toString(target))).containsExactlyInAnyOrder("PASSWORD_CHANGED","SESSIONS_REVOKED");
        assertThat(jdbc.queryForList("SELECT changes FROM audit_log WHERE entity_id=? AND action IN ('PASSWORD_CHANGED','SESSIONS_REVOKED')",String.class,Long.toString(target))).containsOnlyNulls();
        assertThat(output.getAll()).doesNotContain("Temporary1!","Changed2!");
    }
    @Test void provisionedAccountCanCompleteMandatoryChange() throws Exception {
        String name=prefix+"-new";
        create(body(name, name+"@example.test", "ADMIN", "null")).andExpect(status().isCreated());
        long id=jdbc.queryForObject("SELECT id FROM users WHERE username=?",Long.class,name);
        String message=jdbc.queryForObject("SELECT body FROM mock_emails WHERE recipient_user_id=?",String.class,id);
        String temporary=message.split("Mật khẩu tạm: ")[1].split("\n")[0];
        Session session=login(name,temporary,200);
        mvc.perform(get("/api/v1/households").cookie(session.cookie())).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        assertThat(change(session,temporary,"Changed2!")).isEqualTo(204);
        Session fresh=login(name,"Changed2!",200);
        mvc.perform(get("/api/v1/auth/me").cookie(fresh.cookie())).andExpect(status().isOk()).andExpect(jsonPath("$.mustChangePassword").value(false));
        mvc.perform(get("/api/v1/households").cookie(fresh.cookie())).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT body FROM mock_emails WHERE recipient_user_id=?",String.class,id)).doesNotContain(temporary);
    }
    @Test void auditFailureRollsBackEverything() throws Exception {
        long target=createTarget(); jdbc.update("INSERT INTO mock_emails (recipient_user_id, recipient_email, subject, body, status, created_at) VALUES (?, 'fixture@example.test', 'subject', 'Temporary1!', 'SIMULATED', NOW(6))",target); Session session=login(prefix+"-target","Temporary1!",200);
        long sessions=jdbc.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME=?",Long.class,prefix+"-target");
        String hash=jdbc.queryForObject("SELECT password_hash FROM users WHERE id=?",String.class,target);
        AuditService actual=AopTestUtils.getUltimateTargetObject(audit); doThrow(new IllegalStateException()).when(actual).record(eq(target),eq("SESSIONS_REVOKED"),anyString(),anyString(),isNull());
        assertThat(change(session,"Temporary1!","Changed2!")).isEqualTo(500);
        assertThat(jdbc.queryForObject("SELECT password_hash FROM users WHERE id=?",String.class,target)).isEqualTo(hash);
        assertThat(jdbc.queryForObject("SELECT must_change_password FROM users WHERE id=?",Boolean.class,target)).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME=?",Long.class,prefix+"-target")).isEqualTo(sessions);
        assertThat(jdbc.queryForObject("SELECT body FROM mock_emails WHERE recipient_user_id=?",String.class,target)).isEqualTo("Temporary1!");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE entity_id=? AND action='PASSWORD_CHANGED'",Long.class,Long.toString(target))).isZero();
        mvc.perform(get("/api/v1/auth/me").cookie(session.cookie())).andExpect(status().isOk());
    }
    @Test void concurrentChangesHaveOneWinner() throws Exception {
        createTarget(); Session a=login(prefix+"-target","Temporary1!",200), b=login(prefix+"-target","Temporary1!",200);
        var pool=Executors.newFixedThreadPool(2); var start=new CountDownLatch(1);
        try {var x=pool.submit(()->{start.await();return changeResponse(a,"Temporary1!","Changed2!");}); var y=pool.submit(()->{start.await();return changeResponse(b,"Temporary1!","Changed3!");}); start.countDown(); var rx=x.get(30,TimeUnit.SECONDS); var ry=y.get(30,TimeUnit.SECONDS);
            assertThat(List.of(rx.getStatus(),ry.getStatus()).stream().filter(v->v==204)).hasSize(1);
            var loser=rx.getStatus()==204?ry:rx; assertThat(loser.getStatus()).isIn(400,401); if(loser.getStatus()==400) assertThat(loser.getContentAsString()).contains("CURRENT_PASSWORD_INCORRECT");
            String winner=rx.getStatus()==204?"Changed2!":"Changed3!", lost=rx.getStatus()==204?"Changed3!":"Changed2!";
            login(prefix+"-target",lost,401); login(prefix+"-target",winner,200);
        } finally {start.countDown();pool.shutdownNow();}
    }
    @Test void incorrectOldPasswordCommitsCounterAndLocksNonAdmin(CapturedOutput output) throws Exception {
        long target=createTarget(); Session session=login(prefix+"-target","Temporary1!",200);
        for(int i=1;i<=5;i++){assertThat(change(session,"bad","Changed2!")).isEqualTo(400); if(i<5) assertThat(jdbc.queryForObject("SELECT failed_login_count FROM users WHERE id=?",Integer.class,target)).isEqualTo(i);}
        assertThat(jdbc.queryForObject("SELECT lock_level FROM users WHERE id=?",Integer.class,target)).isEqualTo(1);
        var failures=jdbc.queryForList("SELECT action, changes FROM audit_log WHERE entity_id=? AND action IN ('LOGIN_FAILED','PASSWORD_CHANGE_FAILED')",Long.toString(target)); assertThat(failures).hasSize(5); assertThat(failures.toString()).contains("PASSWORD_CHANGE_FAILED").doesNotContain("LOGIN_FAILED","bad","Changed2!");
        assertThat(change(session,"Temporary1!","Changed2!")).isEqualTo(401);
        Session admin=new Session(cookie,csrf); for(int i=0;i<6;i++) assertThat(change(admin,"wrong","Changed2!")).isEqualTo(400);
        assertThat(jdbc.queryForObject("SELECT failed_login_count + lock_level FROM users WHERE id=?",Integer.class,adminId)).isZero(); assertThat(output.getAll()).doesNotContain("Temporary1!","Changed2!","wrong");
    }
}
