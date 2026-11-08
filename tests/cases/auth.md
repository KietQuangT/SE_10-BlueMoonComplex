# Ca kiểm thử auth

### AUTH-01.1 — Đăng nhập RESIDENT hợp lệ

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** RESIDENT ACTIVE đã đổi mật khẩu, thuộc `<HOUSEHOLD_ID>`; JSON `{"username":"<RESIDENT_USERNAME>","password":"<RESIDENT_PASSWORD>"}`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` và lưu token cùng cookie ẩn danh.
  2. Gửi JSON tới `POST /api/v1/auth/login` với `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  3. Gọi `GET /api/v1/auth/me` bằng phiên sau đăng nhập.
- **Kỳ vọng:** Hai response HTTP 200; `/auth/me` có `username`, `fullName`, `role=RESIDENT`, `householdId=<HOUSEHOLD_ID>`, `mustChangePassword=false`.
- **Kiểm sau:** So sánh session ID trước và sau đăng nhập: hai ID khác nhau; không lưu cookie vào bằng chứng.
- **Nguồn:** AUTH-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### AUTH-01.2 — Đăng nhập ADMIN hợp lệ

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** ADMIN ACTIVE; JSON `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` và lưu token.
  2. Gửi JSON tới `POST /api/v1/auth/login` với token.
  3. Gọi `GET /api/v1/auth/me` bằng phiên mới.
- **Kỳ vọng:** HTTP 200; `/auth/me` có `role=ADMIN`, `householdId=null`, `username`, `fullName`, `mustChangePassword`.
- **Kiểm sau:** Gọi lại `/api/v1/auth/me` bằng phiên mới: HTTP 200.
- **Nguồn:** AUTH-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### AUTH-02.1 — Từ chối mật khẩu sai

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** RESIDENT ACTIVE chưa bị khóa; JSON `{"username":"<RESIDENT_USERNAME>","password":"<WRONG_PASSWORD>"}`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` và lưu token.
  2. Gửi JSON tới `POST /api/v1/auth/login` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 401; ProblemDetail có `detail=Tên đăng nhập hoặc mật khẩu không đúng.`.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` bằng phiên đó: HTTP 401.
- **Nguồn:** AUTH-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### AUTH-02.2 — Từ chối tên đăng nhập không tồn tại

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** JSON `{"username":"<UNKNOWN_USERNAME>","password":"<WRONG_PASSWORD>"}`; username không có trong DB.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` và lưu token.
  2. Gửi JSON tới `POST /api/v1/auth/login` với token.
  3. So sánh body với ca mật khẩu sai.
- **Kỳ vọng:** HTTP 401; ProblemDetail có `detail=Tên đăng nhập hoặc mật khẩu không đúng.` như ca mật khẩu sai.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` bằng phiên đó: HTTP 401.
- **Nguồn:** AUTH-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### AUTH-03.1 — Lần sai thứ năm khóa một phút

- **Vai trò:** RESIDENT (chưa đăng nhập).
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Tài khoản ACTIVE chưa từng sai, `lock_level=0`; mật khẩu sai `<WRONG_PASSWORD>`. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi đăng nhập sai 4 lần; xác nhận mỗi lần trả 401.
  2. Gửi lần sai thứ 5 và ghi thời điểm gửi.
  3. Đối chiếu HTTP và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 401; tài khoản bị khóa 1 phút, `lock_level=1`.
- **Kiểm sau:** Đọc `locked_until` và audit `ACCOUNT_LOCKED`; thời hạn lớn hơn thời điểm gửi khoảng một phút.
- **Nguồn:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.


### AUTH-03.2 — Mật khẩu đúng bị từ chối trong thời gian khóa

- **Vai trò:** RESIDENT (chưa đăng nhập).
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Tài khoản đang ở bậc khóa 1; mật khẩu đúng `<RESIDENT_PASSWORD>`. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đọc số lần sai và bậc khóa trước request.
  2. Gửi đăng nhập với mật khẩu đúng trước `locked_until`.
  3. Đối chiếu HTTP và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 401 với body lỗi đăng nhập như khi sai mật khẩu.
- **Kiểm sau:** Đọc lại số lần sai và bậc khóa; cả hai không tăng.
- **Nguồn:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.


### AUTH-03.3 — Sai sau khi hết khóa tăng lên bậc hai

- **Vai trò:** RESIDENT (chưa đăng nhập).
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Tài khoản đã khóa bậc 1 và không đăng nhập thành công sau khi hết hạn. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đợi tới sau `locked_until` của bậc 1.
  2. Gửi một lần đăng nhập với `<WRONG_PASSWORD>`.
  3. Đối chiếu HTTP và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 401; khóa bậc 2 trong 5 phút.
- **Kiểm sau:** Đọc `locked_until`, `lock_level=2` và audit `ACCOUNT_LOCKED`.
- **Nguồn:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.


### AUTH-03.4 — Đăng nhập đúng sau hết khóa đặt lại bộ đếm

- **Vai trò:** RESIDENT (chưa đăng nhập).
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** Tài khoản đã hết hạn khóa bậc 1; mật khẩu đúng `<RESIDENT_PASSWORD>`. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đợi tới sau `locked_until`.
  2. Gửi đăng nhập bằng mật khẩu đúng.
  3. Đối chiếu HTTP và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; response có `username`, `role` và `mustChangePassword`.
- **Kiểm sau:** Đọc bộ đếm và bậc khóa của tài khoản: đều trở về 0.
- **Nguồn:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.


### AUTH-03.5 — ADMIN không bị khóa tự động

- **Vai trò:** ADMIN (chưa đăng nhập).
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** ADMIN ACTIVE; dùng `<ADMIN_USERNAME>` và `<WRONG_PASSWORD>`. Body `{"username":"<ADMIN_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi đăng nhập sai 5 lần.
  2. Gửi thêm một lần đăng nhập với `<ADMIN_PASSWORD>`.
  3. Đối chiếu HTTP và thực hiện kiểm sau.
- **Kỳ vọng:** Năm lần sai đều HTTP 401; lần đúng HTTP 200.
- **Kiểm sau:** Đọc tài khoản ADMIN: không có thời hạn khóa được tạo do các lần sai.
- **Nguồn:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.


### AUTH-04.1 — ADMIN mở khóa tài khoản bị khóa

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/{id}/unlock`.
- **Dữ liệu vào:** `<TARGET_ID>` là RESIDENT đang khóa sau 5 lần sai.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi `POST /api/v1/accounts/<TARGET_ID>/unlock` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** SQL: `locked_until` null, `failed_login_count=0`, `lock_level=0`; có audit `ACCOUNT_UNLOCKED`.
- **Nguồn:** AUTH-04; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.

### AUTH-04.2 — Không mở khóa ADMIN

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/accounts/{id}/unlock`.
- **Dữ liệu vào:** `<TARGET_ADMIN_ID>` là tài khoản ADMIN.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi `POST /api/v1/accounts/<TARGET_ADMIN_ID>/unlock` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 409; `code=ACCOUNT_NOT_LOCKED`.
- **Kiểm sau:** SQL: trạng thái khóa của ADMIN không thay đổi.
- **Nguồn:** AUTH-04; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.

### AUTH-05.1 — Đăng xuất phiên đang hoạt động

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/logout`.
- **Dữ liệu vào:** Phiên RESIDENT đã đăng nhập và token CSRF hiện tại.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` bằng phiên RESIDENT.
  2. Gửi `POST /api/v1/auth/logout` với token.
  3. Gọi `GET /api/v1/auth/me` bằng phiên cũ.
- **Kỳ vọng:** Logout HTTP 204; `/auth/me` HTTP 401.
- **Kiểm sau:** SQL bảng `SPRING_SESSION`: phiên đã đăng xuất không còn hợp lệ.
- **Nguồn:** AUTH-05; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### AUTH-05.2 — Đăng xuất khi chưa đăng nhập

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/logout`.
- **Dữ liệu vào:** Phiên ẩn danh có token CSRF hợp lệ.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` để lấy token.
  2. Gửi `POST /api/v1/auth/logout` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 401.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me`: HTTP 401.
- **Nguồn:** AUTH-05; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.

### AUTH-06.1 — Cookie phiên có thuộc tính bảo vệ

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** ADMIN ACTIVE; JSON `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`.
- **Các bước:**
  1. Lấy token và cookie bằng `GET /api/v1/auth/csrf`.
  2. Gửi POST login với JSON và token.
  3. Kiểm tra thuộc tính cookie `SESSION` trong công cụ trình duyệt.
- **Kỳ vọng:** HTTP 200; cookie `SESSION` có `HttpOnly` và `SameSite=Lax`.
- **Kiểm sau:** SQL bảng `SPRING_SESSION` có phiên đăng nhập; không ghi giá trị cookie vào bằng chứng.
- **Nguồn:** AUTH-06; `backend/src/main/resources/application.yml:17`.

### AUTH-06.2 — Phiên hết hạn sau 30 phút không hoạt động

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/auth/me`.
- **Dữ liệu vào:** Phiên RESIDENT hoạt động; không gửi request từ phiên này trong 31 phút.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/me` và xác nhận HTTP 200.
  2. Đợi 31 phút không dùng phiên.
  3. Gọi lại `GET /api/v1/auth/me` bằng phiên cũ.
- **Kỳ vọng:** Request sau 31 phút trả HTTP 401.
- **Kiểm sau:** Đăng nhập lại bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`: HTTP 200.
- **Nguồn:** AUTH-06; `backend/src/main/resources/application.yml:21`.

### AUTH-07.1 — Thiếu CSRF bị từ chối

- **Vai trò:** ANON.
- **Điểm vào:** `POST /api/v1/auth/login`.
- **Dữ liệu vào:** JSON `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; không gửi header CSRF.
- **Các bước:**
  1. Tạo phiên ẩn danh mới.
  2. Gửi POST `/api/v1/auth/login` với JSON và không có header CSRF.
  3. Đọc response.
- **Kỳ vọng:** HTTP 403; code cụ thể chưa xác định.
- **Kiểm sau:** Gọi `/api/v1/auth/me` trong phiên này: HTTP 401.
- **Nguồn:** AUTH-07; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.

### AUTH-07.2 — Lấy token CSRF khi chưa đăng nhập

- **Vai trò:** ANON.
- **Điểm vào:** `GET /api/v1/auth/csrf`.
- **Dữ liệu vào:** Phiên trình duyệt ẩn danh mới.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf`.
  2. Đọc tên header và token trong response.
  3. Giữ token trong bộ nhớ cho request tiếp theo.
- **Kỳ vọng:** HTTP 200; JSON có `headerName` và `token`.
- **Kiểm sau:** Dùng token trong POST đăng nhập hợp lệ; không lưu token vào bằng chứng.
- **Nguồn:** AUTH-07; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.

### AUTH-08.1 — Chặn endpoint khi phải đổi mật khẩu

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Tài khoản RESIDENT mới cấp có `mustChangePassword=true` và đã đăng nhập.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/me` bằng phiên mới cấp.
  2. Gọi `GET /api/v1/households/me/residents`.
  3. Đọc response.
- **Kỳ vọng:** `/auth/me` HTTP 200 và `mustChangePassword=true`; endpoint hộ HTTP 403, `code=PASSWORD_CHANGE_REQUIRED`.
- **Kiểm sau:** Gọi lại `/api/v1/auth/me`: tài khoản vẫn yêu cầu đổi mật khẩu.
- **Nguồn:** AUTH-08; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:19`.

### AUTH-08.2 — Chuyển giao diện về đổi mật khẩu

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/household`.
- **Dữ liệu vào:** Tài khoản mới cấp đã đăng nhập và `mustChangePassword=true`.
- **Các bước:**
  1. Mở `/resident/household` trong phiên tài khoản mới.
  2. Quan sát URL sau chuyển hướng.
  3. Mở trực tiếp `/profile?tab=password`.
- **Kỳ vọng:** Giao diện chuyển tới `/profile?tab=password`; trang hộ bị chặn.
- **Kiểm sau:** Kiểm `GET /api/v1/auth/me` vẫn có `mustChangePassword=true`.
- **Nguồn:** AUTH-08; `frontend/src/App.tsx:58`.

Tiền điều kiện AUTH-09: RESIDENT thử nghiệm ACTIVE, chưa khóa; `<NEW_VALID_PASSWORD>` có tối thiểu 8 ký tự, tối đa 72 byte UTF-8, gồm chữ hoa, chữ thường, số, ký tự đặc biệt và khác mật khẩu hiện tại.

### AUTH-09.1 — Mật khẩu hiện tại sai

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** `{"currentPassword":"<WRONG_PASSWORD>","newPassword":"<NEW_VALID_PASSWORD>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
  2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
  3. Đối chiếu HTTP, body và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; `code=CURRENT_PASSWORD_INCORRECT`.
- **Kiểm sau:** Kiểm `failed_login_count` tăng 1 và hash mật khẩu không đổi.
- **Nguồn:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.


### AUTH-09.2 — Mật khẩu mới ngắn hơn 8 ký tự

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<SHORT_PASSWORD: 7 ký tự>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
  2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
  3. Đối chiếu HTTP, body và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; `code=PASSWORD_TOO_SHORT`.
- **Kiểm sau:** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
- **Nguồn:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.


### AUTH-09.3 — Mật khẩu mới dài hơn 72 byte UTF-8

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<LONG_PASSWORD: 73 byte UTF-8>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
  2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
  3. Đối chiếu HTTP, body và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; `code=PASSWORD_TOO_LONG`.
- **Kiểm sau:** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
- **Nguồn:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.


### AUTH-09.4 — Mật khẩu mới thiếu ký tự đặc biệt

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<WEAK_PASSWORD: có chữ hoa, chữ thường, số; không có ký tự đặc biệt>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
  2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
  3. Đối chiếu HTTP, body và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; `code=PASSWORD_WEAK`.
- **Kiểm sau:** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
- **Nguồn:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.


### AUTH-09.5 — Mật khẩu mới trùng mật khẩu hiện tại

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<CURRENT_PASSWORD>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
  2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
  3. Đối chiếu HTTP, body và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; `code=PASSWORD_SAME_AS_CURRENT`.
- **Kiểm sau:** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
- **Nguồn:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.


### AUTH-09.6 — Đổi mật khẩu hợp lệ hủy mọi phiên

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/auth/change-password`.
- **Dữ liệu vào:** Hai phiên độc lập của cùng RESIDENT; `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_VALID_PASSWORD>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Đăng nhập cùng RESIDENT trong hai trình duyệt độc lập.
  2. Gọi `GET /api/v1/auth/csrf` trong phiên thứ nhất và lấy token.
  3. Gửi body tới `POST /api/v1/auth/change-password` trong phiên thứ nhất cùng token.
- **Kỳ vọng:** HTTP 204, không có body.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` từ cả hai phiên cũ: mỗi phiên trả 401; đăng nhập bằng mật khẩu mới thành công. Kiểm audit `PASSWORD_CHANGED` và `SESSIONS_REVOKED`.
- **Nguồn:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.


### AUTH-10.1 — Bootstrap tạo ADMIN đầu tiên

- **Vai trò:** ANON.
- **Điểm vào:** `java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap` (console; không có endpoint hoặc route).
- **Dữ liệu vào:** DB thử nghiệm chưa có ADMIN; nhập `<ADMIN_USERNAME>`, tên `Test Admin`, `<TEST_EMAIL>`, `<ADMIN_PASSWORD>` và xác nhận mật khẩu tại console tương tác.
- **Các bước:**
  1. Chạy lệnh bootstrap trên DB thử nghiệm.
  2. Nhập thông tin ADMIN theo prompt mà không ghi mật khẩu.
  3. Đọc kết quả console đã che dữ liệu.
- **Kỳ vọng:** Tạo một ADMIN; mã thoát và thông báo chính xác chưa xác định.
- **Kiểm sau:** SQL: có một ADMIN mới; có audit bootstrap; không lưu mật khẩu hoặc email vào bằng chứng.
- **Nguồn:** AUTH-10; `backend/src/main/java/vn/edu/se10/bluemoon/identity/bootstrap/BootstrapAdminRunner.java:10`.

### AUTH-10.2 — Bootstrap bỏ qua khi đã có ADMIN

- **Vai trò:** ANON.
- **Điểm vào:** `java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap` (console; không có endpoint hoặc route).
- **Dữ liệu vào:** DB thử nghiệm đã có ADMIN; nhập `<ADMIN_USERNAME>`, tên `Test Admin`, `<TEST_EMAIL>`, `<ADMIN_PASSWORD>` và xác nhận mật khẩu khi được hỏi.
- **Các bước:**
  1. Đếm số ADMIN hiện có bằng SQL.
  2. Chạy lệnh bootstrap trong console tương tác.
  3. Nhập các giá trị thử nghiệm tại prompt và đọc kết quả console.
- **Kỳ vọng:** Không tạo ADMIN mới; mã thoát và thông báo chính xác chưa xác định.
- **Kiểm sau:** Đếm lại ADMIN bằng SQL: số lượng không tăng.
- **Nguồn:** AUTH-10; `backend/src/main/java/vn/edu/se10/bluemoon/identity/bootstrap/BootstrapAdminRunner.java:10`.

### AUTH-11.1 — Xem hồ sơ cá nhân

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/profile`.
- **Dữ liệu vào:** Phiên RESIDENT đã đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập RESIDENT.
  2. Mở `/profile`.
  3. Đối chiếu thông tin hiển thị với `GET /api/v1/auth/me`.
- **Kỳ vọng:** Trang hiển thị thông tin tài khoản; không có thao tác tự sửa hồ sơ; nhãn chính xác chưa xác định.
- **Kiểm sau:** GET `/api/v1/auth/me`: username, fullName và role khớp hồ sơ.
- **Nguồn:** AUTH-11; `frontend/src/App.tsx:104`.

### AUTH-11.2 — Đổi mật khẩu từ hồ sơ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/profile?tab=password`.
- **Dữ liệu vào:** Phiên RESIDENT; `<CURRENT_PASSWORD>` đúng và `<NEW_VALID_PASSWORD>` thỏa quy tắc.
- **Các bước:**
  1. Mở `/profile?tab=password`.
  2. Nhập mật khẩu hiện tại và mật khẩu mới bằng placeholder tương ứng.
  3. Gửi biểu mẫu đổi mật khẩu.
- **Kỳ vọng:** Request `POST /api/v1/auth/change-password` HTTP 204; phiên bị hủy.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` với phiên cũ: HTTP 401; đăng nhập bằng mật khẩu mới.
- **Nguồn:** AUTH-11; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.

## Điểm chưa xác định

AUTH-07.1: code lỗi CSRF chưa xác định; AUTH-10.1–10.2: mã thoát và thông báo bootstrap chưa xác định; AUTH-11.1: nhãn giao diện chính xác chưa xác định.
