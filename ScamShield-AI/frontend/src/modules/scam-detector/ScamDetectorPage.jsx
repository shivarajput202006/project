import { useCallback, useEffect, useState } from "react";
import { ArrowLeft, ShieldCheck } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import AnalysisLoader from "./AnalysisLoader.jsx";
import AnalysisResult from "./AnalysisResult.jsx";
import EmailInputPanel from "./EmailInputPanel.jsx";
import FileUploadPanel from "./FileUploadPanel.jsx";
import InputTypeSelector from "./InputTypeSelector.jsx";
import TextInputPanel from "./TextInputPanel.jsx";
import UrlInputPanel from "./UrlInputPanel.jsx";
import { ScamDetectorApiError, scamDetectorService } from "../../services/scamDetectorService.js";
import { validateSelectedFile } from "../../utils/fileValidation.js";
import "../dashboard/dashboard.css";
import "./scam-detector.css";

const EMPTY_EMAIL = { subject: "", senderEmail: "", body: "" };

function formatTime(value) {
  if (!value) return "Time unavailable";
  return new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" })
    .format(new Date(value));
}

function riskLabel(status) {
  return ({
    LOW_RISK: "Fewer warning signs",
    NEEDS_REVIEW: "Review carefully",
    HIGH_RISK: "Significant warning signs",
    UNVERIFIED: "Unverified",
  })[status] ?? "Unverified";
}

export default function ScamDetectorPage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [inputType, setInputType] = useState("TEXT");
  const [text, setText] = useState("");
  const [email, setEmail] = useState(EMPTY_EMAIL);
  const [url, setUrl] = useState("");
  const [file, setFile] = useState(null);
  const [fileError, setFileError] = useState("");
  const [inputError, setInputError] = useState("");
  const [report, setReport] = useState(null);
  const [analysisError, setAnalysisError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(null);
  const [lastSubmission, setLastSubmission] = useState(null);
  const [lastHistoryReportId, setLastHistoryReportId] = useState("");
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState("");
  const [historyRetry, setHistoryRetry] = useState(0);
  const [selectedHistoryId, setSelectedHistoryId] = useState("");
  const [mobileNavigationOpen, setMobileNavigationOpen] = useState(false);
  const [logoutPending, setLogoutPending] = useState(false);
  const [logoutError, setLogoutError] = useState("");

  const loadHistory = useCallback(async () => {
    setHistoryLoading(true);
    setHistoryError("");
    try {
      setHistory(await scamDetectorService.getHistory());
    } catch (error) {
      if (error instanceof ScamDetectorApiError && error.status === 401) {
        try {
          await refreshUser();
        } catch (refreshError) {
          setHistoryError(refreshError.message);
          return;
        }
        navigate("/login", { replace: true, state: { from: { pathname: "/scam-detector" } } });
        return;
      }
      setHistoryError(error.message);
    } finally {
      setHistoryLoading(false);
    }
  }, [navigate, refreshUser]);

  useEffect(() => {
    loadHistory();
  }, [loadHistory, historyRetry]);

  function chooseInputType(type) {
    setInputType(type);
    setInputError("");
    setAnalysisError("");
  }

  async function handleFileSelect(selectedFile) {
    setFileError("");
    setFile(null);
    if (!selectedFile) return;
    try {
      await validateSelectedFile(selectedFile);
      setFile(selectedFile);
    } catch (error) {
      setFileError(error.message);
    }
  }

  function validateAndCreateSubmission() {
    setInputError("");
    if (inputType === "TEXT") {
      if (!text.trim()) {
        setInputError("Paste the message or job offer you want to review.");
        return null;
      }
      return { type: "TEXT", text: text.trim() };
    }
    if (inputType === "EMAIL") {
      if (!email.subject.trim() || !email.senderEmail.trim() || !email.body.trim()) {
        setInputError("Enter the email subject, sender address, and body.");
        return null;
      }
      if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.senderEmail.trim())) {
        setInputError("Enter a valid sender email address.");
        return null;
      }
      return {
        type: "EMAIL",
        payload: {
          subject: email.subject.trim(),
          senderEmail: email.senderEmail.trim(),
          body: email.body.trim(),
        },
      };
    }
    if (inputType === "URL") {
      try {
        const parsed = new URL(url.trim());
        if (!["http:", "https:"].includes(parsed.protocol)) throw new Error();
      } catch {
        setInputError("Enter a complete HTTP or HTTPS URL.");
        return null;
      }
      return { type: "URL", url: url.trim() };
    }
    if (!file) {
      setInputError(fileError || "Choose a supported file to continue.");
      return null;
    }
    return { type: "FILE", file };
  }

  async function runSubmission(submission) {
    if (!submission) return;
    setLastSubmission(submission);
    setAnalysisError("");
    setInputError("");
    setReport(null);
    setSelectedHistoryId("");
    setLastHistoryReportId("");
    setSubmitting(true);
    setUploadProgress(submission.type === "FILE" ? 0 : null);
    try {
      let result;
      if (submission.type === "TEXT") result = await scamDetectorService.analyzeText(submission.text);
      if (submission.type === "EMAIL") result = await scamDetectorService.analyzeEmail(submission.payload);
      if (submission.type === "URL") result = await scamDetectorService.analyzeUrl(submission.url);
      if (submission.type === "FILE") {
        result = await scamDetectorService.analyzeFile(submission.file, setUploadProgress);
      }
      if (!result?.id || !result?.riskStatus || !Number.isInteger(result?.riskScore)) {
        throw new Error("The analysis response was incomplete. No report was displayed.");
      }
      if (
        !Array.isArray(result.indicators)
        || !Array.isArray(result.evidence)
        || !Array.isArray(result.recommendations)
        || !Array.isArray(result.limitations)
      ) {
        throw new Error("The analysis response was incomplete. No report was displayed.");
      }
      setReport(result);
      loadHistory();
    } catch (error) {
      if (error instanceof ScamDetectorApiError && error.status === 401) {
        try {
          await refreshUser();
        } catch (refreshError) {
          setAnalysisError(refreshError.message);
          return;
        }
        navigate("/login", { replace: true, state: { from: { pathname: "/scam-detector" } } });
        return;
      }
      setAnalysisError(error.message);
    } finally {
      setSubmitting(false);
      setUploadProgress(null);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    await runSubmission(validateAndCreateSubmission());
  }

  async function openHistoryReport(id) {
    setSelectedHistoryId(id);
    setAnalysisError("");
    setReport(null);
    setLastSubmission(null);
    setLastHistoryReportId(id);
    try {
      const result = await scamDetectorService.getReport(id);
      if (!result?.id || !result?.riskStatus || !Number.isInteger(result?.riskScore)) {
        throw new Error("The report response was incomplete.");
      }
      setReport(result);
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (error) {
      if (error instanceof ScamDetectorApiError && error.status === 401) {
        try {
          await refreshUser();
        } catch (refreshError) {
          setAnalysisError(refreshError.message);
          return;
        }
        navigate("/login", { replace: true, state: { from: { pathname: "/scam-detector" } } });
        return;
      }
      setAnalysisError(error.message);
    } finally {
      setSelectedHistoryId("");
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
      {mobileNavigationOpen && (
        <button
          className="dashboard-backdrop"
          type="button"
          aria-label="Close navigation"
          onClick={() => setMobileNavigationOpen(false)}
        />
      )}
      <DashboardSidebar
        isOpen={mobileNavigationOpen}
        onNavigate={() => setMobileNavigationOpen(false)}
        onLogout={handleLogout}
        logoutPending={logoutPending}
      />
      <div className="dashboard-workspace">
        <DashboardHeader
          title="AI Scam Detector"
          user={user}
          notificationCount={0}
          onMenuClick={() => setMobileNavigationOpen(true)}
          onLogout={handleLogout}
          logoutPending={logoutPending}
        />
        <main className="dashboard-main scam-detector-main">
          <div className="scam-detector-titlebar">
            <div>
              <Link className="scam-back-link" to="/dashboard"><ArrowLeft size={14} /> Dashboard</Link>
              <span className="dashboard-eyebrow">CAREER SAFETY TOOL</span>
              <h1>AI Scam Detector</h1>
              <p>Review suspicious job messages and opportunities for warning signs.</p>
            </div>
            <span className="scam-title-shield"><ShieldCheck size={23} /></span>
          </div>

          {(logoutError || inputError) && (
            <p className="scam-error-banner" role="alert">{logoutError || inputError}</p>
          )}

          <div className="scam-detector-grid">
            <section className="scam-input-card">
              <header className="scam-card-heading">
                <div><span className="dashboard-eyebrow">START A REVIEW</span><h2>What would you like to check?</h2></div>
              </header>
              <InputTypeSelector value={inputType} onChange={chooseInputType} />
              <form onSubmit={handleSubmit} noValidate>
                {inputType === "TEXT" && <TextInputPanel value={text} onChange={setText} />}
                {inputType === "EMAIL" && <EmailInputPanel value={email} onChange={setEmail} />}
                {inputType === "URL" && <UrlInputPanel value={url} onChange={setUrl} />}
                {inputType === "FILE" && (
                  <FileUploadPanel
                    file={file}
                    fileError={fileError}
                    uploadProgress={uploadProgress}
                    onSelect={handleFileSelect}
                    onRemove={() => { setFile(null); setFileError(""); }}
                  />
                )}
                {analysisError && (
                  <div className="scam-error-banner" role="alert">
                    <span>{analysisError}</span>
                    {(lastSubmission || lastHistoryReportId) && !submitting && (
                      <button
                        type="button"
                        onClick={() => lastSubmission
                          ? runSubmission(lastSubmission)
                          : openHistoryReport(lastHistoryReportId)}
                      >
                        Retry
                      </button>
                    )}
                  </div>
                )}
                <button className="scam-submit-button" type="submit" disabled={submitting}>
                  {submitting ? "Analyzing…" : "Analyze for warning signs"}
                </button>
                <p className="scam-privacy-note">
                  Submitted content is sent to the configured Gemini AI service for analysis. Raw content and uploaded files are not saved to analysis history.
                </p>
              </form>
            </section>

            <section className="scam-results-column" aria-live="polite">
              {submitting ? (
                <AnalysisLoader />
              ) : report ? (
                <AnalysisResult report={report} onRetry={lastSubmission ? () => runSubmission(lastSubmission) : null} />
              ) : (
                <div className="scam-results-placeholder">
                  <span><ShieldCheck size={24} /></span>
                  <strong>Your analysis report will appear here</strong>
                  <p>Choose an input type and submit content to see a real analysis from the configured backend AI service.</p>
                  <ul>
                    <li>AI-generated indicators are labeled as inferences.</li>
                    <li>Company details remain unverified without a trusted source.</li>
                    <li>Submitted links are never opened by this service.</li>
                  </ul>
                </div>
              )}
            </section>
          </div>

          <section className="scam-history-card">
            <header className="scam-card-heading">
              <div><span className="dashboard-eyebrow">PRIVATE TO YOUR ACCOUNT</span><h2>Recent analyses</h2></div>
              {history.length > 0 && <span className="scam-history-count">Latest {history.length}</span>}
            </header>
            {historyLoading ? (
              <div className="scam-history-loading" role="status">Loading your analysis history…</div>
            ) : historyError ? (
              <div className="scam-history-empty" role="alert">
                <p>{historyError}</p>
                <button className="scam-text-button" type="button" onClick={() => setHistoryRetry((value) => value + 1)}>Retry history</button>
              </div>
            ) : history.length ? (
              <div className="scam-history-list">
                {history.map((item) => (
                  <button
                    type="button"
                    className={`scam-history-item${selectedHistoryId === item.id ? " is-loading" : ""}`}
                    key={item.id}
                    onClick={() => openHistoryReport(item.id)}
                    disabled={Boolean(selectedHistoryId)}
                  >
                    <span className={`scam-history-risk risk-${String(item.riskStatus).toLowerCase()}`} />
                    <span className="scam-history-copy">
                      <strong>{item.inputSummary}</strong>
                      <small>{riskLabel(item.riskStatus)} · {formatTime(item.analysisTimestamp)}</small>
                    </span>
                    <span className="scam-history-score">{item.riskScore}/100</span>
                  </button>
                ))}
              </div>
            ) : (
              <div className="scam-history-empty">
                <p>No analyses yet. Once you analyze content, your private reports will appear here.</p>
              </div>
            )}
          </section>

          <p className="scam-global-disclaimer">
            AI analysis is advisory and does not guarantee that an opportunity is genuine or fraudulent. Verify important details independently before sharing personal information or making payments.
          </p>
        </main>
      </div>
    </div>
  );
}
