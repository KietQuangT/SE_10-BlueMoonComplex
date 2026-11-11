import React, { useEffect, useRef, useState } from 'react';
import { approveDeclaration, fetchDeclarations, rejectDeclaration, suggestResidents, type DeclarationDto, type DeclarationPage, type DeclarationStatus, type ResidentSuggestion } from '../../api/residency';
import { ChangeStatusBadge } from '../../components/ResidencyBadge';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { PaginationBar } from '../../components/PaginationBar';
import { ScreenHeader } from '../../components/ScreenHeader';
import { formatBusinessDate } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

type Filter = DeclarationStatus | 'ALL';
export function ApiAbsenceApprovalsPage(): React.ReactElement {
  const { handleCallFailure, refreshAbsenceCount } = useAppState();
  const [filter, setFilter] = useState<Filter>('PENDING');
  const [apartment, setApartment] = useState('');
  const [debouncedApartment, setDebouncedApartment] = useState('');
  const [name, setName] = useState('');
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState<ResidentSuggestion[]>([]);
  const [searching, setSearching] = useState(false);
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(-1);
  const generation = useRef(0);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(10);
  const [data, setData] = useState<DeclarationPage | null>(null);
  const [selected, setSelected] = useState<DeclarationDto | null>(null);
  const [decision, setDecision] = useState<'APPROVED' | 'REJECTED' | null>(null);
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [loadError, setLoadError] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [version, setVersion] = useState(0);
  useEffect(() => { const t = window.setTimeout(() => { setDebouncedApartment(apartment.trim()); setPage(1); }, 300); return () => clearTimeout(t); }, [apartment]);
  useEffect(() => {
    const current = ++generation.current;
    if (name.trim().length < 2 || !open) { setSuggestions([]); setSearching(false); return; }
    setSearching(true);
    const t = window.setTimeout(() => { void suggestResidents(name.trim()).then((items) => { if (current === generation.current) setSuggestions(items); }).catch((err) => { if (current === generation.current) setError(handleCallFailure(err)); }).finally(() => { if (current === generation.current) setSearching(false); }); }, 250);
    return () => { clearTimeout(t); generation.current++; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name, open]);
  useEffect(() => {
    let cancelled = false; setLoading(true); setLoadError('');
    void fetchDeclarations({ ...(filter === 'ALL' ? {} : { status: filter }), apartment: debouncedApartment, q: query, page: page - 1, size }).then((result) => { if (!cancelled) { setData(result); setLoadError(''); } }).catch((err) => { if (!cancelled) setLoadError(handleCallFailure(err)); }).finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter, debouncedApartment, query, page, size, version]);
  const choose = (item: ResidentSuggestion) => { setName(item.fullName); setQuery(item.fullName); setPage(1); setOpen(false); setActive(-1); };
  const confirm = async () => {
    if (!selected || !decision) return;
    setError(''); setLoadError('');
    if (decision === 'REJECTED' && (!reason.trim() || reason.length > 255)) { setError(!reason.trim() ? 'Vui lòng nhập lý do từ chối.' : 'Lý do từ chối tối đa 255 ký tự.'); document.getElementById('decision-reason')?.focus(); return; }
    setBusy(true);
    try { if (decision === 'APPROVED') await approveDeclaration(selected.id); else await rejectDeclaration(selected.id, reason.trim()); setDecision(null); setSelected(null); setReason(''); setVersion((v) => v + 1); await refreshAbsenceCount(); }
    catch (err) { setError(handleCallFailure(err)); setVersion((v) => v + 1); await refreshAbsenceCount(); } finally { setBusy(false); }
  };
  return <>
    <ScreenHeader screenCode="M-16" title="Duyệt khai báo tạm vắng" description="Duyệt đơn và thông báo kết quả trong danh sách của hộ." contract="api-live" tierLabel="Mức B" />
    {error && decision == null && <div className="banner banner-error" role="alert">{error}</div>}
    <section className="surface-box surface-box-pad surface-box-overflow-visible">
      <div className="tab-strip" role="group" aria-label="Lọc trạng thái đơn">{([['PENDING','Chờ duyệt'],['APPROVED','Đã duyệt'],['REJECTED','Từ chối'],['CANCELLED','Đã hủy'],['ALL','Tất cả']] as [Filter,string][]).map(([v,label]) => <button type="button" className={`tab-btn${filter === v ? ' active' : ''}`} aria-pressed={filter === v} key={v} onClick={() => { setError(''); setLoadError(''); setFilter(v); setPage(1); }}>{label}</button>)}</div>
      <div className="filter-row items-start">
        <div className="form-field">
          <label className="field-label" htmlFor="apt-filter">Căn hộ hoặc mã hộ</label>
          <input id="apt-filter" className="input" value={apartment} onChange={(e) => { setError(''); setLoadError(''); setApartment(e.target.value); }} />
        </div>
        <div className="form-field">
          <label className="field-label" htmlFor="name-filter">Tên nhân khẩu</label>
          <div className="suggestion-anchor">
            <input
              id="name-filter"
              className="input"
              role="combobox"
              aria-expanded={open && name.trim().length >= 2}
              aria-controls="resident-suggestions"
              aria-autocomplete="list"
              aria-activedescendant={active >= 0 ? `suggestion-${active}` : undefined}
              value={name}
              onBlur={() => setTimeout(() => setOpen(false), 200)}
              onChange={(e) => {
                generation.current++;
                setSuggestions([]);
                setQuery('');
                setPage(1);
                setName(e.target.value);
                setOpen(true);
                setActive(-1);
                if (e.target.value.trim().length < 2) {
                  setQuery('');
                  setPage(1);
                }
              }}
              onKeyDown={(e) => {
                if (e.key === 'ArrowDown') {
                  e.preventDefault();
                  setOpen(true);
                  setActive((n) => Math.min(n + 1, suggestions.length - 1));
                } else if (e.key === 'ArrowUp') {
                  e.preventDefault();
                  setActive((n) => Math.max(0, n - 1));
                } else if (e.key === 'Enter' && open && active >= 0 && suggestions[active]) {
                  e.preventDefault();
                  choose(suggestions[active]);
                } else if (e.key === 'Enter') {
                  e.preventDefault();
                  if (name.trim().length >= 2) {
                    setQuery(name.trim());
                    setPage(1);
                    setOpen(false);
                    setActive(-1);
                  }
                } else if (e.key === 'Escape') {
                  setOpen(false);
                  setActive(-1);
                }
              }}
            />
            {open && name.trim().length >= 2 && (
              <div className="suggestion-overlay" style={{ position: 'absolute', width: '100%', overflowY: 'auto' }}>
                <div role="status">{searching ? 'Đang tìm...' : suggestions.length === 0 ? 'Không có kết quả' : ''}</div>
                <ul id="resident-suggestions" role="listbox" className="plain-list">
                  {suggestions.map((item, i) => (
                    <li
                      id={`suggestion-${i}`}
                      role="option"
                      aria-selected={active === i}
                      key={item.residentId}
                      className="btn btn-ghost"
                      style={{ minHeight: 44, maxWidth: '100%', whiteSpace: 'normal' }}
                      onMouseDown={(e) => e.preventDefault()}
                      onClick={() => choose(item)}
                    >
                      {item.fullName} · Hộ {item.householdCode} · Căn {item.apartmentCode}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      </div>
      {loading ? <SkeletonRows rows={3} /> : loadError ? <ErrorRetryPanel message={loadError} onRetry={() => setVersion((v) => v + 1)} /> : !data || data.content.length === 0 ? <EmptyStatePanel title="Không có đơn phù hợp." description="Thử đổi trạng thái, căn hộ hoặc tên nhân khẩu để xem các đơn khác." /> : <div className="table-scroll"><table className="ledger-table"><thead><tr><th scope="col">Căn hộ</th><th scope="col">Nhân khẩu</th><th scope="col">Từ ngày</th><th scope="col">Đến ngày</th><th scope="col">Lý do</th><th scope="col">Trạng thái</th><th scope="col">Thao tác</th></tr></thead><tbody>{data.content.map((d) => <tr key={d.id}><td>{d.apartmentCode}</td><td>{d.residentName}</td><td>{formatBusinessDate(d.effectiveDate)}</td><td>{formatBusinessDate(d.endDate)}</td><td>{d.reason}{d.decisionNote && <p>Lý do từ chối: {d.decisionNote}</p>}</td><td><ChangeStatusBadge status={d.status} /></td><td>{d.status === 'PENDING' && <div className="row-actions"><button type="button" className="btn btn-primary btn-sm" onClick={() => { setError(''); setLoadError(''); setSelected(d); setDecision('APPROVED'); }}>Duyệt</button><button type="button" className="btn btn-secondary btn-sm" onClick={() => { setError(''); setLoadError(''); setSelected(d); setReason(''); setDecision('REJECTED'); }}>Từ chối</button></div>}</td></tr>)}</tbody></table></div>}
      {data && !loading && !loadError && <PaginationBar label="Phân trang đơn cần duyệt" page={page} pageSize={size} totalItems={data.totalElements} onPageChange={setPage} onPageSizeChange={setSize} />}
    </section>
    <ConfirmDialog open={decision != null} title={`${decision === 'APPROVED' ? 'Duyệt' : 'Từ chối'} đơn của ${selected?.residentName ?? ''}?`} consequence={decision === 'APPROVED' ? 'Nhân khẩu chuyển sang Tạm vắng ngay.' : 'Lý do từ chối sẽ hiển thị cho hộ.'} confirmLabel={decision === 'APPROVED' ? 'Duyệt đơn' : 'Từ chối đơn'} confirmTone={decision === 'REJECTED' ? 'danger' : 'primary'} busy={busy} onCancel={() => setDecision(null)} onConfirm={() => void confirm()}>{error && <span id="decision-error" className="field-error" role="alert">{error}</span>}{decision === 'REJECTED' && <form noValidate onSubmit={(e) => { e.preventDefault(); void confirm(); }}><div className="form-field"><label htmlFor="decision-reason">Lý do từ chối (bắt buộc)</label><textarea id="decision-reason" aria-describedby={error ? "decision-error" : undefined} className="textarea" value={reason} onChange={(e) => setReason(e.target.value)} /></div></form>}</ConfirmDialog>
  </>;
}
