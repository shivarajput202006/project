import { Files, SearchX } from "lucide-react";

export default function ReportEmptyState({ filtered }) {
  const Icon = filtered ? SearchX : Files;
  return (
    <section className="reports-empty-state">
      <span><Icon size={24} /></span>
      <h2>{filtered ? "No reports match these filters" : "Your report history is empty"}</h2>
      <p>{filtered
        ? "Try another category, search term, or date range."
        : "Completed scam checks, URL scans, resume analyses, and job matches will appear here."}</p>
    </section>
  );
}
