# Phần 1 — Hiểu dự án và giới hạn

BlueMoon số hóa hồ sơ hộ/nhân khẩu, khoản thu theo diện tích, đóng góp tự nguyện, ghi nhận thu tiền, công nợ và báo cáo. Các role backend hiện có: BQT, Kế toán, Cư dân. Cơ quan chức năng là bên nhận dữ liệu ngoài hệ thống trong S12; quyết định có thêm role/account cơ quan vào v1 sau S12 vẫn mở theo D13. Không đồng nhất vai trò nghiệp vụ với quyền truy cập của thành viên GitHub.
Đã đọc 10 file SE_10 ở Drive, 6 file kế hoạch revised và workbook constraint mới nhất. Kiểm kê, xung đột và nguồn trong SOURCE_REVIEW. Repo hiện có README và .github/CODEOWNERS, chưa có mã ứng dụng.
User chọn Java/Spring, MySQL, server session, monorepo và WBS mới làm phạm vi v1. Các quy tắc còn thiếu trong DECISIONS; không tự chốt chỉ từ mã ID sai.

## Stack
Java 21 + Spring Boot 4.0.9 / Web MVC: đồng bộ, dễ theo dõi cho nhóm mới; phương án thay thế Boot 3.5 nếu giảng viên/template yêu cầu. Pin phiên bản, không dùng SNAPSHOT của framework.
MySQL 8.4 + JPA/Hibernate + Flyway: phù hợp dữ liệu quan hệ/transaction; thay thế JDBC thuần cho truy vấn báo cáo, không đổi DB chỉ để test.
Spring Security + JDBC session: đáp ứng hướng thu hồi phiên, không thêm Redis; JWT chỉ dùng nếu có lý do và thiết kế revocation.
JUnit/MockMvc + integration MySQL: test quyền và SQL thật; Testcontainers có thể bổ sung sau. Docker Compose: local dễ đồng nhất; không cần cloud/k8s.

# Phần 2 — Mục tiêu và Definition of Done

## Hai mức bàn giao
**Foundation lần này:** skeleton chạy, health/metadata, migration hạ tầng session, deny-by-default, unit/API tests và integration test có thể chạy trên MySQL, wrapper/CI/hướng dẫn. Không business logic.
**Sprint 1/v1 đích:** toàn bộ phạm vi WBS v1 chạy local và tích hợp với frontend; chỉ hoàn thành khi demo và bằng chứng test đạt, không phải khi có folder hoặc endpoint giả.

## Sprint 1 DoD
- PBI-001–004, 006–008, 010–013, 015, 021–023 có luồng chạy được và test theo AC đã thống nhất; không thiếu audit chỉ vì PBI-004 nói khóa account.
- Role và ownership enforced ở server, có 401/403/cross-household tests; đổi mật khẩu/khóa account vô hiệu phiên cũ.
- Hộ → diện tích/đơn giá → kỳ thu/hóa đơn → thu tiền → giảm nợ → báo cáo khớp dữ liệu mẫu độc lập.
- Tự nguyện không tự cộng công nợ; không tạo trùng kỳ/thu tiền trùng; migration versioned, lịch sử và audit được giữ.
- Export Excel đọc được, đúng tổng/cột đã duyệt; cơ quan không nhận dữ liệu ngoài phạm vi.
- Có backup định kỳ và restore thử; không dùng volume làm bằng chứng backup. Retention chưa chốt thì không purge.
- CI unit + MySQL integration xanh; cài lại từ hướng dẫn trên máy thành viên khác; có dữ liệu giả và biên bản demo.
- Ghi baseline hiệu năng ≤3s/10k theo NFR-004 và cấu hình đo; hoàn thiện đo toàn hệ thống ở v2 theo WBS. Các NFR không thuộc backend vẫn do cả nhóm chịu trách nhiệm.
- Không còn lỗi chặn luồng/quyền/số tiền; lỗi nhỏ còn lại ghi rõ và PO/PM chấp nhận.

## Thứ tự, phụ thuộc và đầu ra
1. F00–F02: repo/hợp đồng yêu cầu/nền tảng; không cần schema nghiệp vụ để dựng health/CI.
2. S01–S04: auth, role, audit, cấp/đổi/khóa account và mock email. Sơn giao hợp đồng cho Kiệt; Tuấn chuẩn bị ca test song song, đóng test khi API/UI đầy đủ.
3. S05–S06: schema hộ/cư trú, CRUD/khai báo/duyệt; UI dựng bằng contract trước, ghép API sau. Quân và Tuấn cùng xác nhận hộ/diện tích dùng cho phí.
4. S07–S08: công thức phí có thể làm trên dữ liệu chuẩn sau schema; kỳ thu hoàn chỉnh phải ghép hộ. Đề xuất đóng kỳ thu/test phí 11/12 thay 09/12 để nhận backend hộ cuối.
5. S09–S10: thu tiền/công nợ và API cư dân xem đúng hộ; cần hộ + kỳ thu; kiểm tra duplicate/transaction và số tiền.
6. S11–S12: báo cáo và chia sẻ gói đã duyệt; chỉ cần dữ liệu/API đúng, không có phụ thuộc kỹ thuật bắt buộc vào màn hình xem hóa đơn.
7. S13–S15: backup, hồi quy, đo hiệu năng nền, sửa/test lại và đóng gói. Có thể soạn ca thử/hướng dẫn sớm; bản bàn giao cuối chờ đủ đầu ra.

## Nguồn lực và phân công
Foundation nằm trong công việc chuẩn bị WBS 1.2.1.1–3 (32h cả nhóm, gồm cả UI), không cộng thêm 32h backend. V1 98h là giờ kỹ thuật của cả nhóm, không phải riêng backend. S/M/L trong TASKS là độ phức tạp tương đối; không quy đổi thẳng thành giờ.
Giả định 8h/người/tuần, trừ họp/tài liệu/dự phòng còn 4,4h kỹ thuật; mốc v1 12/01/2027 là dự báo, chưa trừ thi/nghỉ. Phân bổ từ workbook không phải lịch bàn giao theo ngày đã duyệt.
Ưu tiên sửa/tích hợp v1 trước mở rộng v2 nếu cùng người bị tranh thời gian. Khi task >1 buổi làm mà chưa có đầu ra, chia checklist và đo lại effort thay vì ép lịch.

## Rủi ro và giảm thiểu
| Rủi ro | Giảm thiểu / người giữ |
|---|---|
| Trace sai mã/khác phạm vi | SOURCE_REVIEW, dùng PBI theo nghĩa nghiệp vụ và WBS v1; Sơn/PM xác nhận |
| Account cư dân chưa có luồng cấp | Chốt mapping account–household và bootstrap BQT trước S02; Sơn |
| Thu trùng/sai tiền | BigDecimal, snapshot giá, transaction + unique/idempotency, concurrency tests; Quân |
| Lộ dữ liệu hộ | Scope server và projection/report whitelist, cross-household tests; Sơn/Tuấn |
| Hợp đồng API đổi liên tục | Contract trước triển khai, báo breaking change trong PR; Kiệt/Sơn |
| Nhóm mới / khung giờ ít | Luồng nhỏ chạy được, log giờ học/code/fix; hiệu chỉnh sau foundation; PM |
| Chưa chạy Docker trong môi trường soạn | Chạy CI/MySQL và quickstart trên máy nhóm trước merge; Tuấn |
| Nhầm mock với nghiệp vụ giả | Chỉ mô phỏng kênh ngoài, DB/giao dịch/report nội bộ chạy thật; cả nhóm |

# Phần 3 — Dùng harness trong Codex

Prompt khởi đầu:
```text
Đọc AGENTS.md và docs/sprint-1/TASKS.md. Chỉ thực hiện task [MÃ].
Trình bày đầu vào đã có, assumption còn thiếu và file dự kiến sửa.
Không mở rộng phạm vi hoặc tick task thiếu bằng chứng. Chạy kiểm tra phù hợp,
cập nhật TASKS/VALIDATION và báo cáo API/schema thay đổi cùng đầu ra bàn giao.
```
Nếu thiếu quyết định ảnh hưởng dữ liệu/quyền: hỏi đúng phần đó, giữ task bị chặn, tiếp tục phần độc lập. Chưa đủ căn cứ tuyên bố confidence ≥95% cho toàn Sprint 1.
