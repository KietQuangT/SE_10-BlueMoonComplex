package vn.edu.se10.bluemoon.billing.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.billing.application.FeeCalculationService;

@RestController
@RequestMapping("/api/v1/fee-preview")
public class FeePreviewController {
    private final FeeCalculationService service;
    public FeePreviewController(FeeCalculationService service) { this.service = service; }
    @GetMapping
    public FeePreviewResponse preview(@RequestParam long householdId, @RequestParam(required = false) String period) {
        return service.preview(householdId, period);
    }
}
