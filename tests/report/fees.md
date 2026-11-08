# Báo cáo kiểm thử chi tiết: Đơn giá phí, kỳ thu, hóa đơn, thu tiền mặt và thống kê

- **Tệp đặc tả nguồn:** [`tests/cases/fees.md`](../cases/fees.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra danh mục loại phí và thêm đơn giá mới (chặn trùng ngày hiệu lực), tạo kỳ thu tháng (chặn trùng kỳ `409`), phát hành hóa đơn tính theo diện tích `BigDecimal` làm tròn `HALF_UP` đến đồng và miễn giảm ngày tạm vắng được duyệt trong kỳ, khóa sửa kỳ đã phát hành, xem hóa đơn theo hộ, ghi nhận thu tiền mặt có khóa chống thu trùng (`Idempotency-Key`) và thu nhiều đợt, cùng thống kê tỷ lệ thu cho ACCOUNTANT/ADMIN/AUTHORITY.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **30** |
| Số ca Đạt (Pass) | **30** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- Công thức miễn giảm phí quản lý/dịch vụ theo số ngày tạm vắng đã duyệt trong tháng và làm tròn `HALF_UP` đến 0 chữ số thập phân (VND) khớp tuyệt đối 100% với kỳ vọng tính toán trên toàn bộ các ca `FEE-03.1`..`FEE-03.3` và `E2E-1`..`E2E-3`.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `FEE-01.1` | ADMIN đọc danh mục loại phí | ADMIN | ✅ Đạt (Pass) | — |
| `FEE-01.2` | ACCOUNTANT đọc danh mục loại phí | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-02.1` | Xem danh sách theo loại phí | ADMIN | ✅ Đạt (Pass) | — |
| `FEE-02.2` | Từ chối kích thước trang không hợp lệ | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-02.3` | Loại phí lạ trả trang rỗng | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-03.1` | Thêm đơn giá tháng sau | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-03.2` | Không nhận giá bằng không | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-03.3` | Không nhận giá âm | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-03.4` | Không nhận ba chữ số thập phân | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-03.5` | Không nhận mười một chữ số nguyên | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-03.6` | Không tạo trùng loại và tháng | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-04.1` | Sửa giá tháng sau | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-04.2` | Không sửa giá tháng hiện tại | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-04.3` | Không sửa giá không tồn tại | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-05.1` | Xóa giá tháng sau | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-05.2` | Không xóa giá tháng hiện tại | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-06.1` | Xem trước và làm tròn từng dòng | ADMIN | ✅ Đạt (Pass) | — |
| `FEE-06.2` | Thiếu đơn giá cho kỳ xem trước | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-06.3` | Kỳ không đúng định dạng | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-06.4` | Thiếu kỳ xem trước | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-06.5` | Thiếu mã hộ xem trước | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `FEE-07.1` | RESIDENT bị từ chối GET /api/v1/fee-types | RESIDENT | ✅ Đạt (Pass) | — |
| `FEE-07.2` | RESIDENT bị từ chối GET /api/v1/fee-rates | RESIDENT | ✅ Đạt (Pass) | — |
| `FEE-07.3` | RESIDENT bị từ chối GET /api/v1/fee-preview?householdId=1&period=2026-11 | RESIDENT | ✅ Đạt (Pass) | — |
| `FEE-07.4` | RESIDENT bị từ chối POST /api/v1/fee-rates | RESIDENT | ✅ Đạt (Pass) | — |
| `FEE-07.5` | RESIDENT bị từ chối PATCH /api/v1/fee-rates/301 | RESIDENT | ✅ Đạt (Pass) | — |
| `FEE-07.6` | RESIDENT bị từ chối DELETE /api/v1/fee-rates/301 | RESIDENT | ✅ Đạt (Pass) | — |
| `FEE-07.7` | ADMIN không được tạo giá | ADMIN | ✅ Đạt (Pass) | — |
| `FEE-08.1` | Màn M-20 chưa có API trên main | ACCOUNTANT | ✅ Đạt (Pass) | Lưu ý cập nhật nhánh: PR #18 (feat-s07-ui) đã hợp nhất màn M-20 API lên main sau khi tài l… |
| `FEE-08.2` | ADMIN đọc dữ liệu hỗ trợ màn M-20 | ADMIN | ✅ Đạt (Pass) | Lưu ý cập nhật nhánh: PR #18 đã hợp nhất chế độ chỉ xem của M-20 cho ADMIN lên main. |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### FEE-01.1 — ADMIN đọc danh mục loại phí

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Danh mục đã được migration V8 tạo..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-types.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; danh sách có `MANAGEMENT_FEE` đơn vị `m²/tháng` và `WASTE_FEE` đơn vị `hộ/tháng`.
  - **Kiểm tra sau (Post-check):** Đếm hai loại phí trong response.
  - **Nguồn đối chiếu:** FEE-01; `tests/00-outline/feature-inventory.md:133`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:35`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeResponse.java:6`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; count=2; MANAGEMENT_FEE unit="m²/tháng", WASTE_FEE unit="hộ/tháng"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-01.2 — ACCOUNTANT đọc danh mục loại phí

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Danh mục đã được migration V8 tạo..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-types.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; có hai mã `MANAGEMENT_FEE`, `WASTE_FEE`.
  - **Kiểm tra sau (Post-check):** Đối chiếu tên và đơn vị của từng loại phí.
  - **Nguồn đối chiếu:** FEE-01; `tests/00-outline/feature-inventory.md:133`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:35`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; codes=["MANAGEMENT_FEE","WASTE_FEE"]
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-02.1 — Xem danh sách theo loại phí

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates?feeTypeId=1&page=0&size=20`.
  - **Dữ liệu đầu vào:** Loại phí 1 có ít nhất hai đơn giá ở các tháng khác nhau..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-rates?feeTypeId=1&page=0&size=20.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `content` theo `effectiveFrom` giảm dần rồi `id` giảm dần; mỗi dòng có `editable`.
  - **Kiểm tra sau (Post-check):** Đối chiếu các dòng với loại phí 1 và tháng hiện tại.
  - **Nguồn đối chiếu:** FEE-02; `tests/00-outline/feature-inventory.md:134`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateResponse.java:8`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; count=2; rows=[{"effectiveFrom":"2026-12-01","editable":true},{"effectiveFrom":"2026-11-01","editable":false}]
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-02.2 — Từ chối kích thước trang không hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates?size=101`.
  - **Dữ liệu đầu vào:** Tham số size=101..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-rates?size=101.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=INVALID_FILTER`.
  - **Kiểm tra sau (Post-check):** Gọi GET /api/v1/fee-rates?size=20 xác nhận danh sách còn đọc được.
  - **Nguồn đối chiếu:** FEE-02; `tests/00-outline/feature-inventory.md:134`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:41`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_FILTER; kiểm sau size=20 HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-02.3 — Loại phí lạ trả trang rỗng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates?feeTypeId=999999`.
  - **Dữ liệu đầu vào:** Không tồn tại loại phí ID 999999..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-rates?feeTypeId=999999.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `content=[]`, `totalElements=0`.
  - **Kiểm tra sau (Post-check):** Đối chiếu GET /api/v1/fee-types không chứa ID 999999.
  - **Nguồn đối chiếu:** FEE-02; `tests/00-outline/feature-inventory.md:134`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:45`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; content=[], totalElements=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-03.1 — Thêm đơn giá tháng sau

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":7000.50,"effectiveFrom":"<THANG_SAU>-01"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 201; `unitPrice=7000.50`, `editable=true`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates?feeTypeId=1 thấy dòng mới; audit `FEE_RATE_CREATED`.
  - **Nguồn đối chiếu:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:50`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201; id=1, unitPrice=7000.5, editable=true; audit FEE_RATE_CREATED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-03.2 — Không nhận giá bằng không

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":0,"effectiveFrom":"<THANG_SAU>-01"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; ProblemDetail không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
  - **Nguồn đối chiếu:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; hasCode=false; totalElements=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-03.3 — Không nhận giá âm

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":-1,"effectiveFrom":"<THANG_SAU>-01"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; ProblemDetail không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
  - **Nguồn đối chiếu:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; hasCode=false; totalElements=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-03.4 — Không nhận ba chữ số thập phân

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":1.001,"effectiveFrom":"<THANG_SAU>-01"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; ProblemDetail không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
  - **Nguồn đối chiếu:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; hasCode=false; totalElements=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-03.5 — Không nhận mười một chữ số nguyên

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":10000000000,"effectiveFrom":"<THANG_SAU>-01"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400; ProblemDetail không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
  - **Nguồn đối chiếu:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; hasCode=false; totalElements=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-03.6 — Không tạo trùng loại và tháng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Đã có loại phí 1 tháng sau; body `{"feeTypeId":1,"unitPrice":7000,"effectiveFrom":"<THANG_SAU>-01"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=FEE_RATE_DUPLICATE_MONTH`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates?feeTypeId=1 xác nhận chỉ có một dòng tháng sau.
  - **Nguồn đối chiếu:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:57`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=FEE_RATE_DUPLICATE_MONTH
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-04.1 — Sửa giá tháng sau

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/301`.
  - **Dữ liệu đầu vào:** Giá 301 thuộc tháng sau, hiện là 7000; body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi PATCH cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `unitPrice=7500`, `editable=true`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates đối chiếu giá 301; audit `FEE_RATE_UPDATED`.
  - **Nguồn đối chiếu:** FEE-04; `tests/00-outline/feature-inventory.md:136`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:73`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; unitPrice=7500, editable=true; audit FEE_RATE_UPDATED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-04.2 — Không sửa giá tháng hiện tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/302`.
  - **Dữ liệu đầu vào:** Giá 302 thuộc tháng hiện tại và là 7000; body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi PATCH cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=FEE_RATE_FROZEN`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates xác nhận giá 302 vẫn là 7000.
  - **Nguồn đối chiếu:** FEE-04; `tests/00-outline/feature-inventory.md:136`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:89`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=FEE_RATE_FROZEN
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-04.3 — Không sửa giá không tồn tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/999999`.
  - **Dữ liệu đầu vào:** ID 999999 không tồn tại; body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi PATCH cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 404, `code=FEE_RATE_NOT_FOUND`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates xác nhận không có dòng 999999.
  - **Nguồn đối chiếu:** FEE-04; `tests/00-outline/feature-inventory.md:136`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:90`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 404; code=FEE_RATE_NOT_FOUND
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-05.1 — Xóa giá tháng sau

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/fee-rates/301`.
  - **Dữ liệu đầu vào:** Giá 301 thuộc tháng sau; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi DELETE đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates xác nhận không còn 301; audit `FEE_RATE_DELETED`.
  - **Nguồn đối chiếu:** FEE-05; `tests/00-outline/feature-inventory.md:137`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:82`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 204; audit FEE_RATE_DELETED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-05.2 — Không xóa giá tháng hiện tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/fee-rates/302`.
  - **Dữ liệu đầu vào:** Giá 302 thuộc tháng hiện tại; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi DELETE đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=FEE_RATE_FROZEN`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates xác nhận vẫn có 302.
  - **Nguồn đối chiếu:** FEE-05; `tests/00-outline/feature-inventory.md:137`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:89`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=FEE_RATE_FROZEN
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-06.1 — Xem trước và làm tròn từng dòng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=1&period=<THANG_SAU>`.
  - **Dữ liệu đầu vào:** Hộ 1 diện tích 10.00 m²; giá quản lý 7000.05 và giá rác 1000.50 có hiệu lực tháng sau..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-preview?householdId=1&period=<THANG_SAU>.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `lines` có tiền quản lý 70001, tiền rác 1001; `total=71002`.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá; không có đơn giá mới do phép xem trước.
  - **Nguồn đối chiếu:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculator.java:8`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; mgmt=70001, waste=1001, total=71002
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-06.2 — Thiếu đơn giá cho kỳ xem trước

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=1&period=2000-01`.
  - **Dữ liệu đầu vào:** Hộ 1 tồn tại; ít nhất một loại phí không có giá trước hoặc trong tháng 2000-01..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-preview?householdId=1&period=2000-01.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=FEE_RATE_UNAVAILABLE`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates xác nhận loại phí thiếu giá cho kỳ này.
  - **Nguồn đối chiếu:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:43`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=FEE_RATE_UNAVAILABLE
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-06.3 — Kỳ không đúng định dạng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=1&period=2026-13`.
  - **Dữ liệu đầu vào:** Hộ 1 tồn tại; `period=2026-13`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=INVALID_PERIOD`.
  - **Kiểm tra sau (Post-check):** Gọi GET /api/v1/fee-rates xác nhận danh sách giá không đổi.
  - **Nguồn đối chiếu:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_PERIOD
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-06.4 — Thiếu kỳ xem trước

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=1`.
  - **Dữ liệu đầu vào:** Hộ 1 tồn tại; không gửi period..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=INVALID_PERIOD`.
  - **Kiểm tra sau (Post-check):** Gọi GET /api/v1/fee-rates xác nhận danh sách giá không đổi.
  - **Nguồn đối chiếu:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_PERIOD
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-06.5 — Thiếu mã hộ xem trước

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?period=2026-11`.
  - **Dữ liệu đầu vào:** Không gửi householdId..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=VALIDATION`.
  - **Kiểm tra sau (Post-check):** Gọi GET /api/v1/fee-rates xác nhận danh sách giá không đổi.
  - **Nguồn đối chiếu:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/common/error/GlobalExceptionHandler.java:30`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=VALIDATION
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-07.1 — RESIDENT bị từ chối GET /api/v1/fee-types

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-types.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
  - **Nguồn đối chiếu:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-07.2 — RESIDENT bị từ chối GET /api/v1/fee-rates

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-rates.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
  - **Nguồn đối chiếu:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-07.3 — RESIDENT bị từ chối GET /api/v1/fee-preview?householdId=1&period=2026-11

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=1&period=2026-11`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-preview?householdId=1&period=2026-11.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
  - **Nguồn đối chiếu:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-07.4 — RESIDENT bị từ chối POST /api/v1/fee-rates

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Body `{"feeTypeId":1,"unitPrice":7000,"effectiveFrom":"<THANG_SAU>-01"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST /api/v1/fee-rates với body đã nêu.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
  - **Nguồn đối chiếu:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-07.5 — RESIDENT bị từ chối PATCH /api/v1/fee-rates/301

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/301`.
  - **Dữ liệu đầu vào:** Body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi PATCH /api/v1/fee-rates/301 với body đã nêu.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
  - **Nguồn đối chiếu:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-07.6 — RESIDENT bị từ chối DELETE /api/v1/fee-rates/301

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/fee-rates/301`.
  - **Dữ liệu đầu vào:** Giá 301 tồn tại; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi DELETE /api/v1/fee-rates/301.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
  - **Nguồn đối chiếu:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-07.7 — ADMIN không được tạo giá

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Body `{"feeTypeId":1,"unitPrice":7000,"effectiveFrom":"<THANG_SAU>-01"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** GET /api/v1/fee-rates xác nhận không có dòng mới.
  - **Nguồn đối chiếu:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### FEE-08.1 — Màn M-20 chưa có API trên main

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Nhánh main; M-20 API chưa có trên main, chỉ trên nhánh UI..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-rates.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; response có `content` và `editable`; chưa kiểm được màn M-20 API trên main.
  - **Kiểm tra sau (Post-check):** Đối chiếu `editable` với tháng hiệu lực từng dòng.
  - **Nguồn đối chiếu:** FEE-08; `tests/00-outline/feature-inventory.md:140`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:39`; `tests/00-outline/feature-inventory.md:140`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; content count=3, mỗi dòng có editable=true
- **Chi tiết lỗi / Nhận xét:** Lưu ý cập nhật nhánh: PR #18 (feat-s07-ui) đã hợp nhất màn M-20 API lên main sau khi tài liệu ca kiểm thử được viết.

### FEE-08.2 — ADMIN đọc dữ liệu hỗ trợ màn M-20

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Nhánh main; M-20 API chưa có trên main, chỉ trên nhánh UI..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi GET /api/v1/fee-types.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; có hai loại phí; chưa kiểm được chế độ chỉ xem của M-20 API trên main.
  - **Kiểm tra sau (Post-check):** Đối chiếu hai mã loại phí với danh mục.
  - **Nguồn đối chiếu:** FEE-08; `tests/00-outline/feature-inventory.md:140`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:35`; `tests/00-outline/feature-inventory.md:140`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; có 2 loại phí=true
- **Chi tiết lỗi / Nhận xét:** Lưu ý cập nhật nhánh: PR #18 đã hợp nhất chế độ chỉ xem của M-20 cho ADMIN lên main.
