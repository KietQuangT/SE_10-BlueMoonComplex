# Ma trận kiểm thử phân quyền backend

Mỗi ca dùng dữ liệu kiểm thử độc lập; tạo trước các bản ghi được nêu trong dữ liệu vào bằng môi trường test, không dùng dữ liệu thật. Mọi tài khoản đăng nhập có `must_change_password=false`, trừ ca kiểm riêng; ID 101, 201, 301, 401, 501 là ID fixture cụ thể cần tạo đúng trước từng ca. Ngày 2099 giả định là tương lai tại thời điểm chạy. Dùng HTTPS hoặc môi trường local được kiểm soát; không lưu giá trị mật khẩu, token, cookie thật trong kết quả.

### RBAC-1.1-health.ANON — GET /actuator/health (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /actuator/health` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra `status` là `UP`.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-1.1-health.RESIDENT — GET /actuator/health (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /actuator/health` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra `status` là `UP`.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-1.1-health.ACCOUNTANT — GET /actuator/health (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /actuator/health` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra `status` là `UP`.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-1.1-health.ADMIN — GET /actuator/health (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /actuator/health` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra `status` là `UP`.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-1.2-health-child.ANON — GET /actuator/health/** (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /actuator/health/db`.
- **Dữ liệu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
- **Kiểm sau:** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-1.2-health-child.RESIDENT — GET /actuator/health/** (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /actuator/health/db`.
- **Dữ liệu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
- **Kiểm sau:** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-1.2-health-child.ACCOUNTANT — GET /actuator/health/** (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /actuator/health/db`.
- **Dữ liệu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
- **Kiểm sau:** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-1.2-health-child.ADMIN — GET /actuator/health/** (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /actuator/health/db`.
- **Dữ liệu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
- **Kiểm sau:** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
- **Nguồn:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-2.1-info.ANON — GET /api/v1/system/info (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra response là metadata hệ thống.
- **Nguồn:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-2.1-info.RESIDENT — GET /api/v1/system/info (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra response là metadata hệ thống.
- **Nguồn:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-2.1-info.ACCOUNTANT — GET /api/v1/system/info (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra response là metadata hệ thống.
- **Nguồn:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-2.1-info.ADMIN — GET /api/v1/system/info (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra response là metadata hệ thống.
- **Nguồn:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-3.1-csrf.ANON — GET /api/v1/auth/csrf (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có `headerName` và `token`.
- **Nguồn:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-3.1-csrf.RESIDENT — GET /api/v1/auth/csrf (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có `headerName` và `token`.
- **Nguồn:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-3.1-csrf.ACCOUNTANT — GET /api/v1/auth/csrf (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có `headerName` và `token`.
- **Nguồn:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-3.1-csrf.ADMIN — GET /api/v1/auth/csrf (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có `headerName` và `token`.
- **Nguồn:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-4.1-login.ANON — POST /api/v1/auth/login (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
- **Nguồn:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-4.1-login.RESIDENT — POST /api/v1/auth/login (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
- **Nguồn:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-4.1-login.ACCOUNTANT — POST /api/v1/auth/login (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
- **Nguồn:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-4.1-login.ADMIN — POST /api/v1/auth/login (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
- **Nguồn:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.1-me.ANON — GET /api/v1/auth/me (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.1-me.RESIDENT — GET /api/v1/auth/me (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra `role` trong JSON trùng vai trò đang thử.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.1-me.ACCOUNTANT — GET /api/v1/auth/me (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra `role` trong JSON trùng vai trò đang thử.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.1-me.ADMIN — GET /api/v1/auth/me (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra `role` trong JSON trùng vai trò đang thử.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.2-logout.ANON — POST /api/v1/auth/logout (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/logout`.
- **Dữ liệu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.2-logout.RESIDENT — POST /api/v1/auth/logout (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/logout`.
- **Dữ liệu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.2-logout.ACCOUNTANT — POST /api/v1/auth/logout (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/auth/logout`.
- **Dữ liệu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-5.2-logout.ADMIN — POST /api/v1/auth/logout (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/auth/logout`.
- **Dữ liệu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
- **Nguồn:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-6.1-change-password.ANON — POST /api/v1/auth/change-password (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-6.1-change-password.RESIDENT — POST /api/v1/auth/change-password (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
- **Nguồn:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-6.1-change-password.ACCOUNTANT — POST /api/v1/auth/change-password (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
- **Nguồn:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-6.1-change-password.ADMIN — POST /api/v1/auth/change-password (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
- **Nguồn:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-7.1-create-account.ANON — POST /api/v1/accounts (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"rbactest01@example.invalid","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-7.1-create-account.RESIDENT — POST /api/v1/accounts (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"rbactest01@example.invalid","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-7.1-create-account.ACCOUNTANT — POST /api/v1/accounts (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"rbactest01@example.invalid","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-7.1-create-account.ADMIN — POST /api/v1/accounts (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"rbactest01@example.invalid","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối.
- **Nguồn:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.1-disable.ANON — POST /api/v1/accounts/101/disable (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts/101/disable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.1-disable.RESIDENT — POST /api/v1/accounts/101/disable (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/accounts/101/disable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.1-disable.ACCOUNTANT — POST /api/v1/accounts/101/disable (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/accounts/101/disable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.1-disable.ADMIN — POST /api/v1/accounts/101/disable (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/101/disable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.2-enable.ANON — POST /api/v1/accounts/101/enable (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts/101/enable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.2-enable.RESIDENT — POST /api/v1/accounts/101/enable (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/accounts/101/enable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.2-enable.ACCOUNTANT — POST /api/v1/accounts/101/enable (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/accounts/101/enable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.2-enable.ADMIN — POST /api/v1/accounts/101/enable (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/101/enable`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.3-unlock.ANON — POST /api/v1/accounts/101/unlock (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts/101/unlock`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.3-unlock.RESIDENT — POST /api/v1/accounts/101/unlock (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/accounts/101/unlock`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.3-unlock.ACCOUNTANT — POST /api/v1/accounts/101/unlock (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/accounts/101/unlock`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-8.3-unlock.ADMIN — POST /api/v1/accounts/101/unlock (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/101/unlock`.
- **Dữ liệu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối.
- **Nguồn:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.1-households.ANON — GET /api/v1/households (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.1-households.RESIDENT — GET /api/v1/households (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.1-households.ACCOUNTANT — GET /api/v1/households (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.1-households.ADMIN — GET /api/v1/households (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON trang danh sách hộ.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.2-household.ANON — GET /api/v1/households/201 (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/201`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.2-household.RESIDENT — GET /api/v1/households/201 (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/201`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.2-household.ACCOUNTANT — GET /api/v1/households/201 (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households/201`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-9.2-household.ADMIN — GET /api/v1/households/201 (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/201`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON trả hộ 201 khi được phép.
- **Nguồn:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.1-create-household.ANON — POST /api/v1/households (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.1-create-household.RESIDENT — POST /api/v1/households (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.1-create-household.ACCOUNTANT — POST /api/v1/households (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.1-create-household.ADMIN — POST /api/v1/households (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.2-patch-household.ANON — PATCH /api/v1/households/201 (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `PATCH /api/v1/households/201`.
- **Dữ liệu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.2-patch-household.RESIDENT — PATCH /api/v1/households/201 (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `PATCH /api/v1/households/201`.
- **Dữ liệu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.2-patch-household.ACCOUNTANT — PATCH /api/v1/households/201 (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/households/201`.
- **Dữ liệu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-10.2-patch-household.ADMIN — PATCH /api/v1/households/201 (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `PATCH /api/v1/households/201`.
- **Dữ liệu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối.
- **Nguồn:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-11.1-household-residents.ANON — GET /api/v1/households/201/residents (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/201/residents`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-11.1-household-residents.RESIDENT — GET /api/v1/households/201/residents (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/201/residents`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-11.1-household-residents.ACCOUNTANT — GET /api/v1/households/201/residents (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households/201/residents`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-11.1-household-residents.ADMIN — GET /api/v1/households/201/residents (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/201/residents`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON danh sách nhân khẩu hộ 201 khi được phép.
- **Nguồn:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-12.1-create-resident.ANON — POST /api/v1/households/201/residents (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/households/201/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-12.1-create-resident.RESIDENT — POST /api/v1/households/201/residents (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/households/201/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-12.1-create-resident.ACCOUNTANT — POST /api/v1/households/201/residents (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/households/201/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-12.1-create-resident.ADMIN — POST /api/v1/households/201/residents (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households/201/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép.
- **Nguồn:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-13.1-history.ANON — GET /api/v1/households/201/residency-changes (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/201/residency-changes`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-13.1-history.RESIDENT — GET /api/v1/households/201/residency-changes (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/201/residency-changes`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-13.1-history.ACCOUNTANT — GET /api/v1/households/201/residency-changes (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households/201/residency-changes`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-13.1-history.ADMIN — GET /api/v1/households/201/residency-changes (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/201/residency-changes`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON trang lịch sử biến động của hộ 201 khi được phép.
- **Nguồn:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-14.1-my-residents.ANON — GET /api/v1/households/me/residents (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-14.1-my-residents.RESIDENT — GET /api/v1/households/me/residents (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra danh sách chỉ chứa nhân khẩu hộ 201 và không có số giấy tờ, điện thoại.
- **Nguồn:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-14.1-my-residents.ACCOUNTANT — GET /api/v1/households/me/residents (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-14.1-my-residents.ADMIN — GET /api/v1/households/me/residents (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.1-resident.ANON — GET /api/v1/residents/301 (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residents/301`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 tồn tại.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.1-resident.RESIDENT — GET /api/v1/residents/301 (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residents/301`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.1-resident.ACCOUNTANT — GET /api/v1/residents/301 (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residents/301`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.1-resident.ADMIN — GET /api/v1/residents/301 (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/301`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 tồn tại.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON trả nhân khẩu 301 khi được phép.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.2-patch-resident.ANON — PATCH /api/v1/residents/301 (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `PATCH /api/v1/residents/301`.
- **Dữ liệu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.2-patch-resident.RESIDENT — PATCH /api/v1/residents/301 (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `PATCH /api/v1/residents/301`.
- **Dữ liệu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.2-patch-resident.ACCOUNTANT — PATCH /api/v1/residents/301 (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/residents/301`.
- **Dữ liệu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-15.2-patch-resident.ADMIN — PATCH /api/v1/residents/301 (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `PATCH /api/v1/residents/301`.
- **Dữ liệu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối.
- **Nguồn:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-16.1-move-out.ANON — POST /api/v1/residents/301/move-out (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residents/301/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-16.1-move-out.RESIDENT — POST /api/v1/residents/301/move-out (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residents/301/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-16.1-move-out.ACCOUNTANT — POST /api/v1/residents/301/move-out (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residents/301/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-16.1-move-out.ADMIN — POST /api/v1/residents/301/move-out (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/301/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối.
- **Nguồn:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-17.1-return.ANON — POST /api/v1/residents/301/return (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residents/301/return`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-17.1-return.RESIDENT — POST /api/v1/residents/301/return (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residents/301/return`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-17.1-return.ACCOUNTANT — POST /api/v1/residents/301/return (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residents/301/return`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-17.1-return.ADMIN — POST /api/v1/residents/301/return (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/301/return`.
- **Dữ liệu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối.
- **Nguồn:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-18.1-suggest.ANON — GET /api/v1/residents/suggest?q=An (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=An`.
- **Dữ liệu vào:** Không có body; có nhân khẩu tên chứa `An`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-18.1-suggest.RESIDENT — GET /api/v1/residents/suggest?q=An (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=An`.
- **Dữ liệu vào:** Không có body; có nhân khẩu tên chứa `An`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-18.1-suggest.ACCOUNTANT — GET /api/v1/residents/suggest?q=An (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=An`.
- **Dữ liệu vào:** Không có body; có nhân khẩu tên chứa `An`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-18.1-suggest.ADMIN — GET /api/v1/residents/suggest?q=An (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=An`.
- **Dữ liệu vào:** Không có body; có nhân khẩu tên chứa `An`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON là danh sách gợi ý, không có số giấy tờ hoặc điện thoại.
- **Nguồn:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-19.1-declaration-create.ANON — POST /api/v1/residency-declarations (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-19.1-declaration-create.RESIDENT — POST /api/v1/residency-declarations (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối.
- **Nguồn:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-19.1-declaration-create.ACCOUNTANT — POST /api/v1/residency-declarations (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-19.1-declaration-create.ADMIN — POST /api/v1/residency-declarations (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-20.1-declaration-mine.ANON — GET /api/v1/residency-declarations/mine (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body; RESIDENT gắn hộ 201.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-20.1-declaration-mine.RESIDENT — GET /api/v1/residency-declarations/mine (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body; RESIDENT gắn hộ 201.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra danh sách chỉ chứa đơn thuộc hộ 201.
- **Nguồn:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-20.1-declaration-mine.ACCOUNTANT — GET /api/v1/residency-declarations/mine (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body; RESIDENT gắn hộ 201.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-20.1-declaration-mine.ADMIN — GET /api/v1/residency-declarations/mine (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body; RESIDENT gắn hộ 201.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-21.1-declaration-cancel.ANON — POST /api/v1/residency-declarations/401/cancel (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/cancel`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-21.1-declaration-cancel.RESIDENT — POST /api/v1/residency-declarations/401/cancel (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/cancel`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối.
- **Nguồn:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-21.1-declaration-cancel.ACCOUNTANT — POST /api/v1/residency-declarations/401/cancel (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/cancel`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-21.1-declaration-cancel.ADMIN — POST /api/v1/residency-declarations/401/cancel (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/cancel`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-22.1-declarations.ANON — GET /api/v1/residency-declarations (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-22.1-declarations.RESIDENT — GET /api/v1/residency-declarations (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-22.1-declarations.ACCOUNTANT — GET /api/v1/residency-declarations (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-22.1-declarations.ADMIN — GET /api/v1/residency-declarations (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON trang hàng chờ khi được phép.
- **Nguồn:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.1-approve.ANON — POST /api/v1/residency-declarations/401/approve (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/approve`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.1-approve.RESIDENT — POST /api/v1/residency-declarations/401/approve (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/approve`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.1-approve.ACCOUNTANT — POST /api/v1/residency-declarations/401/approve (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/approve`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.1-approve.ADMIN — POST /api/v1/residency-declarations/401/approve (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/approve`.
- **Dữ liệu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.2-reject.ANON — POST /api/v1/residency-declarations/401/reject (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/reject`.
- **Dữ liệu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.2-reject.RESIDENT — POST /api/v1/residency-declarations/401/reject (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/reject`.
- **Dữ liệu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.2-reject.ACCOUNTANT — POST /api/v1/residency-declarations/401/reject (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/reject`.
- **Dữ liệu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-23.2-reject.ADMIN — POST /api/v1/residency-declarations/401/reject (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/401/reject`.
- **Dữ liệu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối.
- **Nguồn:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.1-fee-types.ANON — GET /api/v1/fee-types (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.1-fee-types.RESIDENT — GET /api/v1/fee-types (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.1-fee-types.ACCOUNTANT — GET /api/v1/fee-types (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có hai loại MANAGEMENT_FEE và WASTE_FEE.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.1-fee-types.ADMIN — GET /api/v1/fee-types (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có hai loại MANAGEMENT_FEE và WASTE_FEE.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.2-fee-rates.ANON — GET /api/v1/fee-rates (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/fee-rates`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.2-fee-rates.RESIDENT — GET /api/v1/fee-rates (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/fee-rates`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.2-fee-rates.ACCOUNTANT — GET /api/v1/fee-rates (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-rates`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON trang danh sách đơn giá.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.2-fee-rates.ADMIN — GET /api/v1/fee-rates (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-rates`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON trang danh sách đơn giá.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.3-fee-preview.ANON — GET /api/v1/fee-preview?householdId=201&period=2099-01 (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.3-fee-preview.RESIDENT — GET /api/v1/fee-preview?householdId=201&period=2099-01 (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.3-fee-preview.ACCOUNTANT — GET /api/v1/fee-preview?householdId=201&period=2099-01 (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có thành tiền và tổng; không tạo bản ghi thu phí.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-24.3-fee-preview.ADMIN — GET /api/v1/fee-preview?householdId=201&period=2099-01 (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
- **Dữ liệu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Kiểm tra JSON có thành tiền và tổng; không tạo bản ghi thu phí.
- **Nguồn:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.1-create-fee-rate.ANON — POST /api/v1/fee-rates (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.1-create-fee-rate.RESIDENT — POST /api/v1/fee-rates (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.1-create-fee-rate.ACCOUNTANT — POST /api/v1/fee-rates (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.1-create-fee-rate.ADMIN — POST /api/v1/fee-rates (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.2-patch-fee-rate.ANON — PATCH /api/v1/fee-rates/501 (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `PATCH /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.2-patch-fee-rate.RESIDENT — PATCH /api/v1/fee-rates/501 (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `PATCH /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.2-patch-fee-rate.ACCOUNTANT — PATCH /api/v1/fee-rates/501 (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.2-patch-fee-rate.ADMIN — PATCH /api/v1/fee-rates/501 (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `PATCH /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.3-delete-fee-rate.ANON — DELETE /api/v1/fee-rates/501 (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `DELETE /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.3-delete-fee-rate.RESIDENT — DELETE /api/v1/fee-rates/501 (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `DELETE /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.3-delete-fee-rate.ACCOUNTANT — DELETE /api/v1/fee-rates/501 (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `DELETE /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-25.3-delete-fee-rate.ADMIN — DELETE /api/v1/fee-rates/501 (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `DELETE /api/v1/fee-rates/501`.
- **Dữ liệu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
  3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-26.1-deny-all.ANON — GET /api/v1/rbac-nonexistent (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/rbac-nonexistent`.
- **Dữ liệu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
- **Các bước:**
  1. Mở phiên HTTP mới chưa đăng nhập.
  2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-26.1-deny-all.RESIDENT — GET /api/v1/rbac-nonexistent (RESIDENT)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/rbac-nonexistent`.
- **Dữ liệu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
- **Các bước:**
  1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-26.1-deny-all.ACCOUNTANT — GET /api/v1/rbac-nonexistent (ACCOUNTANT)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/rbac-nonexistent`.
- **Dữ liệu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
- **Các bước:**
  1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-26.1-deny-all.ADMIN — GET /api/v1/rbac-nonexistent (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/rbac-nonexistent`.
- **Dữ liệu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
  2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
  3. Ghi HTTP và nội dung JSON nhận được.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
- **Kiểm sau:** Không nhận dữ liệu nghiệp vụ từ endpoint.
- **Nguồn:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-extra.password-required.ADMIN — GET /api/v1/households (ADMIN bắt buộc đổi mật khẩu)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Tài khoản ADMIN ACTIVE, `must_change_password=true`; RESIDENT gắn hộ 201.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ phiên.
  2. Gọi `GET /api/v1/households` bằng phiên đó.
  3. Ghi mã HTTP và JSON.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `code=PASSWORD_CHANGE_REQUIRED`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me`; HTTP 200 và `mustChangePassword=true`.
- **Nguồn:** AUTH-08, RBAC quy tắc chung; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:23`.

### RBAC-extra.password-required.RESIDENT — GET /api/v1/households/me/residents (RESIDENT bắt buộc đổi mật khẩu)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Tài khoản RESIDENT ACTIVE, `must_change_password=true`; RESIDENT gắn hộ 201.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ phiên.
  2. Gọi `GET /api/v1/households/me/residents` bằng phiên đó.
  3. Ghi mã HTTP và JSON.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `code=PASSWORD_CHANGE_REQUIRED`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me`; HTTP 200 và `mustChangePassword=true`.
- **Nguồn:** AUTH-08, RBAC quy tắc chung; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:23`.

### RBAC-extra.csrf-missing.ADMIN — POST /api/v1/households (ADMIN, CSRF missing)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"RBAC-CSRF01","apartmentCode":"RBAC-CSRF-A01","areaM2":50.00}`; hai mã chưa tồn tại; không gửi header `X-CSRF-TOKEN`.
- **Các bước:**
  1. Đăng nhập ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ phiên.
  2. Gọi `GET /api/v1/auth/csrf`, lưu token và cookie phiên.
  3. Gửi `POST /api/v1/households` bằng phiên đó, không gửi header `X-CSRF-TOKEN`.
- **Kỳ vọng:** HTTP 403; thuộc tính `code` của lỗi CSRF chưa xác định.
- **Kiểm sau:** Tìm hộ mã `RBAC-CSRF01`; không có bản ghi mới.
- **Nguồn:** AUTH-07, quy tắc CSRF; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:39`.

### RBAC-extra.csrf-invalid.ADMIN — POST /api/v1/households (ADMIN, CSRF invalid)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"RBAC-CSRF01","apartmentCode":"RBAC-CSRF-A01","areaM2":50.00}`; hai mã chưa tồn tại; gửi `X-CSRF-TOKEN: <INVALID_CSRF_TOKEN>` khác token phiên.
- **Các bước:**
  1. Đăng nhập ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ phiên.
  2. Gọi `GET /api/v1/auth/csrf`, lưu token và cookie phiên.
  3. Gửi `POST /api/v1/households` bằng phiên đó, gửi `X-CSRF-TOKEN: <INVALID_CSRF_TOKEN>` khác token phiên.
- **Kỳ vọng:** HTTP 403; thuộc tính `code` của lỗi CSRF chưa xác định.
- **Kiểm sau:** Tìm hộ mã `RBAC-CSRF01`; không có bản ghi mới.
- **Nguồn:** AUTH-07, quy tắc CSRF; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:39`.

## Điểm chưa xác định

- `GET /actuator/health/**`: matcher là public, nhưng HTTP của `/actuator/health/db` tùy thành phần health được công khai và cấu hình môi trường; chưa xác định trong outline.
- Body và thuộc tính `code` của lỗi CSRF thiếu/sai token chưa được outline xác định; chỉ kỳ vọng HTTP 403.
