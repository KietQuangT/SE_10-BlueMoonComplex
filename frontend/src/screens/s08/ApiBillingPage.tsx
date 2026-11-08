import { useEffect, useRef, useState } from 'react';
import { ApiHttpError } from '../../api/client';
import { createPeriod, listBillingPeriods, periodAction, periodPreview, periodInvoices, periodSubmission, updatePeriod, type Invoice, type Page, type Period, type Submission } from '../../api/billing';
import { StatusBadge } from '../../components/Badge';
import './s08.css';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { Pager } from './Pager';
import { ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatBusinessDate, formatVndDecimal } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

const status: Record<string, string> = { DRAFT: 'Nháp', PENDING_APPROVAL: 'Chờ duyệt', ISSUED: 'Đã phát hành', CANCELLED: 'Đã hủy' };
const actions: Record<string, string> = { SUBMIT: 'Trình duyệt', WITHDRAW: 'Rút trình', RETURN: 'Trả lại', ISSUE: 'Phát hành', CANCEL: 'Hủy kỳ' };
function thisMonth() { return new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit' }).format(new Date()); }
let savedDraft: { username: string; period: string; dueDate: string; selected: Period | null; reason: string; action: string } | null = null;
export function ApiBillingPage() {
  const { user, handleCallFailure } = useAppState();
  const [data, setData] = useState<Page<Period> | null>(null);
  const [page, setPage] = useState(0); const [generation, setGeneration] = useState(0);
  const [loading, setLoading] = useState(true); const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  const [selected, setSelected] = useState<Period | null>(() => savedDraft && savedDraft.username === user?.username ? savedDraft.selected : null); const [preview, setPreview] = useState<Submission | null>(null); const [previewPage, setPreviewPage] = useState(0); const [invoices, setInvoices] = useState<Page<Invoice> | null>(null); const [history, setHistory] = useState(false); const [revision, setRevision] = useState(1);
  const [period, setPeriod] = useState(() => savedDraft && savedDraft.username === user?.username ? savedDraft.period : thisMonth());
  const [dueDate, setDueDate] = useState(() => savedDraft && savedDraft.username === user?.username ? savedDraft.dueDate : '');
  const [action, setAction] = useState(() => savedDraft && savedDraft.username === user?.username ? savedDraft.action : ''); const [reason, setReason] = useState(() => savedDraft && savedDraft.username === user?.username ? savedDraft.reason : '');
  const failureRef = useRef<(error: unknown) => void>(() => {});
  failureRef.current = failure;
  useEffect(() => { let live = true; setLoading(true); setError(''); listBillingPeriods(page).then((v) => { if (live) setData(v); }).catch((e: unknown) => { if (live) failureRef.current(e); }).finally(() => { if (live) setLoading(false); }); return () => { live = false; }; }, [page, generation]);
  useEffect(() => { if (!selected) { setPreview(null); setInvoices(null); return; } let live = true; setPreview(null); setInvoices(null); if (['ISSUED', 'CANCELLED'].includes(selected.status) && !history) { periodInvoices(selected.id, previewPage).then((v) => { if (live) setInvoices(v); }).catch((e: unknown) => { if (live) failureRef.current(e); }); return () => { live = false; }; } const request = history ? periodSubmission(selected.id, revision, previewPage) : periodPreview(selected.id, previewPage); request.then((v) => { if (live) setPreview(v); }).catch((e: unknown) => { if (live) { setPreview(null); failureRef.current(e); } }); return () => { live = false; }; }, [selected, previewPage, history, revision, generation]);
  function failure(e: unknown) { if (e instanceof ApiHttpError && e.problem.code === 'STALE_VERSION') { setSelected(null); setHistory(false); } if (e instanceof ApiHttpError && e.problem.status === 401 && user) savedDraft = { username: user.username, period, dueDate, selected, reason, action: ['RETURN', 'CANCEL'].includes(action) ? action : '' }; if (e instanceof ApiHttpError && e.problem.code === 'BILLING_INPUT_CHANGED') { handleCallFailure(e); setSelected(null); setError('Dữ liệu tính phí đã thay đổi. Kế toán cần rút và trình lại kỳ thu.'); } else setError(handleCallFailure(e)); }
  async function save(e: React.FormEvent<HTMLFormElement>) { e.preventDefault(); if (busy) return; if (!dueDate || !e.currentTarget.checkValidity()) { setError('Ngày hạn nộp chưa hợp lệ. Kiểm tra rồi thử lại.'); return; } setBusy(true); setError(''); try { if (selected && selected.allowedActions.includes('UPDATE')) await updatePeriod(selected.id, dueDate, selected.version); else await createPeriod(period, dueDate); savedDraft = null; setSelected(null); setGeneration((n) => n + 1); } catch (err) { failure(err); } finally { setBusy(false); } }
  async function execute() { if (!selected || busy) return; if (['RETURN', 'CANCEL'].includes(action) && !reason.trim()) { setError('Nhập lý do trước khi xác nhận.'); return; } setBusy(true); setError(''); try { const body = action === 'ISSUE' ? { version: selected.version, revision: selected.latestRevision } : ['RETURN', 'CANCEL'].includes(action) ? { version: selected.version, reason: reason.trim() } : { version: selected.version }; await periodAction(selected.id, action.toLowerCase(), body); savedDraft = null; setAction(''); setReason(''); setSelected(null); setGeneration((n) => n + 1); } catch (e) { failure(e); setAction(''); } finally { setBusy(false); } }
  return <div className="wayfinding-page s08-page"><ScreenHeader screenCode="M-24" contract="api-live" title="Kỳ thu bắt buộc" description="Theo dõi và duyệt kỳ thu theo tháng." />
    {error && data && <div role="alert" className="notice-box notice-warning">{error} <button type="button" className="btn btn-secondary" onClick={() => setGeneration((n) => n + 1)}>Tải lại</button></div>}
    {user?.role === 'ACCOUNTANT' && (!selected || selected.allowedActions.includes('UPDATE')) && (
      <form className="surface-box surface-box-pad stack-form" onSubmit={save}>
        <h2 className="box-title">{selected ? 'Sửa hạn nộp' : 'Lập kỳ thu'}</h2>
        <p className="field-hint">Kỳ thu gồm toàn bộ hộ đang hoạt động, với phí quản lý và phí rác. Kế toán trình, Ban quản trị duyệt phát hành.</p>
        <fieldset className="form-grid s08-fieldset" disabled={busy}>
          <legend className="sr-only">Thông tin kỳ thu</legend>
          <label className="form-field">Tháng thu<input className="input" type="month" value={period} disabled={!!selected} onChange={(e) => setPeriod(e.target.value)} required /></label>
          <label className="form-field">Hạn nộp<input className="input" type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} required /></label>
        </fieldset>
        <div className="s08-actions"><button className="btn btn-primary" disabled={busy}>{busy ? 'Đang lưu…' : selected ? 'Lưu hạn nộp' : 'Tạo kỳ thu'}</button></div>
      </form>
    )}
    {loading ? <SkeletonRows /> : !data ? <ErrorRetryPanel message={error} onRetry={() => setGeneration((n) => n + 1)} /> : data.content.length === 0
      ? <EmptyStatePanel title="Chưa có kỳ thu" description="Kỳ thu được lập sẽ hiển thị tại đây." />
      : <section className="surface-box surface-box-pad" aria-labelledby="period-list-title">
        <h2 id="period-list-title" className="box-title">Danh sách kỳ thu</h2>
        <div className="s08-period-list">{data.content.map((p) => <div key={p.id} className="s08-period-row">
          <button type="button" className="btn btn-secondary" disabled={busy} aria-expanded={selected?.id === p.id} onClick={() => {
            setPreview(null); setHistory(false); setRevision(p.latestRevision || 1); setSelected(p); setDueDate(p.dueDate); setPeriod(p.period); setPreviewPage(0); setError('');
          }}>{p.period}</button>
          <span className="field-hint">Hạn nộp {formatBusinessDate(p.dueDate)}</span>
          <div className="s08-title-row"><StatusBadge label={status[p.status] ?? p.status} tone={p.status === 'ISSUED' ? 'paid' : p.status === 'PENDING_APPROVAL' ? 'pending' : 'neutral'} />{p.expired && <span className="badge badge-neutral">Đã hết tháng · Chỉ xem</span>}</div>
        </div>)}</div>
        <Pager page={page} totalPages={data.totalPages} onPageChange={setPage} label="Phân trang kỳ thu" disabled={busy} />
      </section>}
    {selected && <section className="surface-box surface-box-pad" aria-labelledby="period-detail-title">
      <div className="s08-title-row"><h2 id="period-detail-title" className="box-title">Kỳ {selected.period}</h2><StatusBadge label={status[selected.status]} tone={selected.status === 'ISSUED' ? 'paid' : 'neutral'} /></div>
      <p className="s08-description">Hạn nộp {formatBusinessDate(selected.dueDate)}{selected.expired ? '. Kỳ cũ chỉ tra cứu, không thể thay đổi.' : ''}</p>
      {selected.returnReason && <p className="notice-box notice-warning">Lý do trả lại: {selected.returnReason}</p>}
      {selected.cancelReason && <p className="notice-box notice-warning">Lý do hủy: {selected.cancelReason}</p>}
      {selected.latestRevision > 0 && <div className="s08-actions">
        {!history ? <button type="button" className="btn btn-secondary" disabled={busy} onClick={() => { setHistory(true); setRevision(selected.latestRevision); setPreviewPage(0); }}>Xem bản trình</button>
          : <><label className="form-field s08-revision-field">Bản trình<select className="select" disabled={busy} value={revision} onChange={(e) => { setRevision(Number(e.target.value)); setPreviewPage(0); }}>{Array.from({ length: selected.latestRevision }, (_, i) => <option key={i + 1} value={i + 1}>Bản trình số {i + 1}</option>)}</select></label>
            <button type="button" className="btn btn-secondary" disabled={busy} onClick={() => { setHistory(false); setPreviewPage(0); }}>{['ISSUED', 'CANCELLED'].includes(selected.status) ? 'Xem hóa đơn' : 'Xem hiện tại'}</button></>}
      </div>}
      {preview && <>
        <p className="notice-box notice-info">{preview.householdCount} hộ · {formatVndDecimal(preview.total)}{preview.revision ? ` · Bản trình số ${preview.revision}` : ' · Xem trước theo dữ liệu hiện tại'}</p>
        <div className="s08-billing-items">{preview.households.content.map((h) => <article key={h.householdId}>
          <h3 className="box-title">{h.householdCode} · {h.apartmentCode}</h3>
          <p className="field-hint">Diện tích {h.areaM2} m²</p>
          <dl className="s08-billing-lines">{h.lines.map((line) => <div key={line.feeTypeCode}><dt>{line.feeTypeName}</dt><dd className="tabular">{formatVndDecimal(line.amount)}</dd></div>)}<div><dt><strong>Tổng</strong></dt><dd className="tabular"><strong>{formatVndDecimal(h.total)}</strong></dd></div></dl>
        </article>)}</div>
        <Pager page={previewPage} totalPages={preview.households.totalPages} onPageChange={setPreviewPage} label="Phân trang hộ trong bản trình" disabled={busy} />
      </>}
      {invoices && <>
        <h3 className="box-title">Hóa đơn</h3>
        <div className="s08-billing-items">{invoices.content.map((invoice) => <article key={invoice.id}>
          <h4 className="box-title">{invoice.householdCode} · {invoice.apartmentCode}</h4>
          <StatusBadge label={invoice.status === 'CANCELLED' ? 'Đã hủy' : 'Đã phát hành'} tone={invoice.status === 'CANCELLED' ? 'neutral' : 'paid'} />
          <dl className="s08-billing-lines">{invoice.lines.map((line) => <div key={line.feeTypeCode}><dt>{line.feeTypeName}</dt><dd className="tabular">{formatVndDecimal(line.amount)}</dd></div>)}<div><dt><strong>Tổng</strong></dt><dd className="tabular"><strong>{formatVndDecimal(invoice.total)}</strong></dd></div></dl>
        </article>)}</div>
        <Pager page={previewPage} totalPages={invoices.totalPages} onPageChange={setPreviewPage} label="Phân trang hóa đơn" disabled={busy} />
      </>}
      {!preview && !invoices && !error && <p className="s08-description" role="status">Đang tải chi tiết kỳ thu…</p>}
      <div className="s08-actions">{Object.entries(actions).filter(([key]) => selected.allowedActions.includes(key) && (key !== 'SUBMIT' || (!history && preview?.revision === null && preview.version === selected.version)) && (key !== 'ISSUE' || (preview && preview.revision === selected.latestRevision && preview.version === selected.version))).map(([key, label]) => <button className={key === 'ISSUE' || key === 'SUBMIT' ? 'btn btn-primary' : 'btn btn-secondary'} type="button" disabled={busy} key={key} onClick={() => { setReason(''); setError(''); setAction(key); }}>{label}</button>)}<button type="button" className="btn btn-ghost" disabled={busy} onClick={() => { savedDraft = null; setSelected(null); }}>Đóng chi tiết</button></div>
    </section>}
    <ConfirmDialog open={!!action} title={`${actions[action] ?? ''} kỳ ${selected?.period ?? ''}`} consequence={`${(action === 'SUBMIT' ? preview?.householdCount : selected?.householdCount) ?? 0} hộ, tổng ${formatVndDecimal((action === 'SUBMIT' ? preview?.total : selected?.total) ?? '0')}, bản trình số ${selected?.latestRevision ?? 0}.`} confirmLabel={actions[action] ?? 'Xác nhận'} busy={busy} onConfirm={() => void execute()} onCancel={() => { savedDraft = null; setAction(''); }}>{error && <p role="alert">{error}</p>}{['RETURN', 'CANCEL'].includes(action) && <label className="form-field">Lý do <textarea className="input" maxLength={255} value={reason} onChange={(e) => setReason(e.target.value)} /></label>}</ConfirmDialog>
  </div>;
}
