import { useCallback, useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";
import { settingsService } from "../profile/services/settingsService.js";
import AssistantSidebar from "./AssistantSidebar.jsx";
import ChatHeader from "./ChatHeader.jsx";
import ChatWindow from "./ChatWindow.jsx";
import ChatInput from "./ChatInput.jsx";
import ContextIndicator from "./ContextIndicator.jsx";
import EmptyChatState from "./EmptyChatState.jsx";
import { assistantService } from "./assistantService.js";
import "../dashboard/dashboard.css";
import "../notifications/notifications.css";
import "./assistant.css";

function createLocalId() {
  return globalThis.crypto?.randomUUID?.() ?? `message-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export default function AssistantPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [conversations, setConversations] = useState([]);
  const [conversationId, setConversationId] = useState("");
  const [conversationTitle, setConversationTitle] = useState("");
  const [messages, setMessages] = useState([]);
  const [contextSources, setContextSources] = useState([]);
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [feedbackBusy, setFeedbackBusy] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [historyPage, setHistoryPage] = useState(0);
  const [aiEnabled, setAiEnabled] = useState(false);
  const [checkingSettings, setCheckingSettings] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const handleUnauthorized = useCallback((cause) => {
    if (cause?.status === 401) {
      navigate("/login", { replace: true, state: { from: location } });
      return true;
    }
    return false;
  }, [location, navigate]);

  const loadHistory = useCallback(async (page = 0, append = false) => {
    if (!append) setLoadingHistory(true);
    try {
      const result = await assistantService.conversations(page);
      setConversations((current) => append ? [...current, ...result.content] : result.content);
      setHasMore(!result.last);
      setHistoryPage(page);
    } catch (cause) {
      if (!handleUnauthorized(cause)) setError(cause.message);
    } finally {
      setLoadingHistory(false);
    }
  }, [handleUnauthorized]);

  useEffect(() => {
    let active = true;
    Promise.all([assistantService.conversations(0), settingsService.get()])
      .then(([history, settings]) => {
        if (!active) return;
        setConversations(history.content ?? []);
        setHasMore(!history.last);
        setHistoryPage(0);
        setAiEnabled(settings?.aiProcessingEnabled === true);
      })
      .catch((cause) => {
        if (active && !handleUnauthorized(cause)) setError(cause.message);
      })
      .finally(() => {
        if (active) {
          setLoadingHistory(false);
          setCheckingSettings(false);
        }
      });
    return () => { active = false; };
  }, [handleUnauthorized]);

  async function selectConversation(id) {
    setError("");
    setNotice("");
    setLoadingMessages(true);
    setSidebarOpen(false);
    try {
      const conversation = await assistantService.conversation(id);
      setConversationId(conversation.id);
      setConversationTitle(conversation.title);
      setMessages(conversation.messages ?? []);
    } catch (cause) {
      if (!handleUnauthorized(cause)) setError(cause.message);
    } finally {
      setLoadingMessages(false);
    }
  }

  function newChat() {
    setConversationId("");
    setConversationTitle("");
    setMessages([]);
    setError("");
    setNotice("");
    setSidebarOpen(false);
  }

  async function sendMessage(text) {
    if (sending || !aiEnabled) return;
    setSending(true);
    setError("");
    setNotice("");
    const optimistic = {
      id: createLocalId(),
      role: "USER",
      content: text,
      createdAt: new Date().toISOString(),
    };
    setMessages((current) => [...current, optimistic]);
    try {
      const response = await assistantService.chat(text, conversationId, contextSources);
      setConversationId(response.conversationId);
      setConversationTitle(response.title);
      setMessages((current) => [
        ...current.filter((item) => item.id !== optimistic.id),
        response.userMessage,
        response.assistantMessage,
      ]);
      await loadHistory(0);
    } catch (cause) {
      if (!handleUnauthorized(cause)) setError(cause.message);
    } finally {
      setSending(false);
    }
  }

  async function deleteConversation(id) {
    const target = conversations.find((item) => item.id === id);
    if (!window.confirm(`Delete "${target?.title ?? "this conversation"}"? This cannot be undone.`)) return;
    setError("");
    try {
      await assistantService.deleteConversation(id);
      setConversations((current) => current.filter((item) => item.id !== id));
      if (conversationId === id) newChat();
      setNotice("Conversation deleted.");
    } catch (cause) {
      if (!handleUnauthorized(cause)) setError(cause.message);
    }
  }

  async function clearHistory() {
    if (!window.confirm("Delete all of your saved assistant conversations? This cannot be undone.")) return;
    setError("");
    try {
      await assistantService.clearHistory();
      setConversations([]);
      newChat();
      setNotice("Conversation history cleared.");
    } catch (cause) {
      if (!handleUnauthorized(cause)) setError(cause.message);
    }
  }

  async function feedback(id, messageId, value) {
    if (!conversationId) return;
    setFeedbackBusy(true);
    setError("");
    try {
      await assistantService.feedback(id, messageId, value);
      setMessages((current) => current.map((message) => message.id === messageId
        ? { ...message, feedback: value }
        : message));
      setNotice("Thanks for your feedback.");
    } catch (cause) {
      if (!handleUnauthorized(cause)) setError(cause.message);
    } finally {
      setFeedbackBusy(false);
    }
  }

  async function handleLogout() {
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (cause) {
      setError(cause.message);
    }
  }

  return (
    <div className="assistant-layout dashboard-shell">
      {sidebarOpen && <button className="dashboard-backdrop" type="button" aria-label="Close conversation history" onClick={() => setSidebarOpen(false)} />}
      <AssistantSidebar
        isOpen={sidebarOpen}
        conversations={conversations}
        currentId={conversationId}
        onNewChat={newChat}
        onSelect={selectConversation}
        onDelete={deleteConversation}
        onClear={clearHistory}
        onClose={() => setSidebarOpen(false)}
        loading={loadingHistory}
        hasMore={hasMore}
        onLoadMore={() => loadHistory(historyPage + 1, true)}
      />
      <div className="assistant-workspace">
        <div className="assistant-dashboard-header">
          <div className="assistant-user-heading"><span>ScamShield AI</span><strong>{user?.fullName ?? "Career workspace"}</strong></div>
          <div className="assistant-user-actions"><Link to="/dashboard">Dashboard</Link><button type="button" onClick={handleLogout}>Log out</button></div>
        </div>
        <main className="assistant-main">
          <ChatHeader
            title={conversationTitle}
            hasMessages={conversations.length > 0}
            onNewChat={newChat}
            onClear={clearHistory}
            onMenu={() => setSidebarOpen(true)}
          />
          {error && <div className="assistant-alert is-error" role="alert">{error}</div>}
          {notice && <div className="assistant-alert" role="status">{notice}</div>}
          {!checkingSettings && !aiEnabled && (
            <div className="assistant-settings-notice" role="status">
              AI processing is disabled for this account. <Link to="/settings">Enable AI processing in Settings</Link> to start chatting.
            </div>
          )}
          <div className="assistant-session">
            {loadingHistory && !conversations.length ? (
              <div className="assistant-chat-loading" role="status">Loading your assistant…</div>
            ) : messages.length || loadingMessages ? (
              <ChatWindow
                messages={messages}
                conversationId={conversationId}
                loading={loadingMessages}
                sending={sending}
                onPrompt={sendMessage}
                onFeedback={feedback}
                feedbackBusy={feedbackBusy}
                onError={setError}
              />
            ) : conversationId ? (
              <EmptyChatState />
            ) : (
              <ChatWindow
                messages={[]}
                conversationId=""
                loading={false}
                sending={sending}
                onPrompt={sendMessage}
                onFeedback={feedback}
                feedbackBusy={feedbackBusy}
                onError={setError}
              />
            )}
          </div>
          <div className="assistant-composer">
            <ContextIndicator selected={contextSources} onChange={setContextSources} disabled={sending || !aiEnabled} />
            <ChatInput onSend={sendMessage} disabled={sending || checkingSettings || !aiEnabled} />
            <p className="assistant-disclaimer">AI can make mistakes. Do not rely on it as the sole source for employment, legal, or security decisions.</p>
          </div>
        </main>
      </div>
    </div>
  );
}
