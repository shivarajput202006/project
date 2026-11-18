export default function ReportLoading() {
  return (
    <div className="reports-loading" role="status" aria-label="Loading report history">
      <span className="reports-skeleton reports-skeleton-title" />
      <span className="reports-skeleton reports-skeleton-stats" />
      <span className="reports-skeleton reports-skeleton-stats" />
      <span className="reports-skeleton reports-skeleton-cards" />
      <span className="reports-skeleton reports-skeleton-cards" />
    </div>
  );
}
