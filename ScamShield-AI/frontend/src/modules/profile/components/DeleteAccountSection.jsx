import { AlertTriangle, Trash2 } from "lucide-react";
import { useState } from "react";
import { profileService } from "../services/profileService.js";

export default function DeleteAccountSection({ onDeleted }) {
  const [open, setOpen] = useState(false);
  const [password, setPassword] = useState("");
  const [confirmation, setConfirmation] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  async function submit(event) {
    event.preventDefault();
    setError("");
    if (confirmation !== "DELETE") {
      setError('Type "DELETE" exactly to confirm.');
      return;
    }
    setBusy(true);
    try {
      await profileService.deleteAccount({ currentPassword: password, confirmation });
      setPassword("");
      setConfirmation("");
      await onDeleted();
    } catch (deleteError) {
      setError(deleteError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <section className="profile-card profile-danger-card">
        <div className="profile-card-heading"><div><span className="profile-eyebrow">IRREVERSIBLE ACTION</span><h2>Delete account</h2></div><AlertTriangle size={18} /></div>
        <p className="profile-section-description">Permanently remove your account, profile, photo, resume files and analyses, saved jobs, reports, and account settings. Shared job-provider listings are not deleted.</p>
        <button className="profile-danger-button" type="button" onClick={() => { setOpen(true); setError(""); }}><Trash2 size={15} /> Delete my account</button>
      </section>
      {open && (
        <div className="profile-dialog-backdrop" role="presentation">
          <section className="profile-delete-dialog" role="alertdialog" aria-modal="true" aria-labelledby="profile-delete-title">
            <span className="profile-delete-icon"><AlertTriangle size={22} /></span>
            <h2 id="profile-delete-title">Permanently delete your account?</h2>
            <p>This action removes your account and its stored user-owned data. It cannot be undone. Confirm your password and type <strong>DELETE</strong>.</p>
            <form onSubmit={submit}>
              <label className="profile-field"><span>Current password</span><input type="password" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.target.value)} /></label>
              <label className="profile-field"><span>Type DELETE to confirm</span><input autoComplete="off" required value={confirmation} onChange={(event) => setConfirmation(event.target.value)} /></label>
              {error && <p className="profile-field-error" role="alert">{error}</p>}
              <div className="profile-form-actions">
                <button className="profile-danger-button" type="submit" disabled={busy || confirmation !== "DELETE"}>{busy ? "Deleting…" : "Permanently delete"}</button>
                <button className="profile-secondary-button" type="button" disabled={busy} onClick={() => setOpen(false)}>Cancel</button>
              </div>
            </form>
          </section>
        </div>
      )}
    </>
  );
}
