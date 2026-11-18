import {
  Bot, BriefcaseBusiness, FileUser, Flag, Link2, ScanSearch,
} from "lucide-react";
import FeatureCard from "../../components/ui/FeatureCard.jsx";

const features = [
  {
    icon: ScanSearch,
    title: "AI Scam Detector",
    description: "Review suspicious job messages, emails, chat messages, offer letters, screenshots, and documents for warning signs.",
  },
  {
    icon: Link2,
    title: "Smart URL Scanner",
    description: "Check a suspicious link for common phishing indicators before deciding whether to open it.",
  },
  {
    icon: BriefcaseBusiness,
    title: "Live Job Finder",
    description: "Explore opportunities from trusted sources, with clear source details and links to official applications.",
  },
  {
    icon: FileUser,
    title: "AI Resume Analyzer",
    description: "Identify skills, get constructive resume suggestions, and find roles that align with your experience.",
  },
  {
    icon: Flag,
    title: "Scam Reporting",
    description: "Report suspicious offers and help build awareness so others can make more informed decisions.",
  },
  {
    icon: Bot,
    title: "AI Career Assistant",
    description: "Get practical career guidance, job-safety advice, and skill recommendations for your next step.",
  },
];

export default function FeaturesSection() {
  return (
    <section className="section features-section" id="features">
      <div className="page-container">
        <div className="section-heading">
          <span className="eyebrow">CAREER SAFETY, REIMAGINED</span>
          <h2>More clarity for every <span className="text-gradient">career move.</span></h2>
          <p>Practical tools to help you spot warning signs, explore possibilities, and take your next step with care.</p>
        </div>
        <div className="features-grid">
          {features.map((feature, index) => (
            <FeatureCard key={feature.title} {...feature} index={index} />
          ))}
        </div>
      </div>
    </section>
  );
}
