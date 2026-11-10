import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  fetchResidentBillingSummary,
  fetchResidentCreditAllocations,
  fetchResidentInvoiceDetail,
  fetchResidentInvoices,
  fetchResidentReceipts,
} from '../api/residentBilling';
import * as client from '../api/client';
import { formatVnd, formatVndDecimal } from '../lib/format';
import { formatResidentQuantity } from '../screens/resident/ResidentBillingHistory';

vi.mock('../api/client', () => ({
  getAuthenticated: vi.fn(),
}));

describe('S10 resident billing API client & contract', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('fetchResidentInvoices sends exact allowlisted query params and defaults to page 0 size 20', async () => {
    (client.getAuthenticated as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });

    await fetchResidentInvoices();
    expect(client.getAuthenticated).toHaveBeenCalledWith(
      '/api/v1/resident/invoices?page=0&size=20',
    );

    await fetchResidentInvoices({ period: '2026-11', paymentStatus: 'OUTSTANDING', page: 1, size: 20 });
    expect(client.getAuthenticated).toHaveBeenCalledWith(
      '/api/v1/resident/invoices?page=1&size=20&period=2026-11&paymentStatus=OUTSTANDING',
    );

    // ALL does not append paymentStatus parameter
    await fetchResidentInvoices({ paymentStatus: 'ALL', page: 0 });
    expect(client.getAuthenticated).toHaveBeenCalledWith(
      '/api/v1/resident/invoices?page=0&size=20',
    );
  });

  it('fetchResidentInvoiceDetail calls exact path with zero query params', async () => {
    (client.getAuthenticated as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      invoiceId: 42,
    });

    await fetchResidentInvoiceDetail(42);
    expect(client.getAuthenticated).toHaveBeenCalledWith('/api/v1/resident/invoices/42');
  });

  it('fetchResidentReceipts sends query params without extraneous keys', async () => {
    (client.getAuthenticated as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });

    await fetchResidentReceipts({ invoiceId: 99, status: 'POSTED', page: 2 });
    expect(client.getAuthenticated).toHaveBeenCalledWith(
      '/api/v1/resident/receipts?page=2&size=20&invoiceId=99&status=POSTED',
    );

    // Omitted invoiceId for whole-household history
    await fetchResidentReceipts({ status: 'ALL' });
    expect(client.getAuthenticated).toHaveBeenCalledWith(
      '/api/v1/resident/receipts?page=0&size=20',
    );
  });

  it('fetchResidentCreditAllocations sends query params without extraneous keys', async () => {
    (client.getAuthenticated as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });

    await fetchResidentCreditAllocations({ invoiceId: 77, status: 'REVERSED', page: 0 });
    expect(client.getAuthenticated).toHaveBeenCalledWith(
      '/api/v1/resident/credit-allocations?page=0&size=20&invoiceId=77&status=REVERSED',
    );
  });

  it('fetchResidentBillingSummary sends zero query params', async () => {
    (client.getAuthenticated as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      totalOutstanding: '0',
      outstandingInvoiceCount: 0,
      creditBalance: '0',
    });

    await fetchResidentBillingSummary();
    expect(client.getAuthenticated).toHaveBeenCalledWith('/api/v1/resident/billing-summary');
  });

  it('formats >2^53 amounts and decimal unit prices with safe string formatting', () => {
    // Exact string VND > 2^53 (e.g. 9007199254740993)
    expect(formatVnd('9007199254740993')).toBe('9.007.199.254.740.993 đ');
    expect(formatVnd('0')).toBe('0 đ');
    expect(formatVnd(null)).toBe('0 đ');

    // Unit prices decimal rules per contract: 10000.00 -> 10.000 đ, 10000.25 -> 10.000,25 đ
    expect(formatVndDecimal('10000.00')).toBe('10.000 đ');
    expect(formatVndDecimal('10000.25')).toBe('10.000,25 đ');
    expect(formatVndDecimal('500000')).toBe('500.000 đ');

    // Quantity snapshot formatting
    expect(
      formatResidentQuantity({
        calculationMethod: 'PER_HOUSEHOLD',
        unit: 'HOUSEHOLD',
        quantity: '1.00',
        areaM2: null,
      }),
    ).toBe('1 hộ');
    expect(
      formatResidentQuantity({
        calculationMethod: 'AREA',
        unit: 'M2',
        quantity: '55.50',
        areaM2: '55.50',
      }),
    ).toBe('55.50 m²');
  });
});
