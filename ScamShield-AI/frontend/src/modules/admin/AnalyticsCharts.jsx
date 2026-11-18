import { formatAdminNumber } from "./adminHelpers.js";

const series = [
  ["Scam analyses", "scamAnalyses", "#2563EB"],
  ["URL scans", "urlScans", "#06B6D4"],
  ["Resume analyses", "resumeAnalyses", "#8B5CF6"],
  ["Job match analyses", "jobMatchAnalyses", "#F59E0B"],
];

export default function AnalyticsCharts({ metrics }) {
  const maximum = Math.max(1, ...series.map(([, key]) => metrics[key] ?? 0));
  return (
    <section className="admin-panel admin-analytics">
      <div className="admin-panel-heading"><div><span className="admin-eyebrow">PLATFORM USAGE</span><h2>Recorded activity totals</h2></div></div>
      {series.map(([label, key, color]) => {
        const value = metrics[key];
        const width = value == null ? 0 : (value / maximum) * 100;
        return (
          <div className="admin-chart-row" key={key}>
            <div><span>{label}</span><strong>{formatAdminNumber(value)}</strong></div>
            <div className="admin-chart-track" role="img" aria-label={`${label}: ${formatAdminNumber(value)}`}>
              <span style={{ width: `${width}%`, backgroundColor: color }} />
            </div>
          </div>
        );
      })}
      <p className="admin-disclaimer">Bars compare currently stored totals; the application does not retain historical time-series snapshots.</p>
    </section>
  );
}
