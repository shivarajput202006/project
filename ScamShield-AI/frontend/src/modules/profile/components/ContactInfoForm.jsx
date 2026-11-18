import { MapPin, Save } from "lucide-react";
import { useState } from "react";
import { validateProfile } from "../utils/profileValidation.js";

const CONTACT_FIELDS = [
  ["phoneNumber", "Phone number"],
  ["city", "City"],
  ["state", "State"],
  ["country", "Country"],
];

export default function ContactInfoForm({ profile, onSave }) {
  const [editing, setEditing] = useState(false);
  const [values, setValues] = useState(() => Object.fromEntries(
    CONTACT_FIELDS.map(([field]) => [field, profile[field] ?? ""]),
  ));
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  function edit() {
    setValues(Object.fromEntries(CONTACT_FIELDS.map(([field]) => [field, profile[field] ?? ""])));
    setErrors({});
    setEditing(true);
  }

  async function submit(event) {
    event.preventDefault();
    const validationErrors = validateProfile({ ...values, fullName: profile.fullName });
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length) return;
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
      <div className="profile-card-heading"><div><span className="profile-eyebrow">CONTACT INFORMATION</span><h2>How to reach you</h2></div><MapPin size={18} /></div>
      <form onSubmit={submit}>
        <div className="profile-form-grid">
          {CONTACT_FIELDS.map(([field, label]) => (
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
            <button className="profile-primary-button" type="submit" disabled={busy}><Save size={15} />{busy ? "Saving…" : "Save contact info"}</button>
            <button className="profile-secondary-button" type="button" disabled={busy} onClick={() => setEditing(false)}>Cancel</button>
          </> : <button className="profile-secondary-button" type="button" onClick={edit}>Edit contact information</button>}
        </div>
      </form>
    </section>
  );
}
