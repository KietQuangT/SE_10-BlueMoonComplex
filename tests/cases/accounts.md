# Ca kiểm thử ACC

Tiền tố `/api/v1` áp dụng cho endpoint API viết rút gọn; thời gian nghiệp vụ dùng Asia/Ho_Chi_Minh.

## ACC-01 — Tạo tài khoản.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **ACC-01.1:** ADMIN tạo ADMIN, ACCOUNTANT hoặc RESIDENT (`POST /accounts`, màn `/admin/accounts/new`).
- **ACC-01.2:** Username `[A-Za-z0-9._-]`, 3–100 ký tự; email bắt buộc, duy nhất, chuẩn hóa chữ thường; email trùng bị từ chối (409); RESIDENT phải gắn hộ `ACTIVE` (400 nếu không).
- **ACC-01.3:** Mật khẩu tạm do server sinh, không trả trong response, chỉ nằm trong `mock_emails`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## ACC-02 — Email mô phỏng.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **ACC-02.1:** Không có API đọc; kiểm bằng SQL (bảng `mock_emails`).

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## ACC-03 — Vô hiệu hóa, kích hoạt lại.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **ACC-03.1:** `POST /accounts/{id}/disable|enable` (204). Vô hiệu hủy các phiên đang có; request kế tiếp của tài khoản đó bị từ chối.
- **ACC-03.2:** Không tự vô hiệu chính mình, không vô hiệu ADMIN đang hoạt động cuối cùng.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## ACC-04 — Danh sách tài khoản.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **ACC-04.1:** Màn `/admin/accounts` chưa có API liệt kê; chỉ hiện tài khoản tạo trong phiên hiện tại.
- **ACC-04.2:** Tạo một tài khoản trong phiên hiện tại rồi mở danh sách: tài khoản vừa tạo xuất hiện; không kết luận có danh sách toàn hệ thống.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## Điểm chưa xác định

Các nhánh không nêu mã HTTP hoặc nội dung chính xác trong outline: ghi thực tế và đối chiếu mã nguồn trước khi kết luận; không tự suy đoán mã.
