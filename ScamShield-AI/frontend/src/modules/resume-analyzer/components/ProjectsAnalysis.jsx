export default function ProjectsAnalysis({ projects = [], certifications = [] }) {
  return (
    <section className="resume-card" aria-labelledby="resume-projects-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">SELECTED WORK</span><h2 id="resume-projects-title">Projects & certifications</h2></div></div>
      {projects.length
        ? <ul className="resume-project-list">{projects.map((project, index) => (
          <li key={`${project.name}-${index}`}><strong>{project.name}</strong>
            {project.description && <p>{project.description}</p>}
            {project.technologies?.length > 0 && <div className="resume-chip-list">{project.technologies.map((technology) => <span key={technology}>{technology}</span>)}</div>}
            {project.url && <span className="resume-muted">{project.url}</span>}
          </li>
        ))}</ul>
        : <p className="resume-muted">No projects were identified.</p>}
      <h3 className="resume-subheading">Certifications</h3>
      {certifications.length
        ? <ul className="resume-cert-list">{certifications.map((certification) => <li key={certification}>{certification}</li>)}</ul>
        : <p className="resume-muted">No certifications were identified.</p>}
    </section>
  );
}
