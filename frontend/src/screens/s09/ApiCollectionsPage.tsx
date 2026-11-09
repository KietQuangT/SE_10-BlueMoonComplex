import { useEffect, useRef, useState, type FormEvent } from 'react';
import { ApiHttpError } from '../../api/client';
import type { Page } from '../../api/billing';
import { allocationHistory, businessDate, businessToday, clearPending, creditBalance, invoiceDetail, invoiceSummary, listPaymentInvoices, loadPending, previewReceipt, receiptHistory, savePending, sendPending, type CreditAllocation, type InvoiceDetail, type PaymentInvoice, type PaymentReceipt, type PendingPayment, type Summary } from '../../api/payments';
import { FormDialog } from '../../components/FormDialog';
import { ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatBusinessDate, formatInstantVn, formatPeriod, formatVnd } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';
import { Pager } from '../s08/Pager';
import '../s08/s08.css';
import './s09.css';

type Action = { kind: 'receipt' } | { kind: 'allocation' } | { kind: 'void'; receipt: PaymentReceipt } | { kind: 'reverse'; allocation: CreditAllocation };
const statusLabel: Record<string, string> = { UNPAID: 'Chưa thanh toán', PARTIALLY_PAID: 'Đã thanh toán một phần', PAID: 'Đã thanh toán', POSTED: 'Đã ghi nhận', VOIDED: 'Đã hủy', ACTIVE: 'Đang hiệu lực', REVERSED: 'Đã đảo' };

export function ApiCollectionsPage() {
  const { user, handleCallFailure } = useAppState();
  const actor = user?.username ?? '';
  const accountant = user?.role === 'ACCOUNTANT';
  const [pending, setPending] = useState<PendingPayment | null>(() => { try { return actor ? loadPending(actor) : null; } catch { return null; } });
  const [storageError, setStorageError] = useState(() => { try { if (actor) loadPending(actor); return ''; } catch { return 'Không đọc được giao dịch đang chờ. Liên hệ quản trị viên để đối soát.'; } });
  const [query, setQuery] = useState(''); const [period, setPeriod] = useState(''); const [status, setStatus] = useState('');
  const [filter, setFilter] = useState({ q: '', period: '', paymentStatus: '', page: 0 });
  const [list, setList] = useState<Page<PaymentInvoice> | null>(null); const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState(''); const [generation, setGeneration] = useState(0);
  const [selected, setSelected] = useState<PaymentInvoice | null>(null);
  const [detail, setDetail] = useState<InvoiceDetail | null>(null); const [summary, setSummary] = useState<Summary | null>(null);
  const [credit, setCredit] = useState<string | null>(null); const [receipts, setReceipts] = useState<Page<PaymentReceipt> | null>(null);
  const [allocations, setAllocations] = useState<Page<CreditAllocation> | null>(null);
  const [receiptPage, setReceiptPage] = useState(0); const [allocationPage, setAllocationPage] = useState(0);
  const [sourceReceipt, setSourceReceipt] = useState<number | null>(null); const [detailError, setDetailError] = useState('');
  const [busy, setBusy] = useState(false); const busyRef = useRef(false);
  const actorRef = useRef(actor); actorRef.current = actor;
  const previousActor = useRef(actor); const requestToken = useRef<symbol | null>(null);
  const [error, setError] = useState(''); const [success, setSuccess] = useState('');
  const [action, setAction] = useState<Action | null>(null); const [amount, setAmount] = useState('');
  const [paidOn, setPaidOn] = useState(businessToday); const [reason, setReason] = useState(''); const [formError, setFormError] = useState('');
  const failureRef = useRef(handleCallFailure); failureRef.current = handleCallFailure;
  const detailHeading = useRef<HTMLHeadingElement>(null);

  useEffect(() => {
    if (previousActor.current === actor) return;
    previousActor.current = actor; requestToken.current = null; busyRef.current = false; setBusy(false);
    setSelected(null); setDetail(null); setSummary(null); setCredit(null); setReceipts(null); setAllocations(null);
    setAction(null); setAmount(''); setReason(''); setError(''); setSuccess(''); setList(null); setListError(''); setDetailError('');
    setQuery(''); setPeriod(''); setStatus(''); setFilter({ q: '', period: '', paymentStatus: '', page: 0 });
    setReceiptPage(0); setAllocationPage(0); setSourceReceipt(null);
    try { setPending(actor ? loadPending(actor) : null); setStorageError(''); }
    catch { setPending(null); setStorageError('Không đọc được giao dịch đang chờ. Liên hệ quản trị viên để đối soát.'); }
  }, [actor]);
  useEffect(() => {
    let live = true; setLoading(true); setListError('');
    listPaymentInvoices(filter).then((page) => { if (live) setList(page); })
      .catch((err: unknown) => { if (live) { setList(null); setListError(failureRef.current(err)); } })
      .finally(() => { if (live) setLoading(false); });
    return () => { live = false; };
  }, [actor, filter, generation]);
  useEffect(() => {
    setDetail(null); setSummary(null); setCredit(null); setReceipts(null); setAllocations(null); setDetailError('');
    if (!selected) return;
    let live = true;
    Promise.all([invoiceDetail(selected.invoiceId), invoiceSummary(selected.invoiceId), creditBalance(selected.householdId),
      receiptHistory(selected.invoiceId, receiptPage), allocationHistory(selected.householdId, sourceReceipt === null
        ? { invoiceId: selected.invoiceId, page: allocationPage } : { receiptId: sourceReceipt, page: allocationPage })])
      .then(([invoice, nextSummary, balance, receiptPageData, allocationPageData]) => {
        if (live) { setDetail(invoice); setSummary(nextSummary); setCredit(balance.balance); setReceipts(receiptPageData); setAllocations(allocationPageData); }
      }).catch((err: unknown) => { if (live) setDetailError(failureRef.current(err)); });
    return () => { live = false; };
  }, [actor, selected, receiptPage, allocationPage, sourceReceipt, generation]);
  useEffect(() => { if (selected) detailHeading.current?.focus(); }, [selected?.invoiceId]);

  function choose(invoice: PaymentInvoice) { setSelected(invoice); setReceiptPage(0); setAllocationPage(0); setSourceReceipt(null); setError(''); setSuccess(''); }
  function start(next: Action) { setAction(next); setAmount(''); setPaidOn(businessToday()); setReason(''); setFormError(''); setError(''); }
  function unknownOutcome(err: unknown) { return !(err instanceof ApiHttpError) || err.problem.status >= 500 || err.problem.status === 401; }
  async function run(command: PendingPayment, fresh: boolean) {
    if (!accountant || !actor || busyRef.current || storageError) return;
    const token = Symbol('payment-request'); requestToken.current = token;
    busyRef.current = true; setBusy(true); setError(''); setSuccess('');
    try {
      if (fresh) { savePending(actor, command); setPending(command); }
      await sendPending(command, actor);
      clearPending(actor);
      if (actorRef.current !== actor) return;
      setPending(null); setAction(null); setSuccess('Đã ghi nhận giao dịch. Dữ liệu đã được tải lại.'); setGeneration((n) => n + 1);
    } catch (err) {
      if (actorRef.current !== actor) {
        if (!unknownOutcome(err)) { try { clearPending(actor); } catch { /* Retain for safe replay. */ } }
        return;
      }
      if (fresh && !loadPendingSafely(actor)) {
        setStorageError('Không lưu được giao dịch chờ trên trình duyệt. Kiểm tra bộ nhớ phiên trước khi thao tác.');
      } else if (unknownOutcome(err)) {
        setAction(null); setPending(command); setError('Chưa xác định được kết quả. Thử gửi lại đúng giao dịch này trước khi thực hiện thao tác khác.');
        if (err instanceof ApiHttpError && err.problem.status === 401) failureRef.current(err);
      } else {
        try { clearPending(actor); setPending(null); } catch { setStorageError('Không xóa được giao dịch đã đối soát khỏi bộ nhớ phiên.'); }
        const code = err instanceof ApiHttpError ? err.problem.code : undefined;
        setError(code === 'RECEIPT_CREDIT_IN_USE' ? 'Số dư từ biên lai đang được sử dụng. Xem và đảo các phân bổ liên quan trước khi hủy.'
          : code === 'IDEMPOTENCY_CONFLICT' ? 'Giao dịch trùng khóa nhưng khác nội dung. Kiểm tra lịch sử trước khi thao tác lại.'
          : code === 'INVALID_AMOUNT' ? 'Số tiền phải là số nguyên dương, tối đa 24 chữ số.' : failureRef.current(err));
        setAction(null); setGeneration((n) => n + 1);
      }
    } finally {
      if (requestToken.current === token) { requestToken.current = null; busyRef.current = false; setBusy(false); }
    }
  }
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!action || !selected || !summary || !detail || credit === null || pending || busy || storageError) return;
    const idempotencyKey = crypto.randomUUID();
    let command: PendingPayment;
    if (action.kind === 'receipt') {
      const split = previewReceipt(summary.remaining, amount);
      if (!split) { setFormError('Nhập số tiền nguyên dương, tối đa 24 chữ số.'); return; }
      if (!paidOn || paidOn < businessDate(detail.issuedAt) || paidOn > businessToday()) { setFormError('Ngày nhận phải từ ngày phát hành đến hôm nay.'); return; }
      if (paidOn < businessToday() && !reason.trim()) { setFormError('Nhập lý do khi ghi nhận ngày trước hôm nay.'); return; }
      command = { kind: 'receipt', url: `/api/v1/invoices/${selected.invoiceId}/receipts`, body: { amount, paidOn, lateReason: paidOn < businessToday() ? reason.trim() : '', idempotencyKey }, label: `Thu tiền căn ${selected.apartmentCode}, kỳ ${formatPeriod(selected.period)}` };
    } else if (action.kind === 'allocation') {
      if (!previewReceipt(summary.remaining, amount) || BigInt(amount) > BigInt(credit) || BigInt(amount) > BigInt(summary.remaining)) { setFormError('Số tiền phải dương và không vượt số dư hộ hoặc công nợ hóa đơn.'); return; }
      command = { kind: 'allocation', url: `/api/v1/households/${selected.householdId}/credit-allocations`, body: { invoiceId: selected.invoiceId, amount, idempotencyKey }, label: `Phân bổ số dư căn ${selected.apartmentCode}, kỳ ${formatPeriod(selected.period)}` };
    } else {
      if (!reason.trim() || reason.trim().length > 255) { setFormError('Nhập lý do, tối đa 255 ký tự.'); return; }
      command = action.kind === 'void'
        ? { kind: 'void', url: `/api/v1/receipts/${action.receipt.receiptId}/void`, body: { reason: reason.trim(), idempotencyKey }, label: `Hủy biên lai ${action.receipt.receiptId}` }
        : { kind: 'reverse', url: `/api/v1/credit-allocations/${action.allocation.allocationId}/reverse`, body: { reason: reason.trim(), idempotencyKey }, label: `Đảo phân bổ ${action.allocation.allocationId}` };
    }
    void run(command, true);
  }
  const split = action?.kind === 'receipt' && summary ? previewReceipt(summary.remaining, amount) : null;
  const title = action?.kind === 'receipt' ? `Ghi nhận tiền căn ${selected?.apartmentCode ?? ''}` : action?.kind === 'allocation' ? `Phân bổ số dư căn ${selected?.apartmentCode ?? ''}` : action?.kind === 'void' ? `Hủy biên lai ${action.receipt.receiptId}` : action?.kind === 'reverse' ? `Đảo phân bổ ${action.allocation.allocationId}` : '';
  const confirmLabel = action?.kind === 'receipt' ? 'Xác nhận ghi thu' : action?.kind === 'allocation' ? 'Xác nhận phân bổ' : action?.kind === 'void' ? 'Xác nhận hủy' : 'Xác nhận đảo';
  const allocationSplit = previewReceipt(summary?.remaining ?? '0', amount);
  const allocationPreview = action?.kind === 'allocation' && allocationSplit && credit !== null
    && BigInt(amount) <= BigInt(credit) && BigInt(amount) <= BigInt(summary?.remaining ?? '0')
    ? { ...allocationSplit, credit: (BigInt(credit) - BigInt(amount)).toString() }
    : null;
  const dialogDescription = action?.kind === 'receipt' || action?.kind === 'allocation'
    ? `Căn ${selected?.apartmentCode ?? ''} · Hộ ${selected?.householdCode ?? ''} · Kỳ ${formatPeriod(selected?.period)}`
    : action?.kind === 'void'
      ? `Biên lai #${action.receipt.receiptId} · ${formatVnd(action.receipt.amount)}. Nhập lý do để hủy.`
      : action?.kind === 'reverse'
        ? `Phân bổ #${action.allocation.allocationId} · ${formatVnd(action.allocation.amount)}. Nhập lý do để đảo.`
        : 'Kiểm tra giao dịch trước khi xác nhận.';
  const blocked = busy || !!pending || !!storageError;

  if (previousActor.current !== actor) return <SkeletonRows rows={3} />;
  return <div className="wayfinding-page s08-page s09-page">
    <ScreenHeader screenCode="M-21" contract="api-live" tierLabel="Mức C" title={accountant ? 'Thu tiền mặt tại quầy' : 'Tra cứu thu tiền'} description="Tra cứu hóa đơn, công nợ và số dư nộp thừa của hộ." />
    {storageError && <div className="notice-box notice-danger" role="alert">{storageError}</div>}
    {error && <div className="notice-box notice-danger" role="alert">{error}</div>}
    {success && <div className="notice-box" role="status">{success}</div>}
    {pending && accountant && <section className="notice-box s09-pending" aria-label="Giao dịch chưa rõ kết quả"><h2>Giao dịch chưa rõ kết quả</h2><p>{pending.label}</p><p>Thử gửi lại cùng nội dung để đối soát trước khi ghi giao dịch khác.</p><button type="button" className="btn btn-primary" disabled={busy} onClick={() => void run(pending, false)}>Thử gửi lại</button></section>}
    <form className="surface-box surface-box-pad s09-filters" onSubmit={(e) => { e.preventDefault(); setFilter({ q: query, period, paymentStatus: status, page: 0 }); }}>
      <label className="form-field">Mã hộ hoặc căn hộ<input className="input" value={query} maxLength={100} onChange={(e) => setQuery(e.target.value)} /></label>
      <label className="form-field">Kỳ thu<input className="input" type="month" value={period} onChange={(e) => setPeriod(e.target.value)} /></label>
      <label className="form-field">Trạng thái<select className="input" value={status} onChange={(e) => setStatus(e.target.value)}><option value="">Còn nợ</option><option value="UNPAID">Chưa thanh toán</option><option value="PARTIALLY_PAID">Thanh toán một phần</option><option value="PAID">Đã thanh toán</option><option value="ALL">Tất cả</option></select></label>
      <button type="submit" className="btn btn-secondary">Tìm kiếm</button>
    </form>
    <section className="surface-box surface-box-pad s09-section" aria-labelledby="s09-list"><h2 id="s09-list">Hóa đơn</h2>
      {loading ? <SkeletonRows rows={3} /> : listError ? <ErrorRetryPanel message={listError} onRetry={() => setGeneration((n) => n + 1)} /> : !list?.content.length ? <EmptyStatePanel title="Không có hóa đơn phù hợp" description="Thay đổi bộ lọc rồi tìm lại." /> : <>
        <div className="table-scroll"><table className="ledger-table"><thead><tr><th>Căn hộ / Mã hộ</th><th>Kỳ</th><th>Tổng tiền</th><th>Đã thanh toán</th><th>Còn nợ</th><th>Trạng thái</th><th>Chi tiết</th></tr></thead><tbody>
          {list.content.map((item) => <tr key={item.invoiceId}><td data-label="Căn hộ / Mã hộ">{item.apartmentCode} / {item.householdCode}</td><td data-label="Kỳ">{formatPeriod(item.period)}</td><td data-label="Tổng tiền">{formatVnd(item.total)}</td><td data-label="Đã thanh toán">{formatVnd(item.settled)}</td><td data-label="Còn nợ">{formatVnd(item.remaining)}</td><td data-label="Trạng thái">{statusLabel[item.paymentStatus]}</td><td data-label="Chi tiết"><button type="button" className="btn btn-secondary btn-sm" onClick={() => choose(item)} aria-label={`Xem ${item.apartmentCode}, kỳ ${formatPeriod(item.period)}`}>Xem</button></td></tr>)}
        </tbody></table></div><Pager page={list.page} totalPages={list.totalPages} onPageChange={(page) => setFilter((f) => ({ ...f, page }))} /></>}
    </section>
    {selected && <section className="surface-box surface-box-pad s09-section" aria-labelledby="s09-detail">
      <h2 id="s09-detail" ref={detailHeading} tabIndex={-1}>Căn {selected.apartmentCode} · {selected.householdCode} · Kỳ {formatPeriod(selected.period)}</h2>
      {detailError ? <ErrorRetryPanel message={detailError} onRetry={() => setGeneration((n) => n + 1)} /> : !detail || !summary || credit === null || !receipts || !allocations ? <SkeletonRows rows={3} /> : <>
        <p>Hạn nộp {formatBusinessDate(selected.dueDate)} · {statusLabel[summary.status] ?? summary.status}</p>
        <dl className="s09-totals"><div><dt>Tổng hóa đơn</dt><dd>{formatVnd(summary.total)}</dd></div><div><dt>Đã thanh toán</dt><dd>{formatVnd(summary.settled)}</dd></div><div><dt>Còn nợ</dt><dd>{formatVnd(summary.remaining)}</dd></div><div><dt>Số dư nộp thừa</dt><dd>{formatVnd(credit)}</dd></div></dl>
        <h3>Chi tiết phí</h3><ul>{detail.lines.map((line) => <li key={line.feeTypeCode}>{line.feeTypeName}: {formatVnd(line.amount)}</li>)}</ul>
        {accountant && <div className="s09-actions"><button type="button" className="btn btn-primary" disabled={blocked} onClick={() => start({ kind: 'receipt' })}>Ghi nhận tiền</button><button type="button" className="btn btn-secondary" disabled={blocked || BigInt(credit) === 0n || BigInt(summary.remaining) === 0n} onClick={() => start({ kind: 'allocation' })}>Phân bổ số dư</button></div>}
        <h3>Lịch sử biên lai</h3>{!receipts.content.length ? <p>Chưa có biên lai.</p> : <div className="table-scroll"><table className="ledger-table"><thead><tr><th>Biên lai</th><th>Ngày nhận</th><th>Số tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{receipts.content.map((item) => <tr key={item.receiptId}><td data-label="Biên lai">#{item.receiptId}<br /><small>Ghi lúc {formatInstantVn(item.recordedAt)} · Người ghi #{item.recordedBy}</small></td><td data-label="Ngày nhận">{formatBusinessDate(item.paidOn)} {item.hasLateReason && '· Có lý do'}</td><td data-label="Số tiền">{formatVnd(item.amount)}<br /><small>Trả nợ {formatVnd(item.appliedAmount)} · Dư {formatVnd(item.creditAmount)}</small></td><td data-label="Trạng thái">{statusLabel[item.status]} {item.hasVoidReason && '· Có lý do'}{item.voidedAt && <small> · {formatInstantVn(item.voidedAt)}</small>}</td><td data-label="Thao tác">{item.activeAllocationCount > 0 && <button type="button" className="btn btn-secondary btn-sm" onClick={() => { setSourceReceipt(item.receiptId); setAllocationPage(0); }}>Xem {item.activeAllocationCount} phân bổ</button>}{accountant && item.status === 'POSTED' && item.canVoid && <button type="button" className="btn btn-secondary btn-sm" disabled={blocked} onClick={() => start({ kind: 'void', receipt: item })}>Hủy biên lai</button>}</td></tr>)}</tbody></table></div>}
        <Pager page={receipts.page} totalPages={receipts.totalPages} label="Trang biên lai" onPageChange={setReceiptPage} />
        <div className="s09-heading-row"><h3>{sourceReceipt === null ? 'Lịch sử phân bổ vào hóa đơn' : `Phân bổ dùng biên lai #${sourceReceipt}`}</h3>{sourceReceipt !== null && <button type="button" className="btn btn-secondary btn-sm" onClick={() => { setSourceReceipt(null); setAllocationPage(0); }}>Xem phân bổ của hóa đơn</button>}</div>
        {!allocations.content.length ? <p>Chưa có phân bổ phù hợp.</p> : <div className="table-scroll"><table className="ledger-table"><thead><tr><th>Phân bổ</th><th>Số tiền</th><th>Nguồn biên lai</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{allocations.content.map((item) => <tr key={item.allocationId}><td data-label="Phân bổ">#{item.allocationId} · Hóa đơn #{item.invoiceId}<br /><small>{formatInstantVn(item.recordedAt)} · Người ghi #{item.recordedBy}</small></td><td data-label="Số tiền">{formatVnd(item.amount)}</td><td data-label="Nguồn biên lai">{item.sources.map((source) => <div key={source.receiptId}>#{source.receiptId}: {formatVnd(source.amount)}</div>)}</td><td data-label="Trạng thái">{statusLabel[item.status]} {item.hasReverseReason && '· Có lý do'}{item.reversedAt && <small> · {formatInstantVn(item.reversedAt)}</small>}</td><td data-label="Thao tác">{accountant && item.status === 'ACTIVE' && <button type="button" className="btn btn-secondary btn-sm" disabled={blocked} onClick={() => start({ kind: 'reverse', allocation: item })}>Đảo phân bổ</button>}</td></tr>)}</tbody></table></div>}
        <Pager page={allocations.page} totalPages={allocations.totalPages} label="Trang phân bổ" onPageChange={setAllocationPage} />
      </>}
    </section>}
    <FormDialog open={!!action} title={title} description={dialogDescription} submitLabel={confirmLabel} busy={busy} onCancel={() => setAction(null)} onSubmit={submit}>
      {formError && <p role="alert" className="notice-box notice-danger">{formError}</p>}
      {(action?.kind === 'receipt' || action?.kind === 'allocation') && <label className="form-field">{action.kind === 'receipt' ? 'Số tiền nhận (đồng)' : 'Số tiền phân bổ (đồng)'}<input className="input" inputMode="numeric" maxLength={24} value={amount} onChange={(e) => setAmount(e.target.value)} /></label>}
      {action?.kind === 'receipt' && <><label className="form-field">Ngày nhận tiền<input className="input" type="date" min={detail ? businessDate(detail.issuedAt) : undefined} max={businessToday()} value={paidOn} onChange={(e) => setPaidOn(e.target.value)} /></label>{paidOn < businessToday() && <label className="form-field">Lý do nhập lùi ngày<textarea className="input" maxLength={255} value={reason} onChange={(e) => setReason(e.target.value)} /></label>}{split && <dl className="s09-preview"><div><dt>Tiền nhận</dt><dd>{formatVnd(amount)}</dd></div><div><dt>Trả hóa đơn</dt><dd>{formatVnd(split.applied)}</dd></div><div><dt>Số dư mới của hộ</dt><dd>{formatVnd(split.credit)}</dd></div><div><dt>Còn nợ sau thu</dt><dd>{formatVnd(split.remaining)}</dd></div></dl>}</>}
      {action?.kind === 'allocation' && <><p>Số dư hiện có {formatVnd(credit)} · Còn nợ {formatVnd(summary?.remaining)}. Nguồn được dùng theo thứ tự biên lai cũ nhất trước.</p>{allocationPreview && <dl className="s09-preview"><div><dt>Phân bổ vào hóa đơn</dt><dd>{formatVnd(amount)}</dd></div><div><dt>Số dư nộp thừa còn lại</dt><dd>{formatVnd(allocationPreview.credit)}</dd></div><div><dt>Còn nợ sau phân bổ</dt><dd>{formatVnd(allocationPreview.remaining)}</dd></div></dl>}</>}
      {(action?.kind === 'void' || action?.kind === 'reverse') && <label className="form-field">Lý do<textarea className="input" maxLength={255} value={reason} onChange={(e) => setReason(e.target.value)} /></label>}
    </FormDialog>
  </div>;
}

function loadPendingSafely(actor: string): PendingPayment | null {
  try { return loadPending(actor); } catch { return null; }
}
