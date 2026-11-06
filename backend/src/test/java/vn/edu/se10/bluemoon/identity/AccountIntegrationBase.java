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
abstract class AccountIntegrationBase {
    @Autowired vn.edu.se10.bluemoon.identity.persistence.UserRepository users; @Autowired vn.edu.se10.bluemoon.identity.persistence.RoleRepository roles;
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
}
