import { act, render, screen, cleanup, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BrowserRouter, MemoryRouter, Route, Routes } from 'react-router-dom';
import { ApiInvoicesPage } from '../screens/resident/ApiInvoicesPage';
import { ApiInvoiceDetailPage } from '../screens/resident/ApiInvoiceDetailPage';
import * as residentApi from '../api/residentBilling';
import { ApiHttpError } from '../api/client';

const mockHandleCallFailure = vi.fn();
const mockNavigate = vi.fn();

vi.mock('../state/AppStateContext', () => ({
  useAppState: () => ({
    mode: 'api',
    user: { role: 'RESIDENT', username: 'resident10', householdId: 10 },
    handleCallFailure: mockHandleCallFailure,
  }),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('S10 session expiration, auth guards & privacy traps', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(cleanup);

  it.each(['receipts', 'allocations'] as const)(
    'hides all billing data when the %s history request receives 403',
    async (block) => {
      const empty = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
      vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockResolvedValue({
        totalOutstanding: '430000', outstandingInvoiceCount: 2,
        nearestDueDate: null, nearestDuePeriod: null, creditBalance: '70000',
      });
      vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValue(empty);
      const receipt = vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue(empty);
      const allocation = vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue(empty);
      (block === 'receipts' ? receipt : allocation).mockRejectedValue(
        new ApiHttpError({ status: 403, title: 'Forbidden' }),
      );

      render(<BrowserRouter><ApiInvoicesPage /></BrowserRouter>);

      await waitFor(() => expect(screen.getByText('Không có quyền truy cập')).toBeInTheDocument());
      expect(screen.queryByText('430.000 đ')).not.toBeInTheDocument();
      expect(screen.queryByText('Các kỳ hóa đơn')).not.toBeInTheDocument();
    },
  );

  it('ignores a late 401 from the previous screen after unmount', async () => {
    let rejectOld!: (reason: unknown) => void;
    const pending = new Promise<residentApi.ResidentBillingSummary>((_, reject) => { rejectOld = reject; });
    const empty = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockReturnValue(pending);
    vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValue(empty);
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue(empty);
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue(empty);
    const view = render(<BrowserRouter><ApiInvoicesPage /></BrowserRouter>);
    view.unmount();

    await act(async () => { rejectOld(new ApiHttpError({ status: 401, title: 'Unauthorized' })); });
    expect(mockHandleCallFailure).not.toHaveBeenCalled();
  });

  it('triggers session expiration on 401 and clears all rendered billing data', async () => {
    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockRejectedValueOnce(
      new ApiHttpError({ status: 401, title: 'Unauthorized' }),
    );
    vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });

    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Phiên đăng nhập đã hết hạn')).toBeInTheDocument();
    });

    expect(mockHandleCallFailure).toHaveBeenCalled();
    expect(screen.queryByText('Còn nợ bắt buộc')).not.toBeInTheDocument();
    expect(screen.queryByText('Các kỳ hóa đơn')).not.toBeInTheDocument();
  });

  it('redirects to /profile?tab=password when receiving 403 PASSWORD_CHANGE_REQUIRED', async () => {
    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockRejectedValueOnce(
      new ApiHttpError({ status: 403, code: 'PASSWORD_CHANGE_REQUIRED', title: 'Forbidden' }),
    );

    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/profile?tab=password');
    });
  });

  it('displays generic forbidden error and hides private data on other 403 errors', async () => {
    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockRejectedValueOnce(
      new ApiHttpError({ status: 403, title: 'Forbidden' }),
    );

    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Không có quyền truy cập')).toBeInTheDocument();
    });

    expect(screen.queryByText('Còn nợ bắt buộc')).not.toBeInTheDocument();
  });

  it('never displays decoy PII or staff actor traps even if present in raw responses', async () => {
    const rawTrapInvoice = {
      invoiceId: 88,
      period: '2026-11',
      householdCode: 'H-010',
      apartmentCode: 'A1001',
      dueDate: '2026-11-25',
      issuedAt: '2026-11-01T08:00:00Z',
      cancelledAt: null,
      invoiceStatus: 'ISSUED' as const,
      paymentStatus: 'PAID' as const,
      total: '100000',
      settled: '100000',
      remaining: '0',
      // Decoy trap keys that must never be shown
      staff_actor: 'SuperSecretStaffActor',
      collector_phone: '09012345678',
      internal_notes: 'Highly classified financial audit trace',
    };

    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockResolvedValueOnce({
      totalOutstanding: '0',
      outstandingInvoiceCount: 0,
      nearestDueDate: null,
      nearestDuePeriod: null,
      creditBalance: '0',
    });

    vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValueOnce({
      content: [rawTrapInvoice],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    });
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValueOnce({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    });

    render(
      <BrowserRouter>
        <ApiInvoicesPage />
      </BrowserRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Các kỳ hóa đơn')).toBeInTheDocument();
    });

    // Verify none of the trap strings leaked into DOM
    expect(screen.queryByText(/SuperSecretStaffActor/)).not.toBeInTheDocument();
    expect(screen.queryByText(/09012345678/)).not.toBeInTheDocument();
    expect(screen.queryByText(/Highly classified financial audit trace/)).not.toBeInTheDocument();
  });

  it('triggers session expiration on 401 in detail page and clears detail', async () => {
    vi.spyOn(residentApi, 'fetchResidentInvoiceDetail').mockRejectedValueOnce(
      new ApiHttpError({ status: 401, title: 'Unauthorized' }),
    );

    render(
      <MemoryRouter initialEntries={['/resident/invoices/42']}>
        <Routes>
          <Route path="/resident/invoices/:id" element={<ApiInvoiceDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );

    await waitFor(() => {
      expect(screen.getByText('Phiên đăng nhập đã hết hạn')).toBeInTheDocument();
    });

    expect(mockHandleCallFailure).toHaveBeenCalled();
    expect(screen.queryByText('Các khoản phí thành phần')).not.toBeInTheDocument();
  });

  it('ignores a late 401 on detail page after unmount', async () => {
    let rejectOld!: (reason: unknown) => void;
    const pending = new Promise<residentApi.ResidentInvoiceDetail>((_, reject) => { rejectOld = reject; });
    vi.spyOn(residentApi, 'fetchResidentInvoiceDetail').mockReturnValue(pending);
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });

    const view = render(
      <MemoryRouter initialEntries={['/resident/invoices/42']}>
        <Routes>
          <Route path="/resident/invoices/:id" element={<ApiInvoiceDetailPage />} />
        </Routes>
      </MemoryRouter>,
    );
    view.unmount();

    await act(async () => { rejectOld(new ApiHttpError({ status: 401, title: 'Unauthorized' })); });
    expect(mockHandleCallFailure).not.toHaveBeenCalled();
  });

  it('ignores a late 403 PASSWORD_CHANGE_REQUIRED after unmount and does not navigate', async () => {
    let rejectOld!: (reason: unknown) => void;
    const pending = new Promise<residentApi.ResidentBillingSummary>((_, reject) => { rejectOld = reject; });
    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockReturnValue(pending);
    vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValue({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });

    const view = render(<BrowserRouter><ApiInvoicesPage /></BrowserRouter>);
    view.unmount();

    await act(async () => { rejectOld(new ApiHttpError({ status: 403, code: 'PASSWORD_CHANGE_REQUIRED', title: 'Forbidden' })); });
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it.each([['receipts', 401], ['receipts', 403], ['allocations', 401], ['allocations', 403]] as const)(
    'ignores a late %s history error %s after unmount',
    async (block, status) => {
      let rejectHistory!: (reason: unknown) => void;
      const pendingHistory = new Promise<residentApi.PageResponse<any>>((_, reject) => { rejectHistory = reject; });
      const empty = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
      vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockResolvedValue({
        totalOutstanding: '0', outstandingInvoiceCount: 0,
        nearestDueDate: null, nearestDuePeriod: null, creditBalance: '0',
      });
      vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValue(empty);
      const receipt = vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue(empty);
      const allocation = vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue(empty);
      (block === 'receipts' ? receipt : allocation).mockReturnValue(pendingHistory);

      const view = render(<BrowserRouter><ApiInvoicesPage /></BrowserRouter>);
      view.unmount();

      await act(async () => { rejectHistory(new ApiHttpError({ status, code: 'PASSWORD_CHANGE_REQUIRED', title: 'Unauthorized' })); });
      expect(mockHandleCallFailure).not.toHaveBeenCalled();
      expect(mockNavigate).not.toHaveBeenCalled();
    },
  );
  it.each(['receipts', 'allocations'] as const)(
    'redirects on PASSWORD_CHANGE_REQUIRED from %s history', async (block) => {
      const empty = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
      vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockResolvedValue({
        totalOutstanding: '430000', outstandingInvoiceCount: 2,
        nearestDueDate: null, nearestDuePeriod: null, creditBalance: '70000',
      });
      vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValue(empty);
      const receipt = vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue(empty);
      const allocation = vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue(empty);
      (block === 'receipts' ? receipt : allocation).mockRejectedValue(
        new ApiHttpError({ status: 403, code: 'PASSWORD_CHANGE_REQUIRED', title: 'Forbidden' }),
      );
      render(<BrowserRouter><ApiInvoicesPage /></BrowserRouter>);
      await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/profile?tab=password'));
      expect(screen.queryByText('430.000 đ')).not.toBeInTheDocument();
    },
  );

  it.each([401, 403])('ignores a late %s from the prior session after keyed remount', async (status) => {
    let rejectOld!: (reason: unknown) => void;
    const pending = new Promise<residentApi.ResidentBillingSummary>((_, reject) => { rejectOld = reject; });
    const empty = { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
    vi.spyOn(residentApi, 'fetchResidentBillingSummary').mockReturnValueOnce(pending).mockResolvedValue({
      totalOutstanding: '120000', outstandingInvoiceCount: 1,
      nearestDueDate: null, nearestDuePeriod: null, creditBalance: '0',
    });
    vi.spyOn(residentApi, 'fetchResidentInvoices').mockResolvedValue(empty);
    vi.spyOn(residentApi, 'fetchResidentReceipts').mockResolvedValue(empty);
    vi.spyOn(residentApi, 'fetchResidentCreditAllocations').mockResolvedValue(empty);
    const view = render(<BrowserRouter><ApiInvoicesPage key="old-user:10" /></BrowserRouter>);
    view.rerender(<BrowserRouter><ApiInvoicesPage key="new-user:11" /></BrowserRouter>);
    await screen.findByText('120.000 đ');
    await act(async () => { rejectOld(new ApiHttpError({ status, code: 'PASSWORD_CHANGE_REQUIRED', title: 'Forbidden' })); });
    expect(mockHandleCallFailure).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
    expect(screen.getByText('120.000 đ')).toBeInTheDocument();
  });

});
