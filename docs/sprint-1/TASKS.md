# Checklist Sprint 1

Mã F/S là task kỹ thuật trong harness, không phải mã WBS mới. S/M/L chỉ độ phức tạp. Toàn bộ chưa hoàn thành cho tới khi có bằng chứng; file skeleton được tạo không đồng nghĩa F00/F02 đã nghiệm thu.

| Xong | Mã | Cỡ | Người phụ trách | Công việc / WBS | Tiền nhiệm / điều kiện | Acceptance criteria |
|---|---|---|---|---|---|---|
| ☑ | F00 | S | Kiệt | Repo, wrapper, hướng dẫn, CI; WBS 1.2.1.2 | Không | Clone/build được; repo có AGENTS, PR checklist và CI; xác nhận main sau merge. |
| ☑ | F01 | M | Sơn + Quân + Tuấn | Chốt contract, quyền và dữ liệu; WBS 1.2.1.1–3 | F00 | Ma trận quyền/account–hộ, trường dữ liệu và hợp đồng API có người nhận xác nhận; giải quyết mapping sai. |
| ☑ | F02 | M | Sơn + Tuấn | Nền tảng backend/DB và luồng mẫu; WBS 1.2.1.2 | F00 | Health/metadata, security boundary, Flyway/session chạy MySQL; teammate cài lại và có kết quả integration. |
| ☑ | S01 | M | Sơn; Tuấn review | Identity/session/role/audit; WBS 2.1.1.1, 2.1.1.3 | F01,F02 | Đăng nhập đúng/sai, đổi session ID, 401/403, audit write; chốt và test chống brute-force, không mặc định đã có. |
| ☐ | S02 | M | Sơn + Kiệt | Cấp account và email mô phỏng; WBS 2.1.1.1,2.1.2.3 | S01 | Email trùng bị từ chối; mock email đúng người; bootstrap BQT và cấp cư dân đã thống nhất; không log password. |
| ☐ | S03 | M | Kiệt + Sơn | Đổi mật khẩu, hồ sơ; WBS 2.1.1.2,2.1.2.1 | S01 | Kiểm tra mật khẩu cũ/mới, tối thiểu 8 ký tự; đổi xong các session cũ bị từ chối; DTO không lộ hash. |
| ☐ | S04 | M | Sơn + Tuấn | Vô hiệu account và test EP-01; WBS 2.1.2.2,2.1.1.3 | S01 | Khóa xong request mới với cookie cũ thất bại; thử nhiều session và server role; UI cần test tích hợp để đóng. |
| ☐ | S05 | L | Sơn + Tuấn | Hộ/nhân khẩu/biến động; WBS 2.2.1.1 | F01,S01 | CRUD/tra cứu, ownership; chuyển đi xóa mềm và audit/lịch sử; validate trường đã chốt, test hộ khác bị chặn. |
| ☐ | S06 | L | Kiệt + Sơn + Tuấn | Khai báo/duyệt/tra cứu; WBS 2.2.1.2–3 | S05 bản API đầu | Chờ duyệt → duyệt/từ chối đúng quyền; lưu trạng thái/lịch sử, thông báo trong app/mock theo AC; cuối cùng ghép UI. |
| ☐ | S07 | M | Quân + Kiệt; Tuấn kiểm tra | Đơn giá/công thức; WBS 2.3.1.1,2.3.1.3 | F01,F02 | BigDecimal; 50m² × 10.000 = 500.000; hiệu lực/rounding đã chốt; không áp thay giá vào hóa đơn cũ. |
| ☐ | S08 | L | Quân + Tuấn | Kỳ thu/tự nguyện; WBS 2.3.1.2–3 | S07; S05 để ghép cuối | Không trùng kỳ/hộ/loại; tự nguyện không cộng nợ; giao dữ liệu thật cho thu tiền và đối chiếu độc lập. |
| ☐ | S09 | L | Quân + Sơn | Ghi nhận thu tiền/gạch nợ; WBS 2.4.1.1 | S05,S08 | Thu đúng số tiền, nợ giảm đúng, transaction/audit; duplicate/concurrent không thu hai lần; chốt thu thiếu/thừa/sửa/hủy. |
| ☐ | S10 | M | Kiệt + Quân | API hóa đơn/lịch sử cho cư dân; WBS 2.4.1.2 | S08; S09 để ghép cuối | Chỉ dữ liệu hộ của account; chi tiết thành phần/kỳ/nợ đúng; pagination; thay householdId không xem được hộ khác. |
| ☐ | S11 | M | Tuấn + Quân + Kiệt | Export thu phí/cư trú; WBS 2.4.1.3 | S06,S09 | Excel mở được và tổng khớp; mẫu/cột/tỷ lệ đã duyệt; không export dữ liệu thừa; dùng dữ liệu thật từ DB. |
| ☐ | S12 | M | Tuấn + Sơn | Gói dữ liệu cơ quan; WBS 2.4.1.4, PBI-023 | S11 | Chỉ gói/trường BQT phê duyệt; cơ quan không truy DB thô; có test deny trường/hộ ngoài phạm vi. |
| ☐ | S13 | M | Tuấn; Sơn hỗ trợ | Backup tự động/restore, NFR-008; gắn DoD 2.4.1.4 | F02; dữ liệu mẫu S09 | Job backup có lịch/retention được chốt; restore vào DB thử riêng rồi đối chiếu; không xóa DB gốc. |
| ☐ | S14 | L | Cả nhóm; Tuấn điều phối | Hồi quy/quyền/hiệu năng nền; WBS 2.4.1.4 | S02–S13 hoàn tất; có thể chuẩn bị trước | E2E hộ→kỳ→thu→báo cáo; ca sai/IDOR/duplicate; 10k và thời gian có cấu hình đo; sửa và test lại. |
| ☐ | S15 | S | Cả nhóm; Kiệt tổng hợp | Đóng gói/bàn giao v1; WBS 2.4.1.4 | S14 | Cài từ máy sạch, dữ liệu giả, hướng dẫn, kết quả test và lỗi còn lại có PO chấp nhận; không còn blocker. |

## Checklist có thể tick

- [x] F00 — Repo, wrapper, hướng dẫn, CI; WBS 1.2.1.2
- [x] F01 — Chốt contract, quyền và dữ liệu; WBS 1.2.1.1–3
- [x] F02 — Nền tảng backend/DB và luồng mẫu; WBS 1.2.1.2
- [x] S01 — Identity/session/role/audit; WBS 2.1.1.1, 2.1.1.3
- [ ] S02 — Cấp account và email mô phỏng; WBS 2.1.1.1,2.1.2.3
- [ ] S03 — Đổi mật khẩu, hồ sơ; WBS 2.1.1.2,2.1.2.1
- [ ] S04 — Vô hiệu account và test EP-01; WBS 2.1.2.2,2.1.1.3
- [ ] S05 — Hộ/nhân khẩu/biến động; WBS 2.2.1.1
- [ ] S06 — Khai báo/duyệt/tra cứu; WBS 2.2.1.2–3
- [ ] S07 — Đơn giá/công thức; WBS 2.3.1.1,2.3.1.3
- [ ] S08 — Kỳ thu/tự nguyện; WBS 2.3.1.2–3
- [ ] S09 — Ghi nhận thu tiền/gạch nợ; WBS 2.4.1.1
- [ ] S10 — API hóa đơn/lịch sử cho cư dân; WBS 2.4.1.2
- [ ] S11 — Export thu phí/cư trú; WBS 2.4.1.3
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
Kết luận: tick theo bằng chứng chạy thật của Sơn (hai lần -Pintegration xanh ở +07 và UTC) và review rủi ro không còn lỗi chặn, kèm các caveat trên; Tuấn review và push/PR còn chờ cổng G2.
```
