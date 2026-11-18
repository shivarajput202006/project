import { Activity, Bookmark, FileSearch, ScanSearch, ShieldAlert } from "lucide-react";

const activityIcons = {
  SCAM_ANALYSIS: ShieldAlert,
  URL_SCAN: ScanSearch,
  SAVED_JOB: Bookmark,
  RESUME_ANALYSIS: FileSearch,
};

export default function ActivityItem({ activity }) {
  const Icon = activityIcons[activity.type] ?? Activity;
  const date = activity.createdAt
    ? new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" })
        .format(new Date(activity.createdAt))
    : "";

  return (
    <article className="dashboard-activity-item">
      <span className="dashboard-activity-icon"><Icon size={17} /></span>
      <div className="dashboard-activity-copy">
        <strong>{activity.title}</strong>
        <p>{activity.description}</p>
        {date && <time dateTime={activity.createdAt}>{date}</time>}
      </div>
      {activity.status && <span className="dashboard-activity-status">{activity.status}</span>}
    </article>
  );
}
