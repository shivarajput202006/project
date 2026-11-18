import JobCard from "./JobCard.jsx";

export default function JobList({ jobs, savedIds, onSave, onSelect, savingIds }) {
  return (
    <div className="job-list" aria-live="polite">
      {jobs.map((job) => (
        <JobCard
          key={job.id}
          job={job}
          saved={savedIds.has(job.id)}
          onSave={onSave}
          onSelect={onSelect}
          saving={savingIds.has(job.id)}
        />
      ))}
    </div>
  );
}
