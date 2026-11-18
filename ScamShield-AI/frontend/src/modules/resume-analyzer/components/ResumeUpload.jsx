import { useRef, useState } from "react";
import { FileUp, FileText } from "lucide-react";
import { formatResumeFileSize, validateResumeFile } from "../utils/resumeValidation.js";

export default function ResumeUpload({ file, disabled, onSelect }) {
  const inputRef = useRef(null);
  const [dragging, setDragging] = useState(false);
  const [error, setError] = useState("");

  function choose(candidate) {
    const validationMessage = validateResumeFile(candidate);
    setError(validationMessage);
    if (!validationMessage) onSelect(candidate);
    else onSelect(null);
  }

  return (
    <section className="resume-upload-panel" aria-labelledby="resume-upload-title">
      <h2 id="resume-upload-title">Upload your resume</h2>
      <p>PDF or DOCX, up to 5 MB. Your file is stored privately in your account.</p>
      <input
        ref={inputRef}
        className="resume-visually-hidden"
        type="file"
        accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        onChange={(event) => {
          choose(event.target.files?.[0] ?? null);
          event.target.value = "";
        }}
      />
      <button
        className={`resume-dropzone${dragging ? " is-dragging" : ""}`}
        type="button"
        disabled={disabled}
        onClick={() => inputRef.current?.click()}
        onDragOver={(event) => { event.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={(event) => {
          event.preventDefault();
          setDragging(false);
          choose(event.dataTransfer.files?.[0] ?? null);
        }}
        aria-describedby="resume-upload-help"
      >
        <input
          ref={inputRef}
          className="resume-visually-hidden"
          type="file"
          accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
          onChange={(event) => {
            choose(event.target.files?.[0] ?? null);
            event.target.value = "";
          }}
        />
        <span className="resume-upload-icon"><FileUp size={22} aria-hidden="true" /></span>
        <strong>{file ? "Choose a different file" : "Choose a file or drag it here"}</strong>
        <span id="resume-upload-help">PDF and DOCX documents only · 5 MB maximum</span>
      </button>
      {error && <p className="resume-inline-error" role="alert">{error}</p>}
      {file && (
        <div className="resume-selected-file">
          <FileText size={18} aria-hidden="true" />
          <span><strong>{file.name}</strong><small>{formatResumeFileSize(file.size)}</small></span>
          <button type="button" onClick={() => { onSelect(null); setError(""); }}>Remove</button>
        </div>
      )}
    </section>
  );
}
