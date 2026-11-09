import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { FeeRatesPage } from '../screens/accountant/FeeRatesPage';
import { ApiHttpError } from '../api/client';
import { messageForError } from '../api/errors';
import * as fees from '../api/fees';

const state = vi.hoisted(() => ({ role: 'ACCOUNTANT', mode: 'api' }));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ mode: state.mode, user: { role: state.role, username: 'accountant' }, handleCallFailure: messageForError }) }));
vi.mock('../api/fees', () => ({ fetchFeeTypes: vi.fn(), fetchFeeRates: vi.fn(), createFeeRate: vi.fn(), updateFeeRate: vi.fn(), deleteFeeRate: vi.fn() }));
const rates: fees.FeeRate[] = [
  { id: 1, feeTypeId: 1, feeTypeCode: 'MANAGEMENT_FEE', feeTypeName: 'Phí quản lý', unitPrice: 7000.5, effectiveFrom: '2099-12-01', createdAt: '', editable: true },
  { id: 2, feeTypeId: 1, feeTypeCode: 'MANAGEMENT_FEE', feeTypeName: 'Phí quản lý', unitPrice: 10000, effectiveFrom: '2026-11-01', createdAt: '', editable: false },
];
beforeEach(() => {
  vi.setSystemTime(new Date('2026-11-01T00:00:00Z'));
  state.role = 'ACCOUNTANT'; state.mode = 'api';
  vi.mocked(fees.fetchFeeTypes).mockResolvedValue([{ id: 1, code: 'MANAGEMENT_FEE', name: 'Phí quản lý', calculationMethod: 'AREA', unit: 'm²/tháng' }]);
  vi.mocked(fees.fetchFeeRates).mockResolvedValue({ content: rates, page: 0, size: 10, totalElements: 2, totalPages: 1 });
});
afterEach(() => { cleanup(); vi.clearAllMocks(); vi.useRealTimers(); });
it('shows actions only for editable rows', async () => {
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  expect(screen.getAllByRole('button', { name: 'Sửa' })).toHaveLength(1);
  expect(screen.getAllByRole('button', { name: 'Xóa' })).toHaveLength(1);
  expect(screen.getByText('Đã khóa')).toBeInTheDocument();
});
it('keeps edit and delete actions together in the value cell', async () => {
  render(<FeeRatesPage />);
  const edit = await screen.findByRole('button', { name: 'Sửa' });
  const remove = screen.getByRole('button', { name: 'Xóa' });
  const actions = edit.closest('.row-actions');
  expect(actions).not.toBeNull();
  expect(actions).toContainElement(remove);
  expect(actions?.parentElement).toHaveAttribute('data-label', 'Hành động');
});
it('opens the edit form above the table with the current price focused and cancels it', async () => {
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Sửa' }));
  const form = screen.getByRole('region', { name: 'Sửa đơn giá' });
  const price = screen.getByLabelText('Đơn giá');
  expect(form.compareDocumentPosition(screen.getByRole('table')) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  expect(price).toHaveValue(String(rates[0].unitPrice));
  expect(price).toHaveFocus();
  fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
  expect(screen.queryByRole('region', { name: 'Sửa đơn giá' })).not.toBeInTheDocument();
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
  expect(month).toHaveValue('2026-12');
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

it('requires a fee type before creating a rate', async () => {
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Thêm đơn giá' }));
  fireEvent.change(screen.getByLabelText('Loại phí', { selector: '#fee-type' }), { target: { value: '' } });
  fireEvent.change(screen.getByLabelText('Đơn giá'), { target: { value: '7000' } });
  fireEvent.click(screen.getByRole('button', { name: 'Lưu' }));
  expect(screen.getByRole('alert')).toHaveTextContent('Chọn loại phí.');
  fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
});

it('creates a rate with an exact decimal and first day of the month', async () => {
  vi.setSystemTime(new Date('2026-11-01T00:00:00Z'));
  vi.mocked(fees.createFeeRate).mockResolvedValueOnce(rates[0]);
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Thêm đơn giá' }));
  fireEvent.change(screen.getByLabelText('Đơn giá'), { target: { value: '7000,50' } });
  fireEvent.click(screen.getByRole('button', { name: 'Lưu' }));
  expect(await screen.findByRole('status')).toHaveTextContent('Đã thêm đơn giá');
  expect(fees.createFeeRate).toHaveBeenCalledWith({ feeTypeId: 1, unitPrice: '7000.50', effectiveFrom: '2026-12-01' });
});

it('rejects the current month', async () => {
  vi.setSystemTime(new Date('2026-11-01T00:00:00Z'));
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Thêm đơn giá' }));
  fireEvent.change(screen.getByLabelText('Tháng hiệu lực'), { target: { value: '2026-11' } });
  fireEvent.change(screen.getByLabelText('Đơn giá'), { target: { value: '7000' } });
  fireEvent.click(screen.getByRole('button', { name: 'Lưu' }));
  expect(screen.getByRole('alert')).toHaveTextContent('Tháng hiệu lực không hợp lệ');
  expect(fees.createFeeRate).not.toHaveBeenCalled();
  fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
});

it('defaults to the next Vietnam month at the UTC boundary', async () => {
  vi.setSystemTime(new Date('2026-11-30T17:30:00Z'));
  render(<FeeRatesPage />);
  await screen.findByText('7.000,50 đ');
  fireEvent.click(screen.getByRole('button', { name: 'Thêm đơn giá' }));
  expect(screen.getByLabelText('Tháng hiệu lực')).toHaveValue('2027-01');
  fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
});


it('never reads or changes real fee rates when the user enters Demo mode', () => {
  state.mode = 'demo';
  render(<FeeRatesPage />);
  expect(screen.queryByRole('button', { name: 'Thêm đơn giá' })).not.toBeInTheDocument();
  expect(fees.fetchFeeRates).not.toHaveBeenCalled();
  expect(fees.fetchFeeTypes).not.toHaveBeenCalled();
});
