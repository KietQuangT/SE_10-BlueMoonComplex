# Kiểm tra ma trận quyền API

Tiền điều kiện: mỗi vai trò dùng phiên riêng đã đổi mật khẩu; request ghi có CSRF hợp lệ và payload hợp lệ, ID tồn tại, dữ liệu giả. Với `✔`, đối chiếu kết quả nghiệp vụ của module; không mặc định HTTP thành công nếu dữ liệu không hợp lệ. Với 401/403, kiểm HTTP trước khi kiểm nội dung. Thực hiện từng method/endpoint trong từng dòng riêng.

## RBAC-01 — `GET /actuator/health`, `GET /actuator/health/**`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-02 — `GET /api/v1/system/info`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-03 — `GET /api/v1/auth/csrf`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-04 — `POST /api/v1/auth/login`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-05 — `GET /api/v1/auth/me`, `POST /api/v1/auth/logout`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-06 — `POST /api/v1/auth/change-password`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-07 — `POST /api/v1/accounts`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-08 — `POST /api/v1/accounts/{id}/disable`, `/enable`, `/unlock`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-09 — `GET /api/v1/households`, `GET /api/v1/households/{id}`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-10 — `POST /api/v1/households`, `PATCH /api/v1/households/{id}`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-11 — `GET /api/v1/households/{id}/residents`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-12 — `POST /api/v1/households/{id}/residents`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-13 — `GET /api/v1/households/{id}/residency-changes`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-14 — `GET /api/v1/households/me/residents`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | HTTP 403. |

## RBAC-15 — `GET /api/v1/residents/{id}`, `PATCH /api/v1/residents/{id}`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-16 — `POST /api/v1/residents/{id}/move-out`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-17 — `POST /api/v1/residents/{id}/return`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-18 — `GET /api/v1/residents/suggest`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-19 — `POST /api/v1/residency-declarations`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | HTTP 403. |

## RBAC-20 — `GET /api/v1/residency-declarations/mine`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | HTTP 403. |

## RBAC-21 — `POST /api/v1/residency-declarations/{id}/cancel`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | HTTP 403. |

## RBAC-22 — `GET /api/v1/residency-declarations`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-23 — `POST /api/v1/residency-declarations/{id}/approve`, `/reject`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-24 — `GET /api/v1/fee-types`, `GET /api/v1/fee-rates`, `GET /api/v1/fee-preview`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |

## RBAC-25 — `POST /api/v1/fee-rates`, `PATCH /api/v1/fee-rates/{id}`, `DELETE /api/v1/fee-rates/{id}`

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | Được phép qua lớp phân quyền; kiểm kết quả nghiệp vụ theo ca module. |
| ADMIN | HTTP 403. |

## RBAC-26 — Mọi route khác

Thao tác: gọi từng endpoint/method với lần lượt bốn vai trò, giữ các điều kiện khác hợp lệ.

| Vai trò | Kỳ vọng |
|---|---|
| ANON | HTTP 401. |
| RESIDENT | HTTP 403. |
| ACCOUNTANT | HTTP 403. |
| ADMIN | HTTP 403. |

## RBAC-27 — Bắt buộc đổi mật khẩu và CSRF

1. Đăng nhập tài khoản còn cờ `must_change_password`, gọi route ngoài danh sách cho phép: 403 `PASSWORD_CHANGE_REQUIRED`.
2. Gọi `/auth/me`, `/auth/csrf`, `/auth/login`, `/auth/logout`, `/auth/change-password`, health và system/info với điều kiện hợp lệ: không bị chặn bởi bộ lọc đổi mật khẩu.
3. Gửi request ghi thiếu hoặc sai CSRF: HTTP 403.

## Điểm chưa xác định

Các endpoint công khai có thể trả lỗi nghiệp vụ với payload không hợp lệ; dấu ✔ chỉ thể hiện quyền, không xác định HTTP cụ thể.
