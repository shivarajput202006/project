import { useCallback, useEffect, useState } from "react";
import { ArrowLeft, Link2, ScanSearch, Trash2 } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext.jsx";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import ScanProgress from "./ScanProgress.jsx";
import URLInputForm from "./URLInputForm.jsx";
import URLResultPage from "./URLResultPage.jsx";
import { URLScannerApiError, urlScannerService } from "../../services/urlScannerService.js";
import { validateURL } from "../../utils/urlValidation.js";
import "../dashboard/dashboard.css";
import "./url-scanner.css";

function formatScanTime(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "Time unavailable"
    : new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}

function statusLabel(status) {
  return ({
    LOW_RISK: "No reputation match",
    NEEDS_REVIEW: "Needs review",
    HIGH_RISK: "Threat match",
    UNVERIFIED: "Unverified",
  })[status] ?? "Unverified";
}

export default function URLScannerPage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [url, setUrl] = useState("");
  const [inputError, setInputError] = useState("");
  const [scanError, setScanError] = useState("");
  const [report, setReport] = useState(null);
  const [scanning, setScanning] = useState(false);
  const [history, setHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState("");
  const [historyRetry, setHistoryRetry] = useState(0);
  const [selectedId, setSelectedId] = useState("");
  const [deletingId, setDeletingId] = useState("");
  const [deleteError, setDeleteError] = useState("");
  const [mobileNavigationOpen, setMobileNavigationOpen] = useState(false);
  const [logoutPending, setLogoutPending] = useState(false);
  const [logoutError, setLogoutError] = useState("");

  const redirectForExpiredSession = useCallback(async () => {
    try {
      await refreshUser();
    } catch (error) {
      return error.message;
    }
    navigate("/login", { replace: true, state: { from: { pathname: "/url-scanner" } } });
    return "";
  }, [navigate, refreshUser]);

  const loadHistory = useCallback(async () => {
    setHistoryLoading(true);
    setHistoryError("");
    try {
      setHistory(await urlScannerService.history());
    } catch (error) {
      if (error instanceof URLScannerApiError && error.status === 401) {
        const refreshError = await redirectForExpiredSession();
        if (refreshError) setHistoryError(refreshError);
        return;
      }
      setHistoryError(error.message);
    } finally {
      setHistoryLoading(false);
    }
  }, [redirectForExpiredSession]);

  useEffect(() => {
    loadHistory();
  }, [loadHistory, historyRetry]);

  async function handleSubmit(event) {
    event.preventDefault();
    setInputError("");
    setScanError("");
    setDeleteError("");
    const validation = validateURL(url);
    if (!validation.valid) {
      setInputError(validation.message);
      return;
    }

    setScanning(true);
    setReport(null);
    setSelectedId("");
    try {
      const result = await urlScannerService.scan(validation.submittedUrl);
      setReport(result);
      await loadHistory();
    } catch (error) {
      if (error instanceof URLScannerApiError && error.status === 401) {
        const refreshError = await redirectForExpiredSession();
        if (refreshError) setScanError(refreshError);
        return;
      }
      setScanError(error.message);
    } finally {
      setScanning(false);
    }
  }

  async function openReport(id) {
    setSelectedId(id);
    setScanError("");
    setDeleteError("");
    try {
      setReport(await urlScannerService.get(id));
    } catch (error) {
      if (error instanceof URLScannerApiError && error.status === 401) {
        const refreshError = await redirectForExpiredSession();
        if (refreshError) setScanError(refreshError);
      } else {
        setScanError(error.message);
      }
    } finally {
      setSelectedId("");
    }
  }

  async function deleteReport(id) {
    if (!window.confirm("Delete this URL scan from your private history?")) return;
    setDeletingId(id);
    setDeleteError("");
    try {
      await urlScannerService.delete(id);
      if (report?.id === id) setReport(null);
      await loadHistory();
    } catch (error) {
      if (error instanceof URLScannerApiError && error.status === 401) {
        const refreshError = await redirectForExpiredSession();
        if (refreshError) setDeleteError(refreshError);
      } else {
        setDeleteError(error.message);
      }
    } finally {
      setDeletingId("");
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
          title="Smart URL Scanner"
          user={user}
          notificationCount={0}
          onMenuClick={() => setMobileNavigationOpen(true)}
          onLogout={handleLogout}
          logoutPending={logoutPending}
        />
        <main className="dashboard-main url-scanner-main">
          <div className="url-scanner-titlebar">
            <div>
              <Link className="url-scanner-back" to="/dashboard"><ArrowLeft size={14} /> Dashboard</Link>
              <span className="url-scanner-eyebrow">CAREER SAFETY TOOL</span>
              <h1>Smart URL Scanner</h1>
              <p>Check suspicious job application links and identify potential phishing and security threats.</p>
            </div>
            <span className="url-scanner-title-icon"><ScanSearch size={23} /></span>
          </div>

          {(logoutError || deleteError) && (
            <p className="url-scanner-alert" role="alert">{logoutError || deleteError}</p>
          )}
          <div className="url-scanner-layout">
            <div className="url-scanner-primary">
              <section className="url-scanner-card">
                <header className="url-scanner-card-heading">
                  <span className="url-scanner-eyebrow">CHECK BEFORE YOU OPEN</span>
                  <h2>Paste a suspicious link</h2>
                  <p>We inspect the address and, when configured, query a trusted reputation provider.</p>
                </header>
                <URLInputForm
                  value={url}
                  onChange={(value) => { setUrl(value); setInputError(""); }}
                  onSubmit={handleSubmit}
                  disabled={scanning}
                  error={inputError}
                />
                <div className="url-scanner-safety-note">
                  <Link2 size={15} />
                  <p>We do not visit the URL, download files, or follow redirects. The submitted URL is saved in your private history; remove access tokens or sensitive query values first. Heuristic signals are not proof of a threat.</p>
                </div>
              </section>

              {scanError && (
                <div className="url-scanner-alert" role="alert">
                  <span>{scanError}</span>
                  <button type="button" onClick={handleSubmit}>Retry scan</button>
                </div>
              )}
              {scanning ? <ScanProgress /> : report ? (
                <URLResultPage result={report} />
              ) : (
                <section className="url-scanner-placeholder">
                  <span><ScanSearch size={23} /></span>
                  <strong>Your URL scan report will appear here</strong>
                  <p>No URL has been scanned in this view. Submit a link to check its address and reputation status.</p>
                </section>
              )}
            </div>

            <aside className="url-history-card">
              <header className="url-scanner-card-heading">
                <span className="url-scanner-eyebrow">PRIVATE TO YOUR ACCOUNT</span>
                <h2>Recent URL scans</h2>
                <p>Your saved reports are visible only to your account.</p>
              </header>
              {historyLoading ? (
                <div className="url-history-state" role="status">Loading your scan history…</div>
              ) : historyError ? (
                <div className="url-history-state url-history-error" role="alert">
                  <p>{historyError}</p>
                  <button type="button" onClick={() => setHistoryRetry((count) => count + 1)}>Retry history</button>
                </div>
              ) : history.length ? (
                <div className="url-history-list">
                  {history.map((item) => (
                    <article className="url-history-item" key={item.id}>
                      <button
                        type="button"
                        className="url-history-open"
                        onClick={() => openReport(item.id)}
                        disabled={Boolean(selectedId) || Boolean(deletingId)}
                      >
                        <span className={`url-history-status status-${item.riskStatus.toLowerCase()}`} />
                        <span className="url-history-copy">
                          <strong>{item.normalizedDomain}</strong>
                          <small>{statusLabel(item.riskStatus)} · {formatScanTime(item.scanTimestamp)}</small>
                        </span>
                        {selectedId === item.id && <span className="url-history-loading">Loading</span>}
                      </button>
                      <button
                        type="button"
                        className="url-history-delete"
                        aria-label={`Delete scan for ${item.normalizedDomain}`}
                        title="Delete scan"
                        onClick={() => deleteReport(item.id)}
                        disabled={Boolean(deletingId) || Boolean(selectedId)}
                      >
                        <Trash2 size={15} />
                      </button>
                    </article>
                  ))}
                </div>
              ) : (
                <div className="url-history-empty">
                  <span><Link2 size={20} /></span>
                  <strong>No URL scans yet</strong>
                  <p>Your private scan reports will appear here after your first check.</p>
                </div>
              )}
            </aside>
          </div>
          <p className="url-scanner-disclaimer">
            No scanner can guarantee complete phishing protection. A clean reputation result or HTTPS connection does not prove a site is safe. Confirm important links through a trusted source.
          </p>
          <footer className="dashboard-footer">ScamShield AI · Your career safety workspace</footer>
        </main>
      </div>
    </div>
  );
}
