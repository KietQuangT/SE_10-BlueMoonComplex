package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;

/**
 * Bằng chứng tự động cho ánh xạ lỗi HTTP của API kỳ thu (BIL-01.2, 01.4, 02.2, 05.3, 06.1,
 * 07.2, 08.1): mã trạng thái và `code` ổn định, không lộ chi tiết nội bộ.
 */
@WebMvcTest(BillingPeriodController.class)
@Import(SecurityConfig.class)
class BillingErrorMappingApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean BillingPeriodService periods;
    @MockitoBean CurrentAccountService currentAccount;

    @BeforeEach
    void refresh() {
        when(currentAccount.refresh(anyLong())).thenAnswer(invocation ->
                (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private static UsernamePasswordAuthenticationToken as(String role) {
        return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(1, "test", "Test", role, null, false),
                null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void duplicateMonthIsHttp409WithStableCode() throws Exception {
        when(periods.create(eq("2026-11"), any(), anyLong())).thenThrow(new ConflictException("BILLING_PERIOD_EXISTS"));
        mvc.perform(post("/api/v1/billing-periods").with(authentication(as("ACCOUNTANT"))).with(csrf())
                .contentType("application/json").content("{\"period\":\"2026-11\",\"dueDate\":\"2026-11-30\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("BILLING_PERIOD_EXISTS"));
    }

    @Test
    void wrongPeriodIsHttp400InvalidPeriod() throws Exception {
        when(periods.create(eq("2026-13"), any(), anyLong())).thenThrow(new BadRequestException("INVALID_PERIOD"));
        when(periods.create(eq("2026-12"), any(), anyLong())).thenThrow(new BadRequestException("INVALID_PERIOD"));
        for (String period : List.of("2026-13", "2026-12")) {
            mvc.perform(post("/api/v1/billing-periods").with(authentication(as("ACCOUNTANT"))).with(csrf())
                    .contentType("application/json").content("{\"period\":\"" + period + "\",\"dueDate\":\"2026-11-30\"}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
        }
    }

    @Test
    void unparsableOrNonexistentDueDateIsHttp400InvalidDateBeforeService() throws Exception {
        for (String dueDate : List.of("\"2026-02-30\"", "\"2026-11-31\"", "\"abc\"", "\"\"", "null", "\"0999-01-01\"", "\"+10000-01-01\"")) {
            mvc.perform(post("/api/v1/billing-periods").with(authentication(as("ACCOUNTANT"))).with(csrf())
                    .contentType("application/json").content("{\"period\":\"2026-11\",\"dueDate\":" + dueDate + "}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DATE"));
        }
        verify(periods, never()).create(anyString(), any(), anyLong());
    }

    @Test
    void dueDateOutsideMonthFromServiceIsHttp400InvalidDate() throws Exception {
        when(periods.create(eq("2026-11"), any(), anyLong())).thenThrow(new BadRequestException("INVALID_DATE"));
        mvc.perform(post("/api/v1/billing-periods").with(authentication(as("ACCOUNTANT"))).with(csrf())
                .contentType("application/json").content("{\"period\":\"2026-11\",\"dueDate\":\"2026-12-01\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DATE"));
    }

    @Test
    void unknownPeriodIsHttp404WithoutInternalDetails() throws Exception {
        when(periods.detail(anyLong(), anyString())).thenThrow(new NotFoundException("BILLING_PERIOD_NOT_FOUND"));
        mvc.perform(get("/api/v1/billing-periods/999999").with(authentication(as("ADMIN"))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("BILLING_PERIOD_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Không tìm thấy tài nguyên."))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void invalidFiltersAreHttp400() throws Exception {
        when(periods.list(eq("2026-13"), any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt(), anyString()))
                .thenThrow(new BadRequestException("INVALID_PERIOD"));
        when(periods.list(any(), eq("BOGUS"), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt(), anyString()))
                .thenThrow(new BadRequestException("INVALID_FILTER"));
        mvc.perform(get("/api/v1/billing-periods?period=2026-13").with(authentication(as("ACCOUNTANT"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PERIOD"));
        mvc.perform(get("/api/v1/billing-periods?status=BOGUS").with(authentication(as("ACCOUNTANT"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_FILTER"));
    }

    @Test
    void returnWithoutReasonIsHttp400ReasonRequired() throws Exception {
        when(periods.returnToDraft(anyLong(), anyLong(), any(), anyLong())).thenThrow(new BadRequestException("REASON_REQUIRED"));
        mvc.perform(post("/api/v1/billing-periods/5/return").with(authentication(as("ADMIN"))).with(csrf())
                .contentType("application/json").content("{\"version\":0,\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("REASON_REQUIRED"));
    }

    @Test
    void issueConflictsAreHttp409WithStableCodes() throws Exception {
        for (String code : List.of("STALE_VERSION", "BILLING_INPUT_CHANGED", "BILLING_PERIOD_EXPIRED")) {
            doThrow(new ConflictException(code)).when(periods).issue(anyLong(), anyLong(), anyLong(), anyLong());
            mvc.perform(post("/api/v1/billing-periods/5/issue").with(authentication(as("ADMIN"))).with(csrf())
                    .contentType("application/json").content("{\"version\":0,\"revision\":1}"))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(code));
        }
        doThrow(new BadRequestException("INVALID_DATE")).when(periods).issue(anyLong(), anyLong(), anyLong(), anyLong());
        mvc.perform(post("/api/v1/billing-periods/5/issue").with(authentication(as("ADMIN"))).with(csrf())
                .contentType("application/json").content("{\"version\":0,\"revision\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_DATE"));
    }

    @Test
    void cancelConflictsAreHttp409WithStableCodes() throws Exception {
        for (String code : List.of("PERIOD_HAS_RECEIPTS", "BILLING_PERIOD_EXPIRED", "INVALID_STATE")) {
            doThrow(new ConflictException(code)).when(periods).cancel(anyLong(), anyLong(), any(), anyLong());
            mvc.perform(post("/api/v1/billing-periods/5/cancel").with(authentication(as("ADMIN"))).with(csrf())
                    .contentType("application/json").content("{\"version\":2,\"reason\":\"Sai don gia\"}"))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(code));
        }
    }

    @Test
    void submitWithoutSourceDataIsHttp409() throws Exception {
        for (String code : List.of("NO_ACTIVE_HOUSEHOLDS", "FEE_RATE_UNAVAILABLE")) {
            doThrow(new ConflictException(code)).when(periods).submit(anyLong(), anyLong(), anyLong());
            mvc.perform(post("/api/v1/billing-periods/5/submit").with(authentication(as("ACCOUNTANT"))).with(csrf())
                    .contentType("application/json").content("{\"version\":0}"))
                    .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(code));
        }
    }

    @Test
    void passwordChangeRequiredBlocksEveryWriteRouteBeforeTheService() throws Exception {
        var mustChange = UsernamePasswordAuthenticationToken.authenticated(
                new AuthenticatedUser(1, "test", "Test", "ACCOUNTANT", null, true), null,
                List.of(new SimpleGrantedAuthority("ROLE_ACCOUNTANT")));
        mvc.perform(post("/api/v1/billing-periods").with(authentication(mustChange)).with(csrf())
                .contentType("application/json").content("{\"period\":\"2026-11\",\"dueDate\":\"2026-11-30\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        mvc.perform(post("/api/v1/billing-periods/5/submit").with(authentication(mustChange)).with(csrf())
                .contentType("application/json").content("{\"version\":0}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
        verify(periods, never()).create(anyString(), any(), anyLong());
        verify(periods, never()).submit(anyLong(), anyLong(), anyLong());
    }

    @Test
    void missingVersionIsHttp400Validation() throws Exception {
        mvc.perform(post("/api/v1/billing-periods/5/submit").with(authentication(as("ACCOUNTANT"))).with(csrf())
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
        verify(periods, never()).submit(anyLong(), anyLong(), anyLong());
    }
}
