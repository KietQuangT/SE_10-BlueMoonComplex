package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
}
