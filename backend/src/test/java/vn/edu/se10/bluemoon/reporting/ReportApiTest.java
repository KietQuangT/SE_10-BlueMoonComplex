package vn.edu.se10.bluemoon.reporting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import vn.edu.se10.bluemoon.reporting.api.ReportExportController;
import vn.edu.se10.bluemoon.reporting.application.CashReceiptsReportService;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportService;
import vn.edu.se10.bluemoon.reporting.application.ResidencyReportService;

@WebMvcTest(ReportExportController.class)
@Import({SecurityConfig.class, ReportApiTest.TestClockConfig.class})
class ReportApiTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2026-11-10T09:52:00Z");
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
    @MockitoBean FeeCollectionReportService feeCollectionService;
    @MockitoBean CashReceiptsReportService cashReceiptsService;
    @MockitoBean ResidencyReportService residencyService;
    @MockitoBean CurrentAccountService currentAccount;

    @BeforeEach
    void setUp() {
        reset(currentAccount);
        when(currentAccount.refresh(anyLong())).thenAnswer(i -> {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
                return user;
            }
            return null;
        });

        when(feeCollectionService.export(any())).thenReturn("synthetic-excel-bytes".getBytes());
        when(cashReceiptsService.export(any())).thenReturn("synthetic-excel-bytes".getBytes());
        when(residencyService.export()).thenReturn("synthetic-excel-bytes".getBytes());
    }

    private static UsernamePasswordAuthenticationToken auth(String role) {
        return auth(role, false);
    }

    private static UsernamePasswordAuthenticationToken auth(String role, boolean mustChangePassword) {
        AuthenticatedUser user = new AuthenticatedUser(1L, "user1", "User One", role, 100L, mustChangePassword);
        return UsernamePasswordAuthenticationToken.authenticated(
            user,
            null,
            java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    // --- AC3: Authentication guards (401) ---

    @Test
    void unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/reports/residency.xlsx"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void inactiveAccount_returns401() throws Exception {
        when(currentAccount.refresh(anyLong())).thenReturn(null);

        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10").with(authentication(auth("ADMIN"))))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31").with(authentication(auth("ADMIN"))))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/reports/residency.xlsx").with(authentication(auth("ADMIN"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void mustChangePassword_returns403() throws Exception {
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10").with(authentication(auth("ADMIN", true))))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31").with(authentication(auth("ADMIN", true))))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/reports/residency.xlsx").with(authentication(auth("ADMIN", true))))
            .andExpect(status().isForbidden());
    }

    // --- AC3: Role permissions (ADMIN vs ACCOUNTANT vs RESIDENT) ---

    @Test
    void residentRole_returns403OnAllEndpoints() throws Exception {
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10").with(authentication(auth("RESIDENT"))))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31").with(authentication(auth("RESIDENT"))))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/reports/residency.xlsx").with(authentication(auth("RESIDENT"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void accountantRole_allowsFinancialReports_deniesResidency() throws Exception {
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10").with(authentication(auth("ACCOUNTANT"))))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"thu-theo-ky-2026-10.xlsx\""))
            .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andExpect(header().string("Cache-Control", "no-store"));

        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31").with(authentication(auth("ACCOUNTANT"))))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"tien-mat-2026-10-01-2026-10-31.xlsx\""))
            .andExpect(header().string("Cache-Control", "no-store"));

        mvc.perform(get("/api/v1/reports/residency.xlsx").with(authentication(auth("ACCOUNTANT"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminRole_allowsAllReports() throws Exception {
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10").with(authentication(auth("ADMIN"))))
            .andExpect(status().isOk());

        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31").with(authentication(auth("ADMIN"))))
            .andExpect(status().isOk());

        mvc.perform(get("/api/v1/reports/residency.xlsx").with(authentication(auth("ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"cu-tru-hien-tai-20261110.xlsx\""))
            .andExpect(header().string("Cache-Control", "no-store"));
    }

    // --- AC3: Parameter Validation ---

    @Test
    void feeCollection_invalidFilter_returns400() throws Exception {
        // Missing period
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));

        // Invalid period format
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-13").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));

        // Unknown param
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10&extra=1").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));

        // Duplicate param
        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10&period=2026-11").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));
    }

    @Test
    void cashReceipts_invalidFilter_returns400() throws Exception {
        // Missing to
        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));

        // from > to
        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-31&to=2026-10-01").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));

        // to in future (> 2026-11-10)
        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-11-15").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));

        // Unknown key
        mvc.perform(get("/api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31&householdId=12").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));
    }

    @Test
    void residency_withAnyParam_returns400() throws Exception {
        mvc.perform(get("/api/v1/reports/residency.xlsx?status=ACTIVE").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_FILTER"));
    }

    @Test
    void reportTooLarge_returns400WithCode() throws Exception {
        when(feeCollectionService.export(any())).thenThrow(new BadRequestException("REPORT_TOO_LARGE"));

        mvc.perform(get("/api/v1/reports/fee-collection.xlsx?period=2026-10").with(authentication(auth("ADMIN"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("REPORT_TOO_LARGE"));
    }
}
