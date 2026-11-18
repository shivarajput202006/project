import { Download, LoaderCircle } from "lucide-react";

export default function ReportDownloadButton({ onClick, busy = false, compact = false }) {
  return (
    <button className={compact ? "reports-action-button is-compact" : "reports-action-button"}
      type="button" onClick={onClick} disabled={busy}>
      {busy ? <LoaderCircle className="reports-spin" size={15} /> : <Download size={15} />}
      {busy ? "Preparing…" : compact ? "PDF" : "Download PDF"}
    </button>
  );
}
