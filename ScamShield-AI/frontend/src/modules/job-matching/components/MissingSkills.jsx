import { Info } from "lucide-react";

export default function MissingSkills({ skills }) {
  return (
    <div className="match-skill-group is-missing">
      <h4><Info size={14} /> Mentioned in listing, not evidenced in resume</h4>
      {skills.length ? <ul className="match-skill-tags">{skills.map((skill) => <li key={skill}>{skill}</li>)}</ul>
        : <p>No additional listing skills were identified.</p>}
    </div>
  );
}
