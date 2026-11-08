package vn.edu.se10.bluemoon.billing;

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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.billing.api.BillingHouseholdResponse;
import vn.edu.se10.bluemoon.billing.api.BillingLineResponse;
import vn.edu.se10.bluemoon.billing.api.BillingPeriodController;
import vn.edu.se10.bluemoon.billing.api.BillingPeriodResponse;
import vn.edu.se10.bluemoon.billing.api.BillingSubmissionResponse;
import vn.edu.se10.bluemoon.billing.api.InvoiceResponse;
import vn.edu.se10.bluemoon.billing.api.PageResponse;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.CalculationMethod;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;

@WebMvcTest(BillingPeriodController.class)
@Import(SecurityConfig.class)
class BillingPeriodRoutesSuccessApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean BillingPeriodService periods;
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
    void getBillingPeriodsReturns200WithPageMetadataAndDto() throws Exception {
        var dto = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 30), "DRAFT", 1, 0,
                false, true, List.of("UPDATE", "SUBMIT"), 2L, "1060000", Instant.parse("2026-11-08T01:00:00Z"),
                null, null, null, null, null, null, 0);
        var page = new PageResponse<>(List.of(dto), 0, 20, 1, 1);
        when(periods.list("2026-11", "DRAFT", 0, 20, "ACCOUNTANT")).thenReturn(page);

        mvc.perform(get("/api/v1/billing-periods?period=2026-11&status=DRAFT&page=0&size=20")
                        .with(authentication(auth("ACCOUNTANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].period").value("2026-11"))
                .andExpect(jsonPath("$.content[0].dueDate").value("2026-11-30"))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.content[0].editable").value(true))
                .andExpect(jsonPath("$.content[0].allowedActions[0]").value("UPDATE"))
                .andExpect(jsonPath("$.content[0].allowedActions[1]").value("SUBMIT"));
    }

    @Test
    void getBillingPeriodDetailReturns200WithFullMetadata() throws Exception {
        var dto = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 30), "PENDING_APPROVAL", 2, 1,
                false, true, List.of("WITHDRAW"), 5L, "2650000", Instant.parse("2026-11-08T01:00:00Z"),
                Instant.parse("2026-11-08T02:00:00Z"), null, null, null, null, null, 0);
        when(periods.detail(10, "ADMIN")).thenReturn(dto);

        mvc.perform(get("/api/v1/billing-periods/10").with(authentication(auth("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.latestRevision").value(1))
                .andExpect(jsonPath("$.householdCount").value(5))
                .andExpect(jsonPath("$.total").value("2650000"))
                .andExpect(jsonPath("$.submittedAt").value("2026-11-08T02:00:00Z"));
    }

    @Test
    void patchBillingPeriodDueDateReturns200AndDelegatesToService() throws Exception {
        var updated = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 28), "DRAFT", 2, 0,
                false, true, List.of("UPDATE", "SUBMIT"), null, null, Instant.EPOCH, null, null, null, null, null, null, 0);
        when(periods.detail(10, "ACCOUNTANT")).thenReturn(updated);

        mvc.perform(patch("/api/v1/billing-periods/10").with(authentication(auth("ACCOUNTANT"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dueDate\":\"2026-11-28\",\"version\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.dueDate").value("2026-11-28"))
                .andExpect(jsonPath("$.version").value(2));

        verify(periods).updateDueDate(eq(10L), eq(LocalDate.of(2026, 11, 28)), eq(1L), eq(42L));
    }

    @Test
    void getPreviewReturns200WithBillingSubmissionResponse() throws Exception {
        var line1 = new BillingLineResponse("MANAGEMENT_FEE", "Phí quản lý", CalculationMethod.AREA,
                "50.00", "m2", "50.00", "10000", 1, "500000");
        var line2 = new BillingLineResponse("WASTE_FEE", "Phí rác", CalculationMethod.PER_HOUSEHOLD,
                "1", "hộ", null, "30000", 2, "30000");
        var household = new BillingHouseholdResponse(1, "H01", "A101", "50.00", List.of(line1, line2), "530000");
        var preview = new BillingSubmissionResponse(null, null, null, 1, "530000", 1,
                new PageResponse<>(List.of(household), 0, 20, 1, 1));
        when(periods.preview(10, 0, 20)).thenReturn(preview);

        mvc.perform(get("/api/v1/billing-periods/10/preview?page=0&size=20").with(authentication(auth("ACCOUNTANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.householdCount").value(1))
                .andExpect(jsonPath("$.total").value("530000"))
                .andExpect(jsonPath("$.households.content[0].householdCode").value("H01"))
                .andExpect(jsonPath("$.households.content[0].lines[0].feeTypeCode").value("MANAGEMENT_FEE"))
                .andExpect(jsonPath("$.households.content[0].lines[0].amount").value("500000"))
                .andExpect(jsonPath("$.households.content[0].lines[1].feeTypeCode").value("WASTE_FEE"))
                .andExpect(jsonPath("$.households.content[0].lines[1].amount").value("30000"));
    }

    @Test
    void postSubmitReturns200AndDelegatesToService() throws Exception {
        var submitted = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 30), "PENDING_APPROVAL", 2, 1,
                false, true, List.of("WITHDRAW"), 1L, "530000", Instant.EPOCH, Instant.now(), null, null, null, null, null, 0);
        when(periods.detail(10, "ACCOUNTANT")).thenReturn(submitted);

        mvc.perform(post("/api/v1/billing-periods/10/submit").with(authentication(auth("ACCOUNTANT"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.latestRevision").value(1));

        verify(periods).submit(eq(10L), eq(1L), eq(42L));
    }

    @Test
    void postWithdrawReturns200AndDelegatesToService() throws Exception {
        var withdrawn = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 30), "DRAFT", 3, 1,
                false, true, List.of("UPDATE", "SUBMIT"), null, null, Instant.EPOCH, null, null, null, null, null, null, 0);
        when(periods.detail(10, "ACCOUNTANT")).thenReturn(withdrawn);

        mvc.perform(post("/api/v1/billing-periods/10/withdraw").with(authentication(auth("ACCOUNTANT"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.version").value(3));

        verify(periods).withdraw(eq(10L), eq(2L), eq(42L));
    }

    @Test
    void postReturnReturns200WithReasonAndDelegatesToService() throws Exception {
        var returned = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 30), "DRAFT", 3, 1,
                false, true, List.of("UPDATE", "SUBMIT"), null, null, Instant.EPOCH, null, null, null,
                "Cần kiểm tra lại diện tích", null, null, 0);
        when(periods.detail(10, "ADMIN")).thenReturn(returned);

        mvc.perform(post("/api/v1/billing-periods/10/return").with(authentication(auth("ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":2,\"reason\":\"Cần kiểm tra lại diện tích\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.returnReason").value("Cần kiểm tra lại diện tích"));

        verify(periods).returnToDraft(eq(10L), eq(2L), eq("Cần kiểm tra lại diện tích"), eq(42L));
    }

    @Test
    void postIssueReturns200WithFullIssuedDto() throws Exception {
        var issued = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 30), "ISSUED", 3, 1,
                false, true, List.of("CANCEL"), 1L, "530000", Instant.EPOCH, Instant.parse("2026-11-08T02:00:00Z"),
                Instant.parse("2026-11-08T03:00:00Z"), null, null, null, null, 0);
        when(periods.detail(10, "ADMIN")).thenReturn(issued);

        mvc.perform(post("/api/v1/billing-periods/10/issue").with(authentication(auth("ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":2,\"revision\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"))
                .andExpect(jsonPath("$.issuedAt").value("2026-11-08T03:00:00Z"))
                .andExpect(jsonPath("$.allowedActions[0]").value("CANCEL"));

        verify(periods).issue(eq(10L), eq(2L), eq(1L), eq(42L));
    }

    @Test
    void postCancelReturns200WithCancelReasonAndCancelledStatus() throws Exception {
        var cancelled = new BillingPeriodResponse(10, "2026-11", LocalDate.of(2026, 11, 30), "CANCELLED", 4, 1,
                false, false, List.of(), 1L, "530000", Instant.EPOCH, Instant.parse("2026-11-08T02:00:00Z"),
                Instant.parse("2026-11-08T03:00:00Z"), Instant.parse("2026-11-08T04:00:00Z"), null,
                "Sai đơn giá phí rác", null, 0);
        when(periods.detail(10, "ADMIN")).thenReturn(cancelled);

        mvc.perform(post("/api/v1/billing-periods/10/cancel").with(authentication(auth("ADMIN"))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":3,\"reason\":\"Sai đơn giá phí rác\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelReason").value("Sai đơn giá phí rác"))
                .andExpect(jsonPath("$.cancelledAt").value("2026-11-08T04:00:00Z"));

        verify(periods).cancel(eq(10L), eq(3L), eq("Sai đơn giá phí rác"), eq(42L));
    }

    @Test
    void getSubmissionsReturns200WithRevisionAndHouseholdsPage() throws Exception {
        var line = new BillingLineResponse("MANAGEMENT_FEE", "Phí quản lý", CalculationMethod.AREA,
                "50.00", "m2", "50.00", "10000", 1, "500000");
        var household = new BillingHouseholdResponse(1, "H01", "A101", "50.00", List.of(line), "500000");
        var submission = new BillingSubmissionResponse(1L, Instant.parse("2026-11-08T02:00:00Z"), 42L, 1,
                "500000", 2, new PageResponse<>(List.of(household), 0, 20, 1, 1));
        when(periods.submission(10, 1L, 0, 20)).thenReturn(submission);

        mvc.perform(get("/api/v1/billing-periods/10/submissions?revision=1&page=0&size=20")
                        .with(authentication(auth("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revision").value(1))
                .andExpect(jsonPath("$.submittedBy").value(42))
                .andExpect(jsonPath("$.total").value("500000"))
                .andExpect(jsonPath("$.households.content[0].householdCode").value("H01"));
    }

    @Test
    void getInvoicesReturns200WithPageOfInvoices() throws Exception {
        var line = new BillingLineResponse("MANAGEMENT_FEE", "Phí quản lý", CalculationMethod.AREA,
                "50.00", "m2", "50.00", "10000", 1, "500000");
        var invoice = new InvoiceResponse(100, 10, 1, "H01", "A101", "2026-11",
                LocalDate.of(2026, 11, 30), "ISSUED", Instant.parse("2026-11-08T03:00:00Z"), null,
                List.of(line), "500000");
        var page = new PageResponse<>(List.of(invoice), 0, 20, 1, 1);
        when(periods.invoices(10, 0, 20)).thenReturn(page);

        mvc.perform(get("/api/v1/billing-periods/10/invoices?page=0&size=20")
                        .with(authentication(auth("ACCOUNTANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].status").value("ISSUED"))
                .andExpect(jsonPath("$.content[0].lines[0].feeTypeCode").value("MANAGEMENT_FEE"));
    }

    @Test
    void getSingleInvoiceReturns200WithInvoiceDetails() throws Exception {
        var line = new BillingLineResponse("WASTE_FEE", "Phí rác", CalculationMethod.PER_HOUSEHOLD,
                "1", "hộ", null, "30000", 2, "30000");
        var invoice = new InvoiceResponse(101, 10, 1, "H01", "A101", "2026-11",
                LocalDate.of(2026, 11, 30), "CANCELLED", Instant.parse("2026-11-08T03:00:00Z"),
                Instant.parse("2026-11-08T04:00:00Z"), List.of(line), "30000");
        when(periods.invoice(101)).thenReturn(invoice);

        mvc.perform(get("/api/v1/invoices/101").with(authentication(auth("ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").value("2026-11-08T04:00:00Z"))
                .andExpect(jsonPath("$.total").value("30000"));
    }
}
