import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  fetchResidentInvoiceDetail,
  type ResidentInvoiceDetail,
} from '../../api/residentBilling';
import { ApiHttpError } from '../../api/client';
import { messageForError } from '../../api/errors';
import { ScreenHeader } from '../../components/ScreenHeader';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { Icon } from '../../components/Icon';
import {
  formatBusinessDate,
  formatInstantVn,
  formatPeriod,
  formatVnd,
  formatVndDecimal,
} from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';
import {
  AllocationHistoryBlock,
  formatResidentQuantity,
  ReceiptHistoryBlock,
  ResidentInvoiceStatusBadge,
} from './ResidentBillingHistory';
import './s10.css';

export function ApiInvoiceDetailPage(): React.ReactElement {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { handleCallFailure, user } = useAppState();
  const userKey = `${user?.username ?? ''}:${user?.householdId ?? ''}`;
  const userKeyRef = useRef(userKey);
  userKeyRef.current = userKey;

  const [reloadTrigger, setReloadTrigger] = useState(0);
  const [detail, setDetail] = useState<ResidentInvoiceDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [authError, setAuthError] = useState(false);
  const [forbiddenError, setForbiddenError] = useState<string | null>(null);
  const [notFound, setNotFound] = useState(false);
  const [invalidId, setInvalidId] = useState(false);

  const isMountedRef = useRef(true);
  const genRef = useRef(0);
  const isBlockedRef = useRef(false);

  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
      genRef.current++;
    };
  }, []);

  const backLink = (
    <Link to="/resident/invoices" className="btn btn-ghost btn-sm back-link" style={{ marginBottom: '12px', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
      <Icon name="arrow-left" size={16} />
      Về danh sách hóa đơn
    </Link>
  );

  const handleAuthError = useCallback(
    (err: unknown) => {
      if (!isMountedRef.current) return;
      isBlockedRef.current = true;
      setAuthError(true);
      setDetail(null);
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
      setDetail(null);
      if (err instanceof ApiHttpError && err.problem.code === 'PASSWORD_CHANGE_REQUIRED') {
        navigate('/profile?tab=password');
        return;
      }
      setForbiddenError(messageForError(err));
    },
    [navigate],
  );

  const handleHistoryNotFound = useCallback(() => {
    if (!isMountedRef.current) return;
    // 404 history after detail succeeded means data inconsistency: clear detail and show not found
    setDetail(null);
    setNotFound(true);
  }, []);

  const loadDetail = useCallback(async () => {
    if (!id || !/^[1-9]\d*$/.test(id) || !Number.isSafeInteger(Number(id))) {
      setInvalidId(true);
      setDetail(null);
      setLoading(false);
      return;
    }

    const numericId = Number(id);
    const gen = ++genRef.current;
    const keyAtStart = userKeyRef.current;
    setInvalidId(false);
    setDetail(null);
    setLoading(true);
    setError(null);
    setNotFound(false);
    setForbiddenError(null);

    try {
      const res = await fetchResidentInvoiceDetail(numericId);
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || genRef.current !== gen || isBlockedRef.current) return;
      setDetail(res);
      setError(null);
    } catch (err: unknown) {
      if (!isMountedRef.current || userKeyRef.current !== keyAtStart || genRef.current !== gen) return;
      if (err instanceof ApiHttpError) {
        if (err.problem.status === 401) {
          handleAuthError(err);
          return;
        }
        if (err.problem.status === 403) {
          handleForbiddenError(err);
          return;
        }
        if (err.problem.status === 404) {
          setNotFound(true);
          return;
        }
      }
      setError(messageForError(err));
    } finally {
      if (isMountedRef.current && userKeyRef.current === keyAtStart && genRef.current === gen) {
        setLoading(false);
      }
    }
  }, [id, handleAuthError, handleForbiddenError]);

  useEffect(() => {
    void loadDetail();
  }, [loadDetail, reloadTrigger]);

  const handleReloadAll = () => {
    isBlockedRef.current = false;
    setForbiddenError(null);
    setAuthError(false);
    setReloadTrigger((prev) => prev + 1);
  };

  if (invalidId) {
    return (
      <>
        {backLink}
        <div className="state-panel" role="alert">
          <h2>Mã hóa đơn không hợp lệ</h2>
          <p>Đường dẫn hóa đơn không đúng định dạng. Vui lòng quay lại danh sách hóa đơn.</p>
          <Link to="/resident/invoices" className="btn btn-secondary">
            Về danh sách hóa đơn
          </Link>
        </div>
      </>
    );
  }

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

  if (notFound) {
    return (
      <>
        {backLink}
        <div className="state-panel" role="alert">
          <h2>Không tìm thấy hóa đơn</h2>
          <p>Hóa đơn không tồn tại hoặc không thuộc hộ của bạn.</p>
          <Link to="/resident/invoices" className="btn btn-secondary">
            Về danh sách hóa đơn
          </Link>
        </div>
      </>
    );
  }

  if (forbiddenError) {
    return (
      <>
        {backLink}
        <div className="state-panel" role="alert">
          <h2>Không có quyền truy cập</h2>
          <p>{forbiddenError}</p>
          <Link to="/resident/invoices" className="btn btn-secondary">
            Về danh sách hóa đơn
          </Link>
        </div>
      </>
    );
  }

  if (loading) {
    return (
      <>
        {backLink}
        <SkeletonRows rows={3} />
        <SkeletonRows rows={4} />
      </>
    );
  }

  if (error || !detail || detail.invoiceId !== Number(id)) {
    return (
      <>
        {backLink}
        <ErrorRetryPanel
          message={error || 'Không tải được chi tiết hóa đơn.'}
          onRetry={() => void loadDetail()}
        />
      </>
    );
  }

  const isCancelled = detail.invoiceStatus === 'CANCELLED';
  const remainingNonZero = detail.remaining !== '0';

  return (
    <>
      {backLink}

      <div className="s10-toolbar-row">
        <ScreenHeader
          screenCode="M-33"
          title={`Hóa đơn kỳ ${formatPeriod(detail.period)}`}
          description={`Căn ${detail.apartmentCode} · Hộ ${detail.householdCode} · Phát hành ${formatInstantVn(detail.issuedAt)} · Hạn nộp ${formatBusinessDate(detail.dueDate)}`}
          contract="api-live"
          tierLabel="Mức A"
          actions={
            <ResidentInvoiceStatusBadge
              invoiceStatus={detail.invoiceStatus}
              paymentStatus={detail.paymentStatus}
            />
          }
        />
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          onClick={handleReloadAll}
          aria-label="Tải lại chi tiết hóa đơn"
        >
          Tải lại
        </button>
      </div>

      {isCancelled && (
        <div className="banner banner-neutral s10-notice-banner" role="alert">
          <div className="banner-content">
            <Icon name="info" />
            <span>
              <strong>Hóa đơn đã bị hủy</strong>
              {detail.cancelledAt ? ` vào ngày ${formatInstantVn(detail.cancelledAt).slice(0, 10)}` : ''}.
              Tổng tiền gốc chỉ để đối chiếu, không phát sinh nghĩa vụ thanh toán.
            </span>
          </div>
        </div>
      )}

      {/* Fee lines breakdown */}
      <section className="surface-box" aria-labelledby="lines-title">
        <div className="box-toolbar">
          <h2 id="lines-title" className="box-title">
            Các khoản phí thành phần
          </h2>
          <span className="field-hint">Diện tích và đơn giá áp dụng tại thời điểm phát hành</span>
        </div>

        {detail.lines.length === 0 ? (
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
                    <th scope="col" className="align-right">Số lượng</th>
                    <th scope="col" className="align-right">Đơn giá</th>
                    <th scope="col" className="align-right">Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  {detail.lines.map((line) => (
                    <tr key={line.feeTypeCode}>
                      <td>{line.feeTypeName}</td>
                      <td className="align-right tabular">{formatResidentQuantity(line)}</td>
                      <td className="align-right tabular">{formatVndDecimal(line.unitPrice)}</td>
                      <td className="align-right tabular">{formatVnd(line.amount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <ul className="invoice-mobile-list">
              {detail.lines.map((line) => (
                <li key={line.feeTypeCode} className="invoice-mobile-item">
                  <span className="mobile-row-top">
                    <strong>{line.feeTypeName}</strong>
                    <span className="tabular">{formatVnd(line.amount)}</span>
                  </span>
                  <span className="field-hint tabular">
                    {formatResidentQuantity(line)} × {formatVndDecimal(line.unitPrice)}
                  </span>
                </li>
              ))}
            </ul>
          </>
        )}

        <dl className="totals-dl">
          <div>
            <dt>Tổng hóa đơn</dt>
            <dd className="tabular">{formatVnd(detail.total)}</dd>
          </div>
          <div>
            <dt>Đã thanh toán</dt>
            <dd className="tabular">
              {formatVnd(detail.settled)}
              <span className="field-hint" style={{ display: 'block', fontSize: '0.8rem', marginTop: '2px' }}>
                Gồm tiền thu áp dụng và số dư đã bù trừ
              </span>
            </dd>
          </div>
          <div className="totals-due">
            <dt>Còn nợ</dt>
            <dd className="tabular">{formatVnd(detail.remaining)}</dd>
          </div>
        </dl>
      </section>

      {/* Invoice scoped histories */}
      <ReceiptHistoryBlock
        invoiceId={detail.invoiceId}
        reloadTrigger={reloadTrigger}
        onAuthError={handleAuthError}
        onForbiddenError={handleForbiddenError}
        onDataNotFound={handleHistoryNotFound}
        showInvoiceLink={false}
      />

      <AllocationHistoryBlock
        invoiceId={detail.invoiceId}
        reloadTrigger={reloadTrigger}
        onAuthError={handleAuthError}
        onForbiddenError={handleForbiddenError}
        onDataNotFound={handleHistoryNotFound}
        showInvoiceLink={false}
      />

      {/* Cash payment guidance only when ISSUED and remaining > 0 */}
      {!isCancelled && remainingNonZero && (
        <section className="surface-box surface-box-pad how-to-pay" style={{ marginTop: '24px' }}>
          <h2 className="box-title">Cách đóng tiền</h2>
          <p className="screen-desc">
            Liên hệ Kế toán hoặc Ban quản trị để nộp tiền mặt; báo mã căn{' '}
            <strong className="tabular">{detail.apartmentCode}</strong> và kỳ{' '}
            <strong className="tabular">{formatPeriod(detail.period)}</strong>.
          </p>
        </section>
      )}
    </>
  );
}
