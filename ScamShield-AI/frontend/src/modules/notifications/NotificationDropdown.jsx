import { Link } from "react-router-dom";
import { Bell } from "lucide-react";
import NotificationList from "./NotificationList.jsx";

export default function NotificationDropdown({
  notifications,
  loading,
  error,
  unreadCount,
  onRead,
  onUnread,
  onDelete,
  onClose,
}) {
  return (
    <section className="notification-dropdown" aria-label="Recent notifications">
      <header className="notification-dropdown-header">
        <div><strong>Notifications</strong><span>{unreadCount} unread</span></div>
        <button type="button" className="notification-dropdown-close" onClick={onClose} aria-label="Close notifications">×</button>
      </header>
      {error && <p className="notification-inline-error" role="alert">{error}</p>}
      <NotificationList
        notifications={notifications.slice(0, 5)}
        loading={loading}
        onRead={onRead}
        onUnread={onUnread}
        onDelete={onDelete}
        busyId=""
      />
      {!loading && !notifications.length && !error && <Bell size={1} aria-hidden="true" className="notification-hidden-icon" />}
      <footer className="notification-dropdown-footer">
        <Link to="/notifications" onClick={onClose}>View all notifications</Link>
      </footer>
    </section>
  );
}
