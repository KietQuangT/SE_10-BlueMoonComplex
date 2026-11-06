import React from 'react';
import { Icon } from './Icon';

export function SkeletonRows({ rows = 4 }: { rows?: number }): React.ReactElement {
  return (
    <div className="surface-box" aria-busy="true" aria-live="polite">
      <div className="skeleton-stack">
        {Array.from({ length: rows }).map((_, idx) => (
          <div key={idx} className="skeleton-line alt" />
        ))}
      </div>
    </div>
  );
}

export function EmptyStatePanel({
  title,
  description,
  actionLabel,
  onAction,
}: {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
}): React.ReactElement {
  return (
    <div className="surface-box state-panel" role="status">
      <Icon name="info" size={28} />
      <h3>{title}</h3>
      <p>{description}</p>
      {actionLabel && onAction ? (
        <button type="button" className="btn btn-primary" onClick={onAction}>
          {actionLabel}
        </button>
      ) : null}
    </div>
  );
}

export function ErrorRetryPanel({
  message,
  requestId,
  onRetry,
}: {
  message?: string;
  requestId?: string;
  onRetry: () => void;
}): React.ReactElement {
  return (
    <div className="surface-box state-panel" role="alert">
      <Icon name="alert" size={28} />
      <h3>Không thể tải dữ liệu</h3>
      <p>{message || 'Có lỗi xảy ra khi kết nối máy chủ. Vui lòng kiểm tra mạng và thử lại.'}</p>
      {requestId ? (
        <span className="badge badge-neutral tabular">Mã yêu cầu: {requestId}</span>
      ) : null}
      <button type="button" className="btn btn-secondary" onClick={onRetry}>
        Thử lại
      </button>
    </div>
  );
}
