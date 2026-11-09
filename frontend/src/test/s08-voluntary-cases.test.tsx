import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeAll, beforeEach, expect, it, vi } from 'vitest';
import { ApiVoluntaryPage } from '../screens/s08/ApiVoluntaryPage';
import { ApiHttpError } from '../api/client';
import { messageForError } from '../api/errors';
import * as voluntary from '../api/voluntary';

// Evidence for VOL-08.1, VOL-08.2 and the parts of VOL-08.3 that jsdom can check
// (blocked submissions, exact 18-digit money). Layout at 360px, theme contrast and
// the visible focus ring need a real browser and are not covered here.
const state = vi.hoisted(() => ({ role: 'ADMIN', username: 'vol-cases-0', seq: 0 }));
vi.mock('../state/AppStateContext', () => ({
  useAppState: () => ({ user: { role: state.role, username: state.username }, handleCallFailure: messageForError }),
}));
vi.mock('../api/voluntary', () => ({
  listCampaigns: vi.fn(), listContributions: vi.fn(), listDonors: vi.fn(), recordContribution: vi.fn(),
  saveCampaign: vi.fn(), campaignAction: vi.fn(), voidContribution: vi.fn(),
}));

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const page = <T,>(content: T[]) => ({ content, page: 0, size: 10, totalElements: content.length, totalPages: 1 });
const open = { id: 31, name: 'Quỹ khuyến học', description: 'Hỗ trợ học sinh.', opensOn: '2026-11-01', closesOn: '2026-11-30', status: 'PUBLISHED', version: 1, totalCollected: '200000', postedCount: 1 };
const upcoming = { ...open, id: 32, name: 'Quỹ sắp mở', opensOn: '2026-11-20' };
const ended = { ...open, id: 33, name: 'Quỹ đã qua', opensOn: '2026-10-01', closesOn: '2026-10-31' };
const draft = { ...open, id: 34, name: 'Quỹ nháp', status: 'DRAFT', version: 0, totalCollected: '0', postedCount: 0 };
const posted = { id: 18, donorNameSnapshot: 'Người kiểm thử', householdCodeSnapshot: 'H18', amount: '250000', paidOn: '2026-11-08', status: 'POSTED', version: 0, voidReason: null };
const voided = { id: 19, donorNameSnapshot: 'Người đã hủy', householdCodeSnapshot: 'H19', amount: '100000', paidOn: '2026-11-07', status: 'VOIDED', version: 1, voidReason: 'Nhập nhầm' };
const donor = { id: 8, fullName: 'Người thử', householdCode: 'H1', apartmentCode: 'A1' };
const recorded = { id: 50, donorNameSnapshot: 'Người thử', householdCodeSnapshot: 'H1', amount: '200000', paidOn: '2026-11-09', status: 'POSTED', version: 0, voidReason: null };

beforeAll(() => {
  HTMLDialogElement.prototype.showModal = function () { this.setAttribute('open', ''); };
  HTMLDialogElement.prototype.close = function () { this.removeAttribute('open'); };
});

beforeEach(() => {
  // "Today" is 2026-11-09 in Vietnam; only Date is faked so timers and promises keep working.
  vi.useFakeTimers({ toFake: ['Date'] });
  vi.setSystemTime(new Date('2026-11-09T05:00:00Z'));
  state.role = 'ADMIN';
  state.username = `vol-cases-${++state.seq}`; // pending drafts are kept per account at module level
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([open]));
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([]));
  vi.mocked(voluntary.listDonors).mockResolvedValue(page([donor]));
});
afterEach(() => { cleanup(); vi.useRealTimers(); vi.restoreAllMocks(); vi.clearAllMocks(); });

async function openDetail() {
  fireEvent.click(await screen.findByRole('button', { name: 'Xem chi tiết' }));
  await screen.findByRole('heading', { name: 'Sổ đóng góp' });
}

async function chooseDonor() {
  fireEvent.change(screen.getByLabelText('Tìm người đóng'), { target: { value: 'Người' } });
  fireEvent.click(screen.getByRole('button', { name: 'Tìm' }));
  await screen.findByRole('option', { name: /Người thử/ });
  fireEvent.change(screen.getByLabelText('Người đóng'), { target: { value: '8' } });
}

function fillContribution(amount: string, paidOn: string) {
  fireEvent.change(screen.getByLabelText('Số tiền (đồng)'), { target: { value: amount } });
  if (paidOn) fireEvent.change(screen.getByLabelText('Ngày nhận tiền'), { target: { value: paidOn } });
}

// ------------------------------------------------------------------ VOL-08.1
it('lets an administrator publish a saved draft only after confirming the dialog', async () => {
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([draft]));
  vi.mocked(voluntary.campaignAction).mockResolvedValue({ ...draft, status: 'PUBLISHED', version: 1 });
  render(<ApiVoluntaryPage />);
  await openDetail();
  expect(screen.getByRole('button', { name: 'Sửa nháp' })).toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Công bố' }));
  const dialog = screen.getByRole('dialog');
  expect(dialog).toHaveTextContent(/Quỹ nháp/);
  expect(voluntary.campaignAction).not.toHaveBeenCalled();
  fireEvent.click(within(dialog).getByRole('button', { name: 'Công bố' }));
  await waitFor(() => expect(voluntary.campaignAction).toHaveBeenCalledWith(expect.objectContaining({ id: 34, version: 0 }), 'publish'));
  expect(await screen.findByText('Đã công bố đợt vận động cho cư dân.')).toHaveAttribute('role', 'status');
});

it('shows an administrator the ledger and close action but never a record form or void button', async () => {
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([posted]));
  vi.mocked(voluntary.campaignAction).mockResolvedValue({ ...open, status: 'CLOSED', version: 2 });
  render(<ApiVoluntaryPage />);
  const card = await screen.findByRole('article', { name: open.name });
  expect(within(card).getByText('Tổng thu đợt này')).toBeInTheDocument();
  expect(within(card).getByText('200.000 đ')).toBeInTheDocument();
  await openDetail();
  expect(await screen.findByText('Người kiểm thử')).toBeInTheDocument();
  expect(screen.queryByRole('heading', { name: 'Ghi nhận đóng góp' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Ghi đóng góp' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: /Hủy khoản đóng góp/ })).not.toBeInTheDocument();
  expect(screen.queryByRole('columnheader', { name: 'Thao tác' })).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Đóng đợt' }));
  fireEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Đóng đợt' }));
  await waitFor(() => expect(voluntary.campaignAction).toHaveBeenCalledWith(expect.objectContaining({ id: 31, version: 1 }), 'close'));
});

it('lets an accountant record a contribution and offers voiding only on posted rows', async () => {
  state.role = 'ACCOUNTANT';
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([posted, voided]));
  vi.mocked(voluntary.recordContribution).mockResolvedValue(recorded);
  render(<ApiVoluntaryPage />);
  await openDetail();
  await screen.findByText('Người kiểm thử');
  expect(screen.getByRole('button', { name: 'Hủy khoản đóng góp của Người kiểm thử' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Hủy khoản đóng góp của Người đã hủy' })).not.toBeInTheDocument();
  expect(screen.getByText('Lý do hủy: Nhập nhầm')).toBeInTheDocument();
  expect(screen.getAllByText('Đã ghi').length).toBeGreaterThan(0);
  expect(screen.getAllByText('Đã hủy').length).toBeGreaterThan(0);

  await chooseDonor();
  fillContribution('200000', '2026-11-09');
  fireEvent.click(screen.getByRole('button', { name: 'Ghi đóng góp' }));
  await waitFor(() => expect(voluntary.recordContribution).toHaveBeenCalledTimes(1));
  const [campaignId, body, key] = vi.mocked(voluntary.recordContribution).mock.calls[0];
  expect(campaignId).toBe(31);
  expect(body).toEqual({ residentId: 8, amount: '200000', paidOn: '2026-11-09', lateReason: '' });
  expect(key).toMatch(UUID);
  expect(await screen.findByText('Đã ghi nhận khoản đóng góp.')).toBeInTheDocument();
});

it('shows residents only the name, status, dates and description of published campaigns', async () => {
  state.role = 'RESIDENT';
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([upcoming, open, ended]));
  render(<ApiVoluntaryPage />);
  expect(within(await screen.findByRole('article', { name: upcoming.name })).getByText('Sắp bắt đầu')).toBeInTheDocument();
  expect(within(screen.getByRole('article', { name: open.name })).getByText('Đang mở')).toBeInTheDocument();
  expect(within(screen.getByRole('article', { name: ended.name })).getByText('Đã kết thúc')).toBeInTheDocument();
  expect(within(screen.getByRole('article', { name: open.name })).getByText('Hỗ trợ học sinh.')).toBeInTheDocument();
  expect(within(screen.getByRole('article', { name: open.name })).getByText('01/11/2026')).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Xem chi tiết' })).not.toBeInTheDocument();
  expect(screen.queryByText('Tổng thu đợt này')).not.toBeInTheDocument();
  expect(screen.queryByText(/lượt đóng góp/)).not.toBeInTheDocument();
  expect(screen.queryByText('Sổ đóng góp')).not.toBeInTheDocument();
  expect(voluntary.listContributions).not.toHaveBeenCalled();
  expect(voluntary.listDonors).not.toHaveBeenCalled();
});

// ------------------------------------------------------------------ VOL-08.2
it.each([
  new ApiHttpError({ status: 400, code: 'VALIDATION' }),
  new ApiHttpError({ status: 404, code: 'RESIDENT_NOT_FOUND' }),
  new ApiHttpError({ status: 409, code: 'INVALID_PAID_DATE' }),
  new ApiHttpError({ status: 409, code: 'CAMPAIGN_NOT_RECEIVABLE' }),
])('unlocks the form and issues a new key after a definitive %s rejection', async (failure) => {
  state.role = 'ACCOUNTANT';
  vi.mocked(voluntary.recordContribution).mockRejectedValueOnce(failure).mockResolvedValueOnce(recorded);
  render(<ApiVoluntaryPage />);
  await openDetail();
  await chooseDonor();
  fillContribution('200000', '2026-11-09');
  fireEvent.click(screen.getByRole('button', { name: 'Ghi đóng góp' }));
  await screen.findByRole('alert');
  expect(screen.getByLabelText('Số tiền (đồng)')).toBeEnabled();
  expect(screen.getByLabelText('Ngày nhận tiền')).toBeEnabled();
  expect(screen.queryByRole('button', { name: 'Thử ghi lại' })).not.toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Ghi đóng góp' }));
  await waitFor(() => expect(voluntary.recordContribution).toHaveBeenCalledTimes(2));
  const [first, second] = vi.mocked(voluntary.recordContribution).mock.calls;
  expect(first[2]).toMatch(UUID);
  expect(second[2]).toMatch(UUID);
  expect(second[2]).not.toBe(first[2]);
});

it.each([new Error('network'), new ApiHttpError({ status: 503 })])('locks the inputs and retries with the same key after %s', async (failure) => {
  state.role = 'ACCOUNTANT';
  vi.mocked(voluntary.recordContribution).mockRejectedValueOnce(failure).mockResolvedValueOnce(recorded);
  render(<ApiVoluntaryPage />);
  await openDetail();
  await chooseDonor();
  fillContribution('100000', '2026-11-09');
  fireEvent.click(screen.getByRole('button', { name: 'Ghi đóng góp' }));
  const retry = await screen.findByRole('button', { name: 'Thử ghi lại' });
  expect(screen.getByLabelText('Số tiền (đồng)')).toBeDisabled();
  expect(screen.getByLabelText('Ngày nhận tiền')).toBeDisabled();
  expect(screen.getByLabelText('Người đóng')).toBeDisabled();
  expect(screen.getByText(/Chưa xác nhận được kết quả\. Thử lại cùng nội dung để tránh ghi trùng\./)).toBeInTheDocument();
  fireEvent.click(retry);
  await waitFor(() => expect(voluntary.recordContribution).toHaveBeenCalledTimes(2));
  const [first, second] = vi.mocked(voluntary.recordContribution).mock.calls;
  expect(second[2]).toBe(first[2]);
  expect(second[1]).toEqual(first[1]);
  expect(second[0]).toBe(first[0]);
});

// ------------------------------------------------------------------ VOL-08.3 (jsdom part)
it('sends nothing when the receipt date is empty, which is what an unparsable date input reports', async () => {
  state.role = 'ACCOUNTANT';
  render(<ApiVoluntaryPage />);
  await openDetail();
  await chooseDonor();
  fillContribution('200000', '');
  expect(screen.getByLabelText('Ngày nhận tiền')).toHaveValue('');
  const form = screen.getByRole('button', { name: 'Ghi đóng góp' }).closest('form')!;
  fireEvent.click(screen.getByRole('button', { name: 'Ghi đóng góp' }));
  fireEvent.submit(form);
  expect(await screen.findByText('Chọn người đóng, nhập số tiền nguyên dương tối đa 18 chữ số và ngày hợp lệ.')).toBeInTheDocument();
  expect(voluntary.recordContribution).not.toHaveBeenCalled();
});

it('refuses a 19-digit or zero amount before any request and limits the field to 18 digits', async () => {
  state.role = 'ACCOUNTANT';
  render(<ApiVoluntaryPage />);
  await openDetail();
  await chooseDonor();
  const amount = screen.getByLabelText('Số tiền (đồng)');
  expect(amount).toHaveAttribute('maxlength', '18');
  expect(amount).toHaveAttribute('pattern', '[1-9][0-9]{0,17}');
  const form = screen.getByRole('button', { name: 'Ghi đóng góp' }).closest('form')!;
  for (const value of ['1000000000000000000', '0', '012', '1.5', '-5']) {
    fillContribution(value, '2026-11-09');
    fireEvent.submit(form);
    expect(await screen.findByText('Chọn người đóng, nhập số tiền nguyên dương tối đa 18 chữ số và ngày hợp lệ.')).toBeInTheDocument();
  }
  expect(voluntary.recordContribution).not.toHaveBeenCalled();
});

it('blocks a campaign form with empty or reversed dates and sends no request', async () => {
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([]));
  const user = userEvent.setup();
  render(<ApiVoluntaryPage />);
  await screen.findByText('Chưa có đợt đóng góp');
  await user.click(screen.getByRole('button', { name: 'Tạo đợt vận động mới' }));
  const dialog = screen.getByRole('dialog', { name: 'Tạo đợt vận động mới' });
  await user.type(within(dialog).getByLabelText('Tên đợt'), 'Quỹ thử');
  const form = dialog.querySelector('form')!;
  fireEvent.submit(form);
  expect(await within(dialog).findByRole('alert')).toHaveTextContent('Kiểm tra tên đợt và ngày kết thúc không trước ngày bắt đầu.');
  fireEvent.change(within(dialog).getByLabelText('Ngày bắt đầu'), { target: { value: '2026-11-10' } });
  fireEvent.change(within(dialog).getByLabelText('Ngày kết thúc'), { target: { value: '2026-11-05' } });
  fireEvent.submit(form);
  expect(within(dialog).getByRole('alert')).toHaveTextContent('Kiểm tra tên đợt và ngày kết thúc không trước ngày bắt đầu.');
  expect(voluntary.saveCampaign).not.toHaveBeenCalled();
});

it('shows an 18-digit total and ledger amount without rounding', async () => {
  const big = '999999999999999999';
  vi.mocked(voluntary.listCampaigns).mockResolvedValue(page([{ ...open, totalCollected: big }]));
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([{ ...posted, amount: big }]));
  render(<ApiVoluntaryPage />);
  const card = await screen.findByRole('article', { name: open.name });
  expect(within(card).getByText('999.999.999.999.999.999 đ')).toBeInTheDocument();
  await openDetail();
  const row = (await screen.findByText('Người kiểm thử')).closest('tr')!;
  expect(within(row).getByText('999.999.999.999.999.999 đ')).toBeInTheDocument();
  expect(screen.queryByText(/1\.000\.000\.000\.000\.000\.000/)).not.toBeInTheDocument();
});

it('names the donor, household and amount in the void dialog and sends the version and trimmed reason', async () => {
  state.role = 'ACCOUNTANT';
  vi.mocked(voluntary.listContributions).mockResolvedValue(page([posted]));
  vi.mocked(voluntary.voidContribution).mockResolvedValue({ ...posted, status: 'VOIDED', version: 1, voidReason: 'Nhập nhầm số tiền' });
  render(<ApiVoluntaryPage />);
  await openDetail();
  fireEvent.click(await screen.findByRole('button', { name: 'Hủy khoản đóng góp của Người kiểm thử' }));
  const dialog = screen.getByRole('dialog');
  expect(dialog).toHaveTextContent('Người kiểm thử');
  expect(dialog).toHaveTextContent('Hộ H18');
  fireEvent.click(within(dialog).getByRole('button', { name: 'Hủy khoản đóng góp' }));
  expect(await within(dialog).findByRole('alert')).toHaveTextContent('Nhập lý do hủy khoản đóng góp.');
  expect(voluntary.voidContribution).not.toHaveBeenCalled();
  fireEvent.change(within(dialog).getByLabelText('Lý do'), { target: { value: '  Nhập nhầm số tiền  ' } });
  fireEvent.click(within(dialog).getByRole('button', { name: 'Hủy khoản đóng góp' }));
  await waitFor(() => expect(voluntary.voidContribution).toHaveBeenCalledWith(expect.objectContaining({ id: 18, version: 0 }), 'Nhập nhầm số tiền'));
});
