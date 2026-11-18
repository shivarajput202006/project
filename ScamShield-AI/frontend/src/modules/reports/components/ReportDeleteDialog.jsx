import { AlertTriangle, X } from "lucide-react";

export default function ReportDeleteDialog({ report, busy, onCancel, onConfirm }) {
  if (!report) return null;
  return (
    <div className="reports-dialog-backdrop" role="presentation" onMouseDown={(event) => {
      if (event.target === event.currentTarget && !busy) onCancel();
    }}>
      <section className="reports-delete-dialog" role="alertdialog" aria-modal="true"
        aria-labelledby="reports-delete-title" aria-describedby="reports-delete-copy">
        <button className="reports-dialog-close" type="button" aria-label="Close dialog" onClick={onCancel} disabled={busy}>
          <X size={17} />
        </button>
        <span className="reports-dialog-icon"><AlertTriangle size={21} /></span>
        <h2 id="reports-delete-title">Remove this history entry?</h2>
        <p id="reports-delete-copy">“{report.title}” will be hidden from your reports history. The original analysis in its module will not be deleted.</p>
        <div className="reports-dialog-actions">
          <button className="reports-secondary-button" type="button" onClick={onCancel} disabled={busy}>Keep report</button>
          <button className="reports-danger-button" type="button" onClick={onConfirm} disabled={busy}>
            {busy ? "Removing…" : "Remove history entry"}
          </button>
        </div>
      </section>
    </div>
  );
}
