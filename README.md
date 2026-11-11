# BlueMoon Complex — SE_10

BlueMoon Complex là ứng dụng web quản lý vận hành chung cư trong đồ án SE_10. Phiên bản hiện tại tập trung vào tài khoản và phân quyền, hộ gia đình và nhân khẩu, cư trú, đơn giá, hóa đơn, thu tiền, công nợ và gói dữ liệu được Ban quản trị kiểm duyệt. Các vai trò trong hệ thống là Ban quản trị (`ADMIN`), Kế toán (`ACCOUNTANT`) và Cư dân (`RESIDENT`); cơ quan nhận gói không có tài khoản hay quyền truy cập hệ thống.

Repo là monorepo: API Spring Boot, giao diện React, MySQL và tài liệu nghiệp vụ nằm trong cùng một dự án. Trạng thái chính thức từng hạng mục nằm ở [docs/sprint-1/TASKS.md](docs/sprint-1/TASKS.md); README mô tả phạm vi hiện có trong mã nguồn, không thay thế checklists.

S01–S12 backend đã có triển khai. S08 (kỳ thu/tự nguyện), S09 (thu tiền/gạch nợ), S10 (API + UI hóa đơn cư dân) và S11 backend (xuất báo cáo XLSX thu phí/cư trú) đã được đánh dấu hoàn thành trong `TASKS.md`; giới hạn kiểm chứng được ghi trong `VALIDATION.md`. S12 backend hiện đã qua `verify` và MySQL integration; UI S12 đang được triển khai. S09 đã được merge qua PR [#28](https://github.com/KietQuangT/SE_10-BlueMoonComplex/pull/28) và backend S10 qua PR [#32](https://github.com/KietQuangT/SE_10-BlueMoonComplex/pull/32). Giao diện export S11 đã được triển khai trên nhánh `hhoangsonnw/feat-s11-ui`; task chờ review PR trước khi nghiệm thu.

S10 bổ sung API tra cứu và chi tiết hóa đơn/lịch sử gạch nợ cho cư dân (`/api/v1/resident/invoices`), kiểm tra quyền theo hộ ở server (chặn IDOR/truy cập chéo hộ), snapshot hóa đơn và ledger với cô lập giao dịch `REPEATABLE_READ`, phân trang và lọc theo trạng thái. UI cư dân dùng API thật để xem, lọc, phân trang và mở chi tiết/lịch sử; giao diện chỉ đọc và Demo mode dùng dữ liệu riêng. Backend S10 được duyệt G2 ngày 09/11/2026; walkthrough UI hoàn tất ngày 10/11/2026.

Các giới hạn còn lại: chưa xác nhận viewport đúng 360px, zoom 200%, theme tối/reduced motion và trạng thái ledger hiếm (credit, receipt bị hủy, allocation bị đảo); chưa đo 10.000 bản ghi/EXPLAIN (chuyển sang S14); chưa chạy test snapshot khi bỏ `REPEATABLE_READ`; test `MOVED_OUT` chưa chứng minh account thuộc nhân khẩu do schema chưa nối account–nhân khẩu. Các giới hạn này không chặn S10 UI theo quyết định của Sơn. Xem [docs/VALIDATION.md](docs/VALIDATION.md).

S11 backend cung cấp ba endpoint tải XLSX cho Kế toán và Ban quản trị: công nợ theo kỳ, tiền mặt thu theo khoảng ngày và danh sách cư trú hiện tại. Các file được dựng theo mẫu đã duyệt, giới hạn 50.000 dòng và chỉ chứa các trường cần thiết; quyền cư trú bị từ chối với vai trò `ACCOUNTANT` và `RESIDENT`. UI `/reports` cho ADMIN/ACCOUNTANT kết nối cả ba endpoint, kiểm tra bộ lọc và xử lý trạng thái tải/lỗi.

Danh sách tài khoản ADMIN dùng `GET /api/v1/accounts?page=0&size=100`, chỉ ADMIN được truy cập. API phân trang, không trả dữ liệu mật khẩu; M-10 tải dữ liệu thật và giữ tìm kiếm/lọc/phân trang hiện có. Bổ sung này đang chờ review cùng nhánh S11-UI.

S12 backend cho ADMIN tạo gói dữ liệu theo phạm vi hộ, xem trước, duyệt bởi ADMIN khác người tạo, từ chối nháp, thu hồi, tải XLSX và ghi nhận bàn giao. BQT tự tiếp nhận yêu cầu và kiểm tra giấy phép bên ngoài hệ thống; chỉ mã tham chiếu tối thiểu được lưu. Metadata/audit được giữ 5 năm theo mục tiêu NFR-010, không phải khẳng định về thời hạn pháp luật. Tệp có thể tải tối đa 7 ngày từ lúc duyệt và bị xóa tối đa 30 ngày từ lúc tạo; tải bị chặn tại hạn đến trước. Xem [S12 trong VALIDATION.md](docs/VALIDATION.md) và [quyết định D13](docs/DECISIONS.md).

Các task tiếp theo (hoàn tất UI/nghiệm thu S12, S13 backup/restore, S14 hồi quy/hiệu năng, S15 đóng gói/bàn giao) và trạng thái review S11-UI tiếp tục được theo dõi trong [docs/sprint-1/TASKS.md](docs/sprint-1/TASKS.md). Quy tắc đã duyệt và điểm nghiệp vụ còn mở nằm trong [docs/DECISIONS.md](docs/DECISIONS.md).

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
- **Thu bắt buộc S09:** nhiều receipt cho một hóa đơn; thu thừa trở thành credit hộ; phân bổ credit FIFO; hủy receipt và đảo phân bổ lưu lịch sử; idempotency bằng UUID và audit nghiệp vụ trong transaction; Kế toán thao tác, ADMIN chỉ đọc.
- **Hóa đơn cư dân S10:** API và UI cho danh sách/chi tiết hóa đơn theo hộ đăng nhập, dòng phí snapshot, đã thu/còn nợ và lịch sử; lọc tháng/trạng thái, phân trang; server chặn truy cập chéo hộ. UI chỉ đọc, Demo mode độc lập.
- **Báo cáo S11:** API tải XLSX công nợ theo kỳ (`/api/v1/reports/fee-collection.xlsx?period=YYYY-MM`), tiền mặt thu theo ngày (`/api/v1/reports/cash-receipts.xlsx?from=YYYY-MM-DD&to=YYYY-MM-DD`) và cư trú hiện tại (`/api/v1/reports/residency.xlsx`). Tối đa 50.000 dòng mỗi file; chỉ `ADMIN` được tải báo cáo cư trú, `ACCOUNTANT` chỉ được tải hai báo cáo tài chính.
- **Gói dữ liệu S12:** API `/api/v1/agency-data-packages` dành cho ADMIN; kiểm tra yêu cầu/giấy phép và chuyển gói cho cơ quan do BQT xử lý ngoài hệ thống. Cơ quan không có role/account, không đăng nhập và không truy vấn dữ liệu thô.
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

Khi cơ sở dữ liệu mới khởi tạo, hệ thống hoàn toàn không có tài khoản quản trị viên mặc định (`noUsersYet`). Để tạo tài khoản `ADMIN` đầu tiên, ứng dụng cung cấp profile `bootstrap` thông qua `BootstrapAdminRunner` và `BootstrapAdminService`.

#### 1. Điều kiện tiên quyết
- Docker Compose MySQL phải đang chạy và ở trạng thái healthy (ví dụ: `docker compose up -d db` hoặc `docker compose up --build -d`). MySQL công bố ra máy host tại `127.0.0.1:${DB_PORT:-3306}`.
- Docker Compose tự động nạp file `.env`, nhưng Maven, Java CLI và IDE trên máy host **không tự nạp `.env`**. Bạn cần cấu hình các biến môi trường kết nối trực tiếp trong terminal trước khi chạy.

#### 2. Cấu hình biến môi trường kết nối DB
Thiết lập các biến môi trường tương ứng với cấu hình trong `.env` (tham khảo `.env.example` và `compose.yaml`):
- `DB_HOST=127.0.0.1`: kết nối tới cổng MySQL được Compose xuất ra máy host.
- `DB_PORT`: mặc định `3306` (hoặc cổng tùy chỉnh nếu sửa biến `DB_PORT` trong `.env`).
- `DB_NAME`: tương ứng với `MYSQL_DATABASE` trong `.env` (mặc định là `bluemoon`).
- `DB_USER`: tương ứng với `MYSQL_USER` trong `.env` (mặc định là `bluemoon`).
- `DB_PASSWORD`: tương ứng với `MYSQL_PASSWORD` trong `.env`. **Để tránh lưu mật khẩu vào lịch sử shell (`~/.zsh_history`, `~/.bash_history`), hãy nhập tương tác không echo thay vì gõ plaintext:**

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=bluemoon
export DB_USER=bluemoon
printf 'Nhập DB_PASSWORD: '
read -rs DB_PASSWORD
printf '\n'
export DB_PASSWORD
```

*(Trên Windows PowerShell, có thể nhập mật khẩu có che bằng `Get-Credential`: `$env:DB_HOST="127.0.0.1"; $env:DB_PORT="3306"; $env:DB_NAME="bluemoon"; $env:DB_USER="bluemoon"; $cred = Get-Credential -UserName $env:DB_USER -Message "Nhập DB_PASSWORD"; $env:DB_PASSWORD = $cred.GetNetworkCredential().Password`)*

#### 3. Đóng gói backend (JDK 21)
Build file JAR bằng Maven Wrapper:

```bash
cd backend
./mvnw -B -ntp package
```
*(Trên Windows dùng `mvnw.cmd`)*

#### 4. Chạy profile bootstrap trong terminal tương tác
Profile `bootstrap` (cấu hình tại `application-bootstrap.yml`) tắt web server (`spring.main.web-application-type: none`) và kích hoạt `BootstrapAdminRunner` chạy dưới dạng CLI tương tác:

```bash
java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap
```

> [!IMPORTANT]
> Lệnh này **bắt buộc** phải chạy trong terminal tương tác (interactive console) vì runner sử dụng `System.console()`. Nếu chạy qua pipe, redirect hoặc môi trường không có console thực tế (`System.console() == null`), tiến trình sẽ dừng ngay với ngoại lệ `IllegalStateException: Cần terminal tương tác.`

#### 5. Nhập thông tin quản trị viên
Console sẽ lần lượt nhắc bạn nhập các trường:
1. `Tên đăng nhập: ` (username)
2. `Họ tên: ` (fullName)
3. `Email: ` (email)
4. `Mật khẩu: ` (password, không hiển thị ký tự ra màn hình)
5. `Nhập lại mật khẩu: ` (confirmation, không hiển thị ký tự ra màn hình)

#### 6. Quy tắc xác thực thực tế
Các giá trị nhập vào được kiểm tra nghiêm ngặt theo quy tắc nghiệp vụ (`BootstrapAdminService` và `EmailNormalizer`):
- **Tên đăng nhập:** độ dài 3 đến 100 ký tự, chỉ gồm chữ cái, chữ số, dấu chấm, gạch dưới, gạch ngang (khớp regex `^[A-Za-z0-9._-]{3,100}$`), và chưa được dùng bởi bất kỳ tài khoản nào trong hệ thống.
- **Họ tên:** 1 đến 150 ký tự, không được để trống hoặc chỉ chứa khoảng trắng, không chứa ký tự điều khiển ISO; khoảng trắng đầu và cuối được cắt tỉa (`trim()`) khi lưu.
- **Email:** chuẩn hóa tự động qua `EmailNormalizer` (chuyển chữ thường, trim); độ dài tối đa 254 ký tự; có đúng 1 ký tự `@`; phần tên miền có dấu chấm hợp lệ (không ở đầu/cuối tên miền); không chứa ký tự điều khiển ISO, khoảng trắng hoặc ký tự non-ASCII; và chưa tồn tại trong hệ thống.
- **Mật khẩu:** tối thiểu 8 ký tự; mật khẩu và xác nhận phải trùng khớp tuyệt đối; độ dài UTF-8 không vượt quá 72 bytes (giới hạn an toàn của thuật toán Bcrypt). Ký tự mật khẩu trong bộ nhớ được ghi đè byte rỗng (`\0`) ngay sau khi xử lý.

#### 7. Xử lý khi đã có ADMIN (kể cả inactive)
- `BootstrapAdminService` thực hiện khóa dòng role ADMIN (`findByCodeForUpdate("ADMIN")`) và kiểm tra `users.existsByRole_Code("ADMIN")`.
- Kiểm tra này tính **tất cả** tài khoản mang vai trò `ADMIN` trong database, **kể cả tài khoản đang bị vô hiệu hóa / status `INACTIVE`**.
- Nếu cơ sở dữ liệu đã có ít nhất một tài khoản `ADMIN`: tiến trình sẽ bỏ qua tạo mới và in ra thông báo:
  ```text
  Đã có ADMIN, bỏ qua.
  ```
- Nếu chưa có `ADMIN` và thông tin hợp lệ: tài khoản được lưu với vai trò `ADMIN`, ghi nhận audit bootstrap trong cùng transaction và in ra:
  ```text
  Đã tạo ADMIN.
  ```

> [!CAUTION]
> Không chạy bootstrap tự động trong script CI/CD nếu không có console tương tác. Tuyệt đối không commit credential, mật khẩu hoặc ghi mật khẩu vào shell history/log.

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

### Bằng chứng kiểm thử S11 backend

- `./mvnw -B -ntp verify`: 353 test pass, 0 failure/error/skip.
- `./mvnw -B -ntp -Pintegration verify`: 353 Surefire + 173 Failsafe = 526 test pass trên MySQL 8.4 test cô lập; container tạm được xóa sau lượt chạy.
- Review rủi ro độc lập: không có lỗi chặn; ba workbook synthetic đã được mở và duyệt trong Excel.
- UI tải báo cáo chưa nằm trong nhánh backend này; sẽ được làm ở task riêng.

### Bằng chứng kiểm thử S09 và S10 gần nhất

- **Backend verify:** 283 test pass (JDK 21).
- **Backend integration (MySQL 8.4 test cô lập):** 158 test pass (bao gồm `ResidentInvoiceSnapshotIT` và `IdentitySessionIT`).
- **Frontend tests:** 33 file, 185 test pass; TypeScript/Vite build pass.
- **S10 UI:** walkthrough API thật bằng Orca Browser xác nhận danh sách, lọc `PARTIALLY_PAID`, chi tiết và receipt; 393×852 CSS px không cuộn ngang.
- **Giới hạn đã ghi nhận:** viewport 360px, zoom 200%, theme tối/reduced motion, ledger hiếm và phép đo 10.000 bản ghi/EXPLAIN (S14) còn hoãn; xem [docs/VALIDATION.md](docs/VALIDATION.md).

## Cấu trúc repo

```text
backend/                 Spring Boot API, migration, unit/API/integration tests
frontend/                React app, API clients, role-based screens, UI tests
docs/                    quyết định, yêu cầu, kiến trúc, kế hoạch, validation
tests/                   bộ ca kiểm thử nghiệp vụ, báo cáo và quy trình test
_agents_workspace/       contract, interview và evidence theo task
scripts/                 dev-all.sh, dev.sh, free-dev-ports.sh, verify.sh
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
