import { act, renderHook } from '@testing-library/react';
import React from 'react';
import { describe, expect, it } from 'vitest';
import { AppStateProvider, useAppState } from '../state/AppStateContext';

function wrapper({ children }: { children: React.ReactNode }) {
  return <AppStateProvider>{children}</AppStateProvider>;
}

describe('AppStateContext session & draft resilience', () => {
  it('allows logging into demo roles cleanly', async () => {
    const { result } = renderHook(() => useAppState(), { wrapper });

    act(() => {
      result.current.loginDemoAs('resident');
    });

    expect(result.current.user).not.toBeNull();
    expect(result.current.user?.role).toBe('RESIDENT');
    expect(result.current.user?.username).toBe('demo-cudan');
  });

  it('preserves absence form draft across session expiration (M-04 resilience rule)', () => {
    const { result } = renderHook(() => useAppState(), { wrapper });

    act(() => {
      result.current.loginDemoAs('resident');
      result.current.setAbsenceDraft({
        residentId: '1',
        effectiveDate: '2026-11-20',
        endDateAssumed: '2026-11-28',
        reason: 'Công tác dài ngày tại Đà Nẵng',
      });
    });

    // Simulate session expiration trigger
    act(() => {
      result.current.triggerSessionExpired('/resident/absence');
    });

    // User is logged out, banner is shown
    expect(result.current.user).toBeNull();
    expect(result.current.sessionExpiredBanner).toBe(true);
    expect(result.current.returnToPath).toBe('/resident/absence');

    // Draft MUST remain intact so resident does not lose data!
    expect(result.current.absenceDraft.reason).toBe('Công tác dài ngày tại Đà Nẵng');
    expect(result.current.absenceDraft.effectiveDate).toBe('2026-11-20');
  });

  it('rejects duplicate fee rate additions on the same effective date', () => {
    const { result } = renderHook(() => useAppState(), { wrapper });

    const duplicateAttempt = result.current.addFeeRateDemo({
      code: 'QUAN_LY',
      name: 'Phí quản lý',
      unit: 'm2',
      unitPrice: '12000',
      effectiveFrom: '2026-01-01', // Already exists in INITIAL_FEE_RATES
    });

    expect(duplicateAttempt.ok).toBe(false);
    expect(duplicateAttempt.error).toContain('Đã tồn tại mức đơn giá');
  });

  it('safely collects cash payment and marks invoice as DA_DONG with receipt', () => {
    const { result } = renderHook(() => useAppState(), { wrapper });

    act(() => {
      result.current.loginDemoAs('accountant');
    });

    let receipt: any = null;
    act(() => {
      receipt = result.current.collectInvoiceCashDemo(1, 'Đã nhận đủ tiền mặt');
    });

    expect(receipt).not.toBeNull();
    expect(receipt.amount).toBe('560000');
    expect(receipt.apartmentCode).toBe('A01');

    // Verify invoice status changed
    const updatedInvoice = result.current.invoices.find((i) => i.id === 1);
    expect(updatedInvoice?.status).toBe('DA_DONG');
    expect(updatedInvoice?.remaining).toBe('0');
  });
});
