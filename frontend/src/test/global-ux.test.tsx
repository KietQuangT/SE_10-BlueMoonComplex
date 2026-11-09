import { cleanup, fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeAll, beforeEach, expect, it, vi } from 'vitest';
import { VoluntaryAdminPage } from '../screens/admin/VoluntaryAdminPage';
import { ReportPackagesPage } from '../screens/admin/ReportPackagesPage';
import { BillingPeriodsPage } from '../screens/admin/BillingPeriodsPage';
import { ResidencyReportPage } from '../screens/admin/ResidencyReportPage';
import { CollectionRatePage } from '../screens/accountant/CollectionRatePage';
import { AuthorityReportsPage } from '../screens/authority/AuthorityReportsPage';
import { PaginationBar } from '../components/PaginationBar';

const state = vi.hoisted(() => ({ forcedUiState: 'normal', voluntaryCampaigns: [] as unknown[], reportPackages: [] as unknown[], invoices: [] as unknown[], households: [] as unknown[], residents: [] as unknown[], publishVoluntaryCampaignDemo: vi.fn(), approveReportPackageDemo: vi.fn() }));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => state }));
beforeAll(() => {
  HTMLDialogElement.prototype.showModal = function () { this.setAttribute('open', ''); this.querySelector<HTMLElement>('input, button, select')?.focus(); };
  HTMLDialogElement.prototype.close = function () { this.removeAttribute('open'); };
});
beforeEach(() => { state.forcedUiState = 'normal'; state.voluntaryCampaigns = []; state.reportPackages = []; });
afterEach(() => { cleanup(); vi.clearAllMocks(); });

it('opens campaign creation from the empty list and restores focus on cancel', () => {
  render(<MemoryRouter><VoluntaryAdminPage /></MemoryRouter>);
  const trigger = screen.getByRole('button', { name: /Tạo đợt vận động mới/ });
  trigger.focus(); fireEvent.click(trigger);
  const dialog = screen.getByRole('dialog', { name: /Tạo đợt vận động/ });
  expect(within(dialog).getByLabelText(/Tên đợt vận động/)).toBeInTheDocument();
  fireEvent.click(within(dialog).getByRole('button', { name: 'Hủy' }));
  expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  expect(trigger).toHaveFocus();
  expect(state.publishVoluntaryCampaignDemo).not.toHaveBeenCalled();
});

it('shows only one report dialog per step and keeps the draft when returning', () => {
  render(<MemoryRouter><ReportPackagesPage /></MemoryRouter>);
  fireEvent.click(screen.getByRole('button', { name: /Tạo và phê duyệt gói mới/ }));
  const formDialog = screen.getByRole('dialog');
  fireEvent.change(within(formDialog).getByLabelText(/Kỳ báo cáo/), { target: { value: '2026-11' } });
  fireEvent.click(within(formDialog).getByRole('button', { name: 'Tiếp tục phê duyệt' }));
  expect(screen.getAllByRole('dialog')).toHaveLength(1);
  const confirmDialog = screen.getByRole('dialog', { name: 'Phê duyệt và phát hành gói báo cáo' });
  fireEvent.click(within(confirmDialog).getByRole('button', { name: 'Hủy' }));
  expect(screen.getAllByRole('dialog')).toHaveLength(1);
  expect(screen.getByLabelText(/Kỳ báo cáo/)).toHaveValue('2026-11');
  expect(state.approveReportPackageDemo).not.toHaveBeenCalled();
});

it('gives multiple pagers unique labels and resets the page when size changes', () => {
  const onPageChange = vi.fn(), onPageSizeChange = vi.fn();
  render(<><PaginationBar label="Phân trang hộ" page={2} pageSize={10} totalItems={30} onPageChange={onPageChange} onPageSizeChange={onPageSizeChange} /><PaginationBar label="Phân trang đơn" page={1} pageSize={10} totalItems={20} onPageChange={vi.fn()} onPageSizeChange={vi.fn()} /></>);
  const pager = screen.getByRole('navigation', { name: 'Phân trang hộ' });
  expect(screen.getByRole('navigation', { name: 'Phân trang đơn' })).toBeInTheDocument();
  fireEvent.change(within(pager).getByLabelText('Số dòng:'), { target: { value: '25' } });
  expect(onPageSizeChange).toHaveBeenCalledWith(25);
  expect(onPageChange).toHaveBeenCalledWith(1);
});


it('explains the accountant-to-administrator workflow in the billing Demo instead of offering a fake issue action', () => {
  render(<BillingPeriodsPage />);
  expect(screen.getByText(/Kế toán lập nháp/)).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: /Phát hành/ })).not.toBeInTheDocument();
});

it('keeps unavailable report downloads disabled and explains the available preview', () => {
  state.reportPackages = [{ id: 1, status: 'APPROVED', period: '2026-11', allowedFields: ['fullName'], typeLabel: 'Lưu trú' }];
  render(<><ResidencyReportPage /><CollectionRatePage /><AuthorityReportsPage /></>);
  for (const name of ['Tải tệp Excel', 'Xuất bảng Excel', 'Tải về']) {
    const button = screen.getByRole('button', { name });
    expect(button).toBeDisabled();
    expect(button).toHaveAccessibleDescription(/chưa khả dụng/);
  }
  expect(screen.getByRole('button', { name: 'Xem gói' })).toBeEnabled();
});
