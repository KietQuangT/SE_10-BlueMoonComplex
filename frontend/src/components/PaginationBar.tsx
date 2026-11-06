import React from 'react';

interface PaginationBarProps {
  page: number;
  pageSize: number;
  totalItems: number;
  onPageChange: (nextPage: number) => void;
  onPageSizeChange: (nextSize: number) => void;
}

export function PaginationBar({
  page,
  pageSize,
  totalItems,
  onPageChange,
  onPageSizeChange,
}: PaginationBarProps): React.ReactElement {
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
  const startIdx = totalItems === 0 ? 0 : (page - 1) * pageSize + 1;
  const endIdx = Math.min(totalItems, page * pageSize);

  return (
    <div className="pagination-bar">
      <span className="topbar-meta tabular">
        Hiển thị {startIdx}–{endIdx} / {totalItems} bản ghi
      </span>
      <div className="filter-row">
        <label className="inline-field">
          <span className="topbar-meta">Số dòng:</span>
          <select
            className="select"
            value={pageSize}
            onChange={(e) => {
              onPageSizeChange(Number(e.target.value));
              onPageChange(1);
            }}
          >
            <option value={10}>10 / trang</option>
            <option value={25}>25 / trang</option>
            <option value={50}>50 / trang</option>
          </select>
        </label>
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          disabled={page <= 1}
          onClick={() => onPageChange(page - 1)}
        >
          Trang trước
        </button>
        <span className="tabular page-count">
          {page} / {totalPages}
        </span>
        <button
          type="button"
          className="btn btn-secondary btn-sm"
          disabled={page >= totalPages}
          onClick={() => onPageChange(page + 1)}
        >
          Trang sau
        </button>
      </div>
    </div>
  );
}
