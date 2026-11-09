import { useRef, type FormEvent, type ReactNode } from 'react';
import { ConfirmDialog } from './ConfirmDialog';

/** Form modal with the same focus, Escape and scroll behavior as confirmations. */
export function FormDialog({ open, title, description, submitLabel, busy = false, onSubmit, onCancel, children }: {
  open: boolean;
  title: string;
  description: string;
  submitLabel: string;
  busy?: boolean;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
  onCancel: () => void;
  children: ReactNode;
}) {
  const form = useRef<HTMLFormElement>(null);
  return <ConfirmDialog open={open} title={title} consequence={description} confirmLabel={submitLabel}
    busy={busy} onCancel={onCancel} onConfirm={() => form.current?.requestSubmit()}>
    <form ref={form} className="stack-form" onSubmit={onSubmit}>
      <fieldset className="dialog-fields" disabled={busy}>{children}</fieldset>
      <button type="submit" hidden>{submitLabel}</button>
    </form>
  </ConfirmDialog>;
}
