import { FileText } from "lucide-react";
import ReportStatusBadge from "./ReportStatusBadge.jsx";
import { formatReportDate, reportTypeLabel } from "../utils/reportHelpers.js";

export default function ReportDetails({ report }) {
  return (
    <article className="reports-detail-panel">
      <div className="reports-card-top">
        <span className={`reports-type-icon reports-type-${report.type?.toLowerCase()}`}>{reportTypeLabel(report.type)}</span>
        <ReportStatusBadge status={report.status} />
      </div>
      <h1>{report.title}</h1>
      <p className="reports-detail-summary">{report.summary}</p>
      <div className="reports-detail-meta">
        <span><strong>Created</strong>{formatReportDate(report.createdAt)}</span>
        <span><strong>Source module</strong>{report.sourceModule}</span>
        <span><strong>Report ID</strong><code>{report.id}</code></span>
        {report.score !== null && report.score !== undefined && (
          <span><strong>{report.scoreLabel}</strong>{report.score}/100</span>
        )}
      </div>
      <div className="reports-detail-sections">
        {report.sections.map((section, index) => (
          <section className="reports-detail-section" key={`${section.title}-${index}`}>
            <h2><FileText size={15} />{section.title}</h2>
            {section.items.length
              ? <ul>{section.items.map((item, itemIndex) => <li key={`${itemIndex}-${item}`}>{item}</li>)}</ul>
              : <p>No details were stored for this section.</p>}
          </section>
        ))}
      </div>
    </article>
  );
}
