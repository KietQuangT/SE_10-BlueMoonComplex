package vn.edu.se10.bluemoon.billing.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.billing.application.FeeRateService;

@RestController
@RequestMapping("/api/v1/fee-types")
public class FeeTypeController {
    private final FeeRateService service;
    public FeeTypeController(FeeRateService service) { this.service = service; }
    @GetMapping
    public List<FeeTypeResponse> list() { return service.types(); }
}
