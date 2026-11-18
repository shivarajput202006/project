import { ThumbsDown, ThumbsUp } from "lucide-react";

export default function FeedbackButtons({ value, onFeedback, disabled }) {
  return (
    <div className="assistant-feedback" aria-label="Rate this response">
      <button type="button" aria-label="Helpful response" aria-pressed={value === "HELPFUL"} disabled={disabled || Boolean(value)} onClick={() => onFeedback("HELPFUL")}>
        <ThumbsUp size={14} />
      </button>
      <button type="button" aria-label="Not helpful response" aria-pressed={value === "NOT_HELPFUL"} disabled={disabled || Boolean(value)} onClick={() => onFeedback("NOT_HELPFUL")}>
        <ThumbsDown size={14} />
      </button>
    </div>
  );
}
