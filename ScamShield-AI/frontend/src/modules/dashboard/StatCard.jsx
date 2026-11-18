import { motion } from "framer-motion";

export default function StatCard({ label, value, icon: Icon, tone }) {
  return (
    <motion.article
      className="dashboard-stat-card"
      whileHover={{ y: -3 }}
      transition={{ duration: 0.18 }}
    >
      <span className={`dashboard-stat-icon tone-${tone}`}><Icon size={18} /></span>
      <span className="dashboard-stat-label">{label}</span>
      <strong className="dashboard-stat-value">{value}</strong>
      <span className="dashboard-stat-caption">
        {value === "Unavailable" ? "Not available yet" : "Your total"}
      </span>
    </motion.article>
  );
}
