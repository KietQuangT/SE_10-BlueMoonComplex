import { useState } from 'react';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { beforeEach, expect, it, vi } from 'vitest';
import { ApiAbsencePage } from '../screens/resident/ApiAbsencePage';
import * as api from '../api/residency';

const member = { id: 12, householdId: 1, fullName: 'Người thật', residencyStatus: 'Đang cư trú' };
vi.mock('../state/AppStateContext', () => ({ useAppState: () => {
  const [draft, setDraft] = useState({ residentId: '', effectiveDate: '', endDateAssumed: '', reason: '' });
  return { user: { householdId: 1 }, residents: [member], absenceDraft: draft, setAbsenceDraft: setDraft, handleCallFailure: () => 'Lỗi thao tác', refreshAbsenceCount: async () => {}, dataLoadError: false };
} }));
vi.mock('../api/residency', async (load) => ({ ...(await load<typeof import('../api/residency')>()), fetchMyDeclarations: vi.fn(), createDeclaration: vi.fn(), cancelDeclaration: vi.fn() }));
const declaration = { id: 5, residentName: 'Người thật', effectiveDate: '2099-01-01', endDate: null, reason: 'Công tác', status: 'PENDING', cancellable: true };
beforeEach(() => { vi.clearAllMocks(); vi.mocked(api.fetchMyDeclarations).mockResolvedValue({ content: [declaration as api.DeclarationDto], page: 0, size: 10, totalElements: 1, totalPages: 1 }); });
const set = (label: RegExp | string, value: string) => fireEvent.change(screen.getByLabelText(label), { target: { value } });
const submit = () => fireEvent.submit(screen.getByRole('button', { name: 'Gửi đơn khai báo' }).closest('form')!);
it('shows an empty form without sample residents', async () => {
  render(<ApiAbsencePage />);
  expect(screen.getByLabelText('Người vắng mặt')).toHaveValue('');
  expect(screen.getByLabelText('Từ ngày')).toHaveValue('');
  expect(screen.getByLabelText(/Đến ngày/)).toHaveValue('');
  expect(screen.queryByText('Nguyễn Văn An')).not.toBeInTheDocument();
  await screen.findByRole('button', { name: 'Hủy đơn' });
});
it.each([
  ['', '2099-01-01', '', 'Lý do', 'Vui lòng chọn người vắng mặt.', 'Người vắng mặt'],
  ['12', '', '', 'Lý do', 'Vui lòng chọn từ ngày.', 'Từ ngày'],
  ['12', '2000-01-01', '', 'Lý do', 'Từ ngày không được trước hôm nay.', 'Từ ngày'],
  ['12', '2099-01-02', '2099-01-01', 'Lý do', 'Đến ngày không được trước từ ngày.', /Đến ngày/],
  ['12', '2099-01-01', '', '   ', 'Vui lòng nhập lý do.', /Lý do \(/],
  ['12', '2099-01-01', '', 'a'.repeat(256), 'Lý do tối đa 255 ký tự.', /Lý do \(/],
])('rejects invalid member, dates or reason', async (resident, from, to, reason, message, field) => {
  render(<ApiAbsencePage />); await screen.findByRole('button', { name: 'Hủy đơn' });
  set('Người vắng mặt', resident); set('Từ ngày', from); set(/Đến ngày/, to); set(/Lý do \(/, reason); submit();
  expect(screen.getByRole('alert')).toHaveTextContent(message);
  expect(screen.getByLabelText(field)).toHaveFocus();
  expect(screen.getByLabelText(field)).toHaveAttribute('aria-describedby', 'absence-error');
  expect(screen.getByRole('alert').closest('form')).toHaveAttribute('novalidate');
  expect(api.createDeclaration).not.toHaveBeenCalled();
});
it('reloads mine after creating and cancelling', async () => {
  render(<ApiAbsencePage />); await screen.findByRole('button', { name: 'Hủy đơn' });
  set('Người vắng mặt', '12'); set('Từ ngày', '2099-01-01'); set(/Lý do \(/, 'Đi làm'); submit();
  await waitFor(() => expect(api.fetchMyDeclarations).toHaveBeenCalledTimes(2));
  expect(api.createDeclaration).toHaveBeenCalledWith({ residentId: 12, effectiveDate: '2099-01-01', reason: 'Đi làm' });
  fireEvent.click(screen.getByRole('button', { name: 'Hủy đơn' }));
  fireEvent.click(within(document.querySelector('dialog')!).getByText('Hủy đơn'));
  await waitFor(() => expect(api.fetchMyDeclarations).toHaveBeenCalledTimes(3));
});
it('reloads mine after a failed cancellation and shows one error', async () => {
  vi.mocked(api.cancelDeclaration).mockRejectedValueOnce(new Error('conflict'));
  render(<ApiAbsencePage />); fireEvent.click(await screen.findByRole('button', { name: 'Hủy đơn' }));
  fireEvent.click(within(document.querySelector('dialog')!).getByText('Hủy đơn'));
  await waitFor(() => expect(api.fetchMyDeclarations).toHaveBeenCalledTimes(2));
  expect(screen.getAllByText('Lỗi thao tác')).toHaveLength(1);
});

it('clears success on failed submission, preserves fields and clears error on opening cancel', async () => {
  vi.mocked(api.createDeclaration).mockResolvedValueOnce({} as api.DeclarationDto).mockRejectedValueOnce(new Error('duplicate'));
  render(<ApiAbsencePage />); await screen.findByRole('button', { name: 'Hủy đơn' });
  const fill = () => { set('Người vắng mặt', '12'); set('Từ ngày', '2099-01-01'); set(/Đến ngày/, '2099-01-02'); set(/Lý do \(/, 'Đi làm'); };
  fill(); submit(); await screen.findByRole('status');
  expect(screen.getByLabelText('Từ ngày')).toHaveValue(''); expect(screen.getByLabelText(/Đến ngày/)).toHaveValue('');
  fill(); submit(); await screen.findByText('Lỗi thao tác');
  expect(screen.queryByRole('status')).not.toBeInTheDocument();
  expect(screen.getByLabelText('Từ ngày')).toHaveValue('2099-01-01');
  expect(screen.getByRole('alert').closest('form')).toContainElement(screen.getByRole('button', { name: 'Gửi đơn khai báo' }));
  fireEvent.click(screen.getByRole('button', { name: 'Hủy đơn' }));
  expect(within(document.querySelector('dialog')!).queryByText('Lỗi thao tác')).not.toBeInTheDocument();
});
it('shows rejection note on a separate labeled line', async () => {
  vi.mocked(api.fetchMyDeclarations).mockResolvedValueOnce({ content: [{ ...declaration, status: 'REJECTED', decisionNote: 'Thiếu thông tin', cancellable: false } as api.DeclarationDto], page: 0, size: 10, totalElements: 1, totalPages: 1 });
  render(<ApiAbsencePage />);
  const note = await screen.findByText('Lý do từ chối: Thiếu thông tin');
  expect(note.tagName).toBe('P'); expect(note).toHaveClass('field-error');
});
