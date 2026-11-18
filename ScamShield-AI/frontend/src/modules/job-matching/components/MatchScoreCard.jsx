import { CheckCircle2, CircleHelp } from "lucide-react";

export default function MatchScoreCard({ match }) {
  return (
    <section className="match-score-panel">
      <div className="match-score-total">
        <span>{match.score == null ? "—" : `${match.score}%`}</span>
        <small>{match.score == null ? "Not enough data" : "relevance estimate"}</small>
      </div>
      <div className="match-score-criteria">
        {match.scoreBreakdown.map((criterion) => (
          <div className="match-criterion" key={criterion.criterion}>
            <div className="match-criterion-heading">
              <strong>{criterion.criterion}</strong>
              <span>{criterion.score == null
                ? <><CircleHelp size={13} /> Not scored · base {criterion.configuredWeight}%</>
                : <><CheckCircle2 size={13} /> {criterion.score}% · base {criterion.configuredWeight}% → effective {criterion.effectiveWeight}%</>}
              </span>
            </div>
            <p>{criterion.explanation}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
