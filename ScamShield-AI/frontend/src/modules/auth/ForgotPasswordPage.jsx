import { Link } from "react-router-dom";
import { ArrowLeft, KeyRound } from "lucide-react";

export default function ForgotPasswordPage() {
  return (
    <div className="auth-card recovery-card">
      <span className="recovery-icon"><KeyRound size={24} /></span>
      <span className="eyebrow">ACCOUNT RECOVERY</span>
      <h2>Password recovery</h2>
      <p className="auth-card-intro">
        Email-based password recovery has not been configured yet. We have not
        sent a recovery email or changed your account.
      </p>
      <div className="auth-info-box">
        Recovery will be available in a future update. For now, return to sign
        in and use your existing password.
      </div>
      <Link className="button button-secondary recovery-back" to="/login">
        <ArrowLeft size={16} /> Back to sign in
      </Link>
    </div>
  );
}
