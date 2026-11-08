# Ca kiểm thử đợt đóng góp tự nguyện và sổ ghi nhận (S08)

Các ID số trong dữ liệu (`101`, `102`, `103`, `701`, `702`, `801`, `999999`) là ID mẫu đã được tạo trước trên môi trường kiểm thử riêng; thay bằng ID thực tế tương ứng. `<HOM_NAY>`, `<HOM_QUA>`, `<NGAY_MAI>`, `<DAU_THANG_NAY>` và `<CUOI_THANG_NAY>` được xác định theo múi giờ `Asia/Ho_Chi_Minh`. Tài khoản đăng nhập có `status=ACTIVE`, `mustChangePassword=false`; request ghi gửi kèm `X-CSRF-TOKEN: <CSRF_TOKEN>`. Không dùng dữ liệu cư dân thật hoặc secret thật. Trạng thái mặc định của mọi ca là `Not run`.

### VOL-01.1 — ADMIN tạo và cập nhật đợt đóng góp tự nguyện khi còn ở trạng thái DRAFT

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns`, `PATCH /api/v1/voluntary-campaigns/701`.
- **Dữ liệu vào:** Header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body tạo `{"name":"  Quy Khuyen hoc 2026  ","description":"  Ung ho hoc sinh  ","opensOn":"<DAU_THANG_NAY>","closesOn":"<CUOI_THANG_NAY>"}`; body sửa `{"name":"Quy Khuyen hoc dot 1","description":"Cap nhat mo ta","opensOn":"<DAU_THANG_NAY>","closesOn":"<CUOI_THANG_NAY>","version":0}`.
- **Các bước:**
  1. Gửi `POST /api/v1/voluntary-campaigns` cùng body tạo.
  2. Lấy `id` (giả sử `701`) và gửi `PATCH /api/v1/voluntary-campaigns/701` cùng body sửa.
  3. Đối chiếu HTTP, body JSON và thực hiện kiểm sau.
- **Kỳ vọng:** Bước 1 trả HTTP 201; `name="Quy Khuyen hoc 2026"` (đã cắt khoảng trắng hai đầu), `description="Ung ho hoc sinh"`, `status="DRAFT"`, `version=0`, `publishedAt=null`, `closedAt=null`, `postedCount=0`, `totalCollected="0"`. Bước 2 trả HTTP 200; `name="Quy Khuyen hoc dot 1"`, `version=1`.
- **Kiểm sau:** Gọi `GET /api/v1/voluntary-campaigns/701` xác nhận dữ liệu; kiểm tra `audit_log` có `VOLUNTARY_CAMPAIGN_CREATED` và `VOLUNTARY_CAMPAIGN_UPDATED` với `changes` chỉ chứa `status`, `opensOn`, `closesOn`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:69,128,129,136,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:56,62`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:41,49`.

### VOL-01.2 — Từ chối tạo hoặc sửa đợt khi tên/mô tả sai giới hạn, ngày kết thúc trước ngày bắt đầu hoặc sai version

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns`, `PATCH /api/v1/voluntary-campaigns/701`.
- **Dữ liệu vào:** Đợt `701` đang ở `DRAFT` (`version=1`); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi `POST /api/v1/voluntary-campaigns` với tên rỗng `"   "`, tên dài 151 ký tự, hoặc mô tả dài 2001 ký tự.
  2. Gửi `POST /api/v1/voluntary-campaigns` với `opensOn="<NGAY_MAI>"`, `closesOn="<HOM_NAY>"` (`closesOn < opensOn`) hoặc ngày sai định dạng `"2026-13-01"`.
  3. Gửi `PATCH /api/v1/voluntary-campaigns/701` với `version=0` (version cũ) và với ID `999999` không tồn tại.
- **Kỳ vọng:** Bước 1 trả HTTP 400, `code=VALIDATION`. Bước 2 trả HTTP 400, `code=INVALID_DATE`. Bước 3 trả HTTP 409 `code=STALE_VERSION` cho `version=0`, và HTTP 404 `code=CAMPAIGN_NOT_FOUND` cho ID `999999`.
- **Kiểm sau:** Gọi `GET /api/v1/voluntary-campaigns/701` xác nhận đợt `701` giữ nguyên thông tin tại `version=1`.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:140,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:139,146`.

### VOL-02.1 — ADMIN công bố đợt (kể cả công bố trước ngày bắt đầu), khóa sửa nháp và đóng đợt sớm

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns/701/publish`, `PATCH /api/v1/voluntary-campaigns/701`, `POST /api/v1/voluntary-campaigns/701/close`.
- **Dữ liệu vào:** Đợt `701` ở `DRAFT` (`version=1`, `closesOn >= <HOM_NAY>`); đợt `702` ở `DRAFT` có `closesOn="<HOM_QUA>"` (đã hết hạn); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi `POST /api/v1/voluntary-campaigns/702/publish` với `{"version":0}` cho đợt đã hết hạn.
  2. Gửi `POST /api/v1/voluntary-campaigns/701/publish` với `{"version":1}`.
  3. Thử gửi `PATCH /api/v1/voluntary-campaigns/701` với `version=2` hoặc gọi lại `publish` lần hai.
  4. Gửi `POST /api/v1/voluntary-campaigns/701/close` với `{"version":2}`, sau đó thử gọi lại `close` hoặc `publish` trên đợt đã đóng.
- **Kỳ vọng:** Bước 1 trả HTTP 409, `code=INVALID_STATE`. Bước 2 trả HTTP 200; `status="PUBLISHED"`, `version=2`, `publishedAt` và `publishedBy` khác null. Bước 3 trả HTTP 409, `code=INVALID_STATE` (đã công bố không sửa được). Bước 4: lần gọi `close` đầu trả HTTP 200 với `status="CLOSED"`, `closedAt` và `closedBy` khác null; gọi lại `close` hoặc `publish` sau khi đóng trả HTTP 409 `code=INVALID_STATE`.
- **Kiểm sau:** Kiểm tra `audit_log` có `VOLUNTARY_CAMPAIGN_PUBLISHED` và `VOLUNTARY_CAMPAIGN_CLOSED`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26 (P3); `_agents_workspace/S08/01_contract.md:70,92,130,142,164`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:59,69`.

### VOL-02.2 — Tầm nhìn RESIDENT: chỉ thấy đợt đã công bố với DTO rút gọn, nháp trả 404 và đợt quá hạn hiển thị `CLOSED`

- **Vai trò:** RESIDENT, ADMIN.
- **Điểm vào:** `GET /api/v1/voluntary-campaigns?status=&page=0&size=20`, `GET /api/v1/voluntary-campaigns/{id}`.
- **Dữ liệu vào:** Có 3 đợt: đợt `711` ở `DRAFT` (`publishedAt=null`); đợt `712` đã công bố còn hạn (`status=PUBLISHED`, `closesOn >= <HOM_NAY>`, có `postedCount=2`, `totalCollected="500000"`); đợt `713` đã công bố nhưng `closesOn="<HOM_QUA>"` (chưa bấm đóng tay, DB `status=PUBLISHED`).
- **Các bước:**
  1. Từ phiên RESIDENT, gọi `GET /api/v1/voluntary-campaigns?page=0&size=20` và `GET /api/v1/voluntary-campaigns?status=DRAFT`.
  2. Từ phiên RESIDENT, gọi `GET /api/v1/voluntary-campaigns/711` (nháp), `/712` (đang mở) và `/713` (đã qua ngày kết thúc).
  3. Gọi `GET /api/v1/voluntary-campaigns?status=CLOSED` bằng cả RESIDENT và ADMIN.
- **Kỳ vọng:** Bước 1: danh sách của RESIDENT chỉ chứa `712` và `713`, không bao giờ chứa `711`; khi lọc `status=DRAFT`, RESIDENT nhận `content=[]` (`totalElements=0`). Bước 2: `/711` trả HTTP 404 `code=CAMPAIGN_NOT_FOUND`; `/712` và `/713` trả HTTP 200 với đúng 6 trường `id`, `name`, `description`, `opensOn`, `closesOn`, `status` (không có `version`, `createdBy`, `publishedAt`, `postedCount`, `totalCollected`). Đợt `713` có `status="CLOSED"`. Bước 3: lọc `status=CLOSED` trả về đợt `713` (và các đợt đóng tay).
- **Kiểm sau:** Gọi `/api/v1/voluntary-campaigns/712` bằng ADMIN xác nhận staff DTO vẫn có đủ `postedCount=2` và `totalCollected="500000"`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:30,72,73,127,136,209`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryCampaignService.java:78,90,113,123`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/persistence/VoluntaryCampaignRepository.java:14`.

### VOL-03.1 — ACCOUNTANT tìm người đóng qua `/voluntary-donors` gồm cả nhân khẩu MOVED_OUT/hộ INACTIVE và ẩn PII

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `GET /api/v1/voluntary-donors?q=<TU_KHOA>&page=0&size=20`.
- **Dữ liệu vào:** Nhân khẩu `101` (`RESIDING`, hộ `ACTIVE`), nhân khẩu `102` (`TEMPORARILY_ABSENT`, hộ `ACTIVE`), nhân khẩu `103` (`MOVED_OUT`, hộ `INACTIVE`) đều có tên chứa `"Nguyen"` và có `identityNumber`, `phone` trong DB.
- **Các bước:**
  1. Gọi `GET /api/v1/voluntary-donors?q=Nguyen&page=0&size=20`.
  2. Gọi `GET /api/v1/voluntary-donors?q=N` (1 ký tự), `?q=%` (1 ký tự wildcard) và `?q=   ` (rỗng).
  3. Đối chiếu các trường trong response.
- **Kỳ vọng:** Bước 1 trả HTTP 200; `content` gồm cả `101`, `102` và `103`; mỗi phần tử chỉ có 4 trường `id`, `fullName`, `householdCode`, `apartmentCode` (tuyệt đối không có `identityNumber`, `phone`, `dateOfBirth`, `email`). Bước 2 trả HTTP 400, `code=INVALID_FILTER`.
- **Kiểm sau:** Xác nhận ký tự `%` và `_` trong `q` được escape, không lộ toàn bộ danh sách khi nhập `%%` nếu không có tên chứa `%%`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26 (P5); `_agents_workspace/S08/01_contract.md:30,77,94,131`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:42,80`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentDonorLookupService.java:26`.

### VOL-04.1 — Ghi nhận khoản đóng góp hợp lệ (bao gồm nhân khẩu MOVED_OUT/hộ INACTIVE và đóng nhiều lần với key mới)

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns/712/contributions`.
- **Dữ liệu vào:** Đợt `712` đang mở (`PUBLISHED`, `opensOn <= <HOM_NAY> <= closesOn`, `publishedAt` trong ngày hoặc trước `<HOM_NAY>`); nhân khẩu `103` (`MOVED_OUT`, thuộc hộ `INACTIVE`); hai UUID hợp lệ `<UUID_1>` và `<UUID_2>`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi `POST /api/v1/voluntary-campaigns/712/contributions` với header `Idempotency-Key: <UUID_1>` và body `{"residentId":103,"amount":"200000","paidOn":"<HOM_NAY>","lateReason":null}`.
  2. Gửi tiếp lần đóng thứ hai cho cùng nhân khẩu `103`, cùng ngày và số tiền nhưng với header `Idempotency-Key: <UUID_2>`.
  3. Sau đó ADMIN đổi tên nhân khẩu `103` qua `PATCH /api/v1/residents/103`; gọi `GET /api/v1/voluntary-campaigns/712/contributions` và `GET /api/v1/voluntary-campaigns/712`.
- **Kỳ vọng:** Bước 1 và bước 2 đều trả HTTP 201; mỗi bản ghi có `status="POSTED"`, `amount="200000"`, `householdId` do server suy ra từ nhân khẩu `103`, kèm `donorNameSnapshot`, `householdCodeSnapshot`, `apartmentCodeSnapshot`. Bước 3: `donorNameSnapshot` của hai khoản đã ghi giữ nguyên tên lúc ghi nhận; đợt `712` có `postedCount` tăng 2 và `totalCollected` tăng `"400000"`; không làm thay đổi `invoices` hay `receiptCount` của bất kỳ kỳ thu bắt buộc nào.
- **Kiểm sau:** Kiểm tra `audit_log` có 2 dòng `VOLUNTARY_CONTRIBUTION_RECORDED` với `changes` chỉ chứa `campaignId`, `amount`, `paidOn`, `status` (không chứa tên người đóng hay `lateReason`).
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:77,78,80,82,133,136,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:53,64`.

### VOL-04.2 — Quy tắc cửa sổ ngày thực thu (`paidOn`) và bắt buộc `lateReason` khi nhập muộn sau đóng hoặc sau hạn

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns/{id}/contributions`.
- **Dữ liệu vào:** Nhân khẩu `101` tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`, mỗi request thử mới dùng một `Idempotency-Key` UUID mới:
  - Đợt `720` còn ở `DRAFT`.
  - Đợt `721` được công bố hôm nay (`publishedOn = <HOM_NAY>`) nhưng lịch `opensOn = <NGAY_MAI>`, hoặc lịch `opensOn = <DAU_THANG_NAY>` nhưng `publishedOn = <HOM_NAY>`.
  - Đợt `722` đã qua ngày kết thúc (`opensOn = <DAU_THANG_NAY>`, `publishedOn = <DAU_THANG_NAY>`, `closesOn = <HOM_QUA>`) hoặc đã được ADMIN đóng sớm vào `<HOM_QUA>`.
- **Các bước:**
  1. Gửi đóng góp vào đợt `720` (`DRAFT`).
  2. Trên đợt đang mở, gửi `paidOn="<NGAY_MAI>"` (ngày tương lai) và gửi `paidOn` trước `max(opensOn, publishedOn)`.
  3. Trên đợt `722` (đã qua `closesOn` hoặc đã đóng sớm ngày `<HOM_QUA>`):
     - Lần 3a: gửi `paidOn="<HOM_NAY>"` (sau ngày kết thúc/ngày đóng).
     - Lần 3b: gửi `paidOn="<HOM_QUA>"` (nằm trong cửa sổ thực thu hợp lệ, bao gồm trọn ngày đóng sớm) nhưng `lateReason=null` hoặc `"   "`.
     - Lần 3c: gửi `paidOn="<HOM_QUA>"` kèm `lateReason="Thu tien mat cuoi ngay hom qua"`.
- **Kỳ vọng:** Bước 1 trả HTTP 409, `code=CAMPAIGN_NOT_RECEIVABLE`. Bước 2 và lần 3a trả HTTP 409, `code=INVALID_PAID_DATE`. Lần 3b trả HTTP 400, `code=REASON_REQUIRED`. Lần 3c trả HTTP 201, `status="POSTED"`, `lateReason="Thu tien mat cuoi ngay hom qua"`.
- **Kiểm sau:** Gọi `GET /api/v1/voluntary-campaigns/722/contributions` xác nhận chỉ có duy nhất khoản từ lần 3c được lưu.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26 (P3); `_agents_workspace/S08/01_contract.md:71,72,78,79,92,142,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryReceiptWindow.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:114`.

### VOL-04.3 — Từ chối khoản đóng góp khi số tiền, khóa Idempotency-Key, nhân khẩu hoặc độ dài lý do không hợp lệ

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns/712/contributions`.
- **Dữ liệu vào:** Đợt `712` đang mở; nhân khẩu `101` tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi body với `amount` lần lượt là: số JSON `100000` (không phải chuỗi), `"0"`, `"-500"`, `"100.50"`, `"1000000000000000000"` (19 chữ số).
  2. Gửi request thiếu header `Idempotency-Key` hoặc `Idempotency-Key: not-a-uuid`.
  3. Gửi request với `residentId=null`, với `lateReason` dài 256 ký tự, và với `residentId=999999` (không tồn tại).
- **Kỳ vọng:** Bước 1 trả HTTP 400, `code=INVALID_AMOUNT`. Bước 2 trả HTTP 400, `code=INVALID_IDEMPOTENCY_KEY`. Bước 3: `residentId=null` trả HTTP 400 `code=VALIDATION`; `lateReason` 256 ký tự trả HTTP 400 `code=REASON_REQUIRED`; `residentId=999999` trả HTTP 404 `code=RESIDENT_NOT_FOUND`.
- **Kiểm sau:** Gọi `GET /api/v1/voluntary-campaigns/712/contributions` xác nhận không phát sinh khoản đóng góp nào từ các request lỗi.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:78,133,140,142`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:93`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/ContributionInput.java:16`.

### VOL-05.1 — Idempotency replay (HTTP 200), xung đột khác payload (`IDEMPOTENCY_CONFLICT`) và replay sau khi đợt đóng hoặc bản ghi đã hủy

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns/712/contributions`, `POST /api/v1/voluntary-campaigns/712/close`, `POST /api/v1/voluntary-contributions/{id}/void`.
- **Dữ liệu vào:** Đợt `712` đang mở; nhân khẩu `101`; header `Idempotency-Key: 11111111-2222-3333-4444-555555555555` (thử gửi lại với chữ hoa `11111111-2222-3333-4444-555555555555` / chữ thường); header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi `POST /api/v1/voluntary-campaigns/712/contributions` với key trên và body `{"residentId":101,"amount":"150000","paidOn":"<HOM_NAY>","lateReason":null}`; lưu `id` bản ghi (giả sử `801`).
  2. Gửi lại đúng request trên (cùng actor, cùng key, cùng payload).
  3. Gửi lại cùng key trên nhưng đổi `amount` thành `"250000"` (hoặc đổi `residentId`, `paidOn`, `campaignId`).
  4. ADMIN đóng đợt `712`; ACCOUNTANT gửi lại đúng request gốc của bước 1 (không có `lateReason`).
  5. ACCOUNTANT hủy bản ghi `801` qua `POST /api/v1/voluntary-contributions/801/void`; sau đó gửi lại đúng request gốc của bước 1.
- **Kỳ vọng:** Bước 1 trả HTTP 201 (`id=801`, `status="POSTED"`). Bước 2 trả HTTP 200 với cùng `id=801`, không tăng `postedCount` hay `totalCollected`. Bước 3 trả HTTP 409, `code=IDEMPOTENCY_CONFLICT`. Bước 4 trả HTTP 200 với bản ghi `801` (replay kiểm tra trước điều kiện cửa sổ đợt). Bước 5: sau khi `801` đã `VOIDED`, replay cùng key vẫn trả HTTP 200 với bản ghi `801` đang có `status="VOIDED"`, không tạo bản ghi mới.
- **Kiểm sau:** Kiểm tra `audit_log` chỉ có đúng 1 dòng `VOLUNTARY_CONTRIBUTION_RECORDED` và 1 dòng `VOLUNTARY_CONTRIBUTION_VOIDED` cho `801`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:80,81,133,142,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/VoluntaryController.java:98`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:53,107`.

### VOL-05.2 — Race condition: Hai request ghi nhận đóng góp đồng thời cùng actor, cùng Idempotency-Key và cùng payload chỉ tạo một bản ghi và một audit

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/voluntary-campaigns/{id}/contributions`.
- **Dữ liệu vào:** Đợt `712` đang mở (`PUBLISHED`, còn hạn thu); nhân khẩu `101`; header `Idempotency-Key: 33333333-4444-5555-6666-777777777777` cố định; header `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"amount":"500000","paidOn":"<HOM_NAY>","lateReason":null}`. Hai request gửi đồng thời từ cùng tài khoản ACCOUNTANT qua test harness có rào cản đồng bộ (`CountDownLatch` / pool 2 worker hoặc curl concurrent).
- **Các bước:**
  1. Xác nhận đợt `712` đang mở, ghi nhận giá trị `postedCount` và `totalCollected` ban đầu.
  2. Kích hoạt 2 luồng đồng thời gửi `POST /api/v1/voluntary-campaigns/712/contributions` với cùng actor, cùng header `Idempotency-Key` và cùng body trên qua điểm chặn barrier.
  3. Đợi cả hai luồng hoàn tất, thu nhận HTTP status code, body JSON và thuộc tính bản ghi của từng luồng.
  4. Kiểm tra dữ liệu trong bảng `voluntary_contributions`, `audit_log` và các chỉ số tổng của đợt trong `voluntary_campaigns`.
- **Kỳ vọng:** Cả hai request đều thành công và trả về cùng thông tin khoản đóng góp (cùng `id`, `amount="500000"`, `paidOn="<HOM_NAY>"`). Trong đó: đúng một request tạo mới trả về HTTP 201; request còn lại được xử lý replay qua ràng buộc duy nhất trên `(recorded_by, idempotency_key)` và trả về HTTP 200 (idempotent replay). Số lượng khoản đóng góp tăng đúng 1 (`postedCount` tăng 1) và tổng thu tăng đúng 1 lần (`totalCollected` tăng `500.000 đ`), không bị tính trùng hai lần. Chỉ có duy nhất đúng 1 bản ghi audit log được sinh ra.
- **Kiểm sau:**
  - Truy vấn DB: `SELECT COUNT(*) FROM voluntary_contributions WHERE campaign_id=712 AND resident_id=101 AND idempotency_key='33333333-4444-5555-6666-777777777777'` trả về đúng `1`.
  - Truy vấn `audit_log`: `SELECT COUNT(*) FROM audit_log WHERE action='VOLUNTARY_CONTRIBUTION_RECORDED' AND entity_type='VoluntaryContribution' AND entity_id='<ID_KHOAN_DONG>'` trả về đúng `1`.
  - Kiểm tra đợt: `totalCollected` tăng đúng `500000`, `postedCount` tăng đúng `1`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:14,80,133,156,160,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:53`; `backend/src/test/java/vn/edu/se10/bluemoon/billing/VoluntaryContributionIT.java:116`.

### VOL-06.1 — ACCOUNTANT hủy khoản đóng góp (`void`), kiểm tra tính bất biến lịch sử, idempotent khi hủy lặp và cập nhật tổng thu

- **Vai trò:** ACCOUNTANT, ADMIN.
- **Điểm vào:** `POST /api/v1/voluntary-contributions/802/void`, `GET /api/v1/voluntary-campaigns/712/contributions?status=&page=0&size=20`, `GET /api/v1/voluntary-campaigns/712`.
- **Dữ liệu vào:** Đợt `712` có hai khoản `POSTED`: khoản `802` (`amount="300000"`, `version=0`) và khoản `803` (`amount="100000"`, `version=0`); tổng đợt hiện là `postedCount=2`, `totalCollected="400000"`; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Gửi `POST /api/v1/voluntary-contributions/802/void` với `{"version":0,"reason":"   "}` (rỗng) và với `{"version":9,"reason":"Nhap nham"}` (sai version).
  2. Gửi `POST /api/v1/voluntary-contributions/802/void` với `{"version":0,"reason":"  Nhap nham so tien  "}`.
  3. Gửi lặp lại `POST /api/v1/voluntary-contributions/802/void` với `{"version":0,"reason":"Ly do khac"}` trên khoản đã `VOIDED`.
  4. Gọi `GET /api/v1/voluntary-campaigns/712`, `GET /api/v1/voluntary-campaigns/712/contributions?status=POSTED` và `?status=VOIDED`.
- **Kỳ vọng:** Bước 1 trả HTTP 400 `code=REASON_REQUIRED` cho lý do rỗng, và HTTP 409 `code=STALE_VERSION` cho sai version (hoặc 404 `code=CONTRIBUTION_NOT_FOUND` nếu ID không tồn tại). Bước 2 trả HTTP 200; `status="VOIDED"`, `voidReason="Nhap nham so tien"`, `voidedAt` và `voidedBy` khác null. Bước 3 trả HTTP 200 giữ nguyên bản ghi đã `VOIDED`, không sinh thêm audit. Bước 4: đợt `712` có `postedCount=1`, `totalCollected="100000"`; lọc `status=POSTED` chỉ có `803`, lọc `status=VOIDED` có `802` (vẫn giữ lịch sử).
- **Kiểm sau:** Kiểm tra `audit_log` chỉ có 1 dòng `VOLUNTARY_CONTRIBUTION_VOIDED` cho `802` và `changes` không chứa `voidReason`.
- **Trạng thái:** Not run.
- **Nguồn:** `docs/DECISIONS.md` D26; `_agents_workspace/S08/01_contract.md:82,132,134,142,164,208`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/VoluntaryContributionService.java:84,99`.

### VOL-07.1 — Phân quyền API tự nguyện: chặn ANON, RESIDENT đọc sổ/donor, ADMIN ghi đóng góp, ACCOUNTANT quản lý đợt

- **Vai trò:** ANON, RESIDENT, ACCOUNTANT, ADMIN.
- **Điểm vào:** Các endpoint `/api/v1/voluntary-campaigns/**`, `/api/v1/voluntary-donors`, `/api/v1/voluntary-contributions/*/void`.
- **Dữ liệu vào:** Đợt `712` và khoản đóng góp `803` tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Từ phiên ANON, gọi `GET /api/v1/voluntary-campaigns` và `POST /api/v1/voluntary-campaigns`.
  2. Từ phiên RESIDENT, gọi `GET /api/v1/voluntary-campaigns/712/contributions`, `GET /api/v1/voluntary-donors?q=Ng`, `POST /api/v1/voluntary-campaigns/712/contributions`, `POST /api/v1/voluntary-contributions/803/void` và `POST /api/v1/voluntary-campaigns`.
  3. Từ phiên ADMIN, gọi `GET /api/v1/voluntary-donors?q=Ng`, `POST /api/v1/voluntary-campaigns/712/contributions` và `POST /api/v1/voluntary-contributions/803/void`.
  4. Từ phiên ACCOUNTANT, gọi `POST /api/v1/voluntary-campaigns`, `PATCH /api/v1/voluntary-campaigns/712`, `POST /api/v1/voluntary-campaigns/712/publish` và `POST /api/v1/voluntary-campaigns/712/close`.
- **Kỳ vọng:** Bước 1 trả HTTP 401. Bước 2, 3 và 4 trả HTTP 403 cho mọi request vượt quyền.
- **Kiểm sau:** Gọi `GET /api/v1/voluntary-campaigns/712` bằng ADMIN xác nhận trạng thái đợt và tổng tiền không đổi.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:20,30,125,209`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:71`.

### VOL-08.1 — Giao diện `/admin/voluntary`, `/accountant/voluntary` và `/resident/voluntary` theo đúng vai trò

- **Vai trò:** ADMIN, ACCOUNTANT, RESIDENT.
- **Điểm vào:** `/admin/voluntary`, `/accountant/voluntary`, `/resident/voluntary`.
- **Dữ liệu vào:** Chế độ `api`; có nhân khẩu `"Người thử"` trong hệ thống.
- **Các bước:**
  1. Đăng nhập ADMIN, mở `/admin/voluntary`, bấm `Tạo đợt đóng góp`, nhập tên, mô tả, ngày bắt đầu/kết thúc hợp lệ rồi bấm `Lưu`; chọn `Xem chi tiết` đợt vừa tạo, bấm `Công bố` và xác nhận.
  2. Kiểm tra trên màn chi tiết của ADMIN có hiển thị `Tổng đã ghi`, `Sổ đóng góp`, nút `Đóng đợt`, nhưng không có biểu mẫu `Ghi đóng góp` hay nút `Hủy khoản đóng góp`.
  3. Đăng nhập ACCOUNTANT, mở `/accountant/voluntary`, bấm `Xem chi tiết` đợt vừa công bố; tìm `"Người"` tại ô `Tìm người đóng`, chọn người đóng, nhập số tiền `"200000"`, chọn `Ngày nhận tiền` và bấm `Ghi đóng góp`.
  4. Đăng nhập RESIDENT, mở `/resident/voluntary` và kiểm tra danh sách đợt.
- **Kỳ vọng:** ADMIN tạo và công bố đợt thành công; ACCOUNTANT ghi nhận khoản đóng góp và có nút `Hủy khoản đóng góp` trên dòng `Đã ghi`. RESIDENT tại `/resident/voluntary` chỉ thấy tên, trạng thái (`Sắp bắt đầu` / `Đang mở` / `Đã kết thúc`), ngày bắt đầu – kết thúc và mô tả của đợt đã công bố; không có nút `Xem chi tiết`, không hiển thị tổng tiền thu hay danh sách người đóng, và trình duyệt không gọi API `/contributions`.
- **Kiểm sau:** Mở `/admin/voluntary` bằng ADMIN xác nhận tổng đã ghi tăng lên `200.000 đ` và sổ có khoản vừa nhập.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:172,173,174`; `frontend/src/App.tsx:128,139,150`; `frontend/src/screens/s08/ApiVoluntaryPage.tsx:16,36`.

### VOL-08.2 — Giao diện ACCOUNTANT giữ nguyên `Idempotency-Key` khi mất kết nối/5xx để thử ghi lại và mở khóa khi lỗi 400/409

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `/accountant/voluntary`.
- **Dữ liệu vào:** Chế độ `api`; đợt `712` đang mở; đã tìm và chọn người đóng.
- **Các bước:**
  1. Nhập số tiền `"100000"`, ngày nhận tiền hợp lệ; dùng DevTools Network chặn mạng (Offline) hoặc mô phỏng lỗi 503 cho request `POST /api/v1/voluntary-campaigns/712/contributions`, rồi bấm `Ghi đóng góp`.
  2. Quan sát trạng thái khóa các ô nhập và thông báo `Chưa xác nhận được kết quả. Thử lại cùng nội dung để tránh ghi trùng.`; bật lại mạng rồi bấm `Thử ghi lại`.
  3. Thực hiện một lần nhập mới với `Ngày nhận tiền` không hợp lệ (ví dụ ngày tương lai) để server trả HTTP 409 `code=INVALID_PAID_DATE`.
- **Kỳ vọng:** Ở bước 1–2, các ô nhập bị khóa (`disabled`) và nút đổi thành `Thử ghi lại`; khi bấm `Thử ghi lại`, trình duyệt gửi lại đúng `Idempotency-Key` và payload ban đầu (kể cả khi điều hướng sang màn khác rồi quay lại trong cùng tài khoản, nhưng không lộ sang tài khoản khác). Ở bước 3, khi nhận lỗi 400 hoặc 409 (`INVALID_PAID_DATE`, `CAMPAIGN_NOT_RECEIVABLE`), giao diện mở khóa lại các ô nhập và đổi `Idempotency-Key` mới để người dùng sửa ngày.
- **Kiểm sau:** Kiểm tra tab Network xác nhận header `Idempotency-Key` ở bước 1 và bước 2 giống hệt nhau.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:80,173,176`; `frontend/src/screens/s08/ApiVoluntaryPage.tsx:34,42`.

### VOL-08.3 — Giao diện đóng góp tự nguyện: mobile viewport 360px, theme sáng/tối, điều hướng keyboard-only với focus nhìn thấy, date input badInput và hiển thị số tiền lớn

- **Vai trò:** ADMIN, ACCOUNTANT, RESIDENT.
- **Điểm vào:** `/admin/voluntary`, `/accountant/voluntary`, `/resident/voluntary`.
- **Dữ liệu vào:** Chế độ `api`; đợt đóng góp có tổng thu lớn (`999999999999999999` VND - 18 chữ số); ngày nhận tiền hoặc ngày đợt sai định dạng; mô phỏng viewport 360px × 640px.
  *(Lưu ý: Luồng phân quyền hiển thị, giữ `Idempotency-Key` khi mất mạng để retry và mở khóa key khi gặp 400/409 đã được kiểm thử tại [VOL-08.1](#vol-081--giao-diện-adminvoluntary-accountantvoluntary-và-residentvoluntary-theo-đúng-vai-trò) và [VOL-08.2](#vol-082--giao-diện-accountant-giữ-nguyên-idempotency-key-khi-mất-kết-nối5xx-để-thử-ghi-lại-và-mở-khóa-khi-lỗi-400409); ca này tập trung kiểm thử trải nghiệm người dùng, tính tiếp cận và độ chính xác hiển thị tiền lớn theo hợp đồng mục 11).*
- **Các bước:**
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
- **Kỳ vọng:** Layout 360px hiển thị hoàn chỉnh không lỗi tràn ngang; theme sáng/tối có độ tương phản đạt chuẩn; điều hướng bàn phím đầy đủ với focus ring nhìn thấy; date input `badInput` được chặn tại client; số tiền 18 chữ số hiển thị nguyên vẹn độ chính xác.
- **Kiểm sau:** Kiểm tra tab Network xác nhận không có request gửi đi khi form gặp date `badInput`.
- **Trạng thái:** Not run.
- **Nguồn:** `_agents_workspace/S08/01_contract.md:15,98,168,176,209`; `frontend/src/screens/s08/ApiVoluntaryPage.tsx:9,33,34,42`; `frontend/src/lib/format.ts:8`; `frontend/src/test/s08-screens.test.tsx:25,41`.

## Điểm chưa xác định

- Khi gọi `POST /api/v1/voluntary-contributions/{id}/void` với `id` không tồn tại, `VoluntaryContributionService.java:86,88` trả HTTP 404 với `code=CONTRIBUTION_NOT_FOUND`; mã `CONTRIBUTION_NOT_FOUND` chưa được liệt kê tường minh trong dòng tóm tắt 404 tại `_agents_workspace/S08/01_contract.md:142` (hợp đồng mới liệt kê `BILLING_PERIOD_NOT_FOUND/INVOICE_NOT_FOUND/CAMPAIGN_NOT_FOUND/RESIDENT_NOT_FOUND`).
- Khi gọi `GET /api/v1/voluntary-campaigns` hoặc `GET /api/v1/voluntary-campaigns/{id}/contributions` từ giao diện `ApiVoluntaryPage.tsx:28,29`, backend hỗ trợ tham số lọc `?status=` nhưng giao diện hiện chưa có điều khiển lọc theo `status`.

