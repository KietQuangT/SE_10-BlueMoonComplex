package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaignRepository;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContribution;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContributionRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

class VoluntaryContributionServiceTest {
    private final VoluntaryContributionRepository contributions = mock(VoluntaryContributionRepository.class);
    private final VoluntaryCampaignRepository campaigns = mock(VoluntaryCampaignRepository.class);
    private final ResidentDonorLookup donors = mock(ResidentDonorLookup.class);
    private final AuditService audit = mock(AuditService.class);
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC);
    private final VoluntaryContributionService service = new VoluntaryContributionService(contributions, campaigns, donors, audit, clock, manager);
    private final String key = "6fbca292-b3b5-44de-bbb8-291b9ee1c213";

    @Test
    void replayAfterVoidReturnsOriginalWithoutCheckingCampaignOrAuditing() {
        var input = ContributionInput.parse(3, 4, "100", LocalDate.of(2026, 11, 1), null, key);
        var donor = new ResidentDonorLookup.Donor(4, 5, "Test", "H1", "A1");
        var existing = makeContribution(input, donor);
        ReflectionTestUtils.setField(existing, "status", "VOIDED");
        when(contributions.findByRecordedByAndIdempotencyKey(8, key)).thenReturn(Optional.of(existing));
        var result = service.record(input, 8);
        assertTrue(result.replay());
        assertSame(existing, result.contribution());
        verifyNoInteractions(campaigns, donors, audit, manager);
    }

    @Test
    void sameActorAndKeyWithDifferentPayloadConflicts() {
        var original = ContributionInput.parse(3, 4, "100", LocalDate.of(2026, 11, 1), null, key);
        var changed = ContributionInput.parse(3, 4, "200", LocalDate.of(2026, 11, 1), null, key);
        var existing = makeContribution(original, new ResidentDonorLookup.Donor(4, 5, "Test", "H1", "A1"));
        when(contributions.findByRecordedByAndIdempotencyKey(8, key)).thenReturn(Optional.of(existing));
        assertEquals("IDEMPOTENCY_CONFLICT", assertThrows(ConflictException.class, () -> service.record(changed, 8)).code());
        verifyNoInteractions(campaigns, donors, audit, manager);
    }
    @Test
    void concurrentReplayIsCheckedAgainAfterCampaignLockBeforeDateValidation() {
        var input = ContributionInput.parse(3, 4, "100", LocalDate.of(2026, 11, 1), null, key);
        var existing = makeContribution(input, new ResidentDonorLookup.Donor(4, 5, "Test", "H1", "A1"));
        var closed = mock(VoluntaryCampaign.class);
        when(contributions.findByRecordedByAndIdempotencyKey(8, key))
                .thenReturn(Optional.empty(), Optional.empty(), Optional.of(existing));
        when(campaigns.lockById(3)).thenReturn(Optional.of(closed));
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        var result = service.record(input, 8);
        assertTrue(result.replay());
        assertSame(existing, result.contribution());
        verifyNoInteractions(donors, audit);
    }

    @Test
    void receiptDateUsesVietnamesePublicationDayAtUtcBoundary() {
        var input = ContributionInput.parse(3, 4, "100", LocalDate.of(2026, 11, 7), null, key);
        var campaign = new VoluntaryCampaign("Test", null, LocalDate.of(2026, 11, 7), LocalDate.of(2026, 11, 30), 1, clock.instant());
        campaign.publish(1, Instant.parse("2026-11-07T17:30:00Z"));
        when(campaigns.lockById(3)).thenReturn(Optional.of(campaign));
        when(contributions.findByRecordedByAndIdempotencyKey(8, key)).thenReturn(Optional.empty());
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        assertEquals("INVALID_PAID_DATE", assertThrows(ConflictException.class, () -> service.record(input, 8)).code());
        verifyNoInteractions(donors, audit);
    }

    private VoluntaryContribution makeContribution(ContributionInput input, ResidentDonorLookup.Donor donor) {
        return new VoluntaryContribution(input.campaignId(), donor.id(), donor.householdId(), donor.fullName(),
                donor.householdCode(), donor.apartmentCode(), input.amount(), input.paidOn(), input.lateReason(),
                input.idempotencyKey(), input.fingerprint(), 8, clock.instant());
    }

}
