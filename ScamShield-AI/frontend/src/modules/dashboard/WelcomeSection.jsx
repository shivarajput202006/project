import { motion } from "framer-motion";
import { ShieldCheck } from "lucide-react";

export default function WelcomeSection({ user }) {
  const firstName = user?.fullName?.trim().split(/\s+/)[0] || "there";

  return (
    <motion.section
      className="dashboard-welcome"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
    >
      <div>
        <span className="dashboard-eyebrow">YOUR CAREER SAFETY OVERVIEW</span>
        <h2>Welcome back, {firstName}!</h2>
        <p>Your career safety overview.</p>
      </div>
      <div className="dashboard-welcome-badge" aria-hidden="true"><ShieldCheck size={24} /></div>
    </motion.section>
  );
}
