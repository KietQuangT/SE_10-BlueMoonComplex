# Kết quả kiểm chứng

Ngày: 04/11/2026, múi giờ Asia/Ho_Chi_Minh.

## Đã chạy

| Kiểm tra | Kết quả |
|---|---|
| JDK 21.0.13, Maven 3.9.11, Spring Boot 4.0.9 | Build và đóng gói JAR thành công |
| Maven Wrapper `backend/mvnw ... verify` | Thành công; wrapper tải đúng Maven 3.9.11 |
| SystemInfoApiTest | 5 test, 0 failure, 0 error, 0 skipped |
| Module metadata công khai | HTTP 200, response skeleton và header request ID |
| Endpoint chưa cấp quyền | Người chưa login 401; user kiểm thử đã xác thực 403 |
| CSRF | POST thiếu token bị từ chối; xác nhận token attribute từ CSRF filter |
| CORS | Preflight từ origin ngoài cấu hình bị từ chối |
| Compile BootstrapIT | Thành công; chưa chạy các test cần MySQL |
| XML pom / YAML config, Compose và workflow / shell syntax | Phân tích cú pháp thành công |
| CODEOWNERS gốc | Được giữ nguyên |

Lệnh chuẩn cho thành viên:
```bash
cd backend
./mvnw -B -ntp verify
```
Môi trường soạn dùng proxy/truststore riêng bên ngoài repo để tải dependency. Cấu hình đó không nằm trong gói hoặc source; thành viên dùng mạng của mình không cần chép nó.

## F02: AuditService và RoleService mẫu (05/11/2026)

| Kiểm tra | Kết quả |
|---|---|
| `cd backend && ../.claude/bin/mvn-sandbox.sh -B -ntp clean verify` (Java 21.0.9) | BUILD SUCCESS, 26 test: AuditServiceTest 21, SystemInfoApiTest 5; 0 failure/error/skipped |
| Compile `RoleAuditIT`, `BootstrapIT` (javac) | Thành công; chưa chạy các test cần MySQL |
| `cd backend && ./mvnw -B -ntp -Pintegration verify` (Sơn chạy, `JAVA_HOME` = openjdk@21 Java 21.0.9, MySQL 8.4.13 ở 127.0.0.1:3306, 15:31 +07) | BUILD SUCCESS: surefire 26 test; failsafe 7 test (RoleAuditIT 4, BootstrapIT 3), 0 failure/error/skipped. Flyway áp V2, V3, V4 lên DB còn ở V1 |

Nếu `verify` báo "Unresolved compilation problems" thì `target/` bị trình biên dịch Eclipse của IDE ghi đè; chạy lại với `clean`.
Đã chứng minh bằng thực thi trên MySQL thật: full context khởi động với `AuditLog`/`AuditLogRepository` package-private và bean `ObjectMapper` Jackson 3; `ddl-auto=validate` khớp V2 (`Instant`↔`DATETIME(6)`, `TEXT`); rollback audit cùng transaction; FK `fk_audit_actor`; `record` ngoài transaction bị từ chối; round trip microsecond UTC; Flyway V1–V4 và các bảng; JDBC session; health UP.
Chưa xác nhận: teammate hoặc CI cài lại và chạy integration; kết quả trên Windows/ARM khác. `RoleAuditIT` đổi tên role ADMIN rồi khôi phục: không chạy trên DB có người khác dùng. `RoleService` chưa có kiểm tra quyền, không gắn controller trước S01.
Hệ quả của V2–V4 đã nằm trong DB `bluemoon` đã chạy: nếu nhóm đổi V2–V4 trước khi merge, Flyway sẽ báo sai checksum trên DB đó; xử lý bằng cách Sơn tự tạo lại DB cục bộ, không sửa tay `flyway_schema_history`.

## Chưa xác nhận bằng thực thi

- Máy soạn không có Docker/MySQL nên chưa chạy `docker compose up`, image build, Flyway trên MySQL, health runtime hoặc lưu/xóa JDBC session thực tế.
- `BootstrapIT` và profile `integration` đã có; CI cấu hình MySQL 8.4 để kiểm tra những phần trên. CI chưa được kích hoạt trên remote vì chưa push.
- Chưa chạy Maven Wrapper Windows, ARM/macOS hoặc cài lại trên máy thành viên thứ hai.
- Không có kết quả hiệu năng, auth nghiệp vụ, tính phí hoặc báo cáo vì skeleton không triển khai nghiệp vụ.
- Chưa thay đổi branch protection/ruleset hoặc main remote. Nhánh local chuẩn bị là `codex/backend-foundation`.

## Cổng trước khi merge vào main

1. Reviewer đọc SOURCE_REVIEW/DECISIONS và xác nhận phạm vi foundation.
2. CI chạy `./mvnw -B -ntp -Pintegration verify` với MySQL 8.4 thành công.
3. Một thành viên khác chạy quickstart Docker, health và metadata thành công.
4. Không có credential/PII thật, migration không phá dữ liệu; PR chỉ có bộ khung và tài liệu.

Chưa tick foundation DoD khi các điều kiện này chưa có bằng chứng. Build unit thành công không chứng minh toàn bộ ứng dụng đã chạy trên MySQL hay Sprint 1 hoàn thành.
