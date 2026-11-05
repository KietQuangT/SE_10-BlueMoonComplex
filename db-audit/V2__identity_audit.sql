-- BẢN NHÁP ĐỀ XUẤT cho task S01 (identity/session/role/audit; docs/sprint-1/TASKS.md).
-- Chưa chạy trên MySQL. Chờ F01 xác nhận trường dữ liệu và tên vai trò trước khi đưa vào
-- backend/src/main/resources/db/migration/. Không CREATE DATABASE, USE, INSERT (AGENTS.md:22,26,28).
-- Quy ước kỹ thuật (không phải yêu cầu đã chốt): BIGINT có dấu khớp Long; DATETIME(6) do app ghi UTC
-- theo D09 (DECISIONS.md:15, đang ở mức "đề xuất"); trạng thái dùng VARCHAR thay ENUM để tránh rủi ro
-- ENUM với ddl-auto=validate.

CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) COLLATE utf8mb4_0900_as_ci NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_code (code),
    UNIQUE KEY uq_roles_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- Không seed vai trò: tên và ma trận quyền chưa chốt (DECISIONS.md:20; PBI-003 liệt kê Ban quản trị,
-- Kế toán, Cư dân, Bên liên quan; NFR gọi "Cơ quan chức năng").

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    username VARCHAR(100) COLLATE utf8mb4_0900_as_ci NOT NULL,   -- khớp SPRING_SESSION.PRINCIPAL_NAME VARCHAR(100) ở V1; không phân biệt hoa/thường nhưng phân biệt dấu (tránh "nam" trùng "nấm")
    password_hash VARCHAR(255) NOT NULL,      -- AGENTS.md:28: chỉ hash qua PasswordEncoder
    full_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- PBI-004: vô hiệu hóa tài khoản
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- Cố ý CHƯA có: email (PBI-001, chờ quyết định email hay username: QUESTIONS.md câu 1), liên kết hộ
-- (DECISIONS.md:20, câu 4), cột khóa/đếm đăng nhập sai (DECISIONS.md:21).

CREATE TABLE audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    occurred_at DATETIME(6) NOT NULL,
    actor_user_id BIGINT NULL,                -- NULL cho bootstrap bằng lệnh local (DECISIONS.md:20; câu 7)
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
