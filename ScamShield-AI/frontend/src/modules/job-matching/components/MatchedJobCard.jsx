import { ArrowUpRight, Bookmark, BookmarkCheck, MapPin } from "lucide-react";
import MatchScoreCard from "./MatchScoreCard.jsx";
import SkillComparison from "./SkillComparison.jsx";
import ExperienceComparison from "./ExperienceComparison.jsx";
import EducationComparison from "./EducationComparison.jsx";
import MatchExplanation from "./MatchExplanation.jsx";
import SkillRoadmap from "./SkillRoadmap.jsx";
import JobSourceBadge from "../../job-finder/components/JobSourceBadge.jsx";
import { scoreLabel } from "../utils/matchScoreUtils.js";

function listingUrl(job) {
  const value = job.officialApplyUrl || job.providerReference;
  try {
    const url = new URL(value);
    return url.protocol === "https:" ? url.href : null;
  } catch {
    return null;
  }
}

function sourceUrl(job) {
  try {
    return new URL(job.providerReference).origin;
  } catch {
    return null;
  }
}

export default function MatchedJobCard({ match, isSaved, onSave, onUnsave, busy }) {
  const { job } = match;
  const applyUrl = listingUrl(job);
  return (
    <article className="match-job-card">
      <div className="match-job-heading">
        <div className="match-job-title">
          <div className="match-provider-line">
            <JobSourceBadge sourceName={job.sourceName} sourceUrl={job.sourceName === "Adzuna" ? sourceUrl(job) : undefined} />
            <span className="match-provider-badge">{job.verificationStatus === "SOURCE_LISTED" ? "Listed by source · not independently verified" : job.verificationStatus || "Unverified"}</span>
          </div>
          <h3>{job.title}</h3>
          <p>{job.company || "Company not provided"} <span>·</span> <MapPin size={13} /> {job.location || "Location not provided"}</p>
        </div>
        <div className="match-card-score">
          <strong>{match.score == null ? "—" : `${match.score}%`}</strong>
          <small>{scoreLabel(match.score)}</small>
        </div>
      </div>
      <div className="match-job-meta">
        <span>{job.workMode?.replaceAll("_", " ") || "Work mode not stated"}</span>
        <span>{job.jobType?.replaceAll("_", " ") || "Job type not stated"}</span>
        <span>{job.experience?.replaceAll("_", " ") || "Experience not stated"}</span>
        {job.salaryMin != null && <span>{job.salaryCurrency || ""} {job.salaryMin.toLocaleString()} {job.salaryMax != null ? `– ${job.salaryMax.toLocaleString()}` : "+"}{job.salaryIsPredicted ? " · estimated" : ""}</span>}
      </div>
      {job.requiredSkills?.length > 0 && <p className="match-listing-skills"><strong>Skills mentioned:</strong> {job.requiredSkills.join(", ")}</p>}
      <p className="match-listing-date">Posted {job.postedAt ? new Date(job.postedAt).toLocaleDateString() : "date not provided"} · refreshed {job.fetchedAt ? new Date(job.fetchedAt).toLocaleString() : "time not provided"}</p>
      <div className="match-job-actions">
        {applyUrl && <a className="match-apply-link" href={applyUrl} target="_blank" rel="noopener noreferrer">View provider listing <ArrowUpRight size={15} /></a>}
        <button className="match-save-button" type="button" disabled={busy} onClick={() => isSaved ? onUnsave(job) : onSave(job)}>
          {isSaved ? <BookmarkCheck size={15} /> : <Bookmark size={15} />}{isSaved ? "Saved" : "Save job"}
        </button>
      </div>
      <MatchScoreCard match={match} />
      <div className="match-comparison-grid">
        <ExperienceComparison experience={match.experience} />
        <EducationComparison education={match.education} />
      </div>
      <SkillComparison skills={match.skills} />
      <MatchExplanation explanation={match.explanation} />
      <details className="match-roadmap-details">
        <summary>Learning roadmap based on this listing</summary>
        <SkillRoadmap items={match.roadmap} />
      </details>
    </article>
  );
}
