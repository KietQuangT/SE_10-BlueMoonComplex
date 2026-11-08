# Ca kiểm thử households

### HH-01.1 — Tạo hộ với diện tích hợp lệ

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** JSON `{"householdCode":"HH-TEST-01","apartmentCode":"APT-TEST-01","areaM2":65.50,"address":"Dia chi thu nghiem"}`; hai mã chưa tồn tại.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `POST /api/v1/households` với token.
  3. Lưu `id` response.
- **Kỳ vọng:** HTTP 201; response có `householdCode=HH-TEST-01`, `apartmentCode=APT-TEST-01`, `areaM2=65.50`.
- **Kiểm sau:** Gọi `GET /api/v1/households/<ID>`; có audit `HOUSEHOLD_CREATED`.
- **Nguồn:** HH-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.

### HH-01.2 — Từ chối diện tích quá hai chữ số thập phân

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** JSON `{"householdCode":"HH-TEST-02","apartmentCode":"APT-TEST-02","areaM2":65.123,"address":"Dia chi thu nghiem"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `POST /api/v1/households` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 400; code xác thực cụ thể chưa xác định.
- **Kiểm sau:** Tìm `householdCode=HH-TEST-02` bằng GET danh sách: không có hộ mới.
- **Nguồn:** HH-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### HH-01.3 — Từ chối mã hộ trùng

- **Vai trò:** ADMIN.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Đã có `HH-TEST-01`; JSON `{"householdCode":"HH-TEST-01","apartmentCode":"APT-TEST-03","areaM2":50.00}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi JSON tới `POST /api/v1/households` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 409; `code=HOUSEHOLD_CONFLICT`.
- **Kiểm sau:** Lọc `GET /api/v1/households?householdCode=HH-TEST-01`: không có hộ thứ hai.
- **Nguồn:** HH-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.

### HH-02.1 — Lọc danh sách theo mã căn hộ

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households?apartmentCode=APT-TEST-01&page=0&size=10`.
- **Dữ liệu vào:** Có hộ `APT-TEST-01` và hộ `APT-OTHER`.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/households?apartmentCode=APT-TEST-01&page=0&size=10`.
  3. Đọc danh sách response.
- **Kỳ vọng:** HTTP 200; kết quả có hộ `APT-TEST-01` và không có `APT-OTHER`.
- **Kiểm sau:** Đối chiếu mã căn hộ của từng phần tử trong trang.
- **Nguồn:** HH-02; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### HH-02.2 — Từ chối kích thước trang bằng không

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households?page=0&size=0`.
- **Dữ liệu vào:** Query `page=0&size=0`.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/households?page=0&size=0`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 400; `code=INVALID_FILTER`.
- **Kiểm sau:** Gọi lại với `size=10`: HTTP 200.
- **Nguồn:** HH-02; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.

### HH-03.1 — Xem hộ đã tồn tại

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/{id}`.
- **Dữ liệu vào:** `<HOUSEHOLD_ID>` là ID hộ `HH-TEST-01`.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 200; `id=<HOUSEHOLD_ID>` và `householdCode=HH-TEST-01`.
- **Kiểm sau:** Đối chiếu mã căn hộ và diện tích với bản ghi hộ.
- **Nguồn:** HH-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.

### HH-03.2 — Hộ không tồn tại trả lỗi

- **Vai trò:** ADMIN.
- **Điểm vào:** `GET /api/v1/households/{id}`.
- **Dữ liệu vào:** `<MISSING_HOUSEHOLD_ID>` không có trong DB.
- **Các bước:**
  1. Đăng nhập ADMIN.
  2. Gọi `GET /api/v1/households/<MISSING_HOUSEHOLD_ID>`.
  3. Đọc response.
- **Kỳ vọng:** HTTP 404; `code=HOUSEHOLD_NOT_FOUND`.
- **Kiểm sau:** SQL xác nhận ID không tồn tại.
- **Nguồn:** HH-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.

### HH-04.1 — Cập nhật diện tích và địa chỉ

- **Vai trò:** ADMIN.
- **Điểm vào:** `PATCH /api/v1/households/{id}`.
- **Dữ liệu vào:** Hộ `<HOUSEHOLD_ID>` có mã `HH-TEST-01`; JSON `{"areaM2":70.25,"address":"Dia chi moi thu nghiem"}`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi PATCH tới `/api/v1/households/<HOUSEHOLD_ID>` với JSON và token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 200; `areaM2=70.25`, địa chỉ mới; mã hộ không đổi.
- **Kiểm sau:** GET lại hộ; audit `HOUSEHOLD_UPDATED` không chứa giá trị địa chỉ.
- **Nguồn:** HH-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.

### HH-04.2 — Từ chối cập nhật rỗng

- **Vai trò:** ADMIN.
- **Điểm vào:** `PATCH /api/v1/households/{id}`.
- **Dữ liệu vào:** Hộ `<HOUSEHOLD_ID>` tồn tại; body `{}`.
- **Các bước:**
  1. Ghi lại diện tích và địa chỉ hiện tại qua GET.
  2. Lấy CSRF trong phiên ADMIN.
  3. Gửi PATCH body `{}` tới `/api/v1/households/<HOUSEHOLD_ID>`.
- **Kỳ vọng:** HTTP 400; code xác thực cụ thể chưa xác định.
- **Kiểm sau:** GET lại hộ: diện tích và địa chỉ không đổi.
- **Nguồn:** HH-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/UpdateHouseholdRequest.java:4`.

### HH-05.1 — Không có thao tác xóa hộ trên API

- **Vai trò:** ADMIN.
- **Điểm vào:** `DELETE /api/v1/households/{id}`.
- **Dữ liệu vào:** Hộ `<HOUSEHOLD_ID>` đang tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
- **Các bước:**
  1. Lấy CSRF trong phiên ADMIN.
  2. Gửi `DELETE /api/v1/households/<HOUSEHOLD_ID>` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 403; request không xóa hộ.
- **Kiểm sau:** GET lại `/api/v1/households/<HOUSEHOLD_ID>`: HTTP 200; hộ vẫn tồn tại.
- **Nguồn:** HH-05; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.

### HH-05.2 — Người chưa đăng nhập không xóa được hộ

- **Vai trò:** ANON.
- **Điểm vào:** `DELETE /api/v1/households/{id}`.
- **Dữ liệu vào:** Hộ `<HOUSEHOLD_ID>` tồn tại; phiên ẩn danh có CSRF hợp lệ.
- **Các bước:**
  1. Gọi `GET /api/v1/auth/csrf` lấy token.
  2. Gửi DELETE `/api/v1/households/<HOUSEHOLD_ID>` với token.
  3. Đọc response.
- **Kỳ vọng:** HTTP 401.
- **Kiểm sau:** Dùng ADMIN GET lại hộ: HTTP 200; hộ vẫn tồn tại.
- **Nguồn:** HH-05; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.

## Điểm chưa xác định

HH-01.2 và HH-04.2: code lỗi xác thực chưa xác định.
