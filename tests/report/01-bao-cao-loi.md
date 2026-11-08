# Báo cáo lỗi và sai lệch phát hiện (Sprint 1 / v1)

Tài liệu này tổng hợp toàn bộ các lỗi phần mềm, sai lệch đặc tả và thiếu sót công cụ môi trường phát hiện trong quá trình thực thi **432 ca kiểm thử** theo mẫu chuẩn [`tests/templates/bug-report.md`](../templates/bug-report.md). Mọi thông tin nhạy cảm (mật khẩu, cookie phiên, CSRF token, email, CCCD, số điện thoại) đã được che hoàn toàn.

## Bảng tóm tắt danh sách lỗi

| Mã lỗi | Ca kiểm thử bị ảnh hưởng | Tóm tắt lỗi / sai lệch | Mức độ | Phân loại |
| --- | --- | --- | --- | --- |
| `BUG-001` | `SYS-03.1`, `SYS-03.2` | Header `X-Request-Id` được trả về cho client nhưng không được ghi nhận vào log ứng dụng đối với yêu cầu thành công (`200`) và từ chối xác thực (`401`) | Minor | Khả năng quan sát / Truy vết (Observability) |
| `BUG-002` | `ROUTE-20.ADMIN` | Lệch đặc tả phân quyền đường dẫn `/accountant/fee-rates` với vai trò `ADMIN` (`routes.md` kỳ vọng `403`, mã nguồn cho phép xem chỉ đọc theo `DECISIONS.md` D25) | Minor | Lệch tài liệu kiểm thử / Đặc tả |
| `BUG-003` | Quy trình khởi tạo môi trường (`tests/README.md`) | Thiếu thư mục và các script `tools/dev/dev-all.sh`, `tools/dev/dev-close.sh`, `tools/dev/seed-fee-dev.sh` được viện dẫn trong hướng dẫn chạy kiểm thử | Minor | Hạ tầng & Tài liệu kiểm thử |

---

## BUG-001 — Không ghi nhận `X-Request-Id` vào log ứng dụng đối với yêu cầu 200/401

- **ID ca:** `SYS-03.1`, `SYS-03.2` (trong [`system.md`](system.md))
- **Tóm tắt:** Bộ lọc `RequestIdFilter` sinh và trả header `X-Request-Id` hợp lệ về cho client, đồng thời đặt vào SLF4J MDC, nhưng ứng dụng không phát sinh bất kỳ dòng log nào cho các yêu cầu `200 OK` hoặc `401 Unauthorized` nên không thể tra cứu `X-Request-Id` trong log máy chủ.
- **Mức độ:** Minor
- **Môi trường và phiên bản:** Nhánh `main` (`59dfdc9`), Java 21.0.13, Spring Boot 4.0.9 (`--spring.profiles.active=local`), MySQL 9.7.4.
- **Tiền điều kiện, vai trò và dữ liệu giả:** Hệ thống Backend đang chạy; vai trò `ANON` (không cần dữ liệu nhạy cảm).
- **Route/endpoint:** `GET /api/v1/system/info` (`SYS-03.1`) và `GET /api/v1/accounts` (`SYS-03.2`).

### Bước tái hiện

1. Gửi yêu cầu `GET http://localhost:8080/api/v1/system/info` không kèm phiên đăng nhập (`SYS-03.1`).
2. Ghi nhận giá trị UUID trong response header `X-Request-Id` (ví dụ `<REQUEST_ID_1>`).
3. Gửi tiếp yêu cầu `GET http://localhost:8080/api/v1/accounts` không kèm phiên đăng nhập (`SYS-03.2`), nhận HTTP `401` và header `X-Request-Id` mới (`<REQUEST_ID_2>`).
4. Tra cứu chuỗi `<REQUEST_ID_1>` và `<REQUEST_ID_2>` trong tệp nhật ký ứng dụng Backend.

### Kết quả

- **Kỳ vọng:** Tìm thấy mã `X-Request-Id` tương ứng trong log ứng dụng để phục vụ truy vết sự cố, đồng thời dòng log không được chứa `Cookie`, `Authorization`, email, thân yêu cầu hoặc mật khẩu.
- **Thực tế:** Không có dòng log nào chứa `<REQUEST_ID_1>` hoặc `<REQUEST_ID_2>` được ghi ra tệp log ứng dụng. Chỉ khi xảy ra ngoại lệ `500 Internal Server Error` chưa bắt (`GlobalExceptionHandler.unexpected()`), dòng log `ERROR [requestId=...]` mới xuất hiện.
- **Bằng chứng đã che dữ liệu:**
  - Phản hồi `GET /api/v1/system/info`: HTTP `200`, `X-Request-Id` có định dạng UUID v4 hợp lệ, số lần xuất hiện trong `backend-app.log` = `0`.
  - Phản hồi `GET /api/v1/accounts`: HTTP `401`, `X-Request-Id` có định dạng UUID v4 hợp lệ, số lần xuất hiện trong `backend-app.log` = `0`; kiểm tra rò rỉ dữ liệu nhạy cảm trong log = `0`.
- **Ghi chú nguyên nhân & hướng xử lý:**
  - Tại `backend/src/main/java/vn/edu/se10/bluemoon/common/web/RequestIdFilter.java:23`, filter chỉ gọi `MDC.put("requestId", requestId)` rồi chuyển tiếp `chain.doFilter(request, response)` tại dòng 26 mà không ghi dòng tóm tắt hoàn tất request (và tại `SecurityConfig.problem()` cũng không ghi log cảnh báo từ chối truy cập kèm trạng thái HTTP và đường dẫn).
  - Kiến nghị bổ sung một dòng log mức `INFO`/`DEBUG` tối giản ở khối `finally` của `RequestIdFilter` (trước khi `MDC.remove("requestId")`, chỉ ghi `method`, `requestURI`, `status`, thời gian xử lý — tuyệt đối không ghi query nhạy cảm, header hay body) để `MDC` phát huy tác dụng truy vết.

---

## BUG-002 — Lệch đặc tả phân quyền đường dẫn `/accountant/fee-rates` với vai trò `ADMIN`

- **ID ca:** `ROUTE-20.ADMIN` (trong [`routes.md`](routes.md))
- **Tóm tắt:** Đặc tả kiểm thử `ROUTE-20.ADMIN` yêu cầu chặn vai trò `ADMIN` (`403`) khi mở `/accountant/fee-rates`, nhưng mã nguồn Frontend hiện tại cho phép `ADMIN` mở trang này ở chế độ chỉ đọc.
- **Mức độ:** Minor
- **Môi trường và phiên bản:** Nhánh `main` (`59dfdc9`), Vite 8.0.5 + React Router, Playwright Chromium headless.
- **Tiền điều kiện, vai trò và dữ liệu giả:** Đăng nhập thành công tài khoản `ADMIN` đang hoạt động (`ACTIVE`, `mustChangePassword=false`) ở chế độ `Backend thật`.
- **Route/endpoint:** `/accountant/fee-rates`

### Bước tái hiện

1. Mở `/login`, chọn `Backend thật` và đăng nhập bằng tài khoản `ADMIN` hợp lệ.
2. Điều hướng trình duyệt tới đường dẫn `/accountant/fee-rates`.
3. Quan sát tiêu đề `<h1>` và nội dung hiển thị trên trang.

### Kết quả

- **Kỳ vọng (theo `tests/cases/routes.md` dòng 1296):** Hiển thị trang lỗi 403 với tiêu đề `"Bạn không có quyền truy cập trang này."`.
- **Thực tế:** Trang mở bình thường tại `/accountant/fee-rates` với tiêu đề `<h1>` là `"Đơn giá các loại phí"` (ẩn biểu mẫu thêm đơn giá, chỉ cho xem danh sách).
- **Bằng chứng đã che dữ liệu:**
  - `pathname = "/accountant/fee-rates"`, `h1 = "Đơn giá các loại phí"`.
  - Tại `frontend/src/App.tsx:131`: `<Route element={<RequireRole roles={['ACCOUNTANT', 'ADMIN']} />}><Route path="/accountant/fee-rates" element={<FeeRatesPage />} /></Route>`.
- **Ghi chú nguyên nhân & hướng xử lý:**
  - Quyết định nghiệp vụ `D25` trong `docs/DECISIONS.md` xác nhận `ADMIN` được phép xem danh mục loại phí & đơn giá (`GET /api/v1/fee-types`), phù hợp với các ca `FEE-08.1` (Đạt) và `RBAC-20.1-fee-types.ADMIN` (Đạt).
  - Do tệp `tests/cases/routes.md` được biên soạn trước khi nhánh `feat-s07-ui` (PR #18) hợp nhất lên `main`, đặc tả `ROUTE-20.ADMIN` chưa được cập nhật theo `D25`. Cần đồng bộ lại kỳ vọng của `ROUTE-20.ADMIN` từ trang lỗi `403` sang tiêu đề `"Đơn giá các loại phí"` (chế độ chỉ đọc).

---

## BUG-003 — Thiếu bộ script `tools/dev/` được hướng dẫn trong `tests/README.md`

- **ID ca:** Quy trình chuẩn bị môi trường kiểm thử (`tests/README.md` mục 2)
- **Tóm tắt:** Tài liệu `tests/README.md` yêu cầu thực thi `tools/dev/dev-all.sh`, `tools/dev/dev-close.sh` và `tools/dev/seed-fee-dev.sh`, nhưng thư mục `tools/dev/` không tồn tại trong kho mã nguồn.
- **Mức độ:** Minor
- **Môi trường và phiên bản:** Nhánh `main` (`59dfdc9`), hệ điều hành Windows / Bash.
- **Tiền điều kiện, vai trò và dữ liệu giả:** Người kiểm thử chuẩn bị dựng môi trường tích hợp theo đúng tài liệu `tests/README.md`.
- **Route/endpoint:** `tools/dev/dev-all.sh`, `tools/dev/dev-close.sh`, `tools/dev/seed-fee-dev.sh`.

### Bước tái hiện

1. Đọc hướng dẫn tại `tests/README.md` (dòng 24–26 và dòng 33).
2. Kiểm tra sự tồn tại của thư mục `tools/dev/` tại gốc dự án.

### Kết quả

- **Kỳ vọng:** Tồn tại đầy đủ các script khởi chạy, đóng môi trường và nạp dữ liệu mẫu phí hoặc tài liệu trỏ đúng sang đường dẫn thực tế.
- **Thực tế:** Thư mục `tools/` không tồn tại trên nhánh `main`; kho mã chỉ có thư mục `scripts/` chứa `dev-all.sh`, `dev.sh`, `verify.sh` (không có `dev-close.sh` và `seed-fee-dev.sh`).
- **Bằng chứng đã che dữ liệu:** Liệt kê cây thư mục gốc dự án chỉ có `backend/`, `docs/`, `frontend/`, `scripts/`, `tests/`.
- **Ghi chú nguyên nhân & hướng xử lý:** Cập nhật lại đường dẫn trong `tests/README.md` sang `scripts/dev-all.sh` và bổ sung script nạp dữ liệu thử nghiệm nếu nhóm kiểm thử cần chạy thủ công.

---

## Ghi nhận kỹ thuật bổ sung (Observations / Không làm trượt ca kiểm thử)

1. **`OBS-01` — Dòng gợi ý cũ chưa cập nhật trên trang cấp tài khoản (`frontend/src/screens/admin/CreateAccountPage.tsx:239`):**
   - Khi chọn vai trò `RESIDENT` ở chế độ `Backend thật`, giao diện vẫn hiển thị câu ghi chú: *"Danh sách hộ đang lấy từ dữ liệu mẫu cho tới khi có API S05; mã hộ phải tồn tại trong DB."* mặc dù API hộ gia đình S05 (`GET /api/v1/households`) đã hoàn thành và `AppStateContext` đã tải danh sách hộ thật cho `ADMIN`.
2. **`OBS-02` — Hiển thị tiếng Việt có dấu trên Windows Console mặc định khi chạy Bootstrap Admin (`backend/src/main/java/vn/edu/se10/bluemoon/identity/bootstrap/BootstrapAdminRunner.java`):**
   - `System.console().printf("Đã tạo ADMIN.%n")` hoạt động chuẩn trên terminal UTF-8 (Linux/macOS hoặc Windows đã bật `chcp 65001`), nhưng trên Windows ConPTY/CMD mặc định codepage OEM thì ký tự tiếng Việt có dấu bị hiển thị thành dấu hỏi (`?`).
3. **`OBS-03` — Giới hạn thiết kế hiện tại của trang danh sách tài khoản (`/admin/accounts`):**
   - Backend chưa có endpoint `GET /api/v1/accounts` để tải danh sách tài khoản đã cấp từ cơ sở dữ liệu sau khi làm mới trình duyệt (`ACC-04.2` đã kiểm chứng hành vi thông báo rõ ràng cho người dùng). Nếu đưa vào vận hành thực tế ở các Sprint tiếp theo, nên bổ sung API phân trang danh sách tài khoản cho `ADMIN`.
