import { ExternalLink, ShieldCheck } from "lucide-react";
import DomainInformation from "./DomainInformation.jsx";
import RedirectDetails from "./RedirectDetails.jsx";
import SecurityIndicators from "./SecurityIndicators.jsx";
import URLRecommendations from "./URLRecommendations.jsx";
import URLRiskCard from "./URLRiskCard.jsx";

function scanTime(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "Unavailable"
    : new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}

function reputationStatus(result) {
  const finding = result.reputationFinding;
  if (finding.status === "CONFIRMED_MATCH") return "Threat match reported";
  if (finding.status === "NO_MATCH") return "No match returned";
  if (finding.status === "RATE_LIMITED") return "Provider quota limited";
  if (finding.status === "NOT_CONFIGURED") return "Provider not configured";
  return "Provider unavailable";
}

export default function URLResultPage({ result }) {
  return (
    <article className="url-result">
      <header className="url-result-heading">
        <div><span className="url-scanner-eyebrow">URL SCAN REPORT</span><h2>Assessment details</h2></div>
        <span className="url-result-time"><ShieldCheck size={14} /> Scanned {scanTime(result.scanTimestamp)}</span>
      </header>
      <div className="url-submitted-address">
        <span>Submitted address</span>
        <bdi>{result.submittedUrl}</bdi>
      </div>
      <URLRiskCard result={result} />
      <DomainInformation result={result} />
      <section className="url-result-panel">
        <div className="url-panel-heading"><ExternalLink size={17} /><h3>Reputation findings</h3></div>
        <div className={`url-reputation-status rep-${String(result.reputationFinding.status).toLowerCase()}`}>
          <strong>{reputationStatus(result)}</strong>
          {result.reputationFinding.retrievedAt && (
            <time dateTime={result.reputationFinding.retrievedAt}>
              {scanTime(result.reputationFinding.retrievedAt)}
            </time>
          )}
        </div>
        <p className="url-panel-note">{result.reputationFinding.description}</p>
        {result.reputationFinding.threatTypes?.length > 0 && (
          <p className="url-threat-types">Reported categories: {result.reputationFinding.threatTypes.join(", ")}</p>
        )}
        <p className="url-panel-note">
          A provider match is a reported threat signal. A missing match does not establish that a URL is safe.
        </p>
      </section>
      <SecurityIndicators indicators={result.securityIndicators} />
      <RedirectDetails summary={result.redirectSummary} />
      <URLRecommendations recommendations={result.recommendations} />
    </article>
  );
}
