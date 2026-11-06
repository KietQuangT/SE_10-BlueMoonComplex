import React from 'react';
import type { ContractBadgeKind } from '../types/domain';
import { Icon } from './Icon';
import { SHOW_DEV_TOOLS } from '../lib/devTools';

export function StatusBadge({
  tone,
  label,
}: {
  tone: 'paid' | 'pending' | 'danger' | 'neutral' | 'assumed';
  label: string;
}): React.ReactElement {
  return <span className={`badge badge-${tone}`}>{label}</span>;
}

export function ContractBadge({ kind }: { kind: ContractBadgeKind }): React.ReactElement | null {
  if (!SHOW_DEV_TOOLS) return null;
  switch (kind) {
    case 'api-live':
      return (
        <span className="badge badge-paid" title="Đã có API backend chạy thật trên nhánh main">
          <Icon name="check" size={13} />
          API thật
        </span>
      );
    case 'db-ready':
      return (
        <span className="badge badge-neutral" title="Đã có bảng/cột trong schema Flyway V2-V6, chờ API">
          Đã có cột DB
        </span>
      );
    case 'assumed':
      return (
        <span className="badge badge-assumed" title="Trường hoặc màn giả định, chờ chốt hợp đồng">
          Giả định
        </span>
      );
    case 'pending-rule':
      return (
        <span className="badge badge-pending" title="Thuộc phạm vi v1 nhưng quy tắc nghiệp vụ đang chờ chốt">
          Chờ chốt quy tắc
        </span>
      );
  }
}
