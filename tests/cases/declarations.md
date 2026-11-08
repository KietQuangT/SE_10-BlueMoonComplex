# Ca kiểm thử DEC

Tiền tố `/api/v1` áp dụng cho endpoint API viết rút gọn; thời gian nghiệp vụ dùng Asia/Ho_Chi_Minh.

## DEC-01 — Gửi khai báo.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-01.1:** RESIDENT, `POST /residency-declarations` `{residentId, effectiveDate, endDate?, reason}`, màn `/resident/absence`. Server lấy hộ từ principal, không tin client.
- **DEC-01.2:** Ngày bắt đầu không ở quá khứ (giờ Việt Nam).
- **DEC-01.3:** `endDate` tùy chọn, không trước `effectiveDate`, chỉ để hiển thị.
- **DEC-01.4:** Lý do bắt buộc, tối đa 255 ký tự.
- **DEC-01.5:** Trạng thái `PENDING`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-02 — Giới hạn đơn.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-02.1:** Tối đa một đơn `PENDING` mỗi nhân khẩu (409 `DECLARATION_ALREADY_PENDING`).
- **DEC-02.2:** Không khai khi nhân khẩu đang `TEMPORARILY_ABSENT` hoặc `MOVED_OUT`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-03 — Xem đơn của hộ.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-03.1:** RESIDENT, `GET /residency-declarations/mine` (phân trang, mới nhất trước); cả hộ thấy đơn của hộ. Thông báo chỉ là trạng thái và lý do từ chối trong danh sách này; không có hộp thư.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-04 — Hủy đơn.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-04.1:** RESIDENT, `POST /residency-declarations/{id}/cancel`: chỉ đơn đang `PENDING` và do chính account đã gửi; người khác cùng hộ bị 403; đơn đã quyết định hoặc đã hủy trả 409.
- **DEC-04.2:** Trạng thái `CANCELLED`, giữ lịch sử.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-05 — Hàng chờ ADMIN.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-05.1:** ADMIN, `GET /residency-declarations` lọc `status` (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`; khác thì 400 `INVALID_FILTER`), `apartment` (chứa chuỗi, không phân biệt hoa thường), `q` (tên, tối thiểu 2 ký tự).
- **DEC-05.2:** Chỉ liệt kê dòng `TEMPORARY_ABSENCE`.
- **DEC-05.3:** Mở `/admin/absence-approvals` ở chế độ API: hàng chờ hiển thị các đơn tạm vắng phù hợp bộ lọc.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-06 — Duyệt.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-06.1:** ADMIN, `POST .../approve`: đơn chuyển `APPROVED`; nhân khẩu chuyển `TEMPORARILY_ABSENT` ngay.
- **DEC-06.2:** Đơn không ở `PENDING` hoặc nhân khẩu không `RESIDING` trả 409.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-07 — Từ chối.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-07.1:** ADMIN, `POST .../reject` `{decisionNote}`: lý do bắt buộc, tối đa 255 ký tự, hiện cho cư dân; nhân khẩu giữ nguyên.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-08 — Quay lại.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-08.1:** ADMIN, `POST /residents/{id}/return`: nhân khẩu từ `TEMPORARILY_ABSENT` về `RESIDING`; ghi dòng lịch sử `RETURN`, ngày là hôm nay do server đặt (không nhận từ client).
- **DEC-08.2:** Nhân khẩu không tạm vắng trả 409 `RESIDENT_NOT_ABSENT`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## DEC-09 — Trạng thái tạm vắng trên giao diện.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **DEC-09.1:** Cư dân thấy trạng thái và lý do; ADMIN thấy hàng chờ và gợi ý tên.
- **DEC-09.2:** Ở chế độ API, mở hai trang: cư dân thấy trạng thái và lý do, ADMIN thấy hàng chờ và gợi ý tên.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## Điểm chưa xác định

Các nhánh không nêu mã HTTP hoặc nội dung chính xác trong outline: ghi thực tế và đối chiếu mã nguồn trước khi kết luận; không tự suy đoán mã.
