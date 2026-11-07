import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ApiAbsenceApprovalsPage } from '../screens/admin/ApiAbsenceApprovalsPage';
import * as api from '../api/residency';
import '../styles/app.css';

vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ handleCallFailure: () => 'Lỗi', refreshAbsenceCount: async () => {} }) }));
vi.mock('../api/residency', async (load) => ({ ...(await load<typeof import('../api/residency')>()), fetchDeclarations: vi.fn(async () => ({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 })), suggestResidents: vi.fn(async () => [{ residentId: 1, fullName: 'An Bình', householdId: 2, householdCode: 'H02', apartmentCode: 'A02' }]) }));
afterEach(() => { cleanup(); vi.clearAllMocks(); vi.useRealTimers(); });
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

it('ignores a late suggestion response after a newer query', async () => {
  vi.useFakeTimers();
  let resolveOld!: (items: api.ResidentSuggestion[]) => void;
  vi.mocked(api.suggestResidents).mockImplementationOnce(() => new Promise((resolve) => { resolveOld = resolve; })).mockResolvedValueOnce([{ residentId: 2, fullName: 'Bình An', householdId: 2, householdCode: 'H02', apartmentCode: 'A02' }]);
  render(<ApiAbsenceApprovalsPage />);
  const input = screen.getByRole('combobox');
  fireEvent.change(input, { target: { value: 'An' } });
  await act(async () => { vi.advanceTimersByTime(250); });
  fireEvent.change(input, { target: { value: 'Bình' } });
  await act(async () => { vi.advanceTimersByTime(250); });
  await act(async () => { resolveOld([{ residentId: 1, fullName: 'An cũ', householdId: 1, householdCode: 'H01', apartmentCode: 'A01' }]); });
  expect(screen.getByRole('option', { name: /Bình An/ })).toHaveTextContent('Bình An');
  expect(screen.queryByText(/An cũ/)).not.toBeInTheDocument();
});

it.each(['', 'a'.repeat(256)])('requires a rejection reason of at most 255 characters', async (reason) => {
  vi.mocked(api.fetchDeclarations).mockResolvedValueOnce({ content: [{ id: 4, residentName: 'An', apartmentCode: 'A01', effectiveDate: '2099-01-01', endDate: null, reason: 'Việc riêng', status: 'PENDING' } as api.DeclarationDto], page: 0, size: 10, totalElements: 1, totalPages: 1 });
  const reject = vi.spyOn(api, 'rejectDeclaration');
  render(<ApiAbsenceApprovalsPage />);
  await screen.findByText('Việc riêng');
  fireEvent.click(screen.getAllByRole('button', { name: 'Từ chối' }).at(-1)!);
  fireEvent.change(screen.getByLabelText('Lý do từ chối (bắt buộc)', { selector: 'textarea' }), { target: { value: reason } });
  fireEvent.click(screen.getByText('Từ chối đơn', { selector: 'button' }));
  expect(document.querySelector('[role=alert]')).toHaveTextContent(reason ? 'Lý do từ chối tối đa 255 ký tự.' : 'Vui lòng nhập lý do từ chối.');
  expect(screen.getByLabelText('Lý do từ chối (bắt buộc)').closest('form')).toHaveAttribute('novalidate');
  expect(reject).not.toHaveBeenCalled();
  reject.mockRestore();
});

it('filters typed names on Enter and renders an overlay', async () => {
  render(<ApiAbsenceApprovalsPage />); const input = screen.getByRole('combobox');
  fireEvent.change(input, { target: { value: 'a' } }); fireEvent.keyDown(input, { key: 'Enter' });
  await act(async () => {}); expect(api.fetchDeclarations).not.toHaveBeenCalledWith(expect.objectContaining({ q: 'a' }));
  fireEvent.change(input, { target: { value: 'an' } });
  expect(document.querySelector('.suggestion-overlay')).toHaveStyle({ position: 'absolute', width: '100%', overflowY: 'auto' });
  expect(document.querySelector('.suggestion-anchor')).toContainElement(document.querySelector('.suggestion-overlay'));
  fireEvent.keyDown(input, { key: 'Enter' }); await act(async () => {});
  expect(api.fetchDeclarations).toHaveBeenLastCalledWith(expect.objectContaining({ q: 'an' }));
});

it('clears a previous decision error when opening a new rejection', async () => {
  vi.mocked(api.fetchDeclarations).mockResolvedValue({ content: [{ id: 4, residentName: 'An', apartmentCode: 'A01', effectiveDate: '2099-01-01', endDate: null, reason: 'Việc riêng', status: 'PENDING' } as api.DeclarationDto], page: 0, size: 10, totalElements: 1, totalPages: 1 });
  render(<ApiAbsenceApprovalsPage />); await screen.findByText('Việc riêng');
  fireEvent.click(screen.getAllByRole('button', { name: 'Từ chối' }).at(-1)!);
  fireEvent.click(screen.getByText('Từ chối đơn', { selector: 'button' }));
  expect(screen.getByText('Vui lòng nhập lý do từ chối.')).toBeInTheDocument();
  fireEvent.click(screen.getByText('Hủy', { selector: 'button' }));
  fireEvent.click(screen.getAllByRole('button', { name: 'Từ chối' }).at(-1)!);
  expect(screen.queryByText('Vui lòng nhập lý do từ chối.')).not.toBeInTheDocument();
});
