import React, { useEffect, useId, useRef } from 'react';

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
 *
 * The <dialog> stays mounted so closing it runs close() and returns focus to the element that
 * opened it (an unmounted dialog has no ref left to do that). Its body mounts only while open.
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
}: ConfirmDialogProps): React.ReactElement {
  const dialogRef = useRef<HTMLDialogElement | null>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);
  const uid = useId();
  const titleId = `${uid}-title`;
  const descId = `${uid}-desc`;

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (open) {
      previousFocusRef.current = document.activeElement as HTMLElement | null;
      if (!dialog.open && typeof dialog.showModal === 'function') {
        dialog.showModal();
      }
    } else if (dialog.open) {
      dialog.close();
      previousFocusRef.current?.focus();
      previousFocusRef.current = null;
    }
  }, [open]);

  return (
    <dialog
      ref={dialogRef}
      className="confirm-dialog"
      aria-labelledby={titleId}
      aria-describedby={descId}
      onCancel={(e) => {
        e.preventDefault();
        if (!busy) onCancel();
      }}
    >
      {open ? (
        <>
          <div className="dialog-body">
            <h2 id={titleId} className="dialog-title">
              {title}
            </h2>
            <p id={descId} className="dialog-consequence">
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
        </>
      ) : null}
    </dialog>
  );
}
