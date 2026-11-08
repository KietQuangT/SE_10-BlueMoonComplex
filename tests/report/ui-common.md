# Báo cáo kiểm thử chi tiết: Giao diện chung, chế độ Sáng/Tối, Responsive 360px và xử lý lỗi

- **Tệp đặc tả nguồn:** [`tests/cases/ui-common.md`](../cases/ui-common.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra chuyển đổi chủ đề Sáng/Tối lưu `localStorage`, menu điều hướng theo vai trò, thu hồi phiên tức thời khi đang nhập dở biểu mẫu mà không rò rỉ nháp ra Web Storage, ẩn công cụ Demo trên bản build Production, bố cục đáp ứng (responsive) ở độ rộng 360×800 px không tràn ngang, thông báo lỗi đăng nhập tiếng Việt không lộ mật khẩu, và hiển thị mã `X-Request-Id` khi gặp lỗi máy chủ 5xx.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **12** |
| Số ca Đạt (Pass) | **12** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- `UI-05.1` & `UI-05.2`: Ở kích thước màn hình di động `360×800`, cả trang `/admin/households` và `/resident/invoices` (cùng trang chi tiết hóa đơn) đều có độ tràn ngang `docOverflow = 0px`, các thẻ/nút bấm nằm gọn trong khung nhìn 360 px.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `UI-01.1` | Chuyển từ tự động sang sáng | ADMIN | ✅ Đạt (Pass) | — |
| `UI-01.2` | Chuyển từ sáng sang tối | ADMIN | ✅ Đạt (Pass) | — |
| `UI-02.1` | Menu và trang đầu ADMIN | ADMIN | ✅ Đạt (Pass) | — |
| `UI-02.2` | Menu và trang đầu RESIDENT | RESIDENT | ✅ Đạt (Pass) | — |
| `UI-03.1` | Phiên bị vô hiệu giữa lúc khai báo | RESIDENT | ✅ Đạt (Pass) | — |
| `UI-03.2` | Nháp tạm vắng chỉ còn trong bộ nhớ trang | RESIDENT | ✅ Đạt (Pass) | — |
| `UI-04.1` | Công cụ demo xuất hiện trong bản DEV | ANON | ✅ Đạt (Pass) | — |
| `UI-04.2` | Công cụ demo ẩn trong bản production | ANON | ✅ Đạt (Pass) | — |
| `UI-05.1` | Danh sách hộ ở chiều rộng 360 pixel | ADMIN | ✅ Đạt (Pass) | — |
| `UI-05.2` | Danh sách hóa đơn ở chiều rộng 360 pixel | RESIDENT | ✅ Đạt (Pass) | — |
| `UI-06.1` | Sai thông tin đăng nhập hiển thị tiếng Việt | ANON | ✅ Đạt (Pass) | — |
| `UI-06.2` | Lỗi máy chủ hiển thị request ID | ADMIN | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### UI-01.1 — Chuyển từ tự động sang sáng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** Khóa bm-theme chưa tồn tại.
  - **Các bước thực hiện:**
    1. Mở trang với phiên ADMIN.
    2. Bấm nút “Tự động”.
    3. Tải lại trang.
  - **Kết quả mong đợi:** Nút hiển thị “Sáng”; localStorage `bm-theme` là `light`.
  - **Kiểm tra sau (Post-check):** Kiểm tra `document.documentElement.dataset.theme` là `light`.
  - **Nguồn đối chiếu:** UI-01; `frontend/src/components/ThemeToggle.tsx:4`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Nút hiển thị "Sáng"=true, localStorage bm-theme="light", dataset.theme="light"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-01.2 — Chuyển từ sáng sang tối

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** localStorage `bm-theme=light`.
  - **Các bước thực hiện:**
    1. Mở trang với phiên ADMIN.
    2. Bấm nút “Sáng”.
    3. Tải lại trang.
  - **Kết quả mong đợi:** Nút hiển thị “Tối”; localStorage `bm-theme` là `dark`.
  - **Kiểm tra sau (Post-check):** Kiểm tra `document.documentElement.dataset.theme` là `dark`.
  - **Nguồn đối chiếu:** UI-01; `frontend/src/lib/theme.ts:19`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Nút hiển thị "Tối"=true, localStorage bm-theme="dark", dataset.theme="dark"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-02.1 — Menu và trang đầu ADMIN

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/`.
  - **Dữ liệu đầu vào:** Tài khoản ADMIN ACTIVE, đã đổi mật khẩu.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN tại `/login`.
    2. Mở `/`.
    3. Quan sát thanh menu.
  - **Kết quả mong đợi:** URL `/admin/households`; menu có “Hộ gia đình”, “Duyệt tạm vắng”; không có “Thu tiền mặt”.
  - **Kiểm tra sau (Post-check):** Mở “Hộ gia đình”; tiêu đề “Hộ gia đình”.
  - **Nguồn đối chiếu:** UI-02; `frontend/src/lib/format.ts:102`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** URL="/admin/households", h1="Hộ gia đình", menu có "Hộ gia đình" & "Duyệt tạm vắng", không có "Thu tiền mặt"=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-02.2 — Menu và trang đầu RESIDENT

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/`.
  - **Dữ liệu đầu vào:** Tài khoản RESIDENT ACTIVE, đã đổi mật khẩu.
  - **Các bước thực hiện:**
    1. Đăng nhập RESIDENT tại `/login`.
    2. Mở `/`.
    3. Quan sát thanh menu.
  - **Kết quả mong đợi:** URL `/resident/invoices`; menu có “Hộ gia đình của tôi” và không có “Duyệt tạm vắng”.
  - **Kiểm tra sau (Post-check):** Mở `/admin/households`; trang báo 403.
  - **Nguồn đối chiếu:** UI-02; `frontend/src/App.tsx:115`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** URL="/resident/invoices", menu có "Hộ gia đình của tôi" & không có "Duyệt tạm vắng", mở /admin/households -> "Bạn không có quyền truy cập trang này."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-03.1 — Phiên bị vô hiệu giữa lúc khai báo

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT đang hoạt động; ADMIN thứ hai có quyền vô hiệu tài khoản.
  - **Các bước thực hiện:**
    1. Nhập lý do `Kiem thu tam vang` trên biểu mẫu.
    2. Dùng phiên ADMIN vô hiệu tài khoản RESIDENT.
    3. Bấm gửi biểu mẫu ở phiên RESIDENT.
  - **Kết quả mong đợi:** API bị từ chối phiên; trình duyệt chuyển về `/login`.
  - **Kiểm tra sau (Post-check):** Kiểm localStorage và sessionStorage không chứa `Kiem thu tam vang`; kiểm đơn không được tạo.
  - **Nguồn đối chiếu:** UI-03; `frontend/src/state/AppStateContext.tsx:392`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Sau bấm gửi khi tài khoản bị vô hiệu: URL="/login", rò rỉ "Kiem thu tam vang" trong localStorage/sessionStorage=false, SQL đơn được tạo=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-03.2 — Nháp tạm vắng chỉ còn trong bộ nhớ trang

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT đang hoạt động, hộ có nhân khẩu RESIDING.
  - **Các bước thực hiện:**
    1. Nhập lý do `Nhap thu`.
    2. Dùng phiên ADMIN vô hiệu tài khoản.
    3. Bấm gửi biểu mẫu.
  - **Kết quả mong đợi:** Chuyển về `/login`; nháp không được lưu vào localStorage hoặc sessionStorage.
  - **Kiểm tra sau (Post-check):** Tải lại trang và tìm `Nhap thu` trong hai storage; không có giá trị đó.
  - **Nguồn đối chiếu:** UI-03; `frontend/src/state/AppStateContext.tsx:82`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Sau bấm gửi khi tài khoản bị vô hiệu: URL="/login", rò rỉ "Nhap thu" trong localStorage/sessionStorage=false, SQL đơn được tạo=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-04.1 — Công cụ demo xuất hiện trong bản DEV

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** Bản DEV có `SHOW_DEV_TOOLS=true`.
  - **Các bước thực hiện:**
    1. Mở `/login`.
    2. Tìm bảng công cụ dành cho kiểm thử.
    3. Chọn “Demo (mock-data.json)”.
  - **Kết quả mong đợi:** Có nút “Demo (mock-data.json)” và các lựa chọn tài khoản mẫu.
  - **Kiểm tra sau (Post-check):** Chọn tài khoản mẫu ADMIN; hiển thị “Chế độ Demo”.
  - **Nguồn đối chiếu:** UI-04; `frontend/src/lib/devTools.ts:6`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Bản DEV (:5173): nút "Demo (mock-data.json)"=true, nút mẫu ADMIN=true, sau chọn hiển thị "Chế độ Demo"=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-04.2 — Công cụ demo ẩn trong bản production

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** Bản production có `VITE_ENABLE_DEMO` khác `true`.
  - **Các bước thực hiện:**
    1. Mở `/login`.
    2. Quan sát phần cuối trang.
    3. Tìm “Demo (mock-data.json)”.
  - **Kết quả mong đợi:** Không có công cụ chọn chế độ demo hoặc đăng nhập tài khoản mẫu.
  - **Kiểm tra sau (Post-check):** Kiểm DOM không chứa nút “Demo (mock-data.json)”.
  - **Nguồn đối chiếu:** UI-04; `frontend/src/screens/common/LoginPage.tsx:292`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Bản production (:5174): số nút "Demo (mock-data.json)"=0, số phần tử .dev-panel=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-05.1 — Danh sách hộ ở chiều rộng 360 pixel

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/households`.
  - **Dữ liệu đầu vào:** Chrome viewport 360×800; có ít nhất một hộ.
  - **Các bước thực hiện:**
    1. Bật chế độ thiết bị Chrome và đặt chiều rộng 360 px.
    2. Mở danh sách hộ.
    3. Quan sát dòng dữ liệu và các nút.
  - **Kết quả mong đợi:** Dữ liệu hiển thị dạng thẻ; nút không lệch hàng hoặc bị cắt.
  - **Kiểm tra sau (Post-check):** Cuộn ngang: không cần cuộn ngang để thao tác các nút.
  - **Nguồn đối chiếu:** UI-05; `tests/00-outline/feature-inventory.md:154`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Viewport 360×800 /admin/households: docOverflow=0px, số nút=3, số nút tràn ngang=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-05.2 — Danh sách hóa đơn ở chiều rộng 360 pixel

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/invoices`.
  - **Dữ liệu đầu vào:** Chrome viewport 360×800; hộ demo có hóa đơn.
  - **Các bước thực hiện:**
    1. Đặt viewport Chrome 360×800.
    2. Mở danh sách hóa đơn.
    3. Quan sát dữ liệu và nút xem.
  - **Kết quả mong đợi:** Dữ liệu hiển thị dạng thẻ; nút không lệch hàng hoặc bị cắt.
  - **Kiểm tra sau (Post-check):** Bấm nút xem hóa đơn và kiểm nội dung không bị cắt.
  - **Nguồn đối chiếu:** UI-05; `tests/00-outline/feature-inventory.md:154`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Viewport 360×800 /resident/invoices: số thẻ hóa đơn=2, listOverflow=0px, clippedBtns=0; chi tiết "Hóa đơn kỳ 10/2026" detailOverflow=0px
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-06.1 — Sai thông tin đăng nhập hiển thị tiếng Việt

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/login`.
  - **Dữ liệu đầu vào:** Tên `<RESIDENT_USERNAME>`, mật khẩu `<WRONG_PASSWORD>`.
  - **Các bước thực hiện:**
    1. Mở `/login`.
    2. Nhập tên và mật khẩu sai.
    3. Bấm “Đăng nhập”.
  - **Kết quả mong đợi:** Hiển thị “Tên đăng nhập hoặc mật khẩu không đúng.”.
  - **Kiểm tra sau (Post-check):** Kiểm trường mật khẩu không xuất hiện trong thông báo.
  - **Nguồn đối chiếu:** UI-06; `frontend/src/screens/common/LoginPage.tsx:184`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thông báo lỗi="Tên đăng nhập hoặc mật khẩu không đúng.", chứa mật khẩu sai=false
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### UI-06.2 — Lỗi máy chủ hiển thị request ID

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts/new`.
  - **Dữ liệu đầu vào:** Dùng proxy kiểm thử trả HTTP 500 với `X-Request-Id: test-request-01` cho POST /api/v1/accounts; nhập username `<RESIDENT_USERNAME>`, tên `Tai khoan thu`, email `<TEST_EMAIL>`, vai trò RESIDENT và hộ ACTIVE.
  - **Các bước thực hiện:**
    1. Mở DevTools Network.
    2. Mở `/admin/accounts/new` và nhập dữ liệu kiểm thử.
    3. Bấm tạo tài khoản qua proxy kiểm thử và đọc thông báo lỗi.
  - **Kết quả mong đợi:** Hiển thị “Có lỗi xảy ra, vui lòng thử lại. Mã yêu cầu: test-request-01.”.
  - **Kiểm tra sau (Post-check):** Đối chiếu ID hiển thị với header Network.
  - **Nguồn đối chiếu:** UI-06; `frontend/src/api/errors.ts:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Proxy trả HTTP 500 kèm X-Request-Id="test-request-01"; giao diện hiển thị="Có lỗi xảy ra, vui lòng thử lại. Mã yêu cầu: test-request-01."
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
