import { ChevronDown, Menu, Search, Settings2, UserRound } from "lucide-react";
import { Link } from "react-router-dom";
import NotificationBell from "../notifications/NotificationBell.jsx";

export default function DashboardHeader({
  user,
  title = "Dashboard",
  _notificationCount,
  onMenuClick,
  onLogout,
  logoutPending,
}) {
  const initials = user?.fullName
    ?.split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join("") || "U";

  return (
    <header className="dashboard-topbar">
      <button
        className="dashboard-mobile-menu"
        type="button"
        aria-label="Open navigation"
        onClick={onMenuClick}
      >
        <Menu size={20} />
      </button>
      <div className="dashboard-page-heading">
        <span>ScamShield AI</span>
        <h1>{title}</h1>
      </div>
      <div className="dashboard-topbar-actions">
        <label className="dashboard-search">
          <Search size={16} aria-hidden="true" />
          <input type="search" aria-label="Dashboard search" placeholder="Search coming soon" disabled />
        </label>
        <NotificationBell />
        <details className="dashboard-profile-menu">
          <summary aria-label="Open profile menu">
            <span className="dashboard-avatar">{initials}</span>
            <span className="dashboard-profile-name">{user?.fullName ?? "ScamShield member"}</span>
            <ChevronDown size={15} />
          </summary>
          <div className="dashboard-profile-dropdown">
            <div className="dashboard-profile-identity">
              <strong>{user?.fullName ?? "ScamShield member"}</strong>
              <span>{user?.email}</span>
            </div>
            <Link to="/profile"><UserRound size={15} /> My Profile</Link>
            <Link to="/settings"><Settings2 size={15} /> Settings</Link>
            <button type="button" onClick={onLogout} disabled={logoutPending}>
              {logoutPending ? "Signing out…" : "Log out"}
            </button>
          </div>
        </details>
      </div>
    </header>
  );
}
