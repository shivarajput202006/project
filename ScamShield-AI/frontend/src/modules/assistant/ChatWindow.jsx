import { useEffect, useRef } from "react";
import MessageBubble from "./MessageBubble.jsx";
import TypingIndicator from "./TypingIndicator.jsx";
import WelcomeScreen from "./WelcomeScreen.jsx";

export default function ChatWindow({ messages, conversationId, loading, sending, onPrompt, onFeedback, feedbackBusy, onError }) {
  const bottomRef = useRef(null);
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth", block: "end" });
  }, [messages, sending]);

  if (loading) return <div className="assistant-chat-loading" role="status">Loading conversation…</div>;
  if (!messages.length) {
    return <div className="assistant-chat-window"><WelcomeScreen onPrompt={onPrompt} disabled={sending} /><div ref={bottomRef} /></div>;
  }
  return (
    <div className="assistant-chat-window">
      <div className="assistant-messages">
        {messages.map((message) => (
          <MessageBubble
            key={message.id}
            message={message}
            conversationId={conversationId}
            onFeedback={onFeedback}
            feedbackBusy={feedbackBusy}
            onError={onError}
          />
        ))}
        {sending && <TypingIndicator />}
        <div ref={bottomRef} />
      </div>
    </div>
  );
}
