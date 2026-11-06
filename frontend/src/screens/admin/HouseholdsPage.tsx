import React, { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import { formatAreaM2 } from '../../lib/format';
import { useDebouncedValue, usePaged } from '../../lib/hooks';
import { ScreenHeader } from '../../components/ScreenHeader';
import { StatusBadge } from '../../components/Badge';
import { PaginationBar } from '../../components/PaginationBar';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { Icon } from '../../components/Icon';

/** M-12 — Danh sách hộ (tìm theo mã căn/mã hộ, phân trang phía server). */
export function HouseholdsPage(): React.ReactElement {
  const { households, forcedUiState, setForcedUiState } = useAppState();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('ALL');
  const debounced = useDebouncedValue(query);

  const filtered = useMemo(() => {
    if (forcedUiState === 'empty') return [];
    const q = debounced.trim().toUpperCase();
    return households.filter(
      (h) =>
        (status === 'ALL' || h.status === status) &&
        (!q || h.apartmentCode.toUpperCase().includes(q) || h.householdCode.toUpperCase().includes(q)),
    );
  }, [households, debounced, status, forcedUiState]);
  const paged = usePaged(filtered);

  const header = (
    <ScreenHeader
      screenCode="M-12"
      title="Hộ gia đình"
      description="Tra cứu theo mã căn hộ hoặc mã hộ. Danh sách tìm và phân trang trên máy chủ (≤ 3 giây với 10.000 hồ sơ)."
      contract="db-ready"
      tierLabel="Mức B"
      actions={
        <Link to="/admin/households/new" className="btn btn-primary">
          <Icon name="plus" size={16} />
          Thêm hộ
        </Link>
      }
    />
  );

  if (forcedUiState === 'loading') return <>{header}<SkeletonRows rows={6} /></>;
  if (forcedUiState === 'error') return <>{header}<ErrorRetryPanel onRetry={() => setForcedUiState('normal')} /></>;

  const noData = households.length === 0 || forcedUiState === 'empty';

  return (
    <>
      {header}
      <section className="surface-box">
        <div className="box-toolbar">
          <div className="filter-row">
            <label className="inline-field">
              <span className="sr-only">Tìm theo mã căn hộ hoặc mã hộ</span>
              <Icon name="search" size={16} />
              <input
                className="input tabular"
                placeholder="Tìm căn hộ hoặc mã hộ, ví dụ A01"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </label>
            <label className="inline-field">
              <span className="field-hint">Trạng thái</span>
              <select className="select" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="ALL">Tất cả</option>
                <option value="ACTIVE">Hoạt động</option>
                <option value="INACTIVE">Không hoạt động</option>
              </select>
            </label>
          </div>
        </div>
        {filtered.length === 0 ? (
          <div className="state-panel">
            <h3>{noData ? 'Chưa có hộ nào' : 'Không tìm thấy hộ phù hợp'}</h3>
            <p>
              {noData
                ? 'Nhập hộ đầu tiên để bắt đầu quản lý nhân khẩu và thu phí theo diện tích.'
                : `Không có hộ nào khớp "${debounced}". Kiểm tra lại mã căn hộ.`}
            </p>
            {noData ? (
              <Link to="/admin/households/new" className="btn btn-primary">
                Thêm hộ
              </Link>
            ) : null}
          </div>
        ) : (
          <>
            <div className="table-scroll">
              <table className="ledger-table">
                <thead>
                  <tr>
                    <th scope="col">Mã hộ</th>
                    <th scope="col">Căn hộ</th>
                    <th scope="col" className="align-right">
                      Diện tích
                    </th>
                    <th scope="col">Địa chỉ</th>
                    <th scope="col" className="align-right">
                      Số người ở
                    </th>
                    <th scope="col">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {paged.pageItems.map((h) => (
                    <tr key={h.id} className="clickable-row" onClick={() => navigate(`/admin/households/${h.id}`)}>
                      <td className="tabular">
                        <Link to={`/admin/households/${h.id}`} onClick={(e) => e.stopPropagation()}>
                          {h.householdCode}
                        </Link>
                      </td>
                      <td className="tabular">
                        <strong>{h.apartmentCode}</strong>
                      </td>
                      <td className="align-right tabular">{formatAreaM2(h.areaM2)}</td>
                      <td>{h.address}</td>
                      <td className="align-right tabular">{h.currentResidents}</td>
                      <td>
                        {h.status === 'ACTIVE' ? (
                          <StatusBadge tone="paid" label="Hoạt động" />
                        ) : (
                          <StatusBadge tone="neutral" label="Không hoạt động" />
                        )}
                      </td>
                    </tr>
                  ))}
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
      <p className="field-hint">Tra cứu nhân khẩu theo tên hoặc số giấy tờ: chờ quyết định (Q18).</p>
    </>
  );
}
