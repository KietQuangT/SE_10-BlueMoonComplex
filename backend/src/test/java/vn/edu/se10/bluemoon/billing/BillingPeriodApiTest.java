package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.api.BillingPeriodResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpMethod;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
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
    @Test
    void invalidPageHasStableFilterCode() throws Exception {
        mvc.perform(get("/api/v1/billing-periods?page=abc").with(authentication(principal("ACCOUNTANT", false))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
    }

    @Test
    void accountantCanCreateAndAdminCanApprove() throws Exception {
        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, Instant.EPOCH);
        ReflectionTestUtils.setField(period, "id", 5L);
        when(periods.create("2026-11", LocalDate.of(2026, 11, 30), 1)).thenReturn(period);
        var response = new BillingPeriodResponse(5, "2026-11", LocalDate.of(2026, 11, 30), "DRAFT", 0, 0,
                false, true, List.of("UPDATE", "SUBMIT"), null, null, Instant.EPOCH, null, null, null, null, null, null, 0);
        when(periods.detail(5, "ACCOUNTANT")).thenReturn(response);
        mvc.perform(post("/api/v1/billing-periods").with(authentication(principal("ACCOUNTANT", false))).with(csrf())
                .contentType("application/json").content("{\"period\":\"2026-11\",\"dueDate\":\"2026-11-30\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(5));
        mvc.perform(post("/api/v1/billing-periods/5/issue").with(authentication(principal("ADMIN", false))).with(csrf())
                .contentType("application/json").content("{\"version\":3,\"revision\":2}"))
                .andExpect(status().isOk());
        verify(periods).issue(eq(5L), eq(3L), eq(2L), eq(1L));
    }

    @Test
    void everyBillingRouteRejectsAnonymousAndWrongRole() throws Exception {
        record Route(HttpMethod method, String path, String role) { }
        var routes = List.of(
                new Route(HttpMethod.GET, "/api/v1/billing-periods", "RESIDENT"),
                new Route(HttpMethod.GET, "/api/v1/billing-periods/1", "RESIDENT"),
                new Route(HttpMethod.GET, "/api/v1/billing-periods/1/preview", "RESIDENT"),
                new Route(HttpMethod.GET, "/api/v1/billing-periods/1/submissions", "RESIDENT"),
                new Route(HttpMethod.GET, "/api/v1/billing-periods/1/invoices", "RESIDENT"),
                new Route(HttpMethod.GET, "/api/v1/invoices/1", "RESIDENT"),
                new Route(HttpMethod.POST, "/api/v1/billing-periods", "ADMIN"),
                new Route(HttpMethod.PATCH, "/api/v1/billing-periods/1", "ADMIN"),
                new Route(HttpMethod.POST, "/api/v1/billing-periods/1/submit", "ADMIN"),
                new Route(HttpMethod.POST, "/api/v1/billing-periods/1/withdraw", "ADMIN"),
                new Route(HttpMethod.POST, "/api/v1/billing-periods/1/return", "ACCOUNTANT"),
                new Route(HttpMethod.POST, "/api/v1/billing-periods/1/issue", "ACCOUNTANT"),
                new Route(HttpMethod.POST, "/api/v1/billing-periods/1/cancel", "ACCOUNTANT"));
        for (var route : routes) {
            mvc.perform(request(route.method(), route.path()).with(csrf()).contentType("application/json").content("{}"))
                    .andExpect(status().isUnauthorized());
            mvc.perform(request(route.method(), route.path()).with(authentication(principal(route.role(), false))).with(csrf())
                    .contentType("application/json").content("{}")).andExpect(status().isForbidden());
        }
    }

    @Test
    void inactiveAccountIsRejectedBeforeNewApi() throws Exception {
        org.mockito.Mockito.doReturn(null).when(currentAccount).refresh(anyLong());
        mvc.perform(get("/api/v1/billing-periods").with(authentication(principal("ACCOUNTANT", false))))
                .andExpect(status().isUnauthorized());
    }

}
