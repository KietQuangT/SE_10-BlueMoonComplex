# Kiểm thử route frontend trên main

Tiền điều kiện: kiểm từng route bằng cách nhập URL; đăng nhập các tài khoản đã đổi mật khẩu. Với route có `:id`, dùng ID giả lập tồn tại. Vai trò không cho phép: trang 403; ANON vào route bảo vệ chuyển `/login`. Thử riêng từng vai trò ANON, RESIDENT, ACCOUNTANT, ADMIN; AUTHORITY chỉ thử trong demo. Các route DEMO chỉ kiểm dữ liệu mẫu, không kết luận backend.

## ROUTE-01 — `/login`

- Trạng thái: API; menu: —.
- Vai trò được phép: Công khai.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON, RESIDENT, ACCOUNTANT, ADMIN: vào trang đăng nhập.

## ROUTE-02 — `/error/:code`, `*`

- Trạng thái: Trang lỗi chung; menu: —.
- Vai trò được phép: Công khai.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON, RESIDENT, ACCOUNTANT, ADMIN: vào được; route lỗi hiển thị trang lỗi chung.

## ROUTE-03 — `/`

- Trạng thái: Chuyển hướng theo vai trò; menu: —.
- Vai trò được phép: Đã đăng nhập.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT, ACCOUNTANT, ADMIN vào được; `/` chuyển tới trang đầu theo vai trò.

## ROUTE-04 — `/profile`

- Trạng thái: API (`ProfilePage.tsx`); menu: —.
- Vai trò được phép: Mọi vai trò đã đăng nhập.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT, ACCOUNTANT, ADMIN vào được trang hồ sơ.

## ROUTE-05 — `/admin/accounts`

- Trạng thái: API một phần: chỉ thấy tài khoản tạo trong phiên (không có API liệt kê); menu: Danh sách tài khoản.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-06 — `/admin/accounts/new`

- Trạng thái: API qua context (`createAccountApi`); menu: Tạo tài khoản.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-07 — `/admin/households`

- Trạng thái: API qua context (`fetchHouseholds`); menu: Hộ gia đình.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-08 — `/admin/households/new`

- Trạng thái: API qua context (`createHouseholdApi`); menu: (từ trang hộ).
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-09 — `/admin/households/:id`

- Trạng thái: API (`HouseholdDetailPage.tsx`, context); menu: (từ trang hộ).
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-10 — `/admin/households/:id/edit`

- Trạng thái: API qua context (`updateHouseholdApi`); menu: (từ trang hộ).
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-11 — `/admin/households/:id/residents/new`

- Trạng thái: API qua context (`createResidentApi`); menu: (từ trang hộ).
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-12 — `/admin/move-outs`

- Trạng thái: API qua context (`moveOutApi`); menu: Chuyển đi và lưu trữ.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-13 — `/admin/absence-approvals`

- Trạng thái: API ở chế độ api (`ApiAbsenceApprovalsPage.tsx`); menu: Duyệt tạm vắng.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-14 — `/admin/voluntary`

- Trạng thái: DEMO; menu: Đóng góp tự nguyện.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-15 — `/admin/billing-periods`

- Trạng thái: DEMO (chưa có backend, S08); menu: Tạo kỳ thu.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-16 — `/admin/reports/residency`

- Trạng thái: DEMO (chưa có backend, S11); menu: Báo cáo lưu trú.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-17 — `/admin/reports/packages`

- Trạng thái: DEMO (chưa có backend, S12); menu: Gói cho cơ quan.
- Vai trò được phép: ADMIN.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: vào được.

## ROUTE-18 — `/accountant/collections`

- Trạng thái: DEMO (chưa có backend, S09); menu: Thu tiền mặt.
- Vai trò được phép: ACCOUNTANT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: vào được, ADMIN: trang 403.

## ROUTE-19 — `/accountant/fee-rates`

- Trạng thái: DEMO trên `main`; bản API nằm ở nhánh `hhoangsonnw/feat-s07-ui` (chưa merge). Trên nhánh đó ADMIN cũng vào được, chỉ xem; menu: Đơn giá phí.
- Vai trò được phép: ACCOUNTANT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: vào được, ADMIN: trang 403.

## ROUTE-20 — `/accountant/collection-rate`

- Trạng thái: DEMO (chưa có backend, S11); menu: Báo cáo tỷ lệ thu.
- Vai trò được phép: ACCOUNTANT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: vào được, ADMIN: trang 403.

## ROUTE-21 — `/resident/invoices`

- Trạng thái: DEMO (chưa có backend, S10); menu: Hóa đơn.
- Vai trò được phép: RESIDENT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: vào được, ACCOUNTANT: trang 403, ADMIN: trang 403.

## ROUTE-22 — `/resident/invoices/:id`

- Trạng thái: DEMO; menu: (từ danh sách hóa đơn).
- Vai trò được phép: RESIDENT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: vào được, ACCOUNTANT: trang 403, ADMIN: trang 403.

## ROUTE-23 — `/resident/household`

- Trạng thái: API ở chế độ api (context `fetchMyResidents`); menu: Hộ gia đình của tôi.
- Vai trò được phép: RESIDENT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: vào được, ACCOUNTANT: trang 403, ADMIN: trang 403.

## ROUTE-24 — `/resident/absence`

- Trạng thái: API ở chế độ api (`ApiAbsencePage.tsx`); menu: Khai báo tạm vắng.
- Vai trò được phép: RESIDENT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: vào được, ACCOUNTANT: trang 403, ADMIN: trang 403.

## ROUTE-25 — `/resident/voluntary`

- Trạng thái: DEMO (chưa có backend, S08); menu: Đóng góp tự nguyện.
- Vai trò được phép: RESIDENT.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: vào được, ACCOUNTANT: trang 403, ADMIN: trang 403.

## ROUTE-26 — `/authority/reports`

- Trạng thái: DEMO; không có vai trò AUTHORITY ở backend, không đăng nhập thật được; menu: Báo cáo đã duyệt.
- Vai trò được phép: AUTHORITY.
- Thao tác: mở route với từng vai trò. Kỳ vọng: ANON chuyển `/login`; RESIDENT: trang 403, ACCOUNTANT: trang 403, ADMIN: trang 403. AUTHORITY demo: vào báo cáo; không có tài khoản backend thật.

## Điểm chưa xác định

Nhánh UI S07 chưa merge: quyền ADMIN vào M-20 khác main; chỉ kiểm theo nhánh tương ứng.
