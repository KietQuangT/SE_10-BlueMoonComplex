# BlueMoon Complex

BlueMoon là ứng dụng quản lý chung cư cho ban quản trị, kế toán và cư dân. Backend Spring Boot sử dụng MySQL; frontend React cung cấp giao diện theo vai trò. Thông báo email được mô phỏng, không gửi email thật.

## Chức năng v1.0

- Đăng nhập, quản lý tài khoản và vai trò; đổi mật khẩu, vô hiệu hóa tài khoản và ghi nhật ký nghiệp vụ.
- Quản lý hộ, nhân khẩu, biến động cư trú; cư dân khai báo tạm vắng và ban quản trị duyệt hoặc từ chối.
- Thiết lập đơn giá, kỳ thu và khoản đóng góp tự nguyện; ghi nhận tiền mặt, theo dõi công nợ, hóa đơn và lịch sử thu của hộ.
- Xuất báo cáo tài chính, cư trú và lập gói dữ liệu cư trú được quản trị viên thứ hai phê duyệt trước khi chuyển ngoài hệ thống.
- Sao lưu định kỳ và diễn tập phục hồi vào cơ sở dữ liệu riêng. QR/VNeID, khách thăm, ticket và mượn đồ ngoài phạm vi v1.

Trạng thái nghiệm thu và các kiểm tra còn thiếu: [VALIDATION.md](docs/VALIDATION.md). Phạm vi chi tiết: [REQUIREMENTS.md](docs/REQUIREMENTS.md).

## Chuẩn bị

Cần Docker và Docker Compose, Node.js/npm, JDK 21, `curl` và `lsof`; các script dùng Bash. Từ thư mục gốc:

```bash
cp .env.example .env
```

Sửa mật khẩu local trong `.env`; không commit file này. Cổng mặc định: MySQL 3306, backend 8080, frontend 5173.

## Chạy nhanh bằng script

```bash
./scripts/dev-all.sh
```

Script build và chạy MySQL/backend bằng Compose, chờ health tối đa 2 phút, cài dependency frontend nếu cần và chạy Vite tại http://localhost:5173. Ctrl+C dừng frontend; `docker compose stop` dừng backend và MySQL mà giữ dữ liệu.

Nếu cổng 3306, 8080 hoặc 5173 bị chiếm, chạy `./scripts/free-dev-ports.sh`: script liệt kê tiến trình/container và hỏi trước khi dừng từng mục. Sau đó chạy lại `dev-all.sh`. Hai script dùng các cổng mặc định.

## Build và chạy thủ công

1. Chạy MySQL và backend:

   ```bash
   docker compose up --build -d
   curl -fsS http://localhost:8080/actuator/health
   ```

2. Mở terminal khác và chạy frontend:

   ```bash
   cd frontend
   npm ci
   npm run build
   npm run dev
   ```

3. Để chạy backend từ host/IDE thay Compose backend: dùng `docker compose up -d db`, thiết lập `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` theo `.env`, rồi chạy `cd backend && ./mvnw -B -ntp verify && ./mvnw spring-boot:run`. Maven/IDE không tự đọc `.env`; trên Windows dùng `mvnw.cmd`.

4. Với cơ sở dữ liệu mới, tạo ADMIN đầu tiên theo hướng dẫn bên dưới. Không có tài khoản mặc định.

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

## Xem mật khẩu tạm trong email mô phỏng

Khi ADMIN vừa tạo tài khoản và cần lấy mật khẩu đăng nhập lần đầu, đảm bảo MySQL của Docker Compose đang chạy rồi dùng tên đăng nhập của tài khoản:

```bash
./scripts/mock-email.sh <username>
# Liệt kê mọi tài khoản còn mật khẩu tạm:
./scripts/mock-email.sh --all
```

Ví dụ: `./scripts/mock-email.sh accountant`. Script chỉ đọc thư cấp tài khoản mới nhất của username đó. Sau khi người dùng đổi mật khẩu, mật khẩu tạm bị xóa khỏi thư và script không còn trả thư này. `--all` hiển thị username và thư mới nhất của mỗi tài khoản còn mật khẩu tạm. Đầu ra có mật khẩu: chỉ chạy trong terminal riêng, không lưu hoặc chia sẻ log. Script dùng tài khoản MySQL được cấu hình trong container Compose.

## Kiểm thử

```bash
cd backend
./mvnw -B -ntp verify
# MySQL test riêng đang chạy; export DB_* trước khi chạy:
./mvnw -B -ntp -Pintegration verify
```

Frontend: `cd frontend && npm ci && npm test && npm run build`. Test backup: `bash tests/backup/run_all_tests.sh`. Test script email: `bash tests/scripts/test-mock-email.sh`. Hướng dẫn ca thủ công và báo cáo: [tests/README.md](tests/README.md). Không dùng dữ liệu cư dân thật cho kiểm thử.

## Backup và diễn tập phục hồi

| Công cụ | Khi sử dụng |
|---|---|
| `scripts/backup.conf.example` | Sao chép cấu hình ra ngoài repository, bảo vệ quyền truy cập và cấu hình ổ ngoài mã hóa. |
| `scripts/backup.sh` | Tạo backup ban đầu hoặc thủ công: `sudo /opt/bluemoon/scripts/backup.sh --config /etc/bluemoon/backup.conf`. |
| `scripts/backup-dispatch.sh` | Dispatcher cho scheduler, xử lý lịch 02:00 và chạy bù; không cần gọi thủ công thường xuyên. |
| `scripts/scheduler/` | Cài systemd service/timer trên Linux để gọi dispatcher mỗi phút. |
| `scripts/restore-rehearsal.sh` | Diễn tập khôi phục vào schema thử nghiệm riêng, kiểm tra dữ liệu và xuất evidence; không dùng DB nguồn làm đích. |

Xem [hướng dẫn vận hành](docs/BACKUP_RESTORE.md) để cấu hình, cài scheduler và chạy restore an toàn. Giữ 7 bản backup thành công; Docker volume không thay thế backup. Bằng chứng chạy trên host Linux và MySQL thật còn thiếu theo [VALIDATION.md](docs/VALIDATION.md).
