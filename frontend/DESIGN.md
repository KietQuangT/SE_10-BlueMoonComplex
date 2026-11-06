---
name: BlueMoon Complex UI Design System
description: "Hệ thống quản lý cư trú & thu phí BlueMoon — Định hướng 'Biển Chỉ Dẫn Tuyến Văn Phú'"
colors:
  ground: "#F5F7F6"
  rail: "#0F222D"
  primary: "#0A6859"
  accent-amber: "#B45309"
  border: "#D1DCD6"
  slate: "#4A606C"
  danger: "#B91C1C"
  success: "#047857"
typography:
  display:
    fontFamily: "Be Vietnam Pro, sans-serif"
    fontSize: "1.75rem"
    fontWeight: 700
    lineHeight: 1.2
  headline:
    fontFamily: "Be Vietnam Pro, sans-serif"
    fontSize: "1.25rem"
    fontWeight: 700
    lineHeight: 1.3
  body:
    fontFamily: "Be Vietnam Pro, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 400
    lineHeight: 1.5
  tabular:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: "0.875rem"
    fontWeight: 600
    lineHeight: 1.4
rounded:
  sm: "3px"
  md: "6px"
  lg: "8px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.sm}"
    padding: "8px 16px"
  button-secondary:
    backgroundColor: "#FFFFFF"
    textColor: "{colors.rail}"
    rounded: "{rounded.sm}"
    padding: "8px 16px"
---

# Thiết Kế Hệ Thống UI: BlueMoon Complex

Định hướng thiết kế thị giác: **"Biển Chỉ Dẫn Tuyến Văn Phú" (Metro Wayfinding / Transit Infrastructure)**
Cam kết token: `seed:f804f9c82080a8c7`

## Overview

Hệ thống quản lý cư trú và thu phí BlueMoon Complex (30 tầng tại ngã tư Văn Phú, Hà Đông) được xây dựng theo chế độ **Operate**: ưu tiên khả năng quét nhanh (scanability), độ tin cậy cơ sở dữ liệu, kiểm toán giao dịch và tính minh bạch số liệu tài chính. Giao diện lấy cảm hứng từ biển chỉ dẫn nhà ga đường sắt đô thị Cát Linh – Hà Đông (ga Văn Phú), sử dụng dải màu trạm (station strips), thẻ căn hộ dạng biển số (sign plates), và bảng kê sổ cái kế toán dạng tabular.

## Colors

- **Ground (Mặt nền trạm):** `#F5F7F6` — Trắng ngà hạt cát bê tông sạch, không chói mắt khi làm việc liên tục.
- **Rail (Thép đường ray / Văn bản chính):** `#0F222D` — Xanh đen than chì đậm, độ tương phản AA/AAA đạt chuẩn WCAG.
- **Teal Brand (Màu tuyến Văn Phú):** `#0A6859` — Xanh ngọc lam đậm chỉ dẫn tuyến ga, dùng cho hành động chính, trạng thái đã thanh toán.
- **Amber (Cảnh báo nợ / Phí chờ nộp):** `#B45309` — Hổ phách đường ray, báo hiệu công nợ chưa thu.
- **Border (Đường phân làn):** `#D1DCD6` — Xám rêu nhẹ định hình cấu trúc thẻ và bảng.
- **Danger (Lỗi / Cảnh báo hủy):** `#B91C1C` — Đỏ báo động.

## Typography

- **Chữ tiêu đề và nội dung chính:** `Be Vietnam Pro` (400, 500, 600, 700) — Tối ưu hoàn hảo cho dấu thanh tiếng Việt, chân chữ rõ ràng ở kích thước nhỏ.
- **Chữ số liệu, mã căn, tiền tệ và bảng tính:** `JetBrains Mono` (500, 600) — Đảm bảo mọi con số tiền tệ (VND) và diện tích căn hộ (m²) căn thẳng hàng theo cột (tabular numerals), chống nhìn nhầm số 0 và số 8.

## Layout

- **Desktop (≥ 1024px):** Thanh điều hướng dạng ray cố định bên trái (App Rail 240px), thanh trạng thái trạm bên trên (Top Bar), và khu vực làm việc (App Workspace) cuộn độc lập.
- **Mobile (360px – 768px):** Thanh điều hướng dưới đáy (Bottom Navigation 3 tab trọng tâm cho cư dân: Hóa đơn, Hộ của tôi, Khai báo), các bảng số liệu hỗ trợ cuộn ngang mềm (`overflow-x-auto`).
- **Lưới phân trang:** Mặc định 10/25/50 dòng, thanh điều hướng trang có nút Trước / Sau và bộ đếm tổng số bản ghi rõ ràng.

## Elevation & Depth

- Triết lý giao diện phẳng có cấu trúc (Structural Flatness): không sử dụng bóng đổ mờ ảo (drop shadow) lạm dụng.
- Độ sâu được tạo dựng bằng **Tonal Layering** (lớp nền phân tầng `#F5F7F6` và `#FFFFFF`) kết hợp đường viền sắc nét `1px solid #D1DCD6`.
- Hộp thoại nổi (Modal Dialog) sử dụng phông nền đen mờ `rgba(0, 0, 0, 0.4)` với bóng viền nhẹ định vị tâm điểm.

## Shapes

- Bán kính góc bo kỷ luật, cơ khí:
  - Nút bấm và huy hiệu: `3px` (góc vuông gọt nhẹ, phong cách thẻ kiểm soát).
  - Khung trạm và bảng điều khiển: `6px` – `8px`.
- Thẻ căn hộ (`apt-sign-plate`): Khung chữ nhật đính kèm viền đôi mô phỏng biển tên căn hộ tại hành lang chung cư.

## Components

1. **Station Strip (Dải màu trạm):** Đường kẻ dày 3px ở đỉnh mỗi panel (Teal cho BQT, Hổ phách cho hóa đơn chưa nộp, Thép rail cho tài khoản).
2. **Double-Encoded Badges (Huy hiệu mã hóa kép):** Kết hợp đồng thời biểu tượng hình học (stroke) + nhãn chữ + màu sắc để người mù màu vẫn nhận diện chính xác trạng thái:
   - `Đã đóng`: Nền xanh lá + dấu tích tròn `✓`.
   - `Chưa đóng`: Nền hổ phách + chấm cảnh báo `!`.
   - `Tạm vắng`: Nền lam nhạt + biểu tượng đồng hồ.
3. **Contract Badges (Huy hiệu hợp đồng API/DB):**
   - `✅ API thật`: Kết nối trực tiếp máy chủ Spring Boot.
   - `🗄️ Đã có cột DB`: Schema V2–V5 đã hỗ trợ, đang chờ API.
   - `🟡 Giả định`: UI xây dựng theo AC backlog, chờ backend phát triển.
   - `⏸ Chờ chốt quy tắc`: Khung giữ chỗ chờ quyết định họp nhóm.
4. **Confirm Dialog (Tuân thủ file 09, mục G):**
   - Tiêu đề nêu chính xác đối tượng tác động (Căn hộ, Tài khoản).
   - Câu hậu quả rõ ràng trước khi bấm.
   - Nút hành động mang tên động từ cụ thể (ví dụ: "Phát hành ngay", "Vô hiệu hóa ngay", "Xác nhận Đã thu tiền"), tuyệt đối không dùng "OK" hoặc "Có".
   - Phím Esc đóng modal và hoàn trả tiêu điểm (focus) cho nút gọi ban đầu.

## Do's and Don'ts

### Do's
- Luôn hiển thị số tiền VND với dấu chấm phân cách hàng nghìn và hậu tố `đ` (ví dụ `560.000 đ`).
- Luôn hiển thị diện tích căn hộ với dấu phẩy thập phân và hậu tố `m²` (ví dụ `50,00 m²`).
- Mặc định che mờ (masking) số CCCD cư dân thành `********1234` trên mọi màn hình thông thường để bảo vệ dữ liệu cá nhân (NFR-009).
- Giữ nguyên nội dung bản nháp (form draft) khi phiên đăng nhập hết hạn (401) để người dùng không phải nhập lại.

### Don'ts
- Không dùng số thực dấu phẩy động (float/double) để tính toán tiền tệ trên frontend.
- Không tự suy diễn lý do tài khoản bị khóa hoặc hứa hẹn thời gian mở khóa khi đăng nhập thất bại.
- Không cho phép vai trò Kế toán truy cập hồ sơ nhân khẩu, CCCD hoặc danh bạ cư dân.
- Không cho phép vai trò Cơ quan chức năng xem trực tiếp bảng dữ liệu thô chưa qua Ban Quản Trị phê duyệt.
