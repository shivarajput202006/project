import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import DashboardHeader from "./DashboardHeader.jsx";
import DashboardNotifications from "./DashboardNotifications.jsx";
import DashboardSidebar from "./DashboardSidebar.jsx";
import QuickActions from "./QuickActions.jsx";
import RecentActivity from "./RecentActivity.jsx";
import SavedJobsPreview from "./SavedJobsPreview.jsx";
import StatsCards from "./StatsCards.jsx";
import WelcomeSection from "./WelcomeSection.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import { DashboardApiError, getDashboardOverview } from "./dashboardService.js";
import "./dashboard.css";

function DashboardLoading() {
  return (
    <div className="dashboard-loading-grid" role="status" aria-label="Loading dashboard">
      <span className="dashboard-skeleton dashboard-skeleton-title" />
      <span className="dashboard-skeleton dashboard-skeleton-welcome" />
      <span className="dashboard-skeleton dashboard-skeleton-stats" />
      <span className="dashboard-skeleton dashboard-skeleton-panel" />
      <span className="dashboard-skeleton dashboard-skeleton-panel" />
      <span className="dashboard-skeleton dashboard-skeleton-panel" />
    </div>
  );
}

export default function DashboardPage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [overview, setOverview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [logoutError, setLogoutError] = useState("");
  const [logoutPending, setLogoutPending] = useState(false);
  const [mobileNavigationOpen, setMobileNavigationOpen] = useState(false);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setLoadError("");
      try {
        const data = await getDashboardOverview();
        if (!cancelled) setOverview(data);
      } catch (error) {
        if (cancelled) return;
        if (error instanceof DashboardApiError && error.status === 401) {
          try {
            await refreshUser();
          } catch (refreshError) {
            setLoadError(refreshError.message);
            return;
          }
          navigate("/login", { replace: true, state: { from: { pathname: "/dashboard" } } });
          return;
        }
        setLoadError(error.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [navigate, refreshUser, retryCount]);

  async function handleLogout() {
    setLogoutError("");
    setLogoutPending(true);
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
          user={overview?.user ?? user}
          notificationCount={overview?.notifications?.length ?? 0}
          onMenuClick={() => setMobileNavigationOpen(true)}
          onLogout={handleLogout}
          logoutPending={logoutPending}
        />
        <main className="dashboard-main">
          {logoutError && <p className="dashboard-alert" role="alert">{logoutError}</p>}
          {loading ? (
            <DashboardLoading />
          ) : loadError ? (
            <section className="dashboard-error" role="alert">
              <span className="dashboard-eyebrow">DASHBOARD UNAVAILABLE</span>
              <h1>We couldn't load your overview.</h1>
              <p>{loadError}</p>
              <button
                type="button"
                className="dashboard-primary-button"
                onClick={() => setRetryCount((count) => count + 1)}
              >
                Try again
              </button>
            </section>
          ) : (
            <>
              <WelcomeSection user={overview?.user ?? user} />
              <StatsCards stats={overview.stats} />
              <QuickActions />
              <div className="dashboard-content-grid">
                <RecentActivity activities={overview.recentActivities} />
                <div className="dashboard-side-panels">
                  <SavedJobsPreview jobs={overview.savedJobs} />
                  <DashboardNotifications notifications={overview.notifications} />
                </div>
              </div>
            </>
          )}
          <footer className="dashboard-footer">ScamShield AI · Your career safety workspace</footer>
        </main>
      </div>
    </div>
  );
}
