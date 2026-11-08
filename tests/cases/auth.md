# Ca kiểm thử AUTH

Tiền tố `/api/v1` áp dụng cho endpoint API viết rút gọn; thời gian nghiệp vụ dùng Asia/Ho_Chi_Minh.

## AUTH-01 — Đăng nhập thành công.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-01.1:** `POST /auth/login`, màn `/login`. Sau đăng nhập đổi session ID (session fixation).
- **AUTH-01.2:** `GET /auth/me` trả `username`, `fullName`, `role`, `householdId` (null với ADMIN/ACCOUNTANT), `mustChangePassword` (`identity/api/SessionUserResponse.java:5`).

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-02 — Đăng nhập thất bại.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-02.1:** Mọi lỗi (không tồn tại, sai mật khẩu, khóa, vô hiệu) trả cùng một body `401`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-03 — Khóa theo tài khoản

Tiền điều kiện chung: dùng tài khoản giả, các ca độc lập; lấy CSRF từ `GET /api/v1/auth/csrf` trước khi gửi POST. Không ghi mật khẩu, token hoặc cookie vào bằng chứng.

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

## AUTH-04 — Mở khóa thủ công.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-04.1:** `POST /accounts/{id}/unlock` đặt lại bộ đếm và bậc khóa; 409 khi không còn gì để đặt lại hoặc tài khoản là ADMIN.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-05 — Đăng xuất.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-05.1:** `POST /auth/logout`; khi chưa đăng nhập trả `401`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-06 — Hết hạn phiên.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-06.1:** Phiên lưu trong MySQL, cookie `SESSION` `HttpOnly`, `SameSite=Lax`, hết hạn sau 30 phút.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-07 — CSRF.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-07.1:** Ghi không có token hoặc token sai trả `403`.
- **AUTH-07.2:** `GET /auth/csrf` là public.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-08 — Bắt buộc đổi mật khẩu lần đầu.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-08.1:** Tài khoản do ADMIN cấp có `must_change_password = true`; mọi route ngoài danh sách trả `403 PASSWORD_CHANGE_REQUIRED`; frontend chuyển người dùng về `/profile?tab=password` và chặn mọi trang khác (`App.tsx:58-62, 82`); đổi xong phải đăng nhập lại.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-09 — Đổi mật khẩu

Tiền điều kiện: dùng tài khoản RESIDENT thử nghiệm đang hoạt động, không bị khóa; có phiên đăng nhập và CSRF hợp lệ. Mật khẩu thay thế hợp lệ có ít nhất 8 ký tự, tối đa 72 byte UTF-8, đủ chữ hoa, chữ thường, số, ký tự đặc biệt và khác mật khẩu hiện tại. Riêng ca .6 mở hai phiên độc lập của cùng tài khoản. Không lưu giá trị mật khẩu hoặc token trong bằng chứng.

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
- **Dữ liệu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_VALID_PASSWORD>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
  2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
  3. Đối chiếu HTTP, body và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 204, không có body.
- **Kiểm sau:** Gọi `GET /api/v1/auth/me` từ cả hai phiên cũ: mỗi phiên trả 401; đăng nhập bằng mật khẩu mới thành công. Kiểm audit `PASSWORD_CHANGED` và `SESSIONS_REVOKED`.
- **Nguồn:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.

## AUTH-10 — Bootstrap ADMIN.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-10.1:** Chạy bằng profile `bootstrap` và console tương tác; không có tài khoản mặc định; nếu đã có ADMIN thì bỏ qua.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## AUTH-11 — Hồ sơ cá nhân.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-11.1:** `/profile` chỉ xem; ADMIN sửa `full_name` (D23 Q6).
- **AUTH-11.2:** Từ hồ sơ gửi đổi mật khẩu hợp lệ: request đổi mật khẩu trả 204 và phiên bị hủy.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## Điểm chưa xác định

Các nhánh không nêu mã HTTP hoặc nội dung chính xác trong outline: ghi thực tế và đối chiếu mã nguồn trước khi kết luận; không tự suy đoán mã.
