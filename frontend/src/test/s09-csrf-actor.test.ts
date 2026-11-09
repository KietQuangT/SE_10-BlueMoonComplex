import { afterEach, expect, it, vi } from 'vitest';
import { clearCsrfMemory } from '../api/client';
import { PaymentActorChangedError, sendPending, type PendingPayment } from '../api/payments';
const session = (username: string) => new Response(JSON.stringify({ username, fullName: 'Test', role: 'ACCOUNTANT', householdId: null, mustChangePassword: false }), { status: 200 });
const csrf = () => new Response(JSON.stringify({ headerName: 'X-CSRF-TOKEN', token: 'csrf' }), { status: 200 });
afterEach(() => { clearCsrfMemory(); vi.unstubAllGlobals(); });
it('does not retry a CSRF-rejected write after the account changes', async () => {
  const fetchMock = vi.fn().mockResolvedValueOnce(csrf()).mockResolvedValueOnce(session('alice'))
    .mockResolvedValueOnce(new Response('{}', { status: 403 })).mockResolvedValueOnce(session('bob'))
    .mockResolvedValueOnce(csrf()).mockResolvedValueOnce(session('bob'));
  vi.stubGlobal('fetch', fetchMock);
  const command: PendingPayment = { kind: 'receipt', url: '/api/v1/invoices/4/receipts', body: { amount: '100', paidOn: '2026-11-09', idempotencyKey: 'key' }, label: 'Căn A01' };
  await expect(sendPending(command, 'alice')).rejects.toBeInstanceOf(PaymentActorChangedError);
  expect(fetchMock.mock.calls.filter((call) => call[1]?.method === 'POST')).toHaveLength(1);
});
