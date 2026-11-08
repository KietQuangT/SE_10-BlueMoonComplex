export function Pager({ page, totalPages, onPageChange }: { page: number; totalPages: number; onPageChange: (page: number) => void }) {
  return <nav className="pagination-bar" aria-label="Phân trang"><button type="button" className="btn btn-secondary" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>Trang trước</button><span>Trang {page + 1} / {Math.max(totalPages, 1)}</span><button type="button" className="btn btn-secondary" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>Trang sau</button></nav>;
}
