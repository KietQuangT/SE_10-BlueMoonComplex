# Nhận xét review gửi Tuấn (bản nháp để Sơn tự đăng; chưa đăng gì lên GitHub)

Gợi ý nơi đăng: comment trên PR hoặc commit `3eba2aa` của nhánh `feature/database-schema`. Sơn sửa giọng văn trước khi đăng. Mỗi ý kèm nguồn để Tuấn tự kiểm.

---

Chào Tuấn, mình đã đọc `feature/database-schema` và đối chiếu với `docs/` + `AGENTS.md`. Cảm ơn bạn đã dựng sẵn nền: tiền dùng `DECIMAL`, `charge_details` giữ số lượng và đơn giá đã áp (khớp `ARCHITECTURE.md:31`, `REQUIREMENTS.md:17`), `fee_rates` có hiệu lực theo ngày kèm CHECK, UNIQUE theo hộ/kỳ chặn trùng kỳ, người chuyển đi vẫn được giữ lịch sử, và `username` 100 ký tự khớp `PRINCIPAL_NAME` của Spring Session. Số liệu seed cũng cộng đúng.

Mình chưa chạy SQL nào trên MySQL, nên phần dưới là đọc tĩnh.

## Cần sửa trước khi vào repo chính
1. **Đưa vào Flyway, bỏ `CREATE DATABASE`/`USE`** (`01_schema.sql:1,5`). `AGENTS.md:22` yêu cầu mọi thay đổi schema qua `V<n>__mo_ta.sql` với `ddl-auto=validate`; `application.yml` đã bật Flyway; compose tạo DB bằng `MYSQL_DATABASE`. Hiện `V1` là bảng `SPRING_SESSION`, nên file của bạn sẽ thành `V2` trở đi.
2. **Tách theo task** (`ARCHITECTURE.md:13`): bảng nghiệp vụ chỉ tạo trong task có AC đã chốt. Đề xuất: `V2` cho S01 (roles, users, audit), `V3` cho S05 (hộ, nhân khẩu, biến động), bảng phí/thu tiền để cho S07/S08/S09.
3. **Thêm bảng audit** (`ARCHITECTURE.md:18,41`; `REQUIREMENTS.md:36`): actor, action, entity, thời điểm, nội dung thay đổi đã mask PII. S01, S05, S09 đều cần.
4. **`residency_changes.status` mặc định `PENDING`**, không phải `APPROVED` (`REQUIREMENTS.md:14-15`: form hợp lệ vào trạng thái chờ duyệt, BQT duyệt/từ chối).
5. **Seed:** bỏ tài khoản `admin.demo`/`accountant.demo` với hash giả (`AGENTS.md:26,28`; `DECISIONS.md:20`: không tài khoản/mật khẩu mặc định trong migration; BQT đầu tiên bootstrap bằng lệnh local). Dữ liệu mẫu để ngoài migration.
6. **Bỏ `VEHICLE`, `METER` và `MOTORBIKE_PARKING`** (phí xe/tiện ích thuộc V2: `REQUIREMENTS.md:46`).
7. **Quy tắc thu tiền chưa được nhóm chốt** (`DECISIONS.md:22`; `REQUIREMENTS.md:20,50`; S09): `PARTIALLY_PAID`, `CHECK paid_amount <= total_amount` và việc không có cách hủy/đảo đang ngầm chọn một đáp án. Đề nghị để trống cho tới khi nhóm quyết.

## Nên cân nhắc khi làm bảng phí/thu tiền
- Thiếu hạn nộp ở `billing_periods` (`REQUIREMENTS.md:28`) và UNIQUE theo loại phí trong một khoản phải thu (S08: "không trùng kỳ/hộ/loại").
- Chống thu trùng: `payments.reference_code` cho phép NULL nên UNIQUE không chặn tiền mặt (`AGENTS.md:20`; WBS 2.4.1.1: gửi lặp không tăng số thu). Cần khóa idempotency và cột `version`.
- Khoản tự nguyện đang nằm trong `household_charges` với trạng thái `UNPAID` (`REQUIREMENTS.md:18`: không cộng vào công nợ bắt buộc). README của bạn cũng nói "kept separate", nên nên tách bằng thiết kế hoặc ràng buộc.
- `charge_details.fee_rate_id` chưa bắt cùng `fee_type_id` với dòng phí; `paid_amount`/`status` chưa bị ràng buộc khớp với `payments`.
- `DATETIME` không có phần thập phân sẽ làm tròn nửa giây (ví dụ `23:59:59.7` thành ngày sau); `DEFAULT CURRENT_TIMESTAMP` theo múi giờ session trong khi ứng dụng ghi UTC (`application.yml`).
- `head_resident_id` có thể trỏ người của hộ khác và tạo FK vòng với `residents`.
- `03_test.sql`: T1/T3/T5 chủ yếu SELECT, chưa có ca vi phạm ràng buộc; `DELIMITER` chỉ chạy trong client `mysql`. Có thể chuyển các assert thành integration test.

## Câu hỏi để nhóm chốt (mình sẽ đưa vào `DECISIONS.md` nếu mọi người đồng ý)
Email so với username cho tài khoản (PBI-001); cơ chế chuyển đi (`MOVED_OUT` hay `is_deleted`); tên và số vai trò (PBI-003 gọi "Bên liên quan", NFR gọi "Cơ quan chức năng"); mapping account–hộ; CCCD có duy nhất không. Danh sách đầy đủ trong `QUESTIONS.md`.

## Mình đã soạn sẵn
Hai bản nháp migration `V2__identity_audit.sql` và `V3__residency.sql`, kế thừa tên bảng/cột của bạn khi docs không nói gì, chỉ bỏ phần chưa có căn cứ. Đây là đề xuất để bạn xem và chỉnh, không phải bản cuối; cần F01 xác nhận trường dữ liệu trước khi vào repo.
