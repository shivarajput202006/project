import { BriefcaseBusiness } from "lucide-react";
import MatchedJobCard from "./MatchedJobCard.jsx";

export default function RecommendedJobs({ matches, savedIds, busyId, onSave, onUnsave }) {
  if (!matches.length) return null;
  return (
    <section className="match-recommendations">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">CURRENT PROVIDER RESULTS</span><h2><BriefcaseBusiness size={16} /> Recommended live listings</h2></div></div>
      {matches.map((match) => (
        <MatchedJobCard
          key={`${match.sourceName}:${match.sourceJobId}`}
          match={match}
          isSaved={savedIds.has(match.job.id)}
          busy={busyId === match.job.id}
          onSave={onSave}
          onUnsave={onUnsave}
        />
      ))}
    </section>
  );
}
