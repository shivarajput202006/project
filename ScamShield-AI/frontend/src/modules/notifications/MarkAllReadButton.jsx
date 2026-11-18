import { CheckCheck } from "lucide-react";

export default function MarkAllReadButton({ onClick, disabled, busy = false }) {
  return (
    <button className="notification-mark-all" type="button" onClick={onClick} disabled={disabled || busy}>
      <CheckCheck size={15} />
      {busy ? "Updating…" : "Mark all read"}
    </button>
  );
}
