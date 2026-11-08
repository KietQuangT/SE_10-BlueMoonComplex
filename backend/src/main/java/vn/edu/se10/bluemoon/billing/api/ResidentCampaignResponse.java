package vn.edu.se10.bluemoon.billing.api;

import java.time.LocalDate;

public record ResidentCampaignResponse(long id, String name, String description, LocalDate opensOn,
                                       LocalDate closesOn, String status) { }
