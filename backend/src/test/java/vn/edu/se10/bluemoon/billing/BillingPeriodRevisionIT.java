package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodService;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;

/**
 * Bằng chứng trên MySQL thật cho BIL-03.1 (version tăng), BIL-04.2 (thiếu đơn giá) và
 * BIL-05.2 (nhiều revision, revision cũ bất biến). Mỗi test chạy trong một transaction
 * và tự rollback, nên không để lại dữ liệu.
 */
@SpringBootTest
@Import(BillingPeriodRevisionIT.FixedClock.class)
@Transactional
class BillingPeriodRevisionIT {
    private static final String MONTH = "9000-05";
    private static final LocalDate DUE = LocalDate.of(9000, 5, 31);

    @Autowired JdbcTemplate jdbc;
    @Autowired BillingPeriodService service;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary
        Clock billingRevisionClock() { return Clock.fixed(Instant.parse("9000-05-15T00:00:00Z"), ZoneOffset.UTC); }
    }

    @Test
    void updatingDueDateIncrementsVersionOnTheDatabaseAndRejectsTheOldVersion() {
        long actor = actor();
        household(50);
        var period = service.create(MONTH, DUE, actor);
        long id = period.getId();
        assertEquals(0L, dbLong("SELECT version FROM billing_periods WHERE id=?", id));

        service.updateDueDate(id, LocalDate.of(9000, 5, 20), 0, actor);

        assertEquals(1L, dbLong("SELECT version FROM billing_periods WHERE id=?", id));
        assertEquals(LocalDate.of(9000, 5, 20), jdbc.queryForObject("SELECT due_date FROM billing_periods WHERE id=?", LocalDate.class, id));
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM billing_periods WHERE id=?", String.class, id));
        assertEquals(1L, dbLong("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='BILLING_PERIOD_UPDATED'", actor));
        // Version cũ không còn dùng được; dữ liệu giữ nguyên.
        assertEquals("STALE_VERSION", assertThrows(ConflictException.class,
                () -> service.updateDueDate(id, LocalDate.of(9000, 5, 25), 0, actor)).code());
        assertEquals(LocalDate.of(9000, 5, 20), jdbc.queryForObject("SELECT due_date FROM billing_periods WHERE id=?", LocalDate.class, id));
        assertEquals(1L, dbLong("SELECT version FROM billing_periods WHERE id=?", id));
    }

    @Test
    void missingFeeRatesBlockPreviewAndSubmitWithoutCreatingASnapshot() {
        long actor = actor();
        household(50);
        // Điều kiện đầu: không có đơn giá nào hiệu lực tới đầu tháng 9000-05.
        assertEquals(0L, dbLong("SELECT COUNT(*) FROM fee_rates WHERE effective_from <= '9000-05-01'"));
        var period = service.create(MONTH, DUE, actor);
        long id = period.getId();

        assertEquals("FEE_RATE_UNAVAILABLE", assertThrows(ConflictException.class, () -> service.preview(id, 0, 20)).code());
        assertEquals("FEE_RATE_UNAVAILABLE", assertThrows(ConflictException.class, () -> service.submit(id, 0, actor)).code());
        assertNoSnapshot(id);

        // Chỉ có đơn giá phí quản lý, thiếu phí rác: vẫn không đủ.
        rate("MANAGEMENT_FEE", 10000, actor);
        assertEquals("FEE_RATE_UNAVAILABLE", assertThrows(ConflictException.class, () -> service.preview(id, 0, 20)).code());
        assertEquals("FEE_RATE_UNAVAILABLE", assertThrows(ConflictException.class, () -> service.submit(id, 0, actor)).code());
        assertNoSnapshot(id);
    }

    @Test
    void resubmittingKeepsOlderRevisionsImmutableAndReasonIsNotWrittenToAudit() {
        long actor = actor();
        long household = household(50);
        rate("MANAGEMENT_FEE", 10000, actor);
        rate("WASTE_FEE", 30000, actor);
        long id = service.create(MONTH, DUE, actor).getId();

        // Revision 1.
        var first = service.submit(id, 0, actor);
        assertEquals(1L, first.getRevision());
        assertEquals("PENDING_APPROVAL", first.getStatus());

        // Rút lại: về DRAFT nhưng giữ revision 1; sau đó đổi diện tích nguồn và trình lại thành revision 2.
        var withdrawn = service.withdraw(id, first.getVersion(), actor);
        assertEquals("DRAFT", withdrawn.getStatus());
        assertEquals(1L, withdrawn.getRevision());
        jdbc.update("UPDATE households SET area_m2=60 WHERE id=?", household);
        var second = service.submit(id, withdrawn.getVersion(), actor);
        assertEquals(2L, second.getRevision());

        // Revision 1 vẫn là snapshot cũ (50 m2), revision 2 phản ánh dữ liệu mới (60 m2).
        assertEquals(0, new BigDecimal("50").compareTo(area(id, 1, household)));
        assertEquals(0, new BigDecimal("500000").compareTo(managementAmount(id, 1, household)));
        assertEquals(0, new BigDecimal("60").compareTo(area(id, 2, household)));
        assertEquals(0, new BigDecimal("600000").compareTo(managementAmount(id, 2, household)));
        assertEquals(1L, service.submission(id, 1L, 0, 20).revision());
        assertEquals(2L, service.submission(id, 2L, 0, 20).revision());

        // Trả về có lý do: lý do được trim và lưu trên kỳ, nhưng không xuất hiện trong audit.
        String reason = " cần cập nhật hạn nộp ";
        var returned = service.returnToDraft(id, second.getVersion(), reason, actor);
        assertEquals("DRAFT", returned.getStatus());
        assertEquals("cần cập nhật hạn nộp", returned.getReturnReason());
        assertEquals(2L, returned.getRevision());
        String auditChanges = jdbc.queryForObject(
                "SELECT changes FROM audit_log WHERE actor_user_id=? AND action='BILLING_PERIOD_RETURNED' AND entity_id=?",
                String.class, actor, Long.toString(id));
        assertFalse(auditChanges.contains("cập nhật"), auditChanges);

        // Trình lại xóa lý do cũ và tạo revision 3.
        var third = service.submit(id, returned.getVersion(), actor);
        assertEquals(3L, third.getRevision());
        assertEquals(null, third.getReturnReason());
        assertEquals(3L, dbLong("SELECT COUNT(*) FROM billing_submissions WHERE period_id=?", id));
        assertEquals(0, new BigDecimal("50").compareTo(area(id, 1, household)));

        // Revision không tồn tại trả NotFound.
        assertEquals("BILLING_SUBMISSION_NOT_FOUND", assertThrows(NotFoundException.class,
                () -> service.submission(id, 99L, 0, 20)).code());
    }

    private void assertNoSnapshot(long periodId) {
        assertEquals(0L, dbLong("SELECT COUNT(*) FROM billing_submissions WHERE period_id=?", periodId));
        assertEquals("DRAFT", jdbc.queryForObject("SELECT status FROM billing_periods WHERE id=?", String.class, periodId));
        assertEquals(0L, dbLong("SELECT revision FROM billing_periods WHERE id=?", periodId));
    }

    private BigDecimal area(long periodId, long revision, long household) {
        return jdbc.queryForObject("""
                SELECT i.area_m2 FROM billing_submission_items i JOIN billing_submissions s ON s.id=i.submission_id
                WHERE s.period_id=? AND s.revision=? AND i.household_id=? AND i.fee_type_code='MANAGEMENT_FEE'
                """, BigDecimal.class, periodId, revision, household);
    }

    private BigDecimal managementAmount(long periodId, long revision, long household) {
        return jdbc.queryForObject("""
                SELECT i.amount FROM billing_submission_items i JOIN billing_submissions s ON s.id=i.submission_id
                WHERE s.period_id=? AND s.revision=? AND i.household_id=? AND i.fee_type_code='MANAGEMENT_FEE'
                """, BigDecimal.class, periodId, revision, household);
    }

    private long dbLong(String sql, Object... args) {
        return jdbc.queryForObject(sql, Long.class, args);
    }

    private long actor() {
        String marker = "rev-" + UUID.randomUUID();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, 'test', 'test', 'ACTIVE', NOW(6), NOW(6))", marker);
        return jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
    }

    private long household(int area) {
        String marker = "rev-" + UUID.randomUUID();
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, ?, 'ACTIVE', NOW(6), NOW(6))", marker, marker, area);
        return jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
    }

    private void rate(String feeTypeCode, int price, long actor) {
        jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) SELECT id, ?, '9000-05-01', ?, NOW(6) FROM fee_types WHERE code=?",
                price, actor, feeTypeCode);
    }
}
