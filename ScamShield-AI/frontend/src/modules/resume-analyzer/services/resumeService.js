const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class ResumeApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "ResumeApiError";
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
    throw new ResumeApiError(data.message ?? "A secure connection could not be established.", response.status);
  }
  return data.token;
}

async function request(path, { method = "GET", body, timeout = 30000 } = {}) {
  try {
    const headers = { Accept: "application/json" };
    if (method !== "GET" && method !== "HEAD" && method !== "OPTIONS") {
      headers["X-XSRF-TOKEN"] = await csrfToken();
    }
    if (body && !(body instanceof FormData)) headers["Content-Type"] = "application/json";
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body,
      credentials: "include",
      signal: AbortSignal.timeout(timeout),
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new ResumeApiError(
        data.message ?? "The resume request could not be completed. Please try again.",
        response.status,
      );
    }
    return data;
  } catch (error) {
    if (error instanceof ResumeApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new ResumeApiError("The request timed out. Please retry.", 0);
    }
    throw new ResumeApiError("The resume service could not be reached. Check your connection and try again.", 0);
  }
}

function assertResume(value) {
  if (!value || typeof value.id !== "string" || typeof value.originalFilename !== "string") {
    throw new ResumeApiError("The resume service returned an invalid response.", 502);
  }
  return value;
}

function assertAnalysis(value) {
  if (!value || typeof value.resumeId !== "string" || !value.extractedData || !value.score) {
    throw new ResumeApiError("The analysis response was incomplete. Please retry.", 502);
  }
  return value;
}

export const resumeService = {
  upload: async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    return assertResume(await request("/api/resume/upload", { method: "POST", body: formData }));
  },
  analyze: async (id, consentToAiProcessing) => assertAnalysis(await request(
    `/api/resume/${encodeURIComponent(id)}/analyze`,
    {
      method: "POST",
      body: JSON.stringify({ consentToAiProcessing }),
      timeout: 60000,
    },
  )),
  get: async (id) => assertResume(await request(`/api/resume/${encodeURIComponent(id)}`)),
  analysis: async (id) => assertAnalysis(await request(`/api/resume/${encodeURIComponent(id)}/analysis`)),
  recommendations: (id) => request(`/api/resume/${encodeURIComponent(id)}/recommendations`),
  history: async () => {
    const value = await request("/api/resume/history");
    if (!Array.isArray(value)) throw new ResumeApiError("Resume history could not be validated.", 502);
    return value;
  },
  delete: (id) => request(`/api/resume/${encodeURIComponent(id)}`, { method: "DELETE" }),
};
