import { ArrowUpRight, GraduationCap } from "lucide-react";

export default function SkillRoadmap({ items }) {
  if (!items.length) return <p className="match-note">No skill roadmap is needed from the currently identified listing skills.</p>;
  return (
    <div className="match-roadmap-list">
      {items.map((item) => (
        <article className="match-roadmap-item" key={item.skill}>
          <span className="match-compare-icon"><GraduationCap size={16} /></span>
          <div><h4>{item.skill}</h4><p>{item.whyItMatters}</p>
            <ol>{item.learningSteps.map((step) => <li key={step}>{step}</li>)}</ol>
            <small><ArrowUpRight size={13} /> Project idea: {item.practiceProject}</small>
          </div>
        </article>
      ))}
    </div>
  );
}
