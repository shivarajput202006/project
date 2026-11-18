import { ShieldAlert, ShieldCheck, ShieldQuestion } from "lucide-react";

const riskPresentation = {
  LOW_RISK: {
    label: "No reputation match",
    description: "The configured provider did not return a match. This does not prove that the URL is safe.",
    icon: ShieldCheck,
    tone: "low",
  },
  NEEDS_REVIEW: {
    label: "Needs review",
    description: "One or more heuristic signals were found. Heuristics can be false positives and are not confirmed threats.",
    icon: ShieldAlert,
    tone: "review",
  },
  HIGH_RISK: {
    label: "Confirmed reputation match",
    description: "The configured reputation provider returned a threat match. Avoid opening this link or submitting information.",
    icon: ShieldAlert,
    tone: "high",
  },
  UNVERIFIED: {
    label: "Unverified",
    description: "There is not enough reliable reputation information to assess this URL.",
    icon: ShieldQuestion,
    tone: "unknown",
  },
};

export default function URLRiskCard({ result }) {
  const presentation = riskPresentation[result.riskStatus] ?? riskPresentation.UNVERIFIED;
  const Icon = presentation.icon;

  return (
    <section className={`url-risk-card tone-${presentation.tone}`} aria-label="URL risk summary">
      <div className="url-risk-topline">
        <span className="url-risk-symbol"><Icon size={22} /></span>
        <div>
          <span className="url-scanner-eyebrow">SCAN ASSESSMENT</span>
          <h2>{presentation.label}</h2>
        </div>
        <strong className="url-risk-score" aria-label={`Indicator score ${result.riskScore} out of 100`}>
          {result.riskScore}<small>/100</small>
        </strong>
      </div>
      <p>{presentation.description}</p>
      <small className="url-score-disclaimer">Heuristic indicator score, not a probability or proof of safety.</small>
    </section>
  );
}
