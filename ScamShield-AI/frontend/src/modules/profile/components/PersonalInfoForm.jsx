import { Mail, Save, UserRound } from "lucide-react";
import { useState } from "react";
import { validateProfile } from "../utils/profileValidation.js";

export default function PersonalInfoForm({ profile, onSave }) {
  const [editing, setEditing] = useState(false);
  const [values, setValues] = useState(() => ({
    fullName: profile.fullName ?? "",
  }));
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  function startEditing() {
    setValues({
      fullName: profile.fullName ?? "",
    });
    setErrors({});
    setEditing(true);
  }

  async function submit(event) {
    event.preventDefault();
    const nextErrors = validateProfile(values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    setBusy(true);
    try {
      await onSave(values);
      setEditing(false);
    } catch (error) {
      setErrors({ form: error.message });
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">PERSONAL PROFILE</span><h2>Personal and contact information</h2></div><UserRound size={18} /></div>
      <form onSubmit={submit}>
        <div className="profile-form-grid">
          <label className="profile-field">
            <span>Full name</span>
            <input value={values.fullName} disabled={!editing || busy} maxLength={100}
              onChange={(event) => setValues((current) => ({ ...current, fullName: event.target.value }))} />
            {errors.fullName && <small className="profile-field-error">{errors.fullName}</small>}
          </label>
          <label className="profile-field">
            <span>Email <small>Authentication email · read only</small></span>
            <div className="profile-input-icon"><Mail size={15} /><input value={profile.email ?? ""} readOnly aria-readonly="true" /></div>
          </label>
          {[
            ["phoneNumber", "Phone number"],
            ["city", "City"],
            ["state", "State"],
            ["country", "Country"],
          ].map(([field, label]) => (
            <label className="profile-field" key={field}>
              <span>{label}</span>
              <input value={values[field]} disabled={!editing || busy} maxLength={field === "phoneNumber" ? 25 : 100}
                onChange={(event) => setValues((current) => ({ ...current, [field]: event.target.value }))} />
              {errors[field] && <small className="profile-field-error">{errors[field]}</small>}
            </label>
          ))}
        </div>
        {errors.form && <p className="profile-field-error" role="alert">{errors.form}</p>}
        <div className="profile-form-actions">
          {editing ? <>
            <button className="profile-primary-button" type="submit" disabled={busy}><Save size={15} />{busy ? "Saving…" : "Save changes"}</button>
            <button className="profile-secondary-button" type="button" disabled={busy} onClick={() => { setEditing(false); setErrors({}); }}>Cancel</button>
          </> : <button className="profile-secondary-button" type="button" onClick={startEditing}>Edit profile</button>}
        </div>
      </form>
    </section>
  );
}
