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

### S06 backend: khai báo, duyệt, tra cứu tạm vắng, 07/11/2026
- Worker Codex: `cd backend && ./mvnw -B -ntp verify` (JDK 21.0.9) ở worktree feat-s06, HEAD `7b9828e`: 93 test, 0 lỗi, BUILD SUCCESS. Copilot QA cùng SHA: 93 test (lời Copilot, chưa kiểm chứng).
- Bằng chứng do Sơn chạy: `./mvnw -B -ntp -Pintegration verify` (JDK 21, MySQL cổng 3307), 07/11/2026 16:24 +07: failsafe 89 test, 0 lỗi, BUILD SUCCESS, 52 s. Báo cáo failsafe: `DeclarationIntegrationIT` 14 test, `ResidentIntegrationIT` 7 test. V7 đã áp trên DB của Sơn; chưa rõ DB có dữ liệu S05 hay sạch.
- Claude không chạy Maven (sandbox); không gọi subagent verifier, tự đối chiếu SHA, số test, cờ bỏ qua. Review Opus hai lần: đạt.
- Chưa xác nhận: UI (AC "ghép UI"), CI remote, dữ liệu S05 thật, quy tắc phí/báo cáo cho `TEMPORARILY_ABSENT`, văn bản xác nhận của Tuấn/Kiệt/Quân. Chi tiết: `docs/sprint-1/TASKS.md` mục "S06 backend" và `_agents_workspace/S06/04_evidence.md`.

### S06 UI: khai báo, duyệt, quay lại, gợi ý tên tạm vắng, 07/11/2026
- Worker Codex: `cd frontend && npm ci && npx tsc -b && npm test && npm run build` (Node 26.10.0) ở worktree feat-s06-ui, HEAD `70c06c7`: vitest 12 file / 51 test đạt, `tsc` không lỗi, build đạt. Copilot QA cùng SHA: cùng kết quả (lời Copilot, chưa kiểm chứng).
- Review Opus 2 lần: lần 1 có 1 lỗi chặn (dữ liệu mẫu lọt vào form M-31 chế độ api), đã sửa ở `70c06c7`; lần 2 đạt.
- Chưa xác nhận: chạy trên Chrome với backend S06 thật, 360px, screen reader; test Esc và tải lại sau quyết định thất bại; 3 vulnerabilities do `npm ci` báo; PR frontend trỏ vào nhánh S03 chưa merge. Chi tiết: `docs/sprint-1/TASKS.md` mục "S06 UI", `_agents_workspace/S06/ui/04_evidence.md`.
