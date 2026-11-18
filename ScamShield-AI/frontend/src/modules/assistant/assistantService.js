const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080").replace(/\/+$/, "");

export class AssistantApiError extends Error {
  constructor(message, status = 0) {
    super(message);
    this.name = "AssistantApiError";
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
    throw new AssistantApiError(data.message ?? "A secure connection could not be established.", response.status);
  }
  return data.token;
}

async function request(path, { method = "GET", body } = {}) {
  try {
    const headers = { Accept: "application/json" };
    if (method !== "GET") headers["X-XSRF-TOKEN"] = await csrfToken();
    if (body !== undefined) headers["Content-Type"] = "application/json";
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: "include",
      signal: AbortSignal.timeout(45000),
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new AssistantApiError(data.message ?? "The assistant request failed.", response.status);
    return data;
  } catch (error) {
    if (error instanceof AssistantApiError) throw error;
    if (error?.name === "TimeoutError" || error?.name === "AbortError") {
      throw new AssistantApiError("The assistant request timed out. Please try again.", 0);
    }
    throw new AssistantApiError("The assistant service could not be reached.", 0);
  }
}

export const assistantService = {
  chat: (message, conversationId, contextSources) => request("/api/assistant/chat", {
    method: "POST",
    body: { message, conversationId: conversationId || null, contextSources },
  }),
  conversations: (page = 0) => request(`/api/assistant/conversations?page=${page}&size=30`),
  conversation: (id) => request(`/api/assistant/conversations/${encodeURIComponent(id)}`),
  deleteConversation: (id) => request(`/api/assistant/conversations/${encodeURIComponent(id)}`, { method: "DELETE" }),
  clearHistory: () => request("/api/assistant/conversations", { method: "DELETE" }),
  feedback: (conversationId, messageId, feedback) => request(
    `/api/assistant/conversations/${encodeURIComponent(conversationId)}/feedback`,
    { method: "POST", body: { messageId, feedback } },
  ),
};
