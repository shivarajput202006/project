import { toSearchParams } from "../utils/jobFilters.js";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class JobFinderApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "JobFinderApiError";
    this.status = status;
  }
}

async function getCsrfToken() {
  const response = await fetch(`${API_BASE_URL}/api/auth/csrf`, {
    credentials: "include",
    headers: { Accept: "application/json" },
    signal: AbortSignal.timeout(10000),
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok || !data.token) {
    throw new JobFinderApiError(
      data.message ?? "A secure connection could not be established. Please try again.",
      response.status,
    );
  }
  return data.token;
}

async function request(path, options = {}) {
  try {
    const headers = { Accept: "application/json" };
    if (options.body !== undefined) headers["Content-Type"] = "application/json";
    if (options.method === "POST" || options.method === "DELETE") {
      headers["X-XSRF-TOKEN"] = await getCsrfToken();
    }
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      credentials: "include",
      signal: AbortSignal.timeout(15000),
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new JobFinderApiError(
        data.message ?? (response.status === 503
          ? "Live job search is unavailable. Check provider configuration."
          : "The job request could not be completed."),
        response.status,
      );
    }
    return data;
  } catch (error) {
    if (error instanceof JobFinderApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new JobFinderApiError("The job provider request timed out. Please try again.", 0);
    }
    throw new JobFinderApiError("The job service could not be reached. Check your connection and try again.", 0);
  }
}

function isJob(value) {
  return Boolean(
    value
      && typeof value.id === "string"
      && typeof value.title === "string"
      && typeof value.sourceName === "string"
      && typeof value.sourceJobId === "string"
      && Array.isArray(value.requiredSkills),
  );
}

function validateJob(value) {
  if (!isJob(value)) throw new JobFinderApiError("The job service returned an incomplete listing.", 502);
  return value;
}

export const jobFinderService = {
  search: async (filters, page = 1, size = 20) => {
    const data = await request(`/api/jobs/search?${toSearchParams(filters, page, size)}`);
    if (!Array.isArray(data.jobs) || !Number.isInteger(data.page)) {
      throw new JobFinderApiError("The job search response was invalid.", 502);
    }
    return { ...data, jobs: data.jobs.map(validateJob) };
  },
  filters: () => request("/api/jobs/filters"),
  details: async (id) => validateJob(await request(`/api/jobs/${encodeURIComponent(id)}`)),
  save: async (job) => validateJob(await request(`/api/jobs/${encodeURIComponent(job.id)}/save`, {
    method: "POST",
    body: JSON.stringify({ keyword: job.title, location: job.location ?? "" }),
  })),
  unsave: (id) => request(`/api/jobs/${encodeURIComponent(id)}/save`, { method: "DELETE" }),
  saved: async () => {
    const data = await request("/api/jobs/saved");
    if (!Array.isArray(data)) throw new JobFinderApiError("The saved jobs response was invalid.", 502);
    return data.map(validateJob);
  },
  recommendations: () => request("/api/jobs/recommendations"),
};
