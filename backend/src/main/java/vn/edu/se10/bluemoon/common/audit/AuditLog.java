package vn.edu.se10.bluemoon.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Bản ghi audit bất biến: không setter, chỉ tạo mới. Map bảng audit_log (V2). */
@Entity
@Table(name = "audit_log")
class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String changes;

    protected AuditLog() {
    }

    AuditLog(Instant occurredAt, Long actorUserId, String action, String entityType, String entityId, String changes) {
        this.occurredAt = occurredAt;
        this.actorUserId = actorUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.changes = changes;
    }

    Long getId() { return id; }
    Instant getOccurredAt() { return occurredAt; }
    Long getActorUserId() { return actorUserId; }
    String getAction() { return action; }
    String getEntityType() { return entityType; }
    String getEntityId() { return entityId; }
    String getChanges() { return changes; }
}
