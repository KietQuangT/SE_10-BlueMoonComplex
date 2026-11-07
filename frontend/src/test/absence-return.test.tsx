import { render, screen, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { expect, it, vi } from 'vitest';
import { HouseholdDetailPage } from '../screens/admin/HouseholdDetailPage';

let role = 'ADMIN'; let mode = 'api';
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: { role }, mode, households: [{ id: 1, householdCode: 'H01', apartmentCode: 'A01', areaM2: '50', status: 'ACTIVE' }], residents: [{ id: 1, householdId: 1, fullName: 'An', residencyStatus: 'Tạm vắng' }, { id: 2, householdId: 1, fullName: 'Bình', residencyStatus: 'Đang cư trú' }], residencyChanges: [], accounts: [], loadHouseholdDetail: vi.fn(), handleCallFailure: () => 'Lỗi' }) }));
const show = () => render(<MemoryRouter initialEntries={['/admin/households/1']}><Routes><Route path="/admin/households/:id" element={<HouseholdDetailPage />} /></Routes></MemoryRouter>);
it('only shows return for an absent resident to an admin in api mode', () => {
  role = 'ADMIN'; mode = 'api'; const view = show();
  expect(within(screen.getByText('An').closest('tr')!).getByRole('button', { name: 'Đã quay lại' })).toBeInTheDocument();
  expect(within(screen.getByText('Bình').closest('tr')!).queryByRole('button', { name: 'Đã quay lại' })).not.toBeInTheDocument();
  view.unmount(); role = 'RESIDENT'; const other = show(); expect(screen.queryByRole('button', { name: 'Đã quay lại' })).not.toBeInTheDocument();
  other.unmount(); role = 'ADMIN'; mode = 'demo'; show(); expect(screen.queryByRole('button', { name: 'Đã quay lại' })).not.toBeInTheDocument();
});
