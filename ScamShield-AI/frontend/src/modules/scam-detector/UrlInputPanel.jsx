import { Link2 } from "lucide-react";

export default function UrlInputPanel({ value, onChange }) {
  return (
    <div className="scam-input-panel">
      <label className="scam-field-label" htmlFor="scam-url">
        <Link2 size={15} /> Suspicious job application URL
      </label>
      <p className="scam-field-help">The link will not be opened or downloaded. Private and local network addresses are rejected.</p>
      <input
        id="scam-url"
        className="scam-text-input"
        type="url"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        maxLength={2048}
        placeholder="https://example.com/careers"
        required
      />
      <p className="scam-url-note">A URL analysis cannot verify that a site is safe. Be cautious with links you do not recognize.</p>
    </div>
  );
}
