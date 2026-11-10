import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApiHttpError } from '../../api/client';
import {
  exportCashReceiptsReport,
  exportFeeCollectionReport,
  exportResidencyReport,
  triggerBlobDownload,
} from '../../api/reports';
import {
  getVietnamDateParts,
  validateCashReceiptsRange,
  validatePeriodInput,
} from '../../lib/reportDateUtils';
import { ErrorPage } from '../common/ErrorPage';
import { useAppState } from '../../state/AppStateContext';

type ActiveReportType = 'fee-collection' | 'cash-receipts' | 'residency' | null;

interface ErrorState {
  message: string;
  reportType?: 'fee-collection' | 'cash-receipts' | 'residency';
  canRetry?: boolean;
}

export function ReportDownloadsPage(): React.ReactElement {
  const { user, mode, handleCallFailure } = useAppState();
  const navigate = useNavigate();

  // Guard role
  const isAuthorized = user && (user.role === 'ADMIN' || user.role === 'ACCOUNTANT');
  const isAdmin = user?.role === 'ADMIN';

  // State keys for session/account changes
  const sessionKey = `${user?.username ?? ''}:${user?.role ?? ''}:${mode}`;
  const sessionKeyRef = useRef(sessionKey);
  const isMountedRef = useRef(true);
  const genRef = useRef(0);
  const activeAbortRef = useRef<AbortController | null>(null);
  const pendingCleanupsRef = useRef<(() => void)[]>([]);

  // Form states
  const initialVn = getVietnamDateParts();
  const [period, setPeriod] = useState(initialVn.currentPeriod);
  const [fromDate, setFromDate] = useState(initialVn.firstDayOfMonth);
  const [toDate, setToDate] = useState(initialVn.todayDateString);

  // Field error states
  const [periodError, setPeriodError] = useState<string | null>(null);
  const [fromError, setFromError] = useState<string | null>(null);
  const [toError, setToError] = useState<string | null>(null);

  // Download & Feedback states
  const [activeReport, setActiveReport] = useState<ActiveReportType>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorState, setErrorState] = useState<ErrorState | null>(null);

  // Input refs for focus management
  const periodInputRef = useRef<HTMLInputElement | null>(null);
  const fromInputRef = useRef<HTMLInputElement | null>(null);
  const toInputRef = useRef<HTMLInputElement | null>(null);
  const errorAlertRef = useRef<HTMLDivElement | null>(null);

  const resetFormToDefaults = useCallback(() => {
    const vn = getVietnamDateParts();
    setPeriod(vn.currentPeriod);
    setFromDate(vn.firstDayOfMonth);
    setToDate(vn.todayDateString);
    setPeriodError(null);
    setFromError(null);
    setToError(null);
    setSuccessMessage(null);
    setErrorState(null);
    setActiveReport(null);
  }, []);

  // Cleanup on unmount or session/role/mode change
  useEffect(() => {
    isMountedRef.current = true;
    if (sessionKeyRef.current !== sessionKey) {
      sessionKeyRef.current = sessionKey;
      if (activeAbortRef.current) {
        activeAbortRef.current.abort();
        activeAbortRef.current = null;
      }
      for (const cleanup of pendingCleanupsRef.current) {
        cleanup();
      }
      pendingCleanupsRef.current = [];
      resetFormToDefaults();
    }

    return () => {
      isMountedRef.current = false;
      if (activeAbortRef.current) {
        activeAbortRef.current.abort();
        activeAbortRef.current = null;
      }
      for (const cleanup of pendingCleanupsRef.current) {
        cleanup();
      }
      pendingCleanupsRef.current = [];
    };
  }, [sessionKey, resetFormToDefaults]);

  if (!isAuthorized) {
    return <ErrorPage code={403} />;
  }

  // Demo mode per Contract 4.5 & 12.1
  if (mode !== 'api') {
    return (
      <div className="workspace-page">
        <header className="screen-header">
          <div>
            <h1 className="screen-title">Tải báo cáo</h1>
            <p className="screen-subtitle">Xuất dữ liệu báo cáo sang định dạng Excel (.xlsx)</p>
          </div>
        </header>
        <section className="panel p-6 mt-4">
          <div className="banner banner-info" role="status">
            <div className="banner-content">
              <p>Kết nối backend để tải báo cáo.</p>
            </div>
          </div>
        </section>
      </div>
    );
  }

  const isBusy = activeReport !== null;

  const handleDownloadError = (
    err: unknown,
    reportType: 'fee-collection' | 'cash-receipts' | 'residency',
    gen: number,
  ) => {
    if (!isMountedRef.current || sessionKeyRef.current !== sessionKey || genRef.current !== gen) {
      return;
    }

    // Ignore abort errors from navigation
    if (err instanceof DOMException && err.name === 'AbortError') {
      return;
    }

    if (err instanceof ApiHttpError) {
      const { status, code, requestId } = err.problem;

      if (status === 401) {
        handleCallFailure(err);
        navigate('/login', { state: { from: '/reports' } });
        return;
      }

      if (status === 403) {
        if (code === 'PASSWORD_CHANGE_REQUIRED') {
          navigate('/profile?tab=password');
          return;
        }
        setErrorState({
          message: 'Bạn không có quyền tải báo cáo này.',
          reportType,
          canRetry: false,
        });
        setTimeout(() => errorAlertRef.current?.focus(), 50);
        return;
      }

      if (status === 400) {
        if (code === 'REPORT_TOO_LARGE') {
          let advice = 'Kiểm tra lại bộ lọc rồi thử lại.';
          if (reportType === 'cash-receipts') {
            advice = 'Thu hẹp khoảng ngày rồi thử lại.';
          } else if (reportType === 'fee-collection') {
            advice = 'Chọn kỳ khác rồi thử lại.';
          } else if (reportType === 'residency') {
            advice = 'Liên hệ Ban quản trị để xử lý.';
          }
          setErrorState({
            message: `Báo cáo vượt giới hạn 50.000 dòng. ${advice}`,
            reportType,
            canRetry: false,
          });
          setTimeout(() => errorAlertRef.current?.focus(), 50);
          return;
        }

        if (code === 'INVALID_REPORT_FILTER') {
          setErrorState({
            message: 'Bộ lọc chưa hợp lệ. Kiểm tra kỳ thu hoặc khoảng ngày rồi thử lại.',
            reportType,
            canRetry: false,
          });
          setTimeout(() => errorAlertRef.current?.focus(), 50);
          return;
        }
      }

      // Non-2xx / 5xx / specific problem
      const reqInfo = requestId ? ` Mã yêu cầu: ${requestId}.` : '';
      const detailMsg =
        status >= 500
          ? `Có lỗi xảy ra, vui lòng thử lại.${reqInfo}`
          : `${err.problem.detail || 'Không nhận được tệp Excel hợp lệ. Thử lại hoặc báo mã yêu cầu.'}${reqInfo}`;
      setErrorState({
        message: detailMsg,
        reportType,
        canRetry: true,
      });
      setTimeout(() => errorAlertRef.current?.focus(), 50);
      return;
    }

    // Network error
    setErrorState({
      message: 'Không tải được tệp. Kiểm tra kết nối rồi thử lại.',
      reportType,
      canRetry: true,
    });
    setTimeout(() => errorAlertRef.current?.focus(), 50);
  };

  const handleDownloadFeeCollection = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isBusy) return;

    const error = validatePeriodInput(period);
    if (error) {
      setPeriodError(error);
      periodInputRef.current?.focus();
      return;
    }
    setPeriodError(null);
    setSuccessMessage(null);
    setErrorState(null);

    const currentGen = ++genRef.current;
    setActiveReport('fee-collection');

    const abortController = new AbortController();
    activeAbortRef.current = abortController;

    try {
      const result = await exportFeeCollectionReport(
        { period: period.trim() },
        abortController.signal,
      );

      if (
        !isMountedRef.current ||
        sessionKeyRef.current !== sessionKey ||
        genRef.current !== currentGen
      ) {
        return;
      }

      const cleanup = triggerBlobDownload(result.blob, result.filename);
      pendingCleanupsRef.current.push(cleanup);

      setSuccessMessage(`Đã gửi tệp tới trình duyệt: ${result.filename}`);
      setErrorState(null);
    } catch (err) {
      handleDownloadError(err, 'fee-collection', currentGen);
    } finally {
      if (
        isMountedRef.current &&
        sessionKeyRef.current === sessionKey &&
        genRef.current === currentGen
      ) {
        setActiveReport(null);
        activeAbortRef.current = null;
      }
    }
  };

  const handleDownloadCashReceipts = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isBusy) return;

    const validation = validateCashReceiptsRange(fromDate, toDate);
    if (!validation.isValid) {
      setFromError(validation.fromError);
      setToError(validation.toError);

      if (validation.fromError) {
        fromInputRef.current?.focus();
      } else if (validation.toError) {
        toInputRef.current?.focus();
      }
      return;
    }

    setFromError(null);
    setToError(null);
    setSuccessMessage(null);
    setErrorState(null);

    const currentGen = ++genRef.current;
    setActiveReport('cash-receipts');

    const abortController = new AbortController();
    activeAbortRef.current = abortController;

    try {
      const result = await exportCashReceiptsReport(
        { from: fromDate.trim(), to: toDate.trim() },
        abortController.signal,
      );

      if (
        !isMountedRef.current ||
        sessionKeyRef.current !== sessionKey ||
        genRef.current !== currentGen
      ) {
        return;
      }

      const cleanup = triggerBlobDownload(result.blob, result.filename);
      pendingCleanupsRef.current.push(cleanup);

      setSuccessMessage(`Đã gửi tệp tới trình duyệt: ${result.filename}`);
      setErrorState(null);
    } catch (err) {
      handleDownloadError(err, 'cash-receipts', currentGen);
    } finally {
      if (
        isMountedRef.current &&
        sessionKeyRef.current === sessionKey &&
        genRef.current === currentGen
      ) {
        setActiveReport(null);
        activeAbortRef.current = null;
      }
    }
  };

  const handleDownloadResidency = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isBusy || !isAdmin) return;

    setSuccessMessage(null);
    setErrorState(null);

    const currentGen = ++genRef.current;
    setActiveReport('residency');

    const abortController = new AbortController();
    activeAbortRef.current = abortController;

    try {
      const result = await exportResidencyReport(abortController.signal);

      if (
        !isMountedRef.current ||
        sessionKeyRef.current !== sessionKey ||
        genRef.current !== currentGen
      ) {
        return;
      }

      const cleanup = triggerBlobDownload(result.blob, result.filename);
      pendingCleanupsRef.current.push(cleanup);

      setSuccessMessage(`Đã gửi tệp tới trình duyệt: ${result.filename}`);
      setErrorState(null);
    } catch (err) {
      handleDownloadError(err, 'residency', currentGen);
    } finally {
      if (
        isMountedRef.current &&
        sessionKeyRef.current === sessionKey &&
        genRef.current === currentGen
      ) {
        setActiveReport(null);
        activeAbortRef.current = null;
      }
    }
  };

  const handleRetry = (e: React.MouseEvent) => {
    e.preventDefault();
    if (!errorState?.reportType) return;
    if (errorState.reportType === 'fee-collection') {
      handleDownloadFeeCollection(e as unknown as React.FormEvent);
    } else if (errorState.reportType === 'cash-receipts') {
      handleDownloadCashReceipts(e as unknown as React.FormEvent);
    } else if (errorState.reportType === 'residency') {
      handleDownloadResidency(e as unknown as React.FormEvent);
    }
  };

  return (
    <div className="workspace-page">
      <header className="screen-header">
        <div>
          <h1 className="screen-title">Tải báo cáo</h1>
          <p className="screen-subtitle">Xuất dữ liệu báo cáo sang định dạng Excel (.xlsx)</p>
        </div>
      </header>

      {/* Polite Live Status for Successful Downloads */}
      {successMessage && (
        <div className="banner banner-success mt-4" role="status" aria-live="polite">
          <div className="banner-content">
            <p>{successMessage}</p>
          </div>
        </div>
      )}

      {/* Alert Live Region for Errors */}
      {errorState && (
        <div
          ref={errorAlertRef}
          tabIndex={-1}
          className="banner banner-error mt-4"
          role="alert"
          aria-live="assertive"
        >
          <div className="banner-content">
            <p>{errorState.message}</p>
          </div>
          {errorState.canRetry && (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={handleRetry}
              disabled={isBusy}
            >
              Thử tải lại
            </button>
          )}
        </div>
      )}

      <div className="reports-grid mt-6 stack-form">
        {/* Block 1: Thu theo kỳ hóa đơn */}
        <section
          className="panel p-6"
          aria-busy={activeReport === 'fee-collection'}
          aria-labelledby="heading-fee-collection"
        >
          <h2 id="heading-fee-collection" className="text-xl font-bold mb-2">
            Thu theo kỳ
          </h2>
          <p className="text-secondary mb-2 text-sm">
            Hóa đơn đã phát hành trong kỳ. Đã thanh toán gồm tiền áp dụng trực tiếp và bù trừ; tỷ lệ
            tính theo tổng số tiền phải thu. Không gồm đóng góp tự nguyện.
          </p>
          <p className="text-muted text-xs mb-4">
            Nếu không có dữ liệu phù hợp, tệp vẫn có tiêu đề và tổng bằng 0.
          </p>

          <details className="report-schema-details mb-4 text-xs text-secondary">
            <summary className="cursor-pointer font-semibold text-muted hover:text-primary">
              Cột dữ liệu trong tệp Excel
            </summary>
            <div className="mt-2 pl-3 border-l-2 border-hairline">
              <p className="font-semibold text-ink">Sheet TongHop:</p>
              <p className="mb-2">
                Kỳ, Thời điểm xuất, Số invoice ISSUED, Tổng phải thu, Đã thanh toán, Còn nợ, Tỷ lệ %
              </p>
              <p className="font-semibold text-ink">Sheet HoaDon:</p>
              <p>
                Mã hóa đơn, Mã hộ, Căn hộ, Hạn nộp, Phải thu, Đã thanh toán, Còn nợ, Trạng thái
                thanh toán
              </p>
            </div>
          </details>

          <form onSubmit={handleDownloadFeeCollection} noValidate className="stack-form max-w-md">
            <div className="form-field">
              <label htmlFor="period-input" className="field-label">
                Kỳ thu <span className="field-hint">(YYYY-MM)</span>
              </label>
              <input
                id="period-input"
                ref={periodInputRef}
                type="text"
                className="input"
                placeholder="2026-11"
                value={period}
                disabled={isBusy}
                aria-invalid={Boolean(periodError)}
                aria-describedby={periodError ? 'period-error' : undefined}
                onChange={(e) => {
                  setPeriod(e.target.value);
                  if (periodError) setPeriodError(null);
                }}
              />
              {periodError && (
                <span id="period-error" className="field-error" role="alert">
                  {periodError}
                </span>
              )}
            </div>

            <div className="form-actions justify-start">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={isBusy}
                aria-label="Tải thu theo kỳ"
              >
                {activeReport === 'fee-collection' ? 'Đang chuẩn bị tệp…' : 'Tải thu theo kỳ'}
              </button>
            </div>

            {activeReport === 'fee-collection' && (
              <div role="status" aria-live="polite" className="text-sm text-secondary font-medium">
                Đang chuẩn bị tệp…
              </div>
            )}
          </form>
        </section>

        {/* Block 2: Tiền mặt theo ngày thực thu */}
        <section
          className="panel p-6"
          aria-busy={activeReport === 'cash-receipts'}
          aria-labelledby="heading-cash-receipts"
        >
          <h2 id="heading-cash-receipts" className="text-xl font-bold mb-2">
            Tiền mặt theo ngày thực thu
          </h2>
          <p className="text-secondary mb-2 text-sm">
            Phiếu thu còn hiệu lực theo ngày thực thu, gồm phần tiền dư ban đầu; không cộng bù trừ,
            không gồm phiếu đã hủy hoặc đóng góp tự nguyện.
          </p>
          <p className="text-muted text-xs mb-4">
            Nếu không có dữ liệu phù hợp, tệp vẫn có tiêu đề và tổng bằng 0.
          </p>

          <details className="report-schema-details mb-4 text-xs text-secondary">
            <summary className="cursor-pointer font-semibold text-muted hover:text-primary">
              Cột dữ liệu trong tệp Excel
            </summary>
            <div className="mt-2 pl-3 border-l-2 border-hairline">
              <p className="font-semibold text-ink">Sheet TongHop:</p>
              <p className="mb-2">
                Từ ngày, Đến ngày, Thời điểm xuất, Số receipt POSTED, Tổng tiền mặt nhận
              </p>
              <p className="font-semibold text-ink">Sheet PhieuThu:</p>
              <p>
                Mã receipt, Kỳ hóa đơn nguồn, Mã hộ, Căn hộ, Ngày thực thu paidOn, Tiền nhận, Phần
                áp dụng trực tiếp, Credit phát sinh ban đầu
              </p>
            </div>
          </details>

          <form onSubmit={handleDownloadCashReceipts} noValidate className="stack-form max-w-lg">
            <div className="form-grid">
              <div className="form-field">
                <label htmlFor="from-date-input" className="field-label">
                  Từ ngày
                </label>
                <input
                  id="from-date-input"
                  ref={fromInputRef}
                  type="date"
                  className="input"
                  value={fromDate}
                  disabled={isBusy}
                  aria-invalid={Boolean(fromError)}
                  aria-describedby={
                    fromError ? 'from-date-error' : 'cash-range-hint'
                  }
                  onChange={(e) => {
                    setFromDate(e.target.value);
                    if (fromError) setFromError(null);
                  }}
                />
                {fromError && (
                  <span id="from-date-error" className="field-error" role="alert">
                    {fromError}
                  </span>
                )}
              </div>

              <div className="form-field">
                <label htmlFor="to-date-input" className="field-label">
                  Đến ngày
                </label>
                <input
                  id="to-date-input"
                  ref={toInputRef}
                  type="date"
                  className="input"
                  value={toDate}
                  disabled={isBusy}
                  aria-invalid={Boolean(toError)}
                  aria-describedby={toError ? 'to-date-error' : 'cash-range-hint'}
                  onChange={(e) => {
                    setToDate(e.target.value);
                    if (toError) setToError(null);
                  }}
                />
                {toError && (
                  <span id="to-date-error" className="field-error" role="alert">
                    {toError}
                  </span>
                )}
              </div>
            </div>

            <p id="cash-range-hint" className="field-hint">
              Bao gồm cả ngày đầu và ngày cuối.
            </p>

            <div className="form-actions justify-start">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={isBusy}
                aria-label="Tải tiền mặt theo ngày"
              >
                {activeReport === 'cash-receipts' ? 'Đang chuẩn bị tệp…' : 'Tải tiền mặt theo ngày'}
              </button>
            </div>

            {activeReport === 'cash-receipts' && (
              <div role="status" aria-live="polite" className="text-sm text-secondary font-medium">
                Đang chuẩn bị tệp…
              </div>
            )}
          </form>
        </section>

        {/* Block 3: Cư trú hiện tại (ADMIN only) */}
        {isAdmin && (
          <section
            className="panel p-6"
            aria-busy={activeReport === 'residency'}
            aria-labelledby="heading-residency"
          >
            <h2 id="heading-residency" className="text-xl font-bold mb-2">
              Cư trú hiện tại
            </h2>
            <p className="text-secondary mb-2 text-sm">
              Danh sách hiện tại: hộ đang hoạt động, người đang cư trú hoặc tạm vắng. Gồm mã hộ, căn
              hộ, họ tên và trạng thái.
            </p>
            <p className="text-muted text-xs mb-4">
              Nếu không có dữ liệu phù hợp, tệp vẫn có tiêu đề và tổng bằng 0.
            </p>

            <details className="report-schema-details mb-4 text-xs text-secondary">
              <summary className="cursor-pointer font-semibold text-muted hover:text-primary">
                Cột dữ liệu trong tệp Excel
              </summary>
              <div className="mt-2 pl-3 border-l-2 border-hairline">
                <p className="font-semibold text-ink">Sheet CuTru:</p>
                <p>Mã hộ, Căn hộ, Họ tên, Trạng thái</p>
              </div>
            </details>

            <form onSubmit={handleDownloadResidency} className="stack-form max-w-md">
              <div className="form-actions justify-start">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={isBusy}
                  aria-label="Tải cư trú hiện tại"
                >
                  {activeReport === 'residency' ? 'Đang chuẩn bị tệp…' : 'Tải cư trú hiện tại'}
                </button>
              </div>

              {activeReport === 'residency' && (
                <div
                  role="status"
                  aria-live="polite"
                  className="text-sm text-secondary font-medium"
                >
                  Đang chuẩn bị tệp…
                </div>
              )}
            </form>
          </section>
        )}
      </div>
    </div>
  );
}
