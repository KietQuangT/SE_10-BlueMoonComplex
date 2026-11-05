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
CI của PR #4 (job `verify` gồm bước "Build và kiểm thử với MySQL") xanh; Sơn xác nhận PR đã merge và CI pass (05/11/2026), headSha 763aada theo Copilot, chưa đối chiếu riêng. F02 đã tick trong `docs/sprint-1/TASKS.md`.
Chưa xác nhận: thành viên thứ hai tự cài lại; kết quả trên Windows/ARM khác. `RoleAuditIT` đổi tên role ADMIN rồi khôi phục: không chạy trên DB có người khác dùng. `RoleService` chưa có kiểm tra quyền, không gắn controller trước S01.
Hệ quả của V2–V4 đã nằm trong DB `bluemoon` đã chạy: nếu nhóm đổi V2–V4 trước khi merge, Flyway sẽ báo sai checksum trên DB đó; xử lý bằng cách Sơn tự tạo lại DB cục bộ, không sửa tay `flyway_schema_history`.

## S01: identity, session, brute-force (06/11/2026)

| Kiểm tra | Kết quả |
|---|---|
| `cd backend && ./mvnw -B -ntp -Pintegration verify` (Sơn chạy, JDK 21.0.9, MySQL 8.4.13 ở 127.0.0.1:3306, JVM múi giờ +07, 00:06 +07, HEAD 1746a8a, worktree sạch) | BUILD SUCCESS: surefire 45 test (LoginLockPolicyTest 6, UnknownUserTimingTest 1, UserRepositoryQueryTest 1, AuthApiTest 9, AuditServiceTest 23, SystemInfoApiTest 5); failsafe 31 test (RoleAuditIT 4, IdentityCookieIT 1, IdentitySessionIT 23, BootstrapIT 3); 0 failure/error/skipped; 24,120 s |
| Cùng lệnh với `TZ=UTC` (Sơn chạy) | BUILD SUCCESS: 45 + 31 test, 0 lỗi; 26,645 s |
| Lần chạy đầu trên HEAD 6380a70 (Sơn chạy) | failsafe 30 test: IdentitySessionIT 7 Failure + 9 Error (9 NPE do dùng `MockHttpSession` với Spring Session JDBC; 6 ca bậc khóa do `locked_until` map `LocalDateTime` lệch múi giờ JVM +07; 1 assertion CHECK sai kiểu exception). Sửa ở 1746a8a (map `Instant`, cookie `SESSION` thật) |
| Flyway | "Successfully validated 5 migrations", áp V5 lên DB `bluemoon`, `ddl-auto=validate` qua |
| SQL khóa hàng đăng nhập (log Hibernate trong IT) | `... from users u1_0 join roles r1_0 on r1_0.id=u1_0.role_id where u1_0.username=? for update of u1_0`: chỉ khóa `users`; test `loginLookupLocksOnlyUsers` bắt buộc điều này |
| Verifier tự chạy `mvn-sandbox.sh -B -ntp verify` trong worktree Orca | KHÔNG chạy được: sandbox không ghi được `target/` ngoài thư mục làm việc (`Operation not permitted`); không phải lỗi code. 45 test không cần DB dựa trên log của Sơn |

Điều đã chứng minh bằng thực thi trên MySQL thật: đăng nhập 3 vai trò, 5 nhánh lỗi cùng status và body 401, đổi session ID và cookie cũ bị từ chối qua HTTP thật, logout hủy đúng phiên, bộ đếm commit khi trả 401, bậc khóa 1/5/10/15/30/60 phút, 10 lần sai song song không mất cập nhật, `ACCOUNT_LOCKED` rollback cùng transaction, ADMIN không bị khóa, CHECK V5 từ chối giá trị âm, kết quả giống nhau ở JVM +07 và UTC.
Chưa xác nhận: Windows/ARM, máy thứ hai, CI trên remote (chưa push); `EXPLAIN` cho `uq_users_username`; test 403 theo vai trò qua security chain thật; bậc >= 7 qua service; test timing nhánh đang khóa/INACTIVE; round trip MySQL cho trần `9999-12-31 23:59:59.999999`. Hệ quả của V5 trong DB `bluemoon` cục bộ: nếu V5 đổi trước khi merge, Flyway báo sai checksum; Sơn tự tạo lại DB cục bộ, không sửa tay `flyway_schema_history`.
Lệnh chạy IT cần biến `DB_USER`, `DB_PASSWORD`, `DB_NAME` (`.env` dùng tên `MYSQL_*`, Maven không tự đọc); `LOGGING_LEVEL_ORG_HIBERNATE_SQL` không đủ để bật SQL vì tên logger phân biệt hoa thường.

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
