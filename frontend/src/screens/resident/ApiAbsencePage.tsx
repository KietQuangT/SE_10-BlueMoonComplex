import React, { useCallback, useEffect, useState } from 'react';
import { cancelDeclaration, createDeclaration, fetchMyDeclarations, type DeclarationPage } from '../../api/residency';
import { ChangeStatusBadge } from '../../components/ResidencyBadge';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { PaginationBar } from '../../components/PaginationBar';
import { ScreenHeader } from '../../components/ScreenHeader';
import { formatBusinessDate } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

const today = () => new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());

export function ApiAbsencePage(): React.ReactElement {
  const { user, residents, absenceDraft: draft, setAbsenceDraft, handleCallFailure, refreshAbsenceCount, dataLoadError } = useAppState();
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [data, setData] = useState<DeclarationPage | null>(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [cancelNotice, setCancelNotice] = useState('');
  const [loadError, setLoadError] = useState('');
  const [invalidField, setInvalidField] = useState('');
  const [busy, setBusy] = useState(false);
  const [cancelId, setCancelId] = useState<number | null>(null);
  const members = residents.filter((r) => r.householdId === user?.householdId && r.residencyStatus === 'Đang cư trú');
  const load = useCallback(async () => {
    try { setData(await fetchMyDeclarations(page - 1, size)); setLoadError(''); }
    catch (err) { setLoadError(handleCallFailure(err)); }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);
  useEffect(() => { void load(); }, [load]);
  const update = (key: keyof typeof draft, value: string) => setAbsenceDraft((prev) => ({ ...prev, [key]: value }));
  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setNotice(''); setInvalidField('');
    const invalid = (document.getElementById('abs-from') as HTMLInputElement).validity.badInput ? ['abs-from', 'Từ ngày chưa đúng định dạng.']
      : (document.getElementById('abs-to') as HTMLInputElement).validity.badInput ? ['abs-to', 'Đến ngày chưa đúng định dạng.']
      : !members.some((m) => String(m.id) === draft.residentId) ? ['abs-resident', 'Vui lòng chọn người vắng mặt.']
      : !draft.effectiveDate ? ['abs-from', 'Vui lòng chọn từ ngày.']
      : draft.effectiveDate < today() ? ['abs-from', 'Từ ngày không được trước hôm nay.']
      : draft.endDateAssumed && draft.endDateAssumed < draft.effectiveDate ? ['abs-to', 'Đến ngày không được trước từ ngày.']
      : !draft.reason.trim() ? ['abs-reason', 'Vui lòng nhập lý do.']
      : draft.reason.length > 255 ? ['abs-reason', 'Lý do tối đa 255 ký tự.'] : null;
    if (invalid) { setInvalidField(invalid[0]); setError(invalid[1]); document.getElementById(invalid[0])?.focus(); return; }
    setBusy(true);
    try {
      await createDeclaration({ residentId: Number(draft.residentId), effectiveDate: draft.effectiveDate, ...(draft.endDateAssumed ? { endDate: draft.endDateAssumed } : {}), reason: draft.reason.trim() });
      setAbsenceDraft((prev) => ({ ...prev, residentId: '', effectiveDate: '', endDateAssumed: '', reason: '' })); setNotice('Đã gửi đơn và đang chờ duyệt.');
      if (page !== 1) setPage(1); else await load();
      await refreshAbsenceCount();
    } catch (err) { setError(handleCallFailure(err)); } finally { setBusy(false); }
  };
  const cancel = async () => {
    if (cancelId == null) return;
    setBusy(true); setError(''); setNotice(''); setCancelNotice('');
    try { await cancelDeclaration(cancelId); setCancelId(null); await load(); await refreshAbsenceCount(); setCancelNotice('Đã hủy đơn.'); }
    catch (err) { setError(handleCallFailure(err)); await load(); await refreshAbsenceCount(); } finally { setBusy(false); }
  };
  return <>
    <ScreenHeader screenCode="M-31" title="Khai báo tạm vắng" description="Gửi đơn cho thành viên trong hộ và xem kết quả duyệt." contract="api-live" tierLabel="Mức A" />
    {loadError && <div className="banner banner-error" role="alert">{loadError}</div>}
    {dataLoadError && <div className="banner banner-error" role="alert">Không tải được danh sách nhân khẩu. Vui lòng tải lại trang rồi thử lại.</div>}
    <section className="surface-box surface-box-pad"><h2 className="box-title">Tạo đơn mới</h2>
      <form onSubmit={submit} noValidate className="form-grid absence-form">
        <div className="form-field span-2"><label htmlFor="abs-resident">Người vắng mặt</label><select id="abs-resident" aria-describedby={invalidField === 'abs-resident' ? 'absence-error' : undefined} className="select" value={draft.residentId} onChange={(e) => update('residentId', e.target.value)} required><option value="">Chọn người vắng mặt</option>{members.map((m) => <option key={m.id} value={m.id}>{m.fullName}</option>)}</select></div>
        <div className="form-field"><label htmlFor="abs-from">Từ ngày</label><input id="abs-from" aria-describedby={invalidField === 'abs-from' ? 'absence-error' : undefined} className="input" type="date" min={today()} value={draft.effectiveDate} onChange={(e) => update('effectiveDate', e.target.value)} required /></div>
        <div className="form-field"><label htmlFor="abs-to">Đến ngày (không bắt buộc)</label><input id="abs-to" aria-describedby={invalidField === 'abs-to' ? 'absence-error' : undefined} className="input" type="date" min={draft.effectiveDate} value={draft.endDateAssumed} onChange={(e) => update('endDateAssumed', e.target.value)} /></div>
        <div className="form-field span-2"><label htmlFor="abs-reason">Lý do ({draft.reason.length}/255)</label><textarea id="abs-reason" aria-describedby={invalidField === 'abs-reason' ? 'absence-error' : undefined} className="textarea" maxLength={255} value={draft.reason} onChange={(e) => update('reason', e.target.value)} required /></div>
        <div className="form-field span-2">{error && cancelId == null && <div id="absence-error" className="banner banner-error" role="alert">{error}</div>}{notice && <div className="banner banner-success" role="status">{notice}</div>}<button className="btn btn-primary" disabled={busy}>Gửi đơn khai báo</button></div>
      </form>
    </section>
    <h2 className="section-heading">Đơn của tôi</h2>{cancelNotice && <div className="banner banner-success" role="status">{cancelNotice}</div>}<section className="surface-box">
      {!data ? <p>Đang tải danh sách...</p> : data.content.length === 0 ? <p>Chưa có đơn của hộ.</p> : <ul className="form-history">{data.content.map((d) => <li key={d.id}><div className="mobile-row-top"><strong>{d.residentName}</strong><ChangeStatusBadge status={d.status} /></div><span>{formatBusinessDate(d.effectiveDate)}{d.endDate ? ` – ${formatBusinessDate(d.endDate)}` : ''} · {d.reason}</span>{d.status === 'REJECTED' && d.decisionNote && <p className="field-hint">Lý do từ chối: {d.decisionNote}</p>}{d.cancellable && <button className="btn btn-secondary" type="button" onClick={() => { setError(''); setNotice(''); setCancelNotice(''); setInvalidField(''); setCancelId(d.id); }}>Hủy đơn</button>}</li>)}</ul>}
      {data && <PaginationBar page={page} pageSize={size} totalItems={data.totalElements} onPageChange={setPage} onPageSizeChange={setSize} />}
    </section>
    <ConfirmDialog open={cancelId != null} title="Hủy đơn tạm vắng?" consequence="Đơn sẽ được lưu với trạng thái Đã hủy." confirmLabel="Hủy đơn" confirmTone="danger" busy={busy} onCancel={() => { setCancelId(null); setError(''); }} onConfirm={() => void cancel()}>{error && <p className="field-error" role="alert">{error}</p>}</ConfirmDialog>
  </>;
}
