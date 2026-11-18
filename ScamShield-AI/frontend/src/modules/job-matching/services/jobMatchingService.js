const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class JobMatchingApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "JobMatchingApiError";
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
    throw new JobMatchingApiError(data.message ?? "A secure connection could not be established.", response.status);
  }
  return data.token;
}

async function request(path, { method = "GET", body, timeout = 60000 } = {}) {
  try {
    const headers = { Accept: "application/json" };
    if (method !== "GET") headers["X-XSRF-TOKEN"] = await csrfToken();
    if (body !== undefined) headers["Content-Type"] = "application/json";
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: "include",
      signal: AbortSignal.timeout(timeout),
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new JobMatchingApiError(
        data.message ?? "Job matching could not be completed. Please try again.",
        response.status,
      );
    }
    return data;
  } catch (error) {
    if (error instanceof JobMatchingApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new JobMatchingApiError("Live job search timed out. Please retry.", 0);
    }
    throw new JobMatchingApiError("The job-matching service could not be reached.", 0);
  }
}

function validateSearch(value) {
  if (!value || !Array.isArray(value.matches) || typeof value.available !== "boolean") {
    throw new JobMatchingApiError("The matching service returned an invalid response.", 502);
  }
  for (const match of value.matches) {
    if (!match?.job || typeof match.job.title !== "string" || !match.job.sourceName) {
      throw new JobMatchingApiError("The matching service returned an incomplete job listing.", 502);
    }
  }
  return value;
}

export const jobMatchingService = {
  analyze: async (filters) => validateSearch(await request("/api/job-matching/analyze", {
    method: "POST",
    body: filters,
  })),
  recommendations: async (filters = {}) => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") params.set(key, value);
    });
    return validateSearch(await request(`/api/job-matching/recommendations?${params}`));
  },
  refresh: async (filters) => validateSearch(await request("/api/job-matching/refresh", {
    method: "POST",
    body: filters,
  })),
  details: (jobId) => request(`/api/job-matching/${encodeURIComponent(jobId)}`),
  history: async (resumeId) => {
    const params = new URLSearchParams();
    if (resumeId) params.set("resumeId", resumeId);
    const result = await request(`/api/job-matching/history${params.size ? `?${params}` : ""}`);
    if (!Array.isArray(result)) throw new JobMatchingApiError("Match history could not be validated.", 502);
    return result;
  },
  deleteHistory: (id) => request(`/api/job-matching/history/${encodeURIComponent(id)}`, { method: "DELETE" }),
};
