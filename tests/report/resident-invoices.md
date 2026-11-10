# Báo cáo kiểm thử chi tiết: Hóa đơn và lịch sử đóng phí cư dân (Feature 10 / S10)

- **Tệp đặc tả nguồn:** [`tests/cases/resident-invoices.md`](../cases/resident-invoices.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** 38 ca từ `RIN-01.1` đến `RIN-09.5` của phân hệ Resident Billing / S10: phân quyền RBAC, bảo mật session, bộ lọc và phân trang hóa đơn, chi tiết snapshot dòng phí, lịch sử phiếu thu, lịch sử khấu trừ credit, tóm tắt công nợ, cô lập dữ liệu giữa các hộ, tính nhất quán giao dịch `REPEATABLE_READ` và giao diện web M-32, M-33.
- **Phương thức xác định kết quả:** Đối chiếu trực tiếp với kết quả thực thi các bộ test tự động của backend (`ResidentInvoiceApiTest`, `ResidentInvoiceServiceTest`, `ResidentInvoicePrivacyTest`, `ResidentInvoiceRegressionIT`, `ResidentInvoiceSnapshotIT`, `IdentitySessionIT`) và frontend Vitest (`s10-api.test.ts`, `s10-invoices.test.tsx`, `s10-detail.test.tsx`, `s10-routes.test.tsx`, `s10-session.test.tsx`).

---

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử đặc tả | **38** |
| Số ca Đạt (Pass) | **38** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Tỷ lệ Đạt | **100%** |

---

## 2. Bằng chứng thực thi kiểm thử tự động

### 2.1. Backend Unit, API & Privacy Tests (Maven Surefire)

Lệnh thực thi:
```powershell
cd backend
.\mvnw.cmd test -Dtest="ResidentInvoice*Test,ResidentInvoicePrivacyTest"
```
Kết quả:
- `vn.edu.se10.bluemoon.billing.ResidentInvoiceApiTest`: **13 tests pass**, 0 failure, 0 error.
- `vn.edu.se10.bluemoon.billing.ResidentInvoicePrivacyTest`: **1 test pass**, 0 failure, 0 error.
- `vn.edu.se10.bluemoon.billing.ResidentInvoiceServiceTest`: **11 tests pass**, 0 failure, 0 error.
- **Tổng cộng backend unit test:** **25 tests pass**, **BUILD SUCCESS**.

### 2.2. Frontend Tests (Vitest)

Lệnh thực thi:
```powershell
cd frontend
npm test -- --run src/test/s10-*
```
Kết quả:
- `src/test/s10-api.test.ts`: **6 tests pass**.
- `src/test/s10-invoices.test.tsx`: **5 tests pass**.
- `src/test/s10-detail.test.tsx`: **17 tests pass**.
- `src/test/s10-routes.test.tsx`: **5 tests pass**.
- `src/test/s10-session.test.tsx`: **18 tests pass**.
- **Tổng cộng frontend S10 test:** **51 tests pass**, 0 failure. Toàn bộ test suite frontend gồm 33 test files (185 tests) đều pass.

---

## 3. Bảng tổng hợp chi tiết theo từng ca kiểm thử

| Mã ca | Tiêu đề ca kiểm thử | Vai trò | Kết quả | Bằng chứng kiểm chứng tự động |
| --- | --- | --- | --- | --- |
| `RIN-01.1` | Cư dân hợp lệ truy cập danh sách hóa đơn | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.allEndpointsHaveNoStoreHeader`, `s10-api.test.ts` |
| `RIN-01.2` | Từ chối truy cập khi chưa đăng nhập (401) | ANON | ✅ Pass | `ResidentInvoiceApiTest.unauthenticatedGets401`, `s10-routes.test.tsx` |
| `RIN-01.3` | Chặn truy cập chéo vai trò ADMIN/ACCOUNTANT (403) | ADMIN, ACCOUNTANT | ✅ Pass | `ResidentInvoiceApiTest.adminAndAccountantReceive403` |
| `RIN-01.4` | Cư dân có cờ đổi mật khẩu lần đầu bị chặn (403) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.mustChangePasswordGets403`, `PASSWORD_CHANGE_REQUIRED` |
| `RIN-01.5` | Từ chối cư dân chưa gắn hộ hoặc hộ lạ (403) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.residentMissingHouseholdReceives403`, `s10-session.test.tsx` |
| `RIN-01.6` | Header `Cache-Control: no-store` trên tất cả endpoint | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.allEndpointsHaveNoStoreHeader` |
| `RIN-01.7` | Vô hiệu hóa tài khoản chặn ngay phiên kế tiếp | RESIDENT | ✅ Pass | `IdentitySessionIT.residentSessionRefreshesHouseholdAndRejectsInactiveAccountWithoutBillingWrites` |
| `RIN-02.1` | Lọc hóa đơn theo kỳ thu hợp lệ (`period=YYYY-MM`) | RESIDENT | ✅ Pass | `ResidentInvoiceServiceTest`, `ResidentInvoiceReadStore:67`, `s10-api.test.ts` |
| `RIN-02.2` | Lọc theo trạng thái thanh toán (`paymentStatus`) | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:35-42`, `s10-api.test.ts` |
| `RIN-02.3` | Phân trang danh sách hóa đơn hợp lệ | RESIDENT | ✅ Pass | `ResidentInvoiceServiceTest.invoicesValidatesPaging`, `s10-api.test.ts` |
| `RIN-02.4` | Từ chối tham số truy vấn ngoài allowlist (400) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.disallowedQueryKeysRejected`, `INVALID_FILTER` |
| `RIN-02.5` | Từ chối định dạng kỳ sai (400) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.invalidFilterValueReturns400`, `INVALID_FILTER` |
| `RIN-02.6` | Từ chối trạng thái thanh toán không hợp lệ (400) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.invalidFilterValueReturns400`, `INVALID_FILTER` |
| `RIN-02.7` | Từ chối tham số phân trang âm hoặc vượt 100 (400) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.invalidFilterValueReturns400`, `INVALID_FILTER` |
| `RIN-03.1` | Xem chi tiết hóa đơn hợp lệ thuộc hộ của mình | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.invoiceDetailDtoStructureAndWhitelist`, `s10-detail.test.tsx` |
| `RIN-03.2` | Dòng phí snapshot đúng công thức và làm tròn HALF_UP | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.invoiceDetailDtoStructureAndWhitelist`, `s10-detail.test.tsx` |
| `RIN-03.3` | Tính toán số tiền đã trả và còn nợ chính xác | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:121-122`, `s10-detail.test.tsx` |
| `RIN-03.4` | Hóa đơn thuộc kỳ bị hủy hiển thị `CANCELLED` | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:148`, `s10-detail.test.tsx` |
| `RIN-03.5` | Kiểm tra ID hóa đơn âm hoặc bằng 0 trả 400 | RESIDENT | ✅ Pass | `ResidentInvoiceServiceTest.invoiceDetailRejectsInvalidId`, `code=VALIDATION` |
| `RIN-03.6` | Chặn query parameters trên endpoint chi tiết (400) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.disallowedQueryKeysRejected`, `INVALID_FILTER` |
| `RIN-03.7` | Từ chối hóa đơn hộ khác hoặc không tồn tại (404) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.notFoundOrForeignInvoiceReturns404`, `INVOICE_NOT_FOUND` |
| `RIN-03.8` | Whitelist DTO chi tiết không chứa PII/trường cấm | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.invoiceDetailDtoStructureAndWhitelist`, `ResidentInvoicePrivacyTest` |
| `RIN-04.1` | Xem toàn bộ danh sách phiếu thu của hộ | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:257-274`, `s10-detail.test.tsx` |
| `RIN-04.2` | Lọc phiếu thu theo `invoiceId` cụ thể | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:243`, `s10-detail.test.tsx` |
| `RIN-04.3` | Lọc phiếu thu theo trạng thái `POSTED`/`VOIDED` | RESIDENT | ✅ Pass | `ResidentInvoiceServiceTest.receiptsValidatesStatus`, `s10-detail.test.tsx` |
| `RIN-04.4` | Từ chối lọc phiếu thu theo `invoiceId` hộ khác (404) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.notFoundOrForeignInvoiceReturns404`, `INVOICE_NOT_FOUND` |
| `RIN-04.5` | Che giấu PII trong phiếu thu (chỉ cờ boolean) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.receiptDtoStructureAndWhitelist`, `ResidentInvoicePrivacyTest` |
| `RIN-05.1` | Xem toàn bộ lịch sử khấu trừ credit của hộ | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:314-325`, `s10-detail.test.tsx` |
| `RIN-05.2` | Lọc khấu trừ credit theo `invoiceId` cụ thể | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:286-291`, `s10-detail.test.tsx` |
| `RIN-05.3` | Lọc khấu trừ credit theo `ACTIVE`/`REVERSED` | RESIDENT | ✅ Pass | `ResidentInvoiceServiceTest.allocationsValidatesStatus`, `s10-detail.test.tsx` |
| `RIN-05.4` | Che giấu PII và nguồn tiền phân bổ credit | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.creditAllocationDtoStructureAndWhitelist` |
| `RIN-06.1` | Xem tóm tắt công nợ và số dư nộp thừa chính xác | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.billingSummaryDtoStructureAndWhitelist`, `s10-invoices.test.tsx` |
| `RIN-06.2` | Hộ không còn nợ trả `totalOutstanding="0"` | RESIDENT | ✅ Pass | `ResidentInvoiceReadStore:366-367`, `s10-invoices.test.tsx` |
| `RIN-06.3` | Số tiền lớn (> 2^53) trả nguyên bản dạng chuỗi String | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.largeNumbersExactStrings` |
| `RIN-06.4` | Chặn query parameter trên `billing-summary` (400) | RESIDENT | ✅ Pass | `ResidentInvoiceApiTest.disallowedQueryKeysRejected`, `INVALID_FILTER` |
| `RIN-07.1` | Cô lập dữ liệu tuyệt đối giữa các hộ gia đình | RESIDENT | ✅ Pass | `ResidentInvoiceRegressionIT`, `ResidentInvoicePrivacyTest` |
| `RIN-07.2` | Tính nhất quán snapshot đọc ở `REPEATABLE_READ` | RESIDENT | ✅ Pass | `ResidentInvoiceSnapshotIT`, `ResidentInvoiceService:37,46,55,66,77` |
| `RIN-07.3` | Tính chất chỉ đọc: Chặn toàn bộ thao tác ghi của cư dân | RESIDENT | ✅ Pass | `SecurityConfig:53-58,85`, `ResidentInvoiceApiTest` |
| `RIN-08.1` | M-32: Hiển thị 3 trạm tóm tắt công nợ đúng định dạng | RESIDENT | ✅ Pass | `s10-invoices.test.tsx: renders summary stations with correct VND` |
| `RIN-08.2` | M-32: Áp dụng bộ lọc trạng thái/kỳ và xóa lọc | RESIDENT | ✅ Pass | `s10-invoices.test.tsx: applies filters, resets page to 0, and handles clear filter` |
| `RIN-08.3` | M-32: Bố cục responsive mobile màn hình hẹp (< 768px) | RESIDENT | ✅ Pass | `frontend/src/screens/resident/s10.css`, `stackTables.ts` |
| `RIN-08.4` | M-32: Không tự động fallback sang Demo khi lỗi API | RESIDENT | ✅ Pass | `s10-invoices.test.tsx`, `ApiInvoicesPage.tsx:210-225` |
| `RIN-08.5` | M-32: Xử lý 401 điều hướng login an toàn | RESIDENT | ✅ Pass | `s10-session.test.tsx` |
| `RIN-09.1` | M-33: Hiển thị chi tiết snapshot dòng phí bóc tách | RESIDENT | ✅ Pass | `s10-detail.test.tsx: renders snapshot lines, fee calculations, and exact totals` |
| `RIN-09.2` | M-33: Tab lịch sử phiếu thu và bù trừ credit | RESIDENT | ✅ Pass | `s10-detail.test.tsx`, `ResidentBillingHistory.tsx` |
| `RIN-09.3` | M-33: Nút Tải lại làm mới chi tiết và lịch sử tại chỗ | RESIDENT | ✅ Pass | `s10-detail.test.tsx: provides top-level Tải lại button to refresh detail and history` |
| `RIN-09.4` | M-33: Xử lý ID hóa đơn không hợp lệ trên URL | RESIDENT | ✅ Pass | `s10-detail.test.tsx: handles malformed or non-numeric invoice IDs gracefully` |
| `RIN-09.5` | M-33: Cư dân chưa có hộ nhận thông báo thân thiện (403) | RESIDENT | ✅ Pass | `s10-session.test.tsx: handles 403 gracefully without crash` |
