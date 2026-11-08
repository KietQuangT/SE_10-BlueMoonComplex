package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.billing.api.ResidentCampaignResponse;
import vn.edu.se10.bluemoon.billing.api.StaffCampaignResponse;
import vn.edu.se10.bluemoon.billing.api.VoluntaryController;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContribution;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

@WebMvcTest(VoluntaryController.class)
@Import(SecurityConfig.class)
class VoluntaryRoutesSuccessApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean VoluntaryCampaignService campaigns;
    @MockitoBean VoluntaryContributionService contributions;
    @MockitoBean ResidentDonorLookup donors;
    @MockitoBean CurrentAccountService currentAccount;

    @BeforeEach
    void setUp() {
        when(currentAccount.refresh(anyLong())).thenAnswer(invocation ->
                (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private static UsernamePasswordAuthenticationToken auth(String role) {
        return UsernamePasswordAuthenticationToken.authenticated(
                new AuthenticatedUser(42, "user42", "User 42", role, null, false),
                null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void getCampaignsAsAdminReturnsStaffPageWithTotals() throws Exception {
        var item = new StaffCampaignResponse(5, "Ủng hộ bão lũ", "Mô tả", LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30), "PUBLISHED", 2, 1L, Instant.EPOCH, 1L, Instant.now(),
                null, null, 10, "5000000");
        when(campaigns.listStaff(null, 0, 20)).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/voluntary-campaigns?page=0&size=20").with(authentication(auth("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].name").value("Ủng hộ bão lũ"))
                .andExpect(jsonPath("$.content[0].totalCollected").value("5000000"))
                .andExpect(jsonPath("$.content[0].postedCount").value(10))
                .andExpect(jsonPath("$.content[0].version").value(2));
    }

    @Test
    void getCampaignsAsResidentReturnsResidentPageWithoutStaffTotals() throws Exception {
        var item = new ResidentCampaignResponse(5, "Ủng hộ bão lũ", "Mô tả", LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30), "PUBLISHED");
        when(campaigns.listResident(null, 0, 20)).thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/voluntary-campaigns?page=0&size=20").with(authentication(auth("RESIDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].name").value("Ủng hộ bão lũ"))
                .andExpect(jsonPath("$.content[0].version").doesNotExist())
                .andExpect(jsonPath("$.content[0].totalCollected").doesNotExist())
                .andExpect(jsonPath("$.content[0].postedCount").doesNotExist());
    }

    @Test
    void postCampaignAsAdminReturns201Created() throws Exception {
        var campaign = new VoluntaryCampaign("Ủng hộ trẻ em", "Tết thiếu nhi", LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 20), 42, Instant.EPOCH);
        ReflectionTestUtils.setField(campaign, "id", 6L);
        when(campaigns.create("Ủng hộ trẻ em", "Tết thiếu nhi", LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20), 42L))
                .thenReturn(campaign);
        var staffDto = new StaffCampaignResponse(6, "Ủng hộ trẻ em", "Tết thiếu nhi", LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 20), "DRAFT", 0, 42L, Instant.EPOCH, null, null, null, null, 0, "0");
        when(campaigns.staffDetail(6)).thenReturn(staffDto);

        mvc.perform(post("/api/v1/voluntary-campaigns").with(authentication(auth("ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ủng hộ trẻ em\",\"description\":\"Tết thiếu nhi\",\"opensOn\":\"2026-11-10\",\"closesOn\":\"2026-11-20\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.name").value("Ủng hộ trẻ em"));
    }

    @Test
    void patchCampaignAsAdminReturns200WithUpdatedStaffDetail() throws Exception {
        var updated = new StaffCampaignResponse(6, "Ủng hộ trẻ em vùng cao", "Tết thiếu nhi sửa", LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 25), "DRAFT", 1, 42L, Instant.EPOCH, null, null, null, null, 0, "0");
        when(campaigns.staffDetail(6)).thenReturn(updated);

        mvc.perform(patch("/api/v1/voluntary-campaigns/6").with(authentication(auth("ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ủng hộ trẻ em vùng cao\",\"description\":\"Tết thiếu nhi sửa\",\"opensOn\":\"2026-11-12\",\"closesOn\":\"2026-11-25\",\"version\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ủng hộ trẻ em vùng cao"))
                .andExpect(jsonPath("$.version").value(1));

        verify(campaigns).update(eq(6L), eq(0L), eq("Ủng hộ trẻ em vùng cao"), eq("Tết thiếu nhi sửa"),
                eq(LocalDate.of(2026, 11, 12)), eq(LocalDate.of(2026, 11, 25)), eq(42L));
    }

    @Test
    void publishCampaignAsAdminReturns200() throws Exception {
        var published = new StaffCampaignResponse(6, "Ủng hộ trẻ em", "Tết thiếu nhi", LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 20), "PUBLISHED", 2, 42L, Instant.EPOCH, 42L, Instant.now(), null, null, 0, "0");
        when(campaigns.staffDetail(6)).thenReturn(published);

        mvc.perform(post("/api/v1/voluntary-campaigns/6/publish").with(authentication(auth("ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(campaigns).publish(eq(6L), eq(1L), eq(42L));
    }

    @Test
    void closeCampaignAsAdminReturns200() throws Exception {
        var closed = new StaffCampaignResponse(6, "Ủng hộ trẻ em", "Tết thiếu nhi", LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 20), "CLOSED", 3, 42L, Instant.EPOCH, 42L, Instant.EPOCH, 42L, Instant.now(), 0, "0");
        when(campaigns.staffDetail(6)).thenReturn(closed);

        mvc.perform(post("/api/v1/voluntary-campaigns/6/close").with(authentication(auth("ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(campaigns).close(eq(6L), eq(2L), eq(42L));
    }

    @Test
    void getDonorsAsAccountantReturnsWhitelistedDonorInfo() throws Exception {
        var donor = new ResidentDonorLookup.Donor(9, 2, "Nguyễn Văn A", "H05", "A502");
        when(donors.search("Nguyễn", 0, 20)).thenReturn(new PageImpl<>(List.of(donor), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/voluntary-donors?q=Nguyễn&page=0&size=20").with(authentication(auth("ACCOUNTANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(9))
                .andExpect(jsonPath("$.content[0].fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.content[0].householdCode").value("H05"))
                .andExpect(jsonPath("$.content[0].apartmentCode").value("A502"))
                // Ensure sensitive PII are not leaked
                .andExpect(jsonPath("$.content[0].nationalId").doesNotExist())
                .andExpect(jsonPath("$.content[0].phone").doesNotExist())
                .andExpect(jsonPath("$.content[0].email").doesNotExist());
    }

    @Test
    void getContributionsReturnsPageOfLedgerRecords() throws Exception {
        var c = new VoluntaryContribution(5, 9, 3, "Nguyễn Văn A", "H05", "A502", new BigDecimal("1000000"),
                LocalDate.of(2026, 11, 5), null, "key-uuid", "hash", 42, Instant.parse("2026-11-05T08:00:00Z"));
        ReflectionTestUtils.setField(c, "id", 101L);
        when(contributions.list(5, "POSTED", 0, 20)).thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/voluntary-campaigns/5/contributions?status=POSTED&page=0&size=20")
                        .with(authentication(auth("ACCOUNTANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(101))
                .andExpect(jsonPath("$.content[0].campaignId").value(5))
                .andExpect(jsonPath("$.content[0].donorNameSnapshot").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.content[0].amount").value("1000000"))
                .andExpect(jsonPath("$.content[0].status").value("POSTED"));
    }

    @Test
    void voidContributionAsAccountantReturns200WithVoidReason() throws Exception {
        var c = new VoluntaryContribution(5, 9, 3, "Nguyễn Văn A", "H05", "A502", new BigDecimal("1000000"),
                LocalDate.of(2026, 11, 5), null, "key-uuid", "hash", 42, Instant.parse("2026-11-05T08:00:00Z"));
        ReflectionTestUtils.setField(c, "id", 101L);
        c.markVoided(42, Instant.parse("2026-11-06T09:00:00Z"), "Nhầm lẫn số tiền");
        when(contributions.voidContribution(101L, 0L, "Nhầm lẫn số tiền", 42L)).thenReturn(c);

        mvc.perform(post("/api/v1/voluntary-contributions/101/void").with(authentication(auth("ACCOUNTANT"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0,\"reason\":\"Nhầm lẫn số tiền\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("VOIDED"))
                .andExpect(jsonPath("$.voidReason").value("Nhầm lẫn số tiền"))
                .andExpect(jsonPath("$.voidedBy").value(42));

        verify(contributions).voidContribution(eq(101L), eq(0L), eq("Nhầm lẫn số tiền"), eq(42L));
    }
}
