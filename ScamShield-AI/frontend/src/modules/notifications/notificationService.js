const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class NotificationApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "NotificationApiError";
    this.status = status;
  }
}

async function csrfToken() {
  const response = await fetch(`${API_BASE_URL}/api/auth/csrf`, {
    credentials: "include",
    headers: { Accept: "application/json" },
    signal: AbortSignal.timeout(10000),
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok || !data.token) {
    throw new NotificationApiError(data.message ?? "A secure connection could not be established.", response.status);
  }
  return data.token;
}

async function request(path, { method = "GET", body, signal } = {}) {
  try {
    const headers = { Accept: "application/json" };
    if (method !== "GET") headers["X-XSRF-TOKEN"] = await csrfToken();
    if (body !== undefined) headers["Content-Type"] = "application/json";
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: "include",
      signal: signal ?? AbortSignal.timeout(12000),
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new NotificationApiError(data.message ?? "Notifications could not be loaded.", response.status);
    }
    return data;
  } catch (error) {
    if (error instanceof NotificationApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new NotificationApiError("The notification request timed out. Please try again.", 0);
    }
    throw new NotificationApiError("The notification service could not be reached.", 0);
  }
}

export const notificationService = {
  list: ({ filter = "ALL", page = 0, size = 20, signal } = {}) => {
    const params = new URLSearchParams({ filter, page: String(page), size: String(size) });
    return request(`/api/notifications?${params}`, { signal });
  },
  unreadCount: (signal) => request("/api/notifications/unread-count", { signal }),
  markRead: (id) => request(`/api/notifications/${encodeURIComponent(id)}/read`, { method: "PATCH" }),
  markUnread: (id) => request(`/api/notifications/${encodeURIComponent(id)}/unread`, { method: "PATCH" }),
  markAllRead: () => request("/api/notifications/read-all", { method: "PATCH" }),
  remove: (id) => request(`/api/notifications/${encodeURIComponent(id)}`, { method: "DELETE" }),
  clear: () => request("/api/notifications", { method: "DELETE" }),
  preferences: () => request("/api/notifications/preferences"),
  updatePreferences: (preferences) => request("/api/notifications/preferences", {
    method: "PUT",
    body: preferences,
  }),
};
