# Checklist Sprint 1

Mã F/S là task kỹ thuật trong harness, không phải mã WBS mới. S/M/L chỉ độ phức tạp. Toàn bộ chưa hoàn thành cho tới khi có bằng chứng; file skeleton được tạo không đồng nghĩa F00/F02 đã nghiệm thu.

| Xong | Mã | Cỡ | Người phụ trách | Công việc / WBS | Tiền nhiệm / điều kiện | Acceptance criteria |
|---|---|---|---|---|---|---|
| ☑ | F00 | S | Kiệt | Repo, wrapper, hướng dẫn, CI; WBS 1.2.1.2 | Không | Clone/build được; repo có AGENTS, PR checklist và CI; xác nhận main sau merge. |
| ☑ | F01 | M | Sơn + Quân + Tuấn | Chốt contract, quyền và dữ liệu; WBS 1.2.1.1–3 | F00 | Ma trận quyền/account–hộ, trường dữ liệu và hợp đồng API có người nhận xác nhận; giải quyết mapping sai. |
| ☑ | F02 | M | Sơn + Tuấn | Nền tảng backend/DB và luồng mẫu; WBS 1.2.1.2 | F00 | Health/metadata, security boundary, Flyway/session chạy MySQL; teammate cài lại và có kết quả integration. |
| ☑ | S01 | M | Sơn; Tuấn review | Identity/session/role/audit; WBS 2.1.1.1, 2.1.1.3 | F01,F02 | Đăng nhập đúng/sai, đổi session ID, 401/403, audit write; chốt và test chống brute-force, không mặc định đã có. |
| ☑ | S02 | M | Sơn + Kiệt | Cấp account và email mô phỏng; WBS 2.1.1.1,2.1.2.3 | S01 | Email trùng bị từ chối; mock email đúng người; bootstrap BQT và cấp cư dân đã thống nhất; không log password. |
| ☑ | S03 | M | Kiệt + Sơn | Đổi mật khẩu, hồ sơ; WBS 2.1.1.2,2.1.2.1 | S01 | Kiểm tra mật khẩu cũ/mới, tối thiểu 8 ký tự; đổi xong các session cũ bị từ chối; DTO không lộ hash. |
| ☑ | S04 | M | Sơn + Tuấn | Vô hiệu account và test EP-01; WBS 2.1.2.2,2.1.1.3 | S01 | Khóa xong request mới với cookie cũ thất bại; thử nhiều session và server role; UI cần test tích hợp để đóng. |
| ☑ | S05 | L | Sơn + Tuấn | Hộ/nhân khẩu/biến động; WBS 2.2.1.1 | F01,S01 | CRUD/tra cứu, ownership; chuyển đi đổi trạng thái cư trú (không xóa bản ghi, DECISIONS D20) kèm lịch sử biến động và audit; validate trường đã chốt, test hộ khác bị chặn. |
| ☑ | S06 | L | Kiệt + Sơn + Tuấn | Khai báo/duyệt/tra cứu; WBS 2.2.1.2–3 | S05 bản API đầu | Chờ duyệt → duyệt/từ chối đúng quyền; lưu trạng thái/lịch sử, thông báo trong app/mock theo AC; cuối cùng ghép UI. |
| ☑ | S07 | M | Quân + Kiệt; Tuấn kiểm tra | Đơn giá/công thức; WBS 2.3.1.1,2.3.1.3 | F01,F02 | BigDecimal; 50m² × 10.000 = 500.000; hiệu lực/rounding đã chốt; không áp thay giá vào hóa đơn cũ. |
| ☑ | S08 | L | Quân + Tuấn | Kỳ thu/tự nguyện; WBS 2.3.1.2–3 | S07; S05 để ghép cuối | Không trùng kỳ/hộ/loại; tự nguyện không cộng nợ; giao dữ liệu thật cho thu tiền và đối chiếu độc lập. |
| ☑ | S09 | L | Quân + Sơn | Ghi nhận thu tiền/gạch nợ; WBS 2.4.1.1 | S05,S08 | Thu đúng số tiền, nợ giảm đúng, transaction/audit; duplicate/concurrent không thu hai lần; chốt thu thiếu/thừa/sửa/hủy. |
| ☑ | S10 | M | Kiệt + Quân | API và UI hóa đơn/lịch sử cho cư dân; WBS 2.4.1.2 | S08; S09 để ghép cuối | Backend giới hạn theo hộ; UI API mode có danh sách/lọc/phân trang, chi tiết và lịch sử; Demo tách riêng; chỉ đọc; các kiểm tra hoãn được ghi ở VALIDATION. |
| ☑ | S11 | M | Tuấn + Quân + Kiệt | Export thu phí/cư trú; WBS 2.4.1.3 | S06,S09 | Excel mở được và tổng khớp; mẫu/cột/tỷ lệ đã duyệt; không export dữ liệu thừa; dùng dữ liệu thật từ DB. |
| ☐ | S12 | M | Tuấn + Sơn | Gói dữ liệu cơ quan; WBS 2.4.1.4, PBI-023 | S11 | Chỉ gói/trường BQT phê duyệt; cơ quan không truy DB thô; có test deny trường/hộ ngoài phạm vi. |
| ☐ | S13 | M | Tuấn; Sơn hỗ trợ | Backup tự động/restore, NFR-008; gắn DoD 2.4.1.4 | F02; dữ liệu mẫu S09 | Job backup có lịch/retention được chốt; restore vào DB thử riêng rồi đối chiếu; không xóa DB gốc. |
| ☐ | S14 | L | Cả nhóm; Tuấn điều phối | Hồi quy/quyền/hiệu năng nền; WBS 2.4.1.4 | S02–S13 hoàn tất; có thể chuẩn bị trước | E2E hộ→kỳ→thu→báo cáo; ca sai/IDOR/duplicate; 10k và thời gian có cấu hình đo; sửa và test lại. |
| ☐ | S15 | S | Cả nhóm; Kiệt tổng hợp | Đóng gói/bàn giao v1; WBS 2.4.1.4 | S14 | Cài từ máy sạch, dữ liệu giả, hướng dẫn, kết quả test và lỗi còn lại có PO chấp nhận; không còn blocker. |

S08 và S09 được Sơn yêu cầu đánh dấu hoàn thành; các giới hạn còn ghi trong `docs/VALIDATION.md`. S10 backend được Sơn duyệt G2 ngày 09/11/2026; S10 UI hoàn tất theo bằng chứng ngày 10/11/2026. Phép đo 10.000 bản ghi/EXPLAIN chuyển sang S14.

## Checklist có thể tick

- [x] F00 — Repo, wrapper, hướng dẫn, CI; WBS 1.2.1.2
- [x] F01 — Chốt contract, quyền và dữ liệu; WBS 1.2.1.1–3
- [x] F02 — Nền tảng backend/DB và luồng mẫu; WBS 1.2.1.2
- [x] S01 — Identity/session/role/audit; WBS 2.1.1.1, 2.1.1.3
- [x] S02 — Cấp account và email mô phỏng; WBS 2.1.1.1,2.1.2.3
- [x] S03 — Đổi mật khẩu, hồ sơ; WBS 2.1.1.2,2.1.2.1
- [ ] S04 — Vô hiệu account và test EP-01; WBS 2.1.2.2,2.1.1.3
- [x] S05 — Hộ/nhân khẩu/biến động; WBS 2.2.1.1
- [x] S06 — Khai báo/duyệt/tra cứu; WBS 2.2.1.2–3 (tick 08/11/2026 theo lời Sơn, có caveat UI: xem mục S06 UI)
- [x] S07 — Đơn giá/công thức; WBS 2.3.1.1,2.3.1.3
- [x] S08 — Kỳ thu/tự nguyện; WBS 2.3.1.2–3
- [x] S09 — Ghi nhận thu tiền/gạch nợ; WBS 2.4.1.1
- [x] S10 — API và UI hóa đơn/lịch sử cho cư dân; WBS 2.4.1.2 (NFR chuyển S14; kiểm tra hoãn xem `docs/VALIDATION.md`)
- [x] S11 — Export thu phí/cư trú; WBS 2.4.1.3
- [ ] S12 — Gói dữ liệu cơ quan; WBS 2.4.1.4, PBI-023
- [ ] S13 — Backup tự động/restore, NFR-008; gắn DoD 2.4.1.4
- [ ] S14 — Hồi quy/quyền/hiệu năng nền; WBS 2.4.1.4
- [ ] S15 — Đóng gói/bàn giao v1; WBS 2.4.1.4

## Bằng chứng task
Khi hoàn tất, thêm: mã task; commit/PR; lệnh và kết quả test; đầu ra giao cho ai; assumption đã xác nhận; lỗi còn lại.

### F00 (tick 05/11/2026)
```text
Task: F00 (WBS 1.2.1.2) — Repo, wrapper, hướng dẫn, CI
Commit/PR: PR #4 "Backend" đã merge vào main (Sơn xác nhận); commit nền repo 200b680.
Lệnh và kết quả: CI của PR #4 xanh (Sơn xác nhận pass; job verify gồm bước MySQL theo lời Copilot, headSha 763aada chưa đối chiếu riêng). Repo có AGENTS.md, .github/pull_request_template.md, .github/workflows/backend.yml (đã tracked).
AC: Clone/build được: đạt qua CI (chưa có thành viên thứ hai tự clone). Repo có AGENTS, PR checklist, CI: đạt. Xác nhận main sau merge: Sơn báo "F00 đã xác nhận" (05/11/2026); chưa có xác nhận bằng văn bản của Kiệt trong repo.
Đầu ra giao cho: F01, F02 (đã tick), cả nhóm dùng repo/CI.
Assumption đã xác nhận: Sơn báo F00 đã được xác nhận.
Lỗi còn lại, điều chưa kiểm thử: branch protection/ruleset chưa bật (CONTRIBUTING); chưa chạy trên Windows/ARM hoặc máy thứ hai.
Kết luận: đủ để tick (theo xác nhận của Sơn)
```

### F01 (tick 05/11/2026)
```text
Task: F01 (WBS 1.2.1.1–3) — Chốt contract, quyền và dữ liệu
Commit/PR: không có (tài liệu). Bản đề xuất: _agents_workspace/F01/02_final_proposal.md (ngoài repo). Quyết định ghi ở docs/DECISIONS.md D13–D17.
Lệnh và kết quả: không có lệnh chạy; F01 là quyết định, không phải code.
AC:
  - Ma trận quyền/account–hộ: Sơn báo nhóm đồng ý mục 3 (05/11/2026); account–hộ theo D15 (một account một hộ).
  - Trường dữ liệu: Sơn báo đồng ý mục 4 (V2–V4 giữ nguyên).
  - Hợp đồng API S01: Sơn báo đồng ý mục 5 (csrf, login, logout, me).
  - Giải quyết mapping sai: mục 1 đồng ý (vai trò thứ tư là một vai trò; PBI-004 khóa account khác US-004 audit).
Đầu ra giao cho: S01 (Sơn; Tuấn review), S05, S07; S12 (vai trò thứ tư).
Assumption đã xác nhận (qua lời Sơn, 05/11/2026): mục 1–5 đồng ý; Q1 a, Q2 a, Q3 a, Q4 a; Q5 theo D17 (khóa theo account, sai thêm 1 lần là lên bậc 1/5/10/15/30/60 phút rồi tiếp tục tăng, không trần, đếm lại khi đăng nhập thành công, ADMIN không bị khóa).
Lỗi còn lại, điều chưa kiểm thử:
  - Chưa có xác nhận bằng văn bản của Quân, Tuấn, Kiệt; toàn bộ xác nhận là lời Sơn kể lại. Mục 9 của bản đề xuất yêu cầu văn bản: tick theo quyết định của Sơn, không theo điều kiện mục 9.
  - Tên mã vai trò thứ tư chưa có; mô tả của Sơn (yêu cầu trích xuất có giấy phép, thông báo qua BQT) rộng hơn PBI-023, chưa thành quy tắc quyền, chốt ở S12.
  - D17: các bậc cụ thể sau 60 phút chưa liệt kê; không trần + khóa theo account cho phép khóa vô thời hạn tài khoản không phải ADMIN; ADMIN không bị khóa nên không có lớp chặn dò mật khẩu ADMIN ở tầng ứng dụng.
  - Hàng "HOÃN" của ma trận (S05, S06, S07, S08, S11, S12) và tập giá trị residency_status/change_type chưa chốt.
Kết luận: tick theo yêu cầu của Sơn kèm các caveat trên
```

### F02 (tick 05/11/2026)
```text
Task: F02 (WBS 1.2.1.2) — AuditService, RoleService mẫu
Commit/PR: PR #4 "Backend" (KietQuangT/SE_10-BlueMoonComplex) đã merge; commit F02 763aada. Sơn xác nhận đã merge và CI pass. Review APPROVED của KietQuangT chỉ theo lời Copilot, chưa đối chiếu riêng.
Lệnh và kết quả:
  - clean verify (Java 21.0.9): BUILD SUCCESS, 26 test (AuditServiceTest 21, SystemInfoApiTest 5), 05/11/2026.
  - -Pintegration (Sơn chạy, MySQL 8.4.13, 15:31 +07): BUILD SUCCESS, failsafe 7 test (RoleAuditIT 4, BootstrapIT 3), 0 lỗi; Flyway áp V2–V4, "validated 4 migrations".
  - CI PR #4: job verify xanh gồm bước "Build và kiểm thử với MySQL" (Sơn xác nhận pass). headSha 763aada theo Copilot, chưa đối chiếu riêng.
AC:
  - Health/metadata: đạt (SystemInfoApiTest; BootstrapIT health UP trên MySQL).
  - Security boundary: đạt (401, 403, CSRF, CORS ở SystemInfoApiTest; full context ở BootstrapIT).
  - Flyway/session chạy MySQL: đạt (BootstrapIT, V1–V4, 8 bảng, JDBC session).
  - Teammate/CI chạy lại integration: đạt bằng CI của PR #4 (Sơn xác nhận pass); chưa có thành viên thứ hai tự cài lại.
  - Luồng mẫu audit cùng transaction: đạt (AuditServiceTest 21, RoleAuditIT 4 gồm rollback, FK fk_audit_actor, record ngoài transaction bị từ chối).
Đầu ra giao cho: S01 (Sơn; Tuấn review) dùng AuditService; S07, S13 phụ thuộc F02.
Assumption đã xác nhận: Sơn cho vượt tiền nhiệm F00 (F00 chưa tick); Sơn chọn giữ RoleService trong src/main; Tuấn đồng ý khối audit.
Lỗi còn lại, điều chưa kiểm thử: Tuấn xác nhận map Role mới qua lời Sơn, chưa có văn bản; V2–V4 do Sơn duyệt, chờ Quân/Tuấn/Kiệt, đổi sau merge phải thêm migration mới; chưa chạy Windows/ARM hay máy thành viên thứ hai; RoleService.rename không kiểm tra quyền (S01 thay hoặc xóa); Hibernate log lỗi trùng email làm lộ email; uq_roles_name collation không phân biệt dấu, chưa có handler 409; rename vẫn ghi audit khi tên không đổi.
Kết luận: đủ để tick
```

### S01 (tick 06/11/2026)
```text
Task: S01 (WBS 2.1.1.1, 2.1.1.3) — Identity/session/role/audit, chống brute-force
Commit/PR: nhánh local hhoangsonnw/feat-s01-identity-session, HEAD 1746a8a (6626fea, 6380a70, 1746a8a trên main b197a34). CHƯA push, CHƯA PR, Tuấn CHƯA review.
Lệnh và kết quả:
  - Bằng chứng do Sơn chạy (MySQL 8.4.13 ở 127.0.0.1:3306, JDK 21, `cd backend && ./mvnw -B -ntp -Pintegration verify`, worktree sạch ở 1746a8a; commit 00:03:38, chạy 00:06 +07 ngày 06/11/2026):
      JVM +07:  45 test không cần DB + 31 IT (RoleAuditIT 4, IdentityCookieIT 1, IdentitySessionIT 23, BootstrapIT 3), 0 lỗi, BUILD SUCCESS, 24,120 s.
      TZ=UTC:   45 + 31, 0 lỗi, BUILD SUCCESS, 26,645 s.
      Lần chạy đầu (HEAD 6380a70): IdentitySessionIT 7 Failure + 9 Error (9 NPE MockHttpSession, 6 ca bậc khóa lệch múi giờ, 1 assertion CHECK); đã sửa ở 1746a8a.
    Flyway: "Successfully validated 5 migrations", áp V5 lên DB bluemoon, ddl-auto=validate qua.
  - Verifier tự chạy `mvn-sandbox.sh -B -ntp verify` trong worktree Orca: KHÔNG chạy được (sandbox không ghi được target/ ngoài thư mục làm việc). Phần không cần DB (45 test) dựa trên log của Sơn.
  - SQL thật của khóa hàng (log Hibernate): `select ... from users u1_0 join roles r1_0 on r1_0.id=u1_0.role_id where u1_0.username=? for update of u1_0`; chỉ khóa `users`. Chưa có EXPLAIN.
  - Review rủi ro: 3 vòng (6626fea: 2 lỗi chặn; 6380a70: 3 lỗi test; 1746a8a: không còn lỗi chặn). Đã dùng hết 2 vòng sửa. Chi tiết `_agents_workspace/S01/03_review.md`.
  - Copilot QA (lời Copilot, chưa kiểm chứng): verify 37 test xanh trên 6626fea; báo cáo bị cắt giữa chừng (`05_qa.md`).
AC:
  - Đăng nhập đúng/sai: đạt (IdentitySessionIT, IdentityCookieIT; 5 nhánh lỗi cùng status và body 401).
  - Đổi session ID: đạt (IdentityCookieIT qua HTTP thật: cookie cũ /me 401, cookie mới 200, đúng 1 dòng SPRING_SESSION; logout 204, phiên B không bị hủy).
  - 401/403: đạt (AuthApiTest, IdentitySessionIT). 403 theo vai trò dùng endpoint chỉ-ADMIN trong src/test với security chain riêng (không qua chain thật).
  - Audit write: đạt (LOGIN_SUCCESS, LOGIN_FAILED, ACCOUNT_LOCKED cùng transaction, rollback; AuditService.recordBootstrap actor NULL, action cứng BOOTSTRAP_ADMIN, từ chối ngoài transaction).
  - Chống brute-force: đạt theo D17/D18 (P2): bậc 1/5/10/15/30/60 phút, +60 phút từ lần 7, chống tràn DATETIME; đang khóa không đếm không gia hạn; ADMIN không khóa; 10 lần sai song song không mất cập nhật; không phụ thuộc múi giờ JVM (+07 và UTC đều xanh).
Đầu ra giao cho: Kiệt (frontend: GET /auth/csrf, POST /auth/login, POST /auth/logout, GET /auth/me; gọi lại /auth/csrf sau login và logout); Tuấn (review S01; /auth/csrf tạo một dòng SPRING_SESSION cho mỗi lần gọi chưa đăng nhập, tự hết hạn sau 30 phút nhưng làm phình bảng); S02 (lệnh bootstrap BQT, dùng AuditService.recordBootstrap); S03/S04 (kiểm tra lại quyền và status từng request, hủy session theo principal, mở khóa thủ công của BQT).
Assumption đã xác nhận: D13–D18 theo lời Sơn (05/11/2026); Q1=P2, Q2–Q10 theo G1. Chưa có văn bản của Quân, Tuấn, Kiệt.
Lỗi còn lại, điều chưa kiểm thử:
  - Chưa push, chưa PR, chưa có CI trên remote; Tuấn (người review theo bảng) chưa review.
  - Verifier chưa tự chạy lại được `verify`; bằng chứng test là log do Sơn chạy trên một máy macOS, JDK 21.0.9. Chưa chạy trên Windows/ARM/máy thứ hai.
  - N1 (nhánh đang khóa và nhánh INACTIVE không chạy BCrypt thì có thể đo thời gian phản hồi để biết account tồn tại): chưa quyết, chưa test. N7 (INACTIVE và RESIDENT thiếu hộ không đếm, không audit): chờ Sơn xác nhận. N8: `password` giới hạn @Size(max=1024) do worker chọn, chưa chốt (S03).
  - 403 theo vai trò chưa qua chain thật; "đường không khai báo khi đã đăng nhập thì 403" chưa có test; bậc >=7 chỉ test hàm thuần (không qua service); "hết khóa" dựng bằng ghi locked_until quá khứ vào DB, chưa test tiến AdjustableClock; cookie sau login chưa kiểm HttpOnly/SameSite riêng; chưa có EXPLAIN cho index uq_users_username; chưa có round trip MySQL cho trần 9999-12-31 23:59:59.999999.
  - Không có chặn theo IP (không có nguồn dữ liệu); ADMIN không bị khóa nên không có lớp chặn dò mật khẩu ADMIN ở tầng ứng dụng (D17); khóa theo account không trần cho phép khóa lâu mọi account không phải ADMIN cho tới khi S04 có mở khóa thủ công.
  - V5 đã áp lên DB bluemoon cục bộ của Sơn; nếu V5 đổi trước khi merge, Flyway báo sai checksum trên DB đó (Sơn tạo lại DB cục bộ, không sửa tay flyway_schema_history).
Kết luận: tick theo bằng chứng chạy thật của Sơn (hai lần -Pintegration xanh ở +07 và UTC) và review rủi ro không còn lỗi chặn, kèm các caveat trên; Tuấn review và PR còn chờ cổng G2 và Tuấn review.
Cập nhật 06/11/2026 (theo git, không phải bằng chứng test mới): PR #5 đã merge vào main (merge commit 6e12045), nên các dòng "CHƯA push, CHƯA PR" ở trên không còn đúng. Chưa có bằng chứng văn bản về việc Tuấn review.
```

### S02 (tick 06/11/2026)
```text
Task: S02 (WBS 2.1.1.1, 2.1.2.3) — Cấp account, email mô phỏng, bootstrap BQT
Commit/PR: nhánh local feat/s02-account-provisioning, HEAD 6e138e4 (2cdef6f, 20f883f, 6e138e4 trên nền main 6e12045). CHƯA push, CHƯA PR, Tuấn CHƯA review.
Lệnh và kết quả:
  - Bằng chứng do Sơn chạy (MySQL 8.4.13 ở localhost:3306, JDK 21.0.9, macOS aarch64, JVM +07, `cd backend && ./mvnw -B -ntp -Pintegration verify`, worktree sạch ở 6e138e4; commit 11:38, chạy 11:43–11:44 +07 ngày 06/11/2026; log không in SHA nên khớp commit là suy luận từ thời điểm):
      surefire 56 test (AccountApiTest 5, MustChangePasswordGuardTest 3, BootstrapSecurityContextTest 1, EmailNormalizerTest 1, TemporaryPasswordGeneratorTest 1, AuthApiTest 9, AuditServiceTest 23, LoginLockPolicyTest 6, UnknownUserTimingTest 1, UserRepositoryQueryTest 1, SystemInfoApiTest 5);
      failsafe 51 IT (AccountProvisioningIT 11, BootstrapAdminIT 7, AccountAtomicityIT 2, IdentitySessionIT 23, RoleAuditIT 4, IdentityCookieIT 1, BootstrapIT 3); 0 lỗi; BUILD SUCCESS; 35,763 s.
    Flyway: V5 lên V6 áp thành công ở lần chạy đầu; "Successfully validated 6 migrations"; ddl-auto=validate qua với BOOLEAN.
  - Lần chạy trước (HEAD 20f883f): failsafe 49/51; AccountAtomicityIT 2 lỗi do cách viết test (stub spy qua proxy `@Transactional(MANDATORY)`), sửa ở 6e138e4, không đổi code sản phẩm.
  - Bootstrap trong terminal thật (Sơn chạy tay 06/11/2026 12:23–12:28 +07, `java -jar target/bluemoon-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=bootstrap`, DB bluemoon cục bộ): lần 0 mật khẩu bị từ chối ("Dữ liệu không hợp lệ.", không tạo gì); lần 1 "Đã tạo ADMIN."; lần 2 "Đã có ADMIN, bỏ qua."; mật khẩu không hiện trên terminal. SQL đối chiếu của Sơn: `audit_log` có 1 dòng `BOOTSTRAP_ADMIN` actor NULL changes `{"role":"ADMIN"}`; `users` của tài khoản đó `must_change_password=0`, `status=ACTIVE`. Chưa kiểm: lịch sử shell, đăng nhập bằng tài khoản vừa tạo qua API, chạy không có TTY ngoài test.
  - Verifier tự chạy `mvn-sandbox.sh -B -ntp verify`: KHÔNG chạy được (sandbox không ghi được target/ trong worktree Orca). Bằng chứng test là log của Sơn, report trong worktree và file jar.
  - Review rủi ro: C1 (đặt sai tên logger Hibernate, email vào log khi race) và C2 (assertion CHECK sai kiểu exception) sửa ở 20f883f; K1 (tùy biến CSRF) bỏ theo quyết định của Sơn; đã dùng hết 2 vòng sửa, chưa có review lại sau sửa (đóng lỗi dựa vào test xanh và đọc diff). Chi tiết `_agents_workspace/S02/03_review.md`.
  - Copilot QA (lời Copilot, chưa kiểm chứng): verify 55 test xanh trên 2cdef6f; báo sai là không có test race (có: AccountProvisioningIT.concurrentEmailCreationHasOneWinnerAndDoesNotLogEmail).
AC:
  - Email trùng bị từ chối: đạt (AccountProvisioningIT: cùng, khác hoa/thường, có khoảng trắng đều 409 EMAIL_ALREADY_USED, body không chứa email; race 8 luồng đúng một thắng; AccountApiTest; BootstrapAdminIT).
  - Mock email đúng người: đạt ở mức DB (mockEmailsBelongToTheirIntendedRecipients: hai account, recipient khớp, body chứa mật khẩu của chính nó, mật khẩu A không vào B, SIMULATED). Kiểm tra chéo EP-01 của Kiệt chưa làm.
  - Bootstrap BQT và cấp cư dân: đạt. Cấp cư dân đủ ca hộ (thiếu, không tồn tại, INACTIVE, nhân sự kèm hộ); BootstrapAdminService qua BootstrapAdminIT; chạy tay trong terminal thật đạt (lần 1 tạo, lần 2 từ chối, audit actor NULL). "Đã thống nhất" theo lời Sơn (D14–D16, D19).
  - Không log password: đạt trong phạm vi output test (kèm org.hibernate.SQL=DEBUG); không phủ org.hibernate.orm.jdbc.bind=TRACE (sẽ in email, body, password_hash).
Đầu ra giao cho: Kiệt (EP-01 và M-10/M-11; /auth/login và /auth/me nay có 5 trường gồm mustChangePassword; POST /api/v1/accounts trả 201 không mật khẩu; mock email xem bằng SQL trên bảng mock_emails; lỗi 400/409 có code); S03 (xóa cờ must_change_password, che mock_emails.body khi xóa cờ, hủy mọi session của user khi đổi mật khẩu, thêm endpoint đổi mật khẩu vào danh sách cho phép của MustChangePasswordFilter cùng PR, chốt mật khẩu tối đa ≤ 72 byte); S04 (phủ POST /accounts khi kiểm tra lại quyền, email của account INACTIVE vẫn giữ chỗ); S05 (thay truy vấn native trạng thái hộ trong identity/persistence); S12 (vai trò thứ tư, V7).
Assumption đã xác nhận: D14–D16 và D19 theo lời Sơn (06/11/2026), Kiệt đồng ý theo lời Sơn, Tuấn không tham gia S02. Chưa có văn bản của Quân, Tuấn, Kiệt.
Lỗi còn lại, điều chưa kiểm thử:
  - Chưa push, chưa PR, chưa CI, Tuấn chưa review; chưa có người được xác nhận kiểm tra bảo mật S02. Chưa chạy Windows hay máy thứ hai (đã chạy macOS aarch64).
  - Lệnh bootstrap báo "Dữ liệu không hợp lệ." chung chung và in stack trace khi mật khẩu bị từ chối, không nói quy tắc nào sai (lỗi dùng được đã thấy ở lần chạy tay đầu); chưa sửa vì đã hết 2 vòng sửa.
  - Tên logger org.hibernate.orm.jdbc.error đúng ở thực tế nhưng chưa có đối chứng bỏ cấu hình; tắt toàn app làm mất log lỗi SQL của Hibernate.
  - Hành vi session cũ sau khi đổi AuthenticatedUser chưa kiểm chứng (suy luận: đọc được với mustChangePassword=false); xóa SPRING_SESSION khi nâng cấp.
  - Race test chưa chứng minh nhánh chặn ở DB có xảy ra; xanh hai lần, chưa đo độ ổn định.
  - Tồn tại, cố ý không sửa: K2 (race FK hộ trả 409 ACCOUNT_CONFLICT thay vì 400), K4 (race bootstrap in exception có email lúc khởi động thất bại), K5 (JDK 22+ System.console() có thể khác null khi không có TTY), K9 (tab/newline đầu cuối email bị từ chối thay vì cắt).
  - mock_emails.body chứa mật khẩu tạm dạng rõ, không hết hạn (hệ quả D14); ADMIN mới chưa đổi mật khẩu không bị khóa (D17); xóa cờ trong DB không làm session đang mở hết bị chặn; mọi ADMIN INACTIVE thì bootstrap vẫn từ chối, phải sửa DB tay.
  - V6 đã áp lên DB bluemoon cục bộ của Sơn, và DB đó có một tài khoản ADMIN thử tạo bằng bootstrap; nếu V6 đổi trước khi merge, Flyway báo sai checksum (Sơn tạo lại DB cục bộ). V6 chưa được Quân, Tuấn, Kiệt xác nhận.
Kết luận: tick theo bằng chứng chạy thật của Sơn (surefire 56 + failsafe 51 xanh ở 6e138e4, bootstrap chạy tay trong terminal thật, SQL đối chiếu), kèm các caveat trên; Tuấn review và push/PR còn chờ cổng G2.
Cập nhật 06/11/2026 (theo git, không phải bằng chứng test mới): PR #6 đã merge vào main (merge commit faa06fa), nên các dòng "CHƯA push, CHƯA PR, chưa CI" ở trên không còn đúng. Chưa có bằng chứng văn bản về việc Tuấn review S02 (D19 ghi Tuấn không tham gia S02).
```

### S05 (tick 06/11/2026) — lát A hộ + lát B nhân khẩu
```text
Task: S05 (WBS 2.2.1.1) — Hộ/nhân khẩu/biến động
Commit/PR: CHƯA push, CHƯA PR, chưa chạy CI. Worktree Orca feat-s05b chứa cả hai lát (nền main 0283cc9), HEAD 2519f94, worktree sạch:
  cdf06a2 feat: thêm quản lý hộ gia đình (S05)                    [lát A, nhánh hhoangsonnw/feat-s05]
  ca6a649 test: bổ sung test S05 lát A theo review vòng 1 (S05)  [lát A, HEAD nhánh hhoangsonnw/feat-s05]
  ba0c6a7 feat: quản lý nhân khẩu và chuyển đi (S05)             [lát B]
  2519f94 fix: sửa theo review vòng 1 lát B (S05)                [lát B, HEAD]
Lệnh và kết quả:
  - Bằng chứng do Sơn chạy (06/11/2026 16:27–16:30 +07, JDK 21.0.9, MySQL 8.4 ở 127.0.0.1:3306, macOS, worktree feat-s05b sạch ở 2519f94; commit 16:20:07):
      cd backend && ./mvnw -B -ntp -Pintegration verify
      surefire: 75 test, 0 lỗi (log Sơn dán).
      failsafe: 63 test, 0 lỗi, 0 failure (failsafe-summary completed=63 errors=0 failures=0): BootstrapIT 3, AccountAtomicityIT 2, AccountProvisioningIT 11, BootstrapAdminIT 7, HouseholdIntegrationIT 5, IdentityCookieIT 1, IdentitySessionIT 23, ResidentIntegrationIT 7, RoleAuditIT 4.
    Flyway: "Current version of schema bluemoon: 6; up to date, no migration necessary" (không migration mới).
    Dòng "BUILD SUCCESS" KHÔNG có trong log Sơn dán (bị head -60 cắt): kết luận dựa trên failsafe/surefire report, không phải dòng BUILD. Các dòng ERROR IllegalStateException là test cố tình làm audit lỗi để kiểm rollback.
    Lần chạy đầu của Sơn bị Access denied (sai mật khẩu DB trong biến môi trường), không phải lỗi code.
  - Verifier KHÔNG chạy được Maven (sandbox không ghi được target/ ở worktree Orca). Kiểm bằng git/grep: HEAD 2519f94, worktree sạch, không migration mới (chỉ V1–V6), 0 Co-Authored-By, không PII trong audit/log, không PageImpl lộ ra API, không DELETE.
  - Review rủi ro: lát A 1 vòng sửa (2 lỗi chặn đều là thiếu test: rollback PATCH và chứng minh lọc), lát B 1 vòng sửa (1 lỗi code: thứ tự khóa guard có thể deadlock; 4 lỗi thiếu test); re-review cả hai không còn lỗi chặn. Chi tiết `_agents_workspace/S05/03_review.md`, `03_review_B.md`. Lần chạy worker lát B đầu không tạo commit nên không tính vòng sửa. Copilot QA: chưa chạy.
AC:
  - CRUD/tra cứu hộ: đạt (HouseholdIntegrationIT.searchPagingPatchAndAudit; HouseholdApiTest).
  - CRUD/tra cứu nhân khẩu: đạt (ResidentIntegrationIT.moveOutHistoryFilteringAndReuse; ResidentApiTest).
  - Ownership, hộ khác bị chặn: đạt (ResidentIntegrationIT.residentCanOnlySeeOwnHouseholdAndNullHouseholdIsForbidden: hai hộ qua phiên JDBC thật; ResidentApiTest.residentMeRouteIsAuthorizedBeforeHouseholdWildcard). householdId lấy từ session; NULL thì 403.
  - Chuyển đi đổi trạng thái, không xóa bản ghi: đạt (ResidentService.moveOut; moveOutHistoryFilteringAndReuse; concurrentMoveOutHasOneWinner).
  - Lịch sử biến động (1 dòng MOVE_OUT/APPROVED) và audit cùng transaction, không PII: đạt (auditFailuresRollbackAllActions, updateAuditFailureRollsBackHousehold, invalidDataAndAuditFailureRollBack).
  - Validate trường đã chốt (areaM2 ≤ 2 chữ số thập phân và > 0, độ dài, ngày theo Asia/Ho_Chi_Minh, trùng số giấy tờ): đạt, trừ các biên nêu ở "Lỗi còn lại".
  - 401/403/CSRF/PASSWORD_CHANGE_REQUIRED mọi route: đạt ở mức WebMvc; DELETE rơi vào denyAll (suy từ matcher và WebMvc, chưa có IT riêng).
Đầu ra giao cho: Tuấn (review, nhận hợp đồng); S06 (cần luồng tạm vắng, enum trạng thái); S08/S09 (đọc trạng thái hộ qua HouseholdLookup; Quân chốt cách đọc diện tích); S04 (householdId nằm trong session: đổi hộ thì hủy session); Kiệt (API hộ, nhân khẩu, /households/me/residents; trang trả record {content, page, size, totalElements, totalPages}).
Assumption đã xác nhận (qua lời Sơn, 06/11/2026): D20, D21 (RESIDING/TEMPORARILY_ABSENT/MOVED_OUT; change_type MOVE_OUT/TEMPORARY_ABSENCE; BQT ghi APPROVED; trùng số giấy tờ trong RESIDING + TEMPORARILY_ABSENT, kiểm ở service; chuyển đi không tương lai, lần hai 409, không hoàn tác, không tự khóa account; cư dân không thấy số giấy tờ và điện thoại, ADMIN thấy đầy đủ; S05 không có endpoint tạm vắng; không khái niệm chủ hộ; diện tích > 2 chữ số thập phân thì 400, không làm tròn; mã hộ/căn hộ không sửa sau tạo). Sơn báo Tuấn đã đồng ý.
  Tuấn đã review và approve (Sơn báo 06/11/2026; hình thức chưa rõ, chưa có link hay văn bản trong hồ sơ; chưa rõ Tuấn đã đọc các lưu ý không chặn của reviewer ở `03_review.md`, `03_review_B.md`).
  CHƯA xác nhận: chưa có văn bản của Tuấn hay Quân; Quân chưa xác nhận A3 (ACCOUNTANT đọc hộ: code đang trả 403).
Lỗi còn lại, điều chưa kiểm thử:
  - Chưa push/PR/CI; Tuấn đã review và approve theo lời Sơn (chưa có bằng chứng văn bản); chưa chạy Windows hay máy thứ hai; dòng BUILD chưa thấy.
  - Trim mã hộ và số giấy tờ (Sơn chưa quyết): collation NO PAD nên "H1" và "H1 " là hai hộ khác nhau, "123 " khác "123" và lách được kiểm trùng.
  - Test đua lát B yếu hơn mức cần (concurrentPatchAndMoveOutUseSameGuardOrder dùng isIn(200,409), không kiểm trạng thái cuối). Deadlock thứ tự khóa guard chỉ suy luận từ cách InnoDB khóa theo khóa ngoại; test đua xanh một lần, chưa tái hiện lỗi cũ.
  - POST nhân khẩu khi bảng households rỗng trả 400 HOUSEHOLD_INVALID thay vì 404.
  - PATCH không xóa được trường (null = giữ nguyên); mọi ghi residents xếp tuần tự toàn cục (khóa guard), chấp nhận ở v1.
  - Biên chưa có test: PATCH address 256 ký tự; status=INACTIVE riêng; DELETE ở mức IT; PATCH kèm trường lạ; hình dạng ResidentPage; mã lỗi ở biên ngày; rollback PATCH hộ chưa verify(target).record.
  - Test tra cứu giả định không ai ghi bảng households đồng thời; fixture đua ghi vào hộ id nhỏ nhất: chạy IT trên DB không có dữ liệu thật.
  - Nợ kiến trúc: service import DTO của api; controller gọi ResidentService.validate static; IT nằm trong package identity; identity.application phụ thuộc residency.application.
  - ADMIN thấy PII đầy đủ trong DTO nhân khẩu.
  - Worker lát A tự thêm mục S05 vào docs/VALIDATION.md ở ca6a649 (ngoài lệnh); mục này sẽ xung đột với mục S05 ở `docs/VALIDATION.md` khi gộp: giữ bản này.
Kết luận: tick theo bằng chứng chạy thật của Sơn (surefire 75 + failsafe 63 xanh ở 2519f94 trên MySQL 8.4) và hai lượt review rủi ro không còn lỗi chặn, kèm các caveat trên; Tuấn đã review và approve (lời Sơn); push/PR còn chờ cổng G2.
```

### S04 (tick 06/11/2026)
```text
Task: S04 (WBS 2.1.2.2, 2.1.1.3) — Vô hiệu account, mở khóa thủ công, hủy session, kiểm tra lại quyền mỗi request
Commit/PR: nhánh feat/s04-account-disable, nền origin/main ee3ef96 (sau PR #7); CHƯA PR, chưa chạy CI. Code ở ab15b71 (worktree Orca feat-s04, nhánh hhoangsonnw/feat-s04), sạch:
  93a19ca feat: vô hiệu account, mở khóa thủ công, hủy session (S04)   [commit gốc de3091e, đã rebase lên main]
  7ebe38b fix: sửa theo review vòng 1 (S04)
  ab15b71 test: bổ sung test N3, audit, N2 và sửa D22 (S04 vòng 2)
Lệnh và kết quả:
  - Bằng chứng do Sơn chạy (06/11/2026 23:20 +07, JDK 21.0.9, MySQL thật, worktree feat-s04 ở ab15b71):
      cd backend && ./mvnw -B -ntp -Pintegration verify
      BUILD SUCCESS (54,1 s). Log Sơn dán: "Tests run: 75, Failures: 0, Errors: 0, Skipped: 0" (pha failsafe).
  - Verifier đối chiếu độc lập các báo cáo trong worktree: surefire 86 test (18 file) và failsafe 75 test (10 file), 0 failure, 0 error, 0 skipped; failsafe-summary completed=75; không có <failure>/<error>/<skipped> trong TEST-*.xml; report đóng dấu 23:19:53–23:20:44, sau commit ab15b71 (23:17:21) và sau lần biên dịch cuối (23:16:56), nên thuộc đúng ab15b71. Không có dấu vết cờ bỏ qua test. AccountManagementIT: 12 test xanh.
  - Verifier và Claude KHÔNG chạy được Maven (sandbox không ghi target/ ở worktree Orca); dòng lệnh Sơn gõ không có trong hồ sơ ngoài đoạn log dán.
  - Quá trình sửa: vòng 0 review rủi ro không lỗi chặn; vòng sửa 1 (N1, N2, N3, N5, N6, D22) review không lỗi chặn (`03_review_round1.md`); vòng sửa 2 (ab15b71) chỉ thêm test và sửa cột nguồn D22, Claude đọc diff nhưng không giao subagent review lại. Hết hạn mức 2 vòng. Copilot QA chạy một lần (trên 7ebe38b): không chạy được `verify` (bị từ chối quyền), chỉ ra thiếu test HTTP 401 cho N3 và thiếu test audit không lộ email/mật khẩu, cả hai đã bổ sung ở ab15b71.
AC (TASKS.md S04: "Khóa xong request mới với cookie cũ thất bại; thử nhiều session và server role; UI cần test tích hợp để đóng."):
  - Khóa xong, request mới với cookie cũ thất bại (401) và session bị xóa: đạt (AccountManagementIT.disableRevokesAllSessionsAndEnableRestoresLogin, freshStatusAndRoleAreEnforcedOnNextRequest).
  - Nhiều session: đạt (cùng test đầu: hai session của account đích chết, session admin còn sống; "account khác" mới chỉ là admin, chưa có ca nhiều account khác).
  - Server role: đạt (demotedAdminLosesAccessOnNextRequest trên MySQL: ADMIN hạ ACCOUNTANT thì request kế bị 403; CurrentAccountFilterTest.roleAndHouseholdComeFromDatabase; AccountRefreshApiTest.databaseRoleOverridesSessionRoleBeforeAuthorization). Đổi household_id: updatedHouseholdIsUsedImmediately, residentSeesNewHouseholdOnNextRequest.
  - Mở khóa thủ công, đăng nhập ngay, ba cột về 0/0/NULL, 409 theo D22: đạt (unlockClearsLockAndAllowsImmediateLogin, expiredLockOrFailedCountCanBeReset, conflictsAndMissingAccount; AccountManagementServiceTest).
  - 401/403, token chống giả mạo yêu cầu thiếu hoặc sai trên cả ba endpoint: đạt ở mức MockMvc với service mock (AccountApiTest.managementRoutes*); IT chỉ có ca hạ quyền thật.
  - Audit cùng transaction, không email hay mật khẩu: đạt (auditFailureRollsBackStatusAndSessions, managementAuditChangesContainNoEmailOrPassword, concurrentDisableHasOneWinner).
  - N3 (actor mất ACTIVE thì 401, không thao tác): đạt ở mức unit và MockMvc (AccountManagementServiceTest.inactiveActorCannotManageAccounts, AccountApiTest.inactiveActorDuringManagementReturnsHttp401); chưa có test đua thật.
  - UI cần test tích hợp để đóng: CHƯA. Theo hợp đồng đã duyệt (`_agents_workspace/S04/01_contract.md`) phần UI thuộc Kiệt; task này chỉ kiểm phía server.
Đầu ra giao cho: Kiệt (ba endpoint POST /api/v1/accounts/{id}/disable|enable|unlock, ADMIN, 204, 401 khi account bị vô hiệu giữa chừng; UI vô hiệu/bật lại/mở khóa); S03 (cơ chế hủy session theo principal để dùng khi đổi mật khẩu, và đọc lại status mỗi request); S05/S06 (householdId đọc lại từ DB mỗi request, Q3/Q4); Tuấn (review, test EP-01).
Assumption đã xác nhận (qua lời Sơn, 06/11/2026): Q1–Q6 của hợp đồng (vô hiệu = INACTIVE; ADMIN vô hiệu ADMIN khác được, tự vô hiệu và ADMIN ACTIVE cuối cùng thì 409; đọc lại status và household_id mỗi request; không hủy session hàng loạt khi hộ đổi; không migration; không mở khóa ADMIN); vòng sửa 1: N1, N2, N3, N4 (409 khi đã INACTIVE hoặc đã ACTIVE), N5/D22 (mở khóa đặt lại cả lock_level), N6; N7, N8 chấp nhận không sửa.
  CHƯA xác nhận: chưa có văn bản của Tuấn (người review S04) hay Kiệt về hợp đồng; chưa có xác nhận review ab15b71 bằng subagent.
Lỗi còn lại, điều chưa kiểm thử:
  - Chưa PR/CI; chưa chạy Windows hay máy thứ hai; chưa có test tích hợp UI (phần của Kiệt). Sơn đã chạy thử với backend thật (06/11/2026): đăng nhập, hộ, nhân khẩu, chuyển đi, vô hiệu, mở khóa qua giao diện.
  - R2: SQL thật đã thấy trong log IT: `for update of u1_0` (chỉ khóa users) và `for update of r1_0` (roles); loại giả định deadlock "khóa role qua join" ở đăng nhập. Chưa có test đua đăng nhập ADMIN với thao tác quản lý, nên chưa kết luận hết về deadlock.
  - N3 chỉ có test unit và MockMvc (service giả ném BadCredentialsException); không có test đua thật actor bị vô hiệu sau khi qua filter. Body 401 của nhánh GlobalExceptionHandler khác body của filter.
  - Cookie thật ngoài MockMvc: chưa kiểm Spring Session đặt lại cookie SESSION hết hạn trên server thật và trình duyệt thật.
  - Test 401/403 của ba endpoint mới chỉ ở slice với service giả; IT chỉ có ca hạ quyền thật.
  - N7: 409 dùng chung detail "Thông tin bị trùng" (có code riêng). N8: RESIDENT mất household_id vẫn giữ session (các endpoint hộ trả 403). R3: lock() không đọc lại role của actor. R6: locked_until hết hạn khác NULL cùng 0/0 trả 409, chỉ tạo được bằng SQL tay.
  - Worker vòng 1 ghi log verify lỗi ngoài worktree (/tmp/s04_verify.log), vi phạm nhẹ; có thể xóa.
  - Tiền nhiệm S01 đã tick.
Kết luận: tick theo bằng chứng chạy thật của Sơn (surefire 86 + failsafe 75 xanh, BUILD SUCCESS ở ab15b71 trên MySQL), hai lượt review rủi ro không còn lỗi chặn và vòng 2 chỉ thêm test, kèm các caveat trên; phần UI của AC thuộc Kiệt, chưa có; push/PR còn chờ cổng G2.
```

### S03 (tick 07/11/2026)
```text
Task: S03 (WBS 2.1.1.2, 2.1.2.1) — Đổi mật khẩu, hủy session, hồ sơ chỉ xem
Commit/PR: CHƯA push, CHƯA PR, chưa chạy CI. Worktree Orca feat-s03 (nhánh hhoangsonnw/feat-s03), nền origin/main d3d8de3 (sau PR #11), worktree sạch ở c7d0b89:
  c852c51 feat: đổi mật khẩu, hủy session và che mock email (S03)
  83f8682 fix: sửa theo review vòng 1 (S03)
  c7d0b89 fix: sửa theo review vòng 2 (S03)
Lệnh và kết quả:
  - Bằng chứng do Sơn chạy (07/11/2026 ~11:28 +07, JDK 21.0.9, MySQL Docker mysql:8.4 cổng 3307, worktree feat-s03 ở c7d0b89):
      cd backend && ./mvnw -B -ntp -Pintegration verify
      BUILD SUCCESS (50,978 s, kết thúc 11:28:59). Log Sơn dán: "Tests run: 80, Failures: 0, Errors: 0, Skipped: 0" (pha failsafe).
  - Verifier đối chiếu độc lập các báo cáo trong worktree: surefire 103 test (21 file) và failsafe 80 test (11 file), 0 failure, 0 error, 0 skipped; failsafe-summary completed=80; mỗi lớp *Test/*IT trong src/test có đúng một báo cáo; report đóng dấu 11:28:10–11:28:59, sau commit c7d0b89 (11:16:45), sau nguồn mới nhất (11:16:18) và sau lần biên dịch cuối (11:16:26), nên thuộc đúng c7d0b89 (không báo cáo nào còn mtime của các lần chạy lỗi 11:18–11:22). Không có dấu vết cờ bỏ qua test trong pom hay báo cáo. PasswordChangeIT 5 test, PasswordChangeServiceTest 7, PasswordChangeApiTest 5, LoginAttemptServiceTest 5, MustChangePasswordGuardTest 3, đều xanh.
  - Verifier và Claude KHÔNG chạy được Maven (sandbox không ghi target/ ở worktree Orca). Ba lần chạy trước của Sơn (11:18, 11:20, 11:22) lỗi do môi trường (JDK 25 bị enforcer từ chối; sai cổng MySQL: cổng 3306 là một mysqld khác trên máy, MySQL của dự án ở 3307), không do code. Dòng lệnh đầy đủ kèm biến DB_* Sơn gõ không có trong hồ sơ ngoài đoạn log dán.
  - Quá trình sửa: review rủi ro vòng 0 (lỗi chặn: D23 gắn sai nhãn và thiếu Q3/A3) và vòng sửa 1 (lỗi chặn: test GET/PUT xanh giả) do subagent độc lập; cả hai lỗi chặn đã sửa ở vòng 1 và vòng 2. Vòng sửa 2 (c7d0b89) chỉ Claude đọc diff, không có reviewer độc lập kiểm lại. Hết hạn mức 2 vòng. Copilot QA chạy một lần (trên c852c51): verify xanh theo Copilot, chỉ ra thiếu test cho LoginAttemptService sau refactor (đã bổ sung); kết quả là lời chưa kiểm chứng, không dùng làm bằng chứng tick.
AC (TASKS.md S03: "Kiểm tra mật khẩu cũ/mới, tối thiểu 8 ký tự; đổi xong các session cũ bị từ chối; DTO không lộ hash."):
  - Kiểm tra mật khẩu cũ: đạt (PasswordChangeServiceTest.wrongCurrentCountsAndAudits; PasswordChangeIT.incorrectOldPasswordCommitsCounterAndLocksNonAdmin; PasswordChangeApiTest.errorsNeverEchoSecrets, mã CURRENT_PASSWORD_INCORRECT 400).
  - Mật khẩu mới tối thiểu 8 ký tự (và theo D23: chữ hoa/thường Unicode, số, ký tự đặc biệt, tối đa 72 byte, không trùng mật khẩu hiện tại): đạt ở mức unit và slice (validatesInOrderWithoutCountingNewPasswordErrors, unicodeWhitespaceAndLengthBoundaries, weakPrecedesSame, errorsNeverEchoSecrets); IT trên MySQL chỉ chạy các mật khẩu mới hợp lệ, chưa có ca HTTP thật cho PASSWORD_WEAK/PASSWORD_TOO_SHORT.
  - Đổi xong các session cũ bị từ chối: đạt (PasswordChangeIT.threeCookiesAreRevokedAndTemporaryMailIsRedacted, MySQL, cookie thật: 3 session của một account, đổi bằng một cái, cả 3 cookie kể cả cookie đang gọi nhận 401 trên /auth/me; SPRING_SESSION không còn dòng của principal; 204 trả bình thường; mật khẩu cũ đăng nhập 401, mật khẩu mới 200 với mustChangePassword=false; session ADMIN khác không bị ảnh hưởng).
  - DTO không lộ hash: đạt (ChangePasswordRequest chỉ có 2 trường mật khẩu, toString che; 204 không body; lỗi không chứa mật khẩu, "passwordHash", "$2" (PasswordChangeApiTest); AuthApiTest (có sẵn) khẳng định /auth/me không có passwordHash/email).
  - Hợp đồng đã duyệt (D23): đếm và khóa khi sai mật khẩu cũ, ADMIN miễn (IT và unit); đang khóa thì 401 không đổi (IT, unit); thành công đặt lại bộ đếm (chỉ unit); audit mỗi lần sai chỉ id và số lần, không lộ mật khẩu (IT, unit); che mock_emails.body khi cờ từng TRUE, giữ recipient_email/subject/status, dòng account khác nguyên (IT); rollback toàn bộ khi audit lỗi (IT: hash, cờ, mock_emails, session nguyên, không có audit PASSWORD_CHANGED); hai request đổi đồng thời chỉ một thắng (IT, một lần chạy); luồng đầy đủ qua S02 mật khẩu tạm -> 403 PASSWORD_CHANGE_REQUIRED -> đổi -> đăng nhập lại (IT); 401 chưa đăng nhập, 403 thiếu/sai CSRF, mọi vai trò đổi được, userId/username trong body bị bỏ qua (slice, service mock); account không ACTIVE thì 401 (chỉ unit); log không chứa mật khẩu (IT); chéo hộ không áp dụng vì endpoint chỉ đổi cho chính principal; không migration.
  - UI (WBS 2.1.1.2, phần UI của 2.1.2.1): CHƯA. Theo hợp đồng (`_agents_workspace/S03/01_contract.md`) phần UI thuộc Kiệt; task này chỉ kiểm phía server.
Đầu ra giao cho: Kiệt (POST /api/v1/auth/change-password, mọi vai trò đã đăng nhập kể cả khi đang bị cờ, cần X-CSRF-TOKEN, body {currentPassword, newPassword}; 204 nghĩa là đã đăng xuất mọi nơi nên xóa state client, gọi lại GET /auth/csrf rồi về màn đăng nhập; 400 có code CURRENT_PASSWORD_INCORRECT, PASSWORD_TOO_SHORT, PASSWORD_TOO_LONG, PASSWORD_WEAK, PASSWORD_SAME_AS_CURRENT; 401 cũng là phản hồi khi đang bị khóa đăng nhập hoặc account không còn ACTIVE, và 401 do khóa KHÔNG có nghĩa session đã bị hủy; hồ sơ chỉ xem qua GET /auth/me; đọc `code` không đọc `detail`); Tuấn (review, test bảo mật EP-01 WBS 2.1.1.3 kiểm session sau đổi mật khẩu).
Assumption đã xác nhận (qua lời Sơn, 07/11/2026, ghi ở DECISIONS D23): Q1 (độ mạnh, chữ hoa/thường theo Unicode), Q2 (tối đa 72 byte UTF-8), Q3 (từ chối trùng mật khẩu hiện tại), Q4(a) (đếm và khóa như đăng nhập, ADMIN miễn; đang khóa chặn đổi; thành công đặt lại bộ đếm; mỗi lần sai ghi audit), Q5 (hủy hết session kể cả session đang dùng), A3 (đường dẫn, 204, 400 CURRENT_PASSWORD_INCORRECT), Q6 P0 (hồ sơ chỉ xem; chỉ ADMIN sửa full_name; users.full_name phải khớp residents.full_name, cách nối CHƯA CHỐT); người nhận Kiệt đồng ý (lời Sơn).
  CHƯA xác nhận: chưa có văn bản của Kiệt, Tuấn hay Quân; việc Kiệt đồng ý chỉ là lời Sơn.
Lỗi còn lại, điều chưa kiểm thử:
  - Race đăng nhập bằng mật khẩu cũ ngay trước lúc đổi: một login commit trước nhưng tạo session sau lệnh DELETE có thể sống sót; đóng được cần phiên bản mật khẩu hoặc cột mới (migration), nguồn không yêu cầu. Chưa có test; ghi ở ARCHITECTURE.
  - Vòng sửa 2 không có reviewer độc lập kiểm lại; Copilot QA chỉ chạy trên c852c51 và là lời chưa kiểm chứng.
  - UI chưa có (Kiệt). Chưa có văn bản của Kiệt/Tuấn/Quân.
  - Khi non-ADMIN tự khóa qua đổi mật khẩu, endpoint trả 401 nhưng không hủy session; ai giữ session sống vẫn dùng được các endpoint khác. Đánh đổi đã nêu ở Q4: kẻ giữ session sống có thể khóa đăng nhập của người thật.
  - Cách nối users↔residents cho yêu cầu full_name khớp nhau chưa chốt và không thuộc S03 (sẽ chạm S02, S05, có thể migration).
  - Test đồng thời chỉ chạy một lần; không có test đua login-trong-lúc-đổi. IT không phủ: account INACTIVE (chỉ unit), đặt lại bộ đếm khi thành công (chỉ unit), ca cờ FALSE không che mock email (chỉ unit), PASSWORD_WEAK/TOO_SHORT qua HTTP thật, "nhiều account khác" (chỉ một ADMIN khác).
  - Bên thua trong đua đổi đồng thời có thể bị tính một lần sai mật khẩu cũ vì mật khẩu đã đổi; chưa quyết có chấp nhận.
  - Chưa push/PR/CI trên remote; chưa chạy Windows hay máy thứ hai.
  - Code S03 dồn nhiều lệnh vào một dòng dài (service, test), khó đọc; chưa dọn.
  - Tiền nhiệm S01 đã tick.
Kết luận: tick theo bằng chứng chạy thật của Sơn (surefire 103 + failsafe 80 xanh, BUILD SUCCESS ở c7d0b89 trên MySQL), đã đối chiếu độc lập báo cáo đúng HEAD, kèm các caveat trên; phần UI của AC thuộc Kiệt, chưa có; push/PR còn chờ cổng G2.
```

### S06 backend (tick 08/11/2026, theo lời Sơn; caveat UI ở mục "S06 UI"): khai báo, duyệt, tra cứu tạm vắng
```text
Task: S06 (WBS 2.2.1.2–3), lượt backend; tick cùng S06 UI theo lời Sơn 08/11/2026 (AC "cuối cùng ghép UI" chưa kiểm trên Chrome)
Commit/PR: CHƯA push, CHƯA PR, chưa chạy CI. Worktree Orca feat-s06 (nhánh hhoangsonnw/feat-s06), nền main d3d8de3, sạch ở 7b9828e:
  82e5829 fix: hoàn thiện kiểm thử tạm vắng (S06)   (reset --soft main, gồm cả code vòng 0 ac06225)
  7b9828e fix: hủy đơn tạm vắng khi chuyển đi (S06)
Lệnh và kết quả:
  - Worker Codex: cd backend && ./mvnw -B -ntp verify (JDK 21.0.9): 93 test, 0 lỗi, BUILD SUCCESS; verify.log dòng đầu SHA 7b9828e.
  - Copilot QA độc lập (lời Copilot, chưa kiểm chứng) cùng SHA 7b9828e: 93 test, 0 lỗi.
  - Bằng chứng do Sơn chạy (07/11/2026 16:24 +07, worktree feat-s06 ở 7b9828e, JDK 21, MySQL cổng 3307): ./mvnw -B -ntp -Pintegration verify → failsafe "Tests run: 89, Failures: 0, Errors: 0, Skipped: 0", BUILD SUCCESS, 52 s. Claude đối chiếu failsafe-reports trong worktree: DeclarationIntegrationIT 14 test, ResidentIntegrationIT 7 test, 0 lỗi.
  - Claude và subagent KHÔNG chạy được Maven (sandbox không ghi target/ ở worktree Orca); verifier subagent không được gọi, Claude tự grep đối chiếu SHA, số test và cờ bỏ qua (`_agents_workspace/S06/04_evidence.md`).
  - Review rủi ro Opus 2 lần (ac06225, 7b9828e): đạt, không lỗi chặn. 2 vòng sửa (V7 đổi tên CHECK thành ck_rc_status_v2; test còn thiếu; move-out tự hủy đơn PENDING theo lời Sơn).
Hợp đồng và quyết định: `_agents_workspace/S06/01_contract.md`, DECISIONS D24 (lời Sơn 07/11/2026): chỉ tạm vắng; RESIDENT khai báo cho nhân khẩu trong hộ mình; ADMIN duyệt/từ chối/"Đã quay lại"; ngày bắt đầu không quá khứ; duyệt hiệu lực ngay; một đơn PENDING mỗi nhân khẩu; hủy bằng CANCELLED; move-out tự hủy đơn PENDING; thông báo chỉ là trạng thái và lý do từ chối qua /mine; V7.
AC (TASKS.md S06):
  - Chờ duyệt → duyệt/từ chối đúng quyền: đạt phía server (DeclarationApiTest 8 route 401/403/CSRF/PASSWORD_CHANGE_REQUIRED; DeclarationIntegrationIT trên MySQL).
  - Lưu trạng thái/lịch sử: đạt (V7, CANCELLED, dòng RETURN, ResidentIntegrationIT/DeclarationIntegrationIT).
  - Thông báo trong app/mock: đạt theo D24 (trạng thái và lý do từ chối qua GET /residency-declarations/mine).
  - Cuối cùng ghép UI: CHƯA (lượt UI sau; phần UI thuộc Kiệt).
Đầu ra giao cho: Tuấn (kiểm tra luồng duyệt và tra cứu) và Kiệt (8 route mới và trường thêm vào ResidencyChangeResponse). Cả hai đã đồng ý chỉ theo lời Sơn; Quân biết về V7 (lời Sơn). Chưa có văn bản.
Lỗi còn lại, điều chưa kiểm thử:
  - V7 chưa chạy trên bản sao dữ liệu thật của S05 (DB Sơn có thể không có dữ liệu); chưa chạy Windows/máy thứ hai; chưa CI trên remote.
  - Thiếu test: "quay lại" với nhân khẩu RESIDING; thoát %/_ của q và apartment ở hàng chờ; thứ tự lock vs truy vấn đơn PENDING ở moveOut chỉ đọc code.
  - N+1 ở danh sách (~200 truy vấn/trang 100 dòng); `cancellable` luôn false ở phản hồi tạo/hủy; q rỗng ở hàng chờ trả 400; đơn bị hủy do move-out chỉ có nguyên nhân trong audit.
  - Ảnh hưởng của TEMPORARILY_ABSENT tới phí (S08/S09) và báo cáo (S11) chưa có quy tắc.
  - Code worker dồn nhiều lệnh vào một dòng dài, khó đọc; chưa dọn.
Kết luận: backend đủ bằng chứng server và tick cùng S06 theo lời Sơn. Push/PR chờ cổng G2/G3.
```

### S06 UI (tick 08/11/2026, theo lời Sơn; CAVEAT: chưa kiểm Chrome với backend thật, M-31 phía cư dân chưa kiểm): khai báo, duyệt, quay lại, gợi ý tên tạm vắng (frontend)
```text
Task: S06 (WBS 2.2.1.2–3), lượt UI (phần của Kiệt), chế độ Backend thật `api`
Vòng sửa 3 (commit fa786147, bản ghi tại `_agents_workspace/S06/ui/02_impl.md`): worker báo vitest 13 file / 65 test đạt, tsc và build đạt; chưa kiểm Chrome, 360px, sáng/tối, backend thật (lời worker, chưa kiểm chứng ở mức trình duyệt).
Commit/PR: CHƯA push, CHƯA PR. Worktree Orca feat-s06-ui (nhánh hhoangsonnw/feat-s06-ui), xếp trên feat/s03-change-password (3ee97ca, chưa merge vào main), sạch ở 70c06c7:
  b78dadf feat: ghép giao diện khai báo tạm vắng (S06 UI)
  70c06c7 fix: bảo vệ biểu mẫu tạm vắng (S06 UI)
Lệnh và kết quả:
  - Worker Codex: cd frontend && npm ci && npx tsc -b && npm test && npm run build (Node 26.10.0): vitest 12 file / 51 test đạt, tsc không lỗi, build đạt; verify.log SHA 70c06c7.
  - Copilot QA độc lập (lời Copilot): cùng SHA, 12 file / 51 test, tsc và build đạt.
  - Claude đối chiếu SHA, số test; không chạy lại npm được (sandbox); verifier subagent không được gọi.
  - Review rủi ro Opus 2 lần: lần 1 có 1 lỗi chặn (dữ liệu mẫu lọt vào form ghi thật của M-31 ở chế độ api); vòng sửa 1 (70c06c7) đã sửa và thêm test; lần 2 đạt, không lỗi chặn.
AC "cuối cùng ghép UI": M-31, M-16, M-13 đã ghép API S06 ở mức mã và test đơn vị (fetch giả). CHƯA chạy trên Chrome với backend S06 thật, chưa kiểm 360px/screen reader.
Đầu ra giao cho: Kiệt (PR frontend riêng); Kiệt đồng ý chỉ theo lời Sơn.
Lỗi còn lại: lỗi cũ không tự xóa ở M-16/M-13; hộp M-16 mở sau quyết định thất bại; thiếu test Esc, đổi tên đăng nhập, api→demo→api, tải lại sau duyệt/từ chối thất bại; npm báo 3 vulnerabilities; README/PRODUCT còn dòng lỗi thời nhỏ.
Kết luận: tick S06 theo lời Sơn 08/11/2026 khi UI mới đủ bằng chứng ở mức mã và test đơn vị. Còn mở: kiểm Chrome với backend S06 thật (M-31 phía cư dân, M-16 gợi ý, 360px, sáng/tối) và lỗi còn lại ở trên. Nếu kiểm sau thấy lỗi, mở lại task này.
```

### S07 (tick 08/11/2026, theo lời Sơn): đơn giá phí và công thức
```text
Task: S07 (WBS 2.3.1.1, 2.3.1.3), backend; hợp đồng `_agents_workspace/S07/01_contract.md` (G1 duyệt 08/11/2026); quyết định D25 trong `docs/DECISIONS.md`.
Commit: nhánh `hhoangsonnw/feat-s07` (worktree Orca feat-s07), HEAD `f5eef71`: 4a03993 feat, f44fce1 test, 68aa41a sửa C1 (assertion CHECK), f5eef71 sửa W1 (thiếu tham số trả 400). Chưa PR.
Kiểm thử:
  - Unit: `./mvnw -B -ntp verify` (JDK 21) trên f5eef71: 122 test, 0 lỗi, BUILD SUCCESS; `verify.log` dòng đầu SHA f5eef71.
  - Integration: `./mvnw -B -ntp -Pintegration verify` trên f5eef71 với MySQL 8.4 (compose project `bluemoon`, V8 áp thành công): 97 test failsafe, gồm 3 test `FeeRateIntegrationIT` (CHECK, UNIQUE, race hai POST cùng tháng), BUILD SUCCESS. Sơn chạy 08/11/2026 12:42 +07.
Review: Opus `bluemoon-risk-reviewer` vòng 1: không lỗi chặn sau khi sửa C1 và W1. Lời agy trên `qa/s07-agy` (c188766, báo 137 test) CHƯA được chạy lại trên cùng SHA, không dùng làm bằng chứng.
Chưa kiểm / còn mở: D25 chưa commit vào nhánh; test QA của agy chưa đối chiếu; snapshot hóa đơn và thu thiếu/thừa thuộc S08/S09; PR và CI remote chưa có.
```

### S09 — thu tiền và gạch nợ (chưa tick; triển khai + automated QA trên `hhoangsonnw/s09-contract`)
```text
Contract: `_agents_workspace/S09/01_contract.md` và `02_ui_contract.md`; quyết định D27/D28 ở `docs/DECISIONS.md`.
Đầu ra: migration V10; receipt thu thiếu/đủ/thừa; credit FIFO; void/reverse; idempotency gắn actor; audit; API danh sách công nợ/lịch sử; UI Kế toán API thật + Demo và màn ADMIN chỉ đọc.
QA Orca/Agy trên đúng worktree (09/11/2026): `cd backend && ./mvnw -B -ntp verify` — 227 test pass; `-Pintegration verify` với MySQL 8.4 cô lập cổng 3308 — 146 integration pass; frontend `npm test -- --run` — 28 file/134 test pass; `npm run build` — TypeScript/Vite pass. Không reset DB/volume.
Đối chiếu coverage: thu thiếu/thừa, FIFO + đảo, concurrency/idempotency, quyền/privacy reason (`ReceiptAmountsTest`, `CreditAllocationAmountsTest`, `PaymentFlowIT`, `PaymentConcurrencyIT`, `PaymentApiTest`, `PaymentSchemaIT`); UI preview, retry cùng UUID/payload, đổi actor, ADMIN read-only, RESIDENT denial (`s09-page.test.tsx`, `s09-payments.test.ts`, `s09-csrf-actor.test.ts`, `s09-routes.test.tsx`).
Còn mở: chưa có browser E2E nối UI→API thật; chưa kiểm mobile/zoom/keyboard/screen reader; chưa CI remote. Chưa tick trước khi PR được review và CI đạt.
```
