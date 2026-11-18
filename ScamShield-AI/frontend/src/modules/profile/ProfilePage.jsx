import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import DashboardHeader from "../dashboard/DashboardHeader.jsx";
import DashboardSidebar from "../dashboard/DashboardSidebar.jsx";
import { useAuth } from "../auth/AuthContext.jsx";
import AccountSecurity from "./components/AccountSecurity.jsx";
import ContactInfoForm from "./components/ContactInfoForm.jsx";
import EducationList from "./components/EducationList.jsx";
import PersonalInfoForm from "./components/PersonalInfoForm.jsx";
import ProfileCompletion from "./components/ProfileCompletion.jsx";
import ProfileHeader from "./components/ProfileHeader.jsx";
import ProfilePhotoUpload from "./components/ProfilePhotoUpload.jsx";
import ProfessionalBio from "./components/ProfessionalBio.jsx";
import SkillsManager from "./components/SkillsManager.jsx";
import { ProfileApiError, profileService } from "./services/profileService.js";
import "./profile.css";
import "../dashboard/dashboard.css";

export default function ProfilePage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [profile, setProfile] = useState(null);
  const [photoUrl, setPhotoUrl] = useState("");
  const photoUrlRef = useRef("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [retry, setRetry] = useState(0);

  const refreshPhoto = useCallback(async () => {
    try {
      const photo = await profileService.getPhoto();
      const objectUrl = URL.createObjectURL(photo);
      if (photoUrlRef.current) URL.revokeObjectURL(photoUrlRef.current);
      photoUrlRef.current = objectUrl;
      setPhotoUrl(objectUrl);
    } catch (photoError) {
      if (photoError instanceof ProfileApiError && photoError.status === 404) {
        if (photoUrlRef.current) URL.revokeObjectURL(photoUrlRef.current);
        photoUrlRef.current = "";
        setPhotoUrl("");
        return;
      }
      throw photoError;
    }
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await profileService.get();
      setProfile(data);
      try {
        await refreshPhoto();
      } catch (photoError) {
        setError(photoError.message);
      }
    } catch (loadError) {
      if (loadError.status === 401) {
        navigate("/login", { replace: true, state: { from: { pathname: "/profile" } } });
      } else {
        setError(loadError.message);
      }
    } finally {
      setLoading(false);
    }
  }, [navigate, refreshPhoto]);

  useEffect(() => { load(); }, [load, retry]);
  useEffect(() => () => {
    if (photoUrlRef.current) URL.revokeObjectURL(photoUrlRef.current);
  }, []);

  async function saveProfile(changes) {
    const updated = await profileService.update({
      fullName: changes.fullName ?? profile.fullName,
      phoneNumber: changes.phoneNumber ?? profile.phoneNumber ?? "",
      city: changes.city ?? profile.city ?? "",
      state: changes.state ?? profile.state ?? "",
      country: changes.country ?? profile.country ?? "",
      professionalBio: changes.professionalBio ?? profile.professionalBio ?? "",
    });
    setProfile(updated);
    setNotice("Profile changes saved.");
    setError("");
    if (changes.fullName) await refreshUser();
  }

  async function refreshProfile() {
    setProfile(await profileService.get());
  }

  async function saveSkills(skills) {
    const updatedSkills = await profileService.updateSkills(skills);
    setProfile((current) => ({ ...current, skills: updatedSkills }));
    setNotice("Your user-provided skills were saved.");
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
        <DashboardHeader user={user} title="My Profile" notificationCount={0} onMenuClick={() => setSidebarOpen(true)} onLogout={handleLogout} logoutPending={false} />
        <main className="dashboard-main profile-page">
          {loading ? <div className="profile-loading" role="status"><span /> <span /> <span /> Loading your profile…</div>
            : error && !profile ? (
              <section className="profile-error" role="alert"><h2>We couldn't load your profile.</h2><p>{error}</p><button className="profile-primary-button" type="button" onClick={() => setRetry((value) => value + 1)}>Try again</button></section>
            ) : profile ? <>
              <ProfileHeader fullName={profile.fullName} completion={profile.completionPercentage} />
              {error && <div className="profile-alert" role="alert">{error}</div>}
              {notice && <div className="profile-success" role="status">{notice}</div>}
              <ProfileCompletion percentage={profile.completionPercentage} />
              <div className="profile-main-grid">
                <div className="profile-main-column">
                  <PersonalInfoForm profile={profile} onSave={saveProfile} />
                  <ContactInfoForm profile={profile} onSave={saveProfile} />
                  <ProfessionalBio value={profile.professionalBio} onSave={(professionalBio) => saveProfile({ professionalBio })} />
                  <EducationList education={profile.education ?? []}
                    onAdd={async (education) => { await profileService.addEducation(education); await refreshProfile(); setNotice("Education added."); }}
                    onUpdate={async (id, education) => { await profileService.updateEducation(id, education); await refreshProfile(); setNotice("Education updated."); }}
                    onDelete={async (id) => { await profileService.deleteEducation(id); await refreshProfile(); setNotice("Education removed."); }} />
                  <SkillsManager skills={profile.skills ?? []} onSave={saveSkills} />
                </div>
                <aside className="profile-side-column">
                  <ProfilePhotoUpload photoUrl={photoUrl} fullName={profile.fullName} onPhotoChanged={async () => { await refreshPhoto(); await refreshProfile(); setNotice("Profile photo updated."); }} />
                  <AccountSecurity />
                </aside>
              </div>
            </> : null}
          <footer className="dashboard-footer">ScamShield AI · Your career safety workspace</footer>
        </main>
      </div>
    </div>
  );
}
