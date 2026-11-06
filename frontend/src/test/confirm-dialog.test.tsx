import { fireEvent, render, screen } from '@testing-library/react';
import { useState } from 'react';
import { beforeAll, describe, expect, it } from 'vitest';
import { ConfirmDialog } from '../components/ConfirmDialog';

// jsdom has no modal <dialog>: give it the two methods the component calls. Like a browser,
// showModal() moves focus into the dialog, so a test can tell whether focus comes back.
beforeAll(() => {
  HTMLDialogElement.prototype.showModal = function showModal(this: HTMLDialogElement) {
    this.setAttribute('open', '');
    this.querySelector<HTMLElement>('button')?.focus();
  };
  HTMLDialogElement.prototype.close = function close(this: HTMLDialogElement) {
    this.removeAttribute('open');
  };
});

function Harness({ onConfirm }: { onConfirm?: () => void }) {
  const [open, setOpen] = useState(false);
  return (
    <>
      <button type="button" onClick={() => setOpen(true)}>
        Mở hộp
      </button>
      <ConfirmDialog
        open={open}
        title="Vô hiệu hóa tài khoản demo-cudan?"
        consequence="Cư dân này sẽ không đăng nhập được nữa."
        confirmLabel="Vô hiệu hóa ngay"
        onConfirm={() => {
          onConfirm?.();
          setOpen(false);
        }}
        onCancel={() => setOpen(false)}
      />
    </>
  );
}

describe('ConfirmDialog (M1: focus returns to the element that opened it)', () => {
  it('shows the title, the consequence and a verb-named action while open', () => {
    render(<Harness />);
    fireEvent.click(screen.getByRole('button', { name: 'Mở hộp' }));
    expect(screen.getByText('Vô hiệu hóa tài khoản demo-cudan?')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Vô hiệu hóa ngay' })).toBeInTheDocument();
  });

  it('returns focus to the trigger after Hủy', () => {
    render(<Harness />);
    const trigger = screen.getByRole('button', { name: 'Mở hộp' });
    trigger.focus();
    fireEvent.click(trigger);
    fireEvent.click(screen.getByRole('button', { name: 'Hủy' }));
    expect(trigger).toHaveFocus();
  });

  it('returns focus to the trigger after Esc (cancel event)', () => {
    const { container } = render(<Harness />);
    const trigger = screen.getByRole('button', { name: 'Mở hộp' });
    trigger.focus();
    fireEvent.click(trigger);
    const dialog = container.querySelector('dialog') as HTMLDialogElement;
    fireEvent(dialog, new Event('cancel', { cancelable: true }));
    expect(trigger).toHaveFocus();
  });

  it('returns focus to the trigger after the action is confirmed', () => {
    let confirmed = 0;
    render(<Harness onConfirm={() => (confirmed += 1)} />);
    const trigger = screen.getByRole('button', { name: 'Mở hộp' });
    trigger.focus();
    fireEvent.click(trigger);
    fireEvent.click(screen.getByRole('button', { name: 'Vô hiệu hóa ngay' }));
    expect(confirmed).toBe(1);
    expect(trigger).toHaveFocus();
  });
});
