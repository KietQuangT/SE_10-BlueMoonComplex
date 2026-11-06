import { useEffect } from 'react';

/**
 * Presentation only: copy each column header onto the cells under it (data-label) so a table in a
 * narrow box can print one card per row (see the container query in app.css). Cells with a
 * colspan get no label. No data is read or changed.
 */
export function labelTableCells(root: ParentNode): void {
  root.querySelectorAll<HTMLTableElement>('table.ledger-table').forEach((table) => {
    const heads = Array.from(table.querySelectorAll<HTMLTableCellElement>('thead th')).map(
      (th) => th.textContent?.trim() ?? '',
    );
    table.querySelectorAll<HTMLTableRowElement>('tbody tr').forEach((tr) => {
      let col = 0;
      Array.from(tr.children).forEach((cell) => {
        if (!(cell instanceof HTMLTableCellElement)) return;
        const label = cell.colSpan > 1 ? '' : (heads[col] ?? '');
        if (cell.dataset.label !== label) cell.dataset.label = label;
        col += cell.colSpan || 1;
      });
    });
  });
}

/** Keeps `table.ledger-table` cells labelled inside #main-content as screens render and update. */
export function useStackedTables(ready: boolean): void {
  useEffect(() => {
    if (!ready) return;
    const root = document.getElementById('main-content');
    if (!root) return;
    labelTableCells(root);
    const observer = new MutationObserver(() => labelTableCells(root));
    observer.observe(root, { childList: true, subtree: true });
    return () => observer.disconnect();
  }, [ready]);
}
