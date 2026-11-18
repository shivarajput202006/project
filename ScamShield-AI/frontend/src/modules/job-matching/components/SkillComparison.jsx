import MatchingSkills from "./MatchingSkills.jsx";
import MissingSkills from "./MissingSkills.jsx";

export default function SkillComparison({ skills }) {
  return (
    <section className="resume-card">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">SKILL EVIDENCE</span><h2>Resume and listing comparison</h2></div></div>
      <p className="match-note">{skills.note}</p>
      <div className="match-skills-grid">
        <MatchingSkills skills={skills.matchedListingSkills} />
        <MissingSkills skills={skills.missingListingSkills} />
      </div>
      {!!skills.resumeSkills.length && <p className="match-resume-skills"><strong>Resume skills used:</strong> {skills.resumeSkills.join(", ")}</p>}
    </section>
  );
}
