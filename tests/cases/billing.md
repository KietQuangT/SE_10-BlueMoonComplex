# Ca kiểm thử kỳ thu bắt buộc và hóa đơn nhân sự (S08)

Các ID số trong dữ liệu (`1`, `10`, `20`, `501`, `601`, `999999`) là ID mẫu đã được tạo trước trên môi trường kiểm thử riêng; thay bằng ID thực tế tương ứng. `<THANG_NAY>`, `<THANG_TRUOC>`, `<THANG_SAU>`, `<HOM_NAY>`, `<HOM_QUA>`, `<NGAY_MAI>` và `<CUOI_THANG_NAY>` được xác định theo múi giờ `Asia/Ho_Chi_Minh`. Tài khoản đăng nhập có `status=ACTIVE`, `mustChangePassword=false`; request ghi gửi kèm `X-CSRF-TOKEN: <CSRF_TOKEN>`. Không dùng dữ liệu cư dân thật hoặc secret thật. Trạng thái mặc định của mọi ca là `Not run`.

### BIL-01.1 — ACCOUNTANT tạo kỳ thu nháp cho tháng hiện tại

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/billing-periods`.
- **Dữ liệu vào:** Tháng `<THANG_NAY>` chưa có kỳ thu còn hiệu lực; hạn nộp `<CUOI_THANG_NAY>` thuộc `<THANG_NAY>`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"period":"<THANG_NAY>","dueDate":"<CUOI_THANG_NAY>"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và phiên ACCOUNTANT nêu trên.
  2. Gửi `POST /api/v1/billing-periods` cùng body và CSRF token.
  3. Đối chiếu HTTP, body JSON và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 201; `period=<THANG_NAY>`, `dueDate=<CUOI_THANG_NAY>`, `status=DRAFT`, `version=0`, `latestRevision=0`, `expired=false`, `editable=true`, `allowedActions` có `PREVIEW`, `UPDATE`, `SUBMIT`, `householdCount=null`, `total=null`, `replacesPeriodId=null`, `receiptCount=0`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/<ID>` xác nhận bản ghi đã lưu; kiểm tra `audit_log` có `BILLING_PERIOD_CREATED` với `changes` chỉ chứa `period`, `status`, `dueDate`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:45,105,117,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/BillingPeriodController.java:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:57,309`.

### BIL-01.2 — Từ chối tạo kỳ thu trùng tháng còn hiệu lực

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/billing-periods`.
- **Dữ liệu vào:** Đã tồn tại kỳ `501` của `<THANG_NAY>` ở trạng thái `DRAFT`, `PENDING_APPROVAL` hoặc `ISSUED`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"period":"<THANG_NAY>","dueDate":"<CUOI_THANG_NAY>"}`.
- **Các bước:**
  1. Xác nhận kỳ `501` đang còn hiệu lực trong `<THANG_NAY>`.
  2. Gửi `POST /api/v1/billing-periods` cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, ProblemDetail có `code=BILLING_PERIOD_EXISTS`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods?period=<THANG_NAY>` xác nhận chỉ có duy nhất một kỳ chưa hủy cho tháng này.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26 (P1); `_agents_workspace/S08/01_contract.md:90,105,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:60,69`.

### BIL-01.3 — Từ chối tạo kỳ thu cho tháng trước hoặc tháng sau

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/billing-periods`.
- **Dữ liệu vào:** Header `X-CSRF-TOKEN: <CSRF_TOKEN>`; lần 1 gửi `{"period":"<THANG_TRUOC>","dueDate":"<THANG_TRUOC>-25"}`, lần 2 gửi `{"period":"<THANG_SAU>","dueDate":"<THANG_SAU>-25"}`.
- **Các bước:**
  1. Gửi `POST /api/v1/billing-periods` với tháng trước.
  2. Gửi `POST /api/v1/billing-periods` với tháng sau.
  3. Đối chiếu kết quả của cả hai lần và thực hiện kiểm sau.
- **Kỳ vọng:** Cả hai lần trả HTTP 400, `code=INVALID_PERIOD`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods` xác nhận không có kỳ thu mới cho `<THANG_TRUOC>` hoặc `<THANG_SAU>`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:36,61,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodRules.java:19`.

### BIL-01.4 — Từ chối tạo kỳ thu khi định dạng tháng sai hoặc hạn nộp ngoài tháng

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/billing-periods`.
- **Dữ liệu vào:** Header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body A `{"period":"2026-13","dueDate":"<CUOI_THANG_NAY>"}`, body B `{"period":"<THANG_NAY>","dueDate":"<THANG_SAU>-05"}`, body C `{"period":"<THANG_NAY>","dueDate":"2026-02-30"}`.
- **Các bước:**
  1. Gửi lần lượt body A, body B và body C tới `POST /api/v1/billing-periods`.
  2. Ghi nhận HTTP và mã lỗi `code` từng lần.
  3. Thực hiện kiểm sau.
- **Kỳ vọng:** Body A trả HTTP 400, `code=INVALID_PERIOD`; body B và body C trả HTTP 400, `code=INVALID_DATE`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods` xác nhận không phát sinh bản ghi nháp từ ba request lỗi.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:38,140,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/BillingPeriodController.java:107`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodRules.java:26,49`.

### BIL-01.5 — Race condition: Hai request tạo kỳ thu cùng tháng đồng thời chỉ một kỳ còn hiệu lực và một audit log

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/billing-periods`.
- **Dữ liệu vào:** Tháng `<THANG_NAY>` chưa có kỳ thu nào còn hiệu lực; hai request đồng thời từ ACCOUNTANT gửi cùng body `{"period":"<THANG_NAY>","dueDate":"<CUOI_THANG_NAY>"}` kèm header `X-CSRF-TOKEN: <CSRF_TOKEN>`. Cần test harness có transaction barrier (`CountDownLatch` / thread pool 2 worker) để kích hoạt đồng thời hai giao dịch.
- **Các bước:**
  1. Xác nhận DB chưa có kỳ thu nào cho tháng `<THANG_NAY>`.
  2. Kích hoạt 2 luồng đồng thời gửi `POST /api/v1/billing-periods` với cùng payload qua điểm chặn barrier.
  3. Đợi cả hai luồng kết thúc, thu thập mã trạng thái HTTP và body `ProblemDetail`.
  4. Kiểm tra số lượng bản ghi kỳ thu trong bảng `billing_periods` và số lượng audit log trong bảng `audit_log`.
- **Kỳ vọng:** Đúng một request thành công trả HTTP 201 (`status=DRAFT`, `version=0`, `latestRevision=0`, `receiptCount=0`); request còn lại bị xung đột bởi ràng buộc duy nhất trên cột sinh `active_month` và trả HTTP 409, `code=BILLING_PERIOD_EXISTS`. Không tạo ra hai kỳ thu song song cho cùng một tháng.
- **Kiểm sau:** Truy vấn DB `SELECT COUNT(*) FROM billing_periods WHERE period='<THANG_NAY>' AND status <> 'CANCELLED'` trả về đúng `1`; kiểm tra `audit_log` chỉ có đúng 1 bản ghi `BILLING_PERIOD_CREATED` cho `<THANG_NAY>`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26 (P1); `_agents_workspace/S08/01_contract.md:13,90,105,142,150,164,207`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:57`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/BillingPeriodConcurrencyIT.java:68`.

### BIL-02.1 — Xem danh sách và chi tiết kỳ thu theo vai trò ADMIN và ACCOUNTANT

- **Vai trò:** ADMIN, ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/billing-periods?period=<THANG_NAY>&status=DRAFT&page=0&size=20`, `GET /api/v1/billing-periods/501`.
- **Dữ liệu vào:** Kỳ `501` thuộc `<THANG_NAY>` đang ở `DRAFT`, `version=0`.
- **Các bước:**
  1. Dùng phiên ACCOUNTANT gọi danh sách và chi tiết kỳ `501`.
  2. Dùng phiên ADMIN gọi cùng hai endpoint.
  3. So sánh `editable` và `allowedActions` giữa hai vai trò.
- **Kỳ vọng:** HTTP 200 ở cả hai vai trò; danh sách sắp theo `period` giảm dần rồi `id` giảm dần. Với ACCOUNTANT: `editable=true`, `allowedActions=["PREVIEW","UPDATE","SUBMIT"]`. Với ADMIN: `editable=false`, `allowedActions=["PREVIEW"]`.
- **Kiểm sau:** Kiểm tra GET không tạo thêm dòng mới trong `audit_log`.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:20,104,106,117`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:201,208,309`.

### BIL-02.2 — Từ chối lọc danh sách kỳ thu sai tham số và đọc kỳ không tồn tại

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/billing-periods`, `GET /api/v1/billing-periods/999999`.
- **Dữ liệu vào:** Không tồn tại kỳ `999999`; các bộ tham số lỗi: `?period=2026-00`, `?status=UNKNOWN`, `?page=-1&size=20`, `?page=0&size=101`, `?page=abc`.
- **Các bước:**
  1. Gọi `GET /api/v1/billing-periods?period=2026-00`.
  2. Gọi lần lượt với `?status=UNKNOWN`, `?page=-1&size=20`, `?page=0&size=101` và `?page=abc`.
  3. Gọi `GET /api/v1/billing-periods/999999`.
- **Kỳ vọng:** Bước 1 trả HTTP 400, `code=INVALID_PERIOD`; bước 2 trả HTTP 400, `code=INVALID_FILTER`; bước 3 trả HTTP 404, `code=BILLING_PERIOD_NOT_FOUND`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods?page=0&size=20` xác nhận danh sách hợp lệ vẫn trả HTTP 200.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:140,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/BillingApiAdvice.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:208,328`.

### BIL-03.1 — ACCOUNTANT cập nhật hạn nộp khi kỳ thu đang DRAFT

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/billing-periods/501`.
- **Dữ liệu vào:** Kỳ `501` thuộc `<THANG_NAY>`, `status=DRAFT`, `version=0`; ngày `<NGAY_HAN_MOI>` thuộc `<THANG_NAY>`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"dueDate":"<NGAY_HAN_MOI>","version":0}`.
- **Các bước:**
  1. Xác nhận kỳ `501` đang ở `DRAFT` với `version=0`.
  2. Gửi `PATCH /api/v1/billing-periods/501` cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `dueDate=<NGAY_HAN_MOI>`, `status=DRAFT`, `version=1`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/501` đối chiếu `dueDate` và `version=1`; kiểm tra `audit_log` có `BILLING_PERIOD_UPDATED`.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:46,107,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/BillingPeriodController.java:50`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:78`.

### BIL-03.2 — Từ chối cập nhật hạn nộp khi sai version, sai trạng thái hoặc thiếu version

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `PATCH /api/v1/billing-periods/501`.
- **Dữ liệu vào:** Kỳ `501` thuộc `<THANG_NAY>`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Khi kỳ `501` ở `DRAFT` (`version=1`), gửi `{"dueDate":"<CUOI_THANG_NAY>","version":null}` hoặc `{"dueDate":"<CUOI_THANG_NAY>","version":-1}`.
  2. Gửi `{"dueDate":"<CUOI_THANG_NAY>","version":0}` (version cũ).
  3. Sau khi trình kỳ `501` sang `PENDING_APPROVAL` (`version=2`), gửi `{"dueDate":"<CUOI_THANG_NAY>","version":2}`.
- **Kỳ vọng:** Bước 1 trả HTTP 400, `code=VALIDATION`; bước 2 trả HTTP 409, `code=STALE_VERSION`; bước 3 trả HTTP 409, `code=INVALID_STATE`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/501` xác nhận `dueDate` và `status` không bị thay đổi bởi các request lỗi.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:46,107,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/BillingPeriodController.java:118`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:78,338`.

### BIL-04.1 — Xem trước kỳ nháp tính đúng công thức, làm tròn HALF_UP và không giảm phí tạm vắng

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `GET /api/v1/billing-periods/501/preview?page=0&size=20`.
- **Dữ liệu vào:** Kỳ `501` ở `DRAFT` thuộc `<THANG_NAY>`. Có hộ `1` (`ACTIVE`, diện tích `50.00` m², mọi nhân khẩu đang `TEMPORARILY_ABSENT`), hộ `2` (`ACTIVE`, diện tích `10.00` m²) và hộ `3` (`INACTIVE`, diện tích `80.00` m²). Đơn giá hiệu lực cho `<THANG_NAY>`: `MANAGEMENT_FEE = 10000.00` (hoặc `7000.05` khi thử lẻ), `WASTE_FEE = 30000.00` (hoặc `1000.50` khi thử lẻ).
- **Các bước:**
  1. Gọi `GET /api/v1/billing-periods/501/preview?page=0&size=20` với đơn giá chẵn (`10000.00` và `30000.00`).
  2. Kiểm tra trên môi trường có đơn giá lẻ (`MANAGEMENT_FEE=7000.05`, `WASTE_FEE=1000.50`) cho hộ `10.00` m².
  3. Đối chiếu danh sách hộ, từng dòng phí và tổng tiền.
- **Kỳ vọng:** HTTP 200; `revision=null`, `submittedAt=null`, `submittedBy=null`. Chỉ gồm các hộ `ACTIVE` (hộ `1`, hộ `2`), không có hộ `3` (`INACTIVE`). Hộ `1` dù toàn bộ nhân khẩu `TEMPORARILY_ABSENT` vẫn tính đủ `50.00 × 10000 = 500000` + `30000 = 530000`. Với đơn giá lẻ trên `10.00` m²: dòng `MANAGEMENT_FEE` có `unit="M2"`, `quantity="10.00"`, `areaM2="10.00"`, `amount="70001"`; dòng `WASTE_FEE` có `unit="HOUSEHOLD"`, `quantity="1"`, `areaM2=null`, `amount="1001"`; tổng hộ `"71002"`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/501` xác nhận `latestRevision=0`, `householdCount=null`, preview không ghi dữ liệu xuống DB.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D25, D26; `_agents_workspace/S08/01_contract.md:37,108,119,205`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:178,227`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/BillingLineResponse.java:6`.

### BIL-04.2 — Lưu nháp được nhưng chặn xem trước và trình duyệt khi không có hộ ACTIVE hoặc thiếu đơn giá

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/billing-periods/501/preview`, `POST /api/v1/billing-periods/501/submit`.
- **Dữ liệu vào:** Kỳ `501` ở `DRAFT` (`version=0`) đã được tạo thành công; trường hợp A: toàn bộ hộ trong DB đang `INACTIVE`; trường hợp B: có hộ `ACTIVE` nhưng thiếu đơn giá `WASTE_FEE` (hoặc `MANAGEMENT_FEE`) có `effective_from <= <THANG_NAY>-01`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Ở trường hợp A, gọi `GET /api/v1/billing-periods/501/preview` và `POST /api/v1/billing-periods/501/submit` với `{"version":0}`.
  2. Ở trường hợp B, gọi lại hai endpoint trên.
  3. Đối chiếu HTTP và `code` lỗi.
- **Kỳ vọng:** Trường hợp A trả HTTP 409, `code=NO_ACTIVE_HOUSEHOLDS` cho cả preview và submit. Trường hợp B trả HTTP 409, `code=FEE_RATE_UNAVAILABLE` cho cả preview và submit.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/501` xác nhận kỳ vẫn ở `DRAFT`, `latestRevision=0`, không sinh bản trình rỗng hay tính giá thiếu bằng `0`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26 (P2); `_agents_workspace/S08/01_contract.md:91,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:193,233`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/BillingSnapshotStore.java:53`.

### BIL-05.1 — ACCOUNTANT trình kỳ thu chụp snapshot revision 1 và đọc lại qua preview/submissions

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `POST /api/v1/billing-periods/501/submit`, `GET /api/v1/billing-periods/501/preview`, `GET /api/v1/billing-periods/501/submissions?revision=1&page=0&size=20`.
- **Dữ liệu vào:** Kỳ `501` ở `DRAFT` (`version=0`), có 2 hộ `ACTIVE` và đủ 2 đơn giá; header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"version":0}`.
- **Các bước:**
  1. Gửi `POST /api/v1/billing-periods/501/submit` với `{"version":0}`.
  2. Dùng ADMIN gọi `GET /api/v1/billing-periods/501`, `GET /api/v1/billing-periods/501/preview` và `GET /api/v1/billing-periods/501/submissions?revision=1`.
  3. Đối chiếu dữ liệu bản trình.
- **Kỳ vọng:** Bước 1 trả HTTP 200; `status=PENDING_APPROVAL`, `version=1`, `latestRevision=1`, `editable=false`, `householdCount=2`, `total` khớp tổng các hộ, `submittedAt` khác null. Bước 2 với ADMIN có `allowedActions=["PREVIEW","RETURN","ISSUE"]` (với ACCOUNTANT là `["PREVIEW","WITHDRAW"]`); cả `/preview` và `/submissions?revision=1` trả cùng snapshot có `revision=1`, `submittedBy`, `submittedAt`, phân trang hộ ổn định theo `householdId` tăng dần.
- **Kiểm sau:** Kiểm tra `audit_log` có `BILLING_PERIOD_SUBMITTED` chứa `period`, `revision="1"`, `count="2"`, `total`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:47,53,108,109,114,121,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:89,227,245,317`.

### BIL-05.2 — Rút trình, trả lại có lý do và trình lại tạo revision 2 không ghi đè revision 1

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `POST /api/v1/billing-periods/501/withdraw`, `POST /api/v1/billing-periods/501/return`, `POST /api/v1/billing-periods/501/submit`, `GET /api/v1/billing-periods/501/submissions`.
- **Dữ liệu vào:** Kỳ `501` đang ở `PENDING_APPROVAL` (`version=1`, `latestRevision=1`); ACCOUNTANT thứ hai (hoặc cùng ACCOUNTANT) thao tác tiếp theo P4; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Phiên ACCOUNTANT gửi `POST /api/v1/billing-periods/501/withdraw` với `{"version":1}`.
  2. Phiên ACCOUNTANT gửi `POST /api/v1/billing-periods/501/submit` với `{"version":2}` để lên `latestRevision=2`.
  3. Phiên ADMIN gửi `POST /api/v1/billing-periods/501/return` với `{"version":3,"reason":"  Can kiem tra lai dien tich ho 2  "}`.
  4. ADMIN cập nhật diện tích hộ `2` qua `PATCH /api/v1/households/<ID_HO_2>`, sau đó ACCOUNTANT trình lại `POST /api/v1/billing-periods/501/submit` với `{"version":4}`.
  5. Gọi `GET /api/v1/billing-periods/501/submissions?revision=1` và `?revision=3`.
- **Kỳ vọng:** Sau bước 1: `status=DRAFT`, `latestRevision=1`, `returnReason=null`. Sau bước 2: `status=PENDING_APPROVAL`, `latestRevision=2`. Sau bước 3: `status=DRAFT`, `returnReason="Can kiem tra lai dien tich ho 2"`. Sau bước 4: `status=PENDING_APPROVAL`, `latestRevision=3`, `returnReason=null`. Bước 5: `revision=1` giữ nguyên diện tích và tổng cũ, `revision=3` phản ánh diện tích và tổng mới; gọi `?revision=0` trả HTTP 400 `code=INVALID_FILTER`, gọi `?revision=99` trả HTTP 404 `code=BILLING_SUBMISSION_NOT_FOUND`.
- **Kiểm sau:** Kiểm tra `audit_log` có đủ `BILLING_PERIOD_WITHDRAWN`, `BILLING_PERIOD_RETURNED`, `BILLING_PERIOD_SUBMITTED` và `changes` của `BILLING_PERIOD_RETURNED` không chứa nguyên văn `reason`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26 (P4, P5); `_agents_workspace/S08/01_contract.md:48,49,53,93,110,111,114,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:89,125,135,245`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/BillingReadStore.java:43`.

### BIL-05.3 — Từ chối trả lại kỳ thu khi thiếu lý do, lý do rỗng hoặc vượt 255 ký tự

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/billing-periods/501/return`.
- **Dữ liệu vào:** Kỳ `501` đang ở `PENDING_APPROVAL` (`version=1`); header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body A `{"version":1,"reason":"   "}`, body B `{"version":1,"reason":"<CHUOI_256_KY_TU>"}`.
- **Các bước:**
  1. Gửi body A tới `POST /api/v1/billing-periods/501/return`.
  2. Gửi body B tới `POST /api/v1/billing-periods/501/return`.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** Cả hai lần trả HTTP 400, `code=REASON_REQUIRED`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/501` xác nhận kỳ vẫn ở `PENDING_APPROVAL` và `version=1`.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:49,140,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:135,159`.

### BIL-06.1 — Chặn phát hành khi sai revision, hạn nộp đã qua hoặc dữ liệu nguồn tính phí thay đổi

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/billing-periods/501/issue`.
- **Dữ liệu vào:** Kỳ `501` đang ở `PENDING_APPROVAL` (`version=3`, `latestRevision=2`); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi `POST /api/v1/billing-periods/501/issue` với `{"version":3,"revision":1}` (revision cũ).
  2. Trên kỳ chờ duyệt có `dueDate=<HOM_QUA>` (trước ngày hiện tại tại Việt Nam), gửi `issue` với đúng `version` và `revision`.
  3. Trên kỳ chờ duyệt có `dueDate=<CUOI_THANG_NAY>`, ADMIN đổi diện tích một hộ `ACTIVE` (hoặc tạo thêm hộ `ACTIVE` mới, hoặc hai hộ đổi diện tích bù trừ nhau giữ nguyên tổng tiền), rồi gửi `issue` với `{"version":3,"revision":2}`.
- **Kỳ vọng:** Bước 1 trả HTTP 409, `code=STALE_VERSION`. Bước 2 trả HTTP 400, `code=INVALID_DATE`. Bước 3 trả HTTP 409, `code=BILLING_INPUT_CHANGED` (kể cả khi tổng tiền toàn kỳ không đổi).
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/501` và `GET /api/v1/billing-periods/501/invoices` xác nhận kỳ vẫn ở `PENDING_APPROVAL` (không tự rơi về `DRAFT`), không có hóa đơn nào được sinh.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:38,50,53,112,142,206,207`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:106`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingInputs.java:21`.

### BIL-06.2 — ADMIN phát hành kỳ thu sinh hóa đơn bất biến và không bị ảnh hưởng khi nguồn đổi sau phát hành

- **Vai trò:** ADMIN, ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/billing-periods/501/issue`, `GET /api/v1/billing-periods/501/invoices?page=0&size=20`, `GET /api/v1/invoices/601`.
- **Dữ liệu vào:** Kỳ `501` đang ở `PENDING_APPROVAL` (`version=1`, `latestRevision=1`, `dueDate=<CUOI_THANG_NAY>`); dữ liệu nguồn khớp bản trình; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Phiên ADMIN gửi `POST /api/v1/billing-periods/501/issue` với `{"version":1,"revision":1}`.
  2. Gọi `GET /api/v1/billing-periods/501/invoices?page=0&size=20` và `GET /api/v1/invoices/601` (ID hóa đơn của hộ 1); gọi thêm `GET /api/v1/billing-periods/501/preview`.
  3. Sau phát hành, ADMIN sửa `areaM2` của hộ 1 qua `PATCH /api/v1/households/1` và tạo thêm một hộ `ACTIVE` mới; gọi lại `GET /api/v1/billing-periods/501/invoices` và `GET /api/v1/invoices/601`.
- **Kỳ vọng:** Bước 1 trả HTTP 200; `status=ISSUED`, `issuedAt` khác null, `allowedActions=["CANCEL"]` cho ADMIN và `[]` cho ACCOUNTANT. Bước 2: mỗi hộ `ACTIVE` tại thời điểm trình có đúng 1 hóa đơn `status=ISSUED` gồm 2 dòng `MANAGEMENT_FEE` và `WASTE_FEE`, tổng tiền khớp bản trình; gọi `/preview` khi đã `ISSUED` trả HTTP 409 `code=INVALID_STATE`; gọi `/invoices/999999` trả HTTP 404 `code=INVOICE_NOT_FOUND`. Bước 3: số lượng hóa đơn, `areaM2`, `unitPrice`, `amount` và `total` trên hóa đơn đã phát hành giữ nguyên theo snapshot, hộ mới không được chèn thêm vào kỳ `501`.
- **Kiểm sau:** Kiểm tra `audit_log` có `BILLING_PERIOD_ISSUED` cùng transaction; bảng `invoices` và `invoice_lines` khớp snapshot.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:39,50,55,112,115,119,164,205`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:106,231,254,264`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/BillingReadStore.java:83`.

### BIL-06.3 — Race condition: Hai request phát hành cùng version/revision đồng thời chỉ một thành công và một bộ hóa đơn

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/billing-periods/{id}/issue`.
- **Dữ liệu vào:** Kỳ `501` đang ở `PENDING_APPROVAL` (`version=1`, `revision=1`, `dueDate=<CUOI_THANG_NAY>`); hệ thống có $N$ hộ `ACTIVE`; hai request đồng thời từ ADMIN gửi cùng body `{"version":1,"revision":1}` kèm header `X-CSRF-TOKEN: <CSRF_TOKEN>`. Cần test harness có transaction barrier (`CountDownLatch` / thread pool 2 worker) để kích hoạt hai request đồng thời.
- **Các bước:**
  1. Xác nhận kỳ `501` ở `PENDING_APPROVAL` với `version=1`, `revision=1`, dữ liệu hộ và đơn giá khớp snapshot bản trình.
  2. Kích hoạt 2 luồng đồng thời gửi `POST /api/v1/billing-periods/501/issue` với `{"version":1,"revision":1}` qua điểm chặn barrier.
  3. Thu nhận HTTP status và ProblemDetail từ cả hai luồng.
  4. Kiểm tra hóa đơn trong bảng `invoices`, các dòng phí trong `invoice_lines`, và bảng `audit_log`.
- **Kỳ vọng:** Đúng một request thành công trả HTTP 200 (`status=ISSUED`, `issuedAt` khác null); request còn lại thất bại do cập nhật phiên bản lạc quan/khóa dòng kỳ thu và trả HTTP 409, `code=STALE_VERSION`. Chỉ sinh ra đúng một bộ hóa đơn duy nhất (mỗi hộ `ACTIVE` có đúng 1 hóa đơn, mỗi hóa đơn có đúng 2 dòng phí `MANAGEMENT_FEE` và `WASTE_FEE`).
- **Kiểm sau:** Truy vấn DB `SELECT COUNT(*) FROM invoices WHERE period_id=501` đúng bằng $N$; `SELECT COUNT(*) FROM invoice_lines WHERE invoice_id IN (SELECT id FROM invoices WHERE period_id=501)` đúng bằng $2 \times N$; kiểm tra `audit_log` chỉ có đúng 1 dòng `BILLING_PERIOD_ISSUED` cho kỳ `501`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:13,50,55,112,142,153,164,207`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:106`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/BillingPeriodConcurrencyIT.java:82`.

### BIL-06.4 — Kiểm soát xung đột: Thay đổi tập hộ (tạo ACTIVE, kích hoạt INACTIVE, đổi diện tích) trong lúc phát hành kỳ thu

- **Vai trò:** ADMIN (phát hành) và ADMIN / tiến trình hệ thống (thay đổi dữ liệu hộ cư dân).
- **Điểm vào:** `POST /api/v1/billing-periods/{id}/issue` kết hợp thao tác thay đổi dữ liệu hộ qua `POST /api/v1/households` hoặc `PATCH /api/v1/households/{id}`.
- **Dữ liệu vào:** Kỳ `501` ở `PENDING_APPROVAL` (`version=1`, `revision=1`) được lập với snapshot ban đầu gồm hộ `1` (`ACTIVE`, diện tích `50.00` m²), có hộ `2` đang `INACTIVE` (diện tích `60.00` m²). Dùng transaction barrier (test harness DB hai transaction với `CountDownLatch` trên isolation `REPEATABLE_READ`) để mô phỏng tương tranh giữa transaction phát hành (`BillingPeriodService.issue`) và transaction thay đổi hộ:
  - Biến thể A: Thêm hộ mới có `status='ACTIVE'`.
  - Biến thể B: Kích hoạt hộ `2` từ `INACTIVE` sang `ACTIVE`.
  - Biến thể C: Cập nhật diện tích hộ `1` từ `50.00` m² sang `55.00` m² (hoặc chuyển `status='INACTIVE'`).
- **Các bước:**
  1. Thiết lập kỳ `501` chờ duyệt với snapshot cố định.
  2. Dùng test harness thực hiện kiểm tra qua hai thứ tự cạnh tranh:
     - **Thứ tự 1 (Thao tác sửa đổi hộ commit trước khi phát hành đọc khóa):** Thực hiện thay đổi hộ (Biến thể A, B hoặc C) và commit thành công. Sau đó gửi `POST /api/v1/billing-periods/501/issue` với `{"version":1,"revision":1}`.
     - **Thứ tự 2 (Transaction phát hành đang giữ khóa khoảng hộ `lockActiveForBilling()` thì thao tác sửa đổi hộ diễn ra):** Transaction phát hành bắt đầu, gọi `lockActiveForBilling()` khóa khoảng `status='ACTIVE'`, giữ kết nối chưa commit qua latch. Luồng thứ hai cố gắng thực hiện Biến thể A, B hoặc C. Sau một khoảng chờ, giải phóng latch để transaction phát hành commit trước, rồi quan sát luồng thứ hai.
  3. Đối chiếu HTTP status, dữ liệu hóa đơn sinh ra và trạng thái bản ghi hộ.
- **Kỳ vọng:**
  - Ở Thứ tự 1: Request phát hành đọc dữ liệu nguồn hiện tại, so sánh với snapshot bản trình thấy khác biệt (khác số lượng hộ hoặc khác diện tích), từ chối phát hành và trả HTTP 409, `code=BILLING_INPUT_CHANGED`. Kỳ `501` giữ nguyên `PENDING_APPROVAL`, không có hóa đơn nào được sinh ra, không phát sinh audit phát hành.
  - Ở Thứ tự 2: Khóa dòng/khoảng (range lock / `SELECT ... FOR UPDATE` trên index `status, id`) buộc thao tác sửa đổi hộ (INSERT ACTIVE, UPDATE status sang ACTIVE, UPDATE diện tích) phải chờ (block) cho đến khi transaction phát hành hoàn tất commit. Toàn bộ hóa đơn phát hành sinh ra đúng và đủ theo đúng snapshot đã duyệt (hộ `1` diện tích `50.00` m², không có hộ mới hay hộ `2`); thao tác sửa đổi hộ chỉ commit sau đó và áp dụng cho kỳ tháng tiếp theo, không làm sai lệch bộ hóa đơn đã phát hành.
  - Cả hai thứ tự: Kết quả không bao giờ bỏ sót hộ `ACTIVE`, không lọt hộ `INACTIVE`, không thêm hộ thừa ngoài snapshot đã duyệt, và không phát hành theo snapshot cũ sau khi dữ liệu DB đã đổi.
- **Kiểm sau:**
  - Ở Thứ tự 1: `SELECT COUNT(*) FROM invoices WHERE period_id=501` bằng `0`; kỳ `501` vẫn ở `PENDING_APPROVAL`.
  - Ở Thứ tự 2: Hóa đơn kỳ `501` chỉ gồm hộ `1` với diện tích `50.00` m²; bảng `households` cập nhật dữ liệu mới sau khi phát hành xong.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:13,50,53,142,162,207`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:106`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingInputs.java:21`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/ActiveHouseholdLockIT.java:46`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/BillingMutationSourceAndRaceTest.java:62`.

### BIL-07.1 — ADMIN hủy kỳ đã phát hành trong tháng khi chưa có phiếu thu và ACCOUNTANT lập kỳ thay thế

- **Vai trò:** ADMIN, ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/billing-periods/501/cancel`, `POST /api/v1/billing-periods`.
- **Dữ liệu vào:** Kỳ `501` thuộc `<THANG_NAY>`, `status=ISSUED`, `version=2`, `receiptCount=0`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Phiên ADMIN gửi `POST /api/v1/billing-periods/501/cancel` với `{"version":2,"reason":"Sai don gia rac can lap lai"}`.
  2. Gọi `GET /api/v1/billing-periods/501/invoices` và `GET /api/v1/invoices/601`.
  3. Phiên ACCOUNTANT gửi `POST /api/v1/billing-periods` với `{"period":"<THANG_NAY>","dueDate":"<CUOI_THANG_NAY>"}`.
- **Kỳ vọng:** Bước 1 trả HTTP 200; kỳ `501` có `status=CANCELLED`, `cancelReason="Sai don gia rac can lap lai"`, `cancelledAt` khác null, `allowedActions=[]`. Bước 2: toàn bộ hóa đơn của kỳ `501` chuyển sang `status=CANCELLED`, `cancelledAt` khác null, vẫn đọc được lịch sử. Bước 3 trả HTTP 201 tạo kỳ mới `502` với `status=DRAFT` và `replacesPeriodId=501`.
- **Kiểm sau:** Kiểm tra `audit_log` có `BILLING_PERIOD_CANCELLED` (không chứa nguyên văn `reason`) và `BILLING_PERIOD_CREATED` cho kỳ `502`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:51,59,113,115,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:63,146`.

### BIL-07.2 — Chặn hủy kỳ đã phát hành khi đã có giao dịch thu (`receiptCount > 0`) hoặc thiếu lý do

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/billing-periods/502/cancel`.
- **Dữ liệu vào:** Kỳ `502` thuộc `<THANG_NAY>`, `status=ISSUED`, `version=2`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Khi `receiptCount=0`, gửi `POST /api/v1/billing-periods/502/cancel` với `{"version":2,"reason":" "}`.
  2. Mô phỏng giao kèo S09 (tăng `receipt_count = 1` qua `BillingReceiptGuard.recordReceipt(502)` hoặc cập nhật DB thử nghiệm), sau đó gửi `POST /api/v1/billing-periods/502/cancel` với `{"version":2,"reason":"Muon huy ky da thu"}`.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** Bước 1 trả HTTP 400, `code=REASON_REQUIRED`. Bước 2 trả HTTP 409, `code=PERIOD_HAS_RECEIPTS`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/502` và `GET /api/v1/billing-periods/502/invoices` xác nhận kỳ và các hóa đơn vẫn ở trạng thái `ISSUED`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:51,65,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:146`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingReceiptGuard.java:20`.

### BIL-07.3 — Race condition: Hủy kỳ phát hành đua với ghi nhận phiếu thu qua `BillingReceiptGuard.recordReceipt` trong hai DB transaction thật

- **Vai trò:** ADMIN (hủy kỳ) và Module thanh toán S09 (ghi phiếu thu qua `BillingReceiptGuard`).
- **Điểm vào:** `POST /api/v1/billing-periods/{id}/cancel` và method giao kèo `BillingReceiptGuard.recordReceipt(Long billingPeriodId)`.
- **Dữ liệu vào:** Kỳ `501` thuộc `<THANG_NAY>`, `status=ISSUED`, `version=1`, `receipt_count=0`. Hai DB transaction thật được điều khiển bằng test harness xác định (như `BillingReceiptRaceIT` dùng `PlatformTransactionManager` và `CountDownLatch` trên hai thread riêng biệt). Không dùng cập nhật trực tiếp `receipt_count` bằng câu lệnh UPDATE SQL để giả lập race.
- **Các bước:**
  1. Khởi tạo kỳ `501` ở trạng thái `ISSUED` với `receipt_count=0`.
  2. **Thứ tự 1 (Phiếu thu thắng đua):**
     - Transaction A (ghi thu) gọi `BillingReceiptGuard.recordReceipt(501)`, khóa dòng kỳ thu bằng `lockById`, tăng `receipt_count=1`, giữ latch chưa commit.
     - Transaction B (hủy kỳ) gọi `BillingPeriodService.cancel(501, 1, "Huy ky", adminId)` (hoặc gửi HTTP `POST /cancel`); transaction B bị block do tranh chấp khóa dòng `billing_periods`.
     - Giải phóng latch để Transaction A commit thành công.
     - Quan sát kết quả của Transaction B sau khi hết block.
  3. Khởi tạo kỳ mới `502` ở trạng thái `ISSUED` với `version=0`, `receipt_count=0`.
  4. **Thứ tự 2 (Hủy kỳ thắng đua):**
     - Transaction C (hủy kỳ) gọi `BillingPeriodService.cancel(502, 0, "Huy ky", adminId)`, khóa dòng kỳ thu, chuyển `status='CANCELLED'`, giữ latch chưa commit.
     - Transaction D (ghi thu) gọi `BillingReceiptGuard.recordReceipt(502)`; transaction D bị block do tranh chấp khóa dòng.
     - Giải phóng latch để Transaction C commit thành công.
     - Quan sát kết quả của Transaction D sau khi hết block.
- **Kỳ vọng:**
  - Ở Thứ tự 1 (Receipt thắng): Transaction hủy kỳ B nhận lỗi từ chối HTTP 409, `code=PERIOD_HAS_RECEIPTS`. Kỳ thu `501` và toàn bộ hóa đơn giữ nguyên `status=ISSUED`; `receipt_count` được ghi nhận bằng `1`.
  - Ở Thứ tự 2 (Cancel thắng): Transaction ghi thu D bị từ chối với lỗi nghiệp vụ `code=INVALID_STATE`. Transaction D rollback hoàn toàn, `receipt_count` của kỳ `502` không tăng (vẫn giữ bằng `0`); kỳ `502` và toàn bộ hóa đơn chuyển sang `CANCELLED`.
- **Kiểm sau:**
  - Thứ tự 1: `SELECT status, receipt_count FROM billing_periods WHERE id=501` trả về `ISSUED`, `1`; không có audit log `BILLING_PERIOD_CANCELLED`.
  - Thứ tự 2: `SELECT status, receipt_count FROM billing_periods WHERE id=502` trả về `CANCELLED`, `0`; có audit log `BILLING_PERIOD_CANCELLED` cho kỳ `502`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:13,51,65,142,207`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingReceiptGuard.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:146`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/BillingReceiptRaceIT.java:64,100`.

### BIL-08.1 — Kỳ thu của tháng cũ bị khóa thao tác ghi (`BILLING_PERIOD_EXPIRED`) và hiển thị đúng cờ `expired`

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `GET /api/v1/billing-periods/{id}`, `PATCH /api/v1/billing-periods/{id}`, `POST /api/v1/billing-periods/{id}/submit|withdraw|return|issue|cancel`.
- **Dữ liệu vào:** Ba kỳ thuộc `<THANG_TRUOC>`: kỳ `511` ở `DRAFT` (`version=0`), kỳ `512` ở `PENDING_APPROVAL` (`version=1`, `revision=1`), kỳ `513` ở `ISSUED` (`version=2`, `receiptCount=0`); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gọi `GET /api/v1/billing-periods/511`, `/512` và `/513`.
  2. Dùng ACCOUNTANT thử `PATCH /511`, `POST /511/submit` và `POST /512/withdraw`.
  3. Dùng ADMIN thử `POST /512/return`, `POST /512/issue` và `POST /513/cancel`.
- **Kỳ vọng:** Bước 1: kỳ `511` và `512` có `expired=true`, `editable=false`, `allowedActions=["PREVIEW"]`; kỳ `513` (`ISSUED`) có `expired=false`, `editable=false`, `allowedActions=[]`. Bước 2 và bước 3: mọi thao tác ghi trên kỳ tháng cũ đều trả HTTP 409, `code=BILLING_PERIOD_EXPIRED`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/512/submissions?revision=1` và `GET /api/v1/billing-periods/513/invoices` xác nhận vẫn tra cứu được dữ liệu lịch sử HTTP 200.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:61,142,206`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodService.java:309,338`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingPeriodRules.java:33`.

### BIL-09.1 — Phân quyền API kỳ thu/hóa đơn: chặn ANON, RESIDENT, chéo vai trò, thiếu CSRF và `PASSWORD_CHANGE_REQUIRED`

- **Vai trò:** ANON, RESIDENT, ACCOUNTANT, ADMIN.
- **Điểm vào:** Các endpoint `/api/v1/billing-periods/**` và `/api/v1/invoices/*`.
- **Dữ liệu vào:** Kỳ `501` và hóa đơn `601` tồn tại; có thêm tài khoản ACCOUNTANT mới cấp có `mustChangePassword=true`.
- **Các bước:**
  1. Từ phiên ANON (có CSRF hợp lệ), gọi `GET /api/v1/billing-periods` và `POST /api/v1/billing-periods`.
  2. Từ phiên RESIDENT, gọi `GET /api/v1/billing-periods`, `GET /api/v1/billing-periods/501/invoices`, `GET /api/v1/invoices/601` và `POST /api/v1/billing-periods`.
  3. Từ phiên ADMIN, gửi `POST /api/v1/billing-periods`, `PATCH /api/v1/billing-periods/501`, `POST /api/v1/billing-periods/501/submit`, `POST /api/v1/billing-periods/501/withdraw`.
  4. Từ phiên ACCOUNTANT, gửi `POST /api/v1/billing-periods/501/return`, `POST /api/v1/billing-periods/501/issue`, `POST /api/v1/billing-periods/501/cancel`.
  5. Từ phiên ACCOUNTANT hợp lệ, gửi `POST /api/v1/billing-periods` không có header `X-CSRF-TOKEN`; từ phiên ACCOUNTANT có `mustChangePassword=true`, gọi `GET /api/v1/billing-periods`.
- **Kỳ vọng:** Bước 1 trả HTTP 401. Bước 2, 3, 4 trả HTTP 403. Bước 5: thiếu CSRF trả HTTP 403; tài khoản `mustChangePassword=true` trả HTTP 403 với `code=PASSWORD_CHANGE_REQUIRED`.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/501` bằng ADMIN xác nhận dữ liệu kỳ thu không bị thay đổi.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:20,30,102,209`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44,67`.

### BIL-10.1 — Giao diện `/accountant/billing-periods` và `/admin/billing-periods`: lập, trình, duyệt phát hành và đối chiếu bản trình

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `/accountant/billing-periods`, `/admin/billing-periods`.
- **Dữ liệu vào:** Chế độ `api`; tháng `<THANG_NAY>` chưa có kỳ thu; có hộ `ACTIVE` và đủ hai đơn giá; có một hộ có tổng phí lớn để kiểm định dạng số nguyên chuỗi thập phân.
- **Các bước:**
  1. Đăng nhập ACCOUNTANT, mở `/accountant/billing-periods`, nhập hạn nộp `<CUOI_THANG_NAY>` và bấm `Tạo kỳ thu`.
  2. Bấm vào kỳ `<THANG_NAY>` trong danh sách, kiểm tra bảng xem trước hộ, sau đó bấm `Trình duyệt` và xác nhận trong hộp thoại.
  3. Đăng nhập ADMIN, mở `/admin/billing-periods`, chọn kỳ `<THANG_NAY>`, kiểm tra thông tin `<N> hộ · <TỔNG> đ · Bản trình số 1`, kiểm tra không có nút `Lập kỳ thu` hay `Trình duyệt`, rồi bấm `Phát hành`.
  4. Đối chiếu nội dung hộp thoại xác nhận (`<N> hộ, tổng <TỔNG> đ, bản trình số 1.`) và bấm `Phát hành`.
- **Kỳ vọng:** ACCOUNTANT không thấy nút `Phát hành`, `Trả lại`, `Hủy kỳ`; ADMIN không thấy biểu mẫu lập/sửa kỳ thu. Sau khi ADMIN xác nhận phát hành, trạng thái kỳ chuyển sang `Đã phát hành`, mục `Hóa đơn` hiển thị danh sách hóa đơn của từng hộ với trạng thái `Đã phát hành`; nút `Xem bản trình` cho phép chuyển đổi xem lại các revision đã trình.
- **Kiểm sau:** Gọi `GET /api/v1/billing-periods/<ID>/invoices` đối chiếu số lượng và tổng tiền hóa đơn khớp giao diện.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:170,171,176`; `frontend/src/App.tsx:129,138`; `frontend/src/screens/s08/ApiBillingPage.tsx:31`.

### BIL-10.2 — Giao diện kỳ thu xử lý lỗi 409 (`BILLING_INPUT_CHANGED`, `STALE_VERSION`), giữ nháp khi 401, chặn sai vai trò và không fallback demo

- **Vai trò:** ACCOUNTANT, ADMIN, RESIDENT.
- **Điểm vào:** `/admin/billing-periods`, `/accountant/billing-periods`.
- **Dữ liệu vào:** Chế độ `api`; kỳ `501` đang ở `PENDING_APPROVAL` (`latestRevision=1`).
- **Các bước:**
  1. ADMIN mở `/admin/billing-periods`, chọn kỳ `501`; ở tab khác thay đổi diện tích hộ `ACTIVE`, rồi quay lại bấm `Phát hành` và xác nhận.
  2. ADMIN mở lại kỳ `501`, ở phiên ACCOUNTANT rút trình kỳ `501` để đổi `version`, rồi ở phiên ADMIN bấm `Phát hành` và xác nhận.
  3. ACCOUNTANT mở `/accountant/billing-periods`, nhập hạn nộp `<CUOI_THANG_NAY>`;ADMIN vô hiệu hoặc xóa phiên của ACCOUNTANT để gây 401 khi bấm `Tạo kỳ thu`, sau đó đăng nhập lại cùng tài khoản ACCOUNTANT và mở `/accountant/billing-periods`.
  4. Dùng RESIDENT điều hướng tới `/admin/billing-periods` và `/accountant/billing-periods`; dùng ACCOUNTANT chuyển sang chế độ `demo` (nếu bật dev tools) tại `/accountant/billing-periods`.
- **Kỳ vọng:** Bước 1 hiển thị cảnh báo `Dữ liệu tính phí đã thay đổi. Kế toán cần rút và trình lại kỳ thu.` và đóng chi tiết kỳ, không tự phát hành. Bước 2 hiển thị `Dữ liệu đã thay đổi. Tải lại rồi kiểm tra trước khi thử lại.` kèm nút `Tải lại`, buộc chọn lại kỳ sau khi tải lại. Bước 3 giữ lại giá trị hạn nộp đã nhập trong bộ nhớ phiên làm việc của đúng `username` (không lưu vào `localStorage`/`sessionStorage`). Bước 4: RESIDENT nhận màn lỗi 403; ở chế độ `demo`, `/accountant/billing-periods` hiện thông báo `Kỳ thu chưa khả dụng trong chế độ Demo.`, còn ở chế độ `api` khi lỗi API tuyệt đối không hiển thị dữ liệu demo.
- **Kiểm sau:** Kiểm tra `localStorage` và `sessionStorage` không chứa dữ liệu nháp kỳ thu.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:168,170,176,209`; `frontend/src/App.tsx:91,138`; `frontend/src/api/errors.ts:5,6`; `frontend/src/screens/s08/ApiBillingPage.tsx:28`.

### BIL-10.3 — Giao diện kỳ thu bắt buộc: mobile viewport 360px, theme sáng/tối, điều hướng keyboard-only với focus nhìn thấy, date input badInput và định dạng số lớn

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `/accountant/billing-periods`, `/admin/billing-periods`.
- **Dữ liệu vào:** Chế độ `api`; trình duyệt desktop và trình duyệt mobile (mô phỏng viewport 360px × 640px); có kỳ thu với tổng tiền lớn (`999999999999999999` VND - 18 chữ số); ngày hạn nộp không hợp lệ để kích hoạt cờ `validity.badInput` của trình duyệt.
  *(Lưu ý: Luồng xử lý lỗi 401/409, giữ nháp và chặn fallback demo đã được kiểm thử tại [BIL-10.2](#bil-102--giao-diện-kỳ-thu-xử-lý-lỗi-409-billing_input_changed-stale_version-giữ-nháp-khi-401-chặn-sai-vai-trò-và-không-fallback-demo); ca này tập trung kiểm thử trải nghiệm người dùng, tính tiếp cận và tính toàn vẹn dữ liệu giao diện theo hợp đồng mục 11).*
- **Các bước:**
  1. **Kiểm tra Viewport 360px:** Mở DevTools, chỉnh kích thước màn hình `360px × 640px`. Mở màn `/accountant/billing-periods` và `/admin/billing-periods`:
     - Kiểm tra khối lập/sửa kỳ thu, danh sách kỳ thu, bảng xem trước danh sách hộ và danh sách hóa đơn: nội dung vừa vặn màn hình, không bị tràn ngang (không xuất hiện thanh cuộn ngang toàn trang).
     - Các nút hành động (`Tạo kỳ thu`, `Trình duyệt`, `Phát hành`, `Hủy kỳ`) hiển thị đầy đủ, không bị chồng chéo và vùng bấm tối thiểu 44px.
  2. **Kiểm tra Theme sáng/tối:**
     - Chuyển đổi giữa theme sáng (Light) và theme tối (Dark) qua nút `ThemeToggle` hoặc tùy chọn hệ thống (`prefers-color-scheme`).
     - Kiểm tra độ tương phản màu chữ so với màu nền (`surface-box`, `notice-box`), viền hộp thông báo và màu các badge trạng thái (`Nháp`, `Chờ duyệt`, `Đã phát hành`, `Đã hủy`).
     - Đảm bảo toàn bộ nhãn và số liệu đọc rõ ràng, không bị lóa hoặc mất độ tương phản theo chuẩn WCAG AA.
  3. **Kiểm tra Điều hướng bàn phím (Keyboard-only):**
     - Chỉ sử dụng bàn phím (`Tab`, `Shift+Tab`, `Enter`, `Space`, `Esc`), không dùng chuột.
     - Di chuyển qua form lập kỳ thu, danh sách kỳ thu, các nút hành động và hộp thoại xác nhận `ConfirmDialog`.
     - Kiểm tra vòng viền tiêu điểm (`:focus-visible` outline) hiển thị rõ ràng trên mọi điều khiển tương tác.
     - Khi mở `ConfirmDialog` (bấm `Phát hành`, `Trả lại` hoặc `Hủy kỳ`), tiêu điểm tự động chuyển vào hộp thoại; phím `Esc` hoặc bấm phím `Tab` tới nút `Hủy` cho phép đóng hộp thoại an toàn.
  4. **Kiểm tra Date input ở trạng thái `badInput`:**
     - Tại ô `Hạn nộp` (`input[type="date"]`), gõ ngày sai định dạng (ví dụ ngày không tồn tại hoặc chuỗi không hợp lệ trên trình duyệt có hỗ trợ) khiến thuộc tính `validity.badInput` bằng `true`.
     - Bấm nút `Tạo kỳ thu` hoặc `Lưu hạn nộp`.
     - Quan sát phản hồi: form gọi `checkValidity()`, phát hiện dữ liệu không hợp lệ, chặn việc gửi form và hiển thị cảnh báo `Ngày hạn nộp chưa hợp lệ. Kiểm tra rồi thử lại.`; trình duyệt không gửi request nào lên server.
  5. **Kiểm tra Số tiền lớn dạng chuỗi không làm tròn:**
     - Mở kỳ thu có tổng tiền lớn `999999999999999999`.
     - Quan sát giá trị tổng tiền trên bản xem trước, trên hộp thoại xác nhận và trên danh sách hóa đơn.
     - Xác nhận số tiền được hiển thị đầy đủ từng chữ số `999.999.999.999.999.999 đ` thông qua hàm `formatVndDecimal`, không bị chuyển thành ký pháp khoa học (như `1e+18`) hoặc bị làm tròn sai số do giới hạn số thực của JavaScript Number.
- **Kỳ vọng:** Layout 360px co giãn hợp lý không vỡ giao diện; theme sáng/tối có độ tương phản rõ ràng; thao tác keyboard-only thuận tiện với focus ring nhìn thấy; date `badInput` chặn gửi request sai; số tiền lớn chuỗi thập phân giữ nguyên độ chính xác từng chữ số.
- **Kiểm sau:** Kiểm tra tab Network xác nhận không phát sinh HTTP request khi date input gặp trạng thái `badInput`.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:15,98,168,176,209`; `frontend/src/screens/s08/ApiBillingPage.tsx:8,29,33,36`; `frontend/src/lib/format.ts:8`; `frontend/src/test/s08-screens.test.tsx:22`.

## Điểm chưa xác định

- Khi gọi `GET /api/v1/billing-periods/{id}/submissions?revision=99` với `revision` chưa tồn tại, `BillingReadStore.java:47` trả HTTP 404 với `code=BILLING_SUBMISSION_NOT_FOUND`; mã này chưa được liệt kê tường minh trong dòng tóm tắt 404 tại `_agents_workspace/S08/01_contract.md:142` (hợp đồng mới liệt kê `BILLING_PERIOD_NOT_FOUND/INVOICE_NOT_FOUND/CAMPAIGN_NOT_FOUND/RESIDENT_NOT_FOUND`).
- Trên giao diện `ApiBillingPage.tsx:33`, bộ lọc danh sách kỳ thu theo `period` và `status` (`GET /api/v1/billing-periods?period=&status=`) đã hỗ trợ ở backend nhưng chưa có ô lọc trên màn hình (giao diện hiện gọi `listBillingPeriods(page)` theo trang).

