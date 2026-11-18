import { useEffect, useState } from "react";
import { BellRing, Save } from "lucide-react";
import { notificationService } from "./notificationService.js";

const LABELS = [
  ["scamAnalysisEnabled", "Scam analysis", "When a completed scam review is ready."],
  ["urlScanEnabled", "URL scans", "When a completed URL scan is ready."],
  ["jobEnabled", "Job updates", "Relevant live job and provider updates."],
  ["resumeEnabled", "Resume analysis", "When a resume analysis is complete."],
  ["jobMatchEnabled", "Job matching", "When resume-based matches are available."],
  ["reportEnabled", "Reports", "When a requested report export is ready."],
  ["systemEnabled", "System updates", "General service announcements."],
];

export default function NotificationPreferences() {
  const [preferences, setPreferences] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    notificationService.preferences()
      .then((data) => {
        if (active) setPreferences(data);
      })
      .catch((cause) => {
        if (active) setError(cause.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  async function save() {
    setSaving(true);
    setError("");
    setMessage("");
    try {
      setPreferences(await notificationService.updatePreferences(preferences));
      setMessage("Notification preferences saved.");
    } catch (cause) {
      setError(cause.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <details className="notification-preferences">
      <summary><BellRing size={16} /> Notification preferences</summary>
      {loading ? (
        <p className="notification-preferences-status">Loading preferences…</p>
      ) : error && !preferences ? (
        <p className="notification-preferences-error" role="alert">{error}</p>
      ) : (
        <>
          <div className="notification-preference-grid">
            {LABELS.map(([key, label, description]) => (
              <label className="notification-preference" key={key}>
                <span><strong>{label}</strong><small>{description}</small></span>
                <input
                  type="checkbox"
                  checked={Boolean(preferences?.[key])}
                  onChange={(event) => setPreferences((current) => ({ ...current, [key]: event.target.checked }))}
                />
              </label>
            ))}
            <div className="notification-preference is-locked">
              <span><strong>Security alerts</strong><small>Critical account alerts are always enabled.</small></span>
              <input type="checkbox" checked readOnly aria-label="Security alerts always enabled" />
            </div>
          </div>
          {error && <p className="notification-preferences-error" role="alert">{error}</p>}
          {message && <p className="notification-preferences-status" role="status">{message}</p>}
          <button type="button" className="notification-preferences-save" onClick={save} disabled={saving}>
            <Save size={14} /> {saving ? "Saving…" : "Save preferences"}
          </button>
        </>
      )}
    </details>
  );
}
