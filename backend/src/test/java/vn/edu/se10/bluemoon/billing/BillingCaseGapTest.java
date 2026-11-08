package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodRules;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriod;
import vn.edu.se10.bluemoon.billing.persistence.BillingPeriodRepository;
import vn.edu.se10.bluemoon.billing.persistence.BillingReadStore;
import vn.edu.se10.bluemoon.billing.persistence.BillingSnapshotStore;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

/**
 * Bổ sung bằng chứng tự động cho các ca BIL-01.3, BIL-01.4, BIL-05.3 và BIL-08.1
 * mà các test hiện có chưa kiểm đủ: tháng sau, period sai định dạng, ranh giới
 * 255/256 ký tự của lý do trả về, và khóa mọi thao tác ghi của kỳ tháng cũ.
 */
class BillingCaseGapTest {
    // 2026-11-08 07:00 theo giờ Việt Nam: tháng hiện tại là 2026-11.
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-08T00:00:00Z"), ZoneOffset.UTC);
    private final BillingPeriodRepository periods = mock(BillingPeriodRepository.class);
    private final BillingSnapshotStore snapshots = mock(BillingSnapshotStore.class);
    private final AuditService audit = mock(AuditService.class);
    private final BillingPeriodService service = new BillingPeriodService(periods, audit, clock,
            mock(HouseholdLookup.class), snapshots, mock(BillingReadStore.class));

    @Test
    void creationRejectsNextMonthAndMalformedPeriodsWithInvalidPeriod() {
        var rules = new BillingPeriodRules(clock);
        for (String period : new String[] {"2026-12", "2026-10", "2026-13", "2026-00", "2026-1", "26-11", "abc", ""}) {
            assertEquals("INVALID_PERIOD", assertThrows(BadRequestException.class,
                    () -> rules.validateCreation(period, LocalDate.of(2026, 11, 30)), period).code(), period);
        }
        assertEquals("INVALID_PERIOD", assertThrows(BadRequestException.class,
                () -> rules.validateCreation(null, LocalDate.of(2026, 11, 30))).code());
        assertDoesNotThrow(() -> rules.validateCreation("2026-11", LocalDate.of(2026, 11, 1)));
        assertDoesNotThrow(() -> rules.validateCreation("2026-11", LocalDate.of(2026, 11, 30)));
    }

    @Test
    void creationRejectsDueDateOutsideMonthOrMissingWithInvalidDate() {
        var rules = new BillingPeriodRules(clock);
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> rules.validateCreation("2026-11", LocalDate.of(2026, 10, 31))).code());
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> rules.validateCreation("2026-11", LocalDate.of(2026, 12, 1))).code());
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> rules.validateCreation("2026-11", null)).code());
    }

    @Test
    void invalidPeriodIsRejectedBeforeAnythingIsPersisted() {
        assertEquals("INVALID_PERIOD", assertThrows(BadRequestException.class,
                () -> service.create("2026-12", LocalDate.of(2026, 12, 31), 1)).code());
        verify(periods, never()).saveAndFlush(any());
    }

    @Test
    void returnReasonAccepts255CodePointsAndRejects256() {
        var period = pending("2026-11");
        service.returnToDraft(5, 0, "A".repeat(255), 1);
        assertEquals("DRAFT", period.getStatus());
        assertEquals(255, period.getReturnReason().length());

        var other = pending("2026-11");
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.returnToDraft(5, 0, "A".repeat(256), 1)).code());
        assertEquals("PENDING_APPROVAL", other.getStatus());
        // Chỉ khoảng trắng và null cũng bị từ chối, trạng thái không đổi.
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.returnToDraft(5, 0, "   ", 1)).code());
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.returnToDraft(5, 0, null, 1)).code());
        assertEquals("PENDING_APPROVAL", other.getStatus());
    }

    @Test
    void returnReasonLimitCountsCodePointsNotUtf16Units() {
        var period = pending("2026-11");
        // 255 ký tự ngoài BMP = 510 đơn vị UTF-16 nhưng vẫn là 255 code point.
        String emoji255 = "😀".repeat(255);
        service.returnToDraft(5, 0, emoji255, 1);
        assertEquals("DRAFT", period.getStatus());
        pending("2026-11");
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> service.returnToDraft(5, 0, "😀".repeat(256), 1)).code());
    }

    @Test
    void everyWriteActionOnAPastMonthIsRejectedAsExpiredAndChangesNothing() {
        record Action(String name, String status, Consumer<BillingPeriodService> run) { }
        var actions = java.util.List.<Action>of(
                new Action("updateDueDate", "DRAFT", s -> s.updateDueDate(5, LocalDate.of(2026, 10, 30), 0, 1)),
                new Action("submit", "DRAFT", s -> s.submit(5, 0, 1)),
                new Action("withdraw", "PENDING_APPROVAL", s -> s.withdraw(5, 0, 1)),
                new Action("return", "PENDING_APPROVAL", s -> s.returnToDraft(5, 0, "reason", 1)),
                new Action("issue", "PENDING_APPROVAL", s -> s.issue(5, 0, 0, 1)),
                new Action("cancel", "ISSUED", s -> s.cancel(5, 0, "reason", 1)));
        for (Action action : actions) {
            var period = period("2026-10", action.status());
            assertEquals("BILLING_PERIOD_EXPIRED", assertThrows(ConflictException.class,
                    () -> action.run().accept(service), action.name()).code(), action.name());
            assertEquals(action.status(), period.getStatus(), action.name());
            assertEquals(0L, period.getVersion(), action.name());
        }
        verify(snapshots, never()).save(anyLong(), anyLong(), anyLong(), any(), org.mockito.ArgumentMatchers.anyList(), anyLong(), any());
        verify(snapshots, never()).issue(anyLong(), anyLong(), any(), any());
        verify(snapshots, never()).cancelInvoices(anyLong(), any());
        verify(audit, never()).record(anyLong(), any(), any(), any(), any());
    }

    @Test
    void monthBoundaryUsesVietnamTimeNotUtc() {
        // 17:30 UTC ngày 31/10 đã là 00:30 ngày 01/11 tại Việt Nam.
        var boundary = new BillingPeriodRules(Clock.fixed(Instant.parse("2026-10-31T17:30:00Z"), ZoneOffset.UTC));
        assertDoesNotThrow(() -> boundary.requireCurrent("2026-11"));
        assertEquals("BILLING_PERIOD_EXPIRED", assertThrows(ConflictException.class,
                () -> boundary.requireCurrent("2026-10")).code());
        // 16:59 UTC cùng ngày vẫn là 23:59 ngày 31/10 tại Việt Nam.
        var before = new BillingPeriodRules(Clock.fixed(Instant.parse("2026-10-31T16:59:59Z"), ZoneOffset.UTC));
        assertDoesNotThrow(() -> before.requireCurrent("2026-10"));
        assertEquals("BILLING_PERIOD_EXPIRED", assertThrows(ConflictException.class,
                () -> before.requireCurrent("2026-11")).code());
    }

    @Test
    void newPeriodStartsAsDraftAtVersionAndRevisionZeroWithNoReceipts() {
        var created = new BillingPeriod("2026-11", LocalDate.of(2026, 11, 30), null, 1, clock.instant());
        assertEquals("DRAFT", created.getStatus());
        assertEquals(0L, created.getVersion());
        assertEquals(0L, created.getRevision());
        assertEquals(0L, created.getReceiptCount());
    }

    @Test
    void allowedActionsFollowRoleAndStatusForCurrentMonth() {
        period("2026-11", "DRAFT");
        var accountant = service.detail(5, "ACCOUNTANT");
        assertEquals(java.util.List.of("PREVIEW", "UPDATE", "SUBMIT"), accountant.allowedActions());
        assertEquals(true, accountant.editable());
        assertEquals(false, accountant.expired());
        assertEquals(0L, accountant.version());
        assertEquals(0L, accountant.latestRevision());
        // ADMIN không được lập hoặc sửa kỳ nháp.
        var admin = service.detail(5, "ADMIN");
        assertEquals(java.util.List.of("PREVIEW"), admin.allowedActions());
        assertEquals(false, admin.editable());

        period("2026-11", "PENDING_APPROVAL");
        assertEquals(java.util.List.of("PREVIEW", "WITHDRAW"), service.detail(5, "ACCOUNTANT").allowedActions());
        assertEquals(java.util.List.of("PREVIEW", "RETURN", "ISSUE"), service.detail(5, "ADMIN").allowedActions());
    }

    @Test
    void pastMonthDraftIsExpiredAndOffersNoWriteAction() {
        period("2026-10", "DRAFT");
        var response = service.detail(5, "ACCOUNTANT");
        assertEquals(true, response.expired());
        assertEquals(false, response.editable());
        assertEquals(java.util.List.of("PREVIEW"), response.allowedActions());
    }

    @Test
    void listSortsByPeriodThenIdDescendingAndValidatesFilters() {
        when(periods.search(any(), any(), any())).thenReturn(org.springframework.data.domain.Page.empty());
        service.list(null, null, 0, 20, "ADMIN");
        var pageable = org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(periods).search(any(), any(), pageable.capture());
        var orders = pageable.getValue().getSort().toList();
        assertEquals("period", orders.get(0).getProperty());
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC, orders.get(0).getDirection());
        assertEquals("id", orders.get(1).getProperty());
        assertEquals(org.springframework.data.domain.Sort.Direction.DESC, orders.get(1).getDirection());

        assertEquals("INVALID_PERIOD", assertThrows(BadRequestException.class,
                () -> service.list("2026-13", null, 0, 20, "ADMIN")).code());
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class,
                () -> service.list(null, "BOGUS", 0, 20, "ADMIN")).code());
        assertEquals("INVALID_FILTER", assertThrows(BadRequestException.class,
                () -> service.list(null, null, -1, 20, "ADMIN")).code());
    }

    @Test
    void unknownPeriodIsNotFoundForDetailAndPreview() {
        assertEquals("BILLING_PERIOD_NOT_FOUND", assertThrows(vn.edu.se10.bluemoon.common.error.NotFoundException.class,
                () -> service.detail(999, "ADMIN")).code());
        assertEquals("BILLING_PERIOD_NOT_FOUND", assertThrows(vn.edu.se10.bluemoon.common.error.NotFoundException.class,
                () -> service.preview(999, 0, 20)).code());
    }

    @Test
    void previewOfDraftUsesBigDecimalHalfUpAndWritesNothing() {
        period("2026-11", "DRAFT");
        var householdLookup = mock(HouseholdLookup.class);
        var preview = new BillingPeriodService(periods, audit, clock, householdLookup, snapshots, mock(BillingReadStore.class));
        when(householdLookup.listActiveForBilling()).thenReturn(java.util.List.of(
                new HouseholdLookup.BillingHousehold(2, "H2", "A2", new java.math.BigDecimal("10.005"))));
        when(snapshots.readRates(LocalDate.of(2026, 11, 1))).thenReturn(java.util.List.of(
                new BillingSnapshotStore.Rate(1, "MANAGEMENT_FEE", "Management",
                        vn.edu.se10.bluemoon.billing.persistence.CalculationMethod.AREA, 11, new java.math.BigDecimal("100")),
                new BillingSnapshotStore.Rate(2, "WASTE_FEE", "Waste",
                        vn.edu.se10.bluemoon.billing.persistence.CalculationMethod.PER_HOUSEHOLD, 12, new java.math.BigDecimal("30000.50"))));
        var result = preview.preview(5, 0, 20);
        // 10.005 * 100 = 1000.5 -> 1001 ; 30000.50 -> 30001 (HALF_UP, mỗi dòng làm tròn riêng) ; tổng 31002
        var lines = result.households().content().get(0).lines();
        assertEquals("1001", lines.get(0).amount());
        assertEquals("30001", lines.get(1).amount());
        assertEquals("31002", result.total());
        assertEquals("31002", result.households().content().get(0).total());
        assertEquals(1L, result.householdCount());
        verify(snapshots, never()).save(anyLong(), anyLong(), anyLong(), any(), org.mockito.ArgumentMatchers.anyList(), anyLong(), any());
        verify(audit, never()).record(anyLong(), any(), any(), any(), any());
    }

    @Test
    void previewWithNoActiveHouseholdsIsConflictAndWritesNothing() {
        period("2026-11", "DRAFT");
        assertEquals("NO_ACTIVE_HOUSEHOLDS", assertThrows(ConflictException.class, () -> service.preview(5, 0, 20)).code());
        verify(snapshots, never()).save(anyLong(), anyLong(), anyLong(), any(), org.mockito.ArgumentMatchers.anyList(), anyLong(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateDueDateWritesAuditWithOnlyAllowedFields() {
        var period = period("2026-11", "DRAFT");
        service.updateDueDate(5, LocalDate.of(2026, 11, 25), 0, 7);
        var changes = org.mockito.ArgumentCaptor.forClass(java.util.Map.class);
        verify(audit).record(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq("BILLING_PERIOD_UPDATED"),
                org.mockito.ArgumentMatchers.eq("BillingPeriod"), org.mockito.ArgumentMatchers.eq("5"), changes.capture());
        assertEquals(java.util.Map.of("period", "2026-11", "dueDate", "2026-11-25"), changes.getValue());
        assertEquals("DRAFT", period.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void submitAuditCarriesPeriodRevisionCountAndTotalOnly() {
        var period = period("2026-11", "DRAFT");
        var householdLookup = mock(HouseholdLookup.class);
        var submitting = new BillingPeriodService(periods, audit, clock, householdLookup, snapshots, mock(BillingReadStore.class));
        when(householdLookup.lockActiveForBilling()).thenReturn(java.util.List.of(
                new HouseholdLookup.BillingHousehold(2, "H2", "A2", new java.math.BigDecimal("50"))));
        when(snapshots.lockRates(any())).thenReturn(java.util.List.of(
                new BillingSnapshotStore.Rate(1, "MANAGEMENT_FEE", "Management",
                        vn.edu.se10.bluemoon.billing.persistence.CalculationMethod.AREA, 11, new java.math.BigDecimal("10000")),
                new BillingSnapshotStore.Rate(2, "WASTE_FEE", "Waste",
                        vn.edu.se10.bluemoon.billing.persistence.CalculationMethod.PER_HOUSEHOLD, 12, new java.math.BigDecimal("30000"))));
        when(snapshots.save(anyLong(), anyLong(), anyLong(), any(), org.mockito.ArgumentMatchers.anyList(), anyLong(), any()))
                .thenReturn(new BillingSnapshotStore.Submission(9, 1, new java.math.BigDecimal("530000")));
        submitting.submit(5, 0, 7);
        assertEquals("PENDING_APPROVAL", period.getStatus());
        assertEquals(1L, period.getRevision());
        var changes = org.mockito.ArgumentCaptor.forClass(java.util.Map.class);
        verify(audit).record(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.eq("BILLING_PERIOD_SUBMITTED"),
                org.mockito.ArgumentMatchers.eq("BillingPeriod"), org.mockito.ArgumentMatchers.eq("5"), changes.capture());
        assertEquals(java.util.Map.of("period", "2026-11", "revision", "1", "count", "1", "total", "530000"), changes.getValue());
    }

    @Test
    void issuedPeriodOffersCancelOnlyToAdminAndOnlyForCurrentMonthWithoutReceipts() {
        var period = period("2026-11", "ISSUED");
        assertEquals(java.util.List.of("CANCEL"), service.detail(5, "ADMIN").allowedActions());
        assertEquals(java.util.List.of(), service.detail(5, "ACCOUNTANT").allowedActions());
        ReflectionTestUtils.setField(period, "receiptCount", 1L);
        assertEquals(java.util.List.of(), service.detail(5, "ADMIN").allowedActions());

        // Kỳ ISSUED của tháng cũ là lịch sử: không expired, không có action nào.
        period("2026-10", "ISSUED");
        var old = service.detail(5, "ADMIN");
        assertEquals(false, old.expired());
        assertEquals(false, old.editable());
        assertEquals(java.util.List.of(), old.allowedActions());

        // Kỳ PENDING_APPROVAL của tháng cũ: expired, chỉ còn PREVIEW.
        period("2026-10", "PENDING_APPROVAL");
        var oldPending = service.detail(5, "ADMIN");
        assertEquals(true, oldPending.expired());
        assertEquals(java.util.List.of("PREVIEW"), oldPending.allowedActions());
    }

    @Test
    void previewOfAnIssuedOrCancelledPeriodIsInvalidState() {
        for (String state : new String[] {"ISSUED", "CANCELLED"}) {
            period("2026-11", state);
            assertEquals("INVALID_STATE", assertThrows(ConflictException.class, () -> service.preview(5, 0, 20)).code(), state);
        }
    }

    @Test
    void historyOfAPastMonthPeriodStaysReadable() {
        var reads = mock(BillingReadStore.class);
        var reading = new BillingPeriodService(periods, audit, clock, mock(HouseholdLookup.class), snapshots, reads);
        period("2026-10", "PENDING_APPROVAL");
        when(reads.submission(5, 1)).thenReturn(new BillingReadStore.SubmissionInfo(9, 1, 1, clock.instant(), 2,
                new java.math.BigDecimal("1060000")));
        var submission = reading.submission(5, 1L, 0, 20);
        assertEquals(1L, submission.revision());
        assertEquals(2L, submission.householdCount());
        assertEquals("1060000", submission.total());

        period("2026-10", "ISSUED");
        var invoices = reading.invoices(5, 0, 20);
        assertEquals(0L, invoices.totalElements());
    }

    private BillingPeriod pending(String month) {
        return period(month, "PENDING_APPROVAL");
    }

    private BillingPeriod period(String month, String status) {
        var period = new BillingPeriod(month, LocalDate.parse(month + "-20"), null, 1, clock.instant());
        ReflectionTestUtils.setField(period, "id", 5L);
        ReflectionTestUtils.setField(period, "status", status);
        when(periods.lockById(5)).thenReturn(Optional.of(period));
        when(periods.findById(5L)).thenReturn(Optional.of(period));
        return period;
    }
}
