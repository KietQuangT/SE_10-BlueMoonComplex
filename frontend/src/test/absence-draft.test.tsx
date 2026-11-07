import { act, renderHook, waitFor } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import { ApiHttpError } from '../api/client';
import { AppStateProvider, useAppState } from '../state/AppStateContext';

const user = { username: 'resident', role: 'RESIDENT', householdId: 1 };
afterEach(() => vi.unstubAllGlobals());
it('starts the API draft and resident list empty and keeps the draft after re-login', async () => {
  vi.stubGlobal('fetch', vi.fn(async (url: string) => {
    if (url.includes('/auth/csrf')) return new Response(JSON.stringify({ headerName: 'X-CSRF-TOKEN', token: 'test' }));
    if (url.includes('/auth/')) return new Response(JSON.stringify(user));
    if (url.includes('/residency-declarations')) return new Response(JSON.stringify({ content: [], totalElements: 0 }));
    return new Response(JSON.stringify([]));
  }));
  const { result } = renderHook(() => useAppState(), { wrapper: AppStateProvider });
  expect(result.current.absenceDraft).toEqual({ residentId: '', effectiveDate: '', endDateAssumed: '', reason: '' });
  expect(result.current.residents).toEqual([]);
  await waitFor(() => expect(result.current.user?.username).toBe('resident'));
  act(() => result.current.setAbsenceDraft({ residentId: '12', effectiveDate: '2027-01-01', endDateAssumed: '', reason: 'Công tác' }));
  act(() => { result.current.handleCallFailure(new ApiHttpError({ status: 401 })); });
  await act(async () => { await result.current.login('resident', 'password'); });
  expect(result.current.absenceDraft.reason).toBe('Công tác');
  expect(result.current.absenceDraft.residentId).toBe('12');
});
