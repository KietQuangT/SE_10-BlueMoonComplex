# Kiểm thử route theo vai trò

Kiểm trên main; phiên đăng nhập phải có `mustChangePassword=false`. Với route API dùng dữ liệu kiểm thử đã có; với AUTHORITY dùng nút tài khoản mẫu trong bản bật công cụ demo. HTTP status của tài liệu SPA không đại diện quyền trang.

### ROUTE-01.ANON — /login (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** URL `/login`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị màn đăng nhập có tiêu đề “Quản lý cư trú và thu phí”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-01.RESIDENT — /login (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** URL `/login`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Phiên đã đăng nhập được chuyển tới `/resident/invoices`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-01.ACCOUNTANT — /login (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** URL `/login`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Phiên đã đăng nhập được chuyển tới `/accountant/collections`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-01.ADMIN — /login (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** URL `/login`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Phiên đã đăng nhập được chuyển tới `/admin/households`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-01.AUTHORITY — /login (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/login`.
- **Dữ liệu vào:** URL `/login`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/login"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Phiên đã đăng nhập được chuyển tới `/authority/reports`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-01, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-02.ANON — /error/:code (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/error/404`.
- **Dữ liệu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-02.RESIDENT — /error/:code (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/error/404`.
- **Dữ liệu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-02.ACCOUNTANT — /error/:code (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/error/404`.
- **Dữ liệu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-02.ADMIN — /error/:code (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/error/404`.
- **Dữ liệu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-02.AUTHORITY — /error/:code (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/error/404`.
- **Dữ liệu vào:** URL `/error/404`; trạng thái route: Trang lỗi chung. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/error/404"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-02, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-03.ANON — * (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/duong-dan-khong-ton-tai`.
- **Dữ liệu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-03.RESIDENT — * (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/duong-dan-khong-ton-tai`.
- **Dữ liệu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-03.ACCOUNTANT — * (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/duong-dan-khong-ton-tai`.
- **Dữ liệu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-03.ADMIN — * (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/duong-dan-khong-ton-tai`.
- **Dữ liệu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-03.AUTHORITY — * (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/duong-dan-khong-ton-tai`.
- **Dữ liệu vào:** URL `/duong-dan-khong-ton-tai`; trạng thái route: Trang lỗi chung. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/duong-dan-khong-ton-tai"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang lỗi với tiêu đề “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-03, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-04.ANON — / (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/`.
- **Dữ liệu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-04.RESIDENT — / (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/`.
- **Dữ liệu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển tới `/resident/invoices`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-04.ACCOUNTANT — / (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/`.
- **Dữ liệu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển tới `/accountant/collections`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-04.ADMIN — / (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/`.
- **Dữ liệu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển tới `/admin/households`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-04.AUTHORITY — / (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/`.
- **Dữ liệu vào:** URL `/`; trạng thái route: Chuyển hướng theo vai trò. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển tới `/authority/reports`.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-04, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-05.ANON — /profile (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/profile`.
- **Dữ liệu vào:** URL `/profile`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-05.RESIDENT — /profile (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/profile`.
- **Dữ liệu vào:** URL `/profile`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-05.ACCOUNTANT — /profile (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/profile`.
- **Dữ liệu vào:** URL `/profile`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-05.ADMIN — /profile (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/profile`.
- **Dữ liệu vào:** URL `/profile`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-05.AUTHORITY — /profile (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/profile`.
- **Dữ liệu vào:** URL `/profile`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/profile"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hồ sơ cá nhân và đổi mật khẩu”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-05, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-06.ANON — /admin/accounts (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/accounts`.
- **Dữ liệu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-06.RESIDENT — /admin/accounts (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/accounts`.
- **Dữ liệu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-06.ACCOUNTANT — /admin/accounts (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/accounts`.
- **Dữ liệu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-06.ADMIN — /admin/accounts (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/accounts`.
- **Dữ liệu vào:** URL `/admin/accounts`; trạng thái route: API một phần.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Danh sách tài khoản”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-06.AUTHORITY — /admin/accounts (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/accounts`.
- **Dữ liệu vào:** URL `/admin/accounts`; trạng thái route: API một phần. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-06, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-07.ANON — /admin/accounts/new (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/accounts/new`.
- **Dữ liệu vào:** URL `/admin/accounts/new`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-07.RESIDENT — /admin/accounts/new (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/accounts/new`.
- **Dữ liệu vào:** URL `/admin/accounts/new`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-07.ACCOUNTANT — /admin/accounts/new (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/accounts/new`.
- **Dữ liệu vào:** URL `/admin/accounts/new`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-07.ADMIN — /admin/accounts/new (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/accounts/new`.
- **Dữ liệu vào:** URL `/admin/accounts/new`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Tạo tài khoản”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-07.AUTHORITY — /admin/accounts/new (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/accounts/new`.
- **Dữ liệu vào:** URL `/admin/accounts/new`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/accounts/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-07, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-08.ANON — /admin/households (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** URL `/admin/households`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-08.RESIDENT — /admin/households (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** URL `/admin/households`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-08.ACCOUNTANT — /admin/households (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** URL `/admin/households`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-08.ADMIN — /admin/households (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** URL `/admin/households`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hộ gia đình”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-08.AUTHORITY — /admin/households (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/households`.
- **Dữ liệu vào:** URL `/admin/households`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-08, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-09.ANON — /admin/households/new (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/households/new`.
- **Dữ liệu vào:** URL `/admin/households/new`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-09.RESIDENT — /admin/households/new (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/households/new`.
- **Dữ liệu vào:** URL `/admin/households/new`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-09.ACCOUNTANT — /admin/households/new (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/households/new`.
- **Dữ liệu vào:** URL `/admin/households/new`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-09.ADMIN — /admin/households/new (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households/new`.
- **Dữ liệu vào:** URL `/admin/households/new`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Thêm hộ gia đình”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-09.AUTHORITY — /admin/households/new (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/households/new`.
- **Dữ liệu vào:** URL `/admin/households/new`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-09, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-10.ANON — /admin/households/:id (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/households/<ID>`.
- **Dữ liệu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-10.RESIDENT — /admin/households/:id (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/households/<ID>`.
- **Dữ liệu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-10.ACCOUNTANT — /admin/households/:id (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/households/<ID>`.
- **Dữ liệu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-10.ADMIN — /admin/households/:id (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households/<ID>`.
- **Dữ liệu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hộ <HOUSEHOLD_CODE> · Căn <APARTMENT_CODE>”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-10.AUTHORITY — /admin/households/:id (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/households/<ID>`.
- **Dữ liệu vào:** URL `/admin/households/<ID>`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-10, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-11.ANON — /admin/households/:id/edit (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/households/<ID>/edit`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-11.RESIDENT — /admin/households/:id/edit (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/households/<ID>/edit`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-11.ACCOUNTANT — /admin/households/:id/edit (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/households/<ID>/edit`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-11.ADMIN — /admin/households/:id/edit (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households/<ID>/edit`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Sửa hộ <HOUSEHOLD_CODE>”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-11.AUTHORITY — /admin/households/:id/edit (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/households/<ID>/edit`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/edit`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/edit"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-11, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-12.ANON — /admin/households/:id/residents/new (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/households/<ID>/residents/new`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-12.RESIDENT — /admin/households/:id/residents/new (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/households/<ID>/residents/new`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-12.ACCOUNTANT — /admin/households/:id/residents/new (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/households/<ID>/residents/new`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-12.ADMIN — /admin/households/:id/residents/new (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/households/<ID>/residents/new`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Thêm nhân khẩu cho hộ <HOUSEHOLD_CODE>”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-12.AUTHORITY — /admin/households/:id/residents/new (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/households/<ID>/residents/new`.
- **Dữ liệu vào:** URL `/admin/households/<ID>/residents/new`; trạng thái route: API. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/households/<ID>/residents/new"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-12, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-13.ANON — /admin/move-outs (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/move-outs`.
- **Dữ liệu vào:** URL `/admin/move-outs`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-13.RESIDENT — /admin/move-outs (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/move-outs`.
- **Dữ liệu vào:** URL `/admin/move-outs`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-13.ACCOUNTANT — /admin/move-outs (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/move-outs`.
- **Dữ liệu vào:** URL `/admin/move-outs`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-13.ADMIN — /admin/move-outs (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/move-outs`.
- **Dữ liệu vào:** URL `/admin/move-outs`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Chuyển đi và lưu trữ”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-13.AUTHORITY — /admin/move-outs (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/move-outs`.
- **Dữ liệu vào:** URL `/admin/move-outs`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/move-outs"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-13, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-14.ANON — /admin/absence-approvals (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/absence-approvals`.
- **Dữ liệu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-14.RESIDENT — /admin/absence-approvals (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/absence-approvals`.
- **Dữ liệu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-14.ACCOUNTANT — /admin/absence-approvals (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/absence-approvals`.
- **Dữ liệu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-14.ADMIN — /admin/absence-approvals (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/absence-approvals`.
- **Dữ liệu vào:** URL `/admin/absence-approvals`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Duyệt khai báo tạm vắng”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-14.AUTHORITY — /admin/absence-approvals (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/absence-approvals`.
- **Dữ liệu vào:** URL `/admin/absence-approvals`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/absence-approvals"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-14, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-15.ANON — /admin/voluntary (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/voluntary`.
- **Dữ liệu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-15.RESIDENT — /admin/voluntary (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/voluntary`.
- **Dữ liệu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-15.ACCOUNTANT — /admin/voluntary (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/voluntary`.
- **Dữ liệu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-15.ADMIN — /admin/voluntary (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/voluntary`.
- **Dữ liệu vào:** URL `/admin/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Quản lý đợt vận động tự nguyện”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-15.AUTHORITY — /admin/voluntary (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/voluntary`.
- **Dữ liệu vào:** URL `/admin/voluntary`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-15, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-16.ANON — /admin/billing-periods (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/billing-periods`.
- **Dữ liệu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-16.RESIDENT — /admin/billing-periods (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/billing-periods`.
- **Dữ liệu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-16.ACCOUNTANT — /admin/billing-periods (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/billing-periods`.
- **Dữ liệu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-16.ADMIN — /admin/billing-periods (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/billing-periods`.
- **Dữ liệu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Tạo kỳ thu / Phát hành hóa đơn”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-16.AUTHORITY — /admin/billing-periods (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/billing-periods`.
- **Dữ liệu vào:** URL `/admin/billing-periods`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/billing-periods"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-16, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-17.ANON — /admin/reports/residency (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/reports/residency`.
- **Dữ liệu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-17.RESIDENT — /admin/reports/residency (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/reports/residency`.
- **Dữ liệu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-17.ACCOUNTANT — /admin/reports/residency (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/reports/residency`.
- **Dữ liệu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-17.ADMIN — /admin/reports/residency (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/reports/residency`.
- **Dữ liệu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Xuất báo cáo lưu trú”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-17.AUTHORITY — /admin/reports/residency (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/reports/residency`.
- **Dữ liệu vào:** URL `/admin/reports/residency`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/residency"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-17, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-18.ANON — /admin/reports/packages (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/admin/reports/packages`.
- **Dữ liệu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-18.RESIDENT — /admin/reports/packages (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/admin/reports/packages`.
- **Dữ liệu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-18.ACCOUNTANT — /admin/reports/packages (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/admin/reports/packages`.
- **Dữ liệu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-18.ADMIN — /admin/reports/packages (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/reports/packages`.
- **Dữ liệu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Đóng gói báo cáo cho cơ quan”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-18.AUTHORITY — /admin/reports/packages (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/admin/reports/packages`.
- **Dữ liệu vào:** URL `/admin/reports/packages`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/admin/reports/packages"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-18, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-19.ANON — /accountant/collections (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/accountant/collections`.
- **Dữ liệu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-19.RESIDENT — /accountant/collections (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/accountant/collections`.
- **Dữ liệu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-19.ACCOUNTANT — /accountant/collections (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/accountant/collections`.
- **Dữ liệu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Thu tiền mặt tại quầy”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-19.ADMIN — /accountant/collections (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/accountant/collections`.
- **Dữ liệu vào:** URL `/accountant/collections`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-19.AUTHORITY — /accountant/collections (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/accountant/collections`.
- **Dữ liệu vào:** URL `/accountant/collections`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collections"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-19, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-20.ANON — /accountant/fee-rates (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/accountant/fee-rates`.
- **Dữ liệu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-20.RESIDENT — /accountant/fee-rates (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/accountant/fee-rates`.
- **Dữ liệu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-20.ACCOUNTANT — /accountant/fee-rates (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/accountant/fee-rates`.
- **Dữ liệu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Đơn giá các loại phí”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-20.ADMIN — /accountant/fee-rates (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/accountant/fee-rates`.
- **Dữ liệu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-20.AUTHORITY — /accountant/fee-rates (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/accountant/fee-rates`.
- **Dữ liệu vào:** URL `/accountant/fee-rates`; trạng thái route: DEMO trên main. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/fee-rates"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-20, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-21.ANON — /accountant/collection-rate (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/accountant/collection-rate`.
- **Dữ liệu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-21.RESIDENT — /accountant/collection-rate (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/accountant/collection-rate`.
- **Dữ liệu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-21.ACCOUNTANT — /accountant/collection-rate (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/accountant/collection-rate`.
- **Dữ liệu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Báo cáo tỷ lệ thu phí”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-21.ADMIN — /accountant/collection-rate (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/accountant/collection-rate`.
- **Dữ liệu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-21.AUTHORITY — /accountant/collection-rate (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/accountant/collection-rate`.
- **Dữ liệu vào:** URL `/accountant/collection-rate`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/accountant/collection-rate"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-21, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-22.ANON — /resident/invoices (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/resident/invoices`.
- **Dữ liệu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-22.RESIDENT — /resident/invoices (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/invoices`.
- **Dữ liệu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hóa đơn và lịch sử đóng phí”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-22.ACCOUNTANT — /resident/invoices (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/resident/invoices`.
- **Dữ liệu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-22.ADMIN — /resident/invoices (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/resident/invoices`.
- **Dữ liệu vào:** URL `/resident/invoices`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-22.AUTHORITY — /resident/invoices (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/resident/invoices`.
- **Dữ liệu vào:** URL `/resident/invoices`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-22, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-23.ANON — /resident/invoices/:id (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/resident/invoices/<ID>`.
- **Dữ liệu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-23.RESIDENT — /resident/invoices/:id (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/invoices/<ID>`.
- **Dữ liệu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hóa đơn kỳ <PERIOD>”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-23.ACCOUNTANT — /resident/invoices/:id (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/resident/invoices/<ID>`.
- **Dữ liệu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-23.ADMIN — /resident/invoices/:id (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/resident/invoices/<ID>`.
- **Dữ liệu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-23.AUTHORITY — /resident/invoices/:id (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/resident/invoices/<ID>`.
- **Dữ liệu vào:** URL `/resident/invoices/<ID>`; trạng thái route: DEMO. `<ID>` là ID hộ ACTIVE đã có; với hóa đơn là ID hóa đơn demo thuộc hộ đang đăng nhập. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/invoices/<ID>"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-23, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-24.ANON — /resident/household (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/resident/household`.
- **Dữ liệu vào:** URL `/resident/household`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-24.RESIDENT — /resident/household (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/household`.
- **Dữ liệu vào:** URL `/resident/household`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Hộ gia đình của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-24.ACCOUNTANT — /resident/household (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/resident/household`.
- **Dữ liệu vào:** URL `/resident/household`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-24.ADMIN — /resident/household (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/resident/household`.
- **Dữ liệu vào:** URL `/resident/household`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-24.AUTHORITY — /resident/household (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/resident/household`.
- **Dữ liệu vào:** URL `/resident/household`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/household"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-24, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-25.ANON — /resident/absence (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** URL `/resident/absence`; trạng thái route: API.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-25.RESIDENT — /resident/absence (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** URL `/resident/absence`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Khai báo tạm vắng”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-25.ACCOUNTANT — /resident/absence (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** URL `/resident/absence`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-25.ADMIN — /resident/absence (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** URL `/resident/absence`; trạng thái route: API.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-25.AUTHORITY — /resident/absence (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** URL `/resident/absence`; trạng thái route: API. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/absence"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-25, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-26.ANON — /resident/voluntary (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/resident/voluntary`.
- **Dữ liệu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-26.RESIDENT — /resident/voluntary (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/voluntary`.
- **Dữ liệu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Đợt đóng góp tự nguyện”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-26.ACCOUNTANT — /resident/voluntary (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/resident/voluntary`.
- **Dữ liệu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-26.ADMIN — /resident/voluntary (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/resident/voluntary`.
- **Dữ liệu vào:** URL `/resident/voluntary`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-26.AUTHORITY — /resident/voluntary (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/resident/voluntary`.
- **Dữ liệu vào:** URL `/resident/voluntary`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/resident/voluntary"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-26, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-27.ANON — /authority/reports (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `/authority/reports`.
- **Dữ liệu vào:** URL `/authority/reports`; trạng thái route: DEMO.
- **Các bước:**
  1. Mở cửa sổ riêng tư chưa đăng nhập.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Chuyển đến `/login`; hiển thị “Đăng nhập”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-27.RESIDENT — /authority/reports (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/authority/reports`.
- **Dữ liệu vào:** URL `/authority/reports`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử RESIDENT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-27.ACCOUNTANT — /authority/reports (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/authority/reports`.
- **Dữ liệu vào:** URL `/authority/reports`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ACCOUNTANT; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-27.ADMIN — /authority/reports (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `/authority/reports`.
- **Dữ liệu vào:** URL `/authority/reports`; trạng thái route: DEMO.
- **Các bước:**
  1. Đăng nhập bằng tài khoản kiểm thử ADMIN; chọn chế độ api.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị trang 403 với tiêu đề “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

### ROUTE-27.AUTHORITY — /authority/reports (AUTHORITY)

- **Vai trò:** AUTHORITY.
- **Điểm vào:** `/authority/reports`.
- **Dữ liệu vào:** URL `/authority/reports`; trạng thái route: DEMO. AUTHORITY chỉ có trong demo, không đăng nhập backend thật.
- **Các bước:**
  1. Ở `/login`, chọn tài khoản mẫu AUTHORITY trong công cụ demo.
  2. Mở DevTools Console và chạy `history.pushState({}, "", "/authority/reports"); window.dispatchEvent(new PopStateEvent("popstate"))` để điều hướng trong ứng dụng.
  3. Đối chiếu nội dung trang và URL với kỳ vọng.
- **Kỳ vọng:** Hiển thị tiêu đề “Báo cáo đã được phê duyệt”.
- **Kiểm sau:** Kiểm tra thanh địa chỉ và tiêu đề sau khi tải xong; không thực hiện ghi dữ liệu.
- **Nguồn:** ROUTE-27, outline mục 2; `frontend/src/App.tsx:104`; `frontend/src/App.tsx:89`; `frontend/src/lib/format.ts:102`.

## Điểm chưa xác định

- HTTP status tài liệu SPA theo cấu hình máy chủ chưa xác định; các ca kiểm nội dung và điều hướng phía trình duyệt.
- Route tham số cần dùng ID có sẵn; dữ liệu hóa đơn DEMO phải thuộc hộ mẫu của phiên RESIDENT.
