import { afterEach, expect, it, vi } from 'vitest';
import { clearCsrfMemory, sendAuthenticatedWrite } from '../api/client';

afterEach(() => { clearCsrfMemory(); vi.unstubAllGlobals(); });
it('sends the same idempotency key alongside the CSRF token on a contribution write', async () => {
  const fetchMock = vi.fn().mockResolvedValueOnce(new Response(JSON.stringify({ headerName: 'X-CSRF-TOKEN', token: 'csrf' }), { status: 200 })).mockResolvedValueOnce(new Response('{}', { status: 201 }));
  vi.stubGlobal('fetch', fetchMock);
  await sendAuthenticatedWrite('/api/v1/voluntary-campaigns/1/contributions', 'POST', { amount: '999999999999999999' }, { 'Idempotency-Key': 'unique-key' });
  expect(fetchMock.mock.calls[1][1].headers).toMatchObject({ 'X-CSRF-TOKEN': 'csrf', 'Idempotency-Key': 'unique-key' });
  expect(JSON.parse(fetchMock.mock.calls[1][1].body).amount).toBe('999999999999999999');
});
