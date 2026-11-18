import { Clock3 } from "lucide-react";

export default function ExperienceComparison({ experience }) {
  return (
    <section className="match-compare-card">
      <span className="match-compare-icon"><Clock3 size={16} /></span>
      <div><h3>Experience</h3><p>{experience.explanation}</p>
        {experience.available && <small>{experience.documentedYears} documented years · {experience.listingExperience?.replaceAll("_", " ").toLowerCase()}</small>}
      </div>
    </section>
  );
}
