# BlueMoon Complex — SE_10

BlueMoon là ứng dụng web nội bộ phục vụ Ban quản trị, Kế toán và Cư dân trong việc quản lý hộ, nhân khẩu, khoản thu, công nợ và các nghiệp vụ cư trú. Repo là monorepo gồm backend Java/Spring Boot, frontend React và MySQL.

> **Trạng thái nhánh S09:** nhánh này gồm nghiệp vụ thu tiền/công nợ và giao diện Kế toán/Admin. S09 đã qua backend unit/API + MySQL integration tests và frontend tests/build; chưa có browser E2E hoặc CI remote. Checklist chính thức vẫn ở [docs/sprint-1/TASKS.md](docs/sprint-1/TASKS.md).

## Công nghệ

| Phần | Công nghệ |
| --- | --- |
| Backend | Java 21, Spring Boot 4.0.9, Maven |
| Frontend | React 19, TypeScript 5.7, Vite 6 |
| Database | MySQL 8.4 |
| Schema | Flyway; JPA dùng ddl-auto=validate |
| Xác thực | Spring Security, JDBC session, CSRF |
| Frontend tests | Vitest, Testing Library, jsdom |

## Chức năng trong mã nguồn nhánh này

- **Tài khoản và phiên:** đăng nhập/đăng xuất, đổi mật khẩu, cấp và quản lý account; kiểm tra vai trò và trạng thái account ở server.
- **Hộ và cư trú:** quản lý hộ/nhân khẩu, chuyển đi, khai báo tạm vắng, duyệt/từ chối và ghi nhận đã quay lại.
- **Phí và hóa đơn:** đơn giá có hiệu lực theo kỳ, xem trước phí, tạo kỳ thu và lưu snapshot hóa đơn.
- **Thu tiền S09:** thu một phần/toàn bộ hoặc thừa; phần dư thành credit. ACCOUNTANT phân bổ credit FIFO, hủy receipt hoặc đảo phân bổ. ADMIN chỉ xem. Giao dịch dùng UUID idempotency và ghi audit cùng transaction.
- **Frontend:** API mode dùng backend thật ở các màn đã nối; Demo mode tiếp tục dùng dữ liệu mẫu. Kế toán dùng /accountant/collections; Admin dùng /admin/collections.

Phạm vi sản phẩm và trạng thái task có thể khác nhau; xem [docs/DECISIONS.md](docs/DECISIONS.md), [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) và [docs/sprint-1/TASKS.md](docs/sprint-1/TASKS.md). Các phần báo cáo, sao lưu, kiểm thử toàn hệ thống và bàn giao vẫn là task riêng.

## Chạy local

Yêu cầu: JDK 21, Node.js, Docker và Docker Compose.

1. Tạo cấu hình local:

   ~~~bash
   cp .env.example .env
   ~~~

   Đổi mật khẩu trong .env; không commit file này.

2. Chạy MySQL và backend:

   ~~~bash
   docker compose up --build -d
   curl -fsS http://localhost:8080/actuator/health
   ~~~

3. Chạy frontend trong terminal khác:

   ~~~bash
   cd frontend
   npm ci
   npm run dev
   ~~~

   Mở http://localhost:5173. Dev server proxy /api và /actuator tới backend cục bộ.

Không có account mặc định. Tạo ADMIN đầu tiên bằng profile bootstrap trong terminal tương tác sau khi DB đã sẵn sàng. Bootstrap dùng DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD trong môi trường của tiến trình backend; .env của Compose không tự nạp vào Maven/IDE. Xem hướng dẫn môi trường và bootstrap trong [AGENTS.md](AGENTS.md).

Với DB mới, đóng gói backend rồi chạy bootstrap từ terminal tương tác. Trước lệnh cuối, export các biến DB_* trỏ tới MySQL local đã chạy ở trên:

~~~bash
cd backend
./mvnw -B -ntp package
java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap
~~~

Không có mật khẩu mặc định; bootstrap yêu cầu nhập thông tin ADMIN qua console.

Dừng môi trường bằng docker compose down; lệnh này giữ volume MySQL. Không dùng docker compose down -v nếu chưa chủ động chấp nhận xóa dữ liệu local.

## Kiểm thử

Backend unit/API:

~~~bash
cd backend
./mvnw -B -ntp verify
~~~

Backend integration cần MySQL test riêng và các biến DB_*:

~~~bash
cd backend
./mvnw -B -ntp -Pintegration verify
~~~

Frontend:

~~~bash
cd frontend
npm test
npm run build
~~~

Integration tests ghi/xóa fixture và chạy migration; chỉ dùng database dành riêng cho kiểm thử. Không chạy trên database dùng chung hoặc có dữ liệu thật. Ghi kết quả và giới hạn vào [docs/VALIDATION.md](docs/VALIDATION.md).

### Bằng chứng S09 gần nhất

- Backend verify: 227 test pass.
- MySQL 8.4 test cô lập, profile integration: 146 test pass.
- Frontend: 134 test pass; TypeScript/Vite build pass.
- Chưa walkthrough E2E từ browser qua API thật, chưa CI remote, chưa kiểm tra hiệu năng 10.000 hồ sơ. Chi tiết và phạm vi từng lượt nằm trong [docs/VALIDATION.md](docs/VALIDATION.md).

## Cấu trúc repo

~~~text
backend/             Spring Boot API, migrations và tests
frontend/            React app, API client và UI tests
docs/                quyết định, yêu cầu, kiến trúc, kế hoạch và validation
tests/cases/         bộ ca kiểm thử nghiệp vụ
tests/report/        báo cáo kết quả kiểm thử theo từng bộ ca
scripts/             script hỗ trợ môi trường local
compose.yaml         MySQL và backend local
~~~

## Tài liệu

- [AGENTS.md](AGENTS.md): quy tắc repo và quy trình phát triển.
- [docs/DECISIONS.md](docs/DECISIONS.md): quyết định nghiệp vụ/kỹ thuật đã duyệt và điểm còn mở.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): kiến trúc và quy ước API.
- [docs/sprint-1/PLAN.md](docs/sprint-1/PLAN.md), [docs/sprint-1/TASKS.md](docs/sprint-1/TASKS.md): kế hoạch và checklist.
- [docs/VALIDATION.md](docs/VALIDATION.md): lệnh, kết quả test và giới hạn.
- [frontend/README.md](frontend/README.md): phát triển frontend.
- [tests/README.md](tests/README.md): ghi nhận ca kiểm thử thủ công.

## Quy ước đóng góp

Làm trên feature branch và mở PR vào main. Mọi thay đổi schema dùng migration mới V<n>__mo_ta.sql; không sửa migration đã chia sẻ. Bảo vệ quyền ở backend, không chỉ ẩn nút trên UI. Không commit .env, credential, PII, mật khẩu hoặc dữ liệu cư dân thật; không tắt CSRF để làm test pass.
