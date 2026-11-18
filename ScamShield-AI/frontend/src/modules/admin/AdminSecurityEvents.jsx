import { ShieldAlert } from "lucide-react";
import { formatAdminDate } from "./adminHelpers.js";

export default function AdminSecurityEvents({ events }) {
  return (
    <section className="admin-panel">
      <div className="admin-panel-heading"><div><span className="admin-eyebrow">SECURITY</span><h2>Recent security notifications</h2></div></div>
      {!events?.length ? <p className="admin-empty">No security notifications are recorded.</p> : (
        <ul className="admin-event-list">
          {events.map((event) => (
            <li key={event.id}>
              <span className="admin-event-icon"><ShieldAlert size={17} /></span>
              <span><strong>{event.type.replaceAll("_", " ")}</strong><small>{formatAdminDate(event.createdAt)}</small></span>
              <span className={`admin-priority priority-${event.priority.toLowerCase()}`}>{event.priority}</span>
            </li>
          ))}
        </ul>
      )}
      <p className="admin-disclaimer">This view contains security-type notifications only. A dedicated security incident audit log is not configured.</p>
    </section>
  );
}
