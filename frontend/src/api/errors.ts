import { ApiHttpError } from './client';

const BY_CODE: Record<string, string> = {
  HOUSEHOLD_CONFLICT: 'Mã hộ hoặc mã căn hộ đã tồn tại. Kiểm tra lại rồi thử lần nữa.',
  HOUSEHOLD_NOT_FOUND: 'Không tìm thấy hộ này.',
  HOUSEHOLD_INVALID: 'Hộ không tồn tại hoặc không còn hoạt động.',
  RESIDENT_NOT_FOUND: 'Không tìm thấy nhân khẩu này.',
  RESIDENT_ALREADY_MOVED_OUT: 'Nhân khẩu này đã chuyển đi trước đó.',
  // AGENTS.md: the duplicate message must never contain the identity number.
  IDENTITY_NUMBER_ALREADY_USED:
    'Số giấy tờ định danh bị trùng với một nhân khẩu đang cư trú. Vui lòng đối chiếu hồ sơ gốc.',
  INVALID_DATE: 'Ngày không hợp lệ. Kiểm tra ngày bắt đầu và kết thúc rồi thử lại.',
  VALIDATION: 'Thông tin chưa hợp lệ. Kiểm tra các trường rồi thử lại.',
  INVALID_FILTER: 'Bộ lọc chưa hợp lệ. Kiểm tra lại rồi thử tìm kiếm.',
  RESIDENT_NOT_AVAILABLE: 'Nhân khẩu không còn đủ điều kiện. Tải lại danh sách rồi kiểm tra.',
  DECLARATION_ALREADY_PENDING: 'Nhân khẩu đã có đơn chờ duyệt. Kiểm tra danh sách đơn.',
  DECLARATION_NOT_PENDING: 'Đơn không còn chờ duyệt. Tải lại danh sách để xem trạng thái.',
  DECLARATION_NOT_FOUND: 'Không tìm thấy đơn. Tải lại danh sách rồi thử lại.',
  RESIDENT_NOT_ABSENT: 'Nhân khẩu không còn tạm vắng. Tải lại chi tiết hộ để kiểm tra.',
  EMPTY_UPDATE: 'Chưa có thay đổi nào để lưu.',
  ACCOUNT_NOT_FOUND: 'Không tìm thấy tài khoản này.',
  ACCOUNT_DISABLE_CONFLICT:
    'Không thể vô hiệu hóa: đây là chính bạn, là Ban quản trị cuối cùng, hoặc tài khoản đã bị vô hiệu.',
  ACCOUNT_ALREADY_ACTIVE: 'Tài khoản này đang hoạt động.',
  CURRENT_PASSWORD_INCORRECT: 'Mật khẩu hiện tại không đúng. Kiểm tra lại rồi thử lần nữa.',
  PASSWORD_TOO_SHORT: 'Mật khẩu mới phải có ít nhất 8 ký tự.',
  PASSWORD_TOO_LONG: 'Mật khẩu mới quá dài (tối đa 72 byte; chữ có dấu chiếm nhiều byte hơn).',
  PASSWORD_WEAK:
    'Mật khẩu mới phải có chữ hoa, chữ thường, chữ số và ký tự đặc biệt (kể cả khoảng trắng).',
  PASSWORD_SAME_AS_CURRENT: 'Mật khẩu mới phải khác mật khẩu hiện tại.',
  ACCOUNT_NOT_LOCKED: 'Tài khoản không còn dấu vết khóa để mở (hoặc là tài khoản Ban quản trị).',
};

/** One Vietnamese sentence for any failed call: what happened and what to do next. */
export function messageForError(err: unknown): string {
  if (!(err instanceof ApiHttpError)) return 'Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại.';
  const { status, code, requestId } = err.problem;
  if (status >= 500) return `Có lỗi xảy ra, vui lòng thử lại.${requestId ? ` Mã yêu cầu: ${requestId}.` : ''}`;
  if (code && BY_CODE[code]) return BY_CODE[code];
  if (status === 401) return 'Phiên đăng nhập đã hết hạn. Đăng nhập lại để tiếp tục.';
  if (status === 403) return 'Bạn không có quyền thực hiện thao tác này.';
  if (status === 404) return 'Không tìm thấy bản ghi.';
  if (status === 400) return 'Dữ liệu chưa hợp lệ. Kiểm tra lại từng ô.';
  if (status === 409) return 'Thông tin bị trùng hoặc xung đột với dữ liệu hiện có.';
  return `Có lỗi xảy ra, vui lòng thử lại.${requestId ? ` Mã yêu cầu: ${requestId}.` : ''}`;
}
