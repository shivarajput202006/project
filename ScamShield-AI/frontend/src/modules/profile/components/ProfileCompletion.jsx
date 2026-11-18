import { CheckCircle2 } from "lucide-react";

export default function ProfileCompletion({ percentage }) {
  return (
    <section className="profile-card profile-completion-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">PROFILE COMPLETION</span><h2>Make your profile useful</h2></div><CheckCircle2 size={18} /></div>
      <div className="profile-progress-row"><strong>{percentage}%</strong><span>based on completed profile sections</span></div>
      <div className="profile-progress-track" role="progressbar" aria-valuenow={percentage} aria-valuemin="0" aria-valuemax="100">
        <span style={{ width: `${percentage}%` }} />
      </div>
    </section>
  );
}
