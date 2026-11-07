import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiHttpError, clearCsrfMemory, manageAccountApi } from '../api/client';
import { messageForError } from '../api/errors';
import { maskTail, toChange, toHousehold, toMyResident, toResident } from '../api/residency';

afterEach(() => {
  vi.unstubAllGlobals();
  clearCsrfMemory();
});

describe('S05 mapping', () => {
  it('masks identity numbers to the last 4 characters', () => {
    expect(maskTail('079012345678')).toBe('********5678');
    expect(maskTail('1234')).toBe('1234');
    expect(maskTail(null)).toBeNull();
  });

  it('maps backend enums to the labels the screens use', () => {
    const r = toResident({
      id: 1, householdId: 2, fullName: 'A', identityNumber: '079012345678', dateOfBirth: null,
      phone: '0901234567', relationshipToHead: null, residencyStatus: 'MOVED_OUT',
    });
    expect(r.residencyStatus).toBe('Đã chuyển đi');
    expect(r.identityNumberMasked).toBe('********5678');
    expect(r.relationshipToHead).toBe('');
    expect(toChange({ id: 1, residentId: 1, changeType: 'MOVE_OUT', effectiveDate: '2026-11-01', status: 'APPROVED', recordedBy: 1 }, 2).changeType).toBe('CHUYEN_DI');
  });

  it('never shows an identity number to a resident (D20)', () => {
    const m = toMyResident({ id: 1, fullName: 'B', dateOfBirth: null, relationshipToHead: 'Con', residencyStatus: 'RESIDING' }, 9);
    expect(m.identityNumberMasked).toBeNull();
    expect(m.householdId).toBe(9);
  });

  it('keeps the area as the decimal string and has no member count yet', () => {
    const h = toHousehold({ id: 1, householdCode: 'H1', apartmentCode: 'A01', areaM2: 50, address: null, status: 'ACTIVE' });
    expect(h.areaM2).toBe('50');
    expect(h.address).toBe('');
    expect(h.currentResidents).toBeNull();
  });
});

describe('error messages', () => {
  it('uses the code, not the raw server text, and never echoes an identity number', () => {
    const msg = messageForError(new ApiHttpError({ status: 409, code: 'IDENTITY_NUMBER_ALREADY_USED', detail: 'Thông tin bị trùng.' }));
    expect(msg).toContain('trùng');
    expect(msg).not.toMatch(/\d{9}/);
  });
  it('falls back by status and treats a non-API failure as a network problem', () => {
    expect(messageForError(new ApiHttpError({ status: 403 }))).toContain('quyền');
    expect(messageForError(new TypeError('Failed to fetch'))).toContain('Không kết nối');
  });
});

describe('S04 call', () => {
  it('POSTs /accounts/{id}/disable with the CSRF header and accepts 204', async () => {
    const calls: Array<[string, RequestInit | undefined]> = [];
    vi.stubGlobal('fetch', async (url: string, init?: RequestInit) => {
      calls.push([url, init]);
      if (url === '/api/v1/auth/csrf') return new Response(JSON.stringify({ headerName: 'X-CSRF-TOKEN', token: 'tok' }), { status: 200 });
      return new Response(null, { status: 204 });
    });
    await manageAccountApi(7, 'disable');
    const [url, init] = calls[calls.length - 1];
    expect(url).toBe('/api/v1/accounts/7/disable');
    expect(init?.method).toBe('POST');
    expect((init?.headers as Record<string, string>)['X-CSRF-TOKEN']).toBe('tok');
  });
});
