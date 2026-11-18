import NotificationItem from "./NotificationItem.jsx";
import NotificationEmptyState from "./NotificationEmptyState.jsx";
import NotificationSkeleton from "./NotificationSkeleton.jsx";

export default function NotificationList({
  notifications,
  loading,
  filter,
  onRead,
  onUnread,
  onDelete,
  busyId,
}) {
  if (loading) return <NotificationSkeleton />;
  if (!notifications.length) return <NotificationEmptyState filter={filter} />;
  return (
    <div className="notifications-list">
      {notifications.map((notification) => (
        <NotificationItem
          key={notification.id}
          notification={notification}
          busy={busyId === notification.id}
          onRead={() => onRead(notification.id)}
          onUnread={() => onUnread(notification.id)}
          onDelete={() => onDelete(notification.id)}
        />
      ))}
    </div>
  );
}
