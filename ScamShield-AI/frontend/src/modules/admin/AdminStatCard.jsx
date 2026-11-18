import { Activity } from "lucide-react";
import { formatAdminNumber } from "./adminHelpers.js";

export default function AdminStatCard({ label, value, note, icon: Icon = Activity }) {
  return (
    <article className="admin-stat-card">
      <span className="admin-stat-icon"><Icon size={18} aria-hidden="true" /></span>
      <span className="admin-stat-label">{label}</span>
      <strong className="admin-stat-value">{formatAdminNumber(value)}</strong>
      <span className="admin-stat-note">{note}</span>
    </article>
  );
}
