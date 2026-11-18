import { ArrowUpRight, Bookmark } from "lucide-react";
import { Link } from "react-router-dom";

export default function SavedJobsPreview({ jobs = [] }) {
  return (
    <section className="dashboard-panel">
      <div className="dashboard-panel-heading">
        <div><span className="dashboard-eyebrow">YOUR COLLECTION</span><h2>Saved jobs</h2></div>
        <Link className="dashboard-panel-link" to="/saved-jobs" aria-label="View saved jobs">
          <ArrowUpRight size={16} />
        </Link>
      </div>
      {jobs.length ? (
        <div className="dashboard-saved-jobs">
          {jobs.map((job) => (
            <article className="dashboard-saved-job" key={job.id}>
              <span className="dashboard-empty-icon"><Bookmark size={16} /></span>
              <div><strong>{job.title}</strong><p>{job.company}</p></div>
            </article>
          ))}
        </div>
      ) : (
        <div className="dashboard-empty-state dashboard-empty-state-compact">
          <span className="dashboard-empty-icon"><Bookmark size={18} /></span>
          <strong>No saved jobs yet</strong>
          <p>Jobs you save will be listed here.</p>
        </div>
      )}
    </section>
  );
}
