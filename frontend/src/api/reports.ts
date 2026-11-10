import { ApiHttpError, parseProblem } from './client';
import { isValidCalendarDate } from '../lib/reportDateUtils';

export const XLSX_MIME = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

export interface ReportDownloadResult {
  blob: Blob;
  filename: string;
}

export interface FeeCollectionFilter {
  period: string; // YYYY-MM
}

export interface CashReceiptsFilter {
  from: string; // YYYY-MM-DD
  to: string; // YYYY-MM-DD
}

function parseFilenameFromHeader(disposition: string | null): string | null {
  if (!disposition) return null;
  // Match filename="foo.xlsx" or filename*=UTF-8''foo.xlsx or filename=foo.xlsx
  const match = /filename\*?=(?:UTF-8'')?"?([^";\r\n]+)"?/i.exec(disposition);
  if (!match) return null;
  return match[1].trim();
}

async function handleExcelResponse(
  res: Response,
  validateExpectedFilename: (filename: string) => boolean,
): Promise<ReportDownloadResult> {
  const requestId = res.headers.get('X-Request-Id') || undefined;

  if (!res.ok) {
    const problem = await parseProblem(res);
    throw new ApiHttpError(problem);
  }

  if (res.status !== 200) {
    throw new ApiHttpError({
      status: res.status,
      code: 'INVALID_RESPONSE_STATUS',
      detail: 'Không nhận được tệp Excel hợp lệ. Thử lại hoặc báo mã yêu cầu.',
      requestId,
    });
  }

  const rawContentType = res.headers.get('Content-Type') || '';
  const mediaType = rawContentType.split(';')[0].trim().toLowerCase();
  if (mediaType !== XLSX_MIME) {
    throw new ApiHttpError({
      status: res.status,
      code: 'INVALID_MIME_TYPE',
      detail: 'Không nhận được tệp Excel hợp lệ. Thử lại hoặc báo mã yêu cầu.',
      requestId,
    });
  }

  const disposition = res.headers.get('Content-Disposition');
  const filename = parseFilenameFromHeader(disposition);
  if (!filename || !validateExpectedFilename(filename)) {
    throw new ApiHttpError({
      status: res.status,
      code: 'INVALID_FILENAME',
      detail: 'Không nhận được tệp đúng định dạng.',
      requestId,
    });
  }

  let blob: Blob;
  try {
    blob = await res.blob();
  } catch {
    throw new ApiHttpError({
      status: res.status,
      code: 'BODY_READ_FAILED',
      detail: 'Không nhận được tệp Excel hợp lệ. Thử lại hoặc báo mã yêu cầu.',
      requestId,
    });
  }

  if (blob.size === 0) {
    throw new ApiHttpError({
      status: res.status,
      code: 'EMPTY_BLOB',
      detail: 'Không nhận được tệp Excel hợp lệ. Thử lại hoặc báo mã yêu cầu.',
      requestId,
    });
  }

  return { blob, filename };
}

/**
 * FEE_COLLECTION_V1: GET /api/v1/reports/fee-collection.xlsx?period=YYYY-MM
 * Filename: thu-theo-ky-YYYY-MM.xlsx
 */
export async function exportFeeCollectionReport(
  filter: FeeCollectionFilter,
  signal?: AbortSignal,
): Promise<ReportDownloadResult> {
  const searchParams = new URLSearchParams();
  searchParams.set('period', filter.period.trim());
  const url = `/api/v1/reports/fee-collection.xlsx?${searchParams.toString()}`;

  const res = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: XLSX_MIME,
    },
    signal,
  });

  return handleExcelResponse(res, (fn) => fn === `thu-theo-ky-${filter.period.trim()}.xlsx`);
}

/**
 * CASH_RECEIPTS_V1: GET /api/v1/reports/cash-receipts.xlsx?from=YYYY-MM-DD&to=YYYY-MM-DD
 * Filename: tien-mat-FROM-TO.xlsx
 */
export async function exportCashReceiptsReport(
  filter: CashReceiptsFilter,
  signal?: AbortSignal,
): Promise<ReportDownloadResult> {
  const searchParams = new URLSearchParams();
  searchParams.set('from', filter.from.trim());
  searchParams.set('to', filter.to.trim());
  const url = `/api/v1/reports/cash-receipts.xlsx?${searchParams.toString()}`;

  const res = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: XLSX_MIME,
    },
    signal,
  });

  return handleExcelResponse(
    res,
    (fn) => fn === `tien-mat-${filter.from.trim()}-${filter.to.trim()}.xlsx`,
  );
}

/**
 * CURRENT_RESIDENCY_V1: GET /api/v1/reports/residency.xlsx (strictly NO query parameters)
 * Filename: cu-tru-hien-tai-YYYYMMDD.xlsx (server date in Asia/Ho_Chi_Minh)
 */
export async function exportResidencyReport(
  signal?: AbortSignal,
): Promise<ReportDownloadResult> {
  const url = '/api/v1/reports/residency.xlsx';

  const res = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: XLSX_MIME,
    },
    signal,
  });

  return handleExcelResponse(res, (fn) => {
    const match = /^cu-tru-hien-tai-(\d{4})(\d{2})(\d{2})\.xlsx$/.exec(fn);
    if (!match) return false;
    const [, y, m, d] = match;
    return isValidCalendarDate(`${y}-${m}-${d}`);
  });
}

/**
 * Triggers a browser download using a temporary object URL and anchor element.
 * Returns a cleanup function that revokes the object URL.
 */
export function triggerBlobDownload(blob: Blob, filename: string): () => void {
  const objectUrl = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = objectUrl;
  anchor.download = filename;
  anchor.style.display = 'none';
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);

  const timer = setTimeout(() => {
    window.URL.revokeObjectURL(objectUrl);
  }, 2000);

  return () => {
    clearTimeout(timer);
    window.URL.revokeObjectURL(objectUrl);
  };
}
