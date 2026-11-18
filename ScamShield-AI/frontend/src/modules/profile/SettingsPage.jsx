import { useCallback, useEffect, useState } from "react";
import { ArrowLeft } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import AccountSecurity from "./components/AccountSecurity.jsx";
import ChangePasswordForm from "./components/ChangePasswordForm.jsx";
import DataExportSection from "./components/DataExportSection.jsx";
import DeleteAccountSection from "./components/DeleteAccountSection.jsx";
import PrivacySettings from "./components/PrivacySettings.jsx";
import { settingsService } from "./services/settingsService.js";
import "./profile.css";
import "../dashboard/dashboard.css";

export default function SettingsPage() {
  const { user, logout, invalidateSession } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setSettings(await settingsService.get());
    } catch (loadError) {
      if (loadError.status === 401) {
        navigate("/login", { replace: true, state: { from: { pathname: "/settings" } } });
      } else {
        setError(loadError.message);
      }
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  useEffect(() => { load(); }, [load, retry]);

  async function handleLogout() {
    try {
      await logout();
      navigate("/", { replace: true });
    } catch (logoutError) {
      setError(logoutError.message);
    }
  }

  function signOutForPasswordChange() {
    invalidateSession();
    navigate("/login", { replace: true, state: { message: "Password updated. Sign in again with your new password." } });
  }

  function finishAccountDeletion() {
    invalidateSession();
    navigate("/", { replace: true });
  }

  return (
    <div className="dashboard-shell">
      {sidebarOpen && <button className="dashboard-backdrop" type="button" aria-label="Close navigation" onClick={() => setSidebarOpen(false)} />}
      <DashboardSidebar isOpen={sidebarOpen} onNavigate={() => setSidebarOpen(false)} onLogout={handleLogout} logoutPending={false} />
      <div className="dashboard-workspace">
        <DashboardHeader user={user} title="Settings" notificationCount={0} onMenuClick={() => setSidebarOpen(true)} onLogout={handleLogout} logoutPending={false} />
        <main className="dashboard-main profile-page profile-settings-page">
          <Link className="profile-back-link" to="/profile"><ArrowLeft size={15} /> Back to profile</Link>
          {loading ? <div className="profile-loading" role="status"><span /> Loading your settings…</div>
            : error ? <section className="profile-error" role="alert"><h2>We couldn't load your settings.</h2><p>{error}</p><button className="profile-primary-button" type="button" onClick={() => setRetry((value) => value + 1)}>Try again</button></section>
              : settings && <>
                <section className="profile-hero profile-settings-hero"><div><span className="profile-eyebrow">ACCOUNT PREFERENCES</span><h2>Privacy and security settings</h2><p>Control AI resume processing and manage your account data.</p></div></section>
                <PrivacySettings settings={settings} onSaved={setSettings} />
                <ChangePasswordForm onChanged={signOutForPasswordChange} />
                <AccountSecurity />
                <DataExportSection />
                <div className="profile-privacy-policy"><span>Want to understand how features use your information?</span><Link to="/privacy-policy">Read the privacy policy</Link></div>
                <DeleteAccountSection onDeleted={finishAccountDeletion} />
              </>}
          <footer className="dashboard-footer">ScamShield AI · Your career safety workspace</footer>
        </main>
      </div>
    </div>
  );
}
