import { Search } from "lucide-react";

export default function ReportSearch({ value, onChange, onSubmit }) {
  return (
    <form className="reports-search" onSubmit={(event) => { event.preventDefault(); onSubmit(); }}>
      <Search size={16} aria-hidden="true" />
      <label className="sr-only" htmlFor="reports-keyword">Search report history</label>
      <input id="reports-keyword" value={value} maxLength={120} onChange={(event) => onChange(event.target.value)}
        placeholder="Search reports by title or findings" />
      <button type="submit">Search</button>
    </form>
  );
}
