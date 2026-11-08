# Ca kiểm thử SYS

Tiền tố `/api/v1` áp dụng cho endpoint API viết rút gọn; thời gian nghiệp vụ dùng Asia/Ho_Chi_Minh.

## SYS-01 — Health.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **SYS-01.1:** `GET /actuator/health` trả trạng thái; không lộ chi tiết nhạy cảm.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## SYS-02 — Thông tin hệ thống.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **SYS-02.1:** `GET /api/v1/system/info` trả metadata hằng số, public.
- **SYS-02.2:** Đối chiếu body: không chứa thông tin nhạy cảm.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## SYS-03 — Request ID.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **SYS-03.1:** Mọi response có request ID trong header; lỗi có thể truy vết bằng request ID; log không chứa body, cookie, Authorization, CCCD, email, SĐT (AGENTS.md).

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## SYS-04 — Mặc định từ chối.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **SYS-04.1:** Route không khai báo trả 401/403 (`denyAll`).

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## SYS-05 — Trang lỗi.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **SYS-05.1:** Route không tồn tại và `/error/:code` hiện trang lỗi chung.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## Điểm chưa xác định

Các nhánh không nêu mã HTTP hoặc nội dung chính xác trong outline: ghi thực tế và đối chiếu mã nguồn trước khi kết luận; không tự suy đoán mã.
