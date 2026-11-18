const INTERNAL_DESTINATIONS = new Set([
  "/dashboard",
  "/scam-detector",
  "/url-scanner",
  "/live-jobs",
  "/resume-analyzer",
  "/job-matching",
  "/reports",
  "/profile",
  "/settings",
  "/notifications",
]);

export function safeNotificationDestination(value) {
  return typeof value === "string" && INTERNAL_DESTINATIONS.has(value) ? value : "/notifications";
}

export function formatNotificationTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Time unavailable";
  const elapsed = Math.max(0, Date.now() - date.getTime());
  if (elapsed < 60_000) return "Just now";
  if (elapsed < 3_600_000) return `${Math.floor(elapsed / 60_000)}m ago`;
  if (elapsed < 86_400_000) return `${Math.floor(elapsed / 3_600_000)}h ago`;
  return new Intl.DateTimeFormat(undefined, { dateStyle: "medium" }).format(date);
}

export function normalizeNotificationFilter(filter) {
  return ["ALL", "UNREAD", "SECURITY", "JOBS", "SYSTEM"].includes(filter) ? filter : "ALL";
}
