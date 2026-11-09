import { render, screen, cleanup } from '@testing-library/react';
import { afterEach, expect, it, vi } from 'vitest';
import { Outlet } from 'react-router-dom';
import { App } from '../App';
const state = vi.hoisted(() => ({ mode: 'demo', role: 'ADMIN' }));
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ mode: state.mode, user: { role: state.role, username: 'user' }, authLoading: false }) }));
vi.mock('../components/AppShell', () => ({ AppShell: () => <Outlet /> }));
vi.mock('../components/QaDrawer', () => ({ QaDrawer: () => null }));
vi.mock('../screens/accountant/CollectionsPage', () => ({ CollectionsPage: ({ readOnly }: { readOnly?: boolean }) => <div>{readOnly ? 'DEMO READ ONLY' : 'DEMO WRITABLE'}</div> }));
vi.mock('../screens/s09/ApiCollectionsPage', () => ({ ApiCollectionsPage: () => <div>LIVE COLLECTIONS</div> }));
afterEach(cleanup);
it('opens the real accountant page only in API mode', () => {
  state.role = 'ACCOUNTANT'; state.mode = 'api'; window.history.pushState({}, '', '/accountant/collections'); render(<App />);
  expect(screen.getByText('LIVE COLLECTIONS')).toBeInTheDocument();
});
it('opens read-only demo for administrators', () => {
  state.role = 'ADMIN'; state.mode = 'demo'; window.history.pushState({}, '', '/admin/collections'); render(<App />);
  expect(screen.getByText('DEMO READ ONLY')).toBeInTheDocument();
});
it('prevents residents from opening either staff route', () => {
  state.role = 'RESIDENT'; state.mode = 'api'; window.history.pushState({}, '', '/admin/collections'); render(<App />);
  expect(screen.queryByText('LIVE COLLECTIONS')).not.toBeInTheDocument();
  expect(screen.getByText(/không có quyền/i)).toBeInTheDocument();
});
