const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class URLScannerApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "URLScannerApiError";
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
    throw new URLScannerApiError(
      data.message ?? "A secure connection could not be established. Please try again.",
      response.status,
    );
  }
  return data.token;
}

async function readResponse(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    const message = response.status === 503
      ? "The URL scanner is temporarily unavailable. Please try again later."
      : data.message ?? "The request could not be completed. Please try again.";
    throw new URLScannerApiError(message, response.status);
  }
  return data;
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
    return readResponse(response);
  } catch (error) {
    if (error instanceof URLScannerApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new URLScannerApiError("The scan request timed out. Please try again.", 0);
    }
    throw new URLScannerApiError("The scanner could not be reached. Check your connection and try again.", 0);
  }
}

function isScanResponse(value) {
  return Boolean(
    value
      && typeof value.id === "string"
      && typeof value.submittedUrl === "string"
      && typeof value.normalizedDomain === "string"
      && ["LOW_RISK", "NEEDS_REVIEW", "HIGH_RISK", "UNVERIFIED"].includes(value.riskStatus)
      && Number.isInteger(value.riskScore)
      && Array.isArray(value.securityIndicators)
      && Array.isArray(value.recommendations)
      && value.reputationFinding
      && value.redirectSummary
      && value.scanTimestamp,
  );
}

function validateScanResponse(value) {
  if (!isScanResponse(value)) {
    throw new URLScannerApiError("The scanner returned an incomplete report. No result was displayed.", 502);
  }
  return value;
}

export const urlScannerService = {
  scan: async (url) => validateScanResponse(await request("/api/url-scanner/scan", {
    method: "POST",
    body: JSON.stringify({ url }),
  })),
  history: async () => {
    const data = await request("/api/url-scanner/history");
    if (!Array.isArray(data)) {
      throw new URLScannerApiError("The scan history response was invalid.", 502);
    }
    return data.map(validateScanResponse);
  },
  get: async (id) => validateScanResponse(await request(`/api/url-scanner/${encodeURIComponent(id)}`)),
  delete: (id) => request(`/api/url-scanner/${encodeURIComponent(id)}`, { method: "DELETE" }),
};
