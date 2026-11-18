import { Filter, RotateCcw } from "lucide-react";
import { REPORT_TYPES } from "../utils/reportHelpers.js";

export default function ReportFilters({ filters, onChange, onApply, onClear }) {
  return (
    <form className="reports-filter-panel" onSubmit={(event) => { event.preventDefault(); onApply(); }}>
      <div className="reports-filter-heading"><Filter size={15} /><strong>Filter history</strong></div>
      <div className="reports-filter-fields">
        <label>Category
          <select value={filters.type} onChange={(event) => onChange("type", event.target.value)}>
            {REPORT_TYPES.map((type) => <option key={type.value} value={type.value}>{type.label}</option>)}
          </select>
        </label>
        <label>Status
          <select value={filters.status} onChange={(event) => onChange("status", event.target.value)}>
            <option value="">All statuses</option><option value="COMPLETED">Completed</option>
          </select>
        </label>
        <label>From date
          <input type="date" value={filters.fromDate} onChange={(event) => onChange("fromDate", event.target.value)} />
        </label>
        <label>To date
          <input type="date" value={filters.toDate} onChange={(event) => onChange("toDate", event.target.value)} />
        </label>
        <label>Sort
          <select value={filters.sort} onChange={(event) => onChange("sort", event.target.value)}>
            <option value="NEWEST">Newest first</option><option value="OLDEST">Oldest first</option>
          </select>
        </label>
      </div>
      <div className="reports-filter-actions">
        <button className="reports-primary-button" type="submit">Apply filters</button>
        <button className="reports-secondary-button" type="button" onClick={onClear}><RotateCcw size={14} /> Clear</button>
      </div>
    </form>
  );
}
