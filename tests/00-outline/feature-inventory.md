# Danh mục chức năng BlueMoon (outline cho tester)

Nguồn đối chiếu: mã nguồn tại `origin/main` (2de4efc, sau PR #17) và `docs/`. Mọi mục đều có nguồn trong ngoặc. Khi tài liệu và mã khác nhau, ghi vào mục "Điểm bất thường" (cuối file), không tự chọn.

## 0. Quy ước dùng trong outline
- **Vai trò thật ở backend:** `ADMIN`, `ACCOUNTANT`, `RESIDENT`, và `ANON` (chưa đăng nhập). Vai trò `AUTHORITY` chỉ có ở dữ liệu demo frontend (`frontend/src/data/mockData.ts:48`), không có trong backend (`V2__identity_audit.sql:21-23`).
- **Chế độ dữ liệu frontend:** `api` (gọi backend thật, mặc định sau đăng nhập) và `demo` (dữ liệu mẫu trong bộ nhớ; chỉ hiện khi `SHOW_DEV_TOOLS` = DEV hoặc `VITE_ENABLE_DEMO=true`, `frontend/src/lib/devTools.ts:6-7`).
- **Mã ID:** `AUTH-`, `ACC-`, `HH-`, `RES-`, `DEC-`, `FEE-`, `RBAC-`, `ROUTE-`, `UI-`, `SYS-`.
- **Mã lỗi:** trả theo ProblemDetail có thuộc tính `code`; `401` cho chưa xác thực, `403` cho sai quyền (`SecurityConfig.java:70-71`).

## 1. Ma trận phân quyền backend (RBAC)
Nguồn: `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:44-67` (thứ tự matcher quan trọng; cuối cùng là `anyRequest().denyAll()`, dòng 67).

| Endpoint | ANON | RESIDENT | ACCOUNTANT | ADMIN | Ghi chú |
|---|---|---|---|---|---|
| `GET /actuator/health`, `GET /actuator/health/**` | ✔ | ✔ | ✔ | ✔ | public |
| `GET /api/v1/system/info` | ✔ | ✔ | ✔ | ✔ | public, metadata hằng số |
| `GET /api/v1/auth/csrf` | ✔ | ✔ | ✔ | ✔ | public |
| `POST /api/v1/auth/login` | ✔ | ✔ | ✔ | ✔ | public |
| `GET /api/v1/auth/me`, `POST /api/v1/auth/logout` | 401 | ✔ | ✔ | ✔ | authenticated |
| `POST /api/v1/auth/change-password` | 401 | ✔ | ✔ | ✔ | authenticated, được phép khi `must_change_password` |
| `POST /api/v1/accounts` | 401 | 403 | 403 | ✔ | ADMIN |
| `POST /api/v1/accounts/{id}/disable`, `/enable`, `/unlock` | 401 | 403 | 403 | ✔ | ADMIN |
| `GET /api/v1/households`, `GET /api/v1/households/{id}` | 401 | 403 | 403 | ✔ | ADMIN |
| `POST /api/v1/households`, `PATCH /api/v1/households/{id}` | 401 | 403 | 403 | ✔ | ADMIN |
| `GET /api/v1/households/{id}/residents` | 401 | 403 | 403 | ✔ | ADMIN |
| `POST /api/v1/households/{id}/residents` | 401 | 403 | 403 | ✔ | ADMIN |
| `GET /api/v1/households/{id}/residency-changes` | 401 | 403 | 403 | ✔ | ADMIN |
| `GET /api/v1/households/me/residents` | 401 | ✔ | 403 | 403 | RESIDENT, theo hộ của chính người gọi |
| `GET /api/v1/residents/{id}`, `PATCH /api/v1/residents/{id}` | 401 | 403 | 403 | ✔ | ADMIN |
| `POST /api/v1/residents/{id}/move-out` | 401 | 403 | 403 | ✔ | ADMIN |
| `POST /api/v1/residents/{id}/return` | 401 | 403 | 403 | ✔ | ADMIN |
| `GET /api/v1/residents/suggest` | 401 | 403 | 403 | ✔ | ADMIN |
| `POST /api/v1/residency-declarations` | 401 | ✔ | 403 | 403 | RESIDENT khai báo cho nhân khẩu trong hộ mình |
| `GET /api/v1/residency-declarations/mine` | 401 | ✔ | 403 | 403 | RESIDENT |
| `POST /api/v1/residency-declarations/{id}/cancel` | 401 | ✔ | 403 | 403 | RESIDENT, chỉ đơn do chính mình gửi |
| `GET /api/v1/residency-declarations` | 401 | 403 | 403 | ✔ | ADMIN, hàng chờ toàn hệ thống |
| `POST /api/v1/residency-declarations/{id}/approve`, `/reject` | 401 | 403 | 403 | ✔ | ADMIN |
| `GET /api/v1/fee-types`, `GET /api/v1/fee-rates`, `GET /api/v1/fee-preview` | 401 | 403 | ✔ | ✔ | ADMIN và ACCOUNTANT đều chỉ đọc |
| `POST /api/v1/fee-rates`, `PATCH /api/v1/fee-rates/{id}`, `DELETE /api/v1/fee-rates/{id}` | 401 | 403 | ✔ | 403 | ACCOUNTANT ghi, ADMIN không ghi |
| Mọi route khác | 401 | 403 | 403 | 403 | `denyAll` |

Quy tắc chung (`MustChangePasswordFilter`, đăng ký tại `SecurityConfig.java:44`): tài khoản có `must_change_password = true` bị `403 PASSWORD_CHANGE_REQUIRED` ở mọi route ngoài danh sách cho phép (`/auth/me`, `/auth/csrf`, `/auth/login`, `/auth/logout`, `/auth/change-password`, health, system/info). Nguồn: `docs/DECISIONS.md` D14, D19; `docs/ARCHITECTURE.md` mục Auth.

Quy tắc CSRF: mọi request ghi cần `X-CSRF-TOKEN` hợp lệ; thiếu hoặc sai trả `403` (ARCHITECTURE.md; D19).

## 2. Route frontend theo vai trò
Nguồn: `frontend/src/App.tsx:104-154`; menu: `frontend/src/components/AppShell.tsx` (case theo vai trò). Trạng thái: **API** = gọi backend thật ở chế độ api; **DEMO** = chỉ dữ liệu mẫu (đã kiểm tra: không import `api/`, không gọi hàm context API).

| Route | Vai trò được vào | Menu (nhãn) | Trạng thái trên `main` |
|---|---|---|---|
| `/login` | Công khai | — | API |
| `/error/:code`, `*` | Công khai | — | Trang lỗi chung |
| `/` | Đã đăng nhập | — | Chuyển hướng theo vai trò |
| `/profile` | Mọi vai trò đã đăng nhập | — | API (`ProfilePage.tsx`) |
| `/admin/accounts` | ADMIN | Danh sách tài khoản | API một phần: chỉ thấy tài khoản tạo trong phiên (không có API liệt kê) |
| `/admin/accounts/new` | ADMIN | Tạo tài khoản | API qua context (`createAccountApi`) |
| `/admin/households` | ADMIN | Hộ gia đình | API qua context (`fetchHouseholds`) |
| `/admin/households/new` | ADMIN | (từ trang hộ) | API qua context (`createHouseholdApi`) |
| `/admin/households/:id` | ADMIN | (từ trang hộ) | API (`HouseholdDetailPage.tsx`, context) |
| `/admin/households/:id/edit` | ADMIN | (từ trang hộ) | API qua context (`updateHouseholdApi`) |
| `/admin/households/:id/residents/new` | ADMIN | (từ trang hộ) | API qua context (`createResidentApi`) |
| `/admin/move-outs` | ADMIN | Chuyển đi và lưu trữ | API qua context (`moveOutApi`) |
| `/admin/absence-approvals` | ADMIN | Duyệt tạm vắng | API ở chế độ api (`ApiAbsenceApprovalsPage.tsx`) |
| `/admin/voluntary` | ADMIN | Đóng góp tự nguyện | DEMO |
| `/admin/billing-periods` | ADMIN | Tạo kỳ thu | DEMO (chưa có backend, S08) |
| `/admin/reports/residency` | ADMIN | Báo cáo lưu trú | DEMO (chưa có backend, S11) |
| `/admin/reports/packages` | ADMIN | Gói cho cơ quan | DEMO (chưa có backend, S12) |
| `/accountant/collections` | ACCOUNTANT | Thu tiền mặt | DEMO (chưa có backend, S09) |
| `/accountant/fee-rates` | ACCOUNTANT | Đơn giá phí | DEMO trên `main`; bản API nằm ở nhánh `hhoangsonnw/feat-s07-ui` (chưa merge). Trên nhánh đó ADMIN cũng vào được, chỉ xem |
| `/accountant/collection-rate` | ACCOUNTANT | Báo cáo tỷ lệ thu | DEMO (chưa có backend, S11) |
| `/resident/invoices` | RESIDENT | Hóa đơn | DEMO (chưa có backend, S10) |
| `/resident/invoices/:id` | RESIDENT | (từ danh sách hóa đơn) | DEMO |
| `/resident/household` | RESIDENT | Hộ gia đình của tôi | API ở chế độ api (context `fetchMyResidents`) |
| `/resident/absence` | RESIDENT | Khai báo tạm vắng | API ở chế độ api (`ApiAbsencePage.tsx`) |
| `/resident/voluntary` | RESIDENT | Đóng góp tự nguyện | DEMO (chưa có backend, S08) |
| `/authority/reports` | AUTHORITY | Báo cáo đã duyệt | DEMO; không có vai trò AUTHORITY ở backend, không đăng nhập thật được |

Menu theo vai trò (`AppShell.tsx`): ADMIN thấy 9 mục (tài khoản, tạo tài khoản, hộ, chuyển đi, duyệt tạm vắng, đóng góp, tạo kỳ thu, báo cáo lưu trú, gói cho cơ quan); ACCOUNTANT thấy 3 mục (thu tiền mặt, đơn giá phí, báo cáo tỷ lệ thu); RESIDENT thấy hóa đơn, hộ của tôi, khai báo, đóng góp; AUTHORITY thấy báo cáo đã duyệt.

Kiểm quyền route: vai trò không được phép vào route thì thấy trang 403 (`RequireRole`, `App.tsx:89`). Ẩn menu không thay thế kiểm tra server.

## 3. Chức năng chi tiết theo module

### 3.1. Xác thực và phiên (AUTH)
- **AUTH-01 Đăng nhập thành công.** `POST /auth/login`, màn `/login`. Sau đăng nhập đổi session ID (session fixation). `GET /auth/me` trả `username`, `fullName`, `role`, `householdId` (null với ADMIN/ACCOUNTANT), `mustChangePassword` (`identity/api/SessionUserResponse.java:5`). Nguồn: ARCHITECTURE.md mục Auth; D19.
- **AUTH-02 Đăng nhập thất bại.** Mọi lỗi (không tồn tại, sai mật khẩu, khóa, vô hiệu) trả cùng một body `401`. Nguồn: README 5.3; D17/D18.
- **AUTH-03 Khóa theo tài khoản.** 5 lần sai liên tiếp khóa 1 phút; bậc tăng 1, 5, 10, 15, 30, 60 phút; sau 60 phút tăng thêm 60 phút mỗi lần (P2). Đếm lại khi đăng nhập thành công. ADMIN không bị khóa. Đang khóa thì mật khẩu đúng vẫn bị từ chối và không đếm. Nguồn: D17, D18.
- **AUTH-04 Mở khóa thủ công.** `POST /accounts/{id}/unlock` đặt lại bộ đếm và bậc khóa; 409 khi không còn gì để đặt lại hoặc tài khoản là ADMIN. Nguồn: D22.
- **AUTH-05 Đăng xuất.** `POST /auth/logout`; khi chưa đăng nhập trả `401`. Nguồn: D18.
- **AUTH-06 Hết hạn phiên.** Phiên lưu trong MySQL, cookie `SESSION` `HttpOnly`, `SameSite=Lax`, hết hạn sau 30 phút. Nguồn: README 5.2.
- **AUTH-07 CSRF.** Ghi không có token hoặc token sai trả `403`. `GET /auth/csrf` là public. Nguồn: ARCHITECTURE.md; D19.
- **AUTH-08 Bắt buộc đổi mật khẩu lần đầu.** Tài khoản do ADMIN cấp có `must_change_password = true`; mọi route ngoài danh sách trả `403 PASSWORD_CHANGE_REQUIRED`; frontend chuyển người dùng về `/profile?tab=password` và chặn mọi trang khác (`App.tsx:58-62, 82`); đổi xong phải đăng nhập lại. Nguồn: D14, D19.
- **AUTH-09 Đổi mật khẩu.** `POST /auth/change-password` body `{currentPassword, newPassword}`, trả `204`. Lỗi `400` có `code`: `CURRENT_PASSWORD_INCORRECT`, `PASSWORD_TOO_SHORT`, `PASSWORD_TOO_LONG`, `PASSWORD_WEAK`, `PASSWORD_SAME_AS_CURRENT`. Quy tắc mật khẩu mới (D23): tối thiểu 8 ký tự theo char, có chữ hoa, chữ thường, số, ký tự đặc biệt (bao gồm khoảng trắng); tối đa 72 byte UTF-8; không trùng mật khẩu hiện tại. Thành công thì mọi phiên của tài khoản bị hủy (kể cả phiên đang dùng). Sai mật khẩu cũ được đếm và khóa như đăng nhập sai.
- **AUTH-10 Bootstrap ADMIN.** Chạy bằng profile `bootstrap` và console tương tác; không có tài khoản mặc định; nếu đã có ADMIN thì bỏ qua. Nguồn: README 7.3; D16.
- **AUTH-11 Hồ sơ cá nhân.** `/profile` chỉ xem; ADMIN sửa `full_name` (D23 Q6). Đổi mật khẩu từ hồ sơ gọi `changePasswordApi`.

### 3.2. Quản lý tài khoản (ACC)
- **ACC-01 Tạo tài khoản.** ADMIN tạo ADMIN, ACCOUNTANT hoặc RESIDENT (`POST /accounts`, màn `/admin/accounts/new`). Username `[A-Za-z0-9._-]`, 3–100 ký tự; email bắt buộc, duy nhất, chuẩn hóa chữ thường; email trùng bị từ chối (409); RESIDENT phải gắn hộ `ACTIVE` (400 nếu không). Mật khẩu tạm do server sinh, không trả trong response, chỉ nằm trong `mock_emails`. Nguồn: D14, D19.
- **ACC-02 Email mô phỏng.** Không có API đọc; kiểm bằng SQL (bảng `mock_emails`). Nguồn: D19.
- **ACC-03 Vô hiệu hóa, kích hoạt lại.** `POST /accounts/{id}/disable|enable` (204). Vô hiệu hủy các phiên đang có; request kế tiếp của tài khoản đó bị từ chối. Không tự vô hiệu chính mình, không vô hiệu ADMIN đang hoạt động cuối cùng. Nguồn: README 5.3; S04.
- **ACC-04 Danh sách tài khoản.** Màn `/admin/accounts` chưa có API liệt kê; chỉ hiện tài khoản tạo trong phiên hiện tại. Đây là giới hạn đã biết (README mục 13).

### 3.3. Hộ gia đình (HH)
- **HH-01 Tạo hộ.** ADMIN, `POST /households`, màn `/admin/households/new`. Mã hộ và mã căn hộ do ADMIN nhập, duy nhất, không sửa sau khi tạo. Diện tích `areaM2` `DECIMAL(10,2)`, > 0; quá 2 chữ số thập phân trả `400`, không làm tròn. Nguồn: D20; `V3__residency.sql`.
- **HH-02 Danh sách hộ.** ADMIN, `GET /households` có lọc theo mã căn hộ và mã hộ, phân trang. Màn `/admin/households`.
- **HH-03 Chi tiết hộ.** ADMIN, `GET /households/{id}`; màn `/admin/households/:id`.
- **HH-04 Cập nhật hộ.** ADMIN, `PATCH /households/{id}`; mã không đổi; diện tích và địa chỉ đổi được. Audit `HOUSEHOLD_UPDATED`, không ghi `address`. Nguồn: D20.
- **HH-05 Không xóa hộ.** Không có endpoint xóa hộ. Nguồn: D20.

### 3.4. Nhân khẩu và biến động cư trú (RES)
- **RES-01 Thêm nhân khẩu vào hộ.** ADMIN, `POST /households/{id}/residents`, màn `/admin/households/:id/residents/new`. Trạng thái cư trú: `RESIDING`, `TEMPORARILY_ABSENT`, `MOVED_OUT`. Trùng số giấy tờ (`identity_number`) trong nhóm `RESIDING` và `TEMPORARILY_ABSENT` trên toàn hệ thống trả `409`, lỗi không chứa số giấy tờ. Ngày sinh không ở tương lai. Nguồn: D20, D21.
- **RES-02 Danh sách nhân khẩu của hộ.** ADMIN, `GET /households/{id}/residents` (lọc `status`, phân trang). Quyền xem số giấy tờ và số điện thoại chỉ có ADMIN; cư dân không thấy. Nguồn: D20.
- **RES-03 Xem và sửa nhân khẩu.** ADMIN, `GET` và `PATCH /residents/{id}`.
- **RES-04 Chuyển đi.** ADMIN, `POST /residents/{id}/move-out`, màn `/admin/move-outs`. Trạng thái sang `MOVED_OUT`; không xóa bản ghi; ghi lịch sử. Không có ngày tương lai; lý do không bắt buộc; chuyển đi lần hai trả 409; không hoàn tác. Chuyển đi không tự khóa tài khoản và không tự đóng hộ. Mọi đơn tạm vắng đang chờ của người đó tự chuyển `CANCELLED`, mỗi đơn một audit `ABSENCE_CANCELLED`. Nguồn: D20, D24 (mục chuyển đi).
- **RES-05 Lịch sử biến động.** ADMIN, `GET /households/{id}/residency-changes` (phân trang). Dòng `change_type` gồm `MOVE_OUT`, `TEMPORARY_ABSENCE`, `RETURN`.
- **RES-06 Thành viên hộ của cư dân.** RESIDENT, `GET /households/me/residents`; chỉ thấy hộ của chính mình; không thấy số giấy tờ và số điện thoại. Màn `/resident/household`. Nguồn: D20, D15.
- **RES-07 Gợi ý tên nhân khẩu.** ADMIN, `GET /residents/suggest?q=`: tối thiểu 2 ký tự (ngắn hơn trả `400 INVALID_FILTER`), tối đa 10 kết quả, chỉ nhân khẩu `RESIDING` hoặc `TEMPORARILY_ABSENT`, không trả CCCD hay SĐT. Nguồn: D24.

### 3.5. Khai báo tạm vắng (DEC)
- **DEC-01 Gửi khai báo.** RESIDENT, `POST /residency-declarations` `{residentId, effectiveDate, endDate?, reason}`, màn `/resident/absence`. Server lấy hộ từ principal, không tin client. Ngày bắt đầu không ở quá khứ (giờ Việt Nam). `endDate` tùy chọn, không trước `effectiveDate`, chỉ để hiển thị. Lý do bắt buộc, tối đa 255 ký tự. Trạng thái `PENDING`. Nguồn: D24.
- **DEC-02 Giới hạn đơn.** Tối đa một đơn `PENDING` mỗi nhân khẩu (409 `DECLARATION_ALREADY_PENDING`). Không khai khi nhân khẩu đang `TEMPORARILY_ABSENT` hoặc `MOVED_OUT`.
- **DEC-03 Xem đơn của hộ.** RESIDENT, `GET /residency-declarations/mine` (phân trang, mới nhất trước); cả hộ thấy đơn của hộ. Thông báo chỉ là trạng thái và lý do từ chối trong danh sách này; không có hộp thư.
- **DEC-04 Hủy đơn.** RESIDENT, `POST /residency-declarations/{id}/cancel`: chỉ đơn đang `PENDING` và do chính account đã gửi; người khác cùng hộ bị 403; đơn đã quyết định hoặc đã hủy trả 409. Trạng thái `CANCELLED`, giữ lịch sử.
- **DEC-05 Hàng chờ ADMIN.** ADMIN, `GET /residency-declarations` lọc `status` (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`; khác thì 400 `INVALID_FILTER`), `apartment` (chứa chuỗi, không phân biệt hoa thường), `q` (tên, tối thiểu 2 ký tự). Chỉ liệt kê dòng `TEMPORARY_ABSENCE`. Màn `/admin/absence-approvals`.
- **DEC-06 Duyệt.** ADMIN, `POST .../approve`: đơn chuyển `APPROVED`; nhân khẩu chuyển `TEMPORARILY_ABSENT` ngay. Đơn không ở `PENDING` hoặc nhân khẩu không `RESIDING` trả 409.
- **DEC-07 Từ chối.** ADMIN, `POST .../reject` `{decisionNote}`: lý do bắt buộc, tối đa 255 ký tự, hiện cho cư dân; nhân khẩu giữ nguyên.
- **DEC-08 Quay lại.** ADMIN, `POST /residents/{id}/return`: nhân khẩu từ `TEMPORARILY_ABSENT` về `RESIDING`; ghi dòng lịch sử `RETURN`, ngày là hôm nay do server đặt (không nhận từ client). Nhân khẩu không tạm vắng trả 409 `RESIDENT_NOT_ABSENT`.
- **DEC-09 Trạng thái tạm vắng trên giao diện.** Cư dân thấy trạng thái và lý do; ADMIN thấy hàng chờ và gợi ý tên. Trang `/resident/absence` và `/admin/absence-approvals` ở chế độ api. Nguồn: D24.

### 3.6. Đơn giá phí (FEE) — chỉ có trên nhánh `hhoangsonnw/feat-s07`
Trên `main`, các endpoint này đã có ở backend nhưng màn M-20 còn là demo. Test API được; test màn M-20 chỉ trên nhánh UI.
- **FEE-01 Danh mục loại phí.** `GET /fee-types` (ADMIN, ACCOUNTANT): hai loại cố định `MANAGEMENT_FEE` (Phí quản lý, tính theo m², đơn vị `m²/tháng`) và `WASTE_FEE` (Phí rác sinh hoạt, cố định mỗi hộ mỗi tháng, đơn vị `hộ/tháng`). Không có API tạo, sửa, xóa loại phí. Nguồn: D25; `V8__fee_rates.sql`.
- **FEE-02 Danh sách đơn giá.** `GET /fee-rates?feeTypeId=&page=&size=` (ADMIN, ACCOUNTANT): sắp `effectiveFrom` giảm dần rồi `id` giảm dần; `size` từ 1 đến 100; sai trả `400 INVALID_FILTER`; `feeTypeId` lạ trả trang rỗng 200. Mỗi dòng có `editable`.
- **FEE-03 Thêm đơn giá.** ACCOUNTANT, `POST /fee-rates` `{feeTypeId, unitPrice, effectiveFrom}`: `effectiveFrom` là ngày 1, không trước tháng hiện tại (giờ Việt Nam); trùng loại và tháng trả 409 `FEE_RATE_DUPLICATE_MONTH`; `unitPrice` > 0, tối đa 2 chữ số thập phân, tối đa 10 chữ số phần nguyên (400 nếu vượt). Nguồn: D25.
- **FEE-04 Sửa đơn giá.** ACCOUNTANT, `PATCH /fee-rates/{id}` `{unitPrice}`: chỉ sửa đơn giá có tháng sau tháng hiện tại; tháng hiện tại và trước đó trả 409 `FEE_RATE_FROZEN`; không tồn tại trả 404 `FEE_RATE_NOT_FOUND`.
- **FEE-05 Xóa đơn giá.** ACCOUNTANT, `DELETE /fee-rates/{id}` (204): cùng quy tắc đóng băng như FEE-04.
- **FEE-06 Xem trước thành tiền.** ADMIN, ACCOUNTANT, `GET /fee-preview?householdId=&period=YYYY-MM`: chỉ tính, không lưu. Phí quản lý = diện tích × đơn giá; phí rác = đơn giá; làm tròn VND nguyên HALF_UP theo từng dòng; tổng là tổng các dòng đã làm tròn. Thiếu đơn giá cho bất kỳ loại nào trong tháng đó trả 409 `FEE_RATE_UNAVAILABLE` (không coi là 0). `period` sai trả 400 `INVALID_PERIOD`. Thiếu tham số trả 400 (không trả 500). Nguồn: D25.
- **FEE-07 Quyền đơn giá.** RESIDENT gọi bất kỳ route đơn giá nào: 403. ADMIN ghi đơn giá: 403. Ma trận đổi do Sơn quyết định (D25), chưa có văn bản của Quân và Tuấn.
- **FEE-08 Màn M-20 (nhánh UI).** ACCOUNTANT thêm, sửa, xóa dòng `editable`; ADMIN chỉ xem; chọn tháng (`type="month"`), mặc định tháng sau; giá hiển thị `7.000,50 đ`; thông báo lỗi tiếng Việt theo bảng mã. Nguồn: `docs/ui-wireframe-guide/05-man-hinh-ke-toan.md`, hợp đồng UI S07.

### 3.7. Nền tảng và thông tin hệ thống (SYS)
- **SYS-01 Health.** `GET /actuator/health` trả trạng thái; không lộ chi tiết nhạy cảm. Nguồn: AGENTS.md mục bảo mật.
- **SYS-02 Thông tin hệ thống.** `GET /api/v1/system/info` trả metadata hằng số, public. Kiểm không có dữ liệu nhạy cảm.
- **SYS-03 Request ID.** Mọi response có request ID trong header; lỗi có thể truy vết bằng request ID; log không chứa body, cookie, Authorization, CCCD, email, SĐT (AGENTS.md).
- **SYS-04 Mặc định từ chối.** Route không khai báo trả 401/403 (`denyAll`).
- **SYS-05 Trang lỗi.** Route không tồn tại và `/error/:code` hiện trang lỗi chung.

### 3.8. Giao diện chung (UI)
- **UI-01 Chủ đề sáng/tối.** Chuyển được; lưu khóa `bm-theme`.
- **UI-02 Vai trò và menu.** Menu đúng theo mục 2. Trang đầu sau đăng nhập theo `defaultHomeByRole` (`frontend/src/lib/format.ts:102`): ADMIN → `/admin/households`; ACCOUNTANT → `/accountant/collections`; RESIDENT → `/resident/invoices`; AUTHORITY → trang báo cáo đã duyệt.
- **UI-03 Hết hạn phiên giữa chừng.** Gọi API trả 401: giao diện đưa về đăng nhập; nội dung nháp không lưu vào storage (trừ nháp M-20 chỉ giữ trong bộ nhớ trang).
- **UI-04 Chế độ demo.** Chỉ hiện khi `SHOW_DEV_TOOLS` (DEV hoặc `VITE_ENABLE_DEMO=true`). Bản build thật không có công cụ demo.
- **UI-05 Responsive.** Bố cục hẹp (360px): bảng chuyển dạng thẻ; nút không lệch hàng. Kiểm trên Chrome, không chỉ đo bằng test tự động.
- **UI-06 Thông báo lỗi.** Tiếng Việt, nêu việc gì đã xảy ra và cách xử lý; lỗi 5xx hiện mã request ID.

## 4. Quy tắc nghiệp vụ tham chiếu (tóm tắt, nguồn `docs/DECISIONS.md`)
- D09: VND, BigDecimal; ngày nghiệp vụ theo Asia/Ho_Chi_Minh; thời điểm lưu UTC.
- D14–D16: cấp tài khoản, một tài khoản một hộ, bootstrap ADMIN.
- D17–D18, D22: khóa đăng nhập và mở khóa thủ công.
- D19: cờ đổi mật khẩu lần đầu, mock email.
- D20–D21: hộ, nhân khẩu, biến động cư trú.
- D23: quy tắc mật khẩu mới.
- D24: khai báo tạm vắng (S06).
- D25: đơn giá phí (S07). Chưa có văn bản xác nhận của Quân và Tuấn.

## 5. Điểm bất thường cần tester chú ý
1. **AUTHORITY chưa có ở backend.** Route `/authority/reports` và menu tương ứng chỉ chạy được với dữ liệu demo. Không có tài khoản thật để kiểm.
2. **Nhiều màn còn demo.** Danh sách ở mục 2 (cột trạng thái). Không kiểm được nghiệp vụ thật của các màn này cho đến khi có backend.
3. **Danh sách tài khoản chưa có API liệt kê.** Chỉ thấy tài khoản tạo trong phiên hiện tại.
4. **Đơn giá phí trên `main` chưa có màn API.** M-20 vẫn là demo; ADMIN chưa có mục menu đơn giá trên `main`.
5. **Báo cáo chưa có số liệu thật.** Báo cáo lưu trú, gói cơ quan, tỷ lệ thu chỉ có dữ liệu demo.
6. **Khóa đăng nhập với ADMIN.** ADMIN không bị khóa tự động (D17); đây là rủi ro bảo mật đã ghi nhận.
7. **Số liệu kiểm thử trong README** là tại thời điểm ghi nhận; không dùng để xác nhận hành vi hiện tại.

## 6. Ngoài phạm vi v1 (không test như chức năng)
QR/VNeID, ticket, khách thăm, mượn đồ, tạm trú, thông báo qua hộp thư, email thật, thanh toán trực tuyến, Redis, Kafka, JWT (AGENTS.md:10,18,25; REQUIREMENTS.md mục V2).

## 7. Nguồn đã đối chiếu
- Backend: `backend/src/main/java/vn/edu/se10/bluemoon/` — controller ở `identity/api`, `residency/api`, `billing/api`, `systeminfo/api`; `config/SecurityConfig.java`; `billing/api/FeeRateController.java`, `FeePreviewController.java`, `FeeTypeController.java`.
- Migration: `backend/src/main/resources/db/migration/V1`–`V8`.
- Frontend: `frontend/src/App.tsx`, `frontend/src/components/AppShell.tsx`, `frontend/src/api/*.ts`, `frontend/src/state/AppStateContext.tsx`, `frontend/src/data/mockData.ts`, `frontend/src/lib/devTools.ts`.
- Tài liệu: `docs/DECISIONS.md`, `docs/REQUIREMENTS.md`, `docs/ARCHITECTURE.md`, `docs/sprint-1/TASKS.md`, `docs/ui-wireframe-guide/`.
