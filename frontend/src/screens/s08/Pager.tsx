export function Pager({ page, totalPages, onPageChange, label = 'Phân trang', disabled = false }: {
  page: number; totalPages: number; onPageChange: (page: number) => void; label?: string; disabled?: boolean;
}) {
  if (totalPages <= 1) return null;
  return <nav className="pagination-bar s08-pager" aria-label={label}>
    <button type="button" className="btn btn-secondary" disabled={disabled || page <= 0} onClick={() => onPageChange(page - 1)}>Trang trước</button>
    <span aria-live="polite">Trang {page + 1} / {totalPages}</span>
    <button type="button" className="btn btn-secondary" disabled={disabled || page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>Trang sau</button>
  </nav>;
}
