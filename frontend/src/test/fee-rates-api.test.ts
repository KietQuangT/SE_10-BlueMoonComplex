import { afterEach, expect, it, vi } from 'vitest';
import { clearCsrfMemory } from '../api/client';
import { messageForError } from '../api/errors';
import { createFeeRate, deleteFeeRate, fetchFeeRates, fetchFeeTypes, updateFeeRate } from '../api/fees';
import { formatMonthYear, formatVndDecimal } from '../lib/format';

afterEach(() => { vi.unstubAllGlobals(); clearCsrfMemory(); });
it('sends fee requests through authenticated client with CSRF and accepts 204', async () => {
  const calls: Array<[string, RequestInit | undefined]> = [];
  vi.stubGlobal('fetch', async (url: string, init?: RequestInit) => {
    calls.push([url, init]);
    if (url.endsWith('/auth/csrf')) return new Response(JSON.stringify({ headerName: 'X-CSRF-TOKEN', token: 'token' }));
    if (init?.method === 'DELETE') return new Response(null, { status: 204 });
    return new Response('{}');
  });
  await fetchFeeTypes();
  await fetchFeeRates('2', 1, 25);
  await createFeeRate({ feeTypeId: 2, unitPrice: '7000.50', effectiveFrom: '2026-12-01' });
  await updateFeeRate(4, '8000');
  await deleteFeeRate(4);
  expect(calls[0][0]).toBe('/api/v1/fee-types');
  expect(calls[1][0]).toBe('/api/v1/fee-rates?page=1&size=25&feeTypeId=2');
  expect(calls[3][1]).toMatchObject({ method: 'POST', body: '{"feeTypeId":2,"unitPrice":"7000.50","effectiveFrom":"2026-12-01"}', headers: { 'X-CSRF-TOKEN': 'token' } });
  expect(calls[4]).toMatchObject(['/api/v1/fee-rates/4', { method: 'PATCH', body: '{"unitPrice":"8000"}', headers: { 'X-CSRF-TOKEN': 'token' } }]);
  expect(calls[5]).toMatchObject(['/api/v1/fee-rates/4', { method: 'DELETE', headers: { 'X-CSRF-TOKEN': 'token' } }]);
});
it('parses a fee conflict without exposing server text', async () => {
  vi.stubGlobal('fetch', async () => new Response(JSON.stringify({ code: 'FEE_RATE_FROZEN', detail: 'private server text' }), { status: 409 }));
  const error = await fetchFeeTypes().catch((err: unknown) => err);
  expect(messageForError(error)).toBe('Không sửa được đơn giá của tháng hiện tại hoặc tháng đã qua.');
});
it('formats decimal prices and months without arithmetic', () => {
  expect(formatVndDecimal(7000.5)).toBe('7.000,50 đ');
  expect(formatVndDecimal(7000.05)).toBe('7.000,05 đ');
  expect(formatVndDecimal(9999999999.99)).toBe('9.999.999.999,99 đ');
  expect(formatVndDecimal('7000.50')).toBe('7.000,50 đ');
  expect(formatVndDecimal(10000)).toBe('10.000 đ');
  expect(formatVndDecimal('10000.00')).toBe('10.000 đ');
  expect(formatMonthYear('2026-12-01')).toBe('12/2026');
});
