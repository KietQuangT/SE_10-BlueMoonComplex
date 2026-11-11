import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import { formatAreaM2, roleDisplayName } from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { Icon } from '../../components/Icon';
import { ApiHttpError, changePasswordApi, fetchCurrentUser } from '../../api/client';
import { messageForError } from '../../api/errors';
import { passwordRuleChecks, passwordRulesOk } from '../../lib/passwordRules';

type PwStatus = 'idle' | 'submitting' | 'wrong-current' | 'success';

const LOCKED_MESSAGE =
  'Tài khoản tạm thời bị khóa do nhập sai mật khẩu nhiều lần. Đợi hết thời gian khóa rồi thử lại.';

/** M-03 — Hồ sơ cá nhân (chỉ xem, từ /auth/me) và Đổi mật khẩu (S03: POST /api/v1/auth/change-password). */
export function ProfilePage(): React.ReactElement {
  const { user, households, mode, forcedUiState, completePasswordChangeDemo, logout } = useAppState();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const mustChange = Boolean(user?.mustChangePassword);
  const tab = mustChange || params.get('tab') === 'password' ? 'password' : 'profile';

  const [current, setCurrent] = useState('');
  const [next, setNext] = useState('');
  const [confirm, setConfirm] = useState('');
  const [touched, setTouched] = useState(false);
  const [status, setStatus] = useState<PwStatus>('idle');
  const [apiMessage, setApiMessage] = useState<string | null>(null);

  const effectiveStatus: PwStatus =
    forcedUiState === 'submitting'
      ? 'submitting'
      : forcedUiState === 'success'
        ? 'success'
        : forcedUiState === 'error'
          ? 'wrong-current'
          : status;

  useEffect(() => {
    if (status !== 'success') return;
    const handle = window.setTimeout(async () => {
      navigate('/login', { replace: true, state: { passwordChanged: true } });
      await completePasswordChangeDemo();
    }, 1800);
    return () => window.clearTimeout(handle);
  }, [status, navigate, completePasswordChangeDemo]);

  if (!user) return <></>;
  const household = households.find((h) => h.id === user.householdId);

  const rules = passwordRuleChecks(next);
  const rulesOk = passwordRulesOk(rules);
  const matchOk = next.length > 0 && next === confirm;
  const errors = {
    current: touched && !current ? 'Vui lòng nhập mật khẩu hiện tại.' : undefined,
    next: touched && !rulesOk ? 'Mật khẩu mới chưa đạt đủ quy tắc bên dưới.' : undefined,
    confirm: touched && !matchOk ? 'Mật khẩu nhập lại không khớp.' : undefined,
  };

  const finish = () => {
    setCurrent('');
    setNext('');
    setConfirm('');
    setStatus('success');
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched(true);
    setApiMessage(null);
    if (!current || !rulesOk || !matchOk) return;
    setStatus('submitting');
    if (mode !== 'api') {
      window.setTimeout(finish, 500);
      return;
    }
    try {
      await changePasswordApi(current, next);
      finish();
    } catch (err) {
      setStatus('idle');
      const problem = err instanceof ApiHttpError ? err.problem : undefined;
      if (problem?.code === 'CURRENT_PASSWORD_INCORRECT') {
        setStatus('wrong-current');
      } else if (problem?.status === 401) {
        // 401 khi phiên còn sống = tài khoản đang bị khóa do nhập sai (phiên KHÔNG bị hủy).
        const sessionAlive = await fetchCurrentUser().then(
          () => true,
          () => false,
        );
        setApiMessage(sessionAlive ? LOCKED_MESSAGE : messageForError(err));
      } else {
        setApiMessage(messageForError(err));
      }
    }
  };

  return (
    <>
      <ScreenHeader
        screenCode="M-03"
        title="Hồ sơ cá nhân và đổi mật khẩu"
        description="Thông tin hồ sơ người dùng và quản lý mật khẩu tài khoản."
        contract={tab === 'profile' || mode === 'api' ? 'api-live' : 'pending-rule'}
        tierLabel="Mức A"
      />

      {mustChange ? (
        <div className="banner banner-warn" role="alert">
          <div className="banner-content">
            <Icon name="lock" />
            <span>
              Bạn đang dùng mật khẩu tạm do Ban quản trị cấp. Hãy đổi mật khẩu trước khi dùng các
              chức năng khác; máy chủ đang chặn mọi thao tác còn lại.
            </span>
          </div>
        </div>
      ) : null}

      <div className="tab-strip page-tabs" role="tablist" aria-label="Hồ sơ">
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'profile'}
          className={`tab-btn${tab === 'profile' ? ' active' : ''}`}
          disabled={mustChange}
          onClick={() => setParams({})}
        >
          Hồ sơ
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'password'}
          className={`tab-btn${tab === 'password' ? ' active' : ''}`}
          onClick={() => setParams({ tab: 'password' })}
        >
          Đổi mật khẩu
        </button>
      </div>

      {tab === 'profile' ? (
        <section className="surface-box surface-box-pad" aria-label="Thông tin hồ sơ">
          <dl className="detail-dl">
            <div>
              <dt>Họ tên</dt>
              <dd>{user.fullName}</dd>
            </div>
            <div>
              <dt>Vai trò</dt>
              <dd>{roleDisplayName(user.role)}</dd>
            </div>
            <div>
              <dt>Tên đăng nhập</dt>
              <dd className="tabular">{user.username}</dd>
            </div>
            {household ? (
              <div>
                <dt>Căn hộ</dt>
                <dd>
                  <span className="tabular">{household.apartmentCode}</span> ·{' '}
                  {formatAreaM2(household.areaM2)}
                </dd>
              </div>
            ) : null}
          </dl>
        </section>
      ) : (
        <section className="surface-box surface-box-pad narrow-form" aria-label="Đổi mật khẩu">
          {apiMessage ? (
            <div className="banner banner-error" role="alert">
              <div className="banner-content">
                <Icon name="alert" />
                <span>{apiMessage}</span>
              </div>
            </div>
          ) : null}
          {effectiveStatus === 'wrong-current' ? (
            <div className="banner banner-error" role="alert">
              <div className="banner-content">
                <Icon name="alert" />
                <span>Mật khẩu hiện tại không đúng. Kiểm tra lại rồi thử lần nữa.</span>
              </div>
            </div>
          ) : null}
          {effectiveStatus === 'success' ? (
            <div className="banner banner-success" role="status">
              <div className="banner-content">
                <Icon name="check" />
                <span>Đổi thành công, vui lòng đăng nhập lại. Các phiên cũ đã bị hủy.</span>
              </div>
            </div>
          ) : null}

          <form onSubmit={onSubmit} noValidate className="stack-form">
            <div className="form-field">
              <label className="field-label" htmlFor="pw-current">
                Mật khẩu hiện tại
              </label>
              <input
                id="pw-current"
                className="input"
                type="password"
                autoComplete="current-password"
                value={current}
                onChange={(e) => setCurrent(e.target.value)}
                aria-invalid={Boolean(errors.current)}
                aria-describedby={errors.current ? 'pw-current-err' : undefined}
              />
              {errors.current ? (
                <span id="pw-current-err" className="field-error">
                  {errors.current}
                </span>
              ) : null}
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="pw-next">
                Mật khẩu mới
              </label>
              <input
                id="pw-next"
                className="input"
                type="password"
                autoComplete="new-password"
                value={next}
                onChange={(e) => setNext(e.target.value)}
                aria-invalid={Boolean(errors.next)}
                aria-describedby="pw-rules"
              />
              <ul id="pw-rules" className="criteria-list">
                {(
                  [
                    ['length', 'Tối thiểu 8 ký tự'],
                    ['upper', 'Có chữ hoa'],
                    ['lower', 'Có chữ thường'],
                    ['digit', 'Có chữ số'],
                    ['special', 'Có ký tự đặc biệt (không phải chữ hoặc số, kể cả khoảng trắng)'],
                    ['maxBytes', 'Tối đa 72 byte (chữ có dấu chiếm nhiều byte hơn)'],
                  ] as const
                ).map(([key, label]) => (
                  <li key={key} className={rules[key] && next ? 'met' : ''}>
                    <Icon name={rules[key] && next ? 'check' : 'info'} size={14} />
                    {label}
                  </li>
                ))}
              </ul>
              {errors.next ? <span className="field-error">{errors.next}</span> : null}
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="pw-confirm">
                Nhập lại mật khẩu mới
              </label>
              <input
                id="pw-confirm"
                className="input"
                type="password"
                autoComplete="new-password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                aria-invalid={Boolean(errors.confirm)}
                aria-describedby={errors.confirm ? 'pw-confirm-err' : undefined}
              />
              {errors.confirm ? (
                <span id="pw-confirm-err" className="field-error">
                  {errors.confirm}
                </span>
              ) : null}
            </div>
            <div className="filter-row">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={effectiveStatus === 'submitting' || effectiveStatus === 'success'}
              >
                {effectiveStatus === 'submitting' ? 'Đang đổi mật khẩu...' : 'Đổi mật khẩu'}
              </button>
              {mustChange ? (
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={async () => {
                    await logout();
                    navigate('/login', { replace: true });
                  }}
                >
                  Đăng xuất
                </button>
              ) : null}
            </div>
          </form>
        </section>
      )}
    </>
  );
}
