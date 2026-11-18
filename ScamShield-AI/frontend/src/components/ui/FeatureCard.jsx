import { motion } from "framer-motion";

export default function FeatureCard({ icon: Icon, title, description, index }) {
  return (
    <motion.article
      className="feature-card glass-card"
      initial={{ opacity: 0, y: 22 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.2 }}
      transition={{ duration: 0.45, delay: index * 0.07 }}
    >
      <span className="feature-icon"><Icon size={22} strokeWidth={1.8} /></span>
      <h3>{title}</h3>
      <p>{description}</p>
    </motion.article>
  );
}
