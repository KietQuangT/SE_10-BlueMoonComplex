ALTER TABLE residency_changes
    ADD COLUMN approved_by BIGINT NULL,
    ADD COLUMN decided_at DATETIME(6) NULL,
    ADD COLUMN decision_note VARCHAR(255) NULL,
    ADD COLUMN end_date DATE NULL,
    ADD CONSTRAINT fk_rc_approver FOREIGN KEY (approved_by) REFERENCES users (id),
    ADD CONSTRAINT ck_rc_end_date CHECK (end_date IS NULL OR end_date >= effective_date),
    DROP CHECK ck_rc_status,
    ADD CONSTRAINT ck_rc_status_v2 CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    ADD INDEX idx_rc_status_created (status, created_at);
