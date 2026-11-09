package vn.edu.se10.bluemoon.billing.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Set;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.billing.application.ResidentInvoiceService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;

@RestController
@RequestMapping("/api/v1/resident")
public class ResidentInvoiceController {
    private static final Set<String> INVOICE_QUERY_KEYS = Set.of("period", "paymentStatus", "page", "size");
    private static final Set<String> RECEIPT_QUERY_KEYS = Set.of("invoiceId", "status", "page", "size");
    private static final Set<String> ALLOCATION_QUERY_KEYS = Set.of("invoiceId", "status", "page", "size");
    private static final Set<String> EMPTY_QUERY_KEYS = Set.of();

    private final ResidentInvoiceService service;

    public ResidentInvoiceController(ResidentInvoiceService service) {
        this.service = service;
    }

    @GetMapping("/invoices")
    public ResponseEntity<PageResponse<ResidentInvoiceSummary>> invoices(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request
    ) {
        checkAllowedQueryKeys(request, INVOICE_QUERY_KEYS);
        var result = service.invoices(user, period, paymentStatus, page, size);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<ResidentInvoiceDetail> invoiceDetail(
            @PathVariable long invoiceId,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request
    ) {
        checkAllowedQueryKeys(request, EMPTY_QUERY_KEYS);
        var result = service.invoiceDetail(user, invoiceId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @GetMapping("/receipts")
    public ResponseEntity<PageResponse<ResidentReceipt>> receipts(
            @RequestParam(required = false) Long invoiceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request
    ) {
        checkAllowedQueryKeys(request, RECEIPT_QUERY_KEYS);
        var result = service.receipts(user, invoiceId, status, page, size);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @GetMapping("/credit-allocations")
    public ResponseEntity<PageResponse<ResidentAllocation>> creditAllocations(
            @RequestParam(required = false) Long invoiceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request
    ) {
        checkAllowedQueryKeys(request, ALLOCATION_QUERY_KEYS);
        var result = service.creditAllocations(user, invoiceId, status, page, size);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    @GetMapping("/billing-summary")
    public ResponseEntity<ResidentBillingSummary> billingSummary(
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request
    ) {
        checkAllowedQueryKeys(request, EMPTY_QUERY_KEYS);
        var result = service.billingSummary(user);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(result);
    }

    private static void checkAllowedQueryKeys(HttpServletRequest request, Set<String> allowed) {
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!allowed.contains(name)) {
                throw new BadRequestException("INVALID_FILTER");
            }
        }
    }
}
