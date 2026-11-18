import { motion } from "framer-motion";
import { CalendarClock, RotateCcw, ShieldAlert } from "lucide-react";
import EvidenceList from "./EvidenceList.jsx";
import RiskScoreCard from "./RiskScoreCard.jsx";
import SafetyRecommendations from "./SafetyRecommendations.jsx";

export default function AnalysisResult({ report, onRetry }) {
  const analyzedAt = report.analysisTimestamp
    ? new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" })
        .format(new Date(report.analysisTimestamp))
    : "Time unavailable";

  return (
    <motion.section
      className="scam-report-card"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      aria-labelledby="scam-report-title"
    >
      <header className="scam-report-header">
        <div>
          <span className="dashboard-eyebrow">ANALYSIS REPORT</span>
          <h2 id="scam-report-title">Review results</h2>
          <p>{report.inputSummary}</p>
        </div>
        {onRetry && (
          <button className="scam-secondary-button" type="button" onClick={onRetry}>
            <RotateCcw size={14} /> Analyze again
          </button>
        )}
      </header>
      <RiskScoreCard status={report.riskStatus} score={report.riskScore} />
      <section className="scam-report-section">
        <div className="scam-report-section-heading"><span>Summary</span></div>
        <p className="scam-report-summary"><ShieldAlert size={17} />{report.summary}</p>
      </section>
      <EvidenceList indicators={report.indicators} evidence={report.evidence} />
      <SafetyRecommendations
        recommendations={report.recommendations}
        limitations={report.limitations}
        companyStatus={report.companyVerificationStatus}
      />
      <div className="scam-report-timestamp"><CalendarClock size={14} /> Analyzed {analyzedAt}</div>
      <p className="scam-report-disclaimer">{report.disclaimer}</p>
    </motion.section>
  );
}
