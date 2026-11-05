-- V4 cho task S02 (cấp account, email mô phỏng, bootstrap BQT và cư dân). Phụ thuộc V2 (users) và V3 (households).
-- Quyết định: docs/DECISIONS.md, bullet "Schema nghiệp vụ V2/V3/V4", câu 1 và 4 (Sơn duyệt 05/11/2026; chờ nhóm xác nhận).
-- Đã chạy qua Flyway trên MySQL 8.4.13 và smoke test ràng buộc. Không INSERT; không đụng dữ liệu.

ALTER TABLE users
    ADD COLUMN email VARCHAR(255) COLLATE utf8mb4_0900_as_ci NULL,   -- câu 1(a): PBI-001 "từ chối email trùng". NULL được để thêm cột an toàn khi đã có dòng; service bắt buộc email khi cấp account mới
    ADD COLUMN household_id BIGINT NULL,                              -- câu 4(a): NULL cho nhân sự, service bắt buộc cho role RESIDENT; một account một hộ. PBI-006: quyền theo hộ lấy từ cột này, không tin householdId của client
    ADD UNIQUE KEY uq_users_email (email),
    ADD KEY idx_users_household (household_id),
    ADD CONSTRAINT fk_users_household FOREIGN KEY (household_id) REFERENCES households (id);
-- Nếu nhóm đổi sang một người dùng nhiều hộ: bỏ household_id, thay bằng bảng nối user_households (xem db-fix/README.md).
