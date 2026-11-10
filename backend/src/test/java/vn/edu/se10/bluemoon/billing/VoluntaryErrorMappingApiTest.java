package vn.edu.se10.bluemoon.billing;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.billing.api.ResidentCampaignResponse;
import vn.edu.se10.bluemoon.billing.api.VoluntaryController;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

/**
 * Bằng chứng tự động cho ánh xạ lỗi HTTP và phân quyền của API đóng góp tự nguyện
 * (VOL-01.2, 02.1, 02.2, 03.1, 04.3, 05.1, 06.1, 07.1). Service là đối tượng giả, nên các test
 * này chứng minh tầng controller, bộ lọc bảo mật và bộ xử lý lỗi, không chứng minh logic nghiệp vụ.
 */
@WebMvcTest(VoluntaryController.class)
@Import(SecurityConfig.class)
class VoluntaryErrorMappingApiTest {
    private static final String KEY = "6fbca292-b3b5-44de-bbb8-291b9ee1c213";
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

    private static UsernamePasswordAuthenticationToken as(String role) {
        return as(role, false);
    }

    private static UsernamePasswordAuthenticationToken as(String role, boolean mustChange) {
        return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(1, "test", "Test", role, null, mustChange),
                null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    // ---------------------------------------------------------------- VOL-01.2
    @Test
    void unparsableOrMissingCampaignDatesAreHttp400InvalidDateBeforeTheService() throws Exception {
        for (String opensOn : List.of("\"2026-13-01\"", "\"2026-02-30\"", "\"abc\"", "\"\"", "null", "\"0999-01-01\"")) {
            mvc.perform(post("/api/v1/voluntary-campaigns").with(authentication(as("ADMIN"))).with(csrf())
                    .contentType("application/json")
                    .content("{\"name\":\"Quy\",\"opensOn\":" + opensOn + ",\"closesOn\":\"2026-11-30\"}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DATE"));
        }
        verify(campaigns, never()).create(anyString(), any(), any(), any(), anyLong());
    }

    @Test
    void campaignValidationAndDateErrorsFromTheServiceAreHttp400() throws Exception {
        when(campaigns.create(anyString(), any(), any(), any(), anyLong()))
                .thenThrow(new BadRequestException("VALIDATION"), new BadRequestException("INVALID_DATE"));
        String body = "{\"name\":\"   \",\"opensOn\":\"2026-11-10\",\"closesOn\":\"2026-11-30\"}";
        mvc.perform(post("/api/v1/voluntary-campaigns").with(authentication(as("ADMIN"))).with(csrf())
                .contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        mvc.perform(post("/api/v1/voluntary-campaigns").with(authentication(as("ADMIN"))).with(csrf())
                .contentType("application/json").content(body.replace("   ", "Quy").replace("2026-11-30", "2026-11-09")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DATE"));
    }

    @Test
    void staleVersionIsHttp409AndUnknownCampaignIsHttp404() throws Exception {
        when(campaigns.update(anyLong(), anyLong(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new ConflictException("STALE_VERSION"), new NotFoundException("CAMPAIGN_NOT_FOUND"));
        String body = "{\"name\":\"Quy\",\"opensOn\":\"2026-11-01\",\"closesOn\":\"2026-11-30\",\"version\":0}";
        mvc.perform(patch("/api/v1/voluntary-campaigns/701", body))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("STALE_VERSION"));
        mvc.perform(patch("/api/v1/voluntary-campaigns/999999", body))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    void missingVersionOnWritesIsHttp400Validation() throws Exception {
        mvc.perform(patch("/api/v1/voluntary-campaigns/701", "{\"name\":\"Quy\",\"opensOn\":\"2026-11-01\",\"closesOn\":\"2026-11-30\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        for (String action : List.of("publish", "close")) {
            mvc.perform(post("/api/v1/voluntary-campaigns/701/" + action).with(authentication(as("ADMIN"))).with(csrf())
                    .contentType("application/json").content("{}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        }
    }

    // ---------------------------------------------------------------- VOL-02.1
    @Test
    void invalidStateOnPublishCloseAndEditIsHttp409() throws Exception {
        when(campaigns.publish(anyLong(), anyLong(), anyLong())).thenThrow(new ConflictException("INVALID_STATE"));
        when(campaigns.close(anyLong(), anyLong(), anyLong())).thenThrow(new ConflictException("INVALID_STATE"));
        when(campaigns.update(anyLong(), anyLong(), any(), any(), any(), any(), anyLong())).thenThrow(new ConflictException("INVALID_STATE"));
        for (String action : List.of("publish", "close")) {
            mvc.perform(post("/api/v1/voluntary-campaigns/702/" + action).with(authentication(as("ADMIN"))).with(csrf())
                    .contentType("application/json").content("{\"version\":0}"))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("INVALID_STATE"));
        }
        mvc.perform(patch("/api/v1/voluntary-campaigns/701", "{\"name\":\"Quy\",\"opensOn\":\"2026-11-01\",\"closesOn\":\"2026-11-30\",\"version\":2}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("INVALID_STATE"));
    }

    // ---------------------------------------------------------------- VOL-02.2
    @Test
    void residentDetailHasExactlySixFieldsAndDraftIsHttp404() throws Exception {
        when(campaigns.residentDetail(712)).thenReturn(new ResidentCampaignResponse(712, "Dang mo", "Mo ta",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "PUBLISHED"));
        when(campaigns.residentDetail(711)).thenThrow(new NotFoundException("CAMPAIGN_NOT_FOUND"));
        mvc.perform(get("/api/v1/voluntary-campaigns/712").with(authentication(as("RESIDENT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.*").value(hasSize(6)))
                .andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.name").exists()).andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.opensOn").exists()).andExpect(jsonPath("$.closesOn").exists()).andExpect(jsonPath("$.status").exists());
        mvc.perform(get("/api/v1/voluntary-campaigns/711").with(authentication(as("RESIDENT"))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("CAMPAIGN_NOT_FOUND"));
    }

    @Test
    void residentListDtoHasExactlySixFieldsPerItem() throws Exception {
        var item = new ResidentCampaignResponse(713, "Qua han", null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7), "CLOSED");
        when(campaigns.listResident("CLOSED", 0, 20)).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));
        mvc.perform(get("/api/v1/voluntary-campaigns?status=CLOSED&page=0&size=20").with(authentication(as("RESIDENT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].*").value(hasSize(6)))
                .andExpect(jsonPath("$.content[0].status").value("CLOSED"));
        when(campaigns.listResident("BOGUS", 0, 20)).thenThrow(new BadRequestException("INVALID_FILTER"));
        mvc.perform(get("/api/v1/voluntary-campaigns?status=BOGUS").with(authentication(as("RESIDENT"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
    }

    // ---------------------------------------------------------------- VOL-03.1
    @Test
    void donorSearchReturnsExactlyFourFieldsAndShortOrWildcardQueriesAreHttp400() throws Exception {
        when(donors.search(anyString(), anyInt(), anyInt())).thenAnswer(call -> {
            String q = call.getArgument(0);
            if (q.strip().codePointCount(0, q.strip().length()) < 2) throw new BadRequestException("INVALID_FILTER");
            return new PageImpl<>(List.of(new ResidentDonorLookup.Donor(103, 13, "Nguyen Van C", "H3", "A3")), PageRequest.of(0, 20), 1);
        });
        mvc.perform(get("/api/v1/voluntary-donors?q=Nguyen&page=0&size=20").with(authentication(as("ACCOUNTANT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].*").value(hasSize(4)))
                .andExpect(jsonPath("$.content[0].id").value(103)).andExpect(jsonPath("$.content[0].fullName").value("Nguyen Van C"))
                .andExpect(jsonPath("$.content[0].householdCode").value("H3")).andExpect(jsonPath("$.content[0].apartmentCode").value("A3"));
        for (String q : List.of("N", "%", "   ")) {
            mvc.perform(get("/api/v1/voluntary-donors").param("q", q).with(authentication(as("ACCOUNTANT"))))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
        }
    }

    // ---------------------------------------------------------------- VOL-04.3
    @Test
    void invalidAmountsAreHttp400InvalidAmountBeforeTheService() throws Exception {
        for (String amount : List.of("100000", "\"0\"", "\"-500\"", "\"100.50\"", "\"1000000000000000000\"", "\"\"", "null", "\"1e3\"")) {
            mvc.perform(contribution(KEY, "{\"residentId\":101,\"amount\":" + amount + ",\"paidOn\":\"2026-11-08\"}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_AMOUNT"));
        }
        verifyNoInteractions(contributions);
    }

    @Test
    void missingOrMalformedIdempotencyKeyIsHttp400() throws Exception {
        String body = "{\"residentId\":101,\"amount\":\"100000\",\"paidOn\":\"2026-11-08\"}";
        mvc.perform(contribution(null, body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_IDEMPOTENCY_KEY"));
        for (String key : List.of("not-a-uuid", "1-1-1-1-1", "6fbca292b3b544debbb8291b9ee1c213", " ")) {
            mvc.perform(contribution(key, body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_IDEMPOTENCY_KEY"));
        }
        verifyNoInteractions(contributions);
    }

    @Test
    void nullResidentAndOverlongLateReasonAreHttp400AndUnknownResidentIsHttp404() throws Exception {
        mvc.perform(contribution(KEY, "{\"residentId\":null,\"amount\":\"100000\",\"paidOn\":\"2026-11-08\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        mvc.perform(contribution(KEY, "{\"amount\":\"100000\",\"paidOn\":\"2026-11-08\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        mvc.perform(contribution(KEY, "{\"residentId\":101,\"amount\":\"100000\",\"paidOn\":\"2026-11-08\",\"lateReason\":\"" + "A".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("REASON_REQUIRED"));
        mvc.perform(contribution(KEY, "{\"residentId\":101,\"amount\":\"100000\",\"paidOn\":\"2026-13-40\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DATE"));
        verifyNoInteractions(contributions);

        when(contributions.record(any(), anyLong())).thenThrow(new NotFoundException("RESIDENT_NOT_FOUND"));
        mvc.perform(contribution(KEY, "{\"residentId\":999999,\"amount\":\"100000\",\"paidOn\":\"2026-11-08\"}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("RESIDENT_NOT_FOUND"));
    }

    @Test
    void windowAndIdempotencyErrorsFromTheServiceKeepTheirStatusAndCode() throws Exception {
        record Case(RuntimeException failure, int status, String code) { }
        var cases = List.of(
                new Case(new ConflictException("CAMPAIGN_NOT_RECEIVABLE"), 409, "CAMPAIGN_NOT_RECEIVABLE"),
                new Case(new ConflictException("INVALID_PAID_DATE"), 409, "INVALID_PAID_DATE"),
                new Case(new ConflictException("IDEMPOTENCY_CONFLICT"), 409, "IDEMPOTENCY_CONFLICT"),
                new Case(new BadRequestException("REASON_REQUIRED"), 400, "REASON_REQUIRED"),
                new Case(new NotFoundException("CAMPAIGN_NOT_FOUND"), 404, "CAMPAIGN_NOT_FOUND"));
        for (var c : cases) {
            org.mockito.Mockito.reset(contributions);
            when(contributions.record(any(), anyLong())).thenThrow(c.failure());
            mvc.perform(contribution(KEY, "{\"residentId\":101,\"amount\":\"150000\",\"paidOn\":\"2026-11-08\"}"))
                    .andExpect(status().is(c.status())).andExpect(jsonPath("$.code").value(c.code()));
        }
    }

    // ---------------------------------------------------------------- VOL-06.1
    @Test
    void voidErrorsKeepTheirStatusAndCode() throws Exception {
        record Case(RuntimeException failure, int status, String code) { }
        var cases = List.of(
                new Case(new BadRequestException("REASON_REQUIRED"), 400, "REASON_REQUIRED"),
                new Case(new ConflictException("STALE_VERSION"), 409, "STALE_VERSION"),
                new Case(new NotFoundException("CONTRIBUTION_NOT_FOUND"), 404, "CONTRIBUTION_NOT_FOUND"));
        for (var c : cases) {
            org.mockito.Mockito.reset(contributions);
            when(contributions.voidContribution(anyLong(), anyLong(), any(), anyLong())).thenThrow(c.failure());
            mvc.perform(post("/api/v1/voluntary-contributions/802/void").with(authentication(as("ACCOUNTANT"))).with(csrf())
                    .contentType("application/json").content("{\"version\":0,\"reason\":\"Nhap nham\"}"))
                    .andExpect(status().is(c.status())).andExpect(jsonPath("$.code").value(c.code()));
        }
        mvc.perform(post("/api/v1/voluntary-contributions/802/void").with(authentication(as("ACCOUNTANT"))).with(csrf())
                .contentType("application/json").content("{\"reason\":\"Nhap nham\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    // ---------------------------------------------------------------- VOL-07.1
    @Test
    void roleMatrixMatchesTheSpecificationForEveryRouteThatTheCaseListsAndNothingLeaksThroughToTheServices() throws Exception {
        record Route(HttpMethod method, String path) { }
        var anonymous = List.of(new Route(HttpMethod.GET, "/api/v1/voluntary-campaigns"), new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns"));
        var resident = List.of(
                new Route(HttpMethod.GET, "/api/v1/voluntary-campaigns/712/contributions"),
                new Route(HttpMethod.GET, "/api/v1/voluntary-donors?q=Ng"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns/712/contributions"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-contributions/803/void"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns"),
                new Route(HttpMethod.PATCH, "/api/v1/voluntary-campaigns/712"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns/712/publish"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns/712/close"));
        var admin = List.of(
                new Route(HttpMethod.GET, "/api/v1/voluntary-donors?q=Ng"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns/712/contributions"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-contributions/803/void"));
        var accountant = List.of(
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns"),
                new Route(HttpMethod.PATCH, "/api/v1/voluntary-campaigns/712"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns/712/publish"),
                new Route(HttpMethod.POST, "/api/v1/voluntary-campaigns/712/close"));
        for (var route : anonymous) {
            mvc.perform(request(route.method(), route.path()).with(csrf()).contentType("application/json").content("{}"))
                    .andExpect(status().isUnauthorized());
        }
        var byRole = java.util.Map.of("RESIDENT", resident, "ADMIN", admin, "ACCOUNTANT", accountant);
        for (var entry : byRole.entrySet()) {
            for (var route : entry.getValue()) {
                mvc.perform(request(route.method(), route.path()).with(authentication(as(entry.getKey()))).with(csrf())
                        .contentType("application/json").content("{}"))
                        .andExpect(status().isForbidden());
            }
        }
        verifyNoInteractions(campaigns, contributions, donors);
    }

    @Test
    void allowedRolesReachTheServiceOnTheReadRoutes() throws Exception {
        when(campaigns.listStaff(any(), anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(campaigns.listResident(any(), anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(contributions.list(anyLong(), any(), anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(donors.search(anyString(), anyInt(), anyInt())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        for (String role : List.of("ADMIN", "ACCOUNTANT", "RESIDENT")) {
            mvc.perform(get("/api/v1/voluntary-campaigns").with(authentication(as(role)))).andExpect(status().isOk());
        }
        for (String role : List.of("ADMIN", "ACCOUNTANT")) {
            mvc.perform(get("/api/v1/voluntary-campaigns/712/contributions").with(authentication(as(role)))).andExpect(status().isOk());
        }
        mvc.perform(get("/api/v1/voluntary-donors?q=Ng").with(authentication(as("ACCOUNTANT")))).andExpect(status().isOk());
    }

    @Test
    void writesWithoutCsrfOrWithPasswordChangePendingAreRejectedBeforeTheServices() throws Exception {
        String body = "{\"residentId\":101,\"amount\":\"100000\",\"paidOn\":\"2026-11-08\"}";
        mvc.perform(post("/api/v1/voluntary-campaigns/712/contributions").with(authentication(as("ACCOUNTANT"))).header("Idempotency-Key", KEY)
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/voluntary-contributions/803/void").with(authentication(as("ACCOUNTANT")))
                .contentType("application/json").content("{\"version\":0,\"reason\":\"x\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/voluntary-campaigns/712/publish").with(authentication(as("ADMIN")))
                .contentType("application/json").content("{\"version\":0}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/voluntary-campaigns/712/contributions").with(authentication(as("ACCOUNTANT", true))).with(csrf())
                .header("Idempotency-Key", KEY).contentType("application/json").content(body))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        verifyNoInteractions(contributions, campaigns);
    }

    // ---------------------------------------------------------------- helpers
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder contribution(String key, String body) {
        var builder = post("/api/v1/voluntary-campaigns/712/contributions").with(authentication(as("ACCOUNTANT"))).with(csrf())
                .contentType("application/json").content(body);
        return key == null ? builder : builder.header("Idempotency-Key", key);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder patch(String path, String body) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(path)
                .with(authentication(as("ADMIN"))).with(csrf()).contentType("application/json").content(body);
    }
}
