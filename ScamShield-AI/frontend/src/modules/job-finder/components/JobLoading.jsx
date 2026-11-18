export default function JobLoading() {
  return (
    <div className="job-loading-list" role="status" aria-label="Loading job listings">
      <span className="sr-only">Loading job listings</span>
      {Array.from({ length: 4 }, (_, index) => (
        <div className="job-loading-card" key={index}>
          <i /><b /><span /><span /><span />
        </div>
      ))}
    </div>
  );
}
