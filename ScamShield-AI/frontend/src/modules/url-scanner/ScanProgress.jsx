import { LoaderCircle, ShieldCheck } from "lucide-react";

export default function ScanProgress() {
  return (
    <div className="url-scan-progress" role="status" aria-live="polite">
      <span className="url-scanner-icon"><LoaderCircle size={22} className="url-scan-spin" /></span>
      <strong>Checking URL signals</strong>
      <p>Validating the address and checking the configured reputation provider. The site itself is not opened.</p>
      <div className="url-scan-progress-track"><span /></div>
      <small><ShieldCheck size={14} /> Safe, non-browsing check in progress</small>
    </div>
  );
}
