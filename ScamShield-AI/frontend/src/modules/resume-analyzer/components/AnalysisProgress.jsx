import { LoaderCircle } from "lucide-react";

export default function AnalysisProgress({ stage }) {
  if (!stage) return null;
  return (
    <div className="resume-progress" role="status" aria-live="polite">
      <LoaderCircle size={19} className="resume-spin" aria-hidden="true" />
      <div><strong>{stage}</strong><span>Keep this page open while the analysis is in progress.</span></div>
    </div>
  );
}
