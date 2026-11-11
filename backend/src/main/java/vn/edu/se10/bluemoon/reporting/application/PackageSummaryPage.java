package vn.edu.se10.bluemoon.reporting.application;

import java.util.List;

public record PackageSummaryPage(
    List<PackageSummary> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
