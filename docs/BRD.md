# Business Requirements Document (BRD)

**Dự án:** SE_10 — BlueMoon Complex  
**Phiên bản:** 0.1 — Draft for Review  
**Ngày:** 06/11/2026  
**Trạng thái:** Chưa phê duyệt baseline

## 1. Mục đích tài liệu

Tài liệu này mô tả nhu cầu nghiệp vụ, phạm vi, tác nhân, luồng chính và
tiêu chí cấp cao của hệ thống BlueMoon. Đây là bản BRD dự thảo để nhóm rà
soát; các mục `TBD` hoặc `Blocked` chưa được xem là quyết định triển khai.

Nguồn yêu cầu chi tiết và các mâu thuẫn đã đối chiếu được ghi trong
[SOURCE_REVIEW.md](./SOURCE_REVIEW.md). Phạm vi thực thi v1 lấy theo
[REQUIREMENTS.md](./REQUIREMENTS.md) và WBS đã được nhóm chọn.

## 2. Bối cảnh và vấn đề

Ban quản trị chung cư BlueMoon hiện quản lý hồ sơ hộ/nhân khẩu, khoản thu,
đóng góp và thanh toán bằng phương thức thủ công hoặc bảng tính. Cách làm
này gây khó khăn khi:

- theo dõi hộ, nhân khẩu và biến động cư trú;
- lập kỳ thu và tính phí theo diện tích;
- ghi nhận thanh toán, công nợ và đóng góp tự nguyện;
- truy vết người thay đổi dữ liệu;
- tổng hợp và chia sẻ báo cáo đúng phạm vi quyền.

Hệ thống cần số hóa các luồng trên, bảo vệ dữ liệu theo vai trò và giữ được
lịch sử tài chính/cư trú.

## 3. Mục tiêu nghiệp vụ

1. Quản lý tập trung tài khoản, hộ gia đình, nhân khẩu và lịch sử cư trú.
2. Tạo đơn giá, kỳ thu, hóa đơn/khoản phải thu và chi tiết từng loại phí.
3. Ghi nhận thanh toán đúng số tiền, ngăn thu trùng và cập nhật công nợ nhất quán.
4. Phân biệt phí bắt buộc với đóng góp tự nguyện.
5. Cung cấp báo cáo tổng thu, tổng nợ và tỷ lệ theo công thức được duyệt.
6. Giới hạn truy cập và xuất dữ liệu theo vai trò, hộ và phạm vi phê duyệt.
7. Lưu audit cho các thay đổi nghiệp vụ quan trọng.

## 4. Stakeholder và actor

| Actor | Trách nhiệm nghiệp vụ |
|---|---|
| Ban quản trị | Quản lý tài khoản, hộ/nhân khẩu, duyệt cư trú, tạo khoản thu, xem/xuất báo cáo |
| Kế toán | Thiết lập đơn giá, lập kỳ thu, ghi nhận thanh toán, đối soát và báo cáo tài chính |
| Cư dân | Xem dữ liệu của hộ mình, gửi khai báo cư trú, xem hóa đơn và lịch sử thanh toán |
| Cơ quan chức năng/Bên liên quan | Bên nhận gói ngoài hệ thống; trong S12 không có account/API/role đăng nhập. BQT xử lý yêu cầu/giấy phép ngoài hệ thống, tạo, duyệt và audit gói trước khi chuyển |

Actor nghiệp vụ không đồng nhất với thành viên hoặc owner của repository.

## 5. Phạm vi

### 5.1. Phạm vi v1

| Mã | Năng lực |
|---|---|
| PBI-001–004 | Cấp tài khoản, đổi mật khẩu, phân quyền và vô hiệu hóa tài khoản |
| PBI-006 | Cư dân xem thành viên hộ của mình |
| PBI-007–008 | Khai báo và duyệt tạm vắng |
| PBI-010 | Ghi nhận chuyển đi bằng trạng thái/xóa mềm |
| PBI-011 | Thiết lập đơn giá và tính phí theo diện tích |
| PBI-012 | Tạo đợt đóng góp tự nguyện |
| PBI-013 | Cư dân xem chi tiết hóa đơn và lịch sử |
| PBI-015 | Kế toán xác nhận giao dịch thủ công |
| PBI-021–023 | Báo cáo thu/nợ, báo cáo cư trú và gói dữ liệu được phê duyệt |

Phần WBS bắt buộc đi kèm: CRUD hộ/nhân khẩu, tra cứu, lịch sử biến động,
tạo kỳ thu hàng loạt, audit và kiểm soát chống thu trùng.

### 5.2. Ngoài phạm vi v1 hoặc cần chốt thêm

- VNeID, QR, ví điện tử, thanh toán tự động.
- Phí xe và tiện ích nếu chưa có AC v1 được phê duyệt.
- Thông báo/push, phản ánh, khách thăm, mượn đồ, camera.
- Chat cộng đồng và các dịch vụ tòa nhà mở rộng.
- Thuế, ký số, hóa đơn điện tử.
- Công khai quỹ khi chưa có quy tắc sổ thu–chi và số dư.

Các nội dung trên không bị xóa khỏi backlog; chúng được theo dõi ở v2 hoặc
`TBD` theo [SOURCE_REVIEW.md](./SOURCE_REVIEW.md).

## 6. Sơ đồ Use Case

Các sơ đồ Draw.io hiện có:

- [uc1.drawio](../uc1.drawio): tài khoản, bảo mật, hộ gia đình và cư trú.
- [uc2.drawio](../uc2.drawio): khoản thu, kỳ thu, thanh toán và kế toán.
- [uc3.drawio](../uc3.drawio): thông báo, dịch vụ và báo cáo.

`uc1` và các phần v1 của `uc2` là đầu vào trực tiếp cho Sprint 1. `uc3`
chứa cả năng lực v2/TBD nên không được dùng riêng nó để mở rộng phạm vi v1.

## 7. Yêu cầu nghiệp vụ cấp cao

### BR-01 — Tài khoản và quyền

- BQT cấp tài khoản theo email/role hợp lệ.
- Email trùng bị từ chối.
- Người dùng đổi mật khẩu sau lần đăng nhập đầu.
- Server kiểm tra quyền; 401 dùng cho chưa đăng nhập, 403 dùng cho thiếu quyền.
- Khóa tài khoản phải ngăn request mới và giữ lịch sử.

### BR-02 — Hộ và cư trú

- Một hộ có thể có nhiều nhân khẩu.
- BQT quản lý hồ sơ hộ/nhân khẩu và tra cứu theo trường được phép.
- Cư dân chỉ xem hoặc khai báo trong phạm vi hộ được cấp.
- Tạm vắng/tạm trú có trạng thái chờ duyệt, duyệt hoặc từ chối.
- Chuyển đi không xóa mất lịch sử.

### BR-03 — Khoản thu và kỳ thu

- Kế toán/BQT quản lý loại khoản thu và đơn giá có hiệu lực.
- Hệ thống tạo kỳ thu hàng loạt cho danh sách hộ được chọn.
- Khoản phí bắt buộc và đóng góp tự nguyện phải được phân biệt.
- Không tạo trùng kỳ thu hoặc bảng kê theo khóa nghiệp vụ đã duyệt.
- Hóa đơn lưu snapshot diện tích, đơn giá và chi tiết đã áp dụng.

### BR-04 — Thanh toán và công nợ

- Kế toán ghi nhận giao dịch tiền mặt hoặc phương thức được phê duyệt.
- Giao dịch, số đã thu và số còn nợ được cập nhật trong cùng transaction.
- Phải có quy tắc được duyệt cho thu thiếu, thu thừa, sửa và hủy.
- Hệ thống ngăn duplicate và xử lý cạnh tranh giao dịch.
- Mọi thay đổi quan trọng có audit.

### BR-05 — Báo cáo và chia sẻ dữ liệu

- Báo cáo tổng thu, tổng nợ và tỷ lệ dùng công thức được phê duyệt.
- Báo cáo phải khớp dữ liệu giao dịch trong DB.
- Danh sách cư trú dùng mẫu/cột đã thống nhất.
- Cơ quan chức năng chỉ nhận gói do BQT tạo, duyệt và audit; S12 không cấp account/API/role đăng nhập cho cơ quan. Yêu cầu và kiểm tra giấy phép do BQT xử lý ngoài hệ thống. ADMIN chọn danh sách hộ ACTIVE rõ ràng, người ADMIN khác duyệt; người tạo được từ chối gói DRAFT; gói cư trú chỉ có mã hộ, căn hộ, họ tên, trạng thái cho nhân khẩu RESIDING/TEMPORARILY_ABSENT. Snapshot lưu 30 ngày từ lúc tạo, tải tối đa đến mốc sớm hơn giữa duyệt+7 ngày và tạo+30 ngày; các trường audit/retention theo D13.
- Không trả dữ liệu thô hoặc trường ngoài whitelist.

## 8. Quy tắc dữ liệu và phi chức năng

- Tiền dùng `DECIMAL/BigDecimal`, không dùng số thực nhị phân.
- Lưu lịch sử thay đổi và audit; không ghi PII/secret vào log kỹ thuật.
- NFR-004: tra cứu hồ sơ hoặc danh sách khoản thu không quá 3 giây với
  10.000 hồ sơ; phải ghi cấu hình máy, query, phân trang và cách đo.
- NFR-008: backup định kỳ và có bằng chứng restore; Docker volume không thay
  thế backup.
- NFR-010: thời hạn lưu dữ liệu phải được xác nhận trước khi purge.
- Dữ liệu chia sẻ phải dùng projection/whitelist theo vai trò.

## 9. Luồng nghiệp vụ chính

### 9.1. Tạo kỳ thu và lập khoản phải thu

1. Người có quyền chọn kỳ thu, loại phí và danh sách hộ.
2. Hệ thống kiểm tra đơn giá/hiệu lực và dữ liệu diện tích.
3. Hệ thống tính chi tiết từng khoản và tạo bảng kê.
4. Hệ thống từ chối bản ghi trùng.
5. Người có quyền xác nhận kỳ thu.

### 9.2. Ghi nhận thanh toán

1. Kế toán tìm khoản phải thu theo hộ/kỳ.
2. Hệ thống hiển thị số phải thu, đã thu và còn nợ.
3. Kế toán nhập số tiền và phương thức.
4. Hệ thống kiểm tra trạng thái và quy tắc thu thiếu/thừa.
5. Transaction ghi payment, cập nhật công nợ và audit.

### 9.3. Khai báo và duyệt tạm vắng

1. Cư dân gửi khai báo trong phạm vi hộ được cấp.
2. Hệ thống lưu trạng thái chờ duyệt.
3. BQT duyệt hoặc từ chối.
4. Hệ thống lưu quyết định và lịch sử cư trú.
5. Hộ nhận kết quả qua kênh đã được nhóm chốt.

## 10. Tiêu chí nghiệm thu cấp BRD

BRD chỉ được chuyển sang baseline khi nhóm đã xác nhận:

- ma trận quyền và quan hệ account–hộ;
- quy tắc làm tròn, hiệu lực đơn giá, thu thiếu/thừa, sửa/hủy;
- khóa nghiệp vụ chống trùng kỳ thu và giao dịch;
- công thức báo cáo và mẫu xuất;
- trường được phép chia sẻ cho cơ quan;
- chính sách audit, backup và retention;
- mapping cuối giữa Use Case, PBI, User Story và NFR.

## 11. Vấn đề mở cần quyết định

| Mã | Nội dung | Trạng thái |
|---|---|---|
| TBD-01 | Ma trận quyền chi tiết theo actor và đối tượng | Chưa chốt |
| TBD-02 | Account cư dân liên kết với hộ/nhân khẩu thế nào | Chưa chốt |
| TBD-03 | Rounding, thu thiếu/thừa, sửa/hủy thanh toán | Chưa chốt |
| TBD-04 | Công thức tỷ lệ thu hồi nợ và mẫu Excel | Chưa chốt |
| TBD-05 | Phạm vi, allowlist và hợp đồng template/API/lifecycle S12 được Sơn duyệt tại G1 ngày 11/11/2026; implementation còn chờ kiểm chứng G2 | Đã chốt G1; chưa có bằng chứng G2 |
| TBD-06 | Retention chung cho các miền dữ liệu còn mở; riêng S12 đã chốt snapshot 30 ngày, metadata/scope/reference và audit 5 năm theo D13 | Chưa chốt ngoài S12 |
| TBD-07 | Nguồn và quy tắc báo cáo quỹ | Blocked |

## 12. Tài liệu liên quan

- [REQUIREMENTS.md](./REQUIREMENTS.md)
- [ARCHITECTURE.md](./ARCHITECTURE.md)
- [DECISIONS.md](./DECISIONS.md)
- [SOURCE_REVIEW.md](./SOURCE_REVIEW.md)
- [Sprint 1 PLAN](./sprint-1/PLAN.md)
- [Sprint 1 TASKS](./sprint-1/TASKS.md)
