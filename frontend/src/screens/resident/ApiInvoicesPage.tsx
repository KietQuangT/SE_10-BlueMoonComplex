import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  fetchResidentBillingSummary,
  fetchResidentInvoices,
  type InvoiceFilterStatus,
  type PageResponse,
  type ResidentBillingSummary,
  type ResidentInvoiceSummary,
} from '../../api/residentBilling';
import { ApiHttpError } from '../../api/client';
import { messageForError } from '../../api/errors';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import {
  formatBusinessDate,
  formatInstantVn,
  formatPeriod,
  formatVnd,
} from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';
import { Pager } from '../s08/Pager';
import {
  AllocationHistoryBlock,
  ReceiptHistoryBlock,
  ResidentInvoiceStatusBadge,
} from './ResidentBillingHistory';
import './s10.css';

export function ApiInvoicesPage(): React.ReactElement {
  const navigate = useNavigate();
  const { handleCallFailure, user } = useAppState();
  const userKey = `${user?.username ?? ''}:${user?.householdId ?? ''}`;
  const userKeyRef = useRef(userKey);
  userKeyRef.current = userKey;

  const [reloadTrigger, setReloadTrigger] = useState(0);
  const [authError, setAuthError] = useState(false);
  const [forbiddenError, setForbiddenError] = useState<string | null>(null);

  // Billing summary state
  const [summary, setSummary] = useState<ResidentBillingSummary | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(true);
  const [summaryError, setSummaryError] = useState<string | null>(null);

  // Invoice list state
  const [invoicesData, setInvoicesData] = useState<PageResponse<ResidentInvoiceSummary> | null>(null);
  const [invoicesLoading, setInvoicesLoading] = useState(true);
  const [invoicesError, setInvoicesError] = useState<string | null>(null);

  // Filter state
  const [periodInput, setPeriodInput] = useState('');
  const [statusInput, setStatusInput] = useState<InvoiceFilterStatus>('ALL');
  const [appliedPeriod, setAppliedPeriod] = useState('');
  const [appliedStatus, setAppliedStatus] = useState<InvoiceFilterStatus>('ALL');
  const [invoicePage, setInvoicePage] = useState(0);

  const isMountedRef = useRef(true);
  const summaryGenRef = useRef(0);
  const invoiceGenRef = useRef(0);
  const isBlockedRef = useRef(false);

  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
      summaryGenRef.current++;
      invoiceGenRef.current++;
    };
  }, []);

  const handleAuthError = useCallback(
    (err: unknown) => {
      if (!isMountedRef.current) return;
      isBlockedRef.current = true;
      setAuthError(true);
      setSummary(null);
      setInvoicesData(null);
      if (err instanceof ApiHttpError && err.problem.code === 'PASSWORD_CHANGE_REQUIRED') {
        navigate('/profile?tab=password');
        return;
      }
      handleCallFailure(err);
    },
    [handleCallFailure, navigate],
  );

  const handleForbiddenError = useCallback(
    (err: unknown) => {
      if (!isMountedRef.current) return;
      isBlockedRef.current = true;
      setSummary(null);
      setInvoicesData(null);
      if (err instanceof ApiHttpError && err.problem.code === 'PASSWORD_CHANGE_REQUIRED') {
        navigate('/profile?tab=password');
        return;
      }
      setForbiddenError(messageForError(err));
    },
    [navigate],
  );

  const loadSummary = useCallback(async () => {
    const gen = ++summaryGenRef.current;
    const keyAtStart = userKeyRef.current;
    setSummaryLoading(true);
    setSummaryError(null);
    try {
      const res = await fetchResidentBillingSummary();
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || summaryGenRef.current !== gen || isBlockedRef.current) return;
      setSummary(res);
      setSummaryError(null);
    } catch (err: unknown) {
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || summaryGenRef.current !== gen) return;
      if (err instanceof ApiHttpError) {
        if (err.problem.status === 401) {
          handleAuthError(err);
          return;
        }
        if (err.problem.status === 403) {
          handleForbiddenError(err);
          return;
        }
      }
      setSummaryError(messageForError(err));
    } finally {
      if (isMountedRef.current && userKeyRef.current === keyAtStart && summaryGenRef.current === gen) {
        setSummaryLoading(false);
      }
    }
  }, [handleAuthError, handleForbiddenError]);

  const loadInvoices = useCallback(async () => {
    const gen = ++invoiceGenRef.current;
    const keyAtStart = userKeyRef.current;
    setInvoicesLoading(true);
    setInvoicesError(null);
    try {
      const res = await fetchResidentInvoices({
        period: appliedPeriod || undefined,
        paymentStatus: appliedStatus,
        page: invoicePage,
        size: 20,
      });
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || invoiceGenRef.current !== gen || isBlockedRef.current) return;
      setInvoicesData(res);
      setInvoicesError(null);
    } catch (err: unknown) {
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || invoiceGenRef.current !== gen) return;
      if (err instanceof ApiHttpError) {
        if (err.problem.status === 401) {
          handleAuthError(err);
          return;
        }
        if (err.problem.status === 403) {
          handleForbiddenError(err);
          return;
        }
      }
      setInvoicesError(messageForError(err));
    } finally {
      if (isMountedRef.current && userKeyRef.current === keyAtStart && invoiceGenRef.current === gen) {
        setInvoicesLoading(false);
      }
    }
  }, [appliedPeriod, appliedStatus, invoicePage, handleAuthError, handleForbiddenError]);

  useEffect(() => {
    if (!authError && !forbiddenError) {
      void loadSummary();
    }
  }, [loadSummary, reloadTrigger, authError, forbiddenError]);

  useEffect(() => {
    if (!authError && !forbiddenError) {
      void loadInvoices();
    }
  }, [loadInvoices, reloadTrigger, authError, forbiddenError]);

  const handleApplyFilter = () => {
    setAppliedPeriod(periodInput);
    setAppliedStatus(statusInput);
    setInvoicePage(0);
  };

  const handleClearFilter = () => {
    setPeriodInput('');
    setStatusInput('ALL');
    setAppliedPeriod('');
    setAppliedStatus('ALL');
    setInvoicePage(0);
  };

  const handleReloadAll = () => {
    isBlockedRef.current = false;
    setForbiddenError(null);
    setAuthError(false);
    setReloadTrigger((prev) => prev + 1);
  };

  const isFiltered = Boolean(appliedPeriod || appliedStatus !== 'ALL');

  if (authError) {
    return (
      <div className="state-panel" role="alert">
        <h2>Phiên đăng nhập đã hết hạn</h2>
        <p>Vui lòng đăng nhập lại để tiếp tục xem thông tin hóa đơn.</p>
        <button type="button" className="btn btn-primary" onClick={() => navigate('/login')}>
          Đăng nhập lại
        </button>
      </div>
    );
  }

  if (forbiddenError) {
    return (
      <div className="state-panel" role="alert">
        <h2>Không có quyền truy cập</h2>
        <p>{forbiddenError}</p>
      </div>
    );
  }

  return (
    <>
      <div className="s10-toolbar-row">
        <ScreenHeader
          screenCode="M-32"
          title="Hóa đơn và lịch sử đóng phí"
          description="Chỉ hiển thị hóa đơn của hộ bạn. Nộp tiền mặt trực tiếp cho Ban quản lý hoặc Kế toán."
          contract="api-live"
          tierLabel="Mức A"
        />
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={handleReloadAll}
          aria-label="Tải lại toàn bộ dữ liệu"
        >
          Tải lại
        </button>
      </div>

      {/* Summary strip */}
      {summaryLoading ? (
        <SkeletonRows rows={1} />
      ) : summaryError ? (
        <ErrorRetryPanel message={summaryError} onRetry={() => void loadSummary()} />
      ) : summary ? (
        <section className="wayfinding-strip" aria-label="Tóm tắt nghĩa vụ của hộ">
          <div className="wayfinding-station">
            <span className="station-title">
              <span className={`station-node${summary.outstandingInvoiceCount > 0 ? ' amber' : ''}`} />
              Còn nợ bắt buộc
            </span>
            <span className={`station-figure${summary.outstandingInvoiceCount > 0 ? ' due' : ''}`}>
              {formatVnd(summary.totalOutstanding)}
            </span>
            <span className="station-sub">
              {summary.outstandingInvoiceCount} hóa đơn còn nợ
            </span>
          </div>

          <div className="wayfinding-station">
            <span className="station-title">
              <span className={`station-node${summary.nearestDueDate ? '' : ' neutral'}`} />
              Hạn nộp gần nhất
            </span>
            <span className="station-figure">
              {summary.nearestDueDate ? formatBusinessDate(summary.nearestDueDate) : '—'}
            </span>
            <span className="station-sub">
              {summary.nearestDueDate
                ? summary.nearestDuePeriod
                  ? `Kỳ ${formatPeriod(summary.nearestDuePeriod)}`
                  : 'Hạn nộp gần nhất'
                : 'Không có hóa đơn còn nợ'}
            </span>
          </div>

          <div className="wayfinding-station">
            <span className="station-title">
              <span className="station-node neutral" />
              Số dư để bù trừ
            </span>
            <span className="station-figure">{formatVnd(summary.creditBalance)}</span>
            <span className="station-sub">Số dư dùng bù trừ hóa đơn, không tự trừ nợ</span>
          </div>
        </section>
      ) : null}

      {/* Invoice list section */}
      <section className="surface-box" aria-labelledby="invoice-list-title">
        <div className="box-toolbar">
          <h2 id="invoice-list-title" className="box-title">
            Các kỳ hóa đơn
          </h2>
        </div>

        <div className="s10-filter-bar">
          <div className="s10-filter-group">
            <label htmlFor="inv-period">Kỳ thu (tháng)</label>
            <input
              id="inv-period"
              type="month"
              className="input"
              value={periodInput}
              onChange={(e) => setPeriodInput(e.target.value)}
              aria-label="Lọc theo kỳ thu"
            />
          </div>

          <div className="s10-filter-group">
            <label htmlFor="inv-status">Trạng thái thanh toán</label>
            <select
              id="inv-status"
              className="select"
              value={statusInput}
              onChange={(e) => setStatusInput(e.target.value as InvoiceFilterStatus)}
              aria-label="Lọc theo trạng thái thanh toán"
            >
              <option value="ALL">Tất cả</option>
              <option value="OUTSTANDING">Còn nợ</option>
              <option value="UNPAID">Chưa thanh toán</option>
              <option value="PARTIALLY_PAID">Đã thanh toán một phần</option>
              <option value="PAID">Đã thanh toán</option>
              <option value="CANCELLED">Đã hủy</option>
            </select>
          </div>

          <div className="s10-filter-actions">
            <button type="button" className="btn btn-primary" onClick={handleApplyFilter}>
              Áp dụng
            </button>
            <button type="button" className="btn btn-secondary" onClick={handleClearFilter}>
              Xóa lọc
            </button>
          </div>
        </div>

        {invoicesLoading ? (
          <SkeletonRows rows={4} />
        ) : invoicesError ? (
          <ErrorRetryPanel message={invoicesError} onRetry={() => void loadInvoices()} />
        ) : !invoicesData || invoicesData.content.length === 0 ? (
          <div className="state-panel">
            <h3>{isFiltered ? 'Không có hóa đơn phù hợp' : 'Chưa có hóa đơn'}</h3>
            <p>
              {isFiltered
                ? 'Không tìm thấy hóa đơn khớp với bộ lọc đã chọn.'
                : 'BQT chưa phát hành hóa đơn cho hộ của bạn. Hóa đơn tháng sẽ xuất hiện tại đây.'}
            </p>
            {isFiltered && (
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={handleClearFilter}
                style={{ marginTop: '8px' }}
              >
                Xóa lọc
              </button>
            )}
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
                    <th scope="col" className="align-right">Tổng cộng</th>
                    <th scope="col" className="align-right">Còn nợ</th>
                    <th scope="col">Trạng thái</th>
                    <th scope="col">
                      <span className="sr-only">Thao tác</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {invoicesData.content.map((inv) => (
                    <tr
                      key={inv.invoiceId}
                      className="clickable-row"
                      onClick={() => navigate(`/resident/invoices/${inv.invoiceId}`)}
                    >
                      <td>
                        <strong className="tabular">{formatPeriod(inv.period)}</strong>
                      </td>
                      <td className="tabular">
                        {inv.issuedAt ? formatInstantVn(inv.issuedAt).slice(0, 10) : '—'}
                      </td>
                      <td className="tabular">{formatBusinessDate(inv.dueDate)}</td>
                      <td className="align-right tabular">{formatVnd(inv.total)}</td>
                      <td className="align-right tabular">
                        <strong>{formatVnd(inv.remaining)}</strong>
                      </td>
                      <td>
                        <ResidentInvoiceStatusBadge
                          invoiceStatus={inv.invoiceStatus}
                          paymentStatus={inv.paymentStatus}
                        />
                      </td>
                      <td className="align-right">
                        <button
                          type="button"
                          className="btn btn-ghost btn-sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/resident/invoices/${inv.invoiceId}`);
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
              {invoicesData.content.map((inv) => (
                <li key={inv.invoiceId}>
                  <button
                    type="button"
                    className="invoice-mobile-item mobile-row-btn"
                    onClick={() => navigate(`/resident/invoices/${inv.invoiceId}`)}
                  >
                    <span className="mobile-row-top">
                      <strong className="tabular">Kỳ {formatPeriod(inv.period)}</strong>
                      <ResidentInvoiceStatusBadge
                        invoiceStatus={inv.invoiceStatus}
                        paymentStatus={inv.paymentStatus}
                      />
                    </span>
                    <span className="mobile-row-figure tabular">{formatVnd(inv.remaining)}</span>
                    <span className="field-hint">
                      Hạn nộp {formatBusinessDate(inv.dueDate)} · Tổng {formatVnd(inv.total)}
                    </span>
                  </button>
                </li>
              ))}
            </ul>

            <Pager
              page={invoicePage}
              totalPages={invoicesData.totalPages}
              onPageChange={setInvoicePage}
              label="Phân trang hóa đơn"
            />
          </>
        )}
      </section>

      {/* Two history sections */}
      <ReceiptHistoryBlock
        reloadTrigger={reloadTrigger}
        onAuthError={handleAuthError}
        onForbiddenError={handleForbiddenError}
        showInvoiceLink={true}
      />

      <AllocationHistoryBlock
        reloadTrigger={reloadTrigger}
        onAuthError={handleAuthError}
        onForbiddenError={handleForbiddenError}
        showInvoiceLink={true}
      />
    </>
  );
}
