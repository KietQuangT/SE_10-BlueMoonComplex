-- Smoke test ràng buộc cho V2–V4. Chạy bằng smoke.sh (mysql --force: lỗi không dừng script).
-- Mỗi dòng "expect" ghi kết quả đúng; sau dòng ERROR tương ứng phải thấy mã lỗi ghi trong ngoặc.
-- Dữ liệu giả, chỉ tồn tại trong container tạm.

SELECT 'T1 expect OK: 3 vai trò' AS test, COUNT(*) AS n FROM roles;

INSERT INTO users (role_id, username, password_hash, full_name, created_at, updated_at)
VALUES (1, 'nam', 'hash-gia', 'Nguoi A', NOW(6), NOW(6));
SELECT 'T2 expect OK: "nấm" khác "nam"' AS test;
INSERT INTO users (role_id, username, password_hash, full_name, created_at, updated_at)
VALUES (1, 'nấm', 'hash-gia', 'Nguoi B', NOW(6), NOW(6));

SELECT 'T3 expect ERROR 1062: "Nam" trùng "nam" (không phân biệt hoa/thường)' AS test;
INSERT INTO users (role_id, username, password_hash, full_name, created_at, updated_at)
VALUES (1, 'Nam', 'hash-gia', 'Nguoi C', NOW(6), NOW(6));

SELECT 'T4 expect ERROR 1452: role_id không tồn tại' AS test;
INSERT INTO users (role_id, username, password_hash, full_name, created_at, updated_at)
VALUES (999, 'zz', 'hash-gia', 'Nguoi D', NOW(6), NOW(6));

SELECT 'T5 expect ERROR 3819: users.status ngoài ACTIVE/INACTIVE' AS test;
INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at)
VALUES (1, 'yy', 'hash-gia', 'Nguoi E', 'LOCKED', NOW(6), NOW(6));

UPDATE users SET email = 'a@example.test' WHERE username = 'nam';
SELECT 'T6 expect ERROR 1062: email trùng (kể cả khác hoa/thường)' AS test;
UPDATE users SET email = 'A@EXAMPLE.TEST' WHERE username = 'nấm';

INSERT INTO households (household_code, apartment_code, area_m2, created_at, updated_at)
VALUES ('H1', 'A-101', 50.00, NOW(6), NOW(6));
SELECT 'T7 expect ERROR 3819: area_m2 <= 0' AS test;
INSERT INTO households (household_code, apartment_code, area_m2, created_at, updated_at)
VALUES ('H2', 'A-102', 0, NOW(6), NOW(6));
SELECT 'T8 expect ERROR 1062: apartment_code trùng ("a-101")' AS test;
INSERT INTO households (household_code, apartment_code, area_m2, created_at, updated_at)
VALUES ('H3', 'a-101', 40.00, NOW(6), NOW(6));

UPDATE users SET household_id = 1 WHERE username = 'nam';
SELECT 'T9 expect ERROR 1452: users.household_id không tồn tại' AS test;
UPDATE users SET household_id = 999 WHERE username = 'nấm';

SELECT 'T10 expect ERROR 1364: residency_status không có DEFAULT' AS test;
INSERT INTO residents (household_id, full_name, created_at, updated_at)
VALUES (1, 'Cu dan 1', NOW(6), NOW(6));
INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at)
VALUES (1, 'Cu dan 1', 'TEST_VALUE', NOW(6), NOW(6));
SELECT 'T11 expect OK: identity_number trùng được phép (câu 5)' AS test;
INSERT INTO residents (household_id, full_name, identity_number, residency_status, created_at, updated_at)
VALUES (1, 'Cu dan 2', '000000000001', 'TEST_VALUE', NOW(6), NOW(6)),
       (1, 'Cu dan 3', '000000000001', 'TEST_VALUE', NOW(6), NOW(6));

INSERT INTO residency_changes (resident_id, household_id, change_type, effective_date, recorded_by, created_at)
VALUES ((SELECT MIN(id) FROM residents), 1, 'TEST_VALUE', CURDATE(), (SELECT MIN(id) FROM users), NOW(6));   -- id lấy bằng MIN vì INSERT lỗi trước đó có thể đã tiêu id tự tăng
SELECT 'T12 expect OK: status mặc định PENDING' AS test, status FROM residency_changes WHERE id = 1;
SELECT 'T13 expect ERROR 3819: residency_changes.status ngoài PENDING/APPROVED/REJECTED' AS test;
INSERT INTO residency_changes (resident_id, household_id, change_type, effective_date, status, recorded_by, created_at)
VALUES ((SELECT MIN(id) FROM residents), 1, 'TEST_VALUE', CURDATE(), 'DONE', (SELECT MIN(id) FROM users), NOW(6));

SELECT 'T14 expect OK: audit_log actor NULL (bootstrap, câu 7)' AS test;
INSERT INTO audit_log (occurred_at, actor_user_id, action, entity_type)
VALUES (NOW(6), NULL, 'BOOTSTRAP_ADMIN', 'users');
SELECT 'T15 expect ERROR 1452: audit_log actor không tồn tại' AS test;
INSERT INTO audit_log (occurred_at, actor_user_id, action, entity_type)
VALUES (NOW(6), 999, 'X', 'users');

SELECT 'T16 expect OK: số dòng cuối' AS test,
       (SELECT COUNT(*) FROM users) AS users_n,        -- 2
       (SELECT COUNT(*) FROM households) AS hh_n,      -- 1
       (SELECT COUNT(*) FROM residents) AS res_n,      -- 3
       (SELECT COUNT(*) FROM residency_changes) AS rc_n, -- 1
       (SELECT COUNT(*) FROM audit_log) AS audit_n;    -- 1
