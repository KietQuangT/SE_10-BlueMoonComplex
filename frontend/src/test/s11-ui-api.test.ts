import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import {
  exportCashReceiptsReport,
  exportFeeCollectionReport,
  exportResidencyReport,
  triggerBlobDownload,
  XLSX_MIME,
} from '../api/reports';
import { ApiHttpError } from '../api/client';
import {
  getVietnamDateParts,
  isLeapYear,
  isValidCalendarDate,
  isValidPeriod,
  validateCashReceiptsRange,
  validatePeriodInput,
} from '../lib/reportDateUtils';

describe('S11-UI Report Date Utils', () => {
  it('identifies leap years accurately', () => {
    expect(isLeapYear(2024)).toBe(true);
    expect(isLeapYear(2000)).toBe(true);
    expect(isLeapYear(2025)).toBe(false);
    expect(isLeapYear(2026)).toBe(false);
    expect(isLeapYear(1900)).toBe(false);
  });

  it('validates calendar dates rejecting invalid or non-existent dates', () => {
    expect(isValidCalendarDate('2026-11-10')).toBe(true);
    expect(isValidCalendarDate('2024-02-29')).toBe(true); // leap year
    expect(isValidCalendarDate('2025-02-29')).toBe(false); // non-leap year
    expect(isValidCalendarDate('2026-02-29')).toBe(false);
    expect(isValidCalendarDate('2026-04-31')).toBe(false); // April has 30 days
    expect(isValidCalendarDate('2026-04-30')).toBe(true);
    expect(isValidCalendarDate('0999-01-01')).toBe(false); // year < 1000
    expect(isValidCalendarDate('10000-01-01')).toBe(false); // year > 9999
    expect(isValidCalendarDate('2026-13-01')).toBe(false); // invalid month
    expect(isValidCalendarDate('2026-00-10')).toBe(false);
    expect(isValidCalendarDate('')).toBe(false);
  });

  it('validates periods YYYY-MM across past, present, and future', () => {
    expect(isValidPeriod('2026-11')).toBe(true);
    expect(isValidPeriod('1999-01')).toBe(true);
    expect(isValidPeriod('2099-12')).toBe(true);
    expect(isValidPeriod('2026-00')).toBe(false);
    expect(isValidPeriod('2026-13')).toBe(false);
    expect(isValidPeriod('0999-12')).toBe(false);
    expect(isValidPeriod('10000-01')).toBe(false);
    expect(isValidPeriod('2026-5')).toBe(false);
    expect(isValidPeriod('invalid')).toBe(false);
  });

  it('validates period inputs returning Vietnamese guidance', () => {
    expect(validatePeriodInput('2026-11')).toBeNull();
    expect(validatePeriodInput('')).toBe('Vui lòng nhập kỳ thu.');
    expect(validatePeriodInput('2026-13')).toBe(
      'Kỳ thu phải đúng định dạng YYYY-MM (năm 1000–9999, tháng 01–12).',
    );
  });

  it('validates cash receipts date range adhering to Asia/Ho_Chi_Minh boundaries', () => {
    const fixedNow = new Date('2026-11-10T05:00:00Z'); // 12:00 PM in Vietnam
    const parts = getVietnamDateParts(fixedNow);
    expect(parts.todayDateString).toBe('2026-11-10');
    expect(parts.firstDayOfMonth).toBe('2026-11-01');

    // Valid range
    const valid = validateCashReceiptsRange('2026-11-01', '2026-11-10', fixedNow);
    expect(valid.isValid).toBe(true);

    // from > to
    const reversed = validateCashReceiptsRange('2026-11-10', '2026-11-01', fixedNow);
    expect(reversed.isValid).toBe(false);
    expect(reversed.fromError).toBe('Từ ngày không được sau đến ngày.');

    // to > today in Vietnam
    const future = validateCashReceiptsRange('2026-11-01', '2026-11-11', fixedNow);
    expect(future.isValid).toBe(false);
    expect(future.toError).toBe('Đến ngày không được sau hôm nay (theo giờ Việt Nam).');

    // Invalid calendar date
    const badDate = validateCashReceiptsRange('2026-02-29', '2026-11-10', fixedNow);
    expect(badDate.isValid).toBe(false);
    expect(badDate.fromError).toContain('Từ ngày không hợp lệ');
  });
});

describe('S11-UI Report API Client', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('exports fee collection report with correct endpoint, query allowlist, and headers', async () => {
    const fakeBlob = new Blob(['fake-fee-xlsx'], { type: XLSX_MIME });
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(fakeBlob, {
        status: 200,
        headers: {
          'Content-Type': `${XLSX_MIME};charset=UTF-8`,
          'Content-Disposition': 'attachment; filename="thu-theo-ky-2026-11.xlsx"',
          'X-Request-Id': 'req-fee-123',
        },
      }),
    );
    globalThis.fetch = fetchMock;

    const res = await exportFeeCollectionReport({ period: '2026-11' });
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/reports/fee-collection.xlsx?period=2026-11',
      expect.objectContaining({
        method: 'GET',
        credentials: 'include',
        headers: { Accept: XLSX_MIME },
      }),
    );
    expect(res.filename).toBe('thu-theo-ky-2026-11.xlsx');
    expect(res.blob.size).toBe(fakeBlob.size);
  });

  it('exports cash receipts report with strictly allowlisted query parameters', async () => {
    const fakeBlob = new Blob(['fake-cash-xlsx'], { type: XLSX_MIME });
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(fakeBlob, {
        status: 200,
        headers: {
          'Content-Type': XLSX_MIME,
          'Content-Disposition': 'attachment; filename="tien-mat-2026-11-01-2026-11-10.xlsx"',
        },
      }),
    );
    globalThis.fetch = fetchMock;

    const res = await exportCashReceiptsReport({
      from: '2026-11-01',
      to: '2026-11-10',
    });
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/reports/cash-receipts.xlsx?from=2026-11-01&to=2026-11-10',
      expect.objectContaining({
        method: 'GET',
        credentials: 'include',
      }),
    );
    expect(res.filename).toBe('tien-mat-2026-11-01-2026-11-10.xlsx');
  });

  it('exports residency report without query parameters and validates server date filename', async () => {
    const fakeBlob = new Blob(['fake-res-xlsx'], { type: XLSX_MIME });
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(fakeBlob, {
        status: 200,
        headers: {
          'Content-Type': XLSX_MIME,
          'Content-Disposition': 'attachment; filename="cu-tru-hien-tai-20261110.xlsx"',
        },
      }),
    );
    globalThis.fetch = fetchMock;

    const res = await exportResidencyReport();
    expect(fetchMock).toHaveBeenCalledWith(
      '/api/v1/reports/residency.xlsx',
      expect.objectContaining({
        method: 'GET',
        credentials: 'include',
      }),
    );
    expect(res.filename).toBe('cu-tru-hien-tai-20261110.xlsx');
  });

  it('rejects unexpected MIME type (such as HTML or JSON returned with 200)', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue(
      new Response('<html>Error</html>', {
        status: 200,
        headers: {
          'Content-Type': 'text/html',
          'Content-Disposition': 'attachment; filename="thu-theo-ky-2026-11.xlsx"',
          'X-Request-Id': 'req-html-200',
        },
      }),
    );

    await expect(exportFeeCollectionReport({ period: '2026-11' })).rejects.toThrow(ApiHttpError);
  });

  it('rejects empty blob with 200 status', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue(
      new Response('', {
        status: 200,
        headers: {
          'Content-Type': XLSX_MIME,
          'Content-Disposition': 'attachment; filename="thu-theo-ky-2026-11.xlsx"',
        },
      }),
    );

    await expect(exportFeeCollectionReport({ period: '2026-11' })).rejects.toThrow(ApiHttpError);
  });

  it('rejects missing or mismatched filename in Content-Disposition', async () => {
    const fakeBlob = new Blob(['data'], { type: XLSX_MIME });
    globalThis.fetch = vi.fn().mockResolvedValue(
      new Response(fakeBlob, {
        status: 200,
        headers: {
          'Content-Type': XLSX_MIME,
          'Content-Disposition': 'attachment; filename="wrong-name.xlsx"',
        },
      }),
    );

    await expect(exportFeeCollectionReport({ period: '2026-11' })).rejects.toThrow(
      'Không nhận được tệp đúng định dạng.',
    );
  });

  it('parses error problem detail for non-2xx responses', async () => {
    const problemJson = JSON.stringify({
      status: 400,
      code: 'INVALID_REPORT_FILTER',
      detail: 'Bộ lọc không hợp lệ',
    });
    globalThis.fetch = vi.fn().mockResolvedValue(
      new Response(problemJson, {
        status: 400,
        headers: {
          'Content-Type': 'application/problem+json',
          'X-Request-Id': 'req-400-err',
        },
      }),
    );

    try {
      await exportFeeCollectionReport({ period: 'invalid' });
      expect.unreachable('Should have thrown ApiHttpError');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiHttpError);
      const apiErr = err as ApiHttpError;
      expect(apiErr.problem.status).toBe(400);
      expect(apiErr.problem.code).toBe('INVALID_REPORT_FILTER');
      expect(apiErr.problem.requestId).toBe('req-400-err');
    }
  });

  it('triggers browser download and revokes Object URL', () => {
    const fakeBlob = new Blob(['excel-content'], { type: XLSX_MIME });
    window.URL.createObjectURL = vi.fn().mockReturnValue('blob:test');
    window.URL.revokeObjectURL = vi.fn();
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});

    const cleanup = triggerBlobDownload(fakeBlob, 'test.xlsx');
    expect(window.URL.createObjectURL).toHaveBeenCalledWith(fakeBlob);
    expect(clickSpy).toHaveBeenCalled();

    // Call cleanup immediately
    cleanup();
    expect(window.URL.revokeObjectURL).toHaveBeenCalledWith('blob:test');
    clickSpy.mockRestore();
  });
});
