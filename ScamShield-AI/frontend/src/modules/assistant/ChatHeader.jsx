import { Menu, Plus, Trash2 } from "lucide-react";

export default function ChatHeader({ title, onNewChat, onClear, onMenu, hasMessages }) {
  return (
    <header className="assistant-chat-header">
      <button type="button" className="assistant-mobile-menu" onClick={onMenu} aria-label="Open conversation history"><Menu size={18} /></button>
      <div><span className="dashboard-eyebrow">AI CAREER ASSISTANT</span><h2>{title || "New conversation"}</h2></div>
      <div className="assistant-chat-header-actions">
        <button type="button" onClick={onNewChat}><Plus size={15} /> New chat</button>
        {hasMessages && <button type="button" onClick={onClear} aria-label="Clear conversation history"><Trash2 size={15} /></button>}
      </div>
    </header>
  );
}
