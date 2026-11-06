import React from 'react';
import { Link } from 'react-router-dom';
import { useAppState } from '../state/AppStateContext';
import { FIELD_CHANGE_PROPOSALS, STORYBOARDS } from '../data/mockData';
import type { ForcedUiState } from '../types/domain';
import { ContractBadge } from './Badge';
import { Icon } from './Icon';

const STATES: { value: ForcedUiState; label: string }[] = [
  { value: 'normal', label: 'Bình thường' },
  { value: 'loading', label: 'Đang tải' },
  { value: 'empty', label: 'Rỗng' },
  { value: 'error', label: 'Lỗi (có thử lại)' },
  { value: 'forbidden-403', label: 'Không có quyền (403)' },
  { value: 'unauthorized-401', label: 'Phiên hết hạn (401)' },
  { value: 'submitting', label: 'Đang gửi' },
  { value: 'success', label: 'Thành công' },
];

/** QA & handoff drawer: forced UI states, storyboards L1/L3/L5, field-change table. Dev only. */
export function QaDrawer(): React.ReactElement | null {
  const {
    qaDrawerOpen,
    setQaDrawerOpen,
    forcedUiState,
    setForcedUiState,
    mode,
    setMode,
    user,
    triggerSessionExpired,
  } = useAppState();

  if (!qaDrawerOpen) return null;

  return (
    <aside className="qa-drawer" aria-label="Bảng kiểm thử UI">
      <div className="qa-drawer-head">
        <strong>Kiểm thử UI và bàn giao</strong>
        <button
          type="button"
          className="btn btn-ghost btn-sm qa-close"
          onClick={() => setQaDrawerOpen(false)}
          aria-label="Đóng bảng kiểm thử"
        >
          <Icon name="close" />
        </button>
      </div>
      <div className="qa-drawer-body">
        <section>
          <h3 className="qa-heading">Ép trạng thái màn hình</h3>
          <p className="field-hint">
            Đủ 8 trạng thái cho M-01, M-31, M-33. 401/403 dùng khung chung M-04 cho mọi màn.
          </p>
          <div className="state-grid">
            {STATES.map((s) => (
              <button
                key={s.value}
                type="button"
                className={`tab-btn${forcedUiState === s.value ? ' active' : ''}`}
                aria-pressed={forcedUiState === s.value}
                onClick={() => setForcedUiState(s.value)}
              >
                {s.label}
              </button>
            ))}
          </div>
        </section>

        <section>
          <h3 className="qa-heading">Nguồn dữ liệu</h3>
          <div className="tab-strip">
            <button
              type="button"
              className={`tab-btn${mode === 'api' ? ' active' : ''}`}
              onClick={() => setMode('api')}
            >
              Backend thật
            </button>
            <button
              type="button"
              className={`tab-btn${mode === 'demo' ? ' active' : ''}`}
              onClick={() => setMode('demo')}
            >
              Demo
            </button>
          </div>
          {user ? (
            <button
              type="button"
              className="btn btn-secondary btn-sm qa-gap"
              onClick={() => {
                triggerSessionExpired(window.location.pathname);
                setQaDrawerOpen(false);
              }}
            >
              Mô phỏng phiên hết hạn (giữ nội dung form khai báo)
            </button>
          ) : null}
        </section>

        <section>
          <h3 className="qa-heading">Storyboard L1, L3, L5</h3>
          {STORYBOARDS.map((sb) => (
            <div key={sb.code} className="storyboard">
              <div className="screen-title-row">
                <span className="badge badge-code">{sb.code}</span>
                <strong className="storyboard-title">{sb.title}</strong>
              </div>
              <span className="field-hint">{sb.role}</span>
              <ol className="storyboard-steps">
                {sb.steps.map((step) => (
                  <li key={step.label}>
                    <Link to={step.path} onClick={() => setQaDrawerOpen(false)}>
                      {step.label}
                    </Link>
                  </li>
                ))}
              </ol>
            </div>
          ))}
        </section>

        <section>
          <h3 className="qa-heading">Bảng đổi trường (gửi Sơn chốt)</h3>
          <div className="table-scroll">
            <table className="ledger-table compact">
              <thead>
                <tr>
                  <th>Màn</th>
                  <th>Trường / màn</th>
                  <th>Bắt buộc</th>
                  <th>Hỏi</th>
                </tr>
              </thead>
              <tbody>
                {FIELD_CHANGE_PROPOSALS.map((f) => (
                  <tr key={f.id}>
                    <td className="tabular">{f.screenCode}</td>
                    <td>
                      <strong>{f.fieldOrScreen}</strong>
                      <div className="field-hint">{f.reason}</div>
                    </td>
                    <td>{f.required ? 'Có' : 'Không'}</td>
                    <td>{f.ownerQuestion}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section>
          <h3 className="qa-heading">Ký hiệu hợp đồng</h3>
          <ul className="legend-list">
            <li>
              <ContractBadge kind="api-live" /> Ghép backend thật (S01, S02)
            </li>
            <li>
              <ContractBadge kind="db-ready" /> Có cột DB V2–V6, chưa có API
            </li>
            <li>
              <ContractBadge kind="assumed" /> Trường giả định, chờ hợp đồng
            </li>
            <li>
              <ContractBadge kind="pending-rule" /> Thuộc v1, quy tắc chưa chốt
            </li>
          </ul>
        </section>
      </div>
    </aside>
  );
}
