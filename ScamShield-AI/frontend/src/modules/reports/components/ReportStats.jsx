import { Activity, BriefcaseBusiness, FileSearch, ScanSearch, ShieldAlert } from "lucide-react";

const stats = [
  ["scamDetections", "Scam checks", ShieldAlert],
  ["urlScans", "URL scans", ScanSearch],
  ["resumeAnalyses", "Resume analyses", FileSearch],
  ["jobMatches", "Job matches", BriefcaseBusiness],
];

export default function ReportStats({ summary }) {
  return (
    <div className="reports-stat-grid" aria-label="Report totals">
      <article className="reports-stat-card reports-stat-total">
        <span className="reports-stat-icon"><Activity size={17} /></span>
        <div><strong>{summary?.totalReports ?? "—"}</strong><span>Total reports</span></div>
      </article>
      {stats.map(([key, label, Icon]) => (
        <article className="reports-stat-card" key={key}>
          <span className="reports-stat-icon"><Icon size={17} /></span>
          <div><strong>{summary?.[key] ?? "—"}</strong><span>{label}</span></div>
        </article>
      ))}
    </div>
  );
}
