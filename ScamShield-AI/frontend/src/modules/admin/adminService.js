const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export class AdminApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "AdminApiError";
    this.status = status;
  }
}

async function get(path) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: "include",
    headers: { Accept: "application/json" },
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new AdminApiError(
      data.message ?? "The admin service could not complete this request.",
      response.status,
    );
  }
  return data;
}

export const adminService = {
  getOverview: () => get("/api/admin/dashboard/overview"),
  getUsers: ({ search = "", page = 0, size = 20 } = {}) => {
    const params = new URLSearchParams({ search, page: String(page), size: String(size) });
    return get(`/api/admin/users?${params}`);
  },
  getSecurityEvents: () => get("/api/admin/monitoring/security-events"),
  getHealth: () => get("/api/admin/system/health"),
};
