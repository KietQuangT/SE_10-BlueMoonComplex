import React, { useEffect, useRef, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAppState } from '../state/AppStateContext';
import { formatAreaM2, roleDisplayName } from '../lib/format';
import type { RoleCode } from '../types/domain';
import { Icon, type IconName } from './Icon';
import { ThemeToggle } from './ThemeToggle';
import { ErrorPage } from '../screens/common/ErrorPage';
import { useStackedTables } from '../lib/stackTables';

interface NavItem {
  to: string;
  label: string;
  icon: IconName;
  end?: boolean;
  count?: number;
}

interface NavGroup {
  heading: string;
  items: NavItem[];
}

const SHOW_DEV_TOOLS = import.meta.env.DEV || import.meta.env.VITE_ENABLE_DEMO === 'true';

function useNavGroups(role: RoleCode): NavGroup[] {
  const { residencyChanges, invoices, user } = useAppState();
  const pendingAbsence = residencyChanges.filter(
    (c) => c.status === 'PENDING' && c.changeType === 'TAM_VANG',
  ).length;
  const unpaidMine = invoices.filter(
    (i) => i.householdId === user?.householdId && i.status === 'CHUA_DONG',
  ).length;
  const pendingMine = residencyChanges.filter(
    (c) => c.householdId === user?.householdId && c.status === 'PENDING',
  ).length;

  switch (role) {
    case 'ADMIN':
      return [
        {
          heading: 'Tài khoản',
          items: [
            { to: '/admin/accounts', label: 'Danh sách tài khoản', icon: 'user-cog', end: true },
            { to: '/admin/accounts/new', label: 'Tạo tài khoản', icon: 'plus' },
          ],
        },
        {
          heading: 'Hộ và cư trú',
          items: [
            { to: '/admin/households', label: 'Hộ gia đình', icon: 'building' },
            { to: '/admin/move-outs', label: 'Chuyển đi và lưu trữ', icon: 'archive' },
            {
              to: '/admin/absence-approvals',
              label: 'Duyệt tạm vắng',
              icon: 'calendar',
              count: pendingAbsence,
            },
          ],
        },
        {
          heading: 'Phí',
          items: [
            { to: '/admin/voluntary', label: 'Đóng góp tự nguyện', icon: 'heart' },
            { to: '/admin/billing-periods', label: 'Tạo kỳ thu', icon: 'receipt' },
          ],
        },
        {
          heading: 'Báo cáo',
          items: [
            { to: '/admin/reports/residency', label: 'Báo cáo lưu trú', icon: 'file-spreadsheet' },
            { to: '/admin/reports/packages', label: 'Gói cho cơ quan', icon: 'package-check' },
          ],
        },
      ];
    case 'ACCOUNTANT':
      return [
        {
          heading: 'Phí',
          items: [
            { to: '/accountant/collections', label: 'Thu tiền mặt', icon: 'wallet' },
            { to: '/accountant/fee-rates', label: 'Đơn giá phí', icon: 'coins' },
          ],
        },
        {
          heading: 'Báo cáo',
          items: [
            {
              to: '/accountant/collection-rate',
              label: 'Báo cáo tỷ lệ thu',
              icon: 'file-spreadsheet',
            },
          ],
        },
      ];
    case 'RESIDENT':
      return [
        {
          heading: 'Hộ của tôi',
          items: [
            {
              to: '/resident/invoices',
              label: 'Hóa đơn và lịch sử',
              icon: 'receipt',
              count: unpaidMine,
            },
            { to: '/resident/household', label: 'Hộ gia đình của tôi', icon: 'users' },
            {
              to: '/resident/absence',
              label: 'Khai báo tạm vắng',
              icon: 'calendar',
              count: pendingMine,
            },
            { to: '/resident/voluntary', label: 'Đóng góp tự nguyện', icon: 'heart' },
          ],
        },
      ];
    case 'AUTHORITY':
      return [
        {
          heading: 'Báo cáo',
          items: [{ to: '/authority/reports', label: 'Báo cáo đã duyệt', icon: 'shield' }],
        },
      ];
  }
}

const RESIDENT_TABS: NavItem[] = [
  { to: '/resident/invoices', label: 'Hóa đơn', icon: 'receipt' },
  { to: '/resident/household', label: 'Hộ của tôi', icon: 'users' },
  { to: '/resident/absence', label: 'Khai báo', icon: 'calendar' },
];

export function AppShell(): React.ReactElement {
  const { user, households, logout, mode, forcedUiState, setQaDrawerOpen } = useAppState();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!menuOpen) return;
    const onDoc = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) setMenuOpen(false);
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMenuOpen(false);
    };
    document.addEventListener('mousedown', onDoc);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onDoc);
      document.removeEventListener('keydown', onKey);
    };
  }, [menuOpen]);

  const groups = useNavGroups(user?.role ?? 'RESIDENT');
  useStackedTables(Boolean(user));

  if (!user) return <Outlet />;

  const household = households.find((h) => h.id === user.householdId);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  let content: React.ReactNode = <Outlet />;
  if (forcedUiState === 'unauthorized-401') content = <ErrorPage code={401} />;
  if (forcedUiState === 'forbidden-403') content = <ErrorPage code={403} />;

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">
        Bỏ qua menu, tới nội dung chính
      </a>
      <aside className="app-rail" aria-label="Điều hướng chính">
        <div>
          <NavLink to="/" className="rail-brand">
            <span className="line-mark" aria-hidden="true" />
            <span className="rail-brand-title">BlueMoon</span>
          </NavLink>
          <nav className="rail-nav">
            {groups.map((g) => (
              <div className="rail-group" key={g.heading} role="group" aria-label={g.heading}>
                <p className="rail-group-heading" aria-hidden="true">
                  {g.heading}
                </p>
                {g.items.map((item) => (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    end={item.end}
                    className={({ isActive }) => `rail-link${isActive ? ' active' : ''}`}
                  >
                    <span className="rail-link-main">
                      <Icon name={item.icon} />
                      <span>{item.label}</span>
                    </span>
                    {item.count ? (
                      <span className="rail-count" aria-label={`${item.count} mục cần xử lý`}>
                        {String(item.count).padStart(2, '0')}
                      </span>
                    ) : null}
                  </NavLink>
                ))}
              </div>
            ))}
          </nav>
        </div>
        <div className="rail-footer">
          <span className="rail-user-name">{user.fullName}</span>
          <span className="rail-role-chip">
            {roleDisplayName(user.role)}
            {household ? ` · ${household.householdCode}` : ''}
          </span>
        </div>
      </aside>

      <div className="app-workspace">
        <header className="app-topbar">
          <div className="topbar-scope">
            <NavLink to="/" className="mobile-brand" aria-label="BlueMoon — trang đầu">
              <span className="line-mark" aria-hidden="true" />
              BlueMoon
            </NavLink>
            {household ? (
              <>
                <span className="apt-sign-plate" aria-label={`Căn hộ ${household.apartmentCode}`}>
                  {household.apartmentCode}
                </span>
                <span className="topbar-meta hide-mobile">
                  {household.address} · {formatAreaM2(household.areaM2)}
                </span>
              </>
            ) : (
              <span className="role-plate">{roleDisplayName(user.role)}</span>
            )}
            {mode === 'demo' ? (
              <span className="badge badge-assumed">Chế độ Demo</span>
            ) : SHOW_DEV_TOOLS ? (
              <span className="badge badge-paid hide-mobile">Kết nối backend</span>
            ) : null}
          </div>
          <div className="topbar-actions">
            <ThemeToggle />
            <span className="topbar-meta hide-mobile">{user.fullName}</span>
            <NavLink to="/profile" className="btn btn-ghost btn-sm hide-mobile">
              Hồ sơ và đổi mật khẩu
            </NavLink>
            {SHOW_DEV_TOOLS ? (
              <button
                type="button"
                className="btn btn-secondary btn-sm hide-mobile"
                onClick={() => setQaDrawerOpen(true)}
              >
                <Icon name="sliders" size={16} />
                Kiểm thử UI
              </button>
            ) : null}
            <button type="button" className="btn btn-secondary btn-sm hide-mobile" onClick={handleLogout}>
              <Icon name="log-out" size={16} />
              Đăng xuất
            </button>
            <div className="mobile-user-menu-wrap show-mobile" ref={menuRef}>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                aria-haspopup="true"
                aria-expanded={menuOpen}
                onClick={() => setMenuOpen((v) => !v)}
              >
                <Icon name="user-cog" size={18} />
                Menu
              </button>
              {menuOpen ? (
                <div className="mobile-user-popover" role="menu">
                  <span className="popover-user">
                    {user.fullName} · {roleDisplayName(user.role)}
                  </span>
                  {groups
                    .flatMap((g) => g.items)
                    .map((item) => (
                      <NavLink key={item.to} to={item.to} end={item.end} className="popover-link" role="menuitem">
                        <Icon name={item.icon} size={16} />
                        {item.label}
                      </NavLink>
                    ))}
                  <NavLink to="/profile" className="popover-link" role="menuitem">
                    <Icon name="lock" size={16} />
                    Hồ sơ và đổi mật khẩu
                  </NavLink>
                  {SHOW_DEV_TOOLS ? (
                    <button
                      type="button"
                      className="popover-link"
                      role="menuitem"
                      onClick={() => setQaDrawerOpen(true)}
                    >
                      <Icon name="sliders" size={16} />
                      Kiểm thử UI
                    </button>
                  ) : null}
                  <button type="button" className="popover-link" role="menuitem" onClick={handleLogout}>
                    <Icon name="log-out" size={16} />
                    Đăng xuất
                  </button>
                </div>
              ) : null}
            </div>
          </div>
        </header>

        <main id="main-content" className="app-main" tabIndex={-1}>
          {content}
        </main>
      </div>

      {user.role === 'RESIDENT' ? (
        <nav className="mobile-bottom-nav" aria-label="Điều hướng cư dân">
          {RESIDENT_TABS.map((tab) => (
            <NavLink
              key={tab.to}
              to={tab.to}
              className={({ isActive }) => `mobile-nav-item${isActive ? ' active' : ''}`}
            >
              <Icon name={tab.icon} size={20} />
              {tab.label}
            </NavLink>
          ))}
        </nav>
      ) : null}
    </div>
  );
}
