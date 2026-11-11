import { cleanup, fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { ReportPackagesPage } from '../screens/admin/ReportPackagesPage';

const state = vi.hoisted(() => ({ mode: 'api', dataLoadError: false, reloadHouseholds: vi.fn(), households: [
  { id: 11, status: 'ACTIVE', apartmentCode: 'A01', householdCode: 'H01', currentResidents: 1 },
  { id: 12, status: 'INACTIVE', apartmentCode: 'A02', householdCode: 'H02', currentResidents: 1 },
] }));
const api = vi.hoisted(() => ({ fetchAgencyPackages: vi.fn(), fetchAgencyPackage: vi.fn(), fetchAgencyPackagePreview: vi.fn(), createAgencyPackage: vi.fn(), actOnAgencyPackage: vi.fn(), downloadAgencyPackage: vi.fn() }));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => state }));
vi.mock('../api/agencyPackages', () => api);

const draft = { id: 7, status: 'DRAFT', version: 3, createdAt: '2026-11-01T00:00:00Z', createdBy: 1, approvedAt: null, approvedBy: null, downloadUntil: null, contentDeleteAfter: '2026-12-01T00:00:00Z', contentDeletedAt: null, householdCount: 1, residentCount: 1, handedOffAt: null, revokedAt: null, canApprove: false, canReject: true, canDownload: false, canRevoke: false, canHandoff: false, householdIds: [11], recipientAgencyCode: 'AG-1', requestReference: 'REQ-1', licenceReference: 'LIC-1', contentSha256: 'abc123' };
const page = (content: unknown[]) => ({ content, page: 0, size: 20, totalElements: content.length, totalPages: 1 });

beforeEach(() => {
  vi.clearAllMocks();
  api.fetchAgencyPackages.mockResolvedValue(page([]));
  api.fetchAgencyPackage.mockResolvedValue(draft);
  api.fetchAgencyPackagePreview.mockResolvedValue({ content: [{ householdCode: 'H01', apartmentCode: 'A01', fullName: 'Test Person', residencyStatus: 'TEMPORARILY_ABSENT' }], page: 0, totalElements: 1, totalPages: 1 });
  HTMLDialogElement.prototype.showModal = function () { this.setAttribute('open', ''); };
  HTMLDialogElement.prototype.close = function () { this.removeAttribute('open'); };
});
afterEach(cleanup);

it('requires an explicit active household before creating a package', async () => {
  render(<MemoryRouter><ReportPackagesPage /></MemoryRouter>);
  await screen.findByText('Chưa có gói dữ liệu');
  fireEvent.click(screen.getAllByRole('button', { name: 'Tạo gói dữ liệu' })[0]);
  expect(screen.getByText(/A01/)).toBeInTheDocument();
  expect(screen.queryByText(/A02/)).not.toBeInTheDocument();
  fireEvent.change(screen.getByLabelText('Mã cơ quan nhận gói'), { target: { value: ' AG-1 ' } });
  fireEvent.change(screen.getByLabelText('Mã tham chiếu yêu cầu'), { target: { value: ' REQ-1 ' } });
  fireEvent.change(screen.getByLabelText('Mã tham chiếu giấy phép'), { target: { value: ' LIC-1 ' } });
  fireEvent.click(screen.getByRole('button', { name: 'Tạo bản nháp' }));
  expect(screen.getByRole('alert')).toHaveTextContent('Chọn ít nhất một hộ đang hoạt động');
  expect(api.createAgencyPackage).not.toHaveBeenCalled();
  api.createAgencyPackage.mockResolvedValue(draft);
  fireEvent.click(screen.getByRole('checkbox'));
  fireEvent.click(screen.getByRole('button', { name: 'Tạo bản nháp' }));
  expect(api.createAgencyPackage).toHaveBeenCalledWith({ householdIds: [11], recipientAgencyCode: 'AG-1', requestReference: 'REQ-1', licenceReference: 'LIC-1' });
  expect(await screen.findByText(/Đã tạo bản nháp gói #7/)).toBeInTheDocument();
});

it('shows only permitted draft actions and the four preview columns', async () => {
  api.fetchAgencyPackages.mockResolvedValue(page([draft]));
  render(<MemoryRouter><ReportPackagesPage /></MemoryRouter>);
  fireEvent.click(await screen.findByRole('button', { name: 'Xem chi tiết' }));
  expect(await screen.findByText('Test Person')).toBeInTheDocument();
  const table = screen.getByText('Test Person').closest('table')!;
  expect(within(table).getAllByRole('columnheader').map((cell) => cell.textContent)).toEqual(['Mã hộ', 'Căn hộ', 'Họ tên', 'Trạng thái cư trú']);
  expect(within(table).getByText('Tạm vắng')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Từ chối bản nháp' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Duyệt gói' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Tải XLSX' })).not.toBeInTheDocument();
});

it('requires confirmation and submits the displayed version and hash for approval', async () => {
  const approvable = { ...draft, canApprove: true };
  api.fetchAgencyPackages.mockResolvedValue(page([approvable]));
  api.fetchAgencyPackage.mockResolvedValue(approvable);
  api.actOnAgencyPackage.mockResolvedValue({ ...approvable, status: 'APPROVED' });
  render(<MemoryRouter><ReportPackagesPage /></MemoryRouter>);
  fireEvent.click(await screen.findByRole('button', { name: 'Xem chi tiết' }));
  fireEvent.click(await screen.findByRole('button', { name: 'Duyệt gói' }));
  const dialog = screen.getByRole('dialog', { name: 'Duyệt gói dữ liệu #7' });
  expect(api.actOnAgencyPackage).not.toHaveBeenCalled();
  fireEvent.click(within(dialog).getByRole('button', { name: 'Duyệt gói' }));
  expect(api.actOnAgencyPackage).toHaveBeenCalledWith(7, 'approve', 3, 'abc123');
  expect(await screen.findByRole('status')).toHaveTextContent('Duyệt gói: gói #7');
});
