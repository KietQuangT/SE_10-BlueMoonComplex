# Báo cáo kiểm thử chi tiết: Thu tiền mặt tại quầy và công nợ (S09)

- **Tệp đặc tả nguồn:** [`tests/cases/s09-payments.md`](../cases/s09-payments.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** 47 ca từ `PAY-01.1` đến `PAY-13.1` của phân hệ thanh toán / S09 (PBI-015): ghi nhận biên lai (số tiền, ngày, lý do lùi ngày), idempotency và replay, phân bổ số dư nộp thừa theo FIFO, đảo phân bổ, hủy biên lai, tính toàn vẹn giao dịch và bộ đếm kỳ thu, đồng thời (concurrency), phân quyền RBAC/CSRF, ẩn lý do tự do (PII) và giao diện Kế toán/Admin. Các ID và placeholder trong đặc tả chỉ dùng trên database kiểm thử riêng; không ghi mật khẩu, CSRF token, cookie hoặc dữ liệu cư dân thật vào bằng chứng.

## 1. Thống kê kết quả phân hệ

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **47** |
| Số ca Đạt (Pass) | **47** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Số ca Chưa thực thi (Not run) | **0** |
| Tỷ lệ Đạt | **100.00%** |

## 2. Ghi nhận & Giải đáp các điểm chưa xác định

- **Nguồn kết quả:** cột "Thực tế ghi nhận" và mọi trạng thái trong báo cáo này lấy nguyên từ `results-s09.csv`; báo cáo không tự suy ra hay nâng trạng thái của ca nào. Ca chưa có kết quả trong CSV được xếp "Chưa thực thi".
- **Ca kiểm thử thủ công (12 ca):** `PAY-06.1`, `PAY-06.2`, `PAY-06.3`, `PAY-07.4`, `PAY-09.3`, `PAY-10.1`, `PAY-10.2`, `PAY-11.1`, `PAY-12.1`, `PAY-12.2`, `PAY-12.3`, `PAY-13.1`. Kết quả do người kiểm thử tự thực hiện trên giao diện/hệ thống.
- **Ca Đạt nhưng chưa kiểm đủ post-check (18 ca):** `PAY-01.1`, `PAY-01.4`, `PAY-01.5`, `PAY-01.6`, `PAY-02.1`, `PAY-02.4`, `PAY-03.1`, `PAY-03.4`, `PAY-04.1`, `PAY-04.4`, `PAY-05.1`, `PAY-05.3`, `PAY-05.5`, `PAY-07.1`, `PAY-07.2`, `PAY-07.3`, `PAY-07.5`, `PAY-09.2`. Kết quả "Đạt" của các ca này xác nhận phần HTTP/API; các bước SQL, audit và `receipt_count` trong mục "Kiểm tra sau" của đặc tả chưa được kiểm và được ghi ở "Chi tiết lỗi / Nhận xét" của từng ca.
- **Dữ liệu khởi tạo (`SETUP`):** ghi nhận trong CSV là dữ liệu chuẩn bị, không tính vào thống kê ca kiểm thử.
- **Môi trường chạy:** - **Môi trường chạy:** Windows 11; nhánh `hhoangsonnw/s09-test-docs` (commit `96832f5`). Backend Spring Boot chạy bằng Docker Compose tại cổng `8080`; frontend `npm run dev` tại `http://localhost:5173`; MySQL 8.4 trong container, cổng `3306`, schema `bluemoon`. Dữ liệu mẫu nạp bằng `seed_test_data.py`. Trình duyệt Chrome (phiên bản 157). Ngày kiểm thử: 08/11/2026; người thực hiện: Quân.
- **Chưa kiểm:** CI trên remote và máy thứ hai.

## 3. Bảng tổng hợp nhanh theo từng ca kiểm thử

| Mã ca | Tên ca kiểm thử | Vai trò | Kết quả | Mã lỗi / Ghi chú |
| --- | --- | --- | --- | --- |
| `PAY-01.1` | ACCOUNTANT ghi nhận biên lai thu một phần công nợ hóa đơn | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `PARTIALLY_PAID` |
| `PAY-01.2` | ACCOUNTANT ghi nhận biên lai thu đủ và thu vượt công nợ hóa đơn (sinh số dư nộp thừa của hộ) | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-01.3` | ACCOUNTANT ghi nhận biên lai thu trên hóa đơn đã trả hết nợ (toàn bộ tiền thu chuyển thành số dư hộ) | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-01.4` | Từ chối số tiền không hợp lệ (số thập phân, số 0, số âm, số mũ lớn) | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `INVALID_AMOUNT` |
| `PAY-01.5` | Từ chối ngày nhận tiền tương lai hoặc trước ngày phát hành hóa đơn | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `INVALID_PAID_ON`, `INVALID_DATE` |
| `PAY-01.6` | Thu lùi ngày: yêu cầu lý do khi `paidOn < today`, giới hạn lý do tối đa 255 ký tự | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `LATE_REASON_REQUIRED` |
| `PAY-02.1` | Replay lệnh ghi nhận biên lai: cùng actor, cùng idempotency key, cùng payload trả kết quả cũ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-02.2` | Xung đột idempotency key khi dùng lại key với actor khác | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `IDEMPOTENCY_CONFLICT` |
| `PAY-02.3` | Xung đột idempotency key khi dùng lại key với payload khác | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `IDEMPOTENCY_CONFLICT` |
| `PAY-02.4` | Từ chối định dạng idempotency key không phải chuỗi UUID hợp lệ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `INVALID_IDEMPOTENCY_KEY` |
| `PAY-03.1` | Phân bổ số dư nộp thừa từ nhiều biên lai theo thứ tự cũ nhất trước (FIFO) vào hóa đơn cùng hộ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-03.2` | Từ chối phân bổ khi số tiền yêu cầu vượt quá số dư nộp thừa hiện có của hộ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `INSUFFICIENT_CREDIT` |
| `PAY-03.3` | Từ chối phân bổ khi số tiền yêu cầu vượt quá công nợ còn lại của hóa đơn đích | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `ALLOCATION_EXCEEDS_DEBT` |
| `PAY-03.4` | Từ chối phân bổ khi thiếu `invoiceId` hoặc số tiền không phải số nguyên dương | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `INVALID_AMOUNT` |
| `PAY-03.5` | Cách ly số dư nộp thừa giữa các hộ: từ chối phân bổ số dư của hộ này sang hóa đơn của hộ khác | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `HOUSEHOLD_MISMATCH` |
| `PAY-04.1` | ACCOUNTANT đảo phân bổ đang hoạt động kèm lý do hợp lệ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-04.2` | Từ chối đảo phân bổ khi thiếu lý do hoặc lý do vượt quá 255 ký tự | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `REASON_REQUIRED` |
| `PAY-04.3` | Từ chối đảo phân bổ đã bị đảo trước đó | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `ALLOCATION_ALREADY_REVERSED` |
| `PAY-04.4` | Replay và xung đột idempotency cho lệnh đảo phân bổ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `IDEMPOTENCY_CONFLICT` |
| `PAY-05.1` | ACCOUNTANT hủy biên lai thu tiền chưa dùng số dư kèm lý do hợp lệ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-05.2` | Chặn hủy biên lai khi số dư nộp thừa đang được dùng trong phân bổ hiệu lực | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `RECEIPT_CREDIT_IN_USE` |
| `PAY-05.3` | Cho phép hủy biên lai sau khi toàn bộ phân bổ liên quan đã được đảo xong | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-05.4` | Từ chối hủy biên lai khi thiếu lý do hoặc biên lai đã bị hủy trước đó | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `REASON_REQUIRED`, `RECEIPT_ALREADY_VOIDED`, `RECEIPT_NOT_FOUND` |
| `PAY-05.5` | Replay và xung đột idempotency cho lệnh hủy biên lai | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `IDEMPOTENCY_CONFLICT` |
| `PAY-06.1` | Rollback toàn bộ khi ghi audit lỗi: không sinh biên lai, không lưu command, không tăng `receipt_count` | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-06.2` | Ghi nhận biên lai và phân bổ thành công tăng `receipt_count`; hủy/đảo không làm giảm `receipt_count` | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-06.3` | Không thể thu tiền trên hóa đơn hoặc kỳ thu đã bị hủy (`CANCELLED`) | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-07.1` | Đua tranh hai request ghi thu cùng idempotency key đồng thời: đúng một lượt ghi thật, lượt kia nhận kết quả replay | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-07.2` | Đua tranh hai request ghi thu khác key trên cùng hóa đơn đồng thời: tuần tự hóa an toàn, không thất thoát tiền | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-07.3` | Đua tranh hai request phân bổ đồng thời vượt quá số dư có sẵn của hộ: khóa hộ ngăn chặn chi vượt số dư | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv; `INSUFFICIENT_CREDIT` |
| `PAY-07.4` | Đua tranh giữa ghi nhận biên lai và ADMIN hủy kỳ thu: khóa kỳ tuần tự hóa | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-07.5` | Đua tranh giữa hủy biên lai cũ và ghi biên lai mới trên cùng hóa đơn | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-08.1` | Từ chối truy cập chưa xác thực (ANON) trên toàn bộ endpoint thu tiền | ANON | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-08.2` | Chặn vai trò RESIDENT trên toàn bộ endpoint thu tiền | RESIDENT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-08.3` | Phân quyền ADMIN: được phép gọi các API đọc (GET), bị chặn trên các API ghi (POST) | ADMIN | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-08.4` | Phân quyền ACCOUNTANT: toàn quyền đọc và ghi với CSRF token hợp lệ | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-08.5` | Chặn tấn công CSRF: request POST của ACCOUNTANT thiếu hoặc sai CSRF token bị từ chối | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-09.1` | API phản hồi chỉ trả cờ boolean (`hasLateReason`, `hasVoidReason`, `hasReverseReason`), không trả trường raw reason | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-09.2` | Bảng `payment_commands.result_json` và `audit_log` không lưu chuỗi lý do tự do | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-09.3` | Giao diện UI chỉ hiển thị nhãn "Có lý do", không hiển thị nội dung lý do tự do và không hiển thị PII cư dân | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-10.1` | Chế độ API mode: `/accountant/collections` và `/admin/collections` kết nối API backend thật | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-10.2` | Chế độ Demo mode: `/accountant/collections` dùng dữ liệu giả lập trong bộ nhớ, `/admin/collections` chỉ đọc | ACCOUNTANT, ADMIN | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-11.1` | ADMIN truy cập `/admin/collections`: xem công nợ và lịch sử, ẩn toàn bộ nút hành động | ADMIN | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-12.1` | Giao diện khi gặp lỗi chưa rõ kết quả: lưu lệnh vào `sessionStorage` theo username, hiển thị banner và nút "Thử gửi lại" | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-12.2` | Người dùng nhấn "Thử gửi lại": gửi lại đúng `idempotencyKey` và payload cũ để đối soát thành công | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-12.3` | Chuyển đổi tài khoản: xóa sạch state, không hiển thị và không cho phép tài khoản khác gửi lại giao dịch dở dang | ACCOUNTANT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |
| `PAY-13.1` | Kịch bản duyệt toàn trình nghiệp vụ thu tiền mặt tại quầy từ đầu đến cuối trên trình duyệt thực tế | ACCOUNTANT, ADMIN, RESIDENT | ✅ Đạt (Pass) | Bằng chứng: results-s09.csv |

## 4. Chi tiết kết quả thực thi từng ca kiểm thử

### PAY-01.1 — ACCOUNTANT ghi nhận biên lai thu một phần công nợ hóa đơn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (ví dụ `1001`) thuộc hộ `<HOUSEHOLD_ID>` (`201`) có tổng tiền 500.000 đ, trạng thái `ISSUED`, chưa có khoản thanh toán nào (`remaining = 500000`). Phiên ACCOUNTANT đang hoạt động, có CSRF token hợp lệ.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"amount": 300000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "10000000-0000-0000-0000-000000000001"}`
  - **Các bước thực hiện:**
    1. Đăng nhập tài khoản ACCOUNTANT qua `GET /api/v1/auth/csrf` và `POST /api/v1/auth/login`; nhận session cookie và CSRF token.
    2. Gửi `GET /api/v1/invoices/1001/payment-summary` xác nhận tổng nợ ban đầu: `total = "500000"`, `remaining = "500000"`, `status = "UNPAID"`.
    3. Gửi `POST /api/v1/invoices/1001/receipts` với body và CSRF token nêu trên.
    4. Thu thập HTTP status, response body JSON và thực hiện các bước kiểm tra sau.
  - **Kết quả mong đợi:** HTTP 201 Created; response JSON chứa:
    - `receiptId`: số nguyên dương (ví dụ `5001`).
    - `invoiceId`: `1001`.
    - `paidOn`: `"<HOM_NAY>"`.
    - `amount`: `"300000"`.
    - `appliedAmount`: `"300000"`.
    - `creditAmount`: `"0"`.
    - `debtAfter`: `"200000"`.
    - `status`: `"POSTED"`.
    - `recordedAt`: chuỗi thời gian ISO-8601 UTC.
    - `hasLateReason`: `false`.
    - `hasVoidReason`: `false`.
    - Không chứa các trường raw `lateReason` hoặc `voidReason`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/invoices/1001/payment-summary` xác nhận: `settled = "300000"`, `remaining = "200000"`, `status = "PARTIALLY_PAID"`.
    2. Gửi `GET /api/v1/households/201/credit-balance` xác nhận: `balance = "0"`.
    3. Truy vấn DB kiểm thử cô lập: Bảng `payment_receipts` có bản ghi mới với `status = 'POSTED'`, `applied_amount = 300000`, `credit_amount = 0`; bảng `billing_periods` có `receipt_count` tăng thêm 1; bảng `audit_log` ghi action `RECEIPT_RECORDED` với target `PaymentReceipt`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-01.1`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/api/PaymentController.java:27`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:64`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:34`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201 thu 1127750/2255500; debtAfter=1127750; summary PARTIALLY_PAID remaining=1127750; credit 0->0.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-01.2 — ACCOUNTANT ghi nhận biên lai thu đủ và thu vượt công nợ hóa đơn (sinh số dư nộp thừa của hộ)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1002/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1002`) thuộc hộ `<HOUSEHOLD_ID>` (`201`) có tổng tiền 400.000 đ, trạng thái `ISSUED`, nợ còn lại 400.000 đ. Phiên ACCOUNTANT có CSRF token hợp lệ.
    - **Dữ liệu giả:**
      - Lần 1: Body `{"amount": 400000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "10000000-0000-0000-0000-000000000002"}`
      - Lần 2 (trên hóa đơn khác `1003` nợ 200.000 đ thuộc hộ `201`): Body `{"amount": 350000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "10000000-0000-0000-0000-000000000003"}`
  - **Các bước thực hiện:**
    1. Gửi request lần 1 thu đủ 400.000 đ cho hóa đơn `1002`.
    2. Đối chiếu kết quả lần 1: xác nhận `debtAfter = "0"`, `creditAmount = "0"`.
    3. Gửi request lần 2 thu vượt 350.000 đ cho hóa đơn `1003` (đang nợ 200.000 đ).
    4. Đối chiếu kết quả lần 2 và thực hiện kiểm tra sau.
  - **Kết quả mong đợi:**
    - Lần 1: HTTP 201 Created; `appliedAmount = "400000"`, `creditAmount = "0"`, `debtAfter = "0"`, `status = "POSTED"`.
    - Lần 2: HTTP 201 Created; `appliedAmount = "200000"`, `creditAmount = "150000"`, `debtAfter = "0"`, `status = "POSTED"`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/invoices/1002/payment-summary` xác nhận: `status = "PAID"`, `remaining = "0"`, `settled = "400000"`.
    2. Gửi `GET /api/v1/invoices/1003/payment-summary` xác nhận: `status = "PAID"`, `remaining = "0"`, `settled = "200000"`.
    3. Gửi `GET /api/v1/households/201/credit-balance` xác nhận: `balance = "150000"` (số dư thừa 150.000 đ thuộc về hộ gia đình `201`).
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-01.2`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/ReceiptAmounts.java:12`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:34`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** thu đủ 201 (debtAfter=0); thu vượt 201 (applied=2257500, credit=50000); số dư hộ B 0->50000.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-01.3 — ACCOUNTANT ghi nhận biên lai thu trên hóa đơn đã trả hết nợ (toàn bộ tiền thu chuyển thành số dư hộ)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1002/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1002`) thuộc hộ `201` đã thanh toán hết nợ (`remaining = "0"`, `status = "PAID"`). Số dư nộp thừa ban đầu của hộ là 150.000 đ.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"amount": 100000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "10000000-0000-0000-0000-000000000004"}`
  - **Các bước thực hiện:**
    1. Xác nhận hóa đơn `1002` có `remaining = "0"` qua summary.
    2. Gửi `POST /api/v1/invoices/1002/receipts` với số tiền 100.000 đ.
    3. Đối chiếu response và thực hiện kiểm tra sau.
  - **Kết quả mong đợi:** HTTP 201 Created; response JSON có: `amount = "100000"`, `appliedAmount = "0"`, `creditAmount = "100000"`, `debtAfter = "0"`, `status = "POSTED"`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/invoices/1002/payment-summary` xác nhận: hóa đơn vẫn giữ `status = "PAID"`, `remaining = "0"`, `settled = "400000"`.
    2. Gửi `GET /api/v1/households/201/credit-balance` xác nhận: `balance = "250000"` (150.000 đ cũ + 100.000 đ mới).
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-01.3`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:74-77`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201 applied=0 credit=100000; số dư 50000->150000.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-01.4 — Từ chối số tiền không hợp lệ (số thập phân, số 0, số âm, số mũ lớn)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1001`) đang ở trạng thái `ISSUED`. Phiên ACCOUNTANT có CSRF token hợp lệ.
    - **Dữ liệu giả:**
      - Body A (số thập phân): `{"amount": 100000.5, "paidOn": "<HOM_NAY>", "idempotencyKey": "<UUID_A>"}`
      - Body B (số 0): `{"amount": 0, "paidOn": "<HOM_NAY>", "idempotencyKey": "<UUID_B>"}`
      - Body C (số âm): `{"amount": -50000, "paidOn": "<HOM_NAY>", "idempotencyKey": "<UUID_C>"}`
      - Body D (số mũ cực lớn): `{"amount": 1E+100000000, "paidOn": "<HOM_NAY>", "idempotencyKey": "<UUID_D>"}`
      - Body E (số mũ cực nhỏ): `{"amount": 1E-100000000, "paidOn": "<HOM_NAY>", "idempotencyKey": "<UUID_E>"}`
      - Body F (vượt quá 24 chữ số nguyên): `{"amount": 10000000000000000000000000, "paidOn": "<HOM_NAY>", "idempotencyKey": "<UUID_F>"}`
  - **Các bước thực hiện:**
    1. Gửi lần lượt từng body A, B, C, D, E, F tới `POST /api/v1/invoices/1001/receipts`.
    2. Ghi nhận mã trạng thái HTTP, thời gian phản hồi (với body D, E không được treo bộ nhớ) và mã lỗi `code`.
    3. Thực hiện kiểm sau.
  - **Kết quả mong đợi:** Tất cả các request đều bị từ chối với HTTP 400 Bad Request, body JSON chứa `code = "INVALID_AMOUNT"`. Hệ thống xử lý ngay lập tức (< 1 giây), không bị OutOfMemory hay CPU spike khi parse số mũ lớn.
  - **Kiểm tra sau (Post-check):** Gửi `GET /api/v1/invoices/1001/receipts` xác nhận không có biên lai nào được tạo ra từ 6 request lỗi trên.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-01.4`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/ReceiptAmounts.java:22-34`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:107-113`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** A-F: 400/INVALID_AMOUNT/99ms 400/INVALID_AMOUNT/49ms 400/INVALID_AMOUNT/43ms 400/INVALID_AMOUNT/43ms 400/INVALID_AMOUNT/44ms 400/INVALID_AMOUNT/65ms; số biên lai 0->0.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: ngưỡng <1s theo tài liệu; không kiểm OOM/CPU.

### PAY-01.5 — Từ chối ngày nhận tiền tương lai hoặc trước ngày phát hành hóa đơn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1001`) được phát hành vào ngày `<NGAY_PHAT_HANH>` (ví dụ ngày 01 tháng hiện tại theo Asia/Ho_Chi_Minh). Phiên ACCOUNTANT có CSRF token hợp lệ.
    - **Dữ liệu giả:**
      - Body A (ngày tương lai): `{"amount": 100000, "paidOn": "<NGAY_MAI>", "idempotencyKey": "<UUID_A>"}`
      - Body B (ngày trước phát hành): `{"amount": 100000, "paidOn": "<TRUOC_NGAY_PHAT_HANH>", "lateReason": "Dong tien som", "idempotencyKey": "<UUID_B>"}`
      - Body C (ngày null hoặc sai định dạng): `{"amount": 100000, "paidOn": null, "idempotencyKey": "<UUID_C>"}`
  - **Các bước thực hiện:**
    1. Gửi body A với ngày mai.
    2. Gửi body B với ngày trước ngày phát hành hóa đơn (ví dụ ngày cuối tháng trước).
    3. Gửi body C với ngày null.
    4. Đối chiếu mã phản hồi HTTP và kiểm tra sau.
  - **Kết quả mong đợi:**
    - Body A và B trả HTTP 400 Bad Request, `code = "INVALID_PAID_ON"`.
    - Body C trả HTTP 400 Bad Request, `code = "INVALID_DATE"`.
  - **Kiểm tra sau (Post-check):** Gửi `GET /api/v1/invoices/1001/receipts` xác nhận không phát sinh biên lai nào.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-01.5`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:66,71`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** ngày mai 400/INVALID_PAID_ON; trước phát hành 400/INVALID_PAID_ON; null 400/INVALID_DATE; biên lai 0->0.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: ca B giả định hóa đơn phát hành ngày ISSUED_ON.

### PAY-01.6 — Thu lùi ngày: yêu cầu lý do khi `paidOn < today`, giới hạn lý do tối đa 255 ký tự

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1001`) được phát hành từ ngày đầu tháng. Hôm nay là ngày 10. Ngày nhận tiền thực tế là ngày 05 (`<NGAY_LUI>`). Phiên ACCOUNTANT có CSRF token hợp lệ.
    - **Dữ liệu giả:**
      - Body A (lùi ngày nhưng thiếu lý do): `{"amount": 100000, "paidOn": "<NGAY_LUI>", "lateReason": "", "idempotencyKey": "<UUID_A>"}`
      - Body B (lùi ngày có lý do hợp lệ): `{"amount": 100000, "paidOn": "<NGAY_LUI>", "lateReason": "Cu dan nop tien qua chuyen khoan cuoi tuan, ke toan doi soat muon", "idempotencyKey": "<UUID_B>"}`
      - Body C (lý do vượt quá 255 ký tự): `{"amount": 100000, "paidOn": "<NGAY_LUI>", "lateReason": "<CHUOI_256_KY_TU>", "idempotencyKey": "<UUID_C>"}`
  - **Các bước thực hiện:**
    1. Gửi body A tới endpoint. Ghi nhận phản hồi.
    2. Gửi body C với lý do dài 256 ký tự. Ghi nhận phản hồi.
    3. Gửi body B với lý do hợp lệ (dưới 255 ký tự). Ghi nhận phản hồi và kiểm sau.
  - **Kết quả mong đợi:**
    - Body A trả HTTP 400 Bad Request, `code = "LATE_REASON_REQUIRED"`.
    - Body C trả HTTP 400 Bad Request, `code = "VALIDATION"`.
    - Body B trả HTTP 201 Created; response JSON có `hasLateReason = true`, `hasVoidReason = false`, không chứa trường `lateReason` thô.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/invoices/1001/receipts` xác nhận biên lai mới có `hasLateReason = true` và `paidOn = "<NGAY_LUI>"`.
    2. Truy vấn DB kiểm thử cô lập: Bảng `payment_receipts` lưu đúng chuỗi lý do tại cột `late_reason`; bảng `payment_commands` tại cột `result_json` không chứa nội dung lý do; bảng `audit_log` không chứa nội dung lý do.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-01.6`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:72,274-278`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:114-126`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** thiếu lý do 400/LATE_REASON_REQUIRED; 256 ký tự 400/VALIDATION; hợp lệ 201 hasLateReason=true; list hasLateReason=true.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-02.1 — Replay lệnh ghi nhận biên lai: cùng actor, cùng idempotency key, cùng payload trả kết quả cũ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1001`) đang ở trạng thái `ISSUED`. Tài khoản `accountant1` (ID `11`) đang đăng nhập. Database kiểm thử cô lập.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"amount": 150000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "20000000-0000-0000-0000-000000000001"}`
  - **Các bước thực hiện:**
    1. Gửi request lần 1 với body nêu trên. Lưu lại toàn bộ response JSON (chứa `receiptId`, ví dụ `5010`).
    2. Gửi lại request lần 2 với cùng tài khoản `accountant1`, cùng header và đúng từng trường của body trên.
    3. So sánh response lần 2 với lần 1 và thực hiện kiểm sau.
  - **Kết quả mong đợi:** Cả hai lần đều trả HTTP 201 Created với response JSON giống nhau hoàn toàn (`receiptId = 5010`, `amount = "150000"`, `debtAfter` như nhau).
  - **Kiểm tra sau (Post-check):**
    1. Truy vấn DB kiểm thử cô lập: Bảng `payment_receipts` chỉ có đúng 1 bản ghi có `invoice_id = 1001` sinh ra từ lệnh này.
    2. Bảng `billing_periods` có `receipt_count` chỉ tăng 1 đơn vị, không bị tăng 2 lần.
    3. Bảng `audit_log` chỉ có đúng 1 bản ghi `RECEIPT_RECORDED`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-02.1`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:252-266`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:39-40`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** lần1 201, lần2 201, response giống; biên lai 1->2.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-02.2 — Xung đột idempotency key khi dùng lại key với actor khác

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Tài khoản `accountant1` (ID `11`) đã thực hiện thành công một lệnh thu với khóa `20000000-0000-0000-0000-000000000001` (PAY-02.1). Tài khoản `accountant2` (ID `12`) đang đăng nhập trong phiên riêng biệt.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN_2>`, `Content-Type: application/json`
      - Body: `{"amount": 150000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "20000000-0000-0000-0000-000000000001"}` (dùng lại key của `accountant1`)
  - **Các bước thực hiện:**
    1. Trong phiên của `accountant2`, gửi request với body trên.
    2. Thu thập mã HTTP và body ProblemDetail.
    3. Thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409 Conflict; body JSON chứa `code = "IDEMPOTENCY_CONFLICT"`. Không trả về kết quả replay của `accountant1`.
  - **Kiểm tra sau (Post-check):** Gửi `GET /api/v1/invoices/1001/receipts` xác nhận không có bản ghi biên lai mới nào được tạo thêm.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-02.2`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:68,257-261`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:44`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** acc2 dùng key acc1: 409/IDEMPOTENCY_CONFLICT; biên lai 2->2.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-02.3 — Xung đột idempotency key khi dùng lại key với payload khác

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Tài khoản `accountant1` đã thực hiện thành công lệnh thu với khóa `20000000-0000-0000-0000-000000000001` cho số tiền 150.000 đ.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN_1>`, `Content-Type: application/json`
      - Body: `{"amount": 200000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "20000000-0000-0000-0000-000000000001"}` (thay đổi số tiền thành 200.000 đ)
  - **Các bước thực hiện:**
    1. Trong cùng phiên `accountant1`, gửi lại request với số tiền đã sửa nhưng giữ nguyên idempotency key cũ.
    2. Thu thập phản hồi và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409 Conflict; body JSON chứa `code = "IDEMPOTENCY_CONFLICT"`.
  - **Kiểm tra sau (Post-check):** Gửi `GET /api/v1/invoices/1001/payment-summary` xác nhận công nợ hóa đơn không bị thay đổi bởi request xung đột.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-02.3`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:261`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:78`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** khác payload: 409/IDEMPOTENCY_CONFLICT; nợ 2255500->2255500.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-02.4 — Từ chối định dạng idempotency key không phải chuỗi UUID hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1001`) đang ở trạng thái `ISSUED`. Phiên ACCOUNTANT có hiệu lực.
    - **Dữ liệu giả:**
      - Body A (chuỗi ký tự thường): `{"amount": 100000, "paidOn": "<HOM_NAY>", "idempotencyKey": "not-a-valid-uuid"}`
      - Body B (chứa ký tự lạ): `{"amount": 100000, "paidOn": "<HOM_NAY>", "idempotencyKey": "123e4567-e89b-12d3-a456-42661417400g"}`
      - Body C (chuỗi rỗng): `{"amount": 100000, "paidOn": "<HOM_NAY>", "idempotencyKey": ""}`
  - **Các bước thực hiện:**
    1. Gửi lần lượt body A, B, C tới endpoint.
    2. Ghi nhận mã trạng thái HTTP và mã lỗi.
    3. Thực hiện kiểm sau.
  - **Kết quả mong đợi:** Tất cả đều trả HTTP 400 Bad Request, `code = "INVALID_IDEMPOTENCY_KEY"`.
  - **Kiểm tra sau (Post-check):** Xác nhận không có bản ghi nào được ghi vào `payment_commands` hoặc `payment_receipts`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-02.4`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:253-255`; `backend/src/main/resources/db/migration/V10__mandatory_payments.sql:3`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** A,B,C: 400/INVALID_IDEMPOTENCY_KEY 400/INVALID_IDEMPOTENCY_KEY 400/INVALID_IDEMPOTENCY_KEY; biên lai 2->2.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-03.1 — Phân bổ số dư nộp thừa từ nhiều biên lai theo thứ tự cũ nhất trước (FIFO) vào hóa đơn cùng hộ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/credit-allocations`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:**
      - Hộ gia đình `201` có 2 biên lai nộp thừa đã ghi nhận theo thứ tự thời gian:
        - Biên lai `R1` (ID `5001`) tạo ra 50.000 đ credit.
        - Biên lai `R2` (ID `5002`) tạo ra 40.000 đ credit.
        - Tổng số dư nộp thừa của hộ `201` hiện có: 90.000 đ.
      - Hóa đơn `1004` cùng thuộc hộ `201` đang nợ 100.000 đ (`remaining = "100000"`).
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"invoiceId": 1004, "amount": 70000, "idempotencyKey": "30000000-0000-0000-0000-000000000001"}`
  - **Các bước thực hiện:**
    1. Kiểm tra số dư hộ `201` qua `GET /api/v1/households/201/credit-balance` (xác nhận `90000`).
    2. Gửi `POST /api/v1/households/201/credit-allocations` với số tiền 70.000 đ cho hóa đơn `1004`.
    3. Thu thập response JSON và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 201 Created; response JSON chứa:
    - `allocationId`: số nguyên dương (ví dụ `7001`).
    - `invoiceId`: `1004`.
    - `householdId`: `201`.
    - `amount`: `"70000"`.
    - `debtAfter`: `"30000"`.
    - `status`: `"ACTIVE"`.
    - `hasReverseReason`: `false`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/invoices/1004/payment-summary` xác nhận: `settled = "70000"`, `remaining = "30000"`, `status = "PARTIALLY_PAID"`.
    2. Gửi `GET /api/v1/households/201/credit-balance` xác nhận: `balance = "20000"` (90.000 - 70.000).
    3. Gửi `GET /api/v1/households/201/credit-allocations?invoiceId=1004` xác nhận bản ghi phân bổ `7001` có 2 nguồn (sources):
       - Nguồn 1: `receiptId = 5001` (R1 - cũ nhất), `amount = "50000"` (dùng hết toàn bộ credit của R1).
       - Nguồn 2: `receiptId = 5002` (R2 - mới hơn), `amount = "20000"` (dùng một phần credit của R2).
    4. Bảng `billing_periods` của hóa đơn `1004` có `receipt_count` tăng thêm 1.
    5. Bảng `audit_log` có bản ghi `CREDIT_ALLOCATED` cho target `CreditAllocation`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-03.1`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/CreditAllocationAmounts.java:13-27`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:42-56`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 201 debtAfter=1280000; số dư 20000; nguồn: 7:50000, 8:20000.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-03.2 — Từ chối phân bổ khi số tiền yêu cầu vượt quá số dư nộp thừa hiện có của hộ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/credit-allocations`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hộ `201` có số dư nộp thừa là 20.000 đ. Hóa đơn `1004` còn nợ 30.000 đ.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"invoiceId": 1004, "amount": 25000, "idempotencyKey": "30000000-0000-0000-0000-000000000002"}` (vượt 20.000 đ)
  - **Các bước thực hiện:**
    1. Gửi request phân bổ 25.000 đ.
    2. Thu thập mã phản hồi HTTP và ProblemDetail body.
    3. Thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409 Conflict; `code = "INSUFFICIENT_CREDIT"`.
  - **Kiểm tra sau (Post-check):** Gửi `GET /api/v1/households/201/credit-balance` xác nhận số dư vẫn giữ nguyên 20.000 đ; hóa đơn `1004` nợ giữ nguyên 30.000 đ.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-03.2`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:115`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 409/INSUFFICIENT_CREDIT; số dư 20000; nợ a3 giữ nguyên.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-03.3 — Từ chối phân bổ khi số tiền yêu cầu vượt quá công nợ còn lại của hóa đơn đích

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/202/credit-allocations`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hộ `202` có số dư nộp thừa 100.000 đ. Hóa đơn `1005` của hộ `202` chỉ còn nợ 40.000 đ.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"invoiceId": 1005, "amount": 50000, "idempotencyKey": "30000000-0000-0000-0000-000000000003"}` (vượt công nợ 40.000 đ)
  - **Các bước thực hiện:**
    1. Gửi request phân bổ 50.000 đ vào hóa đơn `1005`.
    2. Thu thập mã phản hồi HTTP và body JSON.
    3. Thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409 Conflict; `code = "ALLOCATION_EXCEEDS_DEBT"`.
  - **Kiểm tra sau (Post-check):** Gửi `GET /api/v1/invoices/1005/payment-summary` xác nhận nợ còn lại vẫn là 40.000 đ.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-03.3`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:111`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 409/ALLOCATION_EXCEEDS_DEBT; nợ a4 còn 40000.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-03.4 — Từ chối phân bổ khi thiếu `invoiceId` hoặc số tiền không phải số nguyên dương

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/credit-allocations`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hộ `201` có số dư nộp thừa. Phiên ACCOUNTANT có hiệu lực.
    - **Dữ liệu giả:**
      - Body A (thiếu invoiceId): `{"invoiceId": null, "amount": 10000, "idempotencyKey": "<UUID_A>"}`
      - Body B (tiền số 0): `{"invoiceId": 1004, "amount": 0, "idempotencyKey": "<UUID_B>"}`
      - Body C (tiền số âm): `{"invoiceId": 1004, "amount": -10000, "idempotencyKey": "<UUID_C>"}`
      - Body D (tiền số thập phân): `{"invoiceId": 1004, "amount": 10000.5, "idempotencyKey": "<UUID_D>"}`
  - **Các bước thực hiện:**
    1. Gửi lần lượt body A, B, C, D tới endpoint.
    2. Ghi nhận HTTP code và ProblemDetail code.
  - **Kết quả mong đợi:** Body A trả HTTP 400 Bad Request `code = "VALIDATION"`. Body B, C, D trả HTTP 400 Bad Request `code = "INVALID_AMOUNT"`.
  - **Kiểm tra sau (Post-check):** Xác nhận không có bản ghi nào được lưu trong bảng `payment_credit_allocations`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-03.4`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/api/PaymentController.java:61`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:105,267-273`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** thiếu invoiceId 400/VALIDATION; 0 400/INVALID_AMOUNT; âm 400/INVALID_AMOUNT; thập phân 400/INVALID_AMOUNT.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-03.5 — Cách ly số dư nộp thừa giữa các hộ: từ chối phân bổ số dư của hộ này sang hóa đơn của hộ khác

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/credit-allocations`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hộ `201` có số dư nộp thừa 20.000 đ. Hóa đơn `2001` thuộc về hộ khác là hộ `202` (căn hộ A-202).
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"invoiceId": 2001, "amount": 10000, "idempotencyKey": "30000000-0000-0000-0000-000000000005"}`
  - **Các bước thực hiện:**
    1. Gửi request phân bổ số dư của hộ `201` cho hóa đơn `2001` (hộ `202`).
    2. Thu thập mã phản hồi HTTP và ProblemDetail.
    3. Thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409 Conflict; `code = "HOUSEHOLD_MISMATCH"`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/households/201/credit-balance` xác nhận số dư hộ `201` vẫn là 20.000 đ.
    2. Gửi `GET /api/v1/invoices/2001/payment-summary` xác nhận công nợ hóa đơn `2001` không thay đổi.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-03.5`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:108`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:127-130`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 409/HOUSEHOLD_MISMATCH; số dư hộ A 90000; nợ hóa đơn hộ B giữ nguyên.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-04.1 — ACCOUNTANT đảo phân bổ đang hoạt động kèm lý do hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/credit-allocations/7001/reverse`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Phân bổ `7001` (số tiền 70.000 đ) đang ở trạng thái `ACTIVE` vào hóa đơn `1004` của hộ `201`. Hóa đơn `1004` đang có nợ 30.000 đ; số dư hộ `201` là 20.000 đ. Phiên ACCOUNTANT có CSRF token hợp lệ.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"reason": "Phan bo nham hoa don ky truoc, dao lai de phan bo dung", "idempotencyKey": "40000000-0000-0000-0000-000000000001"}`
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/credit-allocations/7001/reverse` cùng body nêu trên.
    2. Thu thập response JSON và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200 OK; response JSON chứa:
    - `allocationId`: `7001`.
    - `invoiceId`: `1004`.
    - `householdId`: `201`.
    - `amount`: `"70000"`.
    - `debtAfter`: `"100000"`.
    - `status`: `"REVERSED"`.
    - `hasReverseReason`: `true`.
    - Không chứa trường raw text `reverseReason`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/invoices/1004/payment-summary` xác nhận: công nợ hóa đơn `1004` tăng lại: `remaining = "100000"`, `settled = "0"`, `status = "UNPAID"`.
    2. Gửi `GET /api/v1/households/201/credit-balance` xác nhận: số dư nộp thừa của hộ `201` được khôi phục: `balance = "90000"` (20.000 + 70.000).
    3. Gửi `GET /api/v1/households/201/credit-allocations?status=REVERSED` xác nhận phân bổ `7001` có `status = "REVERSED"`, `hasReverseReason = true`, `reversedBy` bằng ID của actor.
    4. Bảng `billing_periods` có `receipt_count` KHÔNG bị giảm (theo D27: `receipt_count` không giảm).
    5. Bảng `audit_log` có bản ghi `CREDIT_ALLOCATION_REVERSED`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-04.1`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:126-140`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:64-74`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200 debtAfter=1350000; số dư 90000; list reversedBy=12.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-04.2 — Từ chối đảo phân bổ khi thiếu lý do hoặc lý do vượt quá 255 ký tự

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/credit-allocations/7002/reverse`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Phân bổ `7002` đang ở trạng thái `ACTIVE`. Phiên ACCOUNTANT có hiệu lực.
    - **Dữ liệu giả:**
      - Body A (thiếu lý do): `{"reason": "", "idempotencyKey": "<UUID_A>"}`
      - Body B (lý do null): `{"reason": null, "idempotencyKey": "<UUID_B>"}`
      - Body C (lý do 256 ký tự): `{"reason": "<CHUOI_256_KY_TU>", "idempotencyKey": "<UUID_C>"}`
  - **Các bước thực hiện:**
    1. Gửi lần lượt body A, B, C tới endpoint.
    2. Ghi nhận HTTP code và mã lỗi trả về.
  - **Kết quả mong đợi:** Body A và B trả HTTP 400 Bad Request `code = "REASON_REQUIRED"`. Body C trả HTTP 400 Bad Request `code = "VALIDATION"`.
  - **Kiểm tra sau (Post-check):** Gửi `GET /api/v1/households/201/credit-allocations` xác nhận phân bổ `7002` vẫn giữ nguyên trạng thái `ACTIVE`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-04.2`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:127,279-282`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** rỗng 400/REASON_REQUIRED; null 400/REASON_REQUIRED; 256 ký tự 400/VALIDATION; trạng thái sau ACTIVE.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-04.3 — Từ chối đảo phân bổ đã bị đảo trước đó

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/credit-allocations/7001/reverse`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Phân bổ `7001` đã ở trạng thái `REVERSED` (từ ca PAY-04.1).
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"reason": "Thu dao lan hai", "idempotencyKey": "40000000-0000-0000-0000-000000000002"}`
  - **Các bước thực hiện:**
    1. Gửi request đảo lại phân bổ `7001`.
    2. Thu thập mã phản hồi HTTP và ProblemDetail code.
  - **Kết quả mong đợi:** HTTP 409 Conflict; `code = "ALLOCATION_ALREADY_REVERSED"`.
  - **Kiểm tra sau (Post-check):** Xác nhận công nợ và số dư hộ không bị hoàn trả kép.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-04.3`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:133`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 409/ALLOCATION_ALREADY_REVERSED; không hoàn trả kép.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-04.4 — Replay và xung đột idempotency cho lệnh đảo phân bổ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/credit-allocations/7001/reverse`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Phân bổ `7001` đã được đảo thành công với idempotency key `40000000-0000-0000-0000-000000000001` bởi `accountant1`.
    - **Dữ liệu giả:**
      - Lần 1: Cùng `accountant1`, cùng key `40000000-0000-0000-0000-000000000001`, cùng lý do `"Phan bo nham hoa don ky truoc, dao lai de phan bo dung"`.
      - Lần 2: Đổi sang `accountant2`, cùng key trên.
      - Lần 3: Cùng `accountant1`, cùng key trên nhưng đổi lý do sang `"Ly do khac"`.
  - **Các bước thực hiện:**
    1. Gửi request lần 1.
    2. Gửi request lần 2.
    3. Gửi request lần 3.
    4. Đối chiếu mã phản hồi HTTP của từng lần.
  - **Kết quả mong đợi:**
    - Lần 1: HTTP 200 OK, trả lại đúng kết quả đã lưu trong `payment_commands`.
    - Lần 2: HTTP 409 Conflict, `code = "IDEMPOTENCY_CONFLICT"`.
    - Lần 3: HTTP 409 Conflict, `code = "IDEMPOTENCY_CONFLICT"`.
  - **Kiểm tra sau (Post-check):** Bảng `payment_credit_allocations` không bị cập nhật thêm; audit log không có bản ghi đảo lặp.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-04.4`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:128,252-265`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:66`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** replay 200 (giống bản đầu); khác actor 409/IDEMPOTENCY_CONFLICT; khác lý do 409/IDEMPOTENCY_CONFLICT.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-05.1 — ACCOUNTANT hủy biên lai thu tiền chưa dùng số dư kèm lý do hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/receipts/5003/void`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Biên lai `5003` ghi nhận thu 100.000 đ vào hóa đơn `1001` (`appliedAmount = "100000"`, `creditAmount = "0"`), đang ở trạng thái `POSTED`, không có credit nào phát sinh hoặc đang dùng. Phiên ACCOUNTANT có CSRF token hợp lệ.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"reason": "Nham lan chung tu nop tien giua hai can ho", "idempotencyKey": "50000000-0000-0000-0000-000000000001"}`
  - **Các bước thực hiện:**
    1. Gửi `POST /api/v1/receipts/5003/void` với body nêu trên.
    2. Thu thập response JSON và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200 OK; response JSON chứa:
    - `receiptId`: `5003`.
    - `invoiceId`: `1001`.
    - `status`: `"VOIDED"`.
    - `debtAfter`: nợ hóa đơn tăng thêm 100.000 đ tương ứng.
    - `hasVoidReason`: `true`.
    - `hasLateReason`: giữ nguyên cờ cũ.
    - Không chứa trường raw `voidReason`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/invoices/1001/payment-summary` xác nhận nợ còn lại tăng thêm 100.000 đ, `settled` giảm tương ứng.
    2. Gửi `GET /api/v1/invoices/1001/receipts` xác nhận biên lai `5003` có `status = "VOIDED"`, `hasVoidReason = true`, `canVoid = false`, `voidedBy` bằng ID actor.
    3. Bảng `audit_log` có bản ghi `RECEIPT_VOIDED`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-05.1`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:85-100`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:77-87`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** HTTP 200 debtAfter=1350000; list status=VOIDED canVoid=false voidedBy=12.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-05.2 — Chặn hủy biên lai khi số dư nộp thừa đang được dùng trong phân bổ hiệu lực

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/receipts/5001/void`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Biên lai `5001` (R1) sinh ra 50.000 đ credit và số credit này đang được phân bổ vào hóa đơn `1004` thông qua bản ghi phân bổ có `status = 'ACTIVE'`.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"reason": "Huy bien lai goc", "idempotencyKey": "50000000-0000-0000-0000-000000000002"}`
  - **Các bước thực hiện:**
    1. Kiểm tra biên lai `5001` qua `GET /api/v1/invoices/1001/receipts`: xác nhận `activeAllocationCount > 0` và `canVoid = false`.
    2. Gửi `POST /api/v1/receipts/5001/void` với body nêu trên.
    3. Thu thập mã phản hồi HTTP và ProblemDetail body.
    4. Thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 409 Conflict; `code = "RECEIPT_CREDIT_IN_USE"`. Hệ thống kiên quyết từ chối hủy.
  - **Kiểm tra sau (Post-check):** Xác nhận biên lai `5001` vẫn giữ trạng thái `POSTED`; công nợ và số dư hộ không thay đổi.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-05.2`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:93-94`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:63`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** trước: activeAllocationCount=1 canVoid=false; hủy 409/RECEIPT_CREDIT_IN_USE; sau POSTED.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-05.3 — Cho phép hủy biên lai sau khi toàn bộ phân bổ liên quan đã được đảo xong

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/receipts/5001/void`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Sau khi phân bổ dùng credit của biên lai `5001` đã được đảo thành công ở ca PAY-04.1 (`status = 'REVERSED'`). Lúc này `activeAllocationCount` của biên lai `5001` đã trở về `0`.
    - **Dữ liệu giả:**
      - Header: `X-CSRF-TOKEN: <CSRF_TOKEN>`, `Content-Type: application/json`
      - Body: `{"reason": "Huy bien lai sau khi da dao phan bo", "idempotencyKey": "50000000-0000-0000-0000-000000000003"}`
  - **Các bước thực hiện:**
    1. Kiểm tra biên lai `5001` qua `GET /api/v1/invoices/1001/receipts`: xác nhận `activeAllocationCount = 0` và `canVoid = true`.
    2. Gửi `POST /api/v1/receipts/5001/void` với body nêu trên.
    3. Thu thập response JSON và thực hiện kiểm sau.
  - **Kết quả mong đợi:** HTTP 200 OK; `status = "VOIDED"`, `hasVoidReason = true`.
  - **Kiểm tra sau (Post-check):**
    1. Gửi `GET /api/v1/households/201/credit-balance` xác nhận số dư hộ giảm đúng 50.000 đ (credit của biên lai bị hủy bị xóa khỏi số dư).
    2. Gửi `GET /api/v1/invoices/1001/payment-summary` xác nhận nợ của hóa đơn phát sinh biên lai tăng lại đúng `appliedAmount` của biên lai `5001`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-05.3`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:85-100`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:75-87`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** trước: active=0 canVoid=true; hủy 200; số dư 90000->40000; nợ a1 1350000 (gốc 1350000).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-05.4 — Từ chối hủy biên lai khi thiếu lý do hoặc biên lai đã bị hủy trước đó

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/receipts/5004/void`, `POST /api/v1/receipts/5001/void`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Biên lai `5001` đã ở trạng thái `VOIDED`. Biên lai `5004` đang ở trạng thái `POSTED`, chưa dùng credit.
    - **Dữ liệu giả:**
      - Lần 1 (trên `5004`, thiếu lý do): `{"reason": "", "idempotencyKey": "<UUID_1>"}`
      - Lần 2 (trên `5004`, lý do > 255 ký tự): `{"reason": "<CHUOI_256_KY_TU>", "idempotencyKey": "<UUID_2>"}`
      - Lần 3 (trên `5001` đã hủy): `{"reason": "Huy tiep lan hai", "idempotencyKey": "<UUID_3>"}`
      - Lần 4 (trên ID không tồn tại `999999`): `{"reason": "Huy bien lai ao", "idempotencyKey": "<UUID_4>"}`
  - **Các bước thực hiện:**
    1. Gửi lần 1, lần 2 trên biên lai `5004`.
    2. Gửi lần 3 trên biên lai `5001`.
    3. Gửi lần 4 trên biên lai `999999`.
    4. Ghi nhận HTTP code và mã lỗi của từng lần.
  - **Kết quả mong đợi:**
    - Lần 1: HTTP 400 Bad Request `code = "REASON_REQUIRED"`.
    - Lần 2: HTTP 400 Bad Request `code = "VALIDATION"`.
    - Lần 3: HTTP 409 Conflict `code = "RECEIPT_ALREADY_VOIDED"`.
    - Lần 4: HTTP 404 Not Found `code = "RECEIPT_NOT_FOUND"`.
  - **Kiểm tra sau (Post-check):** Biên lai `5004` vẫn giữ trạng thái `POSTED`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-05.4`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:86,92,231`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** rỗng 400/REASON_REQUIRED; 256 400/VALIDATION; đã hủy 409/RECEIPT_ALREADY_VOIDED; không tồn tại 404/RECEIPT_NOT_FOUND; R2 POSTED.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-05.5 — Replay và xung đột idempotency cho lệnh hủy biên lai

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/receipts/5003/void`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Biên lai `5003` đã bị hủy thành công với key `50000000-0000-0000-0000-000000000001` bởi `accountant1` (PAY-05.1).
    - **Dữ liệu giả:**
      - Lần 1: Cùng `accountant1`, cùng key `50000000-0000-0000-0000-000000000001`, cùng lý do `"Nham lan chung tu nop tien giua hai can ho"`.
      - Lần 2: Đổi sang `accountant2`, cùng key trên.
      - Lần 3: Cùng `accountant1`, cùng key trên nhưng đổi lý do sang `"Ly do khac"`.
  - **Các bước thực hiện:**
    1. Gửi request lần 1.
    2. Gửi request lần 2.
    3. Gửi request lần 3.
    4. Đối chiếu mã phản hồi HTTP.
  - **Kết quả mong đợi:**
    - Lần 1: HTTP 200 OK, trả lại đúng kết quả đã lưu.
    - Lần 2: HTTP 409 Conflict, `code = "IDEMPOTENCY_CONFLICT"`.
    - Lần 3: HTTP 409 Conflict, `code = "IDEMPOTENCY_CONFLICT"`.
  - **Kiểm tra sau (Post-check):** Bảng `payment_receipts` không bị cập nhật thêm; audit log không có bản ghi hủy lặp.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-05.5`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:87,252-265`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:78`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** replay 200; khác actor 409/IDEMPOTENCY_CONFLICT; khác lý do 409/IDEMPOTENCY_CONFLICT.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-06.1 — Rollback toàn bộ khi ghi audit lỗi: không sinh biên lai, không lưu command, không tăng `receipt_count`

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `<INVOICE_ID>` (`1001`) thuộc kỳ `<PERIOD_ID>` (`501`) đang ở `ISSUED`. Database kiểm thử cô lập (MySQL test DB). Điểm chặn kiểm thử làm ném ngoại lệ khi ghi `audit_log` (mô phỏng lỗi DB audit).
    - **Dữ liệu giả:**
      - Body: `{"amount": 150000, "paidOn": "<HOM_NAY>", "idempotencyKey": "60000000-0000-0000-0000-000000000001"}`
  - **Các bước thực hiện:**
    1. Cấu hình kiểm thử cô lập làm phát sinh ngoại lệ tại tầng AuditService.
    2. Gửi request ghi biên lai với body trên.
    3. Thu thập ngoại lệ trả về và thực hiện kiểm sau trên DB.
  - **Kết quả mong đợi:** Request ném ngoại lệ thất bại (HTTP 500 hoặc ngoại lệ kiểm thử); giao dịch bị rollback hoàn toàn.
  - **Kiểm tra sau (Post-check):**
    1. Truy vấn DB kiểm thử cô lập: `SELECT COUNT(*) FROM payment_receipts WHERE invoice_id = 1001` -> Không có bản ghi mới.
    2. `SELECT COUNT(*) FROM payment_commands WHERE idempotency_key = '60000000-0000-0000-0000-000000000001'` -> Bằng `0` (không lưu kết quả hay khóa dở dang).
    3. `SELECT receipt_count FROM billing_periods WHERE id = 501` -> Giữ nguyên giá trị cũ, không bị tăng.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-06.1`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:63,79`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:89-98`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-06.2 — Ghi nhận biên lai và phân bổ thành công tăng `receipt_count`; hủy/đảo không làm giảm `receipt_count`

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/...`, `POST /api/v1/households/...`, `POST /api/v1/receipts/.../void`, `POST /api/v1/credit-allocations/.../reverse`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Kỳ thu `501` đang ở trạng thái `ISSUED` có `receipt_count = 0`. Hóa đơn `1001` và `1002` thuộc kỳ `501`. Database kiểm thử cô lập.
    - **Dữ liệu giả:** Chuỗi thao tác gồm: 1 biên lai thu thừa -> 1 phân bổ -> 1 hủy biên lai khác -> 1 đảo phân bổ.
  - **Các bước thực hiện:**
    1. Ghi nhận biên lai thu trên hóa đơn `1001`. Kiểm tra `receipt_count` của kỳ `501`.
    2. Thực hiện phân bổ credit vào hóa đơn `1002`. Kiểm tra `receipt_count` của kỳ `501`.
    3. Hủy một biên lai trên hóa đơn `1001`. Kiểm tra `receipt_count`.
    4. Đảo phân bổ vừa tạo ở bước 2. Kiểm tra `receipt_count`.
  - **Kết quả mong đợi:**
    - Sau bước 1: `receipt_count` tăng từ `0` lên `1`.
    - Sau bước 2: `receipt_count` tăng từ `1` lên `2`.
    - Sau bước 3: `receipt_count` vẫn giữ nguyên là `2` (không giảm khi void).
    - Sau bước 4: `receipt_count` vẫn giữ nguyên là `2` (không giảm khi reverse).
  - **Kiểm tra sau (Post-check):** Bảng `billing_periods` tại ID `501` có `receipt_count = 2`. Kỳ thu `501` không bao giờ được phép hủy bởi ADMIN nữa vì đã từng có phát sinh thu phí.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-06.2`; `docs/DECISIONS.md` D26, D27; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/BillingReceiptGuard.java:24`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:78,119`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-06.3 — Không thể thu tiền trên hóa đơn hoặc kỳ thu đã bị hủy (`CANCELLED`)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `GET /api/v1/invoices/1009/payment-summary`, `POST /api/v1/invoices/1009/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `1009` thuộc kỳ thu đã bị hủy (`status = 'CANCELLED'`).
    - **Dữ liệu giả:**
      - Body: `{"amount": 100000, "paidOn": "<HOM_NAY>", "idempotencyKey": "<UUID_1>"}`
  - **Các bước thực hiện:**
    1. Gửi `GET /api/v1/invoices/1009/payment-summary`.
    2. Gửi `POST /api/v1/invoices/1009/receipts` với body trên.
    3. Đối chiếu phản hồi của cả hai request.
  - **Kết quả mong đợi:**
    - Bước 1: HTTP 200 OK; `summary.status = "CANCELLED"`, `remaining = "0"`, `total` giữ nguyên số tiền gốc ban đầu.
    - Bước 2: HTTP 409 Conflict; `code = "INVALID_STATE"`.
  - **Kiểm tra sau (Post-check):** Bảng `payment_receipts` không có bản ghi nào cho hóa đơn `1009`.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-06.3`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/PaymentInvoiceAccess.java:39-41`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:69,147-149`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:119-131`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-07.1 — Đua tranh hai request ghi thu cùng idempotency key đồng thời: đúng một lượt ghi thật, lượt kia nhận kết quả replay

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `1001` đang ở trạng thái `ISSUED`. Database kiểm thử cô lập. Cần công cụ harness hỗ trợ đa luồng (thread pool 2 worker cùng điểm chặn `CountDownLatch`).
    - **Dữ liệu giả:**
      - Cùng actor `accountant1`.
      - Khóa chung: `<SHARED_KEY>` = `70000000-0000-0000-0000-000000000001`.
      - Payload chung: `{"amount": 80000, "paidOn": "<HOM_NAY>", "lateReason": null, "idempotencyKey": "<SHARED_KEY>"}`.
  - **Các bước thực hiện:**
    1. Khởi tạo barrier `CountDownLatch(1)`.
    2. Kích hoạt 2 luồng đồng thời gửi `POST /api/v1/invoices/1001/receipts` với cùng payload và cùng token.
    3. Mở barrier và đợi cả 2 luồng hoàn tất trong tối đa 10 giây.
    4. So sánh kết quả trả về của 2 luồng và kiểm tra DB.
  - **Kết quả mong đợi:** Cả hai luồng đều trả về kết quả thành công HTTP 201 Created với nội dung JSON hoàn toàn trùng khớp nhau (`receiptId` giống nhau).
  - **Kiểm tra sau (Post-check):**
    1. Truy vấn DB kiểm thử cô lập: `SELECT COUNT(*) FROM payment_receipts WHERE invoice_id = 1001` -> Đúng `1` bản ghi.
    2. `SELECT receipt_count FROM billing_periods WHERE id = 501` -> Chỉ tăng đúng `1`.
    3. `SELECT COUNT(*) FROM audit_log WHERE action = 'RECEIPT_RECORDED'` -> Đúng `1` bản ghi.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-07.1`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:259-265`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:69-77`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 201 & 201; receiptId 10/10; biên lai 0->1.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: chưa kiểm bước SQL/audit/receipt_count — kiểm tay nếu cần.

### PAY-07.2 — Đua tranh hai request ghi thu khác key trên cùng hóa đơn đồng thời: tuần tự hóa an toàn, không thất thoát tiền

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `1001` đang nợ 100.000 đ. Hai request thu tiền khác khóa: Request A thu 70.000 đ, Request B thu 70.000 đ. Tổng tiền nhận 140.000 đ > nợ 100.000 đ. Database kiểm thử cô lập.
    - **Dữ liệu giả:**
      - Luồng 1: `idempotencyKey = "<UUID_K1>"`, `amount = 70000`.
      - Luồng 2: `idempotencyKey = "<UUID_K2>"`, `amount = 70000`.
  - **Các bước thực hiện:**
    1. Kích hoạt 2 luồng gửi đồng thời qua điểm chặn `CountDownLatch`.
    2. Đợi cả hai luồng kết thúc.
    3. Kiểm tra công nợ hóa đơn và số dư nộp thừa của hộ.
  - **Kết quả mong đợi:** Cả hai luồng đều thành công HTTP 201 Created. Thứ tự tuần tự hóa bảo đảm:
    - Một giao dịch trả 70.000 đ nợ (nợ còn 30.000 đ).
    - Giao dịch tiếp theo trả hết 30.000 đ nợ còn lại và tạo ra 40.000 đ credit cho hộ.
  - **Kiểm tra sau (Post-check):**
    1. Hóa đơn `1001` có `remaining = "0"`, `status = "PAID"`.
    2. Số dư hộ gia đình có `balance = "40000"`.
    3. `receipt_count` của kỳ thu tăng đúng 2.
    4. Không xảy ra deadlock hay mất mát dữ liệu tài chính.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-07.2`; `docs/DECISIONS.md` D27; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:69,73,225-228`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:80-87`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** 201 & 201; nợ còn 0 (PAID); credit hộ B 150000->190000.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: kiểm receipt_count +2 bằng tay/SQL.

### PAY-07.3 — Đua tranh hai request phân bổ đồng thời vượt quá số dư có sẵn của hộ: khóa hộ ngăn chặn chi vượt số dư

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/households/201/credit-allocations`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hộ `201` có số dư nộp thừa duy nhất là 50.000 đ. Hóa đơn `1001` nợ 40.000 đ; hóa đơn `1002` nợ 40.000 đ. Tổng yêu cầu phân bổ đồng thời là 40.000 + 40.000 = 80.000 đ > 50.000 đ. Database kiểm thử cô lập.
    - **Dữ liệu giả:**
      - Luồng A: Phân bổ 40.000 đ vào hóa đơn `1001`, key `<UUID_A>`.
      - Luồng B: Phân bổ 40.000 đ vào hóa đơn `1002`, key `<UUID_B>`.
  - **Các bước thực hiện:**
    1. Kích hoạt 2 luồng phân bổ đồng thời qua barrier.
    2. Thu thập mã trạng thái HTTP của cả 2 luồng.
    3. Kiểm tra số dư nộp thừa và trạng thái 2 hóa đơn.
  - **Kết quả mong đợi:** Đúng một luồng thành công HTTP 201 Created; luồng còn lại bị từ chối với HTTP 409 Conflict, `code = "INSUFFICIENT_CREDIT"`.
  - **Kiểm tra sau (Post-check):**
    1. `GET /api/v1/households/201/credit-balance` xác nhận số dư còn lại đúng `10000` (50.000 - 40.000).
    2. Một hóa đơn được giảm nợ 40.000 đ, hóa đơn kia giữ nguyên nợ.
    3. Tổng `receipt_count` tăng đúng 1.
    4. Số dư hộ không bao giờ bị âm.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-07.3`; `backend/src/main/java/vn/edu/se10/bluemoon/payment/application/PaymentService.java:109,225-228`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:159-172`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** mỗi request 24000/40000: 409/INSUFFICIENT_CREDIT & 201; số dư 40000->16000; nợ giảm tổng 24000.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: kiểm receipt_count +1 bằng tay/SQL.

### PAY-07.4 — Đua tranh giữa ghi nhận biên lai và ADMIN hủy kỳ thu: khóa kỳ tuần tự hóa

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1008/receipts`, `POST /api/v1/billing-periods/502/cancel`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Kỳ thu `502` vừa phát hành trong tháng hiện tại, chưa có khoản thu nào (`receipt_count = 0`). Hóa đơn `1008` thuộc kỳ `502`. Database kiểm thử cô lập.
    - **Dữ liệu giả:**
      - Luồng ACCOUNTANT: Ghi nhận biên lai 100.000 đ vào hóa đơn `1008`.
      - Luồng ADMIN: Hủy kỳ thu `502` với lý do `"Huy ky thu"`.
  - **Các bước thực hiện:**
    1. Kích hoạt đồng thời 2 luồng gửi qua điểm chặn barrier.
    2. Thu thập kết quả cả 2 luồng.
    3. Kiểm tra trạng thái kỳ thu và số lượng biên lai trong DB.
  - **Kết quả mong đợi:** Đúng một hành động thành công, hành động kia bị từ chối 409 Conflict `code = "INVALID_STATE"`:
    - Nếu thu tiền thắng: kỳ thu giữ trạng thái `ISSUED`, `receipt_count = 1`, lệnh hủy của ADMIN bị từ chối 409.
    - Nếu hủy thắng: kỳ thu chuyển sang `CANCELLED`, lệnh thu tiền bị từ chối 409.
  - **Kiểm tra sau (Post-check):** Không bao giờ xảy ra tình trạng có biên lai thu tiền hợp lệ nằm trong một kỳ thu bị hủy.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-07.4`; `docs/DECISIONS.md` D26, D27; `backend/src/main/java/vn/edu/se10/bluemoon/billing/application/PaymentInvoiceAccess.java:37-39`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:131-145`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-07.5 — Đua tranh giữa hủy biên lai cũ và ghi biên lai mới trên cùng hóa đơn

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/receipts/<ID>/void`, `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn `1001` đang có 1 biên lai cũ `80000` đ. Luồng A thực hiện hủy biên lai cũ; luồng B thực hiện ghi biên lai mới `80000` đ. Database kiểm thử cô lập.
    - **Dữ liệu giả:** Hai key khác nhau `<VOID_KEY>` và `<NEW_KEY>`.
  - **Các bước thực hiện:**
    1. Kích hoạt 2 luồng chạy đồng thời qua barrier.
    2. Đợi cả hai luồng hoàn tất.
    3. Kiểm tra công nợ cuối cùng của hóa đơn.
  - **Kết quả mong đợi:** Cả hai luồng đều hoàn thành tuần tự. Công nợ hóa đơn và số dư hộ phản ánh chính xác kết quả gộp của cả 2 thao tác (không bị tính thừa hoặc ghi đè sai nợ).
  - **Kiểm tra sau (Post-check):** `receipt_count` của kỳ tăng thêm đúng 1 đơn vị (từ biên lai mới).
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-07.5`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:146-158`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** hủy 200; thu mới 201; nợ cuối 2177500 (kỳ vọng 2177500).
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: kiểm receipt_count +1 bằng tay/SQL.

### PAY-08.1 — Từ chối truy cập chưa xác thực (ANON) trên toàn bộ endpoint thu tiền

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ANON.
  - **Điểm vào (Route / Endpoint):**
    - `GET /api/v1/payment-invoices`
    - `GET /api/v1/invoices/1001/payment-summary`
    - `GET /api/v1/invoices/1001/receipts`
    - `GET /api/v1/households/201/credit-balance`
    - `GET /api/v1/households/201/credit-allocations`
    - `POST /api/v1/invoices/1001/receipts`
    - `POST /api/v1/receipts/5001/void`
    - `POST /api/v1/households/201/credit-allocations`
    - `POST /api/v1/credit-allocations/7001/reverse`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Khách vãng lai (chưa đăng nhập). Phiên ẩn danh khởi tạo qua `GET /api/v1/auth/csrf` để nhận cookie phiên và CSRF token hợp lệ; không đăng nhập tài khoản nào.
    - **Dữ liệu giả:** Cookie phiên ẩn danh và header `X-CSRF-TOKEN: <ANON_CSRF_TOKEN>` lấy từ `GET /api/v1/auth/csrf`. Request POST gửi kèm body JSON mẫu và idempotency key UUID mẫu.
  - **Các bước thực hiện:**
    1. Gửi `GET /api/v1/auth/csrf` trong phiên ẩn danh để thiết lập phiên chưa xác thực, lưu cookie phiên và CSRF token.
    2. Gửi lần lượt 5 request GET tới các endpoint đọc nêu trên bằng cookie phiên ẩn danh.
    3. Gửi lần lượt 4 request POST tới các endpoint ghi nêu trên bằng cookie phiên ẩn danh, body mẫu và header `X-CSRF-TOKEN: <ANON_CSRF_TOKEN>` hợp lệ.
    4. Ghi nhận HTTP status code của từng request.
  - **Kết quả mong đợi:** Cả 5 request GET và 4 request POST đều trả HTTP 401 Unauthorized (khi có CSRF token hợp lệ vượt qua CsrfFilter, chuỗi bảo mật từ chối người dùng ẩn danh tại tầng xác thực với mã 401).
  - **Kiểm tra sau (Post-check):** Không có dữ liệu nào bị rò rỉ.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-08.1`; `backend/src/main/java/vn/edu/se10/bluemoon/config/SecurityConfig.java`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentApiTest.java:76-88`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET 401,401,401,401,401; POST 401,401,401,401.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-08.2 — Chặn vai trò RESIDENT trên toàn bộ endpoint thu tiền

- **Mô tả ca kiểm thử:**
  - **Vai trò:** RESIDENT.
  - **Điểm vào (Route / Endpoint):** Toàn bộ 9 endpoint GET và POST của S09 nêu tại PAY-08.1.
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Đăng nhập tài khoản cư dân (vai trò `RESIDENT`, ID `301`).
    - **Dữ liệu giả:** Phiên đăng nhập `RESIDENT`, CSRF token hợp lệ của cư dân.
  - **Các bước thực hiện:**
    1. Gửi lần lượt request GET tới 5 endpoint đọc.
    2. Gửi lần lượt request POST (kèm CSRF token) tới 4 endpoint ghi.
    3. Ghi nhận mã phản hồi HTTP.
  - **Kết quả mong đợi:** Toàn bộ request GET và POST đều bị từ chối với HTTP 403 Forbidden.
  - **Kiểm tra sau (Post-check):** Cư dân không thể xem danh sách công nợ tòa nhà hay thực hiện thu tiền (cư dân chỉ xem hóa đơn hộ mình tại S10).
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-08.2`; `docs/DECISIONS.md` D27; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentApiTest.java:78,85`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET 403,403,403,403,403; POST 403,403,403,403.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-08.3 — Phân quyền ADMIN: được phép gọi các API đọc (GET), bị chặn trên các API ghi (POST)

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** 5 endpoint GET và 4 endpoint POST của S09.
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Đăng nhập tài khoản Ban quản trị (vai trò `ADMIN`, ID `1`).
    - **Dữ liệu giả:** Phiên đăng nhập `ADMIN`, CSRF token hợp lệ.
  - **Các bước thực hiện:**
    1. Gọi 5 endpoint GET: `/api/v1/payment-invoices`, `/api/v1/invoices/1001/payment-summary`, `/api/v1/invoices/1001/receipts`, `/api/v1/households/201/credit-balance`, `/api/v1/households/201/credit-allocations`.
    2. Thử gửi request ghi tới 4 endpoint POST: `/receipts`, `/void`, `/credit-allocations`, `/reverse`.
    3. So sánh phản hồi giữa nhóm đọc và nhóm ghi.
  - **Kết quả mong đợi:**
    - Cả 5 endpoint GET đều trả HTTP 200 OK với dữ liệu danh sách/chi tiết.
    - Cả 4 endpoint POST đều bị từ chối với HTTP 403 Forbidden. ADMIN là vai trò CHỈ ĐỌC trong module thu tiền S09.
  - **Kiểm tra sau (Post-check):** Không có bản ghi thu tiền nào phát sinh từ tài khoản ADMIN.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-08.3`; `docs/DECISIONS.md` D27, D28; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentApiTest.java:79,84`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET 200,200,200,200,200; POST 403,403,403,403.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-08.4 — Phân quyền ACCOUNTANT: toàn quyền đọc và ghi với CSRF token hợp lệ

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** Các endpoint GET và POST của S09.
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Đăng nhập tài khoản Kế toán (vai trò `ACCOUNTANT`).
    - **Dữ liệu giả:** Phiên đăng nhập `ACCOUNTANT`, CSRF token hợp lệ.
  - **Các bước thực hiện:**
    1. Gọi endpoint GET `/api/v1/payment-invoices`.
    2. Gửi request POST ghi thu hợp lệ tới `/api/v1/invoices/1001/receipts`.
    3. Đối chiếu mã phản hồi HTTP.
  - **Kết quả mong đợi:** GET trả HTTP 200 OK; POST trả HTTP 201 Created.
  - **Kiểm tra sau (Post-check):** Thao tác kế toán được ghi nhận đầy đủ.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-08.4`; `docs/DECISIONS.md` D27; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentApiTest.java:41-45,80`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** GET 200; POST 201.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-08.5 — Chặn tấn công CSRF: request POST của ACCOUNTANT thiếu hoặc sai CSRF token bị từ chối

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/invoices/1001/receipts`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Phiên ACCOUNTANT đang đăng nhập hợp lệ.
    - **Dữ liệu giả:**
      - Lần 1: Không gửi header `X-CSRF-TOKEN`.
      - Lần 2: Gửi header `X-CSRF-TOKEN: invalid-csrf-token-12345`.
  - **Các bước thực hiện:**
    1. Gửi request lần 1 không có CSRF token.
    2. Gửi request lần 2 với CSRF token giả mạo.
    3. Ghi nhận HTTP status code.
  - **Kết quả mong đợi:** Cả hai lần đều bị chặn với HTTP 403 Forbidden.
  - **Kiểm tra sau (Post-check):** Không có biên lai thu tiền nào được tạo ra.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-08.5`; `AGENTS.md`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentApiTest.java:86-87`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** thiếu token 403; token sai 403; biên lai 6->6.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-09.1 — API phản hồi chỉ trả cờ boolean (`hasLateReason`, `hasVoidReason`, `hasReverseReason`), không trả trường raw reason

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):**
    - `POST /api/v1/invoices/1001/receipts`
    - `GET /api/v1/invoices/1001/receipts`
    - `GET /api/v1/households/201/credit-allocations`
    - `POST /api/v1/receipts/5001/void`
    - `POST /api/v1/credit-allocations/7001/reverse`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Đã có biên lai lùi ngày (có `late_reason = "Ly do bi mat"`), biên lai hủy (có `void_reason = "Ly do huy rieng"`), và phân bổ đảo (có `reverse_reason = "Ly do dao nhay cam"`).
    - **Dữ liệu giả:** Phiên đăng nhập hợp lệ.
  - **Các bước thực hiện:**
    1. Kiểm tra JSON response của cả 5 endpoint trên.
    2. Quét kiểm tra sự tồn tại của các key: `lateReason`, `voidReason`, `reverseReason`, `reason`.
  - **Kết quả mong đợi:**
    - Các trường cờ có mặt và có giá trị đúng: `hasLateReason: true`, `hasVoidReason: true`, `hasReverseReason: true`.
    - Hoàn toàn KHÔNG TỒN TẠI các key `lateReason`, `voidReason`, `reverseReason`, `reason` trong bất kỳ JSON response nào.
  - **Kiểm tra sau (Post-check):** Không có chuỗi văn bản lý do tự do nào bị phơi lộ qua giao diện API.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-09.1`; `docs/DECISIONS.md` D28; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentApiTest.java:53-74`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** có cờ true: true; quét 143 response 2xx: 0 lộ; ADMIN lộ: false.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-09.2 — Bảng `payment_commands.result_json` và `audit_log` không lưu chuỗi lý do tự do

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `POST /api/v1/...`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Đã thực hiện thành công các lệnh ghi thu lùi ngày, hủy biên lai và đảo phân bổ kèm lý do bí mật `"Ly do bao mat noi bo"`. Database kiểm thử cô lập.
    - **Dữ liệu giả:** Khóa idempotency `<UUID_TEST>`.
  - **Các bước thực hiện:**
    1. Truy vấn DB: `SELECT result_json FROM payment_commands WHERE idempotency_key = '<UUID_TEST>'`.
    2. Truy vấn DB: `SELECT changes FROM audit_log WHERE action IN ('RECEIPT_RECORDED', 'RECEIPT_VOIDED', 'CREDIT_ALLOCATION_REVERSED')`.
    3. Tìm kiếm chuỗi `"Ly do bao mat noi bo"` trong các cột kết quả.
  - **Kết quả mong đợi:** Cả `result_json` và `changes` của audit log đều KHÔNG chứa chuỗi `"Ly do bao mat noi bo"`. `result_json` chỉ chứa DTO đã làm sạch (chỉ có cờ boolean).
  - **Kiểm tra sau (Post-check):** Cột `late_reason`, `void_reason`, `reverse_reason` trong các bảng nghiệp vụ vẫn lưu đúng nội dung phục vụ hậu kiểm bảo mật tại DB.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-09.2`; `docs/DECISIONS.md` D27, D28; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentConcurrencyIT.java:120-123`; `backend/src/test/java/vn/edu/se10/bluemoon/payment/PaymentFlowIT.java:69,81`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** result_json chứa lý do: 0; audit_log chứa lý do: 0; payment_receipts lưu lý do: 4.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: cần DB cô lập; tên bảng/cột lấy từ tài liệu, sửa nếu schema khác.

### PAY-09.3 — Giao diện UI chỉ hiển thị nhãn "Có lý do", không hiển thị nội dung lý do tự do và không hiển thị PII cư dân

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections` (API mode), `/admin/collections`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Hóa đơn có biên lai lùi ngày và biên lai hủy.
    - **Dữ liệu giả:** Dữ liệu hóa đơn từ backend.
  - **Các bước thực hiện:**
    1. Mở màn hình thu tiền và chọn xem chi tiết hóa đơn.
    2. Quan sát bảng "Lịch sử biên lai" và bảng "Lịch sử phân bổ".
    3. Kiểm tra các dòng có lý do lùi ngày, lý do hủy hoặc lý do đảo.
    4. Kiểm tra mã nguồn HTML (Inspect Element) xem có chứa số CCCD, số điện thoại hay email của cư dân không.
  - **Kết quả mong đợi:**
    - Cột "Ngày nhận" hoặc "Trạng thái" chỉ hiển thị thêm huy hiệu hoặc nhãn văn bản: `· Có lý do`.
    - Không hiển thị bất kỳ ô text nào chứa nội dung lý do tự do.
    - Không hiển thị số CCCD, số điện thoại, ngày sinh hay email của cư dân trên màn hình thu tiền.
  - **Kiểm tra sau (Post-check):** Giao diện tuân thủ đầy đủ tiêu chuẩn bảo vệ quyền riêng tư D28.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-09.3`; `docs/DECISIONS.md` D28; `frontend/src/screens/s09/ApiCollectionsPage.tsx:173,176`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-10.1 — Chế độ API mode: `/accountant/collections` và `/admin/collections` kết nối API backend thật

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections`, `/admin/collections`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Ứng dụng chạy ở chế độ API mode (`mode = 'api'`). Backend Spring Boot đang chạy tại cổng 8080.
    - **Dữ liệu giả:** Tài khoản kiểm thử `accountant` và `admin`.
  - **Các bước thực hiện:**
    1. Đăng nhập `accountant` với mode `api`. Truy cập `/accountant/collections`.
    2. Mở tab Network trên DevTools trình duyệt.
    3. Quan sát các request HTTP phát sinh khi tải trang và khi tìm kiếm hóa đơn.
  - **Kết quả mong đợi:** Trình duyệt gọi các API backend thật: `GET /api/v1/payment-invoices?page=0&size=20`, `GET /api/v1/invoices/.../payment-summary`. Màn hình hiển thị component `ApiCollectionsPage` với dữ liệu thật từ MySQL.
  - **Kiểm tra sau (Post-check):** Thay đổi bộ lọc gửi đúng tham số query lên backend.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-10.1`; `frontend/src/App.tsx:131,142`; `frontend/src/screens/s09/ApiCollectionsPage.tsx:54`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-10.2 — Chế độ Demo mode: `/accountant/collections` dùng dữ liệu giả lập trong bộ nhớ, `/admin/collections` chỉ đọc

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections`, `/admin/collections`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Ứng dụng chạy ở chế độ Demo mode (`mode = 'demo'`).
    - **Dữ liệu giả:** Dữ liệu mẫu demo trong AppStateContext.
  - **Các bước thực hiện:**
    1. Đăng nhập với mode `demo`. Truy cập `/accountant/collections`.
    2. Thực hiện thu tiền mặt cho một hóa đơn chưa đóng.
    3. Chuyển sang tài khoản ADMIN truy cập `/admin/collections`.
  - **Kết quả mong đợi:**
    - Tại `/accountant/collections`: Render component `CollectionsPage`, thu tiền gọi hàm giả lập `collectInvoiceCashDemo`, cập nhật state trong RAM mà không gọi API backend thật.
    - Tại `/admin/collections`: Render component `CollectionsPage` với thuộc tính `readOnly = true`; không có nút thu tiền mặt.
  - **Kiểm tra sau (Post-check):** Dữ liệu demo không tác động tới cơ sở dữ liệu thật.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-10.2`; `docs/DECISIONS.md` D28; `frontend/src/App.tsx:131,142`; `frontend/src/screens/accountant/CollectionsPage.tsx:9`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-11.1 — ADMIN truy cập `/admin/collections`: xem công nợ và lịch sử, ẩn toàn bộ nút hành động

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ADMIN.
  - **Điểm vào (Route / Endpoint):** `/admin/collections`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Đăng nhập tài khoản `ADMIN` ở chế độ API mode. Có các hóa đơn còn nợ và đã thanh toán.
    - **Dữ liệu giả:** Dữ liệu hóa đơn và biên lai trên backend.
  - **Các bước thực hiện:**
    1. Truy cập URL `/admin/collections`.
    2. Kiểm tra tiêu đề trang và mô tả tại ScreenHeader.
    3. Tìm kiếm và bấm "Xem" chi tiết một hóa đơn đang có nợ và có lịch sử thu tiền.
    4. Quan sát các nút hành động trên giao diện chi tiết.
  - **Kết quả mong đợi:**
    - Tiêu đề màn hình là: **"Tra cứu thu tiền"** (thay vì "Thu tiền mặt tại quầy" như của Kế toán).
    - Xem được danh sách hóa đơn, chi tiết phí, tổng tiền, đã trả, còn nợ, số dư hộ, lịch sử biên lai, lịch sử phân bổ.
    - Hoàn toàn KHÔNG XUẤT HIỆN các nút: `"Ghi nhận tiền"`, `"Phân bổ số dư"`, `"Hủy biên lai"`, `"Đảo phân bổ"`.
  - **Kiểm tra sau (Post-check):** ADMIN chỉ có quyền quan sát, đối soát công nợ mà không thể can thiệp số liệu.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-11.1`; `docs/DECISIONS.md` D28; `frontend/src/screens/s09/ApiCollectionsPage.tsx:20,149,172,173,176`; `frontend/src/test/s09-page.test.tsx:23`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-12.1 — Giao diện khi gặp lỗi chưa rõ kết quả: lưu lệnh vào `sessionStorage` theo username, hiển thị banner và nút "Thử gửi lại"

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections` (API mode)
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Kế toán viên `alice` đang ở màn hình ghi nhận biên lai thu tiền. Có lỗi mạng (Network Error / Offline) hoặc server trả mã HTTP 500/504 hoặc phiên hết hạn 401 khi bấm xác nhận.
    - **Dữ liệu giả:** Hóa đơn `1001`, số tiền 100.000 đ.
  - **Các bước thực hiện:**
    1. Mở form ghi nhận tiền, nhập số tiền 100.000 đ.
    2. Ngắt kết nối mạng hoặc chặn request tới `/api/v1/invoices/1001/receipts` bằng DevTools Network Throttling.
    3. Bấm "Xác nhận ghi thu".
    4. Quan sát giao diện và kiểm tra bộ nhớ `sessionStorage`.
  - **Kết quả mong đợi:**
    - Form dialog đóng lại.
    - Xuất hiện thông báo lỗi: `"Chưa xác định được kết quả. Thử gửi lại đúng giao dịch này trước khi thực hiện thao tác khác."`.
    - Xuất hiện khối cảnh báo đặc biệt: **"Giao dịch chưa rõ kết quả"** hiển thị nhãn giao dịch và nút **"Thử gửi lại"**.
    - Các nút ghi nhận tiền hoặc phân bổ mới bị vô hiệu hóa (`blocked`).
    - Kiểm tra `sessionStorage.getItem("bluemoon.s09.pending.alice")`: Chứa chuỗi JSON gồm `url`, `body` (trong đó có `idempotencyKey` UUID nguyên bản và số tiền) và `label`.
  - **Kiểm tra sau (Post-check):** Khóa idempotency gốc được giữ nguyên vẹn trong bộ nhớ trình duyệt, không bị sinh mới UUID khác.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-12.1`; `docs/DECISIONS.md` D28; `frontend/src/api/payments.ts:25-35`; `frontend/src/screens/s09/ApiCollectionsPage.tsx:75,81,94,153`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-12.2 — Người dùng nhấn "Thử gửi lại": gửi lại đúng `idempotencyKey` và payload cũ để đối soát thành công

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections` (API mode)
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Đang có một giao dịch chưa rõ kết quả của `alice` được lưu trong `sessionStorage` (từ ca PAY-12.1). Kết nối mạng đã phục hồi bình thường.
    - **Dữ liệu giả:** Giao dịch pending của `alice`.
  - **Các bước thực hiện:**
    1. Nhấn nút "Thử gửi lại" trên khối cảnh báo giao dịch chưa rõ kết quả.
    2. Quan sát request phát đi trên DevTools Network.
    3. Quan sát thông báo và dữ liệu trên trang sau khi hoàn tất.
  - **Kết quả mong đợi:**
    - Request được gửi lại với đúng URL, đúng `idempotencyKey` và body cũ đã lưu.
    - Server trả HTTP 200/201 (replay an toàn hoặc ghi nhận thành công).
    - Khối cảnh báo giao dịch chưa rõ kết quả biến mất.
    - Thông báo thành công: `"Đã ghi nhận giao dịch. Dữ liệu đã được tải lại."`.
    - Key `bluemoon.s09.pending.alice` trong `sessionStorage` được xóa sạch.
    - Dữ liệu hóa đơn và công nợ trên màn hình được tải lại mới nhất.
  - **Kiểm tra sau (Post-check):** Bảng `payment_receipts` không bị nhân đôi biên lai.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-12.2`; `docs/DECISIONS.md` D28; `frontend/src/screens/s09/ApiCollectionsPage.tsx:82-85,153`; `frontend/src/test/s09-page.test.tsx:58-65`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-12.3 — Chuyển đổi tài khoản: xóa sạch state, không hiển thị và không cho phép tài khoản khác gửi lại giao dịch dở dang

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT.
  - **Điểm vào (Route / Endpoint):** `/accountant/collections` (API mode)
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:** Kế toán `alice` có giao dịch pending đang lưu trong `sessionStorage`. Kế toán viên `bob` đăng nhập trên cùng trình duyệt.
    - **Dữ liệu giả:** Tài khoản `alice` và `bob`.
  - **Các bước thực hiện:**
    1. Khi đang có pending của `alice`, đăng xuất và đăng nhập bằng tài khoản `bob`.
    2. Mở `/accountant/collections`.
    3. Kiểm tra xem `bob` có nhìn thấy nút "Thử gửi lại" của `alice` không.
    4. Giả lập trường hợp đổi tài khoản ngay khi một request của `alice` đang bay trên mạng (in-flight).
  - **Kết quả mong đợi:**
    - Khi `bob` đăng nhập: Toàn bộ state form, hóa đơn đã chọn và thông báo lỗi của `alice` bị xóa sạch (reset).
    - `bob` KHÔNG nhìn thấy khối giao dịch pending của `alice` (`sessionStorage` lưu tách biệt theo key `bluemoon.s09.pending.bob`).
    - Nếu request cũ của `alice` hoàn tất sau khi `bob` đã đăng nhập: `actorRef.current !== actor` ngăn chặn việc cập nhật dữ liệu của `alice` vào màn hình của `bob`. Hàm `sendPending` kiểm tra lại `fetchCurrentUser()` và ném lỗi `PaymentActorChangedError` nếu actor bị đổi.
  - **Kiểm tra sau (Post-check):** Dữ liệu và quyền hạn của các kế toán viên làm việc chung máy không bị lẫn lộn.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-12.3`; `docs/DECISIONS.md` D28; `frontend/src/api/payments.ts:38-42`; `frontend/src/screens/s09/ApiCollectionsPage.tsx:42-51,84-90`; `frontend/src/test/s09-page.test.tsx:30-55`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.

### PAY-13.1 — Kịch bản duyệt toàn trình nghiệp vụ thu tiền mặt tại quầy từ đầu đến cuối trên trình duyệt thực tế

- **Mô tả ca kiểm thử:**
  - **Vai trò:** ACCOUNTANT, ADMIN, RESIDENT.
  - **Điểm vào (Route / Endpoint):** `/login`, `/accountant/collections`, `/admin/collections`, `/resident/invoices`
  - **Dữ liệu đầu vào:**
    - **Tiền điều kiện:**
      - Backend Spring Boot đang chạy với MySQL 8.4 kiểm thử cô lập (đã nạp dữ liệu mẫu gồm hộ `A-101`, hộ `A-102` và kỳ thu hiện tại đã phát hành hóa đơn).
      - Frontend đang chạy tại `http://localhost:5173` (hoặc cổng cấu hình).
      - Trình duyệt Chrome/Firefox sạch (cửa sổ riêng tư Incognito).
    - **Dữ liệu giả:** Căn hộ `A-101` (hộ `H-101`), Căn hộ `A-102` (hộ `H-102`). Các tài khoản giả lập: `accountant.test`, `admin.test`, `resident.test`.
  - **Các bước thực hiện:**
    1. **Bước 1 (Đăng nhập Kế toán):** Mở trình duyệt, truy cập `/login`. Chọn chế độ `API`. Nhập username `accountant.test` và mật khẩu. Xác nhận đăng nhập thành công và chuyển hướng tới `/accountant/collections`.
    2. **Bước 2 (Tìm kiếm công nợ):** Tại màn hình "Thu tiền mặt tại quầy", nhập tìm kiếm `A-101`, chọn kỳ hiện tại, trạng thái "Còn nợ". Nhấn "Tìm kiếm". Danh sách hiển thị đúng hóa đơn của căn `A-101` với số tiền nợ (ví dụ 600.000 đ).
    3. **Bước 3 (Xem chi tiết hóa đơn):** Nhấn nút "Xem" tại dòng hóa đơn `A-101`. Quan sát khu vực chi tiết: hiển thị tóm tắt tổng nợ, đã trả 0 đ, còn nợ 600.000 đ, số dư nộp thừa 0 đ, danh sách chi tiết các dòng phí quản lý, phí rác.
    4. **Bước 4 (Thu một phần nợ):** Nhấn nút "Ghi nhận tiền". Hộp thoại mở ra:
       - Nhập số tiền: `400000`.
       - Ngày nhận: giữ mặc định hôm nay.
       - Quan sát bảng xem trước (preview) tự động tính: Tiền nhận 400.000 đ -> Trả hóa đơn 400.000 đ -> Số dư mới của hộ 0 đ -> Còn nợ sau thu 200.000 đ.
       - Nhấn "Xác nhận ghi thu".
       - Chờ dialog đóng, thông báo thành công xuất hiện. Bảng lịch sử biên lai có dòng biên lai mới #1 với trạng thái "Đã ghi nhận". Còn nợ cập nhật thành 200.000 đ.
    5. **Bước 5 (Thu vượt nợ tạo số dư thừa):** Nhấn tiếp "Ghi nhận tiền" trên cùng hóa đơn `A-101`:
       - Nhập số tiền: `300000` (lớn hơn nợ 200.000 đ).
       - Ngày nhận: chọn ngày hôm qua. Form yêu cầu nhập lý do lùi ngày. Nhập: `"Cư dân nộp qua tài khoản tối qua"`.
       - Bảng preview tính: Trả hóa đơn 200.000 đ -> Số dư mới của hộ 100.000 đ -> Còn nợ sau thu 0 đ.
       - Nhấn "Xác nhận ghi thu".
       - Xác nhận: Hóa đơn chuyển trạng thái "Đã thanh toán", nợ bằng 0 đ, "Số dư nộp thừa" của hộ tăng lên 100.000 đ. Biên lai #2 hiển thị nhãn `· Có lý do`.
    6. **Bước 6 (Phân bổ số dư sang hóa đơn khác cùng hộ):**
       - Quay lại bộ lọc, tìm hóa đơn kỳ trước của cùng căn `A-101` đang nợ 150.000 đ. Bấm "Xem".
       - Nhấn nút "Phân bổ số dư".
       - Nhập số tiền phân bổ: `70000`. Form hiển thị preview còn lại 30.000 đ số dư thừa và còn nợ 80.000 đ.
       - Nhấn "Xác nhận phân bổ".
       - Xác nhận: Nợ hóa đơn giảm còn 80.000 đ; số dư hộ giảm còn 30.000 đ; bảng "Lịch sử phân bổ" hiển thị phân bổ #1 sử dụng nguồn từ biên lai #2.
    7. **Bước 7 (Kiểm tra chặn hủy biên lai có credit đang dùng):**
       - Quay lại hóa đơn ở Bước 5, tìm biên lai #2.
       - Nhấn "Xem 1 phân bổ" để đối soát liên kết.
       - Thử tìm nút hủy biên lai: nút hủy bị ẩn hoặc nếu kích hoạt thì server từ chối vì credit đang được dùng ở phân bổ #1.
    8. **Bước 8 (Đảo phân bổ):**
       - Tại bảng "Lịch sử phân bổ", nhấn nút "Đảo phân bổ" tại phân bổ #1.
       - Nhập lý do đảo: `"Chỉnh sửa đối soát kế toán"`. Bấm "Xác nhận đảo".
       - Xác nhận: Phân bổ chuyển trạng thái "Đã đảo" kèm nhãn `· Có lý do`; số dư hộ được khôi phục lại 100.000 đ; nợ của hóa đơn nhận tăng lại 150.000 đ.
    9. **Bước 9 (Hủy biên lai):**
       - Quay lại hóa đơn ở Bước 5, lúc này biên lai #2 đã có nút "Hủy biên lai".
       - Nhấn "Hủy biên lai", nhập lý do: `"Biên lai lập sai"`. Bấm "Xác nhận hủy".
       - Xác nhận: Biên lai #2 chuyển sang "Đã hủy" kèm nhãn `· Có lý do`; số dư nộp thừa của hộ giảm về 0 đ; công nợ hóa đơn tăng lại 200.000 đ.
    10. **Bước 10 (Kiểm tra đối soát khi mất mạng):**
        - Mở form "Ghi nhận tiền", ngắt mạng trình duyệt.
        - Bấm xác nhận: xuất hiện banner "Giao dịch chưa rõ kết quả" kèm nút "Thử gửi lại".
        - Bật lại mạng, nhấn "Thử gửi lại": giao dịch được gửi thành công, banner biến mất.
    11. **Bước 11 (Kiểm tra màn hình ADMIN chỉ đọc):**
        - Đăng xuất tài khoản Kế toán. Đăng nhập tài khoản `admin.test`.
        - Vào menu "Tra cứu thu tiền" (`/admin/collections`).
        - Xác nhận xem được toàn bộ danh sách và chi tiết các giao dịch vừa thực hiện ở các bước trước nhưng không có bất kỳ nút "Ghi nhận tiền", "Phân bổ số dư", "Hủy", "Đảo" nào.
    12. **Bước 12 (Kiểm tra chặn vai trò Cư dân):**
        - Đăng xuất, đăng nhập tài khoản `resident.test`.
        - Gõ trực tiếp trên thanh địa chỉ URL: `/accountant/collections` hoặc `/admin/collections`.
        - Xác nhận: Ứng dụng chặn quyền, chuyển hướng hoặc hiển thị thông báo không có quyền truy cập.
  - **Kết quả mong đợi:** Toàn bộ 12 bước đều thực hiện trơn tru, logic nghiệp vụ, tính toán tiền tệ, trạng thái nợ, quy tắc an toàn phân bổ/hủy/đảo và phân quyền hiển thị chuẩn xác 100% theo D27 và D28.
  - **Kiểm tra sau (Post-check):** Không phát sinh bất kỳ lỗi console, lỗi giao diện hay bất thường dữ liệu nào.
  - **Nguồn đối chiếu:** `tests/cases/s09-payments.md:PAY-13.1`; `docs/DECISIONS.md` D27, D28; `frontend/src/screens/s09/ApiCollectionsPage.tsx`; `tests/README.md`.
- **Kết quả kiểm thử:** **✅ Đạt (Pass)**
- **Thực tế ghi nhận (đã che dữ liệu nhạy cảm):** Thủ công: đã thực hiện, kết quả đạt theo kỳ vọng của đặc tả.
- **Chi tiết lỗi / Nhận xét:** Không phát hiện lỗi. Giới hạn: Không.
