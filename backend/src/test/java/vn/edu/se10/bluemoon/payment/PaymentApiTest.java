package vn.edu.se10.bluemoon.payment;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;
import vn.edu.se10.bluemoon.payment.api.PaymentController;
import vn.edu.se10.bluemoon.payment.application.PaymentService;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentApiTest {
    @Autowired MockMvc mvc;
    @MockitoBean PaymentService payments;
    @MockitoBean CurrentAccountService currentAccount;
    @BeforeEach void refresh() {
        when(currentAccount.refresh(anyLong())).thenAnswer(i -> (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
    private static UsernamePasswordAuthenticationToken principal(String role) {
        return UsernamePasswordAuthenticationToken.authenticated(new AuthenticatedUser(1, "test", "Test", role, null, false), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
    @Test void accountantCanSubmitValidReceipt() throws Exception {
        mvc.perform(post("/api/v1/invoices/1/receipts").with(csrf()).with(authentication(principal("ACCOUNTANT")))
                .contentType("application/json").content("{\"amount\":100,\"paidOn\":\"2026-11-09\",\"idempotencyKey\":\"00000000-0000-0000-0000-000000000001\"}"))
                .andExpect(status().isCreated());
    }
    @Test void listResponseIncludesStringAmountsAndStatus() throws Exception {
        var item = new vn.edu.se10.bluemoon.billing.application.PaymentInvoiceQuery.Invoice(1, 2, 3, "H-1", "A-1", "2026-11", "2026-11-30", "530000", "200000", "330000", "PARTIALLY_PAID");
        when(payments.paymentInvoices(any(), any(), any(), eq(0), eq(20))).thenReturn(new vn.edu.se10.bluemoon.billing.application.PaymentInvoiceQuery.Page(List.of(item), 0, 20, 1, 1));
        mvc.perform(get("/api/v1/payment-invoices").with(authentication(principal("ACCOUNTANT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].remaining").value("330000"))
                .andExpect(jsonPath("$.content[0].paymentStatus").value("PARTIALLY_PAID"));
    }
    @Test void receiptResponseShowsReasonFlagsWithoutRawReason() throws Exception {
        var receipt = new PaymentService.Receipt(5, 1, "2026-11-08", "100", "100", "0", "0", "POSTED", "2026-11-09T00:00:00Z", true, false);
        when(payments.receipt(eq(1L), any(), any(), eq("private reason"), any(), eq(1L))).thenReturn(receipt);
        mvc.perform(post("/api/v1/invoices/1/receipts").with(csrf()).with(authentication(principal("ACCOUNTANT")))
                .contentType("application/json").content("{\"amount\":100,\"paidOn\":\"2026-11-08\",\"lateReason\":\"private reason\",\"idempotencyKey\":\"00000000-0000-0000-0000-000000000001\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.hasLateReason").value(true))
                .andExpect(jsonPath("$.lateReason").doesNotExist()).andExpect(jsonPath("$.voidReason").doesNotExist());
    }
    @Test void readHistoryShowsPresenceFlagsWithoutRawReasonFields() throws Exception {
        var receipt = new PaymentService.ReceiptHistory(5, 1, "2026-11-08", "100", "100", "0", "0", "VOIDED", "2026-11-09T00:00:00Z", 1, true, 1L, "2026-11-09T01:00:00Z", true, 0, false);
        when(payments.receipts(1L, 0, 20)).thenReturn(new PaymentService.Page(List.of(receipt), 0, 20, 1, 1));
        mvc.perform(get("/api/v1/invoices/1/receipts").with(authentication(principal("ADMIN"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].hasLateReason").value(true))
                .andExpect(jsonPath("$.content[0].hasVoidReason").value(true))
                .andExpect(jsonPath("$.content[0].lateReason").doesNotExist()).andExpect(jsonPath("$.content[0].voidReason").doesNotExist());
        var allocation = new PaymentService.AllocationHistory(7, 1, 2, "50", "50", "REVERSED", 1, "2026-11-09T00:00:00Z", 1L, "2026-11-09T01:00:00Z", true, List.of(new PaymentService.AllocationSource(5, "50")));
        when(payments.allocations(eq(2L), any(), any(), any(), eq(0), eq(20))).thenReturn(new PaymentService.AllocationPage(List.of(allocation), 0, 20, 1, 1));
        mvc.perform(get("/api/v1/households/2/credit-allocations").with(authentication(principal("ACCOUNTANT"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].hasReverseReason").value(true))
                .andExpect(jsonPath("$.content[0].reverseReason").doesNotExist())
                .andExpect(jsonPath("$.content[0].sources[0].receiptId").value(5));
    }
    @Test void paymentRoutesEnforceRoleAuthenticationAndCsrf() throws Exception {
        for (String path : List.of("/api/v1/invoices/1/receipts", "/api/v1/invoices/1/payment-summary", "/api/v1/households/1/credit-balance", "/api/v1/payment-invoices", "/api/v1/households/1/credit-allocations")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).with(authentication(principal("RESIDENT")))).andExpect(status().isForbidden());
            mvc.perform(get(path).with(authentication(principal("ADMIN")))).andExpect(status().isOk());
            mvc.perform(get(path).with(authentication(principal("ACCOUNTANT")))).andExpect(status().isOk());
        }
        for (String path : List.of("/api/v1/invoices/1/receipts", "/api/v1/receipts/1/void", "/api/v1/households/1/credit-allocations", "/api/v1/credit-allocations/1/reverse")) {
            mvc.perform(post(path).with(csrf())).andExpect(status().isUnauthorized());
            mvc.perform(post(path).with(csrf()).with(authentication(principal("ADMIN")))).andExpect(status().isForbidden());
            mvc.perform(post(path).with(csrf()).with(authentication(principal("RESIDENT")))).andExpect(status().isForbidden());
            mvc.perform(post(path).with(authentication(principal("ACCOUNTANT")))).andExpect(status().isForbidden());
            mvc.perform(post(path).with(csrf().useInvalidToken()).with(authentication(principal("ACCOUNTANT")))).andExpect(status().isForbidden());
        }
    }
}
