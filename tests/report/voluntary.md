# Báo cáo kiểm thử chi tiết: Đợt đóng góp tự nguyện và sổ ghi nhận

- **Tệp đặc tả nguồn:** [`tests/cases/voluntary.md`](../cases/voluntary.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** 15 ca từ `VOL-01.1` đến `VOL-08.3` của phân hệ đóng góp tự nguyện (S08): tạo, sửa, công bố và đóng đợt, tầm nhìn của cư dân, tìm người đóng, ghi nhận khoản đóng góp (cửa sổ ngày thực thu, lý do nhập muộn, khóa `Idempotency-Key`, race), hủy khoản đóng góp, phân quyền RBAC/CSRF và giao diện `/admin/voluntary`, `/accountant/voluntary`, `/resident/voluntary`. Các ID số trong đặc tả là ID mẫu; test dùng dữ liệu tự sinh trên database kiểm thử riêng. Không ghi mật khẩu, CSRF token, cookie hoặc dữ liệu cư dân thật vào bằng chứng.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **15** |
| Số ca Đạt (Pass) | **14** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **1** |
| Tỷ lệ Đạt | **93.33%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- **Cách xác định kết quả "Đạt" của báo cáo này:** khác với các báo cáo phân hệ khác, các ca `VOL-01` đến `VOL-07` **chưa được thực thi thủ công** qua HTTP, session và CSRF thật, và các ca `VOL-08.1`, `VOL-08.2` chưa được thao tác trên trình duyệt thật. Mỗi ca được xếp "Đạt" khi có test tự động cụ thể (unit, lát WebMvc, integration trên MySQL thật hoặc Vitest) xác nhận kết quả mong đợi chính của ca đó: mã trạng thái, `code` lỗi, trạng thái dữ liệu hoặc hành vi giao diện. Tên test nằm ở trường "Thực tế ghi nhận" của từng ca. "Đạt" ở đây không thay thế kiểm thử thủ công end-to-end; giới hạn của từng ca được ghi ở "Chi tiết lỗi / Nhận xét".
- **`VOL-08.3` (Bị chặn):** ca yêu cầu trình duyệt thật cho bố cục 360px, theme sáng/tối với độ tương phản WCAG AA, điều hướng bàn phím với viền focus và ô ngày `badInput` thật. jsdom không kiểm được các điểm này nên ca không được xếp Đạt. Phần jsdom kiểm được (chặn gửi khi ngày trống hoặc đảo ngược, số tiền 18 chữ số hiển thị đúng, số tiền 19 chữ số bị chặn) đã Pass và được liệt kê ở ca.
- **Nhãn trong đặc tả lệch giao diện (cần quyết định):** `VOL-08.1` ghi `Tạo đợt đóng góp`, `Lưu` và `Tổng đã ghi`, còn giao diện hiện là `Tạo đợt vận động mới`, `Lưu nháp` và `Tổng thu đợt này` (kèm `Đã ghi nhận N lượt đóng góp`); `VOL-08.3` ghi thông báo `Kiểm tra các ngày và thông tin đợt đóng góp.`, còn giao diện là `Kiểm tra tên đợt và ngày kết thúc không trước ngày bắt đầu.`. Hành vi khớp đặc tả nên các ca vẫn xếp Đạt; nếu muốn xử lý như `BUG-002` (lệch đặc tả) thì cần sửa `tests/cases/voluntary.md` hoặc đổi nhãn giao diện. Báo cáo này không sửa cả hai.
- **Giải đáp điểm chưa xác định trong đặc tả:** (1) `POST /api/v1/voluntary-contributions/{id}/void` với ID không tồn tại trả HTTP 404 `CONTRIBUTION_NOT_FOUND`, được xác nhận bởi `VoluntaryCaseIT#voidKeepsHistoryUpdatesTotalsAndIsIdempotent`, `VoluntaryCaseGapTest#voidOfUnknownContributionIsNotFound` và `VoluntaryErrorMappingApiTest#voidErrorsKeepTheirStatusAndCode`; mã này chưa có trong dòng tóm tắt 404 của hợp đồng S08. (2) Backend hỗ trợ `?status=` nhưng giao diện không gửi tham số này: `listCampaigns` trong `frontend/src/api/voluntary.ts` chỉ gửi `page` và `size`.
- **Môi trường chạy:** Windows, JDK 21.0.12.1 (JDK mặc định của máy là 26 nên Maven Enforcer từ chối; phải đặt `JAVA_HOME`), Node 22.19.0. MySQL 8.4 trong container `bluemoon-db-1` cổng `3306`; integration chạy trên schema riêng `bluemoon_it`, không dùng schema `bluemoon` của backend. Chỉ container MySQL được bật (container backend không cần cho các test này).
- **Lệnh và kết quả (10/11/2026):** backend package billing (gồm voluntary) **172 test unit/API** và **37 test integration**, 0 failure, 0 error, 0 skipped, `BUILD SUCCESS`; kiểm tra chuẩn `./mvnw.cmd -B -ntp verify` toàn backend (không profile integration) **296 test**, `BUILD SUCCESS`; frontend `npx vitest run` **149 test (29 file)** Pass và `npx tsc -b` không lỗi. Chạy toàn bộ như CI (`./mvnw.cmd -B -ntp -Pintegration verify`, không lọc, schema trống): **296 test unit và 157 test integration**, `BUILD SUCCESS`.

  ```powershell
  $env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.12.1'
  $env:Path="$env:JAVA_HOME\bin;$env:Path"
  $env:DB_HOST='127.0.0.1'; $env:DB_PORT='<DB_PORT trong .env>'; $env:DB_NAME='bluemoon_it'
  $env:DB_USER='<MYSQL_USER trong .env>'; $env:DB_PASSWORD='<MYSQL_PASSWORD trong .env>'
  cd backend
  .\mvnw.cmd -B -ntp -Pintegration verify "-Dtest=vn/edu/se10/bluemoon/billing/*Test" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dit.test=vn/edu/se10/bluemoon/billing/*IT" "-Dfailsafe.failIfNoSpecifiedTests=false"
  cd ../frontend
  npx vitest run
  npx tsc -b
  ```

- **Test được thêm để lấp khoảng trống của test cũ (61 test):** `VoluntaryCaseGapTest` (22 test), `VoluntaryErrorMappingApiTest` (16 test), `VoluntaryCaseIT` (8 test, MySQL thật, dữ liệu commit và dọn theo ID vì `record` dùng `Propagation.NEVER`; là `@SpringBootTest` thuần dùng đồng hồ thật để dùng chung Spring context đã cache, vì mỗi context riêng giữ 10 kết nối và MySQL mặc định chỉ cho 151: một context thừa làm `PaymentConcurrencyIT` lỗi `Too many connections` khi chạy toàn bộ integration test), `frontend/src/test/s08-voluntary-cases.test.tsx` (15 test). Không sửa mã nguồn `backend/src/main` và `frontend/src` ngoài thư mục test. Ba phép thử đột biến đều làm test thất bại, sau đó đã hoàn nguyên: bỏ ràng buộc "không trước ngày công bố" trong cửa sổ thu; bỏ escape `%` trong tìm người đóng; làm lỗi HTTP 400 không mở khóa biểu mẫu. Phép thử thứ tư (nới regex số tiền của giao diện lên 19 chữ số) không làm test thất bại vì ô nhập còn thuộc tính `pattern` chặn song song; số tiền 19 chữ số vì vậy được chặn bằng hai lớp.
- **Chưa kiểm:** CI trên remote, máy thứ hai, thực thi thủ công end-to-end qua HTTP/session thật, trình duyệt thật với backend thật, và các điểm của `VOL-08.3` nêu trên. Chi tiết lệnh và giới hạn được ghi trong `docs/VALIDATION.md`. Báo cáo này chưa nằm trong bảng tổng hợp 432 ca của `tests/report/README.md`.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `VOL-01.1` | ADMIN tạo và cập nhật đợt đóng góp tự nguyện khi còn ở trạng thái DRAFT | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `VOL-01.2` | Từ chối tạo hoặc sửa đợt khi tên/mô tả sai giới hạn, ngày kết thúc trước ngày bắt đầu hoặc sai version | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `VALIDATION`, `INVALID_DATE`, `STALE_VERSION` |
| `VOL-02.1` | ADMIN công bố đợt (kể cả công bố trước ngày bắt đầu), khóa sửa nháp và đóng đợt sớm | ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `INVALID_STATE` |
| `VOL-02.2` | Tầm nhìn RESIDENT: chỉ thấy đợt đã công bố với DTO rút gọn, nháp trả 404 và đợt quá hạn hiển thị `CLOSED` | RESIDENT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `CAMPAIGN_NOT_FOUND` |
| `VOL-03.1` | ACCOUNTANT tìm người đóng qua `/voluntary-donors` gồm cả nhân khẩu MOVED_OUT/hộ INACTIVE và ẩn PII | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `INVALID_FILTER` |
| `VOL-04.1` | Ghi nhận khoản đóng góp hợp lệ (bao gồm nhân khẩu MOVED_OUT/hộ INACTIVE và đóng nhiều lần với key mới) | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `VOL-04.2` | Quy tắc cửa sổ ngày thực thu (`paidOn`) và bắt buộc `lateReason` khi nhập muộn sau đóng hoặc sau hạn | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `CAMPAIGN_NOT_RECEIVABLE`, `INVALID_PAID_DATE`, `REASON_REQUIRED` |
| `VOL-04.3` | Từ chối khoản đóng góp khi số tiền, khóa Idempotency-Key, nhân khẩu hoặc độ dài lý do không hợp lệ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động; `INVALID_AMOUNT`, `INVALID_IDEMPOTENCY_KEY`, `VALIDATION` |
| `VOL-05.1` | Idempotency replay (HTTP 200), xung đột khác payload (`IDEMPOTENCY_CONFLICT`) và replay sau khi đợt đóng hoặc bản ghi đã hủy | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `IDEMPOTENCY_CONFLICT` |
| `VOL-05.2` | Race condition: Hai request ghi nhận đóng góp đồng thời cùng actor, cùng Idempotency-Key và cùng payload chỉ tạo một bản ghi và một audit | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `VOL-06.1` | ACCOUNTANT hủy khoản đóng góp (`void`), kiểm tra tính bất biến lịch sử, idempotent khi hủy lặp và cập nhật tổng thu | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động; `REASON_REQUIRED`, `STALE_VERSION`, `CONTRIBUTION_NOT_FOUND` |
| `VOL-07.1` | Phân quyền API tự nguyện: chặn ANON, RESIDENT đọc sổ/donor, ADMIN ghi đóng góp, ACCOUNTANT quản lý đợt | ANON, RESIDENT, ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: test tự động |
| `VOL-08.1` | Giao diện `/admin/voluntary`, `/accountant/voluntary` và `/resident/voluntary` theo đúng vai trò | ADMIN, ACCOUNTANT, RESIDENT | ✅ Đạt (Pass) | Bằng chứng: Vitest |
| `VOL-08.2` | Giao diện ACCOUNTANT giữ nguyên `Idempotency-Key` khi mất kết nối/5xx để thử ghi lại và mở khóa khi lỗi 400/409 | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: Vitest |
| `VOL-08.3` | Giao diện đóng góp tự nguyện: mobile viewport 360px, theme sáng/tối, điều hướng keyboard-only với focus nhìn thấy, date input badInput và hiển thị số tiền lớn | ADMIN, ACCOUNTANT, RESIDENT | Bị chặn (Blocked) | Cần trình duyệt thật (360px, theme, focus ring) |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### VOL-01.1 — ADMIN tạo và cập nhật đợt đóng góp tự nguyện khi còn ở trạng thái DRAFT

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns`, `PATCH /api/v1/voluntary-campaigns/701`.
  - **Dữ liệu đầu vào:** Header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body tạo `{"name":"  Quy Khuyen hoc 2026  ","description":"  Ung ho hoc sinh  ","opensOn":"<DAU_THANG_NAY>","closesOn":"<CUOI_THANG_NAY>"}`; body sửa `{"name":"Quy Khuyen hoc dot 1","description":"Cap nhat mo ta","opensOn":"<DAU_THANG_NAY>","closesOn":"<CUOI_THANG_NAY>","version":0}`.
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/voluntary-campaigns` cùng body tạo.
    2. Lấy `id` (giả sử `701`) và gửi `PATCH /api/v1/voluntary-campaigns/701` cùng body sửa.
    3. Đối chiếu HTTP, body JSON và thực hiện kiểm sau.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 201; `name="Quy Khuyen hoc 2026"` (đã cắt khoảng trắng hai đầu), `description="Ung ho hoc sinh"`, `status="DRAFT"`, `version=0`, `publishedAt=null`, `closedAt=null`, `postedCount=0`, `totalCollected="0"`. Bước 2 trả HTTP 200; `name="Quy Khuyen hoc dot 1"`, `version=1`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/voluntary-campaigns/701` xác nhận dữ liệu; kiểm tra `audit_log` có `VOLUNTARY_CAMPAIGN_CREATED` và `VOLUNTARY_CAMPAIGN_UPDATED` với `changes` chỉ chứa `status`, `opensOn`, `closesOn`.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:69,128,129,136,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:56,62`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:41,49`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#campaignLifecycleIsPersistedWithVersionsActorsAndAudit` (MySQL thật: tên và mô tả được cắt khoảng trắng, `DRAFT`, `version=0`, `publishedAt` và `closedAt` null, `postedCount=0`, `totalCollected="0"`; PATCH đưa version 0 → 1 và đổi tên; audit `VOLUNTARY_CAMPAIGN_CREATED` và `VOLUNTARY_CAMPAIGN_UPDATED` mỗi loại đúng 1 dòng với `changes` chỉ gồm `status`, `opensOn`, `closesOn`); `VoluntaryCaseGapTest#createTrimsTextStartsAsDraftAndAuditsOnlyStatusAndDates`; `VoluntaryRoutesSuccessApiTest#postCampaignAsAdminReturns201Created` và `#patchCampaignAsAdminReturns200WithUpdatedStaffDetail` (HTTP 201 và 200).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: HTTP 201/200 kiểm trên lát WebMvc với service giả; phần dữ liệu kiểm bằng gọi service trực tiếp trên MySQL thật.

### VOL-01.2 — Từ chối tạo hoặc sửa đợt khi tên/mô tả sai giới hạn, ngày kết thúc trước ngày bắt đầu hoặc sai version

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns`, `PATCH /api/v1/voluntary-campaigns/701`.
  - **Dữ liệu đầu vào:** Đợt `701` đang ở `DRAFT` (`version=1`); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/voluntary-campaigns` với tên rỗng `"   "`, tên dài 151 ký tự, hoặc mô tả dài 2001 ký tự.
    2. Gửi `POST /api/v1/voluntary-campaigns` với `opensOn="<NGAY_MAI>"`, `closesOn="<HOM_NAY>"` (`closesOn < opensOn`) hoặc ngày sai định dạng `"2026-13-01"`.
    3. Gửi `PATCH /api/v1/voluntary-campaigns/701` với `version=0` (version cũ) và với ID `999999` không tồn tại.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 400, `code=VALIDATION`. Bước 2 trả HTTP 400, `code=INVALID_DATE`. Bước 3 trả HTTP 409 `code=STALE_VERSION` cho `version=0`, và HTTP 404 `code=CAMPAIGN_NOT_FOUND` cho ID `999999`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/voluntary-campaigns/701` xác nhận đợt `701` giữ nguyên thông tin tại `version=1`.
  - **Nguồn đối chiếu:** `_agents_workspace/S08/01_contract.md:140,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:139,146`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseGapTest#nameAndDescriptionLimitsAreEnforcedByCodePoint` (tên 150 và mô tả 2000 ký tự được chấp nhận; tên null, rỗng, khoảng trắng, 151 ký tự và mô tả 2001 ký tự → `VALIDATION`; tính theo code point); `VoluntaryCaseGapTest#closingDateBeforeOpeningDateIsInvalidButSameDayIsAllowed` (`INVALID_DATE`); `VoluntaryErrorMappingApiTest#unparsableOrMissingCampaignDatesAreHttp400InvalidDateBeforeTheService` (`2026-13-01`, `2026-02-30`, chuỗi sai, rỗng, null, năm 0999 → HTTP 400 `INVALID_DATE`, service không bị gọi); `VoluntaryErrorMappingApiTest#campaignValidationAndDateErrorsFromTheServiceAreHttp400`; `VoluntaryErrorMappingApiTest#staleVersionIsHttp409AndUnknownCampaignIsHttp404`; `VoluntaryCaseIT#campaignLifecycleIsPersistedWithVersionsActorsAndAudit` (version cũ → `STALE_VERSION`, ID lạ → `CAMPAIGN_NOT_FOUND`, đợt giữ nguyên ở version 1).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP kiểm với service giả.

### VOL-02.1 — ADMIN công bố đợt (kể cả công bố trước ngày bắt đầu), khóa sửa nháp và đóng đợt sớm

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns/701/publish`, `PATCH /api/v1/voluntary-campaigns/701`, `POST /api/v1/voluntary-campaigns/701/close`.
  - **Dữ liệu đầu vào:** Đợt `701` ở `DRAFT` (`version=1`, `closesOn >= <HOM_NAY>`); đợt `702` ở `DRAFT` có `closesOn="<HOM_QUA>"` (đã hết hạn); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/voluntary-campaigns/702/publish` với `{"version":0}` cho đợt đã hết hạn.
    2. Gửi `POST /api/v1/voluntary-campaigns/701/publish` với `{"version":1}`.
    3. Thử gửi `PATCH /api/v1/voluntary-campaigns/701` với `version=2` hoặc gọi lại `publish` lần hai.
    4. Gửi `POST /api/v1/voluntary-campaigns/701/close` với `{"version":2}`, sau đó thử gọi lại `close` hoặc `publish` trên đợt đã đóng.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 409, `code=INVALID_STATE`. Bước 2 trả HTTP 200; `status="PUBLISHED"`, `version=2`, `publishedAt` và `publishedBy` khác null. Bước 3 trả HTTP 409, `code=INVALID_STATE` (đã công bố không sửa được). Bước 4: lần gọi `close` đầu trả HTTP 200 với `status="CLOSED"`, `closedAt` và `closedBy` khác null; gọi lại `close` hoặc `publish` sau khi đóng trả HTTP 409 `code=INVALID_STATE`.
  - **Kiểm tra sau (Post-check):** Kiểm tra `audit_log` có `VOLUNTARY_CAMPAIGN_PUBLISHED` và `VOLUNTARY_CAMPAIGN_CLOSED`.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26 (P3); `_agents_workspace/S08/01_contract.md:70,92,130,142,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:59,69`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#campaignLifecycleIsPersistedWithVersionsActorsAndAudit` (MySQL thật: công bố đưa version 1 → 2, `published_by` và `published_at` khác null; công bố lại và sửa sau khi công bố → `INVALID_STATE`; đóng đưa version 2 → 3, `closed_by` và `closed_at` khác null; đóng lại hoặc công bố sau khi đóng → `INVALID_STATE`; audit `VOLUNTARY_CAMPAIGN_PUBLISHED` và `VOLUNTARY_CAMPAIGN_CLOSED` mỗi loại đúng 1 dòng); `VoluntaryCaseIT#anExpiredDraftCannotBePublishedAndStaysADraft` (đợt đã hết hạn → `INVALID_STATE`, vẫn DRAFT, không audit); `VoluntaryCaseGapTest#publishThenCloseRecordsActorsAndAuditsAndBlocksFurtherChanges`; `VoluntaryCaseGapTest#publishIsAllowedBeforeOpeningDayAndOnClosingDayButNotAfter` (công bố trước ngày bắt đầu được phép); `VoluntaryErrorMappingApiTest#invalidStateOnPublishCloseAndEditIsHttp409`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP 409 kiểm với service giả.

### VOL-02.2 — Tầm nhìn RESIDENT: chỉ thấy đợt đã công bố với DTO rút gọn, nháp trả 404 và đợt quá hạn hiển thị `CLOSED`

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/voluntary-campaigns?status=&page=0&size=20`, `GET /api/v1/voluntary-campaigns/{id}`.
  - **Dữ liệu đầu vào:** Có 3 đợt: đợt `711` ở `DRAFT` (`publishedAt=null`); đợt `712` đã công bố còn hạn (`status=PUBLISHED`, `closesOn >= <HOM_NAY>`, có `postedCount=2`, `totalCollected="500000"`); đợt `713` đã công bố nhưng `closesOn="<HOM_QUA>"` (chưa bấm đóng tay, DB `status=PUBLISHED`).
  - **Các bước thực hiện:**
    1. Từ phiên RESIDENT, gọi `GET /api/v1/voluntary-campaigns?page=0&size=20` và `GET /api/v1/voluntary-campaigns?status=DRAFT`.
    2. Từ phiên RESIDENT, gọi `GET /api/v1/voluntary-campaigns/711` (nháp), `/712` (đang mở) và `/713` (đã qua ngày kết thúc).
    3. Gọi `GET /api/v1/voluntary-campaigns?status=CLOSED` bằng cả RESIDENT và ADMIN.
  - **Kết quả mong đợi:** Bước 1: danh sách của RESIDENT chỉ chứa `712` và `713`, không bao giờ chứa `711`; khi lọc `status=DRAFT`, RESIDENT nhận `content=[]` (`totalElements=0`). Bước 2: `/711` trả HTTP 404 `code=CAMPAIGN_NOT_FOUND`; `/712` và `/713` trả HTTP 200 với đúng 6 trường `id`, `name`, `description`, `opensOn`, `closesOn`, `status` (không có `version`, `createdBy`, `publishedAt`, `postedCount`, `totalCollected`). Đợt `713` có `status="CLOSED"`. Bước 3: lọc `status=CLOSED` trả về đợt `713` (và các đợt đóng tay).
  - **Kiểm tra sau (Post-check):** Gọi `/api/v1/voluntary-campaigns/712` bằng ADMIN xác nhận staff DTO vẫn có đủ `postedCount=2` và `totalCollected="500000"`.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:30,72,73,127,136,209`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:78,90,113,123`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/VoluntaryCampaignRepository.java:14`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#residentsOnlySeePublishedCampaignsAndEndedOnesShowAsClosed` (MySQL thật: danh sách cư dân có đợt đang mở và đợt quá hạn nhưng không có nháp; đợt quá hạn hiển thị `CLOSED`; lọc `DRAFT` trả 0 phần tử; lọc `CLOSED` và `PUBLISHED` tách đúng; chi tiết nháp → `CAMPAIGN_NOT_FOUND`; danh sách nhân sự thấy đủ cả ba); `VoluntaryCaseGapTest#residentDtoHasExactlySixFieldsAndAutoClosedCampaignShowsClosed` (DTO cư dân đúng 6 trường); `VoluntaryErrorMappingApiTest#residentDetailHasExactlySixFieldsAndDraftIsHttp404` và `VoluntaryErrorMappingApiTest#residentListDtoHasExactlySixFieldsPerItem` (JSON đúng 6 khóa, nháp HTTP 404); `VoluntaryRoutesSuccessApiTest#getCampaignsAsAdminReturnsStaffPageWithTotals` (DTO nhân sự có `postedCount` và `totalCollected`).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### VOL-03.1 — ACCOUNTANT tìm người đóng qua `/voluntary-donors` gồm cả nhân khẩu MOVED_OUT/hộ INACTIVE và ẩn PII

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/voluntary-donors?q=<TU_KHOA>&page=0&size=20`.
  - **Dữ liệu đầu vào:** Nhân khẩu `101` (`RESIDING`, hộ `ACTIVE`), nhân khẩu `102` (`TEMPORARILY_ABSENT`, hộ `ACTIVE`), nhân khẩu `103` (`MOVED_OUT`, hộ `INACTIVE`) đều có tên chứa `"Nguyen"` và có `identityNumber`, `phone` trong DB.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/voluntary-donors?q=Nguyen&page=0&size=20`.
    2. Gọi `GET /api/v1/voluntary-donors?q=N` (1 ký tự), `?q=%` (1 ký tự wildcard) và `?q=   ` (rỗng).
    3. Đối chiếu các trường trong response.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 200; `content` gồm cả `101`, `102` và `103`; mỗi phần tử chỉ có 4 trường `id`, `fullName`, `householdCode`, `apartmentCode` (tuyệt đối không có `identityNumber`, `phone`, `dateOfBirth`, `email`). Bước 2 trả HTTP 400, `code=INVALID_FILTER`.
  - **Kiểm tra sau (Post-check):** Xác nhận ký tự `%` và `_` trong `q` được escape, không lộ toàn bộ danh sách khi nhập `%%` nếu không có tên chứa `%%`.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26 (P5); `_agents_workspace/S08/01_contract.md:30,77,94,131`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:42,80`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentDonorLookupService.java:26`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#donorSearchFindsEveryResidencyStatusAndTreatsWildcardsAsLiterals` (MySQL thật: tìm thấy đúng nhân khẩu RESIDING, TEMPORARILY_ABSENT và MOVED_OUT thuộc hộ INACTIVE; `%`, `_`, `!!` và `%%` được escape nên không khớp tràn; `q` một ký tự, `%`, khoảng trắng, rỗng, null → `INVALID_FILTER`); `VoluntaryErrorMappingApiTest#donorSearchReturnsExactlyFourFieldsAndShortOrWildcardQueriesAreHttp400` (JSON đúng 4 khóa `id`, `fullName`, `householdCode`, `apartmentCode`; HTTP 400 `INVALID_FILTER`); `VoluntaryRoutesSuccessApiTest#getDonorsAsAccountantReturnsWhitelistedDonorInfo` (không có `nationalId`, `phone`, `email`).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP kiểm với service giả; đối chiếu PII dựa trên việc DTO chỉ có 4 trường.

### VOL-04.1 — Ghi nhận khoản đóng góp hợp lệ (bao gồm nhân khẩu MOVED_OUT/hộ INACTIVE và đóng nhiều lần với key mới)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns/712/contributions`.
  - **Dữ liệu đầu vào:** Đợt `712` đang mở (`PUBLISHED`, `opensOn <= <HOM_NAY> <= closesOn`, `publishedAt` trong ngày hoặc trước `<HOM_NAY>`); nhân khẩu `103` (`MOVED_OUT`, thuộc hộ `INACTIVE`); hai UUID hợp lệ `<UUID_1>` và `<UUID_2>`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/voluntary-campaigns/712/contributions` với header `Idempotency-Key: <UUID_1>` và body `{"residentId":103,"amount":"200000","paidOn":"<HOM_NAY>","lateReason":null}`.
    2. Gửi tiếp lần đóng thứ hai cho cùng nhân khẩu `103`, cùng ngày và số tiền nhưng với header `Idempotency-Key: <UUID_2>`.
    3. Sau đó ADMIN đổi tên nhân khẩu `103` qua `PATCH /api/v1/residents/103`; gọi `GET /api/v1/voluntary-campaigns/712/contributions` và `GET /api/v1/voluntary-campaigns/712`.
  - **Kết quả mong đợi:** Bước 1 và bước 2 đều trả HTTP 201; mỗi bản ghi có `status="POSTED"`, `amount="200000"`, `householdId` do server suy ra từ nhân khẩu `103`, kèm `donorNameSnapshot`, `householdCodeSnapshot`, `apartmentCodeSnapshot`. Bước 3: `donorNameSnapshot` của hai khoản đã ghi giữ nguyên tên lúc ghi nhận; đợt `712` có `postedCount` tăng 2 và `totalCollected` tăng `"400000"`; không làm thay đổi `invoices` hay `receiptCount` của bất kỳ kỳ thu bắt buộc nào.
  - **Kiểm tra sau (Post-check):** Kiểm tra `audit_log` có 2 dòng `VOLUNTARY_CONTRIBUTION_RECORDED` với `changes` chỉ chứa `campaignId`, `amount`, `paidOn`, `status` (không chứa tên người đóng hay `lateReason`).
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:77,78,80,82,133,136,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:53,64`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#twoContributionsKeepSnapshotsAddUpAndNeverTouchBillingOrLeakIntoAudit` (MySQL thật: hai khoản cho nhân khẩu MOVED_OUT thuộc hộ INACTIVE, hai khóa khác nhau, đều `POSTED`, `householdId` do server suy ra; đổi tên nhân khẩu sau đó nhưng sổ vẫn giữ tên lúc ghi; `postedCount` tăng 2 và `totalCollected="400000"`; số hóa đơn và tổng `receipt_count` không đổi; đúng 2 audit `VOLUNTARY_CONTRIBUTION_RECORDED` với `changes` chỉ gồm `campaignId`, `amount`, `paidOn`, `status`, không chứa tên người đóng); `VoluntaryCaseGapTest#openCampaignAcceptsTodayWithoutALateReason`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### VOL-04.2 — Quy tắc cửa sổ ngày thực thu (`paidOn`) và bắt buộc `lateReason` khi nhập muộn sau đóng hoặc sau hạn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns/{id}/contributions`.
  - **Dữ liệu đầu vào:** Nhân khẩu `101` tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`, mỗi request thử mới dùng một `Idempotency-Key` UUID mới:
    - Đợt `720` còn ở `DRAFT`.
    - Đợt `721` được công bố hôm nay (`publishedOn = <HOM_NAY>`) nhưng lịch `opensOn = <NGAY_MAI>`, hoặc lịch `opensOn = <DAU_THANG_NAY>` nhưng `publishedOn = <HOM_NAY>`.
    - Đợt `722` đã qua ngày kết thúc (`opensOn = <DAU_THANG_NAY>`, `publishedOn = <DAU_THANG_NAY>`, `closesOn = <HOM_QUA>`) hoặc đã được ADMIN đóng sớm vào `<HOM_QUA>`.
  - **Các bước thực hiện:**
    1. Gửi đóng góp vào đợt `720` (`DRAFT`).
    2. Trên đợt đang mở, gửi `paidOn="<NGAY_MAI>"` (ngày tương lai) và gửi `paidOn` trước `max(opensOn, publishedOn)`.
    3. Trên đợt `722` (đã qua `closesOn` hoặc đã đóng sớm ngày `<HOM_QUA>`):
       - Lần 3a: gửi `paidOn="<HOM_NAY>"` (sau ngày kết thúc/ngày đóng).
       - Lần 3b: gửi `paidOn="<HOM_QUA>"` (nằm trong cửa sổ thực thu hợp lệ, bao gồm trọn ngày đóng sớm) nhưng `lateReason=null` hoặc `"   "`.
       - Lần 3c: gửi `paidOn="<HOM_QUA>"` kèm `lateReason="Thu tien mat cuoi ngay hom qua"`.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 409, `code=CAMPAIGN_NOT_RECEIVABLE`. Bước 2 và lần 3a trả HTTP 409, `code=INVALID_PAID_DATE`. Lần 3b trả HTTP 400, `code=REASON_REQUIRED`. Lần 3c trả HTTP 201, `status="POSTED"`, `lateReason="Thu tien mat cuoi ngay hom qua"`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/voluntary-campaigns/722/contributions` xác nhận chỉ có duy nhất khoản từ lần 3c được lưu.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26 (P3); `_agents_workspace/S08/01_contract.md:71,72,78,79,92,142,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryReceiptWindow.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:114`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseGapTest#draftCampaignIsNotReceivableAndNothingIsLookedUpOrWritten` (`CAMPAIGN_NOT_RECEIVABLE`); `VoluntaryCaseGapTest#futureAndBeforeWindowPaidDatesAreInvalidPaidDate` (ngày tương lai, ngày trước ngày công bố và trước ngày mở → `INVALID_PAID_DATE`); `VoluntaryCaseGapTest#lateEntryAfterScheduledEndNeedsReasonAndStoresItWithoutAuditingIt` (lần 3a `INVALID_PAID_DATE`, lần 3b lý do null hoặc rỗng → `REASON_REQUIRED` và chưa ghi gì, lần 3c có lý do → `POSTED`, `lateReason` được lưu nhưng không vào audit); `VoluntaryCaseGapTest#manualEarlyCloseBoundsTheWindowAndStillRequiresAReasonOnTheClosingDay`; `VoluntaryCaseIT#replayConflictCloseAndVoidSequenceNeverCreatesASecondRecordOrAudit` (đợt đã đóng tay: ngày hôm nay chỉ ghi được kèm lý do); `VoluntaryErrorMappingApiTest#windowAndIdempotencyErrorsFromTheServiceKeepTheirStatusAndCode` (HTTP 409/400).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP kiểm với service giả.

### VOL-04.3 — Từ chối khoản đóng góp khi số tiền, khóa Idempotency-Key, nhân khẩu hoặc độ dài lý do không hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns/712/contributions`.
  - **Dữ liệu đầu vào:** Đợt `712` đang mở; nhân khẩu `101` tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi body với `amount` lần lượt là: số JSON `100000` (không phải chuỗi), `"0"`, `"-500"`, `"100.50"`, `"1000000000000000000"` (19 chữ số).
    2. Gửi request thiếu header `Idempotency-Key` hoặc `Idempotency-Key: not-a-uuid`.
    3. Gửi request với `residentId=null`, với `lateReason` dài 256 ký tự, và với `residentId=999999` (không tồn tại).
  - **Kết quả mong đợi:** Bước 1 trả HTTP 400, `code=INVALID_AMOUNT`. Bước 2 trả HTTP 400, `code=INVALID_IDEMPOTENCY_KEY`. Bước 3: `residentId=null` trả HTTP 400 `code=VALIDATION`; `lateReason` 256 ký tự trả HTTP 400 `code=REASON_REQUIRED`; `residentId=999999` trả HTTP 404 `code=RESIDENT_NOT_FOUND`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/voluntary-campaigns/712/contributions` xác nhận không phát sinh khoản đóng góp nào từ các request lỗi.
  - **Nguồn đối chiếu:** `_agents_workspace/S08/01_contract.md:78,133,140,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:93`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ContributionInput.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryErrorMappingApiTest#invalidAmountsAreHttp400InvalidAmountBeforeTheService` (số JSON `100000`, `"0"`, `"-500"`, `"100.50"`, 19 chữ số, rỗng, null, `"1e3"` → HTTP 400 `INVALID_AMOUNT`, service không bị gọi); `VoluntaryErrorMappingApiTest#missingOrMalformedIdempotencyKeyIsHttp400` (thiếu, `not-a-uuid`, `1-1-1-1-1`, UUID không dấu gạch, khoảng trắng → `INVALID_IDEMPOTENCY_KEY`); `VoluntaryErrorMappingApiTest#nullResidentAndOverlongLateReasonAreHttp400AndUnknownResidentIsHttp404` (`residentId` null hoặc thiếu → `VALIDATION`, lý do 256 ký tự → `REASON_REQUIRED`, ngày sai → `INVALID_DATE`, `residentId=999999` → HTTP 404 `RESIDENT_NOT_FOUND`); `VoluntaryCaseGapTest#unknownResidentIsNotFoundAndNothingIsWritten` (không ghi, không audit).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP kiểm với service giả; các yêu cầu lỗi không tới được service nên không phát sinh khoản đóng góp.

### VOL-05.1 — Idempotency replay (HTTP 200), xung đột khác payload (`IDEMPOTENCY_CONFLICT`) và replay sau khi đợt đóng hoặc bản ghi đã hủy

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns/712/contributions`, `POST /api/v1/voluntary-campaigns/712/close`, `POST /api/v1/voluntary-contributions/{id}/void`.
  - **Dữ liệu đầu vào:** Đợt `712` đang mở; nhân khẩu `101`; header `Idempotency-Key: 11111111-2222-3333-4444-555555555555` (thử gửi lại với chữ hoa `11111111-2222-3333-4444-555555555555` / chữ thường); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/voluntary-campaigns/712/contributions` với key trên và body `{"residentId":101,"amount":"150000","paidOn":"<HOM_NAY>","lateReason":null}`; lưu `id` bản ghi (giả sử `801`).
    2. Gửi lại đúng request trên (cùng actor, cùng key, cùng payload).
    3. Gửi lại cùng key trên nhưng đổi `amount` thành `"250000"` (hoặc đổi `residentId`, `paidOn`, `campaignId`).
    4. ADMIN đóng đợt `712`; ACCOUNTANT gửi lại đúng request gốc của bước 1 (không có `lateReason`).
    5. ACCOUNTANT hủy bản ghi `801` qua `POST /api/v1/voluntary-contributions/801/void`; sau đó gửi lại đúng request gốc của bước 1.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 201 (`id=801`, `status="POSTED"`). Bước 2 trả HTTP 200 với cùng `id=801`, không tăng `postedCount` hay `totalCollected`. Bước 3 trả HTTP 409, `code=IDEMPOTENCY_CONFLICT`. Bước 4 trả HTTP 200 với bản ghi `801` (replay kiểm tra trước điều kiện cửa sổ đợt). Bước 5: sau khi `801` đã `VOIDED`, replay cùng key vẫn trả HTTP 200 với bản ghi `801` đang có `status="VOIDED"`, không tạo bản ghi mới.
  - **Kiểm tra sau (Post-check):** Kiểm tra `audit_log` chỉ có đúng 1 dòng `VOLUNTARY_CONTRIBUTION_RECORDED` và 1 dòng `VOLUNTARY_CONTRIBUTION_VOIDED` cho `801`.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:80,81,133,142,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:98`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:53,107`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#replayConflictCloseAndVoidSequenceNeverCreatesASecondRecordOrAudit` (MySQL thật: lần đầu tạo mới, gửi lại cùng khóa (cả chữ hoa) trả bản ghi cũ và `postedCount` không tăng; đổi số tiền, nhân khẩu hoặc lý do → `IDEMPOTENCY_CONFLICT`; đóng đợt rồi gửi lại vẫn là replay; hủy rồi gửi lại trả bản ghi `VOIDED`; chỉ 1 bản ghi, đúng 1 audit `VOLUNTARY_CONTRIBUTION_RECORDED` và 1 audit `VOLUNTARY_CONTRIBUTION_VOIDED`); `VoluntaryCaseGapTest#changingAnyPayloadFieldWithTheSameKeyIsAnIdempotencyConflict` (đổi số tiền, nhân khẩu, ngày, đợt hoặc lý do đều xung đột); `VoluntaryCaseGapTest#uppercaseAndLowercaseKeysAreTheSameKeyWithTheSameFingerprint`; `VoluntaryCaseGapTest#keyIsScopedToTheActorSoAnotherActorWithTheSameKeyIsNotAReplay`; `VoluntaryApiTest#contributionAmountStaysExactAndRetryReturns200` (HTTP 201 rồi 200); `VoluntaryErrorMappingApiTest#windowAndIdempotencyErrorsFromTheServiceKeepTheirStatusAndCode` (HTTP 409).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Mã HTTP 200/201 kiểm trên lát WebMvc với service giả; trên DB thật kiểm cờ `replay`.

### VOL-05.2 — Race condition: Hai request ghi nhận đóng góp đồng thời cùng actor, cùng Idempotency-Key và cùng payload chỉ tạo một bản ghi và một audit

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-campaigns/{id}/contributions`.
  - **Dữ liệu đầu vào:** Đợt `712` đang mở (`PUBLISHED`, còn hạn thu); nhân khẩu `101`; header `Idempotency-Key: 33333333-4444-5555-6666-777777777777` cố định; header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"amount":"500000","paidOn":"<HOM_NAY>","lateReason":null}`. Hai request gửi đồng thời từ cùng tài khoản ACCOUNTANT qua test harness có rào cản đồng bộ (`CountDownLatch` / pool 2 worker hoặc curl concurrent).
  - **Các bước thực hiện:**
    1. Xác nhận đợt `712` đang mở, ghi nhận giá trị `postedCount` và `totalCollected` ban đầu.
    2. Kích hoạt 2 luồng đồng thời gửi `POST /api/v1/voluntary-campaigns/712/contributions` với cùng actor, cùng header `Idempotency-Key` và cùng body trên qua điểm chặn barrier.
    3. Đợi cả hai luồng hoàn tất, thu nhận HTTP status code, body JSON và thuộc tính bản ghi của từng luồng.
    4. Kiểm tra dữ liệu trong bảng `voluntary_contributions`, `audit_log` và các chỉ số tổng của đợt trong `voluntary_campaigns`.
  - **Kết quả mong đợi:** Cả hai request đều thành công và trả về cùng thông tin khoản đóng góp (cùng `id`, `amount="500000"`, `paidOn="<HOM_NAY>"`). Trong đó: đúng một request tạo mới trả về HTTP 201; request còn lại được xử lý replay qua ràng buộc duy nhất trên `(recorded_by, idempotency_key)` và trả về HTTP 200 (idempotent replay). Số lượng khoản đóng góp tăng đúng 1 (`postedCount` tăng 1) và tổng thu tăng đúng 1 lần (`totalCollected` tăng `500.000 đ`), không bị tính trùng hai lần. Chỉ có duy nhất đúng 1 bản ghi audit log được sinh ra.
  - **Kiểm tra sau (Post-check):**
    - Truy vấn DB: `SELECT COUNT(*) FROM voluntary_contributions WHERE campaign_id=712 AND resident_id=101 AND idempotency_key='33333333-4444-5555-6666-777777777777'` trả về đúng `1`.
    - Truy vấn `audit_log`: `SELECT COUNT(*) FROM audit_log WHERE action='VOLUNTARY_CONTRIBUTION_RECORDED' AND entity_type='VoluntaryContribution' AND entity_id='<ID_KHOAN_DONG>'` trả về đúng `1`.
    - Kiểm tra đợt: `totalCollected` tăng đúng `500000`, `postedCount` tăng đúng `1`.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:14,80,133,156,160,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:53`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/VoluntaryContributionIT.java:116`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#concurrentSameKeyAddsTheAmountToTheCampaignTotalExactlyOnce` (MySQL thật, hai luồng cùng khóa và cùng payload: cùng `id`, đúng một luồng tạo mới và một luồng replay, `postedCount=1`, `totalCollected="500000"`, đúng 1 audit); `VoluntaryContributionIT#concurrentSameKeyCreatesOneRecordAndOneAudit` (đúng 1 dòng trong `voluntary_contributions` và 1 audit); `VoluntaryContributionServiceTest#concurrentReplayIsCheckedAgainAfterCampaignLockBeforeDateValidation`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Gọi service trực tiếp bằng hai luồng, không qua HTTP; cặp HTTP 201/200 được suy ra từ cờ `replay`.

### VOL-06.1 — ACCOUNTANT hủy khoản đóng góp (`void`), kiểm tra tính bất biến lịch sử, idempotent khi hủy lặp và cập nhật tổng thu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/voluntary-contributions/802/void`, `GET /api/v1/voluntary-campaigns/712/contributions?status=&page=0&size=20`, `GET /api/v1/voluntary-campaigns/712`.
  - **Dữ liệu đầu vào:** Đợt `712` có hai khoản `POSTED`: khoản `802` (`amount="300000"`, `version=0`) và khoản `803` (`amount="100000"`, `version=0`); tổng đợt hiện là `postedCount=2`, `totalCollected="400000"`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/voluntary-contributions/802/void` với `{"version":0,"reason":"   "}` (rỗng) và với `{"version":9,"reason":"Nhap nham"}` (sai version).
    2. Gửi `POST /api/v1/voluntary-contributions/802/void` với `{"version":0,"reason":"  Nhap nham so tien  "}`.
    3. Gửi lặp lại `POST /api/v1/voluntary-contributions/802/void` với `{"version":0,"reason":"Ly do khac"}` trên khoản đã `VOIDED`.
    4. Gọi `GET /api/v1/voluntary-campaigns/712`, `GET /api/v1/voluntary-campaigns/712/contributions?status=POSTED` và `?status=VOIDED`.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 400 `code=REASON_REQUIRED` cho lý do rỗng, và HTTP 409 `code=STALE_VERSION` cho sai version (hoặc 404 `code=CONTRIBUTION_NOT_FOUND` nếu ID không tồn tại). Bước 2 trả HTTP 200; `status="VOIDED"`, `voidReason="Nhap nham so tien"`, `voidedAt` và `voidedBy` khác null. Bước 3 trả HTTP 200 giữ nguyên bản ghi đã `VOIDED`, không sinh thêm audit. Bước 4: đợt `712` có `postedCount=1`, `totalCollected="100000"`; lọc `status=POSTED` chỉ có `803`, lọc `status=VOIDED` có `802` (vẫn giữ lịch sử).
  - **Kiểm tra sau (Post-check):** Kiểm tra `audit_log` chỉ có 1 dòng `VOLUNTARY_CONTRIBUTION_VOIDED` cho `802` và `changes` không chứa `voidReason`.
  - **Nguồn đối chiếu:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:82,132,134,142,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:84,99`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryCaseIT#voidKeepsHistoryUpdatesTotalsAndIsIdempotent` (MySQL thật: lý do rỗng → `REASON_REQUIRED`, version 9 → `STALE_VERSION`, ID lạ → `CONTRIBUTION_NOT_FOUND`; hủy có lý do được cắt khoảng trắng, `voidedBy` và `voidedAt` khác null; hủy lặp giữ nguyên lý do cũ; đợt còn `postedCount=1` và `totalCollected="100000"`; lọc `POSTED` chỉ có khoản còn lại, lọc `VOIDED` có khoản đã hủy; đúng 1 audit `VOLUNTARY_CONTRIBUTION_VOIDED` với `changes` chỉ gồm `campaignId`, `amount`, `status`, không chứa lý do); `VoluntaryCaseGapTest#voidTrimsReasonRejectsStaleVersionAndRepeatedVoidIsIdempotentWithoutSecondAudit`; `VoluntaryCaseGapTest#voidRejectsMissingOrOverlongReasonBeforeTouchingAnyData`; `VoluntaryErrorMappingApiTest#voidErrorsKeepTheirStatusAndCode`; `VoluntaryRoutesSuccessApiTest#voidContributionAsAccountantReturns200WithVoidReason`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Ánh xạ HTTP kiểm với service giả.

### VOL-07.1 — Phân quyền API tự nguyện: chặn ANON, RESIDENT đọc sổ/donor, ADMIN ghi đóng góp, ACCOUNTANT quản lý đợt

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON, RESIDENT, ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** Các endpoint `/api/v1/voluntary-campaigns/**`, `/api/v1/voluntary-donors`, `/api/v1/voluntary-contributions/*/void`.
  - **Dữ liệu đầu vào:** Đợt `712` và khoản đóng góp `803` tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Từ phiên ANON, gọi `GET /api/v1/voluntary-campaigns` và `POST /api/v1/voluntary-campaigns`.
    2. Từ phiên RESIDENT, gọi `GET /api/v1/voluntary-campaigns/712/contributions`, `GET /api/v1/voluntary-donors?q=Ng`, `POST /api/v1/voluntary-campaigns/712/contributions`, `POST /api/v1/voluntary-contributions/803/void` và `POST /api/v1/voluntary-campaigns`.
    3. Từ phiên ADMIN, gọi `GET /api/v1/voluntary-donors?q=Ng`, `POST /api/v1/voluntary-campaigns/712/contributions` và `POST /api/v1/voluntary-contributions/803/void`.
    4. Từ phiên ACCOUNTANT, gọi `POST /api/v1/voluntary-campaigns`, `PATCH /api/v1/voluntary-campaigns/712`, `POST /api/v1/voluntary-campaigns/712/publish` và `POST /api/v1/voluntary-campaigns/712/close`.
  - **Kết quả mong đợi:** Bước 1 trả HTTP 401. Bước 2, 3 và 4 trả HTTP 403 cho mọi request vượt quyền.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/voluntary-campaigns/712` bằng ADMIN xác nhận trạng thái đợt và tổng tiền không đổi.
  - **Nguồn đối chiếu:** `_agents_workspace/S08/01_contract.md:20,30,125,209`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:71`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `VoluntaryErrorMappingApiTest#roleMatrixMatchesTheSpecificationForEveryRouteThatTheCaseListsAndNothingLeaksThroughToTheServices` (ANON → 401 cho đọc và tạo đợt; RESIDENT, ADMIN và ACCOUNTANT đều bị 403 ở đúng các route vượt quyền mà ca liệt kê; không service nào được gọi); `VoluntaryErrorMappingApiTest#allowedRolesReachTheServiceOnTheReadRoutes` (đối chứng: các vai trò hợp lệ đọc được); `VoluntaryErrorMappingApiTest#writesWithoutCsrfOrWithPasswordChangePendingAreRejectedBeforeTheServices` (thiếu CSRF → 403, `PASSWORD_CHANGE_REQUIRED`); `VoluntaryApiTest#everyVoluntaryRouteRejectsAnonymousAndWrongRole` và `#enforcesAuthenticationRolesCsrfAndPasswordChange`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Kiểm trên lát WebMvc với `SecurityConfig` thật nhưng service và tài khoản là đối tượng giả.

### VOL-08.1 — Giao diện `/admin/voluntary`, `/accountant/voluntary` và `/resident/voluntary` theo đúng vai trò

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, ACCOUNTANT, RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/voluntary`, `/accountant/voluntary`, `/resident/voluntary`.
  - **Dữ liệu đầu vào:** Chế độ `api`; có nhân khẩu `"Người thử"` trong hệ thống.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN, mở `/admin/voluntary`, bấm `Tạo đợt đóng góp`, nhập tên, mô tả, ngày bắt đầu/kết thúc hợp lệ rồi bấm `Lưu`; chọn `Xem chi tiết` đợt vừa tạo, bấm `Công bố` và xác nhận.
    2. Kiểm tra trên màn chi tiết của ADMIN có hiển thị `Tổng đã ghi`, `Sổ đóng góp`, nút `Đóng đợt`, nhưng không có biểu mẫu `Ghi đóng góp` hay nút `Hủy khoản đóng góp`.
    3. Đăng nhập ACCOUNTANT, mở `/accountant/voluntary`, bấm `Xem chi tiết` đợt vừa công bố; tìm `"Người"` tại ô `Tìm người đóng`, chọn người đóng, nhập số tiền `"200000"`, chọn `Ngày nhận tiền` và bấm `Ghi đóng góp`.
    4. Đăng nhập RESIDENT, mở `/resident/voluntary` và kiểm tra danh sách đợt.
  - **Kết quả mong đợi:** ADMIN tạo và công bố đợt thành công; ACCOUNTANT ghi nhận khoản đóng góp và có nút `Hủy khoản đóng góp` trên dòng `Đã ghi`. RESIDENT tại `/resident/voluntary` chỉ thấy tên, trạng thái (`Sắp bắt đầu` / `Đang mở` / `Đã kết thúc`), ngày bắt đầu – kết thúc và mô tả của đợt đã công bố; không có nút `Xem chi tiết`, không hiển thị tổng tiền thu hay danh sách người đóng, và trình duyệt không gọi API `/contributions`.
  - **Kiểm tra sau (Post-check):** Mở `/admin/voluntary` bằng ADMIN xác nhận tổng đã ghi tăng lên `200.000 đ` và sổ có khoản vừa nhập.
  - **Nguồn đối chiếu:** `_agents_workspace/S08/01_contract.md:172,173,174`; `frontend/src/App.tsx:128,139,150`; `frontend/src/screens/s08/ApiVoluntaryPage.tsx:16,36`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `s08-voluntary-cases.test.tsx` (Vitest, API client giả): ADMIN lưu nháp rồi công bố chỉ sau khi xác nhận hộp thoại (`campaignAction(…, 'publish')`), chi tiết đợt có `Sổ đóng góp` và `Đóng đợt` nhưng không có biểu mẫu ghi đóng góp, không có nút hủy khoản đóng góp và không có cột thao tác; ACCOUNTANT ghi đóng góp (gọi API với `residentId`, `amount`, `paidOn` và khóa dạng UUID), nút hủy chỉ có trên dòng `Đã ghi`; RESIDENT thấy `Sắp bắt đầu`, `Đang mở`, `Đã kết thúc` kèm tên, mô tả và ngày, không có `Xem chi tiết`, không có tổng tiền và không gọi API đóng góp hay tìm người đóng. Test hiện có: `s08-voluntary-ux.test.tsx` và `s08-screens.test.tsx`.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Kiểm bằng jsdom với `api/voluntary` giả, chưa chạy trình duyệt thật với backend thật. Nhãn trong đặc tả lệch giao diện: `Tạo đợt đóng góp` (giao diện: `Tạo đợt vận động mới`), `Lưu` (`Lưu nháp`), `Tổng đã ghi` (`Tổng thu đợt này` và `Đã ghi nhận N lượt đóng góp`). Hành vi khớp đặc tả.

### VOL-08.2 — Giao diện ACCOUNTANT giữ nguyên `Idempotency-Key` khi mất kết nối/5xx để thử ghi lại và mở khóa khi lỗi 400/409

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/accountant/voluntary`.
  - **Dữ liệu đầu vào:** Chế độ `api`; đợt `712` đang mở; đã tìm và chọn người đóng.
  - **Các bước thực hiện:**
    1. Nhập số tiền `"100000"`, ngày nhận tiền hợp lệ; dùng DevTools Network chặn mạng (Offline) hoặc mô phỏng lỗi 503 cho request `POST /api/v1/voluntary-campaigns/712/contributions`, rồi bấm `Ghi đóng góp`.
    2. Quan sát trạng thái khóa các ô nhập và thông báo `Chưa xác nhận được kết quả. Thử lại cùng nội dung để tránh ghi trùng.`; bật lại mạng rồi bấm `Thử ghi lại`.
    3. Thực hiện một lần nhập mới với `Ngày nhận tiền` không hợp lệ (ví dụ ngày tương lai) để server trả HTTP 409 `code=INVALID_PAID_DATE`.
  - **Kết quả mong đợi:** Ở bước 1–2, các ô nhập bị khóa (`disabled`) và nút đổi thành `Thử ghi lại`; khi bấm `Thử ghi lại`, trình duyệt gửi lại đúng `Idempotency-Key` và payload ban đầu (kể cả khi điều hướng sang màn khác rồi quay lại trong cùng tài khoản, nhưng không lộ sang tài khoản khác). Ở bước 3, khi nhận lỗi 400 hoặc 409 (`INVALID_PAID_DATE`, `CAMPAIGN_NOT_RECEIVABLE`), giao diện mở khóa lại các ô nhập và đổi `Idempotency-Key` mới để người dùng sửa ngày.
  - **Kiểm tra sau (Post-check):** Kiểm tra tab Network xác nhận header `Idempotency-Key` ở bước 1 và bước 2 giống hệt nhau.
  - **Nguồn đối chiếu:** `_agents_workspace/S08/01_contract.md:80,173,176`; `frontend/src/screens/s08/ApiVoluntaryPage.tsx:34,42`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công; xác nhận bằng test tự động đã chạy xanh: `s08-voluntary-cases.test.tsx` (Vitest): sau lỗi mạng hoặc HTTP 503 các ô nhập bị khóa, nút đổi thành `Thử ghi lại`, thông báo `Chưa xác nhận được kết quả. Thử lại cùng nội dung để tránh ghi trùng.` hiển thị, và lần thử lại gửi đúng khóa và payload cũ; sau lỗi 400, 404 `RESIDENT_NOT_FOUND`, 409 `INVALID_PAID_DATE` hoặc `CAMPAIGN_NOT_RECEIVABLE` các ô nhập được mở khóa và lần gửi sau dùng khóa UUID mới; `s08-screens.test.tsx` (khóa chưa xác nhận được giữ qua điều hướng và không lộ sang tài khoản khác); `s08-csrf.test.ts` (khóa được gửi trong header `Idempotency-Key` cùng CSRF).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Kiểm bằng jsdom với `api/voluntary` giả; chưa thao tác DevTools Network trên trình duyệt thật.

### VOL-08.3 — Giao diện đóng góp tự nguyện: mobile viewport 360px, theme sáng/tối, điều hướng keyboard-only với focus nhìn thấy, date input badInput và hiển thị số tiền lớn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, ACCOUNTANT, RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/admin/voluntary`, `/accountant/voluntary`, `/resident/voluntary`.
  - **Dữ liệu đầu vào:** Chế độ `api`; đợt đóng góp có tổng thu lớn (`999999999999999999` VND - 18 chữ số); ngày nhận tiền hoặc ngày đợt sai định dạng; mô phỏng viewport 360px × 640px.
    *(Lưu ý: Luồng phân quyền hiển thị, giữ `Idempotency-Key` khi mất mạng để retry và mở khóa key khi gặp 400/409 đã được kiểm thử tại [VOL-08.1](#vol-081--giao-diện-adminvoluntary-accountantvoluntary-và-residentvoluntary-theo-đúng-vai-trò) và [VOL-08.2](#vol-082--giao-diện-accountant-giữ-nguyên-idempotency-key-khi-mất-kết-nối5xx-để-thử-ghi-lại-và-mở-khóa-khi-lỗi-400409); ca này tập trung kiểm thử trải nghiệm người dùng, tính tiếp cận và độ chính xác hiển thị tiền lớn theo hợp đồng mục 11).*
  - **Các bước thực hiện:**
    1. **Kiểm tra Viewport 360px:** Mở DevTools ở kích thước màn hình `360px × 640px`:
       - Mở `/admin/voluntary`: form tạo/sửa đợt, danh sách đợt và sổ đóng góp tự căn chỉnh vừa vặn màn hình dọc mà không tràn chiều ngang.
       - Mở `/accountant/voluntary`: form tìm người đóng, select chọn người, ô nhập tiền, ngày nhận tiền và nút `Ghi đóng góp` hiển thị rõ ràng, không đè lấn nhau.
       - Mở `/resident/voluntary`: các thẻ đợt đóng góp hiển thị thông tin tên, trạng thái và ngày mở/đóng ngay ngắn, chữ không bị cắt cụt.
    2. **Kiểm tra Theme sáng/tối:**
       - Chuyển đổi giữa Light mode và Dark mode qua nút `ThemeToggle` hoặc thiết lập hệ thống.
       - Kiểm tra các thành phần giao diện: màu nền bề mặt (`surface-box`), màu badge trạng thái (`Sắp bắt đầu`, `Đang mở`, `Đã kết thúc`, `Đã ghi`, `Đã hủy`), màu chữ và viền hộp thông báo cảnh báo.
       - Đảm bảo độ tương phản đáp ứng chuẩn trực quan WCAG AA, không bị lóa hoặc chìm màu chữ.
    3. **Kiểm tra Điều hướng bàn phím (Keyboard-only):**
       - Dùng phím `Tab` và `Shift+Tab` duyệt tuần tự qua tất cả các trường và nút tương tác trên màn hình `/admin/voluntary` và `/accountant/voluntary`.
       - Đảm bảo đường viền focus (`:focus-visible` outline) hiển thị nổi bật trên phần tử đang chọn.
       - Mở hộp thoại xác nhận (ví dụ `Công bố`, `Đóng đợt`, hoặc `Hủy khoản đóng góp`): tiêu điểm nhảy vào hộp thoại, có thể dùng phím `Tab` để di chuyển giữa ô nhập lý do hủy và nút `Xác nhận`/`Hủy`, dùng `Esc` để đóng dialog an toàn.
    4. **Kiểm tra Date input ở trạng thái `badInput`:**
       - Tại `/admin/voluntary`, gõ giá trị ngày sai định dạng vào ô `Ngày bắt đầu` hoặc `Ngày kết thúc`. Bấm `Lưu`.
       - Tại `/accountant/voluntary`, gõ giá trị ngày sai định dạng vào ô `Ngày nhận tiền`. Bấm `Ghi đóng góp`.
       - Trình duyệt kích hoạt cờ `validity.badInput`, phương thức `checkValidity()` trả về `false`. Giao diện chặn gửi form, hiển thị thông báo lỗi cục bộ (`Kiểm tra các ngày và thông tin đợt đóng góp.` hoặc `Chọn người đóng, nhập số tiền nguyên dương tối đa 18 chữ số và ngày hợp lệ.`), và không phát sinh HTTP request nào lên server.
    5. **Kiểm tra Số tiền lớn dạng chuỗi không làm tròn:**
       - Trên màn hình của ADMIN hoặc ACCOUNTANT, xem đợt có `totalCollected="999999999999999999"` và khoản đóng góp có `amount="999999999999999999"`.
       - Kiểm tra số tiền hiển thị: phải hiển thị đầy đủ `999.999.999.999.999.999 đ` thông qua hàm `formatVndDecimal`.
       - Xác nhận không xảy ra hiện tượng làm tròn sai (ví dụ `1.000.000.000.000.000.000 đ`) do ép kiểu số nguyên lớn về kiểu Number trong JavaScript.
  - **Kết quả mong đợi:** Layout 360px hiển thị hoàn chỉnh không lỗi tràn ngang; theme sáng/tối có độ tương phản đạt chuẩn; điều hướng bàn phím đầy đủ với focus ring nhìn thấy; date input `badInput` được chặn tại client; số tiền 18 chữ số hiển thị nguyên vẹn độ chính xác.
  - **Kiểm tra sau (Post-check):** Kiểm tra tab Network xác nhận không có request gửi đi khi form gặp date `badInput`.
  - **Nguồn đối chiếu:** `_agents_workspace/S08/01_contract.md:15,98,168,176,209`; `frontend/src/screens/s08/ApiVoluntaryPage.tsx:9,33,34,42`; `frontend/src/lib/format.ts:8`; `frontend/src/test/s08-screens.test.tsx:25,41`.
- **Kết quả kiểm thử:** **Bị chặn (Blocked)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Không thực thi thủ công. Bằng chứng tự động cho phần kiểm được: jsdom (`s08-voluntary-cases.test.tsx`): ô ngày trống (kết quả của ô ngày `badInput`) chặn gửi ở cả biểu mẫu đóng góp và biểu mẫu tạo đợt, hiển thị thông báo cục bộ và không gọi API; ngày kết thúc trước ngày bắt đầu bị chặn; số tiền 19 chữ số, `0`, `012`, `1.5`, `-5` bị chặn trước khi gọi API và ô nhập giới hạn 18 chữ số; tổng thu và số tiền trong sổ `999999999999999999` hiển thị đầy đủ `999.999.999.999.999.999 đ`, không làm tròn thành `1.000.000.000.000.000.000`; hộp thoại hủy nêu đúng người đóng, hộ và số tiền.
- **Chi tiết lỗi / Nhận xét:** Chưa kiểm được và cần trình duyệt thật (ví dụ Playwright): bố cục 360px không tràn ngang, theme sáng/tối và độ tương phản WCAG AA, điều hướng bàn phím với viền focus nhìn thấy, phím Esc trong hộp thoại, và việc không có request nào khi ô ngày ở trạng thái `badInput` thật (jsdom không tạo được `validity.badInput`). Thông báo lỗi của biểu mẫu tạo đợt trong giao diện là `Kiểm tra tên đợt và ngày kết thúc không trước ngày bắt đầu.`, khác với `Kiểm tra các ngày và thông tin đợt đóng góp.` trong đặc tả.
