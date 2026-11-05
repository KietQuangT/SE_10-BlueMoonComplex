package vn.edu.se10.bluemoon.common.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;

class AuditServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-05T01:02:03.123456Z");

    private final List<AuditLog> saved = new ArrayList<>();
    // Repository chỉ có 1 method nên dùng lambda làm bản giả, không cần Mockito.
    private final AuditLogRepository repository = log -> { saved.add(log); return log; };
    private final AuditService service =
            new AuditService(repository, Clock.fixed(NOW, ZoneOffset.UTC), JsonMapper.builder().build());

    @Test void ghiDungTruongVaThoiDiemTuClock() {
        service.record(7, "ROLE_RENAMED", "Role", "1", Map.of("oldName", "A"));
        assertThat(saved).hasSize(1);
        AuditLog log = saved.get(0);
        assertThat(log.getOccurredAt()).isEqualTo(NOW);
        assertThat(log.getActorUserId()).isEqualTo(7L);
        assertThat(log.getAction()).isEqualTo("ROLE_RENAMED");
        assertThat(log.getEntityType()).isEqualTo("Role");
        assertThat(log.getEntityId()).isEqualTo("1");
        assertThat(log.getChanges()).isEqualTo("{\"oldName\":\"A\"}");
    }

    @Test void bootstrapCoActorNullVaActionCoDinh() {
        service.recordBootstrap("42", Map.of("source", "local"));
        AuditLog log = saved.get(0);
        assertThat(log.getActorUserId()).isNull();
        assertThat(log.getAction()).isEqualTo("BOOTSTRAP_ADMIN");
        assertThat(log.getEntityType()).isEqualTo("User");
        assertThat(log.getEntityId()).isEqualTo("42");
        assertThat(log.getChanges()).isEqualTo("{\"source\":\"local\"}");
    }

    @Test void bootstrapVanTuChoiKhoaPii() {
        assertThatThrownBy(() -> service.recordBootstrap("42", Map.of("passwordHash", "secret")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(saved).isEmpty();
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {"  "})
    void tuChoiActionRong(String action) {
        assertThatThrownBy(() -> service.record(1, action, "Role", "1", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(saved).isEmpty();
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {"  "})
    void tuChoiEntityTypeRong(String entityType) {
        assertThatThrownBy(() -> service.record(1, "A", entityType, "1", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(saved).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "EMAIL", "Email", "phone", "PHONE", "identityNumber", "IDENTITYNUMBER",
            "identity_number", "password", "Password", "passwordHash", "PASSWORD_CONFIRM"})
    void tuChoiKhoaPii(String key) {
        assertThatThrownBy(() -> service.record(1, "A", "User", "1", Map.of(key, "x")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(saved).isEmpty();
    }

    @Test void tuChoiChangesQuaDai() {
        Map<String, String> changes = Map.of("note", "x".repeat(AuditService.MAX_CHANGES));
        assertThatThrownBy(() -> service.record(1, "A", "Role", "1", changes))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(saved).isEmpty();
    }

    @Test void tuChoiTruongQuaDai() {
        String tooLong = "x".repeat(AuditService.MAX_FIELD + 1);
        assertThatThrownBy(() -> service.record(1, tooLong, "Role", "1", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.record(1, "A", tooLong, "1", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.record(1, "A", "Role", tooLong, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(saved).isEmpty();
    }
}
