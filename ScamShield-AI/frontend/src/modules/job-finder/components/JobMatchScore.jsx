import { Sparkles } from "lucide-react";

export default function JobMatchScore({ match }) {
  if (!match?.available) {
    return (
      <div className="job-match-unavailable" role="note">
        <Sparkles size={16} aria-hidden="true" />
        <span>{match?.explanation ?? "Resume matching is unavailable until resume analysis is connected."}</span>
      </div>
    );
  }
  return (
    <section className="job-match-score" aria-label="Resume skill match">
      <strong>{match.score}% skills overlap</strong>
      <p>{match.explanation}</p>
      <div>Matching: {match.matchingSkills.join(", ") || "None identified"}</div>
      <div>Not listed on resume: {match.missingSkills.join(", ") || "None"}</div>
    </section>
  );
}
