package vn.edu.se10.bluemoon.identity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.json.JsonMapper;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.common.web.RequestIdFilter;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.api.AuthController;
import vn.edu.se10.bluemoon.identity.application.LoginAttemptService;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RequestIdFilter.class, AuthApiTest.AdminProbe.class, AuthApiTest.AdminProbeSecurity.class})
@TestPropertySource(properties = "app.cors.allowed-origin=http://localhost:5173")
class AuthApiTest {
    @TestConfiguration
    static class AdminProbeSecurity {
        @Bean @Order(0)
        SecurityFilterChain adminOnlyTestEndpoint(HttpSecurity http) throws Exception {
            return http.securityMatcher("/api/v1/test/admin")
                    .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                    .build();
        }
    }

    @RestController
    static class AdminProbe {
        @GetMapping("/api/v1/test/admin") String adminOnly() { return "ok"; }
    }

    @Autowired MockMvc mvc;
    @MockitoBean LoginAttemptService attempts;

    @Test void csrfBootstrapIsPublicAndReturnsHeaderNameAndToken() throws Exception {
        mvc.perform(get("/api/v1/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test void meAndUnknownAuthPathRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mvc.perform(get("/api/v1/auth/khong-co"))
                .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test void writeAuthRoutesStillRequireCsrf() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("{\"username\":\"a\",\"password\":\"b\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/auth/logout").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test void testOnlyAdminEndpointRejectsResidentAndAccountant() throws Exception {
        mvc.perform(get("/api/v1/test/admin").with(user("resident").roles("RESIDENT"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/test/admin").with(user("accountant").roles("ACCOUNTANT"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/test/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
    }
    @Test void invalidLoginBodiesAreRejected() throws Exception {
        for (String body : new String[] {"{}", "{\"username\":\"x\"}",
                "{\"username\":\"" + "x".repeat(101) + "\",\"password\":\"p\"}"}) {
            mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(attempts);
    }

    @Test void successfulRolesExposeOnlyFiveFields() throws Exception {
        for (String role : new String[] {"ADMIN", "ACCOUNTANT", "RESIDENT"}) {
            when(attempts.login(anyString(), anyString())).thenReturn(new LoginAttemptService.Result(
                    new AuthenticatedUser(1, "canonical", "Test User", role, 42L, false), true));
            var result = mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType("application/json")
                    .content("{\"username\":\"canonical\",\"password\":\"secret\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.role").value(role)).andReturn();
            var body = JsonMapper.builder().build().readTree(result.getResponse().getContentAsString());
            assertThat(body.size()).isEqualTo(5);
            for (String field : new String[] {"passwordHash", "email", "status", "failedLoginCount", "lockedUntil"})
                assertThat(body.has(field)).isFalse();
        }
    }

    @Test void csrfRejectsMissingAndInvalidTokensWithoutCallingLogin() throws Exception {
        for (var request : new org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder[] {
                post("/api/v1/auth/login"), post("/api/v1/auth/login").with(csrf().useInvalidToken()),
                post("/api/v1/auth/logout").with(user("admin")),
                post("/api/v1/auth/logout").with(user("admin")).with(csrf().useInvalidToken())}) {
            mvc.perform(request.contentType("application/json").content("{\"username\":\"a\",\"password\":\"b\"}"))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(attempts);
    }

    @Test void corsPreflightAllowsConfiguredOriginAndRejectsOthers() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "X-CSRF-TOKEN"))
                .andExpect(status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/v1/auth/login")
                        .header("Origin", "https://untrusted.example").header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "X-CSRF-TOKEN"))
                .andExpect(status().isForbidden());
    }

    @Test void csrfTokenRotatesOnLoginAndLogout() throws Exception {
        when(attempts.login(anyString(), anyString())).thenReturn(new LoginAttemptService.Result(
                new AuthenticatedUser(1, "canonical", "Test", "ADMIN", null, false), true));
        var initial = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
        var session = (MockHttpSession) initial.getRequest().getSession(false);
        String before = JsonMapper.builder().build().readTree(initial.getResponse().getContentAsString()).get("token").asString();
        mvc.perform(post("/api/v1/auth/login").session(session).header("X-CSRF-TOKEN", before)
                .contentType("application/json").content("{\"username\":\"a\",\"password\":\"b\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/logout").session(session).header("X-CSRF-TOKEN", before))
                .andExpect(status().isForbidden());
        var refreshed = mvc.perform(get("/api/v1/auth/csrf").session(session)).andReturn();
        String after = JsonMapper.builder().build().readTree(refreshed.getResponse().getContentAsString()).get("token").asString();
        mvc.perform(post("/api/v1/auth/logout").session(session).header("X-CSRF-TOKEN", after)).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/login").header("X-CSRF-TOKEN", after).contentType("application/json")
                .content("{\"username\":\"a\",\"password\":\"b\"}")).andExpect(status().isForbidden());
        var fresh = mvc.perform(get("/api/v1/auth/csrf")).andReturn();
        String token = JsonMapper.builder().build().readTree(fresh.getResponse().getContentAsString()).get("token").asString();
        mvc.perform(post("/api/v1/auth/login").session((MockHttpSession) fresh.getRequest().getSession(false))
                .header("X-CSRF-TOKEN", token).contentType("application/json")
                .content("{\"username\":\"a\",\"password\":\"b\"}")).andExpect(status().isOk());
    }

}
