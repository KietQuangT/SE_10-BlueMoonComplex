import { cleanup, fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { afterEach, expect, it, vi } from 'vitest';
import { AppShell } from '../components/AppShell';
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: { role: 'ADMIN', fullName: 'Người kiểm thử' }, households: [], invoices: [], residencyChanges: [], mode: 'demo', forcedUiState: 'normal', logout: vi.fn(), setQaDrawerOpen: vi.fn() }) }));
afterEach(cleanup);

it('supports arrow keys and Escape in the mobile navigation and returns focus to its trigger', () => {
  render(<MemoryRouter initialEntries={['/admin/accounts']}><Routes><Route element={<AppShell />}><Route path="/admin/accounts" element={<h1>Tài khoản</h1>} /></Route></Routes></MemoryRouter>);
  const trigger = screen.getByRole('button', { name: 'Menu' });
  fireEvent.click(trigger);
  const menu = screen.getByRole('menu');
  const items = within(menu).getAllByRole('menuitem');
  expect(items[0]).toHaveFocus();
  fireEvent.keyDown(menu, { key: 'ArrowDown' });
  expect(items[1]).toHaveFocus();
  fireEvent.keyDown(menu, { key: 'End' });
  expect(items.at(-1)).toHaveFocus();
  fireEvent.keyDown(menu, { key: 'Escape' });
  expect(screen.queryByRole('menu')).not.toBeInTheDocument();
  expect(trigger).toHaveFocus();
});
