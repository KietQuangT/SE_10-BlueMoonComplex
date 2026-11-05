package vn.edu.se10.bluemoon.systeminfo.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.systeminfo.application.SystemInfoService;

@RestController
@RequestMapping("/api/v1/system/info")
public class SystemInfoController {
    private final SystemInfoService service;

    public SystemInfoController(SystemInfoService service) { this.service = service; }

    @GetMapping
    public SystemInfoResponse read() {
        var info = service.read();
        return new SystemInfoResponse(info.name(), info.stage());
    }
}
