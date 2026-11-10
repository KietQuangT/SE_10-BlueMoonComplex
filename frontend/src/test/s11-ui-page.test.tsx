import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ReportDownloadsPage } from '../screens/reporting/ReportDownloadsPage';
import * as reportsApi from '../api/reports';
import { ApiHttpError } from '../api/client';
import type { RoleCode } from '../types/domain';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockState = {
  user: {
    username: 'admin',
    fullName: 'Quản trị viên',
    role: 'ADMIN' as RoleCode,
    householdId: null as number | null,
    mustChangePassword: false,
  },
  mode: 'api' as const,
  handleCallFailure: vi.fn((err: unknown) => {
    if (err instanceof ApiHttpError && err.problem.status === 401) {
      return 'Phiên đăng nhập đã hết hạn. Đăng nhập lại để tiếp tục.';
    }
    return 'Lỗi thao tác';
  }),
};

vi.mock('../state/AppStateContext', () => ({
  useAppState: () => mockState,
}));

describe('S11-UI ReportDownloadsPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockState.user = {
      username: 'admin',
      fullName: 'Quản trị viên',
      role: 'ADMIN',
      householdId: null,
      mustChangePassword: false,
    };
    mockState.mode = 'api';
    vi.spyOn(reportsApi, 'triggerBlobDownload').mockReturnValue(() => {});
  });

  afterEach(() => {
    cleanup();
  });

  it('renders all three report blocks for ADMIN in API mode', () => {
    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { level: 1, name: 'Tải báo cáo' })).toBeInTheDocument();
    expect(screen.getByRole('heading', { level: 2, name: 'Thu theo kỳ' })).toBeInTheDocument();
    expect(
      screen.getByRole('heading', { level: 2, name: 'Tiền mặt theo ngày thực thu' }),
    ).toBeInTheDocument();
    expect(screen.getByRole('heading', { level: 2, name: 'Cư trú hiện tại' })).toBeInTheDocument();

    expect(screen.getByRole('button', { name: 'Tải thu theo kỳ' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Tải tiền mặt theo ngày' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Tải cư trú hiện tại' })).toBeInTheDocument();
  });

  it('renders only two financial blocks for ACCOUNTANT, hiding residency completely', () => {
    mockState.user = {
      username: 'accountant',
      fullName: 'Kế toán viên',
      role: 'ACCOUNTANT',
      householdId: null,
      mustChangePassword: false,
    };

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { level: 2, name: 'Thu theo kỳ' })).toBeInTheDocument();
    expect(
      screen.getByRole('heading', { level: 2, name: 'Tiền mặt theo ngày thực thu' }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole('heading', { level: 2, name: 'Cư trú hiện tại' }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Tải cư trú hiện tại' }),
    ).not.toBeInTheDocument();
  });

  it('displays connection notice and no download actions in Demo mode', () => {
    mockState.mode = 'demo' as any;

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { level: 1, name: 'Tải báo cáo' })).toBeInTheDocument();
    expect(screen.getByText('Kết nối backend để tải báo cáo.')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Tải thu theo kỳ' })).not.toBeInTheDocument();
  });

  it('validates period and focuses input when invalid', async () => {
    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const periodInput = screen.getByLabelText(/Kỳ thu/);
    fireEvent.change(periodInput, { target: { value: '' } });

    const submitBtn = screen.getByRole('button', { name: 'Tải thu theo kỳ' });
    fireEvent.click(submitBtn);

    expect(await screen.findByText('Vui lòng nhập kỳ thu.')).toBeInTheDocument();
    expect(periodInput).toHaveFocus();
    expect(periodInput).toHaveAttribute('aria-invalid', 'true');
  });

  it('handles successful fee collection download and shows polite confirmation status', async () => {
    const fakeBlob = new Blob(['fee-xlsx'], { type: reportsApi.XLSX_MIME });
    vi.spyOn(reportsApi, 'exportFeeCollectionReport').mockResolvedValue({
      blob: fakeBlob,
      filename: 'thu-theo-ky-2026-11.xlsx',
    });

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const periodInput = screen.getByLabelText(/Kỳ thu/);
    fireEvent.change(periodInput, { target: { value: '2026-11' } });

    const submitBtn = screen.getByRole('button', { name: 'Tải thu theo kỳ' });
    fireEvent.click(submitBtn);

    expect(
      await screen.findByText('Đã gửi tệp tới trình duyệt: thu-theo-ky-2026-11.xlsx'),
    ).toBeInTheDocument();
    expect(reportsApi.triggerBlobDownload).toHaveBeenCalledWith(
      fakeBlob,
      'thu-theo-ky-2026-11.xlsx',
    );
  });

  it('locks other download actions and reflects aria-busy during active download', async () => {
    let resolveExport!: (val: reportsApi.ReportDownloadResult) => void;
    const promise = new Promise<reportsApi.ReportDownloadResult>((resolve) => {
      resolveExport = resolve;
    });
    vi.spyOn(reportsApi, 'exportResidencyReport').mockReturnValue(promise);

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const residencyBtn = screen.getByRole('button', { name: 'Tải cư trú hiện tại' });
    const feeBtn = screen.getByRole('button', { name: 'Tải thu theo kỳ' });
    const cashBtn = screen.getByRole('button', { name: 'Tải tiền mặt theo ngày' });

    fireEvent.click(residencyBtn);

    // All buttons disabled
    expect(residencyBtn).toBeDisabled();
    expect(feeBtn).toBeDisabled();
    expect(cashBtn).toBeDisabled();

    // Section has aria-busy
    const residencySection = residencyBtn.closest('section');
    expect(residencySection).toHaveAttribute('aria-busy', 'true');
    expect(screen.getAllByText('Đang chuẩn bị tệp…').length).toBeGreaterThan(0);

    // Resolve export
    resolveExport({
      blob: new Blob(['res']),
      filename: 'cu-tru-hien-tai-20261110.xlsx',
    });

    await waitFor(() => {
      expect(
        screen.getByText('Đã gửi tệp tới trình duyệt: cu-tru-hien-tai-20261110.xlsx'),
      ).toBeInTheDocument();
    });

    expect(residencyBtn).not.toBeDisabled();
    expect(feeBtn).not.toBeDisabled();
    expect(cashBtn).not.toBeDisabled();
  });

  it('displays advice for 400 REPORT_TOO_LARGE', async () => {
    vi.spyOn(reportsApi, 'exportCashReceiptsReport').mockRejectedValue(
      new ApiHttpError({
        status: 400,
        code: 'REPORT_TOO_LARGE',
        detail: 'Report exceeds limit',
      }),
    );

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const cashBtn = screen.getByRole('button', { name: 'Tải tiền mặt theo ngày' });
    fireEvent.click(cashBtn);

    expect(
      await screen.findByText(
        'Báo cáo vượt giới hạn 50.000 dòng. Thu hẹp khoảng ngày rồi thử lại.',
      ),
    ).toBeInTheDocument();
  });

  it('displays retry button on server error and re-executes on click', async () => {
    const error500 = new ApiHttpError({
      status: 500,
      requestId: 'req-err-500',
    });
    const fakeBlob = new Blob(['data'], { type: reportsApi.XLSX_MIME });
    const spy = vi
      .spyOn(reportsApi, 'exportFeeCollectionReport')
      .mockRejectedValueOnce(error500)
      .mockResolvedValueOnce({
        blob: fakeBlob,
        filename: 'thu-theo-ky-2026-11.xlsx',
      });

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const submitBtn = screen.getByRole('button', { name: 'Tải thu theo kỳ' });
    fireEvent.click(submitBtn);

    expect(
      await screen.findByText('Có lỗi xảy ra, vui lòng thử lại. Mã yêu cầu: req-err-500.'),
    ).toBeInTheDocument();

    const retryBtn = screen.getByRole('button', { name: 'Thử tải lại' });
    fireEvent.click(retryBtn);

    expect(
      await screen.findByText('Đã gửi tệp tới trình duyệt: thu-theo-ky-2026-11.xlsx'),
    ).toBeInTheDocument();
    expect(spy).toHaveBeenCalledTimes(2);
  });

  it('redirects to /profile?tab=password when receiving 403 PASSWORD_CHANGE_REQUIRED', async () => {
    vi.spyOn(reportsApi, 'exportResidencyReport').mockRejectedValue(
      new ApiHttpError({
        status: 403,
        code: 'PASSWORD_CHANGE_REQUIRED',
      }),
    );

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const submitBtn = screen.getByRole('button', { name: 'Tải cư trú hiện tại' });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/profile?tab=password');
    });
  });

  it('redirects to /login when receiving 401 Unauthorized', async () => {
    vi.spyOn(reportsApi, 'exportResidencyReport').mockRejectedValue(
      new ApiHttpError({
        status: 401,
      }),
    );

    render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const submitBtn = screen.getByRole('button', { name: 'Tải cư trú hiện tại' });
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockState.handleCallFailure).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith('/login', {
        state: { from: '/reports' },
      });
    });
  });
});
