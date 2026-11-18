import { Save, X } from "lucide-react";
import { useState } from "react";
import { validateEducation } from "../utils/profileValidation.js";

const EMPTY_EDUCATION = {
  institutionName: "",
  degree: "",
  course: "",
  specialization: "",
  startYear: "",
  endYear: "",
  gradeType: "",
  gradeValue: "",
  status: "IN_PROGRESS",
};

export default function EducationForm({ initialValue, onSave, onCancel }) {
  const [values, setValues] = useState(() => initialValue ? {
    ...initialValue,
    startYear: String(initialValue.startYear),
    endYear: initialValue.endYear ? String(initialValue.endYear) : "",
    gradeValue: initialValue.gradeValue ?? "",
    status: initialValue.status ?? "IN_PROGRESS",
  } : EMPTY_EDUCATION);
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  function set(field, value) {
    setValues((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    const nextErrors = validateEducation(values);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    const request = {
      ...values,
      startYear: Number(values.startYear),
      endYear: values.endYear ? Number(values.endYear) : null,
      gradeType: values.gradeType || null,
      gradeValue: values.gradeValue === "" ? null : Number(values.gradeValue),
    };
    setBusy(true);
    try {
      await onSave(request);
    } catch (error) {
      setErrors({ form: error.message });
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="profile-education-form" onSubmit={submit}>
      <div className="profile-form-grid">
        <Field label="Institution" value={values.institutionName} error={errors.institutionName} onChange={(value) => set("institutionName", value)} />
        <Field label="Degree" value={values.degree} error={errors.degree} onChange={(value) => set("degree", value)} />
        <Field label="Course" value={values.course} error={errors.course} onChange={(value) => set("course", value)} />
        <Field label="Specialization" value={values.specialization} onChange={(value) => set("specialization", value)} />
        <label className="profile-field"><span>Start year</span><input type="number" min="1950" max={new Date().getFullYear() + 5} value={values.startYear} onChange={(event) => set("startYear", event.target.value)} />{errors.startYear && <small className="profile-field-error">{errors.startYear}</small>}</label>
        <label className="profile-field"><span>End year</span><input type="number" min="1950" max="2100" value={values.endYear} disabled={values.status === "IN_PROGRESS"} onChange={(event) => set("endYear", event.target.value)} />{errors.endYear && <small className="profile-field-error">{errors.endYear}</small>}</label>
        <label className="profile-field"><span>Status</span><select value={values.status} onChange={(event) => {
          const status = event.target.value;
          setValues((current) => ({ ...current, status, endYear: status === "IN_PROGRESS" ? "" : current.endYear }));
        }}><option value="IN_PROGRESS">In progress</option><option value="COMPLETED">Completed</option></select></label>
        <label className="profile-field"><span>Grade type</span><select value={values.gradeType} onChange={(event) => set("gradeType", event.target.value)}><option value="">Not provided</option><option value="CGPA">CGPA (0–10)</option><option value="PERCENTAGE">Percentage (0–100)</option></select></label>
        <label className="profile-field"><span>Grade value</span><input type="number" min="0" max={values.gradeType === "CGPA" ? "10" : "100"} step="0.01" value={values.gradeValue} disabled={!values.gradeType} onChange={(event) => set("gradeValue", event.target.value)} />{errors.gradeValue && <small className="profile-field-error">{errors.gradeValue}</small>}</label>
      </div>
      {errors.form && <p className="profile-field-error" role="alert">{errors.form}</p>}
      <div className="profile-form-actions">
        <button className="profile-primary-button" type="submit" disabled={busy}><Save size={15} />{busy ? "Saving…" : "Save education"}</button>
        <button className="profile-secondary-button" type="button" disabled={busy} onClick={onCancel}><X size={15} /> Cancel</button>
      </div>
    </form>
  );
}

function Field({ label, value, onChange, error }) {
  return <label className="profile-field"><span>{label}</span><input maxLength={150} value={value ?? ""} onChange={(event) => onChange(event.target.value)} />{error && <small className="profile-field-error">{error}</small>}</label>;
}
