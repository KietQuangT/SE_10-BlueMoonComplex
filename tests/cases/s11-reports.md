# Ca kiểm thử xuất báo cáo Excel thu phí, tiền mặt và cư trú (S11 / PBI-021, PBI-022, PBI-023)

Tài liệu này định nghĩa bộ ca kiểm thử chức năng, kiểm thử tích hợp và kiểm thử giao diện cho tính năng xuất báo cáo Excel (XLSX) thu phí theo kỳ, sổ quỹ tiền mặt và thống kê cư trú hiện tại (Feature 11 / S11, WBS 2.4.1.3).

Các quy tắc kỹ thuật và nghiệp vụ tuân thủ quyết định trong [docs/DECISIONS.md](../../docs/DECISIONS.md), kiến trúc trong [docs/ARCHITECTURE.md](../../docs/ARCHITECTURE.md), hướng dẫn kiểm thử tại [tests/README.md](../README.md) và kết quả đối soát tại [docs/VALIDATION.md](../../docs/VALIDATION.md).

### Quy ước và môi trường kiểm thử

1. **Môi trường & Cơ sở dữ liệu:**
   - Các ca kiểm thử tích hợp yêu cầu MySQL 8.4 cô lập, đã chạy đầy đủ migration Flyway từ V1 đến V10.
   - Tuyệt đối không chạy trên database production, DB chung hoặc database có dữ liệu cư dân thật.
   - Không dùng `docker compose down -v` hoặc xóa volume để dọn dẹp dữ liệu.
2. **Dữ liệu giả lập & Placeholder:**
   - Ngày tháng `<HOM_NAY>`, `<HOM_QUA>`, `<DAU_THANG>`, `<CUOI_THANG>`, kỳ `<THANG_NAY>` (`YYYY-MM`) được tính theo múi giờ `Asia/Ho_Chi_Minh`.
   - Báo cáo xuất ra dưới định dạng tệp XLSX với streaming Apache POI (`SXSSFWorkbook`) tránh tràn bộ nhớ JVM.
   - Định dạng ngày xuất file: `cu-tru-hien-tai-YYYYMMDD.xlsx`, `thu-theo-ky-YYYY-MM.xlsx`, `tien-mat-YYYY-MM-DD-YYYY-MM-DD.xlsx`.
3. **Phân quyền vai trò:**
   - `ADMIN`: Toàn quyền xuất cả 3 loại báo cáo (thu phí kỳ, sổ quỹ tiền mặt, cư trú hiện tại).
   - `ACCOUNTANT`: Chỉ được phép xuất báo cáo thu phí kỳ và sổ quỹ tiền mặt; bị chặn 403 khi xuất báo cáo cư trú.
   - `RESIDENT`: Bị chặn 403 trên toàn bộ endpoint xuất báo cáo.
   - `ANON` (Chưa đăng nhập): Bị từ chối 401 trên toàn bộ endpoint.
4. **Trạng thái thực thi:**
   - Trạng thái mặc định của mọi ca là **Not run**.

---

## Mục lục bộ ca kiểm thử

- **Nhóm 1: Phân quyền & Kiểm soát truy cập (RBAC & Authorization)**
  - `REP-01.1` — Từ chối truy cập chưa xác thực (ANON) trên toàn bộ endpoint báo cáo (HTTP 401)
  - `REP-01.2` — Chặn vai trò cư dân (RESIDENT) trên toàn bộ endpoint báo cáo (HTTP 403)
  - `REP-01.3` — Phân quyền ACCOUNTANT: Được phép tải báo cáo thu phí và tiền mặt, bị chặn khi tải báo cáo cư trú (HTTP 403)
  - `REP-01.4` — Phân quyền ADMIN: Được phép tải cả 3 loại báo cáo (thu phí, tiền mặt, cư trú)
- **Nhóm 2: Xác thực tham số truy vấn (Query Parameter Validation)**
  - `REP-02.1` — Thu phí kỳ: Chấp nhận định dạng kỳ `YYYY-MM` hợp lệ (HTTP 200); từ chối định dạng sai (HTTP 400)
  - `REP-02.2` — Thu phí kỳ: Từ chối khi thiếu tham số `period` hoặc kèm tham số lạ ngoài đặc tả (HTTP 400)
  - `REP-02.3` — Sổ quỹ tiền mặt: Chấp nhận khoảng ngày `from <= to` và `to <= today` (HTTP 200)
  - `REP-02.4` — Sổ quỹ tiền mặt: Từ chối khi `from > to` hoặc ngày `to` ở tương lai (HTTP 400)
  - `REP-02.5` — Sổ quỹ tiền mặt: Từ chối khi thiếu tham số hoặc kèm tham số lạ ngoài `from`/`to` (HTTP 400)
  - `REP-02.6` — Cư trú: Từ chối khi request có chứa bất kỳ query parameter nào (HTTP 400)
- **Nhóm 3: Tiêu đề HTTP, MIME Type & Tên tệp đính kèm (Headers & Content-Disposition)**
  - `REP-03.1` — Content-Type trả về chuẩn XLSX `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
  - `REP-03.2` — Header `Cache-Control: no-store` được áp dụng trên tất cả response báo cáo
  - `REP-03.3` — Tên file đính kèm khớp định dạng quy ước theo ngày/kỳ lọc
- **Nhóm 4: Cấu trúc tệp Excel & Toàn vẹn dữ liệu (Workbook Content & Layout)**
  - `REP-04.1` — Báo cáo thu phí kỳ: Đầy đủ các cột hóa đơn, số tiền, ngày phát hành và dòng tổng cộng
  - `REP-04.2` — Sổ quỹ tiền mặt: Chi tiết phiếu thu, số tiền áp dụng, số dư nộp thừa và dòng tổng
  - `REP-04.3` — Báo cáo cư trú: Khối metadata 9 dòng đầu tiên, danh sách nhân khẩu kèm quan hệ chủ hộ và trạng thái
  - `REP-04.4` — Bọc chữ (wrap text) và tự động giãn chiều cao dòng cho trường họ tên cư dân dài
- **Nhóm 5: Giới hạn số dòng & Quản lý tài nguyên bộ nhớ (Row Limits & SXSSFWorkbook)**
  - `REP-05.1` — Chặn xuất báo cáo thu phí khi số dòng vượt ngưỡng 50.000 (`REPORT_ROW_LIMIT_EXCEEDED`)
  - `REP-05.2` — Chặn xuất báo cáo tiền mặt khi số dòng vượt ngưỡng 50.000 (`REPORT_ROW_LIMIT_EXCEEDED`)
  - `REP-05.3` — Chặn xuất báo cáo cư trú khi số dòng vượt ngưỡng 50.000 (`REPORT_ROW_LIMIT_EXCEEDED`)
  - `REP-05.4` — Đảm bảo dọn dẹp file tạm của SXSSFWorkbook trên ổ đĩa sau khi hoàn tất hoặc gặp lỗi sớm
- **Nhóm 6: Giao diện người dùng & Điều hướng tải báo cáo (UI Downloads & UX)**
  - `REP-06.1` — ADMIN mở trang `/reports`: Hiển thị đầy đủ 3 khối tải báo cáo
  - `REP-06.2` — ACCOUNTANT mở trang `/reports`: Chỉ hiển thị 2 khối tải báo cáo thu phí và tiền mặt, ẩn khối cư trú
  - `REP-06.3` — Điều hướng từ thanh menu header vào trang `/reports` cho ADMIN và ACCOUNTANT
  - `REP-06.4` — Xử lý tải file qua blob URL trong API mode và hiển thị thông báo lỗi khi API thất bại

---

## Chi tiết các ca kiểm thử

### Nhóm 1: Phân quyền & Kiểm soát truy cập (RBAC & Authorization)

#### REP-01.1 — Từ chối truy cập chưa xác thực (ANON) trên toàn bộ endpoint báo cáo (HTTP 401)
- **ID:** REP-01.1
- **Tiêu đề:** Khách vãng lai (chưa đăng nhập) bị chặn khi truy cập endpoint báo cáo
- **Tiền điều kiện:** Chưa đăng nhập hoặc session cookie không hợp lệ.
- **Vai trò:** ANON
- **Điểm vào:**
  - `GET /api/v1/reports/fee-collection.xlsx?period=2026-10`
  - `GET /api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31`
  - `GET /api/v1/reports/residency.xlsx`
- **Các bước:**
  1. Gửi request GET tới lần lượt 3 endpoint mà không kèm header Cookie session.
  2. Thu thập HTTP status code.
- **Kỳ vọng:** HTTP 401 Unauthorized trên cả 3 endpoint; không trả về nội dung bảng tính.
- **Kiểm sau:** Không có ngoại lệ chưa bắt trên server log.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:90`

#### REP-01.2 — Chặn vai trò cư dân (RESIDENT) trên toàn bộ endpoint báo cáo (HTTP 403)
- **ID:** REP-01.2
- **Tiêu đề:** Tài khoản vai trò RESIDENT bị từ chối truy cập báo cáo quản trị
- **Tiền điều kiện:** Đăng nhập thành công với tài khoản vai trò RESIDENT.
- **Vai trò:** RESIDENT
- **Điểm vào:**
  - `GET /api/v1/reports/fee-collection.xlsx?period=2026-10`
  - `GET /api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31`
  - `GET /api/v1/reports/residency.xlsx`
- **Các bước:**
  1. Đăng nhập với tài khoản RESIDENT.
  2. Gửi request GET tới 3 endpoint báo cáo.
- **Kỳ vọng:** HTTP 403 Forbidden trên cả 3 endpoint.
- **Kiểm sau:** Không rò rỉ dữ liệu tài chính hoặc danh sách cư dân cho cư dân khác.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:54-55`

#### REP-01.3 — Phân quyền ACCOUNTANT: Được phép tải báo cáo thu phí và tiền mặt, bị chặn khi tải báo cáo cư trú (HTTP 403)
- **ID:** REP-01.3
- **Tiêu đề:** Kế toán viên chỉ được xuất báo cáo tài chính, không có quyền xuất danh sách cư trú
- **Tiền điều kiện:** Đăng nhập thành công với tài khoản vai trò ACCOUNTANT.
- **Vai trò:** ACCOUNTANT
- **Điểm vào:**
  - `GET /api/v1/reports/fee-collection.xlsx?period=2026-10` -> HTTP 200
  - `GET /api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31` -> HTTP 200
  - `GET /api/v1/reports/residency.xlsx` -> HTTP 403
- **Các bước:**
  1. Đăng nhập với tài khoản ACCOUNTANT.
  2. Gửi request tới 2 endpoint thu phí và tiền mặt.
  3. Gửi request tới endpoint cư trú.
- **Kỳ vọng:** 2 endpoint tài chính trả về HTTP 200 và nội dung tệp XLSX; endpoint cư trú trả về HTTP 403 Forbidden.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:54-55`

#### REP-01.4 — Phân quyền ADMIN: Được phép tải cả 3 loại báo cáo (thu phí, tiền mặt, cư trú)
- **ID:** REP-01.4
- **Tiêu đề:** Quản trị viên (ADMIN) có quyền xuất toàn bộ các loại báo cáo
- **Tiền điều kiện:** Đăng nhập với tài khoản vai trò ADMIN.
- **Vai trò:** ADMIN
- **Điểm vào:** Cả 3 endpoint `/api/v1/reports/*.xlsx`
- **Các bước:**
  1. Đăng nhập tài khoản ADMIN.
  2. Lần lượt gửi request tải 3 báo cáo với tham số hợp lệ.
- **Kỳ vọng:** Cả 3 request đều trả về HTTP 200 với nội dung file Excel hợp lệ.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java:54-55`

---

### Nhóm 2: Xác thực tham số truy vấn (Query Parameter Validation)

#### REP-02.1 — Thu phí kỳ: Chấp nhận định dạng kỳ `YYYY-MM` hợp lệ; từ chối định dạng sai
- **ID:** REP-02.1
- **Tiêu đề:** Xác thực định dạng chuỗi `period` trên báo cáo thu phí theo kỳ
- **Vai trò:** ADMIN / ACCOUNTANT
- **Điểm vào:** `GET /api/v1/reports/fee-collection.xlsx?period=<VALUE>`
- **Dữ liệu vào:**
  - Hợp lệ: `period=2026-10`, `period=2025-01`
  - Không hợp lệ: `period=2026-13`, `period=2026/10`, `period=2026-1`, `period=abc`
- **Kỳ vọng:**
  - Tham số hợp lệ trả về HTTP 200.
  - Tham số sai định dạng trả về HTTP 400 Bad Request với mã lỗi `INVALID_REPORT_FILTER`.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:53-64`

#### REP-02.2 — Thu phí kỳ: Từ chối khi thiếu tham số `period` hoặc kèm tham số lạ ngoài đặc tả
- **ID:** REP-02.2
- **Tiêu đề:** Chống rò rỉ tham số truy vấn lạ trên endpoint thu phí kỳ
- **Vai trò:** ADMIN / ACCOUNTANT
- **Điểm vào:**
  - Thiếu param: `GET /api/v1/reports/fee-collection.xlsx`
  - Thừa param lạ: `GET /api/v1/reports/fee-collection.xlsx?period=2026-10&extra=1`
- **Kỳ vọng:** Trả về HTTP 400 với mã lỗi `INVALID_REPORT_FILTER`.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:51,122-142`

#### REP-02.3 — Sổ quỹ tiền mặt: Chấp nhận khoảng ngày `from <= to` và `to <= today`
- **ID:** REP-02.3
- **Tiêu đề:** Xác thực khoảng ngày hợp lệ trên báo cáo sổ quỹ tiền mặt
- **Vai trò:** ADMIN / ACCOUNTANT
- **Điểm vào:** `GET /api/v1/reports/cash-receipts.xlsx?from=2026-10-01&to=2026-10-31`
- **Kỳ vọng:** Trả về HTTP 200 và file Excel chứa các giao dịch trong khoảng thời gian chỉ định.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:85-108`

#### REP-02.4 — Sổ quỹ tiền mặt: Từ chối khi `from > to` hoặc ngày `to` ở tương lai
- **ID:** REP-02.4
- **Tiêu đề:** Từ chối khoảng ngày đảo ngược hoặc vượt quá ngày hiện tại
- **Vai trò:** ADMIN / ACCOUNTANT
- **Dữ liệu vào:**
  - `from=2026-11-05&to=2026-11-01` (`from > to`)
  - `from=2026-10-01&to=2099-12-31` (`to > today`)
- **Kỳ vọng:** HTTP 400 với `INVALID_REPORT_FILTER`.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:96-103`

#### REP-02.5 — Sổ quỹ tiền mặt: Từ chối khi thiếu tham số hoặc kèm tham số lạ ngoài `from`/`to`
- **ID:** REP-02.5
- **Tiêu đề:** Kiểm tra danh sách tham số nghiêm ngặt trên sổ quỹ tiền mặt
- **Điểm vào:** `GET /api/v1/reports/cash-receipts.xlsx?from=2026-10-01` (thiếu `to`)
- **Kỳ vọng:** HTTP 400 `INVALID_REPORT_FILTER`.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:74,122-142`

#### REP-02.6 — Cư trú: Từ chối khi request có chứa bất kỳ query parameter nào
- **ID:** REP-02.6
- **Tiêu đề:** Báo cáo cư trú chỉ lấy dữ liệu snapshot hiện tại, không nhận tham số lọc
- **Điểm vào:** `GET /api/v1/reports/residency.xlsx?limit=10`
- **Kỳ vọng:** HTTP 400 `INVALID_REPORT_FILTER`.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:112`

---

### Nhóm 3: Tiêu đề HTTP, MIME Type & Tên tệp đính kèm (Headers & Content-Disposition)

#### REP-03.1 — Content-Type trả về chuẩn XLSX
- **ID:** REP-03.1
- **Tiêu đề:** Xác minh Content-Type chuẩn openxmlformats cho file Excel
- **Kỳ vọng:** Response header `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:28,147`

#### REP-03.2 — Header `Cache-Control: no-store` được áp dụng
- **ID:** REP-03.2
- **Tiêu đề:** Ngăn chặn proxy/trình duyệt lưu đệm file báo cáo nhạy cảm
- **Kỳ vọng:** Response header `Cache-Control: no-store`.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:148`

#### REP-03.3 — Tên file đính kèm khớp định dạng quy ước theo ngày/kỳ lọc
- **ID:** REP-03.3
- **Tiêu đề:** Kiểm tra header `Content-Disposition` trả về đúng tên file
- **Kỳ vọng:**
  - Thu phí kỳ: `filename="thu-theo-ky-2026-10.xlsx"`
  - Sổ quỹ: `filename="tien-mat-2026-10-01-2026-10-31.xlsx"`
  - Cư trú: `filename="cu-tru-hien-tai-<YYYYMMDD>.xlsx"`
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/api/ReportExportController.java:67,106,117`

---

### Nhóm 4: Cấu trúc tệp Excel & Toàn vẹn dữ liệu (Workbook Content & Layout)

#### REP-04.1 — Báo cáo thu phí kỳ: Đầy đủ các cột hóa đơn, số tiền, ngày phát hành và dòng tổng cộng
- **ID:** REP-04.1
- **Tiêu đề:** Kiểm tra cấu trúc sheet "Thu_theo_ky"
- **Kỳ vọng:** Có đủ các cột: Mã căn hộ, Chủ hộ, Khoản phí, Kỳ thu, Số tiền cần nộp, Đã nộp, Còn lại, Trạng thái. Dòng cuối cùng là dòng Tổng cộng tính tổng chính xác.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/application/XlsxReportWriter.java:101-125`

#### REP-04.2 — Sổ quỹ tiền mặt: Chi tiết phiếu thu, số tiền áp dụng, số dư nộp thừa và dòng tổng
- **ID:** REP-04.2
- **Tiêu đề:** Kiểm tra cấu trúc sheet "So_quy_tien_mat"
- **Kỳ vọng:** Có các cột: Mã phiếu thu, Ngày thu, Căn hộ, Người nộp, Số tiền thu, Tiền áp dụng hóa đơn, Tiền dư thừa, Người lập phiếu.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/application/XlsxReportWriter.java:160-190`

#### REP-04.3 — Báo cáo cư trú: Khối metadata 9 dòng đầu tiên, danh sách nhân khẩu kèm quan hệ chủ hộ và trạng thái
- **ID:** REP-04.3
- **Tiêu đề:** Kiểm tra cấu trúc sheet "Cu_tru_hien_tai"
- **Kỳ vọng:** Dòng 1-8 chứa thông tin đơn vị phát hành, ngày xuất, tổng số căn hộ, tổng nhân khẩu; dòng 9 là tiêu đề cột; dòng 10 trở đi là danh sách cư dân; bật tính năng AutoFilter.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/application/XlsxReportWriter.java:200-240`

#### REP-04.4 — Bọc chữ (wrap text) và tự động giãn chiều cao dòng cho trường họ tên cư dân dài
- **ID:** REP-04.4
- **Tiêu đề:** Định dạng hiển thị đẹp mắt, không bị cắt cụt văn bản khi họ tên có nhiều ký tự
- **Kỳ vọng:** Ô họ tên được bật `wrapText = true`, chiều cao dòng được điều chỉnh dựa trên độ dài chuỗi (`Math.max(15.0f, lines * 15.0f)`).
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/application/XlsxReportWriter.java:220`

---

### Nhóm 5: Giới hạn số dòng & Quản lý tài nguyên bộ nhớ (Row Limits & SXSSFWorkbook)

#### REP-05.1 — Chặn xuất báo cáo thu phí khi số dòng vượt ngưỡng 50.000
- **ID:** REP-05.1
- **Tiêu đề:** Bảo vệ server tránh cạn kiệt tài nguyên khi dữ liệu hóa đơn vượt quá 50.000 bản ghi
- **Kỳ vọng:** Ném lỗi BadRequestException `REPORT_ROW_LIMIT_EXCEEDED` (HTTP 400).
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/persistence/FeeCollectionReportReadStore.java:39`

#### REP-05.2 — Chặn xuất báo cáo tiền mặt khi số dòng vượt ngưỡng 50.000
- **ID:** REP-05.2
- **Tiêu đề:** Bảo vệ server tránh cạn kiệt tài nguyên khi phiếu thu vượt quá 50.000 bản ghi
- **Kỳ vọng:** Ném lỗi BadRequestException `REPORT_ROW_LIMIT_EXCEEDED` (HTTP 400).
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/persistence/CashReceiptsReportReadStore.java:40`

#### REP-05.3 — Chặn xuất báo cáo cư trú khi số dòng vượt ngưỡng 50.000
- **ID:** REP-05.3
- **Tiêu đề:** Bảo vệ server tránh cạn kiệt tài nguyên khi nhân khẩu vượt quá 50.000 bản ghi
- **Kỳ vọng:** Ném lỗi BadRequestException `REPORT_ROW_LIMIT_EXCEEDED` (HTTP 400).
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/persistence/ResidencyReportReadStore.java:35`

#### REP-05.4 — Đảm bảo dọn dẹp file tạm của SXSSFWorkbook trên ổ đĩa sau khi hoàn tất hoặc gặp lỗi sớm
- **ID:** REP-05.4
- **Tiêu đề:** Gọi `workbook.dispose()` để giải phóng file tạm sinh ra bởi SXSSFWorkbook
- **Kỳ vọng:** File tạm trong thư mục `java.io.tmpdir` (`poifiles`) được xóa tự động sau khi xuất stream, không gây đầy phân vùng ổ đĩa.
- **Trạng thái:** Not run
- **Nguồn:** `backend/src/main/java/vn/edu/se10/bluemoon/reporting/application/XlsxReportWriter.java:60-90`

---

### Nhóm 6: Giao diện người dùng & Điều hướng tải báo cáo (UI Downloads & UX)

#### REP-06.1 — ADMIN mở trang `/reports`: Hiển thị đầy đủ 3 khối tải báo cáo
- **ID:** REP-06.1
- **Tiêu đề:** Giao diện tải báo cáo hiển thị đủ 3 khối cho Quản trị viên
- **Vai trò:** ADMIN
- **Điểm vào:** `/reports` (hoặc `/admin/reports/residency`)
- **Kỳ vọng:** Thấy 3 khối: "Báo cáo thu phí theo kỳ", "Sổ quỹ thu tiền mặt tại quầy", "Báo cáo thống kê cư trú hiện tại".
- **Trạng thái:** Not run
- **Nguồn:** `frontend/src/screens/reporting/ReportDownloadsPage.tsx`

#### REP-06.2 — ACCOUNTANT mở trang `/reports`: Chỉ hiển thị 2 khối tải báo cáo thu phí và tiền mặt, ẩn khối cư trú
- **ID:** REP-06.2
- **Tiêu đề:** Phân quyền giao diện cho Kế toán viên
- **Vai trò:** ACCOUNTANT
- **Điểm vào:** `/reports`
- **Kỳ vọng:** Chỉ hiển thị 2 khối tài chính, khối báo cáo cư trú không xuất hiện trong DOM.
- **Trạng thái:** Not run
- **Nguồn:** `frontend/src/screens/reporting/ReportDownloadsPage.tsx`

#### REP-06.3 — Điều hướng từ thanh menu header vào trang `/reports` cho ADMIN và ACCOUNTANT
- **ID:** REP-06.3
- **Tiêu đề:** Menu "Báo cáo" xuất hiện trên Header cho ADMIN và ACCOUNTANT
- **Kỳ vọng:** Nhấp vào liên kết "Báo cáo" chuyển route chính xác tới `/reports`.
- **Trạng thái:** Not run
- **Nguồn:** `frontend/src/components/AppShell.tsx`

#### REP-06.4 — Xử lý tải file qua blob URL trong API mode và hiển thị thông báo lỗi khi API thất bại
- **ID:** REP-06.4
- **Tiêu đề:** Nhấp "Tải tệp Excel (.xlsx)" kích hoạt tải xuống trình duyệt
- **Kỳ vọng:** Trình duyệt tải về tệp có nội dung binary; nếu server trả về lỗi (ví dụ 400 hoặc 500), UI hiển thị alert lỗi rõ ràng và mở khóa nút bấm.
- **Trạng thái:** Not run
- **Nguồn:** `frontend/src/screens/reporting/ReportDownloadsPage.tsx`, `frontend/src/api/reports.ts`
