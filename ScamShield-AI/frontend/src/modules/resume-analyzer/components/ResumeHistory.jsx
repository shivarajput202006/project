import { FileText, LoaderCircle, Trash2 } from "lucide-react";

export default function ResumeHistory({ items, selectedId, loading, error, onSelect, onDelete }) {
  return (
    <section className="resume-history resume-card" aria-labelledby="resume-history-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">YOUR FILES</span><h2 id="resume-history-title">Resume history</h2></div></div>
      {loading && <p className="resume-muted"><LoaderCircle size={15} className="resume-spin" /> Loading history…</p>}
      {error && <p className="resume-inline-error" role="alert">{error}</p>}
      {!loading && !error && !items.length && <p className="resume-muted">Uploaded resumes will appear here.</p>}
      {items.length > 0 && <ul>
        {items.map((item) => (
          <li className={selectedId === item.id ? "is-selected" : ""} key={item.id}>
            <button type="button" className="resume-history-select" onClick={() => onSelect(item)}>
              <FileText size={17} aria-hidden="true" />
              <span><strong>{item.originalFilename}</strong><small>
                {item.latestAnalysisAt
                  ? `Analyzed ${new Date(item.latestAnalysisAt).toLocaleDateString()}`
                  : "Not analyzed yet"}
                {Number.isInteger(item.latestScore) ? ` · Advisory score ${item.latestScore}/100` : ""}
              </small></span>
            </button>
            <button type="button" className="resume-history-delete" aria-label={`Delete ${item.originalFilename}`} onClick={() => onDelete(item)}>
              <Trash2 size={15} aria-hidden="true" />
            </button>
          </li>
        ))}
      </ul>}
    </section>
  );
}
