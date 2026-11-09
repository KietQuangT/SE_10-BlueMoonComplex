import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import { ApiAbsencePage } from '../screens/resident/ApiAbsencePage';
import { ApiAbsenceApprovalsPage } from '../screens/admin/ApiAbsenceApprovalsPage';
import * as residency from '../api/residency';
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: { householdId: 1 }, residents: [], absenceDraft: { residentId: '', effectiveDate: '', endDateAssumed: '', reason: '' }, setAbsenceDraft: vi.fn(), handleCallFailure: () => 'Không kết nối được máy chủ. Thử lại.', refreshAbsenceCount: vi.fn(), dataLoadError: false }) }));
vi.mock('../api/residency', () => ({ fetchMyDeclarations: vi.fn(), fetchDeclarations: vi.fn(), createDeclaration: vi.fn(), cancelDeclaration: vi.fn(), approveDeclaration: vi.fn(), rejectDeclaration: vi.fn(), suggestResidents: vi.fn().mockResolvedValue([]) }));
afterEach(() => { cleanup(); vi.clearAllMocks(); });
const empty = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };

it('offers an explicit retry instead of an endless loading label after resident history fails', async () => {
  vi.mocked(residency.fetchMyDeclarations).mockRejectedValueOnce(new Error('offline')).mockResolvedValue(empty);
  render(<ApiAbsencePage />);
  await screen.findByText('Không thể tải dữ liệu');
  expect(screen.queryByText('Đang tải danh sách...')).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Thử lại' }));
  await waitFor(() => expect(residency.fetchMyDeclarations).toHaveBeenCalledTimes(2));
  expect(await screen.findByText('Chưa có đơn của hộ.')).toBeInTheDocument();
});

it('lets administrators retry a failed approval list without reloading the whole application', async () => {
  vi.mocked(residency.fetchDeclarations).mockRejectedValueOnce(new Error('offline')).mockResolvedValue(empty);
  render(<ApiAbsenceApprovalsPage />);
  await screen.findByText('Không thể tải dữ liệu');
  fireEvent.click(screen.getByRole('button', { name: 'Thử lại' }));
  expect(await screen.findByText('Không có đơn phù hợp.')).toBeInTheDocument();
  expect(residency.fetchDeclarations).toHaveBeenCalledTimes(2);
});
