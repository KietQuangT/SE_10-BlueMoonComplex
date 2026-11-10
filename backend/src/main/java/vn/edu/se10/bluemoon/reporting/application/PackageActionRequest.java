package vn.edu.se10.bluemoon.reporting.application;

public record PackageActionRequest(
    Long expectedVersion,
    String contentSha256
) {}
