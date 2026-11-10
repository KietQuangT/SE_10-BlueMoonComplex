import { render, screen, cleanup, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ApiInvoiceDetailPage } from '../screens/resident/ApiInvoiceDetailPage';
import * as residentApi from '../api/residentBilling';
import { ApiHttpError } from '../api/client';

const mockHandleCallFailure = vi.fn();

vi.mock('../state/AppStateContext', () => ({
  useAppState: () => ({
    mode: 'api',
    user: { role: 'RESIDENT', username: 'resident10', householdId: 10 },
    handleCallFailure: mockHandleCallFailure,
  }),
}));

const mockDetail: residentApi.ResidentInvoiceDetail = {
  invoiceId: 42,
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
  lines: [
    {
      feeTypeCode: 'SERVICE',
      feeTypeName: 'Phí dịch vụ quản lý',
      calculationMethod: 'AREA',
      quantity: '50.00',
      unit: 'M2',
      areaM2: '50.00',
      unitPrice: '10000.00',
      amount: '500000',
    },
    {
      feeTypeCode: 'GARBAGE',
      feeTypeName: 'Phí rác sinh hoạt',
      calculationMethod: 'PER_HOUSEHOLD',
      quantity: '1.00',
      unit: 'HOUSEHOLD',
      areaM2: null,
      unitPrice: '30000.00',
      amount: '30000',
    },
  ],
};

const mockReceipts: residentApi.PageResponse<residentApi.ResidentReceipt> = {
  content: [
    {
      receiptId: 501,
      invoiceId: 42,
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
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
};

describe('S10 ApiInvoiceDetailPage (M-33)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(residentApi, 'fetchResidentInvoiceDetail').mockResolvedValue(mockDetail);
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue(mockReceipts);
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue(mockAllocations);
  });

  afterEach(cleanup);

  it('renders snapshot lines, fee calculations, and exact totals with settled wording', async () => {
    render(
      <MemoryRouter initialEntries={['/resident/invoices/42']}>
        <Routes>
          <Route path="/resident/invoices/:id" element={<ApiInvoiceDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Các khoản phí thành phần')).toBeInTheDocument();
    });

    // Check lines and quantities (both desktop table and mobile list)
    expect(screen.getAllByText('Phí dịch vụ quản lý')[0]).toBeInTheDocument();
    expect(screen.getAllByText('50.00 m²')[0]).toBeInTheDocument();
    expect(screen.getAllByText('10.000 đ')[0]).toBeInTheDocument();
    expect(screen.getAllByText('500.000 đ')[0]).toBeInTheDocument();

    expect(screen.getAllByText('Phí rác sinh hoạt')[0]).toBeInTheDocument();
    expect(screen.getAllByText('1 hộ')[0]).toBeInTheDocument();
    expect(screen.getAllByText('30.000 đ')[0]).toBeInTheDocument();

    // Check totals DL
    expect(screen.getByText('Tổng hóa đơn')).toBeInTheDocument();
    expect(screen.getByText('530.000 đ')).toBeInTheDocument();

    expect(screen.getByText('Đã thanh toán')).toBeInTheDocument();
    expect(screen.getAllByText('200.000 đ').length).toBeGreaterThan(0);
    expect(screen.getByText(/Gồm tiền thu áp dụng và số dư đã bù trừ/)).toBeInTheDocument();
    expect(screen.queryByText('Đã thu')).not.toBeInTheDocument();

    expect(screen.getByText('Còn nợ')).toBeInTheDocument();
    expect(screen.getByText('330.000 đ')).toBeInTheDocument();

    // Cash payment guide rendered because ISSUED and remaining > 0
    expect(screen.getByText('Cách đóng tiền')).toBeInTheDocument();
    expect(
      screen.getByText(/Liên hệ Kế toán hoặc Ban quản trị để nộp tiền mặt; báo mã căn/i),
    ).toBeInTheDocument();
  });

  it('renders cancelled invoice with banner and suppresses cash payment guide', async () => {
    const cancelledDetail: residentApi.ResidentInvoiceDetail = {
      ...mockDetail,
      invoiceStatus: 'CANCELLED',
      paymentStatus: 'CANCELLED',
      cancelledAt: '2026-11-05T10:00:00Z',
      settled: '0',
      remaining: '0',
    };
    vi.spyOn(residentApi, 'fetchResidentInvoiceDetail').mockResolvedValueOnce(cancelledDetail);

    render(
      <MemoryRouter initialEntries={['/resident/invoices/42']}>
        <Routes>
          <Route path="/resident/invoices/:id" element={<ApiInvoiceDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText(/Hóa đơn đã bị hủy/)).toBeInTheDocument();
    });

    expect(
      screen.getByText(/Tổng tiền gốc chỉ để đối chiếu, không phát sinh nghĩa vụ thanh toán/),
    ).toBeInTheDocument();
    expect(screen.queryByText('Cách đóng tiền')).not.toBeInTheDocument();
  });

  it('scopes history calls to this invoiceId', async () => {
    render(
      <MemoryRouter initialEntries={['/resident/invoices/42']}>
        <Routes>
          <Route path="/resident/invoices/:id" element={<ApiInvoiceDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Thu tiền mặt')).toBeInTheDocument();
    });

    expect(residentApi.fetchResidentReceipts).toHaveBeenCalledWith(
      expect.objectContaining({ invoiceId: 42 }),
    );
    expect(residentApi.fetchResidentCreditAllocations).toHaveBeenCalledWith(
      expect.objectContaining({ invoiceId: 42 }),
    );
  });

  it('handles 404 invoice not found gracefully and does not call history', async () => {
    vi.spyOn(residentApi, 'fetchResidentInvoiceDetail').mockRejectedValueOnce(
      new ApiHttpError({ status: 404, title: 'Not Found' }),
    );

    render(
      <MemoryRouter initialEntries={['/resident/invoices/999']}>
        <Routes>
          <Route path="/resident/invoices/:id" element={<ApiInvoiceDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Không tìm thấy hóa đơn')).toBeInTheDocument();
    });

    expect(residentApi.fetchResidentReceipts).not.toHaveBeenCalled();
    expect(residentApi.fetchResidentCreditAllocations).not.toHaveBeenCalled();
  });

  it('handles invalid route id without calling API', async () => {
    render(
      <MemoryRouter initialEntries={['/resident/invoices/not-a-number']}>
        <Routes>
          <Route path="/resident/invoices/:id" element={<ApiInvoiceDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Mã hóa đơn không hợp lệ')).toBeInTheDocument();
    });

    expect(residentApi.fetchResidentInvoiceDetail).not.toHaveBeenCalled();
  });
});
