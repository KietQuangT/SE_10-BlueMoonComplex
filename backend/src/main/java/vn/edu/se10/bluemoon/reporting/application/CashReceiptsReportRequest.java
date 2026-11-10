package vn.edu.se10.bluemoon.reporting.application;

import java.time.LocalDate;

public record CashReceiptsReportRequest(LocalDate from, LocalDate to) {}
