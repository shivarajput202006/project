import { BookOpen, Pencil, Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import EducationForm from "./EducationForm.jsx";

export default function EducationList({ education, onAdd, onUpdate, onDelete }) {
  const [editing, setEditing] = useState(null);
  const [adding, setAdding] = useState(false);
  const [pendingId, setPendingId] = useState("");
  const [error, setError] = useState("");

  async function save(item) {
    setError("");
    try {
      if (editing) await onUpdate(editing.id, item);
      else await onAdd(item);
      setEditing(null);
      setAdding(false);
    } catch (saveError) {
      throw saveError;
    }
  }

  async function remove(item) {
    if (!window.confirm(`Remove ${item.degree} at ${item.institutionName} from your profile?`)) return;
    setPendingId(item.id);
    setError("");
    try {
      await onDelete(item.id);
    } catch (deleteError) {
      setError(deleteError.message);
    } finally {
      setPendingId("");
    }
  }

  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">EDUCATION DETAILS</span><h2>Education</h2></div>
        {!adding && !editing && <button className="profile-secondary-button" type="button" onClick={() => setAdding(true)}><Plus size={15} /> Add education</button>}
      </div>
      {error && <p className="profile-field-error" role="alert">{error}</p>}
      {adding && <EducationForm onSave={save} onCancel={() => setAdding(false)} />}
      {editing && <EducationForm key={editing.id} initialValue={editing} onSave={save} onCancel={() => setEditing(null)} />}
      {!adding && !editing && (education.length ? (
        <div className="profile-education-list">
          {education.map((item) => (
            <article className="profile-education-item" key={item.id}>
              <span className="profile-education-icon"><BookOpen size={17} /></span>
              <div className="profile-education-copy">
                <h3>{item.degree} · {item.course}</h3>
                <p>{item.institutionName}{item.specialization ? ` · ${item.specialization}` : ""}</p>
                <small>{item.startYear} – {item.endYear ?? "Present"} · {item.status === "COMPLETED" ? "Completed" : "In progress"}
                  {item.gradeValue != null && ` · ${item.gradeType}: ${item.gradeValue}`}</small>
              </div>
              <div className="profile-item-actions">
                <button type="button" aria-label={`Edit ${item.degree}`} onClick={() => setEditing(item)}><Pencil size={15} /></button>
                <button type="button" aria-label={`Delete ${item.degree}`} disabled={pendingId === item.id} onClick={() => remove(item)}><Trash2 size={15} /></button>
              </div>
            </article>
          ))}
        </div>
      ) : <div className="profile-empty-state"><BookOpen size={20} /><p>No education details added yet.</p><span>Add education to complete your profile.</span></div>)}
    </section>
  );
}
