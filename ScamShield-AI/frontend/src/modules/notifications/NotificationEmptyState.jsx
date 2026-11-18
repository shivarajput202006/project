import { Bell } from "lucide-react";

export default function NotificationEmptyState({ filter = "ALL" }) {
  const unread = filter === "UNREAD";
  return (
    <div className="notification-empty-state">
      <span><Bell size={22} /></span>
      <h3>{unread ? "You're all caught up" : "No notifications yet"}</h3>
      <p>{unread ? "There are no unread alerts right now." : "Updates from your ScamShield workspace will appear here."}</p>
    </div>
  );
}
