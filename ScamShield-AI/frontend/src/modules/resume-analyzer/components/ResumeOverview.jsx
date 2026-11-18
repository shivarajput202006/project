import { CheckCircle2, CircleHelp } from "lucide-react";

export default function ResumeOverview({ analysis }) {
  if (!analysis) return null;
  const { score, limitations = [] } = analysis;
  return (
    <section className="resume-overview resume-card" aria-labelledby="resume-overview-title">
      <div className="resume-section-heading">
        <div><span className="dashboard-eyebrow">ANALYSIS SUMMARY</span><h2 id="resume-overview-title">Resume overview</h2></div>
        {score.available
          ? <span className="resume-score-value">{score.overallScore}<small>/100</small></span>
          : <span className="resume-score-unavailable"><CircleHelp size={15} /> Score unavailable</span>}
      </div>
      <p className="resume-score-disclaimer">{score.disclaimer}</p>
      {score.available && <div className="resume-score-grid">
        {score.criteria.map((criterion) => (
          <article className="resume-score-criterion" key={criterion.name}>
            <div><strong>{criterion.name}</strong><span>{criterion.score}/100</span></div>
            <div className="resume-meter" aria-label={`${criterion.name}: ${criterion.score} out of 100`}>
              <span style={{ width: `${criterion.score}%` }} />
            </div>
            <p>{criterion.explanation}</p>
            <small>{criterion.improvementAction}</small>
          </article>
        ))}
      </div>}
      {limitations.map((limitation) => (
        <p className="resume-limitation" key={limitation}><CheckCircle2 size={14} />{limitation}</p>
      ))}
    </section>
  );
}
