# Báo cáo kiểm thử chi tiết: Điều hướng và bảo vệ đường dẫn Frontend theo vai trò

- **Tệp đặc tả nguồn:** [`tests/cases/routes.md`](../cases/routes.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra toàn bộ 27 đường dẫn giao diện (từ `ROUTE-01` đến `ROUTE-27`) nhân với 5 trạng thái vai trò (`ANON`, `RESIDENT`, `ACCOUNTANT`, `ADMIN`, `AUTHORITY`) trên trình duyệt thật (Playwright Chromium) ở chế độ kết nối Backend thật.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **135** |
| Số ca Đạt (Pass) | **134** |
| Số ca Không đạt (Fail) | **1** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **99.26%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- `ROUTE-20.ADMIN`: Tài liệu ca kiểm thử `routes.md` (viết trước khi hợp nhất PR #18) kỳ vọng `ADMIN` vào `/accountant/fee-rates` nhận trang `403`, trong khi mã nguồn `App.tsx:131` đã mở quyền xem chỉ đọc (`"Đơn giá các loại phí"`) cho `ADMIN` theo Quyết định `D25` trong `docs/DECISIONS.md` (ghi nhận tại `BUG-002` để đồng bộ tài liệu kiểm thử).

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `ROUTE-01.ANON` | /login (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-01.RESIDENT` | /login (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-01.ACCOUNTANT` | /login (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-01.ADMIN` | /login (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-01.AUTHORITY` | /login (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-02.ANON` | /error/:code (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-02.RESIDENT` | /error/:code (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-02.ACCOUNTANT` | /error/:code (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-02.ADMIN` | /error/:code (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-02.AUTHORITY` | /error/:code (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-03.ANON` | * (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-03.RESIDENT` | * (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-03.ACCOUNTANT` | * (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-03.ADMIN` | * (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-03.AUTHORITY` | * (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-04.ANON` | / (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-04.RESIDENT` | / (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-04.ACCOUNTANT` | / (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-04.ADMIN` | / (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-04.AUTHORITY` | / (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-05.ANON` | /profile (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-05.RESIDENT` | /profile (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-05.ACCOUNTANT` | /profile (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-05.ADMIN` | /profile (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-05.AUTHORITY` | /profile (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-06.ANON` | /admin/accounts (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-06.RESIDENT` | /admin/accounts (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-06.ACCOUNTANT` | /admin/accounts (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-06.ADMIN` | /admin/accounts (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-06.AUTHORITY` | /admin/accounts (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-07.ANON` | /admin/accounts/new (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-07.RESIDENT` | /admin/accounts/new (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-07.ACCOUNTANT` | /admin/accounts/new (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-07.ADMIN` | /admin/accounts/new (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-07.AUTHORITY` | /admin/accounts/new (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-08.ANON` | /admin/households (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-08.RESIDENT` | /admin/households (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-08.ACCOUNTANT` | /admin/households (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-08.ADMIN` | /admin/households (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-08.AUTHORITY` | /admin/households (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-09.ANON` | /admin/households/new (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-09.RESIDENT` | /admin/households/new (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-09.ACCOUNTANT` | /admin/households/new (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-09.ADMIN` | /admin/households/new (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-09.AUTHORITY` | /admin/households/new (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-10.ANON` | /admin/households/:id (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-10.RESIDENT` | /admin/households/:id (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-10.ACCOUNTANT` | /admin/households/:id (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-10.ADMIN` | /admin/households/:id (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-10.AUTHORITY` | /admin/households/:id (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-11.ANON` | /admin/households/:id/edit (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-11.RESIDENT` | /admin/households/:id/edit (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-11.ACCOUNTANT` | /admin/households/:id/edit (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-11.ADMIN` | /admin/households/:id/edit (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-11.AUTHORITY` | /admin/households/:id/edit (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-12.ANON` | /admin/households/:id/residents/new (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-12.RESIDENT` | /admin/households/:id/residents/new (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-12.ACCOUNTANT` | /admin/households/:id/residents/new (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-12.ADMIN` | /admin/households/:id/residents/new (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-12.AUTHORITY` | /admin/households/:id/residents/new (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-13.ANON` | /admin/move-outs (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-13.RESIDENT` | /admin/move-outs (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-13.ACCOUNTANT` | /admin/move-outs (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-13.ADMIN` | /admin/move-outs (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-13.AUTHORITY` | /admin/move-outs (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-14.ANON` | /admin/absence-approvals (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-14.RESIDENT` | /admin/absence-approvals (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-14.ACCOUNTANT` | /admin/absence-approvals (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-14.ADMIN` | /admin/absence-approvals (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-14.AUTHORITY` | /admin/absence-approvals (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-15.ANON` | /admin/voluntary (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-15.RESIDENT` | /admin/voluntary (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-15.ACCOUNTANT` | /admin/voluntary (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-15.ADMIN` | /admin/voluntary (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-15.AUTHORITY` | /admin/voluntary (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-16.ANON` | /admin/billing-periods (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-16.RESIDENT` | /admin/billing-periods (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-16.ACCOUNTANT` | /admin/billing-periods (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-16.ADMIN` | /admin/billing-periods (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-16.AUTHORITY` | /admin/billing-periods (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-17.ANON` | /admin/reports/residency (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-17.RESIDENT` | /admin/reports/residency (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-17.ACCOUNTANT` | /admin/reports/residency (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-17.ADMIN` | /admin/reports/residency (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-17.AUTHORITY` | /admin/reports/residency (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-18.ANON` | /admin/reports/packages (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-18.RESIDENT` | /admin/reports/packages (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-18.ACCOUNTANT` | /admin/reports/packages (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-18.ADMIN` | /admin/reports/packages (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-18.AUTHORITY` | /admin/reports/packages (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-19.ANON` | /accountant/collections (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-19.RESIDENT` | /accountant/collections (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-19.ACCOUNTANT` | /accountant/collections (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-19.ADMIN` | /accountant/collections (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-19.AUTHORITY` | /accountant/collections (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-20.ANON` | /accountant/fee-rates (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-20.RESIDENT` | /accountant/fee-rates (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-20.ACCOUNTANT` | /accountant/fee-rates (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-20.ADMIN` | /accountant/fee-rates (ADMIN) | ADMIN | ❌ Không đạt (Fail) | [BUG-002](01-bao-cao-loi.md) |
| `ROUTE-20.AUTHORITY` | /accountant/fee-rates (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-21.ANON` | /accountant/collection-rate (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-21.RESIDENT` | /accountant/collection-rate (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-21.ACCOUNTANT` | /accountant/collection-rate (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-21.ADMIN` | /accountant/collection-rate (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-21.AUTHORITY` | /accountant/collection-rate (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-22.ANON` | /resident/invoices (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-22.RESIDENT` | /resident/invoices (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-22.ACCOUNTANT` | /resident/invoices (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-22.ADMIN` | /resident/invoices (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-22.AUTHORITY` | /resident/invoices (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-23.ANON` | /resident/invoices/:id (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-23.RESIDENT` | /resident/invoices/:id (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-23.ACCOUNTANT` | /resident/invoices/:id (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-23.ADMIN` | /resident/invoices/:id (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-23.AUTHORITY` | /resident/invoices/:id (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-24.ANON` | /resident/household (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-24.RESIDENT` | /resident/household (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-24.ACCOUNTANT` | /resident/household (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-24.ADMIN` | /resident/household (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-24.AUTHORITY` | /resident/household (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-25.ANON` | /resident/absence (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-25.RESIDENT` | /resident/absence (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-25.ACCOUNTANT` | /resident/absence (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-25.ADMIN` | /resident/absence (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-25.AUTHORITY` | /resident/absence (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-26.ANON` | /resident/voluntary (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-26.RESIDENT` | /resident/voluntary (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-26.ACCOUNTANT` | /resident/voluntary (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-26.ADMIN` | /resident/voluntary (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-26.AUTHORITY` | /resident/voluntary (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |
| `ROUTE-27.ANON` | /authority/reports (ANON) | ANON | ✅ Đạt (Pass) | — |
| `ROUTE-27.RESIDENT` | /authority/reports (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `ROUTE-27.ACCOUNTANT` | /authority/reports (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `ROUTE-27.ADMIN` | /authority/reports (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `ROUTE-27.AUTHORITY` | /authority/reports (AUTHORITY) | AUTHORITY | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### ROUTE-01.ANON — /login (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** URL `/login`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị màn đăng nhập có tiêu đề “Quản lý cư trú và thu phí”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/login") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-01.RESIDENT — /login (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** URL `/login`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Phiên đã đăng nhập được chuyển tới `/resident/invoices`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/login") -> URL="/resident/invoices", h1="Hóa đơn và lịch sử đóng phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-01.ACCOUNTANT — /login (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** URL `/login`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Phiên đã đăng nhập được chuyển tới `/accountant/collections`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/login") -> URL="/accountant/collections", h1="Thu tiền mặt tại quầy"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-01.ADMIN — /login (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** URL `/login`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Phiên đã đăng nhập được chuyển tới `/admin/households`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/login") -> URL="/admin/households", h1="Hộ gia đình"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-01.AUTHORITY — /login (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** URL `/login`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Phiên đã đăng nhập được chuyển tới `/authority/reports`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/login") -> URL="/authority/reports", h1="Báo cáo đã được phê duyệt"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-02.ANON — /error/:code (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/error/404`.
  - **Dữ liệu đầu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/error/404") -> URL="/error/404", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-02.RESIDENT — /error/:code (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/error/404`.
  - **Dữ liệu đầu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/error/404") -> URL="/error/404", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-02.ACCOUNTANT — /error/:code (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/error/404`.
  - **Dữ liệu đầu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/error/404") -> URL="/error/404", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-02.ADMIN — /error/:code (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/error/404`.
  - **Dữ liệu đầu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/error/404") -> URL="/error/404", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-02.AUTHORITY — /error/:code (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/error/404`.
  - **Dữ liệu đầu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/error/404") -> URL="/error/404", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-03.ANON — * (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/duong-dan-khong-ton-tai`.
  - **Dữ liệu đầu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/duong-dan-khong-ton-tai") -> URL="/duong-dan-khong-ton-tai", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-03.RESIDENT — * (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/duong-dan-khong-ton-tai`.
  - **Dữ liệu đầu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/duong-dan-khong-ton-tai") -> URL="/duong-dan-khong-ton-tai", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-03.ACCOUNTANT — * (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/duong-dan-khong-ton-tai`.
  - **Dữ liệu đầu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/duong-dan-khong-ton-tai") -> URL="/duong-dan-khong-ton-tai", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-03.ADMIN — * (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/duong-dan-khong-ton-tai`.
  - **Dữ liệu đầu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/duong-dan-khong-ton-tai") -> URL="/duong-dan-khong-ton-tai", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-03.AUTHORITY — * (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/duong-dan-khong-ton-tai`.
  - **Dữ liệu đầu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/duong-dan-khong-ton-tai") -> URL="/duong-dan-khong-ton-tai", h1="Không tìm thấy."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-04.ANON — / (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/`.
  - **Dữ liệu đầu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-04.RESIDENT — / (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/`.
  - **Dữ liệu đầu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển tới `/resident/invoices`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/") -> URL="/resident/invoices", h1="Hóa đơn và lịch sử đóng phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-04.ACCOUNTANT — / (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/`.
  - **Dữ liệu đầu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển tới `/accountant/collections`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/") -> URL="/accountant/collections", h1="Thu tiền mặt tại quầy"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-04.ADMIN — / (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/`.
  - **Dữ liệu đầu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển tới `/admin/households`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/") -> URL="/admin/households", h1="Hộ gia đình"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-04.AUTHORITY — / (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/`.
  - **Dữ liệu đầu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển tới `/authority/reports`.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/") -> URL="/authority/reports", h1="Báo cáo đã được phê duyệt"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-05.ANON — /profile (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/profile`.
  - **Dữ liệu đầu vào:** URL `/profile`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/profile") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-05.RESIDENT — /profile (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/profile`.
  - **Dữ liệu đầu vào:** URL `/profile`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/profile") -> URL="/profile", h1="Hồ sơ cá nhân và đổi mật khẩu"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-05.ACCOUNTANT — /profile (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/profile`.
  - **Dữ liệu đầu vào:** URL `/profile`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/profile") -> URL="/profile", h1="Hồ sơ cá nhân và đổi mật khẩu"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-05.ADMIN — /profile (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/profile`.
  - **Dữ liệu đầu vào:** URL `/profile`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/profile") -> URL="/profile", h1="Hồ sơ cá nhân và đổi mật khẩu"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-05.AUTHORITY — /profile (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/profile`.
  - **Dữ liệu đầu vào:** URL `/profile`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/profile") -> URL="/profile", h1="Hồ sơ cá nhân và đổi mật khẩu"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-06.ANON — /admin/accounts (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-06.RESIDENT — /admin/accounts (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts") -> URL="/admin/accounts", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-06.ACCOUNTANT — /admin/accounts (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts") -> URL="/admin/accounts", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-06.ADMIN — /admin/accounts (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Danh sách tài khoản”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts") -> URL="/admin/accounts", h1="Danh sách tài khoản"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-06.AUTHORITY — /admin/accounts (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts`; trạng thái route: API một phần. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts") -> URL="/admin/accounts", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-07.ANON — /admin/accounts/new (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts/new`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts/new") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-07.RESIDENT — /admin/accounts/new (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts/new`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts/new") -> URL="/admin/accounts/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-07.ACCOUNTANT — /admin/accounts/new (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts/new`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts/new") -> URL="/admin/accounts/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-07.ADMIN — /admin/accounts/new (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts/new`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Tạo tài khoản”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts/new") -> URL="/admin/accounts/new", h1="Tạo tài khoản"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-07.AUTHORITY — /admin/accounts/new (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts/new`.
  - **Dữ liệu đầu vào:** URL `/admin/accounts/new`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/accounts/new") -> URL="/admin/accounts/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-08.ANON — /admin/households (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** URL `/admin/households`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-08.RESIDENT — /admin/households (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** URL `/admin/households`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households") -> URL="/admin/households", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-08.ACCOUNTANT — /admin/households (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** URL `/admin/households`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households") -> URL="/admin/households", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-08.ADMIN — /admin/households (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** URL `/admin/households`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hộ gia đình”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households") -> URL="/admin/households", h1="Hộ gia đình"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-08.AUTHORITY — /admin/households (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** URL `/admin/households`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households") -> URL="/admin/households", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-09.ANON — /admin/households/new (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/households/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/new") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-09.RESIDENT — /admin/households/new (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/new") -> URL="/admin/households/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-09.ACCOUNTANT — /admin/households/new (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/new") -> URL="/admin/households/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-09.ADMIN — /admin/households/new (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/new`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Thêm hộ gia đình”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/new") -> URL="/admin/households/new", h1="Thêm hộ gia đình"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-09.AUTHORITY — /admin/households/new (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/households/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/new`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/new") -> URL="/admin/households/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-10.ANON — /admin/households/:id (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-10.RESIDENT — /admin/households/:id (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1") -> URL="/admin/households/1", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-10.ACCOUNTANT — /admin/households/:id (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1") -> URL="/admin/households/1", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-10.ADMIN — /admin/households/:id (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hộ <HOUSEHOLD_CODE> · Căn <APARTMENT_CODE>”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1") -> URL="/admin/households/1", h1="Hộ HH-TEST-01 · Căn APT-TEST-01"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-10.AUTHORITY — /admin/households/:id (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1") -> URL="/admin/households/1", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-11.ANON — /admin/households/:id/edit (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/edit`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/edit") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-11.RESIDENT — /admin/households/:id/edit (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/edit`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/edit") -> URL="/admin/households/1/edit", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-11.ACCOUNTANT — /admin/households/:id/edit (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/edit`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/edit") -> URL="/admin/households/1/edit", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-11.ADMIN — /admin/households/:id/edit (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/edit`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Sửa hộ <HOUSEHOLD_CODE>”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/edit") -> URL="/admin/households/1/edit", h1="Sửa hộ HH-TEST-01"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-11.AUTHORITY — /admin/households/:id/edit (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/edit`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/edit") -> URL="/admin/households/1/edit", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-12.ANON — /admin/households/:id/residents/new (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/residents/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/residents/new") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-12.RESIDENT — /admin/households/:id/residents/new (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/residents/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/residents/new") -> URL="/admin/households/1/residents/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-12.ACCOUNTANT — /admin/households/:id/residents/new (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/residents/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/residents/new") -> URL="/admin/households/1/residents/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-12.ADMIN — /admin/households/:id/residents/new (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/residents/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Thêm nhân khẩu cho hộ <HOUSEHOLD_CODE>”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/residents/new") -> URL="/admin/households/1/residents/new", h1="Thêm nhân khẩu cho hộ HH-TEST-01"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-12.AUTHORITY — /admin/households/:id/residents/new (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/households/<ID>/residents/new`.
  - **Dữ liệu đầu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/households/1/residents/new") -> URL="/admin/households/1/residents/new", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-13.ANON — /admin/move-outs (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/move-outs`.
  - **Dữ liệu đầu vào:** URL `/admin/move-outs`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/move-outs") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-13.RESIDENT — /admin/move-outs (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/move-outs`.
  - **Dữ liệu đầu vào:** URL `/admin/move-outs`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/move-outs") -> URL="/admin/move-outs", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-13.ACCOUNTANT — /admin/move-outs (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/move-outs`.
  - **Dữ liệu đầu vào:** URL `/admin/move-outs`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/move-outs") -> URL="/admin/move-outs", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-13.ADMIN — /admin/move-outs (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/move-outs`.
  - **Dữ liệu đầu vào:** URL `/admin/move-outs`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Chuyển đi và lưu trữ”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/move-outs") -> URL="/admin/move-outs", h1="Chuyển đi và lưu trữ"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-13.AUTHORITY — /admin/move-outs (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/move-outs`.
  - **Dữ liệu đầu vào:** URL `/admin/move-outs`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/move-outs") -> URL="/admin/move-outs", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-14.ANON — /admin/absence-approvals (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/absence-approvals`.
  - **Dữ liệu đầu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/absence-approvals") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-14.RESIDENT — /admin/absence-approvals (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/absence-approvals`.
  - **Dữ liệu đầu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/absence-approvals") -> URL="/admin/absence-approvals", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-14.ACCOUNTANT — /admin/absence-approvals (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/absence-approvals`.
  - **Dữ liệu đầu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/absence-approvals") -> URL="/admin/absence-approvals", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-14.ADMIN — /admin/absence-approvals (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/absence-approvals`.
  - **Dữ liệu đầu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Duyệt khai báo tạm vắng”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/absence-approvals") -> URL="/admin/absence-approvals", h1="Duyệt khai báo tạm vắng"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-14.AUTHORITY — /admin/absence-approvals (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/absence-approvals`.
  - **Dữ liệu đầu vào:** URL `/admin/absence-approvals`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/absence-approvals") -> URL="/admin/absence-approvals", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-15.ANON — /admin/voluntary (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/voluntary`.
  - **Dữ liệu đầu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/voluntary") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-15.RESIDENT — /admin/voluntary (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/voluntary`.
  - **Dữ liệu đầu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/voluntary") -> URL="/admin/voluntary", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-15.ACCOUNTANT — /admin/voluntary (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/voluntary`.
  - **Dữ liệu đầu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/voluntary") -> URL="/admin/voluntary", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-15.ADMIN — /admin/voluntary (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/voluntary`.
  - **Dữ liệu đầu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Quản lý đợt vận động tự nguyện”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/voluntary") -> URL="/admin/voluntary", h1="Quản lý đợt vận động tự nguyện"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-15.AUTHORITY — /admin/voluntary (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/voluntary`.
  - **Dữ liệu đầu vào:** URL `/admin/voluntary`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/voluntary") -> URL="/admin/voluntary", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-16.ANON — /admin/billing-periods (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/billing-periods`.
  - **Dữ liệu đầu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/billing-periods") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-16.RESIDENT — /admin/billing-periods (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/billing-periods`.
  - **Dữ liệu đầu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/billing-periods") -> URL="/admin/billing-periods", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-16.ACCOUNTANT — /admin/billing-periods (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/billing-periods`.
  - **Dữ liệu đầu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/billing-periods") -> URL="/admin/billing-periods", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-16.ADMIN — /admin/billing-periods (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/billing-periods`.
  - **Dữ liệu đầu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Tạo kỳ thu / Phát hành hóa đơn”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/billing-periods") -> URL="/admin/billing-periods", h1="Tạo kỳ thu / Phát hành hóa đơn"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-16.AUTHORITY — /admin/billing-periods (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/billing-periods`.
  - **Dữ liệu đầu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/billing-periods") -> URL="/admin/billing-periods", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-17.ANON — /admin/reports/residency (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/residency`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/residency") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-17.RESIDENT — /admin/reports/residency (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/residency`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/residency") -> URL="/admin/reports/residency", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-17.ACCOUNTANT — /admin/reports/residency (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/residency`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/residency") -> URL="/admin/reports/residency", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-17.ADMIN — /admin/reports/residency (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/residency`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Xuất báo cáo lưu trú”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/residency") -> URL="/admin/reports/residency", h1="Xuất báo cáo lưu trú"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-17.AUTHORITY — /admin/reports/residency (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/residency`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/residency") -> URL="/admin/reports/residency", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-18.ANON — /admin/reports/packages (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/packages`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/packages") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-18.RESIDENT — /admin/reports/packages (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/packages`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/packages") -> URL="/admin/reports/packages", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-18.ACCOUNTANT — /admin/reports/packages (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/packages`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/packages") -> URL="/admin/reports/packages", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-18.ADMIN — /admin/reports/packages (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/packages`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Đóng gói báo cáo cho cơ quan”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/packages") -> URL="/admin/reports/packages", h1="Đóng gói báo cáo cho cơ quan"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-18.AUTHORITY — /admin/reports/packages (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/admin/reports/packages`.
  - **Dữ liệu đầu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/admin/reports/packages") -> URL="/admin/reports/packages", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-19.ANON — /accountant/collections (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections`.
  - **Dữ liệu đầu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collections") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-19.RESIDENT — /accountant/collections (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections`.
  - **Dữ liệu đầu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collections") -> URL="/accountant/collections", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-19.ACCOUNTANT — /accountant/collections (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections`.
  - **Dữ liệu đầu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Thu tiền mặt tại quầy”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collections") -> URL="/accountant/collections", h1="Thu tiền mặt tại quầy"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-19.ADMIN — /accountant/collections (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections`.
  - **Dữ liệu đầu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collections") -> URL="/accountant/collections", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-19.AUTHORITY — /accountant/collections (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections`.
  - **Dữ liệu đầu vào:** URL `/accountant/collections`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collections") -> URL="/accountant/collections", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-20.ANON — /accountant/fee-rates (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/accountant/fee-rates`.
  - **Dữ liệu đầu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/fee-rates") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-20.RESIDENT — /accountant/fee-rates (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/accountant/fee-rates`.
  - **Dữ liệu đầu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/fee-rates") -> URL="/accountant/fee-rates", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-20.ACCOUNTANT — /accountant/fee-rates (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/accountant/fee-rates`.
  - **Dữ liệu đầu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Đơn giá các loại phí”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/fee-rates") -> URL="/accountant/fee-rates", h1="Đơn giá các loại phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-20.ADMIN — /accountant/fee-rates (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/accountant/fee-rates`.
  - **Dữ liệu đầu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **❌ Không đạt (Fail)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/fee-rates") -> URL="/accountant/fee-rates", h1="Đơn giá các loại phí"
- **Chi tiết lỗi / Nhận xét:** **[BUG-002](01-bao-cao-loi.md)** — Xem chi tiết [BUG-002](01-bao-cao-loi.md#bug-002--lech-dac-ta-phan-quyen-duong-dan-accountantfee-rates-voi-vai-tro-admin): Ca kiểm thử `ROUTE-20.ADMIN` trong `tests/cases/routes.md` kỳ vọng trang lỗi `403`, nhưng trên nhánh `main` hiện tại (`frontend/src/App.tsx:131`), đường dẫn `/accountant/fee-rates` cho phép cả `ACCOUNTANT` và `ADMIN` truy cập (chế độ chỉ đọc cho `ADMIN` theo Quyết định `D25` trong `docs/DECISIONS.md` và khớp với `FEE-08.1`/`RBAC-20.1`). Cần cập nhật lại kỳ vọng trong `tests/cases/routes.md`.

### ROUTE-20.AUTHORITY — /accountant/fee-rates (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/accountant/fee-rates`.
  - **Dữ liệu đầu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/fee-rates") -> URL="/accountant/fee-rates", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-21.ANON — /accountant/collection-rate (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/accountant/collection-rate`.
  - **Dữ liệu đầu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collection-rate") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-21.RESIDENT — /accountant/collection-rate (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/accountant/collection-rate`.
  - **Dữ liệu đầu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collection-rate") -> URL="/accountant/collection-rate", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-21.ACCOUNTANT — /accountant/collection-rate (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/accountant/collection-rate`.
  - **Dữ liệu đầu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Báo cáo tỷ lệ thu phí”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collection-rate") -> URL="/accountant/collection-rate", h1="Báo cáo tỷ lệ thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-21.ADMIN — /accountant/collection-rate (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/accountant/collection-rate`.
  - **Dữ liệu đầu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collection-rate") -> URL="/accountant/collection-rate", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-21.AUTHORITY — /accountant/collection-rate (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/accountant/collection-rate`.
  - **Dữ liệu đầu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/accountant/collection-rate") -> URL="/accountant/collection-rate", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-22.ANON — /resident/invoices (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-22.RESIDENT — /resident/invoices (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hóa đơn và lịch sử đóng phí”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices") -> URL="/resident/invoices", h1="Hóa đơn và lịch sử đóng phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-22.ACCOUNTANT — /resident/invoices (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices") -> URL="/resident/invoices", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-22.ADMIN — /resident/invoices (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices") -> URL="/resident/invoices", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-22.AUTHORITY — /resident/invoices (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices") -> URL="/resident/invoices", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-23.ANON — /resident/invoices/:id (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices/<ID>`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices/1") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-23.RESIDENT — /resident/invoices/:id (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices/<ID>`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hóa đơn kỳ <PERIOD>”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices/1") -> URL="/resident/invoices/1", h1="Hóa đơn kỳ 10/2026"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-23.ACCOUNTANT — /resident/invoices/:id (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices/<ID>`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices/1") -> URL="/resident/invoices/1", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-23.ADMIN — /resident/invoices/:id (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices/<ID>`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices/1") -> URL="/resident/invoices/1", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-23.AUTHORITY — /resident/invoices/:id (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices/<ID>`.
  - **Dữ liệu đầu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/invoices/1") -> URL="/resident/invoices/1", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-24.ANON — /resident/household (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/resident/household`.
  - **Dữ liệu đầu vào:** URL `/resident/household`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/household") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-24.RESIDENT — /resident/household (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/household`.
  - **Dữ liệu đầu vào:** URL `/resident/household`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Hộ gia đình của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/household") -> URL="/resident/household", h1="Hộ gia đình của tôi"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-24.ACCOUNTANT — /resident/household (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/resident/household`.
  - **Dữ liệu đầu vào:** URL `/resident/household`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/household") -> URL="/resident/household", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-24.ADMIN — /resident/household (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/resident/household`.
  - **Dữ liệu đầu vào:** URL `/resident/household`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/household") -> URL="/resident/household", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-24.AUTHORITY — /resident/household (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/resident/household`.
  - **Dữ liệu đầu vào:** URL `/resident/household`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/household") -> URL="/resident/household", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-25.ANON — /resident/absence (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** URL `/resident/absence`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/absence") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-25.RESIDENT — /resident/absence (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** URL `/resident/absence`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Khai báo tạm vắng”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/absence") -> URL="/resident/absence", h1="Khai báo tạm vắng"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-25.ACCOUNTANT — /resident/absence (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** URL `/resident/absence`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/absence") -> URL="/resident/absence", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-25.ADMIN — /resident/absence (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** URL `/resident/absence`; trạng thái route: API.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/absence") -> URL="/resident/absence", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-25.AUTHORITY — /resident/absence (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** URL `/resident/absence`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/absence") -> URL="/resident/absence", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-26.ANON — /resident/voluntary (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/resident/voluntary`.
  - **Dữ liệu đầu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/voluntary") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-26.RESIDENT — /resident/voluntary (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/voluntary`.
  - **Dữ liệu đầu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Đợt đóng góp tự nguyện”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/voluntary") -> URL="/resident/voluntary", h1="Đợt đóng góp tự nguyện"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-26.ACCOUNTANT — /resident/voluntary (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/resident/voluntary`.
  - **Dữ liệu đầu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/voluntary") -> URL="/resident/voluntary", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-26.ADMIN — /resident/voluntary (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/resident/voluntary`.
  - **Dữ liệu đầu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/voluntary") -> URL="/resident/voluntary", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-26.AUTHORITY — /resident/voluntary (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/resident/voluntary`.
  - **Dữ liệu đầu vào:** URL `/resident/voluntary`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/resident/voluntary") -> URL="/resident/voluntary", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-27.ANON — /authority/reports (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/authority/reports`.
  - **Dữ liệu đầu vào:** URL `/authority/reports`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Mở cửa sổ riêng tư chưa đăng nhập.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/authority/reports") -> URL="/login", h1="Quản lý cư trú và thu phí"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-27.RESIDENT — /authority/reports (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/authority/reports`.
  - **Dữ liệu đầu vào:** URL `/authority/reports`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/authority/reports") -> URL="/authority/reports", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-27.ACCOUNTANT — /authority/reports (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/authority/reports`.
  - **Dữ liệu đầu vào:** URL `/authority/reports`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/authority/reports") -> URL="/authority/reports", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-27.ADMIN — /authority/reports (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/authority/reports`.
  - **Dữ liệu đầu vào:** URL `/authority/reports`; trạng thái route: DEMO.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/authority/reports") -> URL="/authority/reports", h1="Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ROUTE-27.AUTHORITY — /authority/reports (AUTHORITY)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** AUTHORITY.
  - **Điểm vào (Route / Endpoint):** `/authority/reports`.
  - **Dữ liệu đầu vào:** URL `/authority/reports`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
  - **Các bước thực hiện:**
    1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
    2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
    3. Đối chiếu nội dung trang và URL với kỳ vọng.
  - **Kết quả mong đợi:** Hiển thị tiêu đề “Báo cáo đã được phê duyệt”.
  - **Kiểm tra sau (Post-check):** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
  - **Nguồn đối chiếu:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** pushState("/authority/reports") -> URL="/authority/reports", h1="Báo cáo đã được phê duyệt"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
