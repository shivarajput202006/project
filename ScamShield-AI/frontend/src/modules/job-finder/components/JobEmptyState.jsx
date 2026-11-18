import { SearchX } from "lucide-react";

export default function JobEmptyState({ icon: Icon = SearchX, title, description, actionLabel, onAction }) {
  return (
    <section className="job-empty-state" role="status">
      <span><Icon size={22} aria-hidden="true" /></span>
      <h2>{title}</h2>
      <p>{description}</p>
      {actionLabel && <button className="job-secondary-button" type="button" onClick={onAction}>{actionLabel}</button>}
    </section>
  );
}
