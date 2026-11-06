# AUDIT.md — frontend BlueMoon (BẢN CUỐI PHA 3, 06/11/2026; có vòng `after3` cho Login)

Công cụ: Taste Skill 2.5.0 (`consistency.mjs --page service`, `perf.mjs`, `shoot.mjs`) trên 8 màn, 4 viewport (1440/1024/768/390), chạy trên bản sao có `?demo=vai_trò`. Bằng chứng: `_agents_workspace/frontend-audit/{before,after,after2,after3,after4,after5,after6}/<màn>/` (ảnh, `consistency.log`, `perf.log`). Bản gốc trước khi sửa: `_agents_workspace/backup/frontend-20261106_184716/`.

## Kết quả tóm tắt
| Mức | Trước | Còn lại |
|---|---|---|
| Critical | 1 | **0** |
| Major | 9 | **0** (2 mục kiểm bằng ảnh/test, không kiểm được bằng trình duyệt thật: xem Giới hạn) |
| Minor | 21 | 8 (có lý do) |

**Nguyên nhân gốc tìm ra khi sửa:** 219 trên 315 class dùng trong TSX không có CSS nào (stylesheet của nhánh `feat/frontend` thiếu khoảng một nửa). Đó là nguồn của C1, M5, M6, M7, M9. Đã viết đủ CSS (`tokens.css`, `app.css`, `utilities.css`); script độ phủ báo 0 class thiếu (trừ 2 class đánh dấu đã có quy tắc).

Số FAIL thô của checker (≈66–116 mỗi màn) gần như không đổi vì phần lớn là luật trang web công ty (xem Miễn trừ). Các FAIL "thật" của vòng đầu (đè chữ, tương phản 1:1, bảng tràn 597px ở 1440, chữ lệch cỡ, khoảng trống 568px ở Login) **không còn** ở `after` và `after2`.

## Đối chiếu từng mục
| # | Mục | Trạng thái | Bằng chứng / lý do |
|---|---|---|---|
| C1 | Link "Bỏ qua menu" không có CSS làm vỡ lưới desktop | **Đã sửa** | Ảnh `after/resident-invoices/desktop-1440.png`: rail trái 264px, nội dung bên phải; `overlap-text` biến mất |
| M1 | Hộp xác nhận không trả focus | **Đã sửa** (jsdom) | Dialog luôn mount; test `confirm-dialog.test.tsx`: bản cũ fail 3/4, bản mới pass 4/4. Chưa thử Chrome thật |
| M2 | Viền ô nhập 2,28:1 | **Đã sửa** | `--border-strong` #767676 (4,5:1) |
| M3 | Nút dưới 44px | **Đã sửa** | `.btn`, `.btn-sm`, `.tab-btn` ≥44px; 48px ≤640px |
| M4 | Nhãn nội bộ lộ cho người dùng | **Đã sửa** | Gắn `SHOW_DEV_TOOLS` (DEV/Demo); xóa emoji và `(Q..)/(PBI-..)/(NFR-..)` khỏi chữ hiển thị. Còn một số tham chiếu dạng "Q8", "Q12" trong câu chữ của vài màn mức C: **chưa sửa** (cần viết lại nội dung với chủ nghiệp vụ) |
| M5 | Thanh điều hướng dưới xếp dọc | **Đã sửa** | `after/resident-invoices/tiles/mobile-sec-01-a.jpg`: 3 tab một hàng, cao 64px |
| M6 | Danh sách tầng dính chữ ở Login | **Đã sửa** | `dl` hai cột; ảnh Login desktop và mobile |
| M7 | Danh sách hóa đơn mobile | **Đã sửa** | Ảnh `after/resident-invoices/mobile-390.png`: thẻ gọn, không còn chấm đầu dòng |
| M8 | Bảng không vừa ở 768/390 | **Đã sửa phần lớn** | Bảng trong khung <860px thành thẻ (`@container`). Còn `invoice-mobile-list` tràn 16px ở 768 (chưa tìm nguyên nhân, nhỏ) |
| M9 | Class kiểu Tailwind không có CSS | **Đã sửa** | `utilities.css` (137 quy tắc, chỉ tên đang dùng, cùng token); 8 file nút chuyển sang `btn btn-primary/secondary`; `button` mặc định không viền |
| m1 | Banner Login hướng dẫn dev cho người dùng | **Đã sửa** | Chữ trung tính khi không ở DEV |
| m2 | Demo và QaDrawer nằm trong bundle | **Không sửa** | Cần tách `AppStateContext`/`mockData` (ngoài phạm vi "không đổi logic/state"); mặc định `mode='api'` nên chưa khai thác được. Cần đảm bảo build thật không bật `VITE_ENABLE_DEMO` |
| m3 | Chưa lazy route | **Không sửa** | `App.tsx` thuộc phần giữ nguyên; LCP ≤360 ms, JS 124 KB gzip |
| m4 | Quá nhiều file font | **Đã sửa phần lớn** | Bỏ Be Vietnam Pro và JetBrains Mono; còn 5–6 file. Đã bỏ trục `opsz` của Newsreader (chỉ `wght@500`) để giảm 458 KiB; chưa đo lại |
| m5 | Reduced-motion tắt hết | **Đã sửa** | Chỉ dừng skeleton và `translateY`; màu vẫn chuyển |
| m6 | Chữ 11px | **Đã sửa** | `text-xs` = 13px |
| m7 | 9 inline style | **Đã sửa** | 0 inline style |
| m8 | z-index, spacing, radius chưa vào token | **Đã sửa** | `--z-*`, `--radius-pill`, thang spacing 4–64 |
| m9 | Class CSS không dùng | **Đã sửa** | Viết lại stylesheet |
| m10 | Một breakpoint | **Đã sửa** | Container query cho bảng + breakpoint 640/900/1100 |
| m11 | Emoji trộn icon | **Đã sửa** | Không còn emoji trong chữ hiển thị |
| m12 | `index.html` thiếu meta | **Đã sửa** | `description`, `theme-color` |
| m13 | Hàng bảng có `onClick` | **Giữ** | Bàn phím đã có `Link` bên trong |
| m14 | DESIGN.md lệch | **Đã sửa** | Viết lại; `PRODUCT.md` cập nhật (S05, D20–D21, S04) |
| m15 | Thiếu test | **Đã sửa một phần** | +7 test (19 tổng): hộp xác nhận, gắn nhãn ô bảng. Chưa có test `api/client.ts` (CSRF retry) |
| m16 | "50,00 m" / "²" tách dòng | **Một phần** | `.tabular` không ngắt dòng; `formatAreaM2` chưa đổi (sợ vỡ test định dạng) |
| m17 | Hover rail | **Đã sửa** | Hover cho mục thương hiệu và mục đang chọn |
| m18 | Thang tiêu đề | **Đã sửa** | `h2` 22px = cỡ số liệu lớn nhất; nhóm rail không còn là `h2` |
| m19 | Orphan một từ | **Đã sửa** | `text-wrap: balance` cho h1/h2 |
| m20 | Login lệch/hole/split | **Đã sửa** | Dựng lại: hero một màn, hai cột cùng hai mép; h1 "Quản lý cư trú và thu phí" |
| m21 | Nút chính dưới màn đầu | **Chấp nhận** | "Thêm hộ", "Tạo tài khoản" nằm trong thanh công cụ của app (WARN `stranded-action`) |

## Hiệu năng (`perf.mjs`, `after2`)
Truyền tải 236–278 KiB, LCP 116–360 ms (CPU), layout shift ≤0,03, 0 ảnh. FAIL `font bytes` 458 KiB (ở `login` và `resident-invoices`) đã hết: sau khi bỏ trục `opsz` của Newsreader (`after3`), Login chỉ còn 105,6 KiB font, 237,6 KiB tổng truyền tải. Còn `broken requests: auth/me` 401 (hành vi mong đợi, miễn trừ). Số file font 5–6 chỉ còn WARN. Các màn app đã đo lại ở `after4`: `font bytes` 90,8–105,6 KiB, PASS.

## Miễn trừ: luật trang web công ty không áp dụng cho khung app
Áp cho 7 màn app. Không miễn trừ cho Login, trừ hai mục cuối.

| Check | Lý do |
|---|---|
| `container-edges`, `section-height`, `one-section`, `one-screen`, `section-air`, `section-gaps`, `one-heading`, `flush`, `hero-screen`, `hero-fill`, `cta`, `fold-action` | App dùng khung rail + vùng làm việc, không có `main > section` một màn mỗi mục; bảng dài hơn một màn |
| `copy-headline`, `copy-fact`, `copy-headings`, `copy-buttons`, `copy-actions` | Tiêu đề màn nội bộ nêu việc, không "bán" sản phẩm |
| `no-visual`, `text-only` | Màn dữ liệu; Login không có ảnh vì thư viện của skill không có chủ đề nhà ở (chỉ café, khách sạn, văn phòng…) |
| `nav`, `pinned`, `off-grid`, `label-centre`, `button-contrast` | Rail cố định là mô hình điều hướng của app; mục đang chọn đã đánh dấu thêm bằng chữ đậm và vạch xanh 3px; checker đếm cả mục nav như nút |
| `button-styles` | Đếm mục nav, nút phụ, nút nguy hiểm (viền đỏ) và nút chính là bốn kiểu; thiết kế chủ ý |
| `stagger` (bảng thành thẻ) | Heuristic đọc độ dài chữ khác nhau giữa các ô; không phải lỗi bố cục |
| `forms`, `assets` / `broken requests` | Màn demo dùng state cục bộ; Login gọi backend thật không có trong bản audit |

## Còn lại sau vòng cuối
- `resident-invoices@768`: danh sách hóa đơn mobile tràn 16px (`row-lines`).
- `authority-reports@1024`: nút "Tải về" chiếm 37% hàng lưới (`row-gap`).
- Mục đang chọn của rail chỉ cách nền 1,16:1 (đã có dấu hiệu thay thế).
- Đã chạy `after3` cho Login: `hero-screen`, `one-section`, `one-heading`, `copy-headline` hết; 5 check còn FAIL: `no-visual` (không có ảnh), `forms` (backend không có trong bản audit), `section-height` (thanh đầu trang không cố định), `container-edges` (nav không dùng class `.wrap` của kit), `split-pair` (hai cột lệch đỉnh 64px).
- **`after4` (06/11/2026, đủ 8 màn, 4 viewport, bản build có `align-items: start`):**
  - Perf: 0 FAIL ở 7 màn app (chỉ WARN: `font files` 5–6 và "host CPU busy" ở `resident-invoices`, `accountant-collections`). `font bytes` 90,8–105,6 KiB ở mọi màn: **đã đo lại, FAIL 458 KiB cũ hết cho cả màn app**. Login còn 1 FAIL `broken requests` = `auth/me` 401 (mong đợi, miễn trừ).
  - Consistency: mọi FAIL ở 7 màn app thuộc nhóm đã miễn trừ ở trên, ngoại trừ hai mục thật, đều đã biết: `row-lines` ở `resident-invoices@768` (nút trong `invoice-mobile-list` kết thúc ở x=751, vượt lề phải 735: tràn 16px) và `row-gap` ở `authority-reports` (nút "Tải về" 37% hàng ở 1024/768, "Xem gói" 58% ở 390). Cả hai **chưa sửa**.
  - Login: `split-pair` **vẫn FAIL**, nhưng lý do đã đổi: lệch đỉnh 64px hết (nhờ `align-items: start`); còn lại cột phải bắt đầu x=841 trong khi đường chia gần nhất là 854 (lệch 13px) và hai cột chạy 80→1360 trong khi container của checker là 0→1440 (cùng gốc với `container-edges`). `section-height` là thanh đầu trang 56–72px đẩy hero lệch khỏi lưới màn. Các FAIL còn lại của Login giữ nguyên như `after3`.
  - `resident-invoice-detail` có thêm `one-heading` (3) và `section-gaps` (4), cùng nhóm miễn trừ khung app.
- **`after5` (06/11/2026, giao diện Blue Moon + sáng/tối + cảnh hồ; Chrome headless của máy chạy ở chế độ TỐI nên đây là bản audit dark):**
  - Perf: `layout shift` FAIL ở `resident-invoices` (0,27) và `authority-reports` (0,49); `admin-accounts` cũng có một lần chạy 0,44 (trung vị 0,01). Đo lại bằng PerformanceObserver trong Chrome: cả trang bị đẩy xuống 38 px rồi kéo lên trong ~120 ms lúc font Newsreader/Hanken đổi từ font dự phòng sang font thật (hàng tiêu đề đổi số dòng). **Không do dark mode**, là lỗi tải font có từ trước, lúc nặng lúc nhẹ theo thời điểm font về. Sửa: `index.html` đổi `display=swap` thành `display=optional`; đo lại ở hai màn: CLS 0. Đánh đổi: lần xem đầu tiên với mạng chậm có thể dùng font dự phòng cho trang đó (lần sau font đã có trong cache).
  - Bắt được hai lỗi thật của chính đợt này, đã sửa: (1) `reduced-motion` Login: quy tắc tắt chuyển động nằm trong `tokens.css`, nạp trước `app.css` nên bị đè; đã chuyển xuống sau các quy tắc cảnh. (2) `fold-action`: ở màn cao 768 nút "Đăng nhập" nằm dưới màn đầu 14 px; trăng đổi từ `52vmin` sang `44vmin`, ở màn 828 px nút nằm trong màn đầu.
  - Còn lại, có chủ ý: `loop-pause` (trăng thở 9 s không có nút dừng; WCAG 2.2.2 đòi cách dừng với chuyển động tự chạy quá 5 s: **cần Sơn quyết** giữ hay thêm nút), `spill` (thẻ dài hơn một màn của cảnh; trang tiếp tục trên màu mặt hồ), `row-lines` của hàng "Tầng" (căn giữa chủ ý), WARN sao 640 px (ô lát, không phải ảnh phóng to). Các FAIL còn lại cùng nhóm miễn trừ ở trên.
  - Chưa sửa từ trước: `row-lines` `resident-invoices@768` (tràn 16 px) và `row-gap` `authority-reports`.
  - Đã chạy lại sau hai sửa trên: xem `after6` bên dưới.
- **`after6` (06/11/2026, chỉ Login, sau khi bỏ dòng "Chung cư … Ngã tư Văn Phú" và đoạn giới thiệu, căn lại logo):** `reduced-motion` và `fold-action` **hết FAIL** (hai sửa của `after5` có hiệu lực). Perf: chỉ còn `auth/me` 401 (mong đợi) và hai WARN cũ. `copy-fact` và `copy-actions` còn lại thuộc nhóm miễn trừ (Login không bán sản phẩm). Còn mở: `loop-pause` (trăng thở không có nút dừng), chờ Sơn quyết.
- Chrome thật (extension Claude in Chrome, `localhost:5173/login`, 06/11/2026): Tab vào "Tên đăng nhập" rồi nút hiện/ẩn mật khẩu đều có vòng focus xanh rõ; console không lỗi. **Chưa kiểm được** 390/768 (`resize_window` không đổi khung nhìn), M1 (hộp xác nhận trả focus), hover. M1 vẫn dựa vào test jsdom.
- Giới hạn của audit này: M1, hover thật, và 768/1024 bằng mắt (chỉ qua checker).

## Việc ngoài giao diện đã làm cùng đợt
`PRODUCT.md` cập nhật (S05 đã có API thật hộ/nhân khẩu giao diện chưa ghép; D20–D21; hợp đồng S04 duyệt nhưng chưa vào `main`); `DESIGN.md` viết lại cho hệ mới ("Quầy lễ tân yên tĩnh"); `.impeccable/surfaces/src-app-tsx.md` thay brief cũ.
