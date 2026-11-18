import { KeyRound, ShieldCheck } from "lucide-react";
import { useState } from "react";
import { validatePassword } from "../utils/profileValidation.js";
import { profileService } from "../services/profileService.js";

export default function ChangePasswordForm({ onChanged }) {
  const [values, setValues] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    const validationErrors = validatePassword(values);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length) return;
    setBusy(true);
    try {
      await profileService.changePassword(values);
      setValues({ currentPassword: "", newPassword: "", confirmPassword: "" });
      await onChanged();
    } catch (error) {
      setErrors({ form: error.message });
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">ACCOUNT SECURITY</span><h2>Change password</h2></div><KeyRound size={18} /></div>
      <p className="profile-section-description">Changing your password signs out active sessions. You will need to sign in again.</p>
      <form className="profile-password-form" onSubmit={submit}>
        {[
          ["currentPassword", "Current password"],
          ["newPassword", "New password"],
          ["confirmPassword", "Confirm new password"],
        ].map(([field, label]) => (
          <label className="profile-field" key={field}>
            <span>{label}</span>
            <input type="password" autoComplete={field === "currentPassword" ? "current-password" : "new-password"}
              value={values[field]} onChange={(event) => setValues((current) => ({ ...current, [field]: event.target.value }))} />
            {errors[field] && <small className="profile-field-error">{errors[field]}</small>}
          </label>
        ))}
        <p className="profile-password-hint"><ShieldCheck size={14} /> 12+ characters with uppercase, lowercase, a number, and a symbol.</p>
        {errors.form && <p className="profile-field-error" role="alert">{errors.form}</p>}
        <button className="profile-primary-button" type="submit" disabled={busy}>{busy ? "Updating…" : "Update password"}</button>
      </form>
    </section>
  );
}
