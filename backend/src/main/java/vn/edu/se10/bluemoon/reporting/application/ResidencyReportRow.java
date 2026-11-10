package vn.edu.se10.bluemoon.reporting.application;

public record ResidencyReportRow(
    String householdCode,
    String apartmentCode,
    String fullName,
    String residencyStatus
) {}
