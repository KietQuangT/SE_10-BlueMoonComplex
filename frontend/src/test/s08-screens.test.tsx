import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { ApiBillingPage } from '../screens/s08/ApiBillingPage';
import { ApiVoluntaryPage } from '../screens/s08/ApiVoluntaryPage';
import { messageForError } from '../api/errors';
import { ApiHttpError } from '../api/client';
import * as billing from '../api/billing';
import * as voluntary from '../api/voluntary';
const state = vi.hoisted(() => ({ role: 'ADMIN' }));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: { role: state.role, username: 'user' }, handleCallFailure: messageForError }) }));
vi.mock('../api/billing', () => ({ listBillingPeriods: vi.fn(), periodPreview: vi.fn(), periodInvoices: vi.fn(), periodSubmission: vi.fn(), createPeriod: vi.fn(), updatePeriod: vi.fn(), periodAction: vi.fn() }));
vi.mock('../api/voluntary', () => ({ listCampaigns: vi.fn(), listContributions: vi.fn(), listDonors: vi.fn(), recordContribution: vi.fn(), saveCampaign: vi.fn(), campaignAction: vi.fn(), voidContribution: vi.fn() }));
const page = <T,>(content: T[]) => ({ content, page: 0, size: 10, totalElements: content.length, totalPages: 1 });
beforeEach(() => {
  state.role = 'ADMIN';
  vi.mocked(billing.listBillingPeriods).mockResolvedValue(page([{ id: 1, period: '2026-11', dueDate: '2026-11-30', status: 'PENDING_APPROVAL', version: 2, latestRevision: 1, expired: false, editable: false, allowedActions: ['ISSUE', 'RETURN'], householdCount: 1, total: '999999999999999999', returnReason: null, cancelReason: null }]));
  vi.mocked(billing.periodPreview).mockResolvedValue({ revision: 1, householdCount: 1, total: '999999999999999999', version: 2, households: page([]) });
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([{ id: 3, name: 'Quỹ học tập', description: '', opensOn: '2026-11-01', closesOn: '2026-11-30', status: 'PUBLISHED', version: 1, totalCollected: '999999999999999999', postedCount: 1 }]));
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([]));
});
afterEach(() => { cleanup(); vi.clearAllMocks(); });
it('shows the submitted amount exactly and asks for approval of the displayed revision', async () => {
  render(<ApiBillingPage />);
  fireEvent.click(await screen.findByRole('button', { name: '2026-11' }));
  expect(await screen.findByText(/999\.999\.999\.999\.999\.999 đ/)).toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Phát hành' }));
  expect(screen.getAllByText(/bản trình số 1/i)).toHaveLength(2);
});
it('does not expose staff totals or donor ledger to residents', async () => {
  state.role = 'RESIDENT'; render(<ApiVoluntaryPage />);
  await screen.findByText('Quỹ học tập');
  expect(screen.queryByText(/999\.999/)).not.toBeInTheDocument();
  expect(screen.queryByText('Sổ đóng góp')).not.toBeInTheDocument();
  expect(voluntary.listContributions).not.toHaveBeenCalled();
});
it('does not offer contribution writes to administrators', async () => {
  render(<ApiVoluntaryPage />);
  fireEvent.click(await screen.findByRole('button', { name: 'Xem chi tiết' }));
  await screen.findByText('Sổ đóng góp');
  expect(screen.queryByRole('button', { name: 'Ghi đóng góp' })).not.toBeInTheDocument();
  expect(screen.getByText(/999\.999\.999\.999\.999\.999 đ/)).toBeInTheDocument();
});
it('keeps the same contribution key and amount after a lost response and retry', async () => {
  state.role = 'ACCOUNTANT';
  vi.mocked(voluntary.listDonors).mockResolvedValue(page([{ id: 8, fullName: 'Người thử', householdCode: 'H1', apartmentCode: 'A1' }]));
  vi.mocked(voluntary.recordContribution).mockRejectedValueOnce(new Error('network')).mockResolvedValueOnce({ id: 1, donorNameSnapshot: 'Người thử', householdCodeSnapshot: 'H1', amount: '999999999999999999', paidOn: '2026-11-08', status: 'POSTED', version: 0, voidReason: null });
  render(<ApiVoluntaryPage />);
  fireEvent.click(await screen.findByRole('button', { name: 'Xem chi tiết' }));
  fireEvent.change(screen.getByLabelText('Tìm người đóng'), { target: { value: 'Người' } });
  fireEvent.click(screen.getByRole('button', { name: 'Tìm' }));
  await screen.findByRole('option', { name: /Người thử/ });
  fireEvent.change(screen.getByLabelText('Người đóng'), { target: { value: '8' } });
  fireEvent.change(screen.getByLabelText('Số tiền (đồng)'), { target: { value: '999999999999999999' } });
  fireEvent.change(screen.getByLabelText('Ngày nhận tiền'), { target: { value: '2026-11-08' } });
  fireEvent.click(screen.getByRole('button', { name: 'Ghi đóng góp' }));
  fireEvent.click(await screen.findByRole('button', { name: 'Thử ghi lại' }));
  await waitFor(() => expect(screen.queryByRole('button', { name: 'Thử ghi lại' })).not.toBeInTheDocument());
  expect(voluntary.recordContribution).toHaveBeenCalledTimes(2);
  expect(vi.mocked(voluntary.recordContribution).mock.calls[1]).toEqual(vi.mocked(voluntary.recordContribution).mock.calls[0]);
});

it('reports changed billing inputs and does not issue a new revision automatically', async () => {
  vi.mocked(billing.periodAction).mockRejectedValueOnce(new ApiHttpError({ status: 409, code: 'BILLING_INPUT_CHANGED' }));
  render(<ApiBillingPage />);
  fireEvent.click(await screen.findByRole('button', { name: '2026-11' }));
  fireEvent.click(await screen.findByRole('button', { name: 'Phát hành' }));
  fireEvent.click(document.querySelector('dialog button.btn-primary')!);
  expect(await screen.findByRole('alert')).toHaveTextContent('Kế toán cần rút và trình lại');
  expect(billing.periodAction).toHaveBeenCalledTimes(1);
});
it('preserves an interrupted accountant period draft after session expiry', async () => {
  state.role = 'ACCOUNTANT';
  vi.mocked(billing.createPeriod).mockRejectedValueOnce(new ApiHttpError({ status: 401 }));
  const view = render(<ApiBillingPage />);
  fireEvent.change(screen.getByLabelText('Hạn nộp'), { target: { value: '2026-11-30' } });
  fireEvent.click(screen.getByRole('button', { name: 'Tạo kỳ thu' }));
  await screen.findByRole('alert');
  view.unmount();
  render(<ApiBillingPage />);
  expect(screen.getByLabelText('Hạn nộp')).toHaveValue('2026-11-30');
  await screen.findByRole('button', { name: '2026-11' });
});
it('reports forbidden access without displaying demo data', async () => {
  vi.mocked(voluntary.listCampaigns).mockRejectedValueOnce(new ApiHttpError({ status: 403 }));
  render(<ApiVoluntaryPage />);
  expect(await screen.findByText('Bạn không có quyền thực hiện thao tác này.')).toBeInTheDocument();
  expect(screen.queryByText('Quỹ học tập')).not.toBeInTheDocument();
});
it('clears the previous ledger while a different campaign loads', async () => {
  const first = { id: 3, name: 'Quỹ thứ nhất', description: '', opensOn: '2026-11-01', closesOn: '2026-11-30', status: 'PUBLISHED', version: 1, totalCollected: '100', postedCount: 1 };
  vi.mocked(voluntary.listCampaigns).mockResolvedValueOnce(page([first, { ...first, id: 4, name: 'Quỹ thứ hai' }]));
  vi.mocked(voluntary.listContributions).mockResolvedValueOnce(page([{ id: 7, donorNameSnapshot: 'Người cũ', householdCodeSnapshot: 'H1', amount: '100', paidOn: '2026-11-08', status: 'POSTED', version: 0, voidReason: null }])).mockReturnValueOnce(new Promise(() => {}));
  render(<ApiVoluntaryPage />);
  fireEvent.click((await screen.findAllByRole('button', { name: 'Xem chi tiết' }))[0]);
  await screen.findByText(/Người cũ/);
  fireEvent.click(screen.getAllByRole('button', { name: 'Xem chi tiết' })[1]);
  expect(screen.queryByText(/Người cũ/)).not.toBeInTheDocument();
});
