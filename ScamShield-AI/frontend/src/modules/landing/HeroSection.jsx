import { motion } from "framer-motion";
import {
  ArrowDownRight,
  ArrowRight,
  BriefcaseBusiness,
  Check,
  FileSearch2,
  Fingerprint,
  Link2,
  LockKeyhole,
  Shield,
  Sparkles,
} from "lucide-react";
import PrimaryButton from "../../components/ui/PrimaryButton.jsx";

export default function HeroSection() {
  return (
    <section className="hero-section" id="home">
      <div className="hero-glow" aria-hidden="true" />
      <div className="page-container hero-grid">
        <motion.div
          className="hero-copy"
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.65 }}
        >
          <div className="hero-badge"><span className="badge-pulse" /> A smarter way to navigate your career</div>
          <h1>Detect Scams.<br />Discover <span className="text-gradient">Trusted</span><br />Opportunities.<br />Stay Protected.</h1>
          <p className="hero-description">
            Your intelligent safety companion for detecting fraudulent job
            offers, verifying suspicious links and discovering better career opportunities.
          </p>
          <div className="hero-actions">
            <PrimaryButton to="/register">Start Protecting Yourself <ArrowRight size={17} /></PrimaryButton>
            <PrimaryButton href="#features" variant="text">Explore Features <ArrowDownRight size={17} /></PrimaryButton>
          </div>
          <div className="hero-assurance">
            <span className="assurance-icon"><LockKeyhole size={15} /></span>
            Built around safer, more informed career decisions
          </div>
        </motion.div>

        <motion.div
          className="hero-visual"
          initial={{ opacity: 0, scale: 0.96, x: 14 }}
          animate={{ opacity: 1, scale: 1, x: 0 }}
          transition={{ duration: 0.75, delay: 0.1 }}
          aria-label="Illustration of the ScamShield career safety workspace"
          role="img"
        >
          <div className="visual-orbit orbit-one" />
          <div className="visual-orbit orbit-two" />
          <div className="visual-card">
            <div className="visual-card-top">
              <div className="window-dots"><i /><i /><i /></div>
              <span className="workspace-label">YOUR SAFETY WORKSPACE</span>
              <span className="workspace-lock"><LockKeyhole size={13} /></span>
            </div>
            <div className="visual-welcome">
              <span className="mini-shield"><Shield size={20} /></span>
              <div><strong>Move forward with confidence</strong><small>One thoughtful check at a time</small></div>
            </div>
            <div className="visual-tools">
              <div className="tool-row">
                <span className="tool-icon blue"><FileSearch2 size={17} /></span>
                <span><strong>Review a message</strong><small>Job offers, emails &amp; more</small></span>
                <ArrowRight size={15} className="tool-arrow" />
              </div>
              <div className="tool-row">
                <span className="tool-icon cyan"><Link2 size={17} /></span>
                <span><strong>Check a link</strong><small>Look for phishing indicators</small></span>
                <ArrowRight size={15} className="tool-arrow" />
              </div>
              <div className="tool-row">
                <span className="tool-icon violet"><BriefcaseBusiness size={17} /></span>
                <span><strong>Explore your next step</strong><small>Career tools in one place</small></span>
                <ArrowRight size={15} className="tool-arrow" />
              </div>
            </div>
            <div className="visual-footer">
              <span><Fingerprint size={14} /> Your choices stay yours</span>
              <span className="visual-ready"><Check size={13} /> Ready when you are</span>
            </div>
          </div>
          <div className="floating-sparkle"><Sparkles size={16} /></div>
          <div className="visual-caption">Thoughtful tools. Clearer next steps.</div>
        </motion.div>
      </div>
      <a className="scroll-cue" href="#features"><span /> Scroll to explore</a>
    </section>
  );
}
