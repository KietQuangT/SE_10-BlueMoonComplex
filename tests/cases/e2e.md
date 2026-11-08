# Luồng xuyên suốt

Dùng dữ liệu giả, phiên riêng cho từng vai trò và CSRF hợp lệ. Ghi Pass/Fail/Blocked/Not run, kết quả thực tế và bằng chứng đã che dữ liệu ở từng bước.

## E2E-01 — Từ tạo hộ đến ghi nhận quay lại

1. ADMIN tạo hộ ACTIVE có diện tích hợp lệ; kỳ vọng mã hộ mới xuất hiện trong chi tiết hộ.
2. ADMIN thêm nhân khẩu RESIDING vào hộ; kỳ vọng nhân khẩu xuất hiện trong danh sách hộ.
3. ADMIN tạo tài khoản RESIDENT gắn hộ; RESIDENT đăng nhập, đổi mật khẩu; kỳ vọng đổi trả 204, phiên bị hủy, đăng nhập lại thành công.
4. RESIDENT gửi khai báo tạm vắng cho nhân khẩu trong hộ; kỳ vọng đơn PENDING.
5. ADMIN duyệt đơn; kỳ vọng đơn APPROVED và nhân khẩu TEMPORARILY_ABSENT.
6. ADMIN ghi nhận quay lại; kỳ vọng nhân khẩu RESIDING, lịch sử có RETURN ngày hôm nay do server đặt.

## E2E-02 — Vòng đời đơn giá

1. ACCOUNTANT thêm đơn giá hợp lệ cho tháng sau và loại phí chưa có; kỳ vọng danh sách có dòng mới.
2. ACCOUNTANT sửa giá dòng tháng sau; kỳ vọng danh sách hiển thị giá mới và `editable` cho phép sửa.
3. ACCOUNTANT sửa đơn giá tháng hiện tại; kỳ vọng 409 `FEE_RATE_FROZEN`, giá không đổi.

## E2E-03 — Thu hồi phiên khi vô hiệu hóa

1. Đăng nhập tài khoản thử nghiệm không phải ADMIN cuối cùng và giữ phiên; kỳ vọng `/auth/me` trả thông tin tài khoản.
2. ADMIN khác vô hiệu hóa tài khoản đó; kỳ vọng 204.
3. Dùng phiên cũ gọi `/auth/me`; kỳ vọng bị từ chối, không trả thông tin người dùng.

## Điểm chưa xác định

Outline không quy định chính xác mã HTTP khi dùng phiên bị hủy; chỉ xác nhận request bị từ chối, không suy đoán mã.
