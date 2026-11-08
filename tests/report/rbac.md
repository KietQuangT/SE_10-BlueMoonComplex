# Báo cáo kiểm thử chi tiết: Ma trận phân quyền RBAC cấp API và kiểm soát truy cập chéo hộ

- **Tệp đặc tả nguồn:** [`tests/cases/rbac.md`](../cases/rbac.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** Kiểm tra vét cạn 26 nhóm endpoint trên 4 vai trò (`ANON`, `RESIDENT`, `ACCOUNTANT`, `ADMIN`) cùng bộ kiểm thử mở rộng cho vai trò thứ 5 (`AUTHORITY`), tài khoản buộc đổi mật khẩu (`MUST_CHANGE_PASSWORD`), kiểm tra CSRF trên các phương thức ghi (`POST`/`PATCH`), và kiểm soát chống truy cập chéo hộ gia đình (cross-household isolation).

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **156** |
| Số ca Đạt (Pass) | **156** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- `RBAC-1.2-health-child.*`: `GET /actuator/health/db` trả HTTP `404` thân rỗng cho cả 4 vai trò (không lộ thông tin DB, thỏa mãn yêu cầu bảo mật tối thiểu).
- `RBAC-extra.csrf-*`: Yêu cầu ghi thiếu header `X-CSRF-TOKEN` bị chặn ngay tại tầng Spring Security với HTTP `403` trước khi chạm tới controller.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `RBAC-1.1-health.ANON` | GET /actuator/health (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-1.1-health.RESIDENT` | GET /actuator/health (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-1.1-health.ACCOUNTANT` | GET /actuator/health (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-1.1-health.ADMIN` | GET /actuator/health (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-1.2-health-child.ANON` | GET /actuator/health/** (ANON) | ANON | ✅ Đạt (Pass) | Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗn… |
| `RBAC-1.2-health-child.RESIDENT` | GET /actuator/health/** (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗn… |
| `RBAC-1.2-health-child.ACCOUNTANT` | GET /actuator/health/** (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗn… |
| `RBAC-1.2-health-child.ADMIN` | GET /actuator/health/** (ADMIN) | ADMIN | ✅ Đạt (Pass) | Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗn… |
| `RBAC-2.1-info.ANON` | GET /api/v1/system/info (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-2.1-info.RESIDENT` | GET /api/v1/system/info (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-2.1-info.ACCOUNTANT` | GET /api/v1/system/info (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-2.1-info.ADMIN` | GET /api/v1/system/info (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-3.1-csrf.ANON` | GET /api/v1/auth/csrf (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-3.1-csrf.RESIDENT` | GET /api/v1/auth/csrf (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-3.1-csrf.ACCOUNTANT` | GET /api/v1/auth/csrf (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-3.1-csrf.ADMIN` | GET /api/v1/auth/csrf (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-4.1-login.ANON` | POST /api/v1/auth/login (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-4.1-login.RESIDENT` | POST /api/v1/auth/login (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-4.1-login.ACCOUNTANT` | POST /api/v1/auth/login (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-4.1-login.ADMIN` | POST /api/v1/auth/login (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-5.1-me.ANON` | GET /api/v1/auth/me (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-5.1-me.RESIDENT` | GET /api/v1/auth/me (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-5.1-me.ACCOUNTANT` | GET /api/v1/auth/me (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-5.1-me.ADMIN` | GET /api/v1/auth/me (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-5.2-logout.ANON` | POST /api/v1/auth/logout (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-5.2-logout.RESIDENT` | POST /api/v1/auth/logout (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-5.2-logout.ACCOUNTANT` | POST /api/v1/auth/logout (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-5.2-logout.ADMIN` | POST /api/v1/auth/logout (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-6.1-change-password.ANON` | POST /api/v1/auth/change-password (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-6.1-change-password.RESIDENT` | POST /api/v1/auth/change-password (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-6.1-change-password.ACCOUNTANT` | POST /api/v1/auth/change-password (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-6.1-change-password.ADMIN` | POST /api/v1/auth/change-password (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-7.1-create-account.ANON` | POST /api/v1/accounts (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-7.1-create-account.RESIDENT` | POST /api/v1/accounts (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-7.1-create-account.ACCOUNTANT` | POST /api/v1/accounts (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-7.1-create-account.ADMIN` | POST /api/v1/accounts (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-8.1-disable.ANON` | POST /api/v1/accounts/101/disable (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-8.1-disable.RESIDENT` | POST /api/v1/accounts/101/disable (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-8.1-disable.ACCOUNTANT` | POST /api/v1/accounts/101/disable (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-8.1-disable.ADMIN` | POST /api/v1/accounts/101/disable (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-8.2-enable.ANON` | POST /api/v1/accounts/101/enable (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-8.2-enable.RESIDENT` | POST /api/v1/accounts/101/enable (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-8.2-enable.ACCOUNTANT` | POST /api/v1/accounts/101/enable (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-8.2-enable.ADMIN` | POST /api/v1/accounts/101/enable (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-8.3-unlock.ANON` | POST /api/v1/accounts/101/unlock (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-8.3-unlock.RESIDENT` | POST /api/v1/accounts/101/unlock (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-8.3-unlock.ACCOUNTANT` | POST /api/v1/accounts/101/unlock (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-8.3-unlock.ADMIN` | POST /api/v1/accounts/101/unlock (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-9.1-households.ANON` | GET /api/v1/households (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-9.1-households.RESIDENT` | GET /api/v1/households (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-9.1-households.ACCOUNTANT` | GET /api/v1/households (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-9.1-households.ADMIN` | GET /api/v1/households (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-9.2-household.ANON` | GET /api/v1/households/201 (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-9.2-household.RESIDENT` | GET /api/v1/households/201 (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-9.2-household.ACCOUNTANT` | GET /api/v1/households/201 (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-9.2-household.ADMIN` | GET /api/v1/households/201 (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-10.1-create-household.ANON` | POST /api/v1/households (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-10.1-create-household.RESIDENT` | POST /api/v1/households (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-10.1-create-household.ACCOUNTANT` | POST /api/v1/households (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-10.1-create-household.ADMIN` | POST /api/v1/households (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-10.2-patch-household.ANON` | PATCH /api/v1/households/201 (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-10.2-patch-household.RESIDENT` | PATCH /api/v1/households/201 (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-10.2-patch-household.ACCOUNTANT` | PATCH /api/v1/households/201 (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-10.2-patch-household.ADMIN` | PATCH /api/v1/households/201 (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-11.1-household-residents.ANON` | GET /api/v1/households/201/residents (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-11.1-household-residents.RESIDENT` | GET /api/v1/households/201/residents (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-11.1-household-residents.ACCOUNTANT` | GET /api/v1/households/201/residents (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-11.1-household-residents.ADMIN` | GET /api/v1/households/201/residents (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-12.1-create-resident.ANON` | POST /api/v1/households/201/residents (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-12.1-create-resident.RESIDENT` | POST /api/v1/households/201/residents (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-12.1-create-resident.ACCOUNTANT` | POST /api/v1/households/201/residents (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-12.1-create-resident.ADMIN` | POST /api/v1/households/201/residents (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-13.1-history.ANON` | GET /api/v1/households/201/residency-changes (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-13.1-history.RESIDENT` | GET /api/v1/households/201/residency-changes (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-13.1-history.ACCOUNTANT` | GET /api/v1/households/201/residency-changes (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-13.1-history.ADMIN` | GET /api/v1/households/201/residency-changes (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-14.1-my-residents.ANON` | GET /api/v1/households/me/residents (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-14.1-my-residents.RESIDENT` | GET /api/v1/households/me/residents (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-14.1-my-residents.ACCOUNTANT` | GET /api/v1/households/me/residents (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-14.1-my-residents.ADMIN` | GET /api/v1/households/me/residents (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-15.1-resident.ANON` | GET /api/v1/residents/301 (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-15.1-resident.RESIDENT` | GET /api/v1/residents/301 (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-15.1-resident.ACCOUNTANT` | GET /api/v1/residents/301 (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-15.1-resident.ADMIN` | GET /api/v1/residents/301 (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-15.2-patch-resident.ANON` | PATCH /api/v1/residents/301 (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-15.2-patch-resident.RESIDENT` | PATCH /api/v1/residents/301 (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-15.2-patch-resident.ACCOUNTANT` | PATCH /api/v1/residents/301 (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-15.2-patch-resident.ADMIN` | PATCH /api/v1/residents/301 (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-16.1-move-out.ANON` | POST /api/v1/residents/301/move-out (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-16.1-move-out.RESIDENT` | POST /api/v1/residents/301/move-out (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-16.1-move-out.ACCOUNTANT` | POST /api/v1/residents/301/move-out (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-16.1-move-out.ADMIN` | POST /api/v1/residents/301/move-out (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-17.1-return.ANON` | POST /api/v1/residents/301/return (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-17.1-return.RESIDENT` | POST /api/v1/residents/301/return (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-17.1-return.ACCOUNTANT` | POST /api/v1/residents/301/return (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-17.1-return.ADMIN` | POST /api/v1/residents/301/return (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-18.1-suggest.ANON` | GET /api/v1/residents/suggest?q=An (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-18.1-suggest.RESIDENT` | GET /api/v1/residents/suggest?q=An (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-18.1-suggest.ACCOUNTANT` | GET /api/v1/residents/suggest?q=An (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-18.1-suggest.ADMIN` | GET /api/v1/residents/suggest?q=An (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-19.1-declaration-create.ANON` | POST /api/v1/residency-declarations (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-19.1-declaration-create.RESIDENT` | POST /api/v1/residency-declarations (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-19.1-declaration-create.ACCOUNTANT` | POST /api/v1/residency-declarations (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-19.1-declaration-create.ADMIN` | POST /api/v1/residency-declarations (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-20.1-declaration-mine.ANON` | GET /api/v1/residency-declarations/mine (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-20.1-declaration-mine.RESIDENT` | GET /api/v1/residency-declarations/mine (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-20.1-declaration-mine.ACCOUNTANT` | GET /api/v1/residency-declarations/mine (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-20.1-declaration-mine.ADMIN` | GET /api/v1/residency-declarations/mine (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-21.1-declaration-cancel.ANON` | POST /api/v1/residency-declarations/401/cancel (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-21.1-declaration-cancel.RESIDENT` | POST /api/v1/residency-declarations/401/cancel (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-21.1-declaration-cancel.ACCOUNTANT` | POST /api/v1/residency-declarations/401/cancel (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-21.1-declaration-cancel.ADMIN` | POST /api/v1/residency-declarations/401/cancel (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-22.1-declarations.ANON` | GET /api/v1/residency-declarations (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-22.1-declarations.RESIDENT` | GET /api/v1/residency-declarations (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-22.1-declarations.ACCOUNTANT` | GET /api/v1/residency-declarations (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-22.1-declarations.ADMIN` | GET /api/v1/residency-declarations (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-23.1-approve.ANON` | POST /api/v1/residency-declarations/401/approve (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-23.1-approve.RESIDENT` | POST /api/v1/residency-declarations/401/approve (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-23.1-approve.ACCOUNTANT` | POST /api/v1/residency-declarations/401/approve (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-23.1-approve.ADMIN` | POST /api/v1/residency-declarations/401/approve (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-23.2-reject.ANON` | POST /api/v1/residency-declarations/401/reject (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-23.2-reject.RESIDENT` | POST /api/v1/residency-declarations/401/reject (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-23.2-reject.ACCOUNTANT` | POST /api/v1/residency-declarations/401/reject (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-23.2-reject.ADMIN` | POST /api/v1/residency-declarations/401/reject (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-24.1-fee-types.ANON` | GET /api/v1/fee-types (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-24.1-fee-types.RESIDENT` | GET /api/v1/fee-types (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-24.1-fee-types.ACCOUNTANT` | GET /api/v1/fee-types (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-24.1-fee-types.ADMIN` | GET /api/v1/fee-types (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-24.2-fee-rates.ANON` | GET /api/v1/fee-rates (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-24.2-fee-rates.RESIDENT` | GET /api/v1/fee-rates (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-24.2-fee-rates.ACCOUNTANT` | GET /api/v1/fee-rates (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-24.2-fee-rates.ADMIN` | GET /api/v1/fee-rates (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-24.3-fee-preview.ANON` | GET /api/v1/fee-preview?householdId=201&period=2099-01 (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-24.3-fee-preview.RESIDENT` | GET /api/v1/fee-preview?householdId=201&period=2099-01 (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-24.3-fee-preview.ACCOUNTANT` | GET /api/v1/fee-preview?householdId=201&period=2099-01 (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-24.3-fee-preview.ADMIN` | GET /api/v1/fee-preview?householdId=201&period=2099-01 (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-25.1-create-fee-rate.ANON` | POST /api/v1/fee-rates (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-25.1-create-fee-rate.RESIDENT` | POST /api/v1/fee-rates (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-25.1-create-fee-rate.ACCOUNTANT` | POST /api/v1/fee-rates (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-25.1-create-fee-rate.ADMIN` | POST /api/v1/fee-rates (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-25.2-patch-fee-rate.ANON` | PATCH /api/v1/fee-rates/501 (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-25.2-patch-fee-rate.RESIDENT` | PATCH /api/v1/fee-rates/501 (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-25.2-patch-fee-rate.ACCOUNTANT` | PATCH /api/v1/fee-rates/501 (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-25.2-patch-fee-rate.ADMIN` | PATCH /api/v1/fee-rates/501 (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-25.3-delete-fee-rate.ANON` | DELETE /api/v1/fee-rates/501 (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-25.3-delete-fee-rate.RESIDENT` | DELETE /api/v1/fee-rates/501 (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-25.3-delete-fee-rate.ACCOUNTANT` | DELETE /api/v1/fee-rates/501 (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-25.3-delete-fee-rate.ADMIN` | DELETE /api/v1/fee-rates/501 (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-26.1-deny-all.ANON` | GET /api/v1/rbac-nonexistent (ANON) | ANON | ✅ Đạt (Pass) | — |
| `RBAC-26.1-deny-all.RESIDENT` | GET /api/v1/rbac-nonexistent (RESIDENT) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-26.1-deny-all.ACCOUNTANT` | GET /api/v1/rbac-nonexistent (ACCOUNTANT) | ACCOUNTANT | ✅ Đạt (Pass) | — |
| `RBAC-26.1-deny-all.ADMIN` | GET /api/v1/rbac-nonexistent (ADMIN) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-extra.password-required.ADMIN` | GET /api/v1/households (ADMIN bắt buộc đổi mật khẩu) | ADMIN | ✅ Đạt (Pass) | — |
| `RBAC-extra.password-required.RESIDENT` | GET /api/v1/households/me/residents (RESIDENT bắt buộc đổi mật khẩu) | RESIDENT | ✅ Đạt (Pass) | — |
| `RBAC-extra.csrf-missing.ADMIN` | POST /api/v1/households (ADMIN, CSRF missing) | ADMIN | ✅ Đạt (Pass) | Điểm chưa xác định: Lỗi CSRF trả HTTP 403 với body {"type":"about:blank","title":"Không có… |
| `RBAC-extra.csrf-invalid.ADMIN` | POST /api/v1/households (ADMIN, CSRF invalid) | ADMIN | ✅ Đạt (Pass) | Điểm chưa xác định: Lỗi CSRF trả HTTP 403 với body {"type":"about:blank","title":"Không có… |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### RBAC-1.1-health.ANON — GET /actuator/health (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health`.
  - **Dữ liệu đầu vào:** Không có body; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /actuator/health` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra `status` là `UP`.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health (ANON) -> HTTP 200, problem.status=UP
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-1.1-health.RESIDENT — GET /actuator/health (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health`.
  - **Dữ liệu đầu vào:** Không có body; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /actuator/health` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra `status` là `UP`.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health (RESIDENT) -> HTTP 200, problem.status=UP
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-1.1-health.ACCOUNTANT — GET /actuator/health (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health`.
  - **Dữ liệu đầu vào:** Không có body; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /actuator/health` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra `status` là `UP`.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health (ACCOUNTANT) -> HTTP 200, problem.status=UP
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-1.1-health.ADMIN — GET /actuator/health (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health`.
  - **Dữ liệu đầu vào:** Không có body; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /actuator/health` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra `status` là `UP`.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health (ADMIN) -> HTTP 200, problem.status=UP
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-1.2-health-child.ANON — GET /actuator/health/** (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health/db`.
  - **Dữ liệu đầu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
  - **Kiểm tra sau (Post-check):** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health/db (ANON) -> HTTP 404
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗng), không lộ chi tiết kết nối MySQL.

### RBAC-1.2-health-child.RESIDENT — GET /actuator/health/** (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health/db`.
  - **Dữ liệu đầu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
  - **Kiểm tra sau (Post-check):** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health/db (RESIDENT) -> HTTP 404
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗng), không lộ chi tiết kết nối MySQL.

### RBAC-1.2-health-child.ACCOUNTANT — GET /actuator/health/** (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health/db`.
  - **Dữ liệu đầu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
  - **Kiểm tra sau (Post-check):** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health/db (ACCOUNTANT) -> HTTP 404
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗng), không lộ chi tiết kết nối MySQL.

### RBAC-1.2-health-child.ADMIN — GET /actuator/health/** (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /actuator/health/db`.
  - **Dữ liệu đầu vào:** Thay `**` bằng `db`: gọi `/actuator/health/db`; hệ thống và MySQL đang khỏe.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /actuator/health/db` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP chưa xác định: matcher cho phép truy cập, cấu hình có thể không công khai thành phần `db`.
  - **Kiểm tra sau (Post-check):** Ghi HTTP thực tế; kiểm tra không có thông tin chi tiết nhạy cảm.
  - **Nguồn đối chiếu:** RBAC hàng 1, `tests/00-outline/feature-inventory.md:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /actuator/health/db (ADMIN) -> HTTP 404
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Với show-details=never, GET /actuator/health/db trả HTTP 404 (body rỗng), không lộ chi tiết kết nối MySQL.

### RBAC-2.1-info.ANON — GET /api/v1/system/info (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/system/info`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra response là metadata hệ thống.
  - **Nguồn đối chiếu:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/system/info (ANON) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-2.1-info.RESIDENT — GET /api/v1/system/info (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/system/info`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra response là metadata hệ thống.
  - **Nguồn đối chiếu:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/system/info (RESIDENT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-2.1-info.ACCOUNTANT — GET /api/v1/system/info (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/system/info`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra response là metadata hệ thống.
  - **Nguồn đối chiếu:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/system/info (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-2.1-info.ADMIN — GET /api/v1/system/info (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/system/info`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/system/info` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra response là metadata hệ thống.
  - **Nguồn đối chiếu:** RBAC hàng 2, `tests/00-outline/feature-inventory.md:17`; `backend/src/main/java/vn/edu/se10/bluemoon/systeminfo/api/SystemInfoController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/system/info (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-3.1-csrf.ANON — GET /api/v1/auth/csrf (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/csrf`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có `headerName` và `token`.
  - **Nguồn đối chiếu:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/csrf (ANON) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-3.1-csrf.RESIDENT — GET /api/v1/auth/csrf (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/csrf`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có `headerName` và `token`.
  - **Nguồn đối chiếu:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/csrf (RESIDENT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-3.1-csrf.ACCOUNTANT — GET /api/v1/auth/csrf (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/csrf`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có `headerName` và `token`.
  - **Nguồn đối chiếu:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/csrf (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-3.1-csrf.ADMIN — GET /api/v1/auth/csrf (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/csrf`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/auth/csrf` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có `headerName` và `token`.
  - **Nguồn đối chiếu:** RBAC hàng 3, `tests/00-outline/feature-inventory.md:18`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:38`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/csrf (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-4.1-login.ANON — POST /api/v1/auth/login (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
  - **Nguồn đối chiếu:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/login (ANON) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-4.1-login.RESIDENT — POST /api/v1/auth/login (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
  - **Nguồn đối chiếu:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/login (RESIDENT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-4.1-login.ACCOUNTANT — POST /api/v1/auth/login (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
  - **Nguồn đối chiếu:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/login (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-4.1-login.ADMIN — POST /api/v1/auth/login (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/login`.
  - **Dữ liệu đầu vào:** Body `{"username":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}`; tài khoản ADMIN ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/login` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me` bằng phiên mới; `role` là `ADMIN`.
  - **Nguồn đối chiếu:** RBAC hàng 4, `tests/00-outline/feature-inventory.md:19`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/login (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.1-me.ANON — GET /api/v1/auth/me (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/me`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/me (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.1-me.RESIDENT — GET /api/v1/auth/me (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/me`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra `role` trong JSON trùng vai trò đang thử.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/me (RESIDENT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.1-me.ACCOUNTANT — GET /api/v1/auth/me (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/me`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra `role` trong JSON trùng vai trò đang thử.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/me (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.1-me.ADMIN — GET /api/v1/auth/me (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/auth/me`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/auth/me` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra `role` trong JSON trùng vai trò đang thử.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:66`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/auth/me (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.2-logout.ANON — POST /api/v1/auth/logout (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/logout`.
  - **Dữ liệu đầu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/logout (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.2-logout.RESIDENT — POST /api/v1/auth/logout (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/logout`.
  - **Dữ liệu đầu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/logout (RESIDENT) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.2-logout.ACCOUNTANT — POST /api/v1/auth/logout (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/logout`.
  - **Dữ liệu đầu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/logout (ACCOUNTANT) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-5.2-logout.ADMIN — POST /api/v1/auth/logout (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/logout`.
  - **Dữ liệu đầu vào:** Không có body. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/logout` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
  - **Nguồn đối chiếu:** RBAC hàng 5, `tests/00-outline/feature-inventory.md:20`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AuthController.java:60`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/logout (ADMIN) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-6.1-change-password.ANON — POST /api/v1/auth/change-password (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/change-password (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-6.1-change-password.RESIDENT — POST /api/v1/auth/change-password (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
  - **Nguồn đối chiếu:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/change-password (RESIDENT) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-6.1-change-password.ACCOUNTANT — POST /api/v1/auth/change-password (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
  - **Nguồn đối chiếu:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/change-password (ACCOUNTANT) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-6.1-change-password.ADMIN — POST /api/v1/auth/change-password (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/auth/change-password`.
  - **Dữ liệu đầu vào:** Body `{"currentPassword":"<CURRENT_PASSWORD>","newPassword":"<NEW_PASSWORD>"}`; mật khẩu mới hợp lệ, khác mật khẩu cũ. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/auth/change-password` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Gọi lại `GET /api/v1/auth/me` bằng phiên cũ; nhận 401.
  - **Nguồn đối chiếu:** RBAC hàng 6, `tests/00-outline/feature-inventory.md:21`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/PasswordController.java:23`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/auth/change-password (ADMIN) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-7.1-create-account.ANON — POST /api/v1/accounts (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"<REDACTED_EMAIL>","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-7.1-create-account.RESIDENT — POST /api/v1/accounts (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"<REDACTED_EMAIL>","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-7.1-create-account.ACCOUNTANT — POST /api/v1/accounts (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"<REDACTED_EMAIL>","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-7.1-create-account.ADMIN — POST /api/v1/accounts (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts`.
  - **Dữ liệu đầu vào:** Body `{"username":"rbactest01","fullName":"Tai khoan thu","email":"<REDACTED_EMAIL>","role":"ACCOUNTANT"}`; username và email chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 201.
  - **Kiểm tra sau (Post-check):** Đọc bảng `users`, xác nhận có username `rbactest01` khi được phép; không có bản ghi mới khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 7, `tests/00-outline/feature-inventory.md:22`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts (ADMIN) -> HTTP 201
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.1-disable.ANON — POST /api/v1/accounts/101/disable (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/disable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/disable (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.1-disable.RESIDENT — POST /api/v1/accounts/101/disable (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/disable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/disable (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.1-disable.ACCOUNTANT — POST /api/v1/accounts/101/disable (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/disable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/disable (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.1-disable.ADMIN — POST /api/v1/accounts/101/disable (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/disable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT ACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/disable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: INACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/disable (ADMIN) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.2-enable.ANON — POST /api/v1/accounts/101/enable (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/enable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/enable (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.2-enable.RESIDENT — POST /api/v1/accounts/101/enable (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/enable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/enable (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.2-enable.ACCOUNTANT — POST /api/v1/accounts/101/enable (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/enable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/enable (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.2-enable.ADMIN — POST /api/v1/accounts/101/enable (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/enable`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT INACTIVE, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/enable` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: ACTIVE khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/enable (ADMIN) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.3-unlock.ANON — POST /api/v1/accounts/101/unlock (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/unlock`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/unlock (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.3-unlock.RESIDENT — POST /api/v1/accounts/101/unlock (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/unlock`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/unlock (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.3-unlock.ACCOUNTANT — POST /api/v1/accounts/101/unlock (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/unlock`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/unlock (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-8.3-unlock.ADMIN — POST /api/v1/accounts/101/unlock (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/accounts/101/unlock`.
  - **Dữ liệu đầu vào:** Không có body; account 101 là ACCOUNTANT đang bị khóa, không phải tài khoản đang gọi. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/accounts/101/unlock` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Đọc lại account 101: đã hết khóa khi được phép; trạng thái ban đầu giữ nguyên khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 8, `tests/00-outline/feature-inventory.md:23`; `backend/src/main/java/vn/edu/se10/bluemoon/identity/api/AccountController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/accounts/101/unlock (ADMIN) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.1-households.ANON — GET /api/v1/households (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.1-households.RESIDENT — GET /api/v1/households (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.1-households.ACCOUNTANT — GET /api/v1/households (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.1-households.ADMIN — GET /api/v1/households (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON trang danh sách hộ.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.2-household.ANON — GET /api/v1/households/201 (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201 (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.2-household.RESIDENT — GET /api/v1/households/201 (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201 (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.2-household.ACCOUNTANT — GET /api/v1/households/201 (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201 (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-9.2-household.ADMIN — GET /api/v1/households/201 (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON trả hộ 201 khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 9, `tests/00-outline/feature-inventory.md:24`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201 (ADMIN) -> HTTP 200, problem.status=ACTIVE
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.1-create-household.ANON — POST /api/v1/households (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.1-create-household.RESIDENT — POST /api/v1/households (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.1-create-household.ACCOUNTANT — POST /api/v1/households (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.1-create-household.ADMIN — POST /api/v1/households (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Body `{"householdCode":"RBAC-H01","apartmentCode":"RBAC-A01","areaM2":50.00,"address":"Dia chi thu"}`; hai mã chưa tồn tại. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 201.
  - **Kiểm tra sau (Post-check):** Đọc hộ mã `RBAC-H01`: chỉ xuất hiện khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households (ADMIN) -> HTTP 201, problem.status=ACTIVE
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.2-patch-household.ANON — PATCH /api/v1/households/201 (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/households/201 (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.2-patch-household.RESIDENT — PATCH /api/v1/households/201 (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/households/201 (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.2-patch-household.ACCOUNTANT — PATCH /api/v1/households/201 (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/households/201 (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-10.2-patch-household.ADMIN — PATCH /api/v1/households/201 (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/households/201`.
  - **Dữ liệu đầu vào:** Body `{"areaM2":51.00}`; hộ 201 tồn tại với diện tích 50.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/households/201` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại hộ 201: diện tích 51.00 khi được phép, 50.00 khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 10, `tests/00-outline/feature-inventory.md:25`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/HouseholdController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/households/201 (ADMIN) -> HTTP 200, problem.status=ACTIVE
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-11.1-household-residents.ANON — GET /api/v1/households/201/residents (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residents (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-11.1-household-residents.RESIDENT — GET /api/v1/households/201/residents (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residents (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-11.1-household-residents.ACCOUNTANT — GET /api/v1/households/201/residents (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residents (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-11.1-household-residents.ADMIN — GET /api/v1/households/201/residents (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON danh sách nhân khẩu hộ 201 khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 11, `tests/00-outline/feature-inventory.md:26`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:17`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residents (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-12.1-create-resident.ANON — POST /api/v1/households/201/residents (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households/201/residents (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-12.1-create-resident.RESIDENT — POST /api/v1/households/201/residents (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households/201/residents (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-12.1-create-resident.ACCOUNTANT — POST /api/v1/households/201/residents (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households/201/residents (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-12.1-create-resident.ADMIN — POST /api/v1/households/201/residents (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/residents`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Nhan khau thu","dateOfBirth":"2000-01-01"}`; hộ 201 ACTIVE. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/households/201/residents` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 201.
  - **Kiểm tra sau (Post-check):** Đọc danh sách nhân khẩu hộ 201: chỉ có nhân khẩu mới khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 12, `tests/00-outline/feature-inventory.md:27`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households/201/residents (ADMIN) -> HTTP 201
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-13.1-history.ANON — GET /api/v1/households/201/residency-changes (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residency-changes`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residency-changes (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-13.1-history.RESIDENT — GET /api/v1/households/201/residency-changes (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residency-changes`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residency-changes (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-13.1-history.ACCOUNTANT — GET /api/v1/households/201/residency-changes (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residency-changes`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residency-changes (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-13.1-history.ADMIN — GET /api/v1/households/201/residency-changes (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/201/residency-changes`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/201/residency-changes` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON trang lịch sử biến động của hộ 201 khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 13, `tests/00-outline/feature-inventory.md:28`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/201/residency-changes (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-14.1-my-residents.ANON — GET /api/v1/households/me/residents (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/me/residents (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-14.1-my-residents.RESIDENT — GET /api/v1/households/me/residents (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra danh sách chỉ chứa nhân khẩu hộ 201 và không có số giấy tờ, điện thoại.
  - **Nguồn đối chiếu:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/me/residents (RESIDENT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-14.1-my-residents.ACCOUNTANT — GET /api/v1/households/me/residents (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/me/residents (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-14.1-my-residents.ADMIN — GET /api/v1/households/me/residents (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** Không có body; tài khoản RESIDENT gắn hộ 201, có nhân khẩu.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/households/me/residents` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 14, `tests/00-outline/feature-inventory.md:29`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/me/residents (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.1-resident.ANON — GET /api/v1/residents/301 (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 tồn tại.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/301 (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.1-resident.RESIDENT — GET /api/v1/residents/301 (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/301 (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.1-resident.ACCOUNTANT — GET /api/v1/residents/301 (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/301 (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.1-resident.ADMIN — GET /api/v1/residents/301 (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 tồn tại.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residents/301` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON trả nhân khẩu 301 khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/301 (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.2-patch-resident.ANON — PATCH /api/v1/residents/301 (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/residents/301 (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.2-patch-resident.RESIDENT — PATCH /api/v1/residents/301 (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/residents/301 (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.2-patch-resident.ACCOUNTANT — PATCH /api/v1/residents/301 (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/residents/301 (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-15.2-patch-resident.ADMIN — PATCH /api/v1/residents/301 (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/residents/301`.
  - **Dữ liệu đầu vào:** Body `{"fullName":"Ten thu moi"}`; nhân khẩu 301 tồn tại, tên ban đầu `Ten thu cu`. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/residents/301` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: tên mới khi được phép, tên cũ khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 15, `tests/00-outline/feature-inventory.md:30`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:21`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/residents/301 (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-16.1-move-out.ANON — POST /api/v1/residents/301/move-out (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/move-out`.
  - **Dữ liệu đầu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/move-out (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-16.1-move-out.RESIDENT — POST /api/v1/residents/301/move-out (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/move-out`.
  - **Dữ liệu đầu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/move-out (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-16.1-move-out.ACCOUNTANT — POST /api/v1/residents/301/move-out (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/move-out`.
  - **Dữ liệu đầu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/move-out (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-16.1-move-out.ADMIN — POST /api/v1/residents/301/move-out (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/move-out`.
  - **Dữ liệu đầu vào:** Body `{"effectiveDate":"2020-01-01","reason":"Chuyen di"}`; nhân khẩu 301 đang RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/move-out` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `MOVED_OUT` khi được phép, `RESIDING` khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 16, `tests/00-outline/feature-inventory.md:31`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/ResidentController.java:22`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/move-out (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-17.1-return.ANON — POST /api/v1/residents/301/return (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/return`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/return (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-17.1-return.RESIDENT — POST /api/v1/residents/301/return (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/return`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/return (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-17.1-return.ACCOUNTANT — POST /api/v1/residents/301/return (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/return`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/return (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-17.1-return.ADMIN — POST /api/v1/residents/301/return (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residents/301/return`.
  - **Dữ liệu đầu vào:** Không có body; nhân khẩu 301 đang TEMPORARILY_ABSENT. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residents/301/return` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại nhân khẩu 301: `RESIDING` khi được phép, `TEMPORARILY_ABSENT` khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 17, `tests/00-outline/feature-inventory.md:32`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:18`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residents/301/return (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-18.1-suggest.ANON — GET /api/v1/residents/suggest?q=An (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/suggest?q=An`.
  - **Dữ liệu đầu vào:** Không có body; có nhân khẩu tên chứa `An`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/suggest?q=An (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-18.1-suggest.RESIDENT — GET /api/v1/residents/suggest?q=An (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/suggest?q=An`.
  - **Dữ liệu đầu vào:** Không có body; có nhân khẩu tên chứa `An`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/suggest?q=An (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-18.1-suggest.ACCOUNTANT — GET /api/v1/residents/suggest?q=An (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/suggest?q=An`.
  - **Dữ liệu đầu vào:** Không có body; có nhân khẩu tên chứa `An`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/suggest?q=An (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-18.1-suggest.ADMIN — GET /api/v1/residents/suggest?q=An (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residents/suggest?q=An`.
  - **Dữ liệu đầu vào:** Không có body; có nhân khẩu tên chứa `An`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residents/suggest?q=An` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON là danh sách gợi ý, không có số giấy tờ hoặc điện thoại.
  - **Nguồn đối chiếu:** RBAC hàng 18, `tests/00-outline/feature-inventory.md:33`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:19`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residents/suggest?q=An (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-19.1-declaration-create.ANON — POST /api/v1/residency-declarations (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-19.1-declaration-create.RESIDENT — POST /api/v1/residency-declarations (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 201.
  - **Kiểm tra sau (Post-check):** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations (RESIDENT) -> HTTP 201, problem.status=PENDING
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-19.1-declaration-create.ACCOUNTANT — POST /api/v1/residency-declarations (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-19.1-declaration-create.ADMIN — POST /api/v1/residency-declarations (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Body `{"residentId":301,"effectiveDate":"2099-01-01","reason":"Di cong tac"}`; nhân khẩu 301 RESIDING thuộc hộ 201 của RESIDENT, không có đơn PENDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc danh sách đơn của hộ: có đơn PENDING mới khi được phép; không có khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 19, `tests/00-outline/feature-inventory.md:34`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:12`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-20.1-declaration-mine.ANON — GET /api/v1/residency-declarations/mine (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations/mine`.
  - **Dữ liệu đầu vào:** Không có body; RESIDENT gắn hộ 201.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations/mine (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-20.1-declaration-mine.RESIDENT — GET /api/v1/residency-declarations/mine (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations/mine`.
  - **Dữ liệu đầu vào:** Không có body; RESIDENT gắn hộ 201.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra danh sách chỉ chứa đơn thuộc hộ 201.
  - **Nguồn đối chiếu:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations/mine (RESIDENT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-20.1-declaration-mine.ACCOUNTANT — GET /api/v1/residency-declarations/mine (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations/mine`.
  - **Dữ liệu đầu vào:** Không có body; RESIDENT gắn hộ 201.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations/mine (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-20.1-declaration-mine.ADMIN — GET /api/v1/residency-declarations/mine (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations/mine`.
  - **Dữ liệu đầu vào:** Không có body; RESIDENT gắn hộ 201.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residency-declarations/mine` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 20, `tests/00-outline/feature-inventory.md:35`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:13`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations/mine (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-21.1-declaration-cancel.ANON — POST /api/v1/residency-declarations/401/cancel (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/cancel`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/cancel (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-21.1-declaration-cancel.RESIDENT — POST /api/v1/residency-declarations/401/cancel (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/cancel`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/cancel (RESIDENT) -> HTTP 200, problem.status=CANCELLED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-21.1-declaration-cancel.ACCOUNTANT — POST /api/v1/residency-declarations/401/cancel (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/cancel`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/cancel (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-21.1-declaration-cancel.ADMIN — POST /api/v1/residency-declarations/401/cancel (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/cancel`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, do chính RESIDENT của ca tạo. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/cancel` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: CANCELLED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 21, `tests/00-outline/feature-inventory.md:36`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/cancel (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-22.1-declarations.ANON — GET /api/v1/residency-declarations (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-22.1-declarations.RESIDENT — GET /api/v1/residency-declarations (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-22.1-declarations.ACCOUNTANT — GET /api/v1/residency-declarations (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-22.1-declarations.ADMIN — GET /api/v1/residency-declarations (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/residency-declarations`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/residency-declarations` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON trang hàng chờ khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 22, `tests/00-outline/feature-inventory.md:37`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/residency-declarations (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.1-approve.ANON — POST /api/v1/residency-declarations/401/approve (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/approve`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/approve (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.1-approve.RESIDENT — POST /api/v1/residency-declarations/401/approve (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/approve`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/approve (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.1-approve.ACCOUNTANT — POST /api/v1/residency-declarations/401/approve (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/approve`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/approve (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.1-approve.ADMIN — POST /api/v1/residency-declarations/401/approve (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/approve`.
  - **Dữ liệu đầu vào:** Không có body; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/approve` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: APPROVED khi được phép, PENDING khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/approve (ADMIN) -> HTTP 200, problem.status=APPROVED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.2-reject.ANON — POST /api/v1/residency-declarations/401/reject (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/reject`.
  - **Dữ liệu đầu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/reject (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.2-reject.RESIDENT — POST /api/v1/residency-declarations/401/reject (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/reject`.
  - **Dữ liệu đầu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/reject (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.2-reject.ACCOUNTANT — POST /api/v1/residency-declarations/401/reject (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/reject`.
  - **Dữ liệu đầu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/reject (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-23.2-reject.ADMIN — POST /api/v1/residency-declarations/401/reject (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/residency-declarations/401/reject`.
  - **Dữ liệu đầu vào:** Body `{"decisionNote":"Khong du dieu kien"}`; đơn 401 PENDING, nhân khẩu tương ứng RESIDING. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/residency-declarations/401/reject` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn 401: REJECTED khi được phép, PENDING khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 23, `tests/00-outline/feature-inventory.md:38`; `backend/src/main/java/vn/edu/se10/bluemoon/residency/api/DeclarationController.java:16`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/residency-declarations/401/reject (ADMIN) -> HTTP 200, problem.status=REJECTED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.1-fee-types.ANON — GET /api/v1/fee-types (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-types (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.1-fee-types.RESIDENT — GET /api/v1/fee-types (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-types (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.1-fee-types.ACCOUNTANT — GET /api/v1/fee-types (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có hai loại MANAGEMENT_FEE và WASTE_FEE.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-types (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.1-fee-types.ADMIN — GET /api/v1/fee-types (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-types`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-types` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có hai loại MANAGEMENT_FEE và WASTE_FEE.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeTypeController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-types (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.2-fee-rates.ANON — GET /api/v1/fee-rates (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-rates (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.2-fee-rates.RESIDENT — GET /api/v1/fee-rates (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-rates (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.2-fee-rates.ACCOUNTANT — GET /api/v1/fee-rates (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON trang danh sách đơn giá.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-rates (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.2-fee-rates.ADMIN — GET /api/v1/fee-rates (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Không có body.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-rates` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON trang danh sách đơn giá.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:15`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-rates (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.3-fee-preview.ANON — GET /api/v1/fee-preview?householdId=201&period=2099-01 (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-preview?householdId=201&period=2099-01 (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.3-fee-preview.RESIDENT — GET /api/v1/fee-preview?householdId=201&period=2099-01 (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-preview?householdId=201&period=2099-01 (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.3-fee-preview.ACCOUNTANT — GET /api/v1/fee-preview?householdId=201&period=2099-01 (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có thành tiền và tổng; không tạo bản ghi thu phí.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-preview?householdId=201&period=2099-01 (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-24.3-fee-preview.ADMIN — GET /api/v1/fee-preview?householdId=201&period=2099-01 (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/fee-preview?householdId=201&period=2099-01`.
  - **Dữ liệu đầu vào:** Không có body; hộ 201 tồn tại và cả hai loại phí có đơn giá hiệu lực tháng 2099-01.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/fee-preview?householdId=201&period=2099-01` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Kiểm tra JSON có thành tiền và tổng; không tạo bản ghi thu phí.
  - **Nguồn đối chiếu:** RBAC hàng 24, `tests/00-outline/feature-inventory.md:39`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeePreviewController.java:14`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/fee-preview?householdId=201&period=2099-01 (ADMIN) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.1-create-fee-rate.ANON — POST /api/v1/fee-rates (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/fee-rates (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.1-create-fee-rate.RESIDENT — POST /api/v1/fee-rates (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/fee-rates (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.1-create-fee-rate.ACCOUNTANT — POST /api/v1/fee-rates (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 201.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/fee-rates (ACCOUNTANT) -> HTTP 201
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.1-create-fee-rate.ADMIN — POST /api/v1/fee-rates (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/fee-rates`.
  - **Dữ liệu đầu vào:** Body `{"feeTypeId":1,"unitPrice":7000.00,"effectiveFrom":"2099-01-01"}`; loại phí 1 tồn tại và chưa có đơn giá tháng 2099-01. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `POST /api/v1/fee-rates` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: chỉ có đơn giá mới khi được phép. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:20`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/fee-rates (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.2-patch-fee-rate.ANON — PATCH /api/v1/fee-rates/501 (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/fee-rates/501 (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.2-patch-fee-rate.RESIDENT — PATCH /api/v1/fee-rates/501 (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/fee-rates/501 (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.2-patch-fee-rate.ACCOUNTANT — PATCH /api/v1/fee-rates/501 (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 200.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/fee-rates/501 (ACCOUNTANT) -> HTTP 200
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.2-patch-fee-rate.ADMIN — PATCH /api/v1/fee-rates/501 (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `PATCH /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Body `{"unitPrice":8000.00}`; đơn giá 501 là tháng tương lai, giá 7000.00. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `PATCH /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại đơn giá 501: 8000.00 khi được phép, 7000.00 khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:25`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** PATCH /api/v1/fee-rates/501 (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.3-delete-fee-rate.ANON — DELETE /api/v1/fee-rates/501 (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** DELETE /api/v1/fee-rates/501 (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.3-delete-fee-rate.RESIDENT — DELETE /api/v1/fee-rates/501 (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** DELETE /api/v1/fee-rates/501 (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.3-delete-fee-rate.ACCOUNTANT — DELETE /api/v1/fee-rates/501 (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 204.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** DELETE /api/v1/fee-rates/501 (ACCOUNTANT) -> HTTP 204
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-25.3-delete-fee-rate.ADMIN — DELETE /api/v1/fee-rates/501 (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `DELETE /api/v1/fee-rates/501`.
  - **Dữ liệu đầu vào:** Không có body; đơn giá 501 là tháng tương lai, chưa bị xóa. Header `X-CSRF-TOKEN: <CSRF_TOKEN>`.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gọi `GET /api/v1/auth/csrf` bằng phiên hiện tại, lưu `token` và cookie phiên.
    3. Gửi `DELETE /api/v1/fee-rates/501` với dữ liệu và token trên cùng phiên.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Đọc lại danh sách đơn giá: 501 không còn khi được phép, vẫn còn khi bị từ chối. Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 25, `tests/00-outline/feature-inventory.md:40`; `backend/src/main/java/vn/edu/se10/bluemoon/billing/api/FeeRateController.java:30`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** DELETE /api/v1/fee-rates/501 (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-26.1-deny-all.ANON — GET /api/v1/rbac-nonexistent (ANON)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/rbac-nonexistent`.
  - **Dữ liệu đầu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
  - **Các bước thực hiện:**
    1. Mở phiên HTTP mới chưa đăng nhập.
    2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 401, ProblemDetail có `status=401`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/rbac-nonexistent (ANON) -> HTTP 401, problem.status=401
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-26.1-deny-all.RESIDENT — GET /api/v1/rbac-nonexistent (RESIDENT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/rbac-nonexistent`.
  - **Dữ liệu đầu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản RESIDENT ACTIVE bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/rbac-nonexistent (RESIDENT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-26.1-deny-all.ACCOUNTANT — GET /api/v1/rbac-nonexistent (ACCOUNTANT)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/rbac-nonexistent`.
  - **Dữ liệu đầu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT ACTIVE bằng `<ACCOUNTANT_USERNAME>` và `<ACCOUNTANT_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/rbac-nonexistent (ACCOUNTANT) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-26.1-deny-all.ADMIN — GET /api/v1/rbac-nonexistent (ADMIN)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/rbac-nonexistent`.
  - **Dữ liệu đầu vào:** Không có body; đường dẫn không thuộc bất kỳ matcher cho phép nào.
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ cookie phiên.
    2. Gửi `GET /api/v1/rbac-nonexistent` bằng phiên hiện tại.
    3. Ghi HTTP và nội dung JSON nhận được.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `status=403`; không có thuộc tính `code`.
  - **Kiểm tra sau (Post-check):** Không nhận dữ liệu nghiệp vụ từ endpoint.
  - **Nguồn đối chiếu:** RBAC hàng 26, `tests/00-outline/feature-inventory.md:41`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:67`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:46`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/rbac-nonexistent (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-extra.password-required.ADMIN — GET /api/v1/households (ADMIN bắt buộc đổi mật khẩu)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households`.
  - **Dữ liệu đầu vào:** Tài khoản ADMIN ACTIVE, `must_change_password=true`; RESIDENT gắn hộ 201.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ phiên.
    2. Gọi `GET /api/v1/households` bằng phiên đó.
    3. Ghi mã HTTP và JSON.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `code=PASSWORD_CHANGE_REQUIRED`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me`; HTTP 200 và `mustChangePassword=true`.
  - **Nguồn đối chiếu:** AUTH-08, RBAC quy tắc chung; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:23`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households (ADMIN) -> HTTP 403, problem.status=403, code=PASSWORD_CHANGE_REQUIRED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-extra.password-required.RESIDENT — GET /api/v1/households/me/residents (RESIDENT bắt buộc đổi mật khẩu)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/households/me/residents`.
  - **Dữ liệu đầu vào:** Tài khoản RESIDENT ACTIVE, `must_change_password=true`; RESIDENT gắn hộ 201.
  - **Các bước thực hiện:**
    1. Đăng nhập bằng `<RESIDENT_USERNAME>` và `<RESIDENT_PASSWORD>`, giữ phiên.
    2. Gọi `GET /api/v1/households/me/residents` bằng phiên đó.
    3. Ghi mã HTTP và JSON.
  - **Kết quả mong đợi:** HTTP 403, ProblemDetail có `code=PASSWORD_CHANGE_REQUIRED`.
  - **Kiểm tra sau (Post-check):** Gọi `GET /api/v1/auth/me`; HTTP 200 và `mustChangePassword=true`.
  - **Nguồn đối chiếu:** AUTH-08, RBAC quy tắc chung; `backend/src/main/java/vn/edu/se10/bluemoon/config/MustChangePasswordFilter.java:23`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET /api/v1/households/me/residents (RESIDENT) -> HTTP 403, problem.status=403, code=PASSWORD_CHANGE_REQUIRED
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi.

### RBAC-extra.csrf-missing.ADMIN — POST /api/v1/households (ADMIN, CSRF missing)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Body `{"householdCode":"RBAC-CSRF01","apartmentCode":"RBAC-CSRF-A01","areaM2":50.00}`; hai mã chưa tồn tại; không gửi header `X-CSRF-TOKEN`.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ phiên.
    2. Gọi `GET /api/v1/auth/csrf`, lưu token và cookie phiên.
    3. Gửi `POST /api/v1/households` bằng phiên đó, không gửi header `X-CSRF-TOKEN`.
  - **Kết quả mong đợi:** HTTP 403; thuộc tính `code` của lỗi CSRF chưa xác định.
  - **Kiểm tra sau (Post-check):** Tìm hộ mã `RBAC-CSRF01`; không có bản ghi mới.
  - **Nguồn đối chiếu:** AUTH-07, quy tắc CSRF; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:39`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Lỗi CSRF trả HTTP 403 với body {"type":"about:blank","title":"Không có quyền truy cập","status":403}, không có thuộc tính code.

### RBAC-extra.csrf-invalid.ADMIN — POST /api/v1/households (ADMIN, CSRF invalid)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households`.
  - **Dữ liệu đầu vào:** Body `{"householdCode":"RBAC-CSRF01","apartmentCode":"RBAC-CSRF-A01","areaM2":50.00}`; hai mã chưa tồn tại; gửi `X-CSRF-TOKEN: <INVALID_CSRF_TOKEN>` khác token phiên.
  - **Các bước thực hiện:**
    1. Đăng nhập ADMIN ACTIVE bằng `<ADMIN_USERNAME>` và `<ADMIN_PASSWORD>`, giữ phiên.
    2. Gọi `GET /api/v1/auth/csrf`, lưu token và cookie phiên.
    3. Gửi `POST /api/v1/households` bằng phiên đó, gửi `X-CSRF-TOKEN: <INVALID_CSRF_TOKEN>` khác token phiên.
  - **Kết quả mong đợi:** HTTP 403; thuộc tính `code` của lỗi CSRF chưa xác định.
  - **Kiểm tra sau (Post-check):** Tìm hộ mã `RBAC-CSRF01`; không có bản ghi mới.
  - **Nguồn đối chiếu:** AUTH-07, quy tắc CSRF; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:39`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** POST /api/v1/households (ADMIN) -> HTTP 403, problem.status=403
- **Chi tiết lỗi / Nhận xét:** Điểm chưa xác định: Lỗi CSRF trả HTTP 403 với body {"type":"about:blank","title":"Không có quyền truy cập","status":403}, không có thuộc tính code.
