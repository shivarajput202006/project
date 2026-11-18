export default function NotificationSkeleton({ compact = false }) {
  return (
    <div className={`notification-skeleton-list${compact ? " is-compact" : ""}`} role="status" aria-label="Loading notifications">
      {Array.from({ length: compact ? 3 : 6 }, (_, index) => (
        <div className="notification-skeleton-item" key={index}>
          <span />
          <div><i /><i /><i /></div>
        </div>
      ))}
    </div>
  );
}
