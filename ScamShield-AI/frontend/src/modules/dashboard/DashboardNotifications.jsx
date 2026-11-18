import { ArrowUpRight, Bell } from "lucide-react";
import { Link } from "react-router-dom";

export default function DashboardNotifications({ notifications = [] }) {
  return (
    <section className="dashboard-panel">
      <div className="dashboard-panel-heading">
        <div><span className="dashboard-eyebrow">UPDATES</span><h2>Notifications</h2></div>
        <Link className="dashboard-panel-link" to="/notifications" aria-label="View notifications">
          <ArrowUpRight size={16} />
        </Link>
      </div>
      {notifications.length ? (
        <div className="dashboard-notification-list">
          {notifications.map((notification) => (
            <article className="dashboard-notification-item" key={notification.id}>
              <span className="dashboard-notification-icon"><Bell size={15} /></span>
              <div><strong>{notification.title}</strong><p>{notification.message}</p></div>
            </article>
          ))}
        </div>
      ) : (
        <div className="dashboard-empty-state dashboard-empty-state-compact">
          <span className="dashboard-empty-icon"><Bell size={18} /></span>
          <strong>You're all caught up</strong>
          <p>New notifications will appear here.</p>
        </div>
      )}
    </section>
  );
}
