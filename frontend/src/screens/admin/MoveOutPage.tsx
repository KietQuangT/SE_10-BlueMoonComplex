import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import { formatBusinessDate } from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { ResidencyBadge } from '../../components/ResidencyBadge';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { Icon } from '../../components/Icon';

const todayIso = () => new Date().toISOString().slice(0, 10);

/** M-15 — Ghi nhận chuyển đi (trạng thái, không xóa) và tab Lưu trữ. */
export function MoveOutPage(): React.ReactElement {
  const { residents, households, residencyChanges, recordMoveOut, loadHouseholdDetail, mode, forcedUiState, setForcedUiState } =
    useAppState();
  const live = mode === 'api';
  const [pickHousehold, setPickHousehold] = useState<string>('');
  const [params, setParams] = useSearchParams();
  const tab = params.get('tab') === 'archive' ? 'archive' : 'active';
  const [selectedId, setSelectedId] = useState<string>(params.get('resident') ?? '');
  const [effectiveDate, setEffectiveDate] = useState(todayIso());
  const [reason, setReason] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);
  const [archiveHousehold, setArchiveHousehold] = useState('ALL');

  // api mode: members are loaded per household, so load the one the admin picked.
  useEffect(() => {
    if (live && pickHousehold) void loadHouseholdDetail(Number(pickHousehold));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [live, pickHousehold]);
  useEffect(() => {
    if (live && tab === 'archive' && archiveHousehold !== 'ALL') void loadHouseholdDetail(Number(archiveHousehold));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [live, tab, archiveHousehold]);

  const hhCode = (hid: number) => households.find((h) => h.id === hid)?.householdCode ?? '—';
  const active = residents.filter(
    (r) => r.residencyStatus !== 'Đã chuyển đi' && (!live || String(r.householdId) === pickHousehold),
  );
  const archived = useMemo(
    () =>
      forcedUiState === 'empty'
        ? []
        : residents.filter(
            (r) =>
              r.residencyStatus === 'Đã chuyển đi' &&
              (archiveHousehold === 'ALL' || String(r.householdId) === archiveHousehold),
          ),
    [residents, archiveHousehold, forcedUiState],
  );
  const selected = residents.find((r) => String(r.id) === selectedId);
  const moveOutRecord = (rid: number) =>
    residencyChanges.find((c) => c.residentId === rid && c.changeType === 'CHUYEN_DI');

  const header = (
    <ScreenHeader
      screenCode="M-15"
      title="Chuyển đi và lưu trữ"
      description="Người chuyển đi rời danh sách đang cư trú nhưng hồ sơ và lịch sử vẫn được giữ trong Lưu trữ."
      contract={live ? 'api-live' : 'db-ready'}
      tierLabel="Mức B"
    />
  );
  if (forcedUiState === 'loading') return <>{header}<SkeletonRows rows={5} /></>;
  if (forcedUiState === 'error') return <>{header}<ErrorRetryPanel onRetry={() => setForcedUiState('normal')} /></>;

  const openConfirm = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected) return setError('Chọn nhân khẩu cần ghi nhận.');
    if (!effectiveDate) return setError('Chọn ngày hiệu lực.');
    if (!reason.trim()) return setError('Nhập lý do chuyển đi.');
    setError(null);
    setConfirmOpen(true);
  };

  return (
    <>
      {header}
      <div className="tab-strip page-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'active'}
          className={`tab-btn${tab === 'active' ? ' active' : ''}`}
          onClick={() => setParams({})}
        >
          Ghi nhận chuyển đi
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'archive'}
          className={`tab-btn${tab === 'archive' ? ' active' : ''}`}
          onClick={() => setParams({ tab: 'archive' })}
        >
          Lưu trữ ({residents.filter((r) => r.residencyStatus === 'Đã chuyển đi').length})
        </button>
      </div>

      {notice ? (
        <div className="banner banner-success" role="status">
          <div className="banner-content">
            <Icon name="check" />
            <span>{notice}</span>
          </div>
        </div>
      ) : null}

      {tab === 'active' ? (
        <section className="surface-box surface-box-pad">
          {error ? (
            <div className="banner banner-error" role="alert">
              <div className="banner-content">
                <Icon name="alert" />
                <span>{error}</span>
              </div>
            </div>
          ) : null}
          <form onSubmit={openConfirm} noValidate className="form-grid">
            {live ? (
              <div className="form-field span-2">
                <label className="field-label" htmlFor="mo-household">
                  Hộ
                </label>
                <select
                  id="mo-household"
                  className="select"
                  value={pickHousehold}
                  onChange={(e) => {
                    setPickHousehold(e.target.value);
                    setSelectedId('');
                  }}
                >
                  <option value="">Chọn hộ</option>
                  {households.map((h) => (
                    <option key={h.id} value={h.id}>
                      {h.householdCode} · {h.apartmentCode}
                    </option>
                  ))}
                </select>
              </div>
            ) : null}
            <div className="form-field span-2">
              <label className="field-label" htmlFor="mo-resident">
                Nhân khẩu
              </label>
              <select
                id="mo-resident"
                className="select"
                value={selectedId}
                onChange={(e) => setSelectedId(e.target.value)}
              >
                <option value="">Chọn nhân khẩu đang cư trú</option>
                {active.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.fullName} · {hhCode(r.householdId)} · {r.residencyStatus}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="mo-date">
                Ngày hiệu lực
              </label>
              <input
                id="mo-date"
                type="date"
                className="input"
                value={effectiveDate}
                onChange={(e) => setEffectiveDate(e.target.value)}
              />
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="mo-reason">
                Lý do
              </label>
              <input
                id="mo-reason"
                className="input"
                maxLength={255}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
              />
            </div>
            <div className="form-field span-2 form-actions">
              <button type="submit" className="btn btn-primary">
                Ghi nhận đã chuyển đi
              </button>
            </div>
          </form>
          <p className="field-hint">
            Ghi nhận chuyển đi không tự khóa tài khoản. Nếu cần, thực hiện riêng tại Danh sách tài khoản.
          </p>
        </section>
      ) : (
        <section className="surface-box">
          <div className="box-toolbar">
            <label className="inline-field">
              <span className="field-hint">Hộ</span>
              <select className="select" value={archiveHousehold} onChange={(e) => setArchiveHousehold(e.target.value)}>
                <option value="ALL">Tất cả hộ</option>
                {households.map((h) => (
                  <option key={h.id} value={h.id}>
                    {h.householdCode} · {h.apartmentCode}
                  </option>
                ))}
              </select>
            </label>
          </div>
          {archived.length === 0 ? (
            <div className="state-panel">
              <h3>Chưa có hồ sơ lưu trữ</h3>
              <p>Người đã chuyển đi sẽ xuất hiện tại đây, chỉ để xem.</p>
            </div>
          ) : (
            <div className="table-scroll">
              <table className="ledger-table">
                <thead>
                  <tr>
                    <th scope="col">Họ tên</th>
                    <th scope="col">Hộ</th>
                    <th scope="col">Ngày chuyển đi</th>
                    <th scope="col">Lý do</th>
                    <th scope="col">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {archived.map((r) => {
                    const rec = moveOutRecord(r.id);
                    return (
                      <tr key={r.id}>
                        <td>
                          <strong>{r.fullName}</strong>
                        </td>
                        <td className="tabular">{hhCode(r.householdId)}</td>
                        <td className="tabular">{formatBusinessDate(rec?.effectiveDate)}</td>
                        <td>{rec?.reason || '—'}</td>
                        <td>
                          <ResidencyBadge status={r.residencyStatus} />
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      <ConfirmDialog
        open={confirmOpen}
        title={`Ghi nhận ${selected?.fullName ?? ''} đã chuyển đi?`}
        consequence={`Người này rời danh sách đang cư trú của hộ ${selected ? hhCode(selected.householdId) : ''} từ ${formatBusinessDate(effectiveDate)}. Hồ sơ vẫn được giữ trong Lưu trữ.${live ? ' Các đơn tạm vắng đang chờ duyệt của người này (nếu có) sẽ tự động chuyển sang Đã hủy.' : ''}`}
        confirmLabel="Ghi nhận chuyển đi"
        onCancel={() => setConfirmOpen(false)}
        onConfirm={async () => {
          setConfirmOpen(false);
          if (!selected) return;
          const res = await recordMoveOut(selected.id, effectiveDate, reason.trim());
          if (!res.ok) {
            setNotice(null);
            setError(res.error ?? 'Không ghi nhận được. Vui lòng thử lại.');
            return;
          }
          setError(null);
          setNotice(`Đã ghi nhận ${selected.fullName} chuyển đi. Hồ sơ đã chuyển vào Lưu trữ.`);
          setSelectedId('');
          setReason('');
        }}
      />
    </>
  );
}
