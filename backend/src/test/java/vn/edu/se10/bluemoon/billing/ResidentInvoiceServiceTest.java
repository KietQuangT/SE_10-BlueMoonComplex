package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import vn.edu.se10.bluemoon.billing.api.PageResponse;
import vn.edu.se10.bluemoon.billing.api.ResidentBillingSummary;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceDetail;
import vn.edu.se10.bluemoon.billing.application.ResidentInvoiceService;
import vn.edu.se10.bluemoon.billing.persistence.ResidentInvoiceReadStore;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

class ResidentInvoiceServiceTest {
    private final ResidentInvoiceReadStore readStore = mock(ResidentInvoiceReadStore.class);
    private final HouseholdLookup householdLookup = mock(HouseholdLookup.class);
    private ResidentInvoiceService service;

    @BeforeEach void setUp() {
        service = new ResidentInvoiceService(readStore, householdLookup);
        when(householdLookup.findStatus(10L)).thenReturn(Optional.of("ACTIVE"));
    }

    private static AuthenticatedUser resident(Long householdId) {
        return new AuthenticatedUser(1L, "resident", "Resident Name", "RESIDENT", householdId, false);
    }

    @Test void residentWithNullHouseholdThrowsAccessDenied() {
        assertThrows(AccessDeniedException.class, () ->
                service.invoices(resident(null), null, null, 0, 20));
        assertThrows(AccessDeniedException.class, () ->
                service.invoiceDetail(resident(null), 1L));
        assertThrows(AccessDeniedException.class, () ->
                service.receipts(resident(null), null, null, 0, 20));
        assertThrows(AccessDeniedException.class, () ->
                service.creditAllocations(resident(null), null, null, 0, 20));
        assertThrows(AccessDeniedException.class, () ->
                service.billingSummary(resident(null)));
    }

    @Test void residentWithNonExistentHouseholdThrowsAccessDenied() {
        when(householdLookup.findStatus(99L)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () ->
                service.invoices(resident(99L), null, null, 0, 20));
    }

    @Test void residentWithInactiveHouseholdIsAllowed() {
        when(householdLookup.findStatus(20L)).thenReturn(Optional.of("INACTIVE"));
        when(readStore.invoices(eq(20L), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
        var result = service.invoices(resident(20L), null, null, 0, 20);
        assertEquals(0, result.totalElements());
    }

    @Test void nonResidentRoleThrowsAccessDenied() {
        var admin = new AuthenticatedUser(2L, "admin", "Admin", "ADMIN", 10L, false);
        assertThrows(AccessDeniedException.class, () ->
                service.invoices(admin, null, null, 0, 20));
    }

    @Test void pagingValidation() {
        var ex1 = assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), null, null, -1, 20));
        assertEquals("INVALID_FILTER", ex1.code());
        var ex2 = assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), null, null, 0, 0));
        assertEquals("INVALID_FILTER", ex2.code());
        var ex3 = assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), null, null, 0, 101));
        assertEquals("INVALID_FILTER", ex3.code());
    }

    @Test void periodValidation() {
        assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), "invalid-period", null, 0, 20));
        assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), "0999-12", null, 0, 20));
        assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), "2026-13", null, 0, 20));

        // valid periods
        when(readStore.invoices(10L, "2026-11", "ALL", 0, 20))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));
        service.invoices(resident(10L), "2026-11", null, 0, 20);
        verify(readStore).invoices(10L, "2026-11", "ALL", 0, 20);
    }

    @Test void paymentStatusValidation() {
        for (String valid : List.of("ALL", "OUTSTANDING", "UNPAID", "PARTIALLY_PAID", "PAID", "CANCELLED")) {
            service.invoices(resident(10L), null, valid, 0, 20);
        }
        assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), null, "paid", 0, 20));
        assertThrows(BadRequestException.class, () ->
                service.invoices(resident(10L), null, "OTHER", 0, 20));
    }

    @Test void receiptStatusValidation() {
        for (String valid : List.of("ALL", "POSTED", "VOIDED")) {
            service.receipts(resident(10L), null, valid, 0, 20);
        }
        assertThrows(BadRequestException.class, () ->
                service.receipts(resident(10L), null, "posted", 0, 20));
        assertThrows(BadRequestException.class, () ->
                service.receipts(resident(10L), null, "UNKNOWN", 0, 20));
    }

    @Test void allocationStatusValidation() {
        for (String valid : List.of("ALL", "ACTIVE", "REVERSED")) {
            service.creditAllocations(resident(10L), null, valid, 0, 20);
        }
        assertThrows(BadRequestException.class, () ->
                service.creditAllocations(resident(10L), null, "active", 0, 20));
        assertThrows(BadRequestException.class, () ->
                service.creditAllocations(resident(10L), null, "UNKNOWN", 0, 20));
    }

    @Test void invoiceIdValidation() {
        assertThrows(BadRequestException.class, () ->
                service.invoiceDetail(resident(10L), 0L));
        assertThrows(BadRequestException.class, () ->
                service.invoiceDetail(resident(10L), -5L));
        assertThrows(BadRequestException.class, () ->
                service.receipts(resident(10L), 0L, null, 0, 20));
        assertThrows(BadRequestException.class, () ->
                service.creditAllocations(resident(10L), -1L, null, 0, 20));
    }

    @Test void delegationToReadStore() {
        when(readStore.invoiceDetail(10L, 42L))
                .thenReturn(new ResidentInvoiceDetail(42L, "2026-11", "H1", "A1", "2026-11-30",
                        "2026-11-09T00:00:00Z", null, "ISSUED", "PAID", "100", "100", "0", List.of()));
        var detail = service.invoiceDetail(resident(10L), 42L);
        assertEquals(42L, detail.invoiceId());

        when(readStore.billingSummary(10L))
                .thenReturn(new ResidentBillingSummary("100", 1L, "2026-11-30", "2026-11", "0"));
        var summary = service.billingSummary(resident(10L));
        assertEquals("100", summary.totalOutstanding());
    }
}
