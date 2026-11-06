package vn.edu.se10.bluemoon.identity;

import java.util.*;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.junit.jupiter.api.extension.ExtendWith(org.springframework.boot.test.system.OutputCaptureExtension.class)
@org.springframework.test.context.TestPropertySource(properties = "logging.level.org.hibernate.SQL=DEBUG")
class AccountProvisioningIT {
    @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc; @Autowired PasswordEncoder encoder; @Autowired org.flywaydb.core.Flyway flyway;
    String prefix = "s02-" + UUID.randomUUID().toString().substring(0, 8); long adminId; Cookie cookie; String csrf;
    final List<String> sessionIds = new ArrayList<>();
    @BeforeEach void setup() throws Exception {
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ADMIN'), ?, ?, 'Admin', 'ACTIVE', NOW(6), NOW(6))", prefix, encoder.encode("test-only-pass"));
        adminId=jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, prefix);
        var initial=mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        Cookie first=remember(initial.getResponse().getCookie("SESSION"));
        String token=JsonMapper.builder().build().readTree(initial.getResponse().getContentAsString()).get("token").asString();
        var login=mvc.perform(post("/api/v1/auth/login").cookie(first).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+prefix+"\",\"password\":\"test-only-pass\"}"))
                .andExpect(status().isOk()).andReturn();
        cookie=remember(login.getResponse().getCookie("SESSION"));
        var after=mvc.perform(get("/api/v1/auth/csrf").cookie(cookie)).andExpect(status().isOk()).andReturn();
        csrf=JsonMapper.builder().build().readTree(after.getResponse().getContentAsString()).get("token").asString();
    }
    Cookie remember(Cookie c) { assertThat(c).isNotNull(); sessionIds.add(new String(Base64.getDecoder().decode(c.getValue()), StandardCharsets.UTF_8)); return c; }
    @AfterEach void cleanup() {
        for (String id : sessionIds) jdbc.update("DELETE FROM SPRING_SESSION WHERE SESSION_ID=?", id);
        jdbc.update("DELETE FROM mock_emails WHERE recipient_user_id IN (SELECT id FROM users WHERE username LIKE ?)", prefix+"%");
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id IN (SELECT id FROM users WHERE username LIKE ?) OR (entity_type='User' AND entity_id IN (SELECT CAST(id AS CHAR) FROM users WHERE username LIKE ?))",prefix+"%",prefix+"%");
        jdbc.update("DELETE FROM users WHERE username LIKE ?",prefix+"%");
    }
    String body(String name, String email, String role, String household) {
        return "{\"username\":\""+name+"\",\"fullName\":\"Người Kiểm Thử\",\"email\":\""+email+"\",\"role\":\""+role+"\",\"householdId\":"+household+"}";
    }
    org.springframework.test.web.servlet.ResultActions create(String body) throws Exception {
        return mvc.perform(post("/api/v1/accounts").cookie(cookie).header("X-CSRF-TOKEN", csrf).contentType("application/json").content(body));
    }
    @Test void createsAccountEmailAndAuditTogether(org.springframework.boot.test.system.CapturedOutput output) throws Exception {
        String email=prefix+"@example.test";
        var result=create(body(prefix+"-new", "  "+email.toUpperCase(Locale.ROOT)+"  ","ACCOUNTANT","null")).andExpect(status().isCreated()).andExpect(jsonPath("$.email").value(email)).andReturn();
        long id=JsonMapper.builder().build().readTree(result.getResponse().getContentAsString()).get("id").asLong();
        var user=jdbc.queryForMap("SELECT email,password_hash,status,must_change_password FROM users WHERE id=?",id);
        assertThat(user).containsEntry("email",email).containsEntry("status","ACTIVE");
        assertThat(jdbc.queryForObject("SELECT must_change_password FROM users WHERE id=?", Boolean.class, id)).isTrue();
        var mail=jdbc.queryForMap("SELECT recipient_email,body,status FROM mock_emails WHERE recipient_user_id=?",id);
        assertThat(mail).containsEntry("recipient_email",email).containsEntry("status","SIMULATED");
        String message=(String)mail.get("body");
        String password=message.lines().filter(line -> line.startsWith("Mật khẩu tạm: ")).findFirst().orElseThrow().substring("Mật khẩu tạm: ".length());
        assertThat(encoder.matches(password,(String)user.get("password_hash"))).isTrue();
        assertThat((String)user.get("password_hash")).doesNotContain(password);
        assertThat(result.getResponse().getContentAsString()).doesNotContain(password);
        assertThat(output.getAll()).doesNotContain(password);
        assertThat(jdbc.queryForObject("SELECT changes FROM audit_log WHERE actor_user_id=? AND action='ACCOUNT_CREATED' AND entity_id=?",String.class,adminId,Long.toString(id))).doesNotContain(password,email,prefix+"-new");
        for (byte[] attribute : jdbc.queryForList("SELECT a.ATTRIBUTE_BYTES FROM SPRING_SESSION_ATTRIBUTES a JOIN SPRING_SESSION s ON s.PRIMARY_ID=a.SESSION_PRIMARY_ID WHERE s.PRINCIPAL_NAME=?",byte[].class,prefix))
            assertThat(new String(attribute, StandardCharsets.ISO_8859_1)).doesNotContain(password);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='ACCOUNT_CREATED' AND entity_id=?",Integer.class,adminId,Long.toString(id))).isEqualTo(1);
        var changes=JsonMapper.builder().build().readTree(jdbc.queryForObject("SELECT changes FROM audit_log WHERE actor_user_id=? AND action='ACCOUNT_CREATED' AND entity_id=?",String.class,adminId,Long.toString(id)));
        assertThat(changes.size()).isEqualTo(1); assertThat(changes.get("role").asString()).isEqualTo("ACCOUNTANT");
    }
    @Test void createsAllRolesAndResidentMustHaveActiveHousehold() throws Exception {
        String household=prefix+"-house";
        jdbc.update("INSERT INTO households (household_code,apartment_code,area_m2,status,created_at,updated_at) VALUES (?,?,50,'ACTIVE',NOW(6),NOW(6))",household,household);
        long householdId=jdbc.queryForObject("SELECT id FROM households WHERE household_code=?",Long.class,household);
        try {
            for (String role : List.of("ADMIN","ACCOUNTANT","RESIDENT")) {
                String username=prefix+"-"+role.toLowerCase(Locale.ROOT);
                String email=username+"@example.test";
                create(body(username,email,role,role.equals("RESIDENT") ? Long.toString(householdId) : "null"))
                        .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value(role));
                assertThat(jdbc.queryForObject("SELECT r.code FROM users u JOIN roles r ON r.id=u.role_id WHERE u.username=?",String.class,username)).isEqualTo(role);
                var stored=jdbc.queryForMap("SELECT id,email,status,household_id,password_hash FROM users WHERE username=?",username);
                long storedId=((Number)stored.get("id")).longValue();
                assertThat(stored).containsEntry("email",email).containsEntry("status","ACTIVE");
                assertThat(jdbc.queryForObject("SELECT must_change_password FROM users WHERE id=?",Boolean.class,storedId)).isTrue();
                if (role.equals("RESIDENT")) assertThat(((Number)stored.get("household_id")).longValue()).isEqualTo(householdId);
                else assertThat(stored.get("household_id")).isNull();
                String body=jdbc.queryForObject("SELECT body FROM mock_emails WHERE recipient_user_id=?",String.class,storedId);
                String password=body.lines().filter(line -> line.startsWith("Mật khẩu tạm: ")).findFirst().orElseThrow().substring("Mật khẩu tạm: ".length());
                assertThat(encoder.matches(password,(String)stored.get("password_hash"))).isTrue();
                if (role.equals("RESIDENT")) {
                    String changes=jdbc.queryForObject("SELECT a.changes FROM audit_log a JOIN users u ON a.entity_id=CAST(u.id AS CHAR) WHERE a.action='ACCOUNT_CREATED' AND u.username=?",String.class,username);
                    var json=JsonMapper.builder().build().readTree(changes);
                    assertThat(json.size()).isEqualTo(2); assertThat(json.get("householdId").asString()).isEqualTo(Long.toString(householdId));
                }
            }
            create(body(prefix+"-bad",prefix+"bad@example.test","RESIDENT",Long.toString(Long.MAX_VALUE)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("HOUSEHOLD_INVALID"));
            jdbc.update("UPDATE households SET status='INACTIVE' WHERE id=?",householdId);
            create(body(prefix+"-bad",prefix+"bad@example.test","RESIDENT",Long.toString(householdId)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("HOUSEHOLD_INVALID"));
        } finally {
            jdbc.update("UPDATE users SET household_id=NULL WHERE username LIKE ?",prefix+"%");
            jdbc.update("DELETE FROM households WHERE id=?",householdId);
        }
    }
    @Test void mockEmailsBelongToTheirIntendedRecipients() throws Exception {
        for (String suffix : List.of("a","b"))
            create(body(prefix+"-"+suffix,prefix+suffix+"@example.test","ACCOUNTANT","null")).andExpect(status().isCreated());
        String[] passwords=new String[2]; int i=0;
        for (String suffix : List.of("a","b")) {
            var row=jdbc.queryForMap("SELECT m.recipient_email,m.body,m.status,u.username,u.password_hash FROM mock_emails m JOIN users u ON u.id=m.recipient_user_id WHERE u.username=?",prefix+"-"+suffix);
            assertThat(row).containsEntry("recipient_email",prefix+suffix+"@example.test").containsEntry("status","SIMULATED");
            String mail=(String)row.get("body");
            assertThat(mail).contains(prefix+"-"+suffix);
            passwords[i++]=mail.lines().filter(line -> line.startsWith("Mật khẩu tạm: ")).findFirst().orElseThrow().substring("Mật khẩu tạm: ".length());
            assertThat(encoder.matches(passwords[i-1],(String)row.get("password_hash"))).isTrue();
        }
        assertThat(encoder.matches(passwords[0],jdbc.queryForObject("SELECT password_hash FROM users WHERE username=?",String.class,prefix+"-b"))).isFalse();
        var initial=mvc.perform(get("/api/v1/auth/csrf")).andReturn();
        Cookie initialCookie=remember(initial.getResponse().getCookie("SESSION"));
        String token=JsonMapper.builder().build().readTree(initial.getResponse().getContentAsString()).get("token").asString();
        mvc.perform(post("/api/v1/auth/login").cookie(initialCookie).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+prefix+"-b\",\"password\":\""+passwords[0]+"\"}")).andExpect(status().isUnauthorized());
        var valid=mvc.perform(post("/api/v1/auth/login").cookie(initialCookie).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+prefix+"-a\",\"password\":\""+passwords[0]+"\"}")).andExpect(status().isOk()).andReturn();
        remember(valid.getResponse().getCookie("SESSION"));
    }
    @Test void logoutInvalidatesTheAdminCookie() throws Exception {
        mvc.perform(post("/api/v1/auth/logout").cookie(cookie).header("X-CSRF-TOKEN",csrf)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/auth/me").cookie(cookie)).andExpect(status().isUnauthorized());
    }
    @Test void migrationDefaultAndConstraintsAreEnforced() {
        flyway.validate();
        org.assertj.core.api.Assertions.assertThat(jdbc.queryForObject("SELECT must_change_password FROM users WHERE id=?",Boolean.class,adminId)).isFalse();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbc.update("INSERT INTO mock_emails (recipient_user_id,recipient_email,subject,body,status,created_at) VALUES (?,?, 'test','test','OTHER',NOW(6))",adminId,prefix+"@example.test")).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbc.update("INSERT INTO mock_emails (recipient_user_id,recipient_email,subject,body,status,created_at) VALUES (?,?, 'test','test','SIMULATED',NOW(6))",Long.MAX_VALUE,prefix+"@example.test")).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
    @Test void nonAdminRealSessionsCannotCreateAccounts() throws Exception {
        String household=prefix+"-house";
        jdbc.update("INSERT INTO households (household_code,apartment_code,area_m2,status,created_at,updated_at) VALUES (?,?,50,'ACTIVE',NOW(6),NOW(6))",household,household);
        long householdId=jdbc.queryForObject("SELECT id FROM households WHERE household_code=?",Long.class,household);
        try {
            for (String role : List.of("ACCOUNTANT", "RESIDENT")) {
                String username=prefix+"-"+role.toLowerCase(Locale.ROOT);
                jdbc.update("INSERT INTO users (role_id, username,password_hash,full_name,status,household_id,created_at,updated_at) VALUES ((SELECT id FROM roles WHERE code=?),?,?,'Test','ACTIVE',?,NOW(6),NOW(6))",role,username,encoder.encode("test-only-pass"),role.equals("RESIDENT") ? householdId : null);
                var initial=mvc.perform(get("/api/v1/auth/csrf")).andReturn();
                Cookie first=remember(initial.getResponse().getCookie("SESSION"));
                String token=JsonMapper.builder().build().readTree(initial.getResponse().getContentAsString()).get("token").asString();
                var login=mvc.perform(post("/api/v1/auth/login").cookie(first).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+username+"\",\"password\":\"test-only-pass\"}"))
                        .andExpect(status().isOk()).andReturn();
                Cookie userCookie=remember(login.getResponse().getCookie("SESSION"));
                var fresh=mvc.perform(get("/api/v1/auth/csrf").cookie(userCookie)).andReturn();
                String freshToken=JsonMapper.builder().build().readTree(fresh.getResponse().getContentAsString()).get("token").asString();
                mvc.perform(post("/api/v1/accounts").cookie(userCookie).header("X-CSRF-TOKEN",freshToken).contentType("application/json").content(body(prefix+"-forbidden",prefix+"forbidden@example.test","ADMIN","null"))).andExpect(status().isForbidden());
            }
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username=?",Integer.class,prefix+"-forbidden")).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mock_emails WHERE recipient_email=?",Integer.class,prefix+"forbidden@example.test")).isZero();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE action='ACCOUNT_CREATED' AND actor_user_id IN (SELECT id FROM users WHERE username IN (?,?))",Integer.class,prefix+"-accountant",prefix+"-resident")).isZero();
        } finally {
            jdbc.update("UPDATE users SET household_id=NULL WHERE username LIKE ?",prefix+"%");
            jdbc.update("DELETE FROM households WHERE id=?",householdId);
        }
    }
    @Test void concurrentEmailCreationHasOneWinnerAndDoesNotLogEmail(org.springframework.boot.test.system.CapturedOutput output) throws Exception {
        String email=prefix+"race@example.test";
        var gate=new java.util.concurrent.CountDownLatch(1);
        var ready=new java.util.concurrent.CountDownLatch(8);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(8);
        var futures=new ArrayList<java.util.concurrent.Future<Integer>>();
        try {
            for (int i=0; i<8; i++) {
                int n=i;
                futures.add(pool.submit(() -> {
                    ready.countDown(); gate.await();
                    return create(body(prefix+"-race-"+n,email,"ADMIN","null")).andReturn().getResponse().getStatus();
                }));
            }
            assertThat(ready.await(20,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            gate.countDown();
            var statuses=new ArrayList<Integer>();
            for (var future:futures) statuses.add(future.get(30,java.util.concurrent.TimeUnit.SECONDS));
            assertThat(statuses).containsOnly(201,409);
            assertThat(statuses.stream().filter(status -> status==201).count()).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email=?",Integer.class,email)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mock_emails WHERE recipient_email=?",Integer.class,email)).isEqualTo(1);
            assertThat(output.getAll()).doesNotContain(email);
        } finally { gate.countDown(); pool.shutdownNow(); }
    }
    @Test void sessionRemainsBlockedUntilUserLogsInAgain() throws Exception {
        String username=prefix+"-new";
        create(body(username,prefix+"new@example.test","ADMIN","null")).andExpect(status().isCreated());
        String mail=jdbc.queryForObject("SELECT body FROM mock_emails WHERE recipient_email=?",String.class,prefix+"new@example.test");
        String password=mail.lines().filter(line -> line.startsWith("Mật khẩu tạm: ")).findFirst().orElseThrow().substring("Mật khẩu tạm: ".length());
        var initial=mvc.perform(get("/api/v1/auth/csrf")).andReturn();
        Cookie first=remember(initial.getResponse().getCookie("SESSION"));
        String token=JsonMapper.builder().build().readTree(initial.getResponse().getContentAsString()).get("token").asString();
        var logged=mvc.perform(post("/api/v1/auth/login").cookie(first).header("X-CSRF-TOKEN",token).contentType("application/json").content("{\"username\":\""+username+"\",\"password\":\""+password+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mustChangePassword").value(true)).andReturn();
        Cookie newCookie=remember(logged.getResponse().getCookie("SESSION"));
        mvc.perform(get("/api/v1/auth/me").cookie(newCookie)).andExpect(status().isOk()).andExpect(jsonPath("$.mustChangePassword").value(true));
        mvc.perform(get("/unknown").cookie(newCookie)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        jdbc.update("UPDATE users SET must_change_password=0 WHERE username=?",username);
        mvc.perform(get("/unknown").cookie(newCookie)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        var fresh=mvc.perform(get("/api/v1/auth/csrf").cookie(newCookie)).andExpect(status().isOk()).andReturn();
        String currentToken=JsonMapper.builder().build().readTree(fresh.getResponse().getContentAsString()).get("token").asString();
        mvc.perform(post("/api/v1/accounts").cookie(newCookie).header("X-CSRF-TOKEN",currentToken).contentType("application/json").content(body(prefix+"-blocked",prefix+"blocked@example.test","ADMIN","null")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(post("/api/v1/auth/logout").cookie(newCookie).header("X-CSRF-TOKEN",currentToken)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/auth/me").cookie(newCookie)).andExpect(status().isUnauthorized());
        fresh=mvc.perform(get("/api/v1/auth/csrf")).andExpect(status().isOk()).andReturn();
        newCookie=remember(fresh.getResponse().getCookie("SESSION"));
        String nextToken=JsonMapper.builder().build().readTree(fresh.getResponse().getContentAsString()).get("token").asString();
        var relogin=mvc.perform(post("/api/v1/auth/login").cookie(newCookie).header("X-CSRF-TOKEN",nextToken).contentType("application/json").content("{\"username\":\""+username+"\",\"password\":\""+password+"\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mustChangePassword").value(false)).andReturn();
        remember(relogin.getResponse().getCookie("SESSION"));
    }
    @Test void rejectsDuplicateEmailAndInvalidHouseholdWithoutExtraRows() throws Exception {
        String email=prefix+"@example.test";
        create(body(prefix+"-one",email,"ADMIN","null")).andExpect(status().isCreated());
        for (String duplicate : List.of(email, email.toUpperCase(Locale.ROOT), "  "+email.toUpperCase(Locale.ROOT)+"  "))
            create(body(prefix+"-two",duplicate,"ADMIN","null")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"));
        create(body((prefix+"-one").toUpperCase(Locale.ROOT),prefix+"other@example.test","ADMIN","null")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("USERNAME_ALREADY_USED"));
        create(body(prefix+"-three",prefix+"3@example.test","RESIDENT","null")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("HOUSEHOLD_REQUIRED"));
        create(body(prefix+"-four",prefix+"4@example.test","ACCOUNTANT","42")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("HOUSEHOLD_NOT_ALLOWED"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username LIKE ?",Integer.class,prefix+"%" )).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='ACCOUNT_CREATED'",Integer.class,adminId)).isEqualTo(1);
    }
}
