import { useState } from "react";
import { Bot, UserRound } from "lucide-react";
import { formatAssistantMarkdown } from "./messageFormatter.jsx";
import { formatChatTime } from "./assistantHelpers.js";
import MessageActions from "./MessageActions.jsx";
import FeedbackButtons from "./FeedbackButtons.jsx";

export default function MessageBubble({ message, conversationId, onFeedback, feedbackBusy, onError }) {
  const assistant = message.role === "ASSISTANT";
  const [copied, setCopied] = useState(false);

  async function copyCode(code) {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1200);
    } catch (_error) {
      onError("Could not copy this code block.");
    }
  }

  return (
    <article className={`assistant-message${assistant ? " is-assistant" : " is-user"}`}>
      <span className="assistant-message-avatar">{assistant ? <Bot size={16} /> : <UserRound size={16} />}</span>
      <div className="assistant-message-main">
        <div className="assistant-message-meta">
          <strong>{assistant ? "ScamShield AI" : "You"}</strong>
          <time dateTime={message.createdAt}>{formatChatTime(message.createdAt)}</time>
        </div>
        <div className="assistant-message-content">
          {assistant
            ? formatAssistantMarkdown(message.content, copyCode)
            : <p>{message.content}</p>}
        </div>
        {assistant && (
          <div className="assistant-message-tools">
            <MessageActions content={message.content} />
            <FeedbackButtons
              value={message.feedback}
              disabled={feedbackBusy}
              onFeedback={(feedback) => onFeedback(conversationId, message.id, feedback)}
            />
            {copied && <span role="status" className="assistant-copy-status">Code copied</span>}
          </div>
        )}
      </div>
    </article>
  );
}
