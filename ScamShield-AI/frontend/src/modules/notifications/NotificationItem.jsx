import { Activity, BriefcaseBusiness, CheckCircle2, FileText, ShieldAlert, ShieldCheck, Settings2 } from "lucide-react";
import { Link } from "react-router-dom";
import NotificationActions from "./NotificationActions.jsx";
import { formatNotificationTime, safeNotificationDestination } from "./notificationHelpers.js";

const ICONS = {
  SECURITY: ShieldCheck,
  SCAM_ANALYSIS: ShieldAlert,
  URL_SCAN: ShieldAlert,
  JOB: BriefcaseBusiness,
  JOB_MATCH: BriefcaseBusiness,
  RESUME: FileText,
  REPORT: Activity,
  SYSTEM: Settings2,
  ADMIN: ShieldCheck,
};

export default function NotificationItem({ notification, onRead, onUnread, onDelete, busy = false }) {
  const Icon = ICONS[notification.type] ?? CheckCircle2;
  const destination = safeNotificationDestination(notification.actionUrl);

  return (
    <article className={`notification-item${notification.isRead ? " is-read" : " is-unread"}`}>
      <span className={`notification-item-icon type-${String(notification.type).toLowerCase()}`}>
        <Icon size={17} aria-hidden="true" />
      </span>
      <div className="notification-item-content">
        <div className="notification-item-heading">
          <Link to={destination} className="notification-item-title" onClick={() => !notification.isRead && onRead()}>
            {notification.title}
          </Link>
          {!notification.isRead && <span className="notification-unread-dot" aria-label="Unread" />}
        </div>
        <p>{notification.message}</p>
        <time dateTime={notification.createdAt}>{formatNotificationTime(notification.createdAt)}</time>
      </div>
      <NotificationActions
        notification={notification}
        busy={busy}
        onRead={onRead}
        onUnread={onUnread}
        onDelete={onDelete}
      />
    </article>
  );
}
