import { cleanup, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { Outlet } from 'react-router-dom';
import { App } from '../App';

const state = vi.hoisted(() => ({
  mode: 'api',
  role: 'ADMIN',
  username: 'admin1',
  householdId: null as number | null,
  mustChangePassword: false,
}));

vi.mock('../state/AppStateContext', () => ({
  useAppState: () => ({
    mode: state.mode,
    user: state.role
      ? {
          role: state.role,
          username: state.username,
          householdId: state.householdId,
          mustChangePassword: state.mustChangePassword,
        }
      : null,
    authLoading: false,
    handleCallFailure: vi.fn(),
  }),
}));

vi.mock('../components/AppShell', () => ({ AppShell: () => <Outlet /> }));
vi.mock('../components/QaDrawer', () => ({ QaDrawer: () => null }));

vi.mock('../screens/admin/ResidencyReportPage', () => ({
  ResidencyReportPage: () => <div>LEGACY DEMO RESIDENCY PAGE</div>,
}));

vi.mock('../screens/accountant/CollectionRatePage', () => ({
  CollectionRatePage: () => <div>LEGACY DEMO COLLECTION RATE PAGE</div>,
}));

vi.mock('../screens/admin/ReportPackagesPage', () => ({
  ReportPackagesPage: () => <div>S12 REPORT PACKAGES PAGE</div>,
}));

vi.mock('../screens/authority/AuthorityReportsPage', () => ({
  AuthorityReportsPage: () => <div>S12 AUTHORITY REPORTS PAGE</div>,
}));

vi.mock('../screens/reporting/ReportDownloadsPage', () => ({
  ReportDownloadsPage: () => <div>S11-UI REPORT DOWNLOADS PAGE</div>,
}));

describe('S11-UI Routes and Access Guards', () => {
  afterEach(cleanup);

  it('renders ReportDownloadsPage when ADMIN visits /reports in API mode', () => {
    state.mode = 'api';
    state.role = 'ADMIN';
    window.history.pushState({}, '', '/reports');
    render(<App />);

    expect(screen.getByText('S11-UI REPORT DOWNLOADS PAGE')).toBeInTheDocument();
  });

  it('renders ReportDownloadsPage when ACCOUNTANT visits /reports in API mode', () => {
    state.mode = 'api';
    state.role = 'ACCOUNTANT';
    window.history.pushState({}, '', '/reports');
    render(<App />);

    expect(screen.getByText('S11-UI REPORT DOWNLOADS PAGE')).toBeInTheDocument();
  });

  it('redirects /admin/reports/residency to /reports in API mode for ADMIN', () => {
    state.mode = 'api';
    state.role = 'ADMIN';
    window.history.pushState({}, '', '/admin/reports/residency');
    render(<App />);

    expect(screen.getByText('S11-UI REPORT DOWNLOADS PAGE')).toBeInTheDocument();
    expect(window.location.pathname).toBe('/reports');
  });

  it('redirects /accountant/collection-rate to /reports in API mode for ACCOUNTANT', () => {
    state.mode = 'api';
    state.role = 'ACCOUNTANT';
    window.history.pushState({}, '', '/accountant/collection-rate');
    render(<App />);

    expect(screen.getByText('S11-UI REPORT DOWNLOADS PAGE')).toBeInTheDocument();
    expect(window.location.pathname).toBe('/reports');
  });

  it('shows 403 Forbidden when RESIDENT attempts to access /reports', () => {
    state.mode = 'api';
    state.role = 'RESIDENT';
    window.history.pushState({}, '', '/reports');
    render(<App />);

    expect(screen.queryByText('S11-UI REPORT DOWNLOADS PAGE')).not.toBeInTheDocument();
    expect(
      screen.getByRole('heading', { name: 'Bạn không có quyền truy cập trang này.' }),
    ).toBeInTheDocument();
  });

  it('preserves legacy Demo screens in Demo mode', () => {
    state.mode = 'demo';
    state.role = 'ADMIN';
    window.history.pushState({}, '', '/admin/reports/residency');
    render(<App />);

    expect(screen.getByText('LEGACY DEMO RESIDENCY PAGE')).toBeInTheDocument();
  });

  it('preserves S12 packages and authority report screens untouched', () => {
    state.mode = 'api';
    state.role = 'ADMIN';
    window.history.pushState({}, '', '/admin/reports/packages');
    render(<App />);

    expect(screen.getByText('S12 REPORT PACKAGES PAGE')).toBeInTheDocument();
  });
});
