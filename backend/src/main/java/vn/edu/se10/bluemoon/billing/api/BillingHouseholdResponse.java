package vn.edu.se10.bluemoon.billing.api;

import java.util.List;

public record BillingHouseholdResponse(long householdId, String householdCode, String apartmentCode,
                                       String areaM2, List<BillingLineResponse> lines, String total) { }
