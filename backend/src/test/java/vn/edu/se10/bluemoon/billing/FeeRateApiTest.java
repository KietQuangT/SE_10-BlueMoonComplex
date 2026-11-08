package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
import vn.edu.se10.bluemoon.billing.api.*;
import vn.edu.se10.bluemoon.billing.application.*;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;

@WebMvcTest({FeeTypeController.class, FeeRateController.class, FeePreviewController.class})
@Import(SecurityConfig.class)
class FeeRateApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean FeeRateService rates;
    @MockitoBean FeeCalculationService calculation;
    @MockitoBean CurrentAccountService currentAccount;
    private static final String BODY = "{\"feeTypeId\":1,\"unitPrice\":7000,\"effectiveFrom\":\"2026-12-01\"}";

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
    void deniesUnauthenticatedResidentAdminWritesAndCsrf() throws Exception {
        for (String path : List.of("/api/v1/fee-types", "/api/v1/fee-rates", "/api/v1/fee-preview?householdId=1&period=2026-11")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).with(authentication(principal("RESIDENT", false)))).andExpect(status().isForbidden());
            mvc.perform(get(path).with(authentication(principal("ACCOUNTANT", true))))
                    .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        }
        mvc.perform(post("/api/v1/fee-rates").with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/v1/fee-rates/1").with(csrf()).contentType("application/json").content("{\"unitPrice\":1}")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/fee-rates/1").with(csrf())).andExpect(status().isUnauthorized());
        var restricted = authentication(principal("ACCOUNTANT", true));
        mvc.perform(post("/api/v1/fee-rates").with(restricted).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(patch("/api/v1/fee-rates/1").with(restricted).with(csrf()).contentType("application/json").content("{\"unitPrice\":1}")).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(delete("/api/v1/fee-rates/1").with(restricted).with(csrf())).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        for (String role : List.of("ADMIN", "RESIDENT")) {
            var auth = authentication(principal(role, false));
            mvc.perform(post("/api/v1/fee-rates").with(auth).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
            mvc.perform(patch("/api/v1/fee-rates/1").with(auth).with(csrf()).contentType("application/json").content("{\"unitPrice\":1}")).andExpect(status().isForbidden());
            mvc.perform(delete("/api/v1/fee-rates/1").with(auth).with(csrf())).andExpect(status().isForbidden());
        }
        var accountant = authentication(principal("ACCOUNTANT", false));
        mvc.perform(post("/api/v1/fee-rates").with(accountant).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/fee-rates").with(accountant).with(csrf().useInvalidToken()).contentType("application/json").content(BODY)).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/fee-rates/1").with(accountant).contentType("application/json").content("{\"unitPrice\":1}")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/fee-rates/1").with(accountant).with(csrf().useInvalidToken()).contentType("application/json").content("{\"unitPrice\":1}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/fee-rates/1").with(accountant)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/fee-rates/1").with(accountant).with(csrf().useInvalidToken())).andExpect(status().isForbidden());
        verifyNoInteractions(rates, calculation);
    }

    @Test
    void validatesPriceAndRequiredFields() throws Exception {
        var auth = authentication(principal("ACCOUNTANT", false));
        for (String body : List.of("{}", BODY.replace("7000", "0"), BODY.replace("7000", "-1"),
                BODY.replace("7000", "1.001"), BODY.replace("7000", "10000000000"))) {
            mvc.perform(post("/api/v1/fee-rates").with(auth).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
        verifyNoInteractions(rates, calculation);
    }

    @Test
    void permitsReadsAndAccountantWrites() throws Exception {
        for (String role : List.of("ADMIN", "ACCOUNTANT")) {
            var auth = authentication(principal(role, false));
            mvc.perform(get("/api/v1/fee-types").with(auth)).andExpect(status().isOk());
            mvc.perform(get("/api/v1/fee-rates").with(auth)).andExpect(status().isOk());
            mvc.perform(get("/api/v1/fee-preview?householdId=1&period=2026-11").with(auth)).andExpect(status().isOk());
        }
        var auth = authentication(principal("ACCOUNTANT", false));
        mvc.perform(post("/api/v1/fee-rates").with(auth).with(csrf()).contentType("application/json").content(BODY)).andExpect(status().isCreated());
        mvc.perform(patch("/api/v1/fee-rates/1").with(auth).with(csrf()).contentType("application/json").content("{\"unitPrice\":1}")).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/fee-rates/1").with(auth).with(csrf())).andExpect(status().isNoContent());
    }
}
