import { useCallback, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Bell, RefreshCw, Trash2 } from "lucide-react";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import NotificationFilters from "./NotificationFilters.jsx";
import NotificationList from "./NotificationList.jsx";
import MarkAllReadButton from "./MarkAllReadButton.jsx";
import NotificationPreferences from "./NotificationPreferences.jsx";
import useNotifications from "./useNotifications.js";
import "../dashboard/dashboard.css";
import "./notifications.css";

export default function NotificationsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [filter, setFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [busyId, setBusyId] = useState("");
  const [actionError, setActionError] = useState("");
  const [actionMessage, setActionMessage] = useState("");
  const {
    notifications,
    unreadCount,
    pageInfo,
    loading,
    error,
    refresh,
    markRead,
    markUnread,
    remove,
    markAllRead,
    clear,
  } = useNotifications({ filter, page, size: 20 });

  const doAction = useCallback(async (id, action, successMessage) => {
    setBusyId(id || "all");
    setActionError("");
    setActionMessage("");
    try {
      await action(id);
      setActionMessage(successMessage);
    } catch (cause) {
      setActionError(cause.message);
    } finally {
      setBusyId("");
    }
  }, []);

  async function handleLogout() {
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (cause) {
      setActionError(cause.message);
    }
  }

  async function clearAll() {
    if (!window.confirm("Delete all notifications from your account? This cannot be undone.")) return;
    await doAction("", clear, "Notifications cleared.");
    setPage(0);
  }

  return (
    <div className="dashboard-shell">
      {mobileOpen && <button className="dashboard-backdrop" type="button" aria-label="Close navigation" onClick={() => setMobileOpen(false)} />}
      <DashboardSidebar isOpen={mobileOpen} onNavigate={() => setMobileOpen(false)} onLogout={handleLogout} />
      <div className="dashboard-workspace">
        <DashboardHeader user={user} title="Notifications" onMenuClick={() => setMobileOpen(true)} onLogout={handleLogout} />
        <main className="dashboard-main notifications-page">
          <div className="notifications-page-heading">
            <div>
              <span className="dashboard-eyebrow">ACCOUNT UPDATES</span>
              <h2>Your notifications</h2>
              <p>Security and activity updates from your ScamShield workspace.</p>
            </div>
            <div className="notifications-page-actions">
              <MarkAllReadButton
                onClick={() => doAction("", markAllRead, "All notifications marked as read.")}
                disabled={!unreadCount}
                busy={busyId === "all"}
              />
              <button type="button" className="notification-clear-button" onClick={clearAll} disabled={!pageInfo?.totalElements}>
                <Trash2 size={15} /> Clear all
              </button>
              <button type="button" className="notification-refresh-button" onClick={() => refresh({ showLoading: true })} aria-label="Refresh notifications">
                <RefreshCw size={15} />
              </button>
            </div>
          </div>
          <section className="notifications-panel">
            <div className="notifications-toolbar">
              <NotificationFilters
                filter={filter}
                onChange={(value) => {
                  setFilter(value);
                  setPage(0);
                  setActionError("");
                }}
              />
              <span className="notifications-unread-summary"><Bell size={14} /> {unreadCount} unread</span>
            </div>
            {error && !notifications.length && (
              <div className="notification-page-error" role="alert">
                <strong>Notifications unavailable</strong>
                <p>{error}</p>
                <button type="button" onClick={() => refresh({ showLoading: true })}>Try again</button>
              </div>
            )}
            {actionError && <p className="notification-action-message is-error" role="alert">{actionError}</p>}
            {actionMessage && <p className="notification-action-message" role="status">{actionMessage}</p>}
            <NotificationList
              notifications={notifications}
              loading={loading}
              filter={filter}
              busyId={busyId}
              onRead={(id) => doAction(id, markRead, "Notification marked as read.")}
              onUnread={(id) => doAction(id, markUnread, "Notification marked as unread.")}
              onDelete={(id) => doAction(id, remove, "Notification deleted.")}
            />
            {pageInfo?.totalPages > 1 && (
              <nav className="notification-pagination" aria-label="Notification pages">
                <button type="button" disabled={page === 0 || loading} onClick={() => setPage((value) => value - 1)}>Previous</button>
                <span>Page {page + 1} of {pageInfo.totalPages}</span>
                <button type="button" disabled={page + 1 >= pageInfo.totalPages || loading} onClick={() => setPage((value) => value + 1)}>Next</button>
              </nav>
            )}
          </section>
          <NotificationPreferences />
          <Link className="notifications-back-link" to="/dashboard">Back to dashboard</Link>
          <footer className="dashboard-footer">ScamShield AI · Your career safety workspace</footer>
        </main>
      </div>
    </div>
  );
}
