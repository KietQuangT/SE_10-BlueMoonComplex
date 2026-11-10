package vn.edu.se10.bluemoon.reporting.application;

import java.time.Instant;

public record ReportMetadata(
    String templateId,
    String version,
    Instant exportedAt,
    String scopeDescription
) {}
