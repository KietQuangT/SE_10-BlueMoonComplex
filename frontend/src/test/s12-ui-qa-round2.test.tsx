import { cleanup, render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { toHousehold } from '../api/residency';
import { ReportPackagesPage } from '../screens/admin/ReportPackagesPage';

const state = vi.hoisted(() => ({
  mode: 'api', dataLoadError: false, reloadHouseholds: vi.fn(),
  households: [] as ReturnType<typeof import('../api/residency').toHousehold>[],
}));
const api = vi.hoisted(() => ({
  fetchAgencyPackages: vi.fn(), fetchAgencyPackage: vi.fn(), fetchAgencyPackagePreview: vi.fn(),
  createAgencyPackage: vi.fn(), actOnAgencyPackage: vi.fn(), downloadAgencyPackage: vi.fn(),
}));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => state }));
vi.mock('../api/agencyPackages', () => api);

beforeEach(() => {
  vi.clearAllMocks();
  const dto = { id: 1, householdCode: 'H01', apartmentCode: 'A01', areaM2: 50, address: null, status: 'ACTIVE' as const };
  state.households = [toHousehold(dto), { ...toHousehold({ ...dto, id: 2, householdCode: 'H02', apartmentCode: 'A02' }), currentResidents: 0 }, { ...toHousehold({ ...dto, id: 3, householdCode: 'H03', apartmentCode: 'A03' }), currentResidents: 5 }];
  api.fetchAgencyPackages.mockResolvedValue({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
});
afterEach(cleanup);

async function openCreate() {
  const user = userEvent.setup();
  render(<MemoryRouter><ReportPackagesPage /></MemoryRouter>);
  await screen.findByText('Chưa có gói dữ liệu');
  await user.click(screen.getAllByRole('button', { name: 'Tạo gói dữ liệu' })[0]);
  return user;
}

it('renders unknown API count without fabricating zero, while preserving numeric zero and positive counts', async () => {
  await openCreate();
  expect(within(screen.getByText(/H01/).closest('label')!).queryByText(/nhân khẩu/)).not.toBeInTheDocument();
  expect(within(screen.getByText(/H02/).closest('label')!).getByText(/0 nhân khẩu/)).toBeInTheDocument();
  expect(within(screen.getByText(/H03/).closest('label')!).getByText(/5 nhân khẩu/)).toBeInTheDocument();
});

it('clears the missing-household alert on selection without creating a package', async () => {
  const user = await openCreate();
  await user.type(screen.getByLabelText('Mã cơ quan nhận gói'), 'AG-1');
  await user.type(screen.getByLabelText('Mã tham chiếu yêu cầu'), 'REQ-1');
  await user.type(screen.getByLabelText('Mã tham chiếu giấy phép'), 'LIC-1');
  await user.click(screen.getByRole('button', { name: 'Tạo bản nháp' }));
  expect(screen.getByRole('alert')).toHaveTextContent('Chọn ít nhất một hộ đang hoạt động.');
  expect(api.createAgencyPackage).not.toHaveBeenCalled();
  await user.click(within(screen.getByText(/H01/).closest('label')!).getByRole('checkbox'));
  expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  expect(api.createAgencyPackage).not.toHaveBeenCalled();
});

it('limits typed agency and reference codes at their approved boundaries', async () => {
  const user = await openCreate();
  for (const [label, max] of [['Mã cơ quan nhận gói', 50], ['Mã tham chiếu yêu cầu', 100], ['Mã tham chiếu giấy phép', 100]] as const) {
    const input = screen.getByLabelText(label) as HTMLInputElement;
    await user.type(input, 'A'.repeat(max + 1));
    expect(input.value).toBe('A'.repeat(max));
  }
});
