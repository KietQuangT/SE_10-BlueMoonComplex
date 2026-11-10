import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ReportDownloadsPage } from '../screens/reporting/ReportDownloadsPage';
import { AppShell } from '../components/AppShell';
import * as reportsApi from '../api/reports';
import type { RoleCode } from '../types/domain';

const mockState = {
  user: {
    username: 'admin1',
    fullName: 'Admin Một',
    role: 'ADMIN' as RoleCode,
    householdId: null,
    mustChangePassword: false,
  },
  mode: 'api' as const,
  households: [],
  invoices: [],
  residencyChanges: [],
  absenceCount: 0,
  handleCallFailure: vi.fn(),
  logout: vi.fn(),
  setQaDrawerOpen: vi.fn(),
  forcedUiState: 'normal' as const,
};

vi.mock('../state/AppStateContext', () => ({
  useAppState: () => mockState,
}));

describe('S11-UI Session Resilience and Navigation UX', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockState.user = {
      username: 'admin1',
      fullName: 'Admin Một',
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

  it('renders navigation menu linking to /reports for ADMIN and ACCOUNTANT', () => {
    // 1. ADMIN
    mockState.user.role = 'ADMIN';
    const { unmount } = render(
      <MemoryRouter initialEntries={['/reports']}>
        <Routes>
          <Route element={<AppShell />}>
            <Route path="/reports" element={<div>REPORTS PAGE</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );

    const reportLinks = screen.getAllByRole('link', { name: /Báo cáo/ });
    expect(reportLinks.some((l) => l.getAttribute('href') === '/reports')).toBe(true);
    expect(
      screen.getByRole('link', { name: /Gói cho cơ quan/ }).getAttribute('href'),
    ).toBe('/admin/reports/packages');
    unmount();

    // 2. ACCOUNTANT
    mockState.user.role = 'ACCOUNTANT';
    const { unmount: unmountAccountant } = render(
      <MemoryRouter initialEntries={['/reports']}>
        <Routes>
          <Route element={<AppShell />}>
            <Route path="/reports" element={<div>REPORTS PAGE</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );

    const accountantReportLinks = screen.getAllByRole('link', { name: /Báo cáo/ });
    expect(
      accountantReportLinks.some((l) => l.getAttribute('href') === '/reports'),
    ).toBe(true);
    unmountAccountant();

    // 3. RESIDENT
    mockState.user.role = 'RESIDENT';
    render(
      <MemoryRouter initialEntries={['/resident/invoices']}>
        <Routes>
          <Route element={<AppShell />}>
            <Route path="/resident/invoices" element={<div>RESIDENT INVOICES</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );

    const residentLinks = screen.queryAllByRole('link', { name: /Báo cáo/ });
    expect(residentLinks.length).toBe(0);
  });

  it('discards deferred download response if user switches account while waiting', async () => {
    let resolveExport!: (val: reportsApi.ReportDownloadResult) => void;
    const pendingPromise = new Promise<reportsApi.ReportDownloadResult>((resolve) => {
      resolveExport = resolve;
    });
    vi.spyOn(reportsApi, 'exportFeeCollectionReport').mockReturnValue(pendingPromise);

    const { rerender } = render(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    const submitBtn = screen.getByRole('button', { name: 'Tải thu theo kỳ' });
    fireEvent.click(submitBtn);

    expect(screen.getAllByText('Đang chuẩn bị tệp…').length).toBeGreaterThan(0);

    // Switch account to accountant2
    mockState.user = {
      username: 'accountant2',
      fullName: 'Kế toán Hai',
      role: 'ACCOUNTANT',
      householdId: null,
      mustChangePassword: false,
    };

    rerender(
      <MemoryRouter>
        <ReportDownloadsPage />
      </MemoryRouter>,
    );

    // Resolve old export
    resolveExport({
      blob: new Blob(['fee']),
      filename: 'thu-theo-ky-2026-11.xlsx',
    });

    await waitFor(() => {
      // Must NOT show old success message
      expect(
        screen.queryByText('Đã gửi tệp tới trình duyệt: thu-theo-ky-2026-11.xlsx'),
      ).not.toBeInTheDocument();
    });
    expect(reportsApi.triggerBlobDownload).not.toHaveBeenCalled();
  });
});
