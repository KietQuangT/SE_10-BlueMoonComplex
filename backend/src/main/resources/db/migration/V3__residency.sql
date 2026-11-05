-- V3 cho task S05 (hộ/nhân khẩu/biến động). Phụ thuộc V2 (users).
-- Quyết định: docs/DECISIONS.md, bullet "Schema nghiệp vụ V2/V3/V4" (Sơn duyệt 05/11/2026; chờ nhóm xác nhận).
-- Đã chạy qua Flyway trên MySQL 8.4.13 và smoke test ràng buộc. Tên cột kế thừa thiết kế của Tuấn khi docs im lặng;
-- F01 phải xác nhận (docs/REQUIREMENTS.md:27, TASKS.md F01); nếu đổi thì sửa file này TRƯỚC khi merge.
-- Không CREATE DATABASE, USE, INSERT.

CREATE TABLE households (
    id BIGINT NOT NULL AUTO_INCREMENT,
    household_code VARCHAR(50) COLLATE utf8mb4_0900_as_ci NOT NULL,
    apartment_code VARCHAR(50) COLLATE utf8mb4_0900_as_ci NOT NULL,
    area_m2 DECIMAL(10,2) NOT NULL,           -- AGENTS.md:19: BigDecimal; scale là quy ước kỹ thuật
    address VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_households_code (household_code),
    UNIQUE KEY uq_households_apartment (apartment_code),   -- REQUIREMENTS.md:27: tra cứu theo căn hộ
    CONSTRAINT ck_households_area CHECK (area_m2 > 0),
    CONSTRAINT ck_households_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- Bỏ head_resident_id của bản Tuấn: FK vòng với residents; quan hệ chủ hộ docs chưa nêu.

CREATE TABLE residents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    identity_number VARCHAR(50) NULL,         -- PII (AGENTS.md:28). Câu 5: KHÔNG UNIQUE; service kiểm tra trùng trong nhóm đang cư trú, lỗi không được chứa số giấy tờ
    date_of_birth DATE NULL,
    phone VARCHAR(30) NULL,
    relationship_to_head VARCHAR(100) NULL,
    residency_status VARCHAR(30) NOT NULL,    -- câu 2: một cơ chế chuyển đi (REQUIREMENTS.md:16); câu 6: tập giá trị F01 chốt, nên KHÔNG DEFAULT/CHECK
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_residents_household (household_id, residency_status),
    KEY idx_residents_name (full_name),
    KEY idx_residents_identity (identity_number),   -- phục vụ kiểm tra trùng ở service (câu 5)
    CONSTRAINT fk_residents_household FOREIGN KEY (household_id) REFERENCES households (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- Không có is_deleted (câu 2): người chuyển đi đổi residency_status và có bản ghi residency_changes.

CREATE TABLE residency_changes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    resident_id BIGINT NOT NULL,
    household_id BIGINT NOT NULL,             -- giữ lịch sử khi đổi hộ (docs/sources UserStory US-012: người, ngày đến, ngày đi)
    change_type VARCHAR(30) NOT NULL,         -- câu 6: tập giá trị F01 chốt, không CHECK
    effective_date DATE NOT NULL,
    reason VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- REQUIREMENTS.md:14-15: form mới chờ duyệt; duyệt/từ chối
    recorded_by BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_rc_resident_date (resident_id, effective_date),
    KEY idx_rc_household (household_id),
    CONSTRAINT fk_rc_resident FOREIGN KEY (resident_id) REFERENCES residents (id),
    CONSTRAINT fk_rc_household FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_rc_user FOREIGN KEY (recorded_by) REFERENCES users (id),
    CONSTRAINT ck_rc_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- Người duyệt không có cột riêng: audit_log ghi actor (ARCHITECTURE.md:41). Chưa có ngày kết thúc tạm vắng:
-- nguồn ghi đây là chi tiết chưa chốt.
