# Báo cáo kiểm thử chi tiết: Kỳ thu bắt buộc và hóa đơn

- **Tệp đặc tả nguồn:** [`tests/cases/billing.md`](../cases/billing.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** 26 ca từ `BIL-01.1` đến `BIL-10.3` của phân hệ Billing / S08: tạo kỳ thu tháng hiện tại (chặn trùng tháng, chặn tháng khác, race), đọc danh sách và chi tiết, cập nhật hạn nộp kỳ nháp, xem trước công thức và làm tròn, trình/rút/trả/phát hành kỳ thu và hóa đơn bất biến, hủy kỳ đã phát hành, khóa kỳ tháng cũ, phân quyền RBAC/CSRF và giao diện S08. Các ID và placeholder trong đặc tả chỉ dùng trên database kiểm thử riêng; không ghi mật khẩu, CSRF token, cookie hoặc dữ liệu cư dân thật vào bằng chứng.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **26** |
| Số ca Đạt (Pass) | **23** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **3** |
| Tỷ lệ Đạt | **88.46%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- **Cách xác định kết quả "Đạt" của báo cáo này:** khác với các báo cáo phân hệ khác, 23 ca backend (`BIL-01` đến `BIL-09`) **chưa được thực thi thủ công** qua HTTP, session và CSRF thật. Mỗi ca được xếp "Đạt" khi có test tự động cụ thể (unit, lát WebMvc hoặc integration trên MySQL thật) xác nhận kết quả mong đợi chính của ca đó: mã trạng thái, `code` lỗi, trạng thái dữ liệu hoặc bất biến. Tên test nằm ở trường "Thực tế ghi nhận" của từng ca. "Đạt" ở đây không thay thế kiểm thử thủ công end-to-end; ca nào chỉ kiểm được một phần thì giới hạn được ghi ở "Chi tiết lỗi / Nhận xét".
- **`BIL-10.1`, `BIL-10.2`, `BIL-10.3` (Bị chặn):** `frontend/src/screens/admin/BillingPeriodsPage.tsx` chỉ là UI demo, dùng `useAppState` với dữ liệu trong bộ nhớ, không gọi `/api/v1/billing-periods`; `frontend/src/api` chưa có client billing, chưa có route `/accountant/billing-periods` và chưa có test Vitest hay Playwright cho kỳ thu. Cần làm UI S08 ở chế độ API trước khi kiểm thử.
- **Đối chiếu đặc tả với mã nguồn:** `BIL-06.2` (audit phát hành) và `BIL-08.1` (cờ `expired` của kỳ ISSUED tháng cũ) khớp giữa `tests/cases/billing.md` và `BillingPeriodService`; không phát hiện sai lệch.
- **Môi trường chạy:** Windows, JDK 21.0.12.1 (JDK mặc định của máy là 26 nên Maven Enforcer từ chối; phải đặt `JAVA_HOME`). MySQL 8.4 trong container `bluemoon-db-1` cổng `3306`; integration chạy trên schema riêng `bluemoon_it`, không dùng schema `bluemoon` của backend.
- **Lệnh và kết quả (09/11/2026):** unit/API **134 test** và integration **29 test**, 0 failure, 0 error, 0 skipped, `BUILD SUCCESS`. Kiểm tra chuẩn `./mvnw.cmd -B -ntp verify` toàn backend (không profile integration): **244 test**, `BUILD SUCCESS`.

  ```powershell
  $env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.12.1'
  $env:Path="$env:JAVA_HOME\bin;$env:Path"
  $env:DB_HOST='127.0.0.1'; $env:DB_PORT='<DB_PORT trong .env>'; $env:DB_NAME='bluemoon_it'
  $env:DB_USER='<MYSQL_USER trong .env>'; $env:DB_PASSWORD='<MYSQL_PASSWORD trong .env>'
  cd backend
  .\mvnw.cmd -B -ntp -Pintegration verify "-Dtest=vn/edu/se10/bluemoon/billing/*Test" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dit.test=vn/edu/se10/bluemoon/billing/*IT" "-Dfailsafe.failIfNoSpecifiedTests=false"
  ```

- **Test được thêm để lấp khoảng trống của test cũ:** `BillingCaseGapTest` (19 test), `BillingErrorMappingApiTest` (12 test), `BillingPeriodRevisionIT` (3 test, MySQL thật, tự rollback); thêm assertion vào `BillingPeriodCancelAndAuditRollbackIT` và `BillingPeriodServiceIT`. Không sửa mã nguồn `backend/src/main`. Hai phép thử đột biến (ranh giới lý do 255 thành 256; số revision khi submit tăng thêm 1) đều làm test thất bại, sau đó đã hoàn nguyên.
- **Chưa kiểm:** CI trên remote, máy thứ hai, thực thi thủ công end-to-end qua HTTP/session thật và toàn bộ giao diện S08. Chi tiết lệnh và giới hạn được ghi trong `docs/VALIDATION.md`.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `BIL-01.1` | ACCOUNTANT tạo kỳ thu nháp cho tháng hiện tại | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-01.2` | Từ chối tạo kỳ thu trùng tháng | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `BILLING_PERIOD_EXISTS` |
| `BIL-01.3` | Từ chối tạo kỳ thu cho tháng trước hoặc tháng sau | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `INVALID_PERIOD` |
| `BIL-01.4` | Từ chối period hoặc hạn nộp không hợp lệ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `INVALID_PERIOD`, `INVALID_DATE` |
| `BIL-01.5` | Race tạo kỳ thu cùng tháng | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `BILLING_PERIOD_EXISTS` |
| `BIL-02.1` | Xem danh sách và chi tiết kỳ thu | ADMIN, ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-02.2` | Từ chối filter sai và kỳ không tồn tại | ADMIN, ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-03.1` | ACCOUNTANT cập nhật hạn nộp kỳ DRAFT | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-03.2` | Từ chối cập nhật sai version hoặc trạng thái | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-04.1` | Preview tính đúng công thức và làm tròn | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-04.2` | Chặn preview/submit khi thiếu dữ liệu nguồn | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `NO_ACTIVE_HOUSEHOLDS`, `FEE_RATE_UNAVAILABLE` |
| `BIL-05.1` | Submit tạo snapshot revision 1 | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-05.2` | Withdraw, return và submit lại không ghi đè revision | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-05.3` | Từ chối return thiếu hoặc quá dài reason | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `REASON_REQUIRED` |
| `BIL-06.1` | Chặn issue khi revision/date/input không hợp lệ | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `STALE_VERSION`, `INVALID_DATE`, `BILLING_INPUT_CHANGED` |
| `BIL-06.2` | Issue tạo hóa đơn bất biến | ADMIN, ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-06.3` | Race issue cùng version/revision | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-06.4` | Phát hiện thay đổi tập hộ khi issue | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `BILLING_INPUT_CHANGED` |
| `BIL-07.1` | Hủy kỳ đã phát hành và lập kỳ thay thế | ADMIN, ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-07.2` | Chặn hủy khi đã thu hoặc thiếu lý do | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `PERIOD_HAS_RECEIPTS` |
| `BIL-07.3` | Race hủy kỳ và ghi receipt | ADMIN, payment | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `BIL-08.1` | Khóa kỳ thu của tháng cũ | ADMIN, ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `BILLING_PERIOD_EXPIRED` |
| `BIL-09.1` | Phân quyền API kỳ thu và hóa đơn | ANON, RESIDENT, ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `PASSWORD_CHANGE_REQUIRED` |
| `BIL-10.1` | UI lập, trình và phát hành kỳ thu | ACCOUNTANT, ADMIN | Bị chặn (Blocked) | Chưa có UI S08 chế độ API |
| `BIL-10.2` | UI xử lý conflict, 401 và giữ nháp | ACCOUNTANT, ADMIN, RESIDENT | Bị chặn (Blocked) | Chưa có UI S08 chế độ API |
| `BIL-10.3` | UI mobile, theme, keyboard và số tiền | ACCOUNTANT, ADMIN | Bị chặn (Blocked) | Chưa có UI S08 chế độ API |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### BIL-01.1 — ACCOUNTANT tạo kỳ thu nháp cho tháng hiện tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods`.
  - **Dữ liệu đầu vào:** `<THANG_NAY>` chưa có kỳ active; due date thuộc tháng; body `{"period":"<THANG_NAY>","dueDate":"<CUOI_THANG_NAY>"}` và CSRF hợp lệ.
  - **Các bước thực hiện:**
    1. Xác nhận tài khoản và dữ liệu nguồn.
    2. Gửi POST.
    3. GET lại kỳ vừa tạo và truy vấn audit.
  - **Kết quả mong đợi:** HTTP 201; `DRAFT`, version/revision bằng 0, chưa có tổng tiền; action gồm `PREVIEW`, `UPDATE`, `SUBMIT`.
  - **Kiểm tra sau (Post-check):** Có một bản ghi kỳ và audit `BILLING_PERIOD_CREATED` chỉ chứa các trường cho phép.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-01.1`; `BillingPeriodServiceTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodServiceIT#createsDraftAndAuditTogetherAndRejectsDuplicate` (DRAFT, receipt 0, đúng 1 audit `BILLING_PERIOD_CREATED`); `BillingCaseGapTest#newPeriodStartsAsDraftAtVersionAndRevisionZeroWithNoReceipts`; `BillingCaseGapTest#allowedActionsFollowRoleAndStatusForCurrentMonth` (PREVIEW/UPDATE/SUBMIT, version 0, revision 0); `BillingPeriodApiTest#accountantCanCreateAndAdminCanApprove` (HTTP 201).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: HTTP 201 được kiểm trên lát WebMvc với service giả; chưa có request thật qua session và CSRF.

### BIL-01.2 — Từ chối tạo kỳ thu trùng tháng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods`.
  - **Dữ liệu đầu vào:** Đã có kỳ active cùng `<THANG_NAY>`; payload tạo kỳ giống BIL-01.1.
  - **Các bước thực hiện:**
    1. Gửi request trùng tháng
    2. GET danh sách và đếm kỳ chưa hủy.
  - **Kết quả mong đợi:** HTTP 409, `code=BILLING_PERIOD_EXISTS`; không tạo bản ghi thứ hai.
  - **Kiểm tra sau (Post-check):** DB chỉ có một kỳ active và một audit tạo kỳ.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-01.2`; `BillingPeriodServiceTest`, `BillingPeriodConcurrencyIT`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodServiceTest#createRejectsAnExistingActivePeriod`; `BillingPeriodServiceIT#createsDraftAndAuditTogetherAndRejectsDuplicate`; `BillingSchemaIT#onlyOneNonCancelledPeriodPerMonthButCancelledHistoryCanRemain` (unique constraint); `BillingErrorMappingApiTest#duplicateMonthIsHttp409WithStableCode` (HTTP 409).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP 409 kiểm với service giả.

### BIL-01.3 — Từ chối tạo kỳ thu cho tháng trước hoặc tháng sau

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods`.
  - **Dữ liệu đầu vào:** Hai payload dùng `<THANG_TRUOC>` và `<THANG_SAU>`.
  - **Các bước thực hiện:**
    1. Gửi lần lượt hai request
    2. kiểm tra response và danh sách kỳ.
  - **Kết quả mong đợi:** Cả hai trả HTTP 400, `code=INVALID_PERIOD`; không có bản ghi mới.
  - **Kiểm tra sau (Post-check):** GET danh sách không có kỳ phát sinh từ hai request.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-01.3`; `BillingPeriodRulesTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingCaseGapTest#creationRejectsNextMonthAndMalformedPeriodsWithInvalidPeriod` (tháng trước, tháng sau, sai định dạng); `BillingCaseGapTest#invalidPeriodIsRejectedBeforeAnythingIsPersisted`; `BillingPeriodRulesTest#creationUsesVietnameseMonthAndRequiresDueDateInThatMonth`; `BillingPeriodServiceTest#createRejectsPastMonthBeforePersisting`; `BillingErrorMappingApiTest#wrongPeriodIsHttp400InvalidPeriod`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ranh giới tháng theo giờ Việt Nam được kiểm ở BIL-08.1.

### BIL-01.4 — Từ chối period hoặc hạn nộp không hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods`.
  - **Dữ liệu đầu vào:** Period `2026-13`, due date ngoài tháng và ngày không tồn tại.
  - **Các bước thực hiện:**
    1. Gửi ba payload
    2. ghi status/code từng response
    3. kiểm tra DB.
  - **Kết quả mong đợi:** Period sai trả `INVALID_PERIOD`; date sai trả `INVALID_DATE`, đều HTTP 400.
  - **Kiểm tra sau (Post-check):** Không tạo kỳ nháp.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-01.4`; `BillingPeriodRulesTest`, `BillingPeriodApiTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingCaseGapTest#creationRejectsNextMonthAndMalformedPeriodsWithInvalidPeriod` (`2026-13`, `2026-00`, `2026-1`, `abc`, rỗng, null); `BillingCaseGapTest#creationRejectsDueDateOutsideMonthOrMissingWithInvalidDate`; `BillingErrorMappingApiTest#unparsableOrNonexistentDueDateIsHttp400InvalidDateBeforeService` (`2026-02-30`, `2026-11-31`, `abc`, rỗng, null, năm 0999 và +10000; service không bị gọi); `BillingErrorMappingApiTest#dueDateOutsideMonthFromServiceIsHttp400InvalidDate`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### BIL-01.5 — Race tạo kỳ thu cùng tháng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** Hai request `POST /api/v1/billing-periods`.
  - **Dữ liệu đầu vào:** Cùng period/due date, hai worker và hai DB transaction có barrier.
  - **Các bước thực hiện:**
    1. Khởi tạo DB sạch
    2. chạy đồng thời
    3. thu hai response
    4. kiểm tra số kỳ và audit.
  - **Kết quả mong đợi:** Một HTTP 201, một HTTP 409 `BILLING_PERIOD_EXISTS`; đúng một kỳ và một audit.
  - **Kiểm tra sau (Post-check):** `billing_periods` active count bằng 1.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-01.5`; `BillingPeriodConcurrencyIT`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodConcurrencyIT#concurrentCreationHasOneWinner` (hai luồng cùng tháng: một `created`, một `BILLING_PERIOD_EXISTS`; đúng 1 kỳ chưa hủy trên MySQL thật).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Gọi service trực tiếp bằng hai luồng, không qua HTTP; chưa đếm số audit sau race.

### BIL-02.1 — Xem danh sách và chi tiết kỳ thu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/billing-periods`, `GET /api/v1/billing-periods/{id}`.
  - **Dữ liệu đầu vào:** Kỳ DRAFT có period, version và dữ liệu phân trang.
  - **Các bước thực hiện:**
    1. Gọi hai endpoint bằng từng vai trò
    2. so sánh sorting, `editable` và `allowedActions`.
  - **Kết quả mong đợi:** Cả hai HTTP 200; action đúng quyền, thứ tự period/id ổn định.
  - **Kiểm tra sau (Post-check):** Không trả entity hoặc trường ngoài contract.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-02.1`; `BillingPeriodApiTest`, `BillingPeriodRoutesSuccessApiTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodRoutesSuccessApiTest#getBillingPeriodsReturns200WithPageMetadataAndDto` và `#getBillingPeriodDetailReturns200WithFullMetadata`; `BillingCaseGapTest#allowedActionsFollowRoleAndStatusForCurrentMonth` (action theo vai trò và trạng thái); `BillingCaseGapTest#listSortsByPeriodThenIdDescendingAndValidatesFilters` (sắp xếp period rồi id giảm dần).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Thứ tự sắp xếp kiểm ở tham số truy vấn, chưa kiểm trên dữ liệu MySQL thật.

### BIL-02.2 — Từ chối filter sai và kỳ không tồn tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** GET danh sách và GET chi tiết kỳ thu.
  - **Dữ liệu đầu vào:** page/size/status/period không hợp lệ và ID không tồn tại.
  - **Các bước thực hiện:**
    1. Gửi từng truy vấn
    2. kiểm status, code và dữ liệu không thay đổi.
  - **Kết quả mong đợi:** Validation trả 400; ID lạ trả 404 với ProblemDetail cố định.
  - **Kiểm tra sau (Post-check):** Không lộ SQL, stack trace hoặc dữ liệu nội bộ.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-02.2`; `BillingReadStorePaginationTest`, `BillingPeriodApiTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodApiTest#invalidPageHasStableFilterCode`; `BillingCaseGapTest#listSortsByPeriodThenIdDescendingAndValidatesFilters` (`INVALID_PERIOD`, `INVALID_FILTER`); `BillingCaseGapTest#unknownPeriodIsNotFoundForDetailAndPreview`; `BillingErrorMappingApiTest#unknownPeriodIsHttp404WithoutInternalDetails` (HTTP 404, `code` cố định, không có `stackTrace`).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### BIL-03.1 — ACCOUNTANT cập nhật hạn nộp kỳ DRAFT

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/billing-periods/{id}`.
  - **Dữ liệu đầu vào:** Kỳ DRAFT, version hiện tại và due date hợp lệ.
  - **Các bước thực hiện:**
    1. Gửi PATCH có version
    2. GET lại kỳ
    3. kiểm tra audit.
  - **Kết quả mong đợi:** HTTP 200; due date đổi; version tăng; audit ghi thay đổi được phép.
  - **Kiểm tra sau (Post-check):** Kỳ vẫn DRAFT và không tạo snapshot ngoài ý muốn.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-03.1`; `BillingPeriodServiceTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodRevisionIT#updatingDueDateIncrementsVersionOnTheDatabaseAndRejectsTheOldVersion` (MySQL thật: version 0 → 1 sau PATCH, `due_date` đổi, kỳ vẫn DRAFT, đúng 1 audit `BILLING_PERIOD_UPDATED`, version cũ bị `STALE_VERSION` và dữ liệu không đổi); `BillingPeriodServiceTest#draftCanChangeDueDateAndRejectsStaleVersion`; `BillingCaseGapTest#updateDueDateWritesAuditWithOnlyAllowedFields`; `BillingPeriodRoutesSuccessApiTest#patchBillingPeriodDueDateReturns200AndDelegatesToService`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: HTTP 200 kiểm trên lát WebMvc với service giả; phần DB kiểm bằng gọi service trực tiếp.

### BIL-03.2 — Từ chối cập nhật sai version hoặc trạng thái

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/billing-periods/{id}`.
  - **Dữ liệu đầu vào:** Thiếu/sai version và các kỳ không ở DRAFT.
  - **Các bước thực hiện:**
    1. Gửi từng request
    2. GET lại kỳ sau mỗi request.
  - **Kết quả mong đợi:** HTTP 400/409 theo contract; version và due date không đổi.
  - **Kiểm tra sau (Post-check):** Không có audit update giả.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-03.2`; `BillingPeriodServiceTest`, `BillingMutationSourceAndRaceTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodServiceTest#draftCanChangeDueDateAndRejectsStaleVersion` (`STALE_VERSION`); `BillingPeriodServiceTest#invalidTransitionsAreRejectedForEachState` (`INVALID_STATE` với mọi trạng thái khác DRAFT); `BillingPeriodServiceTest#expiredDraftCannotBeChanged`; `BillingErrorMappingApiTest#missingVersionIsHttp400Validation` (thiếu version trả 400 `VALIDATION`).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: "Dữ liệu không đổi" kiểm ở mức entity trong test đơn vị.

### BIL-04.1 — Preview tính đúng công thức và làm tròn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/billing-periods/{id}/preview`.
  - **Dữ liệu đầu vào:** Hộ ACTIVE, diện tích, đơn giá quản lý/rác và kỳ DRAFT.
  - **Các bước thực hiện:**
    1. Gọi preview
    2. đối chiếu từng line, tổng, diện tích và đơn giá snapshot.
  - **Kết quả mong đợi:** BigDecimal, HALF_UP đến đồng; không giảm phí tạm vắng ngoài quy tắc.
  - **Kiểm tra sau (Post-check):** Preview không ghi dữ liệu phát hành.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-04.1`; `BillingPeriodServiceTest`, `BillingF1SnapshotPaginationTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingCaseGapTest#previewOfDraftUsesBigDecimalHalfUpAndWritesNothing` (10,005 × 100 = 1000,5 → 1001; 30000,50 → 30001; tổng 31002; không lưu snapshot, không audit); `FeeCalculatorTest#roundsEachFeeToWholeDong`; `BillingPeriodServiceTest#submissionCalculatesTwoRoundedLinesForEveryActiveHousehold` (500000 + 30000); `BillingPeriodServiceIT#issueUsesApprovedSnapshotEvenWhenSourcesChangeAfterIssue` (hộ có cư dân `TEMPORARILY_ABSENT` vẫn 530000, không giảm phí).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Chưa đối chiếu độc lập với dữ liệu hộ/đơn giá thật ngoài các fixture của test.

### BIL-04.2 — Chặn preview/submit khi thiếu dữ liệu nguồn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** Preview và submit kỳ DRAFT.
  - **Dữ liệu đầu vào:** Một trường hợp không có hộ ACTIVE và một trường hợp thiếu đơn giá.
  - **Các bước thực hiện:**
    1. Gọi preview và submit cho từng trường hợp
    2. đọc lại kỳ.
  - **Kết quả mong đợi:** `NO_ACTIVE_HOUSEHOLDS` hoặc `FEE_RATE_UNAVAILABLE`; kỳ vẫn DRAFT, revision bằng 0.
  - **Kiểm tra sau (Post-check):** Không sinh snapshot rỗng hoặc total bằng 0 giả.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-04.2`; `BillingPeriodServiceIT`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodRevisionIT#missingFeeRatesBlockPreviewAndSubmitWithoutCreatingASnapshot` (MySQL thật: không có đơn giá, rồi chỉ có đơn giá phí quản lý → preview và submit đều `FEE_RATE_UNAVAILABLE`; kỳ vẫn DRAFT, revision 0, không có snapshot); `BillingPeriodServiceTest#noActiveHouseholdsPreventsSubmission` và `BillingCaseGapTest#previewWithNoActiveHouseholdsIsConflictAndWritesNothing` (`NO_ACTIVE_HOUSEHOLDS`); `BillingErrorMappingApiTest#submitWithoutSourceDataIsHttp409` (HTTP 409).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP 409 kiểm với service giả.

### BIL-05.1 — Submit tạo snapshot revision 1

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** Submit, preview và submissions của kỳ.
  - **Dữ liệu đầu vào:** DRAFT version 0, hai hộ ACTIVE, đủ đơn giá.
  - **Các bước thực hiện:**
    1. ACCOUNTANT submit
    2. ADMIN đọc kỳ/preview/submissions
    3. đối chiếu tổng và pagination.
  - **Kết quả mong đợi:** `PENDING_APPROVAL`, revision 1, count/total đúng; snapshot đọc lại ổn định.
  - **Kiểm tra sau (Post-check):** Audit `BILLING_PERIOD_SUBMITTED` có period/revision/count/total.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-05.1`; `BillingPeriodServiceIT`, `BillingF1SnapshotPaginationTest`, `BillingSubmissionPaginationTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodServiceTest#submissionCalculatesTwoRoundedLinesForEveryActiveHousehold` (PENDING_APPROVAL, revision 1, tổng); `BillingCaseGapTest#submitAuditCarriesPeriodRevisionCountAndTotalOnly` (audit gồm period, revision, count, total); `BillingPeriodServiceIT#snapshotStoreLoadsOnlyRequestedHouseholdsAndTheirTwoLines` (snapshot và phân trang trên MySQL thật); `BillingF1SnapshotPaginationTest`; `BillingSubmissionPaginationTest`; `BillingPeriodRoutesSuccessApiTest#getSubmissionsReturns200WithRevisionAndHouseholdsPage`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### BIL-05.2 — Withdraw, return và submit lại không ghi đè revision

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `/withdraw`, `/return`, `/submit`, `/submissions`.
  - **Dữ liệu đầu vào:** Kỳ PENDING, version/revision liên tiếp; reason hợp lệ; sau đó thay đổi diện tích hộ.
  - **Các bước thực hiện:**
    1. Withdraw
    2. submit lại
    3. return có reason
    4. thay đổi nguồn
    5. submit revision mới
    6. đọc revision cũ/mới.
  - **Kết quả mong đợi:** Revision cũ bất biến; revision mới phản ánh dữ liệu mới; reason được trim và không ghi nguyên văn vào audit.
  - **Kiểm tra sau (Post-check):** Audit có withdrawn/returned/submitted; revision 99 trả 404.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-05.2`; `BillingPeriodCancellationTest`, `BillingSubmissionPaginationTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodRevisionIT#resubmittingKeepsOlderRevisionsImmutableAndReasonIsNotWrittenToAudit` (MySQL thật: submit revision 1, withdraw về DRAFT giữ revision 1, đổi diện tích 50 → 60, submit revision 2; snapshot revision 1 giữ diện tích 50 và 500000, revision 2 có 60 và 600000; return có lý do được trim và lưu trên kỳ nhưng không có trong audit; submit lại tạo revision 3 và xóa lý do; revision 99 trả `BILLING_SUBMISSION_NOT_FOUND`); `BillingPeriodServiceTest#withdrawnSubmissionKeepsRevisionAndBecomesDraft`; `AuditWhitelistAndRollbackTest#billingPeriodAuditPayloadsNeverContainForbiddenReasonsOrPII`. Đã thử đột biến số revision khi submit: test thất bại, sau đó đã hoàn nguyên.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Gọi service trực tiếp, không qua HTTP.

### BIL-05.3 — Từ chối return thiếu hoặc quá dài reason

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods/{id}/return`.
  - **Dữ liệu đầu vào:** Kỳ PENDING; reason toàn khoảng trắng và reason 256 ký tự.
  - **Các bước thực hiện:**
    1. Gửi hai payload
    2. đọc lại trạng thái và version.
  - **Kết quả mong đợi:** HTTP 400, `code=REASON_REQUIRED`; kỳ vẫn PENDING.
  - **Kiểm tra sau (Post-check):** Không có audit returned.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-05.3`; `BillingPeriodRulesTest`, `BillingPeriodApiTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingCaseGapTest#returnReasonAccepts255CodePointsAndRejects256` (255 ký tự chấp nhận, 256, khoảng trắng và null bị từ chối, trạng thái không đổi); `BillingCaseGapTest#returnReasonLimitCountsCodePointsNotUtf16Units`; `BillingPeriodServiceTest#returningSubmissionRequiresReasonAndStoresIt`; `BillingErrorMappingApiTest#returnWithoutReasonIsHttp400ReasonRequired`. Đã thử đột biến ranh giới 255 thành 256: hai test đầu thất bại, sau đó đã hoàn nguyên.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### BIL-06.1 — Chặn issue khi revision/date/input không hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods/{id}/issue`.
  - **Dữ liệu đầu vào:** Revision cũ; due date đã qua; dữ liệu hộ thay đổi sau snapshot.
  - **Các bước thực hiện:**
    1. Issue từng trường hợp
    2. đọc kỳ và invoices sau mỗi lần.
  - **Kết quả mong đợi:** `STALE_VERSION`, `INVALID_DATE`, `BILLING_INPUT_CHANGED`; không sinh hóa đơn.
  - **Kiểm tra sau (Post-check):** Kỳ vẫn PENDING và không có invoice mới.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-06.1`; `BillingMutationSourceAndRaceTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingMutationSourceAndRaceTest#issueRejectsStaleRevision`, `#issueRejectsStalePeriodVersion`, `#issueRejectsWhenDueDateHasPassedAtIssueTime`, `#issueRejectsWhenAreaChangedBetweenSubmitAndIssue`, `#issueRejectsWhenActiveHouseholdsCountChanged`, `#issueRejectsWhenFeeRatePriceChanged`; `BillingPeriodServiceTest#issueRejectsChangedHouseholdSetWithoutCreatingInvoices` (xác nhận không gọi tạo hóa đơn); `BillingErrorMappingApiTest#issueConflictsAreHttp409WithStableCodes`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### BIL-06.2 — Issue tạo hóa đơn bất biến

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** Issue kỳ và GET invoices.
  - **Dữ liệu đầu vào:** Kỳ PENDING có revision hợp lệ, hộ ACTIVE và đủ đơn giá.
  - **Các bước thực hiện:**
    1. ADMIN issue
    2. đọc invoices
    3. thay đổi diện tích/đơn giá nguồn
    4. đọc lại invoice.
  - **Kết quả mong đợi:** Issue thành công; invoice giữ diện tích, đơn giá và tổng snapshot ban đầu.
  - **Kiểm tra sau (Post-check):** Không có invoice trùng; audit issue có tổng và revision.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-06.2`; `BillingPeriodServiceIT`, `BillingF1SnapshotPaginationTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodServiceIT#issueUsesApprovedSnapshotEvenWhenSourcesChangeAfterIssue` (sau khi đổi diện tích 50 → 60 và thêm hộ ACTIVE mới: hóa đơn giữ tổng 530000 và 2 dòng; dòng quản lý phí giữ diện tích 50, đơn giá 10000, thành tiền 500000; hộ mới không có hóa đơn trong kỳ; đúng 1 audit `BILLING_PERIOD_ISSUED`; `reads.invoice(999999999)` trả `INVOICE_NOT_FOUND`); `BillingCaseGapTest#issuedPeriodOffersCancelOnlyToAdminAndOnlyForCurrentMonthWithoutReceipts` (ADMIN `[CANCEL]`, ACCOUNTANT `[]`); `BillingCaseGapTest#previewOfAnIssuedOrCancelledPeriodIsInvalidState` (`INVALID_STATE`); `BillingPeriodApiTest#accountantCanCreateAndAdminCanApprove` và `#writeRolesAndCsrfAreEnforcedBeforeService` (ADMIN phát hành, ACCOUNTANT bị 403); `BillingAuditRollbackIT#auditFailureRollsBackInvoicesAndIssueState` (audit cùng transaction).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: HTTP 404 `INVOICE_NOT_FOUND` kiểm ở mức store, chưa qua controller; `status=ISSUED` và `issuedAt` đọc qua service, chưa qua HTTP thật.

### BIL-06.3 — Race issue cùng version/revision

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** Hai request issue cùng kỳ.
  - **Dữ liệu đầu vào:** Cùng version và revision trong hai transaction.
  - **Các bước thực hiện:**
    1. Chạy đồng thời
    2. thu hai response
    3. đếm invoice và audit.
  - **Kết quả mong đợi:** Một request thành công, request còn lại conflict; chỉ một bộ invoice.
  - **Kiểm tra sau (Post-check):** Kỳ ISSUED đúng một lần, không duplicate invoice.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-06.3`; `BillingPeriodConcurrencyIT`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodConcurrencyIT#concurrentIssueCreatesOneInvoiceSet` (một `issued`, một `STALE_VERSION`; đúng 1 hóa đơn với 2 dòng trên MySQL thật).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Gọi service trực tiếp bằng hai luồng, không qua HTTP.

### BIL-06.4 — Phát hiện thay đổi tập hộ khi issue

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods/{id}/issue`.
  - **Dữ liệu đầu vào:** Thêm/kích hoạt hộ hoặc đổi diện tích sau snapshot.
  - **Các bước thực hiện:**
    1. Tạo thay đổi nguồn
    2. issue bằng revision cũ
    3. đọc lại kỳ/invoice.
  - **Kết quả mong đợi:** HTTP 409 `BILLING_INPUT_CHANGED`; không phát hành nửa chừng.
  - **Kiểm tra sau (Post-check):** Kỳ PENDING, không có invoice.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-06.4`; `BillingMutationSourceAndRaceTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingMutationSourceAndRaceTest` (đổi diện tích, số hộ ACTIVE, đơn giá, cách tính → `BILLING_INPUT_CHANGED`); `BillingInputsTest#sameTotalWithDifferentHouseholdOrRateStillConflicts` (cùng tổng tiền vẫn xung đột); `BillingPeriodServiceIT#sourceAreaChangeAfterSubmissionBlocksIssue` (DB thật); `ActiveHouseholdLockIT` (4 test: thêm hộ, kích hoạt, đổi diện tích, ngừng hộ phải chờ transaction snapshot).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### BIL-07.1 — Hủy kỳ đã phát hành và lập kỳ thay thế

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/cancel` và POST tạo kỳ thay thế.
  - **Dữ liệu đầu vào:** Kỳ ISSUED trong tháng, receipt count bằng 0.
  - **Các bước thực hiện:**
    1. ADMIN hủy
    2. kiểm tra lịch sử
    3. ACCOUNTANT tạo kỳ thay thế.
  - **Kết quả mong đợi:** Kỳ CANCELLED, lý do/audit hợp lệ, dữ liệu lịch sử và invoice được giữ theo contract.
  - **Kiểm tra sau (Post-check):** Kỳ thay thế liên kết đúng `replacesPeriodId`.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-07.1`; `BillingPeriodCancellationTest`, `BillingPeriodCancelAndAuditRollbackIT`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodCancelAndAuditRollbackIT#cancellationKeepsSnapshotsAndMarksAllInvoicesCancelledAndAllowsReplacementPeriod` (kỳ CANCELLED, hóa đơn CANCELLED có `cancelled_at`, snapshot được giữ, kỳ thay thế tạo được và `replacesPeriodId` trỏ đúng kỳ bị hủy); `BillingPeriodCancellationTest#cancelSuccessfullyTransitionsIssuedPeriodToCancelledAndCancelsInvoices` (lý do được trim, audit chỉ gồm period và status).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Assertion `replacesPeriodId` được thêm trong lần cập nhật này.

### BIL-07.2 — Chặn hủy khi đã thu hoặc thiếu lý do

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/billing-periods/{id}/cancel`.
  - **Dữ liệu đầu vào:** Kỳ ISSUED có receipt và payload thiếu/rỗng lý do.
  - **Các bước thực hiện:**
    1. Gửi từng request
    2. đọc lại kỳ, invoice và audit.
  - **Kết quả mong đợi:** Có receipt trả `PERIOD_HAS_RECEIPTS`; thiếu lý do trả validation; dữ liệu không đổi.
  - **Kiểm tra sau (Post-check):** Không có audit cancelled khi request bị từ chối.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-07.2`; `BillingPeriodCancellationTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodCancellationTest#cancelRejectsWhenReceiptCountIsPositive` (`PERIOD_HAS_RECEIPTS`), `#cancelRejectsBlankOrNullReason`, `#cancelRejectsReasonExceeding255CodePoints`, `#cancelRejectsNonIssuedStatus`, `#cancelRejectsStaleVersion` (mỗi trường hợp xác nhận không hủy hóa đơn); `BillingPeriodServiceTest#cancellationRejectsAnyPreviouslyCommittedReceipt`; `BillingErrorMappingApiTest#cancelConflictsAreHttp409WithStableCodes`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: "Không có audit khi bị từ chối" được suy ra từ việc ngoại lệ xảy ra trước bước audit, chưa có assertion riêng.

### BIL-07.3 — Race hủy kỳ và ghi receipt

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN và payment module.
  - **Điểm vào (Route / Endpoint):** `BillingPeriodService.cancel`, `BillingReceiptGuard.recordReceipt`.
  - **Dữ liệu đầu vào:** Hai DB transaction thật, kỳ ISSUED và receipt count bằng 0.
  - **Các bước thực hiện:**
    1. Chạy hai thứ tự tranh chấp
    2. giải phóng latch
    3. kiểm kết quả và DB.
  - **Kết quả mong đợi:** Transaction thắng quyết định trạng thái; transaction thua rollback; không tăng receipt_count sai.
  - **Kiểm tra sau (Post-check):** Audit chỉ tồn tại cho thao tác commit.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-07.3`; `BillingReceiptRaceIT`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingReceiptRaceIT#receiptCommittedFirstBlocksCancellation` và `#cancellationCommittedFirstBlocksReceipt` (hai thứ tự tranh chấp trên MySQL thật); `BillingReceiptGuardTest#recordsReceiptOnlyForIssuedPeriod`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### BIL-08.1 — Khóa kỳ thu của tháng cũ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** GET/PATCH và các action submit/withdraw/return/issue/cancel.
  - **Dữ liệu đầu vào:** Kỳ DRAFT, PENDING và ISSUED của `<THANG_TRUOC>`.
  - **Các bước thực hiện:**
    1. Đọc cờ expired/action
    2. thử tất cả thao tác ghi
    3. đọc lại lịch sử.
  - **Kết quả mong đợi:** Kỳ cũ chỉ đọc; thao tác ghi trả 409 `BILLING_PERIOD_EXPIRED`; lịch sử vẫn 200.
  - **Kiểm tra sau (Post-check):** Submission/invoice cũ không bị thay đổi.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-08.1`; `BillingPeriodRulesTest`, `BillingPeriodServiceTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingCaseGapTest#everyWriteActionOnAPastMonthIsRejectedAsExpiredAndChangesNothing` (cả 6 thao tác ghi: update, submit, withdraw, return, issue, cancel → `BILLING_PERIOD_EXPIRED`, không tạo snapshot, hóa đơn hay audit); `BillingCaseGapTest#pastMonthDraftIsExpiredAndOffersNoWriteAction` và `#issuedPeriodOffersCancelOnlyToAdminAndOnlyForCurrentMonthWithoutReceipts` (kỳ DRAFT/PENDING tháng cũ: `expired=true`, `editable=false`, `[PREVIEW]`; kỳ ISSUED tháng cũ: `expired=false`, `editable=false`, `[]`); `BillingCaseGapTest#historyOfAPastMonthPeriodStaysReadable` (submissions và invoices của kỳ cũ vẫn đọc được); `BillingCaseGapTest#monthBoundaryUsesVietnamTimeNotUtc` (16:59:59Z là 23:59:59 ngày 31/10, 17:30Z là 00:30 ngày 01/11 giờ Việt Nam); `BillingErrorMappingApiTest#issueConflictsAreHttp409WithStableCodes` và `#cancelConflictsAreHttp409WithStableCodes` (HTTP 409).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Đọc lịch sử kỳ cũ kiểm ở mức service, chưa qua HTTP 200 thật.

### BIL-09.1 — Phân quyền API kỳ thu và hóa đơn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON, RESIDENT, ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `/api/v1/billing-periods/**`, `/api/v1/invoices/*`.
  - **Dữ liệu đầu vào:** Kỳ/invoice tồn tại; ACCOUNTANT hợp lệ, ACCOUNTANT phải đổi mật khẩu và RESIDENT.
  - **Các bước thực hiện:**
    1. Gọi GET/POST/PATCH theo ma trận
    2. thử thiếu/sai CSRF
    3. kiểm dữ liệu sau request.
  - **Kết quả mong đợi:** ANON 401; sai role 403; thiếu CSRF 403; must-change trả `PASSWORD_CHANGE_REQUIRED`.
  - **Kiểm tra sau (Post-check):** Dữ liệu kỳ thu không đổi sau request bị chặn.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-09.1`; `BillingPeriodApiTest`, `BillingPeriodRoutesSuccessApiTest`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `BillingPeriodApiTest#staffReadsAreDeniedToResidentAndAnonymous` (401, 403, `PASSWORD_CHANGE_REQUIRED` với các route GET); `BillingPeriodApiTest#writeRolesAndCsrfAreEnforcedBeforeService` (thiếu CSRF → 403); `BillingPeriodApiTest#everyBillingRouteRejectsAnonymousAndWrongRole` (13 route); `BillingPeriodApiTest#inactiveAccountIsRejectedBeforeNewApi`; `BillingErrorMappingApiTest#passwordChangeRequiredBlocksEveryWriteRouteBeforeTheService` (service không bị gọi).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Kiểm trên lát WebMvc với `SecurityConfig` thật, nhưng service và tài khoản là đối tượng giả.

### BIL-10.1 — UI lập, trình và phát hành kỳ thu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `/accountant/billing-periods`, `/admin/billing-periods`.
  - **Dữ liệu đầu vào:** Chế độ API; hộ ACTIVE; đủ đơn giá; kỳ tháng hiện tại chưa tồn tại.
  - **Các bước thực hiện:**
    1. ACCOUNTANT tạo/trình
    2. ADMIN mở kỳ và phát hành
    3. đối chiếu snapshot, tổng và số hộ.
  - **Kết quả mong đợi:** Nút và action đúng vai trò; dialog hiển thị đúng tổng/revision; phát hành thành công.
  - **Kiểm tra sau (Post-check):** UI không hiện action trái quyền và không dùng dữ liệu demo trong API mode.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-10.1`; contract S08.
- **Kết quả kiểm thử:** **Bị chặn (Blocked)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Chưa thực thi; chưa có UI S08 chế độ API để thao tác và chưa có test cho ca này.
- **Chi tiết lỗi / Nhận xét:** Màn hình `frontend/src/screens/admin/BillingPeriodsPage.tsx` chỉ là UI demo: dùng `useAppState` với dữ liệu trong bộ nhớ, không gọi `/api/v1/billing-periods`, không có client billing trong `frontend/src/api`, không có route `/accountant/billing-periods`, và không có test Vitest hay Playwright cho kỳ thu. Cần làm UI S08 ở chế độ API trước khi kiểm thử.

### BIL-10.2 — UI xử lý conflict, 401 và giữ nháp

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN, RESIDENT.
  - **Điểm vào (Route / Endpoint):** Các màn hình billing period theo API mode.
  - **Dữ liệu đầu vào:** Response `BILLING_INPUT_CHANGED`, `STALE_VERSION`, 401 và mode demo/API.
  - **Các bước thực hiện:**
    1. Kích hoạt từng lỗi
    2. kiểm thông báo, dialog, draft và điều hướng.
  - **Kết quả mong đợi:** Hiển thị thông báo cố định; buộc tải lại khi stale; giữ due date trong bộ nhớ phiên; không fallback demo.
  - **Kiểm tra sau (Post-check):** Không lưu draft vào localStorage/sessionStorage.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-10.2`; contract S08.
- **Kết quả kiểm thử:** **Bị chặn (Blocked)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Chưa thực thi; chưa có UI S08 chế độ API để thao tác và chưa có test cho ca này.
- **Chi tiết lỗi / Nhận xét:** Màn hình `frontend/src/screens/admin/BillingPeriodsPage.tsx` chỉ là UI demo: dùng `useAppState` với dữ liệu trong bộ nhớ, không gọi `/api/v1/billing-periods`, không có client billing trong `frontend/src/api`, không có route `/accountant/billing-periods`, và không có test Vitest hay Playwright cho kỳ thu. Cần làm UI S08 ở chế độ API trước khi kiểm thử.

### BIL-10.3 — UI mobile, theme, keyboard và số tiền

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** Màn hình billing period ở viewport 360px.
  - **Dữ liệu đầu vào:** Theme sáng/tối; keyboard-only; date `badInput`; số tiền lớn dạng chuỗi.
  - **Các bước thực hiện:**
    1. Chạy viewport 360px
    2. điều hướng bằng keyboard
    3. nhập dữ liệu biên
    4. kiểm focus, layout và số.
  - **Kết quả mong đợi:** Không tràn ngang; focus nhìn thấy; date sai bị chặn; số tiền không mất chữ số.
  - **Kiểm tra sau (Post-check):** Không có lỗi console/network ngoài dự kiến.
  - **Nguồn đối chiếu:** `tests/cases/billing.md:BIL-10.3`; contract S08.
- **Kết quả kiểm thử:** **Bị chặn (Blocked)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Chưa thực thi; chưa có UI S08 chế độ API để thao tác và chưa có test cho ca này.
- **Chi tiết lỗi / Nhận xét:** Màn hình `frontend/src/screens/admin/BillingPeriodsPage.tsx` chỉ là UI demo: dùng `useAppState` với dữ liệu trong bộ nhớ, không gọi `/api/v1/billing-periods`, không có client billing trong `frontend/src/api`, không có route `/accountant/billing-periods`, và không có test Vitest hay Playwright cho kỳ thu. Cần làm UI S08 ở chế độ API trước khi kiểm thử.
