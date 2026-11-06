---
name: BlueMoon Complex
description: "Quản lý cư trú và thu phí chung cư: giao diện trung tính, một màu nhấn xanh cobalt, nền xanh ngả trăng, chữ có dấu tiếng Việt rõ."
colors:
  ground: "#f2f6fd"
  panel: "#ffffff"
  subtle: "#f3f3f3"
  muted: "#eeeeee"
  ink: "#0a0a0a"
  text-secondary: "#333333"
  text-muted: "#5a5a5a"
  brand: "#1d4ed8"
  brand-hover: "#185530"
  brand-soft: "#e8f1ec"
  brand-ink: "#14522c"
  amber: "#b45309"
  amber-soft: "#fef3c7"
  amber-ink: "#7c3405"
  danger: "#b91c1c"
  danger-soft: "#fee2e2"
  danger-ink: "#7f1d1d"
  info: "#1d4e6b"
  info-soft: "#e6f0f6"
  hairline: "#dcdcdc"
  control-edge: "#767676"
typography:
  hero:
    fontFamily: "Newsreader, Georgia, serif"
    fontSize: "clamp(2rem, 1.5rem + 2vw, 3.25rem)"
    fontWeight: 500
    lineHeight: 1.2
    letterSpacing: "-0.015em"
  headline:
    fontFamily: "Newsreader, Georgia, serif"
    fontSize: "clamp(1.75rem, 1.4rem + 1.2vw, 2.375rem)"
    fontWeight: 500
    lineHeight: 1.2
    letterSpacing: "-0.015em"
  title:
    fontFamily: "Hanken Grotesk, system-ui, sans-serif"
    fontSize: "1.375rem"
    fontWeight: 600
    lineHeight: 1.3
  body:
    fontFamily: "Hanken Grotesk, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: "Hanken Grotesk, system-ui, sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 600
    lineHeight: 1.4
rounded:
  sm: "8px"
  md: "12px"
  lg: "20px"
  pill: "999px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
  xxl: "48px"
components:
  button-primary:
    backgroundColor: "{colors.brand}"
    textColor: "#ffffff"
    rounded: "{rounded.sm}"
    height: "44px"
    padding: "10px 20px"
  button-primary-hover:
    backgroundColor: "{colors.brand-hover}"
  button-secondary:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    height: "44px"
    padding: "10px 20px"
  input:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    height: "48px"
    padding: "10px 14px"
  panel:
    backgroundColor: "{colors.panel}"
    rounded: "{rounded.md}"
---

# Design System: BlueMoon Complex

## Overview

**Creative North Star: "Quầy lễ tân yên tĩnh"** (The Quiet Front Desk). Một văn phòng Ban quản lý gọn gàng: giấy trắng, mực đen, một cây bút xanh cobalt để đánh dấu việc cần làm. Giao diện không trang trí; số tiền, mã căn hộ và trạng thái là nhân vật chính.

Chế độ **Operate**: người dùng làm việc lặp lại (tra cứu, thu tiền, duyệt đơn), nhiều cư dân lớn tuổi dùng một tay trên điện thoại 360px. Nền trung tính 90–95% màn hình; màu cobalt chỉ xuất hiện ở nút chính, vòng focus, liên kết và dấu mục đang chọn. Gradient và vầng trăng chỉ ở nền (Login, đầu vùng làm việc, chân thanh bên, xem `moon.svg`); panel, bảng, biểu mẫu luôn trắng. Không dùng bóng đổ trang trí, chữ in hoa, hay màu tím.

Hệ này thay thế hoàn toàn bản "Biển chỉ dẫn tuyến Văn Phú" trước đó (bản cũ lưu ở `_agents_workspace/backup/`). Bảng màu `mint-pink-accounts` và cặp chữ Newsreader / Hanken Grotesk được chọn bằng `start.mjs deal --page service` của Taste Skill; Newsreader và Hanken Grotesk đều có bộ ký tự tiếng Việt (Instrument Sans và Schibsted Grotesk, hai cặp còn lại, thì không).

**Key Characteristics:**
- Một màu nhấn duy nhất; trạng thái luôn có nhãn chữ và kiểu viền, không chỉ màu.
- Chữ nội dung ≥16px, nhãn ≥13px; tương phản chữ ≥7:1 trên nền, viền ô nhập ≥4,5:1.
- Nút 44px (48px trên điện thoại), ô nhập 48px, mọi vùng chạm ≥44px.
- Bảng hẹp tự chuyển thành mỗi dòng một thẻ.

## Colors

Trung tính ấm-lạnh cân bằng, một màu xanh cobalt của dịch vụ.

### Primary
- **Xanh cobalt Blue Moon** (#1d4ed8): nút chính (chữ trắng 6,7:1), vòng focus, liên kết, dấu mục đang chọn. Dưới 3% diện tích mỗi màn. Hover #1e40af; nền nhạt #e6eefc; chữ trên nền nhạt #1e3a8a.

### Neutral
- **Giấy ánh trăng** (#f2f6fd): nền trang. **Tờ trắng** (#ffffff): panel, bảng, hộp thoại. **Xanh nhạt** (#eaf0fa, #e2eaf7): hàng được rê chuột, mục đang chọn, thanh công cụ.
- **Mực** (#0a0a0a): chữ chính, nút mã căn hộ. **Chữ phụ** (#333333) và **chữ mờ** (#5a5a5a, ≥6:1) cho nhãn, ngày, ghi chú.
- **Đường kẻ** (#dcdcdc): cấu trúc. **Viền ô nhập** (#767676): cạnh của điều khiển, 4,5:1 trên nền trắng.

### Semantic (chỉ cho trạng thái, luôn kèm nhãn chữ)
- **Hổ phách** (#b45309, nền #fef3c7, chữ #7c3405): còn nợ, chờ xử lý (viền đứt).
- **Đỏ** (#b91c1c, nền #fee2e2, chữ #7f1d1d): lỗi, hành động hủy.
- **Xanh biển** (#1d4e6b, nền #e6f0f6): ghi chú dữ liệu mẫu (chỉ DEV/Demo).

### Named Rules
**The One Voice Rule.** Chỉ một màu nhấn. Màu trạng thái không bao giờ đứng một mình: luôn có chữ ("Chưa đóng", "Đã đóng") và kiểu viền (liền, đứt, kép).

## Typography

**Display Font:** Newsreader (Georgia, serif) · **Body Font:** Hanken Grotesk (system-ui, sans-serif). Số, mã căn hộ và tiền dùng Hanken Grotesk với `tabular-nums` và không ngắt dòng giữa số và đơn vị (`50,00 m²`).

**Character:** serif trầm cho tiêu đề (đáng tin, như văn bản hành chính), sans trung tính cho mọi thứ người dùng phải đọc nhanh.

### Hierarchy
- **Hero** (500, clamp 32–52px, 1.2): chỉ tiêu đề trang Đăng nhập.
- **Headline** (500, clamp 28–38px, 1.2): tên màn (`h1`).
- **Title** (600, 22px, 1.3): tiêu đề khối và thẻ (`h2`), cũng là cỡ lớn nhất của số liệu trên màn.
- **Body** (400, 16px, 1.5): nội dung, dòng dài tối đa khoảng 62 ký tự.
- **Label** (600, 13px): nhãn trường, đầu cột bảng, huy hiệu. Không có cỡ dưới 13px; không in hoa.

### Named Rules
**The Figure Cap Rule.** Một con số không bao giờ lớn hơn tiêu đề `h2` cùng màn (22px).

## Layout

Màn hình làm việc: thanh điều hướng trái cố định 264px (nền trắng, đường kẻ), thanh trên 64px, vùng nội dung tối đa 1240px. Từ 900px trở xuống: ẩn thanh trái, thêm thanh điều hướng dưới 3 tab cho cư dân (64px, ba cột đều). Trang Đăng nhập: một màn hình, nội dung và biểu mẫu nằm trên hai mép lề bằng nhau (lề 24–80px).

Spacing theo thang 4, 8, 12, 16, 24, 32, 48, 64. Bảng và danh sách trong khung hẹp (dưới 720px chiều rộng khung, không phụ thuộc cửa sổ) chuyển thành mỗi dòng một thẻ, nhãn cột lấy từ đầu cột (`data-label`). "Bỏ qua menu" là link ẩn ngoài màn, chỉ hiện khi nhận focus.

## Elevation & Depth

Phẳng. Chiều sâu đến từ lớp nền (giấy, tờ trắng, xám nhạt) và đường kẻ 1px. Chỉ hộp thoại, hộp menu nổi và ngăn QA (chỉ DEV) có một bóng đổ nền (`0 12px 32px rgba(10,10,10,.16)`).

## Shapes

Bo góc "mềm" ba bậc: **8px** điều khiển và nút, **12px** panel và bảng, **20px** khung lớn (thẻ đăng nhập). Huy hiệu dùng bo 8px. Không có pill, trừ bộ đếm tròn trên thanh điều hướng.

## Components

- **Nút:** một chiều cao, một cỡ chữ (16px). Chỉ nút chính được tô màu nhấn. Nút phụ viền xám đậm, nền trắng. Nút nguy hiểm (tô đỏ) chỉ dùng trong hộp xác nhận. Nút ghost cho hành động thứ ba.
- **Ô nhập:** cao 48px, nhãn 14px phía trên, viền 4,5:1, viền xanh khi focus, đỏ nhạt khi lỗi kèm câu lỗi.
- **Huy hiệu trạng thái:** nền nhạt + viền + chữ; liền = hoàn tất, đứt = đang chờ, kép = lỗi.
- **Hộp xác nhận:** `<dialog>` gốc; tiêu đề nêu đúng đối tượng, một câu hậu quả, nút là động từ cụ thể ("Vô hiệu hóa ngay"); Esc đóng và **trả focus về nút đã mở**.
- **Bảng sổ cái:** đầu cột 13px, hàng 16px, số căn phải; trong khung hẹp thành thẻ.
- **Dải 3 số (tóm tắt):** ba cột chia bằng đường kẻ đứng, số 22px.

## Do's and Don'ts

### Do
- Luôn định dạng tiền `560.000 đ`, diện tích `50,00 m²`, ngày `dd/MM/yyyy`; che CCCD thành `********1234`.
- Giữ nội dung form khi phiên hết hạn (401).
- Dùng nhãn chữ cho mọi trạng thái.

### Don't
- Không dùng float để tính tiền trên giao diện.
- Không thêm màu nhấn thứ hai, gradient, bóng đổ trang trí, chữ in hoa hay màu tím.
- Không hiện nhãn nội bộ (mã màn, "Giả định", "Chờ chốt", câu hỏi mở) cho người dùng thật: chúng chỉ hiện ở DEV hoặc Demo.
- Không dùng ảnh chụp không phải tòa nhà thật làm "ảnh tòa nhà". Thư viện ảnh của Taste Skill không có chủ đề nhà ở, nên Đăng nhập hiện chưa có ảnh; khi có ảnh thật của BlueMoon thì thêm làm khung bên phải của khối chữ.


## Blue Moon (06/11/2026)
Đổi từ xanh lá/xám sang cobalt + nền ngả xanh theo yêu cầu của Sơn. Vầng trăng (`src/styles/moon.svg`) là nền: to và thở chậm (`--moon-breathe` 9 s, tắt khi reduced-motion) ở Login; nhỏ, tĩnh ở chân thanh bên. Đầu vùng làm việc có quầng sáng xanh nhạt. Bản trước ở `_agents_workspace/backup/frontend-css-20261106-moon/`.

## Sáng / Tối (06/11/2026)
Hai chủ đề, chọn bằng `<html data-theme>` (script trong `index.html` đặt trước khi vẽ; `src/lib/theme.ts` giữ lựa chọn trong `localStorage` khóa `bm-theme`). Mặc định theo hệ thống; nút `ThemeToggle` xoay vòng Tự động → Sáng → Tối ở thanh trên của Login và app.
- **Tối:** nền gần đen xanh `#04060d`, panel `#0b1222`, nhấn xanh điện `#5b8cff`. Chữ trên nút là màu đêm (`--text-on-brand: #04060d`, 6,4:1) vì chữ trắng không đạt 4,5:1 trên màu này. Login là cảnh đầy đủ: trời sao (hai lớp, một lớp nhấp nháy 4,5 s), trăng lớn có quầng xanh, mặt hồ có đường chân trời và vệt phản chiếu gợn sóng; thẻ đăng nhập là kính mờ nằm vắt trên mặt hồ. Trong app: quầng trăng ở đầu vùng làm việc và trăng mờ ở chân thanh bên, không có hồ.
- **Sáng:** cùng cảnh ban ngày: trời xanh nhạt chuyển trắng, trăng nhạt, hồ xanh sáng với vệt phản chiếu trắng. Không có sao.
- **Chung:** bố cục Login một cột giữa (trăng, tiêu đề, thẻ, hàng tầng nhà); cảnh cao đúng một màn hình, trang dài hơn thì tiếp tục trên màu của mặt hồ. Chuyển động (trăng thở 9 s, sao, gợn sóng) tắt hoàn toàn khi reduced-motion. Panel, bảng, biểu mẫu luôn là bề mặt đặc.
- Bản trước khi thêm chế độ tối: `_agents_workspace/backup/frontend-theme-20261106-darklight/`.
