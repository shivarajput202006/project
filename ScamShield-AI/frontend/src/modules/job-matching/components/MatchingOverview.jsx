import { BriefcaseBusiness, Info } from "lucide-react";

export default function MatchingOverview({ result }) {
  return (
    <section className="resume-card match-overview">
      <div>
        <span className="dashboard-eyebrow">LIVE MATCH RESULTS</span>
        <h2>{result.matches.length} {result.matches.length === 1 ? "listing" : "listings"} ranked</h2>
        <p>{result.message}</p>
      </div>
      <div className="match-overview-meta">
        <span><BriefcaseBusiness size={15} />{result.sourceName || "Live provider"}</span>
        <span><Info size={15} />Model {result.scoringModelVersion}</span>
      </div>
    </section>
  );
}
