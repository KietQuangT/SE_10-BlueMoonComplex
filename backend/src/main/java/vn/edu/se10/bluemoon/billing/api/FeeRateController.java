package vn.edu.se10.bluemoon.billing.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.se10.bluemoon.billing.application.FeeRateService;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;

@RestController
@RequestMapping("/api/v1/fee-rates")
public class FeeRateController {
    private final FeeRateService service;
    public FeeRateController(FeeRateService service) { this.service = service; }
    @GetMapping
    public FeeRatePage list(@RequestParam(required = false) Long feeTypeId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.list(feeTypeId, page, size);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeeRateResponse create(@Valid @RequestBody CreateFeeRateRequest request, @AuthenticationPrincipal AuthenticatedUser actor) {
        return service.create(request, actor.userId());
    }
    @PatchMapping("/{id}")
    public FeeRateResponse update(@PathVariable long id, @Valid @RequestBody UpdateFeeRateRequest request,
            @AuthenticationPrincipal AuthenticatedUser actor) {
        return service.update(id, request.unitPrice(), actor.userId());
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, @AuthenticationPrincipal AuthenticatedUser actor) {
        service.delete(id, actor.userId());
    }
}
