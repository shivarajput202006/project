import { useCallback, useEffect, useState } from "react";
import { ArrowLeft } from "lucide-react";
import { Link, useNavigate, useParams } from "react-router-dom";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import ReportDetails from "./components/ReportDetails.jsx";
import ReportDownloadButton from "./components/ReportDownloadButton.jsx";
import { reportService } from "./services/reportService.js";
import "./reports.css";
import "../dashboard/dashboard.css";

export default function ReportDetailsPage() {
  const { id = "" } = useParams();
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [report, setReport] = useState(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [downloadPending, setDownloadPending] = useState(false);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setReport(await reportService.details(id));
    } catch (loadError) {
      if (loadError.status === 401) {
        try {
          await refreshUser();
        } catch (refreshError) {
          setError(refreshError.message);
          setLoading(false);
          return;
        }
        navigate("/login", { replace: true, state: { from: { pathname: `/reports/${id}` } } });
      } else {
        setError(loadError.message);
      }
    } finally {
      setLoading(false);
    }
  }, [id, navigate, refreshUser]);

  useEffect(() => { load(); }, [load]);

  async function download() {
    setDownloadPending(true);
    setError("");
    try {
      const { blob, filename } = await reportService.download(id);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (downloadError) {
      setError(downloadError.message);
    } finally {
      setDownloadPending(false);
    }
  }

  async function handleLogout() {
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (logoutError) {
      setError(logoutError.message);
    }
  }

  return (
    <div className="dashboard-shell">
      {sidebarOpen && <button className="dashboard-backdrop" type="button" aria-label="Close navigation" onClick={() => setSidebarOpen(false)} />}
      <DashboardSidebar isOpen={sidebarOpen} onNavigate={() => setSidebarOpen(false)} onLogout={handleLogout} logoutPending={false} />
      <div className="dashboard-workspace">
        <DashboardHeader user={user} notificationCount={0} onMenuClick={() => setSidebarOpen(true)} onLogout={handleLogout} logoutPending={false} />
        <main className="dashboard-main reports-page reports-detail-page">
          <div className="reports-detail-toolbar">
            <Link to="/reports" className="reports-back-link"><ArrowLeft size={16} /> Back to reports</Link>
            {report && <ReportDownloadButton onClick={download} busy={downloadPending} />}
          </div>
          {loading ? <div className="reports-detail-loading" role="status">Loading report details…</div>
            : error ? (
              <section className="reports-error-state" role="alert">
                <h1>We couldn't load this report.</h1><p>{error}</p>
                <button className="reports-primary-button" type="button" onClick={load}>Try again</button>
              </section>
            ) : report ? <ReportDetails report={report} /> : null}
          <footer className="dashboard-footer">ScamShield AI · Your career safety workspace</footer>
        </main>
      </div>
    </div>
  );
}
