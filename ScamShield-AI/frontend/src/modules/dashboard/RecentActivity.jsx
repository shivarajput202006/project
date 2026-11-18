import { Clock3 } from "lucide-react";
import ActivityItem from "./ActivityItem.jsx";

export default function RecentActivity({ activities = [] }) {
  return (
    <section className="dashboard-panel dashboard-activity-panel">
      <div className="dashboard-panel-heading">
        <div><span className="dashboard-eyebrow">YOUR ACCOUNT</span><h2>Recent activity</h2></div>
        <span className="dashboard-panel-icon"><Clock3 size={17} /></span>
      </div>
      {activities.length ? (
        <div className="dashboard-activity-list">
          {activities.map((activity) => <ActivityItem key={activity.id} activity={activity} />)}
        </div>
      ) : (
        <div className="dashboard-empty-state">
          <span className="dashboard-empty-icon"><Clock3 size={19} /></span>
          <strong>No activity yet</strong>
          <p>Your scam checks, URL scans, saved jobs, and resume activity will appear here.</p>
        </div>
      )}
    </section>
  );
}
