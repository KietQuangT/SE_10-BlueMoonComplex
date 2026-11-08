# Ca kiểm thử giao diện chung

### UI-01.1 — Chuyển từ tự động sang sáng

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** Khóa bm-theme chưa tồn tại.
- **Các bước:**
  1. Mở trang với phiên ADMIN.
  2. Bấm nút “Tự động”.
  3. Tải lại trang.
- **Kỳ vọng:** Nút hiển thị “Sáng”; localStorage `bm-theme` là `light`.
- **Kiểm sau:** Kiểm tra `document.documentElement.dataset.theme` là `light`.
- **Nguồn:** UI-01; `frontend/src/components/ThemeToggle.tsx:4`.

### UI-01.2 — Chuyển từ sáng sang tối

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** localStorage `bm-theme=light`.
- **Các bước:**
  1. Mở trang với phiên ADMIN.
  2. Bấm nút “Sáng”.
  3. Tải lại trang.
- **Kỳ vọng:** Nút hiển thị “Tối”; localStorage `bm-theme` là `dark`.
- **Kiểm sau:** Kiểm tra `document.documentElement.dataset.theme` là `dark`.
- **Nguồn:** UI-01; `frontend/src/lib/theme.ts:19`.

### UI-02.1 — Menu và trang đầu ADMIN

- **Vai trò:** ADMIN.
- **Điểm vào:** `/`.
- **Dữ liệu vào:** Tài khoản ADMIN ACTIVE, đã đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập ADMIN tại `/login`.
  2. Mở `/`.
  3. Quan sát thanh menu.
- **Kỳ vọng:** URL `/admin/households`; menu có “Hộ gia đình”, “Duyệt tạm vắng”; không có “Thu tiền mặt”.
- **Kiểm sau:** Mở “Hộ gia đình”; tiêu đề “Hộ gia đình”.
- **Nguồn:** UI-02; `frontend/src/lib/format.ts:102`.

### UI-02.2 — Menu và trang đầu RESIDENT

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/`.
- **Dữ liệu vào:** Tài khoản RESIDENT ACTIVE, đã đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập RESIDENT tại `/login`.
  2. Mở `/`.
  3. Quan sát thanh menu.
- **Kỳ vọng:** URL `/resident/invoices`; menu có “Hộ gia đình của tôi” và không có “Duyệt tạm vắng”.
- **Kiểm sau:** Mở `/admin/households`; trang báo 403.
- **Nguồn:** UI-02; `frontend/src/App.tsx:115`.

### UI-03.1 — Phiên bị vô hiệu giữa lúc khai báo

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** Phiên RESIDENT đang hoạt động; ADMIN thứ hai có quyền vô hiệu tài khoản.
- **Các bước:**
  1. Nhập lý do `Kiem thu tam vang` trên biểu mẫu.
  2. Dùng phiên ADMIN vô hiệu tài khoản RESIDENT.
  3. Bấm gửi biểu mẫu ở phiên RESIDENT.
- **Kỳ vọng:** API bị từ chối phiên; trình duyệt chuyển về `/login`.
- **Kiểm sau:** Kiểm localStorage và sessionStorage không chứa `Kiem thu tam vang`; kiểm đơn không được tạo.
- **Nguồn:** UI-03; `frontend/src/state/AppStateContext.tsx:392`.

### UI-03.2 — Nháp tạm vắng chỉ còn trong bộ nhớ trang

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** Phiên RESIDENT đang hoạt động, hộ có nhân khẩu RESIDING.
- **Các bước:**
  1. Nhập lý do `Nhap thu`.
  2. Dùng phiên ADMIN vô hiệu tài khoản.
  3. Bấm gửi biểu mẫu.
- **Kỳ vọng:** Chuyển về `/login`; nháp không được lưu vào localStorage hoặc sessionStorage.
- **Kiểm sau:** Tải lại trang và tìm `Nhap thu` trong hai storage; không có giá trị đó.
- **Nguồn:** UI-03; `frontend/src/state/AppStateContext.tsx:82`.

### UI-04.1 — Công cụ demo xuất hiện trong bản DEV

- **Vai trò:** ANON.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** Bản DEV có `SHOW_DEV_TOOLS=true`.
- **Các bước:**
  1. Mở `/login`.
  2. Tìm bảng công cụ dành cho kiểm thử.
  3. Chọn “Demo (mock-data.json)”.
- **Kỳ vọng:** Có nút “Demo (mock-data.json)” và các lựa chọn tài khoản mẫu.
- **Kiểm sau:** Chọn tài khoản mẫu ADMIN; hiển thị “Chế độ Demo”.
- **Nguồn:** UI-04; `frontend/src/lib/devTools.ts:6`.

### UI-04.2 — Công cụ demo ẩn trong bản production

- **Vai trò:** ANON.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** Bản production có `VITE_ENABLE_DEMO` khác `true`.
- **Các bước:**
  1. Mở `/login`.
  2. Quan sát phần cuối trang.
  3. Tìm “Demo (mock-data.json)”.
- **Kỳ vọng:** Không có công cụ chọn chế độ demo hoặc đăng nhập tài khoản mẫu.
- **Kiểm sau:** Kiểm DOM không chứa nút “Demo (mock-data.json)”.
- **Nguồn:** UI-04; `frontend/src/screens/common/LoginPage.tsx:292`.

### UI-05.1 — Danh sách hộ ở chiều rộng 360 pixel

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** Chrome viewport 360×800; có ít nhất một hộ.
- **Các bước:**
  1. Bật chế độ thiết bị Chrome và đặt chiều rộng 360 px.
  2. Mở danh sách hộ.
  3. Quan sát dòng dữ liệu và các nút.
- **Kỳ vọng:** Dữ liệu hiển thị dạng thẻ; nút không lệch hàng hoặc bị cắt.
- **Kiểm sau:** Cuộn ngang: không cần cuộn ngang để thao tác các nút.
- **Nguồn:** UI-05; `tests/00-outline/feature-inventory.md:154`.

### UI-05.2 — Danh sách hóa đơn ở chiều rộng 360 pixel

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/invoices`.
- **Dữ liệu vào:** Chrome viewport 360×800; hộ demo có hóa đơn.
- **Các bước:**
  1. Đặt viewport Chrome 360×800.
  2. Mở danh sách hóa đơn.
  3. Quan sát dữ liệu và nút xem.
- **Kỳ vọng:** Dữ liệu hiển thị dạng thẻ; nút không lệch hàng hoặc bị cắt.
- **Kiểm sau:** Bấm nút xem hóa đơn và kiểm nội dung không bị cắt.
- **Nguồn:** UI-05; `tests/00-outline/feature-inventory.md:154`.

### UI-06.1 — Sai thông tin đăng nhập hiển thị tiếng Việt

- **Vai trò:** ANON.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** Tên `<RESIDENT_USERNAME>`, mật khẩu `<WRONG_PASSWORD>`.
- **Các bước:**
  1. Mở `/login`.
  2. Nhập tên và mật khẩu sai.
  3. Bấm “Đăng nhập”.
- **Kỳ vọng:** Hiển thị “Tên đăng nhập hoặc mật khẩu không đúng.”.
- **Kiểm sau:** Kiểm trường mật khẩu không xuất hiện trong thông báo.
- **Nguồn:** UI-06; `frontend/src/screens/common/LoginPage.tsx:184`.

### UI-06.2 — Lỗi máy chủ hiển thị request ID

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/accounts/new`.
- **Dữ liệu vào:** Dùng proxy kiểm thử trả HTTP 500 với `X-Request-Id: test-request-01` cho POST /api/v1/accounts; nhập username `<RESIDENT_USERNAME>`, tên `Tai khoan thu`, email `<TEST_EMAIL>`, vai trò RESIDENT và hộ ACTIVE.
- **Các bước:**
  1. Mở DevTools Network.
  2. Mở `/admin/accounts/new` và nhập dữ liệu kiểm thử.
  3. Bấm tạo tài khoản qua proxy kiểm thử và đọc thông báo lỗi.
- **Kỳ vọng:** Hiển thị “Có lỗi xảy ra, vui lòng thử lại. Mã yêu cầu: test-request-01.”.
- **Kiểm sau:** Đối chiếu ID hiển thị với header Network.
- **Nguồn:** UI-06; `frontend/src/api/errors.ts:38`.

## Điểm chưa xác định

- Kích thước nút chính xác chưa xác định; kiểm bằng quan sát tại 360 px. Lỗi 5xx cần proxy kiểm thử; UI-03 phải kiểm nháp trong bộ nhớ và storage riêng.
