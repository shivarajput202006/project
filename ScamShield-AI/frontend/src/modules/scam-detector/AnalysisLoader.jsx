import { LoaderCircle, ShieldCheck } from "lucide-react";

export default function AnalysisLoader() {
  return (
    <div className="scam-analysis-loader" role="status">
      <span className="scam-loader-icon"><ShieldCheck size={23} /></span>
      <strong>Reviewing the information</strong>
      <p>Checking for warning signs. This may take a few moments.</p>
      <LoaderCircle size={18} className="scam-spin" aria-hidden="true" />
    </div>
  );
}
