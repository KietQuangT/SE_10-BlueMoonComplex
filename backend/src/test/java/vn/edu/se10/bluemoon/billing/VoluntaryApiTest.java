package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContribution;
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
import vn.edu.se10.bluemoon.billing.api.ResidentCampaignResponse;
import vn.edu.se10.bluemoon.billing.api.VoluntaryController;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

@WebMvcTest(VoluntaryController.class)
@Import(SecurityConfig.class)
class VoluntaryApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean VoluntaryCampaignService campaigns;
    @MockitoBean VoluntaryContributionService contributions;
    @MockitoBean ResidentDonorLookup donors;
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
    void residentViewContainsNoStaffTotalsOrDonorData() throws Exception {
        when(campaigns.residentDetail(7)).thenReturn(new ResidentCampaignResponse(7, "Test", null,
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "PUBLISHED"));
        var resident = authentication(principal("RESIDENT", false));
        mvc.perform(get("/api/v1/voluntary-campaigns/7").with(resident)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.version").doesNotExist())
                .andExpect(jsonPath("$.totalCollected").doesNotExist())
                .andExpect(jsonPath("$.postedCount").doesNotExist());
        mvc.perform(get("/api/v1/voluntary-campaigns/7/contributions").with(resident)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/voluntary-donors?q=Test").with(resident)).andExpect(status().isForbidden());
    }

    @Test
    void enforcesAuthenticationRolesCsrfAndPasswordChange() throws Exception {
        String body = "{\"name\":\"Test\",\"opensOn\":\"2026-11-01\",\"closesOn\":\"2026-11-30\"}";
        mvc.perform(get("/api/v1/voluntary-campaigns/7")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/voluntary-campaigns").with(csrf()).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/voluntary-campaigns").with(authentication(principal("ACCOUNTANT", false))).with(csrf()).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/voluntary-campaigns").with(authentication(principal("ADMIN", false))).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/voluntary-campaigns/7").with(authentication(principal("RESIDENT", true))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
    }
    @Test
    void invalidPageHasStableFilterCode() throws Exception {
        mvc.perform(get("/api/v1/voluntary-campaigns?page=abc").with(authentication(principal("RESIDENT", false))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
    }

    @Test
    void contributionAmountStaysExactAndRetryReturns200() throws Exception {
        String key = "6fbca292-b3b5-44de-bbb8-291b9ee1c213";
        String amount = "9007199254740993";
        var input = ContributionInput.parse(7, 4, amount, LocalDate.of(2026, 11, 1), null, key);
        var contribution = new VoluntaryContribution(7, 4, 5, "Test", "H1", "A1", input.amount(), input.paidOn(),
                null, key, input.fingerprint(), 1, Instant.EPOCH);
        ReflectionTestUtils.setField(contribution, "id", 9L);
        when(contributions.record(any(), eq(1L))).thenReturn(new VoluntaryContributionService.Result(contribution, false),
                new VoluntaryContributionService.Result(contribution, true));
        var auth = authentication(principal("ACCOUNTANT", false));
        String body = "{\"residentId\":4,\"amount\":\"" + amount + "\",\"paidOn\":\"2026-11-01\"}";
        mvc.perform(post("/api/v1/voluntary-campaigns/7/contributions").with(auth).with(csrf()).header("Idempotency-Key", key)
                .contentType("application/json").content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(amount));
        mvc.perform(post("/api/v1/voluntary-campaigns/7/contributions").with(auth).with(csrf()).header("Idempotency-Key", key)
                .contentType("application/json").content(body)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/voluntary-campaigns/7/contributions").with(auth).with(csrf()).header("Idempotency-Key", key)
                .contentType("application/json").content("{\"residentId\":4,\"amount\":100,\"paidOn\":\"2026-11-01\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
        mvc.perform(post("/api/v1/voluntary-campaigns/7/contributions").with(authentication(principal("ADMIN", false))).with(csrf())
                .header("Idempotency-Key", key).contentType("application/json").content(body)).andExpect(status().isForbidden());
    }

}
