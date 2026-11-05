# Audit schema `origin/feature/database-schema` so với docs

Ngày: 05/11/2026. Phạm vi: `db/01_schema.sql`, `02_seed.sql`, `03_test.sql`, `db/README.md` (Tuấn, commit `3eba2aa`) so với `docs/*.md`, `docs/sprint-1/*`, `docs/sources/*`, `AGENTS.md`. Cách làm: 3 agent đọc song song (docs/sources, ràng buộc docs và backend, audit đối kháng), `/llm-council` (5 advisor, 5 peer review, chairman), rồi tôi tự đối chiếu lại các trích dẫn bị nghi sai.

**Giới hạn:** chưa chạy SQL nào trên MySQL. Flyway chưa từng chạy thật trên MySQL trong repo (`docs/VALIDATION.md:29-30`). Mục nào ghi "kỹ thuật thuần" là suy luận từ hành vi MySQL/Hibernate, không có trong docs.

## Kết luận
**Chưa duyệt để implement nguyên trạng. Không merge cả bộ 11 bảng.**
1. `db/01_schema.sql:1,5` có `CREATE DATABASE` và `USE`, và không nằm trong Flyway: trái `AGENTS.md:22`; `application.yml:13,18-19` đã bật Flyway/validate; compose tạo DB bằng `MYSQL_DATABASE` (`compose.yaml:6-8`). `DECISIONS.md:12` (D06) vẫn ghi "Đề xuất", nên căn cứ cứng là `AGENTS.md:22`.
2. `ARCHITECTURE.md:13`: bảng nghiệp vụ chỉ tạo trong task có AC đã chốt. Bộ 11 bảng tạo một lần, trong khi nhiều quy tắc chưa chốt (`DECISIONS.md:20-23`) và trường dữ liệu do F01 xác nhận (`REQUIREMENTS.md:27`; `TASKS.md` F01).

Nền tảng tốt: tiền dùng `DECIMAL` (không float), `charge_details` lưu số lượng và đơn giá đã áp (`ARCHITECTURE.md:31`; `REQUIREMENTS.md:17`), `fee_rates` có hiệu lực theo ngày kèm CHECK, UNIQUE `(household_id, billing_period_id)` chặn trùng kỳ theo hộ, người chuyển đi vẫn được giữ lịch sử, `username` 100 ký tự khớp `SPRING_SESSION.PRINCIPAL_NAME`, số liệu seed cộng đúng (350k+350k=700k; 490k+490k=980k).

Nguồn trong `docs/sources` được trích theo ID (PBI-xxx trong `SE_10_W02-ProductBacklog.txt`, task WBS trong `WBS_EXECUTION.csv`), không theo số dòng, vì ô nhiều dòng làm số dòng không đáng tin.

## A. Lỗi có căn cứ docs
| # | Lỗi | Vị trí | Căn cứ |
|---|---|---|---|
| A1 | Không phải Flyway; `CREATE DATABASE`/`USE`; README hướng dẫn `mysql < file`, người mới sẽ tạo DB `bluemoon` song song | `01_schema.sql:1,5`; `db/README.md` | `AGENTS.md:22`; `application.yml:13,18-19`; `compose.yaml:6-8` |
| A2 | Không có bảng audit | toàn schema | `ARCHITECTURE.md:18,41`; `REQUIREMENTS.md:36`; `TASKS.md` S01, S05, S09 |
| A3 | `residency_changes.status` mặc định `APPROVED` | `01_schema.sql:87` | `REQUIREMENTS.md:14` (PBI-007: form hợp lệ vào trạng thái chờ duyệt), `:15` (PBI-008: duyệt/từ chối) |
| A4 | Seed tài khoản `admin.demo`/`accountant.demo` với hash giả, trạng thái ACTIVE | `02_seed.sql:8-11` | `AGENTS.md:26,28`; `DECISIONS.md:20` |
| A5 | `VEHICLE`/`METER`, seed `MOTORBIKE_PARKING` | `01_schema.sql:103-109`; `02_seed.sql:49` | `REQUIREMENTS.md:46` (phí xe/tiện ích là V2); WBS 3.1.1.1 |
| A6 | Schema ngầm chọn quy tắc thu tiền: cho thu một phần (`PARTIALLY_PAID`), cấm thu thừa (`CHECK paid_amount <= total_amount`), không có hủy/đảo (`amount > 0`, không trạng thái) | `01_schema.sql:154,164-166,207` | `DECISIONS.md:22`; `REQUIREMENTS.md:20,50`; `TASKS.md` S09; WBS 2.4.1.1 ("chốt xử lý thu thiếu, thu thừa, hủy/sửa") |
| A7 | Khoản tự nguyện nằm trong `household_charges` với trạng thái `UNPAID`; không ràng buộc `period_type` với `fee_category`; mỗi hộ một khoản mỗi đợt; README nói "kept separate" nhưng schema không tách | `01_schema.sql:148-167`; `02_seed.sql:73-75` | `REQUIREMENTS.md:18`; `DECISIONS.md:23`; PBI-012 (hiện lên app nhưng không cộng vào Công nợ) |
| A8 | `billing_periods` thiếu hạn nộp | `01_schema.sql:134-146` | `REQUIREMENTS.md:28` |
| A9 | `charge_details` không UNIQUE theo loại phí: cùng loại phí lặp được trong một khoản phải thu | `01_schema.sql:169-189` | `TASKS.md` S08 ("không trùng kỳ/hộ/loại"); WBS 2.3.1.2 ("không tạo trùng kỳ") |
| A10 | Không khóa chống thu trùng: `reference_code` cho phép NULL nên UNIQUE không chặn tiền mặt; không cột `version` để chặn ghi đồng thời | `01_schema.sql:198,202` | `AGENTS.md:20`; WBS 2.4.1.1 ("gửi lặp không tăng số thu"); `ARCHITECTURE.md:40` (409 xung đột phiên bản) |
| A11 | `paid_amount`/`status`/`total_amount` chỉ có CHECK `paid <= total`; trạng thái `PAID` với `paid < total` vẫn ghi được; không ràng buộc khớp `payments`/`charge_details` | `01_schema.sql:152-166` | `REQUIREMENTS.md:20` (cập nhật nợ đúng số tiền trong transaction) |

## B. Cần nhóm quyết định (không mã hóa vào patch)
| # | Điểm | Căn cứ chưa chốt |
|---|---|---|
| B1 | `users` thiếu email; `username` có thể là email hay không | PBI-001 (AC dùng email; ghi chú "không quy định thêm trường dữ liệu cụ thể nếu chưa được chốt"); `REQUIREMENTS.md:9` |
| B2 | `users` không liên kết với hộ/cư dân; mapping account–hộ | `DECISIONS.md:20`; `REQUIREMENTS.md:14,50` |
| B3 | `roles` chỉ ADMIN và ACCOUNTANT; vai trò thứ ba/bốn tên lệch giữa PBI-003 ("Bên liên quan") và NFR ("Cơ quan chức năng") | `DECISIONS.md:20` |
| B4 | Ba cơ chế chuyển đi chồng nhau: `residency_status = MOVED_OUT`, `is_deleted`, `residency_changes` | `REQUIREMENTS.md:16` cho phép "trạng thái/xóa mềm" |
| B5 | `identity_number` UNIQUE tính cả người đã chuyển đi/xóa mềm | quy tắc trùng chưa chốt; `AGENTS.md:28` (PII) |
| B6 | Precision/rounding của `DECIMAL(12,2)`; `quantity × unit_price` có thể tới 4 chữ số lẻ trong khi `amount` 2 | `DECISIONS.md:22`; `AGENTS.md:19`; ví dụ giá lệch: WBS 2.3.1.1 (7.000) và PBI-011 (10.000) |
| B7 | Hiệu lực đơn giá; `fee_rates` không chặn hai đơn giá chồng ngày | `DECISIONS.md:22` |
| B8 | Brute-force/mật khẩu tạm | `DECISIONS.md:21` |
| B9 | Gói dữ liệu cho cơ quan chức năng cần bảng riêng; thông báo tạm vắng ở v1 (PBI-008) hay V2 | `ARCHITECTURE.md:37`; PBI-008 vs WBS 3.x (thông báo V2) |

## C. Kỹ thuật thuần (chưa chạy thử; không đưa vào patch bắt buộc)
| # | Lỗi | Vị trí | Kịch bản |
|---|---|---|---|
| C1 | `charge_details.fee_rate_id` không bắt cùng `fee_type_id` | `:173,184-185` | Dòng phí dịch vụ trỏ vào đơn giá gửi xe, vẫn được chấp nhận |
| C2 | Race hai kế toán cùng thu một khoản | `:152-153,164-166,203-204` | Lost update nếu service đọc `paid_amount` rồi ghi giá trị tuyệt đối; deadlock nếu INSERT `payments` trước khi khóa dòng charge. Cách tránh ở service: `SELECT ... FOR UPDATE` dòng charge trước, rồi `UPDATE ... SET paid_amount = paid_amount + ?` |
| C3 | `head_resident_id` có thể trỏ người hộ khác; chủ hộ chuyển đi vẫn là `head_resident_id`; hộ rỗng vẫn ACTIVE | `:37,72-74` | `UPDATE households SET head_resident_id = 3 WHERE id = 1` chạy được dù người 3 thuộc hộ 2 |
| C4 | `DATETIME(0)` làm tròn nửa giây | `:23,24,39,40,62,63,89,123,142,156,196` | `paid_at = '2026-11-30 23:59:59.7'` thành `'2026-12-01 00:00:00'`, sang ngày/kỳ sau |
| C5 | `UNIQUE (household_id, billing_period_id)` chặn lập lại hóa đơn sau khi `CANCELLED` | `:154-155,158` | Lập lại khoản phải thu cho cùng hộ và kỳ báo lỗi 1062 |
| C6 | `residency_changes` thiếu `household_id` | `:76-96` | Người đổi hộ làm mất "ai ở hộ nào lúc nào" (US-012: người, ngày đến, ngày đi) |
| C7 | `fee_rates` sửa tại chỗ làm `fee_rate_id` mất ý nghĩa; không ràng buộc `amount = quantity*unit_price`, `total = SUM(amount)` | `:119,152,173-189` | Sửa `unit_price` sau khi lập hóa đơn |
| C8 | `DEFAULT CURRENT_TIMESTAMP` theo múi giờ session, ứng dụng ghi UTC | `:23,24,...` | Cùng cột lệch 7 giờ giữa dòng do DB điền và dòng do app điền (`application.yml:7,17` đặt UTC) |
| C9 | Thông báo lỗi trùng UNIQUE chứa nguyên giá trị CCCD | `:65` | Nếu app log hoặc trả `ex.getMessage()` thì lộ PII (`AGENTS.md:28`) |
| C10 | `02_seed.sql` không có transaction, không idempotent (chạy lại báo lỗi 1062); payment 3 đặt ngày 06/11, tương lai | `02_seed.sql:93` | |
| C11 | `03_test.sql`: T1 và T3 chỉ SELECT; T5 chỉ assert cho `charge_id = 1`; không có ca âm; `DELIMITER` là lệnh của client mysql nên ScriptUtils/Testcontainers lỗi; `SIGNAL` làm script dừng và bỏ lại procedure | `03_test.sql` | |
| C12 | Collation `utf8mb4_unicode_ci` không phân biệt dấu/hoa thường: tên loại phí và `reference_code` trùng nhau theo collation | `01_schema.sql:3,13,26,113,202` | docs không nêu collation |
| C13 | FK vòng `households`↔`residents` chặn `DROP`/`TRUNCATE` | `:37,72-74` | |
| C14 | `idx_household_charges_status` cardinality thấp; thiếu `(billing_period_id, status)` | `:159` | NFR-004 (≤3 giây, 10.000 hồ sơ) chưa đo |
| C15 | ENUM với `ddl-auto=validate` chưa chạy thử | cả schema | một số bản Hibernate 6 báo lệch kiểu |

## D. Đã rút hoặc hạ mức so với báo cáo vòng 1
- **Rút:** "thiếu ngày kết thúc tạm vắng". Nguồn ghi ngày bắt đầu/kết thúc tạm vắng là chi tiết chưa có.
- **Hạ:** "thiếu người duyệt" ở `residency_changes`. `REQUIREMENTS.md:15` chỉ đòi "trạng thái/lịch sử được lưu"; actor nằm ở audit (`ARCHITECTURE.md:41`).
- **Sửa trích dẫn:** email dựa vào PBI-001, không phải task email trong WBS; khóa chống thu trùng dựa vào WBS 2.4.1.1 và `AGENTS.md:20`, không phải PBI-015.

## E. Patch đề xuất
Hai migration nháp, mỗi file một task (theo `ARCHITECTURE.md:13`), chờ F01 xác nhận trường dữ liệu:
- `V2__identity_audit.sql` (S01): `roles`, `users`, `audit_log`.
- `V3__residency.sql` (S05): `households`, `residents`, `residency_changes`.

So với bản Tuấn:
| Giữ | Đổi | Bỏ |
|---|---|---|
| Tên bảng và cột của `roles`, `users` (trừ `status`), `households`, `residents`, `residency_changes`; `UNIQUE` mã hộ/căn hộ/username; CHECK diện tích > 0 | `status` thành VARCHAR + CHECK; `DATETIME(6)` không default; thêm `residency_changes.household_id`; `residency_changes.status` mặc định `PENDING`; BIGINT có dấu; thêm `audit_log` | `CREATE DATABASE`/`USE`; `head_resident_id`; `is_deleted`; UNIQUE `identity_number`; ENUM; toàn bộ seed; bảng phí, hóa đơn, thu tiền (để migration của S07/S08/S09) |

**Checklist cho migration phí/thu tiền (V4, chưa viết):** chỉ viết sau khi chốt rounding và quy tắc thu thiếu/thừa/hủy (`DECISIONS.md:22`). Cần: `due_date` (A8); UNIQUE `(household, period, fee_type)` (A9); khóa idempotency (A10; có NOT NULL hay không là câu hỏi); cột `version`; FK ghép `(fee_rate_id, fee_type_id)` (C1); `DATETIME(6)` (C4); tách đợt tự nguyện (A7); service khóa dòng charge trước khi INSERT/UPDATE (C2); không mã hóa A6 cho tới khi nhóm chốt.

## F. Kiểm chứng
Tĩnh (đã chạy ngày 05/11/2026): `grep -inE 'create database|^ *use |insert|password'` trên `V2`/`V3` chỉ khớp tên cột `password_hash`; không `ENUM`, không `UNSIGNED`, không `DEFAULT CURRENT_TIMESTAMP`, không `ON UPDATE`; 6 bảng, mọi FK trỏ tới bảng được tạo trước (`roles`, `users`, `households`, `residents`). Đây chỉ là kiểm tra văn bản, không phải kiểm tra cú pháp MySQL.
Động (Sơn chạy; cần MySQL 8.4 thật và `DB_*` giả; không dùng MySQL Homebrew có dữ liệu khác): đặt hai file vào `backend/src/main/resources/db/migration/` trên bản sao, chạy `cd backend && ./mvnw -B -ntp -Pintegration verify` với `ddl-auto=validate`. Việc đầu tiên: chạy `-Pintegration` với V1 một mình để có bằng chứng nền.
