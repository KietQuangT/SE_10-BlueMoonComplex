package vn.edu.se10.bluemoon.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.junit.jupiter.api.extension.ExtendWith(org.springframework.boot.test.system.OutputCaptureExtension.class)
class IdentityCookieIT {
    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    final HttpClient client = HttpClient.newHttpClient();
    final List<String> sessionIds = new ArrayList<>();
    String username;
    long userId;

    @BeforeEach void createAccount() {
        username = "s01-cookie-" + UUID.randomUUID();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, ?, 'Test', 'ACTIVE', NOW(6), NOW(6))", username, encoder.encode("test-password-only"));
        userId = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, username);
    }
    @AfterEach void clean() {
        for (String id : sessionIds) jdbc.update("DELETE FROM SPRING_SESSION WHERE SESSION_ID=?", id);
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", userId);
        jdbc.update("DELETE FROM users WHERE id=?", userId);
    }

    @Test void loginRotatesCookieAndLogoutOnlyDeletesItsOwnSession(org.springframework.boot.test.system.CapturedOutput output) throws Exception {
        var bootstrapA = request("GET", "/csrf", null, null, null);
        String oldCookie = cookie(bootstrapA);
        assertThat(bootstrapA.headers().firstValue("set-cookie").orElseThrow()).containsIgnoringCase("HttpOnly").containsIgnoringCase("SameSite=Lax");
        String tokenA = token(bootstrapA);
        var loginA = request("POST", "/login", oldCookie, tokenA, loginBody());
        assertThat(loginA.statusCode()).isEqualTo(200);
        assertThat(JsonMapper.builder().build().readTree(loginA.body()).get("role").asString()).isEqualTo("ACCOUNTANT");
        String newCookie = cookie(loginA);
        assertThat(newCookie).isNotEqualTo(oldCookie);
        assertThat(request("GET", "/me", oldCookie, null, null).statusCode()).isEqualTo(401);
        assertThat(request("GET", "/me", newCookie, null, null).statusCode()).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION WHERE PRINCIPAL_NAME=?", Integer.class, username)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT PRINCIPAL_NAME FROM SPRING_SESSION WHERE SESSION_ID=?", String.class, id(newCookie))).isEqualTo(username);

        var bootstrapB = request("GET", "/csrf", null, null, null);
        String cookieB = cookie(bootstrapB);
        var loginB = request("POST", "/login", cookieB, token(bootstrapB), loginBody());
        assertThat(loginB.statusCode()).isEqualTo(200);
        cookieB = cookie(loginB);
        var refreshed = request("GET", "/csrf", newCookie, null, null);
        assertThat(request("POST", "/logout", newCookie, token(refreshed), "").statusCode()).isEqualTo(204);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION WHERE SESSION_ID=?", Integer.class, id(newCookie))).isZero();
        assertThat(request("GET", "/me", newCookie, null, null).statusCode()).isEqualTo(401);
        assertThat(request("GET", "/me", cookieB, null, null).statusCode()).isEqualTo(200);
        assertThat(output.getAll()).doesNotContain(username, "test-password-only", tokenA, oldCookie, newCookie, cookieB)
                .doesNotContainIgnoringCase("generated security password");
    }

    private String loginBody() { return "{\"username\":\"" + username.toUpperCase(java.util.Locale.ROOT) + "\",\"password\":\"test-password-only\"}"; }
    private String token(HttpResponse<String> response) { return JsonMapper.builder().build().readTree(response.body()).get("token").asString(); }
    private String id(String cookie) { return new String(Base64.getDecoder().decode(cookie.substring(cookie.indexOf('=') + 1)), StandardCharsets.UTF_8); }
    private String cookie(HttpResponse<String> response) {
        String cookie = response.headers().firstValue("set-cookie").orElseThrow().split(";", 2)[0];
        sessionIds.add(id(cookie));
        return cookie;
    }
    private HttpResponse<String> request(String method, String path, String cookie, String token, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/auth" + path));
        if (cookie != null) builder.header("Cookie", cookie);
        if (token != null) builder.header("X-CSRF-TOKEN", token);
        if (body != null) builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
