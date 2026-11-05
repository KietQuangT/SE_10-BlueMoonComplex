package vn.edu.se10.bluemoon.identity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
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
}
