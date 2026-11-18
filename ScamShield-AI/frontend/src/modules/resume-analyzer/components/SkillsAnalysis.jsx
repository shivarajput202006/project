import { Check } from "lucide-react";

function SkillGroup({ title, skills }) {
  return (
    <div className="resume-skill-group">
      <h3>{title}</h3>
      {skills.length
        ? <ul className="resume-skill-list">{skills.map((skill) => <li key={skill}><Check size={13} />{skill}</li>)}</ul>
        : <p className="resume-muted">No {title.toLowerCase()} were explicitly identified.</p>}
    </div>
  );
}

export default function SkillsAnalysis({ skillAnalysis }) {
  if (!skillAnalysis) return null;
  return (
    <section className="resume-card" aria-labelledby="resume-skills-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">RESUME EVIDENCE</span><h2 id="resume-skills-title">Skills analysis</h2></div></div>
      <SkillGroup title="Technical skills" skills={skillAnalysis.technicalSkills ?? []} />
      <SkillGroup title="Soft skills" skills={skillAnalysis.softSkills ?? []} />
    </section>
  );
}
