export default function ExperienceSection({ experience = [] }) {
  return (
    <section className="resume-card" aria-labelledby="resume-experience-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">CAREER HISTORY</span><h2 id="resume-experience-title">Experience</h2></div></div>
      {experience.length
        ? <ul className="resume-timeline">{experience.map((item, index) => (
          <li key={`${item.company}-${item.title}-${index}`}>
            <span className="resume-tag">{item.kind === "INTERNSHIP" ? "Internship" : "Work"}</span>
            <strong>{item.title || "Role not specified"}</strong>
            <span>{[item.company, item.location].filter(Boolean).join(" · ")}</span>
            <small>{[item.startDate, item.endDate].filter(Boolean).join(" — ")}</small>
            {item.summary && <p>{item.summary}</p>}
          </li>
        ))}</ul>
        : <p className="resume-muted">No experience details were identified.</p>}
    </section>
  );
}
