-- BẢN NHÁP ĐỀ XUẤT cho task S05 (hộ/nhân khẩu/biến động; chuyển đi dùng trạng thái/xóa mềm).
-- Chưa chạy trên MySQL. Phụ thuộc V2 (users). Tên cột kế thừa thiết kế của Tuấn khi docs im lặng:
-- đó KHÔNG phải yêu cầu đã chốt, F01 phải xác nhận (docs/REQUIREMENTS.md:27, TASKS.md F01).
-- Không CREATE DATABASE, USE, INSERT.

CREATE TABLE households (
    id BIGINT NOT NULL AUTO_INCREMENT,
    household_code VARCHAR(50) COLLATE utf8mb4_0900_as_ci NOT NULL,   -- phân biệt dấu, không phân biệt hoa/thường
    apartment_code VARCHAR(50) COLLATE utf8mb4_0900_as_ci NOT NULL,
    area_m2 DECIMAL(10,2) NOT NULL,           -- AGENTS.md:19: diện tích dùng BigDecimal; scale là quy ước kỹ thuật
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
-- Bỏ head_resident_id của bản Tuấn: tạo FK vòng với residents, và quan hệ chủ hộ docs chưa nêu.

CREATE TABLE residents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    household_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    identity_number VARCHAR(50) NULL,         -- PII (AGENTS.md:28). KHÔNG unique: quy tắc trùng chưa chốt (câu 5)
    date_of_birth DATE NULL,
    phone VARCHAR(30) NULL,
    relationship_to_head VARCHAR(100) NULL,
    residency_status VARCHAR(30) NOT NULL,    -- REQUIREMENTS.md:16: trạng thái thay cho xóa; tập giá trị do F01 chốt (câu 6), nên KHÔNG đặt DEFAULT hay CHECK ở đây
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_residents_household (household_id, residency_status),
    KEY idx_residents_name (full_name),
    CONSTRAINT fk_residents_household FOREIGN KEY (household_id) REFERENCES households (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- Bỏ is_deleted của bản Tuấn: REQUIREMENTS.md:16 chỉ cần "trạng thái/xóa mềm", giữ một cơ chế (câu 2).

CREATE TABLE residency_changes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    resident_id BIGINT NOT NULL,
    household_id BIGINT NOT NULL,             -- giữ lịch sử khi đổi hộ (docs/sources UserStory US-012: người, ngày đến, ngày đi)
    change_type VARCHAR(30) NOT NULL,         -- tập giá trị do F01 chốt (câu 6)
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
