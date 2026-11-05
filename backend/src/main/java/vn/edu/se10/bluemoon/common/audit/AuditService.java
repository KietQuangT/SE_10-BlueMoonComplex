package vn.edu.se10.bluemoon.common.audit;

import java.time.Clock;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Ghi audit nghiệp vụ. Bắt buộc gọi trong transaction của thay đổi dữ liệu (MANDATORY):
 * rollback thì audit cũng rollback. Caller không được bắt exception quanh lời gọi này.
 *
 * <p>Danh sách khóa PII chỉ là lưới chặn thô. Bảo vệ chính là caller chỉ đưa vào {@code changes}
 * các trường đã được phép ghi (xem DECISIONS D12).
 */
@Service
public class AuditService {
    static final int MAX_FIELD = 100;
    static final int MAX_CHANGES = 4000;

    private final AuditLogRepository repository;
    private final Clock clock;
    private final ObjectMapper mapper;

    AuditService(AuditLogRepository repository, Clock clock, ObjectMapper mapper) {
        this.repository = repository;
        this.clock = clock;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(long actorUserId, String action, String entityType, String entityId, Map<String, String> changes) {
        require(action, "action");
        require(entityType, "entityType");
        if (entityId != null && entityId.length() > MAX_FIELD) {
            throw new IllegalArgumentException("entityId quá dài");
        }
        String json = null;
        if (changes != null && !changes.isEmpty()) {
            changes.keySet().forEach(AuditService::rejectPii);
            json = mapper.writeValueAsString(changes);
            if (json.length() > MAX_CHANGES) {
                throw new IllegalArgumentException("changes quá dài");
            }
        }
        repository.save(new AuditLog(clock.instant(), actorUserId, action, entityType, entityId, json));
    }

    private static void require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " không được rỗng");
        }
        if (value.length() > MAX_FIELD) {
            throw new IllegalArgumentException(name + " quá dài");
        }
    }

    private static void rejectPii(String key) {
        String k = key == null ? "" : key.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        if (k.equals("email") || k.equals("phone") || k.equals("identitynumber") || k.startsWith("password")) {
            throw new IllegalArgumentException("Khóa không được ghi vào audit: " + key);
        }
    }
}
