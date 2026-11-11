package vn.edu.se10.bluemoon.reporting.application;

import java.util.List;

public record PackagePreviewResponse(
    Long packageId,
    Long version,
    String contentSha256,
    List<AgencyPackageRow> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
