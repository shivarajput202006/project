export function formatAdminNumber(value) {
  return value == null ? "—" : new Intl.NumberFormat().format(value);
}

export function formatAdminDate(value) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}
