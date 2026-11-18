import { ShieldAlert, ShieldCheck, ShieldQuestion } from "lucide-react";

const riskLabels = {
  LOW_RISK: {
    label: "Fewer warning signs detected",
    description: "This does not establish that the opportunity or company is genuine.",
    icon: ShieldCheck,
    tone: "low",
  },
  NEEDS_REVIEW: {
    label: "Review carefully",
    description: "Some details may need independent checking before you proceed.",
    icon: ShieldQuestion,
    tone: "review",
  },
  HIGH_RISK: {
    label: "Significant warning signs",
    description: "Avoid sharing sensitive information or paying fees until you verify independently.",
    icon: ShieldAlert,
    tone: "high",
  },
  UNVERIFIED: {
    label: "Unable to verify",
    description: "There is not enough reliable information to assess this opportunity confidently.",
    icon: ShieldQuestion,
    tone: "unverified",
  },
};

export default function RiskScoreCard({ status, score }) {
  const risk = riskLabels[status] ?? riskLabels.UNVERIFIED;
  const Icon = risk.icon;

  return (
    <section className={`scam-risk-card tone-${risk.tone}`}>
      <div className="scam-risk-copy">
        <span className="scam-risk-status"><Icon size={18} /> {risk.label}</span>
        <p>{risk.description}</p>
      </div>
      <div
        className="scam-risk-score"
        role="img"
        aria-label={`Advisory risk score ${score} out of 100, not a probability`}
        style={{ "--score": `${score}%` }}
      >
        <strong>{score}</strong><span> / 100</span>
      </div>
      <small className="scam-risk-caption">Advisory score · not a probability of fraud</small>
    </section>
  );
}
