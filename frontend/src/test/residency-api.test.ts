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

describe('S06 contract', () => {
  it('maps all change types and statuses', () => {
    const base = { id: 1, residentId: 2, effectiveDate: '2026-11-07', recordedBy: 1, reason: null, endDate: null, decisionNote: null, decidedAt: null };
    for (const [changeType, expected] of [['TEMPORARY_ABSENCE', 'TAM_VANG'], ['MOVE_OUT', 'CHUYEN_DI'], ['RETURN', 'QUAY_LAI']] as const) {
      for (const status of ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'] as const) {
        expect(toChange({ ...base, changeType, status }, 3)).toMatchObject({ changeType: expected, status });
      }
    }
  });
});

describe('S06 endpoints', () => {
  it('sends each route with the expected method, body and CSRF header', async () => {
    const api = await import('../api/residency');
    const calls: Array<[string, RequestInit | undefined]> = [];
    vi.stubGlobal('fetch', async (url: string, init?: RequestInit) => {
      calls.push([url, init]);
      const declaration = { id: 3, residentId: 4, residentName: 'An', householdId: 2, householdCode: 'H02', apartmentCode: 'A02', changeType: 'TEMPORARY_ABSENCE', status: 'PENDING', effectiveDate: '2026-11-07', endDate: null, reason: 'Công tác', decisionNote: null, decidedAt: null, createdAt: '2026-11-07T00:00:00Z', cancellable: false };
      const payload = url === '/api/v1/auth/csrf' ? { headerName: 'X-CSRF-TOKEN', token: 'tok' } : url.includes('/suggest') ? [{ residentId: 4, fullName: 'An', householdId: 2, householdCode: 'H02', apartmentCode: 'A02' }] : url.includes('?') ? { content: [declaration], page: 0, size: 10, totalElements: 1, totalPages: 1 } : declaration;
      return new Response(JSON.stringify(payload), { status: 200 });
    });
    expect(await api.createDeclaration({ residentId: 4, effectiveDate: '2026-11-07', reason: 'Công tác' })).toMatchObject({ residentName: 'An', status: 'PENDING' });
    expect((await api.fetchMyDeclarations(1, 10)).content[0]).toMatchObject({ id: 3, reason: 'Công tác' });
    await api.cancelDeclaration(3);
    await api.fetchDeclarations({ status: 'PENDING', q: '', apartment: ' ', page: 0, size: 10 });
    await api.approveDeclaration(3);
    await api.rejectDeclaration(3, 'Thiếu thông tin');
    await api.returnResident(4);
    await api.suggestResidents('An');
    const requests = calls.filter(([url]) => url !== '/api/v1/auth/csrf');
    expect(requests.map(([url]) => url)).toEqual(['/api/v1/residency-declarations', '/api/v1/residency-declarations/mine?page=1&size=10', '/api/v1/residency-declarations/3/cancel', '/api/v1/residency-declarations?status=PENDING&page=0&size=10', '/api/v1/residency-declarations/3/approve', '/api/v1/residency-declarations/3/reject', '/api/v1/residents/4/return', '/api/v1/residents/suggest?q=An']);
    requests.forEach(([, init], i) => { expect(init?.method).toBe([1, 3, 7].includes(i) ? 'GET' : 'POST'); if (![1, 3, 7].includes(i)) expect((init?.headers as Record<string, string>)['X-CSRF-TOKEN']).toBe('tok'); });
    expect(JSON.parse(String(requests[0][1]?.body))).toEqual({ residentId: 4, effectiveDate: '2026-11-07', reason: 'Công tác' });
    expect(JSON.parse(String(requests[5][1]?.body))).toEqual({ decisionNote: 'Thiếu thông tin' });
  });
  it('provides actionable messages for every S06 error', () => {
    const expected: Record<string, string> = { INVALID_DATE: 'Ngày không hợp lệ', VALIDATION: 'Thông tin chưa hợp lệ', INVALID_FILTER: 'Bộ lọc chưa hợp lệ', RESIDENT_NOT_AVAILABLE: 'Nhân khẩu không còn đủ điều kiện', DECLARATION_ALREADY_PENDING: 'Nhân khẩu đã có đơn chờ duyệt', DECLARATION_NOT_PENDING: 'Đơn không còn chờ duyệt', DECLARATION_NOT_FOUND: 'Không tìm thấy đơn', RESIDENT_NOT_ABSENT: 'Nhân khẩu không còn tạm vắng' };
    for (const [code, phrase] of Object.entries(expected)) expect(messageForError(new ApiHttpError({ status: 400, code }))).toContain(phrase);
  });
});
