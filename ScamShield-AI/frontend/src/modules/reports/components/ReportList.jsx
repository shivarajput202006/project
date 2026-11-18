import ReportCard from "./ReportCard.jsx";

export default function ReportList({ reports, onDownload, onDelete, downloadPending }) {
  return (
    <div className="reports-list">
      {reports.map((report) => <ReportCard key={report.id} report={report} onDownload={onDownload} onDelete={onDelete}
        downloadPending={downloadPending === report.id} />)}
    </div>
  );
}
