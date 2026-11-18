import { useCallback, useEffect, useState } from "react";
import { ArrowRight, FileSearch, ShieldCheck, Sparkles } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import AnalysisProgress from "./components/AnalysisProgress.jsx";
import ATSFeedback from "./components/ATSFeedback.jsx";
import EducationSection from "./components/EducationSection.jsx";
import ExperienceSection from "./components/ExperienceSection.jsx";
import FilePreview from "./components/FilePreview.jsx";
import ImprovementSuggestions from "./components/ImprovementSuggestions.jsx";
import MissingSkills from "./components/MissingSkills.jsx";
import PersonalInfoCard from "./components/PersonalInfoCard.jsx";
import ProjectsAnalysis from "./components/ProjectsAnalysis.jsx";
import RecommendedRoles from "./components/RecommendedRoles.jsx";
import ResumeHistory from "./components/ResumeHistory.jsx";
import ResumeOverview from "./components/ResumeOverview.jsx";
import ResumeUpload from "./components/ResumeUpload.jsx";
import SkillsAnalysis from "./components/SkillsAnalysis.jsx";
import StrengthsWeaknesses from "./components/StrengthsWeaknesses.jsx";
import { ResumeApiError, resumeService } from "./services/resumeService.js";
import "../dashboard/dashboard.css";
import "./resume-analyzer.css";

export default function ResumeAnalyzerPage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [file, setFile] = useState(null);
  const [resume, setResume] = useState(null);
  const [analysis, setAnalysis] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState("");
  const [consent, setConsent] = useState(false);
  const [stage, setStage] = useState("");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [logoutPending, setLogoutPending] = useState(false);
  const [logoutError, setLogoutError] = useState("");

  const handleUnauthorized = useCallback(async (cause) => {
    if (!(cause instanceof ResumeApiError) || cause.status !== 401) return;
    try {
      await refreshUser();
    } catch {
      navigate("/login", { replace: true, state: { from: location } });
      return;
    }
    navigate("/login", { replace: true, state: { from: location } });
  }, [location, navigate, refreshUser]);

  const loadHistory = useCallback(async () => {
    setHistoryLoading(true);
    setHistoryError("");
    try {
      setHistory(await resumeService.history());
    } catch (cause) {
      setHistoryError(cause.message);
      await handleUnauthorized(cause);
    } finally {
      setHistoryLoading(false);
    }
  }, [handleUnauthorized]);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  function chooseFile(nextFile) {
    setFile(nextFile);
    setResume(null);
    setAnalysis(null);
    setRecommendation(null);
    setConsent(false);
    setError("");
    setNotice("");
  }

  async function handleAnalyze(event) {
    event.preventDefault();
    if (!consent || (!file && !resume)) {
      setError("Select a resume and confirm the AI-processing disclosure before continuing.");
      return;
    }
    setStage(file ? "Uploading and extracting your resume…" : "Extracting and analyzing your resume…");
    setError("");
    setNotice("");
    try {
      let activeResume = resume;
      if (file) {
        activeResume = await resumeService.upload(file);
        setResume(activeResume);
        setFile(null);
        await loadHistory();
      }
      setStage("Generating evidence-based feedback with the configured AI service…");
      const result = await resumeService.analyze(activeResume.id, true);
      setAnalysis(result);
      try {
        setRecommendation(await resumeService.recommendations(activeResume.id));
      } catch {
        setRecommendation(null);
      }
      setNotice("Analysis completed. Suggestions are advisory and use only information identified in the resume.");
      setConsent(false);
      await loadHistory();
    } catch (cause) {
      setError(cause.message);
      await handleUnauthorized(cause);
    } finally {
      setStage("");
    }
  }

  async function handleSelectHistory(item) {
    setStage("Loading resume analysis…");
    setError("");
    setNotice("");
    setFile(null);
    setConsent(false);
    try {
      const details = await resumeService.get(item.id);
      setResume(details);
      setAnalysis(null);
      setRecommendation(null);
      if (item.latestAnalysisAt) {
        const [result, roles] = await Promise.all([
          resumeService.analysis(item.id),
          resumeService.recommendations(item.id),
        ]);
        setAnalysis(result);
        setRecommendation(roles);
      }
    } catch (cause) {
      setError(cause.message);
      await handleUnauthorized(cause);
    } finally {
      setStage("");
    }
  }

  async function handleDelete(item) {
    if (!window.confirm(`Delete ${item.originalFilename} and its analysis? This cannot be undone.`)) return;
    setError("");
    setNotice("");
    try {
      await resumeService.delete(item.id);
      setHistory((items) => items.filter((entry) => entry.id !== item.id));
      if (resume?.id === item.id) {
        setResume(null);
        setFile(null);
        setAnalysis(null);
        setRecommendation(null);
      }
      setNotice("Resume and stored analysis deleted.");
    } catch (cause) {
      setError(cause.message);
      await handleUnauthorized(cause);
    }
  }

  async function handleLogout() {
    setLogoutPending(true);
    setLogoutError("");
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (cause) {
      setLogoutError(cause.message);
    } finally {
      setLogoutPending(false);
    }
  }

  const displayedResume = resume ?? null;

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
          title="AI Resume Analyzer"
          onMenuClick={() => setMenuOpen(true)}
          onLogout={handleLogout}
          logoutPending={logoutPending}
        />
        <main className="dashboard-main resume-analyzer-main">
          {logoutError && <p className="dashboard-alert" role="alert">{logoutError}</p>}
          <section className="resume-hero">
            <div>
              <span className="dashboard-eyebrow">CAREER INSIGHTS</span>
              <h1>Understand your resume. Plan your next move.</h1>
              <p>Extract resume details, review transparent feedback, and explore roles based on information found in your document.</p>
            </div>
            <span className="resume-hero-icon"><FileSearch size={25} aria-hidden="true" /></span>
          </section>

          <div className="resume-layout">
            <div className="resume-primary-column">
              <form className="resume-card resume-upload-flow" onSubmit={handleAnalyze}>
                <ResumeUpload file={file} disabled={Boolean(stage)} onSelect={chooseFile} />
                <FilePreview file={file} resume={displayedResume} />
                <label className="resume-consent">
                  <input
                    type="checkbox"
                    checked={consent}
                    onChange={(event) => setConsent(event.target.checked)}
                    disabled={Boolean(stage)}
                  />
                  <span>
                    <strong>Consent to AI processing</strong>
                    <small>
                      If I select Analyze, extracted resume text (which may include personal information) will be sent
                      to Google Gemini using the backend’s configured account. I understand the file remains stored
                      privately until I delete it.
                    </small>
                  </span>
                </label>
                <button className="resume-primary-button" type="submit" disabled={Boolean(stage) || !consent || (!file && !resume)}>
                  <Sparkles size={16} aria-hidden="true" /> Analyze Resume <ArrowRight size={15} aria-hidden="true" />
                </button>
                <p className="resume-privacy-note"><ShieldCheck size={14} /> Files are stored in private MongoDB GridFS, never in the public frontend directory.</p>
              </form>

              <AnalysisProgress stage={stage} />
              {error && <div className="resume-message is-error" role="alert">
                <strong>We couldn’t complete that request.</strong><span>{error}</span>
                {resume && <button type="button" onClick={(event) => handleAnalyze(event)}>Retry analysis</button>}
                {error.includes("Enable AI resume processing") && <Link to="/settings">Review your AI privacy setting</Link>}
              </div>}
              {notice && <p className="resume-message is-success" role="status">{notice}</p>}

              {analysis ? (
                <div className="resume-results">
                  <ResumeOverview analysis={analysis} />
                  <PersonalInfoCard personalInfo={analysis.extractedData.personalInfo} />
                  <EducationSection education={analysis.extractedData.education} />
                  <SkillsAnalysis skillAnalysis={analysis.skillAnalysis} />
                  <MissingSkills roleSkillGaps={analysis.skillAnalysis.roleSkillGaps} />
                  <ExperienceSection experience={analysis.extractedData.experience} />
                  <ProjectsAnalysis
                    projects={analysis.extractedData.projects}
                    certifications={analysis.extractedData.certifications}
                  />
                  <StrengthsWeaknesses
                    strengths={analysis.extractedData.strengths}
                    weaknesses={analysis.extractedData.weaknesses}
                  />
                  <ATSFeedback feedback={analysis.atsFeedback} />
                  <ImprovementSuggestions suggestions={analysis.improvementSuggestions} />
                  <RecommendedRoles
                    recommendation={recommendation}
                    roles={analysis.recommendedRoles}
                  />
                </div>
              ) : (
                <section className="resume-empty-state">
                  <span><Sparkles size={21} /></span>
                  <h2>Your analysis will appear here</h2>
                  <p>Upload a resume, review the privacy disclosure, and choose Analyze Resume. No sample analysis or preset score is shown.</p>
                </section>
              )}
            </div>

            <aside className="resume-side-column">
              <ResumeHistory
                items={history}
                selectedId={resume?.id}
                loading={historyLoading}
                error={historyError}
                onSelect={handleSelectHistory}
                onDelete={handleDelete}
              />
              <section className="resume-card resume-trust-card">
                <ShieldCheck size={19} aria-hidden="true" />
                <h2>Your resume stays yours</h2>
                <p>Only your account can access its files and results. Delete an entry to remove its stored file and analysis.</p>
              </section>
            </aside>
          </div>
        </main>
      </div>
    </div>
  );
}
