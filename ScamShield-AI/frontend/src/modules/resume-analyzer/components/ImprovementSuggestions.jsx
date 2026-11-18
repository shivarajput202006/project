export default function ImprovementSuggestions({ suggestions = [] }) {
  return (
    <section className="resume-card" aria-labelledby="resume-suggestions-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">NEXT STEPS</span><h2 id="resume-suggestions-title">Improvement suggestions</h2></div></div>
      {suggestions.length
        ? <ol className="resume-suggestion-list">{suggestions.map((item, index) => (
          <li key={`${item.category}-${index}`}><span>{item.category}</span><strong>{item.suggestion}</strong><p>{item.reason}</p></li>
        ))}</ol>
        : <p className="resume-muted">No improvement suggestions were returned.</p>}
    </section>
  );
}
