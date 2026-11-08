import { render, screen, cleanup } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import { Outlet } from 'react-router-dom';
import { App } from '../App';
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ mode: 'demo', user: { role: 'ACCOUNTANT', username: 'user' }, authLoading: false }) }));
vi.mock('../components/AppShell', () => ({ AppShell: () => <Outlet /> }));
vi.mock('../components/QaDrawer', () => ({ QaDrawer: () => null }));
vi.mock('../screens/s08/ApiBillingPage', () => ({ ApiBillingPage: () => <div>LIVE BILLING</div> }));
vi.mock('../screens/s08/ApiVoluntaryPage', () => ({ ApiVoluntaryPage: () => <div>LIVE VOLUNTARY</div> }));
afterEach(cleanup);
it.each(['/accountant/billing-periods', '/accountant/voluntary'])('does not mount live screen in demo at %s', (path) => {
  window.history.pushState({}, '', path); render(<App />);
  expect(screen.queryByText(/LIVE/)).not.toBeInTheDocument();
  expect(screen.getByText(/Demo/)).toBeInTheDocument();
});
