import { cleanup, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { LoginPage } from '../screens/common/LoginPage';
import { ReportPackagesPage } from '../screens/admin/ReportPackagesPage';

const state = vi.hoisted(() => ({
  user: null,
  authLoading: false,
  mode: 'demo',
  setMode: vi.fn(),
  backendReachable: true,
  sessionExpiredBanner: false,
  clearSessionExpiredBanner: vi.fn(),
  returnToPath: null,
  forcedUiState: 'normal',
  loginDemoAs: vi.fn(),
  setQaDrawerOpen: vi.fn(),
  households: [] as unknown[],
  dataLoadError: false,
  reloadHouseholds: vi.fn(),
}));

vi.mock('../state/AppStateContext', () => ({
  useAppState: () => state,
}));

describe('S12 UI repair: demo mode consistency and D13 alignment', () => {
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('LoginPage demo options exclude authority / agency role', () => {
    render(
      <MemoryRouter>
        <LoginPage />
      </MemoryRouter>
    );

    // Visible demo login only includes ADMIN, ACCOUNTANT, RESIDENT
    expect(screen.getByRole('button', { name: 'Ban quản trị' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Kế toán' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cư dân (căn A01)' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cư dân lần đầu (bắt đổi mật khẩu)' })).toBeInTheDocument();

    // Authority role button is removed per D13
    expect(screen.queryByRole('button', { name: /Cơ quan chức năng/i })).not.toBeInTheDocument();
  });

  it('ReportPackagesPage in Demo mode accurately explains backend real requirement without stale mock claims', () => {
    render(
      <MemoryRouter>
        <ReportPackagesPage />
      </MemoryRouter>
    );

    // Explains sensitive screen is available only in Backend thật mode
    expect(screen.getByText(/chỉ khả dụng ở chế độ Backend thật/i)).toBeInTheDocument();
    expect(screen.getByText(/chuyển sang API mode/i)).toBeInTheDocument();

    // Stale claims are absent
    expect(screen.queryByText(/backend chưa có API tương ứng/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/thao tác chỉ thay đổi trạng thái trong trình duyệt/i)).not.toBeInTheDocument();

    // No mock actions provided
    expect(screen.queryByRole('button', { name: 'Tạo gói dữ liệu' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Duyệt gói' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Tải XLSX' })).not.toBeInTheDocument();
  });
});
