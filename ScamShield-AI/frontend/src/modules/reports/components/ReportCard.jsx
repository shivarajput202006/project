import { ArrowUpRight, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";
import ReportDownloadButton from "./ReportDownloadButton.jsx";
import ReportStatusBadge from "./ReportStatusBadge.jsx";
import { formatReportDate, reportTypeLabel } from "../utils/reportHelpers.js";

export default function ReportCard({ report, onDownload, onDelete, downloadPending }) {
  return (
    <article className="reports-card">
      <div className="reports-card-top">
        <span className={`reports-type-icon reports-type-${report.type?.toLowerCase()}`}>{reportTypeLabel(report.type)}</span>
        <ReportStatusBadge status={report.status} />
      </div>
      <Link className="reports-card-title" to={`/reports/${encodeURIComponent(report.id)}`}>
        <h2>{report.title}</h2><ArrowUpRight size={16} aria-hidden="true" />
      </Link>
      <p className="reports-card-summary">{report.summary}</p>
      <div className="reports-card-meta">
        <span>{formatReportDate(report.createdAt)}</span>
        {report.score !== null && report.score !== undefined && (
          <span className="reports-score">{report.scoreLabel}: <strong>{report.score}/100</strong></span>
        )}
      </div>
      <div className="reports-card-actions">
        <Link className="reports-text-link" to={`/reports/${encodeURIComponent(report.id)}`}>View details</Link>
        <ReportDownloadButton compact busy={downloadPending} onClick={() => onDownload(report)} />
        <button className="reports-delete-button" type="button" onClick={() => onDelete(report)} aria-label={`Remove ${report.title} from report history`}>
          <Trash2 size={15} />
        </button>
      </div>
    </article>
  );
}
