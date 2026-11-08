# Ca kiểm thử accounts

### ACC-01.1 — Tạo cư dân cho hộ đang hoạt động

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Hộ `<HOUSEHOLD_ID>` ACTIVE; JSON `{"username":"resident.test","fullName":"Test Resident","email":"<TEST_EMAIL>","role":"RESIDENT","householdId":<HOUSEHOLD_ID>}`; `<TEST_EMAIL>` là địa chỉ dành riêng cho thử nghiệm.
- **Các bước:**
  1. Lấy CSRF tại `GET /api/v1/auth/csrf` trong phiên ADMIN.
  2. Gửi JSON tới `POST /api/v1/accounts` với `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  3. Lưu `id` từ response.
- **Kỳ vọng:** HTTP 201; có `id`, `username`, `role=RESIDENT`, `householdId`, `emailStatus=SIMULATED`; không có mật khẩu tạm.
- **Kiểm sau:** Đọc tài khoản theo `id` bằng SQL: `must_change_password=true`; có audit `ACCOUNT_CREATED`.
- **Nguồn:** ACC-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.

### ACC-01.2 — Từ chối email đã dùng sau chuẩn hóa

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Có tài khoản với `<TEST_EMAIL>`; JSON `{"username":"other.test","fullName":"Other Test","email":"<TEST_EMAIL_KHAC_CHU_HOA>","role":"ACCOUNTANT"}`; hai email chỉ khác chữ hoa/thường.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `POST /api/v1/accounts` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 409; `code=EMAIL_ALREADY_USED`.
- **Kiểm sau:** SQL: không có tài khoản `other.test` mới và không có email mô phỏng cho tài khoản này.
- **Nguồn:** ACC-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.

### ACC-01.3 — Từ chối cư dân khi thiếu hộ

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** JSON `{"username":"resident.nohouse","fullName":"Test Resident","email":"<UNUSED_TEST_EMAIL>","role":"RESIDENT"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `POST /api/v1/accounts` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 400; `code=HOUSEHOLD_REQUIRED`.
- **Kiểm sau:** SQL: không có tài khoản `resident.nohouse`.
- **Nguồn:** ACC-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.

### ACC-02.1 — Thư mô phỏng được ghi khi tạo tài khoản

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** JSON `{"username":"mail.test","fullName":"Mail Test","email":"<UNUSED_TEST_EMAIL>","role":"ACCOUNTANT"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `POST /api/v1/accounts` với token.
  3. Lưu `id` response.
- **Kỳ vọng:** HTTP 201; response không chứa mật khẩu tạm.
- **Kiểm sau:** SQL đọc `mock_emails` theo user ID: có thư chứa tên đăng nhập và mật khẩu tạm; không sao chép mật khẩu vào bằng chứng.
- **Nguồn:** ACC-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.

### ACC-02.2 — Không tạo thư khi email bị trùng

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Đã có `<TEST_EMAIL>`; JSON `{"username":"mail.duplicate","fullName":"Mail Test","email":"<TEST_EMAIL>","role":"ACCOUNTANT"}`.
- **Các bước:**
  1. Ghi số thư mô phỏng hiện có cho `<TEST_EMAIL>`.
  2. Lấy CSRF trong phiên ADMIN.
  3. Gửi JSON tạo tài khoản với token.
- **Kỳ vọng:** HTTP 409; `code=EMAIL_ALREADY_USED`.
- **Kiểm sau:** SQL: số thư cho `<TEST_EMAIL>` không tăng.
- **Nguồn:** ACC-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.

### ACC-03.1 — Vô hiệu tài khoản đang có phiên

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/{id}/disable`.
- **Dữ liệu vào:** `<TARGET_ID>` là RESIDENT ACTIVE khác ADMIN; có phiên RESIDENT riêng và CSRF của ADMIN.
- **Các bước:**
  1. Gửi `POST /api/v1/accounts/<TARGET_ID>/disable` với token ADMIN.
  2. Gọi `GET /api/v1/auth/me` bằng phiên RESIDENT cũ.
  3. Đọc audit của tài khoản đích.
- **Kỳ vọng:** HTTP 204 khi vô hiệu; phiên cũ nhận HTTP 401.
- **Kiểm sau:** SQL: tài khoản INACTIVE; có audit `ACCOUNT_DISABLED` và `SESSIONS_REVOKED`.
- **Nguồn:** ACC-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.

### ACC-03.2 — Kích hoạt tài khoản đã vô hiệu

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/{id}/enable`.
- **Dữ liệu vào:** `<TARGET_ID>` là tài khoản INACTIVE; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi `POST /api/v1/accounts/<TARGET_ID>/enable`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** SQL: tài khoản ACTIVE; có audit `ACCOUNT_ENABLED`.
- **Nguồn:** ACC-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.

### ACC-03.3 — Không cho ADMIN tự vô hiệu

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/{id}/disable`.
- **Dữ liệu vào:** `<ADMIN_ID>` là ID của phiên hiện tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi `POST /api/v1/accounts/<ADMIN_ID>/disable`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 409; `code=ACCOUNT_DISABLE_CONFLICT`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me`: phiên ADMIN vẫn hoạt động.
- **Nguồn:** ACC-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.

### ACC-04.1 — Danh sách hiển thị tài khoản vừa tạo

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/accounts`.
- **Dữ liệu vào:** Tài khoản thử nghiệm `list.test` vừa tạo thành công trong phiên giao diện hiện tại.
- **Các bước:**
  1. Mở `/admin/accounts/new`.
  2. Tạo ACCOUNTANT `list.test` với `<UNUSED_TEST_EMAIL>`.
  3. Mở `/admin/accounts`.
- **Kỳ vọng:** Danh sách hiển thị tài khoản `list.test`.
- **Kiểm sau:** Ghi nhận dòng `list.test` trong danh sách hiện tại.
- **Nguồn:** ACC-04; `frontend/src/state/AppStateContext.tsx:314`.

### ACC-04.2 — Danh sách không tải lại tài khoản từ backend

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/accounts`.
- **Dữ liệu vào:** Đã tạo `list.test` trong phiên giao diện; backend không có API liệt kê tài khoản.
- **Các bước:**
  1. Mở `/admin/accounts` và xác nhận `list.test`.
  2. Tải lại trang trình duyệt.
  3. Kiểm tra danh sách.
- **Kỳ vọng:** Sau tải lại, danh sách không còn tài khoản được giữ trong bộ nhớ phiên giao diện; nội dung thông báo chính xác chưa xác định.
- **Kiểm sau:** SQL xác nhận tài khoản `list.test` vẫn tồn tại; xem mục Điểm chưa xác định.
- **Nguồn:** ACC-04; `frontend/src/state/AppStateContext.tsx:462`.

## Điểm chưa xác định

ACC-04.2: thông báo danh sách sau tải lại chưa xác định.
