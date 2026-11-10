import { render, screen, cleanup, fireEvent, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { ApiInvoicesPage } from '../screens/resident/ApiInvoicesPage';
import * as residentApi from '../api/residentBilling';

const mockHandleCallFailure = vi.fn();

vi.mock('../state/AppStateContext', () => ({
  useAppState: () => ({
    mode: 'api',
    user: { role: 'RESIDENT', username: 'resident10', householdId: 10 },
    handleCallFailure: mockHandleCallFailure,
  }),
}));

const mockSummary: residentApi.ResidentBillingSummary = {
  totalOutstanding: '430000',
  outstandingInvoiceCount: 2,
  nearestDueDate: '2026-11-25',
  nearestDuePeriod: '2026-11',
  creditBalance: '70000',
};

const mockInvoices: residentApi.PageResponse<residentApi.ResidentInvoiceSummary> = {
  content: [
    {
      invoiceId: 101,
      period: '2026-11',
      householdCode: 'H-010',
      apartmentCode: 'A1001',
      dueDate: '2026-11-25',
      issuedAt: '2026-11-01T08:00:00Z',
      cancelledAt: null,
      invoiceStatus: 'ISSUED',
      paymentStatus: 'PARTIALLY_PAID',
      total: '530000',
      settled: '200000',
      remaining: '330000',
    },
    {
      invoiceId: 102,
      period: '2026-11',
      householdCode: 'H-010',
      apartmentCode: 'A1001',
      dueDate: '2026-11-20',
      issuedAt: '2026-11-01T08:00:00Z',
      cancelledAt: '2026-11-05T10:00:00Z',
      invoiceStatus: 'CANCELLED',
      paymentStatus: 'CANCELLED',
      total: '500000',
      settled: '0',
      remaining: '0',
    },
  ],
  page: 0,
  size: 20,
  totalElements: 2,
  totalPages: 1,
};

const mockReceipts: residentApi.PageResponse<residentApi.ResidentReceipt> = {
  content: [
    {
      receiptId: 501,
      invoiceId: 101,
      period: '2026-11',
      paidOn: '2026-11-03',
      recordedAt: '2026-11-03T09:30:00Z',
      amount: '200000',
      appliedAmount: '200000',
      creditAmount: '0',
      status: 'POSTED',
      voidedAt: null,
      hasLateReason: false,
      hasVoidReason: false,
    },
  ],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

const mockAllocations: residentApi.PageResponse<residentApi.ResidentAllocation> = {
  content: [
    {
      allocationId: 701,
      invoiceId: 101,
      period: '2026-11',
      amount: '50000',
      status: 'ACTIVE',
      recordedAt: '2026-11-04T14:15:00Z',
      reversedAt: null,
      hasReverseReason: false,
    },
  ],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

describe('S10 ApiInvoicesPage (M-32)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockResolvedValue(mockSummary);
    vi.spyOn(residentApi, 'fetchResidentInvoices').mockImplementation(async (params) => {
      if (params?.paymentStatus === 'UNPAID') {
        return { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
      }
      return mockInvoices;
    });
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue(mockReceipts);
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue(mockAllocations);
  });

  afterEach(cleanup);

  it('renders summary stations with correct VND, invoice count and credit explanation', async () => {
    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Còn nợ bắt buộc')).toBeInTheDocument();
    });

    expect(screen.getByText('430.000 đ')).toBeInTheDocument();
    expect(screen.getByText('2 hóa đơn còn nợ')).toBeInTheDocument();
    expect(screen.getAllByText('25/11/2026').length).toBeGreaterThan(0);
    expect(screen.getByText('70.000 đ')).toBeInTheDocument();
    expect(screen.getByText(/Số dư dùng bù trừ hóa đơn, không tự trừ nợ/)).toBeInTheDocument();
  });

  it('renders invoice table rows with status badges and preserves both cancelled and active rows', async () => {
    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getAllByText('Đã thanh toán một phần').length).toBeGreaterThan(0);
      expect(screen.getAllByText('Đã hủy').length).toBeGreaterThan(0);
    });

    // Check row values for invoice 101
    expect(screen.getAllByText('330.000 đ').length).toBeGreaterThan(0);
    expect(screen.getAllByText('530.000 đ').length).toBeGreaterThan(0);
  });

  it('applies filters, resets page to 0, and handles clear filter', async () => {
    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByLabelText('Lọc theo kỳ thu')).toBeInTheDocument();
    });

    const periodInput = screen.getByLabelText('Lọc theo kỳ thu');
    const statusSelect = screen.getByLabelText('Lọc theo trạng thái thanh toán');
    const applyBtn = screen.getByRole('button', { name: 'Áp dụng' });

    fireEvent.change(periodInput, { target: { value: '2026-11' } });
    fireEvent.change(statusSelect, { target: { value: 'PARTIALLY_PAID' } });
    fireEvent.click(applyBtn);

    expect(residentApi.fetchResidentInvoices).toHaveBeenCalledWith({
      period: '2026-11',
      paymentStatus: 'PARTIALLY_PAID',
      page: 0,
      size: 20,
    });

    const clearBtn = screen.getByRole('button', { name: 'Xóa lọc' });
    fireEvent.click(clearBtn);

    expect(residentApi.fetchResidentInvoices).toHaveBeenCalledWith({
      period: undefined,
      paymentStatus: 'ALL',
      page: 0,
      size: 20,
    });
  });

  it('renders history blocks for cash receipts and credit allocations with privacy guarantees', async () => {
    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Thu tiền mặt')).toBeInTheDocument();
      expect(screen.getByText('Bù trừ số dư')).toBeInTheDocument();
    });

    expect(
      screen.getByText('Đây là số dư được dùng cho hóa đơn, không phải khoản tiền nộp mới.'),
    ).toBeInTheDocument();
    expect(screen.getAllByText('#501').length).toBeGreaterThan(0);
    expect(screen.getAllByText('#701').length).toBeGreaterThan(0);

    // Privacy checks: NO staff actor, raw reason, or sensitive keys in DOM
    expect(screen.queryByText(/Người thu/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/staff/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/actor/i)).not.toBeInTheDocument();
  });

  it('renders empty state when filter yields no results', async () => {
    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByLabelText('Lọc theo trạng thái thanh toán')).toBeInTheDocument();
    });

    const statusSelect = screen.getByLabelText('Lọc theo trạng thái thanh toán');
    fireEvent.change(statusSelect, { target: { value: 'UNPAID' } });
    fireEvent.click(screen.getByRole('button', { name: 'Áp dụng' }));

    await waitFor(() => {
      expect(screen.getByText('Không có hóa đơn phù hợp')).toBeInTheDocument();
    });
  });
});
