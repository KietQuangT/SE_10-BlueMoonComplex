package vn.edu.se10.bluemoon.reporting.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class AgencyPackageRetentionScheduler {
    private final AgencyDataPackageService packageService;

    public AgencyPackageRetentionScheduler(AgencyDataPackageService packageService) {
        this.packageService = packageService;
    }

    @Scheduled(fixedDelay = 60000)
    public void runCleanup() {
        packageService.executeRetentionCleanup();
    }
}
