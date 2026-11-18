const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class ScamDetectorApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ScamDetectorApiError";
    this.status = status;
  }
}

async function getCsrfToken() {
  const response = await fetch(`${API_BASE_URL}/api/auth/csrf`, {
    credentials: "include",
    headers: { Accept: "application/json" },
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok || !data.token) {
    throw new ScamDetectorApiError(
      data.message ?? "A secure connection could not be established. Please try again.",
      response.status,
    );
  }
  return data.token;
}

async function readResponse(response) {
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new ScamDetectorApiError(
      data.message ?? "The request could not be completed. Please try again.",
      response.status,
    );
  }
  return data;
}

async function postJson(path, payload) {
  const csrfToken = await getCsrfToken();
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    credentials: "include",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      "X-XSRF-TOKEN": csrfToken,
    },
    body: JSON.stringify(payload),
  });
  return readResponse(response);
}

function postFile(file, onProgress) {
  return getCsrfToken().then((csrfToken) => new Promise((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open("POST", `${API_BASE_URL}/api/scam-detector/analyze/file`);
    request.withCredentials = true;
    request.timeout = 120000;
    request.setRequestHeader("Accept", "application/json");
    request.setRequestHeader("X-XSRF-TOKEN", csrfToken);
    request.upload.addEventListener("progress", (event) => {
      if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100));
    });
    request.addEventListener("load", () => {
      let data = {};
      try {
        data = JSON.parse(request.responseText);
      } catch {
        data = {};
      }
      if (request.status < 200 || request.status >= 300) {
        reject(new ScamDetectorApiError(
          data.message ?? "The file could not be analyzed. Please try again.",
          request.status,
        ));
        return;
      }
      resolve(data);
    });
    request.addEventListener("error", () => {
      reject(new ScamDetectorApiError("The upload failed because the server could not be reached.", 0));
    });
    request.addEventListener("timeout", () => {
      reject(new ScamDetectorApiError("The upload took too long. Check your connection and try again.", 0));
    });
    const body = new FormData();
    body.append("file", file);
    request.send(body);
  }));
}

export const scamDetectorService = {
  analyzeText: (text) => postJson("/api/scam-detector/analyze/text", { text }),
  analyzeEmail: (payload) => postJson("/api/scam-detector/analyze/email", payload),
  analyzeUrl: (url) => postJson("/api/scam-detector/analyze/url", { url }),
  analyzeFile: postFile,
  getHistory: () => fetch(`${API_BASE_URL}/api/scam-detector/history`, {
    credentials: "include",
    headers: { Accept: "application/json" },
  }).then(readResponse).then((data) => {
    if (!Array.isArray(data)) throw new ScamDetectorApiError("The analysis history response was invalid.", 502);
    return data;
  }),
  getReport: (id) => fetch(`${API_BASE_URL}/api/scam-detector/${encodeURIComponent(id)}`, {
    credentials: "include",
    headers: { Accept: "application/json" },
  }).then(readResponse),
};
