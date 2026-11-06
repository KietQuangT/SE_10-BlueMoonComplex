import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ApiHttpError } from '../../api/client';
import { useAppState } from '../../state/AppStateContext';
import { defaultHomeByRole } from '../../lib/format';
import { Icon } from '../../components/Icon';
import { ContractBadge } from '../../components/Badge';
import type { ForcedUiState } from '../../types/domain';

type LoginStatus = 'idle' | 'submitting' | 'invalid' | 'missing' | 'network' | 'csrf' | 'success';

const SHOW_DEV_TOOLS = import.meta.env.DEV || import.meta.env.VITE_ENABLE_DEMO === 'true';

const FORCED_TO_STATUS: Partial<Record<ForcedUiState, LoginStatus>> = {
  empty: 'missing',
  error: 'network',
  'forbidden-403': 'csrf',
  'unauthorized-401': 'invalid',
  submitting: 'submitting',
  success: 'success',
};

const FLOORS = [
  { range: 'Tầng 30', label: 'Penthouse' },
  { range: 'Tầng 6–29', label: '24 tầng nhà ở' },
  { range: 'Tầng 2–5', label: '4 tầng đế' },
  { range: 'Tầng 1', label: 'Kiot và văn phòng Ban quản lý' },
];

/** M-01 — Đăng nhập (API thật: GET /auth/csrf → POST /auth/login → GET /auth/csrf → GET /auth/me). */
export function LoginPage(): React.ReactElement {
  const {
    user,
    authLoading,
    login,
    mode,
    setMode,
    backendReachable,
    sessionExpiredBanner,
    clearSessionExpiredBanner,
    returnToPath,
    forcedUiState,
    loginDemoAs,
    setQaDrawerOpen,
  } = useAppState();
  const navigate = useNavigate();
  const location = useLocation();
  const navState = (location.state as { from?: string; passwordChanged?: boolean } | null) ?? null;

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [status, setStatus] = useState<LoginStatus>('idle');
  const [requestId, setRequestId] = useState<string | undefined>();
  const [fieldErrors, setFieldErrors] = useState<{ username?: string; password?: string }>({});

  useEffect(() => {
    if (user) {
      const target = navState?.from || returnToPath || defaultHomeByRole(user.role);
      navigate(user.mustChangePassword ? '/profile?tab=password' : target, { replace: true });
    }
  }, [user, navigate, navState?.from, returnToPath]);

  const forcedStatus = FORCED_TO_STATUS[forcedUiState];
  const effectiveStatus: LoginStatus = forcedStatus ?? status;
  const effectiveFieldErrors =
    forcedUiState === 'empty'
      ? { username: 'Vui lòng nhập tên đăng nhập.', password: 'Vui lòng nhập mật khẩu.' }
      : fieldErrors;
  const showSkeleton = authLoading || forcedUiState === 'loading';
  const submitting = effectiveStatus === 'submitting';

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearSessionExpiredBanner();
    const errs: { username?: string; password?: string } = {};
    if (!username.trim()) errs.username = 'Vui lòng nhập tên đăng nhập.';
    if (!password) errs.password = 'Vui lòng nhập mật khẩu.';
    setFieldErrors(errs);
    if (errs.username || errs.password) {
      setStatus('missing');
      return;
    }
    setStatus('submitting');
    setRequestId(undefined);
    try {
      await login(username.trim(), password);
      setStatus('success');
    } catch (err) {
      setPassword('');
      if (err instanceof ApiHttpError) {
        setRequestId(err.problem.requestId);
        if (err.problem.status === 401) setStatus('invalid');
        else if (err.problem.status === 400) setStatus('missing');
        else if (err.problem.status === 403) setStatus('csrf');
        else setStatus('network');
      } else {
        setStatus('network');
      }
    }
  };

  return (
    <div className="login-page">
      <aside className="login-brand-column">
        <div>
          <div className="rail-brand login-brand">
            <span className="line-mark" aria-hidden="true" />
            <span>
              <span className="rail-brand-title">BlueMoon Complex</span>
              <span className="rail-brand-sub">Chung cư BlueMoon · Ngã tư Văn Phú</span>
            </span>
          </div>
          <p className="login-lede">
            Hệ thống quản lý cư trú và thu phí dành cho Ban quản trị, Kế toán và cư dân.
          </p>
        </div>
        <ol className="floor-line" aria-label="Sơ đồ tòa nhà 30 tầng">
          {FLOORS.map((f) => (
            <li key={f.range}>
              <span className="floor-stop" aria-hidden="true" />
              <span className="floor-range tabular">{f.range}</span>
              <span className="floor-label">{f.label}</span>
            </li>
          ))}
        </ol>
      </aside>

      <div className="login-form-column">
        <div className="login-panel">
          <div className="screen-title-row login-title-row">
            <h1 className="screen-title">Đăng nhập</h1>
            <span className="badge badge-code">M-01</span>
            <ContractBadge kind="api-live" />
          </div>

          {navState?.passwordChanged ? (
            <div className="banner banner-success" role="status">
              <div className="banner-content">
                <Icon name="check" />
                <span>Đã đổi mật khẩu. Vui lòng đăng nhập lại bằng mật khẩu mới.</span>
              </div>
            </div>
          ) : null}

          {sessionExpiredBanner ? (
            <div className="banner banner-warn" role="status">
              <div className="banner-content">
                <Icon name="lock" />
                <span>Phiên đăng nhập đã hết hạn. Đăng nhập lại để quay về trang đang xem.</span>
              </div>
            </div>
          ) : null}

          {mode === 'api' && backendReachable === false ? (
            <div className="banner banner-warn" role="status">
              <div className="banner-content">
                <Icon name="alert" />
                <span>
                  Không kết nối được backend (localhost:8080). Chạy <code>docker compose up</code> ở
                  thư mục gốc, hoặc dùng chế độ Demo bên dưới để xem giao diện.
                </span>
              </div>
            </div>
          ) : null}

          {showSkeleton ? (
            <div aria-busy="true" aria-live="polite" className="login-skeleton">
              <span className="field-hint">Đang kiểm tra phiên đăng nhập...</span>
              <div className="skeleton-line" style={{ width: '40%' }} />
              <div className="skeleton-line" style={{ height: 44 }} />
              <div className="skeleton-line" style={{ width: '30%' }} />
              <div className="skeleton-line" style={{ height: 44 }} />
              <div className="skeleton-line" style={{ height: 44, marginTop: 8 }} />
            </div>
          ) : (
            <form onSubmit={onSubmit} noValidate className="login-form">
              {effectiveStatus === 'invalid' ? (
                <div className="banner banner-error" role="alert">
                  <div className="banner-content">
                    <Icon name="alert" />
                    <span>Tên đăng nhập hoặc mật khẩu không đúng.</span>
                  </div>
                </div>
              ) : null}
              {effectiveStatus === 'missing' && !effectiveFieldErrors.username && !effectiveFieldErrors.password ? (
                <div className="banner banner-error" role="alert">
                  <div className="banner-content">
                    <Icon name="alert" />
                    <span>Dữ liệu gửi lên không hợp lệ. Kiểm tra lại tên đăng nhập và mật khẩu.</span>
                  </div>
                </div>
              ) : null}
              {effectiveStatus === 'csrf' ? (
                <div className="banner banner-error" role="alert">
                  <div className="banner-content">
                    <Icon name="alert" />
                    <span>Phiên bảo mật của trang đã hết hạn. Tải lại trang rồi đăng nhập lần nữa.</span>
                  </div>
                </div>
              ) : null}
              {effectiveStatus === 'network' ? (
                <div className="banner banner-error" role="alert">
                  <div className="banner-content">
                    <Icon name="alert" />
                    <span>
                      Không kết nối được máy chủ hoặc máy chủ đang lỗi. Vui lòng thử lại sau.
                      {requestId ? ` Mã yêu cầu: ${requestId}.` : ''}
                    </span>
                  </div>
                </div>
              ) : null}
              {effectiveStatus === 'success' ? (
                <div className="banner banner-success" role="status">
                  <div className="banner-content">
                    <Icon name="check" />
                    <span>Đăng nhập thành công. Đang chuyển tới trang đầu...</span>
                  </div>
                </div>
              ) : null}

              <div className="form-field">
                <label className="field-label" htmlFor="login-username">
                  Tên đăng nhập
                </label>
                <input
                  id="login-username"
                  className="input"
                  autoComplete="username"
                  maxLength={100}
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  aria-invalid={Boolean(effectiveFieldErrors.username)}
                  aria-describedby={effectiveFieldErrors.username ? 'login-username-err' : undefined}
                  disabled={submitting}
                />
                {effectiveFieldErrors.username ? (
                  <span id="login-username-err" className="field-error">
                    {effectiveFieldErrors.username}
                  </span>
                ) : null}
              </div>

              <div className="form-field">
                <label className="field-label" htmlFor="login-password">
                  Mật khẩu
                </label>
                <div className="password-wrap">
                  <input
                    id="login-password"
                    className="input"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    aria-invalid={Boolean(effectiveFieldErrors.password)}
                    aria-describedby={effectiveFieldErrors.password ? 'login-password-err' : undefined}
                    disabled={submitting}
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowPassword((v) => !v)}
                    aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                    aria-pressed={showPassword}
                  >
                    <Icon name={showPassword ? 'eye-off' : 'eye'} />
                  </button>
                </div>
                {effectiveFieldErrors.password ? (
                  <span id="login-password-err" className="field-error">
                    {effectiveFieldErrors.password}
                  </span>
                ) : null}
              </div>

              <button type="submit" className="btn btn-primary btn-block" disabled={submitting}>
                {submitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
              </button>
            </form>
          )}

          <div className="login-help-box">
            Nếu nhập sai nhiều lần, tài khoản có thể bị khóa tạm thời. Quên mật khẩu hoặc chưa có
            tài khoản: gọi Văn phòng Ban quản lý (số điện thoại và giờ làm việc do Ban quản trị cung
            cấp). <ContractBadge kind="assumed" />
          </div>
        </div>

        {SHOW_DEV_TOOLS ? (
          <section className="dev-panel" aria-labelledby="dev-panel-title">
            <div className="dev-panel-head">
              <h2 id="dev-panel-title" className="box-title">
                Công cụ trình diễn (chỉ khi phát triển)
              </h2>
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => setQaDrawerOpen(true)}>
                <Icon name="sliders" size={16} />
                Kiểm thử UI
              </button>
            </div>
            <div className="tab-strip" role="group" aria-label="Nguồn dữ liệu">
              <button
                type="button"
                className={`tab-btn${mode === 'api' ? ' active' : ''}`}
                aria-pressed={mode === 'api'}
                onClick={() => setMode('api')}
              >
                Backend thật
              </button>
              <button
                type="button"
                className={`tab-btn${mode === 'demo' ? ' active' : ''}`}
                aria-pressed={mode === 'demo'}
                onClick={() => setMode('demo')}
              >
                Demo (mock-data.json)
              </button>
            </div>
            <p className="field-hint">
              Vào nhanh bằng tài khoản mẫu (chuyển sang chế độ Demo). Ở chế độ Demo cũng có thể gõ
              tên như <code>demo-admin</code> với mật khẩu bất kỳ từ 4 ký tự.
            </p>
            <div className="demo-role-grid">
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => loginDemoAs('admin')}>
                Ban quản trị
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => loginDemoAs('accountant')}>
                Kế toán
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => loginDemoAs('resident')}>
                Cư dân (căn A01)
              </button>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => loginDemoAs('residentFirstLogin')}
              >
                Cư dân lần đầu (bắt đổi mật khẩu)
              </button>
              <button type="button" className="btn btn-secondary btn-sm" onClick={() => loginDemoAs('authority')}>
                Cơ quan chức năng
              </button>
            </div>
          </section>
        ) : null}
      </div>
    </div>
  );
}
