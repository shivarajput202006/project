import { Bookmark, BriefcaseBusiness, Clock3, MapPin, Wallet } from "lucide-react";
import { experienceLabel, jobTypeLabel, workModeLabel } from "../utils/jobFilters.js";
import JobSourceBadge from "./JobSourceBadge.jsx";

function formatSalary(job) {
  if (job.salaryMin == null && job.salaryMax == null) return null;
  const currency = job.salaryCurrency ? `${job.salaryCurrency} ` : "";
  const min = job.salaryMin == null ? "" : Math.round(job.salaryMin).toLocaleString();
  const max = job.salaryMax == null ? "" : Math.round(job.salaryMax).toLocaleString();
  return `${job.salaryIsPredicted ? "Estimated " : ""}${currency}${min}${min && max ? "–" : ""}${max}${job.salaryCurrency ? "" : " (currency not provided)"}`;
}

function sourceUrl(job) {
  try {
    return new URL(job.providerReference).origin;
  } catch {
    return "https://www.adzuna.com";
  }
}

export default function JobCard({ job, saved, onSave, onSelect, saving }) {
  const salary = formatSalary(job);
  return (
    <article className="job-card">
      <div className="job-card-main">
        <div className="job-card-heading">
          <div className="job-company-mark" aria-hidden="true">{job.company?.slice(0, 1)?.toUpperCase() ?? "J"}</div>
          <div className="job-card-title">
            <button type="button" className="job-title-button" onClick={() => onSelect(job)}>{job.title}</button>
            <p>{job.company} <span aria-hidden="true">·</span> {job.location}</p>
          </div>
          <button
            type="button"
            className={`job-bookmark-button${saved ? " is-saved" : ""}`}
            onClick={() => onSave(job)}
            disabled={saving}
            aria-pressed={saved}
            aria-label={saved ? `Unsave ${job.title}` : `Save ${job.title}`}
          >
            <Bookmark size={18} fill={saved ? "currentColor" : "none"} />
          </button>
        </div>
        <div className="job-card-meta">
          <span><MapPin size={14} />{workModeLabel(job.workMode)}</span>
          <span><BriefcaseBusiness size={14} />{experienceLabel(job.experience)}</span>
          <span><BriefcaseBusiness size={14} />{jobTypeLabel(job.jobType)}</span>
          {salary && <span><Wallet size={14} />{salary}</span>}
          {job.postedAt && <span><Clock3 size={14} />{new Date(job.postedAt).toLocaleDateString()}</span>}
        </div>
        <div className="job-skill-list" aria-label="Skills mentioned in listing">
          {job.requiredSkills?.length
            ? job.requiredSkills.slice(0, 7).map((skill) => <span key={skill}>{skill}</span>)
            : <span className="job-muted-label">No skills extracted from listing text</span>}
        </div>
        <div className="job-card-footer">
          <JobSourceBadge sourceName={job.sourceName} sourceUrl={job.sourceName === "Adzuna" ? sourceUrl(job) : undefined} />
          <span className="job-verification-status">
            {job.verificationStatus === "SOURCE_LISTED" ? "Listed by source · not independently verified" : "Unverified"}
          </span>
          <span className="job-updated">Updated {job.fetchedAt ? new Date(job.fetchedAt).toLocaleString() : "time unavailable"}</span>
        </div>
      </div>
    </article>
  );
}
