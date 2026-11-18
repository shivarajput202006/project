import { MessageCircle, Trash2 } from "lucide-react";
import { formatChatTime } from "./assistantHelpers.js";

export default function ConversationItem({ conversation, active, onSelect, onDelete }) {
  return (
    <div className={`assistant-conversation-item${active ? " is-active" : ""}`}>
      <button type="button" className="assistant-conversation-select" onClick={onSelect}>
        <MessageCircle size={15} />
        <span><strong>{conversation.title}</strong><small>{conversation.lastMessagePreview || "No messages"} · {formatChatTime(conversation.updatedAt)}</small></span>
      </button>
      <button type="button" className="assistant-conversation-delete" aria-label={`Delete ${conversation.title}`} onClick={onDelete}>
        <Trash2 size={14} />
      </button>
    </div>
  );
}
