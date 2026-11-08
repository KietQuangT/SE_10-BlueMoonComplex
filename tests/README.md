# Hướng dẫn kiểm thử thủ công

1. Chuẩn bị môi trường phát triển theo README dự án: Java 21, Maven wrapper, Node.js, MySQL 8.4 và Docker Compose; dùng cơ sở dữ liệu thử nghiệm riêng. Không dùng dữ liệu cư dân thật; không ghi mật khẩu thật, token hoặc cookie vào tài liệu hay bằng chứng. Dùng placeholder như `<ADMIN_PASSWORD>`.
2. Đường dẫn được yêu cầu cho stack là `tools/dev/dev-all.sh` và `tools/dev/dev-close.sh`; nạp đơn giá bằng `tools/dev/seed-fee-dev.sh`. Các script này nằm trong `tools/dev/` và có khi nhánh tài liệu được merge; nếu chưa có thì dùng README mục 4.6 cho bootstrap và báo quản trị viên. Khi script chưa có, đánh dấu bước chạy stack hoặc nạp dữ liệu là Blocked. Không chạy `docker compose down -v` hoặc xóa volume.
3. Bootstrap ADMIN qua console tương tác theo README mới mục 7.3; nếu chưa có bản mới, dùng mục 4.6 của README hiện tại. Không có tài khoản mặc định. ADMIN dùng màn tạo tài khoản để tạo ACCOUNTANT và RESIDENT; RESIDENT phải gắn hộ ACTIVE. Mật khẩu tạm chỉ có trong email mô phỏng, không ghi lại giá trị.
4. Chạy ca trong `cases/`; mã gốc như `AUTH-03`, nhánh như `AUTH-03.1`. Ghi Pass, Fail, Blocked hoặc Not run, kết quả thực tế và bằng chứng đã che dữ liệu; mã phủ chỉ chứng minh ca được liệt kê, không chứng minh đã chạy.
5. Khi Fail, dùng `templates/bug-report.md`: ghi môi trường, dữ liệu giả, bước tối thiểu, kỳ vọng, thực tế và ảnh đã che thông tin nhạy cảm; gửi người phụ trách qua kênh dự án. Không gửi bí mật.

## Mức độ nghiêm trọng

- **Blocker:** chặn toàn bộ việc kiểm hoặc luồng chính không có cách tiếp tục.
- **Critical:** mất dữ liệu, lộ dữ liệu hoặc sai quyền nghiêm trọng.
- **Major:** chức năng chính sai, có thể kiểm phần khác.
- **Minor:** lỗi hiển thị hoặc nội dung ít ảnh hưởng nghiệp vụ.

## Điểm chưa xác định

Ba script `tools/dev/` không có trong worktree hiện tại; chưa thể xác nhận cách chạy/dừng stack hoặc nạp dữ liệu theo các đường dẫn đó. README hiện có hướng dẫn bootstrap mục 4.6 thay vì 7.3.
