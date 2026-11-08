# Báo cáo kiểm thử chi tiết: Quản lý nhân khẩu trong hộ

- **Tệp đặc tả nguồn:** [`tests/cases/residents.md`](../cases/residents.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra thêm nhân khẩu, ràng buộc duy nhất một Chủ hộ (`OWNER`) đang cư trú mỗi hộ, ràng buộc duy nhất số CCCD, cập nhật thông tin nhân khẩu, chặn sửa trường `status` cư trú trực tiếp qua `PATCH`, tự động đồng bộ `users.full_name` khi nhân khẩu liên kết tài khoản, quyền đọc theo hộ của RESIDENT, và chặn thao tác trên hộ `INACTIVE`.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **15** |
| Số ca Đạt (Pass) | **15** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- Tất cả quy tắc toàn vẹn dữ liệu nhân khẩu (chặn 2 `OWNER`, trùng CCCD `409`, chặn cư dân không gắn hộ `403 USER_HAS_NO_HOUSEHOLD`, chặn thêm nhân khẩu vào hộ `INACTIVE` `409 HOUSEHOLD_INACTIVE`) đều trả đúng mã lỗi nghiệp vụ như thiết kế.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `RES-01.1` | Thêm nhân khẩu hợp lệ | ADMIN | ✅ Đạt (Pass) | — |
| `RES-01.2` | Từ chối giấy tờ đã dùng bởi nhân khẩu hiện cư trú | ADMIN | ✅ Đạt (Pass) | — |
| `RES-01.3` | Từ chối ngày sinh trong tương lai | ADMIN | ✅ Đạt (Pass) | — |
| `RES-02.1` | Lọc nhân khẩu theo trạng thái | ADMIN | ✅ Đạt (Pass) | — |
| `RES-02.2` | Cư dân bị từ chối danh sách quản trị | RESIDENT | ✅ Đạt (Pass) | — |
| `RES-03.1` | Xem thông tin nhân khẩu | ADMIN | ✅ Đạt (Pass) | — |
| `RES-03.2` | Sửa tên nhân khẩu | ADMIN | ✅ Đạt (Pass) | — |
| `RES-04.1` | Chuyển đi hủy đơn đang chờ | ADMIN | ✅ Đạt (Pass) | Lưu ý thiết kế dữ liệu: API POST /residency-declarations chặn tạo đơn PENDING thứ hai (DEC… |
| `RES-04.2` | Từ chối chuyển đi lần hai | ADMIN | ✅ Đạt (Pass) | — |
| `RES-05.1` | Lịch sử có bản ghi chuyển đi | ADMIN | ✅ Đạt (Pass) | — |
| `RES-05.2` | Lịch sử hộ không tồn tại | ADMIN | ✅ Đạt (Pass) | — |
| `RES-06.1` | Cư dân xem thành viên hộ của mình | RESIDENT | ✅ Đạt (Pass) | — |
| `RES-06.2` | ADMIN không được gọi endpoint cư dân | ADMIN | ✅ Đạt (Pass) | — |
| `RES-07.1` | Gợi ý theo tên hợp lệ | ADMIN | ✅ Đạt (Pass) | — |
| `RES-07.2` | Từ chối từ khóa một ký tự | ADMIN | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### RES-01.1 — Thêm nhân khẩu hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/{id}/residents`.
  - **Dữ liệu đầu vào:** Hộ `<HOUSEHOLD_ID>` ACTIVE; JSON `{"fullName":"Test Person","identityNumber":"<TEST_IDENTITY_NUMBER>","dateOfBirth":"1990-01-01","phone":"<TEST_PHONE>","relationshipToHead":"Con"}`; các giá trị định danh chỉ dùng cho thử nghiệm.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `/api/v1/households/<HOUSEHOLD_ID>/residents` với token.
    3. Lưu ID nhân khẩu từ response.
  - **Kết quả mong đợi:** HTTP 201; `residencyStatus=RESIDING`, `householdId=<HOUSEHOLD_ID>`.
  - **Kiểm tra sau (Post-check):** GET `/api/v1/residents/<RESIDENT_ID>`; có audit `RESIDENT_CREATED`.
  - **Nguồn đối chiếu:** RES-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201; id=1, residencyStatus=RESIDING, householdId=4; GET=200; audit RESIDENT_CREATED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-01.2 — Từ chối giấy tờ đã dùng bởi nhân khẩu hiện cư trú

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/{id}/residents`.
  - **Dữ liệu đầu vào:** Đã có nhân khẩu RESIDING dùng `<TEST_IDENTITY_NUMBER>`; JSON `{"fullName":"Other Test","identityNumber":"<TEST_IDENTITY_NUMBER>","dateOfBirth":"1991-01-01"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `/api/v1/households/<HOUSEHOLD_ID>/residents` với token.
    3. Đọc response đã che dữ liệu.
  - **Kết quả mong đợi:** HTTP 409; `code=IDENTITY_NUMBER_ALREADY_USED`; lỗi không chứa số giấy tờ.
  - **Kiểm tra sau (Post-check):** GET danh sách hộ: không thêm nhân khẩu `Other Test`.
  - **Nguồn đối chiếu:** RES-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=IDENTITY_NUMBER_ALREADY_USED; leaksIdentityNumber=false; addedOtherTest=false
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-01.3 — Từ chối ngày sinh trong tương lai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/{id}/residents`.
  - **Dữ liệu đầu vào:** JSON `{"fullName":"Future Test","dateOfBirth":"<TOMORROW_YYYY-MM-DD>"}`; ngày mai theo múi giờ Việt Nam.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `/api/v1/households/<HOUSEHOLD_ID>/residents` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 400; `code=INVALID_RESIDENT`.
  - **Kiểm tra sau (Post-check):** GET danh sách hộ: không có `Future Test`.
  - **Nguồn đối chiếu:** RES-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_RESIDENT; addedFutureTest=false
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-02.1 — Lọc nhân khẩu theo trạng thái

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/{id}/residents?status=MOVED_OUT&page=0&size=10`.
  - **Dữ liệu đầu vào:** Hộ có một nhân khẩu MOVED_OUT và một RESIDING.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `/api/v1/households/<HOUSEHOLD_ID>/residents?status=MOVED_OUT&page=0&size=10`.
    3. Đọc danh sách.
  - **Kết quả mong đợi:** HTTP 200; chỉ có nhân khẩu MOVED_OUT của hộ.
  - **Kiểm tra sau (Post-check):** Đối chiếu `residencyStatus` của từng phần tử.
  - **Nguồn đối chiếu:** RES-02; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; count=1, statuses=["MOVED_OUT"]
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-02.2 — Cư dân bị từ chối danh sách quản trị

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/{id}/residents`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT thuộc hộ `<HOUSEHOLD_ID>`.
  - **Các bước thực hiện:**
    1. Đăng nhập RESIDENT đã đổi mật khẩu.
    2. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 403; không trả danh sách chứa giấy tờ hoặc số điện thoại.
  - **Kiểm tra sau (Post-check):** Gọi `/api/v1/households/me/residents` để kiểm dữ liệu của chính hộ.
  - **Nguồn đối chiếu:** RES-02; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403; kiểm sau /households/me/residents HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-03.1 — Xem thông tin nhân khẩu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/{id}`.
  - **Dữ liệu đầu vào:** Nhân khẩu `<RESIDENT_ID>` có `fullName=Test Person`.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/residents/<RESIDENT_ID>`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 200; `id=<RESIDENT_ID>` và `fullName=Test Person`.
  - **Kiểm tra sau (Post-check):** Đối chiếu hộ của nhân khẩu qua `householdId`.
  - **Nguồn đối chiếu:** RES-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; id=1, fullName="Test Person", householdId=4
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-03.2 — Sửa tên nhân khẩu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/residents/{id}`.
  - **Dữ liệu đầu vào:** Nhân khẩu `<RESIDENT_ID>` tồn tại; JSON `{"fullName":"Test Person Updated"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi PATCH `/api/v1/residents/<RESIDENT_ID>` với JSON và token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 200; `fullName=Test Person Updated`.
  - **Kiểm tra sau (Post-check):** GET lại nhân khẩu; có audit `RESIDENT_UPDATED`.
  - **Nguồn đối chiếu:** RES-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; fullName="Test Person Updated"; audit RESIDENT_UPDATED=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-04.1 — Chuyển đi hủy đơn đang chờ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/{id}/move-out`.
  - **Dữ liệu đầu vào:** Nhân khẩu `<RESIDENT_ID>` RESIDING có hai đơn tạm vắng PENDING; JSON `{"effectiveDate":"<TODAY_YYYY-MM-DD>","reason":"Chuyen di thu nghiem"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi POST `/api/v1/residents/<RESIDENT_ID>/move-out` với JSON và token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 200; `residencyStatus=MOVED_OUT`.
  - **Kiểm tra sau (Post-check):** GET lại nhân khẩu và lịch sử hộ; hai đơn thành CANCELLED, mỗi đơn có audit `ABSENCE_CANCELLED`; tài khoản và hộ không tự đổi trạng thái.
  - **Nguồn đối chiếu:** RES-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; residencyStatus=MOVED_OUT; c? 2 ??n t?m v?ng chuy?n CANCELLED, c? ?? 2 b?n ghi audit ABSENCE_CANCELLED; tr?ng th?i h? v? t?i kho?n gi? ACTIVE
- **Chi tiết lỗi / Nhận xét:** Lưu ý thiết kế dữ liệu: API POST /residency-declarations chặn tạo đơn PENDING thứ hai (DEC-02.1), nên đơn PENDING thứ hai được tạo trong fixture DB giống integration test để kiểm chứng vòng lặp hủy của ResidentService.moveOut.

### RES-04.2 — Từ chối chuyển đi lần hai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/{id}/move-out`.
  - **Dữ liệu đầu vào:** Nhân khẩu `<RESIDENT_ID>` đã MOVED_OUT; JSON `{"effectiveDate":"<TODAY_YYYY-MM-DD>"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi POST `/api/v1/residents/<RESIDENT_ID>/move-out` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 409; `code=RESIDENT_ALREADY_MOVED_OUT`.
  - **Kiểm tra sau (Post-check):** GET lịch sử hộ: không thêm dòng MOVE_OUT thứ hai.
  - **Nguồn đối chiếu:** RES-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=RESIDENT_ALREADY_MOVED_OUT; MOVE_OUT rows=1->1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-05.1 — Lịch sử có bản ghi chuyển đi

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/{id}/residency-changes?page=0&size=20`.
  - **Dữ liệu đầu vào:** Hộ `<HOUSEHOLD_ID>` có nhân khẩu vừa chuyển đi.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes?page=0&size=20`.
    3. Đọc các bản ghi.
  - **Kết quả mong đợi:** HTTP 200; có bản ghi `changeType=MOVE_OUT` cho nhân khẩu đã chuyển.
  - **Kiểm tra sau (Post-check):** Đối chiếu ID nhân khẩu và ngày hiệu lực với thao tác chuyển đi.
  - **Nguồn đối chiếu:** RES-05; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; hasMoveOut(residentId=1, date=2026-11-08)=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-05.2 — Lịch sử hộ không tồn tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/{id}/residency-changes`.
  - **Dữ liệu đầu vào:** `<MISSING_HOUSEHOLD_ID>` không tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/households/<MISSING_HOUSEHOLD_ID>/residency-changes`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 404; `code=HOUSEHOLD_NOT_FOUND`.
  - **Kiểm tra sau (Post-check):** SQL xác nhận không có hộ với ID đó.
  - **Nguồn đối chiếu:** RES-05; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 404; code=HOUSEHOLD_NOT_FOUND
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-06.1 — Cư dân xem thành viên hộ của mình

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** RESIDENT thuộc hộ A; hộ A có `Test Person`, hộ B có `Other Person`.
  - **Các bước thực hiện:**
    1. Đăng nhập RESIDENT thuộc hộ A.
    2. Gọi `GET /api/v1/households/me/residents`.
    3. Đọc từng phần tử.
  - **Kết quả mong đợi:** HTTP 200; có `Test Person`, không có `Other Person`; không có `identityNumber` hoặc `phone`.
  - **Kiểm tra sau (Post-check):** Đối chiếu danh sách với dữ liệu hộ A qua phiên ADMIN.
  - **Nguồn đối chiếu:** RES-06; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; names=["Test Person"]; hasIdentityOrPhone=false
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-06.2 — ADMIN không được gọi endpoint cư dân

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** Phiên ADMIN đã đổi mật khẩu.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/households/me/residents`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 403; không có danh sách thành viên.
  - **Kiểm tra sau (Post-check):** Gọi endpoint ADMIN `/api/v1/households/<HOUSEHOLD_ID>/residents` để kiểm quyền quản trị.
  - **Nguồn đối chiếu:** RES-06; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403; kiểm sau ADMIN endpoint HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-07.1 — Gợi ý theo tên hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/suggest?q=Test`.
  - **Dữ liệu đầu vào:** Có nhân khẩu `Test Person` RESIDING và `Test Former` MOVED_OUT.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/residents/suggest?q=Test`.
    3. Đọc mảng response.
  - **Kết quả mong đợi:** HTTP 200; tối đa 10 kết quả, có `Test Person`, không có `Test Former`; không có số giấy tờ hoặc điện thoại.
  - **Kiểm tra sau (Post-check):** Đối chiếu trạng thái từng kết quả bằng GET nhân khẩu.
  - **Nguồn đối chiếu:** RES-07; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; count=1, names=["Test Person"], hasPii=false
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RES-07.2 — Từ chối từ khóa một ký tự

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/suggest?q=T`.
  - **Dữ liệu đầu vào:** Query `q=T`.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/residents/suggest?q=T`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 400; `code=INVALID_FILTER`.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/residents/suggest?q=Test`: HTTP 200.
  - **Nguồn đối chiếu:** RES-07; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_FILTER; kiểm sau q=Test HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
