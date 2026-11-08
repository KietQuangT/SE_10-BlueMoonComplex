# Báo cáo kiểm thử chi tiết: Quản trị tài khoản nội bộ và cư dân

- **Tệp đặc tả nguồn:** [`tests/cases/accounts.md`](../cases/accounts.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra cấp tài khoản mới cho 4 vai trò (RESIDENT, ACCOUNTANT, ADMIN, AUTHORITY), ràng buộc hộ gia đình ACTIVE cho RESIDENT, kiểm tra trùng username/email chuẩn hóa, sinh mật khẩu tạm lưu `mock_emails` (không ghi ra log), vô hiệu hóa tài khoản thu hồi phiên tức thời, kích hoạt lại, mở khóa thủ công, và hành vi danh sách tài khoản trên giao diện.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **10** |
| Số ca Đạt (Pass) | **10** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- `ACC-04.2`: Do backend hiện chưa triển khai API `GET /api/v1/accounts` để liệt kê danh sách tài khoản, sau khi tải lại trang `/admin/accounts` ở chế độ `Backend thật`, giao diện hiển thị ghi chú giải thích rõ giới hạn phiên và khung trạng thái rỗng tiêu đề `"Chưa có tài khoản"`, trong khi bản ghi tài khoản vẫn toàn vẹn trong bảng `users` của MySQL.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `ACC-01.1` | Tạo cư dân cho hộ đang hoạt động | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-01.2` | Từ chối email đã dùng sau chuẩn hóa | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-01.3` | Từ chối cư dân khi thiếu hộ | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-02.1` | Thư mô phỏng được ghi khi tạo tài khoản | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-02.2` | Không tạo thư khi email bị trùng | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-03.1` | Vô hiệu tài khoản đang có phiên | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-03.2` | Kích hoạt tài khoản đã vô hiệu | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-03.3` | Không cho ADMIN tự vô hiệu | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-04.1` | Danh sách hiển thị tài khoản vừa tạo | ADMIN | ✅ Đạt (Pass) | — |
| `ACC-04.2` | Danh sách không tải lại tài khoản từ backend | ADMIN | ✅ Đạt (Pass) | Điểm chưa xác định: Sau tải lại trang ở chế độ API, màn hình hiển thị ghi chú "Chưa có API… |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### ACC-01.1 — Tạo cư dân cho hộ đang hoạt động

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Hộ `<HOUSEHOLD_ID>` ACTIVE; JSON `{"username":"resident.test","fullName":"Test Resident","email":"<TEST_EMAIL>","role":"RESIDENT","householdId":<HOUSEHOLD_ID>}`; `<TEST_EMAIL>` là địa chỉ dành riêng cho thử nghiệm.
  - **Các bước thực hiện:**
    1. Lấy CSRF tại `GET /api/v1/auth/csrf` trong phiên ADMIN.
    2. Gửi JSON tới `POST /api/v1/accounts` với `X-CSRF-TOKEN: <CSRF_TOKEN>`.
    3. Lưu `id` từ response.
  - **Kết quả mong đợi:** HTTP 201; có `id`, `username`, `role=RESIDENT`, `householdId`, `emailStatus=SIMULATED`; không có mật khẩu tạm.
  - **Kiểm tra sau (Post-check):** Đọc tài khoản theo `id` bằng SQL: `must_change_password=true`; có audit `ACCOUNT_CREATED`.
  - **Nguồn đối chiếu:** ACC-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201; id=2, role=RESIDENT, householdId=1, emailStatus=SIMULATED, hasTempPass=false; SQL must_change_password=1, audit=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-01.2 — Từ chối email đã dùng sau chuẩn hóa

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Có tài khoản với `<TEST_EMAIL>`; JSON `{"username":"other.test","fullName":"Other Test","email":"<TEST_EMAIL_KHAC_CHU_HOA>","role":"ACCOUNTANT"}`; hai email chỉ khác chữ hoa/thường.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `POST /api/v1/accounts` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 409; `code=EMAIL_ALREADY_USED`.
  - **Kiểm tra sau (Post-check):** SQL: không có tài khoản `other.test` mới và không có email mô phỏng cho tài khoản này.
  - **Nguồn đối chiếu:** ACC-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=EMAIL_ALREADY_USED; SQL count(other.test)=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-01.3 — Từ chối cư dân khi thiếu hộ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** JSON `{"username":"resident.nohouse","fullName":"Test Resident","email":"<UNUSED_TEST_EMAIL>","role":"RESIDENT"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `POST /api/v1/accounts` với token.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 400; `code=HOUSEHOLD_REQUIRED`.
  - **Kiểm tra sau (Post-check):** SQL: không có tài khoản `resident.nohouse`.
  - **Nguồn đối chiếu:** ACC-01; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 400; code=HOUSEHOLD_REQUIRED; SQL count=0
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-02.1 — Thư mô phỏng được ghi khi tạo tài khoản

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** JSON `{"username":"mail.test","fullName":"Mail Test","email":"<UNUSED_TEST_EMAIL>","role":"ACCOUNTANT"}`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi JSON tới `POST /api/v1/accounts` với token.
    3. Lưu `id` response.
  - **Kết quả mong đợi:** HTTP 201; response không chứa mật khẩu tạm.
  - **Kiểm tra sau (Post-check):** SQL đọc `mock_emails` theo user ID: có thư chứa tên đăng nhập và mật khẩu tạm; không sao chép mật khẩu vào bằng chứng.
  - **Nguồn đối chiếu:** ACC-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201; responseContainsTempPassword=false; SQL mock_emails contains username & tempPassword=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-02.2 — Không tạo thư khi email bị trùng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Đã có `<TEST_EMAIL>`; JSON `{"username":"mail.duplicate","fullName":"Mail Test","email":"<TEST_EMAIL>","role":"ACCOUNTANT"}`.
  - **Các bước thực hiện:**
    1. Ghi số thư mô phỏng hiện có cho `<TEST_EMAIL>`.
    2. Lấy CSRF trong phiên ADMIN.
    3. Gửi JSON tạo tài khoản với token.
  - **Kết quả mong đợi:** HTTP 409; `code=EMAIL_ALREADY_USED`.
  - **Kiểm tra sau (Post-check):** SQL: số thư cho `<TEST_EMAIL>` không tăng.
  - **Nguồn đối chiếu:** ACC-02; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountProvisioningService.java:28`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=EMAIL_ALREADY_USED; SQL mock_emails count before=1, after=1
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-03.1 — Vô hiệu tài khoản đang có phiên

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/{id}/disable`.
  - **Dữ liệu đầu vào:** `<TARGET_ID>` là RESIDENT ACTIVE khác ADMIN; có phiên RESIDENT riêng và CSRF của ADMIN.
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/accounts/<TARGET_ID>/disable` với token ADMIN.
    2. Gọi `GET /api/v1/auth/me` bằng phiên RESIDENT cũ.
    3. Đọc audit của tài khoản đích.
  - **Kết quả mong đợi:** HTTP 204 khi vô hiệu; phiên cũ nhận HTTP 401.
  - **Kiểm tra sau (Post-check):** SQL: tài khoản INACTIVE; có audit `ACCOUNT_DISABLED` và `SESSIONS_REVOKED`.
  - **Nguồn đối chiếu:** ACC-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 204; oldSession /auth/me=401; SQL status=INACTIVE, audits=ACCOUNT_CREATED,LOGIN_SUCCESS,PASSWORD_CHANGED,SESSIONS_REVOKED,LOGIN_SUCCESS,ACCOUNT_DISABLED,SESSIONS_REVOKED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-03.2 — Kích hoạt tài khoản đã vô hiệu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/{id}/enable`.
  - **Dữ liệu đầu vào:** `<TARGET_ID>` là tài khoản INACTIVE; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi `POST /api/v1/accounts/<TARGET_ID>/enable`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** SQL: tài khoản ACTIVE; có audit `ACCOUNT_ENABLED`.
  - **Nguồn đối chiếu:** ACC-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 204; SQL status=ACTIVE, audits=ACCOUNT_CREATED,LOGIN_SUCCESS,PASSWORD_CHANGED,SESSIONS_REVOKED,LOGIN_SUCCESS,ACCOUNT_DISABLED,SESSIONS_REVOKED,ACCOUNT_ENABLED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-03.3 — Không cho ADMIN tự vô hiệu

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/{id}/disable`.
  - **Dữ liệu đầu vào:** `<ADMIN_ID>` là ID của phiên hiện tại; header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Lấy CSRF trong phiên ADMIN.
    2. Gửi `POST /api/v1/accounts/<ADMIN_ID>/disable`.
    3. Đọc response.
  - **Kết quả mong đợi:** HTTP 409; `code=ACCOUNT_DISABLE_CONFLICT`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me`: phiên ADMIN vẫn hoạt động.
  - **Nguồn đối chiếu:** ACC-03; `backend/src/main/java/vn/edu/se10/bluemoon/identity/application/AccountManagementService.java:31`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 409; code=ACCOUNT_DISABLE_CONFLICT; ADMIN /auth/me=200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-04.1 — Danh sách hiển thị tài khoản vừa tạo

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts`.
  - **Dữ liệu đầu vào:** Tài khoản thử nghiệm `list.test` vừa tạo thành công trong phiên giao diện hiện tại.
  - **Các bước thực hiện:**
    1. Mở `/admin/accounts/new`.
    2. Tạo ACCOUNTANT `list.test` với `<UNUSED_TEST_EMAIL>`.
    3. Mở `/admin/accounts`.
  - **Kết quả mong đợi:** Danh sách hiển thị tài khoản `list.test`.
  - **Kiểm tra sau (Post-check):** Ghi nhận dòng `list.test` trong danh sách hiện tại.
  - **Nguồn đối chiếu:** ACC-04; `frontend/src/state/AppStateContext.tsx:314`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Mở /admin/accounts trong cùng phiên: hiển thị "list.test"=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### ACC-04.2 — Danh sách không tải lại tài khoản từ backend

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/accounts`.
  - **Dữ liệu đầu vào:** Đã tạo `list.test` trong phiên giao diện; backend không có API liệt kê tài khoản.
  - **Các bước thực hiện:**
    1. Mở `/admin/accounts` và xác nhận `list.test`.
    2. Tải lại trang trình duyệt.
    3. Kiểm tra danh sách.
  - **Kết quả mong đợi:** Sau tải lại, danh sách không còn tài khoản được giữ trong bộ nhớ phiên giao diện; nội dung thông báo chính xác chưa xác định.
  - **Kiểm tra sau (Post-check):** SQL xác nhận tài khoản `list.test` vẫn tồn tại; xem mục Điểm chưa xác định.
  - **Nguồn đối chiếu:** ACC-04; `frontend/src/state/AppStateContext.tsx:462`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Sau tải lại trang: còn "list.test" trên bảng=false; tiêu đề trống="Chưa có tài khoản"; SQL users count(list.test)=1
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Sau tải lại trang ở chế độ API, màn hình hiển thị ghi chú "Chưa có API liệt kê tài khoản: bảng dưới chỉ hiện các tài khoản bạn vừa cấp trong phiên làm việc này. Vô hiệu, kích hoạt lại và mở khóa dùng API thật." và khung trạng thái rỗng tiêu đề "Chưa có tài khoản".
