import { ArrowRight, ShieldCheck } from "lucide-react";
import { Link } from "react-router-dom";

export default function ProfileHeader({ fullName, completion }) {
  return (
    <section className="profile-hero">
      <div>
        <span className="profile-eyebrow"><ShieldCheck size={14} /> YOUR ACCOUNT</span>
        <h2>{fullName ? `Welcome, ${fullName}` : "Your profile"}</h2>
        <p>Manage your personal information, education, and skills in one place.</p>
      </div>
      <div className="profile-hero-side">
        <span className="profile-completion-chip">{completion}% complete</span>
        <Link to="/settings" className="profile-secondary-button">Account settings <ArrowRight size={15} /></Link>
      </div>
    </section>
  );
}
