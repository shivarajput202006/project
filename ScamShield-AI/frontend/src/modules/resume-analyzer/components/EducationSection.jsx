export default function EducationSection({ education = [] }) {
  return (
    <section className="resume-card" aria-labelledby="resume-education-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">ACADEMIC BACKGROUND</span><h2 id="resume-education-title">Education</h2></div></div>
      {education.length
        ? <ul className="resume-timeline">{education.map((item, index) => (
          <li key={`${item.institution}-${index}`}><strong>{item.degree || item.fieldOfStudy || "Education"}</strong>
            <span>{[item.institution, item.fieldOfStudy].filter(Boolean).join(" · ")}</span>
            <small>{[item.startDate, item.endDate].filter(Boolean).join(" — ")}</small>
            {item.details && <p>{item.details}</p>}
          </li>
        ))}</ul>
        : <p className="resume-muted">No education details were identified.</p>}
    </section>
  );
}
