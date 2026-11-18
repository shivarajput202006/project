import { useCallback, useEffect, useMemo, useState } from "react";
import { ArrowRight, BriefcaseBusiness, RefreshCw, Sparkles } from "lucide-react";
import { useLocation, useNavigate } from "react-router-dom";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import { resumeService } from "../resume-analyzer/services/resumeService.js";
import { jobFinderService } from "../job-finder/services/jobFinderService.js";
import ResumeSelector from "./components/ResumeSelector.jsx";
import MatchingOverview from "./components/MatchingOverview.jsx";
import MatchFilters from "./components/MatchFilters.jsx";
import RecommendedJobs from "./components/RecommendedJobs.jsx";
import SavedJobs from "./components/SavedJobs.jsx";
import NoMatchState from "./components/NoMatchState.jsx";
import { jobMatchingService } from "./services/jobMatchingService.js";
import { sortMatches } from "./utils/matchScoreUtils.js";
import "../dashboard/dashboard.css";
import "../resume-analyzer/resume-analyzer.css";
import "./job-matching.css";
import "../job-finder/job-finder.css";

const EMPTY_FILTERS = { keyword: "", location: "", workMode: "", jobType: "" };

export default function JobMatchingPage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [resumes, setResumes] = useState([]);
  const [resumeId, setResumeId] = useState("");
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [result, setResult] = useState(null);
  const [savedJobs, setSavedJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [matching, setMatching] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [sortBy, setSortBy] = useState("best");
  const [page, setPage] = useState(1);
  const [busyId, setBusyId] = useState("");
  const [logoutPending, setLogoutPending] = useState(false);

  const handleUnauthorized = useCallback(async (cause) => {
    if (cause?.status !== 401) return;
    try { await refreshUser(); } catch { navigate("/login", { replace: true, state: { from: location } }); return; }
    navigate("/login", { replace: true, state: { from: location } });
  }, [location, navigate, refreshUser]);

  useEffect(() => {
    let active = true;
    (async () => {
      setLoading(true);
      setError("");
      try {
        const [history, saved] = await Promise.all([resumeService.history(), jobFinderService.saved()]);
        if (!active) return;
        setResumes(history);
        setSavedJobs(saved);
        setResumeId((current) => current && history.some((item) => item.id === current && item.latestAnalysisAt)
          ? current : history.find((item) => item.latestAnalysisAt)?.id ?? "");
      } catch (cause) {
        if (!active) return;
        setError(cause.message);
        await handleUnauthorized(cause);
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => { active = false; };
  }, [handleUnauthorized]);

  async function runMatching(nextPage = 1, refreshing = false, filterValues = filters) {
    if (!resumeId) {
      setError("Select an analyzed resume before matching jobs.");
      return;
    }
    setMatching(true);
    setError("");
    setNotice("");
    try {
      const request = {
        resumeId,
        keyword: filterValues.keyword.trim() || null,
        location: filterValues.location.trim() || null,
        workMode: filterValues.workMode || null,
        jobType: filterValues.jobType || null,
        page: nextPage,
        size: 20,
      };
      const data = refreshing
        ? await jobMatchingService.refresh(request)
        : await jobMatchingService.analyze(request);
      setResult(data);
      setPage(nextPage);
      setNotice(data.message);
      setSavedJobs(await jobFinderService.saved());
    } catch (cause) {
      setError(cause.message);
      await handleUnauthorized(cause);
    } finally {
      setMatching(false);
    }
  }

  async function saveJob(job) {
    setBusyId(job.id);
    setError("");
    try {
      const saved = await jobFinderService.save(job);
      setSavedJobs((current) => current.some((item) => item.id === saved.id) ? current : [saved, ...current]);
    } catch (cause) {
      setError(cause.message);
      await handleUnauthorized(cause);
    } finally { setBusyId(""); }
  }

  async function unsaveJob(job) {
    setBusyId(job.id);
    setError("");
    try {
      await jobFinderService.unsave(job.id);
      setSavedJobs((current) => current.filter((item) => item.id !== job.id));
    } catch (cause) {
      setError(cause.message);
      await handleUnauthorized(cause);
    } finally { setBusyId(""); }
  }

  async function handleLogout() {
    setLogoutPending(true);
    try { await logout(); navigate("/", { replace: true }); }
    catch (cause) { setError(cause.message); }
    finally { setLogoutPending(false); }
  }

  const sortedMatches = useMemo(() => sortMatches(result?.matches ?? [], sortBy), [result, sortBy]);
  const savedIds = useMemo(() => new Set(savedJobs.map((job) => job.id)), [savedJobs]);
  const hasAnalyzedResume = resumes.some((resume) => resume.latestAnalysisAt);

  return (
    <div className="dashboard-shell">
      {menuOpen && <button className="dashboard-backdrop" type="button" aria-label="Close navigation" onClick={() => setMenuOpen(false)} />}
      <DashboardSidebar isOpen={menuOpen} onNavigate={() => setMenuOpen(false)} onLogout={handleLogout} logoutPending={logoutPending} />
      <div className="dashboard-workspace">
        <DashboardHeader user={user} title="Resume-Based Job Matching" onMenuClick={() => setMenuOpen(true)} onLogout={handleLogout} logoutPending={logoutPending} />
        <main className="dashboard-main resume-analyzer-main job-matching-main">
          <section className="resume-hero">
            <div><span className="dashboard-eyebrow">CAREER MATCHING</span><h1>Find live opportunities that overlap with your resume.</h1>
              <p>Compare analyzed resume evidence with current provider listings. Scores explain relevance only, not hiring odds or job safety.</p></div>
            <span className="resume-hero-icon"><BriefcaseBusiness size={25} aria-hidden="true" /></span>
          </section>

          <div className="match-control-grid">
            <section className="resume-card match-resume-control">
              <div className="resume-section-heading"><div><span className="dashboard-eyebrow">MODULE 07 → MODULE 06</span><h2>Select your resume analysis</h2></div></div>
              {loading ? <p className="match-note">Loading analyzed resumes…</p> : (
                <ResumeSelector resumes={resumes} value={resumeId} disabled={matching} onChange={(value) => { setResumeId(value); setResult(null); setPage(1); }} />
              )}
              <button className="resume-primary-button" type="button" disabled={matching || loading || !resumeId}
                onClick={() => runMatching(1)}>
                <Sparkles size={15} /> {matching ? "Finding current listings…" : "Start matching"} <ArrowRight size={14} />
              </button>
              <p className="resume-privacy-note">Only your own completed resume analyses are available. Live listings are fetched from the configured provider.</p>
            </section>
            <MatchFilters
              value={filters}
              onChange={setFilters}
              disabled={matching || !resumeId}
              onSubmit={() => runMatching(1)}
              onClear={() => { setFilters(EMPTY_FILTERS); runMatching(1, false, EMPTY_FILTERS); }}
            />
          </div>

          {error && <div className="resume-message is-error" role="alert"><strong>Matching couldn’t be completed.</strong><span>{error}</span></div>}
          {notice && <p className="resume-message is-success" role="status">{notice}</p>}
          {matching && <section className="resume-progress" role="status"><RefreshCw className="resume-spin" size={17} /><div><strong>Comparing resume evidence with current jobs</strong><span>Checking provider listings, score criteria, and source metadata.</span></div></section>}

          {result && (
            <>
              <MatchingOverview result={result} />
              <div className="match-results-toolbar">
                <label className="match-field"><span>Sort results</span><select value={sortBy} onChange={(event) => setSortBy(event.target.value)}><option value="best">Best relevance</option><option value="recent">Newest listing</option><option value="title">Job title</option></select></label>
                <button className="match-refresh-button" type="button" disabled={matching} onClick={() => runMatching(1, true)}><RefreshCw size={14} /> Refresh live listings</button>
              </div>
              <RecommendedJobs matches={sortedMatches} savedIds={savedIds} busyId={busyId} onSave={saveJob} onUnsave={unsaveJob} />
              {result.hasMore && <button className="resume-primary-button match-more-button" type="button" disabled={matching} onClick={() => runMatching(page + 1)}>{matching ? "Loading…" : "Load more live matches"}</button>}
              {!result.matches.length && <NoMatchState title="No current listings matched" description="Try a different keyword, location, or work preference. No sample listings are substituted for provider results." />}
            </>
          )}

          {!result && !loading && !hasAnalyzedResume && (
            <NoMatchState needsResume title="No analyzed resume available" description="Upload and analyze a PDF or DOCX in the Resume Analyzer before requesting resume-based matches." />
          )}
          {!result && !loading && hasAnalyzedResume && !error && (
            <NoMatchState title="Ready to find relevant jobs" description="Choose an analyzed resume and start matching to search the live job provider. Your results appear here only when real listings are returned." />
          )}
          <SavedJobs jobs={savedJobs} onUnsave={unsaveJob} busyId={busyId} />
        </main>
      </div>
    </div>
  );
}
