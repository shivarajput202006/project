import { Check, Copy } from "lucide-react";
import { useState } from "react";

export default function MessageActions({ content }) {
  const [copied, setCopied] = useState(false);

  async function copy() {
    try {
      await navigator.clipboard.writeText(content);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 1400);
    } catch {
      setCopied(false);
    }
  }

  return (
    <button className="assistant-copy-button" type="button" onClick={copy} aria-label={copied ? "Copied" : "Copy response"}>
      {copied ? <Check size={14} /> : <Copy size={14} />}
      <span>{copied ? "Copied" : "Copy"}</span>
    </button>
  );
}
