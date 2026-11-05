package vn.edu.se10.bluemoon.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.identity.application.RoleService;

// Chạy bằng -Pintegration với MySQL thật. Cố ý KHÔNG @Transactional ở lớp test:
// test phải thấy commit/rollback thật. Đổi tên role seed ADMIN rồi khôi phục: không chạy trên DB có người khác dùng.
@SpringBootTest
class RoleAuditIT {
    static final Instant FIXED = Instant.parse("2026-11-05T01:02:03.123456Z");

    @TestConfiguration
    static class FixedClockConfig {
        @Bean @Primary
        Clock fixedClock() {
            return Clock.fixed(FIXED, ZoneOffset.UTC);
        }
    }

    @Autowired RoleService roleService;
    @Autowired AuditService auditService;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager txManager;

    long roleId;
    String originalName;
    long userId;

    @BeforeEach void taoFixture() {
        roleId = jdbc.queryForObject("SELECT id FROM roles WHERE code = 'ADMIN'", Long.class);
        originalName = jdbc.queryForObject("SELECT name FROM roles WHERE id = ?", String.class, roleId);
        // Fail sớm nếu seed ADMIN đã lệch (ví dụ lần chạy trước bị kill giữa chừng), thay vì khôi phục tên sai.
        assertThat(originalName).isEqualTo("Ban quản trị");
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, email, created_at, updated_at)"
                + " VALUES (?, ?, 'hash-gia', 'Nguoi Kiem Thu', ?, NOW(6), NOW(6))", roleId, "it-" + id, id + "@example.test");
        userId = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, "it-" + id);
    }

    @AfterEach void donDep() {
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id = ?", userId);
        jdbc.update("DELETE FROM users WHERE id = ?", userId);
        jdbc.update("UPDATE roles SET name = ? WHERE id = ?", originalName, roleId);
    }

    @Test void renameGhiDungMotDongAudit() {
        String newName = "Ban quan tri thu " + UUID.randomUUID();
        roleService.rename(userId, roleId, newName);

        assertThat(roleName()).isEqualTo(newName);
        assertThat(auditCount()).isEqualTo(1);
        Map<String, Object> row = jdbc.queryForMap(
                "SELECT action, entity_type, entity_id, changes, occurred_at FROM audit_log WHERE actor_user_id = ?", userId);
        assertThat(row.get("action")).isEqualTo("ROLE_RENAMED");
        assertThat(row.get("entity_type")).isEqualTo("Role");
        assertThat(row.get("entity_id")).isEqualTo(String.valueOf(roleId));
        assertThat((String) row.get("changes")).contains(originalName).contains(newName);
        // Round trip Instant <-> DATETIME(6): giữ nguyên microsecond, UTC.
        assertThat(row.get("occurred_at")).isEqualTo(LocalDateTime.ofInstant(FIXED, ZoneOffset.UTC));
    }

    @Test void loiNghiepVuSauRenameThiRollbackCaAudit() {
        TransactionTemplate tx = new TransactionTemplate(txManager);
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            roleService.rename(userId, roleId, "Ten tam " + UUID.randomUUID());
            throw new IllegalStateException("loi nghiep vu gia");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(roleName()).isEqualTo(originalName);
        assertThat(auditCount()).isZero();
    }

    @Test void actorKhongTonTaiThiKhongDoiTen() {
        int before = totalAuditCount();
        assertThatThrownBy(() -> roleService.rename(Long.MAX_VALUE, roleId, "Ten tam " + UUID.randomUUID()))
                .isInstanceOf(DataAccessException.class);

        assertThat(roleName()).isEqualTo(originalName);
        assertThat(totalAuditCount()).isEqualTo(before);
    }

    @Test void recordNgoaiTransactionBiTuChoi() {
        assertThatThrownBy(() -> auditService.record(userId, "ROLE_RENAMED", "Role", "1", null))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(auditCount()).isZero();
    }

    private String roleName() {
        return jdbc.queryForObject("SELECT name FROM roles WHERE id = ?", String.class, roleId);
    }

    private int totalAuditCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM audit_log", Integer.class);
    }

    private int auditCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id = ?", Integer.class, userId);
    }
}
