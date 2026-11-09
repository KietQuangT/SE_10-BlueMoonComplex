package vn.edu.se10.bluemoon.payment.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.payment.application.PaymentService;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService payments;
    public PaymentController(PaymentService payments) { this.payments = payments; }
    public record ReceiptRequest(BigDecimal amount, LocalDate paidOn, String lateReason, String idempotencyKey) { }
    public record AllocationRequest(Long invoiceId, BigDecimal amount, String idempotencyKey) { }
    public record ReasonRequest(String reason, String idempotencyKey) { }

    @PostMapping("/invoices/{invoiceId}/receipts")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentService.Receipt receipt(@PathVariable long invoiceId, @RequestBody ReceiptRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return payments.receipt(invoiceId, request.amount(), request.paidOn(), request.lateReason(), request.idempotencyKey(), actor.userId());
    }
    @GetMapping("/payment-invoices")
    public vn.edu.se10.bluemoon.billing.application.PaymentInvoiceQuery.Page invoices(
            @RequestParam(required = false) String q, @RequestParam(required = false) String period,
            @RequestParam(required = false) String paymentStatus, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return payments.paymentInvoices(q, period, paymentStatus, page, size);
    }
    @GetMapping("/households/{householdId}/credit-allocations")
    public PaymentService.AllocationPage allocations(@PathVariable long householdId,
            @RequestParam(required = false) Long invoiceId, @RequestParam(required = false) Long receiptId,
            @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return payments.allocations(householdId, invoiceId, receiptId, status, page, size);
    }
    @GetMapping("/invoices/{invoiceId}/receipts")
    public PaymentService.Page receipts(@PathVariable long invoiceId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return payments.receipts(invoiceId, page, size);
    }
    @GetMapping("/invoices/{invoiceId}/payment-summary")
    public PaymentService.Summary summary(@PathVariable long invoiceId) { return payments.summary(invoiceId); }
    @PostMapping("/receipts/{receiptId}/void")
    public PaymentService.Receipt voidReceipt(@PathVariable long receiptId, @RequestBody ReasonRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return payments.voidReceipt(receiptId, request.reason(), request.idempotencyKey(), actor.userId());
    }
    @GetMapping("/households/{householdId}/credit-balance")
    public PaymentService.Balance balance(@PathVariable long householdId) { return payments.creditBalance(householdId); }
    @PostMapping("/households/{householdId}/credit-allocations")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentService.Allocation allocate(@PathVariable long householdId, @RequestBody AllocationRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        if (request.invoiceId() == null) throw new vn.edu.se10.bluemoon.common.error.BadRequestException("VALIDATION");
        return payments.allocate(householdId, request.invoiceId(), request.amount(), request.idempotencyKey(), actor.userId());
    }
    @PostMapping("/credit-allocations/{allocationId}/reverse")
    public PaymentService.Allocation reverse(@PathVariable long allocationId, @RequestBody ReasonRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return payments.reverse(allocationId, request.reason(), request.idempotencyKey(), actor.userId());
    }
}
