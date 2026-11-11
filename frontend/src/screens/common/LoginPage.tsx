import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ApiHttpError } from '../../api/client';
import { useAppState } from '../../state/AppStateContext';
import { defaultHomeByRole } from '../../lib/format';
import { Icon } from '../../components/Icon';
import { ThemeToggle } from '../../components/ThemeToggle';
import { SHOW_DEV_TOOLS } from '../../lib/devTools';
import type { ForcedUiState } from '../../types/domain';

type LoginStatus = 'idle' | 'submitting' | 'invalid' | 'missing' | 'network' | 'csrf' | 'success';

const FORCED_TO_STATUS: Partial<Record<ForcedUiState, LoginStatus>> = {
  empty: 'missing',
  error: 'network',
  'forbidden-403': 'csrf',
  'unauthorized-401': 'invalid',
  submitting: 'submitting',
  success: 'success',
};

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
  } = useAppState();
  const navigate = useNavigate();
  const location = useLocation();
  const navState = (location.state as { from?: string; passwordChanged?: boolean } | null) ?? null;

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [motionPaused, setMotionPaused] = useState(false);
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
    <div className="login-page" data-motion={motionPaused ? 'paused' : 'running'}>
      <a className="skip-link" href="#login-main">
        Bỏ qua thanh trên, tới biểu mẫu đăng nhập
      </a>
      <div className="login-scene" aria-hidden="true">
        <div className="scene-stars" />
        <div className="scene-twinkle" />
        <div className="scene-ambient" />
        <svg
          className="scene-wave"
          viewBox="0 0 1563 960"
          preserveAspectRatio="none"
          aria-hidden="true"
        >
          <defs>
            <linearGradient id="bm-wave-fill" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="var(--login-wave-fill-top)" />
              <stop offset="100%" stopColor="var(--login-wave-fill-bottom)" />
            </linearGradient>
            <linearGradient id="bm-wave-line" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="var(--login-wave-edge)" stopOpacity="0.15" />
              <stop offset="22%" stopColor="var(--login-wave-crest)" stopOpacity="0.72" />
              <stop offset="50%" stopColor="var(--login-wave-edge)" stopOpacity="0.28" />
              <stop offset="72%" stopColor="var(--login-wave-crest)" stopOpacity="0.52" />
              <stop offset="100%" stopColor="var(--login-wave-edge)" stopOpacity="0.18" />
            </linearGradient>
            <filter id="bm-wave-glow" x="-10%" y="-30%" width="120%" height="160%">
              <feGaussianBlur stdDeviation="14" />
            </filter>
          </defs>
          <path
            d="M 0 800 C 220 822, 470 896, 780 911 C 990 912, 1180 868, 1380 865 C 1465 864, 1520 870, 1563 876 L 1563 960 L 0 960 Z"
            fill="url(#bm-wave-fill)"
          />
          <path
            d="M 0 800 C 220 822, 470 896, 780 911 C 990 912, 1180 868, 1380 865 C 1465 864, 1520 870, 1563 876"
            fill="none"
            stroke="url(#bm-wave-line)"
            strokeWidth="12"
            filter="url(#bm-wave-glow)"
          />
          <path
            d="M 0 800 C 220 822, 470 896, 780 911 C 990 912, 1180 868, 1380 865 C 1465 864, 1520 870, 1563 876"
            fill="none"
            stroke="url(#bm-wave-line)"
            strokeWidth="1.75"
          />
        </svg>
      </div>

      <header className="login-bar">
        <div className="login-wrap wrap login-bar-row">
          <div className="login-brand-scope">
            <span className="login-brand">
              <span className="line-mark" aria-hidden="true" />
              <span className="rail-brand-title">BlueMoon Complex</span>
            </span>
            {mode === 'demo' ? (
              <span className="badge badge-assumed">Chế độ Demo</span>
            ) : null}
          </div>
          <div className="login-appearance-controls">
            <button
              type="button"
              className="btn btn-ghost btn-sm motion-toggle"
              aria-label={motionPaused ? 'Bật nền động' : 'Dừng nền động'}
              title={motionPaused ? 'Bật nền động' : 'Dừng nền động'}
              aria-pressed={motionPaused}
              onClick={() => setMotionPaused((value) => !value)}
            >
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                {motionPaused ? <path d="m8 5 11 7-11 7Z" /> : <path d="M8 5v14M16 5v14" />}
              </svg>
              <span className="motion-toggle-label">{motionPaused ? 'Bật nền động' : 'Dừng nền động'}</span>
            </button>
            <ThemeToggle />
          </div>
        </div>
      </header>

      <main id="login-main" className="login-main" tabIndex={-1}>
        <section className="login-hero">
          <div className="login-container-outer">
            <div className="login-split">
              <div className="login-brand-panel">
                <svg
                  className="login-brand-art"
                  viewBox="0 0 662 594"
                  preserveAspectRatio="xMidYMid slice"
                  aria-hidden="true"
                >
                  <defs>
                    {/* Outer halo around the cropped moon in top-right */}
                    <radialGradient id="bm-panel-moon-halo" cx="585" cy="30" r="230" gradientUnits="userSpaceOnUse">
                      <stop offset="0%" stopColor="var(--login-moon-halo)" stopOpacity="0.48" />
                      <stop offset="52%" stopColor="var(--login-moon-halo)" stopOpacity="0.22" />
                      <stop offset="80%" stopColor="var(--login-moon-halo)" stopOpacity="0.06" />
                      <stop offset="100%" stopColor="var(--login-moon-halo)" stopOpacity="0" />
                    </radialGradient>

                    {/* Inner crescent fill of the cropped moon sphere (cx=614, cy=20, r=141) */}
                    <radialGradient
                      id="bm-panel-moon-body"
                      cx="646"
                      cy="-6"
                      r="172"
                      fx="656"
                      fy="-14"
                      gradientUnits="userSpaceOnUse"
                    >
                      <stop offset="0%" stopColor="var(--login-moon-core)" />
                      <stop offset="56%" stopColor="var(--login-moon-mid)" />
                      <stop offset="78%" stopColor="var(--login-moon-crescent)" />
                      <stop offset="93%" stopColor="var(--login-moon-rim)" />
                      <stop offset="100%" stopColor="var(--login-moon-edge)" />
                    </radialGradient>

                    {/* Mask that fades the moon's bright lower-right limb near the vertical divider */}
                    <linearGradient id="bm-moon-fade-grad" x1="470" y1="15" x2="610" y2="132" gradientUnits="userSpaceOnUse">
                      <stop offset="0%" stopColor="#ffffff" stopOpacity="0.94" />
                      <stop offset="46%" stopColor="#ffffff" stopOpacity="0.78" />
                      <stop offset="76%" stopColor="#ffffff" stopOpacity="0.16" />
                      <stop offset="94%" stopColor="#ffffff" stopOpacity="0" />
                      <stop offset="100%" stopColor="#ffffff" stopOpacity="0" />
                    </linearGradient>
                    <mask id="bm-moon-crescent-mask">
                      <rect x="400" y="-150" width="300" height="350" fill="url(#bm-moon-fade-grad)" />
                    </mask>

                    {/* Rim stroke gradient along left/lower-left limb of the moon */}
                    <linearGradient id="bm-panel-moon-stroke" x1="473" y1="10" x2="612" y2="148" gradientUnits="userSpaceOnUse">
                      <stop offset="0%" stopColor="var(--login-moon-stroke-soft)" stopOpacity="0.55" />
                      <stop offset="38%" stopColor="var(--login-moon-stroke-bright)" stopOpacity="0.9" />
                      <stop offset="68%" stopColor="var(--login-moon-stroke-soft)" stopOpacity="0.22" />
                      <stop offset="86%" stopColor="var(--login-moon-stroke-soft)" stopOpacity="0" />
                      <stop offset="100%" stopColor="var(--login-moon-stroke-soft)" stopOpacity="0" />
                    </linearGradient>

                    {/* Upward atmospheric glow above the bottom curved horizon */}
                    <radialGradient
                      id="bm-panel-horizon-bloom"
                      cx="300"
                      cy="488"
                      r="250"
                      gradientTransform="translate(0 130) scale(1 0.72)"
                      gradientUnits="userSpaceOnUse"
                    >
                      <stop offset="0%" stopColor="var(--login-horizon-bloom-core)" stopOpacity="0.46" />
                      <stop offset="36%" stopColor="var(--login-horizon-bloom-mid)" stopOpacity="0.22" />
                      <stop offset="72%" stopColor="var(--login-horizon-bloom-mid)" stopOpacity="0.06" />
                      <stop offset="100%" stopColor="var(--login-horizon-bloom-mid)" stopOpacity="0" />
                    </radialGradient>

                    {/* Fill below the curved horizon arc */}
                    <radialGradient id="bm-panel-horizon-body" cx="295" cy="472" r="240" gradientUnits="userSpaceOnUse">
                      <stop offset="0%" stopColor="var(--login-horizon-under-bright)" />
                      <stop offset="32%" stopColor="var(--login-horizon-under-mid)" />
                      <stop offset="100%" stopColor="var(--login-horizon-under-deep)" />
                    </radialGradient>

                    {/* Bright rim light along the curved horizon arc */}
                    <linearGradient id="bm-panel-horizon-rim" x1="0" y1="500" x2="662" y2="540" gradientUnits="userSpaceOnUse">
                      <stop offset="0%" stopColor="var(--login-horizon-rim-edge)" stopOpacity="0.1" />
                      <stop offset="20%" stopColor="var(--login-horizon-rim-mid)" stopOpacity="0.42" />
                      <stop offset="39%" stopColor="var(--login-horizon-rim-hot)" stopOpacity="0.98" />
                      <stop offset="49%" stopColor="var(--login-horizon-rim-hot)" stopOpacity="0.92" />
                      <stop offset="68%" stopColor="var(--login-horizon-rim-mid)" stopOpacity="0.4" />
                      <stop offset="100%" stopColor="var(--login-horizon-rim-edge)" stopOpacity="0.05" />
                    </linearGradient>

                    <filter id="bm-horizon-blur" x="-20%" y="-40%" width="140%" height="180%">
                      <feGaussianBlur stdDeviation="8" />
                    </filter>
                  </defs>

                  {/* Top-right moon atmospheric glow + base dark sphere + masked crescent */}
                  <circle className="login-art-breathe" cx="585" cy="30" r="230" fill="url(#bm-panel-moon-halo)" />
                  <circle cx="614" cy="20" r="141" fill="var(--login-moon-core)" />
                  <circle
                    cx="614"
                    cy="20"
                    r="141"
                    fill="url(#bm-panel-moon-body)"
                    mask="url(#bm-moon-crescent-mask)"
                  />
                  <circle
                    cx="614"
                    cy="20"
                    r="141"
                    fill="none"
                    stroke="url(#bm-panel-moon-stroke)"
                    strokeWidth="1.5"
                  />

                  {/* Bottom horizon atmospheric bloom + body + luminous rim */}
                  <rect
                    className="login-art-breathe"
                    x="0"
                    y="300"
                    width="662"
                    height="294"
                    fill="url(#bm-panel-horizon-bloom)"
                  />
                  <circle cx="244" cy="1183" r="710" fill="url(#bm-panel-horizon-body)" />
                  <circle
                    cx="244"
                    cy="1183"
                    r="710"
                    fill="none"
                    stroke="url(#bm-panel-horizon-rim)"
                    strokeWidth="7"
                    filter="url(#bm-horizon-blur)"
                  />
                  <circle
                    cx="244"
                    cy="1183"
                    r="710"
                    fill="none"
                    stroke="url(#bm-panel-horizon-rim)"
                    strokeWidth="1.6"
                  />
                </svg>

                <div className="login-brand-content">
                  <span className="login-brand-badge">Phiên bảo mật</span>
                  <div className="login-brand-heading">
                    BlueMoon{' '}
                    <br />
                    Complex
                  </div>
                  <p className="login-brand-desc">
                    Hệ thống quản lý cư trú, hộ gia đình, hóa đơn
                    <br className="hide-mobile" /> và dịch vụ tòa nhà.
                  </p>
                </div>
              </div>

              <div className="login-card" role="group" aria-labelledby="login-form-title">
                <div className="login-card-inner">
                  <h1 id="login-form-title" className="login-card-title">
                    Đăng nhập
                  </h1>

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
                        {SHOW_DEV_TOOLS ? (
                          <span>
                            Không kết nối được backend (localhost:8080). Chạy <code>docker compose up</code>{' '}
                            ở thư mục gốc, hoặc dùng chế độ Demo bên dưới để xem giao diện.
                          </span>
                        ) : (
                          <span>Không kết nối được máy chủ. Vui lòng thử lại sau ít phút.</span>
                        )}
                      </div>
                    </div>
                  ) : null}

                  {showSkeleton ? (
                    <div aria-busy="true" aria-live="polite" className="login-skeleton">
                      <span className="field-hint">Đang kiểm tra phiên đăng nhập...</span>
                      <div className="skeleton-line short" />
                      <div className="skeleton-line tall" />
                      <div className="skeleton-line shorter" />
                      <div className="skeleton-line tall" />
                      <div className="skeleton-line tall" />
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
                      {effectiveStatus === 'missing' &&
                      !effectiveFieldErrors.username &&
                      !effectiveFieldErrors.password ? (
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

                      <div className="form-field login-field">
                        <label className="field-label login-field-label" htmlFor="login-username">
                          Tên đăng nhập
                        </label>
                        <div className="input-icon-wrap">
                          <span className="input-leading-icon" aria-hidden="true">
                            <Icon name="user" size={18} />
                          </span>
                          <input
                            id="login-username"
                            className="input login-input input-with-leading-icon"
                            autoComplete="username"
                            placeholder="Nhập tên đăng nhập"
                            maxLength={100}
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            aria-invalid={Boolean(effectiveFieldErrors.username)}
                            aria-describedby={effectiveFieldErrors.username ? 'login-username-err' : undefined}
                            disabled={submitting}
                          />
                        </div>
                        {effectiveFieldErrors.username ? (
                          <span id="login-username-err" className="field-error">
                            {effectiveFieldErrors.username}
                          </span>
                        ) : null}
                      </div>

                      <div className="form-field login-field">
                        <label className="field-label login-field-label" htmlFor="login-password">
                          Mật khẩu
                        </label>
                        <div className="password-wrap input-icon-wrap">
                          <span className="input-leading-icon" aria-hidden="true">
                            <Icon name="lock" size={18} />
                          </span>
                          <input
                            id="login-password"
                            className="input login-input input-with-leading-icon"
                            type={showPassword ? 'text' : 'password'}
                            autoComplete="current-password"
                            placeholder="Nhập mật khẩu"
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
                            <Icon name={showPassword ? 'eye-off' : 'eye'} size={18} />
                          </button>
                        </div>
                        {effectiveFieldErrors.password ? (
                          <span id="login-password-err" className="field-error">
                            {effectiveFieldErrors.password}
                          </span>
                        ) : null}
                      </div>

                      <button
                        type="submit"
                        className="btn btn-primary btn-block login-submit-btn"
                        disabled={submitting}
                      >
                        {submitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
                      </button>
                      <p className="sr-only" role="status" aria-live="polite">
                        {submitting ? 'Đang xác thực phiên đăng nhập...' : ''}
                      </p>
                    </form>
                  )}

                  <div className="login-help-box">
                    <span className="login-help-icon" aria-hidden="true">
                      <Icon name="info" size={20} />
                    </span>
                    <p className="login-help-text">
                      Nhập sai nhiều lần, tài khoản có thể bị khóa tạm thời.
                      <br className="hide-mobile" /> Liên hệ Văn phòng Ban quản lý để được cấp hoặc đặt lại.
                    </p>
                  </div>

                  {SHOW_DEV_TOOLS ? (
                    <div className="dev-panel" aria-labelledby="dev-panel-title">
                      <div className="dev-panel-head">
                        <h2 id="dev-panel-title" className="box-title">
                          Công cụ trình diễn (chỉ khi phát triển)
                        </h2>
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
                        Vào nhanh bằng tài khoản mẫu (chuyển sang chế độ Demo). Ở chế độ Demo cũng có thể
                        gõ tên như <code>demo-admin</code> với mật khẩu bất kỳ từ 4 ký tự.
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
                      </div>
                    </div>
                  ) : null}
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
