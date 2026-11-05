# SE_10 — BlueMoon Complex

Hệ thống quản lý chung cư BlueMoon (v1.0): tài khoản và phân quyền, hộ/nhân khẩu, kỳ thu và thu phí, báo cáo. Repo này là monorepo; hiện chỉ có **backend** (Java 21, Spring Boot 4.0.9, Maven, MySQL 8.4). Thư mục `frontend/` chưa có mã.

> Nguồn sự thật về tiến độ là [`docs/sprint-1/TASKS.md`](docs/sprint-1/TASKS.md). Một task chỉ được tick khi có bằng chứng (lệnh, kết quả, commit/PR), không tick vì đã viết code.

## Trạng thái

| Task | Nội dung | Trạng thái |
|---|---|---|
| F00 | Repo, wrapper, hướng dẫn, CI | Xong (PR #4) |
| F01 | Chốt hợp đồng, quyền và dữ liệu (D13–D18) | Xong theo xác nhận của Sơn; chưa có văn bản của cả nhóm |
| F02 | Nền tảng backend/DB, audit cùng transaction | Xong (PR #4) |
| **S01** | **Đăng nhập, session, vai trò, audit, chống dò mật khẩu** | **Xong, đang chờ push/PR/review** |
| S02–S15 | Cấp account, đổi mật khẩu, hộ/nhân khẩu, đơn giá, kỳ thu, thu tiền, báo cáo, backup, hồi quy, bàn giao | Chưa bắt đầu |

**Có thể dùng ngay:** health, metadata, session JDBC, audit, đăng nhập/đăng xuất/xem phiên (`/api/v1/auth/*`), khóa tạm thời khi sai mật khẩu nhiều lần.
**Chưa có:** tài khoản mặc định, lệnh tạo BQT đầu tiên và cấp account cư dân (task S02), đổi mật khẩu (S03), vô hiệu account và mở khóa thủ công (S04), mọi API nghiệp vụ hộ/phí. Mọi endpoint không khai báo đều bị chặn mặc định (`denyAll`).

## Cấu trúc repo

```text
backend/            Spring Boot (Maven Wrapper, Dockerfile)
  src/main/java/vn/edu/se10/bluemoon/
    config/         SecurityConfig (session, CSRF, CORS, quyền), ClockConfig
    common/         audit (AuditService), error, web (request ID)
    identity/       api / application / persistence: đăng nhập, session, khóa tài khoản, RoleService mẫu
    systeminfo/     metadata công khai
  src/main/resources/db/migration/   Flyway V1–V5
  src/test/         unit/MVC (verify) và integration (-Pintegration)
docs/               kiến trúc, yêu cầu, quyết định, kế hoạch, kiểm chứng
frontend/           chưa có mã
scripts/            dev.sh (docker compose), verify.sh
compose.yaml        MySQL 8.4 + backend cho môi trường local
```

Package theo tính năng: `api` → `application` → `persistence`. Chi tiết và quy ước: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Khởi động nhanh bằng Docker

Yêu cầu Git và Docker Engine/Desktop có Compose v2.
```bash
cp .env.example .env
# Mở .env, thay hai mật khẩu local (MYSQL_PASSWORD, MYSQL_ROOT_PASSWORD).
docker compose up --build -d
docker compose logs -f backend
```
Kiểm tra ở terminal khác:
```bash
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8080/api/v1/system/info
```
Health chỉ báo trạng thái ứng dụng và DB, không có chi tiết. `/actuator/health/liveness` chỉ kiểm tra tiến trình sống, không dùng riêng nó để kết luận DB sẵn sàng.

## Phát triển bằng IDE

Cài JDK 21. Maven Wrapper tự tải Maven 3.9.11 lần đầu (cần Internet). Build chỉ chấp nhận đúng JDK 21: nếu `java` mặc định là bản khác, enforcer báo "Dự án dùng JDK 21"; đặt `JAVA_HOME` trỏ tới JDK 21 trong shell chạy `./mvnw`.
```bash
docker compose up -d db
export DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=bluemoon DB_USER=bluemoon
export DB_PASSWORD='mat_khau_ban_da_dat_trong_env'
cd backend
./mvnw spring-boot:run
```
- Không chạy backend trong Docker và trong IDE cùng lúc (cùng cổng 8080). Nếu `.env` đổi `DB_PORT`, đổi tương ứng trong IDE.
- `.env` chỉ dùng cho Docker Compose và dùng tên `MYSQL_*`; Maven/IDE không tự đọc nó và cần biến `DB_*` như trên.
- PowerShell: `$env:DB_PASSWORD="..."` và `./mvnw.cmd`. Không dán mật khẩu vào chat, issue hay commit.
- Profile mặc định là `local`: cookie session không có cờ `Secure` vì chưa có TLS. Không dùng cấu hình này để triển khai ra Internet.

## Xác thực và phân quyền (S01)

Session lưu trong MySQL (bảng `SPRING_SESSION`), hết hạn sau 30 phút; cookie `SESSION` là HttpOnly, SameSite=Lax. CSRF luôn bật: mọi `POST` cần header `X-CSRF-TOKEN`. CORS chỉ nhận origin cấu hình ở `CORS_ALLOWED_ORIGIN` (mặc định `http://localhost:5173`).

Ba vai trò (D13): `ADMIN` (BQT), `ACCOUNTANT` (kế toán), `RESIDENT` (cư dân, gắn đúng một hộ). Vai trò thứ tư dành cho cơ quan chức năng sẽ thêm bằng migration riêng ở task S12.

| Endpoint | Cần đăng nhập | Kết quả |
|---|---|---|
| `GET /api/v1/auth/csrf` | không | 200 `{headerName, token}` |
| `POST /api/v1/auth/login` | không; cần CSRF | 200 `{username, fullName, role, householdId}`; 400 body sai; 401 mọi lỗi đăng nhập; 403 thiếu/sai CSRF |
| `POST /api/v1/auth/logout` | có; cần CSRF | 204; 401 nếu chưa đăng nhập |
| `GET /api/v1/auth/me` | có | 200 như login; 401 nếu chưa đăng nhập |

Luồng cho frontend (token và session ID đổi sau login và logout, nên lấy lại token):
```bash
# 1. Lấy CSRF token (lưu cookie)
curl -c jar.txt -s http://localhost:8080/api/v1/auth/csrf
# 2. Đăng nhập, gửi token ở header
curl -b jar.txt -c jar.txt -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' -H 'X-CSRF-TOKEN: <token>' \
  -d '{"username":"<tên>","password":"<mật khẩu>"}'
# 3. Lấy token mới rồi gọi các API khác với cookie mới
curl -b jar.txt -c jar.txt -s http://localhost:8080/api/v1/auth/csrf
curl -b jar.txt -s http://localhost:8080/api/v1/auth/me
```
Lưu ý: DB mới chưa có tài khoản nào (không có tài khoản mặc định, không có trong migration). Lệnh tạo BQT đầu tiên thuộc task S02; các test tự tạo tài khoản fixture.

**Mọi lỗi đăng nhập trả cùng một body 401** (sai mật khẩu, account không tồn tại, bị khóa, `INACTIVE`, RESIDENT chưa có hộ) để không lộ account tồn tại. Response chỉ có 4 trường; không bao giờ có hash, email, `status` hay cột khóa.

**Chống dò mật khẩu (D17, D18):** khóa theo account, không theo IP.
| Lần khóa | 1 | 2 | 3 | 4 | 5 | 6 | từ 7 trở đi |
|---|---|---|---|---|---|---|---|
| Thời gian khóa | 1 phút | 5 | 10 | 15 | 30 | 60 | cộng đều +60 phút mỗi lần (120, 180, …), không trần |

- Lần khóa đầu cần 5 lần sai liên tiếp; từ lần khóa thứ hai, sai thêm **1 lần** là lên bậc kế.
- Đang khóa: mật khẩu đúng vẫn bị từ chối, không đếm thêm và không gia hạn. Hết khóa thì đăng nhập đúng đặt lại bộ đếm.
- `ADMIN` không bị khóa (không có lớp chặn dò mật khẩu ADMIN ở tầng ứng dụng; xem rủi ro trong `docs/DECISIONS.md`).
- Thời điểm khóa lưu UTC (`DATETIME(6)`); kết quả không phụ thuộc múi giờ của JVM.
- Mở khóa thủ công bởi BQT thuộc task S04, chưa có.

**Audit:** `LOGIN_SUCCESS`, `LOGIN_FAILED`, `ACCOUNT_LOCKED` ghi cùng transaction với thay đổi dữ liệu (rollback thì audit cũng rollback). `AuditService.recordBootstrap` dành riêng cho lệnh tạo BQT đầu tiên (actor NULL, action cố định `BOOTSTRAP_ADMIN`).

## Cơ sở dữ liệu và migration

Flyway tự áp migration khi khởi động; Hibernate chỉ `validate`, không `update/create`.

| Migration | Nội dung |
|---|---|
| V1 | Bảng session JDBC |
| V2 | Vai trò, tài khoản, `audit_log` |
| V3 | Hộ và nhân khẩu |
| V4 | Email tài khoản, `users.household_id` |
| V5 | Cột chống dò mật khẩu: `failed_login_count`, `lock_level`, `locked_until` |

Quy tắc: không sửa migration đã merge hoặc đã chạy trên DB của người khác; thêm `V<n>__mo_ta.sql` mới. Nếu đã sửa migration trên DB cục bộ, Flyway báo sai checksum: tạo lại DB cục bộ, không sửa tay `flyway_schema_history`.

## Test và build

```bash
cd backend
./mvnw -B -ntp verify                    # không cần DB
./mvnw -B -ntp -Pintegration verify      # cần MySQL thật và biến DB_*
```
- Lệnh đầu chạy unit/MVC/security (hiện 45 test), đóng gói JAR, không cần DB.
- Lệnh thứ hai chạy thêm integration (hiện 31 test: `BootstrapIT`, `RoleAuditIT`, `IdentityCookieIT`, `IdentitySessionIT`). Không tự bỏ qua khi thiếu DB. Lần S01 xanh ở cả JVM múi giờ +07 và `TZ=UTC`.
- Integration chạy trên DB `bluemoon` đang cấu hình: tự áp migration chưa có, tự thêm rồi xóa tài khoản fixture, và `RoleAuditIT` đổi tên role `ADMIN` rồi khôi phục. Đừng chạy trên DB có người khác đang dùng.
- Ghi lệnh và kết quả vào `docs/VALIDATION.md`. Không dùng cờ bỏ qua test hay enforcer.
- GitHub CI (`.github/workflows/backend.yml`) dựng MySQL riêng và chạy `./mvnw -B -ntp -Pintegration verify`.

## Bảo mật (tóm tắt, chi tiết trong `AGENTS.md`)

- Deny-by-default; không có endpoint public bằng wildcard. Endpoint mới phải có test 401/403 và truy cập chéo hộ nếu liên quan; quyền theo hộ kiểm tra ở server, không chỉ ẩn menu.
- Không tắt CSRF, không thêm JWT để cho test xanh. Mật khẩu băm bằng `PasswordEncoder`.
- Không log body, cookie, `Authorization`, CCCD, email, số điện thoại hay mật khẩu; dùng request ID để truy vết. Không commit `.env`, credential hay dữ liệu cư dân thật.
- Tiền và diện tích dùng `BigDecimal`. Thay đổi dữ liệu và audit trong cùng transaction.

## Cộng tác

- Đọc [`AGENTS.md`](AGENTS.md) trước khi yêu cầu agent sửa code; mỗi lượt chỉ giao một task.
- Phân công: Kiệt (repo, frontend, API), Sơn (nền tảng, tài khoản, cư trú), Quân (phí, kỳ thu, giao dịch), Tuấn (DB, báo cáo, QA). Người viết nghiệp vụ cũng viết test.
- Mỗi người làm nhánh `feat/<ma-task>-<mo-ta>`, mở PR vào `main`, chờ review và CI. Không `push --force` vào `main`. Xem [`docs/CONTRIBUTING.md`](docs/CONTRIBUTING.md).
- Phạm vi v1: PBI-001–004, 006–008, 010–013, 015, 021–023. Không đưa QR/VNeID, ticket, khách thăm, mượn đồ, Redis, Kafka hay microservices vào v1.

## Tài liệu

| File | Nội dung |
|---|---|
| [`docs/REQUIREMENTS.md`](docs/REQUIREMENTS.md) | Yêu cầu |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Kiến trúc, quy ước, auth, audit, lỗi |
| [`docs/DECISIONS.md`](docs/DECISIONS.md) | Quyết định D01–D18 và các điểm chưa xác nhận |
| [`docs/sprint-1/PLAN.md`](docs/sprint-1/PLAN.md) | Kế hoạch Sprint 1 |
| [`docs/sprint-1/TASKS.md`](docs/sprint-1/TASKS.md) | Checklist và khối bằng chứng từng task |
| [`docs/VALIDATION.md`](docs/VALIDATION.md) | Lệnh đã chạy và kết quả |
| [`docs/SOURCE_REVIEW.md`](docs/SOURCE_REVIEW.md) | Đối chiếu nguồn và mâu thuẫn |
| [`docs/STRUCTURE.md`](docs/STRUCTURE.md) | Cấu trúc dự án đầy đủ |

## Xử lý sự cố

| Triệu chứng | Cách xử lý |
|---|---|
| `Dự án dùng JDK 21` | Đặt `JAVA_HOME` trỏ tới JDK 21 rồi chạy lại |
| `Unresolved compilation problems` | IDE ghi đè `backend/target/`; chạy `./mvnw clean verify` |
| Cổng 8080 bận | Dừng backend Docker hoặc IDE, chỉ chạy một |
| Flyway báo sai checksum | Migration đã bị sửa; tạo lại DB cục bộ (không sửa `flyway_schema_history`) |
| Đăng nhập luôn 401 trên DB mới | Chưa có tài khoản; chờ lệnh tạo BQT (S02) hoặc dùng fixture trong test |
| Trình duyệt không gửi cookie session | Cookie `Secure` chỉ tắt ở profile `local`; kiểm tra profile đang chạy |

## Dừng và dữ liệu

```bash
docker compose down
```
Volume vẫn giữ dữ liệu. Không chạy `down -v` khi chưa xác nhận chấp nhận mất dữ liệu. Đổi mật khẩu trong `.env` không đổi mật khẩu của volume MySQL đã tồn tại; thay mật khẩu có kiểm soát thay vì xóa volume. Docker volume không phải bản sao lưu.
