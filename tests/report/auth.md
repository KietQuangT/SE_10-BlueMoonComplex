# Báo cáo kiểm thử chi tiết: Xác thực, phiên, CSRF, khóa tạm, mật khẩu và bootstrap ADMIN

- **Tệp đặc tả nguồn:** [`tests/cases/auth.md`](../cases/auth.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra đăng nhập phiên JDBC HTTP-only, chống cố định phiên (session fixation), đăng xuất hủy phiên, bảo vệ CSRF, chuẩn hóa email + chống liệt kê tài khoản, khóa tạm sau 5 lần sai và mở khóa tự động sau 1 phút, buộc đổi mật khẩu tạm lần đầu, hủy toàn bộ phiên khác khi đổi mật khẩu, và quy trình bootstrap ADMIN qua console.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **29** |
| Số ca Đạt (Pass) | **29** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- `AUTH-07.1`: Thiếu hoặc sai CSRF token trả HTTP `403` dạng `ProblemDetail` `{"type":"about:blank","title":"Không có quyền truy cập","status":403}` (không kèm trường `code` riêng).
- `AUTH-10.1` & `AUTH-10.2`: Cả hai trường hợp `--app.bootstrap.admin=true` (tạo mới thành công và bỏ qua khi đã tồn tại ADMIN) đều kết thúc tiến trình với mã thoát `exitCode = 0`; thông báo console lần lượt là `"Đã tạo ADMIN."` và `"Đã có ADMIN, bỏ qua."`, mật khẩu nhập qua `readPassword` không bị in lại (echo) ra màn hình.
- `AUTH-11.1`: Trên màn hình `/profile` (tab `Hồ sơ`), các nhãn `<dt>` chính xác gồm `["Họ tên", "Vai trò", "Tên đăng nhập"]` (và thêm `"Căn hộ"` khi dữ liệu hộ đã nạp trong ngữ cảnh phiên), hoàn toàn chỉ đọc (0 ô nhập/nút sửa).

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `AUTH-01.1` | Đăng nhập RESIDENT hợp lệ | ANON | ✅ Đạt (Pass) | — |
| `AUTH-01.2` | Đăng nhập ADMIN hợp lệ | ANON | ✅ Đạt (Pass) | — |
| `AUTH-02.1` | Từ chối mật khẩu sai | ANON | ✅ Đạt (Pass) | — |
| `AUTH-02.2` | Từ chối tên đăng nhập không tồn tại | ANON | ✅ Đạt (Pass) | — |
| `AUTH-03.1` | Lần sai thứ năm khóa một phút | RESIDENT (chưa đăng nhập) | ✅ Đạt (Pass) | — |
| `AUTH-03.2` | Mật khẩu đúng bị từ chối trong thời gian khóa | RESIDENT (chưa đăng nhập) | ✅ Đạt (Pass) | — |
| `AUTH-03.3` | Sai sau khi hết khóa tăng lên bậc hai | RESIDENT (chưa đăng nhập) | ✅ Đạt (Pass) | — |
| `AUTH-03.4` | Đăng nhập đúng sau hết khóa đặt lại bộ đếm | RESIDENT (chưa đăng nhập) | ✅ Đạt (Pass) | — |
| `AUTH-03.5` | ADMIN không bị khóa tự động | ADMIN (chưa đăng nhập) | ✅ Đạt (Pass) | — |
| `AUTH-04.1` | ADMIN mở khóa tài khoản bị khóa | ADMIN | ✅ Đạt (Pass) | — |
| `AUTH-04.2` | Không mở khóa ADMIN | ADMIN | ✅ Đạt (Pass) | — |
| `AUTH-05.1` | Đăng xuất phiên đang hoạt động | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-05.2` | Đăng xuất khi chưa đăng nhập | ANON | ✅ Đạt (Pass) | — |
| `AUTH-06.1` | Cookie phiên có thuộc tính bảo vệ | ANON | ✅ Đạt (Pass) | Trong profile local (HTTP), application-local.yml đặt secure=false để trình duyệt gửi cook… |
| `AUTH-06.2` | Phiên hết hạn sau 30 phút không hoạt động | RESIDENT | ✅ Đạt (Pass) | Xác minh timeout 1800s (30m) và mô phỏng 31 phút không hoạt động bằng cách dịch lùi LAST_A… |
| `AUTH-07.1` | Thiếu CSRF bị từ chối | ANON | ✅ Đạt (Pass) | Điểm chưa xác định: Lỗi thiếu CSRF trả ProblemDetail {"type":"about:blank","title":"Không … |
| `AUTH-07.2` | Lấy token CSRF khi chưa đăng nhập | ANON | ✅ Đạt (Pass) | — |
| `AUTH-08.1` | Chặn endpoint khi phải đổi mật khẩu | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-08.2` | Chuyển giao diện về đổi mật khẩu | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-09.1` | Mật khẩu hiện tại sai | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-09.2` | Mật khẩu mới ngắn hơn 8 ký tự | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-09.3` | Mật khẩu mới dài hơn 72 byte UTF-8 | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-09.4` | Mật khẩu mới thiếu ký tự đặc biệt | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-09.5` | Mật khẩu mới trùng mật khẩu hiện tại | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-09.6` | Đổi mật khẩu hợp lệ hủy mọi phiên | RESIDENT | ✅ Đạt (Pass) | — |
| `AUTH-10.1` | Bootstrap tạo ADMIN đầu tiên | ANON | ✅ Đạt (Pass) | Điểm chưa xác định: Mã thoát là 0, thông báo console là "Đã tạo ADMIN.". |
| `AUTH-10.2` | Bootstrap bỏ qua khi đã có ADMIN | ANON | ✅ Đạt (Pass) | Điểm chưa xác định: Mã thoát là 0, thông báo console là "Đã có ADMIN, bỏ qua.". |
| `AUTH-11.1` | Xem hồ sơ cá nhân | RESIDENT | ✅ Đạt (Pass) | Điểm chưa xác định: Nhãn giao diện chính xác trên tab Hồ sơ là ["Họ tên","Vai trò","Tên đă… |
| `AUTH-11.2` | Đổi mật khẩu từ hồ sơ | RESIDENT | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### AUTH-01.1 — Đăng nhập RESIDENT hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** RESIDENT ACTIVE đã đổi mật khẩu, thuộc `<HOUSEHOLD_ID>`; JSON `{"username":"<RESIDENT_USERNAME>","password":"<RESIDENT_PASSWORD>"}`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` và lưu token cùng cookie ẩn danh.
    2. Gửi JSON tới `POST /api/v1/auth/login` với `X-CSRF-TOKEN: <CSRF_TOKEN>`.
    3. Gọi `GET /api/v1/auth/me` bằng phiên sau đăng nhập.
  - **Kết quả mong đợi:** Hai response HTTP 200; `/auth/me` có `username`, `fullName`, `role=RESIDENT`, `householdId=<HOUSEHOLD_ID>`, `mustChangePassword=false`.
  - **Kiểm tra sau (Post-check):** So sánh session ID trước và sau đăng nhập: hai ID khác nhau; không lưu cookie vào bằng chứng.
  - **Nguồn đối chiếu:** AUTH-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** login HTTP 200, /auth/me HTTP 200; role=RESIDENT, householdId=1, mustChangePassword=false; sessionRotated=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-01.2 — Đăng nhập ADMIN hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** ADMIN ACTIVE; JSON `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` và lưu token.
    2. Gửi JSON tới `POST /api/v1/auth/login` với token.
    3. Gọi `GET /api/v1/auth/me` bằng phiên mới.
  - **Kết quả mong đợi:** HTTP 200; `/auth/me` có `role=ADMIN`, `householdId=null`, `username`, `fullName`, `mustChangePassword`.
  - **Kiểm tra sau (Post-check):** Gọi lại `/api/v1/auth/me` bằng phiên mới: HTTP 200.
  - **Nguồn đối chiếu:** AUTH-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** login HTTP 200; /auth/me HTTP 200 (role=ADMIN, householdId=null); lần 2 HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-02.1 — Từ chối mật khẩu sai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** RESIDENT ACTIVE chưa bị khóa; JSON `{"username":"<RESIDENT_USERNAME>","password":"<WRONG_PASSWORD>"}`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` và lưu token.
    2. Gửi JSON tới `POST /api/v1/auth/login` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 401; ProblemDetail có `detail=Tên đăng nhập hoặc mật khẩu không đúng.`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` bằng phiên đó: HTTP 401.
  - **Nguồn đối chiếu:** AUTH-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; detail="Tên đăng nhập hoặc mật khẩu không đúng."; /auth/me=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-02.2 — Từ chối tên đăng nhập không tồn tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** JSON `{"username":"<UNKNOWN_USERNAME>","password":"<WRONG_PASSWORD>"}`; username không có trong DB.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` và lưu token.
    2. Gửi JSON tới `POST /api/v1/auth/login` với token.
    3. So sánh body với ca mật khẩu sai.
  - **Kết quả mong đợi:** HTTP 401; ProblemDetail có `detail=Tên đăng nhập hoặc mật khẩu không đúng.` như ca mật khẩu sai.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` bằng phiên đó: HTTP 401.
  - **Nguồn đối chiếu:** AUTH-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; detail="Tên đăng nhập hoặc mật khẩu không đúng." (khớp AUTH-02.1=true); /auth/me=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-03.1 — Lần sai thứ năm khóa một phút

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT (chưa đăng nhập).
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Tài khoản ACTIVE chưa từng sai, `lock_level=0`; mật khẩu sai `<WRONG_PASSWORD>`. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi đăng nhập sai 4 lần; xác nhận mỗi lần trả 401.
    2. Gửi lần sai thứ 5 và ghi thời điểm gửi.
    3. Đối chiếu HTTP và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 401; tài khoản bị khóa 1 phút, `lock_level=1`.
  - **Kiểm tra sau (Post-check):** Đọc `locked_until` và audit `ACCOUNT_LOCKED`; thời hạn lớn hơn thời điểm gửi khoảng một phút.
  - **Nguồn đối chiếu:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 4 lần đầu=[401,401,401,401], lần 5 HTTP 401; lock_level=1, còn lại ~59s (~1 phút), audit ACCOUNT_LOCKED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-03.2 — Mật khẩu đúng bị từ chối trong thời gian khóa

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT (chưa đăng nhập).
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Tài khoản đang ở bậc khóa 1; mật khẩu đúng `<RESIDENT_PASSWORD>`. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đọc số lần sai và bậc khóa trước request.
    2. Gửi đăng nhập với mật khẩu đúng trước `locked_until`.
    3. Đối chiếu HTTP và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 401 với body lỗi đăng nhập như khi sai mật khẩu.
  - **Kiểm tra sau (Post-check):** Đọc lại số lần sai và bậc khóa; cả hai không tăng.
  - **Nguồn đối chiếu:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; detail="Tên đăng nhập hoặc mật khẩu không đúng."; failed_login_count=0, lock_level=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-03.3 — Sai sau khi hết khóa tăng lên bậc hai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT (chưa đăng nhập).
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Tài khoản đã khóa bậc 1 và không đăng nhập thành công sau khi hết hạn. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đợi tới sau `locked_until` của bậc 1.
    2. Gửi một lần đăng nhập với `<WRONG_PASSWORD>`.
    3. Đối chiếu HTTP và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 401; khóa bậc 2 trong 5 phút.
  - **Kiểm tra sau (Post-check):** Đọc `locked_until`, `lock_level=2` và audit `ACCOUNT_LOCKED`.
  - **Nguồn đối chiếu:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; lock_level=2, thời hạn còn ~300s (~5 phút), tổng audit ACCOUNT_LOCKED=2
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-03.4 — Đăng nhập đúng sau hết khóa đặt lại bộ đếm

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT (chưa đăng nhập).
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Tài khoản đã hết hạn khóa bậc 1; mật khẩu đúng `<RESIDENT_PASSWORD>`. Body `{"username":"<RESIDENT_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đợi tới sau `locked_until`.
    2. Gửi đăng nhập bằng mật khẩu đúng.
    3. Đối chiếu HTTP và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; response có `username`, `role` và `mustChangePassword`.
  - **Kiểm tra sau (Post-check):** Đọc bộ đếm và bậc khóa của tài khoản: đều trở về 0.
  - **Nguồn đối chiếu:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; username=lock.res2; SQL failed_login_count=0, lock_level=0, locked_until=NULL
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-03.5 — ADMIN không bị khóa tự động

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN (chưa đăng nhập).
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** ADMIN ACTIVE; dùng `<ADMIN_USERNAME>` và `<WRONG_PASSWORD>`. Body `{"username":"<ADMIN_USERNAME>","password":"<PASSWORD_THEO_BUOC>"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi đăng nhập sai 5 lần.
    2. Gửi thêm một lần đăng nhập với `<ADMIN_PASSWORD>`.
    3. Đối chiếu HTTP và thực hiện kiểm sau.
  - **Kết quả mong đợi:** Năm lần sai đều HTTP 401; lần đúng HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc tài khoản ADMIN: không có thời hạn khóa được tạo do các lần sai.
  - **Nguồn đối chiếu:** AUTH-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginAttemptService.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/LoginLockPolicy.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 5 lần sai=[401,401,401,401,401], lần 6 đúng HTTP 200; SQL lock_level=0, locked_until=NULL
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-04.1 — ADMIN mở khóa tài khoản bị khóa

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/{id}/unlock`.
  - **Dữ liệu đầu vào:** `<TARGET_ID>` là RESIDENT đang khóa sau 5 lần sai.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi `POST /api/v1/accounts/<TARGET_ID>/unlock` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** SQL: `locked_until` null, `failed_login_count=0`, `lock_level=0`; có audit `ACCOUNT_UNLOCKED`.
  - **Nguồn đối chiếu:** AUTH-04; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 204; SQL failed_login_count=0, lock_level=0, locked_until=NULL; audit ACCOUNT_UNLOCKED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-04.2 — Không mở khóa ADMIN

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/{id}/unlock`.
  - **Dữ liệu đầu vào:** `<TARGET_ADMIN_ID>` là tài khoản ADMIN.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi `POST /api/v1/accounts/<TARGET_ADMIN_ID>/unlock` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 409; `code=ACCOUNT_NOT_LOCKED`.
  - **Kiểm tra sau (Post-check):** SQL: trạng thái khóa của ADMIN không thay đổi.
  - **Nguồn đối chiếu:** AUTH-04; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=ACCOUNT_NOT_LOCKED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-05.1 — Đăng xuất phiên đang hoạt động

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/logout`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT đã đăng nhập và token CSRF hiện tại.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` bằng phiên RESIDENT.
    2. Gửi `POST /api/v1/auth/logout` với token.
    3. Gọi `GET /api/v1/auth/me` bằng phiên cũ.
  - **Kết quả mong đợi:** Logout HTTP 204; `/auth/me` HTTP 401.
  - **Kiểm tra sau (Post-check):** SQL bảng `SPRING_SESSION`: phiên đã đăng xuất không còn hợp lệ.
  - **Nguồn đối chiếu:** AUTH-05; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** logout HTTP 204; /auth/me sau logout=401; SQL SPRING_SESSION dòng phiên vừa đăng xuất bị xóa (1 -> 0)
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-05.2 — Đăng xuất khi chưa đăng nhập

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/logout`.
  - **Dữ liệu đầu vào:** Phiên ẩn danh có token CSRF hợp lệ.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` để lấy token.
    2. Gửi `POST /api/v1/auth/logout` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 401.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me`: HTTP 401.
  - **Nguồn đối chiếu:** AUTH-05; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; /auth/me=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-06.1 — Cookie phiên có thuộc tính bảo vệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** ADMIN ACTIVE; JSON `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`.
  - **Các bước thực hiện:**
    1. Lấy token và cookie bằng `GET /api/v1/auth/csrf`.
    2. Gửi POST login với JSON và token.
    3. Kiểm tra thuộc tính cookie `SESSION` trong công cụ trình duyệt.
  - **Kết quả mong đợi:** HTTP 200; cookie `SESSION` có `HttpOnly` và `SameSite=Lax`.
  - **Kiểm tra sau (Post-check):** SQL bảng `SPRING_SESSION` có phiên đăng nhập; không ghi giá trị cookie vào bằng chứng.
  - **Nguồn đối chiếu:** AUTH-06; `backend/src/main/resources/application.yml:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; Set-Cookie flags: HttpOnly=true, SameSite=Lax=true; SPRING_SESSION rows=4
- **Chi tiết lỗi / Nhận xét:** Trong profile local (HTTP), application-local.yml đặt secure=false để trình duyệt gửi cookie trên localhost; cấu hình mặc định application.yml đặt secure=true.

### AUTH-06.2 — Phiên hết hạn sau 30 phút không hoạt động

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/me`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT hoạt động; không gửi request từ phiên này trong 31 phút.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/me` và xác nhận HTTP 200.
    2. Đợi 31 phút không dùng phiên.
    3. Gọi lại `GET /api/v1/auth/me` bằng phiên cũ.
  - **Kết quả mong đợi:** Request sau 31 phút trả HTTP 401.
  - **Kiểm tra sau (Post-check):** Đăng nhập lại bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`: HTTP 200.
  - **Nguồn đối chiếu:** AUTH-06; `backend/src/main/resources/application.yml:21`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** /auth/me ban đầu=200; SPRING_SESSION.MAX_INACTIVE_INTERVAL=1800s (30 phút); sau 31 phút không hoạt động /auth/me=401; đăng nhập lại=200
- **Chi tiết lỗi / Nhận xét:** Xác minh timeout 1800s (30m) và mô phỏng 31 phút không hoạt động bằng cách dịch lùi LAST_ACCESS_TIME/EXPIRY_TIME 1.860.000ms trong bảng SPRING_SESSION.

### AUTH-07.1 — Thiếu CSRF bị từ chối

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** JSON `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; không gửi header CSRF.
  - **Các bước thực hiện:**
    1. Tạo phiên ẩn danh mới.
    2. Gửi POST `/api/v1/auth/login` với JSON và không có header CSRF.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 403; code cụ thể chưa xác định.
  - **Kiểm tra sau (Post-check):** Gọi `/api/v1/auth/me` trong phiên này: HTTP 401.
  - **Nguồn đối chiếu:** AUTH-07; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403; body={"type":"about:blank","title":"Không có quyền truy cập","status":403}; /auth/me=401
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Lỗi thiếu CSRF trả ProblemDetail {"type":"about:blank","title":"Không có quyền truy cập","status":403}, không có thuộc tính code.

### AUTH-07.2 — Lấy token CSRF khi chưa đăng nhập

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/csrf`.
  - **Dữ liệu đầu vào:** Phiên trình duyệt ẩn danh mới.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf`.
    2. Đọc tên header và token trong response.
    3. Giữ token trong bộ nhớ cho request tiếp theo.
  - **Kết quả mong đợi:** HTTP 200; JSON có `headerName` và `token`.
  - **Kiểm tra sau (Post-check):** Dùng token trong POST đăng nhập hợp lệ; không lưu token vào bằng chứng.
  - **Nguồn đối chiếu:** AUTH-07; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /auth/csrf HTTP 200 (headerName=X-CSRF-TOKEN); POST /auth/login HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-08.1 — Chặn endpoint khi phải đổi mật khẩu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** Tài khoản RESIDENT mới cấp có `mustChangePassword=true` và đã đăng nhập.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/me` bằng phiên mới cấp.
    2. Gọi `GET /api/v1/households/me/residents`.
    3. Đọc response.
  - **Kết quả mong đợi:** `/auth/me` HTTP 200 và `mustChangePassword=true`; endpoint hộ HTTP 403, `code=PASSWORD_CHANGE_REQUIRED`.
  - **Kiểm tra sau (Post-check):** Gọi lại `/api/v1/auth/me`: tài khoản vẫn yêu cầu đổi mật khẩu.
  - **Nguồn đối chiếu:** AUTH-08; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:19`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** /auth/me HTTP 200 (mustChangePassword=true); GET /households/me/residents HTTP 403 (code=PASSWORD_CHANGE_REQUIRED)
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-08.2 — Chuyển giao diện về đổi mật khẩu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/household`.
  - **Dữ liệu đầu vào:** Tài khoản mới cấp đã đăng nhập và `mustChangePassword=true`.
  - **Các bước thực hiện:**
    1. Mở `/resident/household` trong phiên tài khoản mới.
    2. Quan sát URL sau chuyển hướng.
    3. Mở trực tiếp `/profile?tab=password`.
  - **Kết quả mong đợi:** Giao diện chuyển tới `/profile?tab=password`; trang hộ bị chặn.
  - **Kiểm tra sau (Post-check):** Kiểm `GET /api/v1/auth/me` vẫn có `mustChangePassword=true`.
  - **Nguồn đối chiếu:** AUTH-08; `frontend/src/App.tsx:58`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Mở /resident/household chuyển hướng tới "/profile?tab=password"; /api/v1/auth/me mustChangePassword=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-09.1 — Mật khẩu hiện tại sai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** `{"currentPassword":"<WRONG_PASSWORD>","newPassword":"<NEW_VALID_PASSWORD>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
    2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
    3. Đối chiếu HTTP, body và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; `code=CURRENT_PASSWORD_INCORRECT`.
  - **Kiểm tra sau (Post-check):** Kiểm `failed_login_count` tăng 1 và hash mật khẩu không đổi.
  - **Nguồn đối chiếu:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=CURRENT_PASSWORD_INCORRECT; failed_login_count=0->1; hashUnchanged=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-09.2 — Mật khẩu mới ngắn hơn 8 ký tự

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<SHORT_PASSWORD: 7 ký tự>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
    2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
    3. Đối chiếu HTTP, body và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; `code=PASSWORD_TOO_SHORT`.
  - **Kiểm tra sau (Post-check):** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
  - **Nguồn đối chiếu:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=PASSWORD_TOO_SHORT; đăng nhập lại mật khẩu cũ=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-09.3 — Mật khẩu mới dài hơn 72 byte UTF-8

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<LONG_PASSWORD: 73 byte UTF-8>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
    2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
    3. Đối chiếu HTTP, body và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; `code=PASSWORD_TOO_LONG`.
  - **Kiểm tra sau (Post-check):** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
  - **Nguồn đối chiếu:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=PASSWORD_TOO_LONG; đăng nhập lại mật khẩu cũ=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-09.4 — Mật khẩu mới thiếu ký tự đặc biệt

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<WEAK_PASSWORD: có chữ hoa, chữ thường, số; không có ký tự đặc biệt>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
    2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
    3. Đối chiếu HTTP, body và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; `code=PASSWORD_WEAK`.
  - **Kiểm tra sau (Post-check):** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
  - **Nguồn đối chiếu:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=PASSWORD_WEAK; đăng nhập lại mật khẩu cũ=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-09.5 — Mật khẩu mới trùng mật khẩu hiện tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<CURRENT_PASSWORD>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` trong phiên RESIDENT và lấy token từ response vào bộ nhớ.
    2. Gửi body trên tới `POST /api/v1/auth/change-password` cùng token.
    3. Đối chiếu HTTP, body và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; `code=PASSWORD_SAME_AS_CURRENT`.
  - **Kiểm tra sau (Post-check):** Đăng nhập lại bằng mật khẩu cũ; mật khẩu cũ vẫn hợp lệ.
  - **Nguồn đối chiếu:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=PASSWORD_SAME_AS_CURRENT; đăng nhập lại mật khẩu cũ=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-09.6 — Đổi mật khẩu hợp lệ hủy mọi phiên

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** Hai phiên độc lập của cùng RESIDENT; `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_VALID_PASSWORD>"}`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập cùng RESIDENT trong hai trình duyệt độc lập.
    2. Gọi `GET /api/v1/auth/csrf` trong phiên thứ nhất và lấy token.
    3. Gửi body tới `POST /api/v1/auth/change-password` trong phiên thứ nhất cùng token.
  - **Kết quả mong đợi:** HTTP 204, không có body.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` từ cả hai phiên cũ: mỗi phiên trả 401; đăng nhập bằng mật khẩu mới thành công. Kiểm audit `PASSWORD_CHANGED` và `SESSIONS_REVOKED`.
  - **Nguồn đối chiếu:** AUTH-09; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/PasswordChangeService.java:32`; `docs/DECISIONS.md` D23.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 204; phiên cũ 1 /auth/me=401, phiên cũ 2 /auth/me=401; đăng nhập mật khẩu mới=200; audits có PASSWORD_CHANGED & SESSIONS_REVOKED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### AUTH-10.1 — Bootstrap tạo ADMIN đầu tiên

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap` (console; không có endpoint hoặc route).
  - **Dữ liệu đầu vào:** DB thử nghiệm chưa có ADMIN; nhập `<ADMIN_USERNAME>`, tên `Test Admin`, `<TEST_EMAIL>`, `<ADMIN_PASSWORD>` và xác nhận mật khẩu tại console tương tác.
  - **Các bước thực hiện:**
    1. Chạy lệnh bootstrap trên DB thử nghiệm.
    2. Nhập thông tin ADMIN theo prompt mà không ghi mật khẩu.
    3. Đọc kết quả console đã che dữ liệu.
  - **Kết quả mong đợi:** Tạo một ADMIN; mã thoát và thông báo chính xác chưa xác định.
  - **Kiểm tra sau (Post-check):** SQL: có một ADMIN mới; có audit bootstrap; không lưu mật khẩu hoặc email vào bằng chứng.
  - **Nguồn đối chiếu:** AUTH-10; `backend/src/main/java/vn/edu/se10/bluemoon/identity/bootstrap/BootstrapAdminRunner.java:10`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Exit code=0; passwordEchoed=false; SQL ADMIN count=1; audit BOOTSTRAP_ADMIN=1
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Mã thoát là 0, thông báo console là "Đã tạo ADMIN.".

### AUTH-10.2 — Bootstrap bỏ qua khi đã có ADMIN

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap` (console; không có endpoint hoặc route).
  - **Dữ liệu đầu vào:** DB thử nghiệm đã có ADMIN; nhập `<ADMIN_USERNAME>`, tên `Test Admin`, `<TEST_EMAIL>`, `<ADMIN_PASSWORD>` và xác nhận mật khẩu khi được hỏi.
  - **Các bước thực hiện:**
    1. Đếm số ADMIN hiện có bằng SQL.
    2. Chạy lệnh bootstrap trong console tương tác.
    3. Nhập các giá trị thử nghiệm tại prompt và đọc kết quả console.
  - **Kết quả mong đợi:** Không tạo ADMIN mới; mã thoát và thông báo chính xác chưa xác định.
  - **Kiểm tra sau (Post-check):** Đếm lại ADMIN bằng SQL: số lượng không tăng.
  - **Nguồn đối chiếu:** AUTH-10; `backend/src/main/java/vn/edu/se10/bluemoon/identity/bootstrap/BootstrapAdminRunner.java:10`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Exit code=0; passwordEchoed=false; SQL ADMIN count trước=1, sau=1
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Mã thoát là 0, thông báo console là "Đã có ADMIN, bỏ qua.".

### AUTH-11.1 — Xem hồ sơ cá nhân

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/profile`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT đã đổi mật khẩu.
  - **Các bước thực hiện:**
    1. Đăng nhập RESIDENT.
    2. Mở `/profile`.
    3. Đối chiếu thông tin hiển thị với `GET /api/v1/auth/me`.
  - **Kết quả mong đợi:** Trang hiển thị thông tin tài khoản; không có thao tác tự sửa hồ sơ; nhãn chính xác chưa xác định.
  - **Kiểm tra sau (Post-check):** GET `/api/v1/auth/me`: username, fullName và role khớp hồ sơ.
  - **Nguồn đối chiếu:** AUTH-11; `frontend/src/App.tsx:104`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Nhãn dt=["Họ tên","Vai trò","Tên đăng nhập"], giá trị dd khớp /auth/me (username=ui.prof.res, fullName="Ho So Cu Dan", role=RESIDENT); số nút/input sửa hồ sơ=0
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Nhãn giao diện chính xác trên tab Hồ sơ là ["Họ tên","Vai trò","Tên đăng nhập"].

### AUTH-11.2 — Đổi mật khẩu từ hồ sơ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/profile?tab=password`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT; `<CURRENT_PASSWORD>` đúng và `<NEW_VALID_PASSWORD>` thỏa quy tắc.
  - **Các bước thực hiện:**
    1. Mở `/profile?tab=password`.
    2. Nhập mật khẩu hiện tại và mật khẩu mới bằng placeholder tương ứng.
    3. Gửi biểu mẫu đổi mật khẩu.
  - **Kết quả mong đợi:** Request `POST /api/v1/auth/change-password` HTTP 204; phiên bị hủy.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` với phiên cũ: HTTP 401; đăng nhập bằng mật khẩu mới.
  - **Nguồn đối chiếu:** AUTH-11; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/change-password HTTP 204; phiên cũ GET /auth/me=401; đăng nhập mật khẩu mới=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
