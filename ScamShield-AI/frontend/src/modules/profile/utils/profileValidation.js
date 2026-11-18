export function validateProfile(values) {
  const errors = {};
  if (!values.fullName?.trim()) errors.fullName = "Enter your full name.";
  if (values.fullName?.trim().length > 100) errors.fullName = "Name must be 100 characters or fewer.";
  if (values.phoneNumber && !/^[+()0-9 .-]{7,25}$/.test(values.phoneNumber.trim())) {
    errors.phoneNumber = "Enter a valid phone number.";
  }
  for (const field of ["city", "state", "country"]) {
    if (values[field]?.length > 100) errors[field] = "Must be 100 characters or fewer.";
  }
  if (values.professionalBio?.length > 2000) errors.professionalBio = "Bio must be 2,000 characters or fewer.";
  return errors;
}

export function validateEducation(values) {
  const errors = {};
  for (const field of ["institutionName", "degree", "course"]) {
    if (!values[field]?.trim()) errors[field] = "This field is required.";
  }
  const currentYear = new Date().getFullYear();
  if (!Number.isInteger(Number(values.startYear)) || values.startYear < 1950 || values.startYear > currentYear + 5) {
    errors.startYear = "Enter a valid start year.";
  }
  if (values.status === "COMPLETED" && !values.endYear) errors.endYear = "Completed education requires an end year.";
  if (values.endYear && Number(values.endYear) < Number(values.startYear)) {
    errors.endYear = "End year cannot be earlier than start year.";
  }
  if (values.status === "IN_PROGRESS" && values.endYear) {
    errors.endYear = "Leave the end year empty while in progress.";
  }
  const max = values.gradeType === "CGPA" ? 10 : 100;
  if (values.gradeValue !== "" && (!values.gradeType || Number(values.gradeValue) < 0 || Number(values.gradeValue) > max)) {
    errors.gradeValue = values.gradeType === "CGPA" ? "CGPA must be between 0 and 10." : "Percentage must be between 0 and 100.";
  }
  if (Boolean(values.gradeType) !== Boolean(values.gradeValue)) {
    errors.gradeValue = "Choose both a grade type and value, or leave both empty.";
  }
  return errors;
}

export function validatePhoto(file) {
  if (!file) return "Choose an image to upload.";
  if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
    return "Choose a JPEG, PNG, or WEBP image.";
  }
  if (file.size > 5 * 1024 * 1024) return "Profile photos must be 5 MB or smaller.";
  return "";
}

export function validatePassword(values) {
  const errors = {};
  if (!values.currentPassword) errors.currentPassword = "Enter your current password.";
  const password = values.newPassword ?? "";
  if (password.length < 12 || !/[A-Z]/.test(password) || !/[a-z]/.test(password)
      || !/[0-9]/.test(password) || !/[^A-Za-z0-9]/.test(password)) {
    errors.newPassword = "Use 12+ characters with uppercase, lowercase, a number, and a symbol.";
  }
  if (password !== values.confirmPassword) errors.confirmPassword = "Passwords do not match.";
  return errors;
}
