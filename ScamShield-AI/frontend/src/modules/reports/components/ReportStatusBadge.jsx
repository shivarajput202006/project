import { CheckCircle2 } from "lucide-react";

export default function ReportStatusBadge({ status }) {
  return <span className="reports-status-badge"><CheckCircle2 size={13} />{status?.replaceAll("_", " ") ?? "Unknown"}</span>;
}
