# Báo cáo kiểm thử — BlueMoon Complex

Thư mục này chứa báo cáo theo từng bộ ca. Báo cáo tổng hợp dưới đây là **snapshot lịch sử**, không phải trạng thái CI hay báo cáo mới nhất của nhánh S09.

## Snapshot ngày 08/11/2026

- Mã nguồn đối chiếu: main, commit 59dfdc9.
- Phạm vi: 11 bộ, tổng cộng 432 ca.
- Kết quả tổng hợp: 429 Pass, 3 Fail, 0 Blocked (**99,31% Pass**).
- Môi trường lúc chạy: Windows, Java 21, MySQL cục bộ cô lập, frontend Vite và Playwright Chromium headless. Chi tiết từng môi trường/bằng chứng nằm trong các báo cáo bên dưới.

| Bộ ca | Tổng | Pass | Fail | Báo cáo |
| --- | ---: | ---: | ---: | --- |
| System | 10 | 8 | 2 | [system.md](system.md) |
| Auth | 29 | 29 | 0 | [auth.md](auth.md) |
| Accounts | 10 | 10 | 0 | [accounts.md](accounts.md) |
| Households | 11 | 11 | 0 | [households.md](households.md) |
| Residents | 15 | 15 | 0 | [residents.md](residents.md) |
| Declarations | 21 | 21 | 0 | [declarations.md](declarations.md) |
| Fees, billing và voluntary | 30 | 30 | 0 | [fees.md](fees.md) |
| RBAC và truy cập chéo hộ | 156 | 156 | 0 | [rbac.md](rbac.md) |
| Routes | 135 | 134 | 1 | [routes.md](routes.md) |
| UI chung | 12 | 12 | 0 | [ui-common.md](ui-common.md) |
| E2E liên phân hệ | 3 | 3 | 0 | [e2e.md](e2e.md) |
| **Tổng** | **432** | **429** | **3** | — |

## Findings trong snapshot

- **BUG-001, Minor:** không thấy dòng log request cho một số request thành công/401 dù request ID được đặt vào MDC.
- **BUG-002, Minor:** kỳ vọng route ADMIN trong bộ ca cũ chưa đồng bộ với quyết định D25.
- **BUG-003, Minor:** tài liệu cũ trỏ script tools/dev/; repo hiện đặt script local trong scripts/. Hướng dẫn thủ công đã được cập nhật ở [README gốc](../../README.md).

Chi tiết tái hiện và các lỗi khác: [01-bao-cao-loi.md](01-bao-cao-loi.md). Không suy ra rằng các findings lịch sử còn tồn tại trên mã hiện tại nếu chưa chạy lại đúng ca.

## QA S09 ngày 09/11/2026

QA mới nhất nằm trong [docs/VALIDATION.md](../../docs/VALIDATION.md), không gộp vào snapshot 432 ca:

- Backend verify: 227 test pass; MySQL 8.4 cô lập, integration: 146 test pass.
- Frontend: 134 test pass; TypeScript/Vite build pass.
- Chưa kiểm E2E UI→API trên browser thật, CI remote hoặc hiệu năng 10.000 hồ sơ.

## QA S10 (Feature 10: Hóa đơn & Lịch sử đóng phí cư dân) ngày 10/11/2026

Báo cáo chi tiết và kết quả thực thi tại [resident-invoices.md](resident-invoices.md):

- Phạm vi: 38 ca kiểm thử đặc tả từ `RIN-01.1` đến `RIN-09.5` (Feature 10 / S10, WBS 2.4.1.2).
- Kết quả đặc tả: **38 Pass**, 0 Fail, 0 Blocked (**100% Pass**).
- Backend unit/API: **25 test pass** (Surefire: `ResidentInvoiceApiTest`, `ResidentInvoiceServiceTest`, `ResidentInvoicePrivacyTest`).
- Frontend Vitest: **51 test pass** (5 test files: `s10-api.test.ts`, `s10-invoices.test.tsx`, `s10-detail.test.tsx`, `s10-routes.test.tsx`, `s10-session.test.tsx`).
- Giới hạn: kiểm thử tích hợp DB thật và NFR 10.000 hồ sơ xem tại `docs/VALIDATION.md` (chuyển S14).

## QA S11 (Feature 11: Xuất báo cáo Excel thu phí, tiền mặt và cư trú) ngày 10/11/2026

Báo cáo chi tiết và kết quả thực thi tại [s11-reports.md](s11-reports.md):

- Phạm vi: 22 ca kiểm thử đặc tả từ `REP-01.1` đến `REP-06.4` (Feature 11 / S11, WBS 2.4.1.3, PBI-021, PBI-022, PBI-023).
- Kết quả đặc tả: **22 Pass**, 0 Fail, 0 Blocked (**100% Pass**).
- Backend unit/API: **32 test pass** (Surefire: `ReportApiTest`, `ReportingReadStoreTest`, `ReportingServiceTest`, `ReportingWorkbookLimitQaTest`, `ReportingWorkbookQaTest`, `XlsxReportWriterTest`).
- Frontend Vitest: **31 test pass** (4 test files: `s11-ui-api.test.ts`, `s11-ui-page.test.tsx`, `s11-ui-routes.test.tsx`, `s11-ui-session.test.tsx`).
- Bằng chứng và kiểm thử tích hợp: `docs/VALIDATION.md`.

