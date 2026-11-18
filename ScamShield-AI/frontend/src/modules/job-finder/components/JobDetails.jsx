import { ArrowUpRight, BadgeCheck, ExternalLink, X } from "lucide-react";
import { experienceLabel, jobTypeLabel, workModeLabel } from "../utils/jobFilters.js";
import JobMatchScore from "./JobMatchScore.jsx";
import JobSourceBadge from "./JobSourceBadge.jsx";

function sourceUrl(job) {
  try {
    return new URL(job.providerReference).origin;
  } catch {
    return "https://www.adzuna.com";
  }
}

export default function JobDetails({ job, saved, saving, onSave, onClose }) {
  if (!job) return null;
  const applyUrl = job.officialApplyUrl ?? job.providerReference;
  return (
    <aside className="job-details-panel" aria-label="Job details">
      <div className="job-details-top">
        <span className="dashboard-eyebrow">LISTING DETAILS</span>
        <button type="button" className="job-icon-button" onClick={onClose} aria-label="Close job details"><X size={18} /></button>
      </div>
      <h2>{job.title}</h2>
      <p className="job-details-company">{job.company} · {job.location}</p>
      <div className="job-detail-facts">
        <span><strong>Work mode</strong>{workModeLabel(job.workMode)}</span>
        <span><strong>Experience</strong>{experienceLabel(job.experience)}</span>
        <span><strong>Job type</strong>{jobTypeLabel(job.jobType)}</span>
      </div>
      {job.description && <p className="job-details-description">{job.description}</p>}
      <div className="job-details-section">
        <h3>Skills mentioned in listing</h3>
        {job.requiredSkills?.length
          ? <div className="job-skill-list">{job.requiredSkills.map((skill) => <span key={skill}>{skill}</span>)}</div>
          : <p className="job-muted-label">The listing did not provide extractable skills.</p>}
      </div>
      <div className="job-trust-note">
        <BadgeCheck size={17} aria-hidden="true" />
        <span>Listed by {job.sourceName}; this status does not independently verify the employer or role.</span>
      </div>
      <JobMatchScore match={{ available: false }} />
      <div className="job-details-actions">
        {applyUrl && (
          <a className="dashboard-primary-button" href={applyUrl} target="_blank" rel="noopener noreferrer">
            View listing <ExternalLink size={15} aria-hidden="true" />
          </a>
        )}
        <button type="button" className="job-secondary-button" onClick={() => onSave(job)} disabled={saving}>
          {saved ? "Remove saved job" : "Save job"}
        </button>
      </div>
      <div className="job-details-source">
        <span>Source: {job.sourceName}</span>
        <JobSourceBadge sourceName={job.sourceName} sourceUrl={job.sourceName === "Adzuna" ? sourceUrl(job) : undefined} />
        {job.providerReference && <small><ArrowUpRight size={12} /> Provider redirects are not an employer verification.</small>}
      </div>
    </aside>
  );
}
