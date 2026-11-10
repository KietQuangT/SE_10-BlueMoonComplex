package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.se10.bluemoon.billing.api.*;
import vn.edu.se10.bluemoon.billing.application.ResidentInvoiceService;
import vn.edu.se10.bluemoon.config.SecurityConfig;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.identity.application.CurrentAccountService;

@WebMvcTest(ResidentInvoiceController.class)
@Import(SecurityConfig.class)
class ResidentInvoicePrivacyTest {
    @Autowired MockMvc mvc;
    @MockitoBean ResidentInvoiceService service;
    @MockitoBean CurrentAccountService accounts;
    AuthenticatedUser resident = new AuthenticatedUser(1L, "private-user", "private-name", "RESIDENT", 10L, false);
    UsernamePasswordAuthenticationToken auth;

    @BeforeEach void setup() {
        auth = UsernamePasswordAuthenticationToken.authenticated(resident, null, List.of(new SimpleGrantedAuthority("ROLE_RESIDENT")));
        when(accounts.refresh(1L)).thenReturn(resident);
    }
    Map<String, Object> json(String route) throws Exception {
        String body = mvc.perform(get(route).with(authentication(auth))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(body.contains("private-user"));
        assertFalse(body.contains("private-name"));
        return JsonPath.parse(body).read("$");
    }
    @SuppressWarnings("unchecked")
    Map<String,Object> first(Map<String,Object> page) { return ((List<Map<String,Object>>) page.get("content")).getFirst(); }
    void envelope(Map<String,Object> page) { assertEquals(Set.of("content","page","size","totalElements","totalPages"), page.keySet()); }

    @Test void everyResidentResponseHasOnlyContractFieldsAndMoneyIsText() throws Exception {
        String large = "9007199254740993";
        var summary = new ResidentInvoiceSummary(42L,"2026-11","H1","A1","2026-11-30","2026-11-01T00:00:00Z",null,"ISSUED","UNPAID",large,"0",large);
        when(service.invoices(any(),any(),any(),anyInt(),anyInt())).thenReturn(new PageResponse<>(List.of(summary),0,20,1,1));
        var page = json("/api/v1/resident/invoices"); envelope(page);
        var invoice = first(page);
        Set<String> invoiceFields = Set.of("invoiceId","period","householdCode","apartmentCode","dueDate","issuedAt","cancelledAt","invoiceStatus","paymentStatus","total","settled","remaining");
        assertEquals(invoiceFields, invoice.keySet()); assertEquals(large, invoice.get("total")); assertInstanceOf(String.class, invoice.get("total"));
        var line = new ResidentInvoiceLine("WASTE_FEE","fee","PER_HOUSEHOLD","1.00","HOUSEHOLD",null,"10.00","10");
        when(service.invoiceDetail(any(),eq(42L))).thenReturn(new ResidentInvoiceDetail(42L,"2026-11","H1","A1","2026-11-30","2026-11-01T00:00:00Z",null,"ISSUED","UNPAID",large,"0",large,List.of(line)));
        var detail = json("/api/v1/resident/invoices/42");
        var detailFields = new java.util.HashSet<>(invoiceFields); detailFields.add("lines"); assertEquals(detailFields, detail.keySet());
        @SuppressWarnings("unchecked") var lines = (List<Map<String,Object>>) detail.get("lines");
        assertEquals(Set.of("feeTypeCode","feeTypeName","calculationMethod","quantity","unit","areaM2","unitPrice","amount"), lines.getFirst().keySet());
        assertNull(lines.getFirst().get("areaM2"));
        when(service.receipts(any(),any(),any(),anyInt(),anyInt())).thenReturn(new PageResponse<>(List.of(new ResidentReceipt(1,42,"2026-11","2026-11-01","2026-11-02T00:00:00Z",large,large,"0","POSTED",null,true,false)),0,20,1,1));
        page = json("/api/v1/resident/receipts"); envelope(page);
        var receipt = first(page); assertEquals(Set.of("receiptId","invoiceId","period","paidOn","recordedAt","amount","appliedAmount","creditAmount","status","voidedAt","hasLateReason","hasVoidReason"),receipt.keySet());
        assertEquals(large, receipt.get("amount")); assertInstanceOf(String.class, receipt.get("amount"));
        when(service.creditAllocations(any(),any(),any(),anyInt(),anyInt())).thenReturn(new PageResponse<>(List.of(new ResidentAllocation(1,42,"2026-11",large,"ACTIVE","2026-11-02T00:00:00Z",null,false)),0,20,1,1));
        page = json("/api/v1/resident/credit-allocations"); envelope(page);
        var allocation = first(page); assertEquals(Set.of("allocationId","invoiceId","period","amount","status","recordedAt","reversedAt","hasReverseReason"),allocation.keySet());
        assertEquals(large, allocation.get("amount")); assertInstanceOf(String.class, allocation.get("amount"));
        when(service.billingSummary(any())).thenReturn(new ResidentBillingSummary(large,1,"2026-11-30","2026-11","0"));
        var totals = json("/api/v1/resident/billing-summary");
        assertEquals(Set.of("totalOutstanding","outstandingInvoiceCount","nearestDueDate","nearestDuePeriod","creditBalance"), totals.keySet());
        assertEquals(large, totals.get("totalOutstanding")); assertInstanceOf(String.class, totals.get("totalOutstanding"));
    }
}
