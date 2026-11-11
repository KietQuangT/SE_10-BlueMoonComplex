# Báo cáo kiểm thử chi tiết: Xuất báo cáo Excel thu phí, tiền mặt và cư trú (S11)

- **Tệp đặc tả nguồn:** [`tests/cases/s11-reports.md`](../cases/s11-reports.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** 22 ca kiểm thử từ `REP-01.1` đến `REP-06.4` của phân hệ xuất báo cáo Excel (Feature 11 / S11, PBI-021, PBI-022, PBI-023, WBS 2.4.1.3): Phân quyền RBAC, kiểm tra tính hợp lệ của tham số bộ lọc, tiêu đề MIME và tệp đính kèm, định dạng bảng tính và dòng tổng cộng, giới hạn an toàn 50.000 dòng chống cạn kiệt bộ nhớ, dọn dẹp file tạm SXSSFWorkbook, và giao diện tải báo cáo.

---

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **22** |
| Số ca Đạt (Pass) | **22** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Số ca Chưa thực thi (Not run) | **0** |
| **Tỷ lệ Đạt** | **100.00%** |

---

## 2. Môi trường & Bằng chứng thực thi

- **Môi trường chạy:**
  - Hệ điều hành: Windows 11 Pro, JDK 21.0.13, Maven 3.9.11, Node.js v20+, npm 10+.
  - Backend: Spring Boot 4.0.9, Apache POI 5.4.0 (`poi-ooxml`).
  - Frontend: React 18, Vite 6, Vitest v3.2.7.
  - Ngày kiểm thử: 10/11/2026.
- **Bằng chứng kiểm thử tự động:**
  - **Backend Surefire:** 32/32 tests đạt (0 failures, 0 errors, 0 skipped, thời gian chạy 42.77s)
    - `vn.edu.se10.bluemoon.reporting.ReportApiTest`: 10 tests pass
    - `vn.edu.se10.bluemoon.reporting.ReportingReadStoreTest`: 6 tests pass
    - `vn.edu.se10.bluemoon.reporting.ReportingServiceTest`: 3 tests pass
    - `vn.edu.se10.bluemoon.reporting.ReportingWorkbookLimitQaTest`: 3 tests pass
    - `vn.edu.se10.bluemoon.reporting.ReportingWorkbookQaTest`: 2 tests pass
    - `vn.edu.se10.bluemoon.reporting.XlsxReportWriterTest`: 8 tests pass
  - **Frontend Vitest:** 31 tests phân hệ S11 đạt (trong tổng số 234/234 tests toàn dự án đạt)
    - `src/test/s11-ui-api.test.ts`: 13 tests pass
    - `src/test/s11-ui-page.test.tsx`: 10 tests pass
    - `src/test/s11-ui-routes.test.tsx`: 6 tests pass
    - `src/test/s11-ui-session.test.tsx`: 2 tests pass

---

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Bằng chứng kiểm thử |
| --- | --- | --- | --- | --- |
| `REP-01.1` | Khách vãng lai (ANON) bị từ chối 401 trên 3 endpoint | ANON | ✅ Đạt (Pass) | `ReportApiTest.java`: `unauthenticatedIsRejectedWith401` |
| `REP-01.2` | Cư dân (RESIDENT) bị chặn 403 trên 3 endpoint | RESIDENT | ✅ Đạt (Pass) | `ReportApiTest.java`: `residentIsForbiddenWith403` |
| `REP-01.3` | ACCOUNTANT tải được thu phí/tiền mặt (200), bị chặn cư trú (403) | ACCOUNTANT | ✅ Đạt (Pass) | `ReportApiTest.java`: `accountantCanExportFinanceReportsButNotResidency` |
| `REP-01.4` | ADMIN có toàn quyền tải cả 3 loại báo cáo (200) | ADMIN | ✅ Đạt (Pass) | `ReportApiTest.java`: `adminCanExportAllReports` |
| `REP-02.1` | Thu phí kỳ: Kỳ `YYYY-MM` hợp lệ (200), sai định dạng (400) | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `feeCollectionValidatesPeriodFormat` |
| `REP-02.2` | Thu phí kỳ: Thiếu param hoặc kèm tham số lạ bị từ chối (400) | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `feeCollectionRejectsMissingOrExtraParams` |
| `REP-02.3` | Sổ quỹ tiền mặt: Khoảng ngày `from <= to` và `<= today` hợp lệ (200) | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `cashReceiptsValidatesDateRange` |
| `REP-02.4` | Sổ quỹ tiền mặt: `from > to` hoặc `to > today` bị từ chối (400) | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `cashReceiptsRejectsReversedOrFutureDate` |
| `REP-02.5` | Sổ quỹ tiền mặt: Thiếu param hoặc kèm tham số lạ bị từ chối (400) | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `cashReceiptsRejectsExtraParams` |
| `REP-02.6` | Cư trú: Từ chối nếu có bất kỳ query param nào (400) | ADMIN | ✅ Đạt (Pass) | `ReportApiTest.java`: `residencyRejectsQueryParams` |
| `REP-03.1` | Content-Type trả về chuẩn XLSX openxmlformats | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `assertHeaderContentType` |
| `REP-03.2` | Header `Cache-Control: no-store` bảo vệ dữ liệu | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `assertHeaderCacheControl` |
| `REP-03.3` | Content-Disposition trả về đúng tên file theo quy ước | ADMIN, ACC | ✅ Đạt (Pass) | `ReportApiTest.java`: `assertFilenamePatterns` |
| `REP-04.1` | Báo cáo thu phí kỳ: Tiêu đề cột chuẩn, dòng tổng cộng chính xác | ADMIN, ACC | ✅ Đạt (Pass) | `XlsxReportWriterTest.java`: `testFeeCollectionWorkbook` |
| `REP-04.2` | Sổ quỹ tiền mặt: Chi tiết phiếu thu, áp dụng hóa đơn và số dư thừa | ADMIN, ACC | ✅ Đạt (Pass) | `XlsxReportWriterTest.java`: `testCashReceiptsWorkbook` |
| `REP-04.3` | Báo cáo cư trú: Khối metadata 9 dòng, thông tin nhân khẩu và AutoFilter | ADMIN | ✅ Đạt (Pass) | `XlsxReportWriterTest.java`: `testResidencyWorkbook` |
| `REP-04.4` | Bọc chữ (wrap text) và tự giãn chiều cao dòng cho họ tên dài | ADMIN, ACC | ✅ Đạt (Pass) | `XlsxReportWriterTest.java`: `testWrapTextAndRowHeight` |
| `REP-05.1` | Thu phí kỳ: Vượt 50.000 dòng ném `REPORT_ROW_LIMIT_EXCEEDED` | ADMIN, ACC | ✅ Đạt (Pass) | `ReportingWorkbookLimitQaTest.java`: `testFeeCollectionLimit` |
| `REP-05.2` | Sổ quỹ tiền mặt: Vượt 50.000 dòng ném `REPORT_ROW_LIMIT_EXCEEDED` | ADMIN, ACC | ✅ Đạt (Pass) | `ReportingWorkbookLimitQaTest.java`: `testCashReceiptsLimit` |
| `REP-05.3` | Cư trú: Vượt 50.000 dòng ném `REPORT_ROW_LIMIT_EXCEEDED` | ADMIN | ✅ Đạt (Pass) | `ReportingWorkbookLimitQaTest.java`: `testResidencyLimit` |
| `REP-05.4` | Dọn dẹp file tạm SXSSFWorkbook (gọi `dispose()`), không rò rỉ đĩa | ADMIN, ACC | ✅ Đạt (Pass) | `ReportingWorkbookQaTest.java`: `testTempFileDisposal` |
| `REP-06.1` | UI `/reports`: ADMIN thấy đầy đủ 3 khối tải báo cáo | ADMIN | ✅ Đạt (Pass) | `s11-ui-page.test.tsx`: `renders all three report blocks for ADMIN` |
| `REP-06.2` | UI `/reports`: ACCOUNTANT chỉ thấy 2 khối tài chính, ẩn khối cư trú | ACCOUNTANT | ✅ Đạt (Pass) | `s11-ui-page.test.tsx`: `hides residency report for ACCOUNTANT` |
| `REP-06.3` | Điều hướng từ Header menu vào trang `/reports` | ADMIN, ACC | ✅ Đạt (Pass) | `s11-ui-session.test.tsx`: `navigation menu linking to /reports` |
| `REP-06.4` | Tải file qua blob URL trong API mode và hiển thị thông báo lỗi khi API lỗi | ADMIN, ACC | ✅ Đạt (Pass) | `s11-ui-page.test.tsx`: `handles download success and error banners` |

---

## 4. Chi tiết các phát hiện và lưu ý kỹ thuật

1. **Cảnh báo duplicate dependency trong `backend/pom.xml`:**
   - Khi chạy Maven build, xuất hiện cảnh báo:
     `'dependencies.dependency.(groupId:artifactId:type:classifier)' must be unique: org.apache.poi:poi-ooxml:jar -> duplicate declaration of version 5.4.0 @ line 22, column 17`
   - **Vị trí phát hiện:** `backend/pom.xml` có 2 khối khai báo dependency `org.apache.poi:poi-ooxml:5.4.0` (dòng 22 và dòng 224).
   - **Đề xuất:** Xóa bớt một khối khai báo trùng lặp tại dòng 224 để `pom.xml` hợp chuẩn Maven. Tuân thủ phạm vi thao tác, file này nằm ngoài `tests/` nên không tự ý chỉnh sửa và chuyển để người dùng quyết định.
2. **Hiệu năng & Tài nguyên:**
   - Việc áp dụng `SXSSFWorkbook` với window size nhỏ giúp việc xuất các file dữ liệu lớn duy trì mức sử dụng RAM thấp, tránh `OutOfMemoryError`.
   - Ngưỡng 50.000 dòng được bảo vệ ở cả 3 ReadStore đảm bảo tính ổn định của hệ thống trước các truy vấn tải dữ liệu quá lớn.
