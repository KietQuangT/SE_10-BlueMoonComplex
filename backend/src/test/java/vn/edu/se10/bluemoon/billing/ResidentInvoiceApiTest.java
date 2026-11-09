package vn.edu.se10.bluemoon.billing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.billing.api.PageResponse;
import vn.edu.se10.bluemoon.billing.api.ResidentAllocation;
import vn.edu.se10.bluemoon.billing.api.ResidentBillingSummary;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceController;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceDetail;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceLine;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceSummary;
import vn.edu.se10.bluemoon.billing.api.ResidentReceipt;
import vn.edu.se10.bluemoon.billing.application.ResidentInvoiceService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;

@WebMvcTest(ResidentInvoiceController.class)
@Import(SecurityConfig.class)
class ResidentInvoiceApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean ResidentInvoiceService service;
    @MockitoBean CurrentAccountService currentAccount;

    @BeforeEach void setUp() {
        when(currentAccount.refresh(anyLong())).thenAnswer(i ->
                (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal());
    }

    private static UsernamePasswordAuthenticationToken auth(String role, Long householdId) {
        return auth(role, householdId, false);
    }

    private static UsernamePasswordAuthenticationToken auth(String role, Long householdId, boolean mustChangePassword) {
        return UsernamePasswordAuthenticationToken.authenticated(
                new AuthenticatedUser(1L, "resident1", "Nguyen Van A", role, householdId, mustChangePassword),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    // 1. Unauthenticated -> 401
    @Test void unauthenticatedGets401() throws Exception {
        mvc.perform(get("/api/v1/resident/invoices")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/resident/invoices/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/resident/receipts")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/resident/credit-allocations")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/resident/billing-summary")).andExpect(status().isUnauthorized());
    }

    // 2. Non-RESIDENT roles -> 403
    @Test void adminAndAccountantReceive403() throws Exception {
        for (String role : List.of("ADMIN", "ACCOUNTANT", "STAFF")) {
            mvc.perform(get("/api/v1/resident/invoices").with(authentication(auth(role, 10L))))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/v1/resident/invoices/1").with(authentication(auth(role, 10L))))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/v1/resident/receipts").with(authentication(auth(role, 10L))))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/v1/resident/credit-allocations").with(authentication(auth(role, 10L))))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/v1/resident/billing-summary").with(authentication(auth(role, 10L))))
                    .andExpect(status().isForbidden());
        }
    }

    // 3. Must change password -> 403 PASSWORD_CHANGE_REQUIRED
    @Test void mustChangePasswordGets403() throws Exception {
        mvc.perform(get("/api/v1/resident/invoices").with(authentication(auth("RESIDENT", 10L, true))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PASSWORD_CHANGE_REQUIRED"));
    }

    // 4. Resident missing household -> 403
    @Test void residentMissingHouseholdReceives403() throws Exception {
        when(service.invoices(any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new AccessDeniedException("Không có quyền truy cập."));
        mvc.perform(get("/api/v1/resident/invoices").with(authentication(auth("RESIDENT", null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Không có quyền truy cập."));
    }

    // 5. Query parameters allowlist enforcement -> 400 INVALID_FILTER
    @Test void disallowedQueryKeysRejected() throws Exception {
        mvc.perform(get("/api/v1/resident/invoices?householdId=123").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/invoices?userId=123").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/invoices?accountId=123").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/invoices/1?foo=bar").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/receipts?householdId=123").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/credit-allocations?householdId=123").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/billing-summary?foo=bar").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
    }

    // 6. Invalid filter value -> 400 INVALID_FILTER
    @Test void invalidFilterValueReturns400() throws Exception {
        when(service.invoices(any(), eq("2026-99"), any(), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/invoices?period=2026-99").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));

        when(service.invoices(any(), any(), eq("paid"), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/invoices?paymentStatus=paid").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));

        when(service.receipts(any(), any(), eq("UNKNOWN"), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/receipts?status=UNKNOWN").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));

        when(service.creditAllocations(any(), any(), eq("UNKNOWN"), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("INVALID_FILTER"));
        mvc.perform(get("/api/v1/resident/credit-allocations?status=UNKNOWN").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
    }

    // 7. Not found invoice / other household invoice -> 404 INVOICE_NOT_FOUND
    @Test void notFoundOrForeignInvoiceReturns404() throws Exception {
        when(service.invoiceDetail(any(), eq(999L))).thenThrow(new NotFoundException("INVOICE_NOT_FOUND"));
        mvc.perform(get("/api/v1/resident/invoices/999").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVOICE_NOT_FOUND"))
                .andExpect(jsonPath("$.detail").value("Không tìm thấy tài nguyên."));

        when(service.receipts(any(), eq(999L), any(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("INVOICE_NOT_FOUND"));
        mvc.perform(get("/api/v1/resident/receipts?invoiceId=999").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVOICE_NOT_FOUND"));

        when(service.creditAllocations(any(), eq(999L), any(), anyInt(), anyInt()))
                .thenThrow(new NotFoundException("INVOICE_NOT_FOUND"));
        mvc.perform(get("/api/v1/resident/credit-allocations?invoiceId=999").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVOICE_NOT_FOUND"));
    }

    // 8. Cache-Control: no-store header on all endpoints
    @Test void allEndpointsHaveNoStoreHeader() throws Exception {
        when(service.invoices(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
        mvc.perform(get("/api/v1/resident/invoices").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));

        when(service.invoiceDetail(any(), eq(42L)))
                .thenReturn(new ResidentInvoiceDetail(42L, "2026-11", "H1", "A1", "2026-11-30", "2026-11-09T00:00:00Z", null, "ISSUED", "PAID", "1000", "1000", "0", List.of()));
        mvc.perform(get("/api/v1/resident/invoices/42").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));

        when(service.receipts(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
        mvc.perform(get("/api/v1/resident/receipts").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));

        when(service.creditAllocations(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
        mvc.perform(get("/api/v1/resident/credit-allocations").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));

        when(service.billingSummary(any()))
                .thenReturn(new ResidentBillingSummary("0", 0, null, null, "0"));
        mvc.perform(get("/api/v1/resident/billing-summary").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    // 9. DTO response structure & whitelist verification
    @Test void invoiceDetailDtoStructureAndWhitelist() throws Exception {
        var lines = List.of(
                new ResidentInvoiceLine("MANAGEMENT_FEE", "Phí quản lý", "AREA", "50.00", "M2", "50.00", "10000.00", "500000"),
                new ResidentInvoiceLine("WASTE_FEE", "Phí rác sinh hoạt", "PER_HOUSEHOLD", "1.00", "HOUSEHOLD", null, "30000.00", "30000")
        );
        var detail = new ResidentInvoiceDetail(42L, "2026-11", "S10-H-A", "S10-A-01", "2026-11-30",
                "2026-11-09T00:00:00Z", null, "ISSUED", "PARTIALLY_PAID", "530000", "200000", "330000", lines);
        when(service.invoiceDetail(any(), eq(42L))).thenReturn(detail);

        mvc.perform(get("/api/v1/resident/invoices/42").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceId").value(42))
                .andExpect(jsonPath("$.period").value("2026-11"))
                .andExpect(jsonPath("$.householdCode").value("S10-H-A"))
                .andExpect(jsonPath("$.apartmentCode").value("S10-A-01"))
                .andExpect(jsonPath("$.dueDate").value("2026-11-30"))
                .andExpect(jsonPath("$.issuedAt").value("2026-11-09T00:00:00Z"))
                .andExpect(jsonPath("$.cancelledAt").doesNotExist())
                .andExpect(jsonPath("$.invoiceStatus").value("ISSUED"))
                .andExpect(jsonPath("$.paymentStatus").value("PARTIALLY_PAID"))
                .andExpect(jsonPath("$.total").value("530000"))
                .andExpect(jsonPath("$.settled").value("200000"))
                .andExpect(jsonPath("$.remaining").value("330000"))
                .andExpect(jsonPath("$.lines[0].feeTypeCode").value("MANAGEMENT_FEE"))
                .andExpect(jsonPath("$.lines[0].calculationMethod").value("AREA"))
                .andExpect(jsonPath("$.lines[0].unit").value("M2"))
                .andExpect(jsonPath("$.lines[0].areaM2").value("50.00"))
                .andExpect(jsonPath("$.lines[0].unitPrice").value("10000.00"))
                .andExpect(jsonPath("$.lines[0].amount").value("500000"))
                .andExpect(jsonPath("$.lines[1].feeTypeCode").value("WASTE_FEE"))
                .andExpect(jsonPath("$.lines[1].calculationMethod").value("PER_HOUSEHOLD"))
                .andExpect(jsonPath("$.lines[1].unit").value("HOUSEHOLD"))
                .andExpect(jsonPath("$.lines[1].areaM2").doesNotExist())
                .andExpect(jsonPath("$.lines[1].unitPrice").value("30000.00"))
                .andExpect(jsonPath("$.lines[1].amount").value("30000"))
                // Whitelist checks: no forbidden fields
                .andExpect(jsonPath("$.lines[0].feeRateId").doesNotExist())
                .andExpect(jsonPath("$.lines[0].actor").doesNotExist())
                .andExpect(jsonPath("$.householdId").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist());
    }

    @Test void receiptDtoStructureAndWhitelist() throws Exception {
        var receipt = new ResidentReceipt(100L, 42L, "2026-11", "2026-11-08", "2026-11-09T10:00:00Z",
                "200000", "200000", "0", "POSTED", null, true, false);
        when(service.receipts(any(), any(), any(), eq(0), eq(20)))
                .thenReturn(new PageResponse<>(List.of(receipt), 0, 20, 1, 1));

        mvc.perform(get("/api/v1/resident/receipts").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].receiptId").value(100))
                .andExpect(jsonPath("$.content[0].invoiceId").value(42))
                .andExpect(jsonPath("$.content[0].period").value("2026-11"))
                .andExpect(jsonPath("$.content[0].paidOn").value("2026-11-08"))
                .andExpect(jsonPath("$.content[0].amount").value("200000"))
                .andExpect(jsonPath("$.content[0].appliedAmount").value("200000"))
                .andExpect(jsonPath("$.content[0].creditAmount").value("0"))
                .andExpect(jsonPath("$.content[0].status").value("POSTED"))
                .andExpect(jsonPath("$.content[0].hasLateReason").value(true))
                .andExpect(jsonPath("$.content[0].hasVoidReason").value(false))
                // Whitelist: no raw reasons, no staff, no debtAfter, no householdId
                .andExpect(jsonPath("$.content[0].lateReason").doesNotExist())
                .andExpect(jsonPath("$.content[0].voidReason").doesNotExist())
                .andExpect(jsonPath("$.content[0].recordedBy").doesNotExist())
                .andExpect(jsonPath("$.content[0].voidedBy").doesNotExist())
                .andExpect(jsonPath("$.content[0].debtAfter").doesNotExist())
                .andExpect(jsonPath("$.content[0].householdId").doesNotExist());
    }

    @Test void creditAllocationDtoStructureAndWhitelist() throws Exception {
        var allocation = new ResidentAllocation(200L, 42L, "2026-11", "50000", "ACTIVE",
                "2026-11-09T12:00:00Z", null, false);
        when(service.creditAllocations(any(), any(), any(), eq(0), eq(20)))
                .thenReturn(new PageResponse<>(List.of(allocation), 0, 20, 1, 1));

        mvc.perform(get("/api/v1/resident/credit-allocations").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].allocationId").value(200))
                .andExpect(jsonPath("$.content[0].invoiceId").value(42))
                .andExpect(jsonPath("$.content[0].period").value("2026-11"))
                .andExpect(jsonPath("$.content[0].amount").value("50000"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].hasReverseReason").value(false))
                // Whitelist: no raw reasons, no staff, no householdId, no sources
                .andExpect(jsonPath("$.content[0].reverseReason").doesNotExist())
                .andExpect(jsonPath("$.content[0].recordedBy").doesNotExist())
                .andExpect(jsonPath("$.content[0].reversedBy").doesNotExist())
                .andExpect(jsonPath("$.content[0].sources").doesNotExist())
                .andExpect(jsonPath("$.content[0].householdId").doesNotExist());
    }

    @Test void billingSummaryDtoStructureAndWhitelist() throws Exception {
        var summary = new ResidentBillingSummary("330000", 1L, "2026-11-30", "2026-11", "70000");
        when(service.billingSummary(any())).thenReturn(summary);

        mvc.perform(get("/api/v1/resident/billing-summary").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOutstanding").value("330000"))
                .andExpect(jsonPath("$.outstandingInvoiceCount").value(1))
                .andExpect(jsonPath("$.nearestDueDate").value("2026-11-30"))
                .andExpect(jsonPath("$.nearestDuePeriod").value("2026-11"))
                .andExpect(jsonPath("$.creditBalance").value("70000"))
                .andExpect(jsonPath("$.householdId").doesNotExist());
    }

    // 10. Large numbers (> 2^53) as strings in responses
    @Test void largeNumbersExactStrings() throws Exception {
        String large = "9007199254740993000";
        var summary = new ResidentBillingSummary(large, 1L, "2026-11-30", "2026-11", large);
        when(service.billingSummary(any())).thenReturn(summary);

        mvc.perform(get("/api/v1/resident/billing-summary").with(authentication(auth("RESIDENT", 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOutstanding").value(large))
                .andExpect(jsonPath("$.creditBalance").value(large));
    }
}
