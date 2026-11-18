import { Bell } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import useNotifications from "./useNotifications.js";
import NotificationDropdown from "./NotificationDropdown.jsx";
import "./notifications.css";

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [actionError, setActionError] = useState("");
  const containerRef = useRef(null);
  const {
    notifications,
    unreadCount,
    loading,
    error,
    refresh,
    markRead,
    markUnread,
    remove,
  } = useNotifications({ size: 10, polling: true });

  useEffect(() => {
    if (!open) return undefined;
    refresh({ showLoading: true });
    const handlePointer = (event) => {
      if (!containerRef.current?.contains(event.target)) setOpen(false);
    };
    const handleKey = (event) => {
      if (event.key === "Escape") setOpen(false);
    };
    document.addEventListener("pointerdown", handlePointer);
    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("pointerdown", handlePointer);
      document.removeEventListener("keydown", handleKey);
    };
  }, [open, refresh]);

  async function perform(action, id) {
    setActionError("");
    try {
      await action(id);
    } catch (cause) {
      setActionError(cause.message);
    }
  }

  return (
    <div className="notification-bell-container" ref={containerRef}>
      <button
        className="dashboard-icon-button notification-bell-button"
        type="button"
        aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ""}`}
        aria-expanded={open}
        aria-haspopup="dialog"
        onClick={() => setOpen((value) => !value)}
      >
        <Bell size={18} />
        {unreadCount > 0 && (
          <span className="notification-unread-badge" aria-label={`${unreadCount} unread`}>
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>
      {open && (
        <NotificationDropdown
          notifications={notifications}
          loading={loading}
          error={actionError || error}
          unreadCount={unreadCount}
          onRead={(id) => perform(markRead, id)}
          onUnread={(id) => perform(markUnread, id)}
          onDelete={(id) => perform(remove, id)}
          onClose={() => setOpen(false)}
        />
      )}
    </div>
  );
}
