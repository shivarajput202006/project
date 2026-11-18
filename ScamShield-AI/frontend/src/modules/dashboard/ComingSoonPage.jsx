import { ArrowLeft, Clock3 } from "lucide-react";
import { Link, useLocation } from "react-router-dom";

const pageNames = {
  "/scam-detector": "AI Scam Detector",
  "/url-scanner": "URL Scanner",
  "/live-jobs": "Live Job Finder",
  "/resume-analyzer": "Resume Analyzer",
  "/scam-reports": "Scam Reports",
  "/safety-assistant": "AI Safety Assistant",
  "/saved-jobs": "Saved Jobs",
  "/notifications": "Notifications",
  "/settings": "Settings",
};

export default function ComingSoonPage() {
  const { pathname } = useLocation();
  const pageName = pageNames[pathname] ?? "This feature";

  return (
    <main className="dashboard-coming-page">
      <div className="dashboard-coming-card">
        <span className="dashboard-coming-page-icon"><Clock3 size={23} /></span>
        <span className="dashboard-eyebrow">COMING SOON</span>
        <h1>{pageName}</h1>
        <p>This feature isn't available yet. Your dashboard only shows data from features that are implemented.</p>
        <Link className="dashboard-primary-button" to="/dashboard"><ArrowLeft size={16} /> Back to dashboard</Link>
      </div>
    </main>
  );
}
