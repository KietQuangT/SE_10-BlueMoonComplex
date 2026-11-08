# Ma trận kiểm thử phân quyền RBAC

Mỗi hàng dưới đây chọn một endpoint đại diện cho hàng tương ứng trong mục 1 của outline. Dùng dữ liệu kiểm thử riêng cho từng ca ghi; thay ID và placeholder bằng giá trị trong môi trường kiểm thử. Các phiên đã đăng nhập dùng tài khoản ACTIVE có `must_change_password=false`, trừ ca kiểm tra cờ riêng.

### RBAC-01.ANON — GET /actuator/health

- **Vai trò:** ANON.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy và health có trạng thái UP.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /actuator/health` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `status` là `UP`.
- **Kiểm sau:** Đọc `status` trong response: `UP`.
- **Nguồn:** RBAC-01, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-01.RESIDENT — GET /actuator/health

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy và health có trạng thái UP.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /actuator/health` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `status` là `UP`.
- **Kiểm sau:** Đọc `status` trong response: `UP`.
- **Nguồn:** RBAC-01, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-01.ACCOUNTANT — GET /actuator/health

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy và health có trạng thái UP.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /actuator/health` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `status` là `UP`.
- **Kiểm sau:** Đọc `status` trong response: `UP`.
- **Nguồn:** RBAC-01, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-01.ADMIN — GET /actuator/health

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy và health có trạng thái UP.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /actuator/health` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `status` là `UP`.
- **Kiểm sau:** Đọc `status` trong response: `UP`.
- **Nguồn:** RBAC-01, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-02.ANON — GET /api/v1/system/info

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/system/info` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại endpoint và đối chiếu metadata không đổi.
- **Nguồn:** RBAC-02, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.

### RBAC-02.RESIDENT — GET /api/v1/system/info

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/system/info` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại endpoint và đối chiếu metadata không đổi.
- **Nguồn:** RBAC-02, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.

### RBAC-02.ACCOUNTANT — GET /api/v1/system/info

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/system/info` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại endpoint và đối chiếu metadata không đổi.
- **Nguồn:** RBAC-02, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.

### RBAC-02.ADMIN — GET /api/v1/system/info

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/system/info` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại endpoint và đối chiếu metadata không đổi.
- **Nguồn:** RBAC-02, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.

### RBAC-03.ANON — GET /api/v1/auth/csrf

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body. Dùng phiên trình duyệt mới hoặc phiên đã đăng nhập.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/csrf` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON chứa token CSRF.
- **Kiểm sau:** Lưu token từ response để dùng trong phiên hiện tại.
- **Nguồn:** RBAC-03, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### RBAC-03.RESIDENT — GET /api/v1/auth/csrf

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body. Dùng phiên trình duyệt mới hoặc phiên đã đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/csrf` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON chứa token CSRF.
- **Kiểm sau:** Lưu token từ response để dùng trong phiên hiện tại.
- **Nguồn:** RBAC-03, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### RBAC-03.ACCOUNTANT — GET /api/v1/auth/csrf

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body. Dùng phiên trình duyệt mới hoặc phiên đã đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/csrf` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON chứa token CSRF.
- **Kiểm sau:** Lưu token từ response để dùng trong phiên hiện tại.
- **Nguồn:** RBAC-03, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### RBAC-03.ADMIN — GET /api/v1/auth/csrf

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body. Dùng phiên trình duyệt mới hoặc phiên đã đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/csrf` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON chứa token CSRF.
- **Kiểm sau:** Lưu token từ response để dùng trong phiên hiện tại.
- **Nguồn:** RBAC-03, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### RBAC-04.ANON — POST /api/v1/auth/login

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<LOGIN_USERNAME>","password":"<LOGIN_PASSWORD>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`. `<LOGIN_USERNAME>` là tài khoản ACTIVE với mật khẩu đúng; với vai trò đã đăng nhập, giữ phiên của vai trò đó trước khi gọi.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/auth/login` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` trong phiên sau đăng nhập; đối chiếu username.
- **Nguồn:** RBAC-04, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`.

### RBAC-04.RESIDENT — POST /api/v1/auth/login

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<LOGIN_USERNAME>","password":"<LOGIN_PASSWORD>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`. `<LOGIN_USERNAME>` là tài khoản ACTIVE với mật khẩu đúng; với vai trò đã đăng nhập, giữ phiên của vai trò đó trước khi gọi.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/login` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` trong phiên sau đăng nhập; đối chiếu username.
- **Nguồn:** RBAC-04, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`.

### RBAC-04.ACCOUNTANT — POST /api/v1/auth/login

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<LOGIN_USERNAME>","password":"<LOGIN_PASSWORD>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`. `<LOGIN_USERNAME>` là tài khoản ACTIVE với mật khẩu đúng; với vai trò đã đăng nhập, giữ phiên của vai trò đó trước khi gọi.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/login` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` trong phiên sau đăng nhập; đối chiếu username.
- **Nguồn:** RBAC-04, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`.

### RBAC-04.ADMIN — POST /api/v1/auth/login

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<LOGIN_USERNAME>","password":"<LOGIN_PASSWORD>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`. `<LOGIN_USERNAME>` là tài khoản ACTIVE với mật khẩu đúng; với vai trò đã đăng nhập, giữ phiên của vai trò đó trước khi gọi.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/login` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` trong phiên sau đăng nhập; đối chiếu username.
- **Nguồn:** RBAC-04, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`.

### RBAC-05.ANON — GET /api/v1/auth/me

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body. Tài khoản ACTIVE, không bắt buộc đổi mật khẩu.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/me` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-05, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`.

### RBAC-05.RESIDENT — GET /api/v1/auth/me

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body. Tài khoản ACTIVE, không bắt buộc đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/me` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Với phiên đăng nhập, đối chiếu `username` và `role` với tài khoản.
- **Nguồn:** RBAC-05, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`.

### RBAC-05.ACCOUNTANT — GET /api/v1/auth/me

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body. Tài khoản ACTIVE, không bắt buộc đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/me` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Với phiên đăng nhập, đối chiếu `username` và `role` với tài khoản.
- **Nguồn:** RBAC-05, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`.

### RBAC-05.ADMIN — GET /api/v1/auth/me

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body. Tài khoản ACTIVE, không bắt buộc đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/me` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Với phiên đăng nhập, đối chiếu `username` và `role` với tài khoản.
- **Nguồn:** RBAC-05, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`.

### RBAC-06.ANON — POST /api/v1/auth/change-password

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; header CSRF hợp lệ. Tài khoản ACTIVE; mật khẩu mới đáp ứng chính sách và khác mật khẩu hiện tại.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/auth/change-password` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-06, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.

### RBAC-06.RESIDENT — POST /api/v1/auth/change-password

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; header CSRF hợp lệ. Tài khoản ACTIVE; mật khẩu mới đáp ứng chính sách và khác mật khẩu hiện tại.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/change-password` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đăng nhập lại bằng `<NEW_PASSWORD>` và xác nhận thành công; với request bị từ chối, mật khẩu cũ vẫn hoạt động.
- **Nguồn:** RBAC-06, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.

### RBAC-06.ACCOUNTANT — POST /api/v1/auth/change-password

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; header CSRF hợp lệ. Tài khoản ACTIVE; mật khẩu mới đáp ứng chính sách và khác mật khẩu hiện tại.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/change-password` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đăng nhập lại bằng `<NEW_PASSWORD>` và xác nhận thành công; với request bị từ chối, mật khẩu cũ vẫn hoạt động.
- **Nguồn:** RBAC-06, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.

### RBAC-06.ADMIN — POST /api/v1/auth/change-password

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; header CSRF hợp lệ. Tài khoản ACTIVE; mật khẩu mới đáp ứng chính sách và khác mật khẩu hiện tại.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/change-password` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đăng nhập lại bằng `<NEW_PASSWORD>` và xác nhận thành công; với request bị từ chối, mật khẩu cũ vẫn hoạt động.
- **Nguồn:** RBAC-06, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.

### RBAC-07.ANON — POST /api/v1/accounts

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"<NEW_USERNAME>","fullName":"Nguyen Van A","email":"<TEST_EMAIL>","role":"ACCOUNTANT","householdId":null}`; header CSRF hợp lệ. `<NEW_USERNAME>` chưa tồn tại; `<TEST_EMAIL>` là địa chỉ kiểm thử hợp lệ không thuộc người thật.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/accounts` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-07, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.

### RBAC-07.RESIDENT — POST /api/v1/accounts

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"<NEW_USERNAME>","fullName":"Nguyen Van A","email":"<TEST_EMAIL>","role":"ACCOUNTANT","householdId":null}`; header CSRF hợp lệ. `<NEW_USERNAME>` chưa tồn tại; `<TEST_EMAIL>` là địa chỉ kiểm thử hợp lệ không thuộc người thật.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-07, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.

### RBAC-07.ACCOUNTANT — POST /api/v1/accounts

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"<NEW_USERNAME>","fullName":"Nguyen Van A","email":"<TEST_EMAIL>","role":"ACCOUNTANT","householdId":null}`; header CSRF hợp lệ. `<NEW_USERNAME>` chưa tồn tại; `<TEST_EMAIL>` là địa chỉ kiểm thử hợp lệ không thuộc người thật.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-07, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.

### RBAC-07.ADMIN — POST /api/v1/accounts

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"<NEW_USERNAME>","fullName":"Nguyen Van A","email":"<TEST_EMAIL>","role":"ACCOUNTANT","householdId":null}`; header CSRF hợp lệ. `<NEW_USERNAME>` chưa tồn tại; `<TEST_EMAIL>` là địa chỉ kiểm thử hợp lệ không thuộc người thật.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Đọc bản ghi theo username: chỉ ADMIN tạo được tài khoản mới.
- **Nguồn:** RBAC-07, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.

### RBAC-08.ANON — POST /api/v1/accounts/<ACCOUNT_ID>/disable

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<ACCOUNT_ID>` là tài khoản ACCOUNTANT ACTIVE khác tài khoản gọi.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-08, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`.

### RBAC-08.RESIDENT — POST /api/v1/accounts/<ACCOUNT_ID>/disable

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<ACCOUNT_ID>` là tài khoản ACCOUNTANT ACTIVE khác tài khoản gọi.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-08, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`.

### RBAC-08.ACCOUNTANT — POST /api/v1/accounts/<ACCOUNT_ID>/disable

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<ACCOUNT_ID>` là tài khoản ACCOUNTANT ACTIVE khác tài khoản gọi.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-08, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`.

### RBAC-08.ADMIN — POST /api/v1/accounts/<ACCOUNT_ID>/disable

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<ACCOUNT_ID>` là tài khoản ACCOUNTANT ACTIVE khác tài khoản gọi.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đọc trạng thái tài khoản: DISABLED sau ADMIN, không đổi khi bị từ chối.
- **Nguồn:** RBAC-08, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`.

### RBAC-09.ANON — GET /api/v1/households

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body. Có ít nhất một hộ kiểm thử.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-09, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`.

### RBAC-09.RESIDENT — GET /api/v1/households

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body. Có ít nhất một hộ kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-09, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`.

### RBAC-09.ACCOUNTANT — GET /api/v1/households

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body. Có ít nhất một hộ kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-09, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`.

### RBAC-09.ADMIN — GET /api/v1/households

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body. Có ít nhất một hộ kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách hộ trong response với dữ liệu kiểm thử.
- **Nguồn:** RBAC-09, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`.

### RBAC-10.ANON — POST /api/v1/households

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"<NEW_HOUSEHOLD_CODE>","apartmentCode":"<NEW_APARTMENT_CODE>","areaM2":65.5,"address":"Khu kiểm thử"}`; header CSRF hợp lệ. Mã hộ và căn hộ chưa tồn tại.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-10, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### RBAC-10.RESIDENT — POST /api/v1/households

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"<NEW_HOUSEHOLD_CODE>","apartmentCode":"<NEW_APARTMENT_CODE>","areaM2":65.5,"address":"Khu kiểm thử"}`; header CSRF hợp lệ. Mã hộ và căn hộ chưa tồn tại.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-10, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### RBAC-10.ACCOUNTANT — POST /api/v1/households

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"<NEW_HOUSEHOLD_CODE>","apartmentCode":"<NEW_APARTMENT_CODE>","areaM2":65.5,"address":"Khu kiểm thử"}`; header CSRF hợp lệ. Mã hộ và căn hộ chưa tồn tại.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-10, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### RBAC-10.ADMIN — POST /api/v1/households

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"<NEW_HOUSEHOLD_CODE>","apartmentCode":"<NEW_APARTMENT_CODE>","areaM2":65.5,"address":"Khu kiểm thử"}`; header CSRF hợp lệ. Mã hộ và căn hộ chưa tồn tại.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Tìm lại theo mã hộ: chỉ request ADMIN tạo bản ghi.
- **Nguồn:** RBAC-10, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### RBAC-11.ANON — GET /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có nhân khẩu.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-11, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`.

### RBAC-11.RESIDENT — GET /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có nhân khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-11, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`.

### RBAC-11.ACCOUNTANT — GET /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có nhân khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-11, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`.

### RBAC-11.ADMIN — GET /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có nhân khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu nhân khẩu trong response với hộ kiểm thử.
- **Nguồn:** RBAC-11, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`.

### RBAC-12.ANON — POST /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nguyen Van B","relationshipToHead":"Con"}`; header CSRF hợp lệ. `<HOUSEHOLD_ID>` là hộ ACTIVE.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-12, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.

### RBAC-12.RESIDENT — POST /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nguyen Van B","relationshipToHead":"Con"}`; header CSRF hợp lệ. `<HOUSEHOLD_ID>` là hộ ACTIVE.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-12, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.

### RBAC-12.ACCOUNTANT — POST /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nguyen Van B","relationshipToHead":"Con"}`; header CSRF hợp lệ. `<HOUSEHOLD_ID>` là hộ ACTIVE.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-12, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.

### RBAC-12.ADMIN — POST /api/v1/households/<HOUSEHOLD_ID>/residents

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nguyen Van B","relationshipToHead":"Con"}`; header CSRF hợp lệ. `<HOUSEHOLD_ID>` là hộ ACTIVE.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** GET danh sách nhân khẩu của hộ: chỉ ADMIN tạo nhân khẩu mới.
- **Nguồn:** RBAC-12, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.

### RBAC-13.ANON — GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có lịch sử cư trú.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-13, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`.

### RBAC-13.RESIDENT — GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có lịch sử cư trú.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-13, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`.

### RBAC-13.ACCOUNTANT — GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có lịch sử cư trú.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-13, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`.

### RBAC-13.ADMIN — GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có lịch sử cư trú.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu lịch sử trong response với hộ kiểm thử.
- **Nguồn:** RBAC-13, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`.

### RBAC-14.ANON — GET /api/v1/households/me/residents

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body. RESIDENT được gắn hộ có nhân khẩu kiểm thử.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/me/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-14, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`.

### RBAC-14.RESIDENT — GET /api/v1/households/me/residents

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body. RESIDENT được gắn hộ có nhân khẩu kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/me/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách chỉ gồm nhân khẩu thuộc hộ của RESIDENT.
- **Nguồn:** RBAC-14, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`.

### RBAC-14.ACCOUNTANT — GET /api/v1/households/me/residents

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body. RESIDENT được gắn hộ có nhân khẩu kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/me/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-14, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`.

### RBAC-14.ADMIN — GET /api/v1/households/me/residents

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body. RESIDENT được gắn hộ có nhân khẩu kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/me/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-14, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`.

### RBAC-15.ANON — GET /api/v1/residents/<RESIDENT_ID>

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residents/<RESIDENT_ID>`.
- **Dữ liệu vào:** Không có body. `<RESIDENT_ID>` là nhân khẩu kiểm thử hiện có.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/<RESIDENT_ID>` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-15, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`.

### RBAC-15.RESIDENT — GET /api/v1/residents/<RESIDENT_ID>

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residents/<RESIDENT_ID>`.
- **Dữ liệu vào:** Không có body. `<RESIDENT_ID>` là nhân khẩu kiểm thử hiện có.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/<RESIDENT_ID>` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-15, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`.

### RBAC-15.ACCOUNTANT — GET /api/v1/residents/<RESIDENT_ID>

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residents/<RESIDENT_ID>`.
- **Dữ liệu vào:** Không có body. `<RESIDENT_ID>` là nhân khẩu kiểm thử hiện có.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/<RESIDENT_ID>` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-15, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`.

### RBAC-15.ADMIN — GET /api/v1/residents/<RESIDENT_ID>

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/<RESIDENT_ID>`.
- **Dữ liệu vào:** Không có body. `<RESIDENT_ID>` là nhân khẩu kiểm thử hiện có.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/<RESIDENT_ID>` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu ID trong response với nhân khẩu được chọn.
- **Nguồn:** RBAC-15, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`.

### RBAC-16.ANON — POST /api/v1/residents/<RESIDENT_ID>/move-out

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyển đi kiểm thử"}`; header CSRF hợp lệ. `<RESIDENT_ID>` đang cư trú và đủ điều kiện chuyển đi.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/move-out` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-16, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`.

### RBAC-16.RESIDENT — POST /api/v1/residents/<RESIDENT_ID>/move-out

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyển đi kiểm thử"}`; header CSRF hợp lệ. `<RESIDENT_ID>` đang cư trú và đủ điều kiện chuyển đi.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/move-out` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-16, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`.

### RBAC-16.ACCOUNTANT — POST /api/v1/residents/<RESIDENT_ID>/move-out

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyển đi kiểm thử"}`; header CSRF hợp lệ. `<RESIDENT_ID>` đang cư trú và đủ điều kiện chuyển đi.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/move-out` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-16, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`.

### RBAC-16.ADMIN — POST /api/v1/residents/<RESIDENT_ID>/move-out

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyển đi kiểm thử"}`; header CSRF hợp lệ. `<RESIDENT_ID>` đang cư trú và đủ điều kiện chuyển đi.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/move-out` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** GET lại nhân khẩu: trạng thái chuyển đi chỉ thay đổi sau request ADMIN.
- **Nguồn:** RBAC-16, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`.

### RBAC-17.ANON — POST /api/v1/residents/<RESIDENT_ID>/return

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/return`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<RESIDENT_ID>` có đơn tạm vắng đã duyệt và đủ điều kiện trở về.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/return` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-17, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`.

### RBAC-17.RESIDENT — POST /api/v1/residents/<RESIDENT_ID>/return

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/return`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<RESIDENT_ID>` có đơn tạm vắng đã duyệt và đủ điều kiện trở về.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/return` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-17, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`.

### RBAC-17.ACCOUNTANT — POST /api/v1/residents/<RESIDENT_ID>/return

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/return`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<RESIDENT_ID>` có đơn tạm vắng đã duyệt và đủ điều kiện trở về.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/return` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-17, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`.

### RBAC-17.ADMIN — POST /api/v1/residents/<RESIDENT_ID>/return

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/return`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<RESIDENT_ID>` có đơn tạm vắng đã duyệt và đủ điều kiện trở về.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/return` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** GET lại nhân khẩu và đơn: trạng thái trở về chỉ thay đổi sau request ADMIN.
- **Nguồn:** RBAC-17, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`.

### RBAC-18.ANON — GET /api/v1/residents/suggest?q=Nguyen

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=Nguyen`.
- **Dữ liệu vào:** Tham số `q=Nguyen`; không có body. Có nhân khẩu kiểm thử tên Nguyen.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/suggest?q=Nguyen` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-18, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

### RBAC-18.RESIDENT — GET /api/v1/residents/suggest?q=Nguyen

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=Nguyen`.
- **Dữ liệu vào:** Tham số `q=Nguyen`; không có body. Có nhân khẩu kiểm thử tên Nguyen.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/suggest?q=Nguyen` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-18, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

### RBAC-18.ACCOUNTANT — GET /api/v1/residents/suggest?q=Nguyen

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=Nguyen`.
- **Dữ liệu vào:** Tham số `q=Nguyen`; không có body. Có nhân khẩu kiểm thử tên Nguyen.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/suggest?q=Nguyen` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-18, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

### RBAC-18.ADMIN — GET /api/v1/residents/suggest?q=Nguyen

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=Nguyen`.
- **Dữ liệu vào:** Tham số `q=Nguyen`; không có body. Có nhân khẩu kiểm thử tên Nguyen.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/suggest?q=Nguyen` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách gợi ý với dữ liệu kiểm thử.
- **Nguồn:** RBAC-18, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

### RBAC-19.ANON — POST /api/v1/residency-declarations

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY_VN>","endDate":null,"reason":"Đi công tác"}`; header CSRF hợp lệ. `<RESIDENT_ID>` thuộc hộ RESIDENT và đủ điều kiện khai báo; thay `<TODAY_VN>` bằng ngày hiện tại theo giờ Việt Nam.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-19, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`.

### RBAC-19.RESIDENT — POST /api/v1/residency-declarations

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY_VN>","endDate":null,"reason":"Đi công tác"}`; header CSRF hợp lệ. `<RESIDENT_ID>` thuộc hộ RESIDENT và đủ điều kiện khai báo; thay `<TODAY_VN>` bằng ngày hiện tại theo giờ Việt Nam.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** GET `/api/v1/residency-declarations/mine`: chỉ request RESIDENT tạo đơn.
- **Nguồn:** RBAC-19, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`.

### RBAC-19.ACCOUNTANT — POST /api/v1/residency-declarations

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY_VN>","endDate":null,"reason":"Đi công tác"}`; header CSRF hợp lệ. `<RESIDENT_ID>` thuộc hộ RESIDENT và đủ điều kiện khai báo; thay `<TODAY_VN>` bằng ngày hiện tại theo giờ Việt Nam.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-19, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`.

### RBAC-19.ADMIN — POST /api/v1/residency-declarations

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY_VN>","endDate":null,"reason":"Đi công tác"}`; header CSRF hợp lệ. `<RESIDENT_ID>` thuộc hộ RESIDENT và đủ điều kiện khai báo; thay `<TODAY_VN>` bằng ngày hiện tại theo giờ Việt Nam.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-19, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`.

### RBAC-20.ANON — GET /api/v1/residency-declarations/mine

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body. RESIDENT có một đơn kiểm thử do chính tài khoản tạo.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations/mine` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-20, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`.

### RBAC-20.RESIDENT — GET /api/v1/residency-declarations/mine

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body. RESIDENT có một đơn kiểm thử do chính tài khoản tạo.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations/mine` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách với các đơn của tài khoản RESIDENT.
- **Nguồn:** RBAC-20, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`.

### RBAC-20.ACCOUNTANT — GET /api/v1/residency-declarations/mine

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body. RESIDENT có một đơn kiểm thử do chính tài khoản tạo.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations/mine` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-20, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`.

### RBAC-20.ADMIN — GET /api/v1/residency-declarations/mine

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body. RESIDENT có một đơn kiểm thử do chính tài khoản tạo.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations/mine` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-20, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`.

### RBAC-21.ANON — POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING do RESIDENT tạo.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-21, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`.

### RBAC-21.RESIDENT — POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING do RESIDENT tạo.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** GET danh sách đơn của RESIDENT: chỉ request RESIDENT chuyển đơn sang đã hủy.
- **Nguồn:** RBAC-21, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`.

### RBAC-21.ACCOUNTANT — POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING do RESIDENT tạo.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-21, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`.

### RBAC-21.ADMIN — POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING do RESIDENT tạo.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-21, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`.

### RBAC-22.ANON — GET /api/v1/residency-declarations

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body. Có đơn tạm vắng kiểm thử trong hệ thống.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-22, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`.

### RBAC-22.RESIDENT — GET /api/v1/residency-declarations

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body. Có đơn tạm vắng kiểm thử trong hệ thống.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-22, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`.

### RBAC-22.ACCOUNTANT — GET /api/v1/residency-declarations

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body. Có đơn tạm vắng kiểm thử trong hệ thống.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-22, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`.

### RBAC-22.ADMIN — GET /api/v1/residency-declarations

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body. Có đơn tạm vắng kiểm thử trong hệ thống.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách response với đơn kiểm thử.
- **Nguồn:** RBAC-22, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`.

### RBAC-23.ANON — POST /api/v1/residency-declarations/<DECLARATION_ID>/approve

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING đủ điều kiện duyệt.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-23, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`.

### RBAC-23.RESIDENT — POST /api/v1/residency-declarations/<DECLARATION_ID>/approve

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING đủ điều kiện duyệt.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-23, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`.

### RBAC-23.ACCOUNTANT — POST /api/v1/residency-declarations/<DECLARATION_ID>/approve

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING đủ điều kiện duyệt.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-23, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`.

### RBAC-23.ADMIN — POST /api/v1/residency-declarations/<DECLARATION_ID>/approve

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING đủ điều kiện duyệt.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** GET danh sách đơn: chỉ request ADMIN chuyển đơn sang đã duyệt.
- **Nguồn:** RBAC-23, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`.

### RBAC-24.ANON — GET /api/v1/fee-types

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body. Có loại phí kiểm thử.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/fee-types` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-24, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`.

### RBAC-24.RESIDENT — GET /api/v1/fee-types

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body. Có loại phí kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/fee-types` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-24, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`.

### RBAC-24.ACCOUNTANT — GET /api/v1/fee-types

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body. Có loại phí kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/fee-types` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách response với loại phí đã cấu hình.
- **Nguồn:** RBAC-24, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`.

### RBAC-24.ADMIN — GET /api/v1/fee-types

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body. Có loại phí kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/fee-types` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách response với loại phí đã cấu hình.
- **Nguồn:** RBAC-24, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`.

### RBAC-25.ANON — POST /api/v1/fee-rates

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000,"effectiveFrom":"<FUTURE_MONTH_FIRST_DAY>"}`; header CSRF hợp lệ. Loại phí tồn tại; ngày là ngày đầu tháng sau và chưa có đơn giá cho tháng đó.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/fee-rates` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-25, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`.

### RBAC-25.RESIDENT — POST /api/v1/fee-rates

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000,"effectiveFrom":"<FUTURE_MONTH_FIRST_DAY>"}`; header CSRF hợp lệ. Loại phí tồn tại; ngày là ngày đầu tháng sau và chưa có đơn giá cho tháng đó.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/fee-rates` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-25, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`.

### RBAC-25.ACCOUNTANT — POST /api/v1/fee-rates

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000,"effectiveFrom":"<FUTURE_MONTH_FIRST_DAY>"}`; header CSRF hợp lệ. Loại phí tồn tại; ngày là ngày đầu tháng sau và chưa có đơn giá cho tháng đó.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/fee-rates` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** GET `/api/v1/fee-rates`: chỉ request ACCOUNTANT tạo đơn giá.
- **Nguồn:** RBAC-25, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`.

### RBAC-25.ADMIN — POST /api/v1/fee-rates

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000,"effectiveFrom":"<FUTURE_MONTH_FIRST_DAY>"}`; header CSRF hợp lệ. Loại phí tồn tại; ngày là ngày đầu tháng sau và chưa có đơn giá cho tháng đó.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/fee-rates` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-25, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`.

### RBAC-26.ANON — GET /api/v1/unknown-rbac-probe

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/unknown-rbac-probe`.
- **Dữ liệu vào:** Không có body. Đường dẫn kiểm thử không nằm trong matcher đã khai báo.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/unknown-rbac-probe` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-26, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.

### RBAC-26.RESIDENT — GET /api/v1/unknown-rbac-probe

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/unknown-rbac-probe`.
- **Dữ liệu vào:** Không có body. Đường dẫn kiểm thử không nằm trong matcher đã khai báo.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/unknown-rbac-probe` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-26, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.

### RBAC-26.ACCOUNTANT — GET /api/v1/unknown-rbac-probe

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/unknown-rbac-probe`.
- **Dữ liệu vào:** Không có body. Đường dẫn kiểm thử không nằm trong matcher đã khai báo.
- **Các bước:**
  1. Đăng nhập bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/unknown-rbac-probe` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-26, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.

### RBAC-26.ADMIN — GET /api/v1/unknown-rbac-probe

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/unknown-rbac-probe`.
- **Dữ liệu vào:** Không có body. Đường dẫn kiểm thử không nằm trong matcher đã khai báo.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/unknown-rbac-probe` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-26, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.

### RBAC-PASSWORD.ADMIN — GET /api/v1/households khi bắt buộc đổi mật khẩu

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** `<ADMIN_USERNAME>`, `<ADMIN_PASSWORD>`; tài khoản ACTIVE có `must_change_password=true`; không có body.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Gọi `GET /api/v1/auth/me` và xác nhận `mustChangePassword=true`.
  3. Gọi `GET /api/v1/households` trong phiên đó.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `code=PASSWORD_CHANGE_REQUIRED`.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me`, xác nhận cờ vẫn là `true`.
- **Nguồn:** outline mục 1, quy tắc đổi mật khẩu; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:23`.

### RBAC-PASSWORD.RESIDENT — GET /api/v1/households/me/residents khi bắt buộc đổi mật khẩu

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** `<RESIDENT_USERNAME>`, `<RESIDENT_PASSWORD>`; tài khoản ACTIVE có `must_change_password=true`; không có body.
- **Các bước:**
  1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`.
  2. Gọi `GET /api/v1/auth/me` và xác nhận `mustChangePassword=true`.
  3. Gọi `GET /api/v1/households/me/residents` trong phiên đó.
- **Kỳ vọng:** HTTP 403, ProblemDetail có `code=PASSWORD_CHANGE_REQUIRED`.
- **Kiểm sau:** Gọi lại `GET /api/v1/auth/me`, xác nhận cờ vẫn là `true`.
- **Nguồn:** outline mục 1, quy tắc đổi mật khẩu; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:23`.

### RBAC-CSRF.ADMIN — POST /api/v1/accounts không có token CSRF

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"<NEW_USERNAME>","fullName":"Nguyen Van A","email":"<TEST_EMAIL>","role":"ACCOUNTANT","householdId":null}`; không gửi header `X-CSRF-TOKEN`; username chưa tồn tại.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ cookie phiên và bỏ header `X-CSRF-TOKEN`.
  3. Gọi `POST /api/v1/accounts` với body đã nêu.
- **Kỳ vọng:** HTTP 403; nội dung `code` chưa xác định.
- **Kiểm sau:** Đọc tài khoản theo `<NEW_USERNAME>`: không có tài khoản mới.
- **Nguồn:** outline mục 1, quy tắc CSRF; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:39`.

## Ca đánh số để kiểm tra lại

Các ca đánh số lặp lại hai phép thử đại diện của từng hàng để đáp ứng quy ước kiểm đếm; dùng dữ liệu độc lập cho các ca ghi.

### RBAC-01.1 — GET /actuator/health (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy và health có trạng thái UP.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /actuator/health` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `status` là `UP`.
- **Kiểm sau:** Đọc `status` trong response: `UP`.
- **Nguồn:** RBAC-01, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-01.2 — GET /actuator/health (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy và health có trạng thái UP.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /actuator/health` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `status` là `UP`.
- **Kiểm sau:** Đọc `status` trong response: `UP`.
- **Nguồn:** RBAC-01, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### RBAC-02.1 — GET /api/v1/system/info (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/system/info` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại endpoint và đối chiếu metadata không đổi.
- **Nguồn:** RBAC-02, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.

### RBAC-02.2 — GET /api/v1/system/info (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không có body. Ứng dụng đang chạy.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/system/info` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đọc lại endpoint và đối chiếu metadata không đổi.
- **Nguồn:** RBAC-02, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.

### RBAC-03.1 — GET /api/v1/auth/csrf (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body. Dùng phiên trình duyệt mới hoặc phiên đã đăng nhập.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/csrf` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON chứa token CSRF.
- **Kiểm sau:** Lưu token từ response để dùng trong phiên hiện tại.
- **Nguồn:** RBAC-03, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### RBAC-03.2 — GET /api/v1/auth/csrf (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Không có body. Dùng phiên trình duyệt mới hoặc phiên đã đăng nhập.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/csrf` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON chứa token CSRF.
- **Kiểm sau:** Lưu token từ response để dùng trong phiên hiện tại.
- **Nguồn:** RBAC-03, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### RBAC-04.1 — POST /api/v1/auth/login (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<LOGIN_USERNAME>","password":"<LOGIN_PASSWORD>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`. `<LOGIN_USERNAME>` là tài khoản ACTIVE với mật khẩu đúng; với vai trò đã đăng nhập, giữ phiên của vai trò đó trước khi gọi.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/auth/login` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` trong phiên sau đăng nhập; đối chiếu username.
- **Nguồn:** RBAC-04, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`.

### RBAC-04.2 — POST /api/v1/auth/login (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Body `{"username":"<LOGIN_USERNAME>","password":"<LOGIN_PASSWORD>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`. `<LOGIN_USERNAME>` là tài khoản ACTIVE với mật khẩu đúng; với vai trò đã đăng nhập, giữ phiên của vai trò đó trước khi gọi.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/login` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` trong phiên sau đăng nhập; đối chiếu username.
- **Nguồn:** RBAC-04, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`.

### RBAC-05.1 — GET /api/v1/auth/me (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body. Tài khoản ACTIVE, không bắt buộc đổi mật khẩu.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/me` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-05, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`.

### RBAC-05.2 — GET /api/v1/auth/me (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Không có body. Tài khoản ACTIVE, không bắt buộc đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/auth/me` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200. JSON có `username`, `role`, `mustChangePassword`.
- **Kiểm sau:** Với phiên đăng nhập, đối chiếu `username` và `role` với tài khoản.
- **Nguồn:** RBAC-05, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`.

### RBAC-06.1 — POST /api/v1/auth/change-password (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; header CSRF hợp lệ. Tài khoản ACTIVE; mật khẩu mới đáp ứng chính sách và khác mật khẩu hiện tại.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/auth/change-password` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-06, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.

### RBAC-06.2 — POST /api/v1/auth/change-password (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; header CSRF hợp lệ. Tài khoản ACTIVE; mật khẩu mới đáp ứng chính sách và khác mật khẩu hiện tại.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/auth/change-password` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đăng nhập lại bằng `<NEW_PASSWORD>` và xác nhận thành công; với request bị từ chối, mật khẩu cũ vẫn hoạt động.
- **Nguồn:** RBAC-06, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.

### RBAC-07.1 — POST /api/v1/accounts (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"<NEW_USERNAME>","fullName":"Nguyen Van A","email":"<TEST_EMAIL>","role":"ACCOUNTANT","householdId":null}`; header CSRF hợp lệ. `<NEW_USERNAME>` chưa tồn tại; `<TEST_EMAIL>` là địa chỉ kiểm thử hợp lệ không thuộc người thật.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/accounts` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-07, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.

### RBAC-07.2 — POST /api/v1/accounts (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts`.
- **Dữ liệu vào:** Body `{"username":"<NEW_USERNAME>","fullName":"Nguyen Van A","email":"<TEST_EMAIL>","role":"ACCOUNTANT","householdId":null}`; header CSRF hợp lệ. `<NEW_USERNAME>` chưa tồn tại; `<TEST_EMAIL>` là địa chỉ kiểm thử hợp lệ không thuộc người thật.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Đọc bản ghi theo username: chỉ ADMIN tạo được tài khoản mới.
- **Nguồn:** RBAC-07, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.

### RBAC-08.1 — POST /api/v1/accounts/<ACCOUNT_ID>/disable (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<ACCOUNT_ID>` là tài khoản ACCOUNTANT ACTIVE khác tài khoản gọi.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-08, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`.

### RBAC-08.2 — POST /api/v1/accounts/<ACCOUNT_ID>/disable (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<ACCOUNT_ID>` là tài khoản ACCOUNTANT ACTIVE khác tài khoản gọi.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** Đọc trạng thái tài khoản: DISABLED sau ADMIN, không đổi khi bị từ chối.
- **Nguồn:** RBAC-08, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`.

### RBAC-09.1 — GET /api/v1/households (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body. Có ít nhất một hộ kiểm thử.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-09, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`.

### RBAC-09.2 — GET /api/v1/households (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households`.
- **Dữ liệu vào:** Không có body. Có ít nhất một hộ kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách hộ trong response với dữ liệu kiểm thử.
- **Nguồn:** RBAC-09, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`.

### RBAC-10.1 — POST /api/v1/households (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"<NEW_HOUSEHOLD_CODE>","apartmentCode":"<NEW_APARTMENT_CODE>","areaM2":65.5,"address":"Khu kiểm thử"}`; header CSRF hợp lệ. Mã hộ và căn hộ chưa tồn tại.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-10, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### RBAC-10.2 — POST /api/v1/households (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Body `{"householdCode":"<NEW_HOUSEHOLD_CODE>","apartmentCode":"<NEW_APARTMENT_CODE>","areaM2":65.5,"address":"Khu kiểm thử"}`; header CSRF hợp lệ. Mã hộ và căn hộ chưa tồn tại.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** Tìm lại theo mã hộ: chỉ request ADMIN tạo bản ghi.
- **Nguồn:** RBAC-10, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### RBAC-11.1 — GET /api/v1/households/<HOUSEHOLD_ID>/residents (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có nhân khẩu.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-11, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`.

### RBAC-11.2 — GET /api/v1/households/<HOUSEHOLD_ID>/residents (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có nhân khẩu.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu nhân khẩu trong response với hộ kiểm thử.
- **Nguồn:** RBAC-11, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`.

### RBAC-12.1 — POST /api/v1/households/<HOUSEHOLD_ID>/residents (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nguyen Van B","relationshipToHead":"Con"}`; header CSRF hợp lệ. `<HOUSEHOLD_ID>` là hộ ACTIVE.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-12, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.

### RBAC-12.2 — POST /api/v1/households/<HOUSEHOLD_ID>/residents (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households/<HOUSEHOLD_ID>/residents`.
- **Dữ liệu vào:** Body `{"fullName":"Nguyen Van B","relationshipToHead":"Con"}`; header CSRF hợp lệ. `<HOUSEHOLD_ID>` là hộ ACTIVE.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/households/<HOUSEHOLD_ID>/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 201.
- **Kiểm sau:** GET danh sách nhân khẩu của hộ: chỉ ADMIN tạo nhân khẩu mới.
- **Nguồn:** RBAC-12, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.

### RBAC-13.1 — GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có lịch sử cư trú.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-13, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`.

### RBAC-13.2 — GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes`.
- **Dữ liệu vào:** Không có body. `<HOUSEHOLD_ID>` là hộ kiểm thử có lịch sử cư trú.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu lịch sử trong response với hộ kiểm thử.
- **Nguồn:** RBAC-13, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`.

### RBAC-14.1 — GET /api/v1/households/me/residents (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body. RESIDENT được gắn hộ có nhân khẩu kiểm thử.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/me/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-14, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`.

### RBAC-14.2 — GET /api/v1/households/me/residents (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Không có body. RESIDENT được gắn hộ có nhân khẩu kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/households/me/residents` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-14, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`.

### RBAC-15.1 — GET /api/v1/residents/<RESIDENT_ID> (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residents/<RESIDENT_ID>`.
- **Dữ liệu vào:** Không có body. `<RESIDENT_ID>` là nhân khẩu kiểm thử hiện có.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/<RESIDENT_ID>` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-15, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`.

### RBAC-15.2 — GET /api/v1/residents/<RESIDENT_ID> (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/<RESIDENT_ID>`.
- **Dữ liệu vào:** Không có body. `<RESIDENT_ID>` là nhân khẩu kiểm thử hiện có.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/<RESIDENT_ID>` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu ID trong response với nhân khẩu được chọn.
- **Nguồn:** RBAC-15, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`.

### RBAC-16.1 — POST /api/v1/residents/<RESIDENT_ID>/move-out (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyển đi kiểm thử"}`; header CSRF hợp lệ. `<RESIDENT_ID>` đang cư trú và đủ điều kiện chuyển đi.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/move-out` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-16, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`.

### RBAC-16.2 — POST /api/v1/residents/<RESIDENT_ID>/move-out (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/move-out`.
- **Dữ liệu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyển đi kiểm thử"}`; header CSRF hợp lệ. `<RESIDENT_ID>` đang cư trú và đủ điều kiện chuyển đi.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/move-out` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** GET lại nhân khẩu: trạng thái chuyển đi chỉ thay đổi sau request ADMIN.
- **Nguồn:** RBAC-16, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`.

### RBAC-17.1 — POST /api/v1/residents/<RESIDENT_ID>/return (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/return`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<RESIDENT_ID>` có đơn tạm vắng đã duyệt và đủ điều kiện trở về.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/return` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-17, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`.

### RBAC-17.2 — POST /api/v1/residents/<RESIDENT_ID>/return (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/<RESIDENT_ID>/return`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<RESIDENT_ID>` có đơn tạm vắng đã duyệt và đủ điều kiện trở về.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residents/<RESIDENT_ID>/return` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** GET lại nhân khẩu và đơn: trạng thái trở về chỉ thay đổi sau request ADMIN.
- **Nguồn:** RBAC-17, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`.

### RBAC-18.1 — GET /api/v1/residents/suggest?q=Nguyen (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=Nguyen`.
- **Dữ liệu vào:** Tham số `q=Nguyen`; không có body. Có nhân khẩu kiểm thử tên Nguyen.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/suggest?q=Nguyen` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-18, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

### RBAC-18.2 — GET /api/v1/residents/suggest?q=Nguyen (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=Nguyen`.
- **Dữ liệu vào:** Tham số `q=Nguyen`; không có body. Có nhân khẩu kiểm thử tên Nguyen.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residents/suggest?q=Nguyen` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách gợi ý với dữ liệu kiểm thử.
- **Nguồn:** RBAC-18, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

### RBAC-19.1 — POST /api/v1/residency-declarations (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY_VN>","endDate":null,"reason":"Đi công tác"}`; header CSRF hợp lệ. `<RESIDENT_ID>` thuộc hộ RESIDENT và đủ điều kiện khai báo; thay `<TODAY_VN>` bằng ngày hiện tại theo giờ Việt Nam.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-19, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`.

### RBAC-19.2 — POST /api/v1/residency-declarations (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Body `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY_VN>","endDate":null,"reason":"Đi công tác"}`; header CSRF hợp lệ. `<RESIDENT_ID>` thuộc hộ RESIDENT và đủ điều kiện khai báo; thay `<TODAY_VN>` bằng ngày hiện tại theo giờ Việt Nam.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-19, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`.

### RBAC-20.1 — GET /api/v1/residency-declarations/mine (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body. RESIDENT có một đơn kiểm thử do chính tài khoản tạo.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations/mine` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-20, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`.

### RBAC-20.2 — GET /api/v1/residency-declarations/mine (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Không có body. RESIDENT có một đơn kiểm thử do chính tài khoản tạo.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations/mine` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-20, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`.

### RBAC-21.1 — POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING do RESIDENT tạo.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-21, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`.

### RBAC-21.2 — POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING do RESIDENT tạo.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/cancel` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-21, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`.

### RBAC-22.1 — GET /api/v1/residency-declarations (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body. Có đơn tạm vắng kiểm thử trong hệ thống.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-22, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`.

### RBAC-22.2 — GET /api/v1/residency-declarations (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations`.
- **Dữ liệu vào:** Không có body. Có đơn tạm vắng kiểm thử trong hệ thống.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/residency-declarations` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách response với đơn kiểm thử.
- **Nguồn:** RBAC-22, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`.

### RBAC-23.1 — POST /api/v1/residency-declarations/<DECLARATION_ID>/approve (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING đủ điều kiện duyệt.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-23, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`.

### RBAC-23.2 — POST /api/v1/residency-declarations/<DECLARATION_ID>/approve (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve`.
- **Dữ liệu vào:** Không có body; header CSRF hợp lệ. `<DECLARATION_ID>` là đơn PENDING đủ điều kiện duyệt.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** GET danh sách đơn: chỉ request ADMIN chuyển đơn sang đã duyệt.
- **Nguồn:** RBAC-23, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`.

### RBAC-24.1 — GET /api/v1/fee-types (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body. Có loại phí kiểm thử.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/fee-types` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-24, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`.

### RBAC-24.2 — GET /api/v1/fee-types (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body. Có loại phí kiểm thử.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/fee-types` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 200.
- **Kiểm sau:** Đối chiếu danh sách response với loại phí đã cấu hình.
- **Nguồn:** RBAC-24, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`.

### RBAC-25.1 — POST /api/v1/fee-rates (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000,"effectiveFrom":"<FUTURE_MONTH_FIRST_DAY>"}`; header CSRF hợp lệ. Loại phí tồn tại; ngày là ngày đầu tháng sau và chưa có đơn giá cho tháng đó.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` cho phiên hiện tại.
  3. Gọi `POST /api/v1/fee-rates` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-25, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`.

### RBAC-25.2 — POST /api/v1/fee-rates (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000,"effectiveFrom":"<FUTURE_MONTH_FIRST_DAY>"}`; header CSRF hợp lệ. Loại phí tồn tại; ngày là ngày đầu tháng sau và chưa có đơn giá cho tháng đó.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Sau đăng nhập, gọi `GET /api/v1/auth/csrf`, lưu `<CSRF_TOKEN>` mới cho phiên.
  3. Gọi `POST /api/v1/fee-rates` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-25, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`.

### RBAC-26.1 — GET /api/v1/unknown-rbac-probe (ANON)

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/unknown-rbac-probe`.
- **Dữ liệu vào:** Không có body. Đường dẫn kiểm thử không nằm trong matcher đã khai báo.
- **Các bước:**
  1. Mở phiên mới chưa đăng nhập.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/unknown-rbac-probe` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 401. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-26, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.

### RBAC-26.2 — GET /api/v1/unknown-rbac-probe (ADMIN)

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/unknown-rbac-probe`.
- **Dữ liệu vào:** Không có body. Đường dẫn kiểm thử không nằm trong matcher đã khai báo.
- **Các bước:**
  1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`.
  2. Giữ phiên hiện tại cho request tiếp theo.
  3. Gọi `GET /api/v1/unknown-rbac-probe` với dữ liệu đã nêu và ghi response.
- **Kỳ vọng:** HTTP 403. Body ProblemDetail có `status` tương ứng; handler phân quyền không đặt thuộc tính `code`.
- **Kiểm sau:** Đối chiếu trạng thái dữ liệu trước và sau request: không có thay đổi do request bị từ chối.
- **Nguồn:** RBAC-26, outline mục 1; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.

## Điểm chưa xác định

- Body lỗi CSRF: chưa xác định có thuộc tính `code` hay không; HTTP 403 đã xác nhận trong outline.
- Mỗi hàng ma trận chọn một endpoint đại diện; các endpoint còn lại trong hàng ghép cần ca riêng nếu cần chứng minh toàn bộ method và đường dẫn.
