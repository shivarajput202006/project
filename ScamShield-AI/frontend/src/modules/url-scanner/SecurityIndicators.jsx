import { CircleAlert, Fingerprint } from "lucide-react";

export default function SecurityIndicators({ indicators }) {
  return (
    <section className="url-result-panel">
      <div className="url-panel-heading"><Fingerprint size={17} /><h3>Security indicators</h3></div>
      {indicators.length ? (
        <ul className="url-indicator-list">
          {indicators.map((indicator, index) => (
            <li key={`${indicator.code}-${index}`}>
              <span className={`url-indicator-severity severity-${String(indicator.severity).toLowerCase()}`}>
                <CircleAlert size={15} />
              </span>
              <div>
                <strong>{indicator.title}</strong>
                <p>{indicator.description}</p>
                {indicator.heuristic && <small>Heuristic · may be a false positive</small>}
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <p className="url-empty-inline">
          No configured heuristic matched this address. This is not a guarantee of safety.
        </p>
      )}
    </section>
  );
}
