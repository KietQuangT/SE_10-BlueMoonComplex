# Báo cáo kiểm thử chi tiết: Quản lý hộ gia đình và căn hộ

- **Tệp đặc tả nguồn:** [`tests/cases/households.md`](../cases/households.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra tạo hộ gia đình (`BigDecimal` diện tích tối đa 2 chữ số thập phân, mã căn duy nhất), lọc danh sách hộ theo trạng thái, xem chi tiết hộ của ADMIN và xem hộ của chính mình bởi RESIDENT, cập nhật một phần (`PATCH`), chuyển đi (`move-out`) đồng bộ trạng thái nhân khẩu sang `MOVED_OUT`, và ghi nhận `audit_log`.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **11** |
| Số ca Đạt (Pass) | **11** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- `HH-01.2` & `HH-04.2`: Lỗi Bean Validation (`areaM2: 65.123` vi phạm `@Digits(integer=8, fraction=2)`) và `PATCH` rỗng (`{}`) đều trả HTTP `400 Bad Request` theo chuẩn `ProblemDetail` (`title="Bad Request"`, `detail="Dữ liệu gửi lên không hợp lệ."`) không kèm trường `code` nghiệp vụ riêng.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `HH-01.1` | Tạo hộ với diện tích hợp lệ | ADMIN | ✅ Đạt (Pass) | — |
| `HH-01.2` | Từ chối diện tích quá hai chữ số thập phân | ADMIN | ✅ Đạt (Pass) | Điểm chưa xác định: Bean Validation (@Digits) trả ProblemDetail status=400, detail="Dữ liệ… |
| `HH-01.3` | Từ chối mã hộ trùng | ADMIN | ✅ Đạt (Pass) | — |
| `HH-02.1` | Lọc danh sách theo mã căn hộ | ADMIN | ✅ Đạt (Pass) | — |
| `HH-02.2` | Từ chối kích thước trang bằng không | ADMIN | ✅ Đạt (Pass) | — |
| `HH-03.1` | Xem hộ đã tồn tại | ADMIN | ✅ Đạt (Pass) | — |
| `HH-03.2` | Hộ không tồn tại trả lỗi | ADMIN | ✅ Đạt (Pass) | — |
| `HH-04.1` | Cập nhật diện tích và địa chỉ | ADMIN | ✅ Đạt (Pass) | — |
| `HH-04.2` | Từ chối cập nhật rỗng | ADMIN | ✅ Đạt (Pass) | Điểm chưa xác định: PATCH {} trả HTTP 400 với code=(không có). |
| `HH-05.1` | Không có thao tác xóa hộ trên API | ADMIN | ✅ Đạt (Pass) | — |
| `HH-05.2` | Người chưa đăng nhập không xóa được hộ | ANON | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### HH-01.1 — Tạo hộ với diện tích hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** JSON `{"householdCode":"HH-TEST-01","apartmentCode":"APT-TEST-01","areaM2":65.50,"address":"Dia chi thu nghiem"}`; hai mã chưa tồn tại.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `POST /api/v1/households` với token.
    3. Lưu `id` response.
  - **Kết quả mong đợi:** HTTP 201; response có `householdCode=HH-TEST-01`, `apartmentCode=APT-TEST-01`, `areaM2=65.50`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/households/<ID>`; có audit `HOUSEHOLD_CREATED`.
  - **Nguồn đối chiếu:** HH-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201; id=1, householdCode=HH-TEST-01, apartmentCode=APT-TEST-01, areaM2=65.5; GET=200; audit=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-01.2 — Từ chối diện tích quá hai chữ số thập phân

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** JSON `{"householdCode":"HH-TEST-02","apartmentCode":"APT-TEST-02","areaM2":65.123,"address":"Dia chi thu nghiem"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `POST /api/v1/households` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 400; code xác thực cụ thể chưa xác định.
  - **Kiểm tra sau (Post-check):** Tìm `householdCode=HH-TEST-02` bằng GET danh sách: không có hộ mới.
  - **Nguồn đối chiếu:** HH-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; body={"detail":"Dữ liệu gửi lên không hợp lệ.","instance":"/api/v1/households","status":400,"title":"Bad Request"}; totalElements=0
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Bean Validation (@Digits) trả ProblemDetail status=400, detail="Dữ liệu gửi lên không hợp lệ.", không có thuộc tính code.

### HH-01.3 — Từ chối mã hộ trùng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Đã có `HH-TEST-01`; JSON `{"householdCode":"HH-TEST-01","apartmentCode":"APT-TEST-03","areaM2":50.00}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `POST /api/v1/households` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 409; `code=HOUSEHOLD_CONFLICT`.
  - **Kiểm tra sau (Post-check):** Lọc `GET /api/v1/households?householdCode=HH-TEST-01`: không có hộ thứ hai.
  - **Nguồn đối chiếu:** HH-01; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=HOUSEHOLD_CONFLICT; totalElements=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-02.1 — Lọc danh sách theo mã căn hộ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households?apartmentCode=APT-TEST-01&page=0&size=10`.
  - **Dữ liệu đầu vào:** Có hộ `APT-TEST-01` và hộ `APT-OTHER`.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/households?apartmentCode=APT-TEST-01&page=0&size=10`.
    3. Đọc danh sách response.
  - **Kết quả mong đợi:** HTTP 200; kết quả có hộ `APT-TEST-01` và không có `APT-OTHER`.
  - **Kiểm tra sau (Post-check):** Đối chiếu mã căn hộ của từng phần tử trong trang.
  - **Nguồn đối chiếu:** HH-02; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; apartmentCodes=["APT-TEST-01"]
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-02.2 — Từ chối kích thước trang bằng không

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households?page=0&size=0`.
  - **Dữ liệu đầu vào:** Query `page=0&size=0`.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/households?page=0&size=0`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 400; `code=INVALID_FILTER`.
  - **Kiểm tra sau (Post-check):** Gọi lại với `size=10`: HTTP 200.
  - **Nguồn đối chiếu:** HH-02; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=INVALID_FILTER; kiểm sau size=10 -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-03.1 — Xem hộ đã tồn tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/{id}`.
  - **Dữ liệu đầu vào:** `<HOUSEHOLD_ID>` là ID hộ `HH-TEST-01`.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/households/<HOUSEHOLD_ID>`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 200; `id=<HOUSEHOLD_ID>` và `householdCode=HH-TEST-01`.
  - **Kiểm tra sau (Post-check):** Đối chiếu mã căn hộ và diện tích với bản ghi hộ.
  - **Nguồn đối chiếu:** HH-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; id=1, householdCode=HH-TEST-01, apartmentCode=APT-TEST-01, areaM2=65.5
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-03.2 — Hộ không tồn tại trả lỗi

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/{id}`.
  - **Dữ liệu đầu vào:** `<MISSING_HOUSEHOLD_ID>` không có trong DB.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gọi `GET /api/v1/households/<MISSING_HOUSEHOLD_ID>`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 404; `code=HOUSEHOLD_NOT_FOUND`.
  - **Kiểm tra sau (Post-check):** SQL xác nhận ID không tồn tại.
  - **Nguồn đối chiếu:** HH-03; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 404; code=HOUSEHOLD_NOT_FOUND; SQL count=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-04.1 — Cập nhật diện tích và địa chỉ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/households/{id}`.
  - **Dữ liệu đầu vào:** Hộ `<HOUSEHOLD_ID>` có mã `HH-TEST-01`; JSON `{"areaM2":70.25,"address":"Dia chi moi thu nghiem"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi PATCH tới `/api/v1/households/<HOUSEHOLD_ID>` với JSON và token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 200; `areaM2=70.25`, địa chỉ mới; mã hộ không đổi.
  - **Kiểm tra sau (Post-check):** GET lại hộ; audit `HOUSEHOLD_UPDATED` không chứa giá trị địa chỉ.
  - **Nguồn đối chiếu:** HH-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/application/HouseholdService.java:17`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; areaM2=70.25, address="Dia chi moi thu nghiem", audit changes={"areaM2":"70.25","id":"1"}
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-04.2 — Từ chối cập nhật rỗng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/households/{id}`.
  - **Dữ liệu đầu vào:** Hộ `<HOUSEHOLD_ID>` tồn tại; body `{}`.
  - **Các bước thực hiện:**
    1. Ghi lại diện tích và địa chỉ hiện tại qua GET.
    2. Lấy CSRF trong phiên ADMIN.
    3. Gửi PATCH body `{}` tới `/api/v1/households/<HOUSEHOLD_ID>`.
  - **Kết quả mong đợi:** HTTP 400; code xác thực cụ thể chưa xác định.
  - **Kiểm tra sau (Post-check):** GET lại hộ: diện tích và địa chỉ không đổi.
  - **Nguồn đối chiếu:** HH-04; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/UpdateHouseholdRequest.java:4`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; body={"detail":"Dữ liệu gửi lên không hợp lệ.","instance":"/api/v1/households/1","status":400,"title":"Bad Request"}; sau kiểm areaM2=70.25
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: PATCH {} trả HTTP 400 với code=(không có).

### HH-05.1 — Không có thao tác xóa hộ trên API

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/households/{id}`.
  - **Dữ liệu đầu vào:** Hộ `<HOUSEHOLD_ID>` đang tồn tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi `DELETE /api/v1/households/<HOUSEHOLD_ID>` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 403; request không xóa hộ.
  - **Kiểm tra sau (Post-check):** GET lại `/api/v1/households/<HOUSEHOLD_ID>`: HTTP 200; hộ vẫn tồn tại.
  - **Nguồn đối chiếu:** HH-05; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403; kiểm sau GET=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### HH-05.2 — Người chưa đăng nhập không xóa được hộ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/households/{id}`.
  - **Dữ liệu đầu vào:** Hộ `<HOUSEHOLD_ID>` tồn tại; phiên ẩn danh có CSRF hợp lệ.
  - **Các bước thực hiện:**
    1. Gọi `GET /api/v1/auth/csrf` lấy token.
    2. Gửi DELETE `/api/v1/households/<HOUSEHOLD_ID>` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 401.
  - **Kiểm tra sau (Post-check):** Dùng ADMIN GET lại hộ: HTTP 200; hộ vẫn tồn tại.
  - **Nguồn đối chiếu:** HH-05; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; kiểm sau ADMIN GET=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
