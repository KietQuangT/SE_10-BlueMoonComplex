import { useEffect, useRef, useState } from 'react';
import { ApiHttpError } from '../../api/client';
import { createFeeRate, deleteFeeRate, fetchFeeRates, fetchFeeTypes, updateFeeRate, type FeeRate, type FeeRatePage, type FeeType } from '../../api/fees';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { PaginationBar } from '../../components/PaginationBar';
import { ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatMonthYear, formatVndDecimal } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

interface Draft { editing: FeeRate | null; feeTypeId: string; month: string; price: string }
let savedDraft: { username: string; draft: Draft } | null = null;

function nextMonth(): string {
  const parts = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit' }).formatToParts(new Date());
  const year = Number(parts.find((p) => p.type === 'year')?.value);
  const month = Number(parts.find((p) => p.type === 'month')?.value);
  return `${year + (month === 12 ? 1 : 0)}-${String(month === 12 ? 1 : month + 1).padStart(2, '0')}`;
}

export function FeeRatesPage() {
  const { mode } = useAppState();
  if (mode === 'demo') return <div className="wayfinding-page">
    <ScreenHeader screenCode="M-20" contract="api-live" title="Đơn giá các loại phí" description="Đơn giá thật chỉ được đọc và thay đổi trong chế độ Backend thật." />
    <EmptyStatePanel title="Đơn giá chưa có bản Demo" description="Chuyển sang Backend thật để xem hoặc cập nhật đơn giá. Demo không gọi dữ liệu trên máy chủ." />
  </div>;
  return <ApiFeeRatesPage />;
}

function ApiFeeRatesPage() {
  const { user, handleCallFailure } = useAppState();
  const canWrite = user?.role === 'ACCOUNTANT';
  const [types, setTypes] = useState<FeeType[]>([]);
  const [data, setData] = useState<FeeRatePage | null>(null);
  const [filter, setFilter] = useState('');
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [version, setVersion] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);
  const [draft, setDraft] = useState<Draft | null>(() => canWrite && savedDraft?.username === user?.username ? savedDraft.draft : null);
  const [deleting, setDeleting] = useState<FeeRate | null>(null);
  const priceRef = useRef<HTMLInputElement>(null);

  useEffect(() => { if (draft) priceRef.current?.focus(); }, [!!draft, draft?.editing?.id]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setLoadError('');
    void Promise.all([fetchFeeTypes(), fetchFeeRates(filter, page - 1, size)])
      .then(([feeTypes, rates]) => {
        if (!cancelled) { setTypes(feeTypes); setData(rates); }
      })
      .catch((err: unknown) => { if (!cancelled) setLoadError(handleCallFailure(err)); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
    // handleCallFailure is recreated by the provider.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter, page, size, version]);

  const changeDraft = (next: Draft | null) => { if (!next) savedDraft = null; setDraft(next); };
  const showError = (err: unknown) => {
    const message = handleCallFailure(err);
    if (err instanceof ApiHttpError && err.problem.code === 'INVALID_DATE') return 'Tháng hiệu lực không hợp lệ. Chọn tháng sau tháng hiện tại.';
    if (err instanceof ApiHttpError && err.problem.code === 'VALIDATION') return 'Đơn giá chưa hợp lệ. Kiểm tra lại số tiền.';
    return message;
  };
  const save = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!draft || busy) return;
    const normalized = draft.price.trim().replace(',', '.');
    if (!/^\d{1,10}(?:\.\d{1,2})?$/.test(normalized) || !/[1-9]/.test(normalized)) {
      setError('Đơn giá phải lớn hơn 0, tối đa 2 chữ số sau dấu phẩy.');
      return;
    }
    if (!draft.editing && !draft.feeTypeId) {
      setError('Chọn loại phí.');
      return;
    }
    if (!draft.editing && (!/^\d{4}-(0[1-9]|1[0-2])$/.test(draft.month) || draft.month < nextMonth())) {
      setError('Tháng hiệu lực không hợp lệ. Chọn tháng sau tháng hiện tại.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      if (draft.editing) {
        await updateFeeRate(draft.editing.id, normalized);
        setSuccess('Đã sửa đơn giá.');
      } else {
        await createFeeRate({ feeTypeId: Number(draft.feeTypeId), unitPrice: normalized, effectiveFrom: `${draft.month}-01` });
        setSuccess(`Đã thêm đơn giá cho ${types.find((t) => String(t.id) === draft.feeTypeId)?.name ?? ''} từ tháng ${formatMonthYear(`${draft.month}-01`)}.`);
      }
      changeDraft(null);
      setVersion((v) => v + 1);
    } catch (err) {
      if (err instanceof ApiHttpError && err.problem.status === 401 && user) savedDraft = { username: user.username, draft };
      setError(showError(err));
    }
    finally { setBusy(false); }
  };
  const remove = async () => {
    if (!deleting || busy) return;
    setBusy(true);
    setError('');
    try {
      await deleteFeeRate(deleting.id);
      setDeleting(null);
      setSuccess('Đã xóa đơn giá.');
      if (data?.content.length === 1 && page > 1) setPage(page - 1);
      else setVersion((v) => v + 1);
    } catch (err) { setError(showError(err)); }
    finally { setBusy(false); }
  };
  return <div className="wayfinding-page">
    <ScreenHeader screenCode="M-20" contract="api-live" tierLabel="Mức C" title="Đơn giá các loại phí" description="Đơn giá áp dụng theo tháng. Đơn giá của tháng hiện tại và các tháng đã qua không sửa được." actions={canWrite ? <button type="button" className="btn btn-primary" onClick={() => { setError(''); setSuccess(''); changeDraft({ editing: null, feeTypeId: types[0] ? String(types[0].id) : '', month: nextMonth(), price: '' }); }}>Thêm đơn giá</button> : undefined} />
    {!canWrite && <div className="banner">Bạn chỉ xem được đơn giá của các loại phí; chỉ Kế toán được thêm, sửa hoặc xóa.</div>}
    {success && <div className="banner" role="status">{success}</div>}
    {error && !draft && !deleting && <div className="banner banner-error" role="alert">{error}</div>}
    {draft && <div className="surface-box surface-box-pad" role="region" aria-label={draft.editing ? 'Sửa đơn giá' : 'Thêm đơn giá'}><h2 className="box-title">{draft.editing ? 'Sửa đơn giá' : 'Thêm đơn giá'}</h2><form className="stack-form" noValidate onSubmit={(e) => void save(e)}>
      {!draft.editing && <><div className="form-field"><label htmlFor="fee-type">Loại phí</label><select id="fee-type" className="select" value={draft.feeTypeId} onChange={(e) => changeDraft({ ...draft, feeTypeId: e.target.value })}><option value="">Chọn loại phí</option>{types.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}</select></div><div className="form-field"><label htmlFor="fee-month">Tháng hiệu lực</label><input id="fee-month" className="input" type="month" min={nextMonth()} value={draft.month} onChange={(e) => changeDraft({ ...draft, month: e.target.value })} /></div></>}
      {draft.editing && <p>{draft.editing.feeTypeName} · {formatMonthYear(draft.editing.effectiveFrom)}</p>}
      <div className="form-field"><label htmlFor="fee-price">Đơn giá</label><input id="fee-price" ref={priceRef} className="input" type="text" inputMode="decimal" value={draft.price} aria-describedby={error ? 'fee-error' : undefined} onChange={(e) => changeDraft({ ...draft, price: e.target.value })} /></div>
      {error && <p id="fee-error" className="field-error" role="alert">{error}</p>}<div className="form-actions"><button type="button" className="btn btn-secondary" disabled={busy} onClick={() => { changeDraft(null); setError(''); }}>Hủy</button><button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Đang lưu...' : 'Lưu'}</button></div>
    </form></div>}
    <section className="surface-box surface-box-pad">
      <div className="form-field"><label htmlFor="fee-filter">Loại phí</label><select id="fee-filter" className="select" value={filter} onChange={(e) => { setFilter(e.target.value); setPage(1); }}><option value="">Tất cả</option>{types.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}</select></div>
      {loading ? <SkeletonRows /> : loadError ? <ErrorRetryPanel message={loadError} onRetry={() => setVersion((v) => v + 1)} /> : !data?.content.length ? <EmptyStatePanel title="Chưa có đơn giá nào." description={canWrite ? 'Kế toán thêm đơn giá để hệ thống tính phí.' : ''} /> : <div className="table-scroll"><table className="ledger-table"><thead><tr><th>Loại phí</th><th>Đơn vị</th><th>Đơn giá</th><th>Hiệu lực từ</th><th>Trạng thái</th><th>Hành động</th></tr></thead><tbody>{data.content.map((rate) => <tr key={rate.id}><td data-label="Loại phí">{rate.feeTypeName}</td><td data-label="Đơn vị">{types.find((t) => t.id === rate.feeTypeId)?.unit ?? '—'}</td><td data-label="Đơn giá">{formatVndDecimal(rate.unitPrice)}</td><td data-label="Hiệu lực từ">{formatMonthYear(rate.effectiveFrom)}</td><td data-label="Trạng thái">{rate.editable ? 'Có thể sửa' : 'Đã khóa'}</td><td data-label="Hành động">{canWrite && rate.editable && <div className="row-actions"><button type="button" className="btn btn-secondary" onClick={() => { setError(''); setSuccess(''); changeDraft({ editing: rate, feeTypeId: String(rate.feeTypeId), month: rate.effectiveFrom.slice(0, 7), price: String(rate.unitPrice) }); }}>Sửa</button><button type="button" className="btn btn-secondary" onClick={() => { setError(''); setSuccess(''); setDeleting(rate); }}>Xóa</button></div>}</td></tr>)}</tbody></table></div>}
      {!loading && !loadError && data && <PaginationBar label="Phân trang đơn giá" page={page} pageSize={size} totalItems={data.totalElements} onPageChange={setPage} onPageSizeChange={setSize} />}
      <p>Ví dụ: 10.000 đ × 50 m² = 500.000 đ cho Phí quản lý.</p>
    </section>
    <ConfirmDialog open={!!deleting} title={`Xóa đơn giá của ${deleting?.feeTypeName ?? ''} từ tháng ${deleting ? formatMonthYear(deleting.effectiveFrom) : ''}?`} consequence="Thao tác này không hoàn tác." confirmLabel="Xóa" confirmTone="danger" busy={busy} onCancel={() => { setDeleting(null); setError(''); }} onConfirm={() => void remove()}>{error && <p role="alert">{error}</p>}</ConfirmDialog>
  </div>;
}
