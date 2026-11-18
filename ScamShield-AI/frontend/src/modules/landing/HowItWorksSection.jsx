import { ArrowDown, ClipboardList, ScanSearch, ShieldCheck, Lightbulb } from "lucide-react";

const steps = [
  { icon: ClipboardList, number: "01", title: "Submit suspicious information", description: "Share the message, offer, document, or URL you want to check." },
  { icon: ScanSearch, number: "02", title: "AI analyzes the content", description: "Content is reviewed for patterns and potential warning signs." },
  { icon: ShieldCheck, number: "03", title: "Review findings and risk indicators", description: "Understand the evidence and indicators in clear language." },
  { icon: Lightbulb, number: "04", title: "Follow AI safety recommendations", description: "Use practical guidance to decide how you want to proceed." },
];

export default function HowItWorksSection() {
  return (
    <section className="section workflow-section" id="how-it-works">
      <div className="page-container">
        <div className="section-heading">
          <span className="eyebrow">SIMPLE BY DESIGN</span>
          <h2>From uncertainty to a <span className="text-gradient">clearer next step.</span></h2>
          <p>You stay in control at every step. ScamShield helps you look closer—it never makes the decision for you.</p>
        </div>
        <div className="steps-grid">
          {steps.map(({ icon: Icon, number, title, description }, index) => (
            <article className="step-card" key={number}>
              <div className="step-top">
                <span className="step-icon"><Icon size={21} /></span>
                <span className="step-number">{number}</span>
              </div>
              <h3>{title}</h3>
              <p>{description}</p>
              {index < steps.length - 1 && <ArrowDown className="step-connector" size={16} aria-hidden="true" />}
            </article>
          ))}
        </div>
        <p className="workflow-note">Risk indicators are informational—not a guarantee that an opportunity is safe or unsafe.</p>
      </div>
    </section>
  );
}
