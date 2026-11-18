import { useCallback, useEffect, useRef, useState } from "react";
import { notificationService } from "./notificationService.js";

export default function useNotifications({ filter = "ALL", page = 0, size = 20, polling = true } = {}) {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [pageInfo, setPageInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const requestLock = useRef(false);
  const filterRef = useRef(filter);
  const pageRef = useRef(page);
  filterRef.current = filter;
  pageRef.current = page;

  const refresh = useCallback(async ({ showLoading = false } = {}) => {
    if (requestLock.current) return;
    requestLock.current = true;
    if (showLoading) setLoading(true);
    try {
      const [listData, countData] = await Promise.all([
        notificationService.list({ filter: filterRef.current, page: pageRef.current }),
        notificationService.unreadCount(),
      ]);
      if (!Array.isArray(listData?.content) || typeof countData?.unreadCount !== "number") {
        throw new Error("The notification service returned an invalid response.");
      }
      setNotifications(listData.content);
      setUnreadCount(countData.unreadCount);
      setPageInfo(listData);
      setError("");
    } catch (cause) {
      setError(cause.message);
    } finally {
      setLoading(false);
      requestLock.current = false;
    }
  }, []);

  useEffect(() => {
    refresh({ showLoading: true });
  }, [filter, page, refresh]);

  useEffect(() => {
    if (!polling) return undefined;
    const timer = window.setInterval(() => refresh(), 30000);
    return () => window.clearInterval(timer);
  }, [polling, refresh]);

  const markRead = useCallback(async (id) => {
    await notificationService.markRead(id);
    await refresh();
  }, [refresh]);

  const markUnread = useCallback(async (id) => {
    await notificationService.markUnread(id);
    await refresh();
  }, [refresh]);

  const remove = useCallback(async (id) => {
    await notificationService.remove(id);
    await refresh();
  }, [refresh]);

  const markAllRead = useCallback(async () => {
    await notificationService.markAllRead();
    await refresh();
  }, [refresh]);

  const clear = useCallback(async () => {
    await notificationService.clear();
    await refresh();
  }, [refresh]);

  return {
    notifications,
    unreadCount,
    pageInfo,
    loading,
    error,
    refresh,
    markRead,
    markUnread,
    remove,
    markAllRead,
    clear,
  };
}
