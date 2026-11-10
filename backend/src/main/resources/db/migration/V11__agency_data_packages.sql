-- V11: Gói dữ liệu cơ quan chức năng (S12, WBS 2.4.1.4, PBI-023).
-- Quyết định: DECISIONS D12, D13, D29, hợp đồng S12 G1 (11/11/2026).
-- Bảng lưu gói dữ liệu, danh sách hộ được duyệt, nội dung snapshot XLSX và chỉ mục retention audit.

CREATE TABLE agency_data_packages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    template_id VARCHAR(50) NOT NULL,
    template_version VARCHAR(20) NOT NULL,
    recipient_agency_code VARCHAR(50) NOT NULL,
    request_reference VARCHAR(100) NOT NULL,
    licence_reference VARCHAR(100) NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    approved_by BIGINT NULL,
    approved_at DATETIME(6) NULL,
    rejected_by BIGINT NULL,
    rejected_at DATETIME(6) NULL,
    revoked_by BIGINT NULL,
    revoked_at DATETIME(6) NULL,
    handed_off_by BIGINT NULL,
    handed_off_at DATETIME(6) NULL,
    download_until DATETIME(6) NULL,
    content_delete_after DATETIME(6) NOT NULL,
    content_deleted_at DATETIME(6) NULL,
    content_sha256 CHAR(64) NOT NULL,
    household_count BIGINT NOT NULL,
    resident_count BIGINT NOT NULL,
    residing_count BIGINT NOT NULL,
    temporarily_absent_count BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_adp_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_adp_approved_by FOREIGN KEY (approved_by) REFERENCES users (id),
    CONSTRAINT fk_adp_rejected_by FOREIGN KEY (rejected_by) REFERENCES users (id),
    CONSTRAINT fk_adp_revoked_by FOREIGN KEY (revoked_by) REFERENCES users (id),
    CONSTRAINT fk_adp_handed_off_by FOREIGN KEY (handed_off_by) REFERENCES users (id),
    CONSTRAINT ck_adp_status CHECK (status IN ('DRAFT', 'APPROVED', 'REJECTED', 'REVOKED')),
    CONSTRAINT ck_adp_version CHECK (version >= 0),
    CONSTRAINT ck_adp_counts CHECK (household_count >= 0 AND resident_count >= 0 AND residing_count >= 0 AND temporarily_absent_count >= 0),
    CONSTRAINT ck_adp_resident_count_sum CHECK (resident_count = residing_count + temporarily_absent_count),
    CONSTRAINT ck_adp_approved_not_creator CHECK (approved_by IS NULL OR approved_by != created_by),
    KEY idx_adp_created_at_id (created_at, id),
    KEY idx_adp_status (status),
    KEY idx_adp_content_delete_after (content_delete_after)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE agency_data_package_households (
    package_id BIGINT NOT NULL,
    household_id BIGINT NOT NULL,
    PRIMARY KEY (package_id, household_id),
    CONSTRAINT fk_adph_package FOREIGN KEY (package_id) REFERENCES agency_data_packages (id),
    CONSTRAINT fk_adph_household FOREIGN KEY (household_id) REFERENCES households (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE agency_data_package_contents (
    package_id BIGINT NOT NULL,
    file_bytes MEDIUMBLOB NOT NULL,
    byte_length BIGINT NOT NULL,
    content_sha256 CHAR(64) NOT NULL,
    PRIMARY KEY (package_id),
    CONSTRAINT fk_adpc_package FOREIGN KEY (package_id) REFERENCES agency_data_packages (id),
    CONSTRAINT ck_adpc_byte_length CHECK (byte_length <= 10485760)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_audit_s12_retention ON audit_log (action, occurred_at, id);
