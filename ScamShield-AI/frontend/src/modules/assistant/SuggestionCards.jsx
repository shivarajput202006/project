import { ArrowUpRight, BriefcaseBusiness, FileSearch, ShieldAlert } from "lucide-react";
import { Link } from "react-router-dom";

const CARDS = [
  { label: "Analyze a resume", to: "/resume-analyzer", icon: FileSearch },
  { label: "Explore job matches", to: "/job-matching", icon: BriefcaseBusiness },
  { label: "Review a suspicious offer", to: "/scam-detector", icon: ShieldAlert },
];

export default function SuggestionCards() {
  return (
    <div className="assistant-suggestion-cards">
      {CARDS.map(({ label, to, icon: Icon }) => (
        <Link key={to} to={to}><Icon size={15} /><span>{label}</span><ArrowUpRight size={13} /></Link>
      ))}
    </div>
  );
}
