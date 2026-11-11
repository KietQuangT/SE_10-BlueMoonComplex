# Yêu cầu làm chuẩn khi triển khai Sprint 1

## Phạm vi đã chọn

User đã chọn WBS mới làm phạm vi v1 ngày 04/11/2026. Các AC dưới đây được tóm tắt theo Product Backlog rồi đối chiếu với yêu cầu bảo mật/dữ liệu; không khẳng định mapping US cũ đã chính xác. Skeleton chưa hoàn thành bất kỳ PBI nào.

| PBI | Đầu ra backend và acceptance criteria tối thiểu |
|---|---|
| 001 | BQT cấp account theo email/role hợp lệ; từ chối email trùng; email mật khẩu tạm mô phỏng đúng người. Chốt bootstrap BQT và cấp account cư dân trước code. |
| 002 | Người dùng đổi mật khẩu sau khi kiểm tra mật khẩu cũ; mật khẩu mới ít nhất 8 ký tự và quy tắc mạnh đã duyệt; yêu cầu login lại, cookie cũ không truy cập được. |
| 003 | API cung cấp vai trò/quyền để frontend dựng menu; server chặn hành động sai quyền và dữ liệu hộ khác. Chưa login trả 401, đã login thiếu quyền trả 403. |
| 004 | BQT khóa account khi cư dân chuyển đi; request mới dùng mọi session cũ bị từ chối; giữ lịch sử. Không đồng nhất PBI này với US-004 nói audit. |
| 006 | Cư dân lấy đúng danh sách thành viên hộ của mình; không chấp nhận householdId client gửi làm bằng chứng sở hữu. |
| 007 | Gửi form tạm vắng hợp lệ vào trạng thái chờ duyệt; cư dân chỉ khai báo trong phạm vi đã cấp. |
| 008 | BQT duyệt/từ chối đúng quyền; trạng thái/lịch sử được lưu; hộ nhận kết quả qua kênh nội bộ/mô phỏng được chốt. |
| 010 | Chuyển đi dùng trạng thái/xóa mềm, không làm mất lịch sử; hồ sơ không xuất hiện trong danh sách cư trú hiện tại. |
| 011 | Thiết lập đơn giá theo hiệu lực; 50m² × 10.000đ = 500.000đ cho từng loại phí; lịch sử hóa đơn giữ diện tích/giá đã áp dụng. |
| 012 | Có đợt đóng góp tự nguyện, ghi nhận theo người/đợt/số tiền; không tự cộng vào công nợ bắt buộc. |
| 013 | Cư dân xem chi tiết thành phần hóa đơn, kỳ và lịch sử của đúng hộ; phân trang và lọc trạng thái đã chốt. |
| 015 | Kế toán ghi nhận tiền mặt, cập nhật nợ đúng số tiền trong transaction và lưu audit; ngăn duplicate/concurrent. Chốt thu thiếu/thừa/hủy trước code. |
| 021 | Xuất Excel chứa tổng thu, tổng nợ và tỷ lệ; công thức mẫu số phải được duyệt; tổng khớp dữ liệu DB. |
| 022 | Xuất danh sách cư trú theo mẫu/cột đã thống nhất. Chưa có mẫu thì không gọi là đã đạt chuẩn pháp lý. |
| 023 | Cơ quan chỉ nhận gói dữ liệu BQT đã xác thực/phê duyệt, không truy cập dữ liệu thô hoặc trường ngoài phạm vi. |

## Phần WBS gộp nhưng vẫn phải có

- CRUD hộ/nhân khẩu, tra cứu theo căn hộ và lịch sử biến động; schema và contract phải được chốt với người viết UI/phí.
- Tạo kỳ thu hàng loạt có danh sách hộ, loại phí, hạn nộp và ngăn tạo trùng; backlog PBI-011/012 chưa mô tả hết nên cần AC bổ sung trước task.
- Audit theo NFR-003; chống brute-force có triển khai riêng trước khi QA kiểm tra; không chỉ tạo task kiểm thử.

## NFR cần theo dõi

| NFR | Điều cần thực hiện / bằng chứng |
|---|---|
| 001–002 | Quyền vai trò và quyền theo đối tượng ở server; test truy cập chéo hộ và vai trò. |
| 003 | Actor, thời điểm, thao tác và nội dung thay đổi quan trọng; audit nhất quán với transaction, tránh lộ PII. |
| 004 | Tra cứu hồ sơ/danh sách khoản thu ≤3 giây với 10.000 hồ sơ; ghi phần cứng, query, phân trang và cách đo. Đây là ngưỡng có sẵn trong nguồn, sửa nhận định cũ rằng chưa có ngưỡng. |
| 005–006 | Frontend dễ dùng, truy cập bằng trình duyệt; backend có contract/lỗi phù hợp. Mục tiêu 3 thao tác chạm trong Charter thuộc nghiệm thu UI. |
| 007 | BigDecimal, công thức đã duyệt, đối chiếu độc lập, transaction/idempotency/unique theo nghiệp vụ. |
| 008 | Backup tự động định kỳ và diễn tập restore; volume Docker không thay thế backup. |
| 009 | Whitelist trường export và quyền công bố; không trả entity/toàn bộ bản ghi cho cơ quan. |
| 010 | Lưu lịch sử theo thời hạn được xác nhận; “ví dụ 5 năm” trong nguồn chưa phải quyết định pháp lý của dự án. |

## V2 theo quyết định phạm vi

PBI-005, 009, 014, 016–020; phí xe/tiện ích, QR/biên lai, thông báo, phản ánh, khách thăm/mượn đồ, VNeID và công khai quỹ theo WBS. Không xóa nhu cầu khỏi backlog. Thuế/ký số/camera và các yêu cầu rộng khác trong 52 US cần mapping/phạm vi riêng; không tự kéo vào Sprint 1.

## Đầu vào phải chốt theo task

Ma trận quyền và account–hộ → auth/cư trú; trường dữ liệu/duyệt sửa → CRUD; rounding/hiệu lực đơn giá → phí; thu thiếu/thừa/hủy → payment; mẫu/công thức tỷ lệ/trường được phép → reporting. Khi thiếu đầu vào, Codex ghi task bị chặn, không tự tạo quy tắc rồi coi là yêu cầu đã duyệt.
