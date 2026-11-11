import React, { useEffect, useState } from 'react';
import { returnResident } from '../../api/residency';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { Link, useParams } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import { formatAreaM2, formatBusinessDate } from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ContractBadge, StatusBadge } from '../../components/Badge';
import { ChangeStatusBadge, ResidencyBadge, changeTypeLabel } from '../../components/ResidencyBadge';
import { Icon } from '../../components/Icon';
import { ErrorPage } from '../common/ErrorPage';
import { SHOW_DEV_TOOLS } from '../../lib/devTools';

/** M-13 — Chi tiết hộ: thông tin, thành viên, lịch sử biến động, tài khoản gắn hộ (đề xuất). */
export function HouseholdDetailPage(): React.ReactElement {
  const { id } = useParams();
  const { households, residents, residencyChanges, accounts, user, mode, loadHouseholdDetail, handleCallFailure } = useAppState();
  const [returnId, setReturnId] = useState<number | null>(null);
  const [returnError, setReturnError] = useState('');
  const [returnBusy, setReturnBusy] = useState(false);
  useEffect(() => {
    if (mode === 'api' && id) void loadHouseholdDetail(Number(id));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode, id]);
  const household = households.find((h) => String(h.id) === id);
  if (!household) return <ErrorPage code={404} />;

  const members = residents.filter((r) => r.householdId === household.id && r.residencyStatus !== 'Đã chuyển đi');
  const history = residencyChanges.filter((c) => c.householdId === household.id);
  const linkedAccounts = accounts.filter((a) => a.householdId === household.id);
  const nameOf = (rid: number) => residents.find((r) => r.id === rid)?.fullName ?? 'Không rõ';

  return (
    <>
      <Link to="/admin/households" className="btn btn-ghost btn-sm back-link">
        <Icon name="arrow-left" size={16} />
        Về danh sách hộ
      </Link>
      <ScreenHeader
        screenCode="M-13"
        title={`Hộ ${household.householdCode} · Căn ${household.apartmentCode}`}
        contract={mode === 'api' ? 'api-live' : 'db-ready'}
        tierLabel="Mức B"
        actions={
          <>
            <Link to={`/admin/households/${household.id}/edit`} className="btn btn-secondary">
              Sửa thông tin hộ
            </Link>
            <Link to={`/admin/households/${household.id}/residents/new`} className="btn btn-primary">
              <Icon name="plus" size={16} />
              Thêm nhân khẩu
            </Link>
          </>
        }
      />

      <section className="surface-box surface-box-pad">
        <dl className="detail-dl">
          <div>
            <dt>Mã hộ</dt>
            <dd className="tabular">{household.householdCode}</dd>
          </div>
          <div>
            <dt>Căn hộ</dt>
            <dd className="tabular">{household.apartmentCode}</dd>
          </div>
          <div>
            <dt>Diện tích</dt>
            <dd className="tabular">{formatAreaM2(household.areaM2)}</dd>
          </div>
          <div>
            <dt>Địa chỉ</dt>
            <dd>{household.address || '—'}</dd>
          </div>
          <div>
            <dt>Trạng thái</dt>
            <dd>
              {household.status === 'ACTIVE' ? (
                <StatusBadge tone="paid" label="Hoạt động" />
              ) : (
                <StatusBadge tone="neutral" label="Không hoạt động" />
              )}
            </dd>
          </div>
        </dl>
      </section>

      {returnError && returnId == null && <div className="banner banner-error" role="alert">{returnError}</div>}
      <h2 className="section-heading">Thành viên hiện tại ({members.length})</h2><p className="field-hint">Gồm người đang tạm vắng.</p>
      <section className="surface-box">
        {members.length === 0 ? (
          <div className="state-panel">
            <h3>Hộ chưa có nhân khẩu</h3>
            <p>Thêm nhân khẩu đầu tiên cho hộ này.</p>
            <Link to={`/admin/households/${household.id}/residents/new`} className="btn btn-primary">
              Thêm nhân khẩu
            </Link>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="ledger-table">
              <thead>
                <tr>
                  <th scope="col">Họ tên</th>
                  <th scope="col">Quan hệ với chủ hộ</th>
                  <th scope="col">Ngày sinh</th>
                  <th scope="col">Số giấy tờ</th>
                  <th scope="col">Trạng thái cư trú</th>
                  <th scope="col" className="align-right">
                    Thao tác
                  </th>
                </tr>
              </thead>
              <tbody>
                {members.map((m) => (
                  <tr key={m.id}>
                    <td>
                      <strong>{m.fullName}</strong>
                    </td>
                    <td>{m.relationshipToHead}</td>
                    <td className="tabular">{formatBusinessDate(m.dateOfBirth)}</td>
                    <td className="tabular">{m.identityNumberMasked ?? 'Chưa có'}</td>
                    <td>
                      <ResidencyBadge status={m.residencyStatus} />
                    </td>
                    <td className="align-right">
                      {mode === 'api' && user?.role === 'ADMIN' && m.residencyStatus === 'Tạm vắng' && <button type="button" className="btn btn-secondary" onClick={() => { setReturnError(''); setReturnId(m.id); }}>Đã quay lại</button>}
                      <Link to={`/admin/move-outs?household=${household.id}&resident=${m.id}`} className="btn btn-ghost btn-sm">
                        Ghi nhận chuyển đi
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
      {SHOW_DEV_TOOLS && (
        <p className="field-hint">
          Số giấy tờ và số điện thoại mặc định che; quy tắc hiện đầy đủ theo vai trò chờ quyết định.
          Tập giá trị trạng thái cư trú chờ chốt.
        </p>
      )}

      <h2 className="section-heading">Lịch sử biến động</h2>
      <section className="surface-box">
        {history.length === 0 ? (
          <div className="state-panel">
            <h3>Chưa có biến động</h3>
            <p>Khai báo tạm vắng và chuyển đi của hộ sẽ được ghi tại đây.</p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="ledger-table">
              <thead>
                <tr>
                  <th scope="col">Loại</th>
                  <th scope="col">Nhân khẩu</th>
                  <th scope="col">Ngày hiệu lực</th>
                  <th scope="col">Lý do</th>
                  <th scope="col">Đến ngày</th>
                  <th scope="col">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {history.map((c) => (
                  <tr key={c.id}>
                    <td>{changeTypeLabel(c.changeType)}</td>
                    <td>{nameOf(c.residentId)}</td>
                    <td className="tabular">{formatBusinessDate(c.effectiveDate)}</td>
                    <td>{c.reason || '—'}{c.rejectReasonAssumed && <p>Lý do từ chối: {c.rejectReasonAssumed}</p>}</td>
                    <td>{formatBusinessDate(c.endDateAssumed)}</td>
                    <td>
                      <ChangeStatusBadge status={c.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <div className="screen-title-row section-heading-row">
        <h2 className="section-heading">Tài khoản gắn với hộ</h2>
        <ContractBadge kind="assumed" />
      </div>
      <section className="surface-box surface-box-pad">
        {linkedAccounts.length === 0 ? (
          <p className="screen-desc">Hộ chưa có tài khoản Cư dân.</p>
        ) : (
          <ul className="plain-list">
            {linkedAccounts.map((a) => (
              <li key={a.id}>
                <strong>{a.fullName}</strong> · <span className="tabular">{a.username}</span> ·{' '}
                {a.status === 'ACTIVE' ? 'Hoạt động' : 'Vô hiệu'}
              </li>
            ))}
          </ul>
        )}
        <Link to="/admin/accounts" className="btn btn-ghost btn-sm">
          Mở danh sách tài khoản
        </Link>
      </section>
      <ConfirmDialog open={returnId != null} title={`Xác nhận ${members.find((m) => m.id === returnId)?.fullName ?? ''} đã quay lại?`} consequence="Trạng thái nhân khẩu chuyển sang Đang cư trú." confirmLabel="Đã quay lại" busy={returnBusy} onCancel={() => setReturnId(null)} onConfirm={() => { void (async () => {
        if (returnId == null) return;
        setReturnBusy(true); setReturnError('');
        try { await returnResident(returnId); await loadHouseholdDetail(household.id); setReturnId(null); }
        catch (err) { setReturnError(handleCallFailure(err)); await loadHouseholdDetail(household.id); }
        finally { setReturnBusy(false); }
      })(); }}>{returnError && <p className="field-error" role="alert">{returnError}</p>}</ConfirmDialog>
    </>
  );
}
