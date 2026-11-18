import { ArrowUpRight } from "lucide-react";
import { Link } from "react-router-dom";

export default function QuickActionCard({ title, description, path, icon: Icon, tone, comingSoon = true }) {
  return (
    <Link className="dashboard-action-card" to={path}>
      <span className={`dashboard-action-icon tone-${tone}`}><Icon size={19} /></span>
      <span className="dashboard-action-copy">
        <strong>{title}</strong>
        <small>{description}</small>
        {comingSoon ? (
          <span className="dashboard-coming-soon">Coming soon</span>
        ) : (
          <span className="dashboard-coming-soon">Available now</span>
        )}
      </span>
      <ArrowUpRight className="dashboard-action-arrow" size={16} />
    </Link>
  );
}
