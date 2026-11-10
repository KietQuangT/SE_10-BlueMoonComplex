# Ca kiểm thử hóa đơn và lịch sử đóng phí cho cư dân (Feature 10 / S10 / PBI-013)

Tài liệu đặc tả các ca kiểm thử cho chức năng tra cứu hóa đơn, chi tiết snapshot dòng phí, lịch sử phiếu thu, lịch sử khấu trừ số dư thừa (credit) và tóm tắt công nợ dành cho cư dân (Feature 10 / S10, WBS 2.4.1.2).

Các ID số trong dữ liệu (`1`, `10`, `20`, `42`, `100`, `200`, `999999`) là ID mẫu đã được tạo trước trên môi trường kiểm thử riêng; thay bằng ID thực tế tương ứng. `<THANG_NAY>`, `<THANG_TRUOC>`, `<THANG_SAU>`, `<HOM_NAY>` được xác định theo múi giờ `Asia/Ho_Chi_Minh`. Tài khoản cư dân thử nghiệm có `role=RESIDENT`, `status=ACTIVE`, `mustChangePassword=false` và đã liên kết với một hộ gia đình `ACTIVE`. Toàn bộ endpoint của cư dân là chỉ đọc (`GET`), không phát sinh thay đổi dữ liệu trên server và bắt buộc trả header `Cache-Control: no-store`. Tuyệt đối không dùng dữ liệu cư dân thật hoặc secret thật. Trạng thái mặc định của mọi ca là `Not run`.

---

## 1. Phân quyền và bảo mật truy cập (RBAC & Session)

### RIN-01.1 — Cư dân hợp lệ truy cập danh sách hóa đơn thành công

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices`.
- **Dữ liệu vào:** Phiên đăng nhập của tài khoản cư dân có `householdId=10` (hộ `ACTIVE`), `mustChangePassword=false`.
- **Các bước:**
  1. Đăng nhập với tài khoản cư dân hợp lệ.
  2. Gửi request `GET /api/v1/resident/invoices?page=0&size=20`.
  3. Đối chiếu HTTP status code, headers và cấu trúc body JSON.
- **Kỳ vọng:** HTTP 200; header `Cache-Control: no-store`; body trả về `PageResponse` chứa danh sách hóa đơn thuộc hộ `10`, `page=0`, `size=20`.
- **Kiểm sau:** Không có bất kỳ dòng log lỗi nào trên server.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/ResidentInvoiceController.java:32`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:54`.

### RIN-01.2 — Từ chối truy cập khi chưa đăng nhập (ANON)

- **Vai trò:** ANON (chưa đăng nhập).
- **Điểm vào:** `GET /api/v1/resident/invoices`, `GET /api/v1/resident/invoices/42`, `GET /api/v1/resident/receipts`, `GET /api/v1/resident/credit-allocations`, `GET /api/v1/resident/billing-summary`.
- **Dữ liệu vào:** Không có session cookie hoặc session đã hết hạn.
- **Các bước:**
  1. Gửi request `GET` tới lần lượt 5 endpoint trên mà không kèm cookie `SESSION`.
  2. Ghi nhận HTTP status code trả về.
- **Kỳ vọng:** Tất cả 5 endpoint đều trả HTTP 401 Unauthorized.
- **Kiểm sau:** Không để lộ dữ liệu nội bộ trong response body.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:85`.

### RIN-01.3 — Chặn truy cập chéo vai trò: ADMIN và ACCOUNTANT nhận 403

- **Vai trò:** ADMIN, ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/resident/invoices`, `GET /api/v1/resident/invoices/42`, `GET /api/v1/resident/receipts`, `GET /api/v1/resident/credit-allocations`, `GET /api/v1/resident/billing-summary`.
- **Dữ liệu vào:** Phiên đăng nhập hợp lệ của vai trò `ADMIN` hoặc `ACCOUNTANT`.
- **Các bước:**
  1. Đăng nhập với vai trò `ADMIN`, gửi request tới 5 endpoint trên.
  2. Đăng nhập với vai trò `ACCOUNTANT`, gửi request tới 5 endpoint trên.
  3. Ghi nhận HTTP status code.
- **Kỳ vọng:** Toàn bộ các request trả HTTP 403 Forbidden.
- **Kiểm sau:** Không lộ danh sách hay chi tiết hóa đơn cư dân qua các endpoint này.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:75`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:54-58`.

### RIN-01.4 — Cư dân có cờ đổi mật khẩu lần đầu bị chặn truy cập

- **Vai trò:** RESIDENT (chưa đổi mật khẩu ban đầu).
- **Điểm vào:** `GET /api/v1/resident/invoices`.
- **Dữ liệu vào:** Tài khoản cư dân có `must_change_password=true`.
- **Các bước:**
  1. Đăng nhập với tài khoản cư dân có `must_change_password=true`.
  2. Gửi request `GET /api/v1/resident/invoices`.
- **Kỳ vọng:** HTTP 403 Forbidden; ProblemDetail có `code=PASSWORD_CHANGE_REQUIRED`.
- **Kiểm sau:** Giao diện điều hướng người dùng tới màn hình đổi mật khẩu bắt buộc.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D14, D19; `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:91`.

### RIN-01.5 — Từ chối cư dân chưa gắn hộ hoặc hộ không tồn tại

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices`.
- **Dữ liệu vào:** Tài khoản cư dân có `householdId=null` hoặc `householdId=999999` không tồn tại trong hệ thống.
- **Các bước:**
  1. Thiết lập tài khoản cư dân không có `householdId`.
  2. Gửi request `GET /api/v1/resident/invoices`.
- **Kỳ vọng:** HTTP 403 Forbidden; message `Không có quyền truy cập.`.
- **Kiểm sau:** Không cho phép truy vấn cơ sở dữ liệu khi không xác định được hộ gia đình.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:83`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:98`.

### RIN-01.6 — Bắt buộc header Cache-Control no-store trên tất cả endpoint

- **Vai trò:** RESIDENT.
- **Điểm vào:** Cả 5 endpoint `/api/v1/resident/*`.
- **Dữ liệu vào:** Phiên cư dân hợp lệ.
- **Các bước:**
  1. Gửi request `GET` tới lần lượt: `/resident/invoices`, `/resident/invoices/42`, `/resident/receipts`, `/resident/credit-allocations`, `/resident/billing-summary`.
  2. Kiểm tra response header `Cache-Control`.
- **Kỳ vọng:** Tất cả response đều chứa header `Cache-Control: no-store` để ngăn trình duyệt hoặc proxy trung gian lưu trữ thông tin tài chính nhạy cảm.
- **Kiểm sau:** Đối chiếu header trên công cụ kiểm thử.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/ResidentInvoiceController.java:43,54,68,82,92`.

### RIN-01.7 — Vô hiệu hóa tài khoản chặn ngay phiên làm việc kế tiếp

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices`.
- **Dữ liệu vào:** Cư dân đang có session đăng nhập hợp lệ; quản trị viên thực hiện vô hiệu hóa tài khoản (`POST /api/v1/accounts/{id}/disable`).
- **Các bước:**
  1. ADMIN thực hiện vô hiệu hóa tài khoản cư dân.
  2. Cư dân gửi request `GET /api/v1/resident/invoices` bằng session hiện tại.
- **Kỳ vọng:** HTTP 401 hoặc 403; không đọc dữ liệu hóa đơn sau khi tài khoản bị khóa/vô hiệu.
- **Kiểm sau:** Kiểm tra không phát sinh query đọc billing trong DB cho tài khoản đã vô hiệu hóa.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/test/java/vn/edu/se10/bluemoon/identity/IdentitySessionIT.java`; `docs/DECISIONS.md` D18.

---

## 2. Bộ lọc và phân trang danh sách hóa đơn (Invoices Query)

### RIN-02.1 — Lọc hóa đơn theo kỳ thu hợp lệ (period)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices?period=2026-11`.
- **Dữ liệu vào:** Kỳ thu `2026-11` hợp lệ; hộ có hóa đơn trong kỳ này.
- **Các bước:**
  1. Gửi `GET /api/v1/resident/invoices?period=2026-11`.
  2. Kiểm tra các dòng trong mảng `content`.
- **Kỳ vọng:** HTTP 200; tất cả các phần tử trong `content` đều có `period="2026-11"`; sắp xếp giảm dần theo `period DESC, id DESC`.
- **Kiểm sau:** Không có hóa đơn của kỳ khác lọt vào kết quả.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:67`.

### RIN-02.2 — Lọc hóa đơn theo trạng thái thanh toán (paymentStatus)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices?paymentStatus=<STATUS>`.
- **Dữ liệu vào:** Lần lượt các trạng thái: `OUTSTANDING`, `UNPAID`, `PARTIALLY_PAID`, `PAID`, `CANCELLED`.
- **Các bước:**
  1. Gửi request với từng giá trị trạng thái trên.
  2. Kiểm tra điều kiện dữ liệu của kết quả:
     - `OUTSTANDING`: hóa đơn `ISSUED` và `remaining > 0`.
     - `UNPAID`: hóa đơn `ISSUED`, `settled == 0` và `total > 0`.
     - `PARTIALLY_PAID`: hóa đơn `ISSUED`, `settled > 0` và `settled < total`.
     - `PAID`: hóa đơn `ISSUED` và `remaining == 0`.
     - `CANCELLED`: hóa đơn có `invoiceStatus="CANCELLED"`.
- **Kỳ vọng:** HTTP 200 cho từng request; các dòng trả về thỏa mãn đúng điều kiện lọc tương ứng.
- **Kiểm sau:** Không xuất hiện dòng sai lệch trạng thái.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:35-42`.

### RIN-02.3 — Phân trang danh sách hóa đơn hợp lệ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices?page=1&size=5`.
- **Dữ liệu vào:** Hộ có nhiều hơn 5 hóa đơn; yêu cầu trang 1 với kích thước 5.
- **Các bước:**
  1. Gửi `GET /api/v1/resident/invoices?page=1&size=5`.
  2. Đối chiếu `page`, `size`, `totalElements`, `totalPages` và số phần tử trong `content`.
- **Kỳ vọng:** HTTP 200; `page=1`, `size=5`; mảng `content` có tối đa 5 phần tử; tính đúng `totalPages`.
- **Kiểm sau:** Kiểm tra offset phân trang tính đúng `(long) page * size`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:73`.

### RIN-02.4 — Từ chối tham số truy vấn ngoài allowlist

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices?householdId=123`.
- **Dữ liệu vào:** Tham số truy vấn không nằm trong allowlist (`period`, `paymentStatus`, `page`, `size`), ví dụ `householdId`, `userId`, `accountId`, `foo`.
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/invoices?householdId=123`.
  2. Ghi nhận response.
- **Kỳ vọng:** HTTP 400 Bad Request; ProblemDetail có `code=INVALID_FILTER`.
- **Kiểm sau:** Ngăn chặn việc cư dân can thiệp bộ lọc bằng tham số nội bộ.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/ResidentInvoiceController.java:95-103`.

### RIN-02.5 — Từ chối định dạng kỳ thu không hợp lệ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices?period=2026-13`.
- **Dữ liệu vào:** Giá trị `period` sai định dạng (`2026-13`, `2026-00`, `2026/11`, `invalid`).
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/invoices?period=2026-13`.
- **Kỳ vọng:** HTTP 400 Bad Request; ProblemDetail có `code=INVALID_FILTER`.
- **Kiểm sau:** Không gửi query lỗi xuống cơ sở dữ liệu.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:99-116`.

### RIN-02.6 — Từ chối trạng thái thanh toán không nằm trong danh mục

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices?paymentStatus=paid`.
- **Dữ liệu vào:** Giá trị `paymentStatus` viết thường (`paid`), hoặc trạng thái lạ (`PENDING`, `UNKNOWN`, `123`).
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/invoices?paymentStatus=paid`.
- **Kỳ vọng:** HTTP 400 Bad Request; ProblemDetail có `code=INVALID_FILTER`.
- **Kiểm sau:** Bộ lọc chỉ chấp nhận các giá trị in hoa: `ALL`, `OUTSTANDING`, `UNPAID`, `PARTIALLY_PAID`, `PAID`, `CANCELLED`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:118-127`.

### RIN-02.7 — Từ chối tham số phân trang âm hoặc vượt ngưỡng

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices?page=-1` và `?size=0` và `?size=101`.
- **Dữ liệu vào:** `page=-1`, `size=0` hoặc `size=101`.
- **Các bước:**
  1. Gửi request với `page=-1`.
  2. Gửi request với `size=0`.
  3. Gửi request với `size=101`.
- **Kỳ vọng:** Tất cả đều trả HTTP 400 Bad Request; ProblemDetail có `code=INVALID_FILTER`.
- **Kiểm sau:** Giới hạn `size` từ 1 đến 100, `page >= 0`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:93-97`.

---

## 3. Chi tiết hóa đơn và tính bất biến snapshot (Invoice Detail)

### RIN-03.1 — Xem chi tiết hóa đơn hợp lệ thuộc hộ của mình

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/42`.
- **Dữ liệu vào:** Hóa đơn `42` thuộc hộ của cư dân đăng nhập (`householdId=10`).
- **Các bước:**
  1. Gửi `GET /api/v1/resident/invoices/42`.
  2. Kiểm tra dữ liệu JSON trả về.
- **Kỳ vọng:** HTTP 200; `invoiceId=42`, `period`, `householdCode`, `apartmentCode`, `dueDate`, `issuedAt`, `invoiceStatus`, `paymentStatus`, `total`, `settled`, `remaining`, và mảng `lines` chứa đầy đủ các dòng phí bóc tách.
- **Kiểm sau:** Toàn bộ số tiền định dạng chuỗi số nguyên VND (hoặc 2 chữ số thập phân cho đơn giá/diện tích).
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:203-217`.

### RIN-03.2 — Dòng phí snapshot bóc tách đúng công thức và làm tròn HALF_UP

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/42`.
- **Dữ liệu vào:** Hóa đơn gồm phí quản lý (`MANAGEMENT_FEE`, tính theo `AREA`) và phí rác (`WASTE_FEE`, tính theo `PER_HOUSEHOLD`).
- **Các bước:**
  1. Đọc mảng `lines` trong response.
  2. Kiểm tra dòng phí diện tích: `calculationMethod="AREA"`, `unit="M2"`, `areaM2` bằng diện tích căn hộ, `amount = round(areaM2 * unitPrice)`.
  3. Kiểm tra dòng phí cố định: `calculationMethod="PER_HOUSEHOLD"`, `unit="HOUSEHOLD"`, `areaM2=null`, `amount = unitPrice`.
  4. Kiểm tra tổng `total = sum(lines[].amount)`.
- **Kỳ vọng:** HTTP 200; các trường khớp chính xác công thức snapshot bất biến của kỳ phát hành.
- **Kiểm sau:** Không bị ảnh hưởng nếu bảng đơn giá hiện tại (`fee_rates`) bị thay đổi sau thời điểm phát hành.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D25; `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:186-200`.

### RIN-03.3 — Tính toán số tiền đã trả và còn nợ chính xác

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/42`.
- **Dữ liệu vào:** Hóa đơn có phiếu thu tiền mặt POSTED 200.000₫ và khấu trừ credit ACTIVE 50.000₫ trên tổng 530.000₫.
- **Các bước:**
  1. Gửi request đọc chi tiết hóa đơn.
  2. Kiểm tra `settled` và `remaining`.
- **Kỳ vọng:** `settled = "250000"`, `remaining = "280000"`, `paymentStatus = "PARTIALLY_PAID"`.
- **Kiểm sau:** Nếu `settled == 0` thì `paymentStatus = "UNPAID"`; nếu `remaining == 0` thì `paymentStatus = "PAID"`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:121-122,147-156`.

### RIN-03.4 — Hóa đơn thuộc kỳ bị hủy hiển thị trạng thái CANCELLED

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/43`.
- **Dữ liệu vào:** Hóa đơn `43` thuộc kỳ thu đã bị quản trị viên hủy (`CANCELLED`).
- **Các bước:**
  1. Gửi request xem chi tiết hóa đơn `43`.
- **Kỳ vọng:** HTTP 200; `invoiceStatus="CANCELLED"`, `paymentStatus="CANCELLED"`, `settled="0"`, `remaining="0"`, `cancelledAt` khác null.
- **Kiểm sau:** Không tính nợ cho hóa đơn của kỳ đã hủy.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:148`.

### RIN-03.5 — Kiểm tra ID hóa đơn âm hoặc bằng 0

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/0` và `GET /api/v1/resident/invoices/-5`.
- **Dữ liệu vào:** `invoiceId=0` hoặc `invoiceId=-5`.
- **Các bước:**
  1. Gửi request tới endpoint với ID không dương.
- **Kỳ vọng:** HTTP 400 Bad Request; ProblemDetail có `code=VALIDATION`.
- **Kiểm sau:** Chặn ngay tại tầng Service trước khi gọi ReadStore.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:49-51`.

### RIN-03.6 — Chặn query parameters trên endpoint chi tiết hóa đơn

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/42?foo=bar`.
- **Dữ liệu vào:** Bất kỳ tham số query string nào gửi kèm vào endpoint chi tiết.
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/invoices/42?foo=bar`.
- **Kỳ vọng:** HTTP 400 Bad Request; ProblemDetail có `code=INVALID_FILTER`.
- **Kiểm sau:** Endpoint chi tiết không chấp nhận bất kỳ query parameter nào (`EMPTY_QUERY_KEYS`).
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/ResidentInvoiceController.java:52`.

### RIN-03.7 — Từ chối truy cập hóa đơn không tồn tại hoặc thuộc hộ khác (IDOR)

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/999`.
- **Dữ liệu vào:** Hóa đơn `999` không tồn tại, hoặc là hóa đơn của hộ khác (`householdId=20`).
- **Các bước:**
  1. Cư dân thuộc hộ `10` gửi request `GET /api/v1/resident/invoices/999`.
- **Kỳ vọng:** HTTP 404 Not Found; ProblemDetail có `code=INVOICE_NOT_FOUND`, `detail="Không tìm thấy tài nguyên."`. Tuyệt đối không trả HTTP 403 để tránh rò rỉ việc tồn tại ID của hộ khác.
- **Kiểm sau:** Không tiết lộ bất kỳ thông tin nào của hóa đơn hộ khác.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:173-175`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoicePrivacyTest.java`.

### RIN-03.8 — Bảo vệ quyền riêng tư: Whitelist DTO chi tiết hóa đơn

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices/42`.
- **Dữ liệu vào:** Hóa đơn hợp lệ.
- **Các bước:**
  1. Gửi request và kiểm tra cấu trúc JSON toàn bộ response.
- **Kỳ vọng:** Response KHÔNG chứa các trường nhạy cảm hoặc kỹ thuật: `feeRateId`, `actor`, `householdId`, `email`, `phone`, `accountantName`.
- **Kiểm sau:** Đảm bảo nguyên tắc Privacy Whitelist của hệ thống.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:260-266`.

---

## 4. Lịch sử phiếu thu đã nộp (Receipts History)

### RIN-04.1 — Cư dân xem toàn bộ danh sách phiếu thu của hộ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/receipts?page=0&size=20`.
- **Dữ liệu vào:** Phiên cư dân hợp lệ có các phiếu thu đã lập.
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/receipts`.
- **Kỳ vọng:** HTTP 200; `PageResponse` chứa các phiếu thu của hộ mình, sắp xếp theo `r.id DESC`.
- **Kiểm sau:** Mỗi dòng chứa `receiptId`, `invoiceId`, `period`, `paidOn`, `recordedAt`, `amount`, `appliedAmount`, `creditAmount`, `status`, `voidedAt`, `hasLateReason`, `hasVoidReason`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:257-274`.

### RIN-04.2 — Lọc phiếu thu theo invoiceId cụ thể

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/receipts?invoiceId=42`.
- **Dữ liệu vào:** Hóa đơn `42` thuộc hộ cư dân.
- **Các bước:**
  1. Gửi request với tham số `invoiceId=42`.
- **Kỳ vọng:** HTTP 200; tất cả phiếu thu trả về đều có `invoiceId=42`.
- **Kiểm sau:** Không có phiếu thu của hóa đơn khác.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:243`.

### RIN-04.3 — Lọc phiếu thu theo trạng thái POSTED và VOIDED

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/receipts?status=POSTED` và `?status=VOIDED`.
- **Dữ liệu vào:** Giá trị trạng thái `POSTED` hoặc `VOIDED`.
- **Các bước:**
  1. Gửi request lọc `status=POSTED`.
  2. Gửi request lọc `status=VOIDED`.
- **Kỳ vọng:** HTTP 200; kết quả trả về đúng theo trạng thái tương ứng. Phiếu `VOIDED` có `voidedAt` khác null.
- **Kiểm sau:** Truyền `status=ALL` hoặc bỏ trống trả về cả 2 loại trạng thái.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:129-138`.

### RIN-04.4 — Từ chối lọc phiếu thu theo invoiceId thuộc hộ khác

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/receipts?invoiceId=999`.
- **Dữ liệu vào:** `invoiceId=999` thuộc hộ khác hoặc không tồn tại.
- **Các bước:**
  1. Gửi request lọc phiếu thu với `invoiceId=999`.
- **Kỳ vọng:** HTTP 404 Not Found; ProblemDetail có `code=INVOICE_NOT_FOUND`.
- **Kiểm sau:** Chặn việc suy đoán danh sách phiếu thu của hộ khác qua query param.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:229-234`.

### RIN-04.5 — Che giấu PII và thông tin nội bộ trong phiếu thu

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/receipts`.
- **Dữ liệu vào:** Phiếu thu có lý do nộp muộn (`late_reason`) và lý do hủy (`void_reason`).
- **Các bước:**
  1. Đọc response chi tiết các phiếu thu.
- **Kỳ vọng:** Chỉ trả cờ boolean `hasLateReason=true/false` và `hasVoidReason=true/false`. Tuyệt đối KHÔNG trả nội dung chuỗi `lateReason`, `voidReason`, không trả người thu `recordedBy`, người hủy `voidedBy`, không trả `debtAfter`, không trả `householdId`.
- **Kiểm sau:** Tuân thủ quy định bảo vệ dữ liệu cá nhân D28 và NFR-003.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D28; `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:286-292`.

---

## 5. Lịch sử khấu trừ credit / nộp thừa (Credit Allocations)

### RIN-05.1 — Cư dân xem toàn bộ lịch sử khấu trừ credit của hộ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/credit-allocations?page=0&size=20`.
- **Dữ liệu vào:** Phiên cư dân hợp lệ có các đợt khấu trừ credit bù vào hóa đơn.
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/credit-allocations`.
- **Kỳ vọng:** HTTP 200; danh sách `PageResponse` chứa các bản ghi khấu trừ của hộ, sắp xếp `a.id DESC`.
- **Kiểm sau:** Mỗi bản ghi có `allocationId`, `invoiceId`, `period`, `amount`, `status`, `recordedAt`, `reversedAt`, `hasReverseReason`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:314-325`.

### RIN-05.2 — Lọc khấu trừ credit theo invoiceId cụ thể

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/credit-allocations?invoiceId=42`.
- **Dữ liệu vào:** Hóa đơn `42` thuộc hộ cư dân.
- **Các bước:**
  1. Gửi request lọc với `invoiceId=42`.
- **Kỳ vọng:** HTTP 200; toàn bộ kết quả có `invoiceId=42`.
- **Kiểm sau:** Nếu `invoiceId` thuộc hộ khác trả HTTP 404 `INVOICE_NOT_FOUND`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:286-291`.

### RIN-05.3 — Lọc khấu trừ credit theo trạng thái ACTIVE và REVERSED

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/credit-allocations?status=ACTIVE` và `?status=REVERSED`.
- **Dữ liệu vào:** Trạng thái `ACTIVE` (đang có hiệu lực bù trừ) hoặc `REVERSED` (đã đảo phân bổ).
- **Các bước:**
  1. Gửi request lọc với từng trạng thái.
- **Kỳ vọng:** HTTP 200; kết quả lọc chính xác. Bản ghi `REVERSED` có `reversedAt` khác null và `hasReverseReason=true/false`.
- **Kiểm sau:** Bỏ trống hoặc `status=ALL` trả cả hai loại.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:140-149`.

### RIN-05.4 — Che giấu PII và nguồn tiền phân bổ credit

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/credit-allocations`.
- **Dữ liệu vào:** Bản ghi khấu trừ credit đã bị đảo phân bổ.
- **Các bước:**
  1. Kiểm tra các trường trong từng phần tử `content`.
- **Kỳ vọng:** Response KHÔNG chứa nội dung lý do đảo `reverseReason`, không chứa `recordedBy`, `reversedBy`, không chứa mảng chi tiết nguồn `sources` (các receipt id đóng góp vào credit), không chứa `householdId`.
- **Kiểm sau:** Chỉ hiển thị cờ `hasReverseReason`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:310-314`.

---

## 6. Tóm tắt công nợ cư dân (Billing Summary)

### RIN-06.1 — Xem tóm tắt công nợ và số dư nộp thừa chính xác

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/billing-summary`.
- **Dữ liệu vào:** Hộ có 2 hóa đơn còn nợ (kỳ 2026-10 nợ 100.000₫ hạn 2026-10-31; kỳ 2026-11 nợ 200.000₫ hạn 2026-11-30) và số dư nộp thừa credit 50.000₫.
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/billing-summary`.
  2. Đối chiếu các trường dữ liệu trả về.
- **Kỳ vọng:** HTTP 200; `totalOutstanding="300000"`, `outstandingInvoiceCount=2`, `nearestDueDate="2026-10-31"`, `nearestDuePeriod="2026-10"`, `creditBalance="50000"`.
- **Kiểm sau:** `nearestDueDate` lấy hóa đơn còn nợ có ngày đến hạn sớm nhất theo thứ tự tăng dần.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:328-389`.

### RIN-06.2 — Hộ không còn nợ hoặc chưa phát sinh hóa đơn

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/billing-summary`.
- **Dữ liệu vào:** Hộ đã thanh toán hết toàn bộ hóa đơn (`remaining == 0`).
- **Các bước:**
  1. Gửi request `GET /api/v1/resident/billing-summary`.
- **Kỳ vọng:** HTTP 200; `totalOutstanding="0"`, `outstandingInvoiceCount=0`, `nearestDueDate=null`, `nearestDuePeriod=null`, `creditBalance` phản ánh đúng số dư credit (nếu có hoặc "0").
- **Kiểm sau:** Không trả lỗi null pointer exception.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/ResidentInvoiceReadStore.java:366-367`.

### RIN-06.3 — Xử lý số tiền lớn vượt quá 2^53 dưới dạng String

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/billing-summary`.
- **Dữ liệu vào:** Hộ có số tiền lớn (ví dụ `9007199254740993000` > `Number.MAX_SAFE_INTEGER`).
- **Các bước:**
  1. Đọc giá trị chuỗi số tiền `totalOutstanding` và `creditBalance`.
- **Kỳ vọng:** Dữ liệu trả về đúng nguyên bản chuỗi số `9007199254740993000`, không bị làm tròn hoặc chuyển thành dạng khoa học `9.007e18`.
- **Kiểm sau:** Frontend nhận chuỗi và parse/định dạng an toàn.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceApiTest.java:332-341`.

### RIN-06.4 — Từ chối query parameter lạ trên billing-summary

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/billing-summary?foo=bar`.
- **Dữ liệu vào:** Tham số truy vấn bất kỳ trên endpoint summary.
- **Các bước:**
  1. Gửi request kèm tham số.
- **Kỳ vọng:** HTTP 400 Bad Request; ProblemDetail có `code=INVALID_FILTER`.
- **Kiểm sau:** Endpoint summary không nhận tham số query (`EMPTY_QUERY_KEYS`).
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/ResidentInvoiceController.java:90`.

---

## 7. Tính nhất quán giao dịch và cô lập dữ liệu (Consistency & Data Isolation)

### RIN-07.1 — Cô lập dữ liệu tuyệt đối giữa các hộ gia đình

- **Vai trò:** RESIDENT.
- **Điểm vào:** Cả 5 endpoint resident billing.
- **Dữ liệu vào:** Hộ A (`householdId=10`) và Hộ B (`householdId=20`) đều có hóa đơn, phiếu thu và credit riêng.
- **Các bước:**
  1. Đăng nhập bằng tài khoản thuộc Hộ A.
  2. Lấy danh sách hóa đơn, phiếu thu, credit và summary.
  3. Thử truyền `invoiceId` của Hộ B vào endpoint chi tiết, receipts và allocations.
- **Kỳ vọng:** Cư dân Hộ A chỉ thấy dữ liệu của Hộ A; mọi yêu cầu trỏ tới tài nguyên của Hộ B đều trả HTTP 404 `INVOICE_NOT_FOUND`. Tuyệt đối không có rò rỉ dữ liệu chéo hộ.
- **Kiểm sau:** Kiểm tra mệnh đề SQL luôn chứa `WHERE ... household_id = :householdId`.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceRegressionIT.java`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoicePrivacyTest.java`.

### RIN-07.2 — Tính nhất quán snapshot đọc với mức cô lập REPEATABLE_READ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/resident/invoices`.
- **Dữ liệu vào:** Kế toán thực hiện gạch nợ / tạo phiếu thu mới và commit ngay giữa 2 truy vấn (đếm tổng số bản ghi và đọc dữ liệu trang) của cư dân.
- **Các bước:**
  1. Kích hoạt giao dịch đọc của cư dân với `@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)`.
  2. Đan xen giao dịch ghi phiếu thu từ luồng kế toán.
- **Kỳ vọng:** Dữ liệu đọc của cư dân đảm bảo tính nhất quán snapshot, không bị đọc bẩn (dirty read), không bị lệch giữa `totalCount` và số dòng trả về trong cùng một response.
- **Kiểm sau:** Không xảy ra hiện tượng phantom read trong phiên làm việc.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ResidentInvoiceService.java:37,46,55,66,77`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/ResidentInvoiceSnapshotIT.java`.

### RIN-07.3 — Tính chất chỉ đọc: Chặn toàn bộ thao tác ghi của cư dân

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST`, `PATCH`, `PUT`, `DELETE` tới các route `/api/v1/resident/*`.
- **Dữ liệu vào:** Request có method thay đổi dữ liệu kèm body và CSRF token.
- **Các bước:**
  1. Cư dân gửi request `POST /api/v1/resident/invoices`.
  2. Cư dân gửi request `DELETE /api/v1/resident/invoices/42`.
  3. Cư dân gửi request `POST /api/v1/resident/receipts`.
- **Kỳ vọng:** Toàn bộ các request ghi bị chặn với HTTP 403 Forbidden hoặc HTTP 405 Method Not Allowed. Cư dân tuyệt đối không có quyền tự thay đổi hóa đơn, tự gạch nợ hoặc tạo phiếu thu.
- **Kiểm sau:** Database không phát sinh bất kỳ bản ghi mới nào.
- **Trạng thái:** Not run.
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:53-58,85`.

---

## 8. Giao diện web danh sách hóa đơn M-32 (/resident/invoices)

### RIN-08.1 — Hiển thị đầy đủ 3 trạm tóm tắt công nợ (Summary stations)

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện `/resident/invoices` (M-32) ở chế độ API.
- **Dữ liệu vào:** Cư dân có tổng dư nợ 383.500₫, 1 hóa đơn còn nợ, hạn nộp 2026-11-30 và số dư credit 0₫.
- **Các bước:**
  1. Đăng nhập cư dân và mở màn hình `/resident/invoices`.
  2. Quan sát 3 thẻ trạm tóm tắt ở phần đầu trang.
- **Kỳ vọng:**
  - Thẻ 1: Tổng tiền nợ hiển thị đúng định dạng tiền tệ `383.500 đ`, kèm số hóa đơn còn nợ.
  - Thẻ 2: Kỳ và ngày đến hạn gần nhất (`Tháng 11/2026 - 30/11/2026`).
  - Thẻ 3: Số dư nộp thừa hiển thị `0 đ`, kèm văn bản giải thích số dư sẽ tự động khấu trừ vào các kỳ tiếp theo.
- **Kiểm sau:** Không hiển thị `NaN` hay `undefined`.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ApiInvoicesPage.tsx:160-200`; `frontend/src/test/s10-invoices.test.tsx`.

### RIN-08.2 — Áp dụng bộ lọc trạng thái, kỳ thu và nút xóa bộ lọc

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện `/resident/invoices`.
- **Dữ liệu vào:** Lọc theo trạng thái `PARTIALLY_PAID` hoặc kỳ `2026-11`.
- **Các bước:**
  1. Chọn trạng thái `Chưa đóng đủ` (`PARTIALLY_PAID`) trên dropdown bộ lọc và nhấn "Áp dụng".
  2. Kiểm tra danh sách hiển thị và trang hiện tại được đặt lại về `0`.
  3. Nhấn nút "Xóa lọc".
- **Kỳ vọng:** Bước 1 gọi API kèm đúng query string `paymentStatus=PARTIALLY_PAID&page=0`. Bước 3 đặt lại trạng thái dropdown về `Tất cả` và tải lại danh sách đầy đủ.
- **Kiểm sau:** Không giữ lại tham số lọc cũ khi đã xóa lọc.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ApiInvoicesPage.tsx:75-90`; `frontend/src/test/s10-invoices.test.tsx`.

### RIN-08.3 — Bố cục responsive mobile màn hình hẹp (< 768px / 360px)

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện `/resident/invoices` trên thiết bị di động (viewport 360px - 393px).
- **Dữ liệu vào:** Viewport màn hình hẹp, không gian hiển thị hạn chế.
- **Các bước:**
  1. Mở màn hình danh sách hóa đơn với viewport 360px.
  2. Kiểm tra hiển thị bảng dữ liệu.
- **Kỳ vọng:** Bảng hóa đơn tự động chuyển đổi sang dạng thẻ xếp chồng (stacked card layout); không xuất hiện thanh cuộn ngang trang (`scrollWidth <= innerWidth`); các nút bấm và nhãn không bị tràn viền hoặc đè lên nhau.
- **Kiểm sau:** Hoạt động tốt trên cả theme sáng và tối.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/s10.css`; `docs/VALIDATION.md` mục S10 UI.

### RIN-08.4 — Tách biệt chế độ API và Demo: Tuyệt đối không fallback sang dữ liệu mẫu

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện `/resident/invoices` ở chế độ `api`.
- **Dữ liệu vào:** Backend ngắt kết nối hoặc trả HTTP 500.
- **Các bước:**
  1. Thiết lập ứng dụng ở chế độ `api`.
  2. Mô phỏng lỗi kết nối API khi tải danh sách hóa đơn.
- **Kỳ vọng:** Giao diện hiển thị trạng thái lỗi kết nối kèm nút "Thử lại"; tuyệt đối KHÔNG tự ý chuyển sang hiển thị dữ liệu giả định của chế độ `demo`.
- **Kiểm sau:** Giữ vững ranh giới giữa dữ liệu thật và dữ liệu thử nghiệm.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ApiInvoicesPage.tsx:210-225`; `frontend/src/test/s10-invoices.test.tsx`.

### RIN-08.5 — Khả năng phục hồi khi lỗi mạng hoặc hết hạn phiên

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện `/resident/invoices`.
- **Dữ liệu vào:** API trả HTTP 401 do hết hạn phiên làm việc giữa chừng.
- **Các bước:**
  1. Đang xem danh sách hóa đơn, phiên hết hạn.
  2. Thực hiện thao tác chuyển trang hoặc lọc.
- **Kỳ vọng:** Ứng dụng phát hiện HTTP 401, không bị crash trắng trang; chuyển hướng người dùng an toàn về trang đăng nhập `/login`; không lưu thông tin nhạy cảm vào `localStorage`.
- **Kiểm sau:** Sau khi đăng nhập lại có thể tiếp tục sử dụng bình thường.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/test/s10-session.test.tsx`.

---

## 9. Giao diện web chi tiết hóa đơn M-33 (/resident/invoices/:id)

### RIN-09.1 — Hiển thị chi tiết snapshot dòng phí và các khoản thanh toán

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện `/resident/invoices/42` (M-33).
- **Dữ liệu vào:** Hóa đơn hợp lệ có các dòng phí bóc tách và thanh toán một phần.
- **Các bước:**
  1. Mở trang chi tiết hóa đơn `42`.
  2. Kiểm tra các khối thông tin trên màn hình.
- **Kỳ vọng:**
  - Tiêu đề hiển thị mã kỳ `Tháng 11/2026`, mã căn hộ và hạn nộp.
  - Bảng bóc tách chi tiết: hiển thị từng khoản phí (Phí quản lý, Phí rác), phương pháp tính, số lượng/diện tích, đơn giá và thành tiền.
  - Khối tổng kết: Tổng tiền hóa đơn, Số tiền đã thanh toán (dùng từ ngữ "Đã thanh toán" rõ ràng), Số tiền còn nợ.
- **Kiểm sau:** Các con số khớp chính xác với dữ liệu API backend.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ApiInvoiceDetailPage.tsx:160-240`; `frontend/src/test/s10-detail.test.tsx`.

### RIN-09.2 — Tích hợp tab Lịch sử đóng phí & bù trừ (ResidentBillingHistory)

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện chi tiết hóa đơn `/resident/invoices/42`.
- **Dữ liệu vào:** Hóa đơn đã có 1 phiếu thu tiền mặt và 1 lượt khấu trừ credit.
- **Các bước:**
  1. Cuộn xuống phần "Lịch sử đóng phí & bù trừ".
  2. Kiểm tra tab "Phiếu thu tiền mặt" và tab "Bù trừ tiền nộp thừa".
- **Kỳ vọng:**
  - Tab Phiếu thu: hiển thị mã phiếu, ngày nộp, số tiền đã áp dụng vào hóa đơn, số tiền dư nộp thừa sinh ra (nếu có), trạng thái `POSTED`. Nếu có lý do nộp muộn/hủy thì chỉ hiển thị nhãn "Có lý do".
  - Tab Bù trừ credit: hiển thị số tiền được cấn trừ từ quỹ nộp thừa của hộ vào hóa đơn này.
- **Kiểm sau:** Không để lộ tên nhân viên thu tiền hoặc văn bản lý do tự do.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ResidentBillingHistory.tsx`; `frontend/src/test/s10-detail.test.tsx`.

### RIN-09.3 — Nút Tải lại làm mới chi tiết và lịch sử tại chỗ

- **Vai trò:** RESIDENT.
- **Điểm vào:** Giao diện chi tiết hóa đơn `/resident/invoices/42`.
- **Dữ liệu vào:** Màn hình chi tiết đang hiển thị.
- **Các bước:**
  1. Nhấn nút "Tải lại" ở đầu trang chi tiết.
- **Kỳ vọng:** Cả hai API chi tiết hóa đơn (`/api/v1/resident/invoices/42`) và lịch sử (`/api/v1/resident/receipts`, `/api/v1/resident/credit-allocations`) được kích hoạt tải lại đồng thời; cập nhật dữ liệu mới nhất mà không phải reload toàn bộ trang web.
- **Kiểm sau:** Nút chuyển trạng thái loading trong khi đang tải và phục hồi sau khi hoàn tất.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ApiInvoiceDetailPage.tsx:145-155`; `frontend/src/test/s10-detail.test.tsx`.

### RIN-09.4 — Xử lý ID hóa đơn không hợp lệ trên thanh địa chỉ URL

- **Vai trò:** RESIDENT.
- **Điểm vào:** URL `/resident/invoices/abc` hoặc `/resident/invoices/-1`.
- **Dữ liệu vào:** Nhập ID là chuỗi chữ cái hoặc số âm trên URL.
- **Các bước:**
  1. Điều hướng trình duyệt tới `/resident/invoices/abc`.
- **Kỳ vọng:** Giao diện kiểm tra định dạng ID trước khi gọi API; hiển thị thông báo lỗi "Mã hóa đơn không hợp lệ" kèm nút quay lại danh sách; không gửi request rác lên server.
- **Kiểm sau:** Không gây ra lỗi crash Uncaught TypeError trong React console.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ApiInvoiceDetailPage.tsx:55-65`; `frontend/src/test/s10-detail.test.tsx`.

### RIN-09.5 — Xử lý thân thiện khi cư dân chưa có hộ hoặc bị mất quyền

- **Vai trò:** RESIDENT.
- **Điểm vào:** URL `/resident/invoices/42`.
- **Dữ liệu vào:** Cư dân chưa được gắn vào hộ gia đình nào, hoặc tài khoản vừa bị vô hiệu hóa khi đang mở trang.
- **Các bước:**
  1. Tài khoản cư dân không có `householdId` truy cập thẳng vào trang chi tiết hóa đơn.
- **Kỳ vọng:** Hiển thị màn hình thông báo riêng tư thân thiện: "Tài khoản của bạn chưa được liên kết với căn hộ nào. Vui lòng liên hệ Ban quản trị để được hỗ trợ."; không hiển thị mã lỗi kỹ thuật 403 thô; có nút quay về trang chủ.
- **Kiểm sau:** Ứng dụng an toàn, thân thiện với người dùng cuối.
- **Trạng thái:** Not run.
- **Nguồn:** `frontend/src/screens/resident/ApiInvoiceDetailPage.tsx:85-105`; `frontend/src/test/s10-detail.test.tsx`.
