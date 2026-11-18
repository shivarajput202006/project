import { MapPin, Search } from "lucide-react";

export default function JobSearchBar({ values, onChange, onSearch, onClear, loading }) {
  function update(field, value) {
    onChange((current) => ({ ...current, [field]: value }));
  }

  return (
    <form className="job-search-bar" onSubmit={(event) => { event.preventDefault(); onSearch(); }}>
      <label className="job-search-field">
        <Search size={18} aria-hidden="true" />
        <span className="sr-only">Job title, skills or keywords</span>
        <input
          type="search"
          value={values.keyword}
          maxLength={120}
          placeholder="Job title, skills or keywords"
          onChange={(event) => update("keyword", event.target.value)}
        />
      </label>
      <label className="job-search-field">
        <MapPin size={18} aria-hidden="true" />
        <span className="sr-only">Location</span>
        <input
          type="search"
          value={values.location}
          maxLength={100}
          placeholder="City, region or country"
          onChange={(event) => update("location", event.target.value)}
        />
      </label>
      <div className="job-search-actions">
        <button className="dashboard-primary-button" type="submit" disabled={loading}>
          <Search size={16} aria-hidden="true" /> Search
        </button>
        <button className="job-clear-button" type="button" onClick={onClear} disabled={loading}>
          Clear
        </button>
      </div>
    </form>
  );
}
