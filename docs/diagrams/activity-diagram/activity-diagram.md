# Sơ đồ Hoạt động UML (Activity Diagrams) — BlueMoon Complex (Sprint 1)

Tài liệu đặc tả các luồng nghiệp vụ trọng tâm của hệ thống **BlueMoon Complex (SE_10)** dưới dạng **UML Activity Diagram** (Mermaid `flowchart TD` với phân làn trách nhiệm / swimlanes), bám sát `docs/BRD.md`, `docs/SRS.md`, `docs/DECISIONS.md` (`D01`–`D29`) và `docs/BACKUP_RESTORE.md`.

## Danh mục sơ đồ

| Mã | Tên sơ đồ | Phân hệ / Tham chiếu | Tác nhân tham gia |
| :--- | :--- | :--- | :--- |
| **AD-01** | Xác thực, chống brute-force và đổi mật khẩu bắt buộc | `identity` (`D17`–`D19`, `D22`–`D23`) | Người dùng, Security Filter Chain, AuthService, CSDL |
| **AD-02** | Quản lý hộ khẩu, nhân khẩu và ghi nhận chuyển đi | `residency` (`D20`–`D21`, `D24`) | Tổ trưởng, ResidencyService, CSDL |
| **AD-03** | Khai báo tạm vắng, phê duyệt và xác nhận trở về | `residency` (`D24`) | Cư dân, Tổ trưởng, DeclarationService, CSDL |
| **AD-04** | Thiết lập đơn giá, lập kỳ thu và đóng góp tự nguyện | `billing` (`D25`–`D26`) | Kế toán, Tổ trưởng, BillingService, CSDL |
| **AD-05** | Thu tiền mặt tại quầy, phân bổ FIFO và hủy/đảo biên lai | `payment` (`D27`–`D28`) | Kế toán, PaymentService, CSDL |
| **AD-06** | Xuất báo cáo XLSX và chu trình phê duyệt 2 Admin (S12) | `reporting` (`D13`, `D29`) | Kế toán, Tổ trưởng, Admin 1, Admin 2, Scheduler |
| **AD-07** | Sao lưu định kỳ và diễn tập phục hồi thảm họa (S13) | `ops/backup` (`S13 Q1`–`Q5`) | Systemd Timer, Ops Script, MySQL, Cleanup Runner |

---

## AD-01: Xác thực, chống brute-force và đổi mật khẩu bắt buộc

Mô tả luồng từ lúc người dùng đăng nhập (`POST /api/v1/auth/login`), cơ chế khóa leo thang (`1/5/10/15/30/60/+60` phút, miễn trừ `ADMIN`), bắt buộc đổi mật khẩu lần đầu qua `MustChangePasswordFilter`, đến thu hồi toàn bộ `SPRING_SESSION` khi đổi mật khẩu (`D17`–`D19`, `D23`).

```mermaid
flowchart TD
    subgraph User["Người dùng (Web SPA)"]
        U1(["Bắt đầu: Mở trang đăng nhập"])
        U2["Lấy CSRF token và gửi thông tin đăng nhập"]
        U3["Xem thông báo lỗi hoặc thời gian chờ mở khóa"]
        U4["Nhập mật khẩu cũ và mật khẩu mới"]
        U5(["Truy cập dashboard theo vai trò"])
    end

    subgraph Security["Security Filter Chain & AuthService"]
        S1{"Tài khoản tồn tại và đang active?"}
        S2{"Tài khoản khác ADMIN đang bị lock tạm?"}
        S3{"Mật khẩu khớp BCrypt?"}
        S4["Tăng bộ đếm sai và tính thời gian khóa leo thang"]
        S5["Reset bộ đếm sai, xoay Session ID và gắn SecurityContext"]
        S6{"MustChangePasswordFilter: cờ mustChangePassword = true?"}
        S7{"Mật khẩu mới >= 8 ký tự và <= 72 UTF-8 bytes?"}
        S8["Cập nhật hash BCrypt mới, tắt cờ mustChangePassword"]
    end

    subgraph DB["CSDL MySQL & AuditService"]
        D1["Ghi audit_log thất bại và lưu locked_until"]
        D2["Lưu SPRING_SESSION mới và ghi audit_log đăng nhập"]
        D3["Xóa toàn bộ SPRING_SESSION của user và ghi audit_log"]
    end

    U1 --> U2 --> S1
    S1 -- "Không" --> D1 --> U3
    S1 -- "Có" --> S2
    S2 -- "Đang bị khóa" --> U3
    S2 -- "Không bị khóa" --> S3
    S3 -- "Sai mật khẩu" --> S4 --> D1
    S3 -- "Đúng mật khẩu" --> S5 --> D2 --> S6
    S6 -- "Không" --> U5
    S6 -- "Có (chặn API khác ngoài change-password)" --> U4 --> S7
    S7 -- "Không hợp lệ" --> U4
    S7 -- "Hợp lệ" --> S8 --> D3 --> U2
```

---

## AD-02: Quản lý hộ khẩu, nhân khẩu và ghi nhận chuyển đi

Mô tả quy trình tạo/cập nhật hộ khẩu, thêm nhân khẩu (đảm bảo duy nhất 1 chủ hộ `HEAD`), xử lý nhân khẩu chuyển đi (`move-out`) và tự động hủy các đơn tạm vắng đang chờ hoặc đang hiệu lực liên quan (`D20`, `D21`, `D24`).

```mermaid
flowchart TD
    subgraph Leader["Tổ trưởng / Tổ phó (LEADER)"]
        L1(["Bắt đầu thao tác nhân khẩu học"])
        L2{"Chọn nghiệp vụ"}
        L3["Nhập thông tin căn hộ, diện tích và mã hộ"]
        L4["Nhập thông tin nhân khẩu và quan hệ với chủ hộ"]
        L5["Chọn nhân khẩu và xác nhận chuyển đi hoặc sửa thông tin"]
        L6(["Nhận kết quả cập nhật"])
    end

    subgraph Service["HouseholdService & ResidentService"]
        R1{"Mã căn hộ hợp lệ và chưa trùng?"}
        R2{"Hộ đã có HEAD chưa nếu thêm HEAD?"}
        R3["Lưu hộ khẩu mới với trạng thái ACTIVE"]
        R4["Lưu nhân khẩu vào hộ khẩu"]
        R5{"Thao tác là chuyển đi (move-out)?"}
        R6["Đánh dấu nhân khẩu đã chuyển đi và cập nhật lịch sử cư trú"]
        R7["Tự động hủy đơn tạm vắng PENDING hoặc APPROVED của nhân khẩu"]
    end

    subgraph DB["CSDL MySQL & AuditService"]
        DB1["Ghi households, residency_changes và audit_log"]
        DB2["Ghi residents, residency_changes và audit_log"]
        DB3["Cập nhật residents và hủy declaration, ghi audit_log"]
    end

    L1 --> L2
    L2 -- "Tạo hộ khẩu" --> L3 --> R1
    R1 -- "Không hợp lệ" --> L3
    R1 -- "Hợp lệ" --> R3 --> DB1 --> L6
    L2 -- "Thêm nhân khẩu" --> L4 --> R2
    R2 -- "Vi phạm duy nhất HEAD" --> L4
    R2 -- "Hợp lệ" --> R4 --> DB2 --> L6
    L2 -- "Chuyển đi / Cập nhật" --> L5 --> R5
    R5 -- "Cập nhật thường" --> DB2
    R5 -- "Chuyển đi" --> R6 --> R7 --> DB3 --> L6
```

---

## AD-03: Khai báo tạm vắng, phê duyệt và xác nhận trở về

Mô tả vòng đời đơn khai báo tạm vắng của cư dân, quyền tự hủy khi đang chờ duyệt (`PENDING`), quyền duyệt/từ chối của Tổ trưởng (`LEADER`), và nghiệp vụ xác nhận nhân khẩu đã trở về (`POST /api/v1/residents/{id}/return`) (`D24`).

```mermaid
flowchart TD
    subgraph Resident["Cư dân (RESIDENT)"]
        C1(["Bắt đầu khai báo tạm vắng"])
        C2["Gửi đơn khai báo gồm thời gian và lý do"]
        C3{"Cư dân hủy đơn khi còn PENDING?"}
        C4(["Kết thúc luồng khai báo"])
    end

    subgraph Leader["Tổ trưởng / Tổ phó (LEADER)"]
        T1["Xem danh sách đơn chờ duyệt"]
        T2{"Quyết định duyệt đơn?"}
        T3["Nhập lý do từ chối"]
        T4["Xác nhận nhân khẩu đã trở về địa phương"]
    end

    subgraph System["DeclarationService & MySQL + AuditService"]
        SY1{"Nhân khẩu thuộc hộ của cư dân và đang cư trú?"}
        SY2["Tạo đơn trạng thái PENDING và ghi audit_log"]
        SY3["Chuyển đơn sang CANCELLED và ghi audit_log"]
        SY4["Chuyển đơn sang APPROVED và cập nhật trạng thái tạm vắng"]
        SY5["Chuyển đơn sang REJECTED kèm lý do và ghi audit_log"]
        SY6["Kết thúc đợt tạm vắng, khôi phục trạng thái cư trú và ghi audit_log"]
    end

    C1 --> C2 --> SY1
    SY1 -- "Không hợp lệ" --> C2
    SY1 -- "Hợp lệ" --> SY2 --> C3
    C3 -- "Có hủy" --> SY3 --> C4
    C3 -- "Không hủy" --> T1 --> T2
    T2 -- "Từ chối" --> T3 --> SY5 --> C4
    T2 -- "Phê duyệt" --> SY4 --> T4 --> SY6 --> C4
```

---

## AD-04: Thiết lập đơn giá, lập kỳ thu và đóng góp tự nguyện

Mô tả luồng tạo/cập nhật đơn giá (`AREA` hoặc `PER_HOUSEHOLD`, làm tròn `HALF_UP`, chỉ chỉnh tương lai), quy trình `DRAFT -> SUBMITTED -> APPROVED/RETURNED -> ISSUED` và ghi nhận chiến dịch đóng góp tự nguyện (`D25`, `D26`).

```mermaid
flowchart TD
    subgraph Accountant["Kế toán (ACCOUNTANT)"]
        K1(["Bắt đầu nghiệp vụ thu phí"])
        K2["Thiết lập loại phí và đơn giá áp dụng từ kỳ tương lai"]
        K3["Tạo kỳ thu DRAFT và chạy xem trước preview công thức"]
        K4["Gửi trình duyệt kỳ thu (SUBMITTED)"]
        K5["Điều chỉnh lại kỳ thu hoặc rút lại đơn (withdraw)"]
        K6["Phát hành chính thức hóa đơn (ISSUED)"]
        K7["Mở chiến dịch tự nguyện và ghi nhận phiếu đóng góp"]
    end

    subgraph Leader["Tổ trưởng (LEADER)"]
        LD1["Kiểm tra danh sách hộ và tổng số tiền dự kiến"]
        LD2{"Phê duyệt kỳ thu?"}
        LD3["Trả lại bản nháp kèm lý do (return)"]
    end

    subgraph System["FeeCalculation & BillingPeriodService"]
        B1{"Đơn giá thay đổi không ảnh hưởng kỳ đã chốt?"}
        B2["Tính tiền theo AREA * rate hoặc PER_HOUSEHOLD, làm tròn HALF_UP"]
        B3["Khóa snapshot danh sách hộ vào billing_submission_items"]
        B4["Sinh bản ghi invoices và invoice_lines trạng thái UNPAID"]
        B5{"Hủy kỳ thu đã phát hành khi chưa có biên lai hợp lệ?"}
        B6(["Hoàn tất chu trình kỳ thu"])
    end

    K1 --> K2 --> B1
    B1 -- "Hợp lệ" --> K3 --> B2 --> K4 --> B3 --> LD1 --> LD2
    LD2 -- "Trả lại" --> LD3 --> K5 --> K4
    LD2 -- "Chấp thuận" --> K6 --> B4 --> B5
    B5 -- "Không hủy" --> B6
    B5 -- "Có yêu cầu hủy (0 biên lai)" --> B6
    K1 --> K7 --> B6
```

---

## AD-05: Thu tiền mặt tại quầy, phân bổ FIFO và hủy/đảo biên lai

Mô tả giao dịch `READ_COMMITTED` có kiểm soát trùng lặp bằng `Idempotency-Key` (`actor`, `key`, `payload`), khóa hàng hộ khẩu `lockHousehold`, tự động phân bổ tiền thu theo FIFO vào hóa đơn cũ nhất trước, đẩy phần dư thành số dư có hộ (`credit balance`), hoặc hủy biên lai / đảo phân bổ (`D27`, `D28`).

```mermaid
flowchart TD
    subgraph Accountant["Kế toán (ACCOUNTANT)"]
        P1(["Bắt đầu thu tiền hoặc xử lý công nợ"])
        P2["Chọn hộ khẩu, nhập số tiền và gửi kèm Idempotency-Key"]
        P3["Chọn lệnh phân bổ dư có (allocate) hoặc hủy biên lai (void) hoặc đảo (reverse)"]
        P4(["Nhận số biên lai và số dư công nợ mới"])
    end

    subgraph Payment["PaymentService (Isolation.READ_COMMITTED)"]
        S1{"Idempotency-Key đã tồn tại?"}
        S2{"Payload trùng khớp yêu cầu trước?"}
        S3["Trả về kết quả giao dịch đã lưu (idempotent replay)"]
        S4["Khóa bản ghi hộ trong payment_household_locks"]
        S5["Lấy danh sách hóa đơn còn nợ theo thứ tự FIFO"]
        S6["Gạch nợ lần lượt hóa đơn từ cũ đến mới"]
        S7{"Còn tiền dư sau khi trả hết nợ?"}
        S8["Cộng phần dư vào credit balance của hộ khẩu"]
        S9["Kiểm tra điều kiện hủy biên lai hoặc đảo phân bổ và hoàn lại công nợ"]
    end

    subgraph DB["CSDL MySQL & AuditService"]
        DB1["Lưu payment_commands, payment_receipts, cập nhật invoices và audit_log"]
        DB2["Cập nhật payment_credit_allocations, BillingReceiptGuard và audit_log"]
    end

    P1 --> P2 --> S1
    S1 -- "Có và trùng payload" --> S2
    S2 -- "Trùng" --> S3 --> P4
    S2 -- "Xung đột payload (409)" --> P2
    S1 -- "Chưa có" --> S4 --> S5 --> S6 --> S7
    S7 -- "Có" --> S8 --> DB1 --> P4
    S7 -- "Không" --> DB1 --> P4
    P1 --> P3 --> S4 --> S9 --> DB2 --> P4
```

---

## AD-06: Xuất báo cáo XLSX và chu trình phê duyệt 2 Admin (S12)

Mô tả 2 luồng xuất dữ liệu: (a) 3 báo cáo XLSX cho Kế toán/Tổ trưởng với giới hạn `50.000` dòng (`SXSSFWorkbook`), và (b) chu trình S12 tạo gói dữ liệu cơ quan yêu cầu 2 Admin độc lập phê duyệt, kiểm soát tải tối đa 7 ngày và bộ dọn dẹp 60 giây xóa nội dung sau 30 ngày nhưng giữ metadata/audit 5 năm (`D13`, `D29`).

```mermaid
flowchart TD
    subgraph Requestor["Kế toán / Tổ trưởng"]
        Q1(["Yêu cầu báo cáo hoặc gói dữ liệu cơ quan"])
        Q2["Tải 1 trong 3 báo cáo XLSX (thu phí, biên lai, cư trú)"]
        Q3["Tạo yêu cầu Agency Data Package (loại gói, lý do)"]
    end

    subgraph Admins["Admin 1 & Admin 2 (ADMIN)"]
        A1["Admin 1 xem xét và phê duyệt lần 1"]
        A2{"Admin 2 khác Admin 1 phê duyệt lần 2?"}
        A3["Từ chối yêu cầu gói dữ liệu"]
        A4["Tải tệp ZIP kèm kiểm tra checksum SHA-256"]
    end

    subgraph System["Reporting, Writer & AgencyPackageRetentionScheduler"]
        R1{"Số dòng truy vấn <= 50.000?"}
        R2["Stream SXSSFWorkbook(100) trả về HTTP 200 (.xlsx)"]
        R3["Đóng gói dữ liệu hộ khẩu ZIP, tính SHA-256, lưu DB"]
        R4{"Trong cửa sổ tải 7 ngày?"}
        R5["Cơ chế định kỳ mỗi 60 giây kiểm tra hạn lưu trữ 30 ngày"]
        R6["Xóa nội dung nhị phân, giữ lại metadata và audit_log 5 năm"]
    end

    Q1 --> Q2 --> R1
    R1 -- "Vượt 50.000 dòng" --> Q2
    R1 -- "Đạt ngưỡng" --> R2
    Q1 --> Q3 --> A1 --> A2
    A2 -- "Trùng người duyệt (bị chặn)" --> A2
    A2 -- "Từ chối" --> A3
    A2 -- "Chấp thuận (Admin 2)" --> R3 --> R4
    R4 -- "Còn hạn" --> A4
    R4 -- "Hết 7 ngày tải" --> R5
    R3 --> R5 --> R6
```

---

## AD-07: Sao lưu định kỳ và diễn tập phục hồi thảm họa (S13)

Mô tả quy trình sao lưu lúc 02:00 `Asia/Ho_Chi_Minh` (`backup-dispatch.sh` + `backup.sh`, loại trừ `SPRING_SESSION*`, giữ 7 bản, tạo SHA-256 + `COMPLETED`) và quy trình diễn tập phục hồi sang CSDL tách biệt `bluemoon_restore_s13_*` kèm dọn dẹp gói quá hạn bằng profile `restore-cleanup` (`docs/BACKUP_RESTORE.md`).

```mermaid
flowchart TD
    subgraph Scheduler["Systemd Timer & Dispatcher"]
        T1(["Kích hoạt 02:00 Asia/Ho_Chi_Minh hoặc catch-up >24h"])
        T2["Gọi backup.sh đọc cấu hình backup.conf"]
    end

    subgraph Backup["Backup Script & Storage"]
        B1["Dump MySQL ngoại trừ SPRING_SESSION và SPRING_SESSION_ATTRIBUTES"]
        B2["Tạo tệp .sql.gz, checksum SHA-256 và marker COMPLETED"]
        B3["Xoay vòng giữ lại tối đa 7 bản sao lưu gần nhất"]
    end

    subgraph Rehearsal["Restore Rehearsal & RestoreRetentionCleanupRunner"]
        H1(["Chạy restore-rehearsal.sh kiểm chứng bản sao lưu"])
        H2{"Xác nhận SHA-256, marker và DB đích khác DB nguồn?"}
        H3["Tạo DB tạm bluemoon_restore_s13_* và nạp dữ liệu dump"]
        H4["Chạy Flyway V1 tạo lại bảng SPRING_SESSION rỗng"]
        H5["Kích hoạt Spring profile restore-cleanup dọn gói S12 quá hạn"]
        H6(["Xuất JSON bằng chứng Q5 và giữ hoặc xóa DB diễn tập"])
    end

    T1 --> T2 --> B1 --> B2 --> B3
    B3 --> H1 --> H2
    H2 -- "Sai lệch hoặc trùng DB nguồn (Fail)" --> H1
    H2 -- "An toàn" --> H3 --> H4 --> H5 --> H6
```
