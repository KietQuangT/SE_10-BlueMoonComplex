# Schema V2–V4 cần nhóm xác nhận

Tác giả: Quân · 05/11/2026

## Đây là gì
Sau khi đối chiếu `feature/database-schema` với `docs/` và `AGENTS.md`, Sơn đề xuất thay `db/01_schema.sql` bằng 3 migration Flyway, tách theo task, kế thừa tên bảng/cột của Tuấn khi docs im lặng:

| File | Task | Nội dung |
|---|---|---|
| `V2__identity_audit.sql` | S01 | `roles` (3 vai trò), `users`, `audit_log` |
| `V3__residency.sql` | S05 | `households`, `residents`, `residency_changes` |
| `V4__user_email_household.sql` | S02 | `users.email` duy nhất, `users.household_id` |

Phí, hóa đơn, thu tiền chưa có (V5): còn chờ quy tắc ở mục "Cần Quân" bên dưới.

## Đã kiểm chứng
- Chạy `-Pintegration verify` trên MySQL 8.4.13: V1 một mình, rồi V1–V4. Cả hai `BUILD SUCCESS`; Flyway áp đủ 4 migration.
- 16 ca ràng buộc đúng kỳ vọng (UNIQUE, CHECK, FK, collation, mặc định). Chi tiết trong `EVIDENCE.md`.
- Chưa kiểm: ánh xạ entity với `ddl-auto=validate` (chưa có entity).

## Cần mỗi người xác nhận
- **Kiệt (CODEOWNER):** bullet "Schema nghiệp vụ V2/V3/V4" ở cuối mục "Những điểm chưa xác nhận" của `docs/DECISIONS.md`; nhận PR.
- **Tuấn:** nhận xét về nhánh của bạn (bỏ `CREATE DATABASE`/`USE`, bỏ seed tài khoản, bảng audit, `PENDING` thay `APPROVED`); review V2/V3 (S01 do Tuấn review). Bảng phí/thu tiền của bạn giữ lại làm gợi ý cho V5.
- **Sơn:** câu 8, quy tắc phí/thu tiền, chặn V5: làm tròn (đề xuất sẵn VND HALF_UP), hiệu lực đơn giá, thu thiếu/thừa/hủy, khóa idempotency khi thu, khoản tự nguyện tách khỏi công nợ.
- **Cả nhóm (F01):** ma trận quyền; 3 vai trò v1 (ADMIN, ACCOUNTANT, RESIDENT); `users.household_id` một account một hộ; tập giá trị `residency_status` và `change_type`.

## Tám quyết định (Quân đã duyệt, chờ nhóm)
1. Email: cột riêng duy nhất ở V4, tách khỏi `username`.
2. Chuyển đi: `residency_status` + `residency_changes`, không `is_deleted`.
3. 3 vai trò v1; vai trò thứ tư chờ mẫu báo cáo cư trú.
4. Account–hộ: `users.household_id` (NULL cho nhân sự).
5. CCCD không UNIQUE ở DB; service kiểm tra trùng trong nhóm đang cư trú.
6. `residency_status`, `change_type` là VARCHAR không CHECK; F01 chốt giá trị.
7. `audit_log.actor_user_id` NULL chỉ cho bootstrap bằng lệnh local.
8. Phí/thu tiền: chưa viết, chặn V5.
Bản đầy đủ có lý do và cách đổi: bullet trong `docs/DECISIONS.md`.

## Đổi ý thì làm gì
Chưa merge: sửa thẳng file V2–V4. Đã merge: chỉ thêm migration mới (`AGENTS.md`: không sửa migration đã chia sẻ).

## Chạy thử
Lệnh và cảnh báo về volume nằm trong `README.md`. Mô tả bảng, ERD, quan hệ: `DATABASE.md`.
