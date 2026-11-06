import React from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import { defaultHomeByRole } from '../../lib/format';
import { Icon } from '../../components/Icon';

type ErrorCode = 401 | 403 | 404 | 500;

const COPY: Record<ErrorCode, { title: string; body: string }> = {
  401: {
    title: 'Phiên đăng nhập đã hết hạn.',
    body: 'Bạn chưa đăng nhập hoặc đã không hoạt động quá 30 phút. Đăng nhập lại để tiếp tục; hệ thống sẽ đưa bạn về trang đang xem.',
  },
  403: {
    title: 'Bạn không có quyền truy cập trang này.',
    body: 'Tài khoản của bạn không được cấp quyền cho chức năng hoặc dữ liệu này. Nếu cho rằng đây là nhầm lẫn, hãy liên hệ Ban quản trị.',
  },
  404: {
    title: 'Không tìm thấy.',
    body: 'Trang hoặc bản ghi bạn tìm không tồn tại hoặc đã được chuyển đi.',
  },
  500: {
    title: 'Có lỗi xảy ra, vui lòng thử lại.',
    body: 'Máy chủ không xử lý được yêu cầu. Nếu lỗi lặp lại, gửi mã yêu cầu bên dưới cho Ban quản trị.',
  },
};

/** M-04 — shared frame for 401 / 403 / 404 / 500. */
export function ErrorPage({
  code,
  requestId,
}: {
  code: ErrorCode;
  requestId?: string;
}): React.ReactElement {
  const { user, triggerSessionExpired, setForcedUiState } = useAppState();
  const navigate = useNavigate();
  const copy = COPY[code];

  const primary = () => {
    setForcedUiState('normal');
    if (code === 401) {
      triggerSessionExpired(window.location.pathname);
      navigate('/login', { replace: true });
      return;
    }
    if (code === 403) {
      navigate(user ? defaultHomeByRole(user.role) : '/login');
      return;
    }
    if (code === 404) {
      navigate(-1);
      return;
    }
    window.location.reload();
  };

  const primaryLabel =
    code === 401
      ? 'Đăng nhập lại'
      : code === 403
        ? 'Về trang đầu của tôi'
        : code === 404
          ? 'Quay lại'
          : 'Thử lại';

  return (
    <section className="error-frame" role={code === 500 ? 'alert' : 'status'} aria-labelledby="error-title">
      <div className="error-code-plate tabular" aria-hidden="true">
        {code}
      </div>
      <div>
        <span className="badge badge-code">M-04</span>
        <h1 id="error-title" className="screen-title error-title">
          {copy.title}
        </h1>
        <p className="screen-desc">{copy.body}</p>
        {code === 403 ? (
          <p className="field-hint error-note">
            Lưu ý: khi một thao tác ghi bị 403, giao diện gọi lại <code>/auth/me</code> để phân biệt
            phiên hết hạn với thiếu quyền; nội dung biểu mẫu đang nhập được giữ lại.
          </p>
        ) : null}
        {code === 500 ? (
          <span className="badge badge-neutral tabular">Mã yêu cầu: {requestId || 'không có'}</span>
        ) : null}
        <div className="filter-row error-actions">
          <button type="button" className="btn btn-primary" onClick={primary}>
            {code === 401 ? <Icon name="lock" size={16} /> : null}
            {primaryLabel}
          </button>
        </div>
      </div>
    </section>
  );
}

export function StandaloneErrorPage(): React.ReactElement {
  const params = useParams();
  const parsed = Number(params.code);
  const code: ErrorCode = parsed === 401 || parsed === 403 || parsed === 500 ? parsed : 404;
  return (
    <div className="standalone-error">
      <Link to="/" className="rail-brand standalone-brand">
        <span className="line-mark" aria-hidden="true" />
        <span className="rail-brand-title">BlueMoon</span>
      </Link>
      <ErrorPage code={code} />
    </div>
  );
}
