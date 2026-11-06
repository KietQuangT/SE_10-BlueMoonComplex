import { describe, expect, it } from 'vitest';
import {
  defaultHomeByRole,
  formatAreaM2,
  formatPeriod,
  formatVnd,
  maskIdentityNumber,
  normalizeApartmentCode,
  roleDisplayName,
  sumVndStrings,
} from '../lib/format';

describe('format utilities & numerical integrity', () => {
  it('formats VND without IEEE-754 precision loss', () => {
    expect(formatVnd('500000')).toBe('500.000 đ');
    expect(formatVnd('12500000')).toBe('12.500.000 đ');
    expect(formatVnd('0')).toBe('0 đ');
    expect(formatVnd(null)).toBe('0 đ');
  });

  it('performs exact BigInt summation with sumVndStrings without floats', () => {
    const amounts = ['500000', '60000', '12500000'];
    expect(sumVndStrings(amounts)).toBe('13060000');
  });

  it('normalizes apartment codes correctly (A1 -> A01)', () => {
    expect(normalizeApartmentCode('a1')).toBe('A01');
    expect(normalizeApartmentCode('B5')).toBe('B05');
    expect(normalizeApartmentCode('A01')).toBe('A01');
    expect(normalizeApartmentCode('P30')).toBe('P30');
  });

  it('masks identity numbers according to PII security rules', () => {
    expect(maskIdentityNumber('001234567890')).toBe('********7890');
    expect(maskIdentityNumber('079090001234')).toBe('********1234');
    expect(maskIdentityNumber(null)).toBeNull();
    expect(maskIdentityNumber('')).toBeNull();
  });

  it('formats area in m² with 2 decimal places', () => {
    expect(formatAreaM2('50')).toBe('50,00 m²');
    expect(formatAreaM2('72.5')).toBe('72,50 m²');
    expect(formatAreaM2(null)).toBe('0,00 m²');
  });

  it('formats billing period YYYY-MM to MM/YYYY', () => {
    expect(formatPeriod('2026-10')).toBe('10/2026');
    expect(formatPeriod('2026-11')).toBe('11/2026');
  });

  it('provides correct default home paths per role', () => {
    expect(defaultHomeByRole('ADMIN')).toBe('/admin/households');
    expect(defaultHomeByRole('ACCOUNTANT')).toBe('/accountant/collections');
    expect(defaultHomeByRole('RESIDENT')).toBe('/resident/invoices');
    expect(defaultHomeByRole('AUTHORITY')).toBe('/authority/reports');
  });

  it('provides correct Vietnamese display names for roles', () => {
    expect(roleDisplayName('ADMIN')).toBe('Ban quản trị');
    expect(roleDisplayName('ACCOUNTANT')).toBe('Kế toán');
    expect(roleDisplayName('RESIDENT')).toBe('Cư dân');
    expect(roleDisplayName('AUTHORITY')).toBe('Cơ quan chức năng');
  });
});
