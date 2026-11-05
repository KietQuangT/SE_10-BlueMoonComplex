package vn.edu.se10.bluemoon.systeminfo.application;

import org.springframework.stereotype.Service;

@Service
public class SystemInfoService {
    public SystemInfo read() {
        return new SystemInfo("BlueMoon", "skeleton");
    }
}
