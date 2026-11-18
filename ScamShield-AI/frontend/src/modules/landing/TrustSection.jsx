import { BadgeCheck, Eye, Fingerprint, Sparkles } from "lucide-react";

const benefits = [
  { icon: BadgeCheck, title: "Evidence over assumptions", description: "Risk assessments should point to observable indicators and explain what they may mean." },
  { icon: Fingerprint, title: "Privacy-conscious by design", description: "Your sensitive career information deserves care and thoughtful handling." },
  { icon: Eye, title: "Know where opportunities come from", description: "Source transparency helps you verify details and continue on official channels." },
  { icon: Sparkles, title: "Recommendations, not guarantees", description: "AI can help you think through options, while the final decision stays with you." },
];

export default function TrustSection() {
  return (
    <section className="section trust-section" id="about">
      <div className="page-container trust-layout">
        <div className="trust-intro">
          <span className="eyebrow">BUILT FOR BETTER DECISIONS</span>
          <h2>Protection starts with <span className="text-gradient">knowing more.</span></h2>
          <p>Online opportunities can be complex. We believe helpful tools should be transparent about what they know—and what they can’t promise.</p>
        </div>
        <div className="trust-list">
          {benefits.map(({ icon: Icon, title, description }) => (
            <article className="trust-item" key={title}>
              <span className="trust-icon"><Icon size={19} /></span>
              <div><h3>{title}</h3><p>{description}</p></div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
