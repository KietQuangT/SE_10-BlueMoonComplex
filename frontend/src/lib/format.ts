/**
 * Formatting helpers per 09-quy-uoc-ui.md.
 * Money and area stay strings — never parsed into IEEE-754 floats for arithmetic.
 */

export function formatVnd(rawAmount: string | null | undefined): string {
  if (!rawAmount) return '0 đ';
  const cleaned = rawAmount.trim();
  const isNegative = cleaned.startsWith('-');
  const digitsOnly = cleaned.replace(/[^0-9]/g, '') || '0';
  const normalized = digitsOnly.replace(/^0+(?=\d)/, '');
  const grouped = normalized.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${isNegative && normalized !== '0' ? '-' : ''}${grouped} đ`;
}

export function formatAreaM2(rawArea: string | null | undefined): string {
  if (!rawArea) return '0,00 m²';
  const [intPart = '0', decPart = '00'] = rawArea.trim().split('.');
  const groupedInt = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  const paddedDec = (decPart + '00').slice(0, 2);
  return `${groupedInt},${paddedDec} m²`;
}

export function formatBusinessDate(isoDate: string | null | undefined): string {
  if (!isoDate) return '—';
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(isoDate.trim());
  if (!match) return isoDate;
  return `${match[3]}/${match[2]}/${match[1]}`;
}

export function formatInstantVn(isoUtc: string | null | undefined): string {
  if (!isoUtc) return '—';
  const date = new Date(isoUtc);
  if (Number.isNaN(date.getTime())) return isoUtc;
  const parts = new Intl.DateTimeFormat('en-GB', {
    timeZone: 'Asia/Ho_Chi_Minh',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).formatToParts(date);
  const lookup = Object.fromEntries(parts.map((p) => [p.type, p.value]));
  return `${lookup.day}/${lookup.month}/${lookup.year} ${lookup.hour}:${lookup.minute}`;
}

export function formatPeriod(yyyyMm: string | null | undefined): string {
  if (!yyyyMm) return '—';
  const match = /^(\d{4})-(\d{2})$/.exec(yyyyMm.trim());
  if (!match) return yyyyMm;
  return `${match[2]}/${match[1]}`;
}

export function normalizeApartmentCode(input: string): string {
  const trimmed = input.trim().toUpperCase();
  const compactMatch = /^([A-Z])(\d)$/.exec(trimmed);
  if (compactMatch) {
    return `${compactMatch[1]}0${compactMatch[2]}`;
  }
  return trimmed;
}

export function maskIdentityNumber(raw: string | null | undefined): string | null {
  if (!raw || !raw.trim()) return null;
  const trimmed = raw.trim();
  if (trimmed.startsWith('****')) return trimmed;
  const digits = trimmed.replace(/\s+/g, '');
  const last4 = digits.slice(-4);
  return `********${last4}`;
}

export function roleDisplayName(role: string): string {
  switch (role) {
    case 'ADMIN':
      return 'Ban quản trị';
    case 'ACCOUNTANT':
      return 'Kế toán';
    case 'RESIDENT':
      return 'Cư dân';
    default:
      return role;
  }
}

/**
 * Exact integer VND sum using BigInt (no IEEE-754 floats).
 * Only used in Demo mode to stand in for totals the server will return once
 * S08/S10 APIs exist; never used for amounts sent back to the server.
 */
export function sumVndStrings(amounts: string[]): string {
  let total = 0n;
  for (const raw of amounts) {
    const digits = raw.replace(/[^0-9-]/g, '');
    if (digits && digits !== '-') total += BigInt(digits);
  }
  return total.toString();
}

export function defaultHomeByRole(role: string): string {
  switch (role) {
    case 'ADMIN':
      return '/admin/households';
    case 'ACCOUNTANT':
      return '/accountant/collections';
    case 'RESIDENT':
      return '/resident/invoices';
    default:
      return '/login';
  }
}

export function formatVndDecimal(value: string | number): string {
  const [integer, fraction] = String(value).trim().replace(',', '.').split('.');
  const grouped = integer.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${grouped}${fraction && !/^0+$/.test(fraction) ? `,${fraction.padEnd(2, '0')}` : ''} đ`;
}

export function formatMonthYear(value: string): string {
  const match = /^(\d{4})-(\d{2})-\d{2}$/.exec(value);
  return match ? `${match[2]}/${match[1]}` : value;
}
