import {
  Activity,
  Bookmark,
  FileText,
  ScanSearch,
  ShieldAlert,
} from "lucide-react";
import StatCard from "./StatCard.jsx";

const cards = [
  { key: "totalScamAnalyses", label: "Total Scam Analyses", icon: ShieldAlert, tone: "blue" },
  { key: "urlsScanned", label: "URLs Scanned", icon: ScanSearch, tone: "cyan" },
  { key: "savedJobs", label: "Saved Jobs", icon: Bookmark, tone: "violet" },
  { key: "resumeAnalyses", label: "Resume Analyses", icon: FileText, tone: "amber" },
  { key: "reportsSubmitted", label: "Reports Submitted", icon: Activity, tone: "rose" },
];

export default function StatsCards({ stats }) {
  return (
    <section className="dashboard-stats-grid" aria-label="Your statistics">
      {cards.map(({ key, ...card }) => (
        <StatCard
          key={key}
          {...card}
          value={typeof stats?.[key] === "number" ? stats[key] : "Unavailable"}
        />
      ))}
    </section>
  );
}
