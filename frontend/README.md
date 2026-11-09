# Frontend BlueMoon

Ứng dụng React/TypeScript cho Ban quản trị, Kế toán và Cư dân. Công nghệ: React 19, TypeScript 5.7, Vite 6 và React Router 7; kiểm thử bằng Vitest, Testing Library và jsdom.

## Chạy và kiểm tra

Yêu cầu Node.js và npm:

~~~bash
cd frontend
npm ci
npm run dev
npm test
npm run build
~~~

Dev server chạy tại http://localhost:5173; /api và /actuator được proxy tới backend http://localhost:8080. npm run build chạy TypeScript trước Vite và ghi artifact vào dist/.

## Chế độ API và Demo

- **API mode:** dùng session cookie của backend (credentials: include) và CSRF token từ GET /api/v1/auth/csrf. Không lưu session token ở browser storage.
- **Demo mode:** tiếp tục dùng dữ liệu mẫu để xem các luồng chưa nối API. Nhãn/chức năng phát triển không phải quyền bảo mật; backend vẫn phải xác thực từng request.
- Cấu hình môi trường chạy thật không được bật cờ Demo.

## Các luồng đã nối

Frontend trong nhánh hiện tại gồm các màn xác thực/account, hộ/nhân khẩu, khai báo tạm vắng và các luồng thu phí đã nối theo task. Với S09:

- Kế toán thao tác tại /accountant/collections; API mode gọi API công nợ, receipt, credit allocation, void và reverse. Demo mode giữ trang demo.
- Admin tra cứu tại /admin/collections; route chỉ đọc.
- Danh sách mặc định hiện hóa đơn còn nợ ở mọi kỳ; có lọc hóa đơn đã thanh toán.
- Preview số tiền dùng số nguyên/BigInt. Các lệnh chưa rõ kết quả giữ UUID và payload trong sessionStorage theo username để retry cùng lệnh.
- Lý do nhập lùi ngày, hủy receipt và đảo phân bổ không hiển thị nguyên văn trong lịch sử; UI chỉ hiện cờ “Có lý do”.

Danh sách các phần chưa nối và khác biệt giữa API mode/Demo mode được ghi tại mã màn hình và [docs/VALIDATION.md](../docs/VALIDATION.md).

## Cấu trúc

~~~text
src/
  api/          API clients, CSRF và kiểu response
  components/   điều hướng, bảng, trạng thái tải/lỗi và hộp thoại
  screens/      màn hình theo vai trò/nghiệp vụ
  state/        trạng thái phiên và chế độ API/Demo
  styles/       token, chủ đề sáng/tối và bố cục
  test/         unit/component tests
~~~

Thiết kế và thành phần giao diện: [DESIGN.md](DESIGN.md), [PRODUCT.md](PRODUCT.md). API backend và quy tắc dữ liệu: [docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md), [docs/DECISIONS.md](../docs/DECISIONS.md).

## Kiểm thử S09 gần nhất

npm test -- --run: 28 file, 134 test pass. npm run build: TypeScript và Vite pass. Các test component dùng jsdom, không thay cho walkthrough trên browser thật; chưa xác nhận mobile, zoom 200%, keyboard/screen reader hoặc E2E qua backend.
