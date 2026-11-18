import { ArrowRight, ShieldCheck } from "lucide-react";
import { motion } from "framer-motion";
import PrimaryButton from "../../components/ui/PrimaryButton.jsx";

export default function CTASection() {
  return (
    <section className="cta-section">
      <motion.div
        className="page-container cta-card"
        initial={{ opacity: 0, y: 18 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.3 }}
        transition={{ duration: 0.5 }}
      >
        <div className="cta-glow" aria-hidden="true" />
        <span className="cta-icon"><ShieldCheck size={23} /></span>
        <span className="eyebrow">YOUR NEXT CHAPTER, WITH MORE CONFIDENCE</span>
        <h2>Your Career Deserves<br />a Safer Start.</h2>
        <p>Take a thoughtful first step toward making more informed career decisions.</p>
        <PrimaryButton to="/register">Get Started <ArrowRight size={17} /></PrimaryButton>
        <span className="cta-note">Authentication is coming in the next development phase.</span>
      </motion.div>
    </section>
  );
}
