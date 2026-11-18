const MAX_URL_LENGTH = 2048;

export function validateURL(value) {
  const submittedUrl = value.trim();
  if (!submittedUrl) return { valid: false, message: "Enter the URL you want to check." };
  if (submittedUrl.length > MAX_URL_LENGTH) {
    return { valid: false, message: `URLs must be ${MAX_URL_LENGTH} characters or fewer.` };
  }

  let parsed;
  try {
    parsed = new URL(submittedUrl);
  } catch {
    return { valid: false, message: "Enter a complete URL, such as https://example.com/apply." };
  }
  if (!["http:", "https:"].includes(parsed.protocol)) {
    return { valid: false, message: "Only HTTP and HTTPS URLs are supported." };
  }
  if (!parsed.hostname || parsed.username || parsed.password) {
    return { valid: false, message: "Enter a URL with a hostname and no embedded username or password." };
  }
  return { valid: true, submittedUrl };
}
