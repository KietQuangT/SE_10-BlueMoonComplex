import React, { useState } from 'react';
import { ApiAbsencePage } from './ApiAbsencePage';
import { useAppState } from '../../state/AppStateContext';
import { formatBusinessDate } from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ContractBadge } from '../../components/Badge';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { ChangeStatusBadge } from '../../components/ResidencyBadge';
import { Icon } from '../../components/Icon';

type FormErrors = Partial<Record<'residentId' | 'effectiveDate' | 'endDateAssumed' | 'reason', string>>;

/** M-31 — Khai báo tạm vắng + Đơn của tôi (đủ 8 trạng thái; giữ nội dung khi phiên hết hạn). */
export function AbsencePage(): React.ReactElement {
  const { mode } = useAppState();
  return mode === 'api' ? <ApiAbsencePage /> : <DemoAbsencePage />;
}

function DemoAbsencePage(): React.ReactElement {
  const {
    user,
    residents,
    residencyChanges,
    absenceDraft,
    setAbsenceDraft,
    submitAbsenceDemo,
    forcedUiState,
    setForcedUiState,
  } = useAppState();
  const [errors, setErrors] = useState<FormErrors>({});
  const [status, setStatus] = useState<'idle' | 'submitting' | 'success'>('idle');

  const members = residents.filter(
    (r) => r.householdId === user?.householdId && r.residencyStatus === 'Đang cư trú',
  );
  const myForms =
    forcedUiState === 'empty'
      ? []
      : residencyChanges.filter((c) => c.householdId === user?.householdId && c.changeType === 'TAM_VANG');
  const nameOf = (id: number) => residents.find((r) => r.id === id)?.fullName ?? 'Không rõ';

  const effective = forcedUiState === 'submitting' ? 'submitting' : forcedUiState === 'success' ? 'success' : status;

  const header = (
    <ScreenHeader
      screenCode="M-31"
      title="Khai báo tạm vắng"
      description="Gửi đơn cho thành viên trong hộ. Đơn chuyển vào hàng chờ duyệt của Ban quản trị."
      contract="db-ready"
      tierLabel="Mức A"
    />
  );

  if (forcedUiState === 'loading') {
    return (
      <>
        {header}
        <SkeletonRows rows={5} />
        <SkeletonRows rows={3} />
      </>
    );
  }
  if (forcedUiState === 'error') {
    return (
      <>
        {header}
        <ErrorRetryPanel requestId="demo-c4d102" onRetry={() => setForcedUiState('normal')} />
      </>
    );
  }

  const update = (key: keyof typeof absenceDraft, value: string) => {
    setAbsenceDraft((prev) => ({ ...prev, [key]: value }));
    if (status === 'success') setStatus('idle');
  };

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const next: FormErrors = {};
    if (!absenceDraft.residentId || !members.some((m) => String(m.id) === absenceDraft.residentId)) {
      next.residentId = 'Chọn một thành viên đang cư trú trong hộ.';
    }
    if ((document.getElementById('abs-from') as HTMLInputElement).validity.badInput) next.effectiveDate = 'Từ ngày chưa đúng định dạng.';
    else if (!absenceDraft.effectiveDate) next.effectiveDate = 'Chọn ngày bắt đầu vắng mặt.';
    if ((document.getElementById('abs-to') as HTMLInputElement).validity.badInput) next.endDateAssumed = 'Đến ngày chưa đúng định dạng.';
    else if (absenceDraft.endDateAssumed && absenceDraft.effectiveDate && absenceDraft.endDateAssumed < absenceDraft.effectiveDate) {
      next.endDateAssumed = 'Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.';
    }
    if (!absenceDraft.reason.trim()) next.reason = 'Nhập lý do vắng mặt.';
    else if (absenceDraft.reason.length > 255) next.reason = 'Lý do tối đa 255 ký tự.';
    setErrors(next);
    if (next.effectiveDate === 'Từ ngày chưa đúng định dạng.') document.getElementById('abs-from')?.focus();
    else if (next.endDateAssumed === 'Đến ngày chưa đúng định dạng.') document.getElementById('abs-to')?.focus();
    if (Object.keys(next).length) return;

    setStatus('submitting');
    window.setTimeout(() => {
      submitAbsenceDemo({
        residentId: Number(absenceDraft.residentId),
        effectiveDate: absenceDraft.effectiveDate,
        endDateAssumed: absenceDraft.endDateAssumed,
        reason: absenceDraft.reason,
      });
      setAbsenceDraft((prev) => ({ ...prev, reason: '' }));
      setStatus('success');
    }, 450);
  };

  const errorCount = Object.keys(errors).length;

  return (
    <>
      {header}

      {effective === 'success' ? (
        <div className="banner banner-success" role="status">
          <div className="banner-content">
            <Icon name="check" />
            <span>Đã gửi, đang chờ Ban quản trị duyệt. Kết quả sẽ hiện ở mục "Đơn của tôi" bên dưới.</span>
          </div>
        </div>
      ) : null}
      {errorCount > 1 ? (
        <div className="banner banner-error" role="alert">
          <div className="banner-content">
            <Icon name="alert" />
            <span>Đơn còn {errorCount} chỗ cần sửa. Xem ghi chú dưới từng ô.</span>
          </div>
        </div>
      ) : null}

      <section className="surface-box surface-box-pad" aria-labelledby="absence-form-title">
        <h2 id="absence-form-title" className="box-title">
          Tạo đơn mới
        </h2>
        <form onSubmit={onSubmit} noValidate className="form-grid absence-form">
          <div className="form-field span-2">
            <label className="field-label" htmlFor="abs-resident">
              Người vắng mặt
            </label>
            <select
              id="abs-resident"
              className="select"
              value={absenceDraft.residentId}
              onChange={(e) => update('residentId', e.target.value)}
              aria-invalid={Boolean(errors.residentId)}
              aria-describedby={errors.residentId ? 'abs-resident-err' : undefined}
            >
              <option value="">Chọn thành viên trong hộ</option>
              {members.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.fullName} ({m.relationshipToHead})
                </option>
              ))}
            </select>
            {errors.residentId ? (
              <span id="abs-resident-err" className="field-error">
                {errors.residentId}
              </span>
            ) : null}
          </div>
          <div className="form-field">
            <label className="field-label" htmlFor="abs-from">
              Từ ngày
            </label>
            <input
              id="abs-from"
              type="date"
              className="input"
              value={absenceDraft.effectiveDate}
              onChange={(e) => update('effectiveDate', e.target.value)}
              aria-invalid={Boolean(errors.effectiveDate)}
              aria-describedby={errors.effectiveDate ? 'abs-from-err' : undefined}
            />
            {errors.effectiveDate ? (
              <span id="abs-from-err" className="field-error">
                {errors.effectiveDate}
              </span>
            ) : null}
          </div>
          <div className="form-field">
            <label className="field-label" htmlFor="abs-to">
              <span>Đến ngày</span>
              <ContractBadge kind="assumed" />
            </label>
            <input
              id="abs-to"
              type="date"
              className="input"
              value={absenceDraft.endDateAssumed}
              onChange={(e) => update('endDateAssumed', e.target.value)}
              aria-invalid={Boolean(errors.endDateAssumed)}
              aria-describedby={errors.endDateAssumed ? 'abs-to-err' : 'abs-to-hint'}
            />
            {errors.endDateAssumed ? (
              <span id="abs-to-err" className="field-error">{errors.endDateAssumed}</span>
            ) : (
              <span id="abs-to-hint" className="field-hint">
                Chưa có cột trong DB.
              </span>
            )}
          </div>
          <div className="form-field span-2">
            <label className="field-label" htmlFor="abs-reason">
              <span>Lý do</span>
              <span className="field-hint tabular">{absenceDraft.reason.length}/255</span>
            </label>
            <textarea
              id="abs-reason"
              className="textarea"
              rows={3}
              maxLength={255}
              value={absenceDraft.reason}
              onChange={(e) => update('reason', e.target.value)}
              aria-invalid={Boolean(errors.reason)}
              aria-describedby={errors.reason ? 'abs-reason-err' : undefined}
            />
            {errors.reason ? (
              <span id="abs-reason-err" className="field-error">
                {errors.reason}
              </span>
            ) : null}
          </div>
          <div className="form-field span-2 sticky-mobile-cta">
            <button type="submit" className="btn btn-primary" disabled={effective === 'submitting'}>
              {effective === 'submitting' ? 'Đang gửi đơn...' : 'Gửi đơn khai báo'}
            </button>
          </div>
        </form>
      </section>

      <h2 className="section-heading">Đơn của tôi</h2>
      <section className="surface-box">
        {myForms.length === 0 ? (
          <div className="state-panel">
            <h3>Bạn chưa gửi đơn nào</h3>
            <p>Đơn khai báo tạm vắng đã gửi và kết quả duyệt sẽ hiện ở đây.</p>
          </div>
        ) : (
          <ul className="form-history">
            {myForms.map((f) => (
              <li key={f.id}>
                <div className="mobile-row-top">
                  <strong>{nameOf(f.residentId)}</strong>
                  <ChangeStatusBadge status={f.status} />
                </div>
                <span className="field-hint tabular">
                  {formatBusinessDate(f.effectiveDate)}
                  {f.endDateAssumed ? ` – ${formatBusinessDate(f.endDateAssumed)}` : ''} · {f.reason}
                </span>
                {f.status === 'REJECTED' ? (
                  <span className="reject-reason">
                    Lý do từ chối: {f.rejectReasonAssumed || 'Ban quản trị chưa ghi lý do'}{' '}
                    <ContractBadge kind="assumed" />
                  </span>
                ) : null}
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
