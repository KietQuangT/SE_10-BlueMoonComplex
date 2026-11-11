package vn.edu.se10.bluemoon.reporting.application;

import java.time.Instant;

public record PackageSummary(
    Long id,
    String status,
    Long version,
    String templateId,
    String templateVersion,
    Instant createdAt,
    Long createdBy,
    Instant approvedAt,
    Long approvedBy,
    Instant downloadUntil,
    Instant contentDeleteAfter,
    Instant contentDeletedAt,
    Long householdCount,
    Long residentCount,
    Instant handedOffAt,
    Instant revokedAt,
    boolean canApprove,
    boolean canReject,
    boolean canDownload,
    boolean canRevoke,
    boolean canHandoff
) {}
