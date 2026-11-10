package vn.edu.se10.bluemoon.reporting.application;

public record ResidencyReportTotals(
    long residentCount,
    long residingCount,
    long temporarilyAbsentCount
) {}
