import { Check, Trash2 } from "lucide-react";

export default function NotificationActions({ notification, onRead, onUnread, onDelete, busy }) {
  return (
    <div className="notification-item-actions" aria-label="Notification actions">
      {notification.isRead ? (
        <button type="button" aria-label="Mark as unread" title="Mark as unread" disabled={busy} onClick={onUnread}>
          <Check size={15} />
        </button>
      ) : (
        <button type="button" aria-label="Mark as read" title="Mark as read" disabled={busy} onClick={onRead}>
          <Check size={15} />
        </button>
      )}
      <button type="button" aria-label="Delete notification" title="Delete" disabled={busy} onClick={onDelete}>
        <Trash2 size={15} />
      </button>
    </div>
  );
}
