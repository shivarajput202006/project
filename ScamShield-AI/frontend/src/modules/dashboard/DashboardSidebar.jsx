import {
  Activity,
  Bookmark,
  BriefcaseBusiness,
  ChartNoAxesCombined,
  FileSearch,
  LayoutDashboard,
  LogOut,
  MessageCircle,
  ScanSearch,
  ShieldAlert,
  ShieldCheck,
  Settings2,
  UserRound,
  Bell,
} from "lucide-react";
import { Link, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";

const navigation = [
  { label: "Dashboard", path: "/dashboard", icon: LayoutDashboard, comingSoon: false },
  { label: "AI Scam Detector", path: "/scam-detector", icon: ShieldAlert, comingSoon: false },
  { label: "URL Scanner", path: "/url-scanner", icon: ScanSearch, comingSoon: false },
  { label: "Live Job Finder", path: "/live-jobs", icon: BriefcaseBusiness, comingSoon: false },
  { label: "Resume Analyzer", path: "/resume-analyzer", icon: FileSearch, comingSoon: false },
  { label: "Job Matching", path: "/job-matching", icon: ChartNoAxesCombined, comingSoon: false },
  { label: "Reports & History", path: "/reports", icon: Activity, comingSoon: false },
  { label: "AI Career Assistant", path: "/dashboard/assistant", icon: MessageCircle, comingSoon: false },
  { label: "Saved Jobs", path: "/saved-jobs", icon: Bookmark, comingSoon: false },
  { label: "Notifications", path: "/notifications", icon: Bell, comingSoon: false },
  { label: "My Profile", path: "/profile", icon: UserRound },
  { label: "Settings", path: "/settings", icon: Settings2, comingSoon: false },
];

export default function DashboardSidebar({ isOpen, onNavigate, onLogout, logoutPending }) {
  const location = useLocation();
  const { user } = useAuth();
  const visibleNavigation = user?.role === "ADMIN"
    ? [...navigation.slice(0, -2), { label: "Admin Console", path: "/admin", icon: ShieldCheck, comingSoon: false }, ...navigation.slice(-2)]
    : navigation;

  return (
    <aside className={`dashboard-sidebar${isOpen ? " is-open" : ""}`}>
      <Link className="dashboard-brand" to="/dashboard" onClick={onNavigate}>
        <img src="/shield.svg" alt="" width="34" height="34" />
        <span>ScamShield <b>AI</b></span>
      </Link>
      <div className="dashboard-nav-label">WORKSPACE</div>
      <nav className="dashboard-nav" aria-label="Dashboard navigation">
        {visibleNavigation.map(({ label, path, icon: Icon, active: _active, comingSoon = true }) => (
          <Link
            key={path}
            className={`dashboard-nav-link${location.pathname === path ? " is-active" : ""}`}
            to={path}
            onClick={onNavigate}
          >
            <Icon size={17} aria-hidden="true" />
            <span>{label}</span>
            {comingSoon && <small>Coming soon</small>}
          </Link>
        ))}
      </nav>
      <div className="dashboard-sidebar-bottom">
        <div className="dashboard-security-note">
          <span><ShieldCheck size={16} /></span>
          <div><strong>Stay protected</strong><small>Check before you trust.</small></div>
        </div>
        <button
          className="dashboard-nav-link dashboard-logout-link"
          type="button"
          onClick={onLogout}
          disabled={logoutPending}
        >
          <LogOut size={17} aria-hidden="true" />
          <span>{logoutPending ? "Signing out…" : "Logout"}</span>
        </button>
      </div>
    </aside>
  );
}
