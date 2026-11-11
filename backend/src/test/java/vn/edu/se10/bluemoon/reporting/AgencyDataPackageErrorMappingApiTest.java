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
import org.mockito.ArgumentCaptor;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.ForbiddenException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import vn.edu.se10.bluemoon.reporting.api.AgencyDataPackageController;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.CreatePackageRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageActionRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageSummaryPage;

/** Ánh xạ lỗi HTTP của API gói cơ quan: kiểm tra dữ liệu vào, mã lỗi và việc không lộ chi tiết nội bộ. */
@WebMvcTest(AgencyDataPackageController.class)
@Import({SecurityConfig.class, AgencyDataPackageErrorMappingApiTest.ClockConfig.class})
class AgencyDataPackageErrorMappingApiTest {
    private static final String BASE = "/api/v1/agency-data-packages";
    private static final String SHA = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final String VALID_CREATE =
        "{\"householdIds\":[1],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"REF-1\",\"licenceReference\":\"LIC-1\"}";
    private static final List<String> ACTIONS = List.of("approve", "reject", "revoke", "handoff");

    @TestConfiguration
    static class ClockConfig {
        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
        }
    }

    @Autowired MockMvc mvc;
    @MockitoBean AgencyDataPackageService service;
    @MockitoBean CurrentAccountService currentAccount;

    private static UsernamePasswordAuthenticationToken admin(long id) {
        var user = new AuthenticatedUser(id, "admin" + id, "Admin " + id, "ADMIN", null, false);
        return new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @BeforeEach
    void setUp() {
        reset(currentAccount);
        when(currentAccount.refresh(anyLong())).thenAnswer(i -> {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getPrincipal() instanceof AuthenticatedUser u ? u : null;
        });
    }

    private MockHttpServletRequestBuilder postJson(String url, String body) {
        return post(url).with(authentication(admin(7L))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private MockHttpServletRequestBuilder getAsAdmin(String url) {
        return get(url).with(authentication(admin(7L)));
    }

    // ---- Tạo gói: nội dung yêu cầu không hợp lệ ----

    @Test
    void create_malformedOrNonObjectBodyReturnsInvalidRequest() throws Exception {
        for (String body : List.of("{", "[]", "\"text\"", "123", "null")) {
            mvc.perform(postJson(BASE, body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
        }
        mvc.perform(post(BASE).with(authentication(admin(7L))).with(csrf()).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
        verifyNoInteractions(service);
    }

    @Test
    void create_missingAnyRequiredFieldReturnsInvalidRequest() throws Exception {
        List<String> bodies = List.of(
            "{\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"R\",\"licenceReference\":\"L\"}",
            "{\"householdIds\":[1],\"requestReference\":\"R\",\"licenceReference\":\"L\"}",
            "{\"householdIds\":[1],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"licenceReference\":\"L\"}",
            "{\"householdIds\":[1],\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"R\"}");
        for (String body : bodies) {
            mvc.perform(postJson(BASE, body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void create_householdIdsMustBeAnArrayOfIntegers() throws Exception {
        for (String ids : List.of("1", "\"1\"", "{}", "null", "[\"1\"]", "[1.5]", "[null]", "[true]", "[[1]]")) {
            mvc.perform(postJson(BASE, "{\"householdIds\":" + ids
                    + ",\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"R\",\"licenceReference\":\"L\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PACKAGE_SCOPE_INVALID"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void create_textFieldsMustBeJsonStrings() throws Exception {
        for (String fields : List.of(
            "\"recipientAgencyCode\":5,\"requestReference\":\"R\",\"licenceReference\":\"L\"",
            "\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":null,\"licenceReference\":\"L\"",
            "\"recipientAgencyCode\":\"CONG_AN_Q1\",\"requestReference\":\"R\",\"licenceReference\":[\"L\"]")) {
            mvc.perform(postJson(BASE, "{\"householdIds\":[1]," + fields + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void create_clientCannotSupplyActorOrStatusFields() throws Exception {
        for (String extra : List.of("\"createdBy\":2", "\"status\":\"APPROVED\"", "\"actorId\":2", "\"contentSha256\":\"" + SHA + "\"")) {
            mvc.perform(postJson(BASE, VALID_CREATE.replace("}", "," + extra + "}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void create_actorComesFromSessionAndRequestIsPassedUnchanged() throws Exception {
        mvc.perform(postJson(BASE, VALID_CREATE)).andExpect(status().isCreated());

        ArgumentCaptor<CreatePackageRequest> req = ArgumentCaptor.forClass(CreatePackageRequest.class);
        verify(service).createPackage(req.capture(), eq(7L));
        org.junit.jupiter.api.Assertions.assertEquals(List.of(1L), req.getValue().householdIds());
        org.junit.jupiter.api.Assertions.assertEquals("CONG_AN_Q1", req.getValue().recipientAgencyCode());
    }

    // ---- Hành động: expectedVersion và contentSha256 ----

    @Test
    void actions_rejectBadVersionOrHashOnEveryActionRoute() throws Exception {
        List<String> bad = List.of(
            "{\"contentSha256\":\"" + SHA + "\"}",
            "{\"expectedVersion\":1}",
            "{\"expectedVersion\":0,\"contentSha256\":\"" + SHA + "\"}",
            "{\"expectedVersion\":-1,\"contentSha256\":\"" + SHA + "\"}",
            "{\"expectedVersion\":\"1\",\"contentSha256\":\"" + SHA + "\"}",
            "{\"expectedVersion\":1.5,\"contentSha256\":\"" + SHA + "\"}",
            "{\"expectedVersion\":1,\"contentSha256\":\"" + SHA.toUpperCase() + "\"}",
            "{\"expectedVersion\":1,\"contentSha256\":\"" + SHA.substring(1) + "\"}",
            "{\"expectedVersion\":1,\"contentSha256\":\"" + SHA + "0\"}",
            "{\"expectedVersion\":1,\"contentSha256\":5}",
            "{\"expectedVersion\":1,\"contentSha256\":\"" + SHA + "\",\"actorId\":2}",
            "[]", "{", "");
        for (String action : ACTIONS) {
            for (String body : bad) {
                mvc.perform(postJson(BASE + "/1/" + action, body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_REQUEST"));
            }
        }
        verifyNoInteractions(service);
    }

    @Test
    void actions_passSessionActorAndExpectedVersionToService() throws Exception {
        String body = "{\"expectedVersion\":3,\"contentSha256\":\"" + SHA + "\"}";
        for (String action : ACTIONS) {
            mvc.perform(postJson(BASE + "/9/" + action, body)).andExpect(status().isOk());
        }
        ArgumentCaptor<PackageActionRequest> req = ArgumentCaptor.forClass(PackageActionRequest.class);
        verify(service).approvePackage(eq(9L), req.capture(), eq(7L));
        verify(service).rejectPackage(eq(9L), any(), eq(7L));
        verify(service).revokePackage(eq(9L), any(), eq(7L));
        verify(service).handoffPackage(eq(9L), any(), eq(7L));
        org.junit.jupiter.api.Assertions.assertEquals(3L, req.getValue().expectedVersion());
        org.junit.jupiter.api.Assertions.assertEquals(SHA, req.getValue().contentSha256());
    }

    // ---- Bộ lọc danh sách và xem trước ----

    @Test
    void list_rejectsInvalidStatusPageAndSize() throws Exception {
        for (String q : List.of("status=draft", "status=ALL", "status=EXPIRED", "page=-1", "page=x", "page=1.5",
            "size=0", "size=101", "size=abc", "size=-5")) {
            mvc.perform(getAsAdmin(BASE + "?" + q))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void list_defaultsAndExplicitFiltersReachTheService() throws Exception {
        when(service.listPackages(any(), anyInt(), anyInt(), any())).thenReturn(new PackageSummaryPage(List.of(), 0, 20, 0, 0));

        mvc.perform(getAsAdmin(BASE)).andExpect(status().isOk());
        verify(service).listPackages(null, 0, 20, 7L);

        for (String status : List.of("DRAFT", "APPROVED", "REJECTED", "REVOKED")) {
            mvc.perform(getAsAdmin(BASE + "?status=" + status + "&page=2&size=100")).andExpect(status().isOk());
            verify(service).listPackages(status, 2, 100, 7L);
        }
    }

    @Test
    void preview_rejectsInvalidPagingAndExtraParameters() throws Exception {
        for (String q : List.of("page=-1", "size=0", "size=101", "status=DRAFT", "page=0&page=1")) {
            mvc.perform(getAsAdmin(BASE + "/1/preview?" + q))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void detailAndDownload_rejectAnyQueryParameter() throws Exception {
        mvc.perform(getAsAdmin(BASE + "/1?include=residents"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));
        mvc.perform(getAsAdmin(BASE + "/1/content.xlsx?format=csv"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_PACKAGE_FILTER"));
        verifyNoInteractions(service);
    }

    @Test
    void nonNumericPackageIdReturns400WithoutReachingTheService() throws Exception {
        mvc.perform(getAsAdmin(BASE + "/abc")).andExpect(status().isBadRequest());
        mvc.perform(getAsAdmin(BASE + "/abc/content.xlsx")).andExpect(status().isBadRequest());
        mvc.perform(postJson(BASE + "/abc/approve", "{\"expectedVersion\":1,\"contentSha256\":\"" + SHA + "\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    // ---- Ánh xạ lỗi nghiệp vụ -> HTTP ----

    @Test
    void notFoundMapsTo404WithCode() throws Exception {
        when(service.getPackageDetail(eq(404L), any())).thenThrow(new NotFoundException("PACKAGE_NOT_FOUND"));
        when(service.previewPackage(eq(404L), anyInt(), anyInt(), any())).thenThrow(new NotFoundException("PACKAGE_NOT_FOUND"));
        when(service.downloadPackage(eq(404L), any())).thenThrow(new NotFoundException("PACKAGE_NOT_FOUND"));

        mvc.perform(getAsAdmin(BASE + "/404")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PACKAGE_NOT_FOUND"));
        mvc.perform(getAsAdmin(BASE + "/404/preview")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PACKAGE_NOT_FOUND"));
        mvc.perform(getAsAdmin(BASE + "/404/content.xlsx")).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("PACKAGE_NOT_FOUND"));
    }

    @Test
    void conflictCodesMapTo409OnActionsAndPreview() throws Exception {
        String body = "{\"expectedVersion\":1,\"contentSha256\":\"" + SHA + "\"}";
        when(service.approvePackage(anyLong(), any(), any())).thenThrow(new ConflictException("PACKAGE_VERSION_CONFLICT"));
        when(service.rejectPackage(anyLong(), any(), any())).thenThrow(new ConflictException("PACKAGE_STATE_CONFLICT"));
        when(service.revokePackage(anyLong(), any(), any())).thenThrow(new ConflictException("PACKAGE_STATE_CONFLICT"));
        when(service.handoffPackage(anyLong(), any(), any())).thenThrow(new ConflictException("PACKAGE_DOWNLOAD_EXPIRED"));
        when(service.previewPackage(anyLong(), anyInt(), anyInt(), any())).thenThrow(new ConflictException("PACKAGE_CONTENT_UNAVAILABLE"));
        when(service.downloadPackage(anyLong(), any())).thenThrow(new ConflictException("PACKAGE_DOWNLOAD_EXPIRED"));

        mvc.perform(postJson(BASE + "/1/approve", body)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PACKAGE_VERSION_CONFLICT"));
        mvc.perform(postJson(BASE + "/1/reject", body)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PACKAGE_STATE_CONFLICT"));
        mvc.perform(postJson(BASE + "/1/revoke", body)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PACKAGE_STATE_CONFLICT"));
        mvc.perform(postJson(BASE + "/1/handoff", body)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PACKAGE_DOWNLOAD_EXPIRED"));
        mvc.perform(getAsAdmin(BASE + "/1/preview")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("PACKAGE_CONTENT_UNAVAILABLE"));
        mvc.perform(getAsAdmin(BASE + "/1/content.xlsx"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("PACKAGE_DOWNLOAD_EXPIRED"))
            .andExpect(header().doesNotExist("Content-Disposition"));
    }

    @Test
    void selfApprovalMapsTo403WithDedicatedCode() throws Exception {
        when(service.approvePackage(anyLong(), any(), any())).thenThrow(new ForbiddenException("PACKAGE_SELF_DECISION_NOT_ALLOWED"));

        mvc.perform(postJson(BASE + "/1/approve", "{\"expectedVersion\":1,\"contentSha256\":\"" + SHA + "\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("PACKAGE_SELF_DECISION_NOT_ALLOWED"));
    }

    @Test
    void serviceValidationErrorsKeepTheirSpecificCode() throws Exception {
        when(service.createPackage(any(), any()))
            .thenThrow(new BadRequestException("PACKAGE_TOO_LARGE"))
            .thenThrow(new BadRequestException("PACKAGE_SCOPE_INVALID"));

        mvc.perform(postJson(BASE, VALID_CREATE)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PACKAGE_TOO_LARGE"));
        mvc.perform(postJson(BASE, VALID_CREATE)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PACKAGE_SCOPE_INVALID"));
    }

    @Test
    void contentIntegrityFailureReturnsGeneric500WithoutLeakingInternals() throws Exception {
        when(service.downloadPackage(anyLong(), any())).thenThrow(new IllegalStateException("Nội dung gói bị sai lệch mã hash SHA-256"));

        mvc.perform(getAsAdmin(BASE + "/1/content.xlsx"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SHA-256"))))
            .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("IllegalStateException"))))
            .andExpect(header().doesNotExist("Content-Disposition"));
    }
}
