import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeAll, beforeEach, expect, it, vi } from 'vitest';
import { ApiVoluntaryPage } from '../screens/s08/ApiVoluntaryPage';
import { messageForError } from '../api/errors';
import * as voluntary from '../api/voluntary';

const state = vi.hoisted(() => ({ role: 'ADMIN' }));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: { role: state.role, username: 'ux-review' }, handleCallFailure: messageForError }) }));
vi.mock('../api/voluntary', () => ({ listCampaigns: vi.fn(), listContributions: vi.fn(), listDonors: vi.fn(), recordContribution: vi.fn(), saveCampaign: vi.fn(), campaignAction: vi.fn(), voidContribution: vi.fn() }));

const campaign = { id: 31, name: 'Quỹ khuyến học', description: 'Hỗ trợ học sinh trong chung cư.', opensOn: '2026-11-01', closesOn: '2026-11-30', status: 'PUBLISHED', version: 1, totalCollected: '999999999999999999', postedCount: 14 };
const page = <T,>(content: T[]) => ({ content, page: 0, size: 10, totalElements: content.length, totalPages: 1 });

beforeAll(() => {
  HTMLDialogElement.prototype.showModal = function () { this.setAttribute('open', ''); };
  HTMLDialogElement.prototype.close = function () { this.removeAttribute('open'); };
});

beforeEach(() => {
  state.role = 'ADMIN';
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([campaign]));
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([]));
});
afterEach(() => { cleanup(); vi.restoreAllMocks(); vi.clearAllMocks(); });

it('shows staff totals and contribution counts on campaign cards before opening details', async () => {
  render(<ApiVoluntaryPage />);
  const card = await screen.findByRole('article', { name: campaign.name });
  expect(within(card).getByText('999.999.999.999.999.999 đ')).toBeInTheDocument();
  expect(within(card).getByText(/14 lượt đóng góp/)).toBeInTheDocument();
  expect(within(card).getByText('01/11/2026')).toBeInTheDocument();
  expect(screen.queryByRole('heading', { name: 'Sổ đóng góp' })).not.toBeInTheDocument();
});

it('lets an administrator create a draft from the empty state without publishing it', async () => {
  const user = userEvent.setup();
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([]));
  vi.mocked(voluntary.saveCampaign).mockResolvedValue({ ...campaign, status: 'DRAFT', totalCollected: '0', postedCount: 0 });
  render(<ApiVoluntaryPage />);
  await screen.findByText('Chưa có đợt đóng góp');
  await user.click(screen.getByRole('button', { name: 'Tạo đợt vận động mới' }));
  const dialog = screen.getByRole('dialog', { name: 'Tạo đợt vận động mới' });
  const name = within(dialog).getByLabelText('Tên đợt');
  await user.type(name, 'Quỹ khuyến học');
  fireEvent.change(within(dialog).getByLabelText('Ngày bắt đầu'), { target: { value: '2026-11-01' } });
  fireEvent.change(within(dialog).getByLabelText('Ngày kết thúc'), { target: { value: '2026-11-30' } });
  await user.click(within(dialog).getByRole('button', { name: 'Lưu nháp' }));
  await waitFor(() => expect(voluntary.saveCampaign).toHaveBeenCalledWith({ name: 'Quỹ khuyến học', description: '', opensOn: '2026-11-01', closesOn: '2026-11-30' }, undefined));
  expect(voluntary.campaignAction).not.toHaveBeenCalled();
  expect(await screen.findByText(/Đã lưu nháp/)).toHaveAttribute('role', 'status');
  expect(screen.getByRole('button', { name: 'Công bố' })).toBeInTheDocument();
});

it('keeps resident cards free of staff totals and ledger controls', async () => {
  state.role = 'RESIDENT';
  render(<ApiVoluntaryPage />);
  const card = await screen.findByRole('article', { name: campaign.name });
  expect(within(card).getByText(campaign.description)).toBeInTheDocument();
  expect(screen.queryByText(/999\.999/)).not.toBeInTheDocument();
  expect(screen.queryByText(/14 lượt đóng góp/)).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Xem chi tiết' })).not.toBeInTheDocument();
  expect(voluntary.listContributions).not.toHaveBeenCalled();
});

it('identifies the donor, amount and payment date before voiding a contribution', async () => {
  state.role = 'ACCOUNTANT';
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([{ id: 18, donorNameSnapshot: 'Người kiểm thử', householdCodeSnapshot: 'H18', amount: '250000', paidOn: '2026-11-08', status: 'POSTED', version: 0, voidReason: null }]));
  render(<ApiVoluntaryPage />);
  fireEvent.click(await screen.findByRole('button', { name: 'Xem chi tiết' }));
  fireEvent.click(await screen.findByRole('button', { name: /Hủy khoản đóng góp của Người kiểm thử/ }));
  const dialog = screen.getByRole('dialog');
  expect(dialog).toHaveTextContent('Người kiểm thử');
  expect(dialog).toHaveTextContent('250.000 đ');
  expect(dialog).toHaveTextContent('08/11/2026');
  expect(dialog).toHaveTextContent(/không hoàn tiền/);
  expect(voluntary.voidContribution).not.toHaveBeenCalled();
});

it('shows a loading state while a campaign ledger is unresolved', async () => {
  vi.mocked(voluntary.listContributions).mockReturnValue(new Promise(() => {}));
  render(<ApiVoluntaryPage />);
  fireEvent.click(await screen.findByRole('button', { name: 'Xem chi tiết' }));
  expect(screen.getByRole('status')).toHaveTextContent('Đang tải sổ đóng góp');
  expect(screen.queryByText('Chưa có khoản đóng góp.')).not.toBeInTheDocument();
});
