import { CheckCircle2 } from "lucide-react";

export default function SafetyRecommendations({ recommendations = [], limitations = [], companyStatus }) {
  return (
    <section className="scam-report-section scam-recommendations">
      <div className="scam-report-section-heading"><span>Recommended next steps</span></div>
      {recommendations.length ? (
        <ul className="scam-recommendation-list">
          {recommendations.map((recommendation, index) => (
            <li key={`${index}-${recommendation}`}><CheckCircle2 size={16} /><span>{recommendation}</span></li>
          ))}
        </ul>
      ) : (
        <p className="scam-report-empty">No additional steps were provided.</p>
      )}
      <div className="scam-verification-status">
        <strong>Company verification</strong>
        <span>{companyStatus === "UNVERIFIED" ? "Unverified — no trusted verification source is configured." : "Unverified"}</span>
      </div>
      {limitations.length > 0 && (
        <div className="scam-limitations">
          <strong>Limitations</strong>
          <ul>{limitations.map((limitation, index) => <li key={`${index}-${limitation}`}>{limitation}</li>)}</ul>
        </div>
      )}
    </section>
  );
}
