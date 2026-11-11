# BlueMoon S13 — Vận Hành Backup Tự Động & Diễn Tập Phục Hồi (NFR-008)

Tài liệu này hướng dẫn chi tiết kiến trúc, chính sách, quy trình tự động hóa backup và diễn tập phục hồi (restore rehearsal) cho cơ sở dữ liệu BlueMoon theo hợp đồng S13 và quyết định đã duyệt (Q1–Q5).

---

## 1. Căn Cứ & Chính Sách Bắt Buộc (Q1–Q5)

| Mã | Quy Tắc Bắt Buộc | Thực Thi Trong S13 |
|---|---|---|
| **Q1** | **Thời gian & Catch-up**<br>Mỗi ngày 02:00, `Asia/Ho_Chi_Minh`. Nếu host/MySQL bận hoặc tắt, chạy bù ĐÚNG MỘT LẦN ở lần stack khởi động tiếp theo khi lần thành công cuối đã **>24 giờ**. | `scripts/backup-dispatch.sh` tính lịch theo timezone Việt Nam, nhận diện container startup qua `StartedAt`, kiểm tra `elapsed > 86400s`, gộp lượt trùng và ngăn retry mỗi phút. |
| **Q2** | **Nơi Lưu Trữ An Toàn**<br>Bắt buộc `BACKUP_DIR` tuyệt đối trên **ổ ngoài đã mã hóa** at-rest, ngoài repository và volume/datadir MySQL; không dùng cloud. | Kiểm tra fail-closed trong `scripts/backup.sh`: từ chối đường dẫn tương đối, từ chối nằm trong git repo/MySQL volume, kiểm tra mountpoint và cờ xác minh mã hóa `REQUIRE_ENCRYPTION_VERIFIED="true"`. |
| **Q3** | **Chính Sách Lưu Trữ (Retention)**<br>Giữ đúng **7 bản backup thành công mới nhất**, xếp theo timestamp hoàn tất. Không tính bản dở/hỏng. Khi lỗi giữ bản tốt cuối cùng. | Tự động xoay vòng (prune) sau khi bản mới được tạo, kiểm tra tính toàn vẹn và publish thành công. Bản lỗi không kích hoạt prune. |
| **Q4** | **Phạm Vi Schema & Phiên Đăng Nhập**<br>Backup toàn bộ schema nghiệp vụ, audit, Flyway, `mock_emails`; LOẠI BỎ `SPRING_SESSION` và `SPRING_SESSION_ATTRIBUTES`. | `mysqldump` dùng `--ignore-table` cho hai bảng session. Khi restore, dựng lại 2 bảng session rỗng từ DDL `V1__create_session_tables.sql`. Toàn bộ cookie cũ bị hủy, bắt buộc đăng nhập lại. Dọn dẹp S12 hết hạn trước khi phục vụ. |
| **Q5** | **Bảo Mật & Chuẩn Evidence**<br>Chỉ người vận hành được chỉ định đọc/restore. Evidence xuất ra CHỈ gồm **timestamp, checksum SHA-256, số bảng/số dòng**, tuyệt đối KHÔNG có row data, PII, email, mật khẩu hay số tiền. | Manifest JSON và output CLI tuân thủ nghiêm ngặt allowlist Q5. Không lưu raw log SQL hay dữ liệu cư dân vào evidence. |

---

## 2. Danh Sách Công Cụ & Tệp Tin

| Đường Dẫn | Vai Trò |
|---|---|
| `scripts/backup.sh` | CLI tạo dump từ MySQL 8.4 qua Docker Compose exec, staging trên ổ ngoài, kiểm tra tính toàn vẹn, ghi manifest Q5, publish nguyên tử và xoay vòng 7 bản. |
| `scripts/backup-dispatch.sh` | Dispatcher gọi mỗi 60 giây bởi host scheduler, kiểm tra lịch 02:00 VN và điều kiện catch-up >24h. |
| `scripts/restore-rehearsal.sh` | CLI diễn tập phục hồi vào schema thử nghiệm riêng (`bluemoon_restore_s13_*`), đối chiếu snapshot, kiểm tra khóa ngoại/toàn vẹn tài chính, chạy cleanup S12 và xuất evidence. |
| `scripts/backup.conf.example` | Mẫu cấu hình môi trường mode 0600 (không chứa thông tin nhạy cảm thật). |
| `scripts/scheduler/bluemoon-backup.service` | Systemd service unit (Linux host). |
| `scripts/scheduler/bluemoon-backup.timer` | Systemd timer unit chạy mỗi 60 giây (Linux host). |
| `backend/.../RestoreRetentionCleanupRunner.java` | Spring Boot headless adapter (`profile=restore-cleanup`) gọi `AgencyDataPackageService.executeRetentionCleanup()`. |
| `backend/.../application-restore-cleanup.yml` | Cấu hình headless: tắt web (`web-application-type: none`), tắt Flyway, tắt scheduler nền. |
| `tests/backup/` | Bộ kiểm thử tự động gồm 38 ca kiểm tra guard rails, retention 7 bản, catch-up và restore rehearsal. |

---

## 3. Hướng Dẫn Cấu Hình Triển Khai (Operator Runbook)

### Bước 1: Chuẩn bị tệp cấu hình bảo mật ngoài repository
Tạo thư mục cấu hình trên máy chủ và sao chép mẫu:
```bash
sudo mkdir -p /etc/bluemoon
sudo cp scripts/backup.conf.example /etc/bluemoon/backup.conf
sudo chown root:root /etc/bluemoon/backup.conf
sudo chmod 0600 /etc/bluemoon/backup.conf
```

Chỉnh sửa `/etc/bluemoon/backup.conf`:
- `COMPOSE_FILE`: Đường dẫn tuyệt đối tới `compose.yaml` (ví dụ `/opt/bluemoon/compose.yaml`).
- `BACKUP_DIR`: Đường dẫn tuyệt đối trên ổ ngoài mã hóa (ví dụ `/mnt/encrypted_backup/bluemoon_backups`).
- `BACKUP_MOUNT_POINT`: Mountpoint của ổ ngoài (ví dụ `/mnt/encrypted_backup`).
- `REQUIRE_ENCRYPTION_VERIFIED`: Đặt thành `"true"` sau khi xác minh LUKS/FileVault/APFS encrypted.
- `DB_USER` và `DB_PASSWORD`: Tài khoản MySQL có quyền đọc schema `bluemoon`.

### Bước 2: Chạy backup khởi tạo (Initial Backup)
Trước khi bật scheduler, bắt buộc phải có một bản backup thành công ban đầu:
```bash
sudo /opt/bluemoon/scripts/backup.sh --config /etc/bluemoon/backup.conf
```
Kiểm tra kết quả: Output chứa `BACKUP_SUCCESS` và thư mục bundle có tệp `COMPLETED`.

### Bước 3: Cài đặt và kích hoạt Systemd Timer (Host Linux)
Sao chép các unit file vào thư mục systemd của hệ thống:
```bash
sudo cp scripts/scheduler/bluemoon-backup.service /etc/systemd/system/
sudo cp scripts/scheduler/bluemoon-backup.timer /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now bluemoon-backup.timer
```
Kiểm tra trạng thái timer:
```bash
systemctl list-timers | grep bluemoon
```

---

## 4. Hướng Dẫn Diễn Tập Phục Hồi (Restore Rehearsal)

### Bước 1: Tạo schema thử nghiệm và tài khoản cô lập
Người vận hành tạo schema thử nghiệm mới trên MySQL 8.4 với tiền tố bắt buộc `bluemoon_restore_s13_<runid>`:
```sql
CREATE DATABASE bluemoon_restore_s13_20261111_01 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'bluemoon_restore_user'@'%' IDENTIFIED BY 'MatKhauKhoDoan123!';
-- Chỉ cấp quyền trên schema thử nghiệm, không cấp quyền trên schema bluemoon dev gốc:
GRANT ALL ON `bluemoon_restore_s13_20261111_01`.* TO 'bluemoon_restore_user'@'%';
FLUSH PRIVILEGES;
```

### Bước 2: Thực hiện diễn tập phục hồi
Chạy script diễn tập trỏ tới bundle backup và schema thử nghiệm:
```bash
./scripts/restore-rehearsal.sh \
  --bundle /mnt/encrypted_backup/bluemoon_backups/bluemoon_backup_<timestamp>_<id> \
  --target-db bluemoon_restore_s13_20261111_01 \
  --target-user bluemoon_restore_user \
  --target-password 'MatKhauKhoDoan123!' \
  --evidence-out /tmp/restore_evidence.json
```

### Bước 3: Xác minh tiêu chuẩn nghiệm thu
1. **Kiểm tra rào chắn quyền**: Script tự động kiểm tra xem `target-user` có đọc được database `bluemoon` nguồn hay không; nếu đọc được script sẽ lập tức dừng để bảo vệ an toàn dữ liệu.
2. **Kiểm tra phiên đăng nhập**: Hai bảng `SPRING_SESSION` và `SPRING_SESSION_ATTRIBUTES` được tái tạo với số dòng bằng 0.
3. **Đối chiếu snapshot**: Toàn bộ bảng, số dòng và khóa ngoại khớp với dump.
4. **Dọn dẹp S12**: Adapter `RestoreRetentionCleanupRunner` tự động chạy trong profile `restore-cleanup` để dọn dẹp các gói dữ liệu cơ quan đã quá hạn.
5. **Kiểm tra Evidence**: Tệp `/tmp/restore_evidence.json` đạt chuẩn Q5 allowlist (chỉ có số đếm và checksum, không có thông tin cư dân hay số tiền).

---

## 5. Dọn Dẹp Sau Diễn Tập
Sau khi kiểm tra thành công, người vận hành thu hồi quyền và xóa schema thử nghiệm:
```sql
DROP DATABASE bluemoon_restore_s13_20261111_01;
DROP USER 'bluemoon_restore_user'@'%';
```
Tuyệt đối không xóa hoặc chỉnh sửa database gốc `bluemoon`.
