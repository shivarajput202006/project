import { SlidersHorizontal } from "lucide-react";

const options = {
  workMode: [["REMOTE", "Remote"], ["HYBRID", "Hybrid"], ["ON_SITE", "On-site"]],
  experience: [
    ["FRESHER", "Fresher"],
    ["ENTRY_LEVEL", "Entry level"],
    ["ONE_TO_THREE_YEARS", "1–3 years"],
    ["THREE_TO_FIVE_YEARS", "3–5 years"],
  ],
  jobType: [
    ["FULL_TIME", "Full-time"],
    ["INTERNSHIP", "Internship"],
    ["PART_TIME", "Part-time"],
    ["CONTRACT", "Contract"],
  ],
};

function SelectFilter({ label, value, options: choices, onChange }) {
  return (
    <label className="job-filter-control">
      <span>{label}</span>
      <select value={value} onChange={(event) => onChange(event.target.value)}>
        <option value="">All</option>
        {choices.map(([key, text]) => <option key={key} value={key}>{text}</option>)}
      </select>
    </label>
  );
}

export default function JobFilters({ values, onChange, salaryAvailable }) {
  return (
    <section className="job-filters" aria-label="Job filters">
      <div className="job-filters-heading"><SlidersHorizontal size={16} /><span>Refine your search</span></div>
      <SelectFilter label="Work mode" value={values.workMode} options={options.workMode}
        onChange={(value) => onChange("workMode", value)} />
      <SelectFilter label="Experience" value={values.experience} options={options.experience}
        onChange={(value) => onChange("experience", value)} />
      <SelectFilter label="Job type" value={values.jobType} options={options.jobType}
        onChange={(value) => onChange("jobType", value)} />
      {salaryAvailable && (
        <label className="job-filter-control">
          <span>Minimum salary</span>
          <input
            type="number"
            min="0"
            step="1000"
            value={values.salaryMin}
            placeholder="Any"
            onChange={(event) => onChange("salaryMin", event.target.value)}
          />
        </label>
      )}
    </section>
  );
}
