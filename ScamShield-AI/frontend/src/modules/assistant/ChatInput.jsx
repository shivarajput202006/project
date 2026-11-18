import { ArrowUp, Paperclip } from "lucide-react";
import { useRef, useState } from "react";

export default function ChatInput({ onSend, disabled }) {
  const [value, setValue] = useState("");
  const inputRef = useRef(null);
  const remaining = 3000 - value.length;

  async function submit(event) {
    event?.preventDefault();
    const message = value.trim();
    if (!message || disabled || message.length > 3000) return;
    setValue("");
    await onSend(message);
    inputRef.current?.focus();
  }

  function handleKeyDown(event) {
    if (event.key === "Enter" && !event.shiftKey && !event.nativeEvent.isComposing) {
      event.preventDefault();
      submit(event);
    }
  }

  return (
    <form className="assistant-input-wrap" onSubmit={submit}>
      <label className="assistant-textarea-label">
        <span className="sr-only">Ask a career question</span>
        <textarea
          ref={inputRef}
          value={value}
          onChange={(event) => setValue(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask about Java, interviews, resumes, or job safety…"
          rows={1}
          maxLength={3000}
          disabled={disabled}
          aria-describedby="assistant-input-hint"
        />
      </label>
      <div className="assistant-input-footer">
        <span id="assistant-input-hint"><Paperclip size={13} /> Your selected context stays private to your account.</span>
        <div><small>{remaining} characters left</small>
          <button type="submit" disabled={disabled || !value.trim() || remaining < 0} aria-label="Send message">
            <ArrowUp size={18} />
          </button>
        </div>
      </div>
    </form>
  );
}
