import { ArrowLeft, LockKeyhole } from "lucide-react";
import { Link } from "react-router-dom";
import "./profile.css";

export default function PrivacyPolicyPage() {
  return (
    <main className="profile-policy-page">
      <Link to="/settings" className="profile-back-link"><ArrowLeft size={15} /> Back to settings</Link>
      <section className="profile-card profile-policy-card">
        <span className="profile-eyebrow"><LockKeyhole size={14} /> SCAMSHIELD AI</span>
        <h1>Privacy and data practices</h1>
        <p>This page describes how the current application handles account data. It is an application disclosure, not a substitute for legal advice.</p>
        <h2>Account information</h2>
        <p>Your profile and settings are stored by the backend and are available only to your authenticated account. Your login email is read-only in profile settings.</p>
        <h2>Resumes and AI analysis</h2>
        <p>Resume files are stored privately in MongoDB GridFS and accessed through owner-scoped endpoints. Resume analysis requires both the AI processing preference and a separate confirmation at the time you request analysis. When enabled and confirmed, resume text is sent to the configured AI provider for processing.</p>
        <h2>Analysis history and job search</h2>
        <p>Your scam checks, URL scans, resume analyses, and job matches are stored as owner-scoped history. Job searches use the configured job provider; its shared listing cache is separate from your account and is not removed when you delete your account.</p>
        <h2>Export and deletion</h2>
        <p>Settings lets you download an archive of your profile, saved references, stored analysis history, and uploaded resume files. Account deletion requires your current password and explicit confirmation; it removes the account's stored data and signs out the current session.</p>
        <p>Use the <Link to="/settings">account settings</Link> to manage AI processing, request an export, or delete your account.</p>
      </section>
    </main>
  );
}
