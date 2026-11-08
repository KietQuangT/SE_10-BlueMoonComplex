# Báo cáo kiểm thử chi tiết: Hệ thống, sức khỏe, Request ID và trang lỗi

- **Tệp đặc tả nguồn:** [`tests/cases/system.md`](../cases/system.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra endpoint sức khỏe công khai (/actuator/health, /actuator/health/db), thông tin hệ thống (/api/v1/system/info), gắn và ghi nhận X-Request-Id, bảo vệ chống rò rỉ dữ liệu nhạy cảm trong log, và các trang lỗi giao diện (403, 404).

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **10** |
| Số ca Đạt (Pass) | **8** |
| Số ca Không đạt (Fail) | **2** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **80.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- `GET /actuator/health` trả JSON `{"groups":["liveness","readiness"],"status":"UP"}` (2 khóa do bật probe liveness/readiness), không lộ chi tiết thành phần hạ tầng hay phiên bản cơ sở dữ liệu.
- `GET /actuator/health/db` trả HTTP `404` với thân rỗng cho mọi vai trò khi cấu hình `management.endpoint.health.show-details: never`.
- `SYS-03.1` & `SYS-03.2`: Header `X-Request-Id` luôn được sinh mới (UUID hợp lệ) trên mọi phản hồi HTTP, log ứng dụng tuyệt đối không rò rỉ Cookie/Authorization/email/mật khẩu; tuy nhiên `RequestIdFilter` chỉ đặt MDC mà chưa ghi dòng access/security log cho phản hồi `200`/`401` (ghi nhận tại `BUG-001`).

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `SYS-01.1` | Health công khai | ANON | ✅ Đạt (Pass) | JSON trả {"groups":["liveness","readiness"],"status":"UP"}, có status="UP" và không chứa t… |
| `SYS-01.2` | Health không lộ chi tiết cho ADMIN | ADMIN | ✅ Đạt (Pass) | — |
| `SYS-02.1` | Metadata công khai | ANON | ✅ Đạt (Pass) | — |
| `SYS-02.2` | Metadata cho phiên RESIDENT | RESIDENT | ✅ Đạt (Pass) | — |
| `SYS-03.1` | Request ID cho phản hồi thành công | ANON | ❌ Không đạt (Fail) | [BUG-001](01-bao-cao-loi.md) |
| `SYS-03.2` | Request ID cho phản hồi bị từ chối | ANON | ❌ Không đạt (Fail) | [BUG-001](01-bao-cao-loi.md) |
| `SYS-04.1` | Từ chối endpoint lạ khi chưa đăng nhập | ANON | ✅ Đạt (Pass) | — |
| `SYS-04.2` | Từ chối endpoint lạ khi đã đăng nhập | ADMIN | ✅ Đạt (Pass) | — |
| `SYS-05.1` | Trang lỗi 403 trực tiếp | ANON | ✅ Đạt (Pass) | — |
| `SYS-05.2` | Trang không tồn tại | ANON | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### SYS-01.1 — Health công khai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health`.
  - **Dữ liệu đầu vào:** Không gửi cookie.
  - **Các bước thực hiện:**
    1. Gửi GET /actuator/health.
    2. Đọc HTTP và JSON.
    3. Kiểm tra các thuộc tính trả về.
  - **Kết quả mong đợi:** HTTP 200; JSON có `status`, không chứa thông tin kết nối hoặc credential.
  - **Kiểm tra sau (Post-check):** Gọi lại health; status có giá trị.
  - **Nguồn đối chiếu:** SYS-01; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; body={"groups":["liveness","readiness"],"status":"UP"}; lần 2 status=UP
- **Chi tiết lỗi / Nhận xét:** JSON trả {"groups":["liveness","readiness"],"status":"UP"}, có status="UP" và không chứa thông tin kết nối hay credential.

### SYS-01.2 — Health không lộ chi tiết cho ADMIN

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health`.
  - **Dữ liệu đầu vào:** Phiên ADMIN hợp lệ.
  - **Các bước thực hiện:**
    1. Gửi GET /actuator/health cùng phiên ADMIN.
    2. Đọc HTTP và JSON.
    3. Tìm các trường thành phần.
  - **Kết quả mong đợi:** HTTP 200; JSON có `status`, không lộ chi tiết nhạy cảm.
  - **Kiểm tra sau (Post-check):** Đối chiếu JSON với phản hồi ANON.
  - **Nguồn đối chiếu:** SYS-01; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; body={"groups":["liveness","readiness"],"status":"UP"}; khớp ANON=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### SYS-02.1 — Metadata công khai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/system/info`.
  - **Dữ liệu đầu vào:** Không gửi cookie.
  - **Các bước thực hiện:**
    1. Gửi GET /api/v1/system/info.
    2. Đọc HTTP và JSON.
    3. Đối chiếu hai trường.
  - **Kết quả mong đợi:** HTTP 200; JSON `{"name":"BlueMoon","stage":"skeleton"}`.
  - **Kiểm tra sau (Post-check):** Đọc lại; hai giá trị không thay đổi.
  - **Nguồn đối chiếu:** SYS-02; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/application/SystemInfoService.java:8`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; body={"name":"BlueMoon","stage":"skeleton"}; lần 2 không đổi=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### SYS-02.2 — Metadata cho phiên RESIDENT

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/system/info`.
  - **Dữ liệu đầu vào:** Phiên RESIDENT hợp lệ.
  - **Các bước thực hiện:**
    1. Gửi GET /api/v1/system/info.
    2. Đọc JSON.
    3. Tìm thông tin cá nhân.
  - **Kết quả mong đợi:** HTTP 200; `name=BlueMoon`, `stage=skeleton`; không có thông tin tài khoản.
  - **Kiểm tra sau (Post-check):** So sánh với phản hồi ANON.
  - **Nguồn đối chiếu:** SYS-02; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200; body={"name":"BlueMoon","stage":"skeleton"}; khớp ANON=true
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### SYS-03.1 — Request ID cho phản hồi thành công

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/system/info`.
  - **Dữ liệu đầu vào:** Không gửi cookie.
  - **Các bước thực hiện:**
    1. Gửi GET /api/v1/system/info.
    2. Ghi header `X-Request-Id`.
    3. Gửi lại cùng request.
  - **Kết quả mong đợi:** HTTP 200 mỗi lần; mỗi phản hồi có `X-Request-Id` dạng UUID và hai ID khác nhau.
  - **Kiểm tra sau (Post-check):** Tìm hai ID trong log ứng dụng; không ghi nội dung nhạy cảm.
  - **Nguồn đối chiếu:** SYS-03; `backend/src/main/java/vn/edu/se10/bluemoon/common/web/RequestIdFilter.java:22`.
- **Kết quả kiểm thử:** **❌ Không đạt (Fail)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200/200; X-Request-Id UUID hợp lệ và khác nhau (de9b80c7-4307-44d0-b7a3-195c0d3030ab != a41d758e-26fb-441d-997a-4520cd8700dc); xuất hiện trong backend-app.log: id1=false, id2=false; lộ nhạy cảm=false
- **Chi tiết lỗi / Nhận xét:** **[BUG-001](01-bao-cao-loi.md)** — Xem chi tiết [BUG-001](01-bao-cao-loi.md#bug-001--khong-ghi-nhan-x-request-id-vao-log-ung-dung-doi-voi-yeu-cau-200401): Phản hồi HTTP có sinh UUID hợp lệ trong header `X-Request-Id` và log không lộ dữ liệu nhạy cảm, nhưng `RequestIdFilter` chỉ đặt biến vào SLF4J MDC mà không phát sinh dòng log cho request `200 OK` hoặc `401 Unauthorized`, nên không tìm thấy mã `X-Request-Id` trong file log ứng dụng như bước kiểm tra yêu cầu.

### SYS-03.2 — Request ID cho phản hồi bị từ chối

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Không gửi cookie.
  - **Các bước thực hiện:**
    1. Gửi GET /api/v1/accounts.
    2. Ghi HTTP và header `X-Request-Id`.
    3. Tìm request ID trong log.
  - **Kết quả mong đợi:** HTTP 401; header `X-Request-Id` có UUID.
  - **Kiểm tra sau (Post-check):** Kiểm log không chứa cookie, Authorization, email hoặc body.
  - **Nguồn đối chiếu:** SYS-03; `backend/src/main/java/vn/edu/se10/bluemoon/common/web/RequestIdFilter.java:24`.
- **Kết quả kiểm thử:** **❌ Không đạt (Fail)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; X-Request-Id UUID hợp lệ (604d78b8-f3bb-4492-b4e6-1f33e0870ef2); xuất hiện trong backend-app.log=false; lộ cookie/Authorization/email/body=false
- **Chi tiết lỗi / Nhận xét:** **[BUG-001](01-bao-cao-loi.md)** — Xem chi tiết [BUG-001](01-bao-cao-loi.md#bug-001--khong-ghi-nhan-x-request-id-vao-log-ung-dung-doi-voi-yeu-cau-200401): Phản hồi HTTP có sinh UUID hợp lệ trong header `X-Request-Id` và log không lộ dữ liệu nhạy cảm, nhưng `RequestIdFilter` chỉ đặt biến vào SLF4J MDC mà không phát sinh dòng log cho request `200 OK` hoặc `401 Unauthorized`, nên không tìm thấy mã `X-Request-Id` trong file log ứng dụng như bước kiểm tra yêu cầu.

### SYS-04.1 — Từ chối endpoint lạ khi chưa đăng nhập

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/unknown-test`.
  - **Dữ liệu đầu vào:** Không gửi cookie.
  - **Các bước thực hiện:**
    1. Gửi GET /api/v1/unknown-test.
    2. Đọc HTTP.
    3. Đọc `title` trong JSON.
  - **Kết quả mong đợi:** HTTP 401; `title` là “Chưa xác thực”.
  - **Kiểm tra sau (Post-check):** Gọi lại endpoint và xác nhận vẫn 401.
  - **Nguồn đối chiếu:** SYS-04; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 401; title="Chưa xác thực"; lần 2 HTTP 401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### SYS-04.2 — Từ chối endpoint lạ khi đã đăng nhập

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/unknown-test`.
  - **Dữ liệu đầu vào:** Phiên ADMIN hợp lệ.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN.
    2. Gửi GET /api/v1/unknown-test.
    3. Đọc HTTP và JSON.
  - **Kết quả mong đợi:** HTTP 403; `title` là “Không có quyền truy cập”.
  - **Kiểm tra sau (Post-check):** Gọi lại endpoint và xác nhận vẫn 403.
  - **Nguồn đối chiếu:** SYS-04; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:71`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 403; title="Không có quyền truy cập"; lần 2 HTTP 403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### SYS-05.1 — Trang lỗi 403 trực tiếp

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/error/403`.
  - **Dữ liệu đầu vào:** Trình duyệt chưa đăng nhập.
  - **Các bước thực hiện:**
    1. Mở `/error/403`.
    2. Đọc tiêu đề.
    3. Quan sát nút hành động.
  - **Kết quả mong đợi:** Hiển thị “Bạn không có quyền truy cập trang này.” và nút “Về trang đầu của tôi”.
  - **Kiểm tra sau (Post-check):** Bấm nút; chuyển tới `/login`.
  - **Nguồn đối chiếu:** SYS-05; `frontend/src/screens/common/ErrorPage.tsx:15`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** h1="Bạn không có quyền truy cập trang này.", có nút "Về trang đầu của tôi"=true, sau bấm chuyển tới "/login"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### SYS-05.2 — Trang không tồn tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `/duong-dan-khong-ton-tai`.
  - **Dữ liệu đầu vào:** Trình duyệt chưa đăng nhập.
  - **Các bước thực hiện:**
    1. Mở `/login`.
    2. Nhập `/duong-dan-khong-ton-tai`.
    3. Đọc trang lỗi.
  - **Kết quả mong đợi:** Hiển thị “Không tìm thấy.” và nút “Quay lại”.
  - **Kiểm tra sau (Post-check):** Bấm “Quay lại”; trình duyệt về `/login`.
  - **Nguồn đối chiếu:** SYS-05; `frontend/src/App.tsx:153`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** h1="Không tìm thấy.", có nút "Quay lại"=true, sau bấm quay về "/login"
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
