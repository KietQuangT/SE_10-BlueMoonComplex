# Ca kiểm thử đơn giá phí

Các ID số trong dữ liệu là ID mẫu đã được tạo trước; thay bằng ID thực tế tương ứng. `<NGAY_MAI>`, `<HOM_QUA>`, `<THANG_SAU>` và `<THANG_NAY>` được xác định theo múi giờ Asia/Ho_Chi_Minh. Tài khoản đã đăng nhập và đã đổi mật khẩu bắt buộc; request ghi dùng CSRF hợp lệ.

### FEE-01.1 — ADMIN đọc danh mục loại phí

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Danh mục đã được migration V8 tạo..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-types.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; danh sách có `MANAGEMENT_FEE` đơn vị `m²/tháng` và `WASTE_FEE` đơn vị `hộ/tháng`.
- **Kiểm sau:** Đếm hai loại phí trong response.
- **Nguồn:** FEE-01; `tests/00-outline/feature-inventory.md:133`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:35`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeResponse.java:6`.

### FEE-01.2 — ACCOUNTANT đọc danh mục loại phí

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Danh mục đã được migration V8 tạo..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-types.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; có hai mã `MANAGEMENT_FEE`, `WASTE_FEE`.
- **Kiểm sau:** Đối chiếu tên và đơn vị của từng loại phí.
- **Nguồn:** FEE-01; `tests/00-outline/feature-inventory.md:133`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:35`.

### FEE-02.1 — Xem danh sách theo loại phí

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-rates?feeTypeId=1&page=0&size=20`.
- **Dữ liệu vào:** Loại phí 1 có ít nhất hai đơn giá ở các tháng khác nhau..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-rates?feeTypeId=1&page=0&size=20.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `content` theo `effectiveFrom` giảm dần rồi `id` giảm dần; mỗi dòng có `editable`.
- **Kiểm sau:** Đối chiếu các dòng với loại phí 1 và tháng hiện tại.
- **Nguồn:** FEE-02; `tests/00-outline/feature-inventory.md:134`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateResponse.java:8`.

### FEE-02.2 — Từ chối kích thước trang không hợp lệ

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-rates?size=101`.
- **Dữ liệu vào:** Tham số size=101..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-rates?size=101.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=INVALID_FILTER`.
- **Kiểm sau:** Gọi GET /api/v1/fee-rates?size=20 xác nhận danh sách còn đọc được.
- **Nguồn:** FEE-02; `tests/00-outline/feature-inventory.md:134`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:41`.

### FEE-02.3 — Loại phí lạ trả trang rỗng

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-rates?feeTypeId=999999`.
- **Dữ liệu vào:** Không tồn tại loại phí ID 999999..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-rates?feeTypeId=999999.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `content=[]`, `totalElements=0`.
- **Kiểm sau:** Đối chiếu GET /api/v1/fee-types không chứa ID 999999.
- **Nguồn:** FEE-02; `tests/00-outline/feature-inventory.md:134`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:45`.

### FEE-03.1 — Thêm đơn giá tháng sau

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":7000.50,"effectiveFrom":"<THANG_SAU>-01"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 201; `unitPrice=7000.50`, `editable=true`.
- **Kiểm sau:** GET /api/v1/fee-rates?feeTypeId=1 thấy dòng mới; audit `FEE_RATE_CREATED`.
- **Nguồn:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:50`.

### FEE-03.2 — Không nhận giá bằng không

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":0,"effectiveFrom":"<THANG_SAU>-01"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; ProblemDetail không có thuộc tính `code`.
- **Kiểm sau:** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
- **Nguồn:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.

### FEE-03.3 — Không nhận giá âm

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":-1,"effectiveFrom":"<THANG_SAU>-01"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; ProblemDetail không có thuộc tính `code`.
- **Kiểm sau:** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
- **Nguồn:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.

### FEE-03.4 — Không nhận ba chữ số thập phân

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":1.001,"effectiveFrom":"<THANG_SAU>-01"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; ProblemDetail không có thuộc tính `code`.
- **Kiểm sau:** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
- **Nguồn:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.

### FEE-03.5 — Không nhận mười một chữ số nguyên

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Loại phí 1 tồn tại, chưa có giá tháng sau; `effectiveFrom=<THANG_SAU>-01`; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"feeTypeId":1,"unitPrice":10000000000,"effectiveFrom":"<THANG_SAU>-01"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400; ProblemDetail không có thuộc tính `code`.
- **Kiểm sau:** GET /api/v1/fee-rates?feeTypeId=1 xác nhận không tạo dòng tháng sau.
- **Nguồn:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/CreateFeeRateRequest.java:9`.

### FEE-03.6 — Không tạo trùng loại và tháng

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Đã có loại phí 1 tháng sau; body `{"feeTypeId":1,"unitPrice":7000,"effectiveFrom":"<THANG_SAU>-01"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=FEE_RATE_DUPLICATE_MONTH`.
- **Kiểm sau:** GET /api/v1/fee-rates?feeTypeId=1 xác nhận chỉ có một dòng tháng sau.
- **Nguồn:** FEE-03; `tests/00-outline/feature-inventory.md:135`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:57`.

### FEE-04.1 — Sửa giá tháng sau

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/fee-rates/301`.
- **Dữ liệu vào:** Giá 301 thuộc tháng sau, hiện là 7000; body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi PATCH cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `unitPrice=7500`, `editable=true`.
- **Kiểm sau:** GET /api/v1/fee-rates đối chiếu giá 301; audit `FEE_RATE_UPDATED`.
- **Nguồn:** FEE-04; `tests/00-outline/feature-inventory.md:136`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:73`.

### FEE-04.2 — Không sửa giá tháng hiện tại

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/fee-rates/302`.
- **Dữ liệu vào:** Giá 302 thuộc tháng hiện tại và là 7000; body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi PATCH cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=FEE_RATE_FROZEN`.
- **Kiểm sau:** GET /api/v1/fee-rates xác nhận giá 302 vẫn là 7000.
- **Nguồn:** FEE-04; `tests/00-outline/feature-inventory.md:136`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:89`.

### FEE-04.3 — Không sửa giá không tồn tại

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/fee-rates/999999`.
- **Dữ liệu vào:** ID 999999 không tồn tại; body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi PATCH cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 404, `code=FEE_RATE_NOT_FOUND`.
- **Kiểm sau:** GET /api/v1/fee-rates xác nhận không có dòng 999999.
- **Nguồn:** FEE-04; `tests/00-outline/feature-inventory.md:136`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:90`.

### FEE-05.1 — Xóa giá tháng sau

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `DELETE /api/v1/fee-rates/301`.
- **Dữ liệu vào:** Giá 301 thuộc tháng sau; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi DELETE đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 204.
- **Kiểm sau:** GET /api/v1/fee-rates xác nhận không còn 301; audit `FEE_RATE_DELETED`.
- **Nguồn:** FEE-05; `tests/00-outline/feature-inventory.md:137`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:82`.

### FEE-05.2 — Không xóa giá tháng hiện tại

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `DELETE /api/v1/fee-rates/302`.
- **Dữ liệu vào:** Giá 302 thuộc tháng hiện tại; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi DELETE đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=FEE_RATE_FROZEN`.
- **Kiểm sau:** GET /api/v1/fee-rates xác nhận vẫn có 302.
- **Nguồn:** FEE-05; `tests/00-outline/feature-inventory.md:137`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:89`.

### FEE-06.1 — Xem trước và làm tròn từng dòng

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=1&period=<THANG_SAU>`.
- **Dữ liệu vào:** Hộ 1 diện tích 10.00 m²; giá quản lý 7000.05 và giá rác 1000.50 có hiệu lực tháng sau..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-preview?householdId=1&period=<THANG_SAU>.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `lines` có tiền quản lý 70001, tiền rác 1001; `total=71002`.
- **Kiểm sau:** Đọc lại danh sách đơn giá; không có đơn giá mới do phép xem trước.
- **Nguồn:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculator.java:8`.

### FEE-06.2 — Thiếu đơn giá cho kỳ xem trước

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=1&period=2000-01`.
- **Dữ liệu vào:** Hộ 1 tồn tại; ít nhất một loại phí không có giá trước hoặc trong tháng 2000-01..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-preview?householdId=1&period=2000-01.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=FEE_RATE_UNAVAILABLE`.
- **Kiểm sau:** GET /api/v1/fee-rates xác nhận loại phí thiếu giá cho kỳ này.
- **Nguồn:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:43`.

### FEE-06.3 — Kỳ không đúng định dạng

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=1&period=2026-13`.
- **Dữ liệu vào:** Hộ 1 tồn tại; `period=2026-13`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=INVALID_PERIOD`.
- **Kiểm sau:** Gọi GET /api/v1/fee-rates xác nhận danh sách giá không đổi.
- **Nguồn:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:31`.

### FEE-06.4 — Thiếu kỳ xem trước

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=1`.
- **Dữ liệu vào:** Hộ 1 tồn tại; không gửi period..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=INVALID_PERIOD`.
- **Kiểm sau:** Gọi GET /api/v1/fee-rates xác nhận danh sách giá không đổi.
- **Nguồn:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeCalculationService.java:31`.

### FEE-06.5 — Thiếu mã hộ xem trước

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-preview?period=2026-11`.
- **Dữ liệu vào:** Không gửi householdId..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=VALIDATION`.
- **Kiểm sau:** Gọi GET /api/v1/fee-rates xác nhận danh sách giá không đổi.
- **Nguồn:** FEE-06; `tests/00-outline/feature-inventory.md:138`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/common/error/GlobalExceptionHandler.java:30`.

### FEE-07.1 — RESIDENT bị từ chối GET /api/v1/fee-types

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-types.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
- **Nguồn:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.

### FEE-07.2 — RESIDENT bị từ chối GET /api/v1/fee-rates

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/fee-rates`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-rates.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
- **Nguồn:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.

### FEE-07.3 — RESIDENT bị từ chối GET /api/v1/fee-preview?householdId=1&period=2026-11

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/fee-preview?householdId=1&period=2026-11`.
- **Dữ liệu vào:** Không có body.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-preview?householdId=1&period=2026-11.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
- **Nguồn:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.

### FEE-07.4 — RESIDENT bị từ chối POST /api/v1/fee-rates

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":1,"unitPrice":7000,"effectiveFrom":"<THANG_SAU>-01"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST /api/v1/fee-rates với body đã nêu.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
- **Nguồn:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.

### FEE-07.5 — RESIDENT bị từ chối PATCH /api/v1/fee-rates/301

- **Vai trò:** RESIDENT.
- **Điểm vào:** `PATCH /api/v1/fee-rates/301`.
- **Dữ liệu vào:** Body `{"unitPrice":7500}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi PATCH /api/v1/fee-rates/301 với body đã nêu.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
- **Nguồn:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.

### FEE-07.6 — RESIDENT bị từ chối DELETE /api/v1/fee-rates/301

- **Vai trò:** RESIDENT.
- **Điểm vào:** `DELETE /api/v1/fee-rates/301`.
- **Dữ liệu vào:** Giá 301 tồn tại; `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi DELETE /api/v1/fee-rates/301.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** ACCOUNTANT gọi GET /api/v1/fee-rates xác nhận dữ liệu không đổi.
- **Nguồn:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.

### FEE-07.7 — ADMIN không được tạo giá

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Body `{"feeTypeId":1,"unitPrice":7000,"effectiveFrom":"<THANG_SAU>-01"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** GET /api/v1/fee-rates xác nhận không có dòng mới.
- **Nguồn:** FEE-07; `tests/00-outline/feature-inventory.md:139`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:63`.

### FEE-08.1 — Màn M-20 chưa có API trên main

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/fee-rates`.
- **Dữ liệu vào:** Nhánh main; M-20 API chưa có trên main, chỉ trên nhánh UI..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-rates.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; response có `content` và `editable`; chưa kiểm được màn M-20 API trên main.
- **Kiểm sau:** Đối chiếu `editable` với tháng hiệu lực từng dòng.
- **Nguồn:** FEE-08; `tests/00-outline/feature-inventory.md:140`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:39`; `tests/00-outline/feature-inventory.md:140`.

### FEE-08.2 — ADMIN đọc dữ liệu hỗ trợ màn M-20

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/fee-types`.
- **Dữ liệu vào:** Nhánh main; M-20 API chưa có trên main, chỉ trên nhánh UI..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi GET /api/v1/fee-types.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; có hai loại phí; chưa kiểm được chế độ chỉ xem của M-20 API trên main.
- **Kiểm sau:** Đối chiếu hai mã loại phí với danh mục.
- **Nguồn:** FEE-08; `tests/00-outline/feature-inventory.md:140`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/FeeRateService.java:35`; `tests/00-outline/feature-inventory.md:140`.

## Điểm chưa xác định

- FEE-03: Lỗi Bean Validation không có `code` theo `backend/src/main/java/vn/edu/se10/bluemoon/common/error/GlobalExceptionHandler.java:23`.
- FEE-08: Màn M-20 API chưa có trên main, chỉ trên nhánh UI; không có bước UI trong các ca này.
