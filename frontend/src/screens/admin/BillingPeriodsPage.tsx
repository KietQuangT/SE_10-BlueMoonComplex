import { useState } from 'react';
import { ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { StatusBadge } from '../../components/Badge';
import { formatBusinessDate, formatPeriod, formatVnd, sumVndStrings } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

/** Read-only Demo history. Real creation/submission/approval belongs to the S08 API screens. */
export function BillingPeriodsPage() {
  const { invoices, forcedUiState, setForcedUiState } = useAppState();
  const periods = [...new Set(invoices.map((invoice) => invoice.period))].sort().reverse();
  const [period, setPeriod] = useState(() => periods[0] ?? '');
  const shown = forcedUiState === 'empty' ? [] : invoices.filter((invoice) => invoice.period === period);
  const header = <ScreenHeader screenCode="M-24" contract="assumed" title="Kỳ thu bắt buộc"
    description="Xem lịch sử kỳ thu trong Demo. Lập nháp, trình duyệt và phát hành thực hiện ở chế độ Backend thật." />;
  return <div className="wayfinding-page">
    {header}
    <div className="notice-box notice-info mb-6">
      <strong>Kế toán lập nháp và trình duyệt; Ban quản trị duyệt phát hành.</strong>
      <p>Chỉ lập kỳ của tháng hiện tại, mỗi tháng tối đa một kỳ chưa hủy. Kỳ thu gồm toàn bộ hộ hoạt động, với phí quản lý và phí rác.</p>
    </div>
    {forcedUiState === 'loading' ? <SkeletonRows /> : forcedUiState === 'error'
      ? <ErrorRetryPanel onRetry={() => setForcedUiState('normal')} />
      : periods.length === 0 || forcedUiState === 'empty'
        ? <EmptyStatePanel title="Chưa có kỳ thu" description="Kỳ được phát hành sẽ xuất hiện tại đây." />
        : <section className="surface-box" aria-label="Lịch sử kỳ thu Demo">
          <div className="box-toolbar">
            <label className="inline-field">Kỳ thu<select className="select" value={period} onChange={(event) => setPeriod(event.target.value)}>
              {periods.map((value) => <option key={value} value={value}>{formatPeriod(value)}</option>)}
            </select></label>
            <span className="topbar-meta">{shown.length} hóa đơn · Tổng {formatVnd(sumVndStrings(shown.map((invoice) => invoice.total)))}</span>
          </div>
          <div className="table-scroll"><table className="ledger-table">
            <caption className="sr-only">Hóa đơn kỳ {formatPeriod(period)}</caption>
            <thead><tr><th scope="col">Căn hộ</th><th scope="col">Hạn nộp</th><th scope="col" className="align-right">Tổng hóa đơn</th><th scope="col">Thanh toán</th></tr></thead>
            <tbody>{shown.map((invoice) => <tr key={invoice.id}>
              <td className="tabular"><strong>{invoice.apartmentCode}</strong></td>
              <td className="tabular">{formatBusinessDate(invoice.dueDate)}</td>
              <td className="tabular align-right">{formatVnd(invoice.total)}</td>
              <td><StatusBadge tone={invoice.status === 'DA_DONG' ? 'paid' : 'pending'} label={invoice.status === 'DA_DONG' ? 'Đã đóng' : 'Chưa đóng'} /></td>
            </tr>)}</tbody>
          </table></div>
        </section>}
  </div>;
}
