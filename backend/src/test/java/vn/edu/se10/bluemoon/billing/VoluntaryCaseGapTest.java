package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import vn.edu.se10.bluemoon.billing.api.ResidentCampaignResponse;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.billing.application.VoluntaryCampaignService;
import vn.edu.se10.bluemoon.billing.application.VoluntaryContributionService;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaign;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryCampaignRepository;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContribution;
import vn.edu.se10.bluemoon.billing.persistence.VoluntaryContributionRepository;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

/**
 * Bằng chứng tự động cho các ca VOL-01, VOL-02, VOL-04, VOL-05 và VOL-06 mà test cũ chưa kiểm đủ:
 * giới hạn tên/mô tả, version, vòng đời đợt, cửa sổ ngày thực thu, lý do nhập muộn,
 * khóa idempotency, và hủy khoản đóng góp. "Hôm nay" theo giờ Việt Nam là 2026-11-08.
 */
class VoluntaryCaseGapTest {
    private static final String KEY = "6fbca292-b3b5-44de-bbb8-291b9ee1c213";
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC);
    private final VoluntaryCampaignRepository campaigns = mock(VoluntaryCampaignRepository.class);
    private final VoluntaryContributionRepository contributions = mock(VoluntaryContributionRepository.class);
    private final ResidentDonorLookup donors = mock(ResidentDonorLookup.class);
    private final AuditService audit = mock(AuditService.class);
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final VoluntaryCampaignService campaignService = new VoluntaryCampaignService(campaigns, audit, clock, contributions);
    private final VoluntaryContributionService contributionService =
            new VoluntaryContributionService(contributions, campaigns, donors, audit, clock, manager);

    // ---------------------------------------------------------------- VOL-01
    @Test
    @SuppressWarnings("unchecked")
    void createTrimsTextStartsAsDraftAndAuditsOnlyStatusAndDates() {
        when(campaigns.saveAndFlush(any())).thenAnswer(call -> {
            var saved = (VoluntaryCampaign) call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 701L);
            return saved;
        });
        var created = campaignService.create("  Quy Khuyen hoc 2026  ", "  Ung ho hoc sinh  ",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 9);

        assertEquals("Quy Khuyen hoc 2026", created.getName());
        assertEquals("Ung ho hoc sinh", created.getDescription());
        assertEquals("DRAFT", created.getStatus());
        assertEquals(0L, created.getVersion());
        assertNull(created.getPublishedAt());
        assertNull(created.getClosedAt());
        var changes = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(9L), eq("VOLUNTARY_CAMPAIGN_CREATED"), eq("VoluntaryCampaign"), eq("701"), changes.capture());
        assertEquals(Map.of("status", "DRAFT", "opensOn", "2026-11-01", "closesOn", "2026-11-30"), changes.getValue());
    }

    @Test
    void nameAndDescriptionLimitsAreEnforcedByCodePoint() {
        when(campaigns.saveAndFlush(any())).thenAnswer(call -> {
            var saved = (VoluntaryCampaign) call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        var opens = LocalDate.of(2026, 11, 1);
        var closes = LocalDate.of(2026, 11, 30);
        assertDoesNotThrow(() -> campaignService.create("A".repeat(150), "B".repeat(2000), opens, closes, 1));
        assertDoesNotThrow(() -> campaignService.create("😀".repeat(150), null, opens, closes, 1));
        for (String name : new String[] {null, "", "   ", "A".repeat(151), "😀".repeat(151)}) {
            assertEquals("VALIDATION", assertThrows(BadRequestException.class,
                    () -> campaignService.create(name, null, opens, closes, 1)).code(), String.valueOf(name));
        }
        assertEquals("VALIDATION", assertThrows(BadRequestException.class,
                () -> campaignService.create("Hop le", "B".repeat(2001), opens, closes, 1)).code());
    }

    @Test
    void closingDateBeforeOpeningDateIsInvalidButSameDayIsAllowed() {
        when(campaigns.saveAndFlush(any())).thenAnswer(call -> {
            var saved = (VoluntaryCampaign) call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 2L);
            return saved;
        });
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> campaignService.create("Dot", null, LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 8), 1)).code());
        assertDoesNotThrow(() -> campaignService.create("Dot", null, LocalDate.of(2026, 11, 8), LocalDate.of(2026, 11, 8), 1));
    }

    @Test
    void updateRejectsStaleVersionAndUnknownCampaignAndKeepsTheDraft() {
        var draft = campaign("Dot 701", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        ReflectionTestUtils.setField(draft, "id", 701L);
        ReflectionTestUtils.setField(draft, "version", 1L);
        when(campaigns.lockById(701)).thenReturn(Optional.of(draft));

        assertEquals("STALE_VERSION", assertThrows(ConflictException.class, () -> campaignService.update(701, 0, "Moi",
                null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 1)).code());
        assertEquals("CAMPAIGN_NOT_FOUND", assertThrows(NotFoundException.class, () -> campaignService.update(999999, 0, "Moi",
                null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 1)).code());
        assertEquals("Dot 701", draft.getName());
        verify(audit, never()).record(anyLong(), any(), any(), any(), any());
    }

    @Test
    void unknownCampaignIsNotFoundForStaffAndResidentDetail() {
        assertEquals("CAMPAIGN_NOT_FOUND", assertThrows(NotFoundException.class, () -> campaignService.staffDetail(999999)).code());
        assertEquals("CAMPAIGN_NOT_FOUND", assertThrows(NotFoundException.class, () -> campaignService.residentDetail(999999)).code());
    }

    // ---------------------------------------------------------------- VOL-02
    @Test
    @SuppressWarnings("unchecked")
    void publishThenCloseRecordsActorsAndAuditsAndBlocksFurtherChanges() {
        var campaign = campaign("Dot 701", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        ReflectionTestUtils.setField(campaign, "id", 701L);
        when(campaigns.lockById(701)).thenReturn(Optional.of(campaign));

        campaignService.publish(701, 0, 5);
        assertEquals("PUBLISHED", campaign.getStatus());
        assertEquals(5L, campaign.getPublishedBy());
        assertNotNull(campaign.getPublishedAt());
        // Đã công bố: không công bố lại, không sửa.
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaignService.publish(701, 0, 5)).code());
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaignService.update(701, 0, "Doi ten",
                null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), 5)).code());

        campaignService.close(701, 0, 6);
        assertEquals("CLOSED", campaign.getStatus());
        assertEquals(6L, campaign.getClosedBy());
        assertNotNull(campaign.getClosedAt());
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaignService.close(701, 0, 6)).code());
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaignService.publish(701, 0, 6)).code());

        var changes = ArgumentCaptor.forClass(Map.class);
        verify(audit).record(eq(5L), eq("VOLUNTARY_CAMPAIGN_PUBLISHED"), eq("VoluntaryCampaign"), eq("701"), changes.capture());
        assertEquals(Map.of("status", "PUBLISHED", "opensOn", "2026-11-01", "closesOn", "2026-11-30"), changes.getValue());
        verify(audit, times(1)).record(eq(6L), eq("VOLUNTARY_CAMPAIGN_CLOSED"), eq("VoluntaryCampaign"), eq("701"), any());
    }

    @Test
    void publishIsAllowedBeforeOpeningDayAndOnClosingDayButNotAfter() {
        var future = campaign("Sap mo", LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 30));
        var lastDay = campaign("Ngay cuoi", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 8));
        var expired = campaign("Het han", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7));
        ReflectionTestUtils.setField(future, "id", 1L);
        ReflectionTestUtils.setField(lastDay, "id", 2L);
        ReflectionTestUtils.setField(expired, "id", 3L);
        when(campaigns.lockById(1)).thenReturn(Optional.of(future));
        when(campaigns.lockById(2)).thenReturn(Optional.of(lastDay));
        when(campaigns.lockById(3)).thenReturn(Optional.of(expired));

        assertDoesNotThrow(() -> campaignService.publish(1, 0, 1));
        assertDoesNotThrow(() -> campaignService.publish(2, 0, 1));
        assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> campaignService.publish(3, 0, 1)).code());
        assertEquals("DRAFT", expired.getStatus());
    }

    @Test
    void residentDtoHasExactlySixFieldsAndAutoClosedCampaignShowsClosed() {
        var names = java.util.Arrays.stream(ResidentCampaignResponse.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName).toList();
        assertEquals(List.of("id", "name", "description", "opensOn", "closesOn", "status"), names);

        var open = campaign("Dang mo", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        var ended = campaign("Qua han", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7));
        ReflectionTestUtils.setField(open, "id", 712L);
        ReflectionTestUtils.setField(ended, "id", 713L);
        open.publish(1, Instant.parse("2026-11-01T01:00:00Z"));
        ended.publish(1, Instant.parse("2026-11-01T01:00:00Z"));
        when(campaigns.findById(712L)).thenReturn(Optional.of(open));
        when(campaigns.findById(713L)).thenReturn(Optional.of(ended));
        assertEquals("PUBLISHED", campaignService.residentDetail(712).status());
        assertEquals("CLOSED", campaignService.residentDetail(713).status());
        assertEquals("CLOSED", campaignService.staffDetail(713).status());
    }

    @Test
    void listFiltersAreValidatedForResidentAndStaff() {
        when(campaigns.search(anyBoolean(), any(), any(), any())).thenReturn(Page.empty());
        assertDoesNotThrow(() -> campaignService.listResident("CLOSED", 0, 20));
        assertDoesNotThrow(() -> campaignService.listStaff("DRAFT", 0, 100));
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class, () -> campaignService.listResident("BOGUS", 0, 20)).code());
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class, () -> campaignService.listStaff(null, -1, 20)).code());
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class, () -> campaignService.listStaff(null, 0, 101)).code());
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class, () -> campaignService.listStaff(null, 0, 0)).code());
    }

    // ---------------------------------------------------------------- VOL-04
    @Test
    void draftCampaignIsNotReceivableAndNothingIsLookedUpOrWritten() {
        var draft = campaign("Nhap", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        when(campaigns.lockById(720)).thenReturn(Optional.of(draft));
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        assertEquals("CAMPAIGN_NOT_RECEIVABLE", assertThrows(ConflictException.class,
                () -> contributionService.record(input(720, 4, "100", LocalDate.of(2026, 11, 8), null, KEY), 8)).code());
        verifyNoInteractions(donors, audit);
        verify(contributions, never()).saveAndFlush(any());
    }

    @Test
    void futureAndBeforeWindowPaidDatesAreInvalidPaidDate() {
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        // Mở 1/11, công bố 5/11: không nhận ngày trước 5/11 và không nhận ngày tương lai.
        var published = published("Dang mo", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "2026-11-05T03:00:00Z", null);
        when(campaigns.lockById(721)).thenReturn(Optional.of(published));
        for (LocalDate paidOn : new LocalDate[] {LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 9)}) {
            assertEquals("INVALID_PAID_DATE", assertThrows(ConflictException.class,
                    () -> contributionService.record(input(721, 4, "100", paidOn, null, KEY), 8)).code(), paidOn.toString());
        }
        // Công bố hôm nay nhưng lịch mở ngày mai: hôm nay vẫn trước ngày mở.
        var opensTomorrow = published("Mai mo", LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 30), "2026-11-08T00:30:00Z", null);
        when(campaigns.lockById(722)).thenReturn(Optional.of(opensTomorrow));
        assertEquals("INVALID_PAID_DATE", assertThrows(ConflictException.class,
                () -> contributionService.record(input(722, 4, "100", LocalDate.of(2026, 11, 8), null, KEY), 8)).code());
        verify(contributions, never()).saveAndFlush(any());
        verifyNoInteractions(donors);
    }

    @Test
    @SuppressWarnings("unchecked")
    void lateEntryAfterScheduledEndNeedsReasonAndStoresItWithoutAuditingIt() {
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        // Đã qua closesOn (7/11) nhưng chưa bấm đóng tay.
        var ended = published("Het han", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7), "2026-11-01T01:00:00Z", null);
        ReflectionTestUtils.setField(ended, "id", 722L);
        when(campaigns.lockById(722)).thenReturn(Optional.of(ended));
        when(donors.lockById(101)).thenReturn(Optional.of(new ResidentDonorLookup.Donor(101, 11, "Nguyen Van A", "H1", "A1")));
        when(contributions.saveAndFlush(any())).thenAnswer(call -> {
            var saved = (VoluntaryContribution) call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 801L);
            return saved;
        });

        // 3a: ngày nhận sau ngày kết thúc.
        assertEquals("INVALID_PAID_DATE", assertThrows(ConflictException.class,
                () -> contributionService.record(input(722, 101, "100", LocalDate.of(2026, 11, 8), "Thu muon", KEY), 8)).code());
        // 3b: trong cửa sổ nhưng thiếu hoặc rỗng lý do.
        for (String reason : new String[] {null, "   "}) {
            assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                    () -> contributionService.record(input(722, 101, "100", LocalDate.of(2026, 11, 7), reason, KEY), 8)).code());
        }
        verify(contributions, never()).saveAndFlush(any());

        // 3c: có lý do thì ghi nhận.
        var result = contributionService.record(input(722, 101, "100", LocalDate.of(2026, 11, 7), "Thu tien mat cuoi ngay hom qua", KEY), 8);
        assertEquals(false, result.replay());
        assertEquals("POSTED", result.contribution().getStatus());
        assertEquals("Thu tien mat cuoi ngay hom qua", result.contribution().getLateReason());
        var changes = ArgumentCaptor.forClass(Map.class);
        verify(audit, times(1)).record(eq(8L), eq("VOLUNTARY_CONTRIBUTION_RECORDED"), eq("VoluntaryContribution"), eq("801"), changes.capture());
        assertEquals(Map.of("campaignId", "722", "amount", "100", "paidOn", "2026-11-07", "status", "POSTED"), changes.getValue());
        assertTrue(changes.getValue().values().stream().noneMatch(v -> String.valueOf(v).contains("Thu tien") || String.valueOf(v).contains("Nguyen")));
    }

    @Test
    void manualEarlyCloseBoundsTheWindowAndStillRequiresAReasonOnTheClosingDay() {
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        var closedEarly = published("Dong som", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30),
                "2026-11-01T01:00:00Z", "2026-11-07T10:00:00Z");
        when(campaigns.lockById(723)).thenReturn(Optional.of(closedEarly));
        assertEquals("INVALID_PAID_DATE", assertThrows(ConflictException.class,
                () -> contributionService.record(input(723, 4, "100", LocalDate.of(2026, 11, 8), "Muon", KEY), 8)).code());
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> contributionService.record(input(723, 4, "100", LocalDate.of(2026, 11, 7), null, KEY), 8)).code());
    }

    @Test
    void openCampaignAcceptsTodayWithoutALateReason() {
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        var open = published("Dang mo", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "2026-11-05T03:00:00Z", null);
        ReflectionTestUtils.setField(open, "id", 712L);
        when(campaigns.lockById(712)).thenReturn(Optional.of(open));
        when(donors.lockById(103)).thenReturn(Optional.of(new ResidentDonorLookup.Donor(103, 13, "Cu Dan", "H3", "A3")));
        when(contributions.saveAndFlush(any())).thenAnswer(call -> {
            var saved = (VoluntaryContribution) call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 900L);
            return saved;
        });
        var result = contributionService.record(input(712, 103, "200000", LocalDate.of(2026, 11, 8), null, KEY), 8);
        assertEquals(false, result.replay());
        assertEquals(13L, result.contribution().getHouseholdId());
        assertEquals("Cu Dan", result.contribution().getDonorName());
        assertEquals("H3", result.contribution().getHouseholdCode());
        assertEquals(new BigDecimal("200000"), result.contribution().getAmount());
        assertNull(result.contribution().getLateReason());
    }

    @Test
    void unknownResidentIsNotFoundAndNothingIsWritten() {
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        var open = published("Dang mo", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "2026-11-05T03:00:00Z", null);
        when(campaigns.lockById(712)).thenReturn(Optional.of(open));
        when(donors.lockById(999999)).thenReturn(Optional.empty());
        assertEquals("RESIDENT_NOT_FOUND", assertThrows(NotFoundException.class,
                () -> contributionService.record(input(712, 999999, "100", LocalDate.of(2026, 11, 8), null, KEY), 8)).code());
        verify(contributions, never()).saveAndFlush(any());
        verifyNoInteractions(audit);
    }

    @Test
    void unknownCampaignIsNotFoundForRecordAndList() {
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(campaigns.lockById(999999)).thenReturn(Optional.empty());
        assertEquals("CAMPAIGN_NOT_FOUND", assertThrows(NotFoundException.class,
                () -> contributionService.record(input(999999, 4, "100", LocalDate.of(2026, 11, 8), null, KEY), 8)).code());
        assertEquals("CAMPAIGN_NOT_FOUND", assertThrows(NotFoundException.class,
                () -> contributionService.list(999999, null, 0, 20)).code());
        for (String status : new String[] {"BOGUS", "posted"}) {
            assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class,
                    () -> contributionService.list(1, status, 0, 20)).code());
        }
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class, () -> contributionService.list(1, null, 0, 101)).code());
    }

    // ---------------------------------------------------------------- VOL-05
    @Test
    void uppercaseAndLowercaseKeysAreTheSameKeyWithTheSameFingerprint() {
        var lower = ContributionInput.parse(712, 101, "150000", LocalDate.of(2026, 11, 8), null, "11111111-2222-3333-4444-aaaaaaaaaaaa");
        var upper = ContributionInput.parse(712, 101, "150000", LocalDate.of(2026, 11, 8), null, "11111111-2222-3333-4444-AAAAAAAAAAAA");
        assertEquals(lower.idempotencyKey(), upper.idempotencyKey());
        assertEquals(lower.fingerprint(), upper.fingerprint());
    }

    @Test
    void changingAnyPayloadFieldWithTheSameKeyIsAnIdempotencyConflict() {
        var original = input(712, 101, "150000", LocalDate.of(2026, 11, 8), null, KEY);
        var existing = new VoluntaryContribution(712, 101, 11, "Nguyen Van A", "H1", "A1", original.amount(), original.paidOn(),
                null, original.idempotencyKey(), original.fingerprint(), 8, clock.instant());
        ReflectionTestUtils.setField(existing, "id", 801L);
        when(contributions.findByRecordedByAndIdempotencyKey(8, KEY)).thenReturn(Optional.of(existing));

        var variants = List.of(
                input(712, 101, "250000", LocalDate.of(2026, 11, 8), null, KEY),
                input(712, 102, "150000", LocalDate.of(2026, 11, 8), null, KEY),
                input(712, 101, "150000", LocalDate.of(2026, 11, 7), null, KEY),
                input(713, 101, "150000", LocalDate.of(2026, 11, 8), null, KEY),
                input(712, 101, "150000", LocalDate.of(2026, 11, 8), "co ly do", KEY));
        for (var changed : variants) {
            assertEquals("IDEMPOTENCY_CONFLICT", assertThrows(ConflictException.class,
                    () -> contributionService.record(changed, 8)).code(), changed.toString());
        }
        // Gửi lại đúng payload gốc: replay, không ghi thêm, không audit, không mở transaction.
        var replay = contributionService.record(original, 8);
        assertTrue(replay.replay());
        assertEquals(801L, replay.contribution().getId());
        verifyNoInteractions(audit, manager, donors);
    }

    @Test
    void keyIsScopedToTheActorSoAnotherActorWithTheSameKeyIsNotAReplay() {
        var original = input(712, 101, "150000", LocalDate.of(2026, 11, 8), null, KEY);
        var existing = new VoluntaryContribution(712, 101, 11, "Nguyen Van A", "H1", "A1", original.amount(), original.paidOn(),
                null, original.idempotencyKey(), original.fingerprint(), 8, clock.instant());
        when(contributions.findByRecordedByAndIdempotencyKey(8, KEY)).thenReturn(Optional.of(existing));
        when(contributions.findByRecordedByAndIdempotencyKey(9, KEY)).thenReturn(Optional.empty());
        assertTrue(contributionService.record(original, 8).replay());
        verify(contributions, times(1)).findByRecordedByAndIdempotencyKey(8, KEY);
        // Actor khác không thấy bản ghi của actor 8 nên đi tiếp vào luồng ghi mới (ở đây dừng ở khóa đợt không tồn tại).
        when(manager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(campaigns.lockById(712)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> contributionService.record(original, 9));
    }

    // ---------------------------------------------------------------- VOL-06
    @Test
    void voidRejectsMissingOrOverlongReasonBeforeTouchingAnyData() {
        for (String reason : new String[] {null, "", "   ", "A".repeat(256), "😀".repeat(256)}) {
            assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                    () -> contributionService.voidContribution(802, 0, reason, 8)).code(), String.valueOf(reason));
        }
        verifyNoInteractions(contributions, campaigns, audit);
    }

    @Test
    void voidOfUnknownContributionIsNotFound() {
        when(contributions.findCampaignId(999999)).thenReturn(Optional.empty());
        assertEquals("CONTRIBUTION_NOT_FOUND", assertThrows(NotFoundException.class,
                () -> contributionService.voidContribution(999999, 0, "Nhap nham", 8)).code());
        verifyNoInteractions(audit);
    }

    @Test
    @SuppressWarnings("unchecked")
    void voidTrimsReasonRejectsStaleVersionAndRepeatedVoidIsIdempotentWithoutSecondAudit() {
        var contribution = new VoluntaryContribution(712, 101, 11, "Nguyen Van A", "H1", "A1", new BigDecimal("300000"),
                LocalDate.of(2026, 11, 8), null, KEY, "fp", 8, clock.instant());
        ReflectionTestUtils.setField(contribution, "id", 802L);
        var campaign = published("Dang mo", LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "2026-11-05T03:00:00Z", null);
        when(contributions.findCampaignId(802)).thenReturn(Optional.of(712L));
        when(campaigns.lockById(712)).thenReturn(Optional.of(campaign));
        when(contributions.lockById(802)).thenReturn(Optional.of(contribution));

        assertEquals("STALE_VERSION", assertThrows(ConflictException.class,
                () -> contributionService.voidContribution(802, 9, "Nhap nham", 8)).code());
        assertEquals("POSTED", contribution.getStatus());

        var voided = contributionService.voidContribution(802, 0, "  Nhap nham so tien  ", 8);
        assertEquals("VOIDED", voided.getStatus());
        assertEquals("Nhap nham so tien", voided.getVoidReason());
        assertEquals(8L, voided.getVoidedBy());
        assertNotNull(voided.getVoidedAt());

        var again = contributionService.voidContribution(802, 0, "Ly do khac", 8);
        assertEquals("Nhap nham so tien", again.getVoidReason());

        var changes = ArgumentCaptor.forClass(Map.class);
        verify(audit, times(1)).record(eq(8L), eq("VOLUNTARY_CONTRIBUTION_VOIDED"), eq("VoluntaryContribution"), eq("802"), changes.capture());
        assertEquals(Map.of("campaignId", "712", "amount", "300000", "status", "VOIDED"), changes.getValue());
    }

    // ---------------------------------------------------------------- helpers
    private ContributionInput input(long campaign, long resident, String amount, LocalDate paidOn, String reason, String key) {
        return ContributionInput.parse(campaign, resident, amount, paidOn, reason, key);
    }

    private VoluntaryCampaign campaign(String name, LocalDate opensOn, LocalDate closesOn) {
        return new VoluntaryCampaign(name, null, opensOn, closesOn, 1, clock.instant());
    }

    private VoluntaryCampaign published(String name, LocalDate opensOn, LocalDate closesOn, String publishedAt, String closedAt) {
        var campaign = campaign(name, opensOn, closesOn);
        campaign.publish(1, Instant.parse(publishedAt));
        if (closedAt != null) campaign.close(1, Instant.parse(closedAt));
        return campaign;
    }
}
