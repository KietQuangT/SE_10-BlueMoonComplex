import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchResidentCreditAllocations,
  fetchResidentReceipts,
  type AllocationFilterStatus,
  type AllocationStatus,
  type PageResponse,
  type ReceiptFilterStatus,
  type ReceiptStatus,
  type ResidentAllocation,
  type ResidentReceipt,
} from '../../api/residentBilling';
import { ApiHttpError } from '../../api/client';
import { messageForError } from '../../api/errors';
import { StatusBadge } from '../../components/Badge';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import {
  formatBusinessDate,
  formatInstantVn,
  formatPeriod,
  formatVnd,
} from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';
import { Pager } from '../s08/Pager';

export function ResidentInvoiceStatusBadge({
  invoiceStatus,
  paymentStatus,
}: {
  invoiceStatus: 'ISSUED' | 'CANCELLED';
  paymentStatus: 'UNPAID' | 'PARTIALLY_PAID' | 'PAID' | 'CANCELLED';
}): React.ReactElement {
  if (invoiceStatus === 'CANCELLED' || paymentStatus === 'CANCELLED') {
    return <StatusBadge tone="neutral" label="Đã hủy" />;
  }
  switch (paymentStatus) {
    case 'UNPAID':
      return <StatusBadge tone="pending" label="Chưa thanh toán" />;
    case 'PARTIALLY_PAID':
      return <StatusBadge tone="pending" label="Đã thanh toán một phần" />;
    case 'PAID':
      return <StatusBadge tone="paid" label="Đã thanh toán" />;
    default:
      return <StatusBadge tone="neutral" label={paymentStatus} />;
  }
}

export function ResidentReceiptStatusBadge({
  status,
  voidedAt,
}: {
  status: ReceiptStatus;
  voidedAt?: string | null;
}): React.ReactElement {
  if (status === 'VOIDED') {
    return (
      <span>
        <StatusBadge tone="neutral" label="Đã hủy" />
        {voidedAt && (
          <span className="field-hint" style={{ display: 'block', marginTop: '2px' }}>
            Hủy lúc {formatInstantVn(voidedAt)}
          </span>
        )}
      </span>
    );
  }
  return <StatusBadge tone="paid" label="Đã ghi nhận" />;
}

export function ResidentAllocationStatusBadge({
  status,
  reversedAt,
}: {
  status: AllocationStatus;
  reversedAt?: string | null;
}): React.ReactElement {
  if (status === 'REVERSED') {
    return (
      <span>
        <StatusBadge tone="neutral" label="Đã đảo" />
        {reversedAt && (
          <span className="field-hint" style={{ display: 'block', marginTop: '2px' }}>
            Đảo lúc {formatInstantVn(reversedAt)}
          </span>
        )}
      </span>
    );
  }
  return <StatusBadge tone="paid" label="Đang hiệu lực" />;
}

export function formatResidentQuantity(line: {
  calculationMethod?: string;
  unit?: string;
  quantity: string;
  areaM2?: string | null;
}): string {
  if (line.calculationMethod === 'PER_HOUSEHOLD' || line.unit === 'HOUSEHOLD') {
    const qty = line.quantity.replace(/\.0+$/, '').replace(/(\.[0-9]*[1-9])0+$/, '$1');
    return `${qty} hộ`;
  }
  if (line.areaM2) {
    return `${line.areaM2} m²`;
  }
  return `${line.quantity} m²`;
}

interface ReceiptHistoryBlockProps {
  invoiceId?: number;
  reloadTrigger?: number;
  onAuthError: (err: unknown) => void;
  onForbiddenError?: (err: ApiHttpError) => void;
  onDataNotFound?: () => void;
  showInvoiceLink?: boolean;
}

export function ReceiptHistoryBlock({
  invoiceId,
  reloadTrigger,
  onAuthError,
  onForbiddenError,
  onDataNotFound,
  showInvoiceLink = true,
}: ReceiptHistoryBlockProps): React.ReactElement {
  const { user } = useAppState();
  const userKey = `${user?.username ?? ''}:${user?.householdId ?? ''}`;
  const userKeyRef = useRef(userKey);
  userKeyRef.current = userKey;

  const [data, setData] = useState<PageResponse<ResidentReceipt> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<ReceiptFilterStatus>('ALL');
  const [page, setPage] = useState(0);

  const isMountedRef = useRef(true);
  const genRef = useRef(0);

  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
      genRef.current++;
    };
  }, []);

  const load = useCallback(async () => {
    const currentGen = ++genRef.current;
    const keyAtStart = userKeyRef.current;
    setLoading(true);
    setError(null);
    try {
      const res = await fetchResidentReceipts({
        invoiceId,
        status: statusFilter,
        page,
        size: 20,
      });
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || genRef.current !== currentGen) return;
      setData(res);
      setError(null);
    } catch (err: unknown) {
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || genRef.current !== currentGen) return;
      if (err instanceof ApiHttpError) {
        if (err.problem.status === 401) {
          setData(null);
          onAuthError(err);
          return;
        }
        if (err.problem.status === 403) {
          setData(null);
          if (onForbiddenError) {
            onForbiddenError(err);
            return;
          }
        }
        if (err.problem.status === 404 && invoiceId != null) {
          setData(null);
          onDataNotFound?.();
          return;
        }
      }
      setError(messageForError(err));
    } finally {
      if (isMountedRef.current && userKeyRef.current === keyAtStart && genRef.current === currentGen) {
        setLoading(false);
      }
    }
  }, [invoiceId, statusFilter, page, onAuthError, onForbiddenError, onDataNotFound]);

  useEffect(() => {
    void load();
  }, [load, reloadTrigger]);

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatusFilter(e.target.value as ReceiptFilterStatus);
    setPage(0);
  };

  return (
    <section className="surface-box s10-history-section" aria-labelledby="receipt-history-title">
      <div className="s10-section-title-row">
        <h2 id="receipt-history-title" className="box-title">
          Thu tiền mặt
        </h2>
        <div className="s10-filter-group" style={{ flexDirection: 'row', alignItems: 'center', gap: '8px' }}>
          <label htmlFor={`receipt-status-${invoiceId ?? 'all'}`}>Trạng thái:</label>
          <select
            id={`receipt-status-${invoiceId ?? 'all'}`}
            className="select"
            value={statusFilter}
            onChange={handleFilterChange}
            aria-label="Lọc trạng thái thu tiền mặt"
          >
            <option value="ALL">Tất cả</option>
            <option value="POSTED">Đã ghi nhận</option>
            <option value="VOIDED">Đã hủy</option>
          </select>
        </div>
      </div>

      {loading ? (
        <SkeletonRows rows={3} />
      ) : error ? (
        <ErrorRetryPanel message={error} onRetry={() => void load()} />
      ) : !data || data.content.length === 0 ? (
        <EmptyStatePanel
          title="Chưa có khoản thu tiền mặt"
          description="Khi Kế toán ghi nhận khoản thu tiền mặt của hộ, biên nhận sẽ hiển thị tại đây."
        />
      ) : (
        <>
          <div className="table-scroll s10-desktop-table">
            <table className="ledger-table">
              <thead>
                <tr>
                  <th scope="col">Mã biên nhận</th>
                  <th scope="col">Kỳ</th>
                  <th scope="col">Ngày thực thu</th>
                  <th scope="col">Ngày ghi nhận</th>
                  <th scope="col" className="align-right">Tiền đã nộp</th>
                  <th scope="col" className="align-right">Áp dụng hóa đơn</th>
                  <th scope="col" className="align-right">Số dư phát sinh</th>
                  <th scope="col">Trạng thái</th>
                  <th scope="col">Lý do</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((r) => (
                  <tr key={r.receiptId}>
                    <td className="tabular">#{r.receiptId}</td>
                    <td className="tabular">
                      {showInvoiceLink ? (
                        <Link to={`/resident/invoices/${r.invoiceId}`} className="table-link">
                          Kỳ {formatPeriod(r.period)}
                        </Link>
                      ) : (
                        `Kỳ ${formatPeriod(r.period)}`
                      )}
                    </td>
                    <td className="tabular">{formatBusinessDate(r.paidOn)}</td>
                    <td className="tabular">{formatInstantVn(r.recordedAt)}</td>
                    <td className="align-right tabular">
                      <strong>{formatVnd(r.amount)}</strong>
                    </td>
                    <td className="align-right tabular">{formatVnd(r.appliedAmount)}</td>
                    <td className="align-right tabular">{formatVnd(r.creditAmount)}</td>
                    <td>
                      <ResidentReceiptStatusBadge status={r.status} voidedAt={r.voidedAt} />
                    </td>
                    <td>
                      {r.hasLateReason || r.hasVoidReason ? (
                        <span className="badge badge-neutral">Có lý do</span>
                      ) : (
                        '—'
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <ul className="s10-card-list">
            {data.content.map((r) => (
              <li key={r.receiptId} className="s10-card-item">
                <div className="mobile-row-top">
                  <strong>#{r.receiptId} · Kỳ {formatPeriod(r.period)}</strong>
                  <ResidentReceiptStatusBadge status={r.status} voidedAt={r.voidedAt} />
                </div>
                <div className="field-hint">
                  Ngày thực thu: {formatBusinessDate(r.paidOn)} · Ghi nhận: {formatInstantVn(r.recordedAt)}
                </div>
                <div>
                  Tiền đã nộp: <strong>{formatVnd(r.amount)}</strong>
                </div>
                <div className="field-hint">
                  Áp dụng: {formatVnd(r.appliedAmount)} · Số dư phát sinh ban đầu: {formatVnd(r.creditAmount)}
                </div>
                {(r.hasLateReason || r.hasVoidReason) && (
                  <div>
                    <span className="badge badge-neutral">Có lý do</span>
                  </div>
                )}
                {showInvoiceLink && (
                  <div style={{ marginTop: '4px' }}>
                    <Link to={`/resident/invoices/${r.invoiceId}`} className="btn btn-ghost btn-sm">
                      Xem hóa đơn
                    </Link>
                  </div>
                )}
              </li>
            ))}
          </ul>

          <Pager
            page={page}
            totalPages={data.totalPages}
            onPageChange={setPage}
            label="Phân trang thu tiền mặt"
          />
        </>
      )}
    </section>
  );
}

interface AllocationHistoryBlockProps {
  invoiceId?: number;
  reloadTrigger?: number;
  onAuthError: (err: unknown) => void;
  onForbiddenError?: (err: ApiHttpError) => void;
  onDataNotFound?: () => void;
  showInvoiceLink?: boolean;
}

export function AllocationHistoryBlock({
  invoiceId,
  reloadTrigger,
  onAuthError,
  onForbiddenError,
  onDataNotFound,
  showInvoiceLink = true,
}: AllocationHistoryBlockProps): React.ReactElement {
  const { user } = useAppState();
  const userKey = `${user?.username ?? ''}:${user?.householdId ?? ''}`;
  const userKeyRef = useRef(userKey);
  userKeyRef.current = userKey;

  const [data, setData] = useState<PageResponse<ResidentAllocation> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<AllocationFilterStatus>('ALL');
  const [page, setPage] = useState(0);

  const isMountedRef = useRef(true);
  const genRef = useRef(0);

  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
      genRef.current++;
    };
  }, []);

  const load = useCallback(async () => {
    const currentGen = ++genRef.current;
    const keyAtStart = userKeyRef.current;
    setLoading(true);
    setError(null);
    try {
      const res = await fetchResidentCreditAllocations({
        invoiceId,
        status: statusFilter,
        page,
        size: 20,
      });
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || genRef.current !== currentGen) return;
      setData(res);
      setError(null);
    } catch (err: unknown) {
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || genRef.current !== currentGen) return;
      if (err instanceof ApiHttpError) {
        if (err.problem.status === 401) {
          setData(null);
          onAuthError(err);
          return;
        }
        if (err.problem.status === 403) {
          setData(null);
          if (onForbiddenError) {
            onForbiddenError(err);
            return;
          }
        }
        if (err.problem.status === 404 && invoiceId != null) {
          setData(null);
          onDataNotFound?.();
          return;
        }
      }
      setError(messageForError(err));
    } finally {
      if (isMountedRef.current && userKeyRef.current === keyAtStart && genRef.current === currentGen) {
        setLoading(false);
      }
    }
  }, [invoiceId, statusFilter, page, onAuthError, onForbiddenError, onDataNotFound]);

  useEffect(() => {
    void load();
  }, [load, reloadTrigger]);

  const handleFilterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setStatusFilter(e.target.value as AllocationFilterStatus);
    setPage(0);
  };

  return (
    <section className="surface-box s10-history-section" aria-labelledby="allocation-history-title">
      <div className="s10-section-title-row">
        <div>
          <h2 id="allocation-history-title" className="box-title">
            Bù trừ số dư
          </h2>
          <p className="s10-history-note">
            Đây là số dư được dùng cho hóa đơn, không phải khoản tiền nộp mới.
          </p>
        </div>
        <div className="s10-filter-group" style={{ flexDirection: 'row', alignItems: 'center', gap: '8px' }}>
          <label htmlFor={`alloc-status-${invoiceId ?? 'all'}`}>Trạng thái:</label>
          <select
            id={`alloc-status-${invoiceId ?? 'all'}`}
            className="select"
            value={statusFilter}
            onChange={handleFilterChange}
            aria-label="Lọc trạng thái bù trừ số dư"
          >
            <option value="ALL">Tất cả</option>
            <option value="ACTIVE">Đang hiệu lực</option>
            <option value="REVERSED">Đã đảo</option>
          </select>
        </div>
      </div>

      {loading ? (
        <SkeletonRows rows={3} />
      ) : error ? (
        <ErrorRetryPanel message={error} onRetry={() => void load()} />
      ) : !data || data.content.length === 0 ? (
        <EmptyStatePanel
          title="Chưa có bù trừ số dư"
          description="Khoản bù trừ từ số dư khả dụng sẽ xuất hiện tại đây khi được áp dụng vào hóa đơn."
        />
      ) : (
        <>
          <div className="table-scroll s10-desktop-table">
            <table className="ledger-table">
              <thead>
                <tr>
                  <th scope="col">Mã bù trừ</th>
                  <th scope="col">Kỳ</th>
                  <th scope="col" className="align-right">Số tiền bù trừ</th>
                  <th scope="col">Ngày ghi nhận</th>
                  <th scope="col">Trạng thái</th>
                  <th scope="col">Lý do</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((a) => (
                  <tr key={a.allocationId}>
                    <td className="tabular">#{a.allocationId}</td>
                    <td className="tabular">
                      {showInvoiceLink ? (
                        <Link to={`/resident/invoices/${a.invoiceId}`} className="table-link">
                          Kỳ {formatPeriod(a.period)}
                        </Link>
                      ) : (
                        `Kỳ ${formatPeriod(a.period)}`
                      )}
                    </td>
                    <td className="align-right tabular">
                      <strong>{formatVnd(a.amount)}</strong>
                    </td>
                    <td className="tabular">{formatInstantVn(a.recordedAt)}</td>
                    <td>
                      <ResidentAllocationStatusBadge status={a.status} reversedAt={a.reversedAt} />
                    </td>
                    <td>
                      {a.hasReverseReason ? (
                        <span className="badge badge-neutral">Có lý do</span>
                      ) : (
                        '—'
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <ul className="s10-card-list">
            {data.content.map((a) => (
              <li key={a.allocationId} className="s10-card-item">
                <div className="mobile-row-top">
                  <strong>#{a.allocationId} · Kỳ {formatPeriod(a.period)}</strong>
                  <ResidentAllocationStatusBadge status={a.status} reversedAt={a.reversedAt} />
                </div>
                <div>
                  Số tiền bù trừ: <strong>{formatVnd(a.amount)}</strong>
                </div>
                <div className="field-hint">
                  Ghi nhận: {formatInstantVn(a.recordedAt)}
                </div>
                {a.hasReverseReason && (
                  <div>
                    <span className="badge badge-neutral">Có lý do</span>
                  </div>
                )}
                {showInvoiceLink && (
                  <div style={{ marginTop: '4px' }}>
                    <Link to={`/resident/invoices/${a.invoiceId}`} className="btn btn-ghost btn-sm">
                      Xem hóa đơn
                    </Link>
                  </div>
                )}
              </li>
            ))}
          </ul>

          <Pager
            page={page}
            totalPages={data.totalPages}
            onPageChange={setPage}
            label="Phân trang bù trừ số dư"
          />
        </>
      )}
    </section>
  );
}
