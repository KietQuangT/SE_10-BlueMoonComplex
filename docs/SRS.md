# Software Requirements Specification (SRS)

**Dự án:** SE_10 — BlueMoon Complex  
**Phiên bản:** 0.1 — Draft for Review  
**Ngày:** 06/11/2026  
**Trạng thái:** Chưa phê duyệt baseline

## 1. Mục đích và phạm vi

SRS chuyển các yêu cầu nghiệp vụ trong [BRD](./BRD.md) thành yêu cầu hệ
thống có thể thiết kế, lập trình và kiểm thử. Tài liệu này chỉ đặc tả
phạm vi v1 đã chọn; nội dung `TBD` hoặc `Blocked` phải được quyết định
trước khi trở thành acceptance criteria.

Hệ thống là ứng dụng web Spring Boot/Java 21, lưu dữ liệu trên MySQL 8.4,
sử dụng session server-side, phục vụ Ban quản trị, Kế toán, Cư dân và
người nhận dữ liệu được phê duyệt.

## 2. Tài liệu tham chiếu

- [BRD.md](./BRD.md)
- [REQUIREMENTS.md](./REQUIREMENTS.md)
- [ARCHITECTURE.md](./ARCHITECTURE.md)
- [DECISIONS.md](./DECISIONS.md)
- [SOURCE_REVIEW.md](./SOURCE_REVIEW.md)
- [uc1.drawio](../uc1.drawio)
- [uc2.drawio](../uc2.drawio)
- [uc3.drawio](../uc3.drawio)

## 3. Mô tả tổng quan

### 3.1. Actor

| Actor | Quyền/nhiệm vụ trong v1 |
|---|---|
| ADMIN/Ban quản trị | Quản lý account, hộ/nhân khẩu, duyệt cư trú, cấu hình phí, báo cáo |
| ACCOUNTANT/Kế toán | Cấu hình phí/kỳ thu, ghi nhận thanh toán, xem báo cáo tài chính |
| RESIDENT/Cư dân | Xem dữ liệu của hộ được gắn, gửi khai báo, xem hóa đơn |
| Cơ quan chức năng | Chỉ nhận gói dữ liệu đã phê duyệt và giới hạn trường |

### 3.2. Kiến trúc logic

```text
Client
  -> Security/session/CSRF
  -> REST controller + DTO
  -> Application service + transaction
  -> Repository
  -> MySQL (business data, audit, session)
```

Controller không gọi repository trực tiếp. Entity không được trả thẳng ra
API. Các thao tác ghi có audit phải ghi thay đổi và audit trong cùng
transaction.

### 3.3. Quy ước chung

- API prefix: `/api/v1`.
- JSON field: `camelCase`; SQL: `snake_case`.
- Danh sách phải phân trang, page size tối đa đề xuất là 100.
- Ngày nghiệp vụ dùng `LocalDate`; thời điểm audit dùng UTC `Instant`.
- Tiền dùng `BigDecimal`/`DECIMAL`; không dùng `float` hoặc `double`.
- Không log body, cookie, secret, CCCD, email hoặc số điện thoại.

## 4. Yêu cầu chức năng

Mỗi yêu cầu có mã ổn định để liên kết với test và task.

### 4.1. Identity và authorization

| ID | Yêu cầu | Actor | Ưu tiên |
|---|---|---|---|
| FR-ACC-001 | ADMIN tạo account với email/username và role hợp lệ; từ chối giá trị trùng bằng lỗi 409 | ADMIN | Must |
| FR-ACC-002 | Hệ thống sinh mật khẩu tạm, chỉ gửi qua kênh email mô phỏng; không lưu plaintext | ADMIN | Must |
| FR-ACC-003 | Account mới phải đổi mật khẩu; mật khẩu mới tối thiểu 8 ký tự và theo quy tắc mạnh đã duyệt | Mọi user | Must |
| FR-ACC-004 | Đăng nhập đúng tạo session mới; sai thông tin trả cùng dạng lỗi 401 | Mọi user | Must |
| FR-ACC-005 | Server kiểm tra role và ownership ở từng request; chưa login trả 401, thiếu quyền trả 403 | Mọi user | Must |
| FR-ACC-006 | ADMIN vô hiệu account; request mới và session liên quan không được tiếp tục dùng quyền | ADMIN | Must |
| FR-ACC-007 | Ghi audit cho cấp account, đăng nhập, thất bại đăng nhập, khóa account và đổi dữ liệu quan trọng | Hệ thống | Must |

### 4.2. Hộ và cư trú

| ID | Yêu cầu | Actor | Ưu tiên |
|---|---|---|---|
| FR-RES-001 | ADMIN tạo, xem, sửa và chuyển trạng thái hộ theo quyền | ADMIN | Must |
| FR-RES-002 | Hệ thống quản lý nhiều resident trong một household và liên kết account với household theo quyết định được duyệt | ADMIN | Must |
| FR-RES-003 | Tra cứu hộ/nhân khẩu theo trường được cho phép, có phân trang | ADMIN, cơ quan được cấp quyền | Must |
| FR-RES-004 | Resident chỉ xem danh sách thành viên và dữ liệu của household được server xác định | RESIDENT | Must |
| FR-RES-005 | Resident gửi khai báo tạm vắng/tạm trú trong phạm vi được cấp; bản ghi bắt đầu ở trạng thái `PENDING` | RESIDENT | Must |
| FR-RES-006 | ADMIN duyệt hoặc từ chối khai báo; lưu người duyệt, thời gian, lý do và trạng thái | ADMIN | Must |
| FR-RES-007 | Chuyển đi dùng trạng thái/xóa mềm; không xóa lịch sử cư trú và không hiển thị trong danh sách hiện tại | ADMIN | Must |
| FR-RES-008 | Mọi thay đổi hộ/nhân khẩu/cư trú quan trọng có audit cùng transaction | Hệ thống | Must |

### 4.3. Billing

| ID | Yêu cầu | Actor | Ưu tiên |
|---|---|---|---|
| FR-BIL-001 | Tạo và quản lý loại khoản thu, phân biệt `MANDATORY` và `VOLUNTARY` | ADMIN, ACCOUNTANT | Must |
| FR-BIL-002 | Quản lý đơn giá theo thời gian hiệu lực; không sửa lịch sử đã áp dụng | ACCOUNTANT | Must |
| FR-BIL-003 | Tính khoản phí theo công thức đã duyệt, gồm diện tích × đơn giá khi áp dụng | ACCOUNTANT | Must |
| FR-BIL-004 | Theo D26: ACCOUNTANT lập nháp/trình, ADMIN duyệt kỳ tháng hiện tại cho toàn bộ hộ ACTIVE, gồm hai loại phí D25 và hạn nộp chung trong tháng; trả lại có lý do, rút trình để sửa | ACCOUNTANT lập/trình; ADMIN duyệt/trả lại | Must |
| FR-BIL-005 | Một kỳ chưa hủy mỗi tháng kể cả nháp; chống trùng invoice/hộ/loại, kiểm cạnh tranh. ADMIN chỉ hủy cả kỳ chưa từng có khoản thu trong tháng hiện tại, giữ lịch sử để lập lại theo D26 | Hệ thống; ADMIN hủy | Must |
| FR-BIL-006 | Lưu revision khi trình; chặn duyệt nếu hộ/diện tích/đơn giá áp dụng đổi; phát hành snapshot hóa đơn bất biến và audit nguyên tử. Hộ ACTIVE sau phát hành tính từ kỳ sau, tạm vắng không giảm phí | Hệ thống | Must |
| FR-BIL-007 | ADMIN quản lý đợt, ACCOUNTANT ghi/hủy đóng góp theo D26; nguyên VND dương, idempotency, ngày thực thu riêng ngày nhập, nhập muộn có lý do; giữ lịch sử và không cộng nợ bắt buộc | ADMIN quản lý đợt; ACCOUNTANT ghi/hủy | Must |
| FR-BIL-008 | Resident xem chi tiết và lịch sử hóa đơn của đúng household, có lọc/phân trang | RESIDENT | Must |
| FR-BIL-009 | S08: cư dân chỉ xem tên, mô tả, thời gian và trạng thái đợt tự nguyện đã công bố; không danh sách người đóng hoặc tổng/số tiền đóng góp | RESIDENT | Must |

### 4.4. Payment

| ID | Yêu cầu | Actor | Ưu tiên |
|---|---|---|---|
| FR-PAY-001 | ACCOUNTANT tìm khoản phải thu theo household/kỳ và xem tổng, đã thu, còn nợ | ACCOUNTANT | Must |
| FR-PAY-002 | Ghi nhận thanh toán tiền mặt hoặc phương thức được phê duyệt | ACCOUNTANT | Must |
| FR-PAY-003 | Payment, paid amount, remaining debt và audit được cập nhật trong một transaction | Hệ thống | Must |
| FR-PAY-004 | Ngăn giao dịch duplicate và xử lý cạnh tranh bằng unique/idempotency/transaction phù hợp | Hệ thống | Must |
| FR-PAY-005 | Thu thiếu, thu thừa, sửa và hủy chỉ được thực hiện theo quy tắc đã phê duyệt | ACCOUNTANT | Blocked |
| FR-PAY-006 | Trạng thái khoản phải thu phản ánh đúng `UNPAID`, `PARTIALLY_PAID`, `PAID` hoặc `CANCELLED` | Hệ thống | Must |

### 4.5. Reporting và data sharing

| ID | Yêu cầu | Actor | Ưu tiên |
|---|---|---|---|
| FR-REP-001 | Xuất báo cáo tổng thu, tổng nợ và tỷ lệ theo công thức được duyệt | ADMIN, ACCOUNTANT | Must |
| FR-REP-002 | Tổng báo cáo phải khớp dữ liệu giao dịch trong DB tại thời điểm truy vấn | Hệ thống | Must |
| FR-REP-003 | Xuất danh sách cư trú theo mẫu và cột được phê duyệt | ADMIN | Must |
| FR-REP-004 | Cơ quan chức năng chỉ xem/tải gói đã xác thực, phê duyệt và whitelist trường | Cơ quan chức năng | Must |
| FR-REP-005 | Không expose entity hoặc dữ liệu thô ra API báo cáo | Hệ thống | Must |
| FR-REP-006 | File Excel phải đọc được, có tiêu đề/cột/tổng theo mẫu đã duyệt | ADMIN, ACCOUNTANT | Must |

## 5. Hợp đồng API mức khái niệm

URI và schema chi tiết phải được chốt trong API contract trước khi frontend
triển khai. Các nhóm endpoint dự kiến:

| Resource | Operations v1 |
|---|---|
| `/api/v1/auth` | CSRF, login, logout, me |
| `/api/v1/accounts` | ADMIN tạo account |
| `/api/v1/households` | CRUD, tra cứu, thành viên |
| `/api/v1/residency-declarations` | Tạo, duyệt, từ chối |
| `/api/v1/fee-types` | Danh mục và phân loại |
| `/api/v1/fee-rates` | Đơn giá và hiệu lực |
| `/api/v1/billing-periods` | Tạo/mở/đóng kỳ thu |
| `/api/v1/charges` | Tạo bảng kê, xem chi tiết/công nợ |
| `/api/v1/payments` | Ghi nhận và xem giao dịch |
| `/api/v1/reports` | Báo cáo và export đã whitelist |

Quy tắc HTTP:

- `400`: request sai hoặc thiếu trường bắt buộc.
- `401`: chưa xác thực.
- `403`: xác thực nhưng không đủ quyền/ownership.
- `404`: tài nguyên không tồn tại hoặc không được phép tiết lộ.
- `409`: trùng hoặc xung đột nghiệp vụ.
- `500`: lỗi nội bộ; không trả stack trace/secret.

Write request phải có CSRF token và dùng session cookie HttpOnly. CORS chỉ
cho origin cấu hình.

## 6. Mô hình dữ liệu logic

Các aggregate chính:

```text
User -> Role
Household -> Resident -> ResidencyChange
FeeType -> FeeRate
BillingPeriod -> HouseholdCharge -> ChargeDetail
HouseholdCharge -> Payment
Any business write -> AuditLog
```

Ràng buộc tối thiểu:

- Mã household, username/email và mã loại phí không trùng.
- Resident giữ lịch sử khi chuyển đi.
- `FeeRate` có thời gian hiệu lực hợp lệ.
- Charge lưu snapshot giá/diện tích.
- Voluntary contribution không làm tăng mandatory debt.
- Tổng payment không vượt charge nếu quy tắc thu thừa chưa được duyệt.
- Foreign key và transaction bảo vệ tính toàn vẹn.

## 7. Yêu cầu phi chức năng

| ID | Yêu cầu kiểm chứng |
|---|---|
| NFR-SEC-001 | Session JDBC, cookie HttpOnly/SameSite=Lax, đổi session ID sau login; Secure khi HTTPS |
| NFR-SEC-002 | CSRF bật cho write; deny-by-default; kiểm thử 401/403 và truy cập chéo household |
| NFR-SEC-003 | Password chỉ lưu hash; không log secret/PII; audit không chứa PII thô ngoài whitelist |
| NFR-PERF-001 | Tra cứu hồ sơ hoặc danh sách khoản thu ≤3 giây với 10.000 hồ sơ; ghi rõ máy, query, pagination, warm-up và tải đo |
| NFR-DATA-001 | Tiền chính xác bằng BigDecimal/DECIMAL; kết quả báo cáo đối chiếu được |
| NFR-DATA-002 | Audit thành công/rollback nhất quán với transaction nghiệp vụ |
| NFR-AVAIL-001 | Có backup định kỳ và diễn tập restore; volume Docker không được coi là backup |
| NFR-RET-001 | Không purge dữ liệu trước khi thời hạn retention được phê duyệt |
| NFR-API-001 | Response lỗi dùng ProblemDetail, code ổn định, không chứa giá trị nhạy cảm |
| NFR-MAINT-001 | Migration versioned bằng Flyway; không sửa migration đã chia sẻ |

## 8. Tiêu chí kiểm thử và Definition of Done

Một yêu cầu chỉ đạt khi có:

1. Unit/integration/API test phù hợp.
2. Test quyền và ownership nếu có dữ liệu hộ.
3. Test dữ liệu không hợp lệ và xung đột nếu có write.
4. Test audit và rollback nếu là thay đổi nghiệp vụ.
5. Bằng chứng chạy trên MySQL cho migration/query quan trọng.
6. Cập nhật tài liệu/API contract khi shape thay đổi.

Sprint v1 chỉ hoàn thành khi luồng hộ → phí → kỳ thu → khoản phải thu →
thanh toán → công nợ → báo cáo chạy được, dữ liệu tự nguyện tách khỏi công
nợ bắt buộc, export đúng whitelist và CI unit/integration đạt.

## 9. Traceability

| SRS | BRD/PBI/NFR |
|---|---|
| FR-ACC-* | BR-01; PBI-001–004; NFR-001–003 |
| FR-RES-* | BR-02; PBI-006–008, 010; NFR-001–003, 009, 010 |
| FR-BIL-* | BR-03; PBI-011–013; NFR-007 |
| FR-PAY-* | BR-04; PBI-015; NFR-003, 007 |
| FR-REP-* | BR-05; PBI-021–023; NFR-001, 004, 009 |
| NFR-SEC-* | NFR-001–003 |
| NFR-PERF-* | NFR-004 |
| NFR-DATA-* | NFR-007 |
| NFR-AVAIL-* | NFR-008 |
| NFR-RET-* | NFR-010 |

## 10. Vấn đề cần quyết định trước khi code

| Mã | Quyết định cần có | Ảnh hưởng |
|---|---|---|
| SRS-TBD-01 | Ma trận role/quyền và account–household–resident | Identity, residency, API ownership |
| SRS-TBD-02 | Rounding và hiệu lực đơn giá | Billing, invoice, report |
| SRS-TBD-03 | Thu thiếu/thừa, sửa/hủy và idempotency key | Payment, transaction, audit |
| SRS-TBD-04 | Công thức tỷ lệ, mẫu Excel và cột export | Reporting |
| SRS-TBD-05 | Mẫu báo cáo cư trú và whitelist cơ quan | Reporting, privacy |
| SRS-TBD-06 | Backup schedule, restore evidence và retention | Operations, compliance |
| SRS-TBD-07 | Kênh thông báo kết quả duyệt | Residency |

## 11. Trạng thái tài liệu

SRS này là bản dự thảo. Không dùng các mục `Blocked`/`TBD` làm căn cứ
để tự tạo schema, endpoint hoặc acceptance criteria mới. Khi các quyết định
được phê duyệt, cập nhật version, traceability và test plan tương ứng.
