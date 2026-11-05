-- V2 cho task S01 (identity/session/role/audit; docs/sprint-1/TASKS.md).
-- Quyết định: docs/DECISIONS.md, bullet "Schema nghiệp vụ V2/V3/V4" (Sơn duyệt 05/11/2026; chờ nhóm xác nhận).
-- Đã chạy qua Flyway trên MySQL 8.4.13 và smoke test ràng buộc. F01 còn phải xác nhận trường dữ liệu và ma trận quyền:
-- nếu đổi thì sửa file này TRƯỚC khi merge; sau khi merge chỉ thêm migration mới (AGENTS.md).
-- Không CREATE DATABASE, USE; không tài khoản/mật khẩu (AGENTS.md:22,26,28).
-- Quy ước kỹ thuật (không phải yêu cầu): BIGINT có dấu khớp Long; DATETIME(6) do app ghi UTC theo D09
-- (DECISIONS.md:15); trạng thái dùng VARCHAR thay ENUM để tránh rủi ro ENUM với ddl-auto=validate.

CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) COLLATE utf8mb4_0900_as_ci NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_code (code),
    UNIQUE KEY uq_roles_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Câu 3: chỉ 3 vai trò có chức năng v1 (PBI-003). Đây là nhãn tham chiếu, KHÔNG phải ma trận quyền (F01 chốt).
-- Vai trò thứ tư (Bên liên quan / Cơ quan chức năng) chưa thêm: chờ mẫu báo cáo cư trú (DECISIONS.md).
INSERT INTO roles (code, name) VALUES
    ('ADMIN', 'Ban quản trị'),
    ('ACCOUNTANT', 'Kế toán'),
    ('RESIDENT', 'Cư dân');

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    username VARCHAR(100) COLLATE utf8mb4_0900_as_ci NOT NULL,   -- khớp SPRING_SESSION.PRINCIPAL_NAME VARCHAR(100) ở V1; phân biệt dấu, không phân biệt hoa/thường
    password_hash VARCHAR(255) NOT NULL,      -- AGENTS.md:28: chỉ hash qua PasswordEncoder
    full_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- PBI-004: khóa account
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- Câu 1 và 4: email và liên kết hộ thêm ở V4 (task S02), vì liên kết hộ cần bảng households của V3.
-- Chưa có cột khóa/đếm đăng nhập sai: ngưỡng chống brute-force chưa chốt (DECISIONS.md:21).

CREATE TABLE audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    occurred_at DATETIME(6) NOT NULL,
    actor_user_id BIGINT NULL,                -- câu 7: NULL chỉ cho hành động bootstrap bằng lệnh local (action cố định, ví dụ BOOTSTRAP_ADMIN); service bắt buộc actor ở mọi hành động khác
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NULL,
    changes TEXT NULL,                        -- chỉ nội dung được phép ghi, đã mask PII (REQUIREMENTS.md:36; AGENTS.md:28)
    PRIMARY KEY (id),
    KEY idx_audit_entity (entity_type, entity_id, occurred_at),
    KEY idx_audit_actor (actor_user_id, occurred_at),
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- ARCHITECTURE.md:18,41: audit ghi cùng transaction với thay đổi dữ liệu (việc của service, không phải của schema).
-- Tên bảng/cột là đề xuất kỹ thuật: docs chưa định. Không đặt retention (DECISIONS.md:26).
