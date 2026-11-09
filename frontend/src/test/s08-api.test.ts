import { describe, expect, it, vi } from 'vitest';
import { listBillingPeriods } from '../api/billing';
import { recordContribution } from '../api/voluntary';
import * as client from '../api/client';

vi.mock('../api/client', () => ({ getAuthenticated: vi.fn(), sendAuthenticatedWrite: vi.fn() }));

describe('S08 API contract', () => {
  it('requests the selected billing page', () => {
    listBillingPeriods(2);
    expect(client.getAuthenticated).toHaveBeenCalledWith('/api/v1/billing-periods?page=2&size=10');
  });
  it('sends the contribution key with the unchanged string amount', () => {
    const body = { residentId: 4, amount: '999999999999999999', paidOn: '2026-11-08', lateReason: '' };
    recordContribution(3, body, 'key');
    expect(client.sendAuthenticatedWrite).toHaveBeenCalledWith('/api/v1/voluntary-campaigns/3/contributions', 'POST', body, { 'Idempotency-Key': 'key' });
  });
});
