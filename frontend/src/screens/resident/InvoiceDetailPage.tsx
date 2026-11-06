import React from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import {
  formatAreaM2,
  formatBusinessDate,
  formatInstantVn,
  formatPeriod,
  formatVnd,
} from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ContractBadge, StatusBadge } from '../../components/Badge';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { Icon } from '../../components/Icon';
import { ErrorPage } from '../common/ErrorPage';
import type { InvoiceLine } from '../../types/domain';

function quantityLabel(line: InvoiceLine): string {
  return line.unit === 'm2' ? formatAreaM2(line.quantity) : `${line.quantity} hộ`;
}

/** M-33 — Chi tiết hóa đơn (đủ 8 trạng thái). Diện tích và đơn giá là snapshot lúc phát hành. */
export function InvoiceDetailPage(): React.ReactElement {
  const { id } = useParams();
  const { user, invoices, payments, forcedUiState, setForcedUiState } = useAppState();
  const invoice = invoices.find((i) => String(i.id) === id);

  const back = (
    <Link to="/resident/invoices" className="btn btn-ghost btn-sm back-link">
      <Icon name="arrow-left" size={16} />
      Về danh sách hóa đơn
    </Link>
  );

  if (forcedUiState === 'loading') {
    return (
      <>
        {back}
        <SkeletonRows rows={3} />
        <SkeletonRows rows={4} />
      </>
    );
  }
  if (forcedUiState === 'error') {
    return (
      <>
        {back}
        <ErrorRetryPanel requestId="demo-2b81e0" onRetry={() => setForcedUiState('normal')} />
      </>
    );
  }
  if (!invoice) return <ErrorPage code={404} />;
  // Mirrors server-side ownership: another household's invoice is 403, never shown.
  if (invoice.householdId !== user?.householdId) return <ErrorPage code={403} />;

  const lines = forcedUiState === 'empty' ? [] : invoice.lines;
  const paid = invoice.status === 'DA_DONG';
  const receipt = payments.find((p) => p.invoiceId === invoice.id);

  return (
    <>
      {back}
      <ScreenHeader
        screenCode="M-33"
        title={`Hóa đơn kỳ ${formatPeriod(invoice.period)}`}
        description={`Căn ${invoice.apartmentCode} · Phát hành ${formatInstantVn(invoice.issuedAt)} · Hạn nộp ${formatBusinessDate(invoice.dueDate)}`}
        contract="assumed"
        tierLabel="Mức A"
        actions={paid ? <StatusBadge tone="paid" label="Đã đóng" /> : <StatusBadge tone="pending" label="Chưa đóng" />}
      />

      {forcedUiState === 'success' ? (
        <div className="banner banner-success" role="status">
          <div className="banner-content">
            <Icon name="check" />
            <span>Kế toán đã ghi nhận khoản thu. Hóa đơn chuyển sang Đã đóng.</span>
          </div>
        </div>
      ) : null}
      {forcedUiState === 'submitting' ? (
        <div className="banner banner-info" role="status" aria-busy="true">
          <div className="banner-content">
            <Icon name="info" />
            <span>Đang cập nhật trạng thái hóa đơn...</span>
          </div>
        </div>
      ) : null}

      <section className="surface-box" aria-labelledby="lines-title">
        <div className="box-toolbar">
          <h2 id="lines-title" className="box-title">
            Các khoản phí thành phần
          </h2>
          <span className="field-hint">Diện tích và đơn giá áp dụng tại thời điểm phát hành</span>
        </div>
        {lines.length === 0 ? (
          <div className="state-panel">
            <h3>Hóa đơn chưa có khoản phí nào</h3>
            <p>Hóa đơn này chưa có thành phần. Liên hệ Ban quản trị nếu bạn cho rằng đây là sai sót.</p>
          </div>
        ) : (
          <>
            <div className="table-scroll invoice-desktop-table">
              <table className="ledger-table">
                <thead>
                  <tr>
                    <th scope="col">Khoản</th>
                    <th scope="col" className="align-right">
                      Số lượng
                    </th>
                    <th scope="col" className="align-right">
                      Đơn giá
                    </th>
                    <th scope="col" className="align-right">
                      Thành tiền
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {lines.map((line) => (
                    <tr key={line.feeCode}>
                      <td>{line.feeName}</td>
                      <td className="align-right tabular">{quantityLabel(line)}</td>
                      <td className="align-right tabular">{formatVnd(line.unitPrice)}</td>
                      <td className="align-right tabular">{formatVnd(line.amount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <ul className="invoice-mobile-list">
              {lines.map((line) => (
                <li key={line.feeCode} className="invoice-mobile-item">
                  <span className="mobile-row-top">
                    <strong>{line.feeName}</strong>
                    <span className="tabular">{formatVnd(line.amount)}</span>
                  </span>
                  <span className="field-hint tabular">
                    {quantityLabel(line)} × {formatVnd(line.unitPrice)}
                  </span>
                </li>
              ))}
            </ul>
          </>
        )}
        <dl className="totals-dl">
          <div>
            <dt>Tổng cộng</dt>
            <dd className="tabular">{formatVnd(invoice.total)}</dd>
          </div>
          <div>
            <dt>Đã thu</dt>
            <dd className="tabular">{formatVnd(invoice.paid)}</dd>
          </div>
          <div className="totals-due">
            <dt>Còn nợ</dt>
            <dd className="tabular">{formatVnd(invoice.remaining)}</dd>
          </div>
        </dl>
      </section>

      {receipt ? (
        <section className="surface-box surface-box-pad">
          <h2 className="box-title">Biên nhận đã thu</h2>
          <dl className="detail-dl">
            <div>
              <dt>Thời điểm thu</dt>
              <dd className="tabular">{formatInstantVn(receipt.paidAt)}</dd>
            </div>
            <div>
              <dt>Số tiền</dt>
              <dd className="tabular">{formatVnd(receipt.amount)}</dd>
            </div>
            <div>
              <dt>Người thu</dt>
              <dd>{receipt.receivedBy}</dd>
            </div>
          </dl>
        </section>
      ) : (
        <section className="surface-box surface-box-pad how-to-pay">
          <div className="screen-title-row">
            <h2 className="box-title">Cách đóng tiền</h2>
            <ContractBadge kind="assumed" />
          </div>
          <p className="screen-desc">
            Nộp tiền mặt tại Văn phòng Ban quản lý, tầng 1, trong giờ làm việc (giờ cụ thể do Ban quản
            trị cung cấp). Báo mã căn <strong className="tabular">{invoice.apartmentCode}</strong> và kỳ{' '}
            <strong className="tabular">{formatPeriod(invoice.period)}</strong> cho Kế toán.
          </p>
        </section>
      )}
    </>
  );
}
