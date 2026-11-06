import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import {
  formatBusinessDate,
  formatInstantVn,
  formatPeriod,
  formatVnd,
  sumVndStrings,
} from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { StatusBadge } from '../../components/Badge';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { Icon } from '../../components/Icon';

type Filter = 'all' | 'CHUA_DONG' | 'DA_DONG';

/** M-32 — Hóa đơn và lịch sử (trang đầu cư dân). */
export function InvoicesPage(): React.ReactElement {
  const { user, invoices, payments, residencyChanges, voluntaryCampaigns, households, forcedUiState, setForcedUiState } =
    useAppState();
  const navigate = useNavigate();
  const [filter, setFilter] = useState<Filter>('all');

  // Server-scoped: resident only ever sees their own household (never a client-sent householdId).
  const household = households.find((h) => h.id === user?.householdId);
  const mine = forcedUiState === 'empty' ? [] : invoices.filter((i) => i.householdId === user?.householdId);
  const unpaid = mine.filter((i) => i.status === 'CHUA_DONG');
  const nearestDue = [...unpaid].sort((a, b) => a.dueDate.localeCompare(b.dueDate))[0];
  const pendingForms = residencyChanges.filter(
    (c) => c.householdId === user?.householdId && c.status === 'PENDING',
  ).length;
  const shown = filter === 'all' ? mine : mine.filter((i) => i.status === filter);
  const myPayments = payments.filter((p) => household && p.apartmentCode === household.apartmentCode);
  const openCampaign = voluntaryCampaigns.find((c) => c.status === 'PUBLISHED');

  const header = (
    <ScreenHeader
      screenCode="M-32"
      title="Hóa đơn và lịch sử đóng phí"
      description="Chỉ hiển thị hóa đơn của hộ bạn. Nộp tiền mặt tại Văn phòng Ban quản lý, tầng 1."
      contract="assumed"
      tierLabel="Mức A"
    />
  );

  if (forcedUiState === 'loading') {
    return (
      <>
        {header}
        <SkeletonRows rows={2} />
        <SkeletonRows rows={5} />
      </>
    );
  }
  if (forcedUiState === 'error') {
    return (
      <>
        {header}
        <ErrorRetryPanel requestId="demo-7f3a9c" onRetry={() => setForcedUiState('normal')} />
      </>
    );
  }

  return (
    <>
      {header}

      <section className="wayfinding-strip" aria-label="Tóm tắt nghĩa vụ của hộ">
        <div className="wayfinding-station">
          <span className="station-title">
            <span className={`station-node${unpaid.length ? ' amber' : ''}`} />
            Còn nợ bắt buộc
          </span>
          <span className={`station-figure${unpaid.length ? ' due' : ''}`}>
            {formatVnd(sumVndStrings(unpaid.map((i) => i.remaining)))}
          </span>
          <span className="station-sub">{unpaid.length} kỳ chưa đóng</span>
        </div>
        <div className="wayfinding-station">
          <span className="station-title">
            <span className={`station-node${nearestDue ? '' : ' neutral'}`} />
            Hạn nộp gần nhất
          </span>
          <span className="station-figure">{nearestDue ? formatBusinessDate(nearestDue.dueDate) : '—'}</span>
          <span className="station-sub">
            {nearestDue ? `Kỳ ${formatPeriod(nearestDue.period)}` : 'Không có kỳ nào đến hạn'}
          </span>
        </div>
        <div className="wayfinding-station">
          <span className="station-title">
            <span className={`station-node${pendingForms ? '' : ' neutral'}`} />
            Đơn đang chờ duyệt
          </span>
          <span className="station-figure">{String(pendingForms).padStart(2, '0')} đơn</span>
          <span className="station-sub">Khai báo tạm vắng của hộ</span>
        </div>
      </section>

      <section className="surface-box" aria-labelledby="invoice-list-title">
        <div className="box-toolbar">
          <h2 id="invoice-list-title" className="box-title">
            Các kỳ hóa đơn
          </h2>
          <div className="tab-strip" role="group" aria-label="Lọc theo trạng thái">
            {(
              [
                ['all', `Tất cả (${mine.length})`],
                ['CHUA_DONG', `Chưa đóng (${unpaid.length})`],
                ['DA_DONG', `Đã đóng (${mine.length - unpaid.length})`],
              ] as [Filter, string][]
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
        {shown.length === 0 ? (
          <div className="state-panel">
            <h3>Chưa có hóa đơn nào</h3>
            <p>
              {mine.length === 0
                ? 'Ban quản trị chưa phát hành hóa đơn cho hộ của bạn. Hóa đơn tháng sẽ xuất hiện tại đây.'
                : 'Không có hóa đơn ở trạng thái này. Chọn "Tất cả" để xem toàn bộ.'}
            </p>
          </div>
        ) : (
          <>
            <div className="table-scroll invoice-desktop-table">
              <table className="ledger-table">
                <thead>
                  <tr>
                    <th scope="col">Kỳ thu</th>
                    <th scope="col">Ngày phát hành</th>
                    <th scope="col">Hạn nộp</th>
                    <th scope="col" className="align-right">
                      Tổng cộng
                    </th>
                    <th scope="col" className="align-right">
                      Còn nợ
                    </th>
                    <th scope="col">Trạng thái</th>
                    <th scope="col">
                      <span className="sr-only">Thao tác</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {shown.map((inv) => (
                    <tr
                      key={inv.id}
                      className="clickable-row"
                      onClick={() => navigate(`/resident/invoices/${inv.id}`)}
                    >
                      <td>
                        <strong className="tabular">{formatPeriod(inv.period)}</strong>
                      </td>
                      <td className="tabular">{formatInstantVn(inv.issuedAt).slice(0, 10)}</td>
                      <td className="tabular">{formatBusinessDate(inv.dueDate)}</td>
                      <td className="align-right tabular">{formatVnd(inv.total)}</td>
                      <td className="align-right tabular">
                        <strong>{formatVnd(inv.remaining)}</strong>
                      </td>
                      <td>
                        {inv.status === 'CHUA_DONG' ? (
                          <StatusBadge tone="pending" label="Chưa đóng" />
                        ) : (
                          <StatusBadge tone="paid" label="Đã đóng" />
                        )}
                      </td>
                      <td className="align-right">
                        <button
                          type="button"
                          className="btn btn-ghost btn-sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/resident/invoices/${inv.id}`);
                          }}
                        >
                          Xem chi tiết
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <ul className="invoice-mobile-list">
              {shown.map((inv) => (
                <li key={inv.id}>
                  <button
                    type="button"
                    className="invoice-mobile-item mobile-row-btn"
                    onClick={() => navigate(`/resident/invoices/${inv.id}`)}
                  >
                    <span className="mobile-row-top">
                      <strong className="tabular">Kỳ {formatPeriod(inv.period)}</strong>
                      {inv.status === 'CHUA_DONG' ? (
                        <StatusBadge tone="pending" label="Chưa đóng" />
                      ) : (
                        <StatusBadge tone="paid" label="Đã đóng" />
                      )}
                    </span>
                    <span className="mobile-row-figure tabular">{formatVnd(inv.remaining)}</span>
                    <span className="field-hint">
                      Hạn nộp {formatBusinessDate(inv.dueDate)} · Tổng {formatVnd(inv.total)}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          </>
        )}
      </section>

      <h2 className="section-heading">Lịch sử đóng phí</h2>
      {myPayments.length === 0 ? (
        <EmptyStatePanel
          title="Chưa có giao dịch"
          description="Khi Kế toán ghi nhận khoản thu tiền mặt của hộ, biên nhận sẽ hiện ở đây."
        />
      ) : (
        <section className="surface-box">
          <div className="table-scroll">
            <table className="ledger-table">
              <thead>
                <tr>
                  <th scope="col">Ngày thu</th>
                  <th scope="col">Kỳ</th>
                  <th scope="col" className="align-right">
                    Số tiền
                  </th>
                  <th scope="col">Người thu</th>
                </tr>
              </thead>
              <tbody>
                {myPayments.map((p) => (
                  <tr key={p.id}>
                    <td className="tabular">{formatInstantVn(p.paidAt)}</td>
                    <td className="tabular">{formatPeriod(p.period)}</td>
                    <td className="align-right tabular">{formatVnd(p.amount)}</td>
                    <td>{p.receivedBy}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {openCampaign ? (
        <div className="banner banner-info">
          <div className="banner-content">
            <Icon name="heart" />
            <span>
              <strong>{openCampaign.name}</strong> đang mở đến {formatBusinessDate(openCampaign.closesOn)}.
              Tự nguyện, không tính vào công nợ.
            </span>
          </div>
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => navigate('/resident/voluntary')}>
            Xem đợt đóng góp
          </button>
        </div>
      ) : null}
    </>
  );
}
