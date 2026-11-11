# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

React 19 + TypeScript + Vite (dev server port 5173, `/api` proxy to `http://localhost:8080`). Stylesheets: `src/styles/tokens.css` (one token set), `app.css`, `utilities.css`.

## Users

- **Ban quản trị (`ADMIN`):** Thành viên Ban quản trị / Ban quản lý chung cư BlueMoon làm việc trên máy tính tại văn phòng BQL. Chịu trách nhiệm cấp và khóa tài khoản, quản lý hồ sơ hộ gia đình và nhân khẩu, duyệt khai báo tạm vắng, ghi nhận chuyển đi/lưu trữ, mở đợt đóng góp tự nguyện, tạo kỳ thu hàng loạt (chờ chốt quy tắc chi tiết), xuất báo cáo lưu trú và phê duyệt gói dữ liệu cho cơ quan chức năng.
- **Kế toán (`ACCOUNTANT`):** Kế toán chung cư làm việc trên máy tính văn phòng, đặc biệt cao điểm vào cuối tháng. Thiết lập đơn giá theo loại phí, tra cứu mã căn hộ để ghi nhận thu tiền mặt và gạch nợ, xuất báo cáo tỷ lệ thu theo tháng. Không xem hồ sơ nhân khẩu chi tiết (quyền xem hộ của Kế toán đang ở trạng thái chờ chốt).
- **Cư dân (`RESIDENT`):** Chủ hộ hoặc thành viên đại diện hộ gia đình tại BlueMoon (nhiều người dùng lớn tuổi, thao tác chủ yếu bằng một tay trên điện thoại màn hình ~360 px). Gắn cố định với đúng một hộ (`users.household_id`). Kiểm tra số tiền còn nợ, hạn nộp, xem chi tiết thành phần hóa đơn và lịch sử đóng phí của hộ mình, kiểm tra danh sách nhân khẩu trong hộ, gửi đơn khai báo tạm vắng và xem các đợt đóng góp tự nguyện.
- **Cơ quan chức năng (bên nhận ngoài hệ thống, theo D13):** Không có vai trò hay tài khoản trong hệ thống. Ban quản trị tiếp nhận yêu cầu, kiểm tra giấy phép và bàn giao gói dữ liệu ngoài hệ thống; ứng dụng chỉ cho phép Ban quản trị tạo, xem trước, duyệt và kiểm toán gói dữ liệu theo danh sách trường cho phép (whitelist), tuyệt đối không truy cập dữ liệu thô.

## Product Purpose

Số hóa quy trình quản lý thu phí định kỳ, các đợt vận động đóng góp tự nguyện và quản lý cư trú (hộ khẩu, nhân khẩu, tạm vắng, chuyển đi) tại Chung cư BlueMoon (ngã tư Văn Phú, 30 tầng: 1 tầng kiot, 4 tầng đế, 24 tầng nhà ở, 1 tầng penthouse), thay thế sổ ghi chép tay và file Excel rời rạc. Thành công ở phiên bản v1 nghĩa là các luồng cốt lõi chạy chính xác theo phân quyền server-side, không thu trùng hoặc lệch tiền, tách biệt rõ công nợ bắt buộc với đóng góp tự nguyện, và người dùng hoàn thành thao tác lặp lại thường xuyên nhất tối đa trong 3 bước chạm.

## Positioning

Hệ thống vận hành nội bộ chuyên biệt cho Ban quản trị chung cư tự quản tại Việt Nam: kết hợp quản lý nhân khẩu/tạm vắng với thu phí tính theo diện tích căn hộ (`BigDecimal` snapshot đơn giá × diện tích lúc phát hành) và quỹ đóng góp tự nguyện không cộng gộp vào công nợ bắt buộc, bảo vệ bằng phiên server-side JDBC thu hồi tức thời và kiểm toán (`audit_log`) cùng giao dịch.

## Operating Context

- Văn phòng Ban quản lý Chung cư BlueMoon thu phí trực tiếp bằng tiền mặt theo mã căn hộ (ví dụ `A01`, `A02`, `B05`) và đối chiếu công nợ cuối tháng; chưa dùng thanh toán trực tuyến hay mã QR ở v1.
- Cư dân không tự đăng ký tài khoản và không có luồng quên mật khẩu tự phục vụ ở v1: Ban quản trị tạo tài khoản, gán hộ và gửi mật khẩu tạm qua kênh email mô phỏng (`SIMULATED`); lần đăng nhập đầu tiên với cờ `mustChangePassword = true` bị chặn mọi thao tác khác cho tới khi đổi mật khẩu.
- Giao diện chạy hai chế độ trong Sprint 1. (1) Backend Spring Boot thật: giao diện đã ghép `/api/v1/auth/*` và `POST /api/v1/accounts`. Từ 06/11/2026 `main` còn có API hộ (`/api/v1/households`) và nhân khẩu (`/households/{id}/residents`, `/residents/{id}`, `/residents/{id}/move-out`, `/households/{id}/residency-changes`, `/households/me/residents`) theo D20, D21; giao diện đã ghép các API này ở mức mã. (2) Chế độ trình diễn dựa trên dữ liệu mẫu (`mock-data.json`) cho phần chưa ghép; S03–S06 đã ghép ở mức mã. Hợp đồng vô hiệu, bật lại và mở khóa account của S04 (`POST /api/v1/accounts/{id}/disable|enable|unlock`, ADMIN, 204) đã được duyệt nhưng chưa vào `main`. Nhãn nội bộ phân biệt trạng thái hợp đồng (API thật, đã có cột DB, giả định, chờ chốt) chỉ hiện ở chế độ DEV hoặc Demo; người dùng thật không thấy.

## Capabilities and Constraints

- **Phạm vi v1 bắt buộc (24 mã màn hình `M-xx` chia 3 mức A/B/C):**
  - Mức A (8 màn lõi, đầy đủ trạng thái; `M-01`, `M-31`, `M-33` đủ 8 trạng thái; biến thể mobile 360 px cho cư dân): `M-01` Đăng nhập, `M-02` Khung ứng dụng & menu động, `M-03` Hồ sơ & đổi mật khẩu, `M-04` Trang lỗi/phiên hết hạn (401/403/404/500), `M-30` Hộ gia đình của tôi, `M-31` Khai báo tạm vắng, `M-32` Hóa đơn & lịch sử (trang đầu cư dân), `M-33` Chi tiết hóa đơn.
  - Mức B (khung bảng, biểu mẫu, hành động chính + bình thường/rỗng/lỗi): `M-10` Danh sách tài khoản, `M-11` Tạo tài khoản (đã có API `POST /api/v1/accounts`), `M-12` Danh sách hộ, `M-13` Chi tiết hộ, `M-14` Thêm/sửa hộ & nhân khẩu, `M-15` Chuyển đi & lưu trữ, `M-16` Duyệt khai báo tạm vắng.
  - Mức C (khung "chờ chốt" nêu rõ mục đích, trường giả định và câu hỏi mở, các nhãn này chỉ hiện ở DEV/Demo): `M-17` Đợt đóng góp tự nguyện, `M-18` Báo cáo lưu trú, `M-19` Đóng gói báo cáo cho cơ quan, `M-20` Đơn giá phí, `M-21` Thu tiền mặt, `M-22` Báo cáo tỷ lệ thu, `M-24` Tạo kỳ thu/phát hành hóa đơn, `M-34` Xem đợt đóng góp tự nguyện, `M-40` Báo cáo đã duyệt cho cơ quan.
- **Tuyệt đối ngoài phạm vi v1 (không vẽ, không tạo nút chờ):** Đăng nhập VNeID, tự đăng ký tài khoản, quên mật khẩu tự phục vụ, thanh toán QR/VietQR, quản lý khách thăm, phản ánh/khiếu nại sự cố, mượn đồ, thông báo khẩn, nhắc nợ tự động, phí gửi xe/điện/nước/internet (thuộc v2.0).
- **Ràng buộc bảo mật & dữ liệu trên UI:**
  - Server session qua cookie `HttpOnly` (`credentials: 'include'`), tuyệt đối không lưu session/token trong `localStorage` hay `sessionStorage`.
  - Mọi lệnh ghi (`POST`/`PUT`/`PATCH`/`DELETE`) gửi kèm `X-CSRF-TOKEN` lấy từ `GET /api/v1/auth/csrf`, lấy lại token mới ngay sau `login` và sau `logout`. Khi lệnh ghi gặp `403`, kiểm tra `GET /api/v1/auth/me` để phân biệt hết phiên (`401` -> về đăng nhập nhưng giữ nội dung form đang gõ) với thiếu quyền thật sự hoặc `PASSWORD_CHANGE_REQUIRED`.
  - Mọi lỗi đăng nhập `401` chỉ hiển thị đúng một thông điệp chung *"Tên đăng nhập hoặc mật khẩu không đúng."* kèm hướng dẫn liên hệ Ban quản lý; không đoán lý do khóa hay tồn tại tài khoản, không hứa "thử lại sau ít phút".
  - Không dùng `float`/`double` để cộng/nhân tiền trên giao diện; tiền tệ hiển thị định dạng `500.000 đ`, diện tích `50,00 m²`, ngày `dd/MM/yyyy`, thời điểm `dd/MM/yyyy HH:mm` (múi giờ `Asia/Ho_Chi_Minh`), kỳ thu `MM/yyyy`.
  - Mặc định che CCCD (chỉ hiện 4 số cuối `********1234`), không lộ số giấy tờ hoặc chi tiết lỗi thô 5xx (chỉ hiện mã `X-Request-Id` khi có).
- **Các quyết định nghiệp vụ còn mở (ghi nhận rõ trên UI, không tự bịa thành quy tắc cứng):** Q1 và Q2 đã được trả lời trong D20/D24; còn danh mục loại phí chính thức giữa phí quản lý/dịch vụ/rác (Q6), hiệu lực đơn giá & làm tròn (Q7), thu thiếu/thừa/sửa/hủy giao dịch (Q8), công thức mẫu số tỷ lệ thu (Q10), mẫu cột pháp lý báo cáo lưu trú (Q17).

## Brand Commitments

- Tên hệ thống hiển thị: **BlueMoon Complex** (Chung cư BlueMoon — Ngã tư Văn Phú).
- Ngôn ngữ giao diện: **Tiếng Việt** rõ ràng, ngắn gọn, hành chính - dân sự chuẩn mực, thông báo lỗi luôn nêu chuyện gì xảy ra và bước làm tiếp theo.

## Evidence on Hand

- Đặc tả màn hình và luồng: bộ tài liệu `ui-wireframe-guide` (`01`–`11`) và dữ liệu giả chuẩn hóa `mock-data.json` (không chứa PII thật).
- Kiến trúc, yêu cầu và quyết định đã duyệt: `docs/REQUIREMENTS.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md` (D01–D21), `docs/sprint-1/TASKS.md`.
- Hợp đồng API chạy thật trên nhánh `main`: `AuthController` (`/api/v1/auth/{csrf,login,logout,me}`), `AccountController` (`POST /api/v1/accounts`), `HouseholdController`, `ResidentController` (S05), `MustChangePasswordFilter`, `GlobalExceptionHandler` và schema Flyway `V1`–`V6` (S05 và S04 không thêm migration).
- Không có ảnh chụp tòa nhà thật, không có số điện thoại thực của BQL (giao diện không in số nào cho tới khi Ban quản trị cung cấp), không có chứng nhận mẫu biểu pháp lý.

## Product Principles

1. **Trung thực với hợp đồng dữ liệu:** Đội phát triển phân biệt rạch ròi tính năng đã ghép API thật, đã có bảng DB, giả định giao diện và quy tắc chờ chốt (nhãn chỉ hiện ở DEV/Demo, không hiện cho người dùng thật); không bao giờ tự tính tiền bằng số thực hoặc tự gán quyền ở client.
2. **Tách bạch nghĩa vụ và tự nguyện:** Mọi khoản đóng góp tự nguyện luôn mang nhãn khẳng định không cộng vào công nợ bắt buộc của hộ gia đình.
3. **Ưu tiên cư dân lớn tuổi trên di động và tốc độ bàn thu cuối tháng:** Màn cư dân dùng tốt một tay ở chiều rộng 360 px với thanh điều hướng đáy 3 tab và cỡ chữ dễ đọc; màn BQT/Kế toán ưu tiên tra cứu phân trang nhanh, phím Enter và hộp xác nhận nêu rõ đối tượng + hậu quả.
4. **An toàn phiên và dữ liệu cá nhân theo mặc định:** Che định danh cá nhân mặc định, giữ lại dữ liệu biểu mẫu khi phiên hết hạn để người dùng không mất công nhập lại, và khóa thao tác bằng hộp xác nhận động từ cụ thể cho mọi hành động khó hoàn tác.

## Accessibility & Inclusion

- Tương phản chữ tối thiểu 4,5:1 (chữ lớn ≥ 3:1); trạng thái luôn hiển thị bằng nhãn chữ tiếng Việt đi kèm màu/ký hiệu, không bao giờ chỉ dùng màu sắc.
- Điều hướng bàn phím đầy đủ, viền focus rõ ràng, hộp thoại xác nhận giữ focus và đóng được bằng phím `Esc` (trả focus về nút mở).
- Vùng chạm tối thiểu 44×44 px trên thiết bị di động (~360 px), không cuộn ngang toàn trang, phóng chữ 200% không vỡ bố cục.
