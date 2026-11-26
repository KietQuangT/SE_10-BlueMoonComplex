# Sơ đồ Tuần tự UML (Sequence Diagrams) — BlueMoon Complex (Sprint 1)

Tài liệu đặc tả tương tác theo thời gian giữa Client, Spring Security Filter Chain, Controllers, Services, Stores/Repositories và MySQL trong hệ thống **BlueMoon Complex (SE_10)** bằng Mermaid `sequenceDiagram`, đối chiếu trực tiếp với mã nguồn `backend/src/main/java/vn/edu/se10/bluemoon/`.

## Danh mục sơ đồ

| Mã | Kịch bản tương tác | Thành phần chính tham gia |
| :--- | :--- | :--- |
| **SD-01** | CSRF, Đăng nhập, Chống brute-force & Đổi mật khẩu lần đầu | `AuthController`, `LoginAttemptService`, `MustChangePasswordFilter`, `PasswordController`, `AuditService` |
| **SD-02** | Quản lý tài khoản, Cấp mật khẩu tạm & Mở khóa bởi Admin | `AccountController`, `AccountProvisioningService`, `AccountManagementService`, `MockEmailService` |
| **SD-03** | Khai báo tạm vắng, Phê duyệt / Từ chối & Xác nhận trở về | `DeclarationController`, `DeclarationService`, `AuditService` |
| **SD-04** | Thiết lập đơn giá, Dự tính phí & Vòng đời kỳ thu | `FeeRateController`, `FeeCalculationService`, `BillingPeriodController`, `BillingPeriodService`, `BillingSnapshotStore` |
| **SD-05** | Đóng góp tự nguyện & Thu tiền mặt tại quầy (Idempotency + FIFO) | `VoluntaryController`, `PaymentController`, `PaymentService`, `PaymentInvoiceQuery`, `BillingReceiptGuard` |
| **SD-06** | Phân bổ số dư có, Hủy biên lai & Đảo phân bổ công nợ | `PaymentController`, `PaymentService`, `PaymentInvoiceAccess`, `ReceiptAmounts`, `CreditAllocationAmounts` |
| **SD-07** | Cư dân tra cứu hóa đơn & Xuất 3 báo cáo XLSX (50.000 dòng) | `ResidentInvoiceController`, `ReportExportController`, `XlsxReportWriter`, `*ReadStore` |
| **SD-08** | Gói dữ liệu cơ quan S12 (Phê duyệt 2 Admin, Tải ZIP & Retention 60s) | `AgencyDataPackageController`, `AgencyDataPackageService`, `AgencyDataPackageWriter`, `AgencyPackageRetentionScheduler` |

---

## SD-01: CSRF, Đăng nhập, Chống brute-force & Đổi mật khẩu lần đầu

```mermaid
sequenceDiagram
    actor User as Người dùng (SPA)
    participant Filters as CurrentAccountFilter / MustChangePasswordFilter
    participant Auth as AuthController
    participant Attempt as LoginAttemptService & LoginLockPolicy
    participant PwdCtrl as PasswordController & PasswordChangeService
    participant Audit as AuditService
    participant DB as MySQL (users / SPRING_SESSION)

    User->>Auth: GET /api/v1/auth/csrf
    Auth-->>User: 200 OK (CSRF Token trong cookie/header)

    User->>Auth: POST /api/v1/auth/login (username, password, X-CSRF-TOKEN)
    Auth->>Attempt: Kiểm tra trạng thái khóa tạm (ADMIN được miễn trừ)
    Attempt->>DB: Đọc locked_until và failed_attempts
    alt Đang bị khóa tạm
        Attempt-->>Auth: LockedException (còn thời gian khóa)
        Auth-->>User: 423 Locked / 401 Unauthorized
    else Cho phép xác thực
        Auth->>DB: Nạp user và kiểm tra BCryptPasswordEncoder
        alt Sai mật khẩu
            Auth->>Attempt: recordFailure() -> áp dụng bậc 1/5/10/15/30/60/+60 phút
            Attempt->>DB: Cập nhật locked_until
            Auth->>Audit: record(LOGIN_FAILED)
            Auth-->>User: 401 Unauthorized
        else Đúng mật khẩu
            Auth->>Attempt: resetFailures()
            Auth->>Auth: sessionStrategy.onAuthentication() (đổi Session ID & CSRF)
            Auth->>DB: Lưu SPRING_SESSION mới
            Auth->>Audit: record(LOGIN_SUCCESS)
            Auth-->>User: 200 OK (user info, mustChangePassword)
        end
    end

    opt Tài khoản có mustChangePassword = true
        User->>Filters: Gọi API nghiệp vụ (vd: GET /api/v1/households)
        Filters-->>User: 403 Forbidden (bắt buộc đổi mật khẩu)
        User->>PwdCtrl: POST /api/v1/auth/change-password
        PwdCtrl->>PwdCtrl: Kiểm tra >= 8 ký tự và <= 72 UTF-8 bytes
        PwdCtrl->>DB: Cập nhật hash BCrypt mới, tắt mustChangePassword
        PwdCtrl->>DB: Xóa toàn bộ SPRING_SESSION của user hiện tại
        PwdCtrl->>Audit: record(PASSWORD_CHANGED)
        PwdCtrl-->>User: 200 OK (buộc đăng nhập lại)
    end
```

---

## SD-02: Quản lý tài khoản, Cấp mật khẩu tạm & Mở khóa bởi Admin

```mermaid
sequenceDiagram
    actor Admin as Quản trị viên (ADMIN)
    participant AccCtrl as AccountController & AccountListController
    participant Prov as AccountProvisioningService
    participant Mgmt as AccountManagementService
    participant Mail as MockEmailService
    participant Audit as AuditService
    participant DB as MySQL (users / mock_emails)

    Admin->>AccCtrl: GET /api/v1/accounts
    AccCtrl->>DB: Truy vấn danh sách tài khoản
    AccCtrl-->>Admin: 200 OK (danh sách tài khoản & trạng thái)

    Admin->>AccCtrl: POST /api/v1/accounts (username, role, email, householdId)
    AccCtrl->>Prov: createAccount(request)
    Prov->>Prov: Sinh mật khẩu tạm ngẫu nhiên & băm BCrypt
    Prov->>DB: Lưu user với must_change_password = true
    Prov->>Mail: sendTemporaryPassword(email, tempPassword)
    Mail->>DB: Lưu bản ghi vào bảng mock_emails
    Prov->>Audit: record(ACCOUNT_CREATED)
    Prov-->>AccCtrl: AccountDto
    AccCtrl-->>Admin: 201 Created

    Admin->>AccCtrl: POST /api/v1/accounts/{id}/unlock
    AccCtrl->>Mgmt: unlock(accountId)
    Mgmt->>DB: Reset failed_attempts = 0 và xóa locked_until
    Mgmt->>Audit: record(ACCOUNT_UNLOCKED)
    Mgmt-->>AccCtrl: Status
    AccCtrl-->>Admin: 200 OK

    Admin->>AccCtrl: POST /api/v1/accounts/{id}/disable (hoặc /enable)
    AccCtrl->>Mgmt: updateStatus(accountId)
    Mgmt->>DB: Cập nhật active = false và thu hồi SPRING_SESSION nếu vô hiệu hóa
    Mgmt->>Audit: record(ACCOUNT_DISABLED)
    Mgmt-->>AccCtrl: Status
    AccCtrl-->>Admin: 200 OK
```

---

## SD-03: Khai báo tạm vắng, Phê duyệt / Từ chối & Xác nhận trở về

```mermaid
sequenceDiagram
    actor Resident as Cư dân (RESIDENT)
    actor Leader as Tổ trưởng (LEADER)
    participant DeclCtrl as DeclarationController
    participant DeclSvc as DeclarationService
    participant Audit as AuditService
    participant DB as MySQL (residents / residency_changes)

    Resident->>DeclCtrl: POST /api/v1/residency-declarations
    DeclCtrl->>DeclSvc: submitDeclaration(actor, payload)
    DeclSvc->>DB: Kiểm tra nhân khẩu thuộc hộ và đang cư trú
    DeclSvc->>DB: Lưu đơn trạng thái PENDING
    DeclSvc->>Audit: record(DECLARATION_SUBMITTED)
    DeclSvc-->>DeclCtrl: DeclarationDto
    DeclCtrl-->>Resident: 201 Created

    opt Cư dân tự hủy khi đơn còn PENDING
        Resident->>DeclCtrl: POST /api/v1/residency-declarations/{id}/cancel
        DeclCtrl->>DeclSvc: cancel(actor, id)
        DeclSvc->>DB: Kiểm tra quyền sở hữu & trạng thái PENDING -> CANCELLED
        DeclSvc->>Audit: record(DECLARATION_CANCELLED)
        DeclSvc-->>Resident: 200 OK
    end

    Leader->>DeclCtrl: GET /api/v1/residents/suggest (hoặc GET /api/v1/residency-declarations)
    DeclCtrl->>DB: Truy vấn danh sách đơn & gợi ý nhân khẩu
    DeclCtrl-->>Leader: 200 OK

    alt Phê duyệt đơn
        Leader->>DeclCtrl: POST /api/v1/residency-declarations/{id}/approve
        DeclCtrl->>DeclSvc: approve(leader, id)
        DeclSvc->>DB: Cập nhật trạng thái APPROVED & thông tin tạm vắng nhân khẩu
        DeclSvc->>Audit: record(DECLARATION_APPROVED)
        DeclSvc-->>Leader: 200 OK
    else Từ chối đơn
        Leader->>DeclCtrl: POST /api/v1/residency-declarations/{id}/reject
        DeclCtrl->>DeclSvc: reject(leader, id, reason)
        DeclSvc->>DB: Cập nhật trạng thái REJECTED kèm lý do
        DeclSvc->>Audit: record(DECLARATION_REJECTED)
        DeclSvc-->>Leader: 200 OK
    end

    Leader->>DeclCtrl: POST /api/v1/residents/{id}/return
    DeclCtrl->>DeclSvc: markReturned(leader, residentId)
    DeclSvc->>DB: Đóng đợt tạm vắng & khôi phục trạng thái cư trú
    DeclSvc->>Audit: record(RESIDENT_RETURNED)
    DeclSvc-->>Leader: 200 OK
```

---

## SD-04: Thiết lập đơn giá, Dự tính phí & Vòng đời kỳ thu

```mermaid
sequenceDiagram
    actor Accountant as Kế toán (ACCOUNTANT)
    actor Leader as Tổ trưởng (LEADER)
    participant FeeCtrl as FeeRateController & FeePreviewController
    participant FeeCalc as FeeCalculationService & FeeCalculator
    participant BilCtrl as BillingPeriodController
    participant BilSvc as BillingPeriodService
    participant Snap as BillingSnapshotStore & BillingPeriodRules
    participant Audit as AuditService
    participant DB as MySQL (billing_* / invoices)

    Accountant->>FeeCtrl: POST /api/v1/fee-rates (hoặc PUT kỳ tương lai)
    FeeCtrl->>DB: Lưu đơn giá (AREA hoặc PER_HOUSEHOLD)
    FeeCtrl-->>Accountant: 200 OK

    Accountant->>BilCtrl: POST /api/v1/billing-periods/{id}/preview
    BilCtrl->>BilSvc: preview(periodId)
    BilSvc->>FeeCalc: Tính phí từng hộ qua FeeCalculator (HALF_UP)
    FeeCalc-->>BilSvc: Danh sách khoản thu dự kiến
    BilSvc-->>Accountant: 200 OK (bảng dự tính phí)

    Accountant->>BilCtrl: POST /api/v1/billing-periods/{id}/submit
    BilCtrl->>BilSvc: submit(periodId)
    BilSvc->>Snap: Chụp snapshot hộ khẩu vào billing_submission_items
    BilSvc->>DB: Chuyển kỳ thu sang SUBMITTED
    BilSvc->>Audit: record(BILLING_SUBMITTED)
    BilSvc-->>Accountant: 200 OK

    alt Tổ trưởng trả lại nháp
        Leader->>BilCtrl: POST /api/v1/billing-periods/{id}/return
        BilCtrl->>BilSvc: returnToDraft(periodId, reason)
        BilSvc->>DB: Trả trạng thái kỳ thu về DRAFT
        BilSvc->>Audit: record(BILLING_RETURNED)
        BilSvc-->>Leader: 200 OK
    else Kế toán phát hành chính thức (sau duyệt)
        Accountant->>BilCtrl: POST /api/v1/billing-periods/{id}/issue
        BilCtrl->>BilSvc: issue(periodId)
        BilSvc->>BilSvc: Kiểm tra quy tắc qua BillingPeriodRules
        BilSvc->>DB: Chuyển ISSUED, sinh bản ghi invoices & invoice_lines
        BilSvc->>Audit: record(BILLING_ISSUED)
        BilSvc-->>Accountant: 200 OK
    end
```

---

## SD-05: Đóng góp tự nguyện & Thu tiền mặt tại quầy (Idempotency + FIFO)

```mermaid
sequenceDiagram
    actor Accountant as Kế toán (ACCOUNTANT)
    participant VolCtrl as VoluntaryController & VoluntaryContributionService
    participant PayCtrl as PaymentController
    participant PaySvc as PaymentService (READ_COMMITTED)
    participant Query as PaymentInvoiceQuery
    participant Guard as BillingReceiptGuard
    participant Audit as AuditService
    participant DB as MySQL (payment_* / invoices)

    opt Ghi nhận đóng góp tự nguyện
        Accountant->>VolCtrl: POST /api/v1/voluntary-campaigns/{id}/contributions
        VolCtrl->>DB: Lưu voluntary_contributions
        VolCtrl->>Audit: record(VOLUNTARY_CONTRIBUTION_RECORDED)
        VolCtrl-->>Accountant: 201 Created
    end

    Accountant->>PayCtrl: POST /api/v1/payments/receipt (header: Idempotency-Key)
    PayCtrl->>PaySvc: recordReceipt(actor, idempotencyKey, request)
    PaySvc->>DB: command(): kiểm tra payment_commands theo (actor, key)
    alt Đã tồn tại Idempotency-Key và khớp payload hash
        PaySvc-->>PayCtrl: Trả về biên lai đã lưu (replay)
        PayCtrl-->>Accountant: 200 OK
    else Khác payload hash
        PaySvc-->>PayCtrl: IdempotencyConflictException
        PayCtrl-->>Accountant: 409 Conflict
    else Giao dịch mới
        PaySvc->>DB: lockHousehold(): khóa hàng trong payment_household_locks
        PaySvc->>Query: Tìm hóa đơn UNPAID/PARTIAL theo thứ tự FIFO
        PaySvc->>PaySvc: Phân bổ tiền mặt vào các hóa đơn từ cũ đến mới
        opt Tiền thu lớn hơn tổng nợ hiện tại
            PaySvc->>PaySvc: Ghi phần dư vào credit balance của hộ khẩu
        end
        PaySvc->>Guard: incrementReceiptCount(affectedBillingPeriods)
        PaySvc->>DB: Lưu payment_receipts, cập nhật invoices & payment_commands
        PaySvc->>Audit: record(PAYMENT_RECEIPT_CREATED)
        PaySvc-->>PayCtrl: ReceiptResponse
        PayCtrl-->>Accountant: 201 Created
    end
```

---

## SD-06: Phân bổ số dư có, Hủy biên lai & Đảo phân bổ công nợ

```mermaid
sequenceDiagram
    actor Accountant as Kế toán (ACCOUNTANT)
    participant PayCtrl as PaymentController
    participant PaySvc as PaymentService (READ_COMMITTED)
    participant Access as PaymentInvoiceAccess & CreditAllocationAmounts
    participant Guard as BillingReceiptGuard
    participant Audit as AuditService
    participant DB as MySQL (payment_* / invoices)

    alt Phân bổ số dư có vào hóa đơn còn nợ
        Accountant->>PayCtrl: POST /api/v1/payments/credit-allocations (Idempotency-Key)
        PayCtrl->>PaySvc: allocateCredit(actor, key, request)
        PaySvc->>DB: lockHousehold() & đọc credit balance khả dụng
        PaySvc->>Access: Kiểm tra hóa đơn mở và tính phần phân bổ
        PaySvc->>DB: Lưu payment_credit_allocations & sources, giảm dư nợ invoice
        PaySvc->>Audit: record(CREDIT_ALLOCATED)
        PaySvc-->>Accountant: 201 Created
    else Hủy biên lai thu tiền mặt
        Accountant->>PayCtrl: POST /api/v1/payments/receipts/{id}/void
        PayCtrl->>PaySvc: voidReceipt(actor, receiptId, reason)
        PaySvc->>DB: lockHousehold() & kiểm tra biên lai chưa bị void hoặc dùng hết
        PaySvc->>DB: Đánh dấu biên lai VOIDED, khôi phục dư nợ invoices & credit balance
        PaySvc->>Guard: Giảm bộ đếm biên lai hợp lệ của kỳ thu liên quan
        PaySvc->>Audit: record(PAYMENT_RECEIPT_VOIDED)
        PaySvc-->>Accountant: 200 OK
    else Đảo giao dịch phân bổ số dư có
        Accountant->>PayCtrl: POST /api/v1/payments/credit-allocations/{id}/reverse
        PayCtrl->>PaySvc: reverseAllocation(actor, allocationId)
        PaySvc->>DB: lockHousehold() & hoàn lại credit balance cho hộ khẩu
        PaySvc->>DB: Cập nhật trạng thái allocation REVERSED & dư nợ invoice
        PaySvc->>Audit: record(CREDIT_ALLOCATION_REVERSED)
        PaySvc-->>Accountant: 200 OK
    end
```

---

## SD-07: Cư dân tra cứu hóa đơn & Xuất 3 báo cáo XLSX (50.000 dòng)

```mermaid
sequenceDiagram
    actor Resident as Cư dân (RESIDENT)
    actor Staff as Kế toán / Tổ trưởng
    participant ResCtrl as ResidentInvoiceController
    participant ResSvc as ResidentInvoiceService & ResidentInvoiceReadStore
    participant RepCtrl as ReportExportController
    participant RepSvc as FeeCollection / CashReceipts / ResidencyReportService
    participant Writer as XlsxReportWriter (SXSSFWorkbook)
    participant Audit as AuditService
    participant DB as MySQL

    Resident->>ResCtrl: GET /api/v1/resident/invoices (chỉ xem hóa đơn hộ mình)
    ResCtrl->>ResSvc: listOwnInvoices(currentAccount)
    ResSvc->>DB: Truy vấn hóa đơn đã phát hành của hộ thuộc currentAccount
    ResSvc-->>ResCtrl: Danh sách hóa đơn & lịch sử thanh toán
    ResCtrl-->>Resident: 200 OK

    Staff->>RepCtrl: GET /api/v1/reports/{fee-collection|cash-receipts|residency}.xlsx
    RepCtrl->>RepSvc: generateReport(filters)
    RepSvc->>DB: Đếm và đọc dữ liệu qua ReadStore
    alt Vượt quá 50.000 dòng dữ liệu
        RepSvc-->>RepCtrl: ReportRowLimitExceededException
        RepCtrl-->>Staff: 422 Unprocessable Entity / 400 Bad Request
    else Trong giới hạn <= 50.000 dòng
        RepSvc->>Writer: streamWorkbook(rows) (cửa sổ 100 dòng SXSSFWorkbook)
        Writer-->>RepCtrl: Luồng byte Excel .xlsx
        RepCtrl->>Audit: record(REPORT_EXPORTED)
        RepCtrl-->>Staff: 200 OK (Content-Disposition: attachment .xlsx)
    end
```

---

## SD-08: Gói dữ liệu cơ quan S12 (Phê duyệt 2 Admin, Tải ZIP & Retention 60s)

```mermaid
sequenceDiagram
    actor Staff as Kế toán / Tổ trưởng
    actor Admin1 as Admin 1 (ADMIN)
    actor Admin2 as Admin 2 (ADMIN khác Admin 1)
    participant PkgCtrl as AgencyDataPackageController
    participant PkgSvc as AgencyDataPackageService
    participant Writer as AgencyDataPackageWriter
    participant Store as AgencyDataPackageStore
    participant Sched as AgencyPackageRetentionScheduler
    participant Audit as AuditService
    participant DB as MySQL (agency_data_packages*)

    Staff->>PkgCtrl: POST /api/v1/agency-data-packages
    PkgCtrl->>PkgSvc: requestPackage(staff, criteria)
    PkgSvc->>Store: Lưu yêu cầu trạng thái PENDING_APPROVAL
    PkgSvc->>Audit: record(AGENCY_PACKAGE_REQUESTED)
    PkgSvc-->>Staff: 201 Created

    Admin1->>PkgCtrl: POST /api/v1/agency-data-packages/{id}/approve
    PkgCtrl->>PkgSvc: approve(admin1, id)
    PkgSvc->>Store: Ghi nhận phê duyệt lần 1 (approver1 = admin1)
    PkgSvc->>Audit: record(AGENCY_PACKAGE_FIRST_APPROVED)
    PkgSvc-->>Admin1: 200 OK

    Admin2->>PkgCtrl: POST /api/v1/agency-data-packages/{id}/approve
    PkgCtrl->>PkgSvc: approve(admin2, id)
    PkgSvc->>PkgSvc: Xác minh admin2 != admin1 (dual-admin separation)
    PkgSvc->>DB: Truy vấn danh sách hộ khẩu & nhân khẩu theo phạm vi gói
    PkgSvc->>Writer: Đóng gói ZIP & tính SHA-256 checksum
    PkgSvc->>Store: Lưu nội dung vào agency_data_package_contents (hạn tải 7 ngày, lưu 30 ngày)
    PkgSvc->>Audit: record(AGENCY_PACKAGE_READY)
    PkgSvc-->>Admin2: 200 OK

    Admin2->>PkgCtrl: GET /api/v1/agency-data-packages/{id}/download
    PkgCtrl->>Store: Kiểm tra trong cửa sổ tải 7 ngày
    PkgCtrl->>Audit: record(AGENCY_PACKAGE_DOWNLOADED)
    PkgCtrl-->>Admin2: 200 OK (ZIP + X-Checksum-Sha256)

    loop Mỗi 60.000 ms (fixedDelay)
        Sched->>Store: Tìm gói đã quá 30 ngày giữ nội dung
        Sched->>DB: Xóa binary trong agency_data_package_contents
        Sched->>Audit: recordPackageRetentionCleanup() (giữ metadata & audit 5 năm)
    end
```
