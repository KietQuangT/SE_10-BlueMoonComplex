package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaignRepository;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContribution;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContributionRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

class AuditWhitelistAndRollbackTest {
    private static final Set<String> FORBIDDEN_AUDIT_KEYS = Set.of(
            "reason", "cancelReason", "returnReason", "lateReason", "voidReason",
            "fullName", "residentName", "donorName", "householdCode", "apartmentCode",
            "nationalId", "cccd", "phone", "email", "fingerprint", "requestFingerprint",
            "idempotencyKey", "key"
    );

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void billingPeriodAuditPayloadsNeverContainForbiddenReasonsOrPII() {
        var periods = mock(BillingPeriodRepository.class);
        var audit = mock(AuditService.class);
        var snapshots = mock(BillingSnapshotStore.class);
        var households = mock(HouseholdLookup.class);
        var service = new BillingPeriodService(periods, audit, clock, households, snapshots, mock(BillingReadStore.class));

        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 42, clock.instant());
        ReflectionTestUtils.setField(period, "id", 10L);
        ReflectionTestUtils.setField(period, "version", 0L);
        when(periods.lockById(10L)).thenReturn(Optional.of(period));
        when(periods.save(any())).thenReturn(period);

        // 1. Return to draft with sensitive reason
        ReflectionTestUtils.setField(period, "status", "PENDING_APPROVAL");
        service.returnToDraft(10L, 0L, "Nhân khẩu Nguyễn Văn A (CCCD: 001234567890) yêu cầu giảm diện tích", 42L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> returnCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("BILLING_PERIOD_RETURNED"), eq("BillingPeriod"), eq("10"), returnCaptor.capture());
        assertForbiddenKeysAbsent(returnCaptor.getValue());
        assertEquals(Set.of("period", "revision", "status"), returnCaptor.getValue().keySet());

        // 2. Cancel with sensitive reason
        ReflectionTestUtils.setField(period, "status", "ISSUED");
        ReflectionTestUtils.setField(period, "version", 1L);
        service.cancel(10L, 1L, "Hủy do nhân khẩu BĐT khiếu nại sai đơn giá 0912345678", 42L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> cancelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("BILLING_PERIOD_CANCELLED"), eq("BillingPeriod"), eq("10"), cancelCaptor.capture());
        assertForbiddenKeysAbsent(cancelCaptor.getValue());
        assertEquals(Set.of("period", "status"), cancelCaptor.getValue().keySet());
    }

    @Test
    void voluntaryContributionAuditPayloadsNeverContainDonorPIIOrReasonsOrFingerprint() {
        var campaignRepo = mock(VoluntaryCampaignRepository.class);
        var contributionRepo = mock(VoluntaryContributionRepository.class);
        var donorLookup = mock(ResidentDonorLookup.class);
        var audit = mock(AuditService.class);
        var manager = mock(PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        var service = new VoluntaryContributionService(contributionRepo, campaignRepo, donorLookup, audit, clock, manager);

        var campaign = new VoluntaryCampaign("Đợt lũ lụt", "Mô tả", LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30), 1, Instant.parse("2026-11-01T00:00:00Z"));
        ReflectionTestUtils.setField(campaign, "id", 5L);
        campaign.publish(1, Instant.parse("2026-11-01T01:00:00Z"));
        when(campaignRepo.lockById(5L)).thenReturn(Optional.of(campaign));
        when(donorLookup.lockById(99L)).thenReturn(Optional.of(
                new ResidentDonorLookup.Donor(99L, 7L, "Nguyễn Văn Nhạy Cảm", "H99", "A909")));

        var contribution = new VoluntaryContribution(5L, 99L, 7L, "Nguyễn Văn Nhạy Cảm", "H99", "A909",
                new BigDecimal("500000"), LocalDate.of(2026, 11, 5), "Lý do nhập muộn nhạy cảm",
                "00000000-0000-0000-0000-000000000001", "fingerprint-secret", 42L, clock.instant());
        ReflectionTestUtils.setField(contribution, "id", 101L);
        when(contributionRepo.saveAndFlush(any())).thenReturn(contribution);
        when(contributionRepo.lockById(101L)).thenReturn(Optional.of(contribution));
        when(contributionRepo.findCampaignId(101L)).thenReturn(Optional.of(5L));

        // 1. Record contribution with late reason
        var input = ContributionInput.parse(5L, 99L, "500000", LocalDate.of(2026, 11, 5),
                "Lý do nhập muộn nhạy cảm", "00000000-0000-0000-0000-000000000001");
        service.record(input, 42L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> recordCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("VOLUNTARY_CONTRIBUTION_RECORDED"), eq("VoluntaryContribution"), eq("101"), recordCaptor.capture());
        assertForbiddenKeysAbsent(recordCaptor.getValue());
        assertEquals(Set.of("campaignId", "amount", "status", "paidOn"), recordCaptor.getValue().keySet());

        // 2. Void contribution with sensitive void reason
        service.voidContribution(101L, 0L, "Hủy vì nhầm lẫn thông tin cư dân CCCD 034123456789", 42L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> voidCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("VOLUNTARY_CONTRIBUTION_VOIDED"), eq("VoluntaryContribution"), eq("101"), voidCaptor.capture());
        assertForbiddenKeysAbsent(voidCaptor.getValue());
        assertEquals(Set.of("campaignId", "amount", "status"), voidCaptor.getValue().keySet());
    }

    @Test
    void voluntaryCampaignAuditPayloadsContainOnlyWhitelistedFields() {
        var campaignRepo = mock(VoluntaryCampaignRepository.class);
        var contributionRepo = mock(VoluntaryContributionRepository.class);
        var audit = mock(AuditService.class);
        var service = new VoluntaryCampaignService(campaignRepo, audit, clock, contributionRepo);

        var campaign = new VoluntaryCampaign("Chiến dịch ủng hộ", "Chi tiết", LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 20), 42L, clock.instant());
        ReflectionTestUtils.setField(campaign, "id", 77L);
        ReflectionTestUtils.setField(campaign, "version", 0L);
        when(campaignRepo.saveAndFlush(any())).thenAnswer(invocation -> {
            VoluntaryCampaign c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 77L);
            return c;
        });
        when(campaignRepo.lockById(77L)).thenReturn(Optional.of(campaign));

        // Create
        service.create("Chiến dịch ủng hộ", "Chi tiết", LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 20), 42L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> createCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("VOLUNTARY_CAMPAIGN_CREATED"), eq("VoluntaryCampaign"), eq("77"), createCaptor.capture());
        assertEquals(Set.of("opensOn", "closesOn", "status"), createCaptor.getValue().keySet());
        assertForbiddenKeysAbsent(createCaptor.getValue());

        // Update
        service.update(77L, 0L, "Chiến dịch ủng hộ mới", "Chi tiết mới", LocalDate.of(2026, 11, 6), LocalDate.of(2026, 11, 21), 42L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("VOLUNTARY_CAMPAIGN_UPDATED"), eq("VoluntaryCampaign"), eq("77"), updateCaptor.capture());
        assertEquals(Set.of("opensOn", "closesOn", "status"), updateCaptor.getValue().keySet());
        assertForbiddenKeysAbsent(updateCaptor.getValue());

        // Publish
        service.publish(77L, 0L, 42L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> pubCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("VOLUNTARY_CAMPAIGN_PUBLISHED"), eq("VoluntaryCampaign"), eq("77"), pubCaptor.capture());
        assertEquals(Set.of("opensOn", "closesOn", "status"), pubCaptor.getValue().keySet());
        assertEquals("PUBLISHED", pubCaptor.getValue().get("status"));

        // Close
        service.close(77L, 0L, 42L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> closeCaptor = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(42L), eq("VOLUNTARY_CAMPAIGN_CLOSED"), eq("VoluntaryCampaign"), eq("77"), closeCaptor.capture());
        assertEquals(Set.of("opensOn", "closesOn", "status"), closeCaptor.getValue().keySet());
        assertEquals("CLOSED", closeCaptor.getValue().get("status"));
    }

    @Test
    void auditExceptionPropagatesToTriggerTransactionRollback() {
        var periods = mock(BillingPeriodRepository.class);
        var audit = mock(AuditService.class);
        var snapshots = mock(BillingSnapshotStore.class);
        var service = new BillingPeriodService(periods, audit, clock, mock(HouseholdLookup.class), snapshots, mock(BillingReadStore.class));

        var period = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 42, clock.instant());
        ReflectionTestUtils.setField(period, "id", 10L);
        ReflectionTestUtils.setField(period, "status", "ISSUED");
        ReflectionTestUtils.setField(period, "version", 1L);
        ReflectionTestUtils.setField(period, "receiptCount", 0L);
        when(periods.lockById(10L)).thenReturn(Optional.of(period));

        doThrow(new RuntimeException("DB audit error")).when(audit)
                .record(eq(42L), eq("BILLING_PERIOD_CANCELLED"), anyString(), anyString(), any());

        assertThrows(RuntimeException.class, () -> service.cancel(10L, 1L, "Hủy kỳ", 42L));
    }

    private static void assertForbiddenKeysAbsent(Map<String, String> payload) {
        for (String forbidden : FORBIDDEN_AUDIT_KEYS) {
            assertFalse(payload.containsKey(forbidden), "Audit payload contains forbidden key: " + forbidden);
        }
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            for (String forbidden : FORBIDDEN_AUDIT_KEYS) {
                assertFalse(entry.getKey().toLowerCase().contains(forbidden.toLowerCase()),
                        "Audit key " + entry.getKey() + " suspiciously matches forbidden " + forbidden);
            }
        }
    }
}
