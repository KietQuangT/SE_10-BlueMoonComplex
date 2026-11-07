import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiAbsenceApprovalsPage } from '../screens/admin/ApiAbsenceApprovalsPage';
import * as api from '../api/residency';

vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ handleCallFailure: () => 'Lỗi', refreshAbsenceCount: async () => {} }) }));
vi.mock('../api/residency', async (load) => ({ ...(await load<typeof import('../api/residency')>()), fetchDeclarations: vi.fn(async () => ({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })), suggestResidents: vi.fn(async () => [{ residentId: 1, fullName: 'An Bình', householdId: 2, householdCode: 'H02', apartmentCode: 'A02' }]) }));
afterEach(() => { vi.clearAllMocks(); vi.useRealTimers(); });
describe('resident suggestions', () => {
  it('debounces, requires two characters, and selects with keyboard', async () => {
    vi.useFakeTimers();
    render(<ApiAbsenceApprovalsPage />);
    const input = screen.getByRole('combobox');
    fireEvent.change(input, { target: { value: 'A' } });
    await act(async () => { vi.advanceTimersByTime(300); });
    expect(api.suggestResidents).not.toHaveBeenCalled();
    fireEvent.change(input, { target: { value: 'An' } });
    await act(async () => { vi.advanceTimersByTime(249); });
    expect(api.suggestResidents).not.toHaveBeenCalled();
    await act(async () => { vi.advanceTimersByTime(1); });
    expect(api.suggestResidents).toHaveBeenCalledWith('An');
    expect(screen.getByRole('option', { name: /An Bình/ })).toHaveTextContent('H02');
    fireEvent.keyDown(input, { key: 'ArrowDown' });
    fireEvent.keyDown(input, { key: 'Enter' });
    expect(input).toHaveValue('An Bình');
    expect(input).toHaveAttribute('aria-expanded', 'false');
    await act(async () => {});
    expect(api.fetchDeclarations).toHaveBeenLastCalledWith(expect.objectContaining({ q: 'An Bình' }));
    fireEvent.change(input, { target: { value: '' } });
    await act(async () => {});
    expect(api.fetchDeclarations).toHaveBeenLastCalledWith(expect.objectContaining({ q: '' }));
  });
});
