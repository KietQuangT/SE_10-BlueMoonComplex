import { useEffect, useRef, useState } from 'react';
import { ApiHttpError } from '../../api/client';
import type { Page } from '../../api/billing';
import { campaignAction, listCampaigns, listContributions, listDonors, recordContribution, saveCampaign, voidContribution, type Campaign, type Contribution, type Donor } from '../../api/voluntary';
import { StatusBadge } from '../../components/Badge';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatBusinessDate, formatVndDecimal } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';
import { CampaignCard, campaignStatus } from './CampaignCard';
import { Pager } from './Pager';
import './s08.css';

const blank = { name: '', description: '', opensOn: '', closesOn: '' };
type SavedDraft = {
  username: string; selected: Campaign | null; form: typeof blank; editing: Campaign | null;
  showForm: boolean; donors: Donor[]; donor: string; amount: string; paidOn: string;
  lateReason: string; key: string; locked: boolean; reason: string;
  voidItem: Contribution | null; dialog: 'void' | '';
};
let saved: SavedDraft | null = null;
// Unresolved writes survive route changes without putting donor information in browser storage.
const pendingByAccount = new Map<string, SavedDraft>();

export function ApiVoluntaryPage() {
  const { user, handleCallFailure } = useAppState();
  const admin = user?.role === 'ADMIN';
  const accountant = user?.role === 'ACCOUNTANT';
  const resident = user?.role === 'RESIDENT';
  const draft = (user && pendingByAccount.get(user.username)) || (saved?.username === user?.username ? saved : null);
  const [data, setData] = useState<Page<Campaign> | null>(null);
  const [page, setPage] = useState(0);
  const [generation, setGeneration] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selected, setSelected] = useState<Campaign | null>(() => draft?.selected ?? null);
  const [ledger, setLedger] = useState<Page<Contribution> | null>(null);
  const [ledgerPage, setLedgerPage] = useState(0);
  const [ledgerError, setLedgerError] = useState('');
  const [editing, setEditing] = useState<Campaign | null>(() => draft?.editing ?? null);
  const [showForm, setShowForm] = useState(() => draft?.showForm ?? false);
  const [form, setForm] = useState(() => draft?.form ?? blank);
  const [query, setQuery] = useState('');
  const [searchedQuery, setSearchedQuery] = useState('');
  const [donorPage, setDonorPage] = useState<Page<Donor> | null>(null);
  const [donors, setDonors] = useState<Donor[]>(() => draft?.donors ?? []);
  const [donor, setDonor] = useState(() => draft?.donor ?? '');
  const [amount, setAmount] = useState(() => draft?.amount ?? '');
  const [paidOn, setPaidOn] = useState(() => draft?.paidOn ?? '');
  const [lateReason, setLateReason] = useState(() => draft?.lateReason ?? '');
  const [key, setKey] = useState(() => draft?.key ?? crypto.randomUUID());
  const [locked, setLocked] = useState(() => draft?.locked ?? false);
  const [dialog, setDialog] = useState<'publish' | 'close' | 'void' | ''>(() => draft?.dialog ?? '');
  const [voidItem, setVoidItem] = useState<Contribution | null>(() => draft?.voidItem ?? null);
  const [reason, setReason] = useState(() => draft?.reason ?? '');
  const campaignForm = useRef<HTMLFormElement>(null);
  const detailHeading = useRef<HTMLHeadingElement>(null);
  const failureRef = useRef<(error: unknown) => void>(() => {});
  failureRef.current = failure;

  useEffect(() => {
    let live = true;
    setLoading(true);
    listCampaigns(page).then((value) => {
      if (!live) return;
      setData(value);
      setSelected((current) => current ? value.content.find((item) => item.id === current.id) ?? current : null);
    }).catch((err: unknown) => { if (live) failureRef.current(err); })
      .finally(() => { if (live) setLoading(false); });
    return () => { live = false; };
  }, [page, generation]);

  useEffect(() => {
    setLedger(null);
    setLedgerError('');
    if (!selected || resident) return;
    let live = true;
    listContributions(selected.id, ledgerPage).then((value) => { if (live) setLedger(value); })
      .catch((err: unknown) => {
        if (!live) return;
        // A ledger failure must not be presented as an empty ledger.
        if (err instanceof ApiHttpError && err.problem.status === 401) failureRef.current(err);
        else setLedgerError(handleCallFailure(err));
      });
    return () => { live = false; };
  }, [selected, resident, ledgerPage, generation]);

  useEffect(() => { detailHeading.current?.focus(); }, [selected?.id]);

  function snapshot(pending: boolean): SavedDraft {
    return { username: user!.username, selected, form, editing, showForm, donors, donor, amount, paidOn,
      lateReason, key, locked: pending, reason, voidItem, dialog: dialog === 'void' ? 'void' : '' };
  }
  function failure(err: unknown, pending = locked) {
    if (err instanceof ApiHttpError && err.problem.status === 401 && user) saved = snapshot(pending);
    setError(handleCallFailure(err));
  }
  function reload() { setError(''); setGeneration((n) => n + 1); }
  function clearSaved() { saved = null; }
  function openCreate() {
    setEditing(null); setForm(blank); setError(''); setSuccess(''); setShowForm(true);
  }
  function openCampaign(campaign: Campaign) {
    setLedger(null); setLedgerPage(0); setError(''); setSuccess(''); setSelected(campaign);
    if (!locked) {
      setQuery(''); setSearchedQuery(''); setDonorPage(null); setDonors([]);
      setDonor(''); setAmount(''); setPaidOn(''); setLateReason(''); setKey(crypto.randomUUID());
    }
  }
  async function searchPage(value: string, nextPage: number) {
    if (busy || locked) return;
    setBusy(true); setError('');
    try {
      const result = await listDonors(value, nextPage);
      setDonors(result.content); setDonorPage(result); setSearchedQuery(value); setDonor('');
    } catch (err) { failure(err); } finally { setBusy(false); }
  }
  function search(event: React.FormEvent) {
    event.preventDefault();
    if (query.trim().length < 2) { setError('Nhập ít nhất 2 ký tự để tìm nhân khẩu.'); return; }
    void searchPage(query.trim(), 0);
  }
  async function save(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    if (!event.currentTarget.checkValidity() || !form.name.trim() || !form.opensOn || !form.closesOn || form.closesOn < form.opensOn) {
      setError('Kiểm tra tên đợt và ngày kết thúc không trước ngày bắt đầu.'); return;
    }
    setBusy(true); setError(''); setSuccess('');
    try {
      const result = await saveCampaign({ ...form, name: form.name.trim() }, editing ?? undefined);
      setShowForm(false); setEditing(null); setForm(blank); setSelected(result); setLedgerPage(0);
      clearSaved(); setSuccess(`Đã lưu nháp “${result.name}”. Chọn Công bố khi đã kiểm tra nội dung.`);
      setGeneration((n) => n + 1);
    } catch (err) { failure(err); } finally { setBusy(false); }
  }
  async function record(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selected || busy) return;
    if (!donor || !/^[1-9]\d{0,17}$/.test(amount) || !paidOn || !event.currentTarget.checkValidity()) {
      setError('Chọn người đóng, nhập số tiền nguyên dương tối đa 18 chữ số và ngày hợp lệ.'); return;
    }
    setBusy(true); setLocked(true); setError(''); setSuccess('');
    if (user) pendingByAccount.set(user.username, snapshot(true));
    try {
      const result = await recordContribution(selected.id, { residentId: Number(donor), amount, paidOn, lateReason }, key);
      setDonor(''); setAmount(''); setPaidOn(''); setLateReason(''); setKey(crypto.randomUUID()); setLocked(false);
      if (user) pendingByAccount.delete(user.username);
      clearSaved(); setLedgerPage(0);
      setSuccess(result.status === 'VOIDED' ? 'Khoản đóng góp này đã được ghi trước đó và đã hủy. Sổ đóng góp giữ nguyên lịch sử.' : 'Đã ghi nhận khoản đóng góp.');
      setGeneration((n) => n + 1);
    } catch (err) {
      failure(err, true);
      if (err instanceof ApiHttpError && (err.problem.status === 400 ||
        (err.problem.status === 409 && ['INVALID_PAID_DATE', 'CAMPAIGN_NOT_RECEIVABLE'].includes(err.problem.code ?? '')) ||
        (err.problem.status === 404 && err.problem.code === 'RESIDENT_NOT_FOUND'))) {
        if (user) pendingByAccount.delete(user.username);
        clearSaved(); setKey(crypto.randomUUID()); setLocked(false);
      }
    } finally { setBusy(false); }
  }
  async function confirm() {
    if (!selected || !dialog || busy) return;
    if (dialog === 'void' && !reason.trim()) { setError('Nhập lý do hủy khoản đóng góp.'); return; }
    setBusy(true); setError(''); setSuccess('');
    try {
      if (dialog === 'void' && voidItem) {
        await voidContribution(voidItem, reason.trim());
        setSuccess('Đã hủy khoản đóng góp. Bản ghi được giữ trong sổ, không tính vào tổng thu.');
      } else if (dialog === 'publish' || dialog === 'close') {
        const result = await campaignAction(selected, dialog);
        setSelected(result);
        setSuccess(dialog === 'publish' ? 'Đã công bố đợt vận động cho cư dân.' : 'Đã đóng đợt vận động. Kế toán vẫn có thể ghi nhận muộn tiền đã thu hợp lệ.');
      }
      clearSaved(); setDialog(''); setVoidItem(null); setReason(''); setGeneration((n) => n + 1);
    } catch (err) { failure(err); } finally { setBusy(false); }
  }

  const modalError = (showForm || dialog) && error;
  const ended = selected && campaignStatus(selected) === 'Đã kết thúc';
  const today = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date());
  const voidTitle = voidItem ? `Hủy khoản đóng góp của ${voidItem.donorNameSnapshot}?` : 'Hủy khoản đóng góp';
  const actionTitle = dialog === 'void' ? voidTitle : `${dialog === 'publish' ? 'Công bố' : 'Đóng'} đợt “${selected?.name ?? ''}”?`;
  const consequence = dialog === 'void' && voidItem
    ? `${formatVndDecimal(voidItem.amount)} · Hộ ${voidItem.householdCodeSnapshot} · Ngày nhận ${formatBusinessDate(voidItem.paidOn)}. Bản ghi vẫn được lưu nhưng không tính vào tổng thu; thao tác này không hoàn tiền.`
    : dialog === 'publish' ? 'Cư dân sẽ xem được đợt vận động này. Sau khi công bố, nội dung và thời gian không thể sửa.'
      : 'Đợt vận động sẽ kết thúc. Chỉ được ghi nhận muộn khoản tiền đã thu trong thời gian hợp lệ, kèm lý do.';

  return <div className="wayfinding-page s08-page">
    <ScreenHeader screenCode={resident ? 'M-34' : 'M-17'} contract="api-live"
      title={admin ? 'Quản lý đợt vận động tự nguyện' : 'Đóng góp tự nguyện'}
      description={resident ? 'Các đợt vận động Ban quản trị đã công bố. Đóng góp bằng tiền mặt tại Văn phòng Ban quản lý.' : 'Tạo, công bố và theo dõi các đợt vận động trong chung cư.'}
      actions={admin ? <button type="button" className="btn btn-primary" disabled={busy} onClick={openCreate}>Tạo đợt vận động mới</button> : undefined} />

    <aside className="notice-box notice-warning s08-voluntary-note" aria-label="Quy tắc đóng góp tự nguyện">
      <strong>Đóng góp tự nguyện, không tính vào công nợ</strong>
      <p>Các khoản ủng hộ được theo dõi riêng, không cộng vào khoản phải đóng của hộ gia đình.</p>
    </aside>
    {success && <div className="notice-box notice-success" role="status">{success}</div>}
    {error && !modalError && data && <div className="notice-box notice-warning s08-feedback" role="alert"><p>{error}</p><button type="button" className="btn btn-secondary" disabled={busy} onClick={reload}>Tải lại</button></div>}

    {loading ? <SkeletonRows /> : !data ? <ErrorRetryPanel message={error} onRetry={reload} /> : data.content.length === 0
      ? <EmptyStatePanel title="Chưa có đợt đóng góp" description={admin ? 'Chọn Tạo đợt vận động mới để lập nháp. Chỉ khi công bố, cư dân mới xem được.' : 'Khi Ban quản trị công bố đợt vận động, thông tin sẽ hiển thị tại đây.'} />
      : <section aria-label="Danh sách đợt vận động" className="s08-campaign-list">
        {data.content.map((campaign) => <CampaignCard key={campaign.id} campaign={campaign} resident={resident}
          selected={selected?.id === campaign.id} disabled={busy || locked} onSelect={() => openCampaign(campaign)} />)}
        <Pager page={page} totalPages={data.totalPages} onPageChange={setPage} label="Phân trang đợt vận động" disabled={busy || locked} />
      </section>}

    {selected && !resident && <section id="campaign-details" className="surface-box s08-details" aria-labelledby="campaign-detail-title">
      <div className="box-toolbar">
        <div><h2 id="campaign-detail-title" ref={detailHeading} tabIndex={-1} className="box-title">Chi tiết: {selected.name}</h2><p className="field-hint">{campaignStatus(selected)} · {formatBusinessDate(selected.opensOn)} – {formatBusinessDate(selected.closesOn)}</p></div>
        <button type="button" className="btn btn-ghost" disabled={busy || locked} onClick={() => {
          clearSaved(); setSelected(null); document.getElementById(`campaign-${selected.id}`)?.focus();
        }}>Đóng chi tiết</button>
      </div>
      {admin && selected.status !== 'CLOSED' && <div className="s08-section s08-actions">
        {selected.status === 'DRAFT' ? <>
          <p className="field-hint">Bản nháp chỉ hiển thị với nhân sự. Kiểm tra nội dung trước khi công bố.</p>
          <button type="button" className="btn btn-secondary" disabled={busy} onClick={() => {
            setEditing(selected); setForm({ name: selected.name, description: selected.description ?? '', opensOn: selected.opensOn, closesOn: selected.closesOn }); setError(''); setShowForm(true);
          }}>Sửa nháp</button>
          <button type="button" className="btn btn-primary" disabled={busy} onClick={() => { setError(''); setDialog('publish'); }}>Công bố</button>
        </> : <><p className="field-hint">Nội dung và thời gian đã công bố được giữ nguyên.</p><button type="button" className="btn btn-secondary" disabled={busy} onClick={() => { setError(''); setDialog('close'); }}>Đóng đợt</button></>}
      </div>}

      {accountant && (selected.status !== 'DRAFT' || locked) && <section className="s08-section" aria-labelledby="contribution-entry-title">
        <h3 id="contribution-entry-title" className="box-title">Ghi nhận đóng góp</h3>
        <p className="s08-description">Chọn người đóng có hồ sơ, nhập số tiền và ngày thực nhận. Mỗi lần nộp tiền được ghi riêng.</p>
        {ended && <p id="late-reason-hint" className="notice-box notice-warning">Đợt đã kết thúc. Chỉ ghi tiền đã nhận trong thời gian mở đợt; cần lý do nhập muộn.</p>}
        <form className="s08-donor-search" onSubmit={search}>
          <label className="form-field">Tìm người đóng<input className="input" value={query} minLength={2} aria-describedby="donor-search-hint" onChange={(e) => setQuery(e.target.value)} disabled={busy || locked} /></label>
          <button type="submit" className="btn btn-secondary" disabled={busy || locked}>Tìm</button>
          <p id="donor-search-hint" className="field-hint">Nhập ít nhất 2 ký tự trong tên; đối chiếu mã hộ và căn hộ để chọn đúng người.</p>
        </form>
        {donorPage && !locked && <>
          {donors.length === 0 && <p role="status">Không tìm thấy người phù hợp. Kiểm tra tên rồi tìm lại.</p>}
          <Pager page={donorPage.page} totalPages={donorPage.totalPages} onPageChange={(p) => void searchPage(searchedQuery, p)} label="Phân trang người đóng" disabled={busy} />
        </>}
        <form onSubmit={record}>
          <fieldset className="form-grid s08-fieldset" disabled={busy || locked}>
            <legend className="sr-only">Thông tin khoản đóng góp</legend>
            <label className="form-field span-2">Người đóng<select className="select" required value={donor} onChange={(e) => setDonor(e.target.value)}><option value="">Chọn người đóng</option>{donors.map((d) => <option key={d.id} value={d.id}>{d.fullName} · {d.householdCode} · {d.apartmentCode}</option>)}</select></label>
            <label className="form-field">Số tiền (đồng)<input className="input" inputMode="numeric" pattern="[1-9][0-9]{0,17}" maxLength={18} required value={amount} onChange={(e) => setAmount(e.target.value)} /></label>
            <label className="form-field">Ngày nhận tiền<input className="input" type="date" required min={selected.opensOn} max={selected.closesOn < today ? selected.closesOn : today} value={paidOn} onChange={(e) => setPaidOn(e.target.value)} /></label>
            <label className="form-field span-2">{ended ? 'Lý do nhập muộn' : 'Lý do nhập muộn (nếu có)'}<textarea className="input" rows={2} maxLength={255} required={!!ended} aria-describedby={ended ? 'late-reason-hint' : undefined} value={lateReason} onChange={(e) => setLateReason(e.target.value)} /></label>
          </fieldset>
          {locked && <p className="notice-box notice-warning" role="status">Chưa xác nhận được kết quả. Thử lại cùng nội dung để tránh ghi trùng. Nội dung đang được giữ nguyên.</p>}
          <div className="s08-actions"><button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Đang xử lý…' : locked ? 'Thử ghi lại' : 'Ghi đóng góp'}</button></div>
        </form>
      </section>}
      {accountant && selected.status === 'DRAFT' && !locked && <p className="s08-section field-hint">Đợt chưa công bố nên chưa thể ghi nhận đóng góp.</p>}

      <section className="s08-ledger" aria-labelledby="contribution-ledger-title">
        <div className="s08-section"><h3 id="contribution-ledger-title" className="box-title">Sổ đóng góp</h3><p className="field-hint">Khoản đã hủy vẫn lưu lịch sử và không tính vào tổng thu.</p></div>
        {ledgerError ? <ErrorRetryPanel message={ledgerError} onRetry={() => setGeneration((n) => n + 1)} /> : !ledger ? <div className="s08-section"><p role="status">Đang tải sổ đóng góp…</p><SkeletonRows rows={2} /></div>
          : ledger.content.length === 0 ? <p className="s08-section">Chưa có khoản đóng góp.</p>
            : <div className="table-scroll"><table className="ledger-table"><caption className="sr-only">Khoản đóng góp cho {selected.name}</caption>
              <thead><tr><th scope="col">Người đóng / Hộ</th><th scope="col">Ngày nhận tiền</th><th scope="col" className="align-right">Số tiền</th><th scope="col">Trạng thái</th>{accountant && <th scope="col">Thao tác</th>}</tr></thead>
              <tbody>{ledger.content.map((item) => <tr key={item.id}>
                <td data-label="Người đóng / Hộ"><div><strong>{item.donorNameSnapshot}</strong><p className="field-hint">{item.householdCodeSnapshot}</p></div></td>
                <td data-label="Ngày nhận tiền" className="tabular">{formatBusinessDate(item.paidOn)}</td>
                <td data-label="Số tiền" className="align-right tabular">{formatVndDecimal(item.amount)}</td>
                <td data-label="Trạng thái"><div><StatusBadge tone={item.status === 'VOIDED' ? 'neutral' : 'paid'} label={item.status === 'VOIDED' ? 'Đã hủy' : 'Đã ghi'} />{item.voidReason && <p className="field-hint">Lý do hủy: {item.voidReason}</p>}</div></td>
                {accountant && <td data-label="Thao tác">{item.status === 'POSTED' ? <button type="button" className="btn btn-secondary" disabled={busy || locked} aria-label={`Hủy khoản đóng góp của ${item.donorNameSnapshot}`} onClick={() => { setVoidItem(item); setReason(''); setError(''); setDialog('void'); }}>Hủy khoản đóng góp</button> : '—'}</td>}
              </tr>)}</tbody>
            </table></div>}
        {ledger && <Pager page={ledgerPage} totalPages={ledger.totalPages} onPageChange={setLedgerPage} label="Phân trang sổ đóng góp" disabled={busy} />}
      </section>
    </section>}

    <ConfirmDialog open={showForm} title={editing ? 'Sửa đợt nháp' : 'Tạo đợt vận động mới'} consequence="Lưu nháp để kiểm tra trước. Cư dân chỉ xem được sau khi bạn công bố." confirmLabel="Lưu nháp" busy={busy}
      onConfirm={() => campaignForm.current?.requestSubmit()} onCancel={() => { clearSaved(); setShowForm(false); setError(''); }}>
      <form ref={campaignForm} className="stack-form" onSubmit={save}>
        {modalError && !dialog && <p role="alert" className="field-error">{error}</p>}
        <fieldset className="stack-form s08-fieldset" disabled={busy}>
          <legend className="sr-only">Thông tin đợt vận động</legend>
          <label className="form-field">Tên đợt<input className="input" required maxLength={150} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
          <label className="form-field">Mô tả<textarea className="input" rows={3} maxLength={2000} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
          <div className="form-grid">
            <label className="form-field">Ngày bắt đầu<input className="input" type="date" required max={form.closesOn || undefined} value={form.opensOn} onChange={(e) => setForm({ ...form, opensOn: e.target.value })} /></label>
            <label className="form-field">Ngày kết thúc<input className="input" type="date" required min={form.opensOn || undefined} value={form.closesOn} onChange={(e) => setForm({ ...form, closesOn: e.target.value })} /></label>
          </div>
        </fieldset>
        <button type="submit" hidden>Lưu nháp</button>
      </form>
    </ConfirmDialog>
    <ConfirmDialog open={!!dialog} title={actionTitle} consequence={consequence}
      confirmLabel={dialog === 'void' ? 'Hủy khoản đóng góp' : dialog === 'publish' ? 'Công bố' : 'Đóng đợt'} confirmTone={dialog === 'void' ? 'danger' : 'primary'} busy={busy}
      onConfirm={() => void confirm()} onCancel={() => { clearSaved(); setDialog(''); setError(''); }}>
      {error && <p role="alert" className="field-error">{error}</p>}
      {dialog === 'void' && <label className="form-field">Lý do<textarea className="input" rows={3} required maxLength={255} value={reason} onChange={(e) => setReason(e.target.value)} /></label>}
    </ConfirmDialog>
  </div>;
}
