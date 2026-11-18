import { FileText, ShieldCheck } from "lucide-react";
import { formatResumeFileSize } from "../utils/resumeValidation.js";

export default function FilePreview({ file, resume }) {
  if (!file && !resume) return null;
  const filename = file?.name ?? resume.originalFilename;
  const size = file ? formatResumeFileSize(file.size) : `${resume.fileType} · privately stored`;
  return (
    <section className="resume-file-preview" aria-label="Selected resume details">
      <span className="resume-file-icon"><FileText size={20} aria-hidden="true" /></span>
      <span className="resume-file-copy"><strong>{filename}</strong><small>{size}</small></span>
      <span className="resume-private-indicator"><ShieldCheck size={15} aria-hidden="true" /> Private</span>
    </section>
  );
}
