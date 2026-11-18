export default function ATSFeedback({ feedback = [] }) {
  return (
    <section className="resume-card" aria-labelledby="resume-ats-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">FORMATTING GUIDANCE</span><h2 id="resume-ats-title">ATS-related feedback</h2></div></div>
      {feedback.length
        ? <ul className="resume-feedback-list">{feedback.map((item) => <li key={item}>{item}</li>)}</ul>
        : <p className="resume-muted">No ATS-related feedback was returned.</p>}
      <p className="resume-footnote">This is general guidance, not an official ATS evaluation or a guarantee of passing screening.</p>
    </section>
  );
}
