import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ChangeStatusBadge, changeTypeLabel } from '../components/ResidencyBadge';
describe('S06 labels', () => {
  it('shows cancellation and return in Vietnamese', () => {
    render(<ChangeStatusBadge status="CANCELLED" />);
    expect(screen.getByText('Đã hủy')).toBeInTheDocument();
    expect(changeTypeLabel('QUAY_LAI')).toBe('Quay lại cư trú');
  });
});
