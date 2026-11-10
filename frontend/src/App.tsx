import React from 'react';
import { BrowserRouter, Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { QaDrawer } from './components/QaDrawer';
import { ApiBillingPage } from './screens/s08/ApiBillingPage';
import { ApiVoluntaryPage } from './screens/s08/ApiVoluntaryPage';
import { defaultHomeByRole } from './lib/format';
import { useAppState } from './state/AppStateContext';
import type { RoleCode } from './types/domain';

// Common Screens
import { LoginPage } from './screens/common/LoginPage';
import { ProfilePage } from './screens/common/ProfilePage';
import { ErrorPage, StandaloneErrorPage } from './screens/common/ErrorPage';

// Admin Screens
import { AccountsPage } from './screens/admin/AccountsPage';
import { CreateAccountPage } from './screens/admin/CreateAccountPage';
import { HouseholdsPage } from './screens/admin/HouseholdsPage';
import { HouseholdDetailPage } from './screens/admin/HouseholdDetailPage';
import { HouseholdFormPage } from './screens/admin/HouseholdFormPage';
import { MoveOutPage } from './screens/admin/MoveOutPage';
import { AbsenceApprovalsPage } from './screens/admin/AbsenceApprovalsPage';
import { VoluntaryAdminPage } from './screens/admin/VoluntaryAdminPage';
import { BillingPeriodsPage } from './screens/admin/BillingPeriodsPage';
import { ResidencyReportPage } from './screens/admin/ResidencyReportPage';
import { ReportPackagesPage } from './screens/admin/ReportPackagesPage';

// Accountant Screens
import { CollectionsPage } from './screens/accountant/CollectionsPage';
import { ApiCollectionsPage } from './screens/s09/ApiCollectionsPage';
import { FeeRatesPage } from './screens/accountant/FeeRatesPage';
import { CollectionRatePage } from './screens/accountant/CollectionRatePage';

// Resident Screens
import { InvoicesPage } from './screens/resident/InvoicesPage';
import { InvoiceDetailPage } from './screens/resident/InvoiceDetailPage';
import { ApiInvoicesPage } from './screens/resident/ApiInvoicesPage';
import { ApiInvoiceDetailPage } from './screens/resident/ApiInvoiceDetailPage';
import { MyHouseholdPage } from './screens/resident/MyHouseholdPage';
import { AbsencePage } from './screens/resident/AbsencePage';
import { ResidentVoluntaryPage } from './screens/resident/ResidentVoluntaryPage';

// Authority Screen
import { AuthorityReportsPage } from './screens/authority/AuthorityReportsPage';

function RootIndexRedirect(): React.ReactElement {
  const { user, authLoading } = useAppState();

  if (authLoading) {
    return (
      <div className="wayfinding-page p-8 text-center text-slate">
        <span className="station-strip bg-teal-brand mb-4 mx-auto w-16"></span>
        <p>Đang tải thông tin đăng nhập...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.mustChangePassword) {
    return <Navigate to="/profile?tab=password" replace />;
  }

  return <Navigate to={defaultHomeByRole(user.role)} replace />;
}

function RequireAuth(): React.ReactElement {
  const { user, authLoading } = useAppState();
  const location = useLocation();

  if (authLoading) {
    return (
      <div className="wayfinding-page p-8 text-center text-slate">
        <span className="station-strip bg-teal-brand mb-4 mx-auto w-16"></span>
        <p>Đang xác thực phiên làm việc...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  if (user.mustChangePassword && location.pathname !== '/profile') {
    return <Navigate to="/profile?tab=password" replace />;
  }

  return <AppShell />;
}

function RequireRole({ roles }: { roles: RoleCode[] }): React.ReactElement {
  const { user } = useAppState();

  if (!user || !roles.includes(user.role)) {
    return <ErrorPage code={403} />;
  }

  return <Outlet />;
}

export function App(): React.ReactElement {
  const { mode, user } = useAppState();
  const residentRouteKey = `${mode}:${user?.username ?? ''}:${user?.householdId ?? ''}`;
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Login Route */}
        <Route path="/login" element={<LoginPage />} />

        {/* Standalone Error Routes */}
        <Route path="/error/:code" element={<StandaloneErrorPage />} />

        {/* Authenticated Workspace wrapped in AppShell */}
        <Route element={<RequireAuth />}>
          <Route path="/" element={<RootIndexRedirect />} />
          <Route path="/profile" element={<ProfilePage />} />

          {/* ADMIN Routes */}
          <Route element={<RequireRole roles={['ADMIN']} />}>
            <Route path="/admin/accounts" element={<AccountsPage />} />
            <Route path="/admin/accounts/new" element={<CreateAccountPage />} />
            <Route path="/admin/households" element={<HouseholdsPage />} />
            <Route path="/admin/households/new" element={<HouseholdFormPage mode="household" />} />
            <Route path="/admin/households/:id" element={<HouseholdDetailPage />} />
            <Route path="/admin/households/:id/edit" element={<HouseholdFormPage mode="household" />} />
            <Route path="/admin/households/:id/residents/new" element={<HouseholdFormPage mode="resident" />} />
            <Route path="/admin/move-outs" element={<MoveOutPage />} />
            <Route path="/admin/absence-approvals" element={<AbsenceApprovalsPage />} />
            <Route path="/admin/voluntary" element={mode === 'api' ? <ApiVoluntaryPage /> : <VoluntaryAdminPage />} />
            <Route path="/admin/billing-periods" element={mode === 'api' ? <ApiBillingPage /> : <BillingPeriodsPage />} />
            <Route path="/admin/collections" element={mode === 'api' ? <ApiCollectionsPage key={user?.username} /> : <CollectionsPage readOnly />} />
            <Route path="/admin/reports/residency" element={<ResidencyReportPage />} />
            <Route path="/admin/reports/packages" element={<ReportPackagesPage />} />
          </Route>

          <Route element={<RequireRole roles={['ACCOUNTANT', 'ADMIN']} />}><Route path="/accountant/fee-rates" element={<FeeRatesPage />} /></Route>

          {/* ACCOUNTANT Routes */}
          <Route element={<RequireRole roles={['ACCOUNTANT']} />}>
            <Route path="/accountant/billing-periods" element={mode === 'api' ? <ApiBillingPage /> : <div className="wayfinding-page">Kỳ thu chưa khả dụng trong chế độ Demo.</div>} />
            <Route path="/accountant/voluntary" element={mode === 'api' ? <ApiVoluntaryPage /> : <div className="wayfinding-page">Đóng góp chưa khả dụng trong chế độ Demo.</div>} />
            <Route path="/accountant/collections" element={mode === 'api' ? <ApiCollectionsPage key={user?.username} /> : <CollectionsPage />} />
            <Route path="/accountant/collection-rate" element={<CollectionRatePage />} />
          </Route>

          {/* RESIDENT Routes */}
          <Route element={<RequireRole roles={['RESIDENT']} />}>
            <Route
              path="/resident/invoices"
              element={mode === 'api' ? <ApiInvoicesPage key={residentRouteKey} /> : <InvoicesPage />}
            />
            <Route
              path="/resident/invoices/:id"
              element={mode === 'api' ? <ApiInvoiceDetailPage key={residentRouteKey} /> : <InvoiceDetailPage />}
            />
            <Route path="/resident/household" element={<MyHouseholdPage />} />
            <Route path="/resident/absence" element={<AbsencePage />} />
            <Route path="/resident/voluntary" element={mode === 'api' ? <ApiVoluntaryPage /> : <ResidentVoluntaryPage />} />
          </Route>

          {/* AUTHORITY Routes */}
          <Route element={<RequireRole roles={['AUTHORITY']} />}>
            <Route path="/authority/reports" element={<AuthorityReportsPage />} />
          </Route>
        </Route>

        {/* Catch-all 404 */}
        <Route path="*" element={<StandaloneErrorPage />} />
      </Routes>

      {/* Global Developer & QA Simulation Drawer */}
      <QaDrawer />
    </BrowserRouter>
  );
}
