import { FileCheck2 } from "lucide-react";

export default function ResumeSelector({ resumes, value, onChange, disabled }) {
  const analyzed = resumes.filter((resume) => resume.latestAnalysisAt);
  return (
    <label className="match-field">
      <span><FileCheck2 size={15} aria-hidden="true" /> Analyzed resume</span>
      <select value={value} onChange={(event) => onChange(event.target.value)} disabled={disabled || analyzed.length === 0}>
        <option value="">Select an analyzed resume</option>
        {analyzed.map((resume) => (
          <option key={resume.id} value={resume.id}>
            {resume.originalFilename} · {new Date(resume.latestAnalysisAt).toLocaleDateString()}
          </option>
        ))}
      </select>
    </label>
  );
}
