# Audit UI/UX toàn frontend — 09/11/2026

Ứng dụng vận hành chung cư, bốn vai trò: Ban quản trị, Kế toán, Cư dân, Cơ quan chức năng. Áp dụng Taste 2.5.0 để chỉnh hệ thống giao diện đang có, giữ thiết kế S08 đã đối chiếu với `ui-audit`. Nền: `c3b3f7fd539a3460f627bd812f9e050b1e2ffdce`; nhánh làm việc: `hhoangsonnw/feat-frontend-taste`.

## Phát hiện và cách sửa

| Vấn đề | Kết quả sau sửa | Vị trí chính |
|---|---|---|
| Một số màn bị co thành cột nhỏ, đặt giữa khoảng trống; header/nút chính dùng bố cục khác nhau | Trang dùng toàn bộ cột nội dung; nút chính đi cùng header, xuống hàng trên điện thoại | `app.css`, `ScreenHeader.actions`, báo cáo, đóng góp Demo, đơn giá |
| Form xác nhận tự dựng không có focus/Escape thống nhất; hai hộp báo cáo có thể chồng nhau; danh sách đóng góp rỗng không mở được form tạo | Dùng native dialog chung, khôi phục focus, chỉ mở một bước; quay lại giữ bản nháp; phần thân dài cuộn riêng và giữ footer | `FormDialog`, `ReportPackagesPage`, `VoluntaryAdminPage`, `CollectionsPage` |
| Danh sách tạm vắng lỗi mạng trông như đang tải mãi | Tách đang tải/lỗi/rỗng; hiện thông báo và nút thử lại; ẩn phân trang dữ liệu cũ khi tải/lỗi | Hai màn `ApiAbsence*` |
| Menu điện thoại mở nhưng bàn phím không đi tới mục; nhiều phân trang không phân biệt được | Menu nhận focus, hỗ trợ mũi tên/Home/End/Escape; điều hướng chuyển focus tới nội dung; phân trang có tên riêng và thông báo số trang | `AppShell`, `PaginationBar` |
| Bảng chuyển thành thẻ ở vùng hẹp có cột nhãn cứng và nhãn trạng thái bị kéo dài | Cột co được; hành động gom nhóm; badge giữ kích thước nội dung; lọc/nút không ép trang tràn ngang | `app.css`, `AuthorityReportsPage`, `ApiAbsenceApprovalsPage` |
| Demo đơn giá vẫn gọi API thật; Demo kỳ thu cung cấp thao tác phát hành trực tiếp sai vai trò D26 | Đơn giá Demo không gọi máy chủ; kỳ thu Demo chỉ tra cứu và giải thích Kế toán lập/trình, BQT duyệt | `FeeRatesPage`, `BillingPeriodsPage` |
| Ba nút tải báo cáo mô phỏng chờ rồi báo thành công dù không có tệp | Khóa tải chưa khả dụng, giải thích ngay cạnh; vẫn lọc và xem trước được | Báo cáo lưu trú, tỷ lệ thu, báo cáo cơ quan |
| Tên trường kỹ thuật, trạng thái tiếng Anh, ngày thô, mô tả nội bộ lẫn vào thao tác | Nhãn tiếng Việt, ngày theo helper chung, nhãn biểu mẫu nối đúng điều khiển, hướng dẫn theo tác vụ | `reportFields`, các màn báo cáo, tài khoản, hộ gia đình |
| Nền login chuyển động liên tục không có nút dừng | Có dừng/bật riêng cho nền, tôn trọng reduced-motion; nút đặt cùng nhóm sáng/tối. Bỏ dòng “Quản lý cư trú và thu phí” theo yêu cầu; “Đăng nhập” là h1 | `LoginPage`, `app.css` |

Không đổi backend, migration, phân quyền máy chủ, hợp đồng API, khóa idempotency hoặc quy tắc tiền. Không thêm API xuất báo cáo. D26 và D25 vẫn là nguồn quy tắc; các ghi chú nghiệp vụ chưa chốt không được coi là quyết định mới.

## Cách kiểm chứng

Mọi trình duyệt kiểm tra chạy bằng script Taste nguyên bản, riêng một Chrome headless tại một thời điểm. Bản kiểm tra là bản sao frontend trong `.agent_out/taste-global/app`, phục vụ tại `127.0.0.1:5185`. Chỉ bản sao có fixture và chọn vai trò qua query. Mọi API ghi ở bản sao bị trả lỗi, GET dùng dữ liệu mẫu có sẵn; không gọi backend thật, không thao tác dữ liệu cư dân thật. Các query audit không được đưa vào mã sản phẩm.

Trước sửa có 37 ảnh toàn trang ở 1440px. Manifest sau sửa gồm 37 màn/trạng thái và 4 form/hộp thoại, tại 1440×900, 1024×768, 820×1180, 768×1024, 390×844. Ma trận đã chạy và giới hạn cuối cùng được ghi ở phần kết quả bên dưới.

Lệnh chính:

```sh
cd frontend
npm test
npm run build
# Từ gốc repository
git diff --check
node /Users/hoangson/.claude/skills/taste-skill/scripts/consistency.mjs URL --widths 1440,1024,820,768,390 --rhythm-widths 1440,1024,820,768,390 --page software --profile PROFILE --out OUT --stops --quick --no-sweep --json
```

`--quick` kiểm bố cục, nội dung, tương phản và ảnh qua các viewport, lưu ảnh từng đoạn cuộn. Nó bỏ qua kiểm form, motion, hover, reduced-motion và nội dung ẩn; không suy các mục này là đạt. Vòng đầy đủ riêng cho danh sách hộ đã chạy cả 5 viewport, không dùng `--quick`.

`assets/layout/check.mjs` mặc định stress đã được thử: công cụ báo thiếu `.wrap`, rồi lỗi `Cannot read properties of undefined (reading 'map')` ở nhánh lưu ảnh vì kết quả không có `sections`. Không sửa toolkit, không sửa ứng dụng thành trang marketing để né lỗi, không coi lượt đó là đạt. Kiểm headline stress của bộ này chưa áp dụng được.

## Kết quả và giới hạn

Người dùng yêu cầu tạm chốt và đẩy GitHub khi ma trận đang chạy. Dừng mở thêm màn; không tuyên bố đã hoàn tất toàn bộ 41 cấu hình.

| Kiểm tra | Kết quả |
|---|---|
| `npm test` | **24 file, 119/119 test qua**, 5,81 giây; 10 hồi quy mới so với nền 109 test |
| `npm run build` | TypeScript và Vite qua; JS 499,79 kB, gzip 140,88 kB |
| `git diff --check` | Qua |
| Taste `perf.mjs --runs 3`, đơn giá 1440/390 | **0 FAIL, 4 WARN**: bundle JS 494,4 KiB và 6 tệp font ở mỗi viewport; không request hỏng, không tràn ngang. Chỉ là đo CPU headless với fixture, không đại diện độ trễ backend |
| Taste consistency, `--quick`, đủ 5 viewport | **12 cấu hình / 60 lượt viewport**: Login, hồ sơ, đổi mật khẩu, tài khoản, tạo tài khoản, danh sách hộ, chi tiết hộ, tạo/sửa hộ, thêm nhân khẩu, chuyển đi, duyệt tạm vắng Demo. Không probe error; checker vẫn exit 1 theo các luật marketing nêu dưới đây |
| Taste ảnh bổ sung | Đơn giá và đóng góp API tại 1440/390; hai form báo cáo/đóng góp Demo tại 390. Kiểm riêng ảnh viewport của modal vì ảnh ghép toàn trang che fixed dialog từ đoạn cuộn thứ hai |

Log/ảnh cục bộ ở `.agent_out/taste-global/` (ignored): `checks/*/report.json`, `checks-review.json`, `tests-final.log`, `build-final.log`, `final/` và `modal-checks/`. Login được kiểm lại 5 viewport sau khi bỏ dòng tiêu đề theo yêu cầu cuối. Ảnh viewport 390×844 xác nhận hai modal có đầy đủ trường và footer: đáy footer báo cáo 804px, đóng góp 725px; phần ảnh ghép toàn trang của `shoot.mjs` không dùng để đánh giá dialog vì script ẩn thành phần fixed ở đoạn cuộn tiếp theo. Không coi 29 cấu hình còn lại là đã kiểm ảnh; các màn đó mới được đọc mã và/hoặc có ảnh nền trước sửa.

Các luật Taste dành cho landing page như hero chiếm đúng một màn, mỗi section nguyên một/hai màn, bắt buộc ảnh/CTA ở cuối trang, lề 64–120px, một kiểu nút duy nhất không thích hợp với ứng dụng có sidebar, bảng, form và nút chính/phụ. Giữ finding thô để đối chiếu; không đổi bảng dữ liệu thành bố cục marketing và không tuyên bố toàn bộ checker PASS.

Không kiểm chứng luồng ghi thật, phiên hết hạn qua cookie thật, race/idempotency trên server, screen reader, zoom 200%, iOS Safari hoặc thiết bị thật. Test jsdom/ảnh headless không phải chứng nhận WCAG. Các màn báo cáo/S09/S10 còn là prototype; tải báo cáo thật chưa có. Không chạy Maven/MySQL trong lượt frontend này. Thay đổi được bổ sung vào PR #25 theo yêu cầu đẩy GitHub; giữ nguyên nhánh đích `main`.
