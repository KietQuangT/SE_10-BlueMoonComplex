import React, { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import { roleDisplayName } from '../../lib/format';
import { useDebouncedValue, usePaged } from '../../lib/hooks';
import { ScreenHeader } from '../../components/ScreenHeader';
import { StatusBadge } from '../../components/Badge';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { PaginationBar } from '../../components/PaginationBar';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { Icon } from '../../components/Icon';
import type { AccountRecord } from '../../types/domain';

/** M-10 — Danh sách tài khoản (S02/S04: danh sách chưa có API; Vô hiệu hóa, Mở khóa mô phỏng). */
export function AccountsPage(): React.ReactElement {
  const { user, accounts, disableAccountDemo, unlockAccountDemo, forcedUiState, setForcedUiState } = useAppState();
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [query, setQuery] = useState('');
  const debounced = useDebouncedValue(query);
  const [target, setTarget] = useState<{ account: AccountRecord; action: 'disable' | 'unlock' } | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const filtered = useMemo(() => {
    if (forcedUiState === 'empty') return [];
    const q = debounced.trim().toLowerCase();
    return accounts.filter(
      (a) =>
        (roleFilter === 'ALL' || a.role === roleFilter) &&
        (statusFilter === 'ALL' ||
          (statusFilter === 'LOCKED' ? a.locked : a.status === statusFilter)) &&
        (!q || a.fullName.toLowerCase().includes(q) || a.email.toLowerCase().includes(q)),
    );
  }, [accounts, roleFilter, statusFilter, debounced, forcedUiState]);
  const paged = usePaged(filtered);

  const activeAdmins = accounts.filter((a) => a.role === 'ADMIN' && a.status === 'ACTIVE').length;
  const cannotDisable = (a: AccountRecord): string | null => {
    if (a.status === 'INACTIVE') return 'Đã vô hiệu';
    if (a.username === user?.username) return 'Không thể tự vô hiệu hóa chính mình';
    if (a.role === 'ADMIN' && activeAdmins <= 1) return 'Không thể vô hiệu hóa Ban quản trị cuối cùng';
    return null;
  };

  const confirm = () => {
    if (!target) return;
    if (target.action === 'disable') {
      disableAccountDemo(target.account.id);
      setNotice(`Đã vô hiệu hóa ${target.account.fullName}. Mọi phiên đăng nhập của người này bị hủy.`);
    } else {
      unlockAccountDemo(target.account.id);
      setNotice(`Đã mở khóa ${target.account.fullName}. Người này có thể đăng nhập lại.`);
    }
    setTarget(null);
  };

  const header = (
    <ScreenHeader
      screenCode="M-10"
      title="Danh sách tài khoản"
      description="Cấp, vô hiệu hóa và mở khóa tài khoản. Tài khoản Ban quản trị không bị khóa tạm thời."
      contract="db-ready"
      tierLabel="Mức B"
      actions={
        <Link to="/admin/accounts/new" className="btn btn-primary">
          <Icon name="plus" size={16} />
          Tạo tài khoản
        </Link>
      }
    />
  );

  if (forcedUiState === 'loading') return <>{header}<SkeletonRows rows={6} /></>;
  if (forcedUiState === 'error') return <>{header}<ErrorRetryPanel onRetry={() => setForcedUiState('normal')} /></>;

  return (
    <>
      {header}
      {notice ? (
        <div className="banner banner-success" role="status">
          <div className="banner-content">
            <Icon name="check" />
            <span>{notice}</span>
          </div>
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => setNotice(null)}>
            Đóng
          </button>
        </div>
      ) : null}

      <section className="surface-box">
        <div className="box-toolbar">
          <div className="filter-row">
            <label className="inline-field">
              <span className="sr-only">Tìm theo họ tên hoặc email</span>
              <Icon name="search" size={16} />
              <input
                className="input"
                placeholder="Tìm theo họ tên hoặc email"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </label>
            <label className="inline-field">
              <span className="field-hint">Vai trò</span>
              <select className="select" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
                <option value="ALL">Tất cả</option>
                <option value="ADMIN">Ban quản trị</option>
                <option value="ACCOUNTANT">Kế toán</option>
                <option value="RESIDENT">Cư dân</option>
              </select>
            </label>
            <label className="inline-field">
              <span className="field-hint">Trạng thái</span>
              <select className="select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                <option value="ALL">Tất cả</option>
                <option value="ACTIVE">Hoạt động</option>
                <option value="INACTIVE">Vô hiệu</option>
                <option value="LOCKED">Đang khóa tạm thời</option>
              </select>
            </label>
          </div>
        </div>
        {filtered.length === 0 ? (
          <div className="state-panel">
            <h3>{accounts.length === 0 || forcedUiState === 'empty' ? 'Chưa có tài khoản' : 'Không có kết quả'}</h3>
            <p>
              {forcedUiState === 'empty'
                ? 'Hệ thống chưa có tài khoản nào ngoài tài khoản khởi tạo. Hãy cấp tài khoản đầu tiên.'
                : 'Không có tài khoản khớp bộ lọc. Thử bỏ bớt điều kiện tìm kiếm.'}
            </p>
            <Link to="/admin/accounts/new" className="btn btn-primary">
              Tạo tài khoản
            </Link>
          </div>
        ) : (
          <>
            <div className="table-scroll">
              <table className="ledger-table">
                <thead>
                  <tr>
                    <th scope="col">Họ tên</th>
                    <th scope="col">Tên đăng nhập</th>
                    <th scope="col">Email</th>
                    <th scope="col">Vai trò</th>
                    <th scope="col">Hộ</th>
                    <th scope="col">Trạng thái</th>
                    <th scope="col" className="align-right">
                      Thao tác
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {paged.pageItems.map((a) => {
                    const blocked = cannotDisable(a);
                    return (
                      <tr key={a.id}>
                        <td>
                          <strong>{a.fullName}</strong>
                        </td>
                        <td className="tabular">{a.username}</td>
                        <td>{a.email || '—'}</td>
                        <td>{roleDisplayName(a.role)}</td>
                        <td className="tabular">{a.householdCode ?? '—'}</td>
                        <td>
                          <span className="badge-stack">
                            {a.status === 'ACTIVE' ? (
                              <StatusBadge tone="paid" label="Hoạt động" />
                            ) : (
                              <StatusBadge tone="neutral" label="Vô hiệu" />
                            )}
                            {a.locked && a.role !== 'ADMIN' ? (
                              <StatusBadge tone="danger" label="Đang khóa tạm thời" />
                            ) : null}
                          </span>
                        </td>
                        <td className="align-right">
                          <span className="row-actions">
                            {a.locked && a.role !== 'ADMIN' ? (
                              <button
                                type="button"
                                className="btn btn-secondary btn-sm"
                                onClick={() => setTarget({ account: a, action: 'unlock' })}
                              >
                                <Icon name="unlock" size={14} />
                                Mở khóa
                              </button>
                            ) : null}
                            {a.status === 'INACTIVE' ? (
                              <button type="button" className="btn btn-ghost btn-sm" disabled title="Chờ chốt">
                                Kích hoạt lại · chờ chốt
                              </button>
                            ) : (
                              <button
                                type="button"
                                className="btn btn-ghost btn-sm danger-text"
                                disabled={Boolean(blocked)}
                                title={blocked ?? undefined}
                                onClick={() => setTarget({ account: a, action: 'disable' })}
                              >
                                Vô hiệu hóa
                              </button>
                            )}
                            <button type="button" className="btn btn-ghost btn-sm" disabled title="Chờ chốt">
                              Cấp lại mật khẩu tạm · chờ chốt
                            </button>
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <PaginationBar
              page={paged.page}
              pageSize={paged.pageSize}
              totalItems={paged.totalItems}
              onPageChange={paged.setPage}
              onPageSizeChange={paged.setPageSize}
            />
          </>
        )}
      </section>
      <p className="field-hint">
        Ghi nhận chuyển đi (M-15) và vô hiệu hóa tài khoản là hai thao tác tách rời: schema chưa liên
        kết nhân khẩu với tài khoản.
      </p>

      <ConfirmDialog
        open={Boolean(target)}
        title={
          target?.action === 'disable'
            ? `Vô hiệu hóa ${target.account.fullName}?`
            : `Mở khóa ${target?.account.fullName ?? ''}?`
        }
        consequence={
          target?.action === 'disable'
            ? 'Người này sẽ bị đăng xuất ngay và không đăng nhập lại được cho tới khi được kích hoạt lại.'
            : 'Tài khoản đang bị khóa do nhập sai mật khẩu nhiều lần. Mở khóa cho phép đăng nhập lại ngay.'
        }
        confirmLabel={target?.action === 'disable' ? 'Vô hiệu hóa' : 'Mở khóa'}
        confirmTone={target?.action === 'disable' ? 'danger' : 'primary'}
        onConfirm={confirm}
        onCancel={() => setTarget(null)}
      />
    </>
  );
}
