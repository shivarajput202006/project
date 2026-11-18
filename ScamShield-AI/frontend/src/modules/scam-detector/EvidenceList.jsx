const evidenceLabels = {
  USER_PROVIDED: "Provided content",
  AI_INFERRED: "AI-inferred",
  UNVERIFIED: "Unverified",
  MISSING: "Missing information",
  TRUSTED_SOURCE: "Trusted source",
};

export default function EvidenceList({ indicators = [], evidence = [] }) {
  if (indicators.length === 0 && evidence.length === 0) {
    return (
      <section className="scam-report-section">
        <div className="scam-report-section-heading"><span>Evidence and indicators</span></div>
        <p className="scam-report-empty">No specific indicators were returned. This is not proof that the opportunity is genuine.</p>
      </section>
    );
  }

  return (
    <section className="scam-report-section">
      <div className="scam-report-section-heading"><span>Suspicious indicators and evidence</span><small>{indicators.length + evidence.length} items</small></div>
      <div className="scam-evidence-list">
        {indicators.map((indicator, index) => (
          <article className="scam-evidence-item" key={`indicator-${index}`}>
            <span className="scam-evidence-marker" />
            <div className="scam-evidence-copy">
              <div className="scam-evidence-heading"><strong>{indicator.title}</strong><span>{evidenceLabels[indicator.evidenceType] ?? "Unverified"}</span></div>
              <small>{indicator.category}</small>
              <p>{indicator.details}</p>
            </div>
          </article>
        ))}
        {evidence.map((item, index) => (
          <article className="scam-evidence-item" key={`evidence-${index}`}>
            <span className="scam-evidence-marker is-muted" />
            <div className="scam-evidence-copy">
              <div className="scam-evidence-heading"><strong>Supporting evidence</strong><span>{evidenceLabels[item.evidenceType] ?? "Unverified"}</span></div>
              <p>{item.description}</p>
              {item.sourceName && <small>{item.sourceName}{item.retrievedAt ? ` · ${new Date(item.retrievedAt).toLocaleString()}` : ""}</small>}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
