import { Save } from "lucide-react";
import { useState } from "react";

export default function ProfessionalBio({ value, onSave }) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(value ?? "");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    if (draft.length > 2000) return setError("Bio must be 2,000 characters or fewer.");
    setBusy(true);
    setError("");
    try {
      await onSave(draft);
      setEditing(false);
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">PROFESSIONAL BIO</span><h2>About you</h2></div></div>
      <form onSubmit={submit}>
        <label className="profile-field profile-field-full">
          <span>Professional bio <small>Optional</small></span>
          <textarea value={editing ? draft : (value ?? "")} maxLength={2000} rows={4} disabled={!editing || busy}
            placeholder="Briefly describe your professional focus and interests."
            onChange={(event) => setDraft(event.target.value)} />
          {error && <small className="profile-field-error">{error}</small>}
          <small className="profile-field-hint">{(editing ? draft : (value ?? "")).length}/2000 characters</small>
        </label>
        <div className="profile-form-actions">
          {editing ? <>
            <button className="profile-primary-button" type="submit" disabled={busy}><Save size={15} />{busy ? "Saving…" : "Save bio"}</button>
            <button className="profile-secondary-button" type="button" disabled={busy} onClick={() => { setDraft(value ?? ""); setEditing(false); setError(""); }}>Cancel</button>
          </> : <button className="profile-secondary-button" type="button" onClick={() => { setDraft(value ?? ""); setEditing(true); }}>Edit bio</button>}
        </div>
      </form>
    </section>
  );
}
