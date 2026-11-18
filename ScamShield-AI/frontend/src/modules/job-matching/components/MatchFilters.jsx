import { Filter } from "lucide-react";

export default function MatchFilters({ value, onChange, onSubmit, onClear, disabled }) {
  function update(key, next) {
    onChange((current) => ({ ...current, [key]: next }));
  }
  return (
    <form className="resume-card match-filter-form" onSubmit={(event) => { event.preventDefault(); onSubmit(); }}>
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">PERSONALIZE</span><h2><Filter size={15} /> Matching filters</h2></div></div>
      <div className="match-filter-grid">
        <label className="match-field"><span>Role or keyword</span><input value={value.keyword} maxLength={120} placeholder="Use resume role suggestions by default" onChange={(event) => update("keyword", event.target.value)} /></label>
        <label className="match-field"><span>Preferred location</span><input value={value.location} maxLength={100} placeholder="Optional" onChange={(event) => update("location", event.target.value)} /></label>
        <label className="match-field"><span>Work mode</span><select value={value.workMode} onChange={(event) => update("workMode", event.target.value)}><option value="">Any mode</option><option value="REMOTE">Remote</option><option value="HYBRID">Hybrid</option><option value="ON_SITE">On-site</option></select></label>
        <label className="match-field"><span>Job type</span><select value={value.jobType} onChange={(event) => update("jobType", event.target.value)}><option value="">Any type</option><option value="FULL_TIME">Full-time</option><option value="INTERNSHIP">Internship</option><option value="PART_TIME">Part-time</option><option value="CONTRACT">Contract</option></select></label>
      </div>
      <div className="match-filter-actions">
        <button className="resume-primary-button" type="submit" disabled={disabled}>Apply filters</button>
        <button className="match-clear-button" type="button" disabled={disabled} onClick={onClear}>Clear filters</button>
      </div>
    </form>
  );
}
