export const CONTEXT_OPTIONS = [
  ["RESUME_ANALYSIS", "Resume analysis"],
  ["JOB_MATCHING", "Job matching"],
  ["LIVE_JOBS", "Recent live jobs"],
  ["SCAM_ANALYSES", "Scam analysis"],
  ["URL_SCANS", "URL scans"],
  ["PROFILE", "Profile skills and education"],
];

export function titleForConversation(value) {
  const clean = String(value ?? "").replace(/\s+/g, " ").trim();
  return clean.length <= 72 ? clean : `${clean.slice(0, 69)}...`;
}

export function formatChatTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" }).format(date);
}
