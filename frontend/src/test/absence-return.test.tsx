import { fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import { MoveOutPage } from '../screens/admin/MoveOutPage';
import { HouseholdDetailPage } from '../screens/admin/HouseholdDetailPage';

let role = 'ADMIN'; let mode = 'api';
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: { role }, mode, households: [{ id: 1, householdCode: 'H01', apartmentCode: 'A01', areaM2: '50', status: 'ACTIVE' }], residents: [{ id: 1, householdId: 1, fullName: 'An', residencyStatus: 'Tạm vắng' }, { id: 2, householdId: 1, fullName: 'Bình', residencyStatus: 'Đang cư trú' }], residencyChanges: [], accounts: [], loadHouseholdDetail: vi.fn(), handleCallFailure: () => 'Lỗi' }) }));
const show = () => render(<MemoryRouter initialEntries={['/admin/households/1']}><Routes><Route path="/admin/households/:id" element={<HouseholdDetailPage />} /></Routes></MemoryRouter>);
it('only shows return for an absent resident to an admin in api mode', () => {
  role = 'ADMIN'; mode = 'api'; const view = show();
  expect(screen.getByText('Thành viên hiện tại (2)')).toBeInTheDocument();
  expect(screen.getByText('Gồm người đang tạm vắng.')).toBeInTheDocument();
  expect(within(screen.getByText('An').closest('tr')!).getByRole('button', { name: 'Đã quay lại' })).toBeInTheDocument();
  expect(within(screen.getByText('Bình').closest('tr')!).queryByRole('button', { name: 'Đã quay lại' })).not.toBeInTheDocument();
  view.unmount(); role = 'RESIDENT'; const other = show(); expect(screen.queryByRole('button', { name: 'Đã quay lại' })).not.toBeInTheDocument();
  other.unmount(); role = 'ADMIN'; mode = 'demo'; show(); expect(screen.queryByRole('button', { name: 'Đã quay lại' })).not.toBeInTheDocument();
});

it('warns that pending absences will be cancelled when moving out', () => {
  mode = 'api'; role = 'ADMIN';
  render(<MemoryRouter><MoveOutPage /></MemoryRouter>);
  fireEvent.change(screen.getByLabelText('Hộ'), { target: { value: '1' } });
  fireEvent.change(screen.getByLabelText('Nhân khẩu'), { target: { value: '1' } });
  fireEvent.change(screen.getByLabelText('Lý do'), { target: { value: 'Chuyển nhà' } });
  fireEvent.click(screen.getByRole('button', { name: 'Ghi nhận đã chuyển đi' }));
  expect(screen.getByText(/Các đơn tạm vắng đang chờ duyệt của người này/)).toHaveTextContent('sẽ tự động chuyển sang Đã hủy.');
});

it('does not show the cancellation warning in demo mode', () => {
  mode = 'demo'; role = 'ADMIN'; render(<MemoryRouter><MoveOutPage /></MemoryRouter>);
  fireEvent.change(screen.getByLabelText('Nhân khẩu'), { target: { value: '1' } });
  fireEvent.change(screen.getByLabelText('Lý do'), { target: { value: 'Chuyển nhà' } });
  fireEvent.click(screen.getByRole('button', { name: 'Ghi nhận đã chuyển đi' }));
  expect(screen.queryByText(/Các đơn tạm vắng đang chờ duyệt/)).not.toBeInTheDocument();
});
it('clears return failure when reopening confirmation', async () => {
  mode = 'api'; role = 'ADMIN';
  vi.spyOn(await import('../api/residency'), 'returnResident').mockRejectedValueOnce(new Error('conflict'));
  show(); fireEvent.click(screen.getByRole('button', { name: 'Đã quay lại' }));
  fireEvent.click(within(document.querySelector('dialog')!).getByText('Đã quay lại', { selector: 'button' }));
  await screen.findByText('Lỗi');
  fireEvent.click(within(document.querySelector('dialog')!).getByText('Hủy', { selector: 'button' }));
  fireEvent.click(screen.getByRole('button', { name: 'Đã quay lại' }));
  expect(within(document.querySelector('dialog')!).queryByText('Lỗi')).not.toBeInTheDocument();
});
