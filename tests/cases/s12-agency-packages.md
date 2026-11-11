# Ca kiểm thử gói dữ liệu cơ quan chức năng (S12 / PBI-023)

Tài liệu này định nghĩa bộ ca kiểm thử backend cho gói dữ liệu cơ quan chức năng (Feature 12 / S12, WBS 2.4.1.4, PBI-023). Phạm vi chỉ gồm API và dữ liệu; giao diện ADMIN của S12 đang được làm riêng nên chưa có ca giao diện.

Quy tắc nghiệp vụ lấy từ quyết định D13 trong [docs/DECISIONS.md](../../docs/DECISIONS.md): không tạo role/account cho cơ quan; ADMIN tạo gói theo phạm vi hộ và xem trước; người tạo được từ chối nháp; chỉ ADMIN khác người tạo mới được duyệt; BQT thu hồi, tải và ghi nhận bàn giao; thời hạn tải là mốc sớm hơn giữa duyệt + 7 ngày và tạo + 30 ngày; nội dung bị xóa tại tạo + 30 ngày; metadata và audit tối thiểu giữ 5 năm. Kiến trúc tại [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md), hướng dẫn tại [tests/README.md](../README.md), kết quả đối soát tại [docs/VALIDATION.md](../../docs/VALIDATION.md).

### Quy ước và môi trường kiểm thử

1. **Môi trường:** ca tích hợp cần MySQL 8.4 cô lập đã chạy Flyway V1–V11. Tuyệt đối không chạy trên database dùng chung hoặc có dữ liệu cư dân thật; không dùng `docker compose down -v`.
2. **Vai trò:** chỉ `ADMIN` được gọi 9 endpoint dưới `/api/v1/agency-data-packages`; `ACCOUNTANT`, `RESIDENT` bị 403; chưa đăng nhập bị 401. Mọi thao tác ghi cần CSRF.
3. **Thời gian:** mọi mốc tính theo giờ hệ thống (UTC trong DB); ca mốc chính xác dùng đồng hồ cố định.
4. **Mã lỗi:** ứng dụng trả JSON có trường `code` (ví dụ `PACKAGE_STATE_CONFLICT`); không trả chi tiết nội bộ.
5. **Trạng thái mặc định** của mọi ca là **Not run**. Kết quả thực thi nằm ở [tests/report/s12-agency-packages.md](../report/s12-agency-packages.md).

---

## Nhóm 1: Phân quyền và kiểm soát truy cập

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-01.1` | Chưa đăng nhập gọi cả 9 endpoint | ANON | HTTP 401, không có nội dung gói |
| `AGP-01.2` | Cư dân và kế toán gọi cả 9 endpoint | RESIDENT, ACCOUNTANT | HTTP 403 trên mọi endpoint |
| `AGP-01.3` | Ghi (tạo, duyệt, từ chối, thu hồi, bàn giao) thiếu CSRF | ADMIN | HTTP 403, không đổi dữ liệu |
| `AGP-01.4` | Người gọi không tự khai báo người thực hiện hoặc trạng thái | ADMIN | Trường thừa (`createdBy`, `status`, `actorId`, `contentSha256` khi tạo) bị 400; người thực hiện luôn lấy từ phiên đăng nhập |

## Nhóm 2: Kiểm tra dữ liệu vào

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-02.1` | Tạo gói với thân yêu cầu sai: JSON hỏng, không phải object, thiếu trường, trường không phải chuỗi | ADMIN | HTTP 400 `INVALID_PACKAGE_REQUEST`, service không được gọi |
| `AGP-02.2` | `householdIds` không phải mảng số nguyên (chuỗi, số thập phân, null, lồng nhau) | ADMIN | HTTP 400 `PACKAGE_SCOPE_INVALID` |
| `AGP-02.3` | Mã cơ quan và mã tham chiếu sai định dạng | ADMIN | HTTP 400 `INVALID_PACKAGE_REQUEST` |
| `AGP-02.4` | Thân thao tác (duyệt, từ chối, thu hồi, bàn giao) thiếu hoặc sai `expectedVersion` (0, âm, chuỗi, thập phân) hoặc `contentSha256` (hoa, ngắn, dài, không phải chuỗi) | ADMIN | HTTP 400 `INVALID_PACKAGE_REQUEST` trên cả 4 endpoint |
| `AGP-02.5` | Bộ lọc danh sách và xem trước: `status` ngoài tập hợp lệ, `page` âm, `size` ngoài 1–100, tham số trùng hoặc lạ; endpoint chi tiết và tải không nhận tham số | ADMIN | HTTP 400 `INVALID_PACKAGE_FILTER` |
| `AGP-02.6` | Mã gói trên đường dẫn không phải số | ADMIN | HTTP 400, service không được gọi |

## Nhóm 3: Phạm vi hộ và snapshot

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-03.1` | Gói chỉ chứa cư dân của hộ được chọn | ADMIN | Cư dân hộ ngoài phạm vi không xuất hiện trong tệp |
| `AGP-03.2` | Hộ không hoạt động hoặc không tồn tại; danh sách hộ rỗng, trùng, ID không dương | ADMIN | HTTP 400 `PACKAGE_SCOPE_INVALID` |
| `AGP-03.3` | Chỉ cư dân `RESIDING` và `TEMPORARILY_ABSENT` được đưa vào, sắp theo mã căn hộ; số đếm khớp tệp | ADMIN | Cư dân `MOVED_OUT` bị loại; metadata tệp khớp số đếm trong DB |
| `AGP-03.4` | Dữ liệu gốc đổi sau khi tạo gói (đổi tên, chuyển đi, hộ ngừng hoạt động) | ADMIN | Xem trước và tệp vẫn là bản snapshot ban đầu, mã băm không đổi |
| `AGP-03.5` | Giới hạn kích thước: đúng 1.000 hộ được chấp nhận; quá 50.000 dòng cư dân hoặc tệp quá 10 MB bị từ chối | ADMIN | `PACKAGE_TOO_LARGE`, không ghi gói, nội dung hoặc audit; đúng 50.000 dòng vẫn được chấp nhận |
| `AGP-03.6` | Xem trước phân trang | ADMIN | Các trang không chồng lấn, trang cuối đúng số dòng, trang vượt giới hạn trả danh sách rỗng |

## Nhóm 4: Quy trình duyệt, bàn giao và thu hồi

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-04.1` | Người tạo tự duyệt gói của mình | ADMIN (người tạo) | HTTP 403 `PACKAGE_SELF_DECISION_NOT_ALLOWED`; ADMIN khác duyệt được |
| `AGP-04.2` | Người tạo từ chối nháp của mình | ADMIN (người tạo) | Gói chuyển `REJECTED`, ghi người từ chối |
| `AGP-04.3` | Duyệt hoặc từ chối với `expectedVersion` hoặc mã băm cũ; chuyển trạng thái thất bại | ADMIN | HTTP 409 `PACKAGE_VERSION_CONFLICT`; không cập nhật dữ liệu và không ghi audit |
| `AGP-04.4` | Bàn giao gói đã duyệt | ADMIN | Lưu người và thời điểm bàn giao, tăng phiên bản; bàn giao lần hai bị 409 `PACKAGE_STATE_CONFLICT` |
| `AGP-04.5` | Thu hồi gói (kể cả sau bàn giao) | ADMIN | Gói `REVOKED`, tải sau đó bị 409 |
| `AGP-04.6` | Hai thao tác quyết định đồng thời trên cùng gói nháp (duyệt và từ chối; hai ADMIN cùng duyệt) | ADMIN | Đúng một thao tác thành công, thao tác còn lại 409; chỉ có một audit quyết định |

## Nhóm 5: Thời hạn

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-05.1` | Hạn tải khi duyệt | ADMIN | Bằng duyệt + 7 ngày; nếu vượt tạo + 30 ngày thì bằng tạo + 30 ngày; hai nhánh trùng nhau tại biên |
| `AGP-05.2` | Mốc chính xác: đúng thời điểm hết hạn tải; đúng thời điểm xóa nội dung; một giây trước hạn | ADMIN | Tải, bàn giao, xem trước (gói đã duyệt) bị 409 `PACKAGE_DOWNLOAD_EXPIRED`; duyệt, từ chối, xem trước (gói nháp) bị 409 `PACKAGE_CONTENT_UNAVAILABLE`; một giây trước hạn vẫn được |
| `AGP-05.3` | Hết hạn tải trên DB thật | ADMIN | Tải, xem trước, bàn giao đều bị 409; cờ `canDownload` bằng false |

## Nhóm 6: Audit

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-06.1` | Mỗi bước (tạo, duyệt, tải, bàn giao, thu hồi) ghi đúng tên hành động, đúng người thực hiện, cùng transaction | ADMIN | Chỉ các khóa đã duyệt (phiên bản, trạng thái, mã băm, mã cơ quan, số đếm); không có họ tên cư dân |
| `AGP-06.2` | Thao tác thất bại (sai mã băm, tự duyệt, chưa xác thực) | ADMIN | Không ghi audit nào |

## Nhóm 7: Toàn vẹn và phản hồi lỗi

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-07.1` | Nội dung tệp trong DB không khớp mã băm; dòng nội dung bị mất | ADMIN | Không phát hành tệp, không ghi audit tải; mất nội dung trả 409 `PACKAGE_CONTENT_UNAVAILABLE` |
| `AGP-07.2` | Ánh xạ lỗi: không tìm thấy, xung đột, cấm, lỗi dữ liệu, lỗi nội bộ | ADMIN | 404, 409, 403, 400 kèm `code`; lỗi nội bộ trả 500 chung, không lộ nội dung ngoại lệ |
| `AGP-07.3` | Tiêu đề phản hồi khi tải | ADMIN | Content-Type XLSX, `Content-Disposition` đính kèm, `Cache-Control: no-store`; lỗi không có `Content-Disposition` |
| `AGP-07.4` | Ràng buộc schema là lớp bảo vệ cuối | (DB) | Tự duyệt, tổng cư dân sai, trạng thái lạ, nội dung quá 10 MB và hộ không tồn tại đều bị MySQL từ chối |

## Nhóm 8: Retention

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-08.1` | Dọn dẹp định kỳ | Hệ thống | Xóa nội dung sau tạo + 30 ngày và đánh dấu thời điểm xóa; xóa gói, liên kết hộ, nội dung sau 5 năm; xóa audit S12 quá 5 năm; giữ audit S12 còn hạn và audit không thuộc S12; ghi một audit dọn dẹp với actor NULL |
| `AGP-08.2` | Gói quá 5 năm | ADMIN | Không xuất hiện trong danh sách, chi tiết trả 404 `PACKAGE_NOT_FOUND` |
| `AGP-08.3` | Dọn dẹp nhiều lô | Hệ thống | Lặp đến khi hết dữ liệu hết hạn; không ghi audit khi không có gì để dọn |

## Nhóm 9: Bố cục tệp gói

| Mã | Ca kiểm thử | Vai trò | Kỳ vọng |
| --- | --- | --- | --- |
| `AGP-09.1` | Khối metadata và bảng dữ liệu | ADMIN | 8 dòng metadata (mã mẫu, phiên bản, thời điểm snapshot UTC và giờ Việt Nam, các số đếm); tiêu đề ở dòng 10; chỉ 4 cột đã duyệt |
| `AGP-09.2` | Cố định dòng và bộ lọc | ADMIN | Cố định 10 dòng đầu; bộ lọc phủ tiêu đề và mọi dòng dữ liệu |
| `AGP-09.3` | Dữ liệu hiển thị | ADMIN | Giữ thứ tự dòng; nhãn trạng thái tiếng Việt; họ tên dài được bọc chữ và tăng chiều cao dòng; chuỗi bắt đầu bằng ký tự công thức được ghi như văn bản |
| `AGP-09.4` | Mã băm | ADMIN | SHA-256 gồm 64 ký tự thập lục phân và thay đổi khi nội dung đổi |
