# Ca kiểm thử hệ thống

### SYS-01.1 — Health công khai

- **Vai trò:** ANON.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Không gửi cookie.
- **Các bước:**
  1. Gửi GET /actuator/health.
  2. Đọc HTTP và JSON.
  3. Kiểm tra các thuộc tính trả về.
- **Kỳ vọng:** HTTP 200; JSON có `status`, không chứa thông tin kết nối hoặc credential.
- **Kiểm sau:** Gọi lại health; status có giá trị.
- **Nguồn:** SYS-01; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### SYS-01.2 — Health không lộ chi tiết cho ADMIN

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /actuator/health`.
- **Dữ liệu vào:** Phiên ADMIN hợp lệ.
- **Các bước:**
  1. Gửi GET /actuator/health cùng phiên ADMIN.
  2. Đọc HTTP và JSON.
  3. Tìm các trường thành phần.
- **Kỳ vọng:** HTTP 200; JSON có `status`, không lộ chi tiết nhạy cảm.
- **Kiểm sau:** Đối chiếu JSON với phản hồi ANON.
- **Nguồn:** SYS-01; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.

### SYS-02.1 — Metadata công khai

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không gửi cookie.
- **Các bước:**
  1. Gửi GET /api/v1/system/info.
  2. Đọc HTTP và JSON.
  3. Đối chiếu hai trường.
- **Kỳ vọng:** HTTP 200; JSON `{"name":"BlueMoon","stage":"skeleton"}`.
- **Kiểm sau:** Đọc lại; hai giá trị không thay đổi.
- **Nguồn:** SYS-02; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/application/SystemInfoService.java:8`.

### SYS-02.2 — Metadata cho phiên RESIDENT

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Phiên RESIDENT hợp lệ.
- **Các bước:**
  1. Gửi GET /api/v1/system/info.
  2. Đọc JSON.
  3. Tìm thông tin cá nhân.
- **Kỳ vọng:** HTTP 200; `name=BlueMoon`, `stage=skeleton`; không có thông tin tài khoản.
- **Kiểm sau:** So sánh với phản hồi ANON.
- **Nguồn:** SYS-02; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.

### SYS-03.1 — Request ID cho phản hồi thành công

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/system/info`.
- **Dữ liệu vào:** Không gửi cookie.
- **Các bước:**
  1. Gửi GET /api/v1/system/info.
  2. Ghi header `X-Request-Id`.
  3. Gửi lại cùng request.
- **Kỳ vọng:** HTTP 200 mỗi lần; mỗi phản hồi có `X-Request-Id` dạng UUID và hai ID khác nhau.
- **Kiểm sau:** Tìm hai ID trong log ứng dụng; không ghi nội dung nhạy cảm.
- **Nguồn:** SYS-03; `backend/src/main/java/vn/edu/se10/bluemoon/common/web/RequestIdFilter.java:22`.

### SYS-03.2 — Request ID cho phản hồi bị từ chối

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/accounts`.
- **Dữ liệu vào:** Không gửi cookie.
- **Các bước:**
  1. Gửi GET /api/v1/accounts.
  2. Ghi HTTP và header `X-Request-Id`.
  3. Tìm request ID trong log.
- **Kỳ vọng:** HTTP 401; header `X-Request-Id` có UUID.
- **Kiểm sau:** Kiểm log không chứa cookie, Authorization, email hoặc body.
- **Nguồn:** SYS-03; `backend/src/main/java/vn/edu/se10/bluemoon/common/web/RequestIdFilter.java:24`.

### SYS-04.1 — Từ chối endpoint lạ khi chưa đăng nhập

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/unknown-test`.
- **Dữ liệu vào:** Không gửi cookie.
- **Các bước:**
  1. Gửi GET /api/v1/unknown-test.
  2. Đọc HTTP.
  3. Đọc `title` trong JSON.
- **Kỳ vọng:** HTTP 401; `title` là “Chưa xác thực”.
- **Kiểm sau:** Gọi lại endpoint và xác nhận vẫn 401.
- **Nguồn:** SYS-04; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.

### SYS-04.2 — Từ chối endpoint lạ khi đã đăng nhập

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/unknown-test`.
- **Dữ liệu vào:** Phiên ADMIN hợp lệ.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gửi GET /api/v1/unknown-test.
  3. Đọc HTTP và JSON.
- **Kỳ vọng:** HTTP 403; `title` là “Không có quyền truy cập”.
- **Kiểm sau:** Gọi lại endpoint và xác nhận vẫn 403.
- **Nguồn:** SYS-04; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:71`.

### SYS-05.1 — Trang lỗi 403 trực tiếp

- **Vai trò:** ANON.
- **Điểm vào:** `/error/403`.
- **Dữ liệu vào:** Trình duyệt chưa đăng nhập.
- **Các bước:**
  1. Mở `/error/403`.
  2. Đọc tiêu đề.
  3. Quan sát nút hành động.
- **Kỳ vọng:** Hiển thị “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
- **Kiểm sau:** Bấm nút; chuyển tới `/login`.
- **Nguồn:** SYS-05; `frontend/src/screens/common/ErrorPage.tsx:15`.

### SYS-05.2 — Trang không tồn tại

- **Vai trò:** ANON.
- **Điểm vào:** `/duong-dan-khong-ton-tai`.
- **Dữ liệu vào:** Trình duyệt chưa đăng nhập.
- **Các bước:**
  1. Mở `/login`.
  2. Nhập `/duong-dan-khong-ton-tai`.
  3. Đọc trang lỗi.
- **Kỳ vọng:** Hiển thị “Không tìm thấy.” và nút “Quay lại”.
- **Kiểm sau:** Bấm “Quay lại”; trình duyệt về `/login`.
- **Nguồn:** SYS-05; `frontend/src/App.tsx:153`.

## Điểm chưa xác định

- HTTP status cho tài liệu SPA phụ thuộc cấu hình máy chủ; SYS-05 kiểm nội dung trên trình duyệt.
