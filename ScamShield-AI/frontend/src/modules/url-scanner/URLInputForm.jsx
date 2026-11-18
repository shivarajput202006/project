import { Link2, ScanSearch } from "lucide-react";

export default function URLInputForm({ value, onChange, onSubmit, disabled, error }) {
  return (
    <form className="url-scanner-form" onSubmit={onSubmit} noValidate>
      <label htmlFor="url-to-scan">URL to check</label>
      <div className="url-scanner-input-wrap">
        <Link2 size={18} aria-hidden="true" />
        <input
          id="url-to-scan"
          type="url"
          inputMode="url"
          autoComplete="url"
          placeholder="https://example.com/job-application"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? "url-scan-error" : "url-scan-help"}
          maxLength={2048}
        />
      </div>
      {error ? (
        <p id="url-scan-error" className="url-scanner-field-error" role="alert">{error}</p>
      ) : (
        <p id="url-scan-help" className="url-scanner-help">
          Enter a full HTTP or HTTPS address. The scanner will not open or navigate to it.
        </p>
      )}
      <button className="url-scanner-submit" type="submit" disabled={disabled}>
        <ScanSearch size={17} />
        {disabled ? "Checking URL…" : "Scan URL"}
      </button>
    </form>
  );
}
