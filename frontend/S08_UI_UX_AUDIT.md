# Audit và chỉnh UI/UX S08 — 09/11/2026

Phạm vi: Đóng góp tự nguyện cho ADMIN/ACCOUNTANT/RESIDENT; rà và chỉnh các điểm liên quan trong Kỳ thu. Nền triển khai: `f081bee87c98d80144e1d106b2cde45e54504594`; bản cập nhật UI/UX được bàn giao trên nhánh `hhoangsonnw/feat-s08-ui-ux`.

Tham chiếu thiết kế: `ui-audit` tại `68e4199e4ecf91163e2903cee42aceb9c1fdfc71`, hai màn `VoluntaryAdminPage` và `ResidentVoluntaryPage`; đã xem màn tham chiếu đang chạy trong Arc. Hợp đồng D26/S08 và API hiện tại quyết định nghiệp vụ, không lấy nghiệp vụ mock làm quy tắc mới.

## Các vấn đề đã sửa

| Mức | Vấn đề trước sửa | Hành vi sau sửa | Vị trí |
|---|---|---|---|
| P2 | Danh sách đợt chỉ là các đoạn chữ; nhân sự phải mở chi tiết để thấy tổng thu. | Mỗi đợt là thẻ có tên, trạng thái, mô tả, lịch, tổng thu và số lượt; tổng và số lượt chỉ dành cho nhân sự. | `src/screens/s08/CampaignCard.tsx` |
| P2 | Tạo/sửa đợt chen vào trang; chưa giải thích bước lưu nháp và công bố. | Nút tạo trong header mở hộp thoại có nhãn, kiểm tra ngày, chặn gửi lặp; lưu nháp rồi mở chi tiết để công bố riêng với xác nhận. | `src/screens/s08/ApiVoluntaryPage.tsx` |
| P1 | Hộp hủy không nêu rõ người đóng, số tiền và ngày của khoản bị hủy. | Tên người đóng trong tiêu đề; số tiền/hộ/ngày và hậu quả trong nội dung; lý do bắt buộc, nút nguy hiểm riêng. | `src/screens/s08/ApiVoluntaryPage.tsx` |
| P2 | Sổ chưa có bảng, trạng thái tải riêng hoặc lỗi/thử lại riêng. | Sổ dùng bảng có caption/nhãn cột; phân biệt đang tải, chưa có khoản và tải lỗi; có retry, trạng thái chữ và lý do hủy. | `src/screens/s08/ApiVoluntaryPage.tsx` |
| P2 | Các vùng phân trang có cùng nhãn, kể cả khi chỉ có một trang. | Ẩn phân trang không cần thiết; nhãn riêng cho đợt/người đóng/sổ/kỳ thu/bản trình/hóa đơn. | `src/screens/s08/Pager.tsx` |

## Các luồng đã cải thiện

1. **ADMIN:** tạo nháp → kiểm tra chi tiết → công bố qua xác nhận → theo dõi tổng/sổ → đóng đợt. Lưu nháp không tự công bố; không hiện thao tác ghi/hủy khoản của kế toán.
2. **ACCOUNTANT:** chọn đợt → tìm tên/đối chiếu hộ/căn hộ → nhập khoản thực thu. Đợt nháp có lời giải thích thay cho form ghi tiền; đợt kết thúc có hướng dẫn và lý do nhập muộn bắt buộc. Giữ nguyên pending payload/key khi mất phản hồi và qua điều hướng, tách theo account; kiểm tra hiện có vẫn qua.
3. **RESIDENT:** thẻ chỉ có thông tin công bố, ngày và trạng thái, không tổng thu, số lượt, donor hoặc sổ. Không thêm đăng ký/nộp online.
4. **Kỳ thu:** tách biểu mẫu/danh sách/chi tiết; nhãn trạng thái và hạn nộp rõ; dữ liệu hộ/phí có cấu trúc; phân biệt nút chính và nút tra cứu. Khi đang xem bản trình cũ, không hiện Trình duyệt cho dữ liệu tính hiện tại; hộp hủy dùng tổng của kỳ đã phát hành thay vì tổng của bản lịch sử đang xem.

## Thiết kế và ranh giới dữ liệu

Giữ thẻ đợt vận động, tổng thu bên phải và lịch/số lượt phía dưới từ `ui-audit`. Dùng `ScreenHeader.actions` để tránh cách bọc flex làm tiêu đề bị bó hẹp. CSS riêng S08 cho khung nội dung dùng toàn bộ chiều rộng thay cho `place-content: center`, dùng token sẵn có, đổi sang một cột ở màn hẹp; sổ dùng quy tắc bảng responsive hiện có. Không thêm thư viện, font, màu hoặc hiệu ứng trang trí.

Tiền vẫn là chuỗi và định dạng không qua Number. Ghi qua API client có CSRF; account/role/ownership vẫn do server kiểm tra. Không đổi endpoint/schema/backend; không đưa dữ liệu mock vào mode API hoặc dữ liệu người đóng vào browser storage.

## Bằng chứng kiểm tra

| Kiểm tra | Kết quả |
|---|---|
| `cd frontend && npm test` | 20 file, **109/109 test qua**; 3,17 giây ở lần chạy cuối |
| `cd frontend && npm run build` | TypeScript và Vite qua; JS 507,77 kB / gzip 141,66 kB; Vite báo cảnh báo chunk >500 kB |
| Impeccable source detector cho `src/screens/s08` và `src/api/errors.ts` | `[]`, không có finding tĩnh |
| `git diff --check` | Qua |
| Bổ sung hồi quy UX | 5 test thẻ/hộp thoại/phân quyền/sổ tải; 2 test trình kỳ thu/hủy khi đang xem lịch sử |

Log: `../.agent_out/s08-ux-tests.log`, `s08-ux-build.log`, `s08-ui-detect.json`. Test vẫn có cảnh báo React `act` tại FeeRatesPage/AppStateProvider ngoài phạm vi lần sửa này; không có test thất bại.

## Giới hạn kiểm chứng

Đã kiểm tra mã, tương tác qua Testing Library và màn thiết kế tham chiếu. Người dùng đã mở bản `:5174`, nhưng CUA tiếp tục trả cửa sổ Arc khác; chưa quan sát được bản sau sửa qua browser. Không chấm điểm WCAG, tương phản hay responsive bằng mắt khi chưa có bằng chứng render.

Cần kiểm tay bản sau sửa ở 360px và desktop, sáng/tối, zoom 200%, focus/Tab/Esc trong hộp thoại, và thao tác API thật của ba vai trò. Test mô phỏng API không thay cho các kiểm tra đó. Backend/MySQL không được sửa và không chạy lại Maven/IT trong lượt UI này.

Preview cục bộ: `http://localhost:5174/admin/voluntary`; tài khoản kế toán dùng `/accountant/voluntary`, cư dân dùng `/resident/voluntary`.
