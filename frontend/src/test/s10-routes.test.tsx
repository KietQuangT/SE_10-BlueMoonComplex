import { render, screen, cleanup } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { Outlet } from 'react-router-dom';
import { App } from '../App';

const state = vi.hoisted(() => ({
  mode: 'api',
  role: 'RESIDENT',
  username: 'resident1',
  householdId: 10,
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

vi.mock('../screens/resident/InvoicesPage', () => ({
  InvoicesPage: () => <div>DEMO INVOICES PAGE</div>,
}));
vi.mock('../screens/resident/InvoiceDetailPage', () => ({
  InvoiceDetailPage: () => <div>DEMO INVOICE DETAIL PAGE</div>,
}));
vi.mock('../screens/resident/ApiInvoicesPage', () => ({
  ApiInvoicesPage: () => <div>LIVE S10 INVOICES PAGE</div>,
}));
vi.mock('../screens/resident/ApiInvoiceDetailPage', () => ({
  ApiInvoiceDetailPage: () => <div>LIVE S10 INVOICE DETAIL PAGE</div>,
}));

describe('S10 routes and mode guards', () => {
  afterEach(cleanup);

  it('renders ApiInvoicesPage when RESIDENT in API mode opens /resident/invoices', () => {
    state.mode = 'api';
    state.role = 'RESIDENT';
    window.history.pushState({}, '', '/resident/invoices');
    render(<App />);

    expect(screen.getByText('LIVE S10 INVOICES PAGE')).toBeInTheDocument();
    expect(screen.queryByText('DEMO INVOICES PAGE')).not.toBeInTheDocument();
  });

  it('renders Demo InvoicesPage when RESIDENT in Demo mode opens /resident/invoices', () => {
    state.mode = 'demo';
    state.role = 'RESIDENT';
    window.history.pushState({}, '', '/resident/invoices');
    render(<App />);

    expect(screen.getByText('DEMO INVOICES PAGE')).toBeInTheDocument();
    expect(screen.queryByText('LIVE S10 INVOICES PAGE')).not.toBeInTheDocument();
  });

  it('renders ApiInvoiceDetailPage when RESIDENT in API mode opens /resident/invoices/:id', () => {
    state.mode = 'api';
    state.role = 'RESIDENT';
    window.history.pushState({}, '', '/resident/invoices/42');
    render(<App />);

    expect(screen.getByText('LIVE S10 INVOICE DETAIL PAGE')).toBeInTheDocument();
    expect(screen.queryByText('DEMO INVOICE DETAIL PAGE')).not.toBeInTheDocument();
  });

  it('renders Demo InvoiceDetailPage when RESIDENT in Demo mode opens /resident/invoices/:id', () => {
    state.mode = 'demo';
    state.role = 'RESIDENT';
    window.history.pushState({}, '', '/resident/invoices/42');
    render(<App />);

    expect(screen.getByText('DEMO INVOICE DETAIL PAGE')).toBeInTheDocument();
    expect(screen.queryByText('LIVE S10 INVOICE DETAIL PAGE')).not.toBeInTheDocument();
  });

  it('blocks staff (ADMIN / ACCOUNTANT) from accessing resident invoice routes', () => {
    state.mode = 'api';
    state.role = 'ADMIN';
    window.history.pushState({}, '', '/resident/invoices');
    render(<App />);

    expect(screen.queryByText('LIVE S10 INVOICES PAGE')).not.toBeInTheDocument();
    expect(screen.getByText(/không có quyền/i)).toBeInTheDocument();
  });
});
