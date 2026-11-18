const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
let csrfToken;
let csrfTokenPromise;

export class AuthApiError extends Error {
  constructor(message, status, fieldErrors = {}) {
    super(message);
    this.name = "AuthApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

function readCsrfToken() {
  if (csrfToken) return Promise.resolve(csrfToken);
  if (!csrfTokenPromise) {
    csrfTokenPromise = (async () => {
      const response = await fetch(`${API_BASE_URL}/api/auth/csrf`, {
        credentials: "include",
        headers: { Accept: "application/json" },
      });
      if (!response.ok) {
        throw new AuthApiError("Could not establish a secure connection. Please try again.", response.status);
      }
      const data = await response.json();
      if (!data.token) {
        throw new AuthApiError("The server did not provide a CSRF token.", response.status);
      }
      csrfToken = data.token;
      return csrfToken;
    })().catch((error) => {
      csrfTokenPromise = undefined;
      throw error;
    });
  }
  return csrfTokenPromise;
}

async function request(path, options = {}) {
  const method = options.method ?? "GET";
  const headers = { Accept: "application/json", ...options.headers };
  if (options.body) headers["Content-Type"] = "application/json";
  if (method !== "GET" && method !== "HEAD" && method !== "OPTIONS") {
    headers["X-XSRF-TOKEN"] = csrfToken ?? await readCsrfToken();
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    method,
    headers,
    credentials: "include",
  });
  if (response.status === 204) return null;

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new AuthApiError(
      data.message ?? "The request could not be completed. Please try again.",
      response.status,
      data.fieldErrors ?? {},
    );
  }
  return data;
}

async function getCurrentUser() {
  try {
    return (await request("/api/auth/me")).user;
  } catch (error) {
    if (error instanceof AuthApiError && error.status === 401) return null;
    throw error;
  }
}

export const authService = {
  initialize: readCsrfToken,
  getCurrentUser,
  login: (email, password) =>
    request("/api/auth/login", { method: "POST", body: JSON.stringify({ email, password }) }),
  register: (fullName, email, password, confirmPassword) =>
    request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ fullName, email, password, confirmPassword }),
    }),
  logout: () => request("/api/auth/logout", { method: "POST" }),
};
