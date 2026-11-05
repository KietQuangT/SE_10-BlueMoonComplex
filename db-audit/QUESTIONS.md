# Câu hỏi cho nhóm: dán vào `docs/DECISIONS.md`, mục "Những điểm chưa xác nhận và cách xử lý"

Mọi "đề xuất" dưới đây **chưa được nhóm duyệt**. Tôi không sửa `docs/DECISIONS.md`; Kiệt là CODEOWNER của repo, nên nhóm tự quyết cách ghi. Mỗi câu ghi rõ V2/V3 nháp đang mã hóa gì, để đổi đáp án thì biết sửa chỗ nào.

```markdown
- Schema nghiệp vụ, đề xuất V2/V3 (chưa duyệt). Chặn các migration liên quan cho tới khi nhóm trả lời:
  1. Email tài khoản (PBI-001: cấp account theo email, từ chối email trùng, gửi mật khẩu tạm mô phỏng; REQUIREMENTS.md:9).
     (a) Thêm cột `email` duy nhất ở migration của task cấp tài khoản (thêm cột sau không phá dữ liệu). (b) Dùng email làm `username`.
     Đề xuất chưa duyệt: (a). Lý do: `username` giữ độc lập với địa chỉ liên lạc; (b) buộc đổi username khi đổi email. V2 nháp: chưa có cột email.
  2. Chuyển đi (PBI-010, REQUIREMENTS.md:16: "trạng thái/xóa mềm, không mất lịch sử, không hiện trong danh sách cư trú hiện tại").
     (a) chỉ `residency_status`; (b) cờ `is_deleted`; (c) chỉ suy ra từ `residency_changes`.
     Đề xuất chưa duyệt: (a) kèm bản ghi `residency_changes`. Lý do: lọc danh sách hiện tại bằng một cột có index, lịch sử nằm ở bảng biến động; (b) thêm cơ chế thứ hai chồng lên (a). V3 nháp: có (a), không có `is_deleted`.
  3. Tên và số vai trò (PBI-003: Ban quản trị, Kế toán, Cư dân, Bên liên quan; NFR: Cơ quan chức năng) và ma trận quyền.
     Đề xuất chưa duyệt: chỉ chốt trước 3 vai trò có chức năng v1 (Ban quản trị, Kế toán, Cư dân); vai trò thứ tư chờ mẫu báo cáo cư trú (DECISIONS.md, mục mẫu báo cáo). V2 nháp: bảng `roles` có sẵn, không seed; vai trò thêm bằng lệnh/migration riêng.
  4. Mapping account–hộ (chặn S02; PBI-006: không tin householdId do client gửi).
     (a) `users.household_id` NULL cho nhân sự, bắt buộc cho cư dân (một account một hộ). (b) bảng nối `user_households`.
     Đề xuất chưa duyệt: (a) nếu mỗi account cư dân chỉ thuộc một hộ tại một thời điểm; (b) nếu một người dùng nhiều hộ. Cần Ban quản trị xác nhận quy tắc này. V2/V3 nháp: chưa có (cột phải thêm sau V3 vì `households` ra đời ở V3).
  5. `identity_number` của nhân khẩu có duy nhất không, và xử lý khi người đã chuyển đi quay lại.
     (a) UNIQUE toàn bảng: chặn người quay lại. (b) không UNIQUE, service kiểm tra trùng trong nhóm đang cư trú. (c) UNIQUE chỉ với người đang cư trú (MySQL cần cột sinh/hàm index).
     Đề xuất chưa duyệt: (b) cho Sprint 1, vì (a) làm hồ sơ người đã chuyển đi chặn đăng ký lại, còn (c) phụ thuộc tập giá trị ở câu 6. Thông báo lỗi trùng không được chứa số giấy tờ (AGENTS.md:28). V3 nháp: không UNIQUE.
  6. Tập giá trị của `residency_status` và `change_type` (sinh, mất, chuyển đến/đi, tạm trú, tạm vắng xuất hiện ở nguồn nhưng chưa là hợp đồng).
     Đề xuất chưa duyệt: F01 chốt tập giá trị trước S05; schema để VARCHAR không CHECK để đổi không cần migration. V3 nháp: `residency_status` NOT NULL không DEFAULT; `change_type` không CHECK.
  7. `audit_log.actor_user_id` có được NULL cho bootstrap bằng lệnh local không (DECISIONS.md:20: BQT đầu tiên bootstrap bằng lệnh local).
     Đề xuất chưa duyệt: cho phép NULL, chỉ cho hành động bootstrap (action cố định, ví dụ `BOOTSTRAP_ADMIN`); service bắt buộc actor ở mọi hành động khác. V2 nháp: NULL được.
  8. (Cho migration phí/thu tiền, chưa viết V4) Chặn V4 cho tới khi chốt:
     - Rounding/precision: DECISIONS.md:22 đã nêu mặc định đề xuất VND, làm tròn cuối bằng HALF_UP; cần xác nhận.
     - Hiệu lực đơn giá, thu thiếu/thừa/hủy, dạng khóa idempotency (bắt buộc hay không), khoản tự nguyện tách khỏi công nợ (DECISIONS.md:22-23; REQUIREMENTS.md:18).
     Checklist kỹ thuật cho V4 nằm ở AUDIT.md mục E.
```
