import { Bot, MessageSquarePlus, Trash2, X } from "lucide-react";
import ConversationList from "./ConversationList.jsx";

export default function AssistantSidebar({
  isOpen,
  conversations,
  currentId,
  onNewChat,
  onSelect,
  onDelete,
  onClear,
  onClose,
  loading,
  hasMore,
  onLoadMore,
}) {
  return (
    <aside className={`assistant-sidebar${isOpen ? " is-open" : ""}`}>
      <div className="assistant-sidebar-brand"><span><Bot size={19} /></span><div><strong>Career assistant</strong><small>Private AI guidance</small></div>
        <button type="button" onClick={onClose} aria-label="Close history"><X size={17} /></button>
      </div>
      <button type="button" className="assistant-new-chat" onClick={onNewChat}><MessageSquarePlus size={16} /> New Chat</button>
      <div className="assistant-history-header"><span>RECENT CONVERSATIONS</span>
        {conversations.length > 0 && <button type="button" onClick={onClear} aria-label="Clear all conversation history"><Trash2 size={14} /></button>}
      </div>
      <ConversationList
        conversations={conversations}
        currentId={currentId}
        onSelect={onSelect}
        onDelete={onDelete}
        loading={loading}
        hasMore={hasMore}
        onLoadMore={onLoadMore}
      />
      <div className="assistant-sidebar-note">AI responses can be inaccurate. Verify important career and safety information independently.</div>
    </aside>
  );
}
