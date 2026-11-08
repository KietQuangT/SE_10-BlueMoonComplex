package vn.edu.se10.bluemoon.billing.api;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;

@RestController
@RequestMapping("/api/v1")
public class BillingPeriodController {
    private final BillingPeriodService periods;
    public BillingPeriodController(BillingPeriodService periods) { this.periods = periods; }

    public record CreateRequest(String period, String dueDate) { }
    public record UpdateRequest(String dueDate, Long version) { }
    public record VersionRequest(Long version) { }
    public record IssueRequest(Long version, Long revision) { }
    public record ReasonRequest(Long version, String reason) { }

    @GetMapping("/billing-periods")
    public PageResponse<BillingPeriodResponse> list(@RequestParam(required = false) String period, @RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
                                                    @AuthenticationPrincipal AuthenticatedUser actor) {
        return periods.list(period, status, page, size, actor.role());
    }

    @PostMapping("/billing-periods")
    public ResponseEntity<BillingPeriodResponse> create(@RequestBody CreateRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        var created = periods.create(request.period(), date(request.dueDate()), actor.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(periods.detail(created.getId(), actor.role()));
    }

    @GetMapping("/billing-periods/{id}")
    public BillingPeriodResponse detail(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        return periods.detail(id, actor.role());
    }

    @PatchMapping("/billing-periods/{id}")
    public BillingPeriodResponse update(@PathVariable long id, @RequestBody UpdateRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        periods.updateDueDate(id, date(request.dueDate()), number(request.version()), actor.userId());
        return periods.detail(id, actor.role());
    }

    @GetMapping("/billing-periods/{id}/preview")
    public BillingSubmissionResponse preview(@PathVariable long id, @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return periods.preview(id, page, size);
    }

    @PostMapping("/billing-periods/{id}/submit")
    public BillingPeriodResponse submit(@PathVariable long id, @RequestBody VersionRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        periods.submit(id, number(request.version()), actor.userId());
        return periods.detail(id, actor.role());
    }

    @PostMapping("/billing-periods/{id}/withdraw")
    public BillingPeriodResponse withdraw(@PathVariable long id, @RequestBody VersionRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        periods.withdraw(id, number(request.version()), actor.userId());
        return periods.detail(id, actor.role());
    }

    @PostMapping("/billing-periods/{id}/return")
    public BillingPeriodResponse returnToDraft(@PathVariable long id, @RequestBody ReasonRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        periods.returnToDraft(id, number(request.version()), request.reason(), actor.userId());
        return periods.detail(id, actor.role());
    }

    @PostMapping("/billing-periods/{id}/issue")
    public BillingPeriodResponse issue(@PathVariable long id, @RequestBody IssueRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        periods.issue(id, number(request.version()), number(request.revision()), actor.userId());
        return periods.detail(id, actor.role());
    }

    @PostMapping("/billing-periods/{id}/cancel")
    public BillingPeriodResponse cancel(@PathVariable long id, @RequestBody ReasonRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        periods.cancel(id, number(request.version()), request.reason(), actor.userId());
        return periods.detail(id, actor.role());
    }

    @GetMapping("/billing-periods/{id}/submissions")
    public BillingSubmissionResponse submission(@PathVariable long id, @RequestParam(required = false) Long revision,
                                                 @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return periods.submission(id, revision, page, size);
    }

    @GetMapping("/billing-periods/{id}/invoices")
    public PageResponse<InvoiceResponse> invoices(@PathVariable long id, @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return periods.invoices(id, page, size);
    }

    @GetMapping("/invoices/{id}")
    public InvoiceResponse invoice(@PathVariable long id) { return periods.invoice(id); }

    private static LocalDate date(String value) {
        try {
            if (value == null) throw new DateTimeParseException("missing", "", 0);
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("INVALID_DATE");
        }
    }

    private static long number(Long value) {
        if (value == null || value < 0) throw new BadRequestException("VALIDATION");
        return value;
    }
}
