import { ExternalLink } from "lucide-react";

export default function JobSourceBadge({ sourceName = "Job provider", sourceUrl }) {
  if (sourceName.toLowerCase() === "adzuna" && sourceUrl) {
    return (
      <a
        className="job-source-badge job-source-attribution"
        href={sourceUrl}
        target="_blank"
        rel="noopener noreferrer"
        aria-label="Adzuna job listings, opens the Adzuna website in a new tab"
      >
        Adzuna <ExternalLink size={12} aria-hidden="true" />
      </a>
    );
  }
  return <span className="job-source-badge">{sourceName}</span>;
}
