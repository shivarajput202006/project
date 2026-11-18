import { GraduationCap } from "lucide-react";

export default function EducationComparison({ education }) {
  return (
    <section className="match-compare-card">
      <span className="match-compare-icon"><GraduationCap size={16} /></span>
      <div><h3>Education</h3><p>{education.explanation}</p>
        {education.available && <small>{education.resumeEvidence} · listing: {education.listingRequirement}</small>}
      </div>
    </section>
  );
}
