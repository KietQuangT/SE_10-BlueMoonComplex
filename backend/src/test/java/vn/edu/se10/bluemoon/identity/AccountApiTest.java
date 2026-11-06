package vn.edu.se10.bluemoon.identity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.api.*;
import vn.edu.se10.bluemoon.identity.application.*;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean AccountProvisioningService service;
    private static final String BODY = "{\"username\":\"worker\",\"fullName\":\"Test User\",\"email\":\"a@example.test\",\"role\":\"ADMIN\"}";
    private static UsernamePasswordAuthenticationToken principal(String role, boolean change) {
        var user = new AuthenticatedUser(1, "admin", "Admin", role, null, change);
        return UsernamePasswordAuthenticationToken.authenticated(user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
    @Test void accessRequiresAuthenticationCsrfAndAdmin() throws Exception {
        mvc.perform(post("/api/v1/accounts").contentType("application/json").content(BODY)).andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").doesNotExist());
        mvc.perform(post("/api/v1/accounts").with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.detail").doesNotExist());
        mvc.perform(post("/api/v1/accounts").with(csrf().useInvalidToken()).contentType("application/json").content(BODY)).andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").doesNotExist());
        mvc.perform(post("/api/v1/accounts").with(authentication(principal("ADMIN", false))).contentType("application/json").content(BODY)).andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").doesNotExist());
        mvc.perform(post("/api/v1/accounts").with(authentication(principal("ADMIN", false))).with(csrf().useInvalidToken()).contentType("application/json").content(BODY)).andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").doesNotExist());
        for (String role : List.of("ACCOUNTANT", "RESIDENT"))
            mvc.perform(post("/api/v1/accounts").with(authentication(principal(role, false))).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").doesNotExist());
        verifyNoInteractions(service);
    }
    @Test void adminGetsOnlyApprovedFieldsAndExtraFieldsIgnored() throws Exception {
        when(service.create(any(), eq(1L))).thenReturn(new CreatedAccount(7,"worker","Test User","a@example.test","ADMIN",null));
        var result = mvc.perform(post("/api/v1/accounts").with(authentication(principal("ADMIN", false))).with(csrf()).contentType("application/json").content(BODY.substring(0,BODY.length()-1)+",\"password\":\"ignored\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(7)).andExpect(jsonPath("$.emailStatus").value("SIMULATED"))
            .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.passwordHash").doesNotExist()).andExpect(jsonPath("$.temporaryPassword").doesNotExist()).andExpect(jsonPath("$.mustChangePassword").doesNotExist()).andReturn();
        org.assertj.core.api.Assertions.assertThat(tools.jackson.databind.json.JsonMapper.builder().build().readTree(result.getResponse().getContentAsString()).size()).isEqualTo(7);
    }
    @Test void invalidBodiesAreRejectedWithoutCallingService() throws Exception {
        for (String body : List.of("{}", BODY.replace("worker", "ab"), BODY.replace("worker", "!"), BODY.replace("worker", "x".repeat(101)), BODY.replace("Test User", "   "), BODY.replace("Test User", "Test\\nName"), BODY.replace("\"role\":\"ADMIN\"", "\"role\":\"ADMIN\",\"householdId\":0"), BODY.replace("\"role\":\"ADMIN\"", "\"role\":\"ADMIN\",\"householdId\":-1"), BODY.replace("Test User", "x".repeat(151)), BODY.replace("a@example.test", "a@example."), BODY.replace("a@example.test", "a@localhost"), BODY.replace("a@example.test", "aexample.test"), BODY.replace("a@example.test", "a@@example.test"), BODY.replace("a@example.test", "é@example.test"), BODY.replace("ADMIN", "admin"), BODY.replace("ADMIN", "OTHER"), BODY.replace("a@example.test", "x".repeat(250)+"@example.test")))
            mvc.perform(post("/api/v1/accounts").with(authentication(principal("ADMIN", false))).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void conflictCodesDoNotEchoEmail() throws Exception {
        for (String code : List.of("EMAIL_ALREADY_USED", "USERNAME_ALREADY_USED", "ACCOUNT_CONFLICT")) {
            when(service.create(any(), eq(1L))).thenThrow(new ConflictException(code));
            mvc.perform(post("/api/v1/accounts").with(authentication(principal("ADMIN", false))).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(code)).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("a@example.test"))));
        }
    }
    @Test void passwordChangeGuardBlocksUnknownAndAccountRoutes() throws Exception {
        mvc.perform(post("/api/v1/accounts").with(authentication(principal("ADMIN", true))).with(csrf()).contentType("application/json").content(BODY))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(get("/unknown").with(authentication(principal("ADMIN", true))))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(get("/api/v1/auth/csrf").with(authentication(principal("ADMIN", true)))).andExpect(status().isNotFound());
        verifyNoInteractions(service);
    }
}
