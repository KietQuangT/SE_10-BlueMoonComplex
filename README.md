# SE_10 — BlueMoon Complex

Backend skeleton và hướng dẫn để nhóm bắt đầu phát triển v1.0. **Chưa triển khai nghiệp vụ Sprint 1.** Nền tảng hiện có: health/metadata, Flyway V1–V4, session JDBC, và audit cùng transaction (`common/audit`, xem `docs/ARCHITECTURE.md`) kèm thao tác mẫu `identity/application/RoleService` (chưa có kiểm tra quyền, không gắn controller; S01 thay hoặc xóa).

## Khởi động nhanh bằng Docker
Yêu cầu Git và Docker Engine/Desktop có Compose v2.
```bash
cp .env.example .env
# Mở .env, thay hai mật khẩu local.
docker compose up --build -d
docker compose logs -f backend
```
Mở terminal khác:
```bash
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/api/v1/system/info
```
Health kiểm tra trạng thái ứng dụng/DB và không công bố chi tiết. `/actuator/health/liveness` chỉ kiểm tra sống; không dùng riêng liveness để kết luận DB sẵn sàng.
Chưa có login, tài khoản demo, API hộ hoặc tính phí. Các endpoint khác bị chặn mặc định.

## Phát triển bằng IDE
Cài JDK 21; Maven Wrapper tự tải Maven 3.9.11 lần đầu, cần Internet. Build chỉ chấp nhận đúng JDK 21 (enforcer báo "Dự án dùng JDK 21" nếu `java` mặc định là bản khác); khi đó đặt `JAVA_HOME` trỏ tới JDK 21 trong shell chạy `./mvnw`.
```bash
docker compose up -d db
export DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=bluemoon DB_USER=bluemoon
export DB_PASSWORD='mat_khau_ban_da_dat_trong_env'
cd backend
./mvnw spring-boot:run
```
Không chạy backend Docker và IDE cùng cổng 8080. Nếu `.env` đổi DB_PORT, đổi DB_PORT tương ứng trong IDE. `.env` không được Maven/IDE nạp tự động.
PowerShell dùng `$env:DB_PASSWORD="..."` và `./mvnw.cmd`; không paste credential vào chat hoặc commit.

## Test / build
```bash
cd backend
./mvnw -B -ntp verify
./mvnw -B -ntp -Pintegration verify
```
Lệnh đầu chạy test MVC/security và unit test audit, đóng gói, không cần DB. Lệnh thứ hai cần MySQL thật và biến DB_*; chạy cả unit và integration (`BootstrapIT`, `RoleAuditIT`), không bỏ qua khi DB thiếu. Flyway tự áp V1–V4 lên DB trống. `RoleAuditIT` đổi tên role seed ADMIN rồi khôi phục, nên không chạy trên DB có người khác đang dùng. GitHub CI cấu hình MySQL riêng.
Nếu `verify` báo "Unresolved compilation problems" (IDE ghi đè `backend/target/`), chạy `./mvnw clean verify`.
Kết quả thực sự đã kiểm tra trong môi trường soạn: `docs/VALIDATION.md`.

## Dừng và dữ liệu
```bash
docker compose down
```
Volume vẫn giữ dữ liệu. Không chạy `down -v` nếu chưa xác nhận mất dữ liệu. Thay mật khẩu trong `.env` không đổi password của volume MySQL đã tồn tại; thực hiện thay password có kiểm soát thay vì xóa volume.

## Hướng dẫn nhóm
- Đọc `AGENTS.md` trước khi yêu cầu Codex sửa code.
- Kiệt: phối hợp repo/frontend/API; Sơn: nền tảng và tài khoản/cư trú; Quân: phí/kỳ thu/giao dịch; Tuấn: DB, báo cáo và QA.
- Người viết nghiệp vụ cũng viết test; Tuấn không gánh toàn bộ sửa lỗi.
- Tạo branch `feat/<ma-task>-<mo-ta>`, PR vào main, yêu cầu review và CI. Xem `docs/CONTRIBUTING.md`.
- Kiến trúc: `docs/ARCHITECTURE.md`; kế hoạch: `docs/sprint-1/PLAN.md`; checklist: `docs/sprint-1/TASKS.md`.
- Nguồn và mâu thuẫn: `docs/SOURCE_REVIEW.md`; quyết định: `docs/DECISIONS.md`.

## Database

The initial MySQL database design is in [db/](./db/). Run the scripts in
numeric order: `01_schema.sql`, `02_seed.sql`, then `03_test.sql`.
