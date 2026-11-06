# SE_10 — BlueMoon Complex

Hệ thống quản lý chung cư BlueMoon (v1.0): tài khoản và phân quyền, hộ/nhân khẩu, kỳ thu và thu phí, báo cáo. Repo này là monorepo gồm **backend** (Java 21, Spring Boot 4.0.9, Maven, MySQL 8.4) và **frontend** (React 19, TypeScript, Vite; xem [`frontend/README.md`](frontend/README.md)). Giao diện mới ghép API đăng nhập và tạo tài khoản; phần còn lại chạy bằng dữ liệu mẫu.

> Nguồn sự thật về tiến độ là [`docs/sprint-1/TASKS.md`](docs/sprint-1/TASKS.md). Một task chỉ được tick khi có bằng chứng (lệnh, kết quả, commit/PR), không tick vì đã viết code.

## Trạng thái (07/11/2026)

| Task | Nội dung | Trạng thái |
|---|---|---|
| F00 | Repo, wrapper, hướng dẫn, CI | Xong (PR #4) |
| F01 | Chốt hợp đồng, quyền và dữ liệu (D13–D17) | Xong theo xác nhận của Sơn; chưa có văn bản của cả nhóm |
| F02 | Nền tảng backend/DB, audit cùng transaction | Xong (PR #4) |
| S01 | Đăng nhập, session, vai trò, audit, chống dò mật khẩu | Xong (PR #5) |
| S02 | Cấp account, email mô phỏng, bootstrap BQT | Xong (PR #6) |
| S05 | Hộ, nhân khẩu, chuyển đi, lịch sử biến động | Xong (PR #7) |
| **S04** | **Vô hiệu, bật lại, mở khóa account; hủy session; đọc lại quyền mỗi request** | **Xong, đã tick** (nhánh `feat/s04-account-disable`, chưa PR). `verify` + `-Pintegration` xanh (86 + 75 test, MySQL 8.4, JDK 21.0.9, 06/11/2026) và đã chạy thử với backend thật. Phần UI của AC thuộc Kiệt; chờ Tuấn review và PR |
| Frontend | Giao diện cho 24 màn v1 (sáng/tối) | Có giao diện; đã nối API cho S01, S02, S04, S05, các màn còn lại dùng dữ liệu mẫu (xem `frontend/README.md`) |
| S03, S06–S15 | Đổi mật khẩu, khai báo/duyệt, đơn giá, kỳ thu, thu tiền, báo cáo, backup, hồi quy, bàn giao | Chưa bắt đầu |
**Có thể dùng ngay:**
- Health, metadata, session JDBC, audit.
- Đăng nhập, đăng xuất, xem phiên (`/api/v1/auth/*`) và khóa tạm thời khi sai mật khẩu nhiều lần.
- Lệnh tạo BQT đầu tiên (profile `bootstrap`); ADMIN cấp account qua `POST /api/v1/accounts` (kèm email mô phỏng).
- ADMIN vô hiệu, bật lại, mở khóa account (S04); trạng thái và vai trò đọc lại từ DB ở mỗi request nên cookie cũ chết ngay.
- Hộ và nhân khẩu cho ADMIN, chuyển đi, lịch sử biến động, và cư dân xem thành viên hộ mình (`/api/v1/households`, `/api/v1/residents`).

**Chưa có:** tài khoản mặc định (cố ý); đổi mật khẩu và xóa cờ đổi mật khẩu lần đầu (S03); API liệt kê tài khoản; khai báo/duyệt, tạm vắng, quay lại cư trú (S06); mọi API phí và báo cáo. Mọi endpoint không khai báo đều bị chặn mặc định (`denyAll`).

> **Lưu ý khi thử:** vì S03 chưa có, tài khoản mới cấp bị chặn mọi API (403 `PASSWORD_CHANGE_REQUIRED`) cho tới khi có luồng đổi mật khẩu. Tài khoản BQT tạo bằng lệnh bootstrap thì không bị chặn.

## Cấu trúc repo

```text
backend/            Spring Boot (Maven Wrapper, Dockerfile)
  src/main/java/vn/edu/se10/bluemoon/
    config/         SecurityConfig (session, CSRF, CORS, quyền), ClockConfig
    common/         audit (AuditService), error, web (request ID)
    identity/       api / application / persistence: đăng nhập, session, khóa tài khoản, cấp account, vô hiệu/bật lại/mở khóa, bootstrap BQT
    residency/      api / application / persistence: hộ, nhân khẩu, chuyển đi, lịch sử biến động, HouseholdLookup
    systeminfo/     metadata công khai
  src/main/resources/db/migration/   Flyway V1–V6
  src/test/         unit/MVC (verify) và integration (-Pintegration)
docs/               kiến trúc, yêu cầu, quyết định, kế hoạch, kiểm chứng
frontend/           React + TypeScript + Vite (giao diện, dữ liệu mẫu; xem frontend/README.md)
scripts/            dev.sh (docker compose), verify.sh
compose.yaml        MySQL 8.4 + backend cho môi trường local
```

Package theo tính năng: `api` → `application` → `persistence`. Module không gọi repository của module khác; dùng interface ở `application` (ví dụ `identity` kiểm tra trạng thái hộ qua `HouseholdLookup`). Chi tiết và quy ước: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Chạy dự án từng bước

Yêu cầu máy: **Git**, **Docker Desktop** (Compose v2), **JDK 21** (build chỉ chấp nhận đúng JDK 21), **Node 22+** và npm (cho frontend), curl. macOS: `brew install openjdk@21` rồi đặt `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`.

Có hai cách. **Cách B (khuyến nghị khi phát triển)** chạy MySQL trong Docker, backend bằng JAR hoặc IDE, frontend bằng Vite. **Cách A** chạy cả backend trong Docker, nhanh nhưng khó sửa và gỡ lỗi.

### Chung cho cả hai cách

1. Lấy mã và chuyển sang nhánh cần chạy (ví dụ nhánh S04 để thử vô hiệu/mở khóa).
2. Tạo file môi trường và đặt mật khẩu local:
   ```bash
   cp .env.example .env
   # Mở .env, thay MYSQL_PASSWORD và MYSQL_ROOT_PASSWORD (mật khẩu chỉ dùng trên máy bạn).
   ```
   `.env` chỉ Docker Compose đọc, dùng tên `MYSQL_*`; Maven và IDE không tự đọc nó. Không commit `.env`.

### Cách B: MySQL Docker + backend JAR + frontend Vite

1. **Bật MySQL** và chờ sẵn sàng (cột Health là `healthy`):
   ```bash
   docker compose up -d db
   docker compose ps
   ```
2. **Build backend** (chạy test và đóng gói JAR, mất khoảng 1 phút; không cần DB):
   ```bash
   cd backend
   ./mvnw -B -ntp verify          # Windows: mvnw.cmd
   ```
   Báo `Dự án dùng JDK 21` nghĩa là `java` mặc định không phải JDK 21: đặt `JAVA_HOME` rồi chạy lại.
3. **Đặt biến kết nối DB** trong cửa sổ terminal sẽ chạy backend (đọc từ `.env`, không in mật khẩu):
   ```bash
   set -a; . ../.env; set +a
   export DB_HOST=127.0.0.1 DB_PORT=${DB_PORT:-3306} DB_NAME=$MYSQL_DATABASE DB_USER=$MYSQL_USER DB_PASSWORD=$MYSQL_PASSWORD
   ```
   PowerShell: `$env:DB_HOST="127.0.0.1"; $env:DB_NAME="bluemoon"; $env:DB_USER="bluemoon"; $env:DB_PASSWORD="..."` (không dán mật khẩu vào chat, issue hay commit).
4. **Tạo BQT đầu tiên** (một lần, DB mới chưa có tài khoản nào). Lệnh hỏi tên đăng nhập, họ tên, email, mật khẩu hai lần (từ 8 ký tự, không hiện trên màn hình):
   ```bash
   java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap
   ```
   DB đã có ADMIN thì lệnh in "Đã có ADMIN, bỏ qua." và không làm gì. Tự đặt và nhớ mật khẩu: không có tài khoản mặc định và không có API đặt lại (xem "Quên mật khẩu ADMIN" bên dưới).
5. **Chạy backend** (giữ cửa sổ này mở; Flyway tự tạo bảng khi khởi động):
   ```bash
   java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
   # hoặc trong IDE/Maven: ./mvnw spring-boot:run  (cần cùng các biến DB_* ở bước 3)
   ```
   Kiểm tra ở terminal khác; cả hai phải trả `200`:
   ```bash
   curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/actuator/health
   curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/v1/auth/csrf
   ```
   Nếu `auth/csrf` trả 401, cổng 8080 đang có một backend khác (thường là container `backend` của Docker từ lần chạy trước): xem mục "Xử lý sự cố".
6. **Chạy frontend** (cửa sổ khác; cần thư mục `frontend/` trong nhánh bạn đang dùng):
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Mở đúng **http://localhost:5173** (không dùng `127.0.0.1`: CORS của backend chỉ nhận origin `http://localhost:5173`). Vite chuyển tiếp `/api` sang `http://localhost:8080`.
7. **Đăng nhập** ở màn Login bằng tên và mật khẩu BQT vừa tạo. Giữ chế độ **Backend thật** (không bấm "Demo"). Sau đó thử luồng dưới đây.

### Cách A: Docker chạy cả backend

```bash
docker compose up --build -d
docker compose logs -f backend
```
Container `backend` build từ mã đang checkout và dùng cổng 8080. **Không chạy cùng lúc với JAR/IDE** (cổng trùng). Tạo BQT đầu tiên: chạy lệnh bootstrap trong container với `-it` (chưa kiểm chứng) hoặc dùng JAR như Cách B bước 4. Frontend chạy như bước 6 ở trên.

## Frontend

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173, proxy /api tới http://localhost:8080
npm test         # 22 test
npm run build
```
Cần backend chạy ở cổng 8080 để đăng nhập thật; không có backend thì dùng chế độ Demo ở màn đăng nhập (chỉ có khi chạy `npm run dev`). Có chế độ sáng, tối và tự động theo hệ thống. Tiến độ, phần đã ghép API và việc còn mở: [`frontend/README.md`](frontend/README.md); thiết kế: `frontend/DESIGN.md`; báo cáo audit: `frontend/AUDIT.md`.

## Phát triển bằng IDE

### Thử luồng S04 + S05 bằng giao diện

1. **Hộ:** Hộ gia đình → Thêm hộ (ví dụ `DEMO-01`, căn `DEMO-A01`, 50.5 m²). Thêm lần hai cùng mã phải báo trùng.
2. **Nhân khẩu:** trang hộ → Thêm nhân khẩu (dùng số giấy tờ giả). Số giấy tờ trùng với người đang cư trú phải bị chặn và thông báo không chứa số.
3. **Chuyển đi:** Chuyển đi và lưu trữ → chọn hộ, nhân khẩu, ngày, lý do. Người đó sang tab Lưu trữ, không bị xóa.
4. **Cấp tài khoản cư dân:** Tài khoản → Tạo tài khoản, vai trò Cư dân, gắn hộ. Mật khẩu tạm không hiện trên màn hình; xem trong thư mô phỏng (bảng `mock_emails`):
   ```bash
   docker compose exec db sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE" -e "SELECT id, recipient_email, body FROM mock_emails ORDER BY id DESC LIMIT 1\G"'
   ```
   Tài khoản mới bị 403 `PASSWORD_CHANGE_REQUIRED` vì S03 chưa có. **Chỉ để thử trên DB local**, có thể tắt cờ cho đúng tài khoản đó: `UPDATE users SET must_change_password = FALSE WHERE username = '<tên>'` (chạy qua `mysql` như trên với `-e`). Đây không phải hành vi của sản phẩm.
5. **Vô hiệu, bật lại, mở khóa:** Tài khoản (danh sách chỉ hiện tài khoản vừa cấp trong phiên, **đừng tải lại trang**, vì chưa có API liệt kê). Đăng nhập tài khoản cư dân ở cửa sổ ẩn danh; ở cửa sổ BQT bấm Vô hiệu hóa: lần bấm kế của cư dân bị đưa về đăng nhập. Nhập sai mật khẩu 5 lần để bị khóa, rồi BQT bấm Mở khóa.
6. **Kiểm audit:** `SELECT id, action, entity_type, entity_id, changes FROM audit_log ORDER BY id DESC LIMIT 15;` Phải thấy `HOUSEHOLD_CREATED`, `RESIDENT_CREATED`, `RESIDENT_MOVED_OUT`, `ACCOUNT_DISABLED`, `SESSIONS_REVOKED`, `ACCOUNT_ENABLED`, `ACCOUNT_UNLOCKED`; cột `changes` không chứa email hay mật khẩu.

Mở shell MySQL tương tác: `docker compose exec db sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE"'`.

### Quên mật khẩu ADMIN (chỉ DB local)

Chưa có API đặt lại mật khẩu (S03). Trên DB local, tạo hash BCrypt rồi ghi đè; thay `<tên>`. Việc này đi thẳng vào DB, **không có dòng audit**; không dùng trên DB thật:
```bash
read -rs PW; HASH=$(printf '%s' "$PW" | htpasswd -niBC 10 u | tr -d '\n' | cut -d: -f2); unset PW
printf "UPDATE users u JOIN roles r ON r.id=u.role_id SET u.password_hash='%s', u.failed_login_count=0, u.lock_level=0, u.locked_until=NULL WHERE r.code='ADMIN' AND u.username='<tên>';\nDELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME='<tên>';\n" "$HASH" \
  | docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE"'
```
Dự án chỉ nhận hash BCrypt (`BCryptPasswordEncoder`, hash dạng `$2a$`/`$2y$`).

### Ghi chú về cấu hình

- Profile mặc định là `local`: cookie session không có cờ `Secure` vì chưa có TLS. Không dùng cấu hình này để triển khai ra Internet.
- Không chạy backend trong Docker và trong IDE/JAR cùng lúc (cùng cổng 8080). Nếu `.env` đổi `DB_PORT`, đổi tương ứng ở bước 3.
- Health chỉ báo trạng thái ứng dụng và DB, không có chi tiết. `/actuator/health/liveness` chỉ kiểm tra tiến trình sống.


## Xác thực và phân quyền (S01)

Session lưu trong MySQL (bảng `SPRING_SESSION`), hết hạn sau 30 phút; cookie `SESSION` là HttpOnly, SameSite=Lax. CSRF luôn bật: mọi `POST`, `PATCH` cần header `X-CSRF-TOKEN`. CORS chỉ nhận origin cấu hình ở `CORS_ALLOWED_ORIGIN` (mặc định `http://localhost:5173`).

Ba vai trò (D13): `ADMIN` (BQT), `ACCOUNTANT` (kế toán), `RESIDENT` (cư dân, gắn đúng một hộ). Vai trò thứ tư dành cho cơ quan chức năng sẽ thêm bằng migration riêng ở task S12.

| Endpoint | Cần đăng nhập | Kết quả |
|---|---|---|
| `GET /api/v1/auth/csrf` | không | 200 `{headerName, token}` |
| `POST /api/v1/auth/login` | không; cần CSRF | 200 `{username, fullName, role, householdId, mustChangePassword}`; 400 body sai; 401 mọi lỗi đăng nhập; 403 thiếu/sai CSRF |
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
Lưu ý: DB mới chưa có tài khoản nào (không có tài khoản mặc định, không có trong migration). Tạo BQT đầu tiên bằng lệnh bootstrap bên dưới; các test tự tạo tài khoản fixture.

**Tạo BQT đầu tiên (S02, D16):** chạy một lần trong terminal tương tác, sau khi `./mvnw verify` đã tạo JAR và `DB_*` đã export:
```bash
cd backend
java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap
```
- Nhập tên đăng nhập, họ tên, email, mật khẩu hai lần (mật khẩu không hiện trên màn hình). Không có tham số dòng lệnh hay biến môi trường cho các giá trị này.
- Mật khẩu từ 8 ký tự đến 72 byte UTF-8, hai lần nhập phải giống nhau. Nếu sai, lệnh in "Dữ liệu không hợp lệ." kèm stack trace mà không nói quy tắc nào sai (giới hạn đã biết, chưa sửa).
- Đã có bất kỳ ADMIN (kể cả `INACTIVE`) thì lệnh in "Đã có ADMIN, bỏ qua." và không làm gì. Không có terminal tương tác thì thoát mã khác 0. Trong Docker cần `-it` (chưa kiểm chứng).
- Chạy bằng `spring-boot:run` không dùng được vì không có console. Audit: `BOOTSTRAP_ADMIN` với actor NULL; tài khoản bootstrap có `must_change_password = FALSE`.

**ADMIN cấp account (S02, D14):** `POST /api/v1/accounts` (cần đăng nhập ADMIN và CSRF), body `{username, fullName, email, role, householdId}`; `role` là `ADMIN`, `ACCOUNTANT` hoặc `RESIDENT` (RESIDENT bắt buộc có `householdId` của hộ `ACTIVE`; vai trò khác không được kèm hộ). 201 `{id, username, fullName, email, role, householdId, emailStatus}` và không có mật khẩu. Lỗi: 400 (`HOUSEHOLD_REQUIRED`, `HOUSEHOLD_INVALID`, `HOUSEHOLD_NOT_ALLOWED` hoặc body sai), 409 (`EMAIL_ALREADY_USED`, `USERNAME_ALREADY_USED`, `ACCOUNT_CONFLICT`). Thiếu hoặc sai CSRF trả 403 trước khi tới bước kiểm tra quyền.
- Mật khẩu tạm do server sinh, không hết hạn, chỉ nằm trong thư mô phỏng ở bảng `mock_emails`; không có API đọc. Xem bằng SQL: `SELECT id, recipient_email, subject, status, created_at, body FROM mock_emails ORDER BY id DESC`.
- Account mới có cờ `must_change_password`: đăng nhập được nhưng mọi API ngoài `/auth/{me,csrf,login,logout}`, health và system/info đều trả 403 `code: PASSWORD_CHANGE_REQUIRED` cho tới khi S03 có luồng đổi mật khẩu và xóa cờ.
- Không bật log Hibernate `org.hibernate.orm.jdbc.bind=TRACE`: nó in email, nội dung thư và hash mật khẩu. Logger `org.hibernate.orm.jdbc.error` đã đặt `OFF` để lỗi trùng khóa không in email.
- Nâng cấp từ S01 trên DB dev đã có phiên đăng nhập: xóa bảng `SPRING_SESSION` (chỉ môi trường dev) vì `AuthenticatedUser` đã đổi; hành vi với phiên cũ chưa kiểm chứng.

**Mọi lỗi đăng nhập trả cùng một body 401** (sai mật khẩu, account không tồn tại, bị khóa, `INACTIVE`, RESIDENT chưa có hộ) để không lộ account tồn tại. Response có 5 trường (`username`, `fullName`, `role`, `householdId`, `mustChangePassword`); không bao giờ có hash, email, `status` hay cột khóa.

**Chống dò mật khẩu (D17, D18):** khóa theo account, không theo IP.
| Lần khóa | 1 | 2 | 3 | 4 | 5 | 6 | từ 7 trở đi |
|---|---|---|---|---|---|---|---|
| Thời gian khóa | 1 phút | 5 | 10 | 15 | 30 | 60 | cộng đều +60 phút mỗi lần (120, 180, …), không trần |

- Lần khóa đầu cần 5 lần sai liên tiếp; từ lần khóa thứ hai, sai thêm **1 lần** là lên bậc kế.
- Đang khóa: mật khẩu đúng vẫn bị từ chối, không đếm thêm và không gia hạn. Hết khóa thì đăng nhập đúng đặt lại bộ đếm.
- `ADMIN` không bị khóa (không có lớp chặn dò mật khẩu ADMIN ở tầng ứng dụng; xem rủi ro trong `docs/DECISIONS.md`).
- Thời điểm khóa lưu UTC (`DATETIME(6)`); kết quả không phụ thuộc múi giờ của JVM.
- Mở khóa thủ công bởi BQT: xem mục "Vô hiệu, bật lại và mở khóa account (S04)" bên dưới.

**Audit:** `LOGIN_SUCCESS`, `LOGIN_FAILED`, `ACCOUNT_LOCKED` ghi cùng transaction với thay đổi dữ liệu (rollback thì audit cũng rollback). `AuditService.recordBootstrap` dành riêng cho lệnh tạo BQT đầu tiên (actor NULL, action cố định `BOOTSTRAP_ADMIN`).

## Vô hiệu, bật lại và mở khóa account (S04)

Tiền tố `/api/v1`. Cả ba route chỉ dành cho `ADMIN`, cần đăng nhập và CSRF, trả `204` không có body.

| Route | Việc làm | Lỗi |
|---|---|---|
| `POST /accounts/{id}/disable` | Đặt `status = INACTIVE` và xóa mọi phiên của account (`SPRING_SESSION` theo username) | 409 `ACCOUNT_DISABLE_CONFLICT`: tự vô hiệu chính mình, vô hiệu ADMIN `ACTIVE` cuối cùng, hoặc account đã `INACTIVE`; 404 `ACCOUNT_NOT_FOUND` |
| `POST /accounts/{id}/enable` | Đặt `status = ACTIVE`; người đó đăng nhập lại bằng mật khẩu hiện có | 409 `ACCOUNT_ALREADY_ACTIVE` nếu đang `ACTIVE`; 404 |
| `POST /accounts/{id}/unlock` | Đặt lại `failed_login_count`, `lock_level` về 0 và `locked_until` về `NULL` | 409 `ACCOUNT_NOT_LOCKED` nếu account là ADMIN hoặc không còn dấu vết khóa (cả ba cột đã sạch); 404 |

**Quy tắc đã chốt (D22 và hợp đồng S04):**
- Mỗi request đã đăng nhập đọc lại `status`, vai trò và `household_id` từ DB, không tin giá trị trong session. Cookie của account bị vô hiệu nhận 401 ngay; hạ quyền hoặc đổi hộ có hiệu lực ở request kế tiếp. Khi account không còn `ACTIVE`, phiên bị hủy trước khi trả 401.
- Mở khóa đặt lại cả bậc khóa (`lock_level`), kể cả khi khóa đã hết hạn: nếu không, chỉ một lần sai nữa là lên bậc kế (D17, D18). ADMIN không bị khóa nên không có mở khóa ADMIN.
- Vô hiệu, hủy phiên và audit nằm trong cùng một transaction. Audit: `ACCOUNT_DISABLED`, `ACCOUNT_ENABLED`, `ACCOUNT_UNLOCKED`, `SESSIONS_REVOKED`; không ghi email hay mật khẩu. Thao tác của actor đã bị vô hiệu giữa chừng bị từ chối 401.
- Không đổi hộ hàng loạt khi hộ chuyển trạng thái (không hủy phiên cư dân trong hộ); dựa vào việc đọc lại mỗi request.

**Giới hạn đã biết:** chưa có API liệt kê tài khoản; thiếu test đua thật cho trường hợp actor bị vô hiệu sau khi qua filter; chưa kiểm cookie thật ngoài MockMvc; `lock()` không đọc lại vai trò của actor; 409 dùng chung `detail` "Thông tin bị trùng" (có `code` riêng). Chi tiết: khối S04 trong `docs/sprint-1/TASKS.md` khi task được tick.


## Hộ, nhân khẩu và biến động (S05)

Tiền tố `/api/v1`. Mọi route chỉ dành cho `ADMIN`, trừ `GET /households/me/residents` (chỉ `RESIDENT`). `ACCOUNTANT` nhận 403 ở mọi route này (quyền đọc hộ của Kế toán chờ Quân xác nhận). Route ghi cần CSRF. Không có route `DELETE`.

| Route | Kết quả |
|---|---|
| `POST /households` | 201 hộ mới (`ACTIVE`); body `{householdCode, apartmentCode, areaM2, address?}` |
| `GET /households` | Trang hộ; lọc `apartmentCode`, `householdCode`, `status` (`ACTIVE` hoặc `INACTIVE`, khớp chính xác); `page` (mặc định 0), `size` (mặc định 20, tối đa 100) |
| `GET /households/{id}` | Một hộ |
| `PATCH /households/{id}` | Sửa `areaM2` và `address`; trường vắng hoặc null là giữ nguyên; không sửa được mã hộ và mã căn hộ |
| `POST /households/{id}/residents` | 201 nhân khẩu mới (`RESIDING`); body `{fullName, identityNumber?, dateOfBirth?, phone?, relationshipToHead?}`; hộ phải `ACTIVE` |
| `GET /households/{id}/residents` | Trang nhân khẩu; `status` tùy chọn, mặc định là `RESIDING` và `TEMPORARILY_ABSENT` |
| `GET /residents/{id}`, `PATCH /residents/{id}` | Xem, sửa (không đổi hộ, không đổi trạng thái) |
| `POST /residents/{id}/move-out` | Chuyển đi; body `{effectiveDate, reason?}` |
| `GET /households/{id}/residency-changes` | Trang lịch sử biến động |
| `GET /households/me/residents` | Thành viên hiện tại của hộ mình; hộ lấy từ session, không nhận id từ client |

Trang trả `{content, page, size, totalElements, totalPages}`. Hộ trả `{id, householdCode, apartmentCode, areaM2, address, status, createdAt, updatedAt}`.

**Quy tắc đã chốt (D20, D21):**
- `areaM2` lớn hơn 0 và tối đa hai chữ số thập phân; quá thì 400, không tự làm tròn. Mã hộ và mã căn hộ do ADMIN nhập, tối đa 50 ký tự, không phân biệt hoa thường, không sửa sau khi tạo; trùng thì 409 `HOUSEHOLD_CONFLICT`, không nêu giá trị trùng.
- Trạng thái cư trú: `RESIDING`, `TEMPORARILY_ABSENT`, `MOVED_OUT`. Danh sách hiện tại gồm hai trạng thái đầu. Chuyển đi chỉ đổi trạng thái sang `MOVED_OUT` và ghi một dòng `residency_changes` (`MOVE_OUT`, `APPROVED`, người ghi là ADMIN); không xóa bản ghi, không hoàn tác, không tự khóa account, không tự đóng hộ.
- `effectiveDate` không ở tương lai theo ngày Asia/Ho_Chi_Minh; chuyển đi lần hai trả 409 `RESIDENT_ALREADY_MOVED_OUT`.
- Số giấy tờ (`identityNumber`) không bắt buộc. Trùng được kiểm ở service, trên toàn hệ thống, giữa các nhân khẩu `RESIDING` và `TEMPORARILY_ABSENT`; người đã chuyển đi không tính. Trùng thì 409 `IDENTITY_NUMBER_ALREADY_USED`, không nêu số. DB cố ý không có UNIQUE cho cột này (xem `docs/DECISIONS.md`, mục "Schema nghiệp vụ", điểm 5).
- ADMIN thấy đầy đủ thông tin nhân khẩu. Cư dân chỉ thấy `id`, `fullName`, `dateOfBirth`, `relationshipToHead`, `residencyStatus`; không thấy số giấy tờ và số điện thoại. Lịch sử biến động không trả `reason`.
- Audit cùng transaction: `HOUSEHOLD_CREATED`, `HOUSEHOLD_UPDATED`, `RESIDENT_CREATED`, `RESIDENT_UPDATED`, `RESIDENT_MOVED_OUT`. Audit chỉ ghi id, trạng thái, ngày hiệu lực và tên trường đã đổi, không ghi họ tên, số giấy tờ, số điện thoại, địa chỉ hay lý do.
- Lỗi có `code`: 400 (`HOUSEHOLD_INVALID`, `INVALID_DATE`, `INVALID_FILTER`, `INVALID_RESIDENT`, `EMPTY_UPDATE`, `VALIDATION`), 404 (`HOUSEHOLD_NOT_FOUND`, `RESIDENT_NOT_FOUND`). Lỗi do bean validation (độ dài, thiếu trường) trả 400 không kèm `code`.

**Giới hạn đã biết:** mã hộ và số giấy tờ chưa được cắt khoảng trắng đầu/cuối (`"H1"` và `"H1 "` là hai giá trị khác nhau); PATCH không xóa được một trường (null là giữ nguyên); mọi thao tác ghi nhân khẩu bị xếp tuần tự toàn cục để chống đua kiểm tra trùng số giấy tờ; chưa có endpoint ghi tạm vắng hay quay lại cư trú (S06); `householdId` được đọc lại từ DB ở mỗi request (S04), nên đổi hộ của account có hiệu lực ngay. Danh sách đầy đủ ở khối S05 trong `docs/sprint-1/TASKS.md`.

## Cơ sở dữ liệu và migration

Flyway tự áp migration khi khởi động; Hibernate chỉ `validate`, không `update/create`.

| Migration | Nội dung |
|---|---|
| V1 | Bảng session JDBC |
| V2 | Vai trò, tài khoản, `audit_log` |
| V3 | Hộ, nhân khẩu, biến động cư trú (`households`, `residents`, `residency_changes`) |
| V4 | Email tài khoản, `users.household_id` |
| V5 | Cột chống dò mật khẩu: `failed_login_count`, `lock_level`, `locked_until` |
| V6 | Cờ `must_change_password` và bảng `mock_emails` (email mô phỏng) |

S04 và S05 không thêm migration. Phí và vai trò thứ tư bắt đầu từ V7. Quy tắc: không sửa migration đã merge hoặc đã chạy trên DB của người khác; thêm `V<n>__mo_ta.sql` mới. Nếu đã sửa migration trên DB cục bộ, Flyway báo sai checksum: tạo lại DB cục bộ, không sửa tay `flyway_schema_history`.

## Test và build

```bash
cd backend
./mvnw -B -ntp verify                    # không cần DB
./mvnw -B -ntp -Pintegration verify      # cần MySQL thật và biến DB_*
```
- Lệnh đầu chạy unit/MVC/security (86 test ở nhánh S04, 06/11/2026), đóng gói JAR, không cần DB.
- Lệnh thứ hai chạy thêm integration (75 test: `BootstrapIT`, `RoleAuditIT`, `IdentityCookieIT`, `IdentitySessionIT`, `AccountProvisioningIT`, `AccountAtomicityIT`, `AccountManagementIT`, `BootstrapAdminIT`, `HouseholdIntegrationIT`, `ResidentIntegrationIT`). Không tự bỏ qua khi thiếu DB. Lần chạy gần nhất ở nhánh S04 (06/11/2026, MySQL 8.4, JDK 21.0.9) xanh cả hai, `BUILD SUCCESS`.
- Frontend: `cd frontend && npm test` (29 test), `npm run build`.
- `BootstrapAdminIT` chạy `java -jar target/...jar` thật nên cần JAR đã được đóng gói (lệnh `verify` làm việc đó).
- Integration chạy trên DB `bluemoon` đang cấu hình: tự áp migration chưa có, tự thêm rồi xóa dữ liệu fixture, và `RoleAuditIT` đổi tên role `ADMIN` rồi khôi phục. `ResidentIntegrationIT` ghi vào hộ có id nhỏ nhất trong một test đua. Đừng chạy trên DB có người khác đang dùng hoặc dữ liệu thật.
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
| [`docs/DECISIONS.md`](docs/DECISIONS.md) | Quyết định D01–D22 và các điểm chưa xác nhận |
| [`docs/sprint-1/PLAN.md`](docs/sprint-1/PLAN.md) | Kế hoạch Sprint 1 |
| [`docs/sprint-1/TASKS.md`](docs/sprint-1/TASKS.md) | Checklist và khối bằng chứng từng task |
| [`docs/VALIDATION.md`](docs/VALIDATION.md) | Lệnh đã chạy và kết quả |
| [`docs/SOURCE_REVIEW.md`](docs/SOURCE_REVIEW.md) | Đối chiếu nguồn và mâu thuẫn |
| [`docs/STRUCTURE.md`](docs/STRUCTURE.md) | Cấu trúc dự án đầy đủ |
| [`frontend/README.md`](frontend/README.md) | Frontend: chạy, cấu trúc, tiến độ, việc còn mở |

## Xử lý sự cố

| Triệu chứng | Cách xử lý |
|---|---|
| `Dự án dùng JDK 21` | Đặt `JAVA_HOME` trỏ tới JDK 21 rồi chạy lại |
| `Unresolved compilation problems` | IDE ghi đè `backend/target/`; chạy `./mvnw clean verify` |
| Cổng 8080 bận | Dừng backend Docker hoặc IDE, chỉ chạy một |
| Flyway báo sai checksum | Migration đã bị sửa; tạo lại DB cục bộ (không sửa `flyway_schema_history`) |
| `Access denied for user 'bluemoon'` khi chạy integration | `DB_PASSWORD` không khớp mật khẩu mà volume MySQL đã được tạo; đặt đúng `DB_USER`, `DB_PASSWORD`, `DB_NAME` (không có khoảng trắng sau dấu `=`). Không xóa volume để sửa |
| Đăng nhập luôn 401 trên DB mới | Chưa có tài khoản; chạy lệnh bootstrap BQT (mục "Tạo BQT đầu tiên") hoặc dùng fixture trong test |
| Trình duyệt không gửi cookie session | Cookie `Secure` chỉ tắt ở profile `local`; kiểm tra profile đang chạy |
| `GET /api/v1/auth/csrf` trả 401 hoặc web báo "sai mật khẩu" dù mật khẩu đúng | Cổng 8080 đang có backend khác bản (thường container `backend` của Docker từ lần trước). Dừng nó: `docker compose stop backend` (MySQL giữ nguyên), rồi chạy lại JAR. Kiểm: `curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/v1/auth/csrf` phải là 200 |
| Đăng nhập báo lỗi CORS hoặc 403 khi mở trang bằng `127.0.0.1` | Mở đúng `http://localhost:5173`; CORS chỉ nhận origin cấu hình ở `CORS_ALLOWED_ORIGIN` |
| Tài khoản mới đăng nhập được nhưng mọi trang báo 403 `PASSWORD_CHANGE_REQUIRED` | Chờ S03 (đổi mật khẩu). Chỉ để thử trên DB local: xem "Thử luồng S04 + S05", bước 4 |
| Quên mật khẩu ADMIN | Xem "Quên mật khẩu ADMIN (chỉ DB local)" |
| `npm run dev` báo cổng 5173 bận | Có một Vite khác đang chạy; dùng luôn nó hoặc tắt nó rồi chạy lại (`strictPort`) |
| Danh sách tài khoản trống sau khi tải lại trang | Bình thường: chưa có API liệt kê tài khoản; danh sách chỉ nhớ tài khoản cấp trong phiên |

## Dừng và dữ liệu

```bash
docker compose down
```
Volume vẫn giữ dữ liệu. Không chạy `down -v` khi chưa xác nhận chấp nhận mất dữ liệu. Đổi mật khẩu trong `.env` không đổi mật khẩu của volume MySQL đã tồn tại; thay mật khẩu có kiểm soát thay vì xóa volume. Docker volume không phải bản sao lưu.
