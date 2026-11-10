package vn.edu.se10.bluemoon.reporting;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import vn.edu.se10.bluemoon.reporting.api.AgencyDataPackageController;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.AgencyPackageRow;
import vn.edu.se10.bluemoon.reporting.application.CreatePackageRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageActionRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageDetail;
import vn.edu.se10.bluemoon.reporting.application.PackagePreviewResponse;
import vn.edu.se10.bluemoon.reporting.application.PackageSummary;
import vn.edu.se10.bluemoon.reporting.application.PackageSummaryPage;

@WebMvcTest(AgencyDataPackageController.class)
@Import({SecurityConfig.class, AgencyDataPackageApiTest.TestClockConfig.class})
class AgencyDataPackageApiTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2026-11-10T10:00:00Z");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(FIXED_INSTANT, VIETNAM_ZONE);
        }
    }

    @Autowired MockMvc mvc;
    @MockitoBean AgencyDataPackageService service;
    @MockitoBean CurrentAccountService currentAccount;

    private UsernamePasswordAuthenticationToken adminAuth(long userId) {
        var user = new AuthenticatedUser(userId, "admin" + userId, "Admin User " + userId, "ADMIN", null, false);
        return new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private UsernamePasswordAuthenticationToken residentAuth(long userId) {
        var user = new AuthenticatedUser(userId, "resident" + userId, "Resident " + userId, "RESIDENT", 10L, false);
        return new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_RESIDENT")));
    }

    private UsernamePasswordAuthenticationToken accountantAuth(long userId) {
        var user = new AuthenticatedUser(userId, "accountant" + userId, "Accountant " + userId, "ACCOUNTANT", null, false);
        return new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_ACCOUNTANT")));
    }

    private PackageDetail sampleDetail(long id, String status, long version) {
        return new PackageDetail(
            id, status, version, "AGENCY_RESIDENCY_V1", "1.0",
            FIXED_INSTANT, 1L, null, null, null,
            FIXED_INSTANT.plusSeconds(30 * 86400), null,
            2L, 2L, null, null,
            false, true, false, false, false,
            List.of(1L, 2L), "CONG_AN_Q1", "REF-001", "LIC-002",
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            null, null, null, null
        );
    }

    @BeforeEach
    void setUp() {
        reset(currentAccount);
        when(currentAccount.refresh(anyLong())).thenAnswer(i -> {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser u) {
                return u;
            }
            return null;
        });
    }

    @Test
    void unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/agency-data-packages"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/agency-data-packages").with(csrf()))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/agency-data-packages/1"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/agency-data-packages/1/preview"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/agency-data-packages/1/approve").with(csrf()))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/agency-data-packages/1/reject").with(csrf()))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/agency-data-packages/1/revoke").with(csrf()))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/agency-data-packages/1/content.xlsx"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/agency-data-packages/1/handoff").with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdmin_returns403() throws Exception {
        var resident = authentication(residentAuth(5L));
        var accountant = authentication(accountantAuth(6L));

        // Resident forbidden on all 9 routes
        mvc.perform(get("/api/v1/agency-data-packages").with(resident))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages").with(resident).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/agency-data-packages/1").with(resident))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/agency-data-packages/1/preview").with(resident))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/approve").with(resident).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/reject").with(resident).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/revoke").with(resident).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/agency-data-packages/1/content.xlsx").with(resident))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/handoff").with(resident).with(csrf()))
            .andExpect(status().isForbidden());

        // Accountant forbidden on all 9 routes
        mvc.perform(get("/api/v1/agency-data-packages").with(accountant))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages").with(accountant).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/agency-data-packages/1").with(accountant))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/agency-data-packages/1/preview").with(accountant))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/approve").with(accountant).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/reject").with(accountant).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/revoke").with(accountant).with(csrf()))
            .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/agency-data-packages/1/content.xlsx").with(accountant))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/agency-data-packages/1/handoff").with(accountant).with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    void writeWithoutCsrf_returns403() throws Exception {
        String actionBody = "{\"expectedVersion\":1,\"contentSha256\":\"0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef\"}";

        mvc.perform(post("/api/v1/agency-data-packages")
            .with(authentication(adminAuth(1L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"householdIds\":[1],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"REF\",\"licenceReference\":\"LIC\"}"))
            .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/agency-data-packages/1/approve")
            .with(authentication(adminAuth(1L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/agency-data-packages/1/reject")
            .with(authentication(adminAuth(1L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/agency-data-packages/1/revoke")
            .with(authentication(adminAuth(1L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/agency-data-packages/1/handoff")
            .with(authentication(adminAuth(1L)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void postRoutes_withUnexpectedQueryParams_return400() throws Exception {
        String actionBody = "{\"expectedVersion\":1,\"contentSha256\":\"0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef\"}";

        mvc.perform(post("/api/v1/agency-data-packages?extra=1")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"householdIds\":[1],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"REF\",\"licenceReference\":\"LIC\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));

        mvc.perform(post("/api/v1/agency-data-packages/1/approve?actor=2")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));

        mvc.perform(post("/api/v1/agency-data-packages/1/reject?reason=bad")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));

        mvc.perform(post("/api/v1/agency-data-packages/1/revoke?force=true")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));

        mvc.perform(post("/api/v1/agency-data-packages/1/handoff?to=agency")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(actionBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));
    }

    @Test
    void getRoutes_duplicateQueryParams_returns400() throws Exception {
        mvc.perform(get("/api/v1/agency-data-packages?page=0&page=1")
            .with(authentication(adminAuth(1L))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));
    }

    @Test
    void create_householdIdOverflowsLong_returns400() throws Exception {
        String body = "{\"householdIds\":[999999999999999999999999999999999999999999999999],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"REF\",\"licenceReference\":\"LIC\"}";
        mvc.perform(post("/api/v1/agency-data-packages")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("PACKAGE_SCOPE_INVALID"));
    }

    @Test
    void approve_versionOverflowsLong_returns400() throws Exception {
        String body = "{\"expectedVersion\":999999999999999999999999999999999999999999999999,\"contentSha256\":\"0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef\"}";
        mvc.perform(post("/api/v1/agency-data-packages/1/approve")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
    }

    @Test
    void create_unknownJsonField_returns400() throws Exception {
        String body = "{\"householdIds\":[1],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"REF\",\"licenceReference\":\"LIC\",\"extraField\":123}";
        mvc.perform(post("/api/v1/agency-data-packages")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        PackageDetail detail = sampleDetail(1L, "DRAFT", 1L);
        when(service.createPackage(any(CreatePackageRequest.class), eq(1L))).thenReturn(detail);

        String body = "{\"householdIds\":[1,2],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"REF-001\",\"licenceReference\":\"LIC-002\"}";
        mvc.perform(post("/api/v1/agency-data-packages")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void list_unknownQueryParam_returns400() throws Exception {
        mvc.perform(get("/api/v1/agency-data-packages?unknown=val")
            .with(authentication(adminAuth(1L))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));
    }

    @Test
    void list_valid_returns200() throws Exception {
        PackageSummary summary = new PackageSummary(
            1L, "DRAFT", 1L, "AGENCY_RESIDENCY_V1", "1.0",
            FIXED_INSTANT, 1L, null, null, null,
            FIXED_INSTANT.plusSeconds(30 * 86400), null,
            2L, 2L, null, null,
            false, true, false, false, false
        );
        when(service.listPackages(null, 0, 20, 1L))
            .thenReturn(new PackageSummaryPage(List.of(summary), 0, 20, 1L, 1));

        mvc.perform(get("/api/v1/agency-data-packages")
            .with(authentication(adminAuth(1L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void detail_valid_returns200() throws Exception {
        PackageDetail detail = sampleDetail(1L, "DRAFT", 1L);
        when(service.getPackageDetail(1L, 1L)).thenReturn(detail);

        mvc.perform(get("/api/v1/agency-data-packages/1")
            .with(authentication(adminAuth(1L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void preview_valid_returns200() throws Exception {
        PackagePreviewResponse resp = new PackagePreviewResponse(
            1L, 1L, "sha256",
            List.of(new AgencyPackageRow("H01", "A101", "Nguyễn Văn A", "RESIDING")),
            0, 20, 1L, 1
        );
        when(service.previewPackage(1L, 0, 20, 1L)).thenReturn(resp);

        mvc.perform(get("/api/v1/agency-data-packages/1/preview")
            .with(authentication(adminAuth(1L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.packageId").value(1))
            .andExpect(jsonPath("$.content[0].fullName").value("Nguyễn Văn A"));
    }

    @Test
    void approve_valid_returns200() throws Exception {
        PackageDetail detail = sampleDetail(1L, "APPROVED", 2L);
        when(service.approvePackage(eq(1L), any(PackageActionRequest.class), eq(2L))).thenReturn(detail);

        String body = "{\"expectedVersion\":1,\"contentSha256\":\"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"}";
        mvc.perform(post("/api/v1/agency-data-packages/1/approve")
            .with(authentication(adminAuth(2L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void reject_valid_returns200() throws Exception {
        PackageDetail detail = sampleDetail(1L, "REJECTED", 2L);
        when(service.rejectPackage(eq(1L), any(PackageActionRequest.class), eq(1L))).thenReturn(detail);

        String body = "{\"expectedVersion\":1,\"contentSha256\":\"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"}";
        mvc.perform(post("/api/v1/agency-data-packages/1/reject")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REJECTED"))
            .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void revoke_valid_returns200() throws Exception {
        PackageDetail detail = sampleDetail(1L, "REVOKED", 3L);
        when(service.revokePackage(eq(1L), any(PackageActionRequest.class), eq(1L))).thenReturn(detail);

        String body = "{\"expectedVersion\":2,\"contentSha256\":\"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"}";
        mvc.perform(post("/api/v1/agency-data-packages/1/revoke")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REVOKED"))
            .andExpect(jsonPath("$.version").value(3));
    }

    @Test
    void download_valid_returnsExcelAttachment() throws Exception {
        byte[] fakeXlsx = new byte[]{1, 2, 3};
        when(service.downloadPackage(1L, 1L)).thenReturn(fakeXlsx);

        mvc.perform(get("/api/v1/agency-data-packages/1/content.xlsx")
            .with(authentication(adminAuth(1L))))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"goi-cu-tru-1-v1.xlsx\""))
            .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andExpect(header().string("Cache-Control", "no-store"))
            .andExpect(content().bytes(fakeXlsx));
    }

    @Test
    void handoff_valid_returns200() throws Exception {
        PackageDetail detail = sampleDetail(1L, "APPROVED", 3L);
        when(service.handoffPackage(eq(1L), any(PackageActionRequest.class), eq(1L))).thenReturn(detail);

        String body = "{\"expectedVersion\":2,\"contentSha256\":\"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"}";
        mvc.perform(post("/api/v1/agency-data-packages/1/handoff")
            .with(authentication(adminAuth(1L)))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(3));
    }
}
