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

## S02: cấp account, email mô phỏng, bootstrap (06/11/2026)

| Kiểm tra | Kết quả |
|---|---|
| `cd backend && ./mvnw -B -ntp -Pintegration verify` (Sơn chạy, JDK 21.0.9, MySQL 8.4.13 ở localhost:3306, macOS aarch64, JVM +07, 11:43–11:44 +07, nhánh feat/s02-account-provisioning, HEAD 6e138e4, worktree sạch) | BUILD SUCCESS: surefire 56 test (AccountApiTest 5, MustChangePasswordGuardTest 3, BootstrapSecurityContextTest 1, EmailNormalizerTest 1, TemporaryPasswordGeneratorTest 1, AuthApiTest 9, AuditServiceTest 23, LoginLockPolicyTest 6, UnknownUserTimingTest 1, UserRepositoryQueryTest 1, SystemInfoApiTest 5); failsafe 51 test (AccountProvisioningIT 11, BootstrapAdminIT 7, AccountAtomicityIT 2, IdentitySessionIT 23, RoleAuditIT 4, IdentityCookieIT 1, BootstrapIT 3); 0 failure/error/skipped; 35,763 s. Log không in SHA: khớp commit là suy luận từ thời điểm |
| Lần chạy trước trên 20f883f (Sơn chạy) | failsafe 49/51: AccountAtomicityIT 2 Error (`IllegalTransactionStateException`: stub `doThrow().when(spy).record(...)` đi qua proxy `@Transactional(MANDATORY)`). Sửa ở 6e138e4 bằng `AopTestUtils.getUltimateTargetObject`, không đổi code sản phẩm |
| Flyway | "Successfully validated 6 migrations"; áp V6 từ V5 ở lần chạy đầu; `ddl-auto=validate` qua với cột BOOLEAN |
| Bootstrap không console | `BootstrapAdminIT.bootstrapWithoutConsoleExitsWithoutCreatingUsers` chạy `java -jar` thật, thoát mã khác 0, in "Cần terminal tương tác", số user không đổi |
| Bootstrap trong terminal thật (Sơn chạy tay, 12:23–12:28 +07, DB `bluemoon` cục bộ) | Lần 0: mật khẩu bị từ chối, không tạo gì. Lần 1: "Đã tạo ADMIN.". Lần 2: "Đã có ADMIN, bỏ qua.". Mật khẩu không hiện trên terminal. SQL: `audit_log` có 1 dòng `BOOTSTRAP_ADMIN` actor NULL changes `{"role":"ADMIN"}`; `users` của tài khoản đó `must_change_password=0`, `status=ACTIVE` |
| Verifier tự chạy `mvn-sandbox.sh -B -ntp verify` | KHÔNG chạy được (sandbox không ghi được `target/` trong worktree Orca). Kết quả dựa trên log của Sơn và report trong worktree |

Đã chứng minh trên MySQL thật: ADMIN cấp ba vai trò; mock email đúng người (hash khớp, mật khẩu A không vào B); email trùng bị từ chối (3 biến thể) và race 8 luồng chỉ một thắng; mật khẩu tạm không lộ trong response, output log, audit, `users.password_hash`, `SPRING_SESSION`; atomicity (audit lỗi hoặc mail lỗi thì rollback toàn bộ); cờ đổi mật khẩu chặn fail-closed và session đang mở vẫn bị chặn sau khi xóa cờ trong DB; migration V6 (DEFAULT, CHECK, FK); `BootstrapAdminService` (một ADMIN, INACTIVE vẫn chặn, mật khẩu không hợp lệ); bootstrap tay trong terminal thật.
Chưa xác nhận: Windows, máy thứ hai, CI trên remote (chưa push); đối chứng tên logger khi bỏ cấu hình; `org.hibernate.orm.jdbc.bind=TRACE` (sẽ in email, `body`, `password_hash`: không bật); hành vi session cũ; nhánh DB của race test; đăng nhập bằng tài khoản bootstrap qua API và lịch sử shell; EP-01 của Kiệt; K2, K4, K5, K6, K9 (xem khối S02 trong `docs/sprint-1/TASKS.md`). Lệnh bootstrap báo "Dữ liệu không hợp lệ." chung chung kèm stack trace khi mật khẩu bị từ chối (chưa sửa). V6 và một tài khoản ADMIN thử đã nằm trong DB `bluemoon` cục bộ: sửa V6 trước merge thì Sơn tạo lại DB cục bộ.
Mật khẩu tạm nằm dạng rõ trong `mock_emails.body`, không hết hạn (hệ quả D14). Xem bằng SQL: `SELECT id, recipient_email, subject, status, created_at, body FROM mock_emails ORDER BY id DESC`.

## S05 hộ và nhân khẩu (lát A + B, 06/11/2026)

| Lệnh | Kết quả |
|---|---|
| `cd backend && ./mvnw -B -ntp -Pintegration verify` (Sơn chạy 16:27–16:30 +07, JDK 21.0.9, MySQL 8.4 ở 127.0.0.1:3306, macOS, worktree Orca `feat-s05b` sạch ở HEAD 2519f94; commit 16:20:07) | surefire 75 test, 0 lỗi; failsafe 63 test, 0 lỗi, 0 failure (BootstrapIT 3, AccountAtomicityIT 2, AccountProvisioningIT 11, BootstrapAdminIT 7, HouseholdIntegrationIT 5, IdentityCookieIT 1, IdentitySessionIT 23, ResidentIntegrationIT 7, RoleAuditIT 4). Dòng `BUILD SUCCESS` không có trong log đã dán (bị cắt bằng `head -60`): kết luận dựa trên failsafe-summary và surefire report |
| Flyway | "Current version of schema bluemoon: 6; up to date, no migration necessary" (S05 không thêm migration) |
| Lần chạy đầu của Sơn | Access denied (mật khẩu DB trong biến môi trường sai), IT không chạy; không phải lỗi code |
| Verifier tự chạy Maven | KHÔNG chạy được (sandbox không ghi được `target/` ở worktree Orca). Kiểm bằng git/grep và đọc report |

Các dòng `ERROR ... IllegalStateException` trong log là test cố tình làm audit lỗi để kiểm rollback.
Đã chứng minh trên MySQL thật: CRUD/tra cứu hộ và nhân khẩu có lọc và phân trang ở DB; 409 trùng mã hộ/căn hộ (kể cả khác hoa/thường, đua 8 luồng chỉ một thắng, không lộ giá trị); trùng số giấy tờ toàn hệ thống trong nhóm đang cư trú (đua hai hộ, số của người đã chuyển đi dùng lại được, số của người tạm vắng vẫn tính trùng); chuyển đi đổi trạng thái, ghi 1 dòng `residency_changes` (MOVE_OUT/APPROVED), lần hai 409, đua hai request chỉ một thắng; cư dân chỉ thấy hộ của mình, `householdId` NULL thì 403; audit cùng transaction ở cả 5 action (rollback hai chiều), không PII; đua PATCH số giấy tờ và chuyển đi cùng người (không 500 trong một lần chạy).
Chưa xác nhận: Windows, máy thứ hai, CI (chưa push); Tuấn đã review và approve theo lời Sơn (chưa có văn bản); deadlock thứ tự khóa chỉ suy luận, chưa tái hiện; test đua nhân khẩu yếu (`isIn(200,409)`); trim mã hộ và số giấy tờ chưa quyết; Quân chưa xác nhận quyền đọc hộ của ACCOUNTANT (A3). IT phải chạy trên DB không có dữ liệu thật hoặc app khác ghi đồng thời (fixture đua ghi vào hộ id nhỏ nhất). Danh sách đầy đủ ở khối S05 trong `docs/sprint-1/TASKS.md`.

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

### S04 — 06/11/2026

- `cd backend && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -B -ntp verify`: 83 test, 0 lỗi, BUILD SUCCESS (JDK 21.0.9). Script `../.claude/bin/mvn-sandbox.sh` không có trong worktree.
- Không chạy `-Pintegration` theo brief: các test MySQL mới chưa được kiểm chứng. UI thuộc Kiệt, chưa kiểm thử ở đây.

### S04 — vòng sửa 1, 06/11/2026

- Sau rebase `origin/main`: `93a19ca3cdb23f8f2e5aa97618bf666c01d07979`; không có xung đột.
- `cd backend && JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -B -ntp verify`: **85 test, 0 lỗi, BUILD SUCCESS**; script `../.claude/bin/mvn-sandbox.sh` không có.
- Lần verify trước: 85 test, 1 failure (`BootstrapSecurityContextTest.securityBeansCanStartWithoutWebServer`: thiếu bean `CurrentAccountService` sau khi inject bắt buộc); đã sửa test context và chạy lại thành công như trên. Lệnh Maven đầu với JDK mặc định dừng ở enforcer: `Dự án dùng JDK 21`.
- Chưa chạy `-Pintegration` theo brief: IT mở khóa, hạ quyền thật, đổi hộ và đăng nhập lại sau invalidate chưa được kiểm chứng trên MySQL. Không migration.

### S04 — vòng sửa 2, 06/11/2026
- Chạy từ `backend/`, JDK 21; script `../.claude/bin/mvn-sandbox.sh` không có. Dùng bản sao Maven cache trong worktree và thư mục tạm trong `backend/target`: `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home MAVEN_USER_HOME="$PWD/target/s04-maven-home" TMPDIR="$PWD/target/s04-tmp" ./mvnw -B -ntp -Dmaven.repo.local="$PWD/target/s04-maven-home/repository" verify`.
- Kết quả: **BUILD SUCCESS**, 86 test, 0 failure, 0 error, 0 skipped. `AccountApiTest.inactiveActorDuringManagementReturnsHttp401` kiểm HTTP 401 thật qua MockMvc khi service ném `BadCredentialsException`; `AccountManagementServiceTest.inactiveActorCannotManageAccounts` kiểm không ghi audit cho ba thao tác.
- Chưa chạy profile integration theo brief (cần MySQL của Sơn); các kiểm tra IT về CSRF với cookie cũ khi account còn INACTIVE và cột `changes` của audit chưa được kiểm chứng trên DB.

### S03 — 07/11/2026

- Bằng chứng do Sơn chạy: `cd backend && ./mvnw -B -ntp -Pintegration verify` (JDK 21.0.9, MySQL Docker mysql:8.4 cổng 3307) ở worktree feat-s03, c7d0b89. BUILD SUCCESS, 50,978 s, kết thúc 07/11/2026 11:28:59 +07. Log Sơn dán: "Tests run: 80, Failures: 0, Errors: 0, Skipped: 0" (failsafe).
- Đối chiếu báo cáo (verifier): surefire 103 test (21 file) và failsafe 80 test (11 file), 0 failure/error/skipped; failsafe-summary completed=80; mtime báo cáo 11:28:10–11:28:59, sau commit c7d0b89 (11:16:45) và sau lần biên dịch cuối (11:16:26). PasswordChangeIT 5/5, PasswordChangeServiceTest 7/7, PasswordChangeApiTest 5/5, LoginAttemptServiceTest 5/5, MustChangePasswordGuardTest 3/3.
- Ba lần chạy trước (11:18, 11:20, 11:22) lỗi do cấu hình môi trường (JDK 25 bị enforcer từ chối; cổng 3306 là một mysqld khác trên máy, MySQL của dự án ở cổng 3307), không do code. Dòng lệnh đầy đủ kèm biến DB_* không có trong hồ sơ ngoài đoạn log dán. Verifier tự chạy Maven: KHÔNG (sandbox không ghi được `target/` ở worktree Orca); kiểm bằng git và đọc báo cáo.
- Đã chứng minh trên MySQL thật (cookie thật): đổi mật khẩu trả 204; cả 3 session của account, kể cả session đang gọi, nhận 401 sau đó và `SPRING_SESSION` không còn dòng của principal; session account khác không bị ảnh hưởng; mật khẩu cũ đăng nhập thất bại, mật khẩu mới thành công với `mustChangePassword=false`; che `mock_emails.body` khi cờ TRUE (giữ recipient_email, subject, status; dòng account khác nguyên); audit PASSWORD_CHANGED và SESSIONS_REVOKED cùng transaction, rollback toàn bộ khi audit lỗi (hash, cờ, mock_emails, session nguyên); hai request đổi đồng thời chỉ một thắng; sai mật khẩu cũ đếm và khóa non-ADMIN, ADMIN không khóa, đang khóa trả 401; luồng đầy đủ qua S02 (mật khẩu tạm, 403 PASSWORD_CHANGE_REQUIRED, đổi, đăng nhập lại); log không chứa mật khẩu. Các dòng `ERROR ... IllegalStateException` trong log là test cố tình làm audit lỗi.
- Chưa xác nhận: race đăng nhập bằng mật khẩu cũ ngay trước lúc đổi (cần cột phiên bản mật khẩu, chưa làm); vòng sửa 2 không có reviewer độc lập kiểm lại; Copilot QA chạy một lần (trên c852c51), lời chưa kiểm chứng; UI thuộc Kiệt, chưa có; chưa có văn bản của Kiệt/Tuấn/Quân (xác nhận Kiệt là lời Sơn); 401 khi tự khóa không hủy session; cách nối users↔residents cho yêu cầu khớp full_name chưa chốt và không thuộc S03; IT không phủ account INACTIVE, đặt lại bộ đếm khi thành công, ca cờ FALSE, PASSWORD_WEAK/TOO_SHORT qua HTTP thật; test đồng thời chỉ một lần chạy; CI trên remote, Windows, máy thứ hai chưa chạy (chưa push).

### S06 backend: khai báo, duyệt, tra cứu tạm vắng, 07/11/2026
- Worker Codex: `cd backend && ./mvnw -B -ntp verify` (JDK 21.0.9) ở worktree feat-s06, HEAD `7b9828e`: 93 test, 0 lỗi, BUILD SUCCESS. Copilot QA cùng SHA: 93 test (lời Copilot, chưa kiểm chứng).
- Bằng chứng do Sơn chạy: `./mvnw -B -ntp -Pintegration verify` (JDK 21, MySQL cổng 3307), 07/11/2026 16:24 +07: failsafe 89 test, 0 lỗi, BUILD SUCCESS, 52 s. Báo cáo failsafe: `DeclarationIntegrationIT` 14 test, `ResidentIntegrationIT` 7 test. V7 đã áp trên DB của Sơn; chưa rõ DB có dữ liệu S05 hay sạch.
- Claude không chạy Maven (sandbox); không gọi subagent verifier, tự đối chiếu SHA, số test, cờ bỏ qua. Review Opus hai lần: đạt.
- Chưa xác nhận: UI (AC "ghép UI"), CI remote, dữ liệu S05 thật, quy tắc phí/báo cáo cho `TEMPORARILY_ABSENT`, văn bản xác nhận của Tuấn/Kiệt/Quân. Chi tiết: `docs/sprint-1/TASKS.md` mục "S06 backend" và `_agents_workspace/S06/04_evidence.md`.

### S06 UI: khai báo, duyệt, quay lại, gợi ý tên tạm vắng, 07/11/2026
- Tick S06 ngày 08/11/2026 theo lời Sơn, khi chưa kiểm Chrome với backend S06 thật. Vòng sửa 3 (`fa786147`, lời worker): vitest 13 file / 65 test đạt; chưa chạy lại trong Claude.
- Worker Codex: `cd frontend && npm ci && npx tsc -b && npm test && npm run build` (Node 26.10.0) ở worktree feat-s06-ui, HEAD `70c06c7`: vitest 12 file / 51 test đạt, `tsc` không lỗi, build đạt. Copilot QA cùng SHA: cùng kết quả (lời Copilot, chưa kiểm chứng).
- Review Opus 2 lần: lần 1 có 1 lỗi chặn (dữ liệu mẫu lọt vào form M-31 chế độ api), đã sửa ở `70c06c7`; lần 2 đạt.
- Chưa xác nhận: chạy trên Chrome với backend S06 thật, 360px, screen reader; test Esc và tải lại sau quyết định thất bại; 3 vulnerabilities do `npm ci` báo; PR frontend trỏ vào nhánh S03 chưa merge. Chi tiết: `docs/sprint-1/TASKS.md` mục "S06 UI", `_agents_workspace/S06/ui/04_evidence.md`.

### Bản tích hợp S03 + S06, 07/11/2026
- Bằng chứng do Sơn chạy ngày 07/11/2026 20:38 +07 ở thư mục gốc, nhánh `main` cục bộ tại `cef9b93` (`origin/main` d3d8de3 cộng S03, backend S06 và giao diện S06, chưa push): `cd backend && ./mvnw -B -ntp -Pintegration verify` (JDK 21, MySQL 8.4 cổng 3307): failsafe **94 test, 0 lỗi**, BUILD SUCCESS, 57,6 s (80 IT của S03 + 14 `DeclarationIntegrationIT`). Log Sơn dán chỉ gồm pha cuối; số test pha thông thường của bản tích hợp chưa có.
- Chưa xác nhận: `npm test` và `npm run build` trên bản tích hợp (giao diện không đổi so với `fa78614`), CI trên remote, máy thứ hai.

### S07: đơn giá phí và công thức, 08/11/2026
- Unit trên `f5eef71` (JDK 21, `./mvnw -B -ntp verify`): 122 test, 0 lỗi (`feat-s07/.agent_out/verify.log`).
- Integration trên `f5eef71` với MySQL 8.4 (compose project `bluemoon`, V8 đã áp): `./mvnw -B -ntp -Pintegration verify` 97 test, gồm 3 test `FeeRateIntegrationIT`, BUILD SUCCESS (Sơn chạy 08/11/2026 12:42 +07).
- Chưa kiểm: test QA của agy trên `qa/s07-agy` chưa chạy trên cùng SHA; D25 chưa commit; PR và CI remote chưa có. Chi tiết: `docs/sprint-1/TASKS.md` mục S07.

### S08: sửa kiểm thử tích hợp PR #20, 09/11/2026
- Trên nền `ac4cd20aaca4e6f26921841658e5284df08d68a9`, ba lớp billing tái hiện 1 failure, 7 errors với MySQL 8.4 riêng; lỗi stub spy qua proxy MANDATORY làm Mockito dở dang, và truy vấn DATETIME sai kiểu `Instant`.
- Sau sửa test và ngày fixture đóng góp, `cd backend && ./mvnw -B -ntp -Pintegration verify` với JDK 21.0.9 và container MySQL 8.4 riêng: 213 unit, 120 integration, 0 lỗi, BUILD SUCCESS (1 phút 39 giây). Không dùng DB hoặc volume chia sẻ; CI remote cần kiểm sau push.

### S08 UI review fixes (08/11/2026)

- `cd frontend && npm test`: 19 files, 102 tests passed; React `act(...)` warnings in existing fee-rates and auth-session tests.
- `cd frontend && npx tsc -b`: passed.
- `cd frontend && npm run build`: passed.
- Focused regression tests cover Demo route isolation, account-scoped contribution retry, definitive 409 rejection, revision selection across all period statuses, and stale-version reselection.
- Browser checks with the real backend were not run. Unresolved contributions are retained in memory across route navigation and account changes, but not a full page reload or tab closure. No backend files or schema changed.

## S08 — audit và chỉnh UI/UX theo ui-audit, 09/11/2026

Kiểm chứng bản cập nhật `hhoangsonnw/feat-s08-ui-ux`, nền `f081bee87c98d80144e1d106b2cde45e54504594`. Thiết kế tham chiếu `ui-audit` SHA `68e4199e4ecf91163e2903cee42aceb9c1fdfc71`; hành vi vẫn theo hợp đồng S08 và API thật.

| Lệnh / phạm vi | Kết quả |
|---|---|
| `cd frontend && npm test` | 20 file, 109/109 test qua, 3,17 giây; gồm 7 hồi quy UX mới |
| `cd frontend && npm run build` | TypeScript và Vite qua; JS 507,77 kB (gzip 141,66 kB), cảnh báo chunk >500 kB |
| Impeccable source detector: frontend/src/screens/s08 và frontend/src/api/errors.ts | Không finding tĩnh (`[]`) |
| `git diff --check` | Qua |

Báo cáo phạm vi/phát hiện/cách sửa: `frontend/S08_UI_UX_AUDIT.md`. Log thật nằm ở `.agent_out/s08-ux-tests.log`, `.agent_out/s08-ux-build.log`, `.agent_out/s08-ui-detect.json` (không commit các log). Test còn cảnh báo React `act` ở FeeRatesPage/AppStateProvider; không bỏ qua test nào.

Đã xem thiết kế tham chiếu trong Arc. Chưa kiểm bằng mắt bản sau sửa ở 360px/desktop/sáng/tối, focus/Tab/Esc/zoom 200% và toàn bộ luồng trên API thật: CUA không đọc được tab :5174 người dùng mở. Không suy từ test jsdom hoặc detector mã nguồn thành chứng nhận WCAG hay bằng chứng layout. Backend/schema không đổi; không chạy Maven/MySQL IT trong lượt frontend này.


## Frontend — chốt đợt Taste UI/UX toàn ứng dụng, 09/11/2026

Nền `c3b3f7fd539a3460f627bd812f9e050b1e2ffdce`. Chỉnh bố cục dùng chung, header/hành động, native form dialog, điều hướng bàn phím/phân trang, phục hồi lỗi danh sách tạm vắng, nhãn/ngày báo cáo, cách ly Demo đơn giá, Demo kỳ thu theo D26 và khóa tải báo cáo chưa khả dụng. Login có dừng nền động; bỏ dòng “Quản lý cư trú và thu phí” theo yêu cầu cuối, giữ “Đăng nhập” là h1.

| Lệnh / phạm vi | Kết quả |
|---|---|
| `cd frontend && npm test` | **24 file, 119/119 test qua**, 5,81 giây; gồm 10 hồi quy mới |
| `cd frontend && npm run build` | TypeScript và Vite qua; JS 499,79 kB, gzip 140,88 kB |
| `git diff --check` | Qua |
| Taste consistency `--quick --stops`, 1440/1024/820/768/390 | **12 cấu hình / 60 lượt viewport**, không probe error; không coi checker PASS vì còn luật marketing không áp dụng cho app |
| Taste `perf.mjs --runs 3`, đơn giá 1440/390 | **0 FAIL, 4 WARN** (JS 494,4 KiB và 6 file font tại mỗi viewport), không request hỏng/tràn ngang |

Chi tiết/bằng chứng/miễn trừ: `frontend/TASTE_UI_UX_AUDIT.md`. Log và ảnh nằm trong `.agent_out/taste-global/` (ignored), test còn cảnh báo React `act` tại FeeRatesPage/AppStateProvider. Ma trận dự kiến 41 cấu hình được dừng theo yêu cầu tạm chốt của người dùng: **29 cấu hình còn lại chưa hoàn tất kiểm ảnh**. Có ảnh bổ sung cho đơn giá, đóng góp API và hai form; Login được kiểm lại sau khi bỏ tiêu đề. Dùng bản sao frontend với fixture chỉ đọc và một Chrome headless mỗi lượt, không gọi backend thật.

Không đổi backend/schema/hợp đồng API, không chạy Maven/MySQL, không tick thêm TASKS. Chưa kiểm thao tác ghi thật, screen reader, zoom 200%, iOS/thiết bị thật; `--quick` bỏ qua hover/motion/forms/reduced-motion/nội dung ẩn. `layout/check.mjs` không chạy được trọn vẹn với cấu trúc app (thiếu `.wrap`, nhánh chụp ảnh lỗi `sections.map`); không sửa toolkit hoặc coi đó là đạt. Chưa có API tải báo cáo thật; UI chỉ cho xem trước. Không đưa các giới hạn này thành chứng nhận WCAG hay kiểm chứng bảo mật server.

## S09 backend — thu tiền và công nợ, 09/11/2026

- Worktree `s09-contract`, thay đổi chưa commit; JDK 21.0.9. MySQL 8.4.13 là container thử cô lập cổng `127.0.0.1:3308`, không dùng DB hoặc volume dự án. Flyway đã áp V10 trên DB thử.
- `cd backend && ./mvnw -B -ntp verify` với `JAVA_HOME` trỏ JDK 21: **222 test, 0 lỗi, BUILD SUCCESS** (10,7 giây).
- `cd backend && DB_HOST=127.0.0.1 DB_PORT=3308 DB_NAME=bluemoon DB_USER=bluemoon DB_PASSWORD=<mật-khẩu-test-local> ./mvnw -B -ntp -Pintegration verify` với JDK 21: **222 unit/API + 143 integration, 0 lỗi, BUILD SUCCESS** (71 giây).
- Kiểm tra mới gồm phân chia tiền, FIFO qua nhiều receipt, replay và xung đột theo actor/payload, đảo/hủy, ngày và lý do nhập muộn, chéo hộ, 401/403/CSRF, rollback receipt khi audit lỗi, receipt tranh hủy kỳ, hủy tranh thu, 10 lần hai phân bổ khác kỳ tranh credit, 5 lần replay/thu đồng thời. Lượt full trước phát hiện deadlock nâng khóa hộ; sau sửa sang upsert khóa exclusive, các lượt trên và full verify đã qua.
- Giới hạn: test cạnh tranh chỉ kiểm các lịch chạy thực tế đã quan sát, không chứng minh mọi lịch; chưa có test HTTP thật toàn luồng thanh toán, chưa kiểm audit rollback cho từng lệnh đảo/phân bổ/hủy hoặc log request dưới mọi cấu hình; chưa chạy CI remote, Windows hoặc máy thứ hai. UI S09 thuộc lượt sau; không tick task S09.

### S09 — vòng sửa sau review, 09/11/2026

- Summary của hóa đơn `CANCELLED` trả trạng thái `CANCELLED` và nợ có thể thu bằng `0`; test integration xác nhận tổng gốc được giữ. Số tiền có số mũ lớn bị từ chối trước khi mở rộng; test dùng `1E+100000000` và `1E-100000000` với giới hạn thời gian. Cổng billing ủy quyền tăng `receipt_count` cho `BillingReceiptGuard` hiện có.
- Bốn lệnh đều gắn actor vào fingerprint; regression xác nhận cùng key/payload với actor khác trả xung đột, không replay kết quả.
- Sau sửa, JDK 21.0.9: `cd backend && ./mvnw -B -ntp verify` **224 test, 0 lỗi, BUILD SUCCESS** (12,6 giây). MySQL 8.4.13 cô lập tại `127.0.0.1:3308`: `cd backend && DB_HOST=127.0.0.1 DB_PORT=3308 DB_NAME=bluemoon DB_USER=bluemoon DB_PASSWORD=<mật-khẩu-test-local> ./mvnw -B -ntp -Pintegration verify` **224 unit/API + 145 integration, 0 lỗi, BUILD SUCCESS** (67 giây). Các giới hạn kiểm thử và UI ở mục S09 phía trên vẫn còn.

### S09 — API đọc cho UI và quyền riêng tư D28, 09/11/2026

- Bổ sung danh sách công nợ có tìm mã hộ/căn hộ, lọc kỳ/trạng thái và phân trang; lịch sử phân bổ có các receipt nguồn FIFO, trạng thái và metadata đảo; lịch sử receipt có metadata hủy, số allocation đang hoạt động và `canVoid`. GET chỉ ADMIN/ACCOUNTANT. Nội dung lý do giữ trong DB nhưng response đọc/ghi và `payment_commands.result_json` chỉ có cờ `hasLateReason`, `hasVoidReason`, `hasReverseReason`, theo D28.
- JDK 21.0.9: `cd backend && ./mvnw -B -ntp verify` **226 test, 0 lỗi, BUILD SUCCESS** (10,7 giây). MySQL 8.4.13 cô lập tại `127.0.0.1:3308`: `cd backend && DB_HOST=127.0.0.1 DB_PORT=3308 DB_NAME=bluemoon DB_USER=bluemoon DB_PASSWORD=<mật-khẩu-test-local> ./mvnw -B -ntp -Pintegration verify` **226 unit/API + 146 integration, 0 lỗi, BUILD SUCCESS** (67 giây).
- MockMvc kiểm quyền, JSON và không lộ lý do; MySQL kiểm tổng tiền theo receipt/allocation, trạng thái trước/sau đảo, lọc, tìm literal `%`, phân trang, nguồn FIFO và cờ. Chưa kiểm end-to-end với frontend/HTTP thật, chưa đo hiệu năng dữ liệu lớn, chưa CI remote. Không tick S09 khi UI chưa nghiệm thu.
- Sau full verify, thêm một MockMvc regression xác nhận GET receipt/allocation có cờ nhưng không có trường lý do raw; `cd backend && ./mvnw -B -ntp -Dtest=PaymentApiTest test`: **5 test, 0 lỗi, BUILD SUCCESS** (6,1 giây). Không có thay đổi production sau full verify.

### S09 — UI thu tiền, 09/11/2026

- `/accountant/collections` dùng API thật trong API mode và giữ Demo; `/admin/collections` chỉ đọc. Danh sách mặc định công nợ mọi kỳ, có lọc hóa đơn đã trả; hỗ trợ thu, phân bổ, hủy và đảo, phép tính VND nguyên bằng BigInt. Retry lưu UUID và payload trong sessionStorage theo username, kiểm tra actor trước mỗi lần gửi và gửi lại sau CSRF; reason chỉ hiển thị dưới dạng cờ.
- `cd frontend && npm test`: **28 file, 134/134 test qua** (5,07 giây). Có regression đổi tài khoản ngay khi component còn mounted, retry nguyên payload, CSRF đổi actor, quyền ADMIN, lọc, phân bổ và đảo. Còn cảnh báo React `act(...)` ở test FeeRatesPage và AppStateProvider hiện có.
- `cd frontend && npm run build`: TypeScript và Vite qua; JS 518,42 kB (gzip 145,87 kB), có cảnh báo chunk >500 kB.
- `git diff --check`: qua.
- Chưa walkthrough với backend/HTTP thật, chưa kiểm bằng mắt mobile, zoom 200%, keyboard/screen reader hoặc CI remote; test jsdom không chứng minh các trường hợp đó. Không chạy lại backend/MySQL trong lượt UI; bằng chứng backend ở trên. Không tick S09.
- QA bổ sung qua Orca/Agy (`gemini-3.8-flash`, high) trên đúng worktree `s09-contract`: `cd frontend && npm test -- --run` **28 file, 134 test qua**; `cd frontend && npm run build` **TypeScript/Vite qua**; không sửa file. Không có browser/E2E harness trong repo nên chưa walkthrough thật các kích thước/zoom/keyboard.
- Worker có thử thêm `cd backend && ./mvnw -B -ntp test` ngoài phạm vi UI; lệnh thất bại nhưng phần output nguyên nhân không được lưu trong terminal archive. Backend vẫn có bằng chứng `verify` và integration pass ở trên; cần chạy lại đúng lệnh nếu cần chẩn đoán riêng.

### S09 — QA backend và luồng UI qua Orca/Agy, 09/11/2026

- Worktree `s09-contract`, model `gemini-3.8-flash`, effort `high`. Agy chạy `cd backend && ./mvnw -B -ntp verify`: **227 test pass**; MySQL 8.4 cô lập ở port 3308 với `-Pintegration verify`: **146 integration test pass**. Không reset DB/volume.
- Coverage backend được đối chiếu tới `ReceiptAmountsTest`, `CreditAllocationAmountsTest`, `PaymentFlowIT`, `PaymentConcurrencyIT`, `PaymentApiTest` và `PaymentSchemaIT`: thu thiếu/thừa, FIFO + đảo, concurrency/idempotency, quyền và cờ privacy reason.
- Frontend: `cd frontend && npm test -- --run`: **28 file, 134 test pass**; `cd frontend && npm run build`: TypeScript/Vite pass. Đã đối chiếu các test trong `s09-page.test.tsx`, `s09-payments.test.ts`, `s09-csrf-actor.test.ts` và `s09-routes.test.tsx` với preview, lọc, retry cùng UUID/payload, đổi actor, ADMIN read-only và RESIDENT denial.
- Chưa có browser/E2E harness trong repo; chưa walkthrough UI nối API thật, kích thước mobile, zoom 200%, keyboard/screen reader. Kết quả trên xác nhận backend unit/API + MySQL integration và frontend DOM tests/build, không thay thế E2E hoặc CI remote.
