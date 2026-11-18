import { ChevronLeft, ChevronRight } from "lucide-react";

export default function ReportPagination({ page, totalPages, totalElements, size, onChange }) {
  if (totalPages <= 1) return null;
  const start = (page - 1) * size + 1;
  const end = Math.min(page * size, totalElements);
  return (
    <nav className="reports-pagination" aria-label="Report pages">
      <span>Showing {start}–{end} of {totalElements}</span>
      <div>
        <button type="button" aria-label="Previous page" onClick={() => onChange(page - 1)} disabled={page <= 1}>
          <ChevronLeft size={16} />
        </button>
        <span>Page {page} of {totalPages}</span>
        <button type="button" aria-label="Next page" onClick={() => onChange(page + 1)} disabled={page >= totalPages}>
          <ChevronRight size={16} />
        </button>
      </div>
    </nav>
  );
}
