import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Eye, EyeOff, LoaderCircle, LockKeyhole, Mail, UserRound } from "lucide-react";
import { useAuth } from "./AuthContext.jsx";
import { getPasswordStrength, validateRegistration } from "./validation.js";

export default function RegisterPage() {
  const { register, initializationError } = useAuth();
  const navigate = useNavigate();
  const [values, setValues] = useState({
    fullName: "", email: "", password: "", confirmPassword: "", acceptTerms: false,
  });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const strength = getPasswordStrength(values.password);

  function updateField(event) {
    const { name, value, checked, type } = event.target;
    setValues((current) => ({ ...current, [name]: type === "checkbox" ? checked : value }));
    setErrors((current) => ({ ...current, [name]: "" }));
    setMessage("");
  }

  async function handleSubmit(event) {
    event.preventDefault();
    const validationErrors = validateRegistration(values);
    setErrors(validationErrors);
    setMessage("");
    if (Object.keys(validationErrors).length) return;
    setSubmitting(true);
    try {
      await register(
        values.fullName.trim(),
        values.email.trim(),
        values.password,
        values.confirmPassword,
      );
      navigate("/dashboard", { replace: true });
    } catch (error) {
      setMessage(error.message);
      setErrors(error.fieldErrors ?? {});
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-card auth-card-register">
      <span className="eyebrow">START WITH CONFIDENCE</span>
      <h2>Create your account</h2>
      <p className="auth-card-intro">A few details and you’re ready to take your next step.</p>
      {(message || initializationError) && (
        <div className="auth-alert" role="alert">{message || initializationError}</div>
      )}
      <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <label className="auth-label" htmlFor="register-name">Full name</label>
        <div className={`auth-input-wrap${errors.fullName ? " has-error" : ""}`}>
          <UserRound size={17} aria-hidden="true" />
          <input id="register-name" name="fullName" type="text" autoComplete="name" maxLength={100} placeholder="Your full name" value={values.fullName} onChange={updateField} aria-invalid={Boolean(errors.fullName)} />
        </div>
        {errors.fullName && <span className="auth-field-error">{errors.fullName}</span>}

        <label className="auth-label" htmlFor="register-email">Email address</label>
        <div className={`auth-input-wrap${errors.email ? " has-error" : ""}`}>
          <Mail size={17} aria-hidden="true" />
          <input id="register-email" name="email" type="email" autoComplete="email" maxLength={254} placeholder="you@example.com" value={values.email} onChange={updateField} aria-invalid={Boolean(errors.email)} />
        </div>
        {errors.email && <span className="auth-field-error">{errors.email}</span>}

        <label className="auth-label" htmlFor="register-password">Password</label>
        <div className={`auth-input-wrap${errors.password ? " has-error" : ""}`}>
          <LockKeyhole size={17} aria-hidden="true" />
          <input id="register-password" name="password" type={showPassword ? "text" : "password"} autoComplete="new-password" maxLength={72} placeholder="At least 12 characters" value={values.password} onChange={updateField} aria-invalid={Boolean(errors.password)} />
          <button className="password-toggle" type="button" aria-label={showPassword ? "Hide password" : "Show password"} onClick={() => setShowPassword((visible) => !visible)}>
            {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
          </button>
        </div>
        <div className="password-strength" aria-live="polite">
          <div className="strength-bars" aria-hidden="true">
            {[1, 2, 3, 4].map((bar) => (
              <span className={bar <= strength.score ? `strength-${strength.score}` : ""} key={bar} />
            ))}
          </div>
          <span>{strength.label}</span>
        </div>
        {errors.password && <span className="auth-field-error">{errors.password}</span>}

        <label className="auth-label" htmlFor="register-confirm">Confirm password</label>
        <div className={`auth-input-wrap${errors.confirmPassword ? " has-error" : ""}`}>
          <LockKeyhole size={17} aria-hidden="true" />
          <input id="register-confirm" name="confirmPassword" type={showConfirmPassword ? "text" : "password"} autoComplete="new-password" maxLength={72} placeholder="Enter your password again" value={values.confirmPassword} onChange={updateField} aria-invalid={Boolean(errors.confirmPassword)} />
          <button className="password-toggle" type="button" aria-label={showConfirmPassword ? "Hide confirmation password" : "Show confirmation password"} onClick={() => setShowConfirmPassword((visible) => !visible)}>
            {showConfirmPassword ? <EyeOff size={17} /> : <Eye size={17} />}
          </button>
        </div>
        {errors.confirmPassword && <span className="auth-field-error">{errors.confirmPassword}</span>}

        <div className="terms-field">
          <label><input type="checkbox" name="acceptTerms" checked={values.acceptTerms} onChange={updateField} /> <span>I agree to the <a href="#terms">Terms</a> and <Link to="/privacy-policy">Privacy Policy</Link>.</span></label>
          {errors.acceptTerms && <span className="auth-field-error">{errors.acceptTerms}</span>}
        </div>
        <button className="button button-primary auth-submit" type="submit" disabled={submitting}>
          {submitting ? <><LoaderCircle size={17} className="auth-spinner" /> Creating account…</> : "Create account"}
        </button>
      </form>
      <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
      <p className="auth-privacy"><LockKeyhole size={13} /> Your password is securely hashed by the server.</p>
    </div>
  );
}
