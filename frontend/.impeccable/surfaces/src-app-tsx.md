---
version: 1
slug: "src-app-tsx"
primary_target: "src/App.tsx"
related_targets: ["index.html","src/styles/tokens.css"]
---

# BlueMoon Complex — Operate Shell & Role Surfaces

- **Scope & Mode:** `Operate` — toàn bộ 24 màn hình v1 (`M-01`–`M-04`, `M-10`–`M-22`, `M-24`, `M-30`–`M-34`, `M-40`) của ứng dụng quản lý thu phí và cư trú Chung cư BlueMoon.
- **Audience & Task:** Ban quản trị, Kế toán, Cư dân (ưu tiên di động 360 px cho người lớn tuổi) và Cơ quan chức năng thực hiện đăng nhập, tra cứu công nợ/hóa đơn, thu tiền mặt, khai báo/duyệt tạm vắng và xuất gói báo cáo trong ≤ 3 thao tác chạm.
- **Constraints:** Ghép API thật cho `/api/v1/auth/*` và `POST /api/v1/accounts` (kèm khóa `mustChangePassword`); dùng `mock-data.json` cho các màn S03–S12 kèm nhãn phân biệt hợp đồng (`API thật`, `Đã có DB`, `Giả định`, `Chờ chốt`); không tính tiền bằng float; che CCCD mặc định.

## Direction contract

THESIS: Biển chỉ dẫn tuyến metro Văn Phú đưa kỷ luật định hướng nhà ga vào quản lý chung cư, từ chối bảng quản trị xám nhạt nhiều thẻ con lồng nhau để vai trò, nghĩa vụ phí và chặng duyệt đơn đọc ngay trong một ánh nhìn.
OWN-WORLD: Nền gạch sứ sáng `#F5F7F6`, thanh điều hướng dọc xanh than `#0F222D` mang vạch tuyến ngọc lục bảo `#0D7A68`, chữ Việt `Be Vietnam Pro` kết hợp số bảng `JetBrains Mono`, nhãn trạng thái mã hóa kép bằng màu và kiểu nét viền (liền, đứt, kép), cùng dòng đang chọn nổi viền tương phản rõ.
STORY: Người dùng nhận ra ngay căn hộ hoặc vai trò đang thao tác, tin cậy tuyệt đối vào số tiền snapshot và ranh giới giữa phí bắt buộc với quỹ tự nguyện, rồi hoàn tất tra cứu, khai báo hoặc gạch nợ trong ba bước.
FIRST VIEWPORT: Trên máy tính 1440×900, cột điều hướng trái rộng 264px nền `#0F222D` neo vai trò ở đáy; thanh đỉnh trắng 68px gắn mã căn/vai trò khổ lớn và tác vụ phiên; tâm điểm là dải ba trạm ngang (Còn nợ · Hạn nộp · Đơn đang chờ) đặt ngay trên bảng dữ liệu phân trang có thanh lọc tab và nút hành động chính màu `#0D7A68` ở góc phải; trên di động 360px rút thành thanh đỉnh gọn và thanh 3 tab đáy cao ≥ 56px.
FORM: Biển Chỉ Dẫn Tuyến Văn Phú (Metro Wayfinding), vị trí 5/7 trong danh sách đề xuất nội địa, seed:f804f9c82080a8c7.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
