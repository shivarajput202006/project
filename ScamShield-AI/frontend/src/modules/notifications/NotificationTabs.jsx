const TABS = [
  ["ALL", "All"],
  ["UNREAD", "Unread"],
  ["SECURITY", "Security"],
  ["JOBS", "Jobs"],
  ["SYSTEM", "System"],
];

export default function NotificationTabs({ value, onChange }) {
  return (
    <div className="notification-tabs" role="tablist" aria-label="Filter notifications">
      {TABS.map(([key, label]) => (
        <button
          key={key}
          type="button"
          role="tab"
          aria-selected={value === key}
          className={value === key ? "is-active" : ""}
          onClick={() => onChange(key)}
        >
          {label}
        </button>
      ))}
    </div>
  );
}
