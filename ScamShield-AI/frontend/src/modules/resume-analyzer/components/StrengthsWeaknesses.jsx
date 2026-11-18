function ListColumn({ title, values, tone }) {
  return (
    <div className={`resume-feedback-column ${tone}`}>
      <h3>{title}</h3>
      {values.length
        ? <ul>{values.map((value) => <li key={value}>{value}</li>)}</ul>
        : <p className="resume-muted">No feedback available.</p>}
    </div>
  );
}

export default function StrengthsWeaknesses({ strengths = [], weaknesses = [] }) {
  return (
    <section className="resume-card" aria-labelledby="resume-strengths-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">AI-GENERATED FEEDBACK</span><h2 id="resume-strengths-title">Strengths & opportunities</h2></div></div>
      <div className="resume-feedback-grid">
        <ListColumn title="Strengths" values={strengths} tone="is-positive" />
        <ListColumn title="Areas to improve" values={weaknesses} tone="is-growth" />
      </div>
      <p className="resume-footnote">These are generated observations about the document, not independently verified facts about you.</p>
    </section>
  );
}
