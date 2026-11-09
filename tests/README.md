# Hướng dẫn kiểm thử thủ công

Tài liệu này hướng dẫn chạy và ghi nhận các ca trong [cases/](cases/). Danh sách ca chỉ là kế hoạch kiểm thử; không thể hiện rằng ca đã được thực thi. Kết quả chạy thực tế được ghi trong [report/](report/) và [docs/VALIDATION.md](../docs/VALIDATION.md).

## Chuẩn bị

1. Theo hướng dẫn chạy local ở [README gốc](../README.md). Dùng account và dữ liệu giả.
2. Chỉ dùng database local dành riêng cho kiểm thử. Integration tests có thể ghi fixture và chạy migration; không dùng DB chung hoặc dữ liệu cư dân thật.
3. Không ghi mật khẩu, token, cookie, email thật hoặc thông tin định danh vào ảnh/log/báo cáo. Dùng placeholder như ADMIN_PASSWORD và che dữ liệu trong bằng chứng.
4. Không dùng docker compose down -v, không xóa volume và không reset DB để khắc phục test lỗi. Ghi tình trạng Blocked nếu không có DB test an toàn.

## Chạy bộ ca

- Chọn bộ phù hợp trong cases/: auth.md, accounts.md, households.md, residents.md, declarations.md, billing.md, voluntary.md, fees.md, routes.md, ui-common.md, rbac.md, e2e.md.
- Làm theo đúng tiền điều kiện của từng ca. Dùng mã gốc và nhánh ca, ví dụ AUTH-03 hoặc AUTH-03.1.
- Ghi một trong các trạng thái: **Pass**, **Fail**, **Blocked**, **Not run**. Ghi môi trường, bước đã chạy, kết quả kỳ vọng/thực tế và bằng chứng đã che dữ liệu.
- Không đánh dấu Pass chỉ vì endpoint, component hoặc test case tồn tại.

## Khi gặp lỗi

Dùng [templates/bug-report.md](templates/bug-report.md). Ghi bước tái hiện tối thiểu, dữ liệu giả, kết quả mong đợi/thực tế, mức độ nghiêm trọng và log/ảnh đã làm sạch. Không gửi bí mật hoặc dữ liệu thật.

Mức độ: **Blocker** chặn luồng chính; **Critical** sai quyền, lộ hoặc mất dữ liệu; **Major** sai chức năng chính; **Minor** lỗi hiển thị/nội dung ít ảnh hưởng.

## Giới hạn đã biết

Các ca S09 hiện có trong bộ unit/API và component tests; chưa có browser E2E walkthrough nối UI với API thật. Kết quả test tự động mới nhất và các giới hạn theo từng task được ghi ở [docs/VALIDATION.md](../docs/VALIDATION.md).
