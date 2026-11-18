export function scoreLabel(score) {
  if (!Number.isFinite(score)) return "Score unavailable";
  if (score >= 80) return "Strong overlap";
  if (score >= 60) return "Relevant overlap";
  return "Some overlap";
}

export function sortMatches(matches, sortBy = "best") {
  const result = [...matches];
  if (sortBy === "recent") {
    const timestamp = (value) => value ? Date.parse(value) || 0 : 0;
    return result.sort((a, b) => timestamp(b.job.postedAt) - timestamp(a.job.postedAt));
  }
  if (sortBy === "title") {
    return result.sort((a, b) => a.job.title.localeCompare(b.job.title));
  }
  return result.sort((a, b) => (b.score ?? -1) - (a.score ?? -1));
}
