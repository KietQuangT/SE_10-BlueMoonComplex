# Vị trí frontend

Chưa chọn framework hoặc khởi tạo ứng dụng frontend trong gói backend này.
Kiệt quản lý phần này. API local ở http://localhost:8080/api/v1.
Dev server dự kiến http://localhost:5173; phải xác nhận trước khi dựng frontend.
Định hướng: frontend dùng cùng origin /api khi đóng gói, dev dùng proxy /api.
Nếu gọi chéo origin: gửi credentials và CSRF token; không lưu session/token trong localStorage.
Chưa có endpoint login trong skeleton; không coi GET /system/info là auth đã hoàn tất.
