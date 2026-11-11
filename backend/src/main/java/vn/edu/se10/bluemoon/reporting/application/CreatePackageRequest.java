package vn.edu.se10.bluemoon.reporting.application;

import java.util.List;

public record CreatePackageRequest(
    List<Long> householdIds,
    String recipientAgencyCode,
    String requestReference,
    String licenceReference
) {}
