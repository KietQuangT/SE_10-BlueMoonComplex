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
  const [busy, setBusy] = useState(false);
  const [cancelId, setCancelId] = useState<number | null>(null);
  const members = residents.filter((r) => r.householdId === user?.householdId && r.residencyStatus === 'Đang cư trú');
  const load = useCallback(async (preserveError = false) => {
    try { setData(await fetchMyDeclarations(page - 1, size)); if (!preserveError) setError(''); }
    catch (err) { setError(handleCallFailure(err)); }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);
  useEffect(() => { void load(); }, [load]);
  const update = (key: keyof typeof draft, value: string) => setAbsenceDraft((prev) => ({ ...prev, [key]: value }));
  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!members.some((m) => String(m.id) === draft.residentId) || !draft.effectiveDate || draft.effectiveDate < today() || (draft.endDateAssumed && draft.endDateAssumed < draft.effectiveDate) || !draft.reason.trim() || draft.reason.length > 255) {
      setError('Thông tin chưa hợp lệ. Chọn nhân khẩu đang cư trú, kiểm tra ngày và lý do rồi thử lại.'); return;
    }
    setBusy(true); setError('');
    try {
      await createDeclaration({ residentId: Number(draft.residentId), effectiveDate: draft.effectiveDate, ...(draft.endDateAssumed ? { endDate: draft.endDateAssumed } : {}), reason: draft.reason.trim() });
      setAbsenceDraft((prev) => ({ ...prev, reason: '' })); setNotice('Đã gửi đơn và đang chờ duyệt.');
      if (page !== 1) setPage(1); else await load();
      await refreshAbsenceCount();
    } catch (err) { setError(handleCallFailure(err)); } finally { setBusy(false); }
  };
  const cancel = async () => {
    if (cancelId == null) return;
    setBusy(true); setError('');
    try { await cancelDeclaration(cancelId); setCancelId(null); await load(); await refreshAbsenceCount(); setNotice('Đã hủy đơn.'); }
    catch (err) { setError(handleCallFailure(err)); await load(true); await refreshAbsenceCount(); } finally { setBusy(false); }
  };
  return <>
    <ScreenHeader screenCode="M-31" title="Khai báo tạm vắng" description="Gửi đơn cho thành viên trong hộ và xem kết quả duyệt." contract="api-live" tierLabel="Mức A" />
    {error && cancelId == null && <div className="banner banner-error" role="alert">{error}</div>}
    {dataLoadError && <div className="banner banner-error" role="alert">Không tải được danh sách nhân khẩu. Vui lòng tải lại trang rồi thử lại.</div>}
    {notice && <div className="banner banner-success" role="status">{notice}</div>}
    <section className="surface-box surface-box-pad"><h2 className="box-title">Tạo đơn mới</h2>
      <form onSubmit={submit} className="form-grid absence-form">
        <div className="form-field span-2"><label htmlFor="abs-resident">Người vắng mặt</label><select id="abs-resident" className="select" value={draft.residentId} onChange={(e) => update('residentId', e.target.value)} required><option value="">Chọn người vắng mặt</option>{members.map((m) => <option key={m.id} value={m.id}>{m.fullName}</option>)}</select></div>
        <div className="form-field"><label htmlFor="abs-from">Từ ngày</label><input id="abs-from" className="input" type="date" min={today()} value={draft.effectiveDate} onChange={(e) => update('effectiveDate', e.target.value)} required /></div>
        <div className="form-field"><label htmlFor="abs-to">Đến ngày (không bắt buộc)</label><input id="abs-to" className="input" type="date" min={draft.effectiveDate} value={draft.endDateAssumed} onChange={(e) => update('endDateAssumed', e.target.value)} /></div>
        <div className="form-field span-2"><label htmlFor="abs-reason">Lý do ({draft.reason.length}/255)</label><textarea id="abs-reason" className="textarea" maxLength={255} value={draft.reason} onChange={(e) => update('reason', e.target.value)} required /></div>
        <div className="form-field span-2"><button className="btn btn-primary" disabled={busy}>Gửi đơn khai báo</button></div>
      </form>
    </section>
    <h2 className="section-heading">Đơn của tôi</h2><section className="surface-box">
      {!data ? <p>Đang tải danh sách...</p> : data.content.length === 0 ? <p>Chưa có đơn của hộ.</p> : <ul className="form-history">{data.content.map((d) => <li key={d.id}><div className="mobile-row-top"><strong>{d.residentName}</strong><ChangeStatusBadge status={d.status} /></div><span>{formatBusinessDate(d.effectiveDate)}{d.endDate ? ` – ${formatBusinessDate(d.endDate)}` : ''} · {d.reason}</span>{d.status === 'REJECTED' && d.decisionNote && <span>Lý do từ chối: {d.decisionNote}</span>}{d.cancellable && <button className="btn btn-secondary" type="button" onClick={() => setCancelId(d.id)}>Hủy đơn</button>}</li>)}</ul>}
      {data && <PaginationBar page={page} pageSize={size} totalItems={data.totalElements} onPageChange={setPage} onPageSizeChange={setSize} />}
    </section>
    <ConfirmDialog open={cancelId != null} title="Hủy đơn tạm vắng?" consequence="Đơn sẽ được lưu với trạng thái Đã hủy." confirmLabel="Hủy đơn" confirmTone="danger" busy={busy} onCancel={() => setCancelId(null)} onConfirm={() => void cancel()}>{error && <p className="field-error" role="alert">{error}</p>}</ConfirmDialog>
  </>;
}
