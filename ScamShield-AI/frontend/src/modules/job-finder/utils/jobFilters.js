export function toSearchParams(filters = {}, page = 1, size = 20) {
  const params = new URLSearchParams();
  for (const key of ["keyword", "location", "workMode", "experience", "jobType", "salaryMin"]) {
    const value = filters[key];
    if (value !== undefined && value !== null && String(value).trim() !== "") {
      params.set(key, String(value).trim());
    }
  }
  params.set("page", String(page));
  params.set("size", String(size));
  return params;
}

export function hasSalaryData(jobs = []) {
  return jobs.some((job) => Number.isFinite(job.salaryMin) || Number.isFinite(job.salaryMax));
}

export function workModeLabel(workMode) {
  return ({ REMOTE: "Remote", HYBRID: "Hybrid", ON_SITE: "On-site" })[workMode] ?? "Not specified";
}

export function experienceLabel(experience) {
  return ({
    FRESHER: "Fresher",
    ENTRY_LEVEL: "Entry level",
    ONE_TO_THREE_YEARS: "1–3 years",
    THREE_TO_FIVE_YEARS: "3–5 years",
  })[experience] ?? "Not specified";
}

export function jobTypeLabel(jobType) {
  return ({
    FULL_TIME: "Full-time",
    INTERNSHIP: "Internship",
    PART_TIME: "Part-time",
    CONTRACT: "Contract",
  })[jobType] ?? "Not specified";
}
