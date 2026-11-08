import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { FeeRatesPage } from '../screens/accountant/FeeRatesPage';
import { ApiHttpError } from '../api/client';
import { messageForError } from '../api/errors';
import * as fees from '../api/fees';

const state = vi.hoisted(() => ({ role: 'ACCOUNTANT' }));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: { role: state.role, username: 'accountant' }, handleCallFailure: messageForError }) }));
vi.mock('../api/fees', () => ({ fetchFeeTypes: vi.fn(), fetchFeeRates: vi.fn(), createFeeRate: vi.fn(), updateFeeRate: vi.fn(), deleteFeeRate: vi.fn() }));
const rates: fees.FeeRate[] = [
  { id: 1, feeTypeId: 1, feeTypeCode: 'MANAGEMENT_FEE', feeTypeName: 'Phí quản lý', unitPrice: '7000.50', effectiveFrom: '2099-12-01', createdAt: '', editable: true },
  { id: 2, feeTypeId: 1, feeTypeCode: 'MANAGEMENT_FEE', feeTypeName: 'Phí quản lý', unitPrice: 10000, effectiveFrom: '2026-11-01', createdAt: '', editable: false },
];
beforeEach(() => {
  state.role = 'ACCOUNTANT';
  vi.mocked(fees.fetchFeeTypes).mockResolvedValue([{ id: 1, code: 'MANAGEMENT_FEE', name: 'Phí quản lý', calculationMethod: 'AREA', unit: 'm²/tháng' }]);
  vi.mocked(fees.fetchFeeRates).mockResolvedValue({ content: rates, page: 0, size: 10, totalElements: 2, totalPages: 1 });
});
afterEach(() => { cleanup(); vi.clearAllMocks(); });
it('shows actions only for editable rows', async () => {
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  expect(screen.getAllByRole('button', { name: 'Sửa' })).toHaveLength(1);
  expect(screen.getAllByRole('button', { name: 'Xóa' })).toHaveLength(1);
  expect(screen.getByText('Đã khóa')).toBeInTheDocument();
});
it('shows administrators read-only access', async () => {
  state.role = 'ADMIN';
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  expect(screen.getByText(/Bạn chỉ xem được/)).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: /Thêm đơn giá|Sửa|Xóa/ })).not.toBeInTheDocument();
});
it.each(['0', '1.001', '10000000000'])('rejects invalid price %s and defaults to next month', async (price) => {
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Thêm đơn giá' }));
  const month = screen.getByLabelText('Tháng hiệu lực');
  const parts = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit' }).formatToParts(new Date());
  const year = Number(parts.find((p) => p.type === 'year')?.value);
  const current = Number(parts.find((p) => p.type === 'month')?.value);
  expect(month).toHaveValue(`${year + (current === 12 ? 1 : 0)}-${String(current === 12 ? 1 : current + 1).padStart(2, '0')}`);
  fireEvent.change(screen.getByLabelText('Đơn giá'), { target: { value: price } });
  fireEvent.click(screen.getByRole('button', { name: 'Lưu' }));
  expect(screen.getByRole('alert')).toHaveTextContent('Đơn giá phải lớn hơn 0');
  expect(fees.createFeeRate).not.toHaveBeenCalled();
  fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
});
it('shows frozen conflict returned while editing', async () => {
  vi.mocked(fees.updateFeeRate).mockRejectedValueOnce(new ApiHttpError({ status: 409, code: 'FEE_RATE_FROZEN' }));
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Sửa' }));
  fireEvent.click(screen.getByRole('button', { name: 'Lưu' }));
  expect(await screen.findByRole('alert')).toHaveTextContent('Không sửa được đơn giá của tháng hiện tại hoặc tháng đã qua.');
  fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
});

it('preserves an unfinished form after a 401 and remount', async () => {
  vi.mocked(fees.createFeeRate).mockRejectedValueOnce(new ApiHttpError({ status: 401 }));
  const view = render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Thêm đơn giá' }));
  fireEvent.change(screen.getByLabelText('Đơn giá'), { target: { value: '7000,50' } });
  fireEvent.click(screen.getByRole('button', { name: 'Lưu' }));
  await screen.findByRole('alert');
  view.unmount();
  render(<FeeRatesPage />);
  expect(screen.getByLabelText('Đơn giá')).toHaveValue('7000,50');
  fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
});
