import { Bookmark } from "lucide-react";
import JobEmptyState from "./JobEmptyState.jsx";
import JobList from "./JobList.jsx";

export default function SavedJobs({
  jobs,
  loading,
  error,
  savedIds,
  onSave,
  onSelect,
  savingIds,
  onRetry,
}) {
  if (loading) return null;
  if (error) {
    return <JobEmptyState icon={Bookmark} title="Saved jobs are unavailable" description={error} actionLabel="Try again" onAction={onRetry} />;
  }
  if (!jobs.length) {
    return <JobEmptyState icon={Bookmark} title="No saved jobs yet" description="Save a listing from Live Job Finder to keep it here." />;
  }
  return <JobList jobs={jobs} savedIds={savedIds} onSave={onSave} onSelect={onSelect} savingIds={savingIds} />;
}
