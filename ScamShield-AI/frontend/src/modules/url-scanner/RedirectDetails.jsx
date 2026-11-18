import { GitBranch } from "lucide-react";

export default function RedirectDetails({ summary }) {
  return (
    <section className="url-result-panel">
      <div className="url-panel-heading"><GitBranch size={17} /><h3>Redirect information</h3></div>
      <p className="url-redirect-status">{summary?.status === "NOT_CHECKED" ? "Not checked" : "Unavailable"}</p>
      <p className="url-panel-note">
        {summary?.detail ?? "The submitted URL was not contacted; its redirect chain is unknown."}
      </p>
    </section>
  );
}
