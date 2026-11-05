# Quyết định và assumption

Ngày rà soát: 04/11/2026. Chỉ các lựa chọn D01–D04 đã được người dùng trả lời trực tiếp; các mặc định khác là đề xuất kỹ thuật có thể sửa trước khi triển khai task liên quan.

| Mã | Quyết định | Lý do | Thay thế / trạng thái |
|---|---|---|---|
| D01 | Java/Spring Boot, Maven, MySQL | Bám Java/MySQL, nhóm dùng chung stack | Đã chọn; Java 21/Boot 4.0.9/MySQL 8.4 là cấu hình cụ thể đề xuất |
| D02 | WBS mới quyết định phạm vi v1 | Đã cân đối nguồn lực bán thời gian | Đã chọn thay vì nhãn v1 cũ trong Product Backlog |
| D03 | Monorepo backend/frontend/docs | Hợp đồng API và tài liệu chung | Đã chọn; repo KietQuangT/SE_10-BlueMoonComplex |
| D04 | Server session | Thu hồi truy cập tức thời dễ hơn JWT thuần | Đã chọn; JDBC store để không thêm Redis |
| D05 | Monolith theo tính năng; controller/service/repository | Nhóm nhỏ, ít công sức vận hành | Đề xuất; chỉ tách module/service khi có nhu cầu đo được |
| D06 | Flyway sở hữu schema, JPA validate | Tránh mỗi máy tự sửa schema khác nhau | Đề xuất thay Hibernate update/Liquibase |
| D07 | JUnit/MockMvc + MySQL integration | Test quyền và đặc thù SQL thật | Đề xuất; Testcontainers là phương án sau nếu nhóm cần DB tự tạo theo test |
| D08 | Docker Compose local, không cloud | Đúng phạm vi, ít chi phí | Đề xuất; IDE + DB container vẫn dùng được |
| D09 | VND, BigDecimal; ngày nghiệp vụ Asia/Ho_Chi_Minh, thời điểm lưu UTC | Tránh sai số và lệch ngày báo cáo | Đề xuất; phải chốt precision/rounding trước task phí |
| D10 | Dev frontend localhost:5173, API prefix /api/v1 | Mặc định giao tiếp web đơn giản | Chưa chọn framework frontend; ưu tiên proxy cùng origin |
| D11 | 401 chưa xác thực, 403 sai quyền, lỗi JSON ProblemDetail | Hợp đồng lỗi nhất quán | Đề xuất; không cho lộ nội bộ trong response |
| D12 | Audit qua `common/audit/AuditService`, ghi cùng transaction (`MANDATORY`), `Clock` UTC, `changes` JSON, từ chối khóa PII (`email`, `phone`, `identityNumber`, `password*`, không phân biệt hoa thường) | Audit không lệch dữ liệu (ARCHITECTURE, NFR-003); tránh ghi PII | Đề xuất, chờ nhóm xác nhận (F02, Sơn + Tuấn). Giới hạn: danh sách khóa chỉ là lưới chặn thô, không bắt PII nằm trong giá trị hoặc khóa tên khác; bảo vệ chính là caller chỉ ghi trường được phép. `RoleService` là mẫu chưa có kiểm tra quyền, S01 thay hoặc xóa |

## Những điểm chưa xác nhận và cách xử lý
- Quyền chi tiết BQT/Kế toán/Cư dân/Cơ quan chức năng; cách cấp account cư dân và account BQT đầu tiên. Chặn task cấp tài khoản cho tới khi duyệt ma trận. Mặc định đề xuất bootstrap một BQT bằng lệnh local riêng, không password trong migration.
- Đổi mật khẩu: tối thiểu 8 ký tự theo PBI-002; các tiêu chí độ mạnh, mật khẩu tạm hết hạn và brute-force phải chốt. Đề xuất 5 lần sai/15 phút và khóa 15 phút để thảo luận, chưa code thành quy tắc chính thức.
- Phí: hiệu lực theo ngày/tháng, làm tròn, thu thiếu/thừa và sửa/hủy. Mặc định đề xuất VND làm tròn cuối bằng HALF_UP; phải xác nhận trước task phí.
- Tỷ lệ thu: mẫu số theo số tiền phải thu hay số hộ? Đề xuất theo số tiền phải thu bắt buộc, tự nguyện thống kê riêng; chặn AC báo cáo đến khi chốt.
- Mẫu báo cáo cư trú và trường cơ quan được xem chưa có mẫu chuẩn. Không tự tuyên bố đáp ứng pháp lý.
- Backup: NFR-008 yêu cầu tự động; cần chốt lịch, nơi lưu và diễn tập restore. Giữ trong DoD v1, chưa có trong skeleton.
- NFR-010 nêu ví dụ tối thiểu 5 năm; không xem là kết luận pháp luật. Chưa purge dữ liệu tự động khi thời hạn chưa duyệt.
- Lịch thi/nghỉ và công suất 4,4h kỹ thuật/người/tuần chưa xác nhận bằng lịch cá nhân. Không tự công bố xác suất thành công 95%.
- Bộ constraint bổ sung đề xuất thay 3 mốc nhỏ tháng 12; chưa ghi đè WBS/Gantt gốc. PLAN lấy đó làm phương án tích hợp, yêu cầu PM cập nhật baseline nếu chấp thuận.
- Schema nghiệp vụ V2/V3/V4. Sơn đã duyệt ngày 05/11/2026; chờ Quân, Tuấn, Kiệt xác nhận. Đã chạy V1–V4 qua Flyway trên MySQL 8.4.13 và 16 ca ràng buộc (bằng chứng: `_agents_workspace/db-fix/EVIDENCE.md`, ngoài repo). Vẫn chặn V5 (phí/thu tiền).
  1. Email tài khoản (PBI-001): cột `email` riêng, duy nhất, thêm ở V4 (task S02); `username` độc lập với email.
     Đổi sang dùng email làm username: bỏ cột `email` ở V4, service dùng `username` làm email.
  2. Chuyển đi (PBI-010): chỉ `residents.residency_status` kèm bản ghi `residency_changes`; không có `is_deleted`.
     Danh sách cư trú hiện tại lọc theo `residency_status`. Thêm cờ xóa mềm nếu nhóm đổi: ALTER ở migration mới.
  3. Vai trò: 3 vai trò v1 (ADMIN "Ban quản trị", ACCOUNTANT "Kế toán", RESIDENT "Cư dân") được seed ở V2 như nhãn tham chiếu.
     Ma trận quyền vẫn do F01 chốt. Vai trò thứ tư (Bên liên quan / Cơ quan chức năng) chờ mẫu báo cáo cư trú.
     Đổi mã vai trò: sửa INSERT ở V2 trước khi merge; sau khi merge thì dùng migration mới.
  4. Mapping account–hộ: `users.household_id` (V4), NULL cho nhân sự, service bắt buộc cho RESIDENT, một account một hộ.
     Quyền theo hộ lấy từ cột này ở server (PBI-006). Cần Ban quản trị xác nhận một account không thuộc nhiều hộ.
     Nếu một người dùng nhiều hộ: bảng nối `user_households(user_id, household_id)` thay cho cột.
  5. `residents.identity_number`: không UNIQUE ở DB; service kiểm tra trùng trong nhóm đang cư trú; có index để tra cứu.
     Lỗi trùng không được chứa số giấy tờ (AGENTS.md:28).
  6. `residency_status` và `change_type`: VARCHAR, không DEFAULT, không CHECK; F01 chốt tập giá trị trước S05.
     `residency_changes.status` dùng PENDING/APPROVED/REJECTED (PBI-007, PBI-008).
  7. `audit_log.actor_user_id` NULL được, chỉ cho hành động bootstrap bằng lệnh local (action cố định, ví dụ BOOTSTRAP_ADMIN).
     Service bắt buộc actor ở mọi hành động khác; test kiểm tra điều này.
  8. Phí/thu tiền: chưa có migration. Chặn V5 cho tới khi chốt các điểm ở dòng "Phí" và "Tỷ lệ thu" phía trên:
     VND làm tròn cuối HALF_UP (mặc định đề xuất, chờ xác nhận), hiệu lực đơn giá, thu thiếu/thừa/hủy,
     khóa idempotency của giao dịch thu, khoản tự nguyện tách khỏi công nợ.

## Độ chắc chắn
Đủ căn cứ dựng skeleton với stack và cấu trúc đã chọn. Chưa đủ căn cứ khẳng định ≥95% chắc chắn cho toàn bộ nghiệp vụ Sprint 1. Interview tiếp theo theo thứ tự: ma trận quyền/cấp tài khoản → quy tắc phí → mẫu báo cáo/backup; chỉ hỏi khi bắt đầu task phụ thuộc.
