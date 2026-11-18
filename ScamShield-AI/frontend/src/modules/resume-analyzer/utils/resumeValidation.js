export const MAX_RESUME_BYTES = 5 * 1024 * 1024;

const SUPPORTED_TYPES = {
  pdf: "application/pdf",
  docx: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
};

export function validateResumeFile(file) {
  if (!file) return "Choose a PDF or DOCX resume.";
  if (file.size === 0) return "The selected file is empty.";
  if (file.size > MAX_RESUME_BYTES) return "Resume files must be 5 MB or smaller.";
  const extension = file.name.split(".").pop()?.toLowerCase();
  if (!Object.hasOwn(SUPPORTED_TYPES, extension)) {
    return "Only PDF and DOCX resumes are supported.";
  }
  if (file.type && file.type.toLowerCase() !== SUPPORTED_TYPES[extension]) {
    return "The file type does not match its extension.";
  }
  return "";
}

export function formatResumeFileSize(bytes) {
  if (!Number.isFinite(bytes) || bytes < 0) return "Unknown size";
  return bytes < 1024 * 1024
    ? `${Math.max(1, Math.round(bytes / 1024))} KB`
    : `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}
