# BlueMoon Complex — SE_10

BlueMoon Complex là ứng dụng web quản lý vận hành chung cư trong đồ án SE_10. Phiên bản hiện tại tập trung vào tài khoản và phân quyền, hộ gia đình và nhân khẩu, cư trú, đơn giá, hóa đơn, thu tiền và công nợ. Các vai trò chính là Ban quản trị (`ADMIN`), Kế toán (`ACCOUNTANT`) và Cư dân (`RESIDENT`).

Repo là monorepo: API Spring Boot, giao diện React, MySQL và tài liệu nghiệp vụ nằm trong cùng một dự án. Trạng thái chính thức từng hạng mục nằm ở [docs/sprint-1/TASKS.md](docs/sprint-1/TASKS.md); README mô tả phạm vi hiện có trong mã nguồn, không thay thế checklist.

## Trạng thái hiện tại

S01–S09 đã có triển khai trong mã nguồn local `main`. S09 bổ sung thu một phần hoặc toàn bộ hóa đơn, ghi phần nộp thừa thành credit của hộ, phân bổ credit theo FIFO, hủy receipt/đảo phân bổ có lý do, idempotency và audit trong transaction. Kế toán có API mode và Demo mode ở `/accountant/collections`; ADMIN có màn chỉ đọc tại `/admin/collections`.

S09 nằm trên PR [#28](https://github.com/KietQuangT/SE_10-BlueMoonComplex/pull/28). Backend verify, integration, frontend tests và build đã qua; CI `verify` trên PR đã xanh. Chưa có browser E2E walkthrough với backend thật, nên task S09 vẫn chờ review và kiểm tra luồng trình duyệt. Xem bằng chứng và giới hạn trong [docs/VALIDATION.md](docs/VALIDATION.md).

Các task báo cáo, đóng gói hồ sơ thẩm quyền, sao lưu/khôi phục, regression/performance và bàn giao vẫn theo dõi riêng ở TASKS.md. Quy tắc đã duyệt và điểm nghiệp vụ còn mở nằm trong [docs/DECISIONS.md](docs/DECISIONS.md).

## Công nghệ

| Thành phần | Công nghệ |
| --- | --- |
| Backend | Java 21, Spring Boot 4.0.9, Maven Wrapper |
| Frontend | React 19, TypeScript 5.7, Vite 6 |
| Database | MySQL 8.4 |
| Migration | Flyway; Hibernate `ddl-auto=validate` |
| Bảo mật | Spring Security, JDBC session, cookie HTTP-only, CSRF |
| Kiểm thử | JUnit/Spring integration tests, Vitest, Testing Library |
| Local runtime | Docker Compose v2 |

## Phạm vi nghiệp vụ

- **Tài khoản và phiên:** đăng nhập/đăng xuất, session, khóa do đăng nhập sai, cấp/vô hiệu hóa/kích hoạt tài khoản và đổi mật khẩu.
- **Hộ và cư trú:** quản lý hộ/nhân khẩu, lịch sử biến động, chuyển đi, khai báo tạm vắng, duyệt/từ chối và ghi nhận cư dân đã quay lại.
- **Phí và hóa đơn:** quản lý đơn giá theo kỳ, xem trước phí, tạo kỳ thu và snapshot các dòng phí khi phát hành.
- **Thu bắt buộc S09:** nhiều receipt cho một hóa đơn; thu thừa trở thành credit hộ; phân bổ credit FIFO; hủy receipt và đảo phân bổ lưu lịch sử; ADMIN chỉ đọc.
- **Frontend:** có các màn hình v1; một số luồng dùng API thật, một số luồng vẫn là Demo. Chế độ Demo giữ bộ dữ liệu riêng, không ghi vào backend.

Backend kiểm tra quyền và phạm vi hộ ở server. Các thay đổi dữ liệu dùng transaction và audit nghiệp vụ; schema thay đổi qua migration Flyway mới. Không có tài khoản mặc định; ADMIN đầu tiên được tạo bằng profile bootstrap tương tác.

## Yêu cầu chạy local

- Git.
- Docker Desktop hoặc Docker Engine cùng Docker Compose v2.
- JDK 21 để chạy Maven/backend ngoài container.
- Node.js và npm để chạy frontend ngoài container.
- `curl` và `lsof` được script `dev-all.sh` sử dụng.

## Khởi động toàn bộ bằng `scripts/dev-all.sh`

Đây là lối chạy local gọn nhất. Mở terminal tại thư mục gốc repo:

```bash
cp .env.example .env
```

Đổi các mật khẩu local trong `.env`, sau đó chạy:

```bash
scripts/dev-all.sh
```

Script thực hiện lần lượt:

1. Chuyển working directory về thư mục gốc và yêu cầu có `.env`.
2. Kiểm tra các cổng `3306` (MySQL), `8080` (backend) và `5173` (Vite). Nếu có cổng bận, script in tiến trình/container liên quan rồi dừng; nó không tự tắt tiến trình.
3. Chạy `docker compose up --build -d`, khởi động MySQL 8.4 và backend Spring Boot; Compose đợi MySQL healthy trước khi chạy backend.
4. Kiểm tra `http://localhost:8080/actuator/health` tối đa 2 phút.
5. Chạy `npm ci` nếu `frontend/node_modules` chưa có, rồi mở Vite tại `http://localhost:5173`.

Mở `http://localhost:5173` trong trình duyệt. Vite proxy các request `/api` và `/actuator` tới backend cục bộ. Dùng `localhost` để khớp CORS mặc định.

**Dừng dịch vụ:** `Ctrl+C` dừng Vite ở terminal này. MySQL và backend vẫn chạy trong Docker. Dừng chúng bằng lệnh tại thư mục gốc:

```bash
docker compose stop
```

Lệnh `docker compose down` cũng dừng stack nhưng giữ volume MySQL. `docker compose down -v` xóa volume và dữ liệu local; chỉ dùng khi chủ động muốn xóa dữ liệu.

### Khi cổng đang bị chiếm

Chạy công cụ kiểm tra/dọn cổng có hỏi xác nhận:

```bash
scripts/free-dev-ports.sh
```

Công cụ liệt kê listener trên các cổng dev. Với container, nhập `y` để gọi `docker stop`; với process thường, nhập `y` để gửi `SIGTERM`. Nhập `N` hoặc nhấn Enter để giữ process/container. Sau khi cổng trống, chạy lại `scripts/dev-all.sh`.

### Script chạy database và backend

Nếu chỉ cần MySQL và backend trong Docker, không cần Vite:

```bash
scripts/dev.sh
```

Script kiểm tra `.env`, chạy Compose và in trạng thái dịch vụ cùng URL health. Frontend có thể được chạy riêng bằng `cd frontend && npm ci && npm run dev`.

## Chạy từng thành phần

### MySQL và backend bằng Compose

```bash
cp .env.example .env
# Sửa mật khẩu local trong .env
docker compose up --build -d
docker compose ps
curl -fsS http://localhost:8080/actuator/health
```

Compose công bố MySQL ở `127.0.0.1:3306` và backend ở `127.0.0.1:8080` theo mặc định. Có thể xem log backend bằng `docker compose logs -f backend`.

### Backend từ máy host hoặc IDE

Khởi động MySQL trước (`docker compose up -d db`), sau đó cấu hình biến `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` trong môi trường Maven/IDE. `.env` được Docker Compose đọc tự động, Maven/IDE không tự nạp file này.

```bash
cd backend
./mvnw -B -ntp verify
./mvnw spring-boot:run
```

Trên Windows dùng `mvnw.cmd`. Nếu chạy JAR thay cho IDE:

```bash
./mvnw -B -ntp package
java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

### Tạo ADMIN đầu tiên

Với database mới, chạy profile bootstrap trong terminal tương tác sau khi đã cấu hình `DB_*` và build JAR:

```bash
cd backend
java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap
```

Nhập thông tin theo lời nhắc trong console. Không có username hoặc mật khẩu mặc định. Bootstrap bỏ qua tạo mới nếu database đã có ADMIN. Không gửi credential qua chat, issue hoặc log.

### Frontend riêng

```bash
cd frontend
npm ci
npm run dev
```

Mở `http://localhost:5173`. Có thể dùng Demo mode để xem dữ liệu mẫu hoặc API mode khi backend đã sẵn sàng. API mode cần session đăng nhập hợp lệ và CSRF cho các request ghi.

## Kiểm thử

Backend unit/API và đóng gói:

```bash
cd backend
./mvnw -B -ntp verify
```

Backend integration cần MySQL 8.4 dành riêng cho test và các biến `DB_*`:

```bash
cd backend
./mvnw -B -ntp -Pintegration verify
```

Frontend:

```bash
cd frontend
npm test
npm run build
```

Integration tests có thể ghi fixture và áp migration. Chỉ trỏ chúng tới database cô lập dành cho kiểm thử, không dùng database chung hoặc dữ liệu cư dân thật. Ghi lệnh, kết quả và giới hạn kiểm thử vào [docs/VALIDATION.md](docs/VALIDATION.md).

## Cấu trúc repo

```text
backend/                 Spring Boot API, migration, unit/API/integration tests
frontend/                React app, API clients, role-based screens, UI tests
docs/                    quyết định, yêu cầu, kiến trúc, kế hoạch, validation
_agents_workspace/       contract, interview và evidence theo task
scripts/                 dev-all.sh, dev.sh, free-dev-ports.sh
compose.yaml             MySQL và backend local
.env.example             mẫu cấu hình Compose local
```

Backend chia package theo tính năng, với luồng `api → application → persistence`. Không trả JPA entity trực tiếp từ API và không gọi repository từ controller. Chi tiết kiến trúc nằm ở [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Tài liệu và quy ước đóng góp

- [AGENTS.md](AGENTS.md): quy ước code, bảo mật, test và bàn giao.
- [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md): phạm vi yêu cầu.
- [docs/DECISIONS.md](docs/DECISIONS.md): hợp đồng và quyết định đã duyệt.
- [docs/sprint-1/PLAN.md](docs/sprint-1/PLAN.md) và [docs/sprint-1/TASKS.md](docs/sprint-1/TASKS.md): kế hoạch và tiến độ.
- [docs/VALIDATION.md](docs/VALIDATION.md): bằng chứng test và giới hạn.
- [frontend/README.md](frontend/README.md): phát triển UI.
- [tests/README.md](tests/README.md): quy trình ca kiểm thử.

Làm việc trên feature branch và mở PR vào `main`. Migration đã chia sẻ không được sửa; thêm schema bằng `V<n>__mo_ta.sql`. Không tắt CSRF hoặc chỉ dựa vào ẩn nút để bảo vệ quyền. Không commit `.env`, credential, PII hoặc dữ liệu thật.
