import { MessageSquareText } from "lucide-react";

export default function TextInputPanel({ value, onChange }) {
  return (
    <div className="scam-input-panel">
      <label className="scam-field-label" htmlFor="scam-text">
        <MessageSquareText size={15} /> Paste the message or job offer
      </label>
      <p className="scam-field-help">WhatsApp, SMS, job offer, or recruiter communication.</p>
      <textarea
        id="scam-text"
        className="scam-textarea"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        maxLength={20000}
        rows={8}
        placeholder="Paste the message you want to review…"
        required
      />
      <div className="scam-input-meta"><span>Do not include passwords or identity-document numbers.</span><span>{value.length}/20,000</span></div>
    </div>
  );
}
