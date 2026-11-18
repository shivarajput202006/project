import { LogOut, Menu, RefreshCw, ShieldCheck } from "lucide-react";

export default function AdminHeader({ onMenu, onRefresh, onLogout, refreshing, user }) {
  return (
    <header className="admin-header">
      <button className="admin-mobile-menu" type="button" aria-label="Open admin navigation" onClick={onMenu}>
        <Menu size={19} />
      </button>
      <div>
        <span className="admin-eyebrow">PLATFORM MANAGEMENT</span>
        <h1>Admin Dashboard</h1>
      </div>
      <div className="admin-header-actions">
        <span className="admin-identity"><ShieldCheck size={16} />{user?.fullName ?? "Administrator"}</span>
        <button className="admin-icon-button" type="button" onClick={onRefresh} disabled={refreshing} aria-label="Refresh dashboard">
          <RefreshCw size={16} className={refreshing ? "is-spinning" : ""} />
        </button>
        <button className="admin-icon-button" type="button" onClick={onLogout} aria-label="Log out">
          <LogOut size={16} />
        </button>
      </div>
    </header>
  );
}
