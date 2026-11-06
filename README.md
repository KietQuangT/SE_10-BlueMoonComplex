# SE_10 — BlueMoon Complex

BlueMoon Complex là hệ thống quản lý chung cư được phát triển trong khuôn khổ đồ án SE_10. Phiên bản v1 tập trung vào quản lý tài khoản, hộ gia đình, nhân khẩu, thu phí và báo cáo phục vụ Ban quản trị, kế toán và cư dân.

Dự án tổ chức theo mô hình monorepo, gồm backend Spring Boot, frontend React và cơ sở dữ liệu MySQL.

> **Trạng thái cập nhật: 07/11/2026.** Nội dung dưới đây phản ánh tiến độ được ghi nhận trên nhánh `feat/s04-account-disable`. Một số chức năng vẫn đang phát triển; giao diện đã có không đồng nghĩa với API tương ứng đã hoàn thành.

## 1. Công nghệ sử dụng

| Thành phần | Công nghệ |
| --- | --- |
| Backend | Java 21, Spring Boot 4.0.9, Maven |
| Frontend | React 19, TypeScript, Vite |
| Cơ sở dữ liệu | MySQL 8.4 |
| Migration | Flyway |
| Xác thực | Spring Security, session lưu trong MySQL |
| Môi trường phát triển | Docker Compose |
| Kiểm thử | Maven Test, integration test, frontend test |

## 2. Chức năng và tiến độ

### Đã triển khai

- **Nền tảng:** cấu hình backend, kết nối MySQL, migration, health check, audit log và CI.
- **Đăng nhập và phân quyền (S01):** đăng nhập, đăng xuất, kiểm tra phiên, phân quyền theo vai trò và cơ chế khóa tài khoản khi đăng nhập sai nhiều lần.
- **Cấp tài khoản (S02):** tạo tài khoản Ban quản trị đầu tiên bằng lệnh bootstrap; ADMIN cấp tài khoản mới và gửi mật khẩu tạm qua email mô phỏng.
- **Quản lý tài khoản (S04):** vô hiệu hóa, kích hoạt lại, mở khóa tài khoản và hủy phiên đăng nhập. Trạng thái, vai trò và hộ liên kết được kiểm tra lại từ cơ sở dữ liệu ở mỗi request.
- **Hộ và nhân khẩu (S05):** tạo, cập nhật, tra cứu hộ; thêm, sửa, tra cứu nhân khẩu; ghi nhận chuyển đi và lịch sử biến động. Cư dân có thể xem danh sách thành viên trong hộ của mình.
- **Frontend:** đã xây dựng giao diện cho 24 màn hình v1, hỗ trợ giao diện sáng/tối. Các luồng S01, S02, S04 và S05 đã kết nối API; các phần còn lại sử dụng dữ liệu mẫu.

### Đang phát triển hoặc chưa có

- Đổi mật khẩu và hoàn tất luồng đổi mật khẩu lần đầu (S03).
- API liệt kê tài khoản; giao diện danh sách tài khoản hiện chưa lấy dữ liệu đầy đủ từ backend.
- Khai báo cư trú, phê duyệt, tạm vắng và quay lại cư trú (S06).
- Đơn giá, kỳ thu, thu tiền, báo cáo, sao lưu và các công việc kiểm thử/bàn giao còn lại (S07–S15).

**Lưu ý:** Tài khoản được ADMIN cấp hiện có cờ `must_change_password = true`. Tài khoản có thể đăng nhập nhưng phần lớn API sẽ trả `403 PASSWORD_CHANGE_REQUIRED` cho đến khi S03 được triển khai. Tài khoản ADMIN được tạo bằng bootstrap không gặp giới hạn này.

Tiến độ và bằng chứng hoàn thành từng task được quản lý tại [`docs/sprint-1/TASKS.md`](docs/sprint-1/TASKS.md). S04 đã được kiểm thử trên nhánh phát triển nhưng, tại thời điểm cập nhật README, chưa mở PR và còn chờ review.

## 3. Cấu trúc thư mục

```text
SE_10-BlueMoonComplex/
├── backend/                 # Spring Boot, Maven Wrapper, Dockerfile
│   ├── src/main/java/      # Mã nguồn ứng dụng
│   ├── src/main/resources/ # Cấu hình, Flyway migrations
│   └── src/test/           # Unit, MVC và integration tests
├── frontend/                # React, TypeScript, Vite
├── docs/                    # Yêu cầu, kiến trúc, quyết định, kế hoạch, kiểm chứng
├── scripts/                 # Script hỗ trợ phát triển và kiểm tra
├── compose.yaml             # MySQL và backend cho môi trường local
├── .env.example             # Mẫu biến môi trường
└── AGENTS.md                # Quy ước dành cho coding agents
```

Backend chia package theo nghiệp vụ:

- `identity`: xác thực, session, vai trò và quản lý tài khoản.
- `residency`: hộ gia đình, nhân khẩu và biến động cư trú.
- `systeminfo`: thông tin hệ thống công khai.
- `common`: xử lý lỗi, audit, request ID và thành phần dùng chung.
- `config`: cấu hình bảo mật, thời gian và ứng dụng.

Các module sử dụng cấu trúc `api` → `application` → `persistence`; không truy cập trực tiếp repository của module khác. Xem chi tiết trong [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## 4. Cài đặt và chạy dự án

### 4.1. Yêu cầu môi trường

- Git.
- Docker Desktop hỗ trợ Docker Compose v2.
- JDK **21** (backend yêu cầu đúng phiên bản major 21).
- Node.js **22+** và npm.
- `curl` để kiểm tra API (không bắt buộc nếu dùng công cụ HTTP khác).

Khuyến nghị khi lập trình: **MySQL chạy trong Docker, backend chạy từ JAR hoặc IDE, frontend chạy bằng Vite.** Cách này thuận tiện cho debug và không phải build lại container sau mỗi lần sửa backend.

### 4.2. Lấy mã nguồn và cấu hình

```bash
git clone https://github.com/KietQuangT/SE_10-BlueMoonComplex.git
cd SE_10-BlueMoonComplex

# Chuyển sang nhánh có các thay đổi S04 khi cần kiểm tra chức năng mới
git switch feat/s04-account-disable

cp .env.example .env
```

Mở `.env` và thiết lập `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` cùng các giá trị cần thiết cho môi trường local. Không commit `.env` hoặc chia sẻ mật khẩu trong issue, chat và log.

### 4.3. Khởi động MySQL

Tại thư mục gốc dự án:

```bash
docker compose up -d db
docker compose ps
```

Đợi container `db` chuyển sang trạng thái `healthy` trước khi chạy backend.

### 4.4. Build backend

```bash
cd backend
./mvnw -B -ntp verify
```

Lệnh này chạy test và đóng gói JAR, không yêu cầu kết nối MySQL. Trên Windows, dùng `mvnw.cmd` thay cho `./mvnw`.

### 4.5. Kết nối backend với MySQL

Trên macOS/Linux, tại thư mục `backend/`:

```bash
set -a
. ../.env
set +a

export DB_HOST=127.0.0.1
export DB_PORT=${DB_PORT:-3306}
export DB_NAME="$MYSQL_DATABASE"
export DB_USER="$MYSQL_USER"
export DB_PASSWORD="$MYSQL_PASSWORD"
```

Với Windows/PowerShell, đặt các biến `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` tương ứng trong terminal chạy backend. File `.env` chỉ được Docker Compose đọc tự động; Maven hoặc IDE không tự nạp các biến này.

### 4.6. Tạo tài khoản ADMIN đầu tiên

Trên cơ sở dữ liệu mới, chạy lệnh sau **một lần** trong terminal tương tác:

```bash
java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap
```

Nhập tên đăng nhập, họ tên, email và mật khẩu theo hướng dẫn. Hệ thống **không có tài khoản hoặc mật khẩu mặc định**. Nếu đã tồn tại ADMIN, lệnh sẽ bỏ qua việc tạo mới.

Hãy lưu thông tin đăng nhập của bạn: phiên bản hiện tại chưa có chức năng đặt lại mật khẩu qua giao diện.

### 4.7. Chạy backend

```bash
java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

Có thể chạy từ IDE hoặc qua `./mvnw spring-boot:run` với cùng các biến `DB_*`. Flyway tự áp dụng migration khi ứng dụng khởi động.

Kiểm tra ở terminal khác:

```bash
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/api/v1/auth/csrf
```

Cả hai endpoint cần trả về HTTP `200`.

### 4.8. Chạy frontend

Mở terminal mới tại thư mục gốc:

```bash
cd frontend
npm install
npm run dev
```

Truy cập **http://localhost:5173** và đăng nhập bằng tài khoản ADMIN vừa tạo. Để kiểm tra backend, sử dụng chế độ **Backend thật** tại màn đăng nhập. Nếu chỉ muốn xem giao diện mà chưa chạy backend, có thể chọn chế độ Demo trong môi trường phát triển.

Frontend proxy các request `/api` tới `http://localhost:8080`. Nên mở bằng `localhost`, không thay bằng `127.0.0.1`, để tránh lỗi cấu hình CORS mặc định.

### 4.9. Chạy backend bằng Docker (tùy chọn)

Nếu không cần debug backend bằng IDE, tại thư mục gốc có thể chạy:

```bash
docker compose up --build -d
docker compose logs -f backend
```

Cách này chạy MySQL và backend bằng Docker. Không chạy thêm backend JAR/IDE cùng lúc vì cùng sử dụng cổng `8080`. Việc bootstrap ADMIN trong container chưa được xác minh đầy đủ; với DB mới nên thực hiện bước 4.6 từ máy host trước.

## 5. Tài khoản, phân quyền và API

### 5.1. Vai trò

| Vai trò | Quyền chính hiện có |
| --- | --- |
| `ADMIN` | Quản lý tài khoản, hộ gia đình và nhân khẩu |
| `ACCOUNTANT` | Đăng nhập; các chức năng phí đang phát triển |
| `RESIDENT` | Đăng nhập và xem nhân khẩu của hộ được gắn với tài khoản |

Hệ thống áp dụng nguyên tắc **mặc định từ chối truy cập**: API chưa khai báo quyền sẽ không tự động được mở. Quyền truy cập hộ được kiểm tra ở backend, không chỉ ở giao diện.

### 5.2. Các nhóm API đã có

Tiền tố chung: `/api/v1`.

| Nhóm | Endpoint chính | Mô tả |
| --- | --- | --- |
| Xác thực | `GET /auth/csrf`, `POST /auth/login`, `POST /auth/logout`, `GET /auth/me` | Lấy CSRF token, đăng nhập, đăng xuất, xem phiên |
| Cấp tài khoản | `POST /accounts` | ADMIN tạo account và email mô phỏng |
| Quản lý tài khoản | `POST /accounts/{id}/disable`, `/enable`, `/unlock` | Vô hiệu, kích hoạt lại, mở khóa |
| Hộ | `POST /households`, `GET /households`, `GET /households/{id}`, `PATCH /households/{id}` | Tạo, xem, cập nhật hộ |
| Nhân khẩu | `POST /households/{id}/residents`, `GET /households/{id}/residents`, `GET /residents/{id}`, `PATCH /residents/{id}` | Tạo, xem, cập nhật nhân khẩu |
| Biến động | `POST /residents/{id}/move-out`, `GET /households/{id}/residency-changes` | Chuyển đi và xem lịch sử |
| Cư dân | `GET /households/me/residents` | Xem thành viên của hộ mình |

Các request thay đổi dữ liệu cần CSRF token trong header `X-CSRF-TOKEN`. Session lưu trong MySQL, cookie `SESSION` sử dụng `HttpOnly` và `SameSite=Lax`; thời hạn phiên là 30 phút.

Luồng xác thực thông thường:

1. Gọi `GET /auth/csrf` để nhận token và cookie.
2. Gọi `POST /auth/login` kèm token, cookie và thông tin đăng nhập.
3. Lấy lại CSRF token sau khi đăng nhập vì session/token có thể thay đổi.
4. Gửi cookie phiên cho các API cần xác thực; bổ sung CSRF token khi ghi dữ liệu.

### 5.3. Một số quy tắc nghiệp vụ quan trọng

- **Cấp account:** role hợp lệ là `ADMIN`, `ACCOUNTANT`, `RESIDENT`. Cư dân phải được gắn với một hộ `ACTIVE`; mật khẩu tạm chỉ lưu trong email mô phỏng (`mock_emails`), không trả trong response.
- **Đăng nhập thất bại:** hệ thống sử dụng cùng thông báo `401` cho tài khoản không tồn tại, sai mật khẩu, bị khóa hoặc vô hiệu hóa để hạn chế lộ thông tin.
- **Khóa tài khoản:** lần đầu khóa sau 5 lần nhập sai liên tiếp; các lần khóa sau tăng theo bậc. ADMIN hiện không bị khóa tự động. Đây là giới hạn bảo mật cần lưu ý.
- **Vô hiệu tài khoản:** hủy các session đang tồn tại; request kế tiếp của tài khoản đó bị từ chối. Không thể tự vô hiệu chính mình hoặc vô hiệu ADMIN đang hoạt động cuối cùng.
- **Nhân khẩu:** trạng thái cư trú gồm `RESIDING`, `TEMPORARILY_ABSENT`, `MOVED_OUT`. Chuyển đi chỉ cập nhật trạng thái và ghi lịch sử, không xóa dữ liệu.
- **Quyền riêng tư:** cư dân không được xem số giấy tờ và số điện thoại của các thành viên; audit log không ghi mật khẩu, số giấy tờ hoặc thông tin cá nhân nhạy cảm.

## 6. Cơ sở dữ liệu

Flyway quản lý cấu trúc cơ sở dữ liệu. Hibernate chỉ kiểm tra schema bằng chế độ `validate`, không tự tạo hoặc sửa bảng.

| Migration | Nội dung |
| --- | --- |
| V1 | Bảng session JDBC |
| V2 | Vai trò, tài khoản và audit log |
| V3 | Hộ, nhân khẩu và biến động cư trú |
| V4 | Email tài khoản và liên kết hộ |
| V5 | Các trường phục vụ cơ chế khóa tài khoản |
| V6 | Cờ đổi mật khẩu lần đầu và email mô phỏng |

Không sửa migration đã merge hoặc đã áp dụng trên DB của người khác. Khi cần thay đổi schema, tạo một migration mới.

Để mở MySQL trên môi trường local:

```bash
docker compose exec db sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE"'
```

Chỉ sử dụng dữ liệu giả để phát triển và kiểm thử. Không thao tác trực tiếp trên DB có dữ liệu thật.

## 7. Kiểm thử

### Backend

```bash
cd backend

# Unit, MVC, security tests và build JAR (không cần DB)
./mvnw -B -ntp verify

# Chạy thêm integration tests (cần MySQL và các biến DB_*)
./mvnw -B -ntp -Pintegration verify
```

Theo kết quả ghi trong README trước đó, lần kiểm thử nhánh S04 ngày 06/11/2026 có **86 test ở bộ kiểm tra thông thường và 75 integration test**, chạy thành công trên JDK 21.0.9 và MySQL 8.4. Đây là kết quả tại thời điểm ghi nhận, không phải cam kết cho mọi commit sau đó.

**Cảnh báo:** integration test ghi và xóa dữ liệu fixture, thực hiện migration khi cần và có thể thay đổi dữ liệu tạm thời trong database. Chỉ chạy trên DB local dành riêng cho kiểm thử, không chạy trên DB dùng chung hoặc có dữ liệu thật.

### Frontend

```bash
cd frontend
npm test
npm run build
```

Kết quả kiểm thử và lệnh xác minh cần được cập nhật tại [`docs/VALIDATION.md`](docs/VALIDATION.md). CI backend được cấu hình trong `.github/workflows/backend.yml`.

## 8. Quy ước phát triển

- Tạo nhánh theo mẫu `feat/<ma-task>-<mo-ta>`, mở pull request vào `main` và chờ review/CI.
- Mỗi thay đổi nghiệp vụ cần có test tương ứng, đặc biệt với xác thực, phân quyền và thao tác dữ liệu.
- Không tắt CSRF hoặc bỏ qua kiểm tra bảo mật để làm test thành công.
- Không commit `.env`, credential, dữ liệu cư dân thật hoặc log chứa thông tin nhạy cảm.
- Không `push --force` lên `main`.
- Cập nhật tài liệu và bằng chứng kiểm thử khi hoàn thành task.

Phân công hiện tại: Kiệt (repository, frontend, API), Sơn (nền tảng, tài khoản, cư trú), Quân (phí, kỳ thu, giao dịch), Tuấn (cơ sở dữ liệu, báo cáo, QA). Xem thêm [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md) và [`AGENTS.md`](AGENTS.md).

## 9. Xử lý sự cố thường gặp

| Vấn đề | Hướng xử lý |
| --- | --- |
| Maven báo sai phiên bản JDK | Kiểm tra `java -version` và cấu hình `JAVA_HOME` về JDK 21. |
| Cổng `8080` đã được sử dụng | Chỉ chạy một backend; dừng container hoặc tiến trình JAR/IDE đang chiếm cổng. |
| `GET /api/v1/auth/csrf` trả `401` | Kiểm tra có backend cũ đang chạy trên cổng `8080` hay không. |
| MySQL báo `Access denied` | Đối chiếu `DB_*` và giá trị trong `.env`; mật khẩu trong volume MySQL cũ không tự đổi khi sửa `.env`. |
| Đăng nhập `401` trên DB mới | Kiểm tra đã bootstrap ADMIN hay chưa. |
| Tài khoản mới bị `403 PASSWORD_CHANGE_REQUIRED` | Đây là giới hạn hiện tại do S03 chưa hoàn thành. |
| Frontend báo lỗi CORS | Truy cập bằng `http://localhost:5173` và kiểm tra `CORS_ALLOWED_ORIGIN`. |
| Danh sách tài khoản trống sau khi tải lại trang | API liệt kê account chưa triển khai; giao diện chỉ giữ các tài khoản vừa tạo trong phiên hiện tại. |
| Flyway báo sai checksum | Không sửa bảng lịch sử migration thủ công; kiểm tra migration bị thay đổi và xử lý trên DB local riêng. |

Khi cần dừng môi trường Docker:

```bash
docker compose down
```

Lệnh trên giữ lại volume và dữ liệu MySQL. **Không sử dụng `docker compose down -v` nếu chưa chủ động chấp nhận mất dữ liệu trong volume.** Docker volume không thay thế cho một bản sao lưu.

## 10. Tài liệu tham khảo trong dự án

| Tài liệu | Nội dung |
| --- | --- |
| [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) | Yêu cầu chức năng và phi chức năng |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Kiến trúc và quy ước kỹ thuật |
| [`docs/DECISIONS.md`](docs/DECISIONS.md) | Các quyết định thiết kế và điểm cần xác nhận |
| [`docs/STRUCTURE.md`](docs/STRUCTURE.md) | Cấu trúc repository chi tiết |
| [`docs/sprint-1/PLAN.md`](docs/sprint-1/PLAN.md) | Kế hoạch Sprint 1 |
| [`docs/sprint-1/TASKS.md`](docs/sprint-1/TASKS.md) | Checklist task và bằng chứng hoàn thành |
| [`docs/VALIDATION.md`](docs/VALIDATION.md) | Lệnh kiểm thử và kết quả xác minh |
| [`docs/SOURCE_REVIEW.md`](docs/SOURCE_REVIEW.md) | Đối chiếu nguồn thông tin và các mâu thuẫn |
| [`frontend/README.md`](frontend/README.md) | Hướng dẫn frontend và trạng thái tích hợp API |

---

README này mô tả các chức năng đã có và cách chạy dự án tại thời điểm cập nhật. Trạng thái chính thức của từng task, kết quả kiểm thử và các quyết định còn mở được theo dõi trong thư mục `docs/`.
