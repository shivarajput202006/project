import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import ReportCategoryTabs from "./components/ReportCategoryTabs.jsx";
import ReportDeleteDialog from "./components/ReportDeleteDialog.jsx";
import ReportEmptyState from "./components/ReportEmptyState.jsx";
import ReportFilters from "./components/ReportFilters.jsx";
import ReportList from "./components/ReportList.jsx";
import ReportLoading from "./components/ReportLoading.jsx";
import ReportOverview from "./components/ReportOverview.jsx";
import ReportPagination from "./components/ReportPagination.jsx";
import ReportSearch from "./components/ReportSearch.jsx";
import { reportService } from "./services/reportService.js";
import "./reports.css";
import "../dashboard/dashboard.css";

const INITIAL_FILTERS = { type: "", status: "", fromDate: "", toDate: "", sort: "NEWEST" };
const PAGE_SIZE = 12;

export default function ReportsPage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [summary, setSummary] = useState(null);
  const [categories, setCategories] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [draftFilters, setDraftFilters] = useState(INITIAL_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState(INITIAL_FILTERS);
  const [keyword, setKeyword] = useState("");
  const [appliedKeyword, setAppliedKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [reportToDelete, setReportToDelete] = useState(null);
  const [deletePending, setDeletePending] = useState(false);
  const [downloadPending, setDownloadPending] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [retry, setRetry] = useState(0);

  const loadReports = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [list, overview, categoryData] = await Promise.all([
        reportService.list({ ...appliedFilters, keyword: appliedKeyword, page, size: PAGE_SIZE }),
        reportService.summary(),
        reportService.categories(),
      ]);
      setPageData(list);
      setSummary(overview);
      setCategories(categoryData);
    } catch (loadError) {
      if (loadError.status === 401) {
        try {
          await refreshUser();
        } catch (refreshError) {
          setError(refreshError.message);
          setLoading(false);
          return;
        }
        navigate("/login", { replace: true, state: { from: { pathname: "/reports" } } });
        return;
      }
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, [appliedFilters, appliedKeyword, navigate, page, refreshUser, retry]);

  useEffect(() => { loadReports(); }, [loadReports]);

  function updateFilter(key, value) {
    setDraftFilters((current) => ({ ...current, [key]: value }));
  }

  function applyFilters() {
    setAppliedFilters({ ...draftFilters });
    setPage(1);
  }

  function selectCategory(type) {
    setDraftFilters((current) => ({ ...current, type }));
    setAppliedFilters((current) => ({ ...current, type }));
    setPage(1);
  }

  function clearFilters() {
    setDraftFilters(INITIAL_FILTERS);
    setAppliedFilters(INITIAL_FILTERS);
    setKeyword("");
    setAppliedKeyword("");
    setPage(1);
  }

  async function handleDownload(report) {
    setDownloadPending(report.id);
    setNotice("");
    try {
      const { blob, filename } = await reportService.download(report.id);
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
      setDownloadPending("");
    }
  }

  async function confirmDelete() {
    if (!reportToDelete) return;
    setDeletePending(true);
    setError("");
    try {
      await reportService.deleteHistory(reportToDelete.id);
      setReportToDelete(null);
      setNotice("History entry removed. The original analysis remains in its module.");
      setRetry((value) => value + 1);
    } catch (deleteError) {
      setError(deleteError.message);
    } finally {
      setDeletePending(false);
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
        <main className="dashboard-main reports-page">
          <ReportOverview summary={summary} />
          {error && <div className="reports-alert" role="alert">{error}</div>}
          {notice && <div className="reports-notice" role="status">{notice}</div>}
          {loading && !pageData ? <ReportLoading /> : error && !pageData ? (
            <section className="reports-error-state" role="alert">
              <h1>We couldn't load your report history.</h1>
              <p>{error}</p>
              <button className="reports-primary-button" type="button" onClick={() => setRetry((value) => value + 1)}>
                Try again
              </button>
            </section>
          ) : (
            <>
              <section className="reports-controls" aria-label="Search and filter reports">
                <ReportSearch
                  value={keyword}
                  onChange={setKeyword}
                  onSubmit={() => { setAppliedKeyword(keyword.trim()); setPage(1); }}
                />
                <ReportFilters filters={draftFilters} onChange={updateFilter} onApply={applyFilters} onClear={clearFilters} />
              </section>
              <ReportCategoryTabs selected={appliedFilters.type} categories={categories} onSelect={selectCategory} />
              {loading ? <ReportLoading /> : pageData?.reports?.length ? (
                <>
                  <ReportList reports={pageData.reports} onDownload={handleDownload} onDelete={setReportToDelete}
                    downloadPending={downloadPending} />
                  <ReportPagination
                    page={page}
                    size={PAGE_SIZE}
                    totalPages={pageData.totalPages}
                    totalElements={pageData.totalElements}
                    onChange={setPage}
                  />
                </>
              ) : <ReportEmptyState filtered={Boolean(
                appliedKeyword || appliedFilters.type || appliedFilters.status
                || appliedFilters.fromDate || appliedFilters.toDate,
              )} />}
            </>
          )}
          {downloadPending && <span className="reports-download-status" role="status">Preparing report PDF…</span>}
          <footer className="dashboard-footer">ScamShield AI · Your career safety workspace</footer>
        </main>
      </div>
      <ReportDeleteDialog
        report={reportToDelete}
        busy={deletePending}
        onCancel={() => setReportToDelete(null)}
        onConfirm={confirmDelete}
      />
    </div>
  );
}
