import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Eye, EyeOff, LoaderCircle, LockKeyhole, Mail } from "lucide-react";
import { useAuth } from "./AuthContext.jsx";
import { validateLogin } from "./validation.js";

export default function LoginPage() {
  const { login, initializationError } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [values, setValues] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState(location.state?.message ?? "");
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  function updateField(event) {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: "" }));
    setMessage("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const validationErrors = validateLogin(values);
    setErrors(validationErrors);
    setMessage("");
    if (Object.keys(validationErrors).length) return;
    setSubmitting(true);
    try {
      await login(values.email.trim(), values.password);
      const destination = location.state?.from;
      navigate(
        destination
          ? {
              pathname: destination.pathname,
              search: destination.search,
              hash: destination.hash,
            }
          : "/dashboard",
        { replace: true },
      );
    } catch (error) {
      setMessage(error.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-card">
      <span className="eyebrow">WELCOME BACK</span>
      <h2>Sign in to your account</h2>
      <p className="auth-card-intro">Pick up where you left off on your career-safety journey.</p>
      {(message || initializationError) && (
        <div className="auth-alert" role="alert">{message || initializationError}</div>
      )}
      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <label className="auth-label" htmlFor="login-email">Email address</label>
        <div className={`auth-input-wrap${errors.email ? " has-error" : ""}`}>
          <Mail size={17} aria-hidden="true" />
          <input id="login-email" name="email" type="email" autoComplete="email" placeholder="you@example.com" value={values.email} onChange={updateField} aria-invalid={Boolean(errors.email)} aria-describedby={errors.email ? "login-email-error" : undefined} />
        </div>
        {errors.email && <span className="auth-field-error" id="login-email-error">{errors.email}</span>}

        <div className="auth-label-row">
          <label className="auth-label" htmlFor="login-password">Password</label>
          <Link className="auth-inline-link" to="/forgot-password">Forgot password?</Link>
        </div>
        <div className={`auth-input-wrap${errors.password ? " has-error" : ""}`}>
          <LockKeyhole size={17} aria-hidden="true" />
          <input id="login-password" name="password" type={showPassword ? "text" : "password"} autoComplete="current-password" placeholder="Enter your password" value={values.password} onChange={updateField} aria-invalid={Boolean(errors.password)} aria-describedby={errors.password ? "login-password-error" : undefined} />
          <button className="password-toggle" type="button" aria-label={showPassword ? "Hide password" : "Show password"} onClick={() => setShowPassword((visible) => !visible)}>
            {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
          </button>
        </div>
        {errors.password && <span className="auth-field-error" id="login-password-error">{errors.password}</span>}

        <button className="button button-primary auth-submit" type="submit" disabled={submitting}>
          {submitting ? <><LoaderCircle size={17} className="auth-spinner" /> Signing in…</> : "Sign in"}
        </button>
      </form>
      <p className="auth-switch">New to ScamShield? <Link to="/register">Create an account</Link></p>
      <p className="auth-privacy"><LockKeyhole size={13} /> Your password is never stored in this browser.</p>
    </div>
  );
}
