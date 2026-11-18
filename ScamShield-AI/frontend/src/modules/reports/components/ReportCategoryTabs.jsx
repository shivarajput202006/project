import { REPORT_TYPES } from "../utils/reportHelpers.js";

export default function ReportCategoryTabs({ selected, categories = [], onSelect }) {
  const total = categories.reduce((sum, item) => sum + item.count, 0);
  return (
    <div className="reports-category-tabs" role="tablist" aria-label="Report categories">
      <button type="button" role="tab" aria-selected={!selected} className={!selected ? "is-active" : ""}
        onClick={() => onSelect("")}>All <span>{total}</span></button>
      {REPORT_TYPES.filter((item) => item.value).map((item) => {
        const category = categories.find((entry) => entry.type === item.value);
        return (
          <button type="button" role="tab" key={item.value} aria-selected={selected === item.value}
            className={selected === item.value ? "is-active" : ""} onClick={() => onSelect(item.value)}>
            {item.label} <span>{category?.count ?? 0}</span>
          </button>
        );
      })}
    </div>
  );
}
