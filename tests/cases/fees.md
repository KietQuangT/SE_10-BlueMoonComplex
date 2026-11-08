# Ca kiểm thử FEE

Tiền tố `/api/v1` áp dụng cho endpoint API viết rút gọn; thời gian nghiệp vụ dùng Asia/Ho_Chi_Minh.

## FEE-01 — Danh mục loại phí.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-01.1:** `GET /fee-types` (ADMIN, ACCOUNTANT): hai loại cố định `MANAGEMENT_FEE` (Phí quản lý, tính theo m², đơn vị `m²/tháng`) và `WASTE_FEE` (Phí rác sinh hoạt, cố định mỗi hộ mỗi tháng, đơn vị `hộ/tháng`).
- **FEE-01.2:** Không có API tạo, sửa, xóa loại phí.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## FEE-02 — Danh sách đơn giá.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-02.1:** `GET /fee-rates?feeTypeId=&page=&size=` (ADMIN, ACCOUNTANT): sắp `effectiveFrom` giảm dần rồi `id` giảm dần; `size` từ 1 đến 100; sai trả `400 INVALID_FILTER`; `feeTypeId` lạ trả trang rỗng 200.
- **FEE-02.2:** Mỗi dòng có `editable`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## FEE-03 — Thêm đơn giá.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-03.1:** ACCOUNTANT, `POST /fee-rates` `{feeTypeId, unitPrice, effectiveFrom}`: `effectiveFrom` là ngày 1, không trước tháng hiện tại (giờ Việt Nam); trùng loại và tháng trả 409 `FEE_RATE_DUPLICATE_MONTH`; `unitPrice` > 0, tối đa 2 chữ số thập phân, tối đa 10 chữ số phần nguyên (400 nếu vượt).

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## FEE-04 — Sửa đơn giá.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-04.1:** ACCOUNTANT, `PATCH /fee-rates/{id}` `{unitPrice}`: chỉ sửa đơn giá có tháng sau tháng hiện tại; tháng hiện tại và trước đó trả 409 `FEE_RATE_FROZEN`; không tồn tại trả 404 `FEE_RATE_NOT_FOUND`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## FEE-05 — Xóa đơn giá.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-05.1:** ACCOUNTANT, `DELETE /fee-rates/{id}` (204): cùng quy tắc đóng băng như FEE-04.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## FEE-06 — Xem trước thành tiền.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-06.1:** ADMIN, ACCOUNTANT, `GET /fee-preview?householdId=&period=YYYY-MM`: chỉ tính, không lưu.
- **FEE-06.2:** Phí quản lý = diện tích × đơn giá; phí rác = đơn giá; làm tròn VND nguyên HALF_UP theo từng dòng; tổng là tổng các dòng đã làm tròn.
- **FEE-06.3:** Thiếu đơn giá cho bất kỳ loại nào trong tháng đó trả 409 `FEE_RATE_UNAVAILABLE` (không coi là 0).
- **FEE-06.4:** `period` sai trả 400 `INVALID_PERIOD`.
- **FEE-06.5:** Thiếu tham số trả 400 (không trả 500).

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## FEE-07 — Quyền đơn giá.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-07.1:** RESIDENT gọi bất kỳ route đơn giá nào: 403.
- **FEE-07.2:** ADMIN ghi đơn giá: 403.
- **FEE-07.3:** Với từng endpoint ghi đơn giá, ADMIN nhận 403; ACCOUNTANT được phép theo ma trận. D25 chưa có văn bản xác nhận của Quân và Tuấn.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## FEE-08 — Màn M-20 (nhánh UI).

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **FEE-08.1:** ACCOUNTANT thêm, sửa, xóa dòng `editable`; ADMIN chỉ xem; chọn tháng (`type="month"`), mặc định tháng sau; giá hiển thị `7.000,50 đ`; thông báo lỗi tiếng Việt theo bảng mã.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## Điểm chưa xác định

Các nhánh không nêu mã HTTP hoặc nội dung chính xác trong outline: ghi thực tế và đối chiếu mã nguồn trước khi kết luận; không tự suy đoán mã.

Outline gọi nhánh `hhoangsonnw/feat-s07` ở tiêu đề và `hhoangsonnw/feat-s07-ui` ở bảng route; xác nhận nhánh UI trước khi kiểm FEE-08.
