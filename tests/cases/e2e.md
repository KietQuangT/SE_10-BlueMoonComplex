# Luồng kiểm thử đầu cuối

Chỉ dùng dữ liệu kiểm thử biệt lập. `<TODAY>` là ngày hiện tại theo Asia/Ho_Chi_Minh; `<NEXT_MONTH>` và `<CURRENT_MONTH>` là ngày đầu tháng tương ứng. Mỗi phiên dùng cookie và CSRF riêng; các placeholder bí mật không được ghi vào log hoặc báo cáo.

### E2E-1 — Tạo hộ, cấp cư dân và hoàn thành tạm vắng

- **Vai trò:** ADMIN, RESIDENT.
- **Điểm vào:** `POST /api/v1/households`.
- **Dữ liệu vào:** Hộ `{"householdCode":"TEST-E2E-01","apartmentCode":"TEST-A-01","areaM2":65.50,"address":"Dia chi kiem thu"}`; nhân khẩu `{"fullName":"Nhan khau kiem thu","dateOfBirth":"1990-01-01","relationshipToHead":"Chu ho"}`; tài khoản `{"username":"<RESIDENT_USERNAME>","fullName":"Nhan khau kiem thu","email":"<TEST_EMAIL>","role":"RESIDENT","householdId":<HOUSEHOLD_ID>}`; đơn `{"residentId":<RESIDENT_ID>,"effectiveDate":"<TODAY>","endDate":null,"reason":"Kiem thu tam vang"}`. Các mã và username chưa tồn tại.
- **Các bước:**
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
- **Kỳ vọng:** Đơn đã duyệt và nhân khẩu quay về `RESIDING`; đổi mật khẩu thu hồi phiên cũ. Không có mật khẩu tạm trong phản hồi tạo tài khoản.
- **Kiểm sau:** `GET /api/v1/households/<HOUSEHOLD_ID>/residency-changes` có biến động `TEMPORARY_ABSENCE` và `RETURN`; `GET /api/v1/residency-declarations/mine` của RESIDENT có đơn `APPROVED`.
- **Nguồn:** HH-01, RES-01, ACC-01, AUTH-09, DEC-01, DEC-06, DEC-08; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`.

### E2E-2 — Kế toán sửa đơn giá tương lai và không sửa tháng hiện tại

- **Vai trò:** ACCOUNTANT.
- **Điểm vào:** `POST /api/v1/fee-rates`.
- **Dữ liệu vào:** Loại phí `<FEE_TYPE_ID>` chưa có đơn giá ở `<NEXT_MONTH>`; có dòng `<CURRENT_RATE_ID>` hiệu lực `<CURRENT_MONTH>`. Body tạo `{"feeTypeId":<FEE_TYPE_ID>,"unitPrice":7000.50,"effectiveFrom":"<NEXT_MONTH>"}`; body sửa `{"unitPrice":7500.50}`.
- **Các bước:**
  1. Đăng nhập ACCOUNTANT và lấy CSRF mới; xác nhận đăng nhập HTTP 200.
  2. Gửi `GET /api/v1/fee-types`; xác nhận 200 và chọn `<FEE_TYPE_ID>`.
  3. Gửi `POST /api/v1/fee-rates` với body tạo và CSRF; xác nhận 201, lưu `<FUTURE_RATE_ID>` và `editable=true`.
  4. Gửi `PATCH /api/v1/fee-rates/<FUTURE_RATE_ID>` với body sửa; xác nhận 200 và `unitPrice=7500.50`.
  5. Gửi `PATCH /api/v1/fee-rates/<CURRENT_RATE_ID>` với body sửa; xác nhận 409 và `code=FEE_RATE_FROZEN`.
  6. Gửi `GET /api/v1/fee-rates?feeTypeId=<FEE_TYPE_ID>&page=0&size=100`; xác nhận có cả hai dòng.
- **Kỳ vọng:** Dòng tháng sau lưu giá mới; dòng tháng hiện tại bị khóa và giữ giá cũ.
- **Kiểm sau:** Đối chiếu giá dòng tháng hiện tại trước và sau bước 5; dòng tương lai có `editable=true`, dòng hiện tại `editable=false`.
- **Nguồn:** FEE-02, FEE-03, FEE-04; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateResponse.java:12`.

### E2E-3 — ADMIN vô hiệu tài khoản và thu hồi phiên cũ

- **Vai trò:** ADMIN, RESIDENT.
- **Điểm vào:** `POST /api/v1/accounts/<ACCOUNT_ID>/disable`.
- **Dữ liệu vào:** Tài khoản RESIDENT ACTIVE `<ACCOUNT_ID>` với `<RESIDENT_USERNAME>`, `<RESIDENT_PASSWORD>`; ADMIN khác dùng `<ADMIN_USERNAME>`, `<ADMIN_PASSWORD>`; token tương ứng `<RESIDENT_CSRF_TOKEN>`, `<ADMIN_CSRF_TOKEN>`.
- **Các bước:**
  1. Trong phiên RESIDENT lấy CSRF, đăng nhập bằng tài khoản kiểm thử; xác nhận 200.
  2. Gửi `GET /api/v1/auth/me` bằng cookie phiên RESIDENT; xác nhận 200 và đúng username.
  3. Trong phiên ADMIN riêng lấy CSRF, đăng nhập ADMIN; xác nhận 200 và lấy token mới.
  4. Gửi `POST /api/v1/accounts/<ACCOUNT_ID>/disable` với cookie ADMIN và `X-CSRF-TOKEN: <ADMIN_CSRF_TOKEN>`; xác nhận 204.
  5. Gửi lại `GET /api/v1/auth/me` với cookie RESIDENT đã lưu ở bước 2; xác nhận 401.
- **Kỳ vọng:** Tài khoản chuyển INACTIVE; cookie phiên RESIDENT cũ không còn truy cập endpoint xác thực.
- **Kiểm sau:** Đọc `users.status` của `<ACCOUNT_ID>` là `INACTIVE`; thử đăng nhập RESIDENT bằng mật khẩu cũ trả 401.
- **Nguồn:** ACC-03, AUTH-06; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/CurrentAccountFilter.java:28`.

## Điểm chưa xác định

- Cần chọn `<TEST_EMAIL>` từ miền thử nghiệm được ứng dụng chấp nhận; không dùng email thật.
- Không có màn đơn giá API trên main; E2E-2 kiểm qua API.
