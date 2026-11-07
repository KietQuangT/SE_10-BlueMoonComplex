import { useState } from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { expect, it, vi } from 'vitest';
import { AbsencePage } from '../screens/resident/AbsencePage';
const submitDemo = vi.fn();
vi.mock('../state/AppStateContext', () => ({ useAppState: () => {
  const [draft, setDraft] = useState({ residentId: '12', effectiveDate: '2099-01-01', endDateAssumed: '', reason: 'Công tác' });
  return { mode: 'demo', user: { householdId: 1 }, residents: [{ id: 12, householdId: 1, fullName: 'An', residencyStatus: 'Đang cư trú' }], residencyChanges: [], absenceDraft: draft, setAbsenceDraft: setDraft, submitAbsenceDemo: submitDemo, forcedUiState: 'normal' };
} }));
it.each([['Từ ngày', 'Từ ngày chưa đúng định dạng.', 'abs-from-err'], [/Đến ngày/, 'Đến ngày chưa đúng định dạng.', 'abs-to-err']])('blocks incomplete demo date', (label, message, errorId) => {
  render(<AbsencePage />);
  const input = screen.getByLabelText(label) as HTMLInputElement;
  Object.defineProperty(input, 'validity', { configurable: true, value: { badInput: true } });
  fireEvent.submit(input.closest('form')!);
  expect(screen.getByText(message)).toBeInTheDocument(); expect(input).toHaveFocus();
  expect(input).toHaveAttribute('aria-describedby', errorId); expect(submitDemo).not.toHaveBeenCalled();
});
