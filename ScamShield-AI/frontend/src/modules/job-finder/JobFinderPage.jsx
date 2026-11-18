import { useCallback, useEffect, useMemo, useState } from "react";
import { ArrowLeft, BriefcaseBusiness, ChevronLeft, ChevronRight } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import JobDetails from "./components/JobDetails.jsx";
import JobEmptyState from "./components/JobEmptyState.jsx";
import JobFilters from "./components/JobFilters.jsx";
import JobList from "./components/JobList.jsx";
import JobLoading from "./components/JobLoading.jsx";
import JobSearchBar from "./components/JobSearchBar.jsx";
import SavedJobs from "./components/SavedJobs.jsx";
import { jobFinderService, JobFinderApiError } from "./services/jobFinderService.js";
import { hasSalaryData } from "./utils/jobFilters.js";
import "../dashboard/dashboard.css";
import "./job-finder.css";

const EMPTY_FILTERS = {
  keyword: "",
  location: "",
  workMode: "",
  experience: "",
  jobType: "",
  salaryMin: "",
};

export default function JobFinderPage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isSavedRoute = location.pathname === "/saved-jobs";
  const initialKeyword = new URLSearchParams(location.search).get("keyword")?.trim().slice(0, 200) ?? "";
  const [menuOpen, setMenuOpen] = useState(false);
  const [filters, setFilters] = useState(() => ({ ...EMPTY_FILTERS, keyword: initialKeyword }));
  const [activeFilters, setActiveFilters] = useState({});
  const [jobs, setJobs] = useState([]);
  const [savedJobs, setSavedJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(false);
  const [total, setTotal] = useState(0);
  const [providerInfo, setProviderInfo] = useState(null);
  const [loading, setLoading] = useState(!isSavedRoute);
  const [savedLoading, setSavedLoading] = useState(true);
  const [searchError, setSearchError] = useState("");
  const [savedError, setSavedError] = useState("");
  const [notice, setNotice] = useState("");
  const [savingIds, setSavingIds] = useState(() => new Set());
  const [logoutPending, setLogoutPending] = useState(false);
  const [logoutError, setLogoutError] = useState("");

  const savedIds = useMemo(() => new Set(savedJobs.map((job) => job.id)), [savedJobs]);
  const salaryAvailable = hasSalaryData(jobs);

  const loadSavedJobs = useCallback(async () => {
    setSavedLoading(true);
    setSavedError("");
    try {
      setSavedJobs(await jobFinderService.saved());
    } catch (error) {
      setSavedError(error.message);
    } finally {
      setSavedLoading(false);
    }
  }, []);

  const runSearch = useCallback(async (nextFilters = filters, nextPage = 1) => {
    setLoading(true);
    setSearchError("");
    setNotice("");
    const requestFilters = { ...nextFilters };
    if (!hasSalaryData(jobs)) delete requestFilters.salaryMin;
    try {
      const response = await jobFinderService.search(requestFilters, nextPage);
      setJobs(response.jobs);
      setTotal(response.total);
      setHasMore(response.hasMore);
      setPage(response.page);
      setActiveFilters(requestFilters);
      setProviderInfo((current) => ({ ...current, sourceName: response.sourceName, providerAvailable: true }));
      setSelectedJob(null);
    } catch (error) {
      setJobs([]);
      setTotal(0);
      setHasMore(false);
      setSearchError(error.message);
      if (error instanceof JobFinderApiError && error.status === 401) {
        try {
          await refreshUser();
        } catch {
          navigate("/login", { replace: true, state: { from: location } });
          return;
        }
        navigate("/login", { replace: true, state: { from: location } });
      }
    } finally {
      setLoading(false);
    }
  }, [filters, jobs, location, navigate, refreshUser]);

  useEffect(() => {
    let active = true;
    async function initialize() {
      const initialFilters = { ...EMPTY_FILTERS, keyword: isSavedRoute ? "" : initialKeyword };
      setFilters(initialFilters);
      const [filtersResult, savedResult] = await Promise.allSettled([
        jobFinderService.filters(),
        jobFinderService.saved(),
      ]);
      if (!active) return;
      if (filtersResult.status === "fulfilled") setProviderInfo(filtersResult.value);
      else setProviderInfo({ providerAvailable: false, message: filtersResult.reason.message });
      if (savedResult.status === "fulfilled") {
        setSavedJobs(savedResult.value);
        setSavedError("");
      } else {
        setSavedError(savedResult.reason.message);
      }
      setSavedLoading(false);
      if (!isSavedRoute) {
        setLoading(true);
        try {
          const response = await jobFinderService.search(initialFilters, 1);
          if (!active) return;
          setJobs(response.jobs);
          setTotal(response.total);
          setHasMore(response.hasMore);
          setPage(response.page);
          setActiveFilters(initialFilters);
          setProviderInfo((current) => ({ ...current, sourceName: response.sourceName, providerAvailable: true }));
          setSearchError("");
        } catch (error) {
          if (!active) return;
          setSearchError(error.message);
          setJobs([]);
        } finally {
          if (active) setLoading(false);
        }
      } else {
        setLoading(false);
      }
    }
    initialize();
    return () => { active = false; };
  }, [initialKeyword, isSavedRoute, retryCount]);

  async function handleSave(job) {
    setSavingIds((current) => new Set(current).add(job.id));
    setNotice("");
    try {
      if (savedIds.has(job.id)) {
        await jobFinderService.unsave(job.id);
        setSavedJobs((current) => current.filter((saved) => saved.id !== job.id));
        if (isSavedRoute) setSelectedJob(null);
        setNotice("Job removed from your saved list.");
      } else {
        const saved = await jobFinderService.save(job);
        setSavedJobs((current) => [saved, ...current.filter((item) => item.id !== saved.id)]);
        setNotice("Job saved. The listing was refreshed with the provider before saving.");
      }
    } catch (error) {
      setNotice(error.message);
    } finally {
      setSavingIds((current) => {
        const next = new Set(current);
        next.delete(job.id);
        return next;
      });
    }
  }

  async function handleLogout() {
    setLogoutPending(true);
    setLogoutError("");
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (error) {
      setLogoutError(error.message);
    } finally {
      setLogoutPending(false);
    }
  }

  return (
    <div className="dashboard-shell">
      {menuOpen && <button className="dashboard-backdrop" type="button" aria-label="Close navigation" onClick={() => setMenuOpen(false)} />}
      <DashboardSidebar
        isOpen={menuOpen}
        onNavigate={() => setMenuOpen(false)}
        onLogout={handleLogout}
        logoutPending={logoutPending}
      />
      <div className="dashboard-workspace">
        <DashboardHeader
          user={user}
          title={isSavedRoute ? "Saved Jobs" : "Live Job Finder"}
          onMenuClick={() => setMenuOpen(true)}
          onLogout={handleLogout}
          logoutPending={logoutPending}
        />
        <main className="dashboard-main job-finder-main">
          {logoutError && <p className="dashboard-alert" role="alert">{logoutError}</p>}
          <section className="job-finder-intro">
            <div>
              <span className="dashboard-eyebrow">{isSavedRoute ? "YOUR COLLECTION" : "LIVE LISTINGS"}</span>
              <h1>{isSavedRoute ? "Saved jobs" : "Find your next opportunity"}</h1>
              <p>{isSavedRoute
                ? "Your saved listings are tied to your account."
                : "Search current listings from the configured provider. Listings are source-reported, not independently verified."}</p>
            </div>
            <span className="job-finder-intro-icon"><BriefcaseBusiness size={23} /></span>
          </section>

          <nav className="job-finder-tabs" aria-label="Job finder pages">
            <Link className={!isSavedRoute ? "is-active" : ""} to="/live-jobs">Search jobs</Link>
            <Link className={isSavedRoute ? "is-active" : ""} to="/saved-jobs">Saved jobs <span>{savedJobs.length}</span></Link>
          </nav>

          {isSavedRoute ? (
            <div className="saved-jobs-layout">
              <section className="job-results-column" aria-label="Saved jobs">
                {notice && <p className="job-notice" role="status">{notice}</p>}
                {savedLoading ? <JobLoading /> : (
                  <SavedJobs
                    jobs={savedJobs}
                    loading={savedLoading}
                    error={savedError}
                    savedIds={savedIds}
                    onSave={handleSave}
                    onSelect={setSelectedJob}
                    savingIds={savingIds}
                    onRetry={() => { loadSavedJobs(); }}
                  />
                )}
              </section>
              {selectedJob && (
                <JobDetails
                  job={selectedJob}
                  saved={savedIds.has(selectedJob.id)}
                  saving={savingIds.has(selectedJob.id)}
                  onSave={handleSave}
                  onClose={() => setSelectedJob(null)}
                />
              )}
            </div>
          ) : (
            <>
              {providerInfo?.message && !providerInfo.providerAvailable && (
                <p className="job-provider-note" role="status">{providerInfo.message}</p>
              )}
              <JobSearchBar
                values={filters}
                onChange={setFilters}
                onSearch={() => runSearch(filters, 1)}
                onClear={() => {
                  setFilters(EMPTY_FILTERS);
                  runSearch({}, 1);
                }}
                loading={loading}
              />
              <JobFilters values={filters} onChange={(key, value) => setFilters((current) => ({ ...current, [key]: value }))}
                salaryAvailable={salaryAvailable} />
              {notice && <p className="job-notice" role="status">{notice}</p>}
              <div className="job-results-heading">
                <div>
                  <span className="dashboard-eyebrow">OPPORTUNITIES</span>
                  <h2>{loading ? "Searching listings…" : searchError ? "Search unavailable" : `${total.toLocaleString()} listings`}</h2>
                </div>
                {hasMore && <span className="job-page-label">Page {page}</span>}
              </div>
              <div className={`job-results-layout${selectedJob ? " has-details" : ""}`}>
                <section className="job-results-column" aria-label="Job search results">
                  {loading ? <JobLoading /> : searchError ? (
                    <JobEmptyState
                      title="Live listings are unavailable"
                      description={searchError}
                      actionLabel="Try again"
                      onAction={() => runSearch(activeFilters, page)}
                    />
                  ) : jobs.length === 0 ? (
                    <JobEmptyState
                      title="No matching listings"
                      description="Try a different title, skill, location or filter. No listings are invented when the provider has no results."
                    />
                  ) : (
                    <>
                      <JobList
                        jobs={jobs}
                        savedIds={savedIds}
                        onSave={handleSave}
                        onSelect={setSelectedJob}
                        savingIds={savingIds}
                      />
                      {(page > 1 || hasMore) && (
                        <div className="job-pagination">
                          <button type="button" className="job-secondary-button" disabled={page <= 1 || loading}
                            onClick={() => runSearch(activeFilters, page - 1)}>
                            <ChevronLeft size={16} /> Previous
                          </button>
                          <span>Page {page}</span>
                          <button type="button" className="job-secondary-button" disabled={!hasMore || loading}
                            onClick={() => runSearch(activeFilters, page + 1)}>
                            Next <ChevronRight size={16} />
                          </button>
                        </div>
                      )}
                    </>
                  )}
                  {savedError && <p className="job-muted-label" role="status">Saved job status could not be refreshed: {savedError}</p>}
                </section>
                {selectedJob && (
                  <JobDetails
                    job={selectedJob}
                    saved={savedIds.has(selectedJob.id)}
                    saving={savingIds.has(selectedJob.id)}
                    onSave={handleSave}
                    onClose={() => setSelectedJob(null)}
                  />
                )}
              </div>
              <p className="job-trust-disclaimer">
                Search results come directly from {providerInfo?.sourceName ?? "the configured provider"}. “Listed by source” is not a safety endorsement. Verify employers and apply links independently.
              </p>
            </>
          )}
          <footer className="dashboard-footer"><Link to="/dashboard"><ArrowLeft size={13} /> Back to dashboard</Link> · ScamShield AI</footer>
        </main>
      </div>
    </div>
  );
}
