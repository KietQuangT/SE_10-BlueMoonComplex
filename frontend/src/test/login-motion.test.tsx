import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, expect, it, vi } from 'vitest';
import { LoginPage } from '../screens/common/LoginPage';
vi.mock('../state/AppStateContext', () => ({ useAppState: () => ({ user: null, authLoading: false, mode: 'api', backendReachable: true, forcedUiState: 'normal' }) }));
afterEach(cleanup);
it('lets the visitor pause and resume the decorative login background', () => {
  render(<MemoryRouter><LoginPage /></MemoryRouter>);
  const pause = screen.getByRole('button', { name: 'Dừng nền động' });
  fireEvent.click(pause);
  expect(screen.getByRole('button', { name: 'Bật nền động' })).toHaveAttribute('aria-pressed', 'true');
  expect(document.querySelector('.login-page')).toHaveAttribute('data-motion', 'paused');
  fireEvent.click(screen.getByRole('button', { name: 'Bật nền động' }));
  expect(document.querySelector('.login-page')).toHaveAttribute('data-motion', 'running');
});

it('renders the BlueMoon Complex split branding panel, form placeholders, password visibility toggle, and theme toggle', () => {
  render(<MemoryRouter><LoginPage /></MemoryRouter>);
  expect(screen.getByText('Phiên bảo mật')).toBeInTheDocument();
  expect(screen.getByText(/Hệ thống quản lý cư trú, hộ gia đình, hóa đơn/i)).toBeInTheDocument();
  expect(screen.getByPlaceholderText('Nhập tên đăng nhập')).toBeInTheDocument();
  const passwordInput = screen.getByPlaceholderText('Nhập mật khẩu') as HTMLInputElement;
  expect(passwordInput.type).toBe('password');
  const toggleBtn = screen.getByRole('button', { name: 'Hiện mật khẩu' });
  fireEvent.click(toggleBtn);
  expect(passwordInput.type).toBe('text');
  fireEvent.click(screen.getByRole('button', { name: 'Ẩn mật khẩu' }));
  expect(passwordInput.type).toBe('password');
  expect(document.querySelector('.theme-toggle')).toBeInTheDocument();
});
