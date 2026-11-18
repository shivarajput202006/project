const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class ProfileApiError extends Error {
  constructor(message, status = 0, fieldErrors = {}) {
    super(message);
    this.name = "ProfileApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
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
    throw new ProfileApiError(data.message ?? "A secure connection could not be established.", response.status);
  }
  return data.token;
}

export async function profileRequest(path, { method = "GET", body, responseType = "json" } = {}) {
  try {
    const headers = { Accept: responseType === "blob" ? "application/zip, image/*" : "application/json" };
    if (method !== "GET") headers["X-XSRF-TOKEN"] = await getCsrfToken();
    if (body && !(body instanceof FormData)) headers["Content-Type"] = "application/json";
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body instanceof FormData ? body : body ? JSON.stringify(body) : undefined,
      credentials: "include",
      signal: AbortSignal.timeout(30000),
    });
    if (response.status === 204) return null;
    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      throw new ProfileApiError(
        data.message ?? "The account request could not be completed.",
        response.status,
        data.fieldErrors ?? {},
      );
    }
    if (responseType === "blob") {
      const disposition = response.headers.get("Content-Disposition") ?? "";
      const filename = disposition.match(/filename="?([^";]+)"?/i)?.[1] ?? "scamshield-account-export.zip";
      return { blob: await response.blob(), filename };
    }
    if (responseType === "photo") return await response.blob();
    return await response.json();
  } catch (error) {
    if (error instanceof ProfileApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new ProfileApiError("The account request timed out. Please try again.", 0);
    }
    throw new ProfileApiError("The account service could not be reached. Check your connection and retry.", 0);
  }
}

export const profileService = {
  get: () => profileRequest("/api/profile/me"),
  update: (data) => profileRequest("/api/profile/me", { method: "PUT", body: data }),
  education: () => profileRequest("/api/profile/education"),
  addEducation: (data) => profileRequest("/api/profile/education", { method: "POST", body: data }),
  updateEducation: (id, data) =>
    profileRequest(`/api/profile/education/${encodeURIComponent(id)}`, { method: "PUT", body: data }),
  deleteEducation: (id) =>
    profileRequest(`/api/profile/education/${encodeURIComponent(id)}`, { method: "DELETE" }),
  updateSkills: (skills) =>
    profileRequest("/api/profile/skills", { method: "PUT", body: { skills } }),
  uploadPhoto: (file) => {
    const body = new FormData();
    body.append("file", file);
    return profileRequest("/api/profile/photo", { method: "POST", body });
  },
  getPhoto: () => profileRequest("/api/profile/photo", { responseType: "photo" }),
  deletePhoto: () => profileRequest("/api/profile/photo", { method: "DELETE" }),
  changePassword: (data) => profileRequest("/api/profile/password", { method: "PUT", body: data }),
  exportData: () => profileRequest("/api/profile/data-export", { method: "POST", responseType: "blob" }),
  deleteAccount: (data) => profileRequest("/api/profile/me", { method: "DELETE", body: data }),
};
