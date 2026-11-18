import { ChevronDown, ShieldCheck } from "lucide-react";
import { useState } from "react";
import { CONTEXT_OPTIONS } from "./assistantHelpers.js";

export default function ContextIndicator({ selected, onChange, disabled }) {
  const [open, setOpen] = useState(false);

  function toggle(value) {
    if (selected.includes(value)) onChange(selected.filter((item) => item !== value));
    else if (selected.length < 4) onChange([...selected, value]);
  }

  return (
    <div className="assistant-context">
      <button type="button" className="assistant-context-trigger" aria-expanded={open} onClick={() => setOpen((value) => !value)} disabled={disabled}>
        <ShieldCheck size={14} />
        {selected.length ? `${selected.length} private context source${selected.length === 1 ? "" : "s"}` : "No personal context"}
        <ChevronDown size={13} />
      </button>
      {open && (
        <div className="assistant-context-menu">
          <strong>Use your ScamShield data</strong>
          <p>Only selected sources belonging to your account are shared for this answer. Choose up to four.</p>
          {CONTEXT_OPTIONS.map(([value, label]) => (
            <label key={value}>
              <input type="checkbox" checked={selected.includes(value)} disabled={!selected.includes(value) && selected.length >= 4} onChange={() => toggle(value)} />
              <span>{label}</span>
            </label>
          ))}
          <button type="button" onClick={() => setOpen(false)}>Done</button>
        </div>
      )}
    </div>
  );
}
