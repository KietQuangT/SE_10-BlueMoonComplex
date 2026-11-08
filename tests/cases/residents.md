# Ca kiểm thử RES

Tiền tố `/api/v1` áp dụng cho endpoint API viết rút gọn; thời gian nghiệp vụ dùng Asia/Ho_Chi_Minh.

## RES-01 — Thêm nhân khẩu vào hộ.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **RES-01.1:** ADMIN, `POST /households/{id}/residents`, màn `/admin/households/:id/residents/new`. Trạng thái cư trú: `RESIDING`, `TEMPORARILY_ABSENT`, `MOVED_OUT`.
- **RES-01.2:** Trùng số giấy tờ (`identity_number`) trong nhóm `RESIDING` và `TEMPORARILY_ABSENT` trên toàn hệ thống trả `409`, lỗi không chứa số giấy tờ.
- **RES-01.3:** Ngày sinh không ở tương lai.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## RES-02 — Danh sách nhân khẩu của hộ.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **RES-02.1:** ADMIN gọi `GET /households/{id}/residents` với `status` và phân trang: kết quả chỉ chứa nhân khẩu của hộ có trạng thái đã chọn.
- **RES-02.2:** Quyền xem số giấy tờ và số điện thoại chỉ có ADMIN; cư dân không thấy.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## RES-03 — Xem và sửa nhân khẩu.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **RES-03.1:** ADMIN xem một nhân khẩu đã tạo bằng `GET /residents/{id}`, sửa trường hợp lệ bằng `PATCH`, rồi GET lại: trường sửa có giá trị mới.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## RES-04 — Chuyển đi.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **RES-04.1:** ADMIN, `POST /residents/{id}/move-out`, màn `/admin/move-outs`. Trạng thái sang `MOVED_OUT`; không xóa bản ghi; ghi lịch sử.
- **RES-04.2:** Không có ngày tương lai; lý do không bắt buộc; chuyển đi lần hai trả 409; không hoàn tác.
- **RES-04.3:** Chuyển đi không tự khóa tài khoản và không tự đóng hộ.
- **RES-04.4:** Mọi đơn tạm vắng đang chờ của người đó tự chuyển `CANCELLED`, mỗi đơn một audit `ABSENCE_CANCELLED`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## RES-05 — Lịch sử biến động.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **RES-05.1:** ADMIN gọi `GET /households/{id}/residency-changes` sau khi tạo biến động: danh sách phân trang chứa biến động của hộ đã chọn. Dòng `change_type` gồm `MOVE_OUT`, `TEMPORARY_ABSENCE`, `RETURN`.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## RES-06 — Thành viên hộ của cư dân.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **RES-06.1:** RESIDENT, `GET /households/me/residents`; chỉ thấy hộ của chính mình; không thấy số giấy tờ và số điện thoại.
- **RES-06.2:** Mở `/resident/household`: chỉ thấy thành viên hộ của tài khoản, không có số giấy tờ hoặc số điện thoại.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## RES-07 — Gợi ý tên nhân khẩu.

**Tiền điều kiện:** dữ liệu giả lập phù hợp; nếu cần xác thực, dùng vai trò được nêu trong ca và phiên đã đổi mật khẩu; request ghi có CSRF hợp lệ trừ khi đang kiểm CSRF.

**Thao tác:** thực hiện hành vi tại endpoint hoặc màn hình của ca; với mỗi nhánh tạo dữ liệu giả thỏa điều kiện được nêu, gửi request hoặc thao tác trên giao diện, rồi đối chiếu kết quả và trạng thái được mô tả. Mỗi nhánh thực hiện riêng.

**Điều kiện và kết quả mong đợi:**
- **RES-07.1:** ADMIN, `GET /residents/suggest?q=`: tối thiểu 2 ký tự (ngắn hơn trả `400 INVALID_FILTER`), tối đa 10 kết quả, chỉ nhân khẩu `RESIDING` hoặc `TEMPORARILY_ABSENT`, không trả CCCD hay SĐT.

**Ghi nhận:** trạng thái Pass/Fail/Blocked/Not run, thực tế và bằng chứng đã che dữ liệu nhạy cảm.

## Điểm chưa xác định

Các nhánh không nêu mã HTTP hoặc nội dung chính xác trong outline: ghi thực tế và đối chiếu mã nguồn trước khi kết luận; không tự suy đoán mã.
