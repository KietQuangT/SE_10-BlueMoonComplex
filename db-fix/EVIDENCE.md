# Bằng chứng kiểm chứng V1–V4

Ngày 05/11/2026, người chạy: Quân.

## Môi trường
Container `mysql:8.4` tạm (MySQL 8.4.13), cổng 127.0.0.1:3307, DB trống; JDK 21.0.9; Spring Boot 4.0.9; Flyway.

## Flyway và integration
| Lượt | Migration | Lệnh | Kết quả |
|---|---|---|---|
| 1 | V1 | `./mvnw -B -ntp -Pintegration verify` | `BUILD SUCCESS`; unit 5/5, integration 2/2; Flyway áp 1 migration |
| 2 | V1–V4 | cùng lệnh | `BUILD SUCCESS`; unit 5/5, integration 2/2; Flyway `validated 4 migrations`, `applied 3 migrations`, đạt v4 |

`flyway_schema_history`: version 1, 2, 3, 4 đều `success = 1`. Bảng tạo ra: `SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES`, `audit_log`, `households`, `residency_changes`, `residents`, `roles`, `users`.

## Smoke test ràng buộc: 16/16 ca khớp (`verify/smoke.sh`)
Mã lỗi xếp theo số dòng trong `verify/smoke_test.sql`.

| Ca | Kết quả | Chứng minh |
|---|---|---|
| T1 | 3 dòng `roles` | Dữ liệu vai trò của V2 |
| T2 | không lỗi; `users_n = 2` | "nam" và "nấm" là hai username khác nhau |
| T3 | `1062` `uq_users_username` | Username không phân biệt hoa/thường |
| T4 | `1452` `fk_users_role` | FK vai trò |
| T5 | `3819` `ck_users_status` | CHECK trạng thái tài khoản |
| T6 | `1062` `uq_users_email` | Email duy nhất, không phân biệt hoa/thường |
| T7 | `3819` `ck_households_area` | Diện tích > 0 |
| T8 | `1062` `uq_households_apartment` | Căn hộ duy nhất, không phân biệt hoa/thường |
| T9 | `1452` `fk_users_household` | FK `users.household_id` |
| T10 | `1364` | `residency_status` không có mặc định |
| T11 | không lỗi; `res_n = 3` | `identity_number` trùng được phép |
| T12 | `status = PENDING` | Mặc định của `residency_changes.status` |
| T13 | `3819` `ck_rc_status` | CHECK trạng thái biến động |
| T14 | không lỗi; `audit_n = 1` | `actor_user_id` NULL được |
| T15 | `1452` `fk_audit_actor` | FK actor |
| T16 | users 2, households 1, residents 3, residency_changes 1, audit_log 1 | Số dòng cuối đúng |

## Giới hạn
- Chưa có entity nên `ddl-auto=validate` chưa kiểm ánh xạ.
- Flyway cảnh báo MySQL 8.4 mới hơn bản đã kiểm (8.1); không lỗi.
- Chưa đo hiệu năng.

## Dán vào `docs/VALIDATION.md`
```markdown
- 05/11/2026, nhánh backend: `cd backend && ./mvnw -B -ntp -Pintegration verify` trên MySQL 8.4.13 (container tạm, DB trống),
  JDK 21.0.9. Lượt 1 chỉ V1: BUILD SUCCESS, unit 5/5, IT 2/2. Lượt 2 thêm V2–V4: BUILD SUCCESS, unit 5/5, IT 2/2,
  Flyway áp V2–V4, schema history version 1–4 success. Smoke test ràng buộc 16/16 ca khớp (`verify/smoke.sh`).
  Giới hạn: chưa có entity nên `ddl-auto=validate` chưa kiểm ánh xạ; Flyway cảnh báo MySQL 8.4 mới hơn bản đã kiểm (8.1).
```
