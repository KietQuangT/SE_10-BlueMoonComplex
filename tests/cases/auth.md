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

## AUTH-03 — Khóa theo tài khoản.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-03.1:** 5 lần sai liên tiếp khóa 1 phút; bậc tăng 1, 5, 10, 15, 30, 60 phút; sau 60 phút tăng thêm 60 phút mỗi lần (P2).
- **AUTH-03.2:** Đếm lại khi đăng nhập thành công.
- **AUTH-03.3:** ADMIN không bị khóa.
- **AUTH-03.4:** Đang khóa thì mật khẩu đúng vẫn bị từ chối và không đếm.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

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

## AUTH-09 — Đổi mật khẩu.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **AUTH-09.1:** `POST /auth/change-password` body `{currentPassword, newPassword}`, trả `204`.
- **AUTH-09.2:** Lỗi `400` có `code`: `CURRENT_PASSWORD_INCORRECT`, `PASSWORD_TOO_SHORT`, `PASSWORD_TOO_LONG`, `PASSWORD_WEAK`, `PASSWORD_SAME_AS_CURRENT`.
- **AUTH-09.3:** Quy tắc mật khẩu mới (D23): tối thiểu 8 ký tự theo char, có chữ hoa, chữ thường, số, ký tự đặc biệt (bao gồm khoảng trắng); tối đa 72 byte UTF-8; không trùng mật khẩu hiện tại.
- **AUTH-09.4:** Thành công thì mọi phiên của tài khoản bị hủy (kể cả phiên đang dùng).
- **AUTH-09.5:** Sai mật khẩu cũ được đếm và khóa như đăng nhập sai.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

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
