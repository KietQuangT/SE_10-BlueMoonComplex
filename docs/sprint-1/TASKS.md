# Checklist Sprint 1

Mã F/S là task kỹ thuật trong harness, không phải mã WBS mới. S/M/L chỉ độ phức tạp. Toàn bộ chưa hoàn thành cho tới khi có bằng chứng; file skeleton được tạo không đồng nghĩa F00/F02 đã nghiệm thu.

| Xong | Mã | Cỡ | Người phụ trách | Công việc / WBS | Tiền nhiệm / điều kiện | Acceptance criteria |
|---|---|---|---|---|---|---|
| ☐ | F00 | S | Kiệt | Repo, wrapper, hướng dẫn, CI; WBS 1.2.1.2 | Không | Clone/build được; repo có AGENTS, PR checklist và CI; xác nhận main sau merge. |
| ☐ | F01 | M | Sơn + Quân + Tuấn | Chốt contract, quyền và dữ liệu; WBS 1.2.1.1–3 | F00 | Ma trận quyền/account–hộ, trường dữ liệu và hợp đồng API có người nhận xác nhận; giải quyết mapping sai. |
| ☐ | F02 | M | Sơn + Tuấn | Nền tảng backend/DB và luồng mẫu; WBS 1.2.1.2 | F00 | Health/metadata, security boundary, Flyway/session chạy MySQL; teammate cài lại và có kết quả integration. |
| ☐ | S01 | M | Sơn; Tuấn review | Identity/session/role/audit; WBS 2.1.1.1, 2.1.1.3 | F01,F02 | Đăng nhập đúng/sai, đổi session ID, 401/403, audit write; chốt và test chống brute-force, không mặc định đã có. |
| ☐ | S02 | M | Sơn + Kiệt | Cấp account và email mô phỏng; WBS 2.1.1.1,2.1.2.3 | S01 | Email trùng bị từ chối; mock email đúng người; bootstrap BQT và cấp cư dân đã thống nhất; không log password. |
| ☐ | S03 | M | Kiệt + Sơn | Đổi mật khẩu, hồ sơ; WBS 2.1.1.2,2.1.2.1 | S01 | Kiểm tra mật khẩu cũ/mới, tối thiểu 8 ký tự; đổi xong các session cũ bị từ chối; DTO không lộ hash. |
| ☐ | S04 | M | Sơn + Tuấn | Vô hiệu account và test EP-01; WBS 2.1.2.2,2.1.1.3 | S01 | Khóa xong request mới với cookie cũ thất bại; thử nhiều session và server role; UI cần test tích hợp để đóng. |
| ☐ | S05 | L | Sơn + Tuấn | Hộ/nhân khẩu/biến động; WBS 2.2.1.1 | F01,S01 | CRUD/tra cứu, ownership; chuyển đi xóa mềm và audit/lịch sử; validate trường đã chốt, test hộ khác bị chặn. |
| ☐ | S06 | L | Kiệt + Sơn + Tuấn | Khai báo/duyệt/tra cứu; WBS 2.2.1.2–3 | S05 bản API đầu | Chờ duyệt → duyệt/từ chối đúng quyền; lưu trạng thái/lịch sử, thông báo trong app/mock theo AC; cuối cùng ghép UI. |
| ☐ | S07 | M | Quân + Kiệt; Tuấn kiểm tra | Đơn giá/công thức; WBS 2.3.1.1,2.3.1.3 | F01,F02 | BigDecimal; 50m² × 10.000 = 500.000; hiệu lực/rounding đã chốt; không áp thay giá vào hóa đơn cũ. |
| ☐ | S08 | L | Quân + Tuấn | Kỳ thu/tự nguyện; WBS 2.3.1.2–3 | S07; S05 để ghép cuối | Không trùng kỳ/hộ/loại; tự nguyện không cộng nợ; giao dữ liệu thật cho thu tiền và đối chiếu độc lập. |
| ☐ | S09 | L | Quân + Sơn | Ghi nhận thu tiền/gạch nợ; WBS 2.4.1.1 | S05,S08 | Thu đúng số tiền, nợ giảm đúng, transaction/audit; duplicate/concurrent không thu hai lần; chốt thu thiếu/thừa/sửa/hủy. |
| ☐ | S10 | M | Kiệt + Quân | API hóa đơn/lịch sử cho cư dân; WBS 2.4.1.2 | S08; S09 để ghép cuối | Chỉ dữ liệu hộ của account; chi tiết thành phần/kỳ/nợ đúng; pagination; thay householdId không xem được hộ khác. |
| ☐ | S11 | M | Tuấn + Quân + Kiệt | Export thu phí/cư trú; WBS 2.4.1.3 | S06,S09 | Excel mở được và tổng khớp; mẫu/cột/tỷ lệ đã duyệt; không export dữ liệu thừa; dùng dữ liệu thật từ DB. |
| ☐ | S12 | M | Tuấn + Sơn | Gói dữ liệu cơ quan; WBS 2.4.1.4, PBI-023 | S11 | Chỉ gói/trường BQT phê duyệt; cơ quan không truy DB thô; có test deny trường/hộ ngoài phạm vi. |
| ☐ | S13 | M | Tuấn; Sơn hỗ trợ | Backup tự động/restore, NFR-008; gắn DoD 2.4.1.4 | F02; dữ liệu mẫu S09 | Job backup có lịch/retention được chốt; restore vào DB thử riêng rồi đối chiếu; không xóa DB gốc. |
| ☐ | S14 | L | Cả nhóm; Tuấn điều phối | Hồi quy/quyền/hiệu năng nền; WBS 2.4.1.4 | S02–S13 hoàn tất; có thể chuẩn bị trước | E2E hộ→kỳ→thu→báo cáo; ca sai/IDOR/duplicate; 10k và thời gian có cấu hình đo; sửa và test lại. |
| ☐ | S15 | S | Cả nhóm; Kiệt tổng hợp | Đóng gói/bàn giao v1; WBS 2.4.1.4 | S14 | Cài từ máy sạch, dữ liệu giả, hướng dẫn, kết quả test và lỗi còn lại có PO chấp nhận; không còn blocker. |

## Checklist có thể tick

- [ ] F00 — Repo, wrapper, hướng dẫn, CI; WBS 1.2.1.2
- [ ] F01 — Chốt contract, quyền và dữ liệu; WBS 1.2.1.1–3
- [ ] F02 — Nền tảng backend/DB và luồng mẫu; WBS 1.2.1.2
- [ ] S01 — Identity/session/role/audit; WBS 2.1.1.1, 2.1.1.3
- [ ] S02 — Cấp account và email mô phỏng; WBS 2.1.1.1,2.1.2.3
- [ ] S03 — Đổi mật khẩu, hồ sơ; WBS 2.1.1.2,2.1.2.1
- [ ] S04 — Vô hiệu account và test EP-01; WBS 2.1.2.2,2.1.1.3
- [ ] S05 — Hộ/nhân khẩu/biến động; WBS 2.2.1.1
- [ ] S06 — Khai báo/duyệt/tra cứu; WBS 2.2.1.2–3
- [ ] S07 — Đơn giá/công thức; WBS 2.3.1.1,2.3.1.3
- [ ] S08 — Kỳ thu/tự nguyện; WBS 2.3.1.2–3
- [ ] S09 — Ghi nhận thu tiền/gạch nợ; WBS 2.4.1.1
- [ ] S10 — API hóa đơn/lịch sử cho cư dân; WBS 2.4.1.2
- [ ] S11 — Export thu phí/cư trú; WBS 2.4.1.3
- [ ] S12 — Gói dữ liệu cơ quan; WBS 2.4.1.4, PBI-023
- [ ] S13 — Backup tự động/restore, NFR-008; gắn DoD 2.4.1.4
- [ ] S14 — Hồi quy/quyền/hiệu năng nền; WBS 2.4.1.4
- [ ] S15 — Đóng gói/bàn giao v1; WBS 2.4.1.4

## Bằng chứng task
Khi hoàn tất, thêm: mã task; commit/PR; lệnh và kết quả test; đầu ra giao cho ai; assumption đã xác nhận; lỗi còn lại.
