const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export class DashboardApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "DashboardApiError";
    this.status = status;
  }
}

export async function getDashboardOverview() {
  const response = await fetch(`${API_BASE_URL}/api/dashboard/overview`, {
    credentials: "include",
    headers: { Accept: "application/json" },
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new DashboardApiError(
      data.message ?? "We couldn't load your dashboard. Please try again.",
      response.status,
    );
  }
  if (
    !data?.user
    || !data?.stats
    || !Array.isArray(data.recentActivities)
    || !Array.isArray(data.savedJobs)
    || !Array.isArray(data.notifications)
  ) {
    throw new Error("The dashboard response was incomplete. Please try again.");
  }
  return data;
}
