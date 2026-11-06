import React from 'react';
import type { ResidencyChangeRecord, ResidentMember } from '../types/domain';
import { StatusBadge } from './Badge';

export function ResidencyBadge({ status }: { status: ResidentMember['residencyStatus'] }): React.ReactElement {
  if (status === 'Đang cư trú') return <StatusBadge tone="paid" label="Đang cư trú" />;
  if (status === 'Tạm vắng') return <StatusBadge tone="pending" label="Tạm vắng" />;
  return <StatusBadge tone="neutral" label="Đã chuyển đi" />;
}

export function ChangeStatusBadge({ status }: { status: ResidencyChangeRecord['status'] }): React.ReactElement {
  if (status === 'PENDING') return <StatusBadge tone="pending" label="Chờ duyệt" />;
  if (status === 'APPROVED') return <StatusBadge tone="paid" label="Đã duyệt" />;
  return <StatusBadge tone="danger" label="Từ chối" />;
}

export function changeTypeLabel(type: ResidencyChangeRecord['changeType']): string {
  return type === 'TAM_VANG' ? 'Tạm vắng' : 'Chuyển đi';
}
