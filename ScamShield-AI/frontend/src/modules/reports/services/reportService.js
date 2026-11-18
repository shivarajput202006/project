const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class ReportApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "ReportApiError";
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
    throw new ReportApiError(data.message ?? "A secure connection could not be established.", response.status);
  }
  return data.token;
}

async function request(path, { method = "GET", headers = {}, responseType = "json" } = {}) {
  try {
    const requestHeaders = { Accept: responseType === "pdf" ? "application/pdf" : "application/json", ...headers };
    if (method !== "GET") requestHeaders["X-XSRF-TOKEN"] = await csrfToken();
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers: requestHeaders,
      credentials: "include",
      signal: AbortSignal.timeout(30000),
    });
    if (response.status === 204) return null;
    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      throw new ReportApiError(data.message ?? "The report request could not be completed.", response.status);
    }
    if (responseType === "pdf") {
      const disposition = response.headers.get("Content-Disposition") ?? "";
      const filename = disposition.match(/filename="?([^";]+)"?/i)?.[1] ?? "scamshield-report.pdf";
      return { blob: await response.blob(), filename };
    }
    return await response.json();
  } catch (error) {
    if (error instanceof ReportApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new ReportApiError("Loading report history timed out. Please retry.", 0);
    }
    throw new ReportApiError("The reports service could not be reached. Check your connection and retry.", 0);
  }
}

function assertPage(value) {
  if (!value || !Array.isArray(value.reports) || typeof value.totalElements !== "number") {
    throw new ReportApiError("The reports service returned an invalid response.", 502);
  }
  return value;
}

export const reportService = {
  list: async (filters = {}) => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") params.set(key, value);
    });
    return assertPage(await request(`/api/reports?${params}`));
  },
  summary: () => request("/api/reports/summary"),
  categories: () => request("/api/reports/categories"),
  details: (id) => request(`/api/reports/${encodeURIComponent(id)}`),
  download: (id) => request(`/api/reports/${encodeURIComponent(id)}/download`, { responseType: "pdf" }),
  deleteHistory: (id) => request(`/api/reports/${encodeURIComponent(id)}`, { method: "DELETE" }),
};
