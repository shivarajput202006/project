export default function MissingSkills({ roleSkillGaps = [] }) {
  if (!roleSkillGaps.length) return null;
  return (
    <section className="resume-card" aria-labelledby="resume-missing-skills-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">ROLE-SPECIFIC CONTEXT</span><h2 id="resume-missing-skills-title">Skills not evidenced in resume</h2></div></div>
      <p className="resume-muted">Potential gaps suggested for these roles are not confirmed employer requirements and do not mean you lack the skill.</p>
      <div className="resume-gap-list">{roleSkillGaps.map((gap) => (
        <article key={gap.role}><h3>{gap.role}</h3>
          {gap.notEvidencedSkills.length
            ? <div className="resume-chip-list">{gap.notEvidencedSkills.map((skill) => <span key={skill}>{skill}</span>)}</div>
            : <p className="resume-muted">No specific gaps were suggested.</p>}
        </article>
      ))}</div>
    </section>
  );
}
