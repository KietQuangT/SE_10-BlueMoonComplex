import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiHttpError } from '../../api/client';
import { useAppState } from '../../state/AppStateContext';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ContractBadge } from '../../components/Badge';
import { Icon } from '../../components/Icon';
import type { CreateAccountPayload } from '../../types/domain';

type Role = CreateAccountPayload['role'];
type Errors = Partial<Record<'username' | 'fullName' | 'email' | 'role' | 'householdId' | 'form', string>>;

const USERNAME_RE = /^[A-Za-z0-9._-]{3,100}$/;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function mapApiError(err: ApiHttpError): Errors {
  const { status, code } = err.problem;
  if (status === 409) {
    if (code === 'EMAIL_ALREADY_USED') return { email: 'Email đã được sử dụng. Hãy dùng email khác.' };
    if (code === 'USERNAME_ALREADY_USED') return { username: 'Tên đăng nhập đã được sử dụng. Hãy chọn tên khác.' };
    return { form: 'Thông tin tài khoản bị trùng với một tài khoản khác. Kiểm tra lại rồi thử lần nữa.' };
  }
  if (status === 400) {
    if (code === 'HOUSEHOLD_REQUIRED') return { householdId: 'Tài khoản Cư dân bắt buộc gắn với một hộ.' };
    if (code === 'HOUSEHOLD_INVALID') return { householdId: 'Hộ không tồn tại hoặc không còn hoạt động.' };
    if (code === 'HOUSEHOLD_NOT_ALLOWED') return { householdId: 'Chỉ tài khoản Cư dân mới được gắn hộ.' };
    return { form: 'Dữ liệu gửi lên không hợp lệ. Kiểm tra định dạng từng ô.' };
  }
  if (status === 403) return { form: 'Bạn không có quyền cấp tài khoản.' };
  if (status === 401) return { form: 'Phiên đăng nhập đã hết hạn. Đăng nhập lại để tiếp tục.' };
  return {
    form: `Có lỗi xảy ra, vui lòng thử lại.${err.problem.requestId ? ` Mã yêu cầu: ${err.problem.requestId}.` : ''}`,
  };
}

/** M-11 — Tạo tài khoản (API thật: POST /api/v1/accounts, chỉ ADMIN, CSRF bắt buộc). */
export function CreateAccountPage(): React.ReactElement {
  const { households, provisionAccount, mode, forcedUiState } = useAppState();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', fullName: '', email: '', role: 'RESIDENT' as Role, householdId: '' });
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);
  const [created, setCreated] = useState<string | null>(null);

  const activeHouseholds = households.filter((h) => h.status === 'ACTIVE');
  const set = (key: keyof typeof form, value: string) => setForm((prev) => ({ ...prev, [key]: value }));

  const validate = (): Errors => {
    const next: Errors = {};
    if (!USERNAME_RE.test(form.username.trim()))
      next.username = 'Từ 3 đến 100 ký tự, chỉ gồm chữ không dấu, số và . _ -';
    if (!form.fullName.trim()) next.fullName = 'Nhập họ tên.';
    else if (form.fullName.trim().length > 150) next.fullName = 'Họ tên tối đa 150 ký tự.';
    if (!EMAIL_RE.test(form.email.trim())) next.email = 'Email chưa đúng định dạng.';
    if (form.role === 'RESIDENT' && !form.householdId) next.householdId = 'Chọn hộ cho tài khoản Cư dân.';
    return next;
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const next = validate();
    setErrors(next);
    if (Object.keys(next).length) return;
    setSubmitting(true);
    try {
      await provisionAccount({
        username: form.username.trim(),
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        role: form.role,
        householdId: form.role === 'RESIDENT' ? Number(form.householdId) : null,
      });
      setCreated(form.fullName.trim());
      setForm({ username: '', fullName: '', email: '', role: 'RESIDENT', householdId: '' });
    } catch (err) {
      if (err instanceof ApiHttpError) {
        if (err.problem.code === 'PASSWORD_CHANGE_REQUIRED') {
          navigate('/profile?tab=password');
          return;
        }
        setErrors(mapApiError(err));
      } else {
        setErrors({ form: 'Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại.' });
      }
    } finally {
      setSubmitting(false);
    }
  };

  const busy = submitting || forcedUiState === 'submitting';
  const showSuccess = created || forcedUiState === 'success';
  const fieldError = (key: keyof Errors) =>
    errors[key] ? (
      <span id={`acc-${key}-err`} className="field-error">
        {errors[key]}
      </span>
    ) : null;

  return (
    <>
      <Link to="/admin/accounts" className="btn btn-ghost btn-sm back-link">
        <Icon name="arrow-left" size={16} />
        Về danh sách tài khoản
      </Link>
      <ScreenHeader
        screenCode="M-11"
        title="Tạo tài khoản"
        description="Mật khẩu tạm do máy chủ sinh và chỉ gửi qua email mô phỏng; không hiển thị trên màn hình. Người dùng phải đổi mật khẩu ở lần đăng nhập đầu."
        contract="api-live"
        tierLabel="Mức B"
      />

      {showSuccess ? (
        <div className="banner banner-success" role="status">
          <div className="banner-content">
            <Icon name="check" />
            <span>
              Đã tạo tài khoản{created ? ` cho ${created}` : ''}. Mật khẩu tạm đã được gửi tới email (mô phỏng).
            </span>
          </div>
          <Link to="/admin/accounts" className="btn btn-ghost btn-sm">
            Xem danh sách
          </Link>
        </div>
      ) : null}
      {errors.form || forcedUiState === 'error' ? (
        <div className="banner banner-error" role="alert">
          <div className="banner-content">
            <Icon name="alert" />
            <span>{errors.form ?? 'Có lỗi xảy ra, vui lòng thử lại.'}</span>
          </div>
        </div>
      ) : null}

      <section className="surface-box surface-box-pad">
        <form onSubmit={onSubmit} noValidate className="form-grid">
          <div className="form-field">
            <label className="field-label" htmlFor="acc-fullName">
              Họ tên
            </label>
            <input
              id="acc-fullName"
              className="input"
              maxLength={150}
              value={form.fullName}
              onChange={(e) => set('fullName', e.target.value)}
              aria-invalid={Boolean(errors.fullName)}
              aria-describedby={errors.fullName ? 'acc-fullName-err' : undefined}
            />
            {fieldError('fullName')}
          </div>
          <div className="form-field">
            <label className="field-label" htmlFor="acc-email">
              Email
            </label>
            <input
              id="acc-email"
              className="input"
              type="email"
              autoComplete="off"
              value={form.email}
              onChange={(e) => set('email', e.target.value)}
              aria-invalid={Boolean(errors.email)}
              aria-describedby={errors.email ? 'acc-email-err' : 'acc-email-hint'}
            />
            {errors.email ? (
              fieldError('email')
            ) : (
              <span id="acc-email-hint" className="field-hint">
                Bắt buộc và duy nhất. Cư dân chưa có email thì chưa cấp được (Q21).
              </span>
            )}
          </div>
          <div className="form-field">
            <label className="field-label" htmlFor="acc-username">
              Tên đăng nhập
            </label>
            <input
              id="acc-username"
              className="input tabular"
              maxLength={100}
              autoComplete="off"
              value={form.username}
              onChange={(e) => set('username', e.target.value)}
              aria-invalid={Boolean(errors.username)}
              aria-describedby={errors.username ? 'acc-username-err' : 'acc-username-hint'}
            />
            {errors.username ? (
              fieldError('username')
            ) : (
              <span id="acc-username-hint" className="field-hint">
                Ban quản trị nhập (D19): 3–100 ký tự, chữ không dấu, số và . _ -
              </span>
            )}
          </div>
          <div className="form-field">
            <label className="field-label" htmlFor="acc-role">
              Vai trò
            </label>
            <select
              id="acc-role"
              className="select"
              value={form.role}
              onChange={(e) => set('role', e.target.value)}
            >
              <option value="RESIDENT">Cư dân</option>
              <option value="ACCOUNTANT">Kế toán</option>
              <option value="ADMIN">Ban quản trị</option>
            </select>
            <span className="field-hint">Vai trò Cơ quan chức năng chưa có đường cấp (Q19).</span>
          </div>
          {form.role === 'RESIDENT' ? (
            <div className="form-field span-2">
              <label className="field-label" htmlFor="acc-household">
                <span>Hộ gia đình</span>
                {mode === 'api' ? <ContractBadge kind="assumed" /> : null}
              </label>
              <select
                id="acc-household"
                className="select"
                value={form.householdId}
                onChange={(e) => set('householdId', e.target.value)}
                aria-invalid={Boolean(errors.householdId)}
                aria-describedby={errors.householdId ? 'acc-householdId-err' : 'acc-household-hint'}
              >
                <option value="">Chọn hộ đang hoạt động</option>
                {activeHouseholds.map((h) => (
                  <option key={h.id} value={h.id}>
                    {h.householdCode} · Căn {h.apartmentCode}
                  </option>
                ))}
              </select>
              {errors.householdId ? (
                fieldError('householdId')
              ) : (
                <span id="acc-household-hint" className="field-hint">
                  Một tài khoản gắn đúng một hộ (D15).
                  {mode === 'api'
                    ? ' Danh sách hộ đang lấy từ dữ liệu mẫu cho tới khi có API S05; mã hộ phải tồn tại trong DB.'
                    : ''}
                </span>
              )}
            </div>
          ) : null}
          <div className="form-field span-2 form-actions">
            <button type="submit" className="btn btn-primary" disabled={busy}>
              {busy ? 'Đang tạo tài khoản...' : 'Tạo tài khoản'}
            </button>
            <Link to="/admin/accounts" className="btn btn-secondary">
              Hủy
            </Link>
          </div>
        </form>
      </section>
      <p className="field-hint">
        Nơi xem email mô phỏng cho QA và demo: bảng <code>mock_emails</code> qua SQL, chưa có màn hình (Q24).
      </p>
    </>
  );
}
