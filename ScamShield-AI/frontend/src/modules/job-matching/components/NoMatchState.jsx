import { FileSearch, SearchX } from "lucide-react";

export default function NoMatchState({ title, description, needsResume = false }) {
  const Icon = needsResume ? FileSearch : SearchX;
  return (
    <section className="resume-empty-state">
      <span><Icon size={21} aria-hidden="true" /></span>
      <h2>{title}</h2>
      <p>{description}</p>
    </section>
  );
}
