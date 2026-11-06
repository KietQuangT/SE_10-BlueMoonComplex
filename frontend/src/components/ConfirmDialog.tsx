import React, { useEffect, useRef } from 'react';

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  consequence: string;
  confirmLabel: string;
  confirmTone?: 'primary' | 'danger';
  busy?: boolean;
  children?: React.ReactNode;
  onConfirm: () => void;
  onCancel: () => void;
}

/**
 * Shared Confirmation Modal (09-quy-uoc-ui.md Mục G):
 * 1. Exact target object in title
 * 2. Consequence sentence
 * 3. Verb-named primary action (never "OK"/"Có")
 * 4. Cancel button + Esc closes + restores focus to trigger
 */
export function ConfirmDialog({
  open,
  title,
  consequence,
  confirmLabel,
  confirmTone = 'primary',
  busy = false,
  children,
  onConfirm,
  onCancel,
}: ConfirmDialogProps): React.ReactElement | null {
  const dialogRef = useRef<HTMLDialogElement | null>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (open) {
      previousFocusRef.current = document.activeElement as HTMLElement | null;
      if (!dialog.open && typeof dialog.showModal === 'function') {
        dialog.showModal();
      }
    } else if (dialog.open && typeof dialog.close === 'function') {
      dialog.close();
      previousFocusRef.current?.focus();
    }
  }, [open]);

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      className="confirm-dialog"
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-desc"
      onCancel={(e) => {
        e.preventDefault();
        if (!busy) onCancel();
      }}
    >
      <div className="dialog-body">
        <h2 id="confirm-dialog-title" className="dialog-title">
          {title}
        </h2>
        <p id="confirm-dialog-desc" className="dialog-consequence">
          {consequence}
        </p>
        {children}
      </div>
      <div className="dialog-footer">
        <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={busy}>
          Hủy
        </button>
        <button
          type="button"
          className={`btn ${confirmTone === 'danger' ? 'btn-danger' : 'btn-primary'}`}
          onClick={onConfirm}
          disabled={busy}
        >
          {busy ? 'Đang xử lý...' : confirmLabel}
        </button>
      </div>
    </dialog>
  );
}
