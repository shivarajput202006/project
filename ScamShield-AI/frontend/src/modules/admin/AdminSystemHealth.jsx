import { Activity, Database } from "lucide-react";
import { formatAdminDate } from "./adminHelpers.js";

export default function AdminSystemHealth({ health }) {
  return (
    <section className="admin-panel">
      <div className="admin-panel-heading"><div><span className="admin-eyebrow">OPERATIONS</span><h2>System health</h2></div></div>
      {!health ? <p className="admin-empty">System health data is unavailable.</p> : (
        <div className="admin-health-grid">
          <article><Database size={19} /><span>MongoDB</span><strong className={health.database === "UP" ? "health-up" : "health-down"}>{health.database}</strong></article>
          <article><Activity size={19} /><span>Process uptime</span><strong>{Math.floor(health.uptimeMillis / 1000).toLocaleString()} sec</strong></article>
          <p>Checked {formatAdminDate(health.checkedAt)}. This reports database connectivity and process uptime only; it is not a full infrastructure monitor.</p>
        </div>
      )}
    </section>
  );
}
