# Frontend BlueMoon Complex

Giao diện web của hệ thống quản lý cư trú và thu phí chung cư BlueMoon. Kiệt phụ trách phần này; bản hiện tại là kết quả đợt làm lại giao diện ngày 06/11/2026 (Sơn), dựng trên nhánh `feat/frontend` của Kiệt.

> **Trạng thái mã:** frontend đã được commit; các thay đổi giao diện cần Kiệt xem trước khi đưa vào nhánh chung.

## Chạy

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173 ; /api và /actuator proxy tới http://localhost:8080
npm test         # 22 test (vitest + jsdom)
npm run build    # tsc -b && vite build -> dist/
```

- Node 22 trở lên. React 19, TypeScript 5.7, Vite 6, React Router 7. Không có thư viện UI hay CSS framework; không phụ thuộc runtime nào khác.
- Cùng origin `/api` khi đóng gói; dev dùng proxy. Phiên là cookie `HttpOnly` của server (`credentials: 'include'`), lệnh ghi gửi `X-CSRF-TOKEN` lấy từ `GET /api/v1/auth/csrf`. **Không** lưu session hoặc token trong `localStorage`/`sessionStorage` (chỉ lưu lựa chọn giao diện sáng/tối, khóa `bm-theme`).
- Công cụ trình diễn (Demo, ngăn QA, nhãn nội bộ) chỉ hiện ở `npm run dev` hoặc bản build có `VITE_ENABLE_DEMO=true`. Bản build thật không được bật biến này.

## Đã ghép backend và chưa ghép

| Phần | Trạng thái |
|---|---|
| Đăng nhập, đăng xuất, `me`, CSRF (`/api/v1/auth/*`), đổi mật khẩu bắt buộc | Đã ghép (S01) |
| Đổi mật khẩu `POST /api/v1/auth/change-password` (204; mọi phiên bị hủy, đăng nhập lại) | Đã ghép (S03). Cần backend build từ nhánh S03. Mới chạy thử với backend thật ca sai mật khẩu hiện tại (400, banner đúng); chưa thử đổi thành công, luồng bị ép đổi và banner khóa |
| Tạo tài khoản (`POST /api/v1/accounts`) | Đã ghép (S02) |
| Vô hiệu, bật lại, mở khóa tài khoản (S04: `POST /accounts/{id}/disable`, `enable`, `unlock`) | **Đã ghép** ở màn Tài khoản (M-10) trong chế độ API; backend S04 chưa vào `main` (nhánh `hhoangsonnw/feat-s04`). Chưa có API liệt kê tài khoản, nên chế độ API chỉ hiện tài khoản vừa cấp trong phiên |
| Hộ và nhân khẩu (S05: `/households`, `/households/{id}/residents`, `/residents/{id}`, `move-out`, `residency-changes`, `/households/me/residents`) | **Đã ghép** M-12, M-13, M-14 (thêm/sửa hộ, thêm nhân khẩu), M-15 (chuyển đi và lưu trữ), M-30 (Hộ của tôi). Chế độ API tải dữ liệu thật; Demo vẫn dùng dữ liệu mẫu |
| Tạm vắng (S06) | Đã ghép ở mức mã: khai báo, duyệt, từ chối, hủy, quay lại và gợi ý tên; chưa thử với backend thật trên Chrome |
| Hóa đơn, thu tiền, đóng góp, báo cáo | Dữ liệu mẫu `src/data/mockData.ts` (Demo), chưa có API |

Mọi màn dùng dữ liệu mẫu đều ghi rõ trên giao diện ở chế độ DEV/Demo. Người dùng thật không thấy nhãn nội bộ.

## Cấu trúc

```
src/
  api/          client fetch (CSRF, thử lại sau 403, mã X-Request-Id)
  components/   AppShell (thanh bên + thanh trên), ThemeToggle, ConfirmDialog, QaDrawer, ...
  lib/          format (tiền, diện tích, ngày), theme (sáng/tối), stackTables, devTools
  screens/      common (Login, Profile, Error), resident, admin, accountant, authority
  state/        AppStateContext (phiên, chế độ api/demo)
  styles/       tokens.css (token + hai chủ đề), app.css, utilities.css, moon.svg, stars*.svg
  test/         5 file, 22 test
```

## Giao diện hiện tại (Blue Moon)

- **Phông chữ:** Newsreader cho tiêu đề, Hanken Grotesk cho nội dung; cả hai có bộ ký tự tiếng Việt. Tải từ Google Fonts với `display=optional`: lần xem đầu mạng chậm có thể dùng font dự phòng cho trang đó, lần sau dùng font đã lưu.
- **Màu:** nhấn cobalt `#1d4ed8`, nền ngả xanh `#f2f6fd`. Panel, bảng, biểu mẫu luôn trắng (sáng) hoặc tối đặc `#0b1222` (tối).
- **Sáng/Tối:** mặc định theo hệ thống; nút xoay vòng Tự động, Sáng, Tối ở thanh trên của Login và app. Script trong `index.html` đặt chủ đề trước khi vẽ, không nháy. Ở chế độ tối, chữ trên nút là màu đêm vì chữ trắng không đạt 4,5:1 trên xanh điện.
- **Login:** một cột giữa, cảnh trăng lớn có quầng xanh, trời sao, mặt hồ có vệt phản chiếu (tối), hoặc trời xanh nhạt, trăng nhạt, hồ ban ngày (sáng). Cảnh làm bằng SVG và CSS, cao đúng một màn hình.
- **App:** quầng trăng ở đầu vùng làm việc, trăng mờ ở chân thanh bên, logo chỉ có chữ "BlueMoon" và trăng khuyết.
- **Chuyển động** (trăng thở 9 s, sao nhấp nháy, gợn sóng) tắt hoàn toàn khi bật reduced-motion.

Chi tiết token, quy tắc và lý do thiết kế: `DESIGN.md`. Bối cảnh người dùng, phạm vi 24 màn `M-xx` và ràng buộc bảo mật: `PRODUCT.md`.

## Tiến độ

Mốc: 06/11/2026.

| Hạng mục | Kết quả |
|---|---|
| Màn trong phạm vi v1 | 24 mã `M-xx` đã có giao diện: mức A (8 màn lõi), mức B (khung bảng/biểu mẫu), mức C (khung "chờ chốt") |
| Kiểm tra tự động | `tsc` sạch, 22 test qua, build chạy được (JS 125 KB gzip) |
| Audit giao diện (Taste Skill) | Critical 0, Major 0; Minor còn 8 có lý do. Vòng gần nhất: `after5` (8 màn) và `after6` (Login), ở chế độ tối. Bằng chứng: `_agents_workspace/frontend-audit/` |
| Hiệu năng | Truyền tải 230–280 KiB, LCP dưới 400 ms (CPU), layout shift 0 sau khi đổi `font-display` |
| Nguyên nhân gốc đã xử lý | 219/315 class trong TSX từng không có CSS; đã viết đủ trong `tokens.css`, `app.css`, `utilities.css` |

Báo cáo đầy đủ, từng mục và bằng chứng: `AUDIT.md`.

## Chưa làm hoặc còn mở

1. **Chưa kiểm bằng mắt hoặc Chrome thật:** màn 390 và 768 của cảnh Login mới, hộp xác nhận trả focus (M1, mới có test jsdom), hover, chế độ sáng của toàn bộ 8 màn (audit gần nhất chạy ở chế độ tối).
2. **Chờ Sơn quyết:** trăng thở 9 giây không có nút dừng (WCAG 2.2.2 đòi cách dừng với chuyển động tự chạy quá 5 giây). Đề xuất: thêm nút "Dừng chuyển động" cạnh nút Sáng/Tối.
3. **Lỗi giao diện đã biết, chưa sửa:** danh sách hóa đơn tràn 16 px ở 768 (`resident-invoices`); nút "Tải về" và "Xem gói" lệch hàng lưới ở `authority-reports`.
4. **Cố ý không sửa:** demo nằm trong bundle (cần tách `AppStateContext` và `mockData`; bản build thật phải không bật `VITE_ENABLE_DEMO`), chưa lazy route, `formatAreaM2` còn tách dòng "m ²".
5. **Nội dung:** vài màn mức C còn tham chiếu dạng "Q8", "Q12" trong câu chữ; cần viết lại cùng chủ nghiệp vụ. Chưa có test cho `api/client.ts` (thử lại CSRF).
6. **Ghép API (đợt 06/11/2026):** S04 và S05 đã nối ở mức mã; **chưa chạy thử với backend thật** (mới có `tsc`, 29 test, mock `fetch` và Demo trong Chrome). Giới hạn đã biết khi nối:
   - Backend không có API liệt kê tài khoản, không đổi trạng thái hộ, không sửa mã hộ và mã căn hộ sau khi tạo: ở chế độ API, màn sửa hộ khóa hai mã và ẩn ô trạng thái; form nhân khẩu ẩn ô trạng thái cư trú (luôn "Đang cư trú").
   - Danh sách hộ không có số nhân khẩu (hiện "—" tới khi mở chi tiết hộ).
   - Lịch sử biến động không có "lý do" (API không trả), nên hiện "—".
   - Cư dân chỉ gọi được `/households/me/residents`, nên màn Hộ của tôi không có mã căn hộ và diện tích.
   - "Mở khóa" hiện với mọi tài khoản không phải ADMIN và đang hoạt động; API trả 409 nếu không còn gì để mở.
   Các màn còn lại (tạm vắng, hóa đơn, thu tiền, đóng góp, báo cáo) vẫn dùng dữ liệu mẫu.
7. **Quy trình:** commit trên nhánh riêng từ `origin/feat/frontend`, báo Kiệt về thay đổi thương hiệu, rồi mở PR. Việc này cần Sơn duyệt.
