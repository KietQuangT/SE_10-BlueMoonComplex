# Báo cáo kiểm thử chi tiết: Khai báo tạm vắng, trở về và phê duyệt

- **Tệp đặc tả nguồn:** [`tests/cases/declarations.md`](../cases/declarations.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra gửi đơn khai báo tạm vắng theo múi giờ nghiệp vụ `Asia/Ho_Chi_Minh`, kiểm tra nhân khẩu thuộc đúng hộ của cư dân, chặn gửi đơn trùng chờ duyệt (`PENDING_CHANGE_EXISTS`), phê duyệt (`APPROVED` chuyển nhân khẩu sang `TEMPORARY_ABSENT`), từ chối (`REJECTED` bắt buộc có lý do và giữ nguyên `RESIDING`), ghi nhận trở về (`RETURN` chuyển về `RESIDING`), và hiển thị trên giao diện.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **21** |
| Số ca Đạt (Pass) | **21** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- Múi giờ kiểm tra ngày hiệu lực (`effectiveDate >= hôm nay`) vận hành chính xác theo `Asia/Ho_Chi_Minh` độc lập với múi giờ UTC của MySQL.
- Khi ghi nhận trở về (`DEC-08.1`), bản ghi `audit_log` lưu hành động `RESIDENT_RETURNED` trên `entity_type = "ResidencyChange"` kèm chi tiết `residentId` và trạng thái chuyển về `RESIDING`.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `DEC-01.1` | Gửi đơn hợp lệ | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-01.2` | Không nhận ngày quá khứ | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-01.3` | Không nhận lý do dài 256 ký tự | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-02.1` | Không tạo đơn PENDING thứ hai | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-02.2` | Không khai cho người đang tạm vắng | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-03.1` | Thấy đơn do người cùng hộ gửi | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-03.2` | Không thấy đơn của hộ khác | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-04.1` | Chủ đơn hủy đơn đang chờ | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-04.2` | Người khác cùng hộ không được hủy | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-04.3` | Không hủy đơn đã quyết định | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-05.1` | Lọc hàng chờ theo trạng thái | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-05.2` | Từ chối trạng thái lọc không hợp lệ | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-05.3` | Từ chối truy vấn tên một ký tự | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-06.1` | Duyệt đơn chờ chuyển nhân khẩu tạm vắng | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-06.2` | Không duyệt lại đơn đã duyệt | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-07.1` | Từ chối đơn với ghi chú | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-07.2` | Không từ chối khi thiếu ghi chú | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-08.1` | Ghi nhận nhân khẩu quay lại | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-08.2` | Không ghi quay lại khi đang cư trú | ADMIN | ✅ Đạt (Pass) | — |
| `DEC-09.1` | Cư dân thấy lý do đơn bị từ chối | RESIDENT | ✅ Đạt (Pass) | — |
| `DEC-09.2` | Quản trị xem hàng chờ | ADMIN | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### DEC-01.1 — Gửi đơn hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<NGAY_MAI>","endDate":null,"reason":"Cong tac"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi body tới endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 201; `status=PENDING`, `residentId=101`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine tìm đơn và audit `ABSENCE_REQUESTED`.
  - **Nguồn đối chiếu:** DEC-01; `tests/00-outline/feature-inventory.md:121`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201; id=5, status=PENDING, residentId=4; audit ABSENCE_REQUESTED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-01.2 — Không nhận ngày quá khứ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<HOM_QUA>","reason":"Cong tac"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi body với ngày hôm qua.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=INVALID_DATE`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine xác nhận không có đơn mới.
  - **Nguồn đối chiếu:** DEC-01; `tests/00-outline/feature-inventory.md:121`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:18`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_DATE; mine count=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-01.3 — Không nhận lý do dài 256 ký tự

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body như ca DEC-01.1 với `reason` là chuỗi gồm đúng 256 ký tự `a`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi body với lý do 256 ký tự.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=VALIDATION`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine xác nhận không có đơn mới.
  - **Nguồn đối chiếu:** DEC-01; `tests/00-outline/feature-inventory.md:121`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:40`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=VALIDATION; mine count=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-02.1 — Không tạo đơn PENDING thứ hai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Nhân khẩu 101 thuộc hộ A, RESIDING; ngày bắt đầu là ngày mai tại Việt Nam, đã có một đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<NGAY_MAI>","endDate":null,"reason":"Cong tac"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi lại body cho nhân khẩu 101.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=DECLARATION_ALREADY_PENDING`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine xác nhận chỉ có một đơn PENDING của 101.
  - **Nguồn đối chiếu:** DEC-02; `tests/00-outline/feature-inventory.md:122`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:23`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=DECLARATION_ALREADY_PENDING; pendingCount=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-02.2 — Không khai cho người đang tạm vắng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Nhân khẩu 101 thuộc hộ A, TEMPORARILY_ABSENT; ngày bắt đầu là ngày mai tại Việt Nam, không có đơn PENDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`; body `{"residentId":101,"effectiveDate":"<NGAY_MAI>","endDate":null,"reason":"Cong tac"}`.
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi body cho nhân khẩu 101.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=RESIDENT_NOT_AVAILABLE`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine xác nhận không có đơn mới.
  - **Nguồn đối chiếu:** DEC-02; `tests/00-outline/feature-inventory.md:122`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:21`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=RESIDENT_NOT_AVAILABLE
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-03.1 — Thấy đơn do người cùng hộ gửi

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations/mine`.
  - **Dữ liệu đầu vào:** Hộ A có đơn 201 do tài khoản RESIDENT khác của cùng hộ gửi..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint khi đăng nhập tài khoản RESIDENT thuộc hộ A.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `content` chứa đơn 201 của hộ A.
  - **Kiểm tra sau (Post-check):** Đối chiếu `householdId` của đơn 201 với hộ A.
  - **Nguồn đối chiếu:** DEC-03; `tests/00-outline/feature-inventory.md:123`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:30`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; foundId=5, householdId=6 (expected=6)
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-03.2 — Không thấy đơn của hộ khác

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations/mine`.
  - **Dữ liệu đầu vào:** Hộ B có đơn 202; tài khoản hiện tại thuộc hộ A..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint khi đăng nhập tài khoản hộ A.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `content` không chứa đơn 202.
  - **Kiểm tra sau (Post-check):** Đối chiếu `householdId` các dòng trả về đều thuộc hộ A.
  - **Nguồn đối chiếu:** DEC-03; `tests/00-outline/feature-inventory.md:123`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:30`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; containsHouseholdBDec(6)=false, allMatchHhA=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-04.1 — Chủ đơn hủy đơn đang chờ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/201/cancel`.
  - **Dữ liệu đầu vào:** Đơn 201 PENDING do tài khoản hiện tại gửi; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `status=CANCELLED`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine vẫn chứa đơn 201; audit `ABSENCE_CANCELLED`.
  - **Nguồn đối chiếu:** DEC-04; `tests/00-outline/feature-inventory.md:124`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:26`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; status=CANCELLED; audit ABSENCE_CANCELLED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-04.2 — Người khác cùng hộ không được hủy

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/201/cancel`.
  - **Dữ liệu đầu vào:** Đơn 201 PENDING do tài khoản khác cùng hộ gửi; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST bằng tài khoản không tạo đơn.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 403.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine xác nhận đơn 201 vẫn PENDING.
  - **Nguồn đối chiếu:** DEC-04; `tests/00-outline/feature-inventory.md:124`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:26`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403; status sau thử hủy=PENDING
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-04.3 — Không hủy đơn đã quyết định

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/201/cancel`.
  - **Dữ liệu đầu vào:** Đơn 201 APPROVED do tài khoản hiện tại gửi; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=DECLARATION_NOT_PENDING`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine xác nhận đơn vẫn APPROVED.
  - **Nguồn đối chiếu:** DEC-04; `tests/00-outline/feature-inventory.md:124`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=DECLARATION_NOT_PENDING
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-05.1 — Lọc hàng chờ theo trạng thái

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations?status=PENDING`.
  - **Dữ liệu đầu vào:** Có đơn 201 PENDING và đơn 202 REJECTED..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint với tham số status=PENDING.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `content` chứa 201, không chứa 202.
  - **Kiểm tra sau (Post-check):** Đối chiếu mọi dòng có `status=PENDING` và `changeType=TEMPORARY_ABSENCE`.
  - **Nguồn đối chiếu:** DEC-05; `tests/00-outline/feature-inventory.md:125`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; hasPending(7)=true, hasRejected(8)=false
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-05.2 — Từ chối trạng thái lọc không hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations?status=UNKNOWN`.
  - **Dữ liệu đầu vào:** Tham số `status=UNKNOWN`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=INVALID_FILTER`.
  - **Kiểm tra sau (Post-check):** Gọi GET /api/v1/residency-declarations?status=PENDING xác nhận hàng chờ còn đọc được.
  - **Nguồn đối chiếu:** DEC-05; `tests/00-outline/feature-inventory.md:125`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_FILTER; kiểm sau HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-05.3 — Từ chối truy vấn tên một ký tự

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations?q=a`.
  - **Dữ liệu đầu vào:** Tham số `q=a`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gọi endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=INVALID_FILTER`.
  - **Kiểm tra sau (Post-check):** Gọi GET /api/v1/residency-declarations?status=PENDING xác nhận dữ liệu không đổi.
  - **Nguồn đối chiếu:** DEC-05; `tests/00-outline/feature-inventory.md:125`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:42`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_FILTER; kiểm sau HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-06.1 — Duyệt đơn chờ chuyển nhân khẩu tạm vắng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/201/approve`.
  - **Dữ liệu đầu vào:** Đơn 201 PENDING của nhân khẩu 101 RESIDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; đơn `status=APPROVED`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residents/101 có `residencyStatus=TEMPORARILY_ABSENT`; audit `ABSENCE_APPROVED`.
  - **Nguồn đối chiếu:** DEC-06; `tests/00-outline/feature-inventory.md:126`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:27`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; status=APPROVED; residentStatus=TEMPORARILY_ABSENT; audit ABSENCE_APPROVED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-06.2 — Không duyệt lại đơn đã duyệt

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/201/approve`.
  - **Dữ liệu đầu vào:** Đơn 201 đã APPROVED; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=DECLARATION_NOT_PENDING`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations?status=APPROVED xác nhận đơn 201 không đổi.
  - **Nguồn đối chiếu:** DEC-06; `tests/00-outline/feature-inventory.md:126`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:38`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=DECLARATION_NOT_PENDING
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-07.1 — Từ chối đơn với ghi chú

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/201/reject`.
  - **Dữ liệu đầu vào:** Đơn 201 PENDING của nhân khẩu 101 RESIDING; body `{"decisionNote":"Thieu thong tin"}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `status=REJECTED`, `decisionNote=Thieu thong tin`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residents/101 vẫn RESIDING; audit `ABSENCE_REJECTED`.
  - **Nguồn đối chiếu:** DEC-07; `tests/00-outline/feature-inventory.md:127`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:28`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; status=REJECTED, decisionNote="Thieu thong tin"; residentStatus=RESIDING; audit ABSENCE_REJECTED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-07.2 — Không từ chối khi thiếu ghi chú

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/201/reject`.
  - **Dữ liệu đầu vào:** Đơn 201 PENDING; body `{}`; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST cùng body.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 400, `code=VALIDATION`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations?status=PENDING xác nhận đơn 201 còn PENDING.
  - **Nguồn đối chiếu:** DEC-07; `tests/00-outline/feature-inventory.md:127`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:40`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=VALIDATION; đơn 8 vẫn PENDING=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-08.1 — Ghi nhận nhân khẩu quay lại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/101/return`.
  - **Dữ liệu đầu vào:** Nhân khẩu 101 TEMPORARILY_ABSENT; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200; `residencyStatus=RESIDING`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/households/1/residency-changes thấy `RETURN` với ngày hôm nay theo giờ Việt Nam; audit `RESIDENT_RETURNED`.
  - **Nguồn đối chiếu:** DEC-08; `tests/00-outline/feature-inventory.md:128`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:41`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; residencyStatus=RESIDING; l?ch s? h? c? b?n ghi RETURN ng?y h?m nay (Asia/Ho_Chi_Minh); c? audit RESIDENT_RETURNED tr?n ResidencyChange
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-08.2 — Không ghi quay lại khi đang cư trú

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/101/return`.
  - **Dữ liệu đầu vào:** Nhân khẩu 101 RESIDING; `X-CSRF-TOKEN: <CSRF_TOKEN>`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Gửi POST đến endpoint.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409, `code=RESIDENT_NOT_ABSENT`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residents/101 xác nhận vẫn RESIDING và không thêm lịch sử RETURN.
  - **Nguồn đối chiếu:** DEC-08; `tests/00-outline/feature-inventory.md:128`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:29`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=RESIDENT_NOT_ABSENT; residencyStatus=RESIDING
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-09.1 — Cư dân thấy lý do đơn bị từ chối

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/resident/absence`.
  - **Dữ liệu đầu vào:** Chế độ api; hộ hiện tại có đơn REJECTED với `decisionNote=Thieu thong tin`..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Mở /resident/absence.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** Danh sách hiển thị `Lý do từ chối: Thieu thong tin`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations/mine đối chiếu `status` và `decisionNote`.
  - **Nguồn đối chiếu:** DEC-09; `tests/00-outline/feature-inventory.md:129`; `frontend/src/screens/resident/ApiAbsencePage.tsx:72`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:33`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Mở /resident/absence: hiển thị "Lý do từ chối: Thieu thong tin"=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### DEC-09.2 — Quản trị xem hàng chờ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/absence-approvals`.
  - **Dữ liệu đầu vào:** Chế độ api; đơn 201 đang PENDING..
  - **Các bước thực hiện:**
    1. Xác nhận dữ liệu và vai trò nêu trên.
    2. Mở /admin/absence-approvals.
    3. Đối chiếu kết quả và thực hiện kiểm sau.
  - **Kết quả mong đợi:** Danh sách hiển thị đơn 201 trong tab `Chờ duyệt`.
  - **Kiểm tra sau (Post-check):** GET /api/v1/residency-declarations?status=PENDING đối chiếu đơn 201.
  - **Nguồn đối chiếu:** DEC-09; `tests/00-outline/feature-inventory.md:129`; `frontend/src/screens/admin/ApiAbsenceApprovalsPage.tsx:62`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/DeclarationService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Mở /admin/absence-approvals: tab "Chờ duyệt" hiển thị đơn PENDING (id=10)=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
