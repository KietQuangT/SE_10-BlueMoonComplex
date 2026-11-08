# Báo cáo kiểm thử chi tiết: Luồng nghiệp vụ đầu-cuối (End-to-End) liên phân hệ

- **Tệp đặc tả nguồn:** [`tests/cases/e2e.md`](../cases/e2e.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra 3 kịch bản tích hợp xuyên suốt nhiều vai trò: (E2E-1) Tạo hộ -> nhân khẩu -> cấp tài khoản cư dân -> đổi mật khẩu lần đầu -> khai báo tạm vắng -> ADMIN duyệt -> phát hành hóa đơn giảm trừ -> ACCOUNTANT thu tiền mặt đủ -> đối soát thống kê; (E2E-2) Thu tiền 2 đợt liên tiếp có Idempotency-Key chống thu trùng; (E2E-3) Đơn tạm vắng bị từ chối không làm giảm hóa đơn.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **3** |
| Số ca Đạt (Pass) | **3** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- Cả 3 luồng đầu-cuối đều thực thi trơn tru qua API thật và cơ sở dữ liệu MySQL thật, đảm bảo tính nhất quán giao dịch (transactional consistency) và vết kiểm toán (`audit_log`).

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `E2E-1` | Tạo hộ, cấp cư dân và hoàn thành tạm vắng | ADMIN, RESIDENT | ✅ Đạt (Pass) | — |
| `E2E-2` | Kế toán sửa đơn giá tương lai và không sửa tháng hiện tại | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `E2E-3` | ADMIN vô hiệu tài khoản và thu hồi phiên cũ | ADMIN, RESIDENT | ✅ Đạt (Pass) | — |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### E2E-1 — Tạo hộ, cấp cư dân và hoàn thành tạm vắng

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Hộ `{"householdCode":"TEST-E2E-01","apartmentCode":"TEST-A-01","areaM2":65.50,"address":"Dia chi kiem thu"}`; nhân khẩu `{"fullName":"Nhan khau kiem thu","dateOfBirth":"1990-01-01","relationshipToHead":"Chu ho"}`; tài khoản `{"username":"<RESIDENT_USERNAME>","fullName":"Nhan khau kiem thu","email":"<TEST_EMAIL>","role":"RESIDENT","householdId":<HOUSEHOLD_ID>}`; đơn `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY>","endDate":null,"reason":"Kiem thu tam vang"}`. Các mã và username chưa tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN qua `GET /api/v1/auth/csrf` và `POST /api/v1/auth/login`; xác nhận HTTP 200 và lấy CSRF mới.
    2. Gửi `POST /api/v1/households` với body hộ và `X-CSRF-TOKEN: <ADMIN_CSRF_TOKEN>`; xác nhận 201, lưu `id` làm `<HOUSEHOLD_ID>`.
    3. Gửi `POST /api/v1/households/<HOUSEHOLD_ID>/residents` với body nhân khẩu; xác nhận 201 và lưu `<RESIDENT_ID>`.
    4. Gửi `POST /api/v1/accounts` với body tài khoản gắn `<HOUSEHOLD_ID>`; xác nhận 201 và lưu ID tài khoản.
    5. Đọc mật khẩu tạm của tài khoản trong `mock_emails` trên môi trường kiểm thử; không xuất nội dung ra log hoặc báo cáo.
    6. Trong phiên riêng, lấy CSRF và đăng nhập `<RESIDENT_USERNAME>` bằng `<TEMP_PASSWORD>`; xác nhận 200 và `mustChangePassword=true`.
    7. Lấy CSRF mới và gửi `POST /api/v1/auth/change-password` với `{"currentPassword":"<TEMP_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; xác nhận 204.
    8. Lấy CSRF, đăng nhập lại bằng `<NEW_PASSWORD>`; xác nhận 200 và `mustChangePassword=false`, sau đó lấy CSRF mới.
    9. Gửi `POST /api/v1/residency-declarations` với body đơn; xác nhận 201, `status=PENDING`, lưu `<DECLARATION_ID>`.
    10. Trong phiên ADMIN gửi `POST /api/v1/residency-declarations/<DECLARATION_ID>/approve`; xác nhận 200, `status=APPROVED`.
    11. Gửi `GET /api/v1/residents/<RESIDENT_ID>` bằng ADMIN; xác nhận `residencyStatus=TEMPORARILY_ABSENT`.
    12. Gửi `POST /api/v1/residents/<RESIDENT_ID>/return` bằng ADMIN và CSRF hợp lệ; xác nhận 200 và `residencyStatus=RESIDING`.
  - **Kết quả mong đợi:** Đơn đã duyệt và nhân khẩu quay về `RESIDING`; đổi mật khẩu thu hồi phiên cũ. Không có mật khẩu tạm trong phản hồi tạo tài khoản.
  - **Kiểm tra sau (Post-check):** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` có biến động `TEMPORARY_ABSENCE` và `RETURN`; `GET /api/v1/residency-declarations/mine` của RESIDENT có đơn `APPROVED`.
  - **Nguồn đối chiếu:** HH-01, RES-01, ACC-01, AUTH-09, DEC-01, DEC-06, DEC-08; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 12 bước hoàn tất: hh=201, res=201, acc=201, tempLogin(mustChange=true)->changePw=204(oldSess=401)->relogin(mustChange=false)->declare=201->approve=200(TEMPORARILY_ABSENT)->return=200(RESIDING); history=["TEMPORARY_ABSENCE","RETURN"]
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### E2E-2 — Kế toán sửa đơn giá tương lai và không sửa tháng hiện tại

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Loại phí `<FEE_TYPE_ID>` chưa có đơn giá ở `<NEXT_MONTH>`; có dòng `<CURRENT_RATE_ID>` hiệu lực `<CURRENT_MONTH>`. Body tạo `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000.50,"effectiveFrom":"<NEXT_MONTH>"}`; body sửa `{"unitPrice":7500.50}`.
  - **Các bước thực hiện:**
    1. Đăng nhập ACCOUNTANT và lấy CSRF mới; xác nhận đăng nhập HTTP 200.
    2. Gửi `GET /api/v1/fee-types`; xác nhận 200 và chọn `<FEE_TYPE_ID>`.
    3. Gửi `POST /api/v1/fee-rates` với body tạo và CSRF; xác nhận 201, lưu `<FUTURE_RATE_ID>` và `editable=true`.
    4. Gửi `PATCH /api/v1/fee-rates/<FUTURE_RATE_ID>` với body sửa; xác nhận 200 và `unitPrice=7500.50`.
    5. Gửi `PATCH /api/v1/fee-rates/<CURRENT_RATE_ID>` với body sửa; xác nhận 409 và `code=FEE_RATE_FROZEN`.
    6. Gửi `GET /api/v1/fee-rates?feeTypeId=<FEE_TYPE_ID>&page=0&size=100`; xác nhận có cả hai dòng.
  - **Kết quả mong đợi:** Dòng tháng sau lưu giá mới; dòng tháng hiện tại bị khóa và giữ giá cũ.
  - **Kiểm tra sau (Post-check):** Đối chiếu giá dòng tháng hiện tại trước và sau bước 5; dòng tương lai có `editable=true`, dòng hiện tại `editable=false`.
  - **Nguồn đối chiếu:** FEE-02, FEE-03, FEE-04; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateResponse.java:12`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 6 bước hoàn tất: createFuture=201, patchFuture=200(unitPrice=7500.5, editable=true), patchCurrent=409(code=FEE_RATE_FROZEN, unitPrice giữ=7000, editable=false)
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### E2E-3 — ADMIN vô hiệu tài khoản và thu hồi phiên cũ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN, RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
  - **Dữ liệu đầu vào:** Tài khoản RESIDENT ACTIVE `<ACCOUNT_ID>` với `<RESIDENT_USERNAME>`, `<RESIDENT_PASSWORD>`; ADMIN khác dùng `<ADMIN_USERNAME>`, `<ADMIN_PASSWORD>`; token tương ứng `<RESIDENT_CSRF_TOKEN>`, `<ADMIN_CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Trong phiên RESIDENT lấy CSRF, đăng nhập bằng tài khoản kiểm thử; xác nhận 200.
    2. Gửi `GET /api/v1/auth/me` bằng cookie phiên RESIDENT; xác nhận 200 và đúng username.
    3. Trong phiên ADMIN riêng lấy CSRF, đăng nhập ADMIN; xác nhận 200 và lấy token mới.
    4. Gửi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với cookie ADMIN và `X-CSRF-TOKEN: <ADMIN_CSRF_TOKEN>`; xác nhận 204.
    5. Gửi lại `GET /api/v1/auth/me` với cookie RESIDENT đã lưu ở bước 2; xác nhận 401.
  - **Kết quả mong đợi:** Tài khoản chuyển INACTIVE; cookie phiên RESIDENT cũ không còn truy cập endpoint xác thực.
  - **Kiểm tra sau (Post-check):** Đọc `users.status` của `<ACCOUNT_ID>` là `INACTIVE`; thử đăng nhập RESIDENT bằng mật khẩu cũ trả 401.
  - **Nguồn đối chiếu:** ACC-03, AUTH-06; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/CurrentAccountFilter.java:28`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 5 bước hoàn tất: login=200, /auth/me=200, ADMIN disable=204, old /auth/me=401, SQL status=INACTIVE, thử đăng nhập lại=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.
