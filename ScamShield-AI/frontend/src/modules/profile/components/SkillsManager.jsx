import { Check, Pencil, Plus, Save, Trash2, X } from "lucide-react";
import { useMemo, useState } from "react";

const CATEGORIES = [
  ["PROGRAMMING_LANGUAGES", "Programming languages"],
  ["FRAMEWORKS", "Frameworks"],
  ["DATABASES", "Databases"],
  ["DEVELOPMENT_TOOLS", "Development tools"],
  ["OTHER_TECHNICAL_SKILLS", "Other technical skills"],
];
const LABELS = Object.fromEntries(CATEGORIES);

export default function SkillsManager({ skills, onSave }) {
  const [items, setItems] = useState(skills ?? []);
  const [name, setName] = useState("");
  const [category, setCategory] = useState(CATEGORIES[0][0]);
  const [editing, setEditing] = useState(-1);
  const [draft, setDraft] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const grouped = useMemo(() => CATEGORIES.map(([key, label]) => ({
    key, label, values: items.map((skill, index) => ({ ...skill, index })).filter((skill) => skill.category === key),
  })), [items]);

  function addSkill(event) {
    event.preventDefault();
    const value = name.trim().replace(/\s+/g, " ");
    if (!value) return;
    if (items.some((skill) => skill.name.toLowerCase() === value.toLowerCase())) {
      setError("That skill is already in your profile.");
      return;
    }
    setItems((current) => [...current, { name: value, category, source: "USER_PROVIDED" }]);
    setName("");
    setError("");
  }

  function beginEdit(index) {
    setEditing(index);
    setDraft({ ...items[index] });
  }

  function commitEdit() {
    const value = draft.name.trim().replace(/\s+/g, " ");
    if (!value) return setError("Skill name cannot be empty.");
    if (items.some((skill, index) => index !== editing && skill.name.toLowerCase() === value.toLowerCase())) {
      return setError("That skill is already in your profile.");
    }
    setItems((current) => current.map((skill, index) => index === editing ? { ...skill, ...draft, name: value } : skill));
    setEditing(-1);
    setDraft(null);
    setError("");
  }

  async function save() {
    setBusy(true);
    setError("");
    try {
      await onSave(items.map(({ name: skillName, category: skillCategory }) => ({ name: skillName, category: skillCategory })));
    } catch (saveError) {
      setError(saveError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">TECHNICAL SKILLS</span><h2>Your skills</h2></div><span className="profile-user-provided">User-provided</span></div>
      <p className="profile-section-description">These are profile skills you entered. They do not overwrite or certify skills extracted from your resumes.</p>
      <form className="profile-skill-add" onSubmit={addSkill}>
        <input aria-label="Skill name" placeholder="Add a technical skill" maxLength={60} value={name} onChange={(event) => setName(event.target.value)} />
        <select aria-label="Skill category" value={category} onChange={(event) => setCategory(event.target.value)}>{CATEGORIES.map(([key, label]) => <option value={key} key={key}>{label}</option>)}</select>
        <button className="profile-primary-button" type="submit"><Plus size={15} /> Add</button>
      </form>
      {error && <p className="profile-field-error" role="alert">{error}</p>}
      <div className="profile-skill-groups">
        {grouped.map((group) => (
          <div className="profile-skill-group" key={group.key}>
            <h3>{group.label}</h3>
            {group.values.length ? <div className="profile-skill-chips">{group.values.map((skill) => (
              <span className="profile-skill-chip" key={`${skill.index}-${skill.name}`}>
                {editing === skill.index ? <>
                  <input aria-label="Edit skill name" value={draft.name} onChange={(event) => setDraft((current) => ({ ...current, name: event.target.value }))} />
                  <select aria-label="Edit skill category" value={draft.category} onChange={(event) => setDraft((current) => ({ ...current, category: event.target.value }))}>{CATEGORIES.map(([key, label]) => <option value={key} key={key}>{label}</option>)}</select>
                  <button type="button" aria-label="Save skill edit" onClick={commitEdit}><Check size={14} /></button>
                  <button type="button" aria-label="Cancel skill edit" onClick={() => { setEditing(-1); setDraft(null); }}><X size={14} /></button>
                </> : <>
                  <span>{skill.name}</span><small>{LABELS[skill.category] ?? "Other technical skills"}</small>
                  <button type="button" aria-label={`Edit ${skill.name}`} onClick={() => beginEdit(skill.index)}><Pencil size={13} /></button>
                  <button type="button" aria-label={`Remove ${skill.name}`} onClick={() => setItems((current) => current.filter((_, index) => index !== skill.index))}><Trash2 size={13} /></button>
                </>}
              </span>
            ))}</div> : <p className="profile-skill-empty">No skills in this category.</p>}
          </div>
        ))}
      </div>
      <div className="profile-form-actions"><button type="button" className="profile-primary-button" disabled={busy} onClick={save}><Save size={15} />{busy ? "Saving…" : "Save skills"}</button></div>
    </section>
  );
}
