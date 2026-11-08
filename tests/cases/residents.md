# Ca kiểm thử residents

### RES-01.1 — Thêm nhân khẩu hợp lệ

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households/{id}/residents`.
- **Dữ liệu vào:** Hộ `<HOUSEHOLD_ID>` ACTIVE; JSON `{"fullName":"Test Person","identityNumber":"<TEST_IDENTITY_NUMBER>","dateOfBirth":"1990-01-01","phone":"<TEST_PHONE>","relationshipToHead":"Con"}`; các giá trị định danh chỉ dùng cho thử nghiệm.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `/api/v1/households/<HOUSEHOLD_ID>/residents` với token.
  3. Lưu ID nhân khẩu từ response.
- **Kỳ vọng:** HTTP 201; `residencyStatus=RESIDING`, `householdId=<HOUSEHOLD_ID>`.
- **Kiểm sau:** GET `/api/v1/residents/<RESIDENT_ID>`; có audit `RESIDENT_CREATED`.
- **Nguồn:** RES-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-01.2 — Từ chối giấy tờ đã dùng bởi nhân khẩu hiện cư trú

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households/{id}/residents`.
- **Dữ liệu vào:** Đã có nhân khẩu RESIDING dùng `<TEST_IDENTITY_NUMBER>`; JSON `{"fullName":"Other Test","identityNumber":"<TEST_IDENTITY_NUMBER>","dateOfBirth":"1991-01-01"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `/api/v1/households/<HOUSEHOLD_ID>/residents` với token.
  3. Đọc response đã che dữ liệu.
- **Kỳ vọng:** HTTP 409; `code=IDENTITY_NUMBER_ALREADY_USED`; lỗi không chứa số giấy tờ.
- **Kiểm sau:** GET danh sách hộ: không thêm nhân khẩu `Other Test`.
- **Nguồn:** RES-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-01.3 — Từ chối ngày sinh trong tương lai

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households/{id}/residents`.
- **Dữ liệu vào:** JSON `{"fullName":"Future Test","dateOfBirth":"<TOMORROW_YYYY-MM-DD>"}`; ngày mai theo múi giờ Việt Nam.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `/api/v1/households/<HOUSEHOLD_ID>/residents` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 400; `code=INVALID_RESIDENT`.
- **Kiểm sau:** GET danh sách hộ: không có `Future Test`.
- **Nguồn:** RES-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-02.1 — Lọc nhân khẩu theo trạng thái

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/{id}/residents?status=MOVED_OUT&page=0&size=10`.
- **Dữ liệu vào:** Hộ có một nhân khẩu MOVED_OUT và một RESIDING.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `/api/v1/households/<HOUSEHOLD_ID>/residents?status=MOVED_OUT&page=0&size=10`.
  3. Đọc danh sách.
- **Kỳ vọng:** HTTP 200; chỉ có nhân khẩu MOVED_OUT của hộ.
- **Kiểm sau:** Đối chiếu `residencyStatus` của từng phần tử.
- **Nguồn:** RES-02; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-02.2 — Cư dân bị từ chối danh sách quản trị

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/{id}/residents`.
- **Dữ liệu vào:** Phiên RESIDENT thuộc hộ `<HOUSEHOLD_ID>`.
- **Các bước:**
  1. Đăng nhập RESIDENT đã đổi mật khẩu.
  2. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residents`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 403; không trả danh sách chứa giấy tờ hoặc số điện thoại.
- **Kiểm sau:** Gọi `/api/v1/households/me/residents` để kiểm dữ liệu của chính hộ.
- **Nguồn:** RES-02; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.

### RES-03.1 — Xem thông tin nhân khẩu

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/{id}`.
- **Dữ liệu vào:** Nhân khẩu `<RESIDENT_ID>` có `fullName=Test Person`.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/residents/<RESIDENT_ID>`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 200; `id=<RESIDENT_ID>` và `fullName=Test Person`.
- **Kiểm sau:** Đối chiếu hộ của nhân khẩu qua `householdId`.
- **Nguồn:** RES-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-03.2 — Sửa tên nhân khẩu

- **Vai trò:** ADMIN.
- **Điểm vào:** `PATCH /api/v1/residents/{id}`.
- **Dữ liệu vào:** Nhân khẩu `<RESIDENT_ID>` tồn tại; JSON `{"fullName":"Test Person Updated"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi PATCH `/api/v1/residents/<RESIDENT_ID>` với JSON và token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 200; `fullName=Test Person Updated`.
- **Kiểm sau:** GET lại nhân khẩu; có audit `RESIDENT_UPDATED`.
- **Nguồn:** RES-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-04.1 — Chuyển đi hủy đơn đang chờ

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/{id}/move-out`.
- **Dữ liệu vào:** Nhân khẩu `<RESIDENT_ID>` RESIDING có hai đơn tạm vắng PENDING; JSON `{"effectiveDate":"<TODAY_YYYY-MM-DD>","reason":"Chuyen di thu nghiem"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi POST `/api/v1/residents/<RESIDENT_ID>/move-out` với JSON và token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 200; `residencyStatus=MOVED_OUT`.
- **Kiểm sau:** GET lại nhân khẩu và lịch sử hộ; hai đơn thành CANCELLED, mỗi đơn có audit `ABSENCE_CANCELLED`; tài khoản và hộ không tự đổi trạng thái.
- **Nguồn:** RES-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-04.2 — Từ chối chuyển đi lần hai

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/residents/{id}/move-out`.
- **Dữ liệu vào:** Nhân khẩu `<RESIDENT_ID>` đã MOVED_OUT; JSON `{"effectiveDate":"<TODAY_YYYY-MM-DD>"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi POST `/api/v1/residents/<RESIDENT_ID>/move-out` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 409; `code=RESIDENT_ALREADY_MOVED_OUT`.
- **Kiểm sau:** GET lịch sử hộ: không thêm dòng MOVE_OUT thứ hai.
- **Nguồn:** RES-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-05.1 — Lịch sử có bản ghi chuyển đi

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/{id}/residency-changes?page=0&size=20`.
- **Dữ liệu vào:** Hộ `<HOUSEHOLD_ID>` có nhân khẩu vừa chuyển đi.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes?page=0&size=20`.
  3. Đọc các bản ghi.
- **Kỳ vọng:** HTTP 200; có bản ghi `changeType=MOVE_OUT` cho nhân khẩu đã chuyển.
- **Kiểm sau:** Đối chiếu ID nhân khẩu và ngày hiệu lực với thao tác chuyển đi.
- **Nguồn:** RES-05; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`.

### RES-05.2 — Lịch sử hộ không tồn tại

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/{id}/residency-changes`.
- **Dữ liệu vào:** `<MISSING_HOUSEHOLD_ID>` không tồn tại.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/households/<MISSING_HOUSEHOLD_ID>/residency-changes`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 404; `code=HOUSEHOLD_NOT_FOUND`.
- **Kiểm sau:** SQL xác nhận không có hộ với ID đó.
- **Nguồn:** RES-05; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-06.1 — Cư dân xem thành viên hộ của mình

- **Vai trò:** RESIDENT.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** RESIDENT thuộc hộ A; hộ A có `Test Person`, hộ B có `Other Person`.
- **Các bước:**
  1. Đăng nhập RESIDENT thuộc hộ A.
  2. Gọi `GET /api/v1/households/me/residents`.
  3. Đọc từng phần tử.
- **Kỳ vọng:** HTTP 200; có `Test Person`, không có `Other Person`; không có `identityNumber` hoặc `phone`.
- **Kiểm sau:** Đối chiếu danh sách với dữ liệu hộ A qua phiên ADMIN.
- **Nguồn:** RES-06; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/ResidentService.java:17`.

### RES-06.2 — ADMIN không được gọi endpoint cư dân

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/me/residents`.
- **Dữ liệu vào:** Phiên ADMIN đã đổi mật khẩu.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/households/me/residents`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 403; không có danh sách thành viên.
- **Kiểm sau:** Gọi endpoint ADMIN `/api/v1/households/<HOUSEHOLD_ID>/residents` để kiểm quyền quản trị.
- **Nguồn:** RES-06; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.

### RES-07.1 — Gợi ý theo tên hợp lệ

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=Test`.
- **Dữ liệu vào:** Có nhân khẩu `Test Person` RESIDING và `Test Former` MOVED_OUT.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/residents/suggest?q=Test`.
  3. Đọc mảng response.
- **Kỳ vọng:** HTTP 200; tối đa 10 kết quả, có `Test Person`, không có `Test Former`; không có số giấy tờ hoặc điện thoại.
- **Kiểm sau:** Đối chiếu trạng thái từng kết quả bằng GET nhân khẩu.
- **Nguồn:** RES-07; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

### RES-07.2 — Từ chối từ khóa một ký tự

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/residents/suggest?q=T`.
- **Dữ liệu vào:** Query `q=T`.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/residents/suggest?q=T`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 400; `code=INVALID_FILTER`.
- **Kiểm sau:** Gọi lại `GET /api/v1/residents/suggest?q=Test`: HTTP 200.
- **Nguồn:** RES-07; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`.

## Điểm chưa xác định

Không có điểm chưa xác định trong các ca hiện tại.
