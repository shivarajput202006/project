export const PASSWORD_MIN_LENGTH = 12;
export const PASSWORD_MAX_LENGTH = 72;

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateEmail(email) {
  const normalizedEmail = email.trim();
  if (!normalizedEmail) return "Email is required.";
  if (!EMAIL_PATTERN.test(normalizedEmail)) return "Enter a valid email address.";
  return "";
}

export function validateLogin(values) {
  const errors = {};
  const emailError = validateEmail(values.email);
  if (emailError) errors.email = emailError;
  if (!values.password) errors.password = "Password is required.";
  return errors;
}

export function validateRegistration(values) {
  const errors = {};
  const emailError = validateEmail(values.email);
  if (emailError) errors.email = emailError;
  if (!values.fullName.trim()) errors.fullName = "Full name is required.";
  if (values.fullName.trim().length > 100) errors.fullName = "Name must be 100 characters or fewer.";
  if (values.password.length < PASSWORD_MIN_LENGTH) {
    errors.password = `Use at least ${PASSWORD_MIN_LENGTH} characters.`;
  } else if (values.password.length > PASSWORD_MAX_LENGTH) {
    errors.password = `Use no more than ${PASSWORD_MAX_LENGTH} characters.`;
  }
  if (values.confirmPassword !== values.password) {
    errors.confirmPassword = "Passwords do not match.";
  }
  if (!values.acceptTerms) errors.acceptTerms = "You must accept the terms to create an account.";
  return errors;
}

export function getPasswordStrength(password) {
  if (!password) return { score: 0, label: "Enter a password" };
  let score = 0;
  if (password.length >= PASSWORD_MIN_LENGTH) score += 1;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score += 1;
  if (/\d/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;
  const labels = ["Weak", "Fair", "Good", "Strong", "Strong"];
  return { score, label: labels[score] };
}
