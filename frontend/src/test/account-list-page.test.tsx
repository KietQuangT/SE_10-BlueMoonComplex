import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AccountsPage } from '../screens/admin/AccountsPage';
import { fetchAccountPage } from '../api/client';
vi.mock('../api/client', () => ({ fetchAccountPage: vi.fn() }));
const state = {
  user: { username: 'admin', role: 'ADMIN' }, mode: 'api', accounts: [], households: [],
  manageAccount: vi.fn(), forcedUiState: 'normal', setForcedUiState: vi.fn(),
  handleCallFailure: vi.fn(() => 'Không tải được danh sách.'),
};
vi.mock('../state/AppStateContext', () => ({ useAppState: () => state }));
const item = (id: number, username: string) => ({ id, username, fullName: username, email: null,
  role: 'RESIDENT' as const, householdId: null, status: 'ACTIVE' as const, locked: false });
const show = () => render(<MemoryRouter><AccountsPage /></MemoryRouter>);
describe('Danh sách tài khoản API', () => {
  beforeEach(() => { vi.clearAllMocks(); state.user = { username: 'admin', role: 'ADMIN' }; });
  afterEach(cleanup);
  it('loads all pages', async () => {
    vi.mocked(fetchAccountPage).mockResolvedValueOnce({ content: [item(1, 'first')], page: 0, totalPages: 2, totalElements: 2 })
      .mockResolvedValueOnce({ content: [item(2, 'second')], page: 1, totalPages: 2, totalElements: 2 });
    show();
    expect(await screen.findAllByText('first')).toHaveLength(2);
    expect(screen.getAllByText('second')).toHaveLength(2);
    expect(fetchAccountPage).toHaveBeenCalledTimes(2);
  });
  it('shows a retry on failure and recovers', async () => {
    vi.mocked(fetchAccountPage).mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValueOnce({ content: [item(1, 'recovered')], page: 0, totalPages: 1, totalElements: 1 });
    show();
    expect(await screen.findByText('Không tải được danh sách.')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Thử lại' }));
    expect(await screen.findAllByText('recovered')).toHaveLength(2);
  });
});
