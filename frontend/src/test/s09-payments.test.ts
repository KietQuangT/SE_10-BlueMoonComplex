import { describe, expect, it, vi, beforeEach } from 'vitest';
import { previewReceipt, savePending, loadPending, clearPending, listPaymentInvoices, sendPending, type PendingPayment } from '../api/payments';
import * as client from '../api/client';
vi.mock('../api/client', () => ({ fetchCurrentUser: vi.fn(), getAuthenticated: vi.fn(), sendAuthenticatedWrite: vi.fn() }));

describe('S09 money and retry contract', () => {
  beforeEach(() => { sessionStorage.clear(); vi.clearAllMocks(); });
  it('splits partial and excess using exact integer VND', () => {
    expect(previewReceipt('530000', '200000')).toEqual({ applied: '200000', credit: '0', remaining: '330000' });
    expect(previewReceipt('330000', '400000')).toEqual({ applied: '330000', credit: '70000', remaining: '0' });
    expect(previewReceipt('0', '999999999999999999999999')?.credit).toBe('999999999999999999999999');
    expect(previewReceipt('100', '1.1')).toBeNull();
    expect(previewReceipt('100', '1000000000000000000000000')).toBeNull();
  });
  it('preserves the exact pending UUID and payload per account', () => {
    const command: PendingPayment = { kind: 'receipt', url: '/api/v1/invoices/4/receipts', body: { amount: '999999999999999999999999', paidOn: '2026-11-09', idempotencyKey: 'key' }, label: 'Căn A01' };
    savePending('alice', command);
    expect(loadPending('alice')).toEqual(command);
    expect(loadPending('bob')).toBeNull();
    sendPending(loadPending('alice')!, 'alice');
    expect(client.sendAuthenticatedWrite).toHaveBeenCalledWith(command.url, 'POST', command.body, undefined, expect.any(Function));
    clearPending('alice');
    expect(loadPending('alice')).toBeNull();
  });
  it('encodes filters without changing the amount or page', () => {
    listPaymentInvoices({ q: 'A%1', period: '2026-11', paymentStatus: 'PAID', page: 2 });
    expect(client.getAuthenticated).toHaveBeenCalledWith('/api/v1/payment-invoices?q=A%251&period=2026-11&paymentStatus=PAID&page=2&size=20');
  });
});
