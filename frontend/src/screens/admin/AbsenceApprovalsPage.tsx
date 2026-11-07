import React, { useMemo, useState } from 'react';
import { ApiAbsenceApprovalsPage } from './ApiAbsenceApprovalsPage';
import { useAppState } from '../../state/AppStateContext';
import { formatBusinessDate } from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ContractBadge } from '../../components/Badge';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { ChangeStatusBadge } from '../../components/ResidencyBadge';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { Icon } from '../../components/Icon';

type StatusFilter = 'PENDING' | 'APPROVED' | 'REJECTED' | 'ALL';

/** M-16 — Duyệt khai báo tạm vắng (mặc định lọc Chờ duyệt). */
export function AbsenceApprovalsPage(): React.ReactElement {
  const { mode } = useAppState();
  return mode === 'api' ? <ApiAbsenceApprovalsPage /> : <DemoAbsenceApprovalsPage />;
}

function DemoAbsenceApprovalsPage(): React.ReactElement {
  const { residencyChanges, residents, households, decideAbsenceDemo, forcedUiState, setForcedUiState } = useAppState();
  const [filter, setFilter] = useState<StatusFilter>('PENDING');
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [rejectError, setRejectError] = useState<string | null>(null);
  const [pending, setPending] = useState<'APPROVED' | 'REJECTED' | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const forms = useMemo(
    () =>
      forcedUiState === 'empty'
        ? []
        : residencyChanges.filter((c) => c.changeType === 'TAM_VANG' && (filter === 'ALL' || c.status === filter)),
    [residencyChanges, filter, forcedUiState],
  );
  const selected = residencyChanges.find((c) => c.id === selectedId);
  const nameOf = (rid: number) => residents.find((r) => r.id === rid)?.fullName ?? 'Không rõ';
  const aptOf = (hid: number) => households.find((h) => h.id === hid)?.apartmentCode ?? '—';

  const header = (
    <ScreenHeader
      screenCode="M-16"
      title="Duyệt khai báo tạm vắng"
      description="Duyệt chuyển nhân khẩu sang Tạm vắng. Hộ thấy kết quả ở mục Đơn của tôi (kênh thông báo khác chờ chốt, Q5)."
      contract="db-ready"
      tierLabel="Mức B"
    />
  );
  if (forcedUiState === 'loading') return <>{header}<SkeletonRows rows={5} /></>;
  if (forcedUiState === 'error') return <>{header}<ErrorRetryPanel onRetry={() => setForcedUiState('normal')} /></>;

  const askReject = () => {
    if (!rejectReason.trim()) {
      setRejectError('Nhập lý do từ chối; cư dân sẽ thấy lý do này.');
      return;
    }
    setRejectError(null);
    setPending('REJECTED');
  };

  const confirmDecision = () => {
    if (!selected || !pending) return;
    decideAbsenceDemo(selected.id, pending, rejectReason.trim());
    setNotice(
      pending === 'APPROVED'
        ? `Đã duyệt. ${nameOf(selected.residentId)} chuyển sang Tạm vắng từ ${formatBusinessDate(selected.effectiveDate)}.`
        : `Đã từ chối đơn của ${nameOf(selected.residentId)}. Lý do đã gửi tới hộ.`,
    );
    setPending(null);
    setSelectedId(null);
    setRejectReason('');
  };

  return (
    <>
      {header}
      {notice ? (
        <div className="banner banner-success" role="status">
          <div className="banner-content">
            <Icon name="check" />
            <span>{notice}</span>
          </div>
        </div>
      ) : null}

      <div className="split-layout">
        <section className="surface-box">
          <div className="box-toolbar">
            <div className="tab-strip" role="group" aria-label="Lọc trạng thái đơn">
              {(
                [
                  ['PENDING', 'Chờ duyệt'],
                  ['APPROVED', 'Đã duyệt'],
                  ['REJECTED', 'Từ chối'],
                  ['ALL', 'Tất cả'],
                ] as [StatusFilter, string][]
              ).map(([value, label]) => (
                <button
                  key={value}
                  type="button"
                  className={`tab-btn${filter === value ? ' active' : ''}`}
                  aria-pressed={filter === value}
                  onClick={() => setFilter(value)}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>
          {forms.length === 0 ? (
            <div className="state-panel">
              <h3>{filter === 'PENDING' ? 'Không có đơn nào chờ duyệt' : 'Không có đơn ở trạng thái này'}</h3>
              <p>Đơn khai báo mới của cư dân sẽ xuất hiện tại đây.</p>
            </div>
          ) : (
            <div className="table-scroll">
              <table className="ledger-table">
                <thead>
                  <tr>
                    <th scope="col">Căn</th>
                    <th scope="col">Người vắng</th>
                    <th scope="col">Từ ngày</th>
                    <th scope="col">
                      Đến ngày <ContractBadge kind="assumed" />
                    </th>
                    <th scope="col">Lý do</th>
                    <th scope="col">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {forms.map((f) => (
                    <tr
                      key={f.id}
                      className={`clickable-row${f.id === selectedId ? ' selected-row' : ''}`}
                      onClick={() => {
                        setSelectedId(f.id);
                        setRejectReason('');
                        setRejectError(null);
                      }}
                      aria-selected={f.id === selectedId}
                    >
                      <td className="tabular">
                        <strong>{aptOf(f.householdId)}</strong>
                      </td>
                      <td>
                        <button type="button" className="link-btn" onClick={() => setSelectedId(f.id)}>
                          {nameOf(f.residentId)}
                        </button>
                      </td>
                      <td className="tabular">{formatBusinessDate(f.effectiveDate)}</td>
                      <td className="tabular">{formatBusinessDate(f.endDateAssumed)}</td>
                      <td>{f.reason}</td>
                      <td>
                        <ChangeStatusBadge status={f.status} />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <aside className="surface-box surface-box-pad detail-panel" aria-label="Chi tiết đơn">
          {!selected ? (
            <p className="screen-desc">Chọn một đơn trong danh sách để xem chi tiết và duyệt.</p>
          ) : (
            <>
              <div className="screen-title-row">
                <h2 className="box-title">Đơn của {nameOf(selected.residentId)}</h2>
                <ChangeStatusBadge status={selected.status} />
              </div>
              <dl className="detail-dl single-col">
                <div>
                  <dt>Căn hộ</dt>
                  <dd className="tabular">{aptOf(selected.householdId)}</dd>
                </div>
                <div>
                  <dt>Khoảng vắng mặt</dt>
                  <dd className="tabular">
                    {formatBusinessDate(selected.effectiveDate)} – {formatBusinessDate(selected.endDateAssumed)}
                  </dd>
                </div>
                <div>
                  <dt>Lý do</dt>
                  <dd>{selected.reason}</dd>
                </div>
                {selected.status === 'REJECTED' ? (
                  <div>
                    <dt>Lý do từ chối</dt>
                    <dd>{selected.rejectReasonAssumed ?? '—'}</dd>
                  </div>
                ) : null}
              </dl>
              {selected.status === 'PENDING' ? (
                <div className="decision-block">
                  <p className="field-hint consequence-line">
                    Duyệt: {nameOf(selected.residentId)} chuyển sang Tạm vắng từ{' '}
                    {formatBusinessDate(selected.effectiveDate)}.
                  </p>
                  <button type="button" className="btn btn-primary btn-block" onClick={() => setPending('APPROVED')}>
                    Duyệt đơn
                  </button>
                  <div className="form-field">
                    <label className="field-label" htmlFor="reject-reason">
                      <span>Lý do từ chối</span>
                      <ContractBadge kind="assumed" />
                    </label>
                    <textarea
                      id="reject-reason"
                      className="textarea"
                      rows={2}
                      maxLength={255}
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                      aria-invalid={Boolean(rejectError)}
                      aria-describedby={rejectError ? 'reject-reason-err' : undefined}
                    />
                    {rejectError ? (
                      <span id="reject-reason-err" className="field-error">
                        {rejectError}
                      </span>
                    ) : null}
                  </div>
                  <button type="button" className="btn btn-secondary btn-block danger-text" onClick={askReject}>
                    Từ chối đơn
                  </button>
                </div>
              ) : null}
            </>
          )}
        </aside>
      </div>

      <ConfirmDialog
        open={Boolean(pending && selected)}
        title={
          pending === 'APPROVED'
            ? `Duyệt đơn tạm vắng của ${selected ? nameOf(selected.residentId) : ''}?`
            : `Từ chối đơn của ${selected ? nameOf(selected.residentId) : ''}?`
        }
        consequence={
          pending === 'APPROVED'
            ? `Trạng thái cư trú chuyển sang Tạm vắng từ ${formatBusinessDate(selected?.effectiveDate)}. Hộ thấy kết quả ở mục Đơn của tôi.`
            : 'Đơn chuyển sang Từ chối và lý do hiển thị cho hộ.'
        }
        confirmLabel={pending === 'APPROVED' ? 'Duyệt đơn' : 'Từ chối đơn'}
        confirmTone={pending === 'REJECTED' ? 'danger' : 'primary'}
        onCancel={() => setPending(null)}
        onConfirm={confirmDecision}
      />
    </>
  );
}
