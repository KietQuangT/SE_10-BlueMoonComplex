package vn.edu.se10.bluemoon.billing.api;

import java.time.Instant;
import java.time.LocalDate;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContribution;

public record ContributionResponse(long id, long campaignId, long residentId, long householdId,
                                   String donorNameSnapshot, String householdCodeSnapshot, String apartmentCodeSnapshot,
                                   String amount, LocalDate paidOn, Instant recordedAt, long recordedBy, String status,
                                   String lateReason, String voidReason, Instant voidedAt, Long voidedBy, long version) {
    public static ContributionResponse from(VoluntaryContribution c) {
        return new ContributionResponse(c.getId(), c.getCampaignId(), c.getResidentId(), c.getHouseholdId(),
                c.getDonorName(), c.getHouseholdCode(), c.getApartmentCode(), c.getAmount().toPlainString(), c.getPaidOn(),
                c.getRecordedAt(), c.getRecordedBy(), c.getStatus(), c.getLateReason(), c.getVoidReason(),
                c.getVoidedAt(), c.getVoidedBy(), c.getVersion());
    }
}
