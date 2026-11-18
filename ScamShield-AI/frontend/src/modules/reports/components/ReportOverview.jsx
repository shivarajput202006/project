import { Clock3, Files } from "lucide-react";
import { formatReportDate } from "../utils/reportHelpers.js";
import ReportStats from "./ReportStats.jsx";

export default function ReportOverview({ summary }) {
  return (
    <section className="reports-overview">
      <div className="reports-title-row">
        <div>
          <span className="reports-eyebrow"><Files size={14} /> PERSONAL ARCHIVE</span>
          <h1>Reports &amp; analysis history</h1>
          <p>Review saved results from your ScamShield tools in one place.</p>
        </div>
        <div className="reports-last-activity">
          <Clock3 size={15} />
          <span>Last activity <strong>{summary?.lastActivityAt ? formatReportDate(summary.lastActivityAt) : "—"}</strong></span>
        </div>
      </div>
      <ReportStats summary={summary} />
    </section>
  );
}
