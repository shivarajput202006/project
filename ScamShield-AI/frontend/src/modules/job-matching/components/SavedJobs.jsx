import { Bookmark } from "lucide-react";

export default function SavedJobs({ jobs, onUnsave, busyId }) {
  return (
    <section className="resume-card match-saved-panel">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">YOUR LIST</span><h2><Bookmark size={15} /> Saved jobs <small>{jobs.length}</small></h2></div></div>
      {jobs.length === 0 ? <p className="match-note">No saved jobs yet. Save a current listing to keep it in your Module 06 list.</p> : (
        <ul className="match-saved-list">
          {jobs.map((job) => (
            <li key={job.id}>
              <span><strong>{job.title}</strong><small>{job.company || "Company not provided"} · {job.sourceName}</small></span>
              <button type="button" disabled={busyId === job.id} onClick={() => onUnsave(job)}>Remove</button>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
