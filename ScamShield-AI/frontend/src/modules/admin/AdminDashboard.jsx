import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { BriefcaseBusiness, FileCheck2, SearchCheck, Users, UserRoundCheck, X } from "lucide-react";
import { useAuth } from "../auth/AuthContext.jsx";
import AdminHeader from "./AdminHeader.jsx";
import AnalyticsCharts from "./AnalyticsCharts.jsx";
import AdminSecurityEvents from "./AdminSecurityEvents.jsx";
import AdminSidebar from "./AdminSidebar.jsx";
import AdminStatCard from "./AdminStatCard.jsx";
import AdminSystemHealth from "./AdminSystemHealth.jsx";
import AdminUserManagement from "./AdminUserManagement.jsx";
import { adminService } from "./adminService.js";
import "./admin.css";
import "./analytics.css";

const sections = {
  overview: { title: "Platform overview", description: "Live counts from existing ScamShield AI collections." },
  users: { title: "User management", description: "Browse account records. Account changes are not enabled." },
  security: { title: "Security monitoring", description: "Review recorded security notifications." },
  system: { title: "System health", description: "A lightweight live connectivity check." },
};

function LoadingCards() {
  return <div className="admin-stats-grid" role="status" aria-label="Loading platform metrics">{Array.from({ length: 6 }, (_, index) => <span className="admin-skeleton" key={index} />)}</div>;
}

export default function AdminDashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [section, setSection] = useState("overview");
  const [metrics, setMetrics] = useState(null);
  const [events, setEvents] = useState([]);
  const [health, setHealth] = useState(null);
  const [users, setUsers] = useState(null);
  const [search, setSearch] = useState("");
  const [userPage, setUserPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [userLoading, setUserLoading] = useState(false);
  const [error, setError] = useState("");
  const [menuOpen, setMenuOpen] = useState(false);
  const [refresh, setRefresh] = useState(0);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError("");
      try {
        const [overview, securityEvents, systemHealth] = await Promise.all([
          adminService.getOverview(),
          adminService.getSecurityEvents(),
          adminService.getHealth(),
        ]);
        if (!cancelled) {
          setMetrics(overview);
          setEvents(securityEvents);
          setHealth(systemHealth);
        }
      } catch (requestError) {
        if (!cancelled) setError(requestError.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [refresh]);

  useEffect(() => {
    if (section !== "users") return undefined;
    let active = true;
    (async () => {
      setUserLoading(true);
      setUsers(null);
      try {
        const result = await adminService.getUsers({ search, page: userPage });
        if (active) {
          setUsers(result);
          setError("");
        }
      } catch (requestError) {
        if (active) setError(requestError.message);
      } finally {
        if (active) setUserLoading(false);
      }
    })();
    return () => { active = false; };
  }, [section, search, userPage, refresh]);

  useEffect(() => {
    if (error && error.status === 401) navigate("/login", { replace: true });
    if (error && error.status === 403) navigate("/dashboard", { replace: true });
  }, [error, navigate]);

  async function handleLogout() {
    try {
      await logout();
      navigate("/login", { replace: true });
    } catch (logoutError) {
      setError(logoutError.message);
    }
  }

  const current = sections[section];
  return (
    <div className="admin-shell">
      {menuOpen && <button className="admin-backdrop" type="button" aria-label="Close navigation" onClick={() => setMenuOpen(false)} />}
      <AdminSidebar active={section} isOpen={menuOpen} onClose={() => setMenuOpen(false)} onSelect={(next) => { setSection(next); setMenuOpen(false); }} />
      <div className="admin-workspace">
        <AdminHeader user={user} refreshing={loading || userLoading} onMenu={() => setMenuOpen(true)} onRefresh={() => setRefresh((value) => value + 1)} onLogout={handleLogout} />
        <main className="admin-main">
          <div className="admin-title-row"><div><h2>{current.title}</h2><p>{current.description}</p></div><span className="admin-live-tag">LIVE DATA</span></div>
          {error && <div className="admin-alert" role="alert"><span>{error.message ?? error}</span><button type="button" onClick={() => setRefresh((value) => value + 1)} aria-label="Dismiss error"><X size={16} /></button></div>}
          {section === "overview" && (
            loading && !metrics ? <LoadingCards /> : metrics && (
              <>
                <div className="admin-stats-grid">
                  <AdminStatCard label="Registered users" value={metrics.totalUsers} note="Accounts on platform" icon={Users} />
                  <AdminStatCard label="Active users" value={metrics.activeUsers} note="Activity status is not tracked" icon={UserRoundCheck} />
                  <AdminStatCard label="Scam analyses" value={metrics.scamAnalyses} note="Stored analyses" icon={SearchCheck} />
                  <AdminStatCard label="URL scans" value={metrics.urlScans} note="Stored scans" icon={SearchCheck} />
                  <AdminStatCard label="Resume analyses" value={metrics.resumeAnalyses} note="Stored analyses" icon={FileCheck2} />
                  <AdminStatCard label="Job searches" value={metrics.jobSearches} note="Search events are not stored" icon={BriefcaseBusiness} />
                  <AdminStatCard label="Job match analyses" value={metrics.jobMatchAnalyses} note="Stored match results" icon={BriefcaseBusiness} />
                  <AdminStatCard label="Stored job listings" value={metrics.storedJobListings} note="Listings retained for saved jobs" icon={BriefcaseBusiness} />
                </div>
                <div className="admin-overview-grid">
                  <AnalyticsCharts metrics={metrics} />
                  <AdminSecurityEvents events={events} />
                  <AdminSystemHealth health={health} />
                </div>
                <section className="admin-panel admin-audit-note">
                  <span className="admin-eyebrow">ADMIN ACTIVITY</span>
                  <h2>Audit history is not configured</h2>
                  <p>No administrator action log exists in the current application, so no activity entries are shown.</p>
                </section>
              </>
            )
          )}
          {section === "users" && (
            <AdminUserManagement data={users} search={search} loading={userLoading} error={Boolean(error)} onSearch={(value) => { setSearch(value); setUserPage(0); }} onPage={setUserPage} />
          )}
          {section === "security" && <AdminSecurityEvents events={events} />}
          {section === "system" && <AdminSystemHealth health={health} />}
          <footer className="admin-footer">ScamShield AI · Administrator access is audited only by authentication logs currently available.</footer>
        </main>
      </div>
    </div>
  );
}
