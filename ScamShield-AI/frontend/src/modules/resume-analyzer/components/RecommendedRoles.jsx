import { ArrowUpRight, BriefcaseBusiness } from "lucide-react";
import { Link } from "react-router-dom";

export default function RecommendedRoles({ recommendation, roles = [] }) {
  const recommendedRoles = recommendation?.roles ?? roles;
  if (!recommendedRoles.length && !recommendation) return null;
  return (
    <section className="resume-card" aria-labelledby="resume-roles-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">CAREER DIRECTIONS</span><h2 id="resume-roles-title">Recommended roles</h2></div></div>
      <p className="resume-muted">{recommendation?.message ?? "Suggestions are based on resume evidence, not a hiring prediction."}</p>
      {recommendedRoles.length
        ? <div className="resume-role-list">{recommendedRoles.map((role) => (
          <article key={role.title}>
            <span className="resume-role-icon"><BriefcaseBusiness size={17} /></span>
            <div><h3>{role.title}</h3><p>{role.rationale}</p>
              {role.matchingSkills?.length > 0 && <div className="resume-chip-list">{role.matchingSkills.map((skill) => <span key={skill}>{skill}</span>)}</div>}
              {role.missingSkills?.length > 0 && <small>Potential skills not evidenced: {role.missingSkills.join(", ")}</small>}
            </div>
            <Link to={`/live-jobs?keyword=${encodeURIComponent(role.title)}`} aria-label={`Search live jobs for ${role.title}`}>
              Search live jobs <ArrowUpRight size={14} aria-hidden="true" />
            </Link>
          </article>
        ))}</div>
        : <p className="resume-muted">No roles could be recommended from the available resume information.</p>}
      {recommendation && !recommendation.liveSearchConfigured && (
        <p className="resume-live-unavailable" role="status">Live listings are not configured. Role suggestions above do not include job postings.</p>
      )}
    </section>
  );
}
