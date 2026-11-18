export const REPORT_TYPES = [
  { value: "", label: "All categories" },
  { value: "SCAM_DETECTION", label: "Scam detection" },
  { value: "URL_SCAN", label: "URL scans" },
  { value: "RESUME_ANALYSIS", label: "Resume analysis" },
  { value: "JOB_MATCHING", label: "Job matching" },
];

export function reportTypeLabel(type) {
  return REPORT_TYPES.find((item) => item.value === type)?.label ?? "Report";
}

export function formatReportDate(value) {
  if (!value) return "Date unavailable";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "Date unavailable"
    : new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}
