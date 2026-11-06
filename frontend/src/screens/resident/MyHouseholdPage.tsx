import React from 'react';
import { useAppState } from '../../state/AppStateContext';
import { formatAreaM2, formatBusinessDate } from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ContractBadge } from '../../components/Badge';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { ResidencyBadge } from '../../components/ResidencyBadge';

/** M-30 — Hộ gia đình của tôi (chỉ xem; CCCD che 4 số cuối, không hiện SĐT — Q3). */
export function MyHouseholdPage(): React.ReactElement {
  const { user, households, residents, forcedUiState, setForcedUiState } = useAppState();
  const household = households.find((h) => h.id === user?.householdId);
  const members =
    forcedUiState === 'empty'
      ? []
      : residents.filter((r) => r.householdId === user?.householdId && r.residencyStatus !== 'Đã chuyển đi');

  const header = (
    <ScreenHeader
      screenCode="M-30"
      title="Hộ gia đình của tôi"
      description={
        household
          ? `Căn ${household.apartmentCode} · ${formatAreaM2(household.areaM2)} · Mã hộ ${household.householdCode}`
          : undefined
      }
      contract="db-ready"
      tierLabel="Mức A"
    />
  );

  if (forcedUiState === 'loading') {
    return (
      <>
        {header}
        <SkeletonRows rows={4} />
      </>
    );
  }
  if (forcedUiState === 'error') {
    return (
      <>
        {header}
        <ErrorRetryPanel onRetry={() => setForcedUiState('normal')} />
      </>
    );
  }

  return (
    <>
      {header}
      <section className="surface-box" aria-labelledby="members-title">
        <div className="box-toolbar">
          <h2 id="members-title" className="box-title">
            Thành viên đang cư trú ({members.length})
          </h2>
          <span className="field-hint">
            Số giấy tờ che còn 4 số cuối; không hiển thị số điện thoại <ContractBadge kind="pending-rule" />
          </span>
        </div>
        {members.length === 0 ? (
          <div className="state-panel">
            <h3>Chưa có thành viên nào được ghi nhận</h3>
            <p>Hồ sơ nhân khẩu của hộ chưa được Ban quản trị nhập. Liên hệ Văn phòng Ban quản lý.</p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="ledger-table">
              <thead>
                <tr>
                  <th scope="col">Họ tên</th>
                  <th scope="col">Quan hệ với chủ hộ</th>
                  <th scope="col" className="hide-mobile">
                    Ngày sinh
                  </th>
                  <th scope="col" className="hide-mobile">
                    Số giấy tờ
                  </th>
                  <th scope="col">Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {members.map((m) => (
                  <tr key={m.id}>
                    <td>
                      <strong>{m.fullName}</strong>
                    </td>
                    <td>{m.relationshipToHead}</td>
                    <td className="tabular hide-mobile">{formatBusinessDate(m.dateOfBirth)}</td>
                    <td className="tabular hide-mobile">{m.identityNumberMasked ?? 'Chưa có'}</td>
                    <td>
                      <ResidencyBadge status={m.residencyStatus} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
      <p className="screen-desc">Thiếu ai hoặc thông tin chưa đúng? Liên hệ Ban quản trị để cập nhật hồ sơ.</p>
    </>
  );
}
