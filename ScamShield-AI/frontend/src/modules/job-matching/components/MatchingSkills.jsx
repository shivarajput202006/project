import { Check } from "lucide-react";

export default function MatchingSkills({ skills }) {
  return (
    <div className="match-skill-group">
      <h4><Check size={14} /> Evidenced in both</h4>
      {skills.length ? <ul className="match-skill-tags">{skills.map((skill) => <li key={skill}>{skill}</li>)}</ul>
        : <p>No exact skill overlap was identified.</p>}
    </div>
  );
}
