import { Mail, MapPin, Phone, UserRound } from "lucide-react";

export default function PersonalInfoCard({ personalInfo }) {
  if (!personalInfo) return null;
  const entries = [
    ["Name", personalInfo.name, UserRound],
    ["Email", personalInfo.email, Mail],
    ["Phone", personalInfo.phone, Phone],
    ["Location", personalInfo.location, MapPin],
    ["LinkedIn", personalInfo.linkedin, null],
    ["Portfolio", personalInfo.portfolio, null],
  ].filter(([, value]) => value);
  return (
    <section className="resume-card" aria-labelledby="resume-personal-title">
      <div className="resume-section-heading"><div><span className="dashboard-eyebrow">EXTRACTED INFORMATION</span><h2 id="resume-personal-title">Personal information</h2></div></div>
      {entries.length
        ? <dl className="resume-personal-grid">{entries.map(([label, value, Icon]) => (
          <div key={label}>{Icon && <Icon size={15} aria-hidden="true" />}<dt>{label}</dt><dd>{value}</dd></div>
        ))}</dl>
        : <p className="resume-muted">No personal information was identified in the document.</p>}
    </section>
  );
}
