/**
 * Date and period validation utilities for S11-UI reports in Asia/Ho_Chi_Minh timezone (+07:00).
 */

export interface VietnamDateParts {
  year: number;
  month: number;
  day: number;
  currentPeriod: string; // YYYY-MM
  todayDateString: string; // YYYY-MM-DD
  firstDayOfMonth: string; // YYYY-MM-01
}

export function getVietnamDateParts(now: Date = new Date()): VietnamDateParts {
  const formatter = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Ho_Chi_Minh',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
  const parts = formatter.format(now).split('-');
  const year = Number(parts[0]);
  const month = Number(parts[1]);
  const day = Number(parts[2]);
  const yearStr = String(year).padStart(4, '0');
  const monthStr = String(month).padStart(2, '0');
  const dayStr = String(day).padStart(2, '0');

  return {
    year,
    month,
    day,
    currentPeriod: `${yearStr}-${monthStr}`,
    todayDateString: `${yearStr}-${monthStr}-${dayStr}`,
    firstDayOfMonth: `${yearStr}-${monthStr}-01`,
  };
}

export function isLeapYear(year: number): boolean {
  return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0;
}

export function isValidCalendarDate(dateStr: string): boolean {
  if (!dateStr || typeof dateStr !== 'string') return false;
  const match = /^[0-9]{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])$/.exec(dateStr.trim());
  if (!match) return false;

  const [yearStr, monthStr, dayStr] = dateStr.trim().split('-');
  const year = Number(yearStr);
  const month = Number(monthStr);
  const day = Number(dayStr);

  if (year < 1000 || year > 9999) return false;

  const daysInMonth = [
    31,
    isLeapYear(year) ? 29 : 28,
    31,
    30,
    31,
    30,
    31,
    31,
    30,
    31,
    30,
    31,
  ];

  return day >= 1 && day <= daysInMonth[month - 1];
}

export function isValidPeriod(periodStr: string): boolean {
  if (!periodStr || typeof periodStr !== 'string') return false;
  const match = /^[0-9]{4}-(?:0[1-9]|1[0-2])$/.exec(periodStr.trim());
  if (!match) return false;

  const [yearStr] = periodStr.trim().split('-');
  const year = Number(yearStr);
  return year >= 1000 && year <= 9999;
}

export function validatePeriodInput(periodStr: string): string | null {
  const trimmed = periodStr.trim();
  if (!trimmed) {
    return 'Vui lòng nhập kỳ thu.';
  }
  if (!isValidPeriod(trimmed)) {
    return 'Kỳ thu phải đúng định dạng YYYY-MM (năm 1000–9999, tháng 01–12).';
  }
  return null;
}

export interface CashReceiptsRangeValidation {
  fromError: string | null;
  toError: string | null;
  rangeError: string | null;
  isValid: boolean;
}

export function validateCashReceiptsRange(
  fromStr: string,
  toStr: string,
  now: Date = new Date(),
): CashReceiptsRangeValidation {
  const trimmedFrom = fromStr.trim();
  const trimmedTo = toStr.trim();

  let fromError: string | null = null;
  let toError: string | null = null;
  let rangeError: string | null = null;

  if (!trimmedFrom) {
    fromError = 'Vui lòng chọn từ ngày.';
  } else if (!isValidCalendarDate(trimmedFrom)) {
    fromError = 'Từ ngày không hợp lệ. Kiểm tra năm (1000–9999), tháng và ngày thực tế.';
  }

  if (!trimmedTo) {
    toError = 'Vui lòng chọn đến ngày.';
  } else if (!isValidCalendarDate(trimmedTo)) {
    toError = 'Đến ngày không hợp lệ. Kiểm tra năm (1000–9999), tháng và ngày thực tế.';
  }

  if (!fromError && !toError) {
    const today = getVietnamDateParts(now).todayDateString;
    if (trimmedTo > today) {
      toError = 'Đến ngày không được sau hôm nay (theo giờ Việt Nam).';
    } else if (trimmedFrom > trimmedTo) {
      rangeError = 'Từ ngày không được sau đến ngày.';
      fromError = 'Từ ngày không được sau đến ngày.';
    }
  }

  const isValid = !fromError && !toError && !rangeError;
  return {
    fromError,
    toError,
    rangeError,
    isValid,
  };
}
