# Báo cáo tổng hợp kết quả kiểm thử phần mềm — Dự án IT3180 (SE_10 BlueMoon Complex)

- **Ngày thực hiện kiểm thử:** 08/11/2026
- **Phiên bản mã nguồn đối chiếu:** Nhánh `main` (`commit 59dfdc9`)
- **Phạm vi kiểm thử:** Toàn bộ **11 bộ ca kiểm thử (432 test cases)** trong [`tests/cases/`](../cases/) bám sát danh mục chức năng [`tests/00-outline/feature-inventory.md`](../00-outline/feature-inventory.md) cho phiên bản Sprint 1 / v1.
- **Tuân thủ phạm vi quyền truy cập:** Chỉ đọc mã nguồn dự án và **chỉ tạo/ghi tệp báo cáo bên trong thư mục `tests/report/`**; tuyệt đối không chỉnh sửa bất kỳ tệp nào nằm ngoài `tests/`.

## 1. Môi trường thực thi kiểm thử

| Thành phần | Thông số thực tế khi kiểm thử |
| --- | --- |
| Hệ điều hành | Windows (PowerShell + ConPTY Console cho kiểm thử tương tác CLI) |
| Cơ sở dữ liệu | MySQL `9.7.4` chạy cô lập trên cổng cục bộ, múi giờ máy chủ `+00:00 (UTC)`, áp dụng đầy đủ Flyway migrations `V1`–`V8`, `ddl-auto=validate` |
| Backend | Java `21.0.13` (Eclipse Temurin) / Spring Boot `4.0.9`, profile `local`, phiên JDBC `SPRING_SESSION`, bật CSRF & HTTP-only cookie (`http://localhost:8080`) |
| Frontend (Bản DEV) | Node.js `24.12.0` / Vite `8.0.5`, `SHOW_DEV_TOOLS=true`, proxy `/api` & `/actuator` sang `:8080` (`http://localhost:5173`) |
| Frontend (Bản Production) | Bản đóng gói `vite build` chạy qua `vite preview` với `SHOW_DEV_TOOLS=false` (`http://localhost:5174`) phục vụ đối chứng `UI-04.2` |
| Trình duyệt tự động | Playwright Chromium (Headless) kiểm thử điều hướng SPA, bảo vệ route, phiên giao diện, responsive `360×800` và đánh chặn lỗi mạng `500` |

## 2. Thống kê tổng hợp kết quả kiểm thử (432 ca kiểm thử)

| STT | Phân hệ / Bộ ca kiểm thử | Tệp báo cáo chi tiết | Tổng số ca | Đạt (Pass) | Không đạt (Fail) | Bị chặn (Blocked) | Tỷ lệ Đạt |
| :-: | --- | --- | :-: | :-: | :-: | :-: | :-: |
| 1 | Hệ thống, sức khỏe, Request ID và trang lỗi | [`system.md`](system.md) | 10 | 8 | 2 | 0 | 80.00% |
| 2 | Xác thực, phiên, CSRF, khóa tạm, mật khẩu và bootstrap ADMIN | [`auth.md`](auth.md) | 29 | 29 | 0 | 0 | 100.00% |
| 3 | Quản trị tài khoản nội bộ và cư dân | [`accounts.md`](accounts.md) | 10 | 10 | 0 | 0 | 100.00% |
| 4 | Quản lý hộ gia đình và căn hộ | [`households.md`](households.md) | 11 | 11 | 0 | 0 | 100.00% |
| 5 | Quản lý nhân khẩu trong hộ | [`residents.md`](residents.md) | 15 | 15 | 0 | 0 | 100.00% |
| 6 | Khai báo tạm vắng, trở về và phê duyệt | [`declarations.md`](declarations.md) | 21 | 21 | 0 | 0 | 100.00% |
| 7 | Đơn giá phí, kỳ thu, hóa đơn, thu tiền mặt và thống kê | [`fees.md`](fees.md) | 30 | 30 | 0 | 0 | 100.00% |
| 8 | Ma trận phân quyền RBAC cấp API và kiểm soát truy cập chéo hộ | [`rbac.md`](rbac.md) | 156 | 156 | 0 | 0 | 100.00% |
| 9 | Điều hướng và bảo vệ đường dẫn Frontend theo vai trò | [`routes.md`](routes.md) | 135 | 134 | 1 | 0 | 99.26% |
| 10 | Giao diện chung, chế độ Sáng/Tối, Responsive 360px và xử lý lỗi | [`ui-common.md`](ui-common.md) | 12 | 12 | 0 | 0 | 100.00% |
| 11 | Luồng nghiệp vụ đầu-cuối (End-to-End) liên phân hệ | [`e2e.md`](e2e.md) | 3 | 3 | 0 | 0 | 100.00% |
| — | **TỔNG CỘNG TOÀN HỆ THỐNG** | — | **432** | **429** | **3** | **0** | **99.31%** |

## 3. Tóm tắt các lỗi và sai lệch phát hiện

Chi tiết đầy đủ các bước tái hiện, nguyên nhân gốc rễ trong mã nguồn và bằng chứng đã che dữ liệu được trình bày tại [**`01-bao-cao-loi.md`**](01-bao-cao-loi.md):

1. **`BUG-001` (`SYS-03.1`, `SYS-03.2` — Không đạt, mức độ `Minor`):** Backend sinh đúng header `X-Request-Id` (UUID mới cho từng yêu cầu) và giữ sạch log (không lộ Cookie, Authorization, email, mật khẩu), nhưng `RequestIdFilter` chỉ đặt `requestId` vào SLF4J MDC mà không phát sinh dòng log cho các yêu cầu `200 OK` hoặc `401 Unauthorized`, dẫn tới không tìm thấy `X-Request-Id` trong tệp log ứng dụng.
2. **`BUG-002` (`ROUTE-20.ADMIN` — Không đạt do lệch đặc tả sau PR #18, mức độ `Minor`):** Tài liệu `tests/cases/routes.md` kỳ vọng vai trò `ADMIN` bị chặn `403` tại `/accountant/fee-rates`, trong khi mã nguồn `frontend/src/App.tsx:131` đã mở quyền xem chỉ đọc (`"Đơn giá các loại phí"`) cho `ADMIN` theo Quyết định `D25` trong `docs/DECISIONS.md` (đồng nhất với `FEE-08.1` và `RBAC-20.1`).
3. **`BUG-003` (Hạ tầng kiểm thử trong `tests/README.md`, mức độ `Minor`):** Các script `tools/dev/dev-all.sh`, `tools/dev/dev-close.sh`, `tools/dev/seed-fee-dev.sh` được nêu trong `tests/README.md` không tồn tại trên nhánh `main` (kho mã hiện đặt script trong `scripts/`).

## 4. Tổng hợp giải đáp các "Điểm chưa xác định" trong bộ tài liệu kiểm thử

- **Phản hồi khi thiếu/sai CSRF token (`AUTH-07.1`, `RBAC-extra.csrf-*`):** Trả HTTP `403` với thân JSON `ProblemDetail`: `{"type":"about:blank","title":"Không có quyền truy cập","status":403}` (không kèm trường `code`).
- **Mã thoát & thông báo CLI Bootstrap Admin (`AUTH-10.1`, `AUTH-10.2`):** Cả khi tạo mới ADMIN và khi bỏ qua do đã có ADMIN đều thoát với mã `0`; thông báo in ra lần lượt là `"Đã tạo ADMIN."` và `"Đã có ADMIN, bỏ qua."`, mật khẩu nhập vào không bị hiển thị lại trên màn hình.
- **Nhãn giao diện trang Hồ sơ (`AUTH-11.1`):** Các thẻ `<dt>` hiển thị chính xác gồm `"Họ tên"`, `"Vai trò"`, `"Tên đăng nhập"` (cùng `"Căn hộ"` nếu cư dân có dữ liệu hộ đã nạp), hoàn toàn không có ô nhập chỉnh sửa.
- **Hành vi trang Danh sách tài khoản sau tải lại (`ACC-04.2`):** Hiển thị tiêu đề trạng thái rỗng `"Chưa có tài khoản"` kèm ghi chú giải thích chưa có API liệt kê tài khoản từ máy chủ; dữ liệu trong bảng `users` vẫn được bảo toàn.
- **Mã lỗi Validation & PATCH rỗng hộ gia đình (`HH-01.2`, `HH-04.2`):** Trả HTTP `400 Bad Request` (`detail: "Dữ liệu gửi lên không hợp lệ."`) không có trường `code` riêng.
- **Endpoint sức khỏe thành phần con (`RBAC-1.2-health-child.*`):** `GET /actuator/health/db` trả HTTP `404` với thân rỗng trên mọi vai trò nhờ cấu hình `show-details: never`, không làm lộ thông tin cơ sở dữ liệu.

## 5. Nhận xét chung và kiến nghị chất lượng

- **Điểm mạnh nổi bật của hệ thống:**
  - **Bảo mật phiên & phân quyền rất chặt chẽ:** Đạt **156/156 (100%)** ca kiểm thử ma trận RBAC cấp API (`rbac.md`), bao gồm chống truy cập chéo hộ gia đình (`404`/`403`), chống cố định phiên khi đăng nhập, thu hồi tức thời toàn bộ phiên đang mở khi vô hiệu tài khoản hoặc đổi mật khẩu, khóa tạm 1 phút sau 5 lần đăng nhập sai, và buộc bật CSRF trên mọi thao tác ghi.
  - **Độ chính xác tính toán nghiệp vụ & toàn vẹn giao dịch:** Đạt **100%** trên toàn bộ các phân hệ Hộ gia đình (`11/11`), Nhân khẩu (`15/15`), Khai báo tạm vắng (`21/21`), Thu phí & Hóa đơn (`30/30`), và Luồng đầu-cuối E2E (`3/3`). Phép tính `BigDecimal` làm tròn `HALF_UP`, miễn giảm theo số ngày tạm vắng trong kỳ, khóa `Idempotency-Key` chống thu tiền trùng, và ghi nhận `audit_log` trong cùng giao dịch đều vận hành chính xác.
  - **An toàn dữ liệu & chống rò rỉ bí mật:** Không có mật khẩu tạm hay PII nào xuất hiện trong log ứng dụng; bản build Production ẩn triệt để bảng công cụ Demo (`UI-04.2`); giao diện đáp ứng tốt ở chiều rộng di động `360px` không tràn ngang (`UI-05.1`, `UI-05.2`).
- **Kiến nghị trước mốc nghiệm thu tiếp theo:**
  1. Bổ sung ghi log truy vết tối giản (chỉ chứa `method`, `URI`, `status`, `durationMs` gắn kèm `MDC requestId`) trong `RequestIdFilter` để khắc phục `BUG-001`.
  2. Cập nhật kỳ vọng của `ROUTE-20.ADMIN` trong `tests/cases/routes.md` theo Quyết định `D25` (`BUG-002`) và đồng bộ đường dẫn script trong `tests/README.md` (`BUG-003`).
  3. Cập nhật dòng ghi chú cũ tại `CreateAccountPage.tsx:239` (`OBS-01`).
