import { Fingerprint, KeyRound, ShieldCheck } from "lucide-react";

export default function AccountSecurity() {
  return (
    <section className="profile-card">
      <div className="profile-card-heading"><div><span className="profile-eyebrow">ACCOUNT SECURITY</span><h2>Security at a glance</h2></div><ShieldCheck size={18} /></div>
      <div className="profile-security-list">
        <div><KeyRound size={16} /><span><strong>Password protection</strong><small>Credentials are stored as BCrypt hashes and never returned by account APIs.</small></span><b>Enabled</b></div>
        <div><Fingerprint size={16} /><span><strong>Session control</strong><small>Authentication uses a private, HttpOnly cookie. Password changes invalidate existing tokens.</small></span><b>Enabled</b></div>
      </div>
    </section>
  );
}
