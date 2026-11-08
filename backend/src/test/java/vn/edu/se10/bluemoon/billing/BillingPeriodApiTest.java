package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.billing.api.BillingPeriodController;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;

@WebMvcTest(BillingPeriodController.class)
@Import(SecurityConfig.class)
class BillingPeriodApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean BillingPeriodService periods;
    @MockitoBean CurrentAccountService currentAccount;

    @BeforeEach
    void refresh() {
        when(currentAccount.refresh(anyLong())).thenAnswer(invocation ->
                (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
    private static UsernamePasswordAuthenticationToken principal(String role, boolean change) {
        return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(1, "test", "Test", role, null, change),
                null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void staffReadsAreDeniedToResidentAndAnonymous() throws Exception {
        for (String path : List.of("/api/v1/billing-periods", "/api/v1/billing-periods/1", "/api/v1/billing-periods/1/submissions", "/api/v1/invoices/1")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).with(authentication(principal("RESIDENT", false)))).andExpect(status().isForbidden());
            mvc.perform(get(path).with(authentication(principal("ACCOUNTANT", true))))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        }
    }

    @Test
    void writeRolesAndCsrfAreEnforcedBeforeService() throws Exception {
        String create = "{\"period\":\"2026-11\",\"dueDate\":\"2026-11-30\"}";
        String issue = "{\"version\":0,\"revision\":1}";
        mvc.perform(post("/api/v1/billing-periods").with(csrf()).contentType("application/json").content(create)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/billing-periods").with(authentication(principal("ADMIN", false))).with(csrf()).contentType("application/json").content(create)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/billing-periods").with(authentication(principal("ACCOUNTANT", false))).contentType("application/json").content(create)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/billing-periods/1/issue").with(authentication(principal("ACCOUNTANT", false))).with(csrf()).contentType("application/json").content(issue)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/billing-periods/1/submit").with(authentication(principal("ADMIN", false))).with(csrf()).contentType("application/json").content("{\"version\":0}")).andExpect(status().isForbidden());
    }
}
