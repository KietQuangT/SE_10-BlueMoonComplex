package vn.edu.se10.bluemoon.reporting.persistence;

import java.time.Instant;

public record AgencyPackageRecord(
    Long id,
    String status,
    Long version,
    String templateId,
    String templateVersion,
    String recipientAgencyCode,
    String requestReference,
    String licenceReference,
    Long createdBy,
    Instant createdAt,
    Long approvedBy,
    Instant approvedAt,
    Long rejectedBy,
    Instant rejectedAt,
    Long revokedBy,
    Instant revokedAt,
    Long handedOffBy,
    Instant handedOffAt,
    Instant downloadUntil,
    Instant contentDeleteAfter,
    Instant contentDeletedAt,
    String contentSha256,
    Long householdCount,
    Long residentCount,
    Long residingCount,
    Long temporarilyAbsentCount
) {}
