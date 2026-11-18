import { BriefcaseBusiness, FileSearch, ScanSearch, ShieldAlert } from "lucide-react";
import QuickActionCard from "./QuickActionCard.jsx";

const actions = [
  {
    title: "Scan Suspicious Job Offer",
    description: "Review a job offer for warning signs.",
    path: "/scam-detector",
    icon: ShieldAlert,
    tone: "blue",
    comingSoon: false,
  },
  {
    title: "Check Suspicious URL",
    description: "Check a link before you open it.",
    path: "/url-scanner",
    icon: ScanSearch,
    tone: "cyan",
    comingSoon: false,
  },
  {
    title: "Explore Live Jobs",
    description: "Find your next career opportunity.",
    path: "/live-jobs",
    icon: BriefcaseBusiness,
    tone: "violet",
    comingSoon: false,
  },
  {
    title: "Analyze My Resume",
    description: "Get insights on your resume.",
    path: "/resume-analyzer",
    icon: FileSearch,
    tone: "amber",
    comingSoon: false,
  },
];

export default function QuickActions() {
  return (
    <section className="dashboard-section">
      <div className="dashboard-section-heading">
        <div><span className="dashboard-eyebrow">GET STARTED</span><h2>Quick actions</h2></div>
      </div>
      <div className="dashboard-actions-grid">
        {actions.map((action) => <QuickActionCard key={action.path} {...action} />)}
      </div>
    </section>
  );
}
