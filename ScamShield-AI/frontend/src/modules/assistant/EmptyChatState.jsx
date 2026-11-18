import { MessageCircle } from "lucide-react";

export default function EmptyChatState() {
  return (
    <div className="assistant-empty-chat">
      <MessageCircle size={23} />
      <strong>Select a conversation</strong>
      <span>Or start a new chat to ask a career question.</span>
    </div>
  );
}
