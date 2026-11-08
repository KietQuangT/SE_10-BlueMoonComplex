# Ca kiểm thử khai báo tạm vắng

Các ID số trong dữ liệu là ID mẫu đã được tạo trước; thay bằng ID thực tế tương ứng. `<NGAY_MAI>`, `<HOM_QUA>`, `<THANG_SAU>` và `<THANG_NAY>` được xác định theo múi giờ Asia/Ho_Chi_Minh. Tài khoản đã đăng nhập và đã đổi mật khẩu bắt buộc; request ghi dùng CSRF hợp lệ.

### DEC-01.1 — Gửi đơn hợp lệ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<NGAY_MAI>","endDate":null,"reason":"Cong tac"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi body tới endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 201; `status=PENDING`, `residentId=101`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine tìm đơn và audit `ABSENCE_REQUESTED`.
- **Nguồn:** DEC-01; `tests/00-outline/feature-inventory.md:121`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:16`.

### DEC-01.2 — Không nhận ngày quá khứ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<HOM_QUA>","reason":"Cong tac"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi body với ngày hôm qua.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=INVALID_DATE`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine xác nhận không có đơn mới.
- **Nguồn:** DEC-01; `tests/00-outline/feature-inventory.md:121`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:18`.

### DEC-01.3 — Không nhận lý do dài 256 ký tự

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body như ca DEC-01.1 với `reason` là chuỗi gồm đúng 256 ký tự `a`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi body với lý do 256 ký tự.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=VALIDATION`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine xác nhận không có đơn mới.
- **Nguồn:** DEC-01; `tests/00-outline/feature-inventory.md:121`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:40`.

### DEC-02.1 — Không tạo đơn PENDING thứ hai

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, đã có một đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<NGAY_MAI>","endDate":null,"reason":"Cong tac"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi lại body cho nhân khẩu 101.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=DECLARATION_ALREADY_PENDING`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine xác nhận chỉ có một đơn PENDING của 101.
- **Nguồn:** DEC-02; `tests/00-outline/feature-inventory.md:122`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:23`.

### DEC-02.2 — Không khai cho người đang tạm vắng

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations`.
- **Dữ liệu vào:** Nhân khẩu 101 thuộc hộ A, TEMPORARILY_ABSENT; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<NGAY_MAI>","endDate":null,"reason":"Cong tac"}`.
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi body cho nhân khẩu 101.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=RESIDENT_NOT_AVAILABLE`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine xác nhận không có đơn mới.
- **Nguồn:** DEC-02; `tests/00-outline/feature-inventory.md:122`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:21`.

### DEC-03.1 — Thấy đơn do người cùng hộ gửi

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Hộ A có đơn 201 do tài khoản RESIDENT khác của cùng hộ gửi..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint khi đăng nhập tài khoản RESIDENT thuộc hộ A.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `content` chứa đơn 201 của hộ A.
- **Kiểm sau:** Đối chiếu `householdId` của đơn 201 với hộ A.
- **Nguồn:** DEC-03; `tests/00-outline/feature-inventory.md:123`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:30`.

### DEC-03.2 — Không thấy đơn của hộ khác

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/residency-declarations/mine`.
- **Dữ liệu vào:** Hộ B có đơn 202; tài khoản hiện tại thuộc hộ A..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint khi đăng nhập tài khoản hộ A.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `content` không chứa đơn 202.
- **Kiểm sau:** Đối chiếu `householdId` các dòng trả về đều thuộc hộ A.
- **Nguồn:** DEC-03; `tests/00-outline/feature-inventory.md:123`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:30`.

### DEC-04.1 — Chủ đơn hủy đơn đang chờ

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/201/cancel`.
- **Dữ liệu vào:** Đơn 201 PENDING do tài khoản hiện tại gửi; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `status=CANCELLED`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine vẫn chứa đơn 201; audit `ABSENCE_CANCELLED`.
- **Nguồn:** DEC-04; `tests/00-outline/feature-inventory.md:124`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:26`.

### DEC-04.2 — Người khác cùng hộ không được hủy

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/201/cancel`.
- **Dữ liệu vào:** Đơn 201 PENDING do tài khoản khác cùng hộ gửi; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST bằng tài khoản không tạo đơn.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 403.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine xác nhận đơn 201 vẫn PENDING.
- **Nguồn:** DEC-04; `tests/00-outline/feature-inventory.md:124`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:26`.

### DEC-04.3 — Không hủy đơn đã quyết định

- **Vai trò:** RESIDENT.
- **Điểm vào:** `POST /api/v1/residency-declarations/201/cancel`.
- **Dữ liệu vào:** Đơn 201 APPROVED do tài khoản hiện tại gửi; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=DECLARATION_NOT_PENDING`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine xác nhận đơn vẫn APPROVED.
- **Nguồn:** DEC-04; `tests/00-outline/feature-inventory.md:124`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:38`.

### DEC-05.1 — Lọc hàng chờ theo trạng thái

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations?status=PENDING`.
- **Dữ liệu vào:** Có đơn 201 PENDING và đơn 202 REJECTED..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint với tham số status=PENDING.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `content` chứa 201, không chứa 202.
- **Kiểm sau:** Đối chiếu mọi dòng có `status=PENDING` và `changeType=TEMPORARY_ABSENCE`.
- **Nguồn:** DEC-05; `tests/00-outline/feature-inventory.md:125`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`.

### DEC-05.2 — Từ chối trạng thái lọc không hợp lệ

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations?status=UNKNOWN`.
- **Dữ liệu vào:** Tham số `status=UNKNOWN`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=INVALID_FILTER`.
- **Kiểm sau:** Gọi GET /api/v1/residency-declarations?status=PENDING xác nhận hàng chờ còn đọc được.
- **Nguồn:** DEC-05; `tests/00-outline/feature-inventory.md:125`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`.

### DEC-05.3 — Từ chối truy vấn tên một ký tự

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residency-declarations?q=a`.
- **Dữ liệu vào:** Tham số `q=a`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gọi endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=INVALID_FILTER`.
- **Kiểm sau:** Gọi GET /api/v1/residency-declarations?status=PENDING xác nhận dữ liệu không đổi.
- **Nguồn:** DEC-05; `tests/00-outline/feature-inventory.md:125`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:42`.

### DEC-06.1 — Duyệt đơn chờ chuyển nhân khẩu tạm vắng

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/201/approve`.
- **Dữ liệu vào:** Đơn 201 PENDING của nhân khẩu 101 RESIDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; đơn `status=APPROVED`.
- **Kiểm sau:** GET /api/v1/residents/101 có `residencyStatus=TEMPORARILY_ABSENT`; audit `ABSENCE_APPROVED`.
- **Nguồn:** DEC-06; `tests/00-outline/feature-inventory.md:126`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:27`.

### DEC-06.2 — Không duyệt lại đơn đã duyệt

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/201/approve`.
- **Dữ liệu vào:** Đơn 201 đã APPROVED; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=DECLARATION_NOT_PENDING`.
- **Kiểm sau:** GET /api/v1/residency-declarations?status=APPROVED xác nhận đơn 201 không đổi.
- **Nguồn:** DEC-06; `tests/00-outline/feature-inventory.md:126`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:38`.

### DEC-07.1 — Từ chối đơn với ghi chú

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/201/reject`.
- **Dữ liệu vào:** Đơn 201 PENDING của nhân khẩu 101 RESIDING; body `{"decisionNote":"Thieu thong tin"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `status=REJECTED`, `decisionNote=Thieu thong tin`.
- **Kiểm sau:** GET /api/v1/residents/101 vẫn RESIDING; audit `ABSENCE_REJECTED`.
- **Nguồn:** DEC-07; `tests/00-outline/feature-inventory.md:127`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:28`.

### DEC-07.2 — Không từ chối khi thiếu ghi chú

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residency-declarations/201/reject`.
- **Dữ liệu vào:** Đơn 201 PENDING; body `{}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST cùng body.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 400, `code=VALIDATION`.
- **Kiểm sau:** GET /api/v1/residency-declarations?status=PENDING xác nhận đơn 201 còn PENDING.
- **Nguồn:** DEC-07; `tests/00-outline/feature-inventory.md:127`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:40`.

### DEC-08.1 — Ghi nhận nhân khẩu quay lại

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/101/return`.
- **Dữ liệu vào:** Nhân khẩu 101 TEMPORARILY_ABSENT; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 200; `residencyStatus=RESIDING`.
- **Kiểm sau:** GET /api/v1/households/1/residency-changes thấy `RETURN` với ngày hôm nay theo giờ Việt Nam; audit `RESIDENT_RETURNED`.
- **Nguồn:** DEC-08; `tests/00-outline/feature-inventory.md:128`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:41`.

### DEC-08.2 — Không ghi quay lại khi đang cư trú

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/101/return`.
- **Dữ liệu vào:** Nhân khẩu 101 RESIDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Gửi POST đến endpoint.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** HTTP 409, `code=RESIDENT_NOT_ABSENT`.
- **Kiểm sau:** GET /api/v1/residents/101 xác nhận vẫn RESIDING và không thêm lịch sử RETURN.
- **Nguồn:** DEC-08; `tests/00-outline/feature-inventory.md:128`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:29`.

### DEC-09.1 — Cư dân thấy lý do đơn bị từ chối

- **Vai trò:** RESIDENT.
- **Điểm vào:** `/resident/absence`.
- **Dữ liệu vào:** Chế độ api; hộ hiện tại có đơn REJECTED với `decisionNote=Thieu thong tin`..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Mở /resident/absence.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** Danh sách hiển thị `Lý do từ chối: Thieu thong tin`.
- **Kiểm sau:** GET /api/v1/residency-declarations/mine đối chiếu `status` và `decisionNote`.
- **Nguồn:** DEC-09; `tests/00-outline/feature-inventory.md:129`; `frontend/src/screens/resident/ApiAbsencePage.tsx:72`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:33`.

### DEC-09.2 — Quản trị xem hàng chờ

- **Vai trò:** ADMIN.
- **Điểm vào:** `/admin/absence-approvals`.
- **Dữ liệu vào:** Chế độ api; đơn 201 đang PENDING..
- **Các bước:**
  1. Xác nhận dữ liệu và vai trò nêu trên.
  2. Mở /admin/absence-approvals.
  3. Đối chiếu kết quả và thực hiện kiểm sau.
- **Kỳ vọng:** Danh sách hiển thị đơn 201 trong tab `Chờ duyệt`.
- **Kiểm sau:** GET /api/v1/residency-declarations?status=PENDING đối chiếu đơn 201.
- **Nguồn:** DEC-09; `tests/00-outline/feature-inventory.md:129`; `frontend/src/screens/admin/ApiAbsenceApprovalsPage.tsx:62`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`.

## Điểm chưa xác định

- Chưa có điểm chưa xác định sau khi đối chiếu mã nguồn.
