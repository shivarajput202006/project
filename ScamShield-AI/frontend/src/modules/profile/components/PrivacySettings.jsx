import { Eye, LockKeyhole, Sparkles } from "lucide-react";
import { useState } from "react";
import { settingsService } from "../services/settingsService.js";

export default function PrivacySettings({ settings, onSaved }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function toggleAiProcessing(event) {
    setBusy(true);
    setError("");
    try {
      const saved = await settingsService.update({ aiProcessingEnabled: event.target.checked });
      await onSaved(saved);
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">PRIVACY PREFERENCES</span><h2>Your data and privacy</h2></div><LockKeyhole size={18} /></div>
      <div className="profile-privacy-row">
        <span className="profile-setting-icon"><Eye size={17} /></span>
        <div className="profile-setting-copy"><strong>Resume visibility</strong><p>Your uploaded resume files are private and accessible only to your authenticated account. Public resume sharing is not available.</p></div>
        <span className="profile-private-badge">Private</span>
      </div>
      <div className="profile-privacy-row">
        <span className="profile-setting-icon"><Sparkles size={17} /></span>
        <div className="profile-setting-copy"><strong>Allow AI resume analysis</strong><p>When enabled, analysis is available only after you separately confirm before each resume is sent to the configured AI service.</p></div>
        <label className="profile-switch">
          <input type="checkbox" checked={settings.aiProcessingEnabled} disabled={busy} onChange={toggleAiProcessing} />
          <span aria-hidden="true" />
          <span className="profile-visually-hidden">Allow AI resume analysis</span>
        </label>
      </div>
      {error && <p className="profile-field-error" role="alert">{error}</p>}
      <p className="profile-privacy-note">Turning this off blocks new AI resume analyses; it does not delete analyses already saved to your account.</p>
    </section>
  );
}
